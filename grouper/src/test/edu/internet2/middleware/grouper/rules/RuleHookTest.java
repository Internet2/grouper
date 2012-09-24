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
/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;


/**
 * test rule definitions
 * @author mchyzer
 *
 */
public class RuleHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleHookTest("testHookActAs"));
  }
  
  /**
   * 
   * @param name
   */
  public RuleHookTest(String name) {
    super(name);
  }

  /**
   * test hook
   */
  public void testHook() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignName("test:testGroup")
      .assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession).assignName("test:testGroup2")
      .assignCreateParentStemsIfNotExist(true).save();
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "jdbc");
    String validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    assertTrue(validReason, !"T".equals(validReason));
    
    //lets make it valid
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectTestHelper.SUBJ0_ID);
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), group2.getName());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    assertEquals("T", validReason);
    
    
  }

  /**
   * test hook
   */
  public void testHookActAs() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.act.as.group", "etc:rulesActAsGroup, etc:rulesActAsGroupA :::: etc:rulesActAsGroupB");

    
    String validReason = null;
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignName("test:testGroup")
      .assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession).assignName("test:testGroup2")
      .assignCreateParentStemsIfNotExist(true).save();

    Group actAsGroup = new GroupSave(grouperSession).assignName("etc:rulesActAsGroup").assignCreateParentStemsIfNotExist(true).save();
    Group actAsGroupA = new GroupSave(grouperSession).assignName("etc:rulesActAsGroupA").assignCreateParentStemsIfNotExist(true).save();
    Group actAsGroupB = new GroupSave(grouperSession).assignName("etc:rulesActAsGroupB").assignCreateParentStemsIfNotExist(true).save();
    
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(),AttributeDefPrivilege.ATTR_ADMIN, false);
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(),AttributeDefPrivilege.ATTR_ADMIN, false);
    
    actAsGroup.addMember(SubjectTestHelper.SUBJ5);
    actAsGroupA.addMember(SubjectTestHelper.SUBJ6);
    actAsGroupB.addMember(SubjectTestHelper.SUBJ7);
    

    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "jdbc");
    
    //lets make it valid
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectTestHelper.SUBJ0_ID);
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), group2.getName());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    
    //GrouperSession can act as anyone
    assertEquals("T", validReason);
    
    //#####################  subj4 cant act as subj0

    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name() + "abc");
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    //subj4 cant act as subj0
    assertTrue(!StringUtils.isBlank(validReason));

    
    //#####################  subj5 can act as subj0

    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ5);
    
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name() + "abc");
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertEquals(validReason, "T", validReason);
    
    //#####################  subj6 cant act as subj0

    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name() + "abc");
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    assertTrue(validReason, !"T".equals(validReason));
    
    //#####################  subj6 can act as subj7

    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectTestHelper.SUBJ7_ID);
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    assertEquals(validReason, "T", validReason);
    
  }

}
