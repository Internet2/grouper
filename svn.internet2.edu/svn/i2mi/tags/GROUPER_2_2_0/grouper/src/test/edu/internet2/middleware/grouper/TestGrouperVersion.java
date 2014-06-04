/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: TestGrouperVersion.java,v 1.1 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.misc.GrouperVersion;



/**
 *
 */
public class TestGrouperVersion extends TestCase {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new GrouperVersionTest("testIndentJson"));
    TestRunner.run(TestGrouperVersion.class);
  }

  /**
   * @param name
   */
  public TestGrouperVersion(String name) {
    super(name);
  }

  /**
   * make sure versions are compared right
   */
  public void testVersions() {
    
    assertEquals(new GrouperVersion("v1.2.3"), new GrouperVersion("1.2.3"));

    //cli   ser   ok?
    //1.5.2 1.5.2 T 
    //1.5.2 1.5.1 T 
    //1.5.1 1.5.2 T 
    //1.5.2 1.4.0 (released before 1.5.2) F
    //1.5.2 1.4.5 (released after 1.5.2) F 
    //1.4.0 1.5.2 (released before 1.5.2) T
    //1.4.5 1.5.2 (released after 1.5.2) T 
    //1.6.0 1.5.2 F 
    //1.5.2 1.6.0 T 
    //2.0.0 1.5.2 F 
    //1.5.2 2.0.0 T 

    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_5_002").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_002"), true));
    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_5_002").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_001"), true));
    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_5_001").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_002"), true));
    assertFalse(GrouperVersion.valueOfIgnoreCase("v1_5_002").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"), true));
    assertFalse(GrouperVersion.valueOfIgnoreCase("v1_5_002").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_4_005"), true));
    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_4_000").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_002"), true));
    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_4_005").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_002"), true));
    assertFalse(GrouperVersion.valueOfIgnoreCase("v1_6_000").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_002"), true));
    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_5_002").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_6_000"), true));
    assertFalse(GrouperVersion.valueOfIgnoreCase("v2_0_000").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v1_5_002"), true));
    assertTrue(GrouperVersion.valueOfIgnoreCase("v1_5_002").lessThanMajorMinorArg(GrouperVersion.valueOfIgnoreCase("v2_0_000"), true));
    
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
