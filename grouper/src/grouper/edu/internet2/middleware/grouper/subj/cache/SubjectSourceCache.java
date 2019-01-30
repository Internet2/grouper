/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectImpl;


/**
 *
 */
public class SubjectSourceCache {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SubjectSourceCache.class);

  /**
   * 
   */
  public static synchronized void clearCache() {
    statsCurrent = new SubjectSourceCacheStat();
    subjectCache.clear();
    subjectKeyCache.clear();
  }
  
  /**
   * 
   */
  public SubjectSourceCache() {
  }

  /**
   * sweep cache last run
   */
  static long sweepCacheToResolveItemsThatNeedItLastRun = -1;
  
  /**
   * look through cache to look for items that need to be refreshed
   */
  static synchronized void sweepCacheToResolveItemsThatNeedIt() {
    
    if (!cacheEnabled() ) {
      return;
    }

    int storeToStorageAfterThisManySeconds = SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.storeToStorageAfterThisManySeconds", 1800);
    if (((System.currentTimeMillis() - sweepCacheToResolveItemsThatNeedItLastRun) / 1000) < storeToStorageAfterThisManySeconds) {
      return;
    }

    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "sweepCacheToResolveItemsThatNeedIt");
      startNanos = System.nanoTime();
    }

    int timeToLiveNotFoundSeconds = SubjectSourceCacheItem.timeToLiveNotFoundSeconds();
    int timeToLiveSeconds = SubjectSourceCacheItem.timeToLiveSeconds();
    int timeToLiveSecondsPercentageToResolveSubjectsIfNecessary = SubjectSourceCacheItem.timeToLiveSecondsPercentageToResolveSubjectsIfNecessary();
    int timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary = SubjectSourceCacheItem.timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary();
    int minUseInCycleToAutoRefresh = SubjectSourceCacheItem.minUseInCycleToAutoRefresh();
    
    // see how often we need to run
    {
      int checkForTimeToLiveSeconds = (int)(timeToLiveSeconds * (timeToLiveSecondsPercentageToResolveSubjectsIfNecessary / 100D));
      int checkForTimeToLiveNotFoundSeconds = (int)(timeToLiveNotFoundSeconds * (timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary / 100D));
      int checkForTimeSeconds = Math.min(checkForTimeToLiveSeconds, checkForTimeToLiveNotFoundSeconds);
      if (((System.currentTimeMillis() - sweepCacheToResolveItemsThatNeedItLastRun) / 1000) < checkForTimeSeconds) {
        return;
      }

      sweepCacheToResolveItemsThatNeedItLastRun = System.currentTimeMillis();
    }
    
    try {

      int itemsNeedResolved = 0;
      int itemsDontNeedResolved = 0;
      int itemsWithProblems = 0;
      
      Set<MultiKey> entriesToResolve = new HashSet<MultiKey>();
      
      // sweep cache for not recently used expired found items
      for (MultiKey subjectKeyCacheKey : subjectKeyCache.keySet()) {
        
        MultiKey subjectCacheKey = new MultiKey(subjectKeyCacheKey.getKey(0), "id", subjectKeyCacheKey.getKey(1));

        SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(subjectCacheKey);
        if (subjectSourceCacheItem == null) {
          itemsWithProblems++;
          continue;
        }
        
        //if not expired, then see if recently used
        if (subjectSourceCacheItem.needsToBeResolvedHelper(timeToLiveSeconds, 
            timeToLiveSecondsPercentageToResolveSubjectsIfNecessary, timeToLiveNotFoundSeconds, 
            timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary, minUseInCycleToAutoRefresh)) {
                
          itemsNeedResolved++;
          entriesToResolve.add(subjectKeyCacheKey);
        } else {
          itemsDontNeedResolved++;
        }
      }

      //what do we need to do?
      if (LOG.isDebugEnabled()) {

        debugMap.put("itemsNeedResolved", itemsNeedResolved);
        debugMap.put("itemsDontNeedResolved", itemsDontNeedResolved);
        debugMap.put("itemsWithProblems", itemsWithProblems);
                
      }

      // organize subjects by sourceId
      Map<String, Collection<String>> sourceToSubjectIdsMap = new HashMap<String, Collection<String>>();
      
      for (MultiKey multiKey : entriesToResolve) {
        String sourceId = (String)multiKey.getKey(0);
        String subjectId = (String)multiKey.getKey(1);
        Collection<String> subjectIds = sourceToSubjectIdsMap.get(sourceId);
        
        if (subjectIds == null) {
          subjectIds = new HashSet<String>();
          sourceToSubjectIdsMap.put(sourceId, subjectIds);
        }
        
        subjectIds.add(subjectId);
      }
      
      // resolve the sources
      for (String sourceId : sourceToSubjectIdsMap.keySet()) {
        Collection<String> subjectIds = sourceToSubjectIdsMap.get(sourceId);
        Map<String, Subject> subjectIdToSubject = SourceManager.getInstance().getSource(sourceId).getSubjectsByIds(subjectIds);
        
        // loop through the subjects queried and update the cache
        for (String subjectId : subjectIds) {
          
          Subject subject = subjectIdToSubject.get(subjectId);
          
          updateSubjectInCache(subject, null, sourceId, true, subjectId, false, true, false);
          
        }
      }

      
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    
  }
  
  /**
   * millis since 1970 that the last sweep has run
   */
  static long sweepCacheToDeleteLastRun = -1;
  
  /**
   * if cache initted
   */
  private static boolean cacheInitted = false;
  
  /**
   * init the cache if not initted
   */
  static void initCacheIfNotInitted() {
    
    if (cacheInitted) {
      return;
    }
    synchronized (SubjectSourceCache.class) {
      if (cacheInitted) {
        GrouperUtil.sleep(100);
        return;
      }
      cacheInitted = true;
    }

    Thread thread = new Thread(new Runnable() {
      
      public void run() {
        SubjectSourceCache.subjectSourceCacheThread();
      }
    });
    thread.setDaemon(true);
    thread.start();
    GrouperUtil.sleep(100);

  }

  /**
   * 
   */
  public static void subjectSourceCacheThread() {
    try {
      try {
        readCacheFromStorageOnStartup();
      } catch (Exception e) {
        LOG.error("Error reading subject cache from storage on startup", e);
      }
      while(true) {
        if (cacheEnabled()) {
          
          try {
            
            try {
              rebuildCacheToMakeConsistent();
            } catch (Throwable t) {
              LOG.error("Error in rebuildCacheToMakeConsistent", t);
            }

            try {
              sweepCacheToResolveItemsThatNeedIt();
            } catch (Throwable t) {
              LOG.error("Error in sweepCacheToResolveItemsThatNeedIt", t);
            }

            try {
              sweepCacheToDeleteOldItemsIfNeeded();
            } catch (Throwable t) {
              LOG.error("Error in sweepCacheToDeleteOldItemsIfNeeded", t);
            }

            try {
              writeCacheToStorage();
            } catch (Throwable t) {
              LOG.error("Error in writeCacheToStorage", t);
            }
                        
            try {
              logStats();
            } catch (Throwable t) {
              LOG.error("Error in logStats", t);
            }
            
            
          } catch (Throwable e) {
            LOG.error("Error in loop of cache daemon", e);
          }
        }
        GrouperUtil.sleep(SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.sleepSecondsInBetweenThreadRuns", 120) * 1000);
      }
    } catch(Throwable t) {
      LOG.error("Error in outer loop of subject source cache", t);
    }
  }
  
  /**
   * read cache from storage on startup
   */
  static synchronized void readCacheFromStorageOnStartup() {
    
    if (!cacheEnabled() ) {
      return;
    }

    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "readCacheFromStorageOnStartup");
      startNanos = System.nanoTime();
    }
    
    try {

      SubjectSourceSerializer subjectSourceSerializer = retrieveCacheSerializer(debugMap);

      if (subjectSourceSerializer == null) {
        return;
      }
      // see what the timeout is, has to be less than when the cache was written
      final int timeToLiveSeconds = SubjectSourceCacheItem.timeToLiveSeconds();
      if (LOG.isDebugEnabled()) {
        debugMap.put("timeToLiveSeconds", timeToLiveSeconds);
      }

      final long newerThanMillis = System.currentTimeMillis() - (1000*SubjectSourceCacheItem.timeToLiveSeconds());

      if (LOG.isDebugEnabled()) {
        debugMap.put("newerThan", new Date(newerThanMillis));
      }

      SubjectSourceCacheBean subjectSourceCacheBean = subjectSourceSerializer
          .retrieveLatestSubjectCache(newerThanMillis, debugMap);
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("found", subjectSourceCacheBean != null);
      }

      if (subjectSourceCacheBean != null) {

        if (LOG.isDebugEnabled()) {
          debugMap.put("retrieveCacheTook", ((System.nanoTime() - startNanos) / 1000000) + "millis");
        }
        
        long millisCacheLastStored = subjectSourceCacheBean.getCacheLastStored();
        
        if (((System.currentTimeMillis() - millisCacheLastStored) / 1000) >  timeToLiveSeconds) {

          if (LOG.isDebugEnabled()) {
            debugMap.put("tooOld", true);
          }
          return;
        }

        if (LOG.isDebugEnabled()) {
          debugMap.put("numberOfItems", GrouperUtil.length(subjectSourceCacheBean.getSubjectSourceCacheItems()));
        }
        
        // put things in cache
        for (SubjectSourceCacheItem subjectSourceCacheItem : GrouperUtil.nonNull(subjectSourceCacheBean.getSubjectSourceCacheItems())) {
          if (subjectSourceCacheItem != null) {
            updateSubjectInCache(subjectSourceCacheItem.getSubject(), subjectSourceCacheItem, null, true, null, false, false, true);
          }
        }
        
      }
      
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000000) + "millis");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }

  /**
   * when was cache last written to storage
   */
  static long writeCacheToStorageLastRun = System.currentTimeMillis();
  
  /**
   * read cache from storage on startup
   */
  static synchronized void writeCacheToStorage() {
    
    if (!cacheEnabled() ) {
      return;
    }

    int storeToStorageAfterThisManySeconds = SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.storeToStorageAfterThisManySeconds", 1800);
    if (((System.currentTimeMillis() - writeCacheToStorageLastRun) / 1000) < storeToStorageAfterThisManySeconds) {
      return;
    }
    
    writeCacheToStorageLastRun = System.currentTimeMillis();
    
    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "writeCacheToStorage");
      startNanos = System.nanoTime();
    }
    
    try {

      SubjectSourceSerializer subjectSourceSerializer = retrieveCacheSerializer(debugMap);

      if (subjectSourceSerializer == null) {
        return;
      }
      SubjectSourceCacheBean subjectSourceCacheBean = new SubjectSourceCacheBean();
      subjectSourceCacheBean.setCacheLastStored(System.currentTimeMillis());
      
      List<SubjectSourceCacheItem> subjectSourceCacheItems = new ArrayList<SubjectSourceCacheItem>();
      subjectSourceCacheBean.setSubjectSourceCacheItems(subjectSourceCacheItems);
      
      for (MultiKey subjectKeyCacheKey : subjectKeyCache.keySet()) {
        
        MultiKey multiKey = new MultiKey(subjectKeyCacheKey.getKey(0), "id", subjectKeyCacheKey.getKey(1));
        SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);
        
        if (subjectSourceCacheItem != null) {
          subjectSourceCacheItems.add(subjectSourceCacheItem);
        }
        
      }
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("itemsToStore", GrouperUtil.length(subjectSourceCacheItems));
        debugMap.put("buildingITemsTookMicros", ((System.nanoTime() - startNanos) / 1000) + "micros");
      }

      if (GrouperUtil.length(subjectSourceCacheItems) == 0) {
        return;
      }

      subjectSourceSerializer.storeSubjectCache(subjectSourceCacheBean, debugMap);
      
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000000) + "millis");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    deleteOldStorageFiles();

  }

  /**
   * current stats
   */
  static SubjectSourceCacheStat statsCurrent = new SubjectSourceCacheStat();

  /**
   * stats from yesterday
   */
  static SubjectSourceCacheStat statsYesterday = null;

  /**
   * when was stats last written
   */
  static long logStatsLastRun = System.currentTimeMillis();
  
  /**
   * log stats seconds
   * @return true if should write stats
   */
  static int logStatsSeconds() {
    return SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.logStatsSeconds", 86400);
  }
  
  /**
   * write stats if needed
   */
  static synchronized void logStats() {
    
    if (!cacheEnabled() ) {
      return;
    }

    // make sure we are on the right day
    String today = new SimpleDateFormat("yyyy_MM_dd").format(new Date());

    if (statsCurrent.getDate() == null) {
      statsCurrent.setDate(today);
    } else {
      if (!StringUtils.equals(today, statsCurrent.getDate())) {
        statsYesterday = statsCurrent;
        statsCurrent = new SubjectSourceCacheStat();
        statsCurrent.setDate(today);
      }
    }

    int writeStatsAfterThisManySeconds = logStatsSeconds();
    if (writeStatsAfterThisManySeconds < 0) {
      return;
    }

    if (((System.currentTimeMillis() - logStatsLastRun) / 1000) < writeStatsAfterThisManySeconds) {
      return;
    }
    
    logStatsLastRun = System.currentTimeMillis();
    
    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "logStats");
      startNanos = System.nanoTime();
    }
    
    try {

      statsCurrent.setCacheSizeLookups(subjectCache.size());
      statsCurrent.setCacheSizeSubjects(subjectKeyCache.size());

      int cacheSizeResolved = 0;
      int cacheSizeUnresolved = 0;
      int cacheHitsSinceLastRetrieve = 0;
      int cacheHitsTotalOfItemsInCache = 0;
      int cacheRefreshesTotalOfItemsInCache = 0;
      
      int itemsWith10AccessSinceLastRefresh = 0;
      int itemsWith10AccessTotal = 0;
      int itemsWith10Refresh = 0;
      int itemsWith1AccessSinceLastRefresh = 0;
      int itemsWith1AccessTotal = 0;
      int itemsWith1Refresh = 0;
      int itemsWith20AccessSinceLastRefresh = 0;
      int itemsWith20AccessTotal = 0;
      int itemsWith20Refresh = 0;
      int itemsWith3AccessSinceLastRefresh = 0;
      int itemsWith3AccessTotal = 0;
      int itemsWith3Refresh = 0;
      int itemsWith50AccessTotal = 0;
      int itemsWith50Refresh = 0;
      int itemsWithNoAccessSinceLastRefresh = 0;
      
      for (MultiKey subjectKeyCacheKey : subjectKeyCache.keySet()) {
        
        MultiKey multiKey = new MultiKey(subjectKeyCacheKey.getKey(0), "id", subjectKeyCacheKey.getKey(1));
        SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);
        if (subjectSourceCacheItem != null) {
          
          if (subjectSourceCacheItem.getSubject() == null) {
            cacheSizeUnresolved++;
          } else {
            cacheSizeResolved++;
          }
          
          cacheHitsTotalOfItemsInCache += subjectSourceCacheItem.getNumberOfTimesAccessed();
          
          cacheHitsSinceLastRetrieve += subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved();
          
          cacheRefreshesTotalOfItemsInCache += subjectSourceCacheItem.getNumberOfTimesRetrieved();
          
          if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() == 0 ) {
            itemsWithNoAccessSinceLastRefresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() >= 1 ) {
            itemsWith1AccessSinceLastRefresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() >= 3 ) {
            itemsWith3AccessSinceLastRefresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() >= 10 ) {
            itemsWith10AccessSinceLastRefresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() >= 20 ) {
            itemsWith20AccessSinceLastRefresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessed() >= 1 ) {
            itemsWith1AccessTotal++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessed() >= 3 ) {
            itemsWith3AccessTotal++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessed() >= 10 ) {
            itemsWith10AccessTotal++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessed() >= 20 ) {
            itemsWith20AccessTotal++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesAccessed() >= 50 ) {
            itemsWith50AccessTotal++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesRetrieved() >= 1 ) {
            itemsWith1Refresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesRetrieved() >= 3 ) {
            itemsWith3Refresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesRetrieved() >= 10 ) {
            itemsWith10Refresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesRetrieved() >= 20 ) {
            itemsWith20Refresh++;
          }
          if (subjectSourceCacheItem.getNumberOfTimesRetrieved() >= 50 ) {
            itemsWith50Refresh++;
          }
        }
      }

      statsCurrent.setCacheHitsSinceLastRetrieve(cacheHitsSinceLastRetrieve);
      statsCurrent.setCacheHitsTotalOfItemsInCache(cacheHitsTotalOfItemsInCache);
      statsCurrent.setCacheRefreshesTotalOfItemsInCache(cacheRefreshesTotalOfItemsInCache);
      statsCurrent.setCacheSizeSubjectsResolved(cacheSizeResolved);
      statsCurrent.setCacheSizeSubjectsUnresolved(cacheSizeUnresolved);
      
      statsCurrent.setItemsWith10AccessSinceLastRefresh(itemsWith10AccessSinceLastRefresh);
      statsCurrent.setItemsWith10AccessTotal(itemsWith10AccessTotal);
      statsCurrent.setItemsWith10Refresh(itemsWith10Refresh);
      statsCurrent.setItemsWith1AccessSinceLastRefresh(itemsWith1AccessSinceLastRefresh);
      statsCurrent.setItemsWith1AccessTotal(itemsWith1AccessTotal);
      statsCurrent.setItemsWith1Refresh(itemsWith1Refresh);
      statsCurrent.setItemsWith20AccessSinceLastRefresh(itemsWith20AccessSinceLastRefresh);
      statsCurrent.setItemsWith20AccessTotal(itemsWith20AccessTotal);
      statsCurrent.setItemsWith20Refresh(itemsWith20Refresh);
      statsCurrent.setItemsWith3AccessSinceLastRefresh(itemsWith3AccessSinceLastRefresh);
      statsCurrent.setItemsWith3AccessTotal(itemsWith3AccessTotal);
      statsCurrent.setItemsWith3Refresh(itemsWith3Refresh);
      statsCurrent.setItemsWith50AccessTotal(itemsWith50AccessTotal);
      statsCurrent.setItemsWith50Refresh(itemsWith50Refresh);
      statsCurrent.setItemsWithNoAccessSinceLastRefresh(itemsWithNoAccessSinceLastRefresh);
      
      LOG.warn((statsYesterday != null ? (statsYesterday.statsLine() + "\n") : "") + statsCurrent.statsLine());
      
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }


  /**
   * @param debugMap
   * @return SubjectSourceSerializer
   */
  public static SubjectSourceSerializer retrieveCacheSerializer(
      Map<String, Object> debugMap) {
    String cacheSerializerClassName = SubjectConfig.retrieveConfig().propertyValueString(
        "subject.cache.serializer");
    
    if (LOG.isDebugEnabled()) {
      debugMap.put("cacheSerializerClassName", cacheSerializerClassName);
    }

    if (StringUtils.isBlank(cacheSerializerClassName)) {
      return null;
    }
   
    Class<SubjectSourceSerializer> cacheSerializerClass = GrouperUtil.forName(cacheSerializerClassName);
    
    SubjectSourceSerializer subjectSourceSerializer = GrouperUtil.newInstance(cacheSerializerClass);
    return subjectSourceSerializer;
  }

  /**
   * last rebuild of cache to make consistent
   */
  private static long rebuildCacheToMakeConsistentLastRun = -1;

  /**
   * rebuild cache once a day
   */
  static synchronized void rebuildCacheToMakeConsistent() {

    if (!cacheEnabled() ) {
      return;
    }

    int rebuildCacheAfterThisManySeconds = SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.rebuildCacheAfterThisManySeconds", 86400);
    if (((System.currentTimeMillis() - rebuildCacheToMakeConsistentLastRun) / 1000) < rebuildCacheAfterThisManySeconds) {
      return;
    }
    
    rebuildCacheToMakeConsistentLastRun = System.currentTimeMillis();
    
    Map<MultiKey, SubjectSourceCacheItem> subjectCacheTemp = new ConcurrentHashMap<MultiKey, SubjectSourceCacheItem>();

    Map<MultiKey, Set<MultiKey>> subjectKeyCacheTemp = new ConcurrentHashMap<MultiKey, Set<MultiKey>>();

    for (Map.Entry<MultiKey, Set<MultiKey>> entry: subjectKeyCacheTemp.entrySet()) {
      
      final MultiKey multikey = entry.getKey();
      if (multikey != null) {
        Set<MultiKey> multikeys = entry.getValue();
        if (GrouperUtil.length(multikeys) > 0) {
          
          //make sure nothing null etc
          MultiKey subjectCacheKey = multikeys.iterator().next();
          if (subjectCacheKey != null) {
            
            SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(subjectCacheKey);
            
            if (subjectSourceCacheItem != null) {
              
              Set<MultiKey> subjectCacheKeys = ConcurrentHashMap.newKeySet();

              for (MultiKey currentSubjectCacheKey : multikeys) {
                
                SubjectSourceCacheItem currentSubjectSourceCacheItem = subjectCache.get(subjectCacheKey);
                
                if (currentSubjectSourceCacheItem != null) {
                  
                  subjectCacheKeys.add(currentSubjectCacheKey);
                  subjectCacheTemp.put(currentSubjectCacheKey, currentSubjectSourceCacheItem);
                  
                }
              }
              
              //we are ready to rebuild
              subjectKeyCacheTemp.put(multikey, subjectCacheKeys);

            }
          }
        }
      }
      
    }
    
    subjectKeyCache = subjectKeyCacheTemp;
    subjectCache = subjectCacheTemp;
    
  }
  
  /**
   * if this hasnt run in a minute, and the cache is 90% full, then go through and delete old items
   */
  static synchronized void sweepCacheToDeleteOldItemsIfNeeded() {
    
    if (!cacheEnabled() ) {
      return;
    }

    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "sweepCacheToDeleteOldItemsIfNeeded");
      startNanos = System.nanoTime();
    }

    int dontSweepCacheForDeletesMoreOftenThatThisManySeconds = SubjectConfig.retrieveConfig().propertyValueInt(
        "subject.cache.dontSweepCacheForDeletesMoreOftenThatThisManySeconds", 59);
    
    int dontSweepCacheForDeletesUnlessCacheIsThisPercentFull = SubjectConfig.retrieveConfig().propertyValueInt(
        "subject.cache.dontSweepCacheForDeletesUnlessCacheIsThisPercentFull", 90);

    //lets see if we need to run the sweep
    long lastSweepSecondsAgo = (System.currentTimeMillis() - sweepCacheToDeleteLastRun) / 1000;
    if (lastSweepSecondsAgo < dontSweepCacheForDeletesMoreOftenThatThisManySeconds) {
      return;
    }
    sweepCacheToDeleteLastRun = System.currentTimeMillis();
    
    try {

      int maxCacheSize = maxElementsInMemory();
      int currentCacheSize = subjectKeyCache.size();
      
      int cachePercentFull = (int)Math.round((((double)currentCacheSize/(double)maxCacheSize) * 100.0D));
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("maxCacheSize", maxCacheSize);
        debugMap.put("currentCacheSize", currentCacheSize);
        debugMap.put("cachePercentFull", cachePercentFull);
      }
      
      if (cachePercentFull < dontSweepCacheForDeletesUnlessCacheIsThisPercentFull) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("notSweepingSinceNotPercentFull", dontSweepCacheForDeletesUnlessCacheIsThisPercentFull);
        }
        return;
      }

      int expiredItems = 0;
      int unExpiredItems = 0;
      int recentlyUsed = 0;
      
      int minUseInCycleToAutoRefresh = SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.minUseInCycleToAutoRefresh", 1);
      int minUseLevel2ToNotDelete = SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.minUseLevel2ToNotDelete", 5);
      int minUseLevel3ToNotDelete = SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.minUseLevel3ToNotDelete", 20);
      
      int timeToLiveSeconds = SubjectSourceCacheItem.timeToLiveSeconds();
      int timeToLiveNotFoundSeconds = SubjectSourceCacheItem.timeToLiveNotFoundSeconds();
      
      Set<Map.Entry<MultiKey,Set<MultiKey>>> entriesToDelete = new HashSet<Map.Entry<MultiKey,Set<MultiKey>>>();
      Set<Map.Entry<MultiKey,Set<MultiKey>>> entriesNotRecentlyUsedLevel1 = new HashSet<Map.Entry<MultiKey,Set<MultiKey>>>();
      Set<Map.Entry<MultiKey,Set<MultiKey>>> entriesNotRecentlyUsedLevel2 = new HashSet<Map.Entry<MultiKey,Set<MultiKey>>>();
      Set<Map.Entry<MultiKey,Set<MultiKey>>> entriesNotRecentlyUsedLevel3 = new HashSet<Map.Entry<MultiKey,Set<MultiKey>>>();
      
      // sweep cache for not recently used expired found items
      for (Map.Entry<MultiKey,Set<MultiKey>> entry : subjectKeyCache.entrySet()) {
        
        //if things inconsistent, just delete
        boolean deleteEntry = false;
        
        Set<MultiKey> subjectCacheKeys = entry.getValue();
        if (GrouperUtil.length(subjectCacheKeys) == 0) {
          deleteEntry = true;
        } else {
          //see if expired
          MultiKey subjectCacheKey = subjectCacheKeys.iterator().next();
          if (subjectCacheKey == null) {
            deleteEntry = true;
          } else {
            SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(subjectCacheKey);
            if (subjectSourceCacheItem == null) {
              deleteEntry = true;
            } else {
              
              //if not expired, then see if recently used
              if (!subjectSourceCacheItem.expiredHelper(timeToLiveSeconds, timeToLiveNotFoundSeconds)) {
                
                unExpiredItems++;
                
                // has been used
                if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() < minUseInCycleToAutoRefresh) {
                  entriesNotRecentlyUsedLevel1.add(entry);
                } else if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() < minUseLevel2ToNotDelete) {
                  entriesNotRecentlyUsedLevel2.add(entry);
                } else if (subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() < minUseLevel3ToNotDelete) {
                  entriesNotRecentlyUsedLevel3.add(entry);
                } else {
                  recentlyUsed++;
                }

                continue;
              } 
              deleteEntry = true;
              expiredItems++;
              
            }
          }
        }
        if (deleteEntry) {
          // lets delete
          entriesToDelete.add(entry);

        }
      }

      //what do we need to do?
      if (LOG.isDebugEnabled()) {

        debugMap.put("expiredItems", expiredItems);
        debugMap.put("unExpiredItems", unExpiredItems);
        debugMap.put("recentlyUsed", recentlyUsed);
        
        debugMap.put("entriesNotRecentlyUsedLevel1", entriesNotRecentlyUsedLevel1.size());
        debugMap.put("entriesNotRecentlyUsedLevel2", entriesNotRecentlyUsedLevel2.size());
        debugMap.put("entriesNotRecentlyUsedLevel3", entriesNotRecentlyUsedLevel3.size());
        
      }

      if ((((double)currentCacheSize - entriesToDelete.size())/(double)maxCacheSize) * 100.0D > dontSweepCacheForDeletesUnlessCacheIsThisPercentFull) {
        entriesToDelete.addAll(entriesNotRecentlyUsedLevel1);
      }
      if ((((double)currentCacheSize - entriesToDelete.size())/(double)maxCacheSize) * 100.0D > dontSweepCacheForDeletesUnlessCacheIsThisPercentFull) {
        entriesToDelete.addAll(entriesNotRecentlyUsedLevel2);
      }
      if ((((double)currentCacheSize - entriesToDelete.size())/(double)maxCacheSize) * 100.0D > dontSweepCacheForDeletesUnlessCacheIsThisPercentFull) {
        entriesToDelete.addAll(entriesNotRecentlyUsedLevel3);
      }
      
      if (LOG.isDebugEnabled()) {

        debugMap.put("entriesToDelete", entriesToDelete.size());

      }
      
      statsCurrent.cacheRemoveAdd(entriesToDelete.size());
      
      // remove entries from cache
      for (Map.Entry<MultiKey,Set<MultiKey>> entry : entriesToDelete) {
        subjectKeyCache.remove(entry.getKey());
        for (MultiKey subjectCacheKey : GrouperUtil.nonNull(entry.getValue()) ) {
          subjectCache.remove(subjectCacheKey);
        }
      }

      if (LOG.isDebugEnabled()) {

        debugMap.put("newSize", subjectKeyCache.size());

      }

      
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }

  /**
   * max items in memory
   * @return max
   */
  public static int maxElementsInMemory() {
    return SubjectConfig.retrieveConfig().propertyValueInt(
        "subject.cache.maxElementsInMemory", 10000);
  }
  
  
  /**
   * multikey is sourceId, label "id"|"identifier", value of id|identifier 
   */
  static Map<MultiKey, SubjectSourceCacheItem> subjectCache = new ConcurrentHashMap<MultiKey, SubjectSourceCacheItem>();

  /**
   * this is not for looking it, it is for managing the cache
   * multikey is sourceId, value of id|identifier, get the keys in the main subject cache
   */
  static Map<MultiKey, Set<MultiKey>> subjectKeyCache = new ConcurrentHashMap<MultiKey, Set<MultiKey>>();

  /**
   * 
   * @param subject
   * @param subjectSourceCacheItem 
   * @param sourceId 
   * @param isId
   * @param idOrIdentifier 
   * @param accessed 
   * @param retrieved
   * @param updateFromFile
   */
  private static void updateSubjectInCache(Subject subject, SubjectSourceCacheItem subjectSourceCacheItem, 
      String sourceId, boolean isId, String idOrIdentifier, boolean accessed, boolean retrieved, boolean updateFromFile) {

    initCacheIfNotInitted();

    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "updateSubjectInCache");
      debugMap.put("retrieved", retrieved);
      debugMap.put("accessed", accessed);
      startNanos = System.nanoTime();
    }
    
    try {
      
      if (subjectSourceCacheItem == null) {
        
        MultiKey cacheKey = null;
        
        // we need to find items in the cache
        if (subject != null) {
          cacheKey = new MultiKey(subject.getSourceId(), "id", subject.getId());
        } else if (sourceId != null && idOrIdentifier != null) {
          cacheKey = new MultiKey(sourceId, isId ? "id" : "identifier", idOrIdentifier);
        }
        
        subjectSourceCacheItem = subjectCache.get(cacheKey);
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("subjectSourceCacheItemNull", true);
          debugMap.put("subjectSourceCacheItemLookedUp", subjectSourceCacheItem != null);
        }
      }
      
      // if null then create
      if (subjectSourceCacheItem == null) {
        
        if (!retrieved) {
          throw new RuntimeException("Why is retrieved false and subjectSourceCacheItem is null????");
        }
        
        subjectSourceCacheItem = new SubjectSourceCacheItem();
        if (LOG.isDebugEnabled()) {
          debugMap.put("createdNewCacheItem", true);
        }
      }
      
      // set some metadata
      long now = System.currentTimeMillis();
      subjectSourceCacheItem.setLastAccessed(now);
      if (accessed) {
        synchronized (subjectSourceCacheItem) {
          subjectSourceCacheItem.setNumberOfTimesAccessed(subjectSourceCacheItem.getNumberOfTimesAccessed()+1);
          if (!retrieved) {
            subjectSourceCacheItem.setNumberOfTimesAccessedSinceLastRetrieved(subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved() + 1);
          }
        }
      }
      if (retrieved) {
        subjectSourceCacheItem.setLastRetrieved(now);
        synchronized (subjectSourceCacheItem) {
          subjectSourceCacheItem.setNumberOfTimesRetrieved(subjectSourceCacheItem.getNumberOfTimesRetrieved()+1);
          subjectSourceCacheItem.setNumberOfTimesAccessedSinceLastRetrieved(0);
        }
      }

      if (LOG.isDebugEnabled()) {
        debugMap.put("numberOfTimesRetrieved", subjectSourceCacheItem.getNumberOfTimesRetrieved());
        debugMap.put("numberOfTimesAccessed", subjectSourceCacheItem.getNumberOfTimesAccessed());
      }

      // if not retrieved from system of record then it means the cache is ok, we done
      if (!retrieved && !updateFromFile) {
        return;
      }
      subjectSourceCacheItem.setSubject(subject);

      MultiKey keyCacheKey = null;
      
      // we need to find items in the cache
      if (subject != null) {
        keyCacheKey = new MultiKey(subject.getSourceId(), subject.getId());
      } else if (subjectSourceCacheItem != null && subjectSourceCacheItem.getSubject() != null) {
        keyCacheKey = new MultiKey(subjectSourceCacheItem.getSubject().getSourceId(), subjectSourceCacheItem.getSubject().getId());
      } else if (sourceId != null && isId && idOrIdentifier != null) {
        keyCacheKey = new MultiKey(sourceId, idOrIdentifier);
      }

      if (keyCacheKey == null) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("keyCacheKeyNull", true);
        }
        // not sure what to do
        return;
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("sourceId", keyCacheKey.getKey(0));
        debugMap.put(isId ? "id" : "identifier", keyCacheKey.getKey(1));
        startNanos = System.nanoTime();
      }

      Set<MultiKey> existingSubjectCacheKeys = subjectKeyCache.get(keyCacheKey);
      
      if (existingSubjectCacheKeys == null) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("createSubjectKeyCache", true);
        }
        
        if (GrouperUtil.length(subjectKeyCache) >= maxElementsInMemory()) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheIsFull", true);
          }
          return;
        }
        existingSubjectCacheKeys = ConcurrentHashMap.newKeySet();
        subjectKeyCache.put(keyCacheKey, existingSubjectCacheKeys);
      }
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("existingSubjectCacheKeySize", GrouperUtil.length(existingSubjectCacheKeys));
      }
      
      Set<MultiKey> newCacheKeys = new HashSet<MultiKey>();
      
      // multikey is sourceId, label "id"|"identifier", value of id|identifier 
      final MultiKey idCacheKey = new MultiKey(keyCacheKey.getKey(0), "id", keyCacheKey.getKey(1));
      newCacheKeys.add(idCacheKey);
      
      if (subject != null) {
        
        boolean subjectIdentifierIsConfigured = false;
        
        for (String attribute : GrouperUtil.nonNull(subject.getSource().getSubjectIdentifierAttributesAll()).values()) {
          
          String attributeValue = subject.getAttributeValue(attribute, false);

          if (StringUtils.equals(attributeValue, idOrIdentifier)) {
            subjectIdentifierIsConfigured = true;
          }

          if (!StringUtils.isBlank(attributeValue)) {
            newCacheKeys.add(new MultiKey(subject.getSource().getId(), "identifier", attributeValue));
          }
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("identifier_" + attribute, attributeValue);
          }
        }

        if (!isId && !subjectIdentifierIsConfigured) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("identifierNotConfigured", idOrIdentifier);
          }
          if (SubjectConfig.retrieveConfig().propertyValueBoolean("subject.cache.logWarnIfIdentiferNotConfigured")) {
            LOG.warn("In subject source: " + subject.getSourceId() + " the identifier: '" + idOrIdentifier + "' can find subject: '" + subject.getId() + "', but the attribute for that identifier is not configured in the subject source.  In order for caching to be effective, please list all identifier attributes in the subject source.  You can configure to suppress this log message in subject config.");
          }
        }
      }
      
      boolean hasSubjectCacheKeyChange = false;
      
      //remove keys that shouldnt be there
      if (GrouperUtil.length(existingSubjectCacheKeys) > 0) {
        
        Set<MultiKey> removeSubjectCacheKeys = new HashSet<MultiKey>();
        removeSubjectCacheKeys.addAll(removeSubjectCacheKeys);

        removeSubjectCacheKeys.removeAll(newCacheKeys);

        //existing to remove
        for (MultiKey multiKey : removeSubjectCacheKeys) {
          boolean localHasChange = subjectCache.remove(multiKey) == null;
          hasSubjectCacheKeyChange = localHasChange || hasSubjectCacheKeyChange;

          existingSubjectCacheKeys.remove(multiKey);
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("remove_" + multiKey.getKey(2), true);
          }
        }
      }

      // see if we are up to date
      boolean changeAll = subjectSourceCacheItem != subjectCache.get(idCacheKey);

      // dont worry about most of them
      if (!changeAll) {
        newCacheKeys.removeAll(existingSubjectCacheKeys);
      }
      
      //add keys that should be there or new ones, make sure value is right
      for (MultiKey multiKey : newCacheKeys) {
        boolean localHasChange = subjectCache.put(multiKey, subjectSourceCacheItem) == null;
        hasSubjectCacheKeyChange = localHasChange || hasSubjectCacheKeyChange;
        
        if (localHasChange) {
          existingSubjectCacheKeys.add(multiKey);
        }
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("addOrReplace_" + multiKey.getKey(2), true);
        }
      }
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }
  
  /**
   * get a subject by id or from cache
   * @param source
   * @param id
   * @param exceptionIfNotFound
   * @return subject
   */
  public static Subject getSubjectFromCacheOrSource(Source source, String id, boolean exceptionIfNotFound) {
    
    initCacheIfNotInitted();

    boolean cacheHit=false;
    boolean dontCache=false;
    boolean resolved=false;

    Map<String, Object> debugMap = null;
    long startNanos = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "getSubjectFromCacheOrSource");
    }
    
    try {
        if (LOG.isDebugEnabled()) {
          debugMap.put("sourceId", source == null ? null : source.getId());
          debugMap.put("id", id);
        }
        
        if (source == null || StringUtils.isBlank(source.getId()) || StringUtils.isBlank(id)) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("invalidInputs", true);
          }
          if (exceptionIfNotFound) {
            throw new SubjectNotFoundException("Cant find subject by id: '" + id + "'");
          }
          return null;
        }
        if (!cacheEnabled() ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheDisabled", true);
          }
          dontCache = true;
        }
        
        if (SubjectConfig.retrieveConfig().subjectCacheExcludeSourceIds().contains(source.getId())) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("excludeSourceId", true);
          }
          dontCache = true;
        }
        Subject subject = null;
        if (dontCache) {
          subject =  source.getSubject(id, false);
        } else {
          MultiKey multiKey = new MultiKey(source.getId(), "id", id);
          SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("foundCacheItem", subjectSourceCacheItem != null);
          }

          boolean retrieved = false;
          if (subjectSourceCacheItem == null || subjectSourceCacheItem.expired()) {
            subject = source.getSubject(id, false);
            if (subjectSourceCacheItem != null) {
              subjectSourceCacheItem.setSubject(subject);
            }
            retrieved = true;
            cacheHit = false;
          } else {
            subject = subjectSourceCacheItem.getSubject();
            cacheHit = true;
          }
          if (LOG.isDebugEnabled()) {
            debugMap.put("foundSubject", subject != null);
          }
          updateSubjectInCache(subject, subjectSourceCacheItem, source.getId(), true, id, true, retrieved, false);

          subject = cloneSubject(subject);

        }

        resolved = subject != null;

        if (exceptionIfNotFound && subject == null) {
          throw new SubjectNotFoundException("Subject not found: '" + source.getId() + "': '" + id + "'");
        }

        return subject;
        
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        if (re instanceof SubjectNotFoundException) {
          debugMap.put("SubjectNotFound", re.getMessage());
        } else {
          debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
        }
      } 
      throw re;
    } finally {
      long endNanoTime = System.nanoTime();
      if (!dontCache) {
        if (cacheHit) {
          statsCurrent.cacheHitAdd(1);
          statsCurrent.cacheHitNanosAdd(endNanoTime-startNanos);
        } else {
          statsCurrent.cacheMissIndividualAdd(1);
          statsCurrent.cacheMissIndividualNanosAdd(endNanoTime-startNanos);
          if (resolved) {
            statsCurrent.cacheMissResolvedAdd(1);
          } else {
            statsCurrent.cacheMissUnresolvedAdd(1);
          }
        }
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((endNanoTime - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }
  
  /**
   * get a subject by id or from cache
   * @param source
   * @param ids
   * @return subject
   */
  public static Map<String, Subject> getSubjectsByIdsFromCacheOrSource(Source source, Collection<String> ids) {
    
    initCacheIfNotInitted();

    Map<String, Object> debugMap = null;
    long startNanos = System.nanoTime();
    long endNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "getSubjectsByIdsFromCacheOrSource");
    }
    
    try {
        if (LOG.isDebugEnabled()) {
          debugMap.put("sourceId", source == null ? null : source.getId());
          debugMap.put("idSize", GrouperUtil.length(ids));
          int idsPrinted = 0;
          Iterator<String> idIterator = GrouperUtil.nonNull(ids).iterator();
          while (idIterator.hasNext() && idsPrinted < 10) {
            String id = idIterator.next();
            debugMap.put("id_" + idsPrinted, id);
            idsPrinted++;
          }
        }
        
        if (source == null || StringUtils.isBlank(source.getId()) || GrouperUtil.length(ids) == 0) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("invalidInputs", true);
          }
          return null;
        }
        boolean dontCache = false;
        if (!cacheEnabled() ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheDisabled", true);
          }
          dontCache = true;
        }
        
        if (SubjectConfig.retrieveConfig().subjectCacheExcludeSourceIds().contains(source.getId())) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("excludeSourceId", true);
          }
          dontCache = true;
        }
        Map<String, Subject> subjects = null;
        if (dontCache) {
          subjects = source.getSubjectsByIds(ids);
        } else {
          
          subjects = new HashMap<String, Subject>();
          Collection<String> idsToRetrieveFromSource = new HashSet<String>();

          int itemsFoundInCache = 0;
          int subjectsFoundInCache = 0;
          int subjectsResolved = 0;
          int subjectsUnresolvable = 0;
          
          for (String id : ids) {
            
            MultiKey multiKey = new MultiKey(source.getId(), "id", id);
            SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);
            
            Subject subject = null;
            if (subjectSourceCacheItem == null || subjectSourceCacheItem.expired()) {
              idsToRetrieveFromSource.add(id);
            } else {

              if (LOG.isDebugEnabled()) {
                itemsFoundInCache++;
              }

              subject = subjectSourceCacheItem.getSubject();
              if (subject != null) {
                subjectsFoundInCache++;
                subjects.put(id, subject);
              }
              
              updateSubjectInCache(subject, subjectSourceCacheItem, source.getId(), true, id, true, false, false);
            }
          }
          long timeAfterCache = System.nanoTime();
          
          if (!dontCache && itemsFoundInCache > 0) {
            statsCurrent.cacheHitAdd(itemsFoundInCache);
            statsCurrent.cacheHitNanosAdd(timeAfterCache-startNanos);
          }

          // if there are subjects not unexpired in cache, go get them
          if (GrouperUtil.length(idsToRetrieveFromSource) > 0) {
            subjects.putAll(GrouperUtil.nonNull(source.getSubjectsByIds(idsToRetrieveFromSource)));
            
            for (String id: idsToRetrieveFromSource) {
              Subject subject = subjects.get(id);

              if (subject != null) {
                subjectsResolved++;
              } else {
                subjectsUnresolvable++;
              }
              updateSubjectInCache(subject, null, source.getId(), true, id, true, true, false);
            }
            
          }
          endNanos = System.nanoTime();
          if (!dontCache && GrouperUtil.length(idsToRetrieveFromSource) > 0) {
            statsCurrent.cacheMissListAdd(GrouperUtil.length(idsToRetrieveFromSource));
            statsCurrent.cacheMissListNanosAdd(endNanos-timeAfterCache);
          }
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("itemsFoundInCache", itemsFoundInCache);
            debugMap.put("subjectsFoundInCache", subjectsFoundInCache);
            debugMap.put("subjectsResolved", subjectsResolved);
            debugMap.put("subjectsUnresolvable", subjectsUnresolvable);
          }

          subjects = cloneSubjects(subjects);

        }
        
        return subjects;
        
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (endNanos == -1) {
        endNanos = System.currentTimeMillis();
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((endNanos - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }
  

  
  /**
   * get a subject by identifier or from cache
   * @param source
   * @param identifier
   * @param exceptionIfNotFound
   * @return subject
   */
  public static Subject getSubjectByIdentifierFromCacheOrSource(Source source, String identifier, boolean exceptionIfNotFound) {
    
    initCacheIfNotInitted();
    
    Map<String, Object> debugMap = null;
    long startNanos = System.nanoTime();
    boolean cacheHit=false;
    boolean dontCache = false;
    boolean resolved = false;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "getSubjectByIdentifierFromCacheOrSource");
    }
    
    try {
        if (LOG.isDebugEnabled()) {
          debugMap.put("sourceId", source == null ? null : source.getId());
          debugMap.put("identifier", identifier);
        }
        
        if (source == null || StringUtils.isBlank(source.getId()) || StringUtils.isBlank(identifier)) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("invalidInputs", true);
          }
          if (exceptionIfNotFound) {
            throw new SubjectNotFoundException("Invalid inputs: '" + identifier + "'");
          }
          return null;
        }
        if (!cacheEnabled() ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheDisabled", true);
          }
          dontCache = true;
        }
        
        if (SubjectConfig.retrieveConfig().subjectCacheExcludeSourceIds().contains(source.getId())) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("excludeSourceId", true);
          }
          dontCache = true;
        }
        Subject subject = null;
        if (dontCache) {
          subject =  source.getSubjectByIdentifier(identifier, exceptionIfNotFound);
        } else {
          MultiKey multiKey = new MultiKey(source.getId(), "identifier", identifier);
          SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);

          if (LOG.isDebugEnabled()) {
            debugMap.put("foundCacheItem", subjectSourceCacheItem != null);
          }

          boolean retrieved = false;
          if (subjectSourceCacheItem == null || subjectSourceCacheItem.expired()) {
            subject = source.getSubjectByIdentifier(identifier, false);
            if (subjectSourceCacheItem != null) {
              subjectSourceCacheItem.setSubject(subject);
            }
            retrieved = true;
            cacheHit = false;
          } else {
            cacheHit = true;
            subject = subjectSourceCacheItem.getSubject();
          }
          if (LOG.isDebugEnabled()) {
            debugMap.put("foundSubject", subject != null);
          }
          updateSubjectInCache(subject, subjectSourceCacheItem, source.getId(), false, identifier, true, retrieved, false);
          
          subject = cloneSubject(subject);
        }
        
        resolved = subject != null;

        if (exceptionIfNotFound && subject == null) {
          throw new SubjectNotFoundException("Subject not found: '" + source.getId() + "': '" + identifier + "'");
        }

        return subject;
        
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      
      long endNanoTime = System.nanoTime();
      if (!dontCache) {
        if (cacheHit) {
          statsCurrent.cacheHitAdd(1);
          statsCurrent.cacheHitNanosAdd(endNanoTime-startNanos);
        } else {
          statsCurrent.cacheMissIndividualAdd(1);
          statsCurrent.cacheMissIndividualNanosAdd(endNanoTime-startNanos);
          if (resolved) {
            statsCurrent.cacheMissResolvedAdd(1);
          } else {
            statsCurrent.cacheMissUnresolvedAdd(1);
          }
        }
      }

      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((endNanoTime - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * @return if cache enabled
   */
  public static boolean cacheEnabled() {
    return SubjectConfig.retrieveConfig().propertyValueBoolean("subject.cache.enable", true);
  }
  
  /**
   * get a subject by identifier or from cache
   * @param source
   * @param idOrIdentifier
   * @param exceptionIfNotFound
   * @return subject
   */
  public static Subject getSubjectByIdOrIdentifierFromCacheOrSource(Source source, String idOrIdentifier, boolean exceptionIfNotFound) {
    
    initCacheIfNotInitted();

    Map<String, Object> debugMap = null;
    long startNanos = -1;
    
    boolean cacheHit=false;
    boolean dontCache = false;
    boolean resolved=false;

    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "getSubjectByIdentifierFromCacheOrSource");
      startNanos = System.nanoTime();
    }
    
    try {
        if (LOG.isDebugEnabled()) {
          debugMap.put("sourceId", source == null ? null : source.getId());
          debugMap.put("identifier", idOrIdentifier);
        }
        
        if (source == null || StringUtils.isBlank(source.getId()) || StringUtils.isBlank(idOrIdentifier)) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("invalidInputs", true);
          }
          if (exceptionIfNotFound) {
            throw new SubjectNotFoundException("Invalid inputs: '" + idOrIdentifier + "'");
          }
          return null;
        }

        if (!cacheEnabled() ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheDisabled", true);
          }
          dontCache = true;
        }
        
        if (SubjectConfig.retrieveConfig().subjectCacheExcludeSourceIds().contains(source.getId())) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("excludeSourceId", true);
          }
          dontCache = true;
        }
        Subject subject = null;
        if (dontCache) {
          subject =  source.getSubjectByIdOrIdentifier(idOrIdentifier, false);
        } else {
          boolean foundById = false;
          MultiKey multiKey = new MultiKey(source.getId(), "id", idOrIdentifier);
          SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);

          if (subjectSourceCacheItem == null) {
            multiKey = new MultiKey(source.getId(), "identifier", idOrIdentifier);
            subjectSourceCacheItem = subjectCache.get(multiKey);

            if (subjectSourceCacheItem != null) {
              if (LOG.isDebugEnabled()) {
                debugMap.put("foundCacheItemByIdentifier", subjectSourceCacheItem != null);
              }
            }
          } else {
            
            if (LOG.isDebugEnabled()) {
              debugMap.put("foundCacheItemById", subjectSourceCacheItem != null);
            }

            foundById = true;
          }
          
          boolean retrieved = false;
          if (subjectSourceCacheItem == null || subjectSourceCacheItem.expired()) {
            subject = source.getSubjectByIdOrIdentifier(idOrIdentifier, false);
            if (subjectSourceCacheItem != null) {
              subjectSourceCacheItem.setSubject(subject);
            }
            retrieved = true;
            cacheHit = false;
          } else {
            cacheHit = true;
            subject = subjectSourceCacheItem.getSubject();
          }
          if (LOG.isDebugEnabled()) {
            debugMap.put("foundSubject", subject != null);
          }
          if (StringUtils.equals(idOrIdentifier, subject == null ? null : subject.getId())) {
            foundById = true;
          }
          updateSubjectInCache(subject, subjectSourceCacheItem, source.getId(), 
              foundById, idOrIdentifier, true, retrieved, false);

          subject = cloneSubject(subject);

        }
        resolved = subject != null;
        
        if (exceptionIfNotFound && subject == null) {
          throw new SubjectNotFoundException("Subject not found: '" + source.getId() + "': '" + idOrIdentifier + "'");
        }
        
        return subject;
        
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      
      long endNanoTime = System.nanoTime();
      if (!dontCache) {
        if (cacheHit) {
          statsCurrent.cacheHitAdd(1);
          statsCurrent.cacheHitNanosAdd(endNanoTime-startNanos);
        } else {
          statsCurrent.cacheMissIndividualAdd(1);
          statsCurrent.cacheMissIndividualNanosAdd(endNanoTime-startNanos);
          if (resolved) {
            statsCurrent.cacheMissResolvedAdd(1);
          } else {
            statsCurrent.cacheMissUnresolvedAdd(1);
          }
        }
      }

      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((endNanoTime - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * get a subject by id or from cache
   * @param source
   * @param identifiers
   * @return subject
   */
  public static Map<String, Subject> getSubjectsByIdentifiersFromCacheOrSource(Source source, Collection<String> identifiers) {
    
    initCacheIfNotInitted();

    Map<String, Object> debugMap = null;
    long startNanos = System.nanoTime();
    long endNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "getSubjectsByIdentifiersFromCacheOrSource");
    }
    
    try {
        if (LOG.isDebugEnabled()) {
          debugMap.put("sourceId", source == null ? null : source.getId());
          debugMap.put("identifierSize", GrouperUtil.length(identifiers));
          int identifiersPrinted = 0;
          Iterator<String> idIterator = GrouperUtil.nonNull(identifiers).iterator();
          while (idIterator.hasNext() && identifiersPrinted < 10) {
            String id = idIterator.next();
            debugMap.put("identifier_" + identifiersPrinted, id);
            identifiersPrinted++;
          }
        }
        
        if (source == null || StringUtils.isBlank(source.getId()) || GrouperUtil.length(identifiers) == 0) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("invalidInputs", true);
          }
          return null;
        }
        boolean dontCache = false;
        if (!cacheEnabled() ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheDisabled", true);
          }
          dontCache = true;
        }
        
        if (SubjectConfig.retrieveConfig().subjectCacheExcludeSourceIds().contains(source.getId())) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("excludeSourceId", true);
          }
          dontCache = true;
        }
        Map<String, Subject> subjects = null;
        if (dontCache) {
          subjects = source.getSubjectsByIdentifiers(identifiers);
        } else {
          
          subjects = new HashMap<String, Subject>();
          Collection<String> identifiersToRetrieveFromSource = new HashSet<String>();
  
          int itemsFoundInCache = 0;
          int subjectsFoundInCache = 0;
          int subjectsResolved = 0;
          int subjectsUnresolvable = 0;
          
          for (String identifier : identifiers) {
            
            MultiKey multiKey = new MultiKey(source.getId(), "identifier", identifier);
            SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);
            
            Subject subject = null;
            if (subjectSourceCacheItem == null || subjectSourceCacheItem.expired()) {
              identifiersToRetrieveFromSource.add(identifier);
            } else {

              if (LOG.isDebugEnabled()) {
                itemsFoundInCache++;
              }
    
              subject = subjectSourceCacheItem.getSubject();
              if (subject != null) {
                subjectsFoundInCache++;
                subjects.put(identifier, subject);
              }
              
              updateSubjectInCache(subject, subjectSourceCacheItem, source.getId(), false, identifier, true, false, false);
            }
          }
  
          long timeAfterCache = System.nanoTime();
          
          if (!dontCache && itemsFoundInCache > 0) {
            statsCurrent.cacheHitAdd(itemsFoundInCache);
            statsCurrent.cacheHitNanosAdd(timeAfterCache-startNanos);
          }

          // if there are subjects not unexpired in cache, go get them
          if (GrouperUtil.length(identifiersToRetrieveFromSource) > 0) {
            subjects.putAll(GrouperUtil.nonNull(source.getSubjectsByIdentifiers(identifiersToRetrieveFromSource)));
            
            for (String identifier: identifiersToRetrieveFromSource) {
              Subject subject = subjects.get(identifier);
  
              if (subject != null) {
                subjectsResolved++;
              } else {
                subjectsUnresolvable++;
              }
              updateSubjectInCache(subject, null, source.getId(), false, identifier, true, true, false);
            }
            
          }
          
          endNanos = System.nanoTime();
          if (!dontCache && GrouperUtil.length(identifiersToRetrieveFromSource) > 0) {
            statsCurrent.cacheMissListAdd(GrouperUtil.length(identifiersToRetrieveFromSource));
            statsCurrent.cacheMissListNanosAdd(endNanos-timeAfterCache);
          }

          if (LOG.isDebugEnabled()) {
            debugMap.put("itemsFoundInCache", itemsFoundInCache);
            debugMap.put("subjectsFoundInCache", subjectsFoundInCache);
            debugMap.put("subjectsResolved", subjectsResolved);
            debugMap.put("subjectsUnresolvable", subjectsUnresolvable);
          }
          subjects = cloneSubjects(subjects);
        }
        
        return subjects;
        
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (endNanos == -1) {
        endNanos = System.currentTimeMillis();
      }

      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((endNanos - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @param subject
   * @return the cloned subject
   */
  public static Subject cloneSubject(Subject subject) {

    if (subject == null) {
      return null;
    }
    
    if (SubjectConfig.retrieveConfig().propertyValueBoolean("subject.cache.cloneSubjectsOnReturn", true)) {
      subject = new SubjectImpl(subject.getId(), subject.getName(), 
          subject.getDescription(), subject.getTypeName(), subject.getSourceId(), 
          subject.getAttributes(false));
      return subject;
    }
    
    return subject;
  }
  
  /**
   * @param subjects
   * @return the cloned subject
   */
  public static Map<String, Subject> cloneSubjects(Map<String, Subject> subjects) {

    if (GrouperUtil.length(subjects) == 0) {
      return subjects;
    }
    
    if (SubjectConfig.retrieveConfig().propertyValueBoolean("subject.cache.cloneSubjectsOnReturn", true)) {
      for (Map.Entry<String, Subject> entry : subjects.entrySet()) {
        subjects.put(entry.getKey(), cloneSubject(entry.getValue()));
      }
    }
    
    return subjects;
  }
  
  /**
   * get a subject by id or from cache
   * @param source
   * @param idsOrIdentifiers
   * @return subject
   */
  public static Map<String, Subject> getSubjectsByIdsOrIdentifiersFromCacheOrSource(Source source, Collection<String> idsOrIdentifiers) {
    
    initCacheIfNotInitted();

    Map<String, Object> debugMap = null;
    long startNanos = System.nanoTime();
    long endNanos = -1;

    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "getSubjectsByIdsOrIdentifiersFromCacheOrSource");
    }
    
    try {
        if (LOG.isDebugEnabled()) {
          debugMap.put("sourceId", source == null ? null : source.getId());
          debugMap.put("idsOrIdentifierSize", GrouperUtil.length(idsOrIdentifiers));
          int idsOrIdentifiersPrinted = 0;
          Iterator<String> idOrIdentifierIterator = GrouperUtil.nonNull(idsOrIdentifiers).iterator();
          while (idOrIdentifierIterator.hasNext() && idsOrIdentifiersPrinted < 10) {
            String idOrIdentifier = idOrIdentifierIterator.next();
            debugMap.put("idOrIdentifier_" + idsOrIdentifiersPrinted, idOrIdentifier);
            idsOrIdentifiersPrinted++;
          }
        }
        
        if (source == null || StringUtils.isBlank(source.getId()) || GrouperUtil.length(idsOrIdentifiers) == 0) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("invalidInputs", true);
          }
          return null;
        }
        boolean dontCache = false;
        if (!cacheEnabled() ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("cacheDisabled", true);
          }
          dontCache = true;
        }
        
        if (SubjectConfig.retrieveConfig().subjectCacheExcludeSourceIds().contains(source.getId())) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("excludeSourceId", true);
          }
          dontCache = true;
        }
        Map<String, Subject> subjects = null;
        if (dontCache) {
          subjects = source.getSubjectsByIdsOrIdentifiers(idsOrIdentifiers);
        } else {
          
          subjects = new HashMap<String, Subject>();
          Collection<String> idsOrIdentifiersToRetrieveFromSource = new HashSet<String>();
  
          int itemsFoundInCache = 0;
          int subjectsFoundInCache = 0;
          int subjectsResolved = 0;
          int subjectsUnresolvable = 0;
          
          for (String idOrIdentifier : idsOrIdentifiers) {
            
            boolean foundById = false;

            // first try to get by id from cache
            MultiKey multiKey = new MultiKey(source.getId(), "id", idOrIdentifier);
            SubjectSourceCacheItem subjectSourceCacheItem = subjectCache.get(multiKey);

            if (LOG.isDebugEnabled()) {
              debugMap.put("foundCacheItemById", subjectSourceCacheItem != null);
            }

            if (subjectSourceCacheItem == null) {
            
              // didnt find, try to to get from cache by identifier
              multiKey = new MultiKey(source.getId(), "identifier", idOrIdentifier);
              subjectSourceCacheItem = subjectCache.get(multiKey);

              if (LOG.isDebugEnabled()) {
                debugMap.put("foundCacheItemByIdentifier", subjectSourceCacheItem != null);
              }
            } else {
              foundById = true;
            }
            
            if (subjectSourceCacheItem == null || subjectSourceCacheItem.expired()) {
              idsOrIdentifiersToRetrieveFromSource.add(idOrIdentifier);
            } else {
              if (LOG.isDebugEnabled()) {
                itemsFoundInCache++;
              }
    
              Subject subject = subjectSourceCacheItem.getSubject();
              if (subject != null) {
                subjectsFoundInCache++;
                subjects.put(idOrIdentifier, subject);
              }
              if (StringUtils.equals(idOrIdentifier, subject == null ? null : subject.getId())) {
                foundById = true;
              }
              
              updateSubjectInCache(subject, subjectSourceCacheItem, source.getId(), foundById, idOrIdentifier, true, false, false);

            }
            
          }
  
          long timeAfterCache = System.nanoTime();
          
          if (!dontCache && itemsFoundInCache > 0) {
            statsCurrent.cacheHitAdd(itemsFoundInCache);
            statsCurrent.cacheHitNanosAdd(timeAfterCache-startNanos);
          }

          // if there are subjects not unexpired in cache, go get them
          if (GrouperUtil.length(idsOrIdentifiersToRetrieveFromSource) > 0) {
            subjects.putAll(GrouperUtil.nonNull(source.getSubjectsByIdsOrIdentifiers(idsOrIdentifiersToRetrieveFromSource)));
            
            for (String idOrIdentifier: idsOrIdentifiersToRetrieveFromSource) {
              Subject subject = subjects.get(idOrIdentifier);
  
              if (subject != null) {
                subjectsResolved++;
              } else {
                subjectsUnresolvable++;
              }
              boolean foundById = false;
              if (StringUtils.equals(idOrIdentifier, subject == null ? null : subject.getId())) {
                foundById = true;
              }
              updateSubjectInCache(subject, null, source.getId(), foundById, idOrIdentifier, true, true, false);
            }
            
          }
          
          endNanos = System.nanoTime();
          if (!dontCache && GrouperUtil.length(idsOrIdentifiersToRetrieveFromSource) > 0) {
            statsCurrent.cacheMissListAdd(GrouperUtil.length(idsOrIdentifiersToRetrieveFromSource));
            statsCurrent.cacheMissListNanosAdd(endNanos-timeAfterCache);
          }

          if (LOG.isDebugEnabled()) {
            debugMap.put("itemsFoundInCache", itemsFoundInCache);
            debugMap.put("subjectsFoundInCache", subjectsFoundInCache);
            debugMap.put("subjectsResolved", subjectsResolved);
            debugMap.put("subjectsUnresolvable", subjectsUnresolvable);
          }
  
          subjects = cloneSubjects(subjects);

        }
        
        return subjects;
        
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      
      if (endNanos == -1) {
        endNanos = System.currentTimeMillis();
      }

      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((endNanos - startNanos) / 1000) + "micros");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * delete cache files not needed
   */
  static synchronized void deleteOldStorageFiles() {
    
    if (!cacheEnabled() ) {
      return;
    }
  
    Map<String, Object> debugMap = null;
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      debugMap =  new LinkedHashMap();
      debugMap.put("method", "deleteOldStorageFiles");
      startNanos = System.nanoTime();
    }
    
    try {
  
      SubjectSourceSerializer subjectSourceSerializer = retrieveCacheSerializer(debugMap);
  
      if (subjectSourceSerializer == null) {
        return;
      }

      // see what the timeout is, has to be less than when the cache was written
      final int timeToLiveSeconds = SubjectSourceCacheItem.timeToLiveSeconds();
      if (LOG.isDebugEnabled()) {
        debugMap.put("timeToLiveSeconds", timeToLiveSeconds);
      }

      final long newerThanMillis = System.currentTimeMillis() - (1000*SubjectSourceCacheItem.timeToLiveSeconds());

      if (LOG.isDebugEnabled()) {
        debugMap.put("newerThan", new Date(newerThanMillis));
      }

      subjectSourceSerializer.cleanupOldSubjectCaches(newerThanMillis, debugMap);
            
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      } 
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("took", ((System.nanoTime() - startNanos) / 1000000) + "millis");
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  
  }
}
