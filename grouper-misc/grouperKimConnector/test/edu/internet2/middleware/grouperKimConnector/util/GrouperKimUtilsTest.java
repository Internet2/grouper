/**
 * @author mchyzer
 * $Id: GrouperKimUtilsTest.java,v 1.2 2009-12-20 18:03:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperKimUtilsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(GrouperKimUtilsTest.class);
    TestRunner.run(new GrouperKimUtilsTest("testFirstLastMiddleName"));
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
  protected void tearDown() {
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
  
  /**
   * 
   */
  public void testFirstLastMiddleName() {
    assertEquals("Hyzer", GrouperKimUtils.lastName("Hyzer"));
    assertEquals(null, GrouperKimUtils.firstName("Hyzer"));
    assertEquals(null, GrouperKimUtils.middleName("Hyzer"));

    assertEquals("Hyzer", GrouperKimUtils.lastName("Chris Hyzer"));
    assertEquals("Chris", GrouperKimUtils.firstName("Chris Hyzer"));
    assertEquals(null, GrouperKimUtils.middleName("Chris Hyzer"));
  
    assertEquals("Hyzer", GrouperKimUtils.lastName("Chris M. Hyzer"));
    assertEquals("Chris", GrouperKimUtils.firstName("Chris M. Hyzer"));
    assertEquals("M.", GrouperKimUtils.middleName("Chris M. Hyzer"));

    assertEquals("Hyzer", GrouperKimUtils.lastName("Chris M. C. Hyzer"));
    assertEquals("Chris", GrouperKimUtils.firstName("Chris M. C. Hyzer"));
    assertEquals("M. C.", GrouperKimUtils.middleName("Chris M. C. Hyzer"));
}

}
