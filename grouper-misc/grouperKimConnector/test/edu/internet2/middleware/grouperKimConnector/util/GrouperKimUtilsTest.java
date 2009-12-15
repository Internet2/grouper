/**
 * @author mchyzer
 * $Id: GrouperKimUtilsTest.java,v 1.1 2009-12-15 17:07:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperKimUtilsTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperKimUtilsTest.class);
  }

  /**
   * 
   */
  public GrouperKimUtilsTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperKimUtilsTest(String name) {
    super(name);
    
  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    GrouperClientUtils.grouperClientOverrideMap().clear();
  }

  /**
   * 
   */
  public void testCalculateNamespaceCode() {
    GrouperClientUtils.grouperClientOverrideMap().put("kim.stem", "a:b");
    assertEquals("c", GrouperKimUtils.calculateNamespaceCode("a:b:c:d"));
    assertEquals(null, GrouperKimUtils.calculateNamespaceCode("a:b:c"));
  }
  
}
