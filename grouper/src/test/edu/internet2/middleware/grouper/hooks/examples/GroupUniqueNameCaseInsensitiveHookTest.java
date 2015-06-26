/*******************************************************************************
 * Copyright 2015 Internet2
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
 * $Id: GroupAttributeNameValidationHookTest.java,v 1.2 2009-03-24 17:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class GroupUniqueNameCaseInsensitiveHookTest extends GrouperTest {

  /**
   * @param name
   */
  public GroupUniqueNameCaseInsensitiveHookTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GroupUniqueNameCaseInsensitiveHookTest.class);
    //TestRunner.run(new GroupUniqueNameCaseInsensitiveHookTest(""));
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GroupUniqueNameCaseInsensitiveHook.class);
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      overrideHooksAdd();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();
  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHook() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testStem2 = new StemSave(grouperSession).assignName("Test").save();
    
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someGroupName").save();

    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someGroupNameExists").save();

    //get alternate name in there
    group2.setExtension("someGroupNameExists2", true);
    
    try {
      //alternate name
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someGroupNamEExists").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueNameCaseInsensitiveHook.VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE);
    }

    try {
      //name
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someGroupNamE").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueNameCaseInsensitiveHook.VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE);
    }

    try {
      //name though stem
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("Test:someGroupName").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueNameCaseInsensitiveHook.VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE);
    }

    try {
      //rename to alternate name
      group.setExtension("someGroupNameExistS");
      group.store();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueNameCaseInsensitiveHook.VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE);
    }

    try {
      //rename to name
      group.setExtension("someGroupNameExistS2");
      group.store();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueNameCaseInsensitiveHook.VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE);
    }

  }

}
