package edu.internet2.middleware.grouperClient.config;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import junit.framework.TestCase;
import junit.textui.TestRunner;

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
    TestRunner.run(new ConfigPropertiesCascadeBaseTest("testOverrideHasHierarchy"));
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
    
    //base:
    //test1 = something
    //test2 = somethingElse
    
    //test2 = somethingElse2
    //test3 = yet another something
//    assertEquals("something", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getProperty("test1"));
//    assertEquals("somethingElse", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getProperty("test2"));
//    assertEquals("yet another something", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getProperty("test3"));
  }
  
}
