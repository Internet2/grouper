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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.azure.AzureProvisionerTestConfigInput;
import edu.internet2.middleware.grouper.app.azure.AzureProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.SaveMode;
import junit.textui.TestRunner;


/**
 *
 */
public class GroupDoNotDeleteIfProvisionableTest extends GrouperTest {

  /**
   * @param name
   */
  public GroupDoNotDeleteIfProvisionableTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GroupDoNotDeleteIfProvisionableTest.class);
    
//    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
//        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerGroupId = :theOwnerGroupId and attributeAssignTypeDb = 'group'")
//        .setString("theAttributeDefNameId", "52b4589f5779432b917be27f5e7b6970")
//        .setString("theOwnerGroupId", "1793639816ea43c2980371cb26431a48")
//        .setCacheable(false)
//        .listSet(AttributeAssign.class);
//    
//    System.out.println("attributeAssigns size: " + attributeAssigns.size());
//    System.out.println("attributeAssigns: " + GrouperUtil.toStringForLog(attributeAssigns));
    
    System.exit(0);
  }


  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    //dont have the test hook implementation anymore
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
    super.tearDown();
  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHook() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // myAzureProvisioner
    AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
        .assignGroupAttributeCount(5)
        );

    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem);

    // ###### group in a provisionable stem
    Group group = new GroupSave(grouperSession).assignName("test:testProvisionable").save();
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GroupDoNotDeleteIfProvisionable.class); 
    try {
      // provisionable, so should veto
      new GroupSave(grouperSession).assignName(group.getName()).assignSaveMode(SaveMode.DELETE).save();
      
      fail("Should fail");

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupDoNotDeleteIfProvisionable.EXTERNALIZED_TEXT_KEY_FOR_DO_NOT_DELETE_GROUP_IF_PROVISIONABLE);
    }

    // ###### group not in a provisionable stem
    Stem testStem2 = new StemSave(grouperSession).assignName("test2").save();
    group = new GroupSave(grouperSession).assignName("test2:testNotProvisionable").save();
    // not provisionable, so should be able to delete
    new GroupSave(grouperSession).assignName(group.getName()).assignSaveMode(SaveMode.DELETE).save();
    
    // ###### group in a provisionable stem but not provisionable itself
    group = new GroupSave(grouperSession).assignName("test:testNotProvisionable").save();
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setTargetName("myAzureProvisioner");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);

    // not provisionable, so should be able to delete
    new GroupSave(grouperSession).assignName(group.getName()).assignSaveMode(SaveMode.DELETE).save();
    
    // ###### stem in a provisionable stem but not provisionable itself
    Stem testStem3 = new StemSave(grouperSession).assignName("test:testNotProvisionableStem").save();
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setTargetName("myAzureProvisioner");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem3);
    Group group3 = new GroupSave(grouperSession).assignName("test:testNotProvisionableStem:testNotProvisionable").save();
    // not provisionable, so should be able to delete
    new GroupSave(grouperSession).assignName(group3.getName()).assignSaveMode(SaveMode.DELETE).save();
    
    // ####### group in a non provisionable stem but provisionable itself
    group = new GroupSave(grouperSession).assignName("test:testNotProvisionableStem:testProvisionable").save();
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);
    try {
      // provisionable, so should veto
      new GroupSave(grouperSession).assignName(group.getName()).assignSaveMode(SaveMode.DELETE).save();
      
      fail("Should fail");

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupDoNotDeleteIfProvisionable.EXTERNALIZED_TEXT_KEY_FOR_DO_NOT_DELETE_GROUP_IF_PROVISIONABLE);
    }
    
  }

}
