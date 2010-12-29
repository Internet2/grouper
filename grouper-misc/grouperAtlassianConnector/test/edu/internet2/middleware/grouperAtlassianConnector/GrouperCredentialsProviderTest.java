package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


public class GrouperCredentialsProviderTest extends TestCase {
  /** profile provider */
  private GrouperCredentialsProvider grouperCredentialsProvider = new GrouperCredentialsProvider();
  

  /**
   * 
   * @param name
   */
  public GrouperCredentialsProviderTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperCredentialsProviderTest("testList"));
  }

  /**
   * make sure these throw exceptions
   */
  public void testCreateRemoveStore() {
    try {
      this.grouperCredentialsProvider.create("whatever");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperCredentialsProvider.remove("whatever");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperCredentialsProvider.store("whatever", null);
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperCredentialsProvider.authenticate("whatever", "yo");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperCredentialsProvider.changePassword("whatever", "yo");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

  }

  /**
   * 
   */
  public void testList() {
    
    this.grouperCredentialsProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    List<String> userIds = this.grouperCredentialsProvider.list();
    
    assertEquals(cacheHits, GrouperAccessProvider.cacheHits);
    assertTrue(GrouperAccessProvider.cacheMisses > cacheMisses);

    assertTrue(GrouperClientUtils.length(userIds) > 0);
    assertTrue(userIds.contains(GrouperAccessProviderTest.TEST_USERNAME));

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;
    
    userIds = this.grouperCredentialsProvider.list();
    
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);

    
    assertTrue(GrouperClientUtils.length(userIds) > 0);
    assertTrue(userIds.contains(GrouperAccessProviderTest.TEST_USERNAME));

    
  }
  
  /**
   * 
   */
  public void testHandles() {

    this.grouperCredentialsProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperCredentialsProvider.handles("whataslkfdjasldkfj"));

    assertTrue(GrouperAccessProvider.cacheMisses + ", " + cacheMisses, GrouperAccessProvider.cacheMisses > cacheMisses);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertFalse(this.grouperCredentialsProvider.handles("whataslkfdjasldkfj"));
    
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperCredentialsProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);

    this.grouperCredentialsProvider.flushCaches();

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperCredentialsProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertTrue(GrouperAccessProvider.cacheMisses + ", " + cacheMisses, GrouperAccessProvider.cacheMisses > cacheMisses);
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperCredentialsProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);

  }

  /**
   * 
   */
  public void testLoad() {
  
    this.grouperCredentialsProvider.flushCaches();
    
    long cacheHits = GrouperAccessProvider.cacheHits;
    long cacheMisses = GrouperAccessProvider.cacheMisses;
    
    assertFalse(this.grouperCredentialsProvider.load("whataslkfdjasldkfj", null));

    assertTrue(GrouperAccessProvider.cacheMisses + ", " + cacheMisses, GrouperAccessProvider.cacheMisses > cacheMisses);

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertFalse(this.grouperCredentialsProvider.load("whataslkfdjasldkfj", null));
    
    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperCredentialsProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);

    this.grouperCredentialsProvider.flushCaches();

    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperCredentialsProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));

    assertTrue(GrouperAccessProvider.cacheMisses + ", " + cacheMisses, GrouperAccessProvider.cacheMisses > cacheMisses);
    cacheHits = GrouperAccessProvider.cacheHits;
    cacheMisses = GrouperAccessProvider.cacheMisses;

    assertTrue(this.grouperCredentialsProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));

    assertEquals(cacheMisses, GrouperAccessProvider.cacheMisses);
    assertTrue(GrouperAccessProvider.cacheHits > cacheHits);
  
  }

  /**
   * 
   */
  @Override
  protected void tearDown() throws Exception {
    
    super.tearDown();
    GrouperAccessProvider.failOnGrouperForTestingFailsafeCache = false;
  }

}
