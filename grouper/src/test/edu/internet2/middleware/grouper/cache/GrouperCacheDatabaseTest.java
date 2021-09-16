package edu.internet2.middleware.grouper.cache;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import junit.textui.TestRunner;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;


public class GrouperCacheDatabaseTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperCacheDatabaseTest("testCustomCache"));
  }
  
  public GrouperCacheDatabaseTest(String name) {
    super(name);
  }

  public void testCacheExpirableCache() {
    
    // make a cache
    ExpirableCache<String, Integer> someCache = new ExpirableCache<String, Integer>(5);
    someCache.registerDatabaseClearableCache("edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest");

    long nowNanos = System.currentTimeMillis() * 1000000;
    GrouperUtil.sleep(30);
    
    someCache.put("test", 5);
    // this will tell the database to do an update
    someCache.notifyDatabaseOfChanges();
    
    long cacheOverallLastUpdatedNanos = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);

    assertTrue(cacheOverallLastUpdatedNanos > nowNanos);
      
    long cacheInstanceLastUpdatedNanosMyTest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("expirableCache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
    
    assertTrue(cacheInstanceLastUpdatedNanosMyTest > nowNanos);
    
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTest == cacheOverallLastUpdatedNanos);
    
    nowNanos = System.currentTimeMillis() * 1000000;
    
    GrouperUtil.sleep(30);

    // has not been cleared
    assertEquals(1, someCache.size(false));
    
    someCache.put("test2", 6);
    someCache.notifyDatabaseOfChanges();
    
    long cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);

    assertTrue(cacheOverallLastUpdatedNanos + " is not greater than " + nowNanos, cacheOverallLastUpdatedNanosLatest > nowNanos);

    long cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("expirableCache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);

    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > nowNanos);
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > cacheInstanceLastUpdatedNanosMyTest);

    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest == cacheOverallLastUpdatedNanosLatest);

    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;

    // sleep through a few cycles
    GrouperUtil.sleep(10000);

    // has not been cleared
    assertEquals(2, someCache.size(false));

    cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);

    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);

    cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
        .addBindVar("expirableCache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);

    assertEquals(cacheInstanceLastUpdatedNanosMyTestLatest, cacheInstanceLastUpdatedNanosMyTest);

    // hopefully there is no interference here...
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);

    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;

    nowNanos = System.currentTimeMillis() * 1000000;
    
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
      .addBindVar(nowNanos).addBindVar("expirableCache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").executeSql();

    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();

    assertEquals(1, rowsUpdated);

    // sleep through a few cycles
    GrouperUtil.sleep(10000);
    
    // has been cleared
    assertEquals(0, someCache.size(false));

    
  }

  public void testGrouperCache() {
    
    // make a cache
    GrouperCache<String, Integer> someCache = new GrouperCache<String, Integer>("edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest", 1000, false, 3600, 3600, false);
    someCache.registerDatabaseClearableCache();
  
    long nowNanos = System.currentTimeMillis() * 1000000;
    GrouperUtil.sleep(30);
    
    someCache.put("test", 5);
    // this will tell the database to do an update
    someCache.notifyDatabaseOfChanges();

  
    long cacheOverallLastUpdatedNanos = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos > nowNanos);
      
    long cacheInstanceLastUpdatedNanosMyTest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("ehcache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
    
    assertTrue(cacheInstanceLastUpdatedNanosMyTest > nowNanos);
    
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTest == cacheOverallLastUpdatedNanos);
    
    nowNanos = System.currentTimeMillis() * 1000000;
    
    GrouperUtil.sleep(30);
  
    // has not been cleared
    assertEquals(1, someCache.getStats().getObjectCount());
    
    someCache.put("test2", 6);
    // this will tell the database to do an update
    someCache.notifyDatabaseOfChanges();

    long cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos + " is not greater than " + nowNanos, cacheOverallLastUpdatedNanosLatest > nowNanos);
  
    long cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("ehcache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
  
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > nowNanos);
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest == cacheOverallLastUpdatedNanosLatest);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    // sleep through a few cycles
    GrouperUtil.sleep(10000);
  
    // has not been cleared
    assertEquals(2, someCache.getStats().getObjectCount());
  
    cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
        .addBindVar("ehcache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
  
    assertEquals(cacheInstanceLastUpdatedNanosMyTestLatest, cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    nowNanos = System.currentTimeMillis() * 1000000;
    
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
      .addBindVar(nowNanos).addBindVar("ehcache__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").executeSql();
  
    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();
  
    assertEquals(1, rowsUpdated);
  
    // sleep through a few cycles
    GrouperUtil.sleep(10000);
    
    // has been cleared
    assertEquals(0, someCache.getStats().getObjectCount());
  
    
  }
  
  public void testCustomCache() {
    
    // make a cache
    final Map<String, Integer> someCache = new HashMap<String, Integer>();
    GrouperCacheDatabase.customRegisterDatabaseClearable(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2", new GrouperCacheDatabaseClear() {
          
          @Override
          public void clear(GrouperCacheDatabaseClearInput grouperCacheDatabaseClearInput) {
            someCache.clear();
          }
        });
    
    long nowNanos = System.currentTimeMillis() * 1000000;
    GrouperUtil.sleep(30);
    
    someCache.put("test", 5);
    // this will tell the database to do an update
    GrouperCacheDatabase.customNotifyDatabaseOfChanges(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2");

  
    long cacheOverallLastUpdatedNanos = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos > nowNanos);
      
    long cacheInstanceLastUpdatedNanosMyTest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2").select(Long.class);
    
    assertTrue(cacheInstanceLastUpdatedNanosMyTest > nowNanos);
    
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTest == cacheOverallLastUpdatedNanos);
    
    nowNanos = System.currentTimeMillis() * 1000000;
    
    GrouperUtil.sleep(30);
  
    // has not been cleared
    assertEquals(1, someCache.size());
    
    someCache.put("test2", 6);
    // this will tell the database to do an update
    GrouperCacheDatabase.customNotifyDatabaseOfChanges(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2");

    long cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos + " is not greater than " + nowNanos, cacheOverallLastUpdatedNanosLatest > nowNanos);
  
    long cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2").select(Long.class);
  
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > nowNanos);
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest == cacheOverallLastUpdatedNanosLatest);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    // sleep through a few cycles
    GrouperUtil.sleep(10000);
  
    // has not been cleared
    assertEquals(2, someCache.size());
  
    cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
        .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2").select(Long.class);
  
    assertEquals(cacheInstanceLastUpdatedNanosMyTestLatest, cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    nowNanos = System.currentTimeMillis() * 1000000;
    
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
      .addBindVar(nowNanos).addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest2").executeSql();
  
    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();
  
    assertEquals(1, rowsUpdated);
  
    // sleep through a few cycles
    GrouperUtil.sleep(10000);
    
    // has been cleared
    assertEquals(0, someCache.size());
  
    
  }
  
  public void testEhcache() {
    
    // make a cache
    Cache someCache = EhcacheController.ehcacheController().getCache("edu.internet2.middleware.grouper.privs.CachingAccessResolver.HasPrivilege");
    GrouperCacheDatabase.ehcacheRegisterDatabaseClearableCache(someCache.getName());
  
    long nowNanos = System.currentTimeMillis() * 1000000;
    GrouperUtil.sleep(30);
    
    someCache.put(new Element("test", 5));
    // this will tell the database to do an update
    GrouperCacheDatabase.ehcacheNotifyDatabaseOfChanges(someCache.getName());

  
    long cacheOverallLastUpdatedNanos = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos > nowNanos);
      
    long cacheInstanceLastUpdatedNanosMyTest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("ehcache__edu.internet2.middleware.grouper.privs.CachingAccessResolver.HasPrivilege").select(Long.class);
    
    assertTrue(cacheInstanceLastUpdatedNanosMyTest > nowNanos);
    
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTest == cacheOverallLastUpdatedNanos);
    
    nowNanos = System.currentTimeMillis() * 1000000;
    
    GrouperUtil.sleep(30);
  
    // has not been cleared
    assertEquals(1, someCache.getSize());
    
    someCache.put(new Element("test2", 6));
    // this will tell the database to do an update
    GrouperCacheDatabase.ehcacheNotifyDatabaseOfChanges(someCache.getName());

    long cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos + " is not greater than " + nowNanos, cacheOverallLastUpdatedNanosLatest > nowNanos);
  
    long cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("ehcache__edu.internet2.middleware.grouper.privs.CachingAccessResolver.HasPrivilege").select(Long.class);
  
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > nowNanos);
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest == cacheOverallLastUpdatedNanosLatest);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    long fullCount = GrouperCacheDatabase.fullCountForTesting;
    long incrementalCount = GrouperCacheDatabase.incrementalCountForTesting;

    // sleep through a few cycles
    GrouperUtil.sleep(10000);

    assertEquals(fullCount, GrouperCacheDatabase.fullCountForTesting);
    // not sure why this doesnt work on testing server but it works locally
    assertEquals(incrementalCount + 2, GrouperCacheDatabase.incrementalCountForTesting);
    
    // has not been cleared
    assertEquals(2, someCache.getSize());
  
    cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
        .addBindVar("ehcache__edu.internet2.middleware.grouper.privs.CachingAccessResolver.HasPrivilege").select(Long.class);
  
    assertEquals(cacheInstanceLastUpdatedNanosMyTestLatest, cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    nowNanos = System.currentTimeMillis() * 1000000;
    
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
      .addBindVar(nowNanos).addBindVar("ehcache__edu.internet2.middleware.grouper.privs.CachingAccessResolver.HasPrivilege").executeSql();
  
    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();
  
    assertEquals(1, rowsUpdated);
  
    fullCount = GrouperCacheDatabase.fullCountForTesting;
    incrementalCount = GrouperCacheDatabase.incrementalCountForTesting;

    // sleep through a few cycles
    GrouperUtil.sleep(10000);

    assertEquals(fullCount, GrouperCacheDatabase.fullCountForTesting);
    assertEquals(incrementalCount + 2, GrouperCacheDatabase.incrementalCountForTesting);
    
    // has been cleared
    assertEquals(0, someCache.getSize());
    someCache.flush();
    
  }

  public void testCustomCachePrefix() {
    
    // make a cache
    final Map<String, Integer> someCache = new HashMap<String, Integer>();

    final Map<String, Integer> someCache____a = new HashMap<String, Integer>();

    final Map<String, Integer> someCache____b = new HashMap<String, Integer>();

    GrouperCacheDatabaseClear grouperCacheDatabaseClear = new GrouperCacheDatabaseClear() {
      
      @Override
      public void clear(GrouperCacheDatabaseClearInput grouperCacheDatabaseClearInput) {

        if ("edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest".equals(grouperCacheDatabaseClearInput.getCacheName())) {
          someCache.clear();
        } else if ("edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest____a".equals(grouperCacheDatabaseClearInput.getCacheName())) {
          someCache____a.clear();
        } else if ("edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest____b".equals(grouperCacheDatabaseClearInput.getCacheName())) {
          someCache____b.clear();
        } else {
          throw new RuntimeException("Cant find cache name! " + grouperCacheDatabaseClearInput.getCacheName());
        }
      }
    };
    
    GrouperCacheDatabase.customRegisterDatabaseClearable(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest", grouperCacheDatabaseClear);
    GrouperCacheDatabase.customRegisterDatabaseClearable(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest____b", grouperCacheDatabaseClear);

    long nowNanos = System.currentTimeMillis() * 1000000;
    GrouperUtil.sleep(30);
    
    someCache.put("test", 5);

    someCache____a.put("testa", 6);
    someCache____b.put("testa", 7);

    // this will tell the database to do an update
    GrouperCacheDatabase.customNotifyDatabaseOfChanges(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest");
  
    long cacheOverallLastUpdatedNanos = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos > nowNanos);
      
    long cacheInstanceLastUpdatedNanosMyTest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
    
    assertTrue(cacheInstanceLastUpdatedNanosMyTest > nowNanos);
    
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTest == cacheOverallLastUpdatedNanos);
    
    nowNanos = System.currentTimeMillis() * 1000000;
    
    GrouperUtil.sleep(30);
  
    // has not been cleared
    assertEquals(1, someCache.size());
    
    someCache.put("test2", 6);

    // this will tell the database to do an update
    GrouperCacheDatabase.customNotifyDatabaseOfChanges(
        "edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest");
  
    long cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertTrue(cacheOverallLastUpdatedNanos + " is not greater than " + nowNanos, cacheOverallLastUpdatedNanosLatest > nowNanos);
  
    long cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
      .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
  
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > nowNanos);
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest > cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertTrue(cacheInstanceLastUpdatedNanosMyTestLatest == cacheOverallLastUpdatedNanosLatest);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    // sleep through a few cycles
    GrouperUtil.sleep(10000);
  
    // has not been cleared
    assertEquals(2, someCache.size());
  
    cacheOverallLastUpdatedNanosLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_overall where overall_cache = 0").select(Long.class);
  
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTestLatest = new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
        .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").select(Long.class);
  
    assertEquals(cacheInstanceLastUpdatedNanosMyTestLatest, cacheInstanceLastUpdatedNanosMyTest);
  
    // hopefully there is no interference here...
    assertEquals(cacheOverallLastUpdatedNanosLatest, cacheOverallLastUpdatedNanos);
  
    cacheInstanceLastUpdatedNanosMyTest = cacheInstanceLastUpdatedNanosMyTestLatest;
    cacheOverallLastUpdatedNanos = cacheOverallLastUpdatedNanosLatest;
  
    nowNanos = System.currentTimeMillis() * 1000000;
    
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
      .addBindVar(nowNanos).addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest").executeSql();
  
    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();
  
    assertEquals(1, rowsUpdated);

    // sleep through a few cycles
    GrouperUtil.sleep(10000);
    
    // has been cleared
    assertEquals(0, someCache.size());
  
    someCache.put("a", 3);
    assertEquals(1, someCache.size());
    assertEquals(1, someCache____a.size());
    assertEquals(1, someCache____b.size());

    nowNanos = System.currentTimeMillis() * 1000000;
    boolean rowExists = 1 == new GcDbAccess().sql("select count(1) from grouper_cache_instance where cache_name = ?")
        .addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest____a").select(int.class);
    
    if (rowExists) {
      rowsUpdated = new GcDbAccess()
          .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
          .addBindVar(nowNanos).addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest____a").executeSql();
    } else {
      rowsUpdated = new GcDbAccess()
          .sql("insert into grouper_cache_instance (nanos_since_1970, cache_name) values (?, ?)")
          .addBindVar(nowNanos).addBindVar("custom__edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseTest.myTest____a").executeSql();
    }
    
    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();
  
    assertEquals(1, rowsUpdated);

    // sleep through a few cycles
    GrouperUtil.sleep(10000);
    
    assertEquals(1, someCache.size());
    assertEquals("cleared", 0, someCache____a.size());
    assertEquals(1, someCache____b.size());

    
    
  }
  
  
}
