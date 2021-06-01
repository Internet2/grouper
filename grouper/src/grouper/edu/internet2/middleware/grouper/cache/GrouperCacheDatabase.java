package edu.internet2.middleware.grouper.cache;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import net.sf.ehcache.Cache;

/**
 * use the database to clear caches in jvms
 * @author mchyzer
 *
 */
public class GrouperCacheDatabase {

  //  CREATE TABLE grouper_cache_overall
  //  (
  //      overall_cache INTEGER NOT NULL,
  //      nanos_since_1970 BIGINT NOT NULL,
  //      PRIMARY KEY (overall_cache)
  //  );
  //
  //  CREATE TABLE grouper_cache_instance
  //  (
  //      cache_name VARCHAR(400) NOT NULL,
  //      nanos_since_1970 BIGINT NOT NULL,
  //      PRIMARY KEY (cache_name)
  //  );

  //  ############################################
  //  ## Grouper cache database
  //  ############################################
  //
  //  # If we should run a thread to check the database to see if a cache changed in another JVM
  //  # {valueType: "boolean", defaultValue: "true"}
  //  grouper.cache.database.use = true
  //
  //  # How much time to sleep between checks in seconds
  //  # {valueType: "integer", defaultValue: "10"}
  //  grouper.cache.database.checkIncrementalAfterSeconds = 10
  //
  //  # How much time in between full checks (select all from cache instance table)
  //  # {valueType: "integer", defaultValue: "3600"}
  //  grouper.cache.database.checkFullAfterSeconds = 3600
  //
  //

  /**
   * millis since 1970 that last incremental checked
   */
  private static long lastIncrementalCheckedMillis = -1;

  /**
   * millis since 1970 that last full was checked
   */
  private static long lastFullCheckedMillis = -1;
  
  /**
   * nanos since 1970 that any key was changed
   */
  private static Long cacheOverallLastUpdatedNanos = null;
  
  /**
   * local copy of cache key to last updated nanos
   */
  private static Map<String, Long> cacheKeyToLastUpdatedNanos = new TreeMap<String, Long>();

  /**
   * keep track of which ehcache's are database clearable
   */
  private static Map<String, Boolean> ehcacheNameToDatabaseClearable = new HashMap<String, Boolean>();
  
  /**
   * register a cache for database clearable.  Note you cant register one that is already there
   * @param name
   */
  public static synchronized void ehcacheRegisterDatabaseClearableCache(String ehcacheName) {
    if (ehcacheNameToDatabaseClearable.containsKey(ehcacheName)) {
      throw new RuntimeException("Already registered cache: '" + ehcacheName + "'");
    }
    ehcacheNameToDatabaseClearable.put(ehcacheName, true);
  }

  /**
   * 
   */
  public static void customNotifyDatabaseOfChanges(String customCacheName) {

    if (customCacheName.startsWith("ehcache__") || customCacheName.startsWith("expirableCache__") || customCacheName.startsWith("custom__")) {
      throw new RuntimeException("Invalid cache name! '" + customCacheName + "'");
    }

    if (customDatabaseClearables.containsKey(customCacheName)) {
      String realCacheName = "custom__" + customCacheName;
      GrouperCacheDatabase.notifyDatabaseOfCacheUpdate(realCacheName);
    }
  }

  /**
   * 
   */
  public static void ehcacheNotifyDatabaseOfChanges(String ehcacheName) {
    Boolean databaseCacheable = ehcacheNameToDatabaseClearable.get(ehcacheName);
    if (databaseCacheable != null && databaseCacheable) {
      String realCacheName = "ehcache__" + ehcacheName;
      GrouperCacheDatabase.notifyDatabaseOfCacheUpdate(realCacheName);
    }
  }

  // we need to keep incrementing
  private static long lastNanos = -1;
  
  /**
   * @param cacheName name of cache to clear
   * @return true if cache
   */
  public static void notifyDatabaseOfCacheUpdate(String cacheName) {
    notifyDatabaseOfCacheUpdate(cacheName, true);
  }
  
