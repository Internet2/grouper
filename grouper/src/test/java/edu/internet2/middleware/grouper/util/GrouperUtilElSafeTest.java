/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: GrouperUtilTest.java,v 1.16 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 *
 */
public class GrouperUtilElSafeTest extends GrouperTest {
  
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperUtilElSafeTest("testSplitCurlyColons"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
  }
 
  /**
   * 
   */
  public GrouperUtilElSafeTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperUtilElSafeTest(String name) {
    super(name);
    
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUtilElSafeTest.class);

  /**
   * {jobCategory=Staff}:{campus=UM_ANN-ARBOR}:{deptId=316033}:{deptGroup=UM_HOSPITAL}:{deptDescription=MEDICAL SHORT STAY UNIT}:{deptGroupDescription=Univ Hospitals & Health Center}:{deptVPArea=EXEC_VP_MED_AFF}:{jobcode=278040}:{jobFamily=31}:{emplStatus=A}:{regTemp=R}:{supervisorId=44272654}:{tenureStatus=NA}:{jobIndicator=P}
   */
  public void testSplitCurlyColons() {
    
    String input = "{jobCategory=Staff}:{campus=UM_ANN-ARBOR}:{deptId=316033}:{deptGroup=UM_HOSPITAL}:{deptDescription=MEDICAL SHORT STAY UNIT}:{deptDescription=SOME OTHER UNIT}:{deptGroupDescription=Univ Hospitals & Health Center}:{deptVPArea=EXEC_VP_MED_AFF}:{jobcode=278040}:{jobFamily=31}:{emplStatus=A}:{regTemp=R}:{supervisorId=44272654}:{tenureStatus=NA}:{jobIndicator=P}";
    
    List<String> values = GrouperUtilElSafe.splitTrimCurlyColons(input, "deptDescription", "[a-zA-Z0-9_]", "_");
    
    assertEquals(2, GrouperUtil.length(values));
    
    assertContainsString(values, "MEDICAL_SHORT_STAY_UNIT");
    assertContainsString(values, "SOME_OTHER_UNIT");
    
  }
}
