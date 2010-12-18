package edu.internet2.middleware.grouperAtlassianConnector;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperProfileProviderTest extends TestCase {

  /** profile provider */
  private GrouperProfileProvider grouperProfileProvider = new GrouperProfileProvider();
  

  /**
   * 
   * @param name
   */
  public GrouperProfileProviderTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProfileProviderTest("testCreateRemoveStore"));
  }

  /**
   * make sure these throw exceptions
   */
  public void testCreateRemoveStore() {
    try {
      this.grouperProfileProvider.create("whatever");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperProfileProvider.remove("whatever");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperProfileProvider.store("whatever", null);
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

  }

  /**
   * 
   */
  public void testHandles() {

    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperProfileProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertFalse(this.grouperProfileProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperAccessProvider.cacheHits);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperAccessProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperAccessProvider.cacheHits);

  }

  
}
