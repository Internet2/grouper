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
 * $Id: GroupAttributeNameValidationHookTest.java,v 1.2 2009-03-24 17:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 * test unique object hook
 */
public class UniqueObjectHookTest extends GrouperTest {

  /**
   * @param name
   */
  public UniqueObjectHookTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new UniqueObjectHookTest("testUniqueGroupObjectHook"));
    TestRunner.run(UniqueObjectHookTest.class);
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), UniqueObjectGroupHook.class);
    GrouperHookType.addHookOverride(GrouperHookType.STEM.getPropertyFileKey(), UniqueObjectStemHook.class);
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF.getPropertyFileKey(), UniqueObjectAttributeDefHook.class);
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF_NAME.getPropertyFileKey(), UniqueObjectAttributeDefNameHook.class);
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
    GrouperHookType.addHookOverride(GrouperHookType.STEM.getPropertyFileKey(), (Class<?>)null);
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF.getPropertyFileKey(), (Class<?>)null);
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF_NAME.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();
  
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * test unique group names
   * @throws Exception
   */
  public void testUniqueGroupObjectHook() throws Exception {

    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //this should add
    String testObjectName = "test:testObject";
    Group testGroup = new GroupSave(grouperSession).assignName(testObjectName).assignCreateParentStemsIfNotExist(true).save();
   
    Stem test = StemFinder.findByName(grouperSession, testGroup.getParentStemName(), true);

    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //lets do this stuff as another user
    
    try {
      new StemSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    try {
      new AttributeDefSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }

    AttributeDef attributeDefForName = new AttributeDefSave(grouperSession).assignAttributeDefType(AttributeDefType.attr).assignName("test:defNameName").save();

    try {
      new AttributeDefNameSave(grouperSession, attributeDefForName).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    GrouperSession.stopQuietly(grouperSession);
  
  }

  /**
   * test unique stem names
   * @throws Exception
   */
  public void testUniqueStemObjectHook() throws Exception {

    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //this should add
    String testObjectName = "test:testObject";
    Stem testStem = new StemSave(grouperSession).assignName(testObjectName).assignCreateParentStemsIfNotExist(true).save();

    Stem test = StemFinder.findByName(grouperSession, testStem.getParentStemName(), true);

    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //lets do this stuff as another user
    
    try {
      new GroupSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    try {
      new AttributeDefSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }

    AttributeDef attributeDefForName = new AttributeDefSave(grouperSession).assignAttributeDefType(AttributeDefType.attr).assignName("test:defNameName").save();

    try {
      new AttributeDefNameSave(grouperSession, attributeDefForName).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    GrouperSession.stopQuietly(grouperSession);
  
  }


  /**
   * test unique attributeDef names
   * @throws Exception
   */
  public void testUniqueAttributeDefObjectHook() throws Exception {

    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //this should add
    String testObjectName = "test:testObject";
    AttributeDef testAttributeDef = new AttributeDefSave(grouperSession).assignName(testObjectName).assignCreateParentStemsIfNotExist(true)
        .assignAttributeDefType(AttributeDefType.attr).save();

    Stem test = StemFinder.findByName(grouperSession, testAttributeDef.getParentStem().getName(), true);

    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //lets do this stuff as another user
    
    try {
      new GroupSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    try {
      new StemSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }

    AttributeDef attributeDefForName = new AttributeDefSave(grouperSession).assignAttributeDefType(AttributeDefType.attr).assignName("test:defNameName").save();

    try {
      new AttributeDefNameSave(grouperSession, attributeDefForName).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    GrouperSession.stopQuietly(grouperSession);
  
  }


  /**
   * test unique names of attributeDefName 
   * @throws Exception
   */
  public void testUniqueAttributeDefNameObjectHook() throws Exception {

    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //this should add
    String testObjectName = "test:testObject";
    AttributeDef attributeDefForName = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.attr).assignName("test:defNameName").save();
    AttributeDefName testAttributeDefName = new AttributeDefNameSave(grouperSession, attributeDefForName)
      .assignName(testObjectName).assignCreateParentStemsIfNotExist(true).save();

    Stem test = StemFinder.findByName(grouperSession, testAttributeDefName.getStem().getName(), true);

    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    test.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //lets do this stuff as another user
    
    try {
      new GroupSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    try {
      new StemSave(grouperSession).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }


    try {
      new AttributeDefSave(grouperSession).assignAttributeDefType(AttributeDefType.attr).assignName(testObjectName).save();
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    GrouperSession.stopQuietly(grouperSession);
  
  }

  /**
   * test create objects without names
   * @throws Exception
   */
  public void testNonUniqueNames() throws Exception {

    overrideHooksRemove();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    //this should add
    String testObjectName = "test:testObject";
    AttributeDef attributeDefForName = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.attr).assignName("test:defNameName").save();

    new AttributeDefNameSave(grouperSession, attributeDefForName)
      .assignName(testObjectName).assignCreateParentStemsIfNotExist(true).save();

    new GroupSave(grouperSession).assignName(testObjectName).save();

    new StemSave(grouperSession).assignName(testObjectName).save();

    new AttributeDefSave(grouperSession).assignAttributeDefType(AttributeDefType.attr).assignName(testObjectName).save();

    GrouperSession.stopQuietly(grouperSession);

  }

}
