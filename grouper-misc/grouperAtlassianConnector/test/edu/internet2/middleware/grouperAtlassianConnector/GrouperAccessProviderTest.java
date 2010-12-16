/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperAccessProviderTest extends TestCase {

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
    TestRunner.run(new GrouperAccessProviderTest("testCreate"));
  }

  /** access provider */
  private GrouperAccessProvider grouperAccessProvider = new GrouperAccessProvider();
  
  /**
   * 
   */
  public void testCreateRemove() {
    
    this.grouperAccessProvider.remove("junitTestGroup");
    
    assertTrue(this.grouperAccessProvider.create("junitTestGroup"));
    assertFalse(this.grouperAccessProvider.create("junitTestGroup"));
    
    assertTrue(this.grouperAccessProvider.remove("junitTestGroup"));
    assertFalse(this.grouperAccessProvider.remove("junitTestGroup"));
  }
  
}
