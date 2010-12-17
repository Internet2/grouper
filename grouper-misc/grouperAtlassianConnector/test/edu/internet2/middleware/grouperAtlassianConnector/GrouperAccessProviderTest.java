/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import junit.framework.TestCase;
import junit.textui.TestRunner;


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
  private static final String TEST_USERNAME;
  
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
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperAccessProviderTest("testCreateRemove"));
  }

  /** access provider */
  private GrouperAccessProvider grouperAccessProvider = new GrouperAccessProvider();
  
  /**
   * 
   */
  public void testHandles() {

    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    //this goes up to 2 since it is cache miss for user/group
    assertEquals(cacheMisses + 2, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 2, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertFalse(this.grouperAccessProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 2, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 2, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 3, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.handles(JUNIT_TEST_GROUP));

    assertEquals(cacheMisses + 3, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.handles(TEST_USERNAME));

    assertEquals(cacheMisses + 5, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.handles(TEST_USERNAME));

    assertEquals(cacheMisses + 5, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 3, GrouperAccessProvider.cacheHits);

  }
  
  /**
   * 
   */
  public void testLoad() {
    
    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    this.grouperAccessProvider.flushCaches();
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.load(JUNIT_TEST_GROUP, null));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    
  }
  

  /**
   * 
   */
  public void testListUsersInGroup() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));

    this.grouperAccessProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    this.grouperAccessProvider.addToGroup(TEST_USERNAME, JUNIT_TEST_GROUP);
    
    this.grouperAccessProvider.flushCaches();
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperAccessProvider.listUsersInGroup(JUNIT_TEST_GROUP).contains(TEST_USERNAME));
    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    
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
    
    
    
  }
  
  /**
   * 
   */
  public void testCreateRemove() {
    
    assertTrue(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));
    assertFalse(this.grouperAccessProvider.create(JUNIT_TEST_GROUP));
    
    assertTrue(this.grouperAccessProvider.remove(JUNIT_TEST_GROUP));
    assertFalse(this.grouperAccessProvider.remove(JUNIT_TEST_GROUP));
  }
  
  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    this.grouperAccessProvider.remove(JUNIT_TEST_GROUP);
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
    this.grouperAccessProvider.remove(JUNIT_TEST_GROUP);
    
  }
  
}
