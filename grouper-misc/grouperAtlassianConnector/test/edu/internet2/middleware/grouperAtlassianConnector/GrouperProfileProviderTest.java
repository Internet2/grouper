package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.List;

import com.opensymphony.module.propertyset.PropertySet;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

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
    TestRunner.run(new GrouperProfileProviderTest("testGetPropertySet"));
  }

  /**
   * 
   */
  public void testGetPropertySet() {
    
    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    PropertySet propertySet = this.grouperProfileProvider.getPropertySet("whataslkfdjasldkfj");
    
    assertNull(propertySet);
        
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);

    propertySet = this.grouperProfileProvider.getPropertySet("whataslkfdjasldkfj");
    
    assertNull(propertySet);
        
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperProfileProvider.cacheHits);
    
    propertySet = this.grouperProfileProvider.getPropertySet(GrouperAccessProviderTest.TEST_USERNAME);
    
    assertNotNull(propertySet);

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 4, GrouperProfileProvider.cacheHits);
  
    assertEquals(GrouperClientUtils.propertiesValue("atlassian.test.email", true), propertySet.getString("email"));
    assertEquals(GrouperClientUtils.propertiesValue("atlassian.test.name", true), propertySet.getString("fullName"));
    
    //this should look again since not in list
    propertySet = this.grouperProfileProvider.getPropertySet("lkjrwetnsdf");
    
    assertNull(propertySet);
        
    assertEquals(cacheMisses + 3, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 5, GrouperProfileProvider.cacheHits);
    

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
  public void testList() {
    
    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    List<String> userIds = this.grouperProfileProvider.list();
    
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);
    
    assertTrue(GrouperClientUtils.length(userIds) > 0);
    assertTrue(userIds.contains(GrouperAccessProviderTest.TEST_USERNAME));

    userIds = this.grouperProfileProvider.list();
    
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperProfileProvider.cacheHits);
    
    assertTrue(GrouperClientUtils.length(userIds) > 0);
    assertTrue(userIds.contains(GrouperAccessProviderTest.TEST_USERNAME));

    
  }
  
  /**
   * 
   */
  public void testHandles() {

    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    assertFalse(this.grouperProfileProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);

    assertFalse(this.grouperProfileProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperProfileProvider.cacheHits);

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 4, GrouperProfileProvider.cacheHits);

    this.grouperProfileProvider.flushCaches();

    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperProfileProvider.cacheHits);

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 3, GrouperProfileProvider.cacheHits);

  }

  /**
   * 
   */
  public void testLoad() {
  
    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    assertFalse(this.grouperProfileProvider.load("whataslkfdjasldkfj", null));
  
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);
  
    assertFalse(this.grouperProfileProvider.load("whataslkfdjasldkfj", null));
  
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperProfileProvider.cacheHits);
  
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
  
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 4, GrouperProfileProvider.cacheHits);
  
    this.grouperProfileProvider.flushCaches();
  
    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;
  
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
  
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperProfileProvider.cacheHits);
  
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
  
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 3, GrouperProfileProvider.cacheHits);
  
  }

  
}
