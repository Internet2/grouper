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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class StemUniqueNameCaseInsensitiveHookTest extends GrouperTest {

  /**
   * @param name
   */
  public StemUniqueNameCaseInsensitiveHookTest(String name) {
    super(name);
  }

  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDef.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefName.validateExtensionByDefault", "false");
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(StemUniqueNameCaseInsensitiveHookTest.class);
    //TestRunner.run(new StemUniqueNameCaseInsensitiveHookTest(""));
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.STEM.getPropertyFileKey(), 
        StemUniqueNameCaseInsensitiveHook.class);
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.STEM.getPropertyFileKey(), (Class<?>)null);
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
    
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someStemName").save();

    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someStemNameExists").save();

    //get alternate name in there
    stem2.setExtension("someStemNameExists2", true);
    
    try {
      //alternate name
      new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someStemNamEExists").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), StemUniqueNameCaseInsensitiveHook.VETO_STEM_UNIQUE_ID_CASE_INSENSITIVE);
    }

    try {
      //name
      new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:someStemNamE").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), StemUniqueNameCaseInsensitiveHook.VETO_STEM_UNIQUE_ID_CASE_INSENSITIVE);
    }

    try {
      //rename to alternate name
      stem.setExtension("someStemNameExistS");
      stem.store();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), StemUniqueNameCaseInsensitiveHook.VETO_STEM_UNIQUE_ID_CASE_INSENSITIVE);
    }

    try {
      //rename to name
      stem.setExtension("someStemNameExistS2");
      stem.store();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), StemUniqueNameCaseInsensitiveHook.VETO_STEM_UNIQUE_ID_CASE_INSENSITIVE);
    }

  }

}