  /**
   * @param cacheName name of cache to clear
   * @param updateLastUpdatedNanos false to allow the current jvm to also receive the notification
   * @return true if cache
   */
  public static synchronized void notifyDatabaseOfCacheUpdate(String cacheName, boolean updateLastUpdatedNanos) {
    for (int i=0;i<3;i++) {
      try {
        // do a try/catch since another JVM could be updating the same cache at the same time
        long nowNanos = System.currentTimeMillis();
        
        // convert to nanos
        nowNanos *= 1000000;
        // add some random
        nowNanos += Math.random()*1000000;
        
        // we dont want to try the same number
        if (nowNanos <= lastNanos) {
          nowNanos = lastNanos+1;
        }
        lastNanos = nowNanos;

        notifyDatabaseOfCacheUpdateHelper(cacheName, nowNanos, updateLastUpdatedNanos);
        notifyDatabaseOverallOfCacheUpdateHelper(nowNanos, updateLastUpdatedNanos);
        // we good
        return;
      } catch (Exception e) {
        if (i==2) {
          throw new RuntimeException("Problem with cache: '" + cacheName + "'", e);
        }
        // else ignore and try again
        LOG.debug("Problem with cache: '" + cacheName + "'", e);
      }
    }
  }

  /**
   * @param cacheName name of cache to clear
   * @param updateLastUpdatedNanos false to allow the current jvm to also receive the notification
   * @return true if cache
   */
  private static void notifyDatabaseOfCacheUpdateHelper(String cacheName, long nowNanos, boolean updateLastUpdatedNanos) {
    if (!cacheName.startsWith("ehcache__") && !cacheName.startsWith("expirableCache__") && !cacheName.startsWith("custom__")) {
      throw new RuntimeException("Invalid cache name! '" + cacheName + "'");
    }
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ? and nanos_since_1970 != ?")
      .addBindVar(nowNanos).addBindVar(cacheName).addBindVar(nowNanos).executeSql();

    if (rowsUpdated != 1) {
      
      if (rowsUpdated == 0) {
        rowsUpdated = new GcDbAccess()
            .sql("insert into grouper_cache_instance (cache_name, nanos_since_1970) values (?, ?)")
            .addBindVar(cacheName).addBindVar(nowNanos).executeSql();
        if (rowsUpdated != 1) {
          throw new RuntimeException("Why is rows inserted not 1??? " + rowsUpdated);
        }
      } else {
        throw new RuntimeException("Why is rows updated not 0 or 1???? " + rowsUpdated);
      }
    }
    
    if (updateLastUpdatedNanos) {
      // capture here so we dont thrash
      cacheKeyToLastUpdatedNanos.put(cacheName, nowNanos);
    }
  }

  /**
   * @param cacheName name of cache to clear
   * @param updateLastUpdatedNanos false to allow the current jvm to also receive the notification
   * @return true if cache
   */
  private static void notifyDatabaseOverallOfCacheUpdateHelper(long nowNanos, boolean updateLastUpdatedNanos) {
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0 and nanos_since_1970 != ?")
      .addBindVar(nowNanos).addBindVar(nowNanos).executeSql();

    if (rowsUpdated != 1) {
      
      if (rowsUpdated == 0) {
        rowsUpdated = new GcDbAccess()
            .sql("insert into grouper_cache_overall (overall_cache, nanos_since_1970) values (0, ?)")
            .addBindVar(nowNanos).executeSql();
        if (rowsUpdated != 1) {
          throw new RuntimeException("Why is rows inserted not 1??? " + rowsUpdated);
        }
      } else {
        throw new RuntimeException("Why is rows updated not 0 or 1???? " + rowsUpdated);
      }
    }
    
    if (updateLastUpdatedNanos) {
      // capture here so we dont thrash
      cacheOverallLastUpdatedNanos = nowNanos;
    }
  }

  /**
   * @param cacheNameWithPrefix name of cache to clear
   * @return true if cache
   */
  public static boolean clearCacheFromDatabase(String cacheNameWithPrefix) {
    
    // three options
    // 1. ehcache
    if (cacheNameWithPrefix.startsWith("ehcache__")) {
      cacheNameWithPrefix = GrouperUtil.stripPrefix(cacheNameWithPrefix, "ehcache__");
      Cache cache = EhcacheController.ehcacheController().getCache(cacheNameWithPrefix);
      if (cache == null) {
        return false;
      }
      cache.flush();
      return true;
    }
    
    // 2. ehcache
    if (cacheNameWithPrefix.startsWith("expirableCache__")) {
      cacheNameWithPrefix = GrouperUtil.stripPrefix(cacheNameWithPrefix, "expirableCache__");
      return ExpirableCache.clearCache(cacheNameWithPrefix);
    }
    
    // 3. custom
    if (cacheNameWithPrefix.startsWith("custom__")) {
      cacheNameWithPrefix = GrouperUtil.stripPrefix(cacheNameWithPrefix, "custom__");
      GrouperCacheDatabaseClear grouperCacheDatabaseClear = customDatabaseClearables.get(cacheNameWithPrefix);
      
      // look by prefix so caches can insert data for other caches
      if (grouperCacheDatabaseClear == null && cacheNameWithPrefix.contains("____")) {
        String cacheByPrefix = GrouperUtil.prefixOrSuffix(cacheNameWithPrefix, "____", true);
        grouperCacheDatabaseClear = customDatabaseClearables.get(cacheByPrefix);
      }
      if (grouperCacheDatabaseClear == null) {
        return false;
      }
      GrouperCacheDatabaseClearInput grouperCacheDatabaseClearInput = new GrouperCacheDatabaseClearInput();
      grouperCacheDatabaseClearInput.setCacheName(cacheNameWithPrefix);
      grouperCacheDatabaseClear.clear(grouperCacheDatabaseClearInput);
      return true;
    }

    throw new RuntimeException("Invalid cache name: '" + cacheNameWithPrefix + "'");
  }

