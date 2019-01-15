/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.io.File;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;


/**
 *
 */
public class SubjectSourceCacheTest extends GrouperTest {

  /**
   * 
   * @param name
   */
  public SubjectSourceCacheTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SubjectSourceCacheTest("testCache"));
  }
  
  
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subject.cache.enable", "true");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subject.cache.logStatsSeconds", "0");
    
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    // this inits the test subject static vars
    @SuppressWarnings("unused")
    Subject subj = SubjectTestHelper.SUBJ0;

  }

  /**
   * 
   */
  public void testCache() {

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    SubjectSourceCache.clearCache();
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    int cacheMisses = SubjectSourceCache.statsCurrent.getCacheMissIndividual();

    assertEquals(0, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(0, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));
    
    Subject subject = SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ8_ID, "jdbc", true);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    Set<MultiKey> subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    SubjectSourceCacheItem subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(1, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(1, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(0, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());
    
    // ####################################################### look in cache by id
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();

    assertEquals("shouldnt cache", SubjectSourceCache.statsCurrent.getCacheMissIndividual(), cacheMisses + 1);

    int cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    
    // switch subjects so it doesnt cache higher up
    grouperSession.stop();
    GrouperSession.start(SubjectTestHelper.SUBJ1);

    Subject newSubject = SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ8_ID, "jdbc", true);
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("should cache", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits + 1);
    assertNotSame("Should be clone", subject, newSubject);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(2, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(1, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(1, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());

    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();

    assertEquals("shouldnt cache", SubjectSourceCache.statsCurrent.getCacheMissIndividual(), cacheMisses + 1);

    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    subject = newSubject;
    
    // ####################################################### look in cache by identifier
    
    // switch subjects so it doesnt cache higher up
    grouperSession.stop();
    GrouperSession.start(SubjectTestHelper.SUBJ2);

    newSubject = SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ8_ID, "jdbc", true);
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("should cache", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits + 1);
    assertNotSame("Should be clone", subject, newSubject);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(3, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(1, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(2, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());

    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    subject = newSubject;
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subject.cache.maxElementsInMemory", "1");

    // ####################################################### cant surpass the max
    
    // makes eligible for refresh
    grouperSession.stop();
    GrouperSession.start(SubjectTestHelper.SUBJ3);

    newSubject = SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ9_ID, "jdbc", true);
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    // ####################################################### doesnt need refresh

    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    subject = newSubject;

    // makes eligible for refresh
    assertFalse(subjectSourceCacheItem.expired());
    
    SubjectSourceCache.sweepCacheToResolveItemsThatNeedItLastRun = -1;
    SubjectSourceCache.sweepCacheToResolveItemsThatNeedIt();
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();

    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(3, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(1, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(2, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());
    
    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    subject = newSubject;

    // ####################################################### refresh this subject
    
    // makes eligible for refresh
    subjectSourceCacheItem.setLastRetrieved(-1);
    
    assertTrue(subjectSourceCacheItem.expired());
    
    SubjectSourceCache.sweepCacheToResolveItemsThatNeedItLastRun = -1;
    SubjectSourceCache.sweepCacheToResolveItemsThatNeedIt();
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(3, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(2, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(0, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());
    
    // ####################################################### do not delete this subject
    
    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    subject = newSubject;

    // makes eligible for refresh
    assertFalse(subjectSourceCacheItem.expired());
    
    subjectSourceCacheItem.setNumberOfTimesAccessedSinceLastRetrieved(50);
    
    SubjectSourceCache.sweepCacheToDeleteLastRun = -1;
    SubjectSourceCache.sweepCacheToDeleteOldItemsIfNeeded();
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(3, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(2, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(50, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());
    
    // ####################################################### store to disk
    
    File parentStorage = new File(new File("").getAbsolutePath());
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subject.cache.serializer.directory", parentStorage.getAbsolutePath());
    SubjectSourceCache.writeCacheToStorageLastRun = -1;

    //ok we have the directory, delete the files
    File[] files = parentStorage.listFiles(SubjectSourceSerializerFile.serializerFileFilter());

    for (File file : GrouperUtil.nonNull(files, File.class)) {
      
      GrouperUtil.deleteFile(file);
      
    }

    SubjectSourceCache.writeCacheToStorage();

    //ok we have the directory, see the files
    files = parentStorage.listFiles(SubjectSourceSerializerFile.serializerFileFilter());

    assertEquals(1, GrouperUtil.length(files));
    assertTrue(files[0].length() > 0);
    
    // ####################################################### delete this subject
    
    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    subject = newSubject;

    subjectSourceCacheItem.setLastRetrieved(-1);
    subjectSourceCacheItem.setNumberOfTimesAccessedSinceLastRetrieved(15);
    
    // makes eligible for refresh
    assertTrue(subjectSourceCacheItem.expired());
    
    SubjectSourceCache.sweepCacheToDeleteLastRun = -1;
    SubjectSourceCache.sweepCacheToDeleteOldItemsIfNeeded();
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);

    assertEquals(0, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(0, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().remove("subject.cache.maxElementsInMemory");

    // ####################################################### read from disk
    
    SubjectSourceCache.readCacheFromStorageOnStartup();

    SubjectConfig.retrieveConfig().propertiesOverrideMap().remove("subject.cache.serializer.directory");
    
    //remove that cache file
    GrouperUtil.deleteFile(files[0]);
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();
    
    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);

    assertEquals(2, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "test.subject.8")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "test.subject.8"));
    
    assertEquals(2, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "test.subject.8")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "identifier", "id.test.subject.8")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "test.subject.8"));
    assertNotNull(subjectSourceCacheItem);
    assertEquals(subjectSourceCacheItem, SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "identifier", "id.test.subject.8")));
    
    assertEquals(3, subjectSourceCacheItem.getNumberOfTimesAccessed());
    assertEquals(2, subjectSourceCacheItem.getNumberOfTimesRetrieved());
    assertEquals(50, subjectSourceCacheItem.getNumberOfTimesAccessedSinceLastRetrieved());

    // ####################################################### negative cache

    SubjectSourceCache.clearCache();

    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();

    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    cacheMisses = SubjectSourceCache.statsCurrent.getCacheMissIndividual();
    
    // makes eligible for refresh
    grouperSession.stop();
    GrouperSession.start(SubjectTestHelper.SUBJ0);

    newSubject = SubjectFinder.findByIdAndSource("qwertyu", "jdbc", false);
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();

    assertEquals("cache same", SubjectSourceCache.statsCurrent.getCacheHit(), cacheHits);
    assertEquals("cache same", cacheMisses + 1, SubjectSourceCache.statsCurrent.getCacheMissIndividual());

    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "qwertyu")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "qwertyu"));
    
    assertEquals(1, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "qwertyu")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "qwertyu")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "qwertyu"));

    cacheHits = SubjectSourceCache.statsCurrent.getCacheHit();
    cacheMisses = SubjectSourceCache.statsCurrent.getCacheMissIndividual();
    
    grouperSession.stop();
    GrouperSession.start(SubjectTestHelper.SUBJ1);

    newSubject = SubjectFinder.findByIdAndSource("qwertyu", "jdbc", false);
    
    SubjectSourceCache.logStatsLastRun = -1;
    SubjectSourceCache.logStats();

    assertEquals("cache same", cacheHits + 1, SubjectSourceCache.statsCurrent.getCacheHit());
    assertEquals("cache same", cacheMisses, SubjectSourceCache.statsCurrent.getCacheMissIndividual());

    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectCache));
    assertEquals(1, GrouperUtil.length(SubjectSourceCache.subjectKeyCache));

    assertTrue(SubjectSourceCache.subjectKeyCache.containsKey(new MultiKey("jdbc", "qwertyu")));
    subjectCacheKeys = SubjectSourceCache.subjectKeyCache.get(new MultiKey("jdbc", "qwertyu"));
    
    assertEquals(1, GrouperUtil.length(subjectCacheKeys));
    
    assertTrue(subjectCacheKeys.contains(new MultiKey("jdbc", "id", "qwertyu")));
    assertTrue(SubjectSourceCache.subjectCache.keySet().contains(new MultiKey("jdbc", "id", "qwertyu")));

    subjectSourceCacheItem = SubjectSourceCache.subjectCache.get(new MultiKey("jdbc", "id", "qwertyu"));

  }
}
