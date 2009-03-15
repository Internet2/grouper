/*
 * @author mchyzer
 * $Id: GrouperVersionTest.java,v 1.5 2009-03-15 06:37:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.misc.GrouperVersion;



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
    
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc1", "3.1.0"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc1", "4.0.2rc1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.0rc1", "3.0.2rc1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc1", "3.0.2"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc1", "3.0.2rc2"));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.1.0", "3.0.2rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("4.0.2rc1", "3.0.2rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc1", "3.0.0rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2", "3.0.2rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc2", "3.0.2rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2rc1", "3.0.2rc1" ));

    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc1", "3.1.0"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc1", "4.0.2-rc1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.0-rc1", "3.0.2-rc1"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc1", "3.0.2"));
    assertFalse(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc1", "3.0.2-rc2"));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.1.0", "3.0.2-rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("4.0.2-rc1", "3.0.2-rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc1", "3.0.0-rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2", "3.0.2-rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc2", "3.0.2-rc1" ));
    assertTrue(GrouperVersion._grouperVersionGreaterOrEqualHelper("3.0.2-rc1", "3.0.2-rc1" ));
  }
  
}