  static long fullCountForTesting = 0;
  /**
   * @param forStartup is true if not affecting caches, just do a full pull
   */
  public static void retrieveFull(boolean forStartup) {
    
    fullCountForTesting++; 
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long nowNanos = System.nanoTime();
    
    try {
      debugMap.put("method", "retrieveFull");
      
      cacheOverallLastUpdatedNanos = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
      
      debugMap.put("startup", forStartup);
        
      List<Object[]> rows = new GcDbAccess().sql("select cache_name, nanos_since_1970 from grouper_cache_instance").selectList(Object[].class);
  
      debugMap.put("rows", GrouperUtil.length(rows));
  
      int cacheClears = 0;
      
      for (Object[] row : rows) {
        String dbCacheName = (String)row[0];
        long dbLastUpdatedNanos = ((BigDecimal)row[1]).longValue();
        
        if (forStartup) {
          cacheKeyToLastUpdatedNanos.put(dbCacheName, dbLastUpdatedNanos);
        } else {
  
          Long memoryLastUpdatedNanos = cacheKeyToLastUpdatedNanos.get(dbCacheName);
          if (memoryLastUpdatedNanos == null || memoryLastUpdatedNanos.longValue() != dbLastUpdatedNanos) {
            boolean result = clearCacheFromDatabase(dbCacheName);
            if (result) {
              debugMap.put(dbCacheName, "mismatchCleared");
            } else {
              debugMap.put(dbCacheName, "mismatchNotCleared");
            }
            cacheKeyToLastUpdatedNanos.put(dbCacheName, dbLastUpdatedNanos);
            cacheClears++;
          }
        }
      }
  
      debugMap.put("cacheClears", cacheClears);
    } finally {
      if (LOG.isDebugEnabled()) {
        long millis = (System.nanoTime() - nowNanos) / 1000000;
        debugMap.put("tookMillis", millis);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    lastFullCheckedMillis = System.currentTimeMillis();
  }
  
  /**
   * thread to check for cache updates
   */
  private static Thread grouperCacheDatabaseThread = null;
  
  
  
  private static void assignThread() {
    if (grouperCacheDatabaseThread == null) {
      grouperCacheDatabaseThread = new Thread(new Runnable() {

        @Override
        public void run() {
          
         while (true) {
            try {

              if (!shouldRun) {
                return;
              }

              if (cacheOverallLastUpdatedNanos == null) {
                
                retrieveFull(true);
                
              }
              
              int checkIncrementalAfterSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.cache.database.checkIncrementalAfterSeconds", 5);
              int checkFullAfterSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.cache.database.checkFullAfterSeconds", 3600);
              
              
              if (checkIncrementalAfterSeconds <=0) {
                checkIncrementalAfterSeconds = 10;
              }
              
              
              GrouperUtil.sleep(Math.min(checkIncrementalAfterSeconds, checkFullAfterSeconds) * 1000 + 30);

              if (!shouldRun) {
                return;
              }

              if ((System.currentTimeMillis() - lastFullCheckedMillis) > checkFullAfterSeconds*1000) {
                
                retrieveFull(false);
                
              } else if ((System.currentTimeMillis() - lastIncrementalCheckedMillis) > checkIncrementalAfterSeconds*1000) {
    
                retrieveIncremental();
                
              }

            } catch (Exception e) {

              if (!shouldRun) {
                return;
              }

              LOG.error("Error in cache database thread", e);
              // dont throw
              
              // no rapid fire errors
              GrouperUtil.sleep(60000);
            }
            
          }
        }
        
      });
      grouperCacheDatabaseThread.setDaemon(true);

    }
  }

  /**
   * 
   */
  private static boolean shouldRun = false; 
  
  public static void stopThread() {
    shouldRun = false;

    if (grouperCacheDatabaseThread == null) {
      return;
    }

    try {
      try {
        grouperCacheDatabaseThread.interrupt();
      } catch (Exception e) {
        LOG.debug("error interrupting thread", e);
      }
      grouperCacheDatabaseThread.join(20000);
      grouperCacheDatabaseThread = null;
    } catch (Exception e) {
      //ignore
      LOG.warn("error stopping thread", e);
    }
  }

  public static void startThreadIfNotStarted() {
    if (grouperCacheDatabaseThread == null || !grouperCacheDatabaseThread.isAlive()) {
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.cache.database.use", true)) {
        if (grouperCacheDatabaseThread == null) {
          assignThread();
        }
        shouldRun = true;
        grouperCacheDatabaseThread.start();
      }
    }
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperCacheDatabase.class);

  /**
   * if the cache should be able to clear across JVMs through the database
   */
  private static Map<String, GrouperCacheDatabaseClear> customDatabaseClearables
    = Collections.synchronizedMap(new HashMap<String, GrouperCacheDatabaseClear>());
  
  /**
   * register custom cache clearable
   * @param cacheName
   * @param grouperCacheDatabaseClear
   */
  public static void customRegisterDatabaseClearable(String cacheName, GrouperCacheDatabaseClear grouperCacheDatabaseClear) {
    if (cacheName.startsWith("ehcache__") || cacheName.startsWith("expirableCache__") || cacheName.startsWith("custom__")) {
      throw new RuntimeException("Invalid cache name! '" + cacheName + "'");
    }
    if (grouperCacheDatabaseClear == null) {
      throw new RuntimeException("grouperCacheDatabaseClear cant be null");
    }
    if (customDatabaseClearables.containsKey(cacheName)) {
      throw new RuntimeException("cacheName exists: '" + cacheName + "'");
    }
    customDatabaseClearables.put(cacheName, grouperCacheDatabaseClear);
  }
  
  static long incrementalCountForTesting = 0;

  /**
   */
  public static void retrieveIncremental() {
    
    incrementalCountForTesting++;
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long nowNanos = System.nanoTime();
    
    try {
      debugMap.put("method", "retrieveIncremental");
      
      Long cacheOverallLastUpdatedNanosFromDb = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
      
      debugMap.put("cacheOverallLastUpdatedNanosFromDb", cacheOverallLastUpdatedNanosFromDb);
  
      if (GrouperUtil.equals(cacheOverallLastUpdatedNanos, cacheOverallLastUpdatedNanosFromDb)) {
        debugMap.put("cacheChange", false);
        return;
      }
  
      // if we were null, just do a full
      if (cacheOverallLastUpdatedNanos == null) {
        debugMap.put("switchingToFull", true);
        retrieveFull(false);
        return;
      }
      
      debugMap.put("cacheChange", true);

      // subtract a second for race conditions and clock problems
      List<Object[]> rows = new GcDbAccess().sql("select cache_name, nanos_since_1970 from grouper_cache_instance where nanos_since_1970 > ?")
          .addBindVar(cacheOverallLastUpdatedNanos-(10 * 1000 * 1000000L)).selectList(Object[].class);
    
      debugMap.put("rows", GrouperUtil.length(rows));
    
      int cacheClears = 0;
      
      for (Object[] row : rows) {
        String dbCacheName = (String)row[0];
        long dbLastUpdatedNanos = ((BigDecimal)row[1]).longValue();
        
        Long memoryLastUpdatedNanos = cacheKeyToLastUpdatedNanos.get(dbCacheName);
        if (memoryLastUpdatedNanos == null || memoryLastUpdatedNanos.longValue() != dbLastUpdatedNanos) {
          boolean result = clearCacheFromDatabase(dbCacheName);
          if (result) {
            debugMap.put(dbCacheName, "mismatchCleared");
          } else {
            debugMap.put(dbCacheName, "mismatchNotCleared");
          }
          cacheKeyToLastUpdatedNanos.put(dbCacheName, dbLastUpdatedNanos);
          cacheClears++;
        }
      }
    
      cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosFromDb;
      
      debugMap.put("cacheClears", cacheClears);
  
    } finally {
      if (LOG.isDebugEnabled()) {

        long millis = (System.nanoTime() - nowNanos) / 1000000;
        debugMap.put("tookMillis", millis);

        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    lastIncrementalCheckedMillis = System.currentTimeMillis();

  }
}
