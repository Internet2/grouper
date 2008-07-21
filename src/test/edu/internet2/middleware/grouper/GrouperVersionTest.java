/*
 * @author mchyzer
 * $Id: GrouperVersionTest.java,v 1.2 2008-07-21 04:43:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 *
 */
public class GrouperVersionTest extends TestCase {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new GrouperVersionTest("testIndentJson"));
    TestRunner.run(GrouperVersionTest.class);
  }

  /**
   * @param name
   */
  public GrouperVersionTest(String name) {
    super(name);
  }

  /**
   * make sure versions are compared right
   */
  public void testVersions() {
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.1", "3.0.0"));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.1", "2.2.2"));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.1.1", "3.0.2"));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.1.1", "3.1.1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.0", "3.0.1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("2.2.2", "3.0.1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2", "3.1.1"));
    
  }
  
}
