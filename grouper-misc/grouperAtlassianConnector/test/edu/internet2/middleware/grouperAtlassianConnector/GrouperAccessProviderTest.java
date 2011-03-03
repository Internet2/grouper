/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 *
 */
public class GrouperAccessProviderTest extends TestCase {

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperAccessProviderTest.class);

  /**
   * 
   */
  static final String TEST_USERNAME;
  
  static {
    try {
      TEST_USERNAME = GrouperClientUtils.propertiesValue("atlassian.test.subjectIdOrIdentifier", true);
    } catch (RuntimeException re) {
      LOG.error("Error getting subject, config param: atlassian.test.subjectIdOrIdentifier in grouper.client.properties", re);
      throw re;
    }
  }
  
  /**
   * 
   */
  private static final String JUNIT_TEST_GROUP = "junitTestGroup";

  /**
   * 
   */
  public GrouperAccessProviderTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperAccessProviderTest(String name) {
    super(name);
    
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperAccessProviderTest("testLoadClearCacheInFuture"));
    runXmpp();
  }

  /** access provider */
  private GrouperAccessProvider grouperAccessProvider = new GrouperAccessProvider();
  
  /**
   * run XMPP to manually test
   */
  private static void runXmpp() {
    new GrouperAccessProvider().list();
    
    //sleep for two hours while waiting for grouper updates and XMPP stuff
    GrouperClientUtils.sleep(1000 * 60 * 60 * 2);
    
  }
  
  /**
   * 
   */
  public void testHandles() {

    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    //they both go up
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 3, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;
    this.grouperAccessProvider.flushCaches();

    assertFalse(this.grouperAccessProvider.handles("whataslkfdjasldkfj"));

    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertFalse(this.grouperAccessProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.handles(TEST_USERNAME));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.handles(TEST_USERNAME));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperAccessProvider().flushCaches();
    
    assertTrue(this.grouperAccessProvider.handles(TEST_USERNAME));
    assertTrue(GrouperAccessProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperAccessProvider.cacheMisses;
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperAccessProvider.handles(TEST_USERNAME));
    
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperAccessProvider.cacheFailsafeHits);
    
  }
  
  /**
   * 
   */
  public void testLoadClearCacheInFuture() {
    
    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    GrouperAccessProvider.scheduleNextCacheRefreshMillis = -1;
    
    long flushCacheMillis = System.currentTimeMillis() + (3 * 1000);
    
    assertFalse(GrouperAccessProvider.cacheShouldBeClearedNow());
    assertFalse(GrouperAccessProvider.cacheWillBeClearedInFuture());
    
    //10 seconds in the future
    GrouperAccessProvider.flushCaches(flushCacheMillis);

    assertFalse(GrouperAccessProvider.cacheShouldBeClearedNow());
    assertTrue(GrouperAccessProvider.cacheWillBeClearedInFuture());

    long lastCacheRefreshMillis = GrouperAccessProvider.lastCacheRefreshMillis;
    
    assertEquals(flushCacheMillis, GrouperAccessProvider.scheduleNextCacheRefreshMillis);
    
    GrouperAccessProvider.flushCaches(flushCacheMillis + 10);
    
    //should ignore this
    assertEquals(flushCacheMillis, GrouperAccessProvider.scheduleNextCacheRefreshMillis);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    //run a query, should be cached
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    //should not have refreshed
    assertEquals(lastCacheRefreshMillis, GrouperAccessProvider.lastCacheRefreshMillis);
    
    //wait 5 seconds
    GrouperClientUtils.sleep(5000);
    
    assertTrue(GrouperAccessProvider.cacheShouldBeClearedNow());
    assertTrue(GrouperAccessProvider.cacheWillBeClearedInFuture());
    
    //now should clear the cache
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;
    
    //run a query, should be cached
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);
    
    //should be refreshed
    assertTrue(lastCacheRefreshMillis < GrouperAccessProvider.lastCacheRefreshMillis);

    assertFalse(GrouperAccessProvider.cacheShouldBeClearedNow());
    assertFalse(GrouperAccessProvider.cacheWillBeClearedInFuture());
    
  }
  
  /**
   * 
   */
  public void testLoad() {
    
    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits > GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    this.grouperAccessProvider.flushCaches();
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperAccessProvider().flushCaches();
    
    assertTrue(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertTrue(GrouperAccessProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperAccessProvider.cacheMisses;
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperAccessProvider.cacheFailsafeHits);

    
  }
  

  /**
   * 
   */
  public void testListUsersInGroup() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    new GrouperAccessProvider().flushCaches();
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertFalse(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(cacheHits < GrouperAccessProvider.cacheHits);

    this.grouperAccessProvider.addToGroup(TEST_USERNAME, JUNIT_TEST_GROUP);
    
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;
    new GrouperAccessProvider().flushCaches();

    assertTrue(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertTrue(cacheMisses < GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperAccessProvider().flushCaches();
    
    assertTrue(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertTrue(GrouperAccessProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperAccessProvider.cacheMisses;
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperAccessProvider.cacheFailsafeHits);

  }
  
  /**
   * 
   */
  public void testListGroupsContainingUser() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.listGroupsContainingUser(TEST_USERNAME).contains(JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.listGroupsContainingUser(TEST_USERNAME).contains(JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    this.grouperAccessProvider.addToGroup(TEST_USERNAME, JUNIT_TEST_GROUP);
    
    this.grouperAccessProvider.flushCaches();
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.listGroupsContainingUser(TEST_USERNAME).contains(JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.listGroupsContainingUser(TEST_USERNAME).contains(JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperAccessProvider().flushCaches();
    
    assertTrue(this.grouperAccessProvider.listGroupsContainingUser(TEST_USERNAME).contains(JUNIT_TEST_GROUP));
    assertTrue(GrouperAccessProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperAccessProvider.cacheMisses;
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperAccessProvider.listGroupsContainingUser(TEST_USERNAME).contains(JUNIT_TEST_GROUP));
    
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperAccessProvider.cacheFailsafeHits);

    
  }
  

  /**
   * 
   */
  public void testInGroup() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.inGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.inGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    this.grouperAccessProvider.addToGroup(TEST_USERNAME, JUNIT_TEST_GROUP);
    
    this.grouperAccessProvider.flushCaches();
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.inGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.inGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    
    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperAccessProvider().flushCaches();
    
    assertTrue(this.grouperAccessProvider.inGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertTrue(GrouperAccessProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperAccessProvider.cacheMisses;
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperAccessProvider.inGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperAccessProvider.cacheFailsafeHits);

    
  }
  
  /**
   * 
   */
  public void testList() {
    
    this.grouperAccessProvider.flushCaches();
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;

    assertFalse(this.grouperAccessProvider.list().contains(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);
    
    assertFalse(this.grouperAccessProvider.list().contains(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));
    
    this.grouperAccessProvider.flushCaches();
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.list().contains(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);
    
    assertTrue(this.grouperAccessProvider.list().contains(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);
    
    
    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperAccessProvider().flushCaches();
    
    assertTrue(this.grouperAccessProvider.list().contains(JUNIT_TEST_GROUP));
    assertTrue(GrouperAccessProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperAccessProvider.cacheMisses;
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheFailsafeHits = GrouperAccessProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperAccessProvider.list().contains(JUNIT_TEST_GROUP));
    
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperAccessProvider.cacheFailsafeHits);


  }
  
  /**
   * 
   */
  public void testCreateRemove() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));
    assertFalse(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));
    
    assertTrue(this.grouperAccessProvider.remove(JUNIT_TEST_GROUP));
    assertFalse(this.grouperAccessProvider.remove(JUNIT_TEST_GROUP));
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    
    try {
      this.grouperAccessProvider.remove(JUNIT_TEST_GROUP);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = false;
    assertTrue(this.grouperAccessProvider.remove(JUNIT_TEST_GROUP));
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = true;
    
    try {
      this.grouperAccessProvider.create(JUNIT_TEST_GROUP);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = false;
    this.grouperAccessProvider.remove(JUNIT_TEST_GROUP);
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = false;
  }

  /**
   * 
   */
  public void testAddRemoveMember() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    assertTrue(this.grouperAccessProvider.addToGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertFalse(this.grouperAccessProvider.addToGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    
    assertTrue(this.grouperAccessProvider.removeFromGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    assertFalse(this.grouperAccessProvider.removeFromGroup(TEST_USERNAME, JUNIT_TEST_GROUP));
    
    
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = false;
    this.grouperAccessProvider.remove(JUNIT_TEST_GROUP);
    
  }
  
}
