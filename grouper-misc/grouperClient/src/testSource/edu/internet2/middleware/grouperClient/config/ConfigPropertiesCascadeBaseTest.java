package edu.internet2.middleware.grouperClient.config;

import java.util.Date;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * 
 * @author mchyzer
 */
public class ConfigPropertiesCascadeBaseTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ConfigPropertiesCascadeBaseTest("testAutoReload"));
  }
  
  /**
   * 
   * @param name
   */
  public ConfigPropertiesCascadeBaseTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testOverrideHasHierarchy() {
    
    //example:
    //test1 = something
    //test2 = somethingElse
    
    //override:
    //test2 = somethingElse2
    //test3 = yet another something
    assertEquals("something", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test1"));
    assertEquals("somethingElse2", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test2"));
    assertEquals("yet another something", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test3"));
    assertNull(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("test4", null));
    try {
      ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test4");
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * 
   */
  public void testOriginalHasHierarchy() {
    
    //base:
    //test1 = somethingA
    //test2 = somethingElseA

    //test2 = somethingElse2A
    //test3 = yet another somethingA
    assertEquals("somethingA", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test1"));
    assertEquals("somethingElse2A", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test2"));
    assertEquals("yet another somethingA", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test3"));
    assertNull(ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueString("test4", null));
    try {
      ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test4");
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * 
   */
  public void testAutoReload() {
    
    for (int i=0;i<60;i++) {
      
      System.out.println("Last reloaded: " + new Date(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getCreatedTime()) + ", last checked: " + new Date(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getLastCheckedTime()));
      System.out.println("Property: testAutoReload: " + ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("testAutoReload", null));
      GrouperClientUtils.sleep(1000);
    }
    
  }
  
}
