/**
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
 */
/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.role.Role;


/**
 *
 */
public class RuleNameChangeTest extends GrouperTest {

  /** top level stems */
  private Stem edu, edu2, edu3;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleNameChangeTest("testCheckOwnerNameUpdateOnAttributeDefNameChange"));
  }
  
  /**
   * 
   */
  public RuleNameChangeTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleNameChangeTest(String name) {
    super(name);
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    edu2   = StemHelper.addChildStem(root, "edu2", "education2");
    edu3   = StemHelper.addChildStem(root, "edu3", "education3");
  }
  
  /**
   * 
   */
  public void testCheckOwnerNameUpdateOnAttributeDefNameChange() {

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu3.addChildAttributeDefName(attributeDef, "attributeDefName", "attributeDefName");
    Role role = edu3.addChildRole("role", "role");
    role.addMember(member0.getSubject(), true);
    AttributeAssign attributeAssign = edu3.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.permissionAssignToSubject.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerNameName(), attributeDef.getName());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "bogus");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "bogus");

    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //now rename the attributeDef..
    attributeDef.setExtensionDb("attributeDef-renamed");
    attributeDef.setNameDb("edu:attributeDef-renamed");
    attributeDef.store();

    //query to get the updated attribute
    attributeDefName = AttributeDefNameFinder.findById(attributeDefName.getId(), true);
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    try {
      role.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, member0.getSubject(), PermissionAllowed.ALLOWED);
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("edu:attributeDef-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));

    // now rename stem..
    edu.setExtension("edu-renamed");
    edu.store();

    //query to get the updated attribute
    attributeDefName = AttributeDefNameFinder.findById(attributeDefName.getId(), true);
    

    // make sure rule is still working
    try {
      role.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, member0.getSubject(), PermissionAllowed.ALLOWED);
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("edu-renamed:attributeDef-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));

    // now move stem..
    edu.move(edu2);

    //query to get the updated attribute
    attributeDefName = AttributeDefNameFinder.findById(attributeDefName.getId(), true);

    // again make sure the rule is still working
    try {
      role.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, member0.getSubject(), PermissionAllowed.ALLOWED);
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertEquals(initialFirings + 3, RuleEngine.ruleFirings);
    assertEquals("edu2:edu-renamed:attributeDef-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));
  }
  
  /**
   * 
   */
  public void testIfOwnerNameUpdateOnStemNameChange() {

    Group group1 = edu3.addChildGroup("test1", "test1");
    edu.addChildGroup("test2", "test2");
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    AttributeAssign attributeAssign = group1.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.noGroupInFolderHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfOwnerNameName(), edu.getName());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "bogus");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "bogus");
    

    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    // now rename stem..
    edu.setExtension("edu-renamed");
    edu.store();
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    try {
      group1.addMember(member0.getSubject());
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertFalse(group1.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("edu-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfOwnerName"));

    // now move stem..
    edu.move(edu2);
    
    // again make sure the rule is still working
    try {
      group1.addMember(member0.getSubject());
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertFalse(group1.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("edu2:edu-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfOwnerName"));

    // now move the parent stem..
    edu2.move(edu3);
    
    // again make sure the rule is still working
    edu = StemFinder.findByUuid(grouperSession, edu.getUuid(), true);
    try {
      group1.addMember(member0.getSubject());
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertFalse(group1.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 3, RuleEngine.ruleFirings);
    assertEquals("edu3:edu2:edu-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfOwnerName"));
  }
  
  /**
   * 
   */
  public void testCheckOwnerNameUpdateOnStemNameChange() {

    AttributeAssign attributeAssign = edu3.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerNameName(), edu.getName());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "bogus");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "bogus");

    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    // now rename stem..
    edu.setExtension("edu-renamed");
    edu.store();
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    try {
      edu.addChildGroup("test", "test");
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertNull(GrouperDAOFactory.getFactory().getGroup().findByName(edu.getName() + ":" + "test", false, new QueryOptions().secondLevelCache(false)));
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("edu-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));

    // now move stem..
    edu.move(edu2);
    
    // again make sure the rule is still working
    try {
      edu.addChildGroup("test", "test");
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertNull(GrouperDAOFactory.getFactory().getGroup().findByName(edu.getName() + ":" + "test", false, new QueryOptions().secondLevelCache(false)));
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("edu2:edu-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));

    // now move the parent stem..
    edu2.move(edu3);
    
    // again make sure the rule is still working
    edu = StemFinder.findByUuid(grouperSession, edu.getUuid(), true);
    try {
      edu.addChildGroup("test", "test");
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertNull(GrouperDAOFactory.getFactory().getGroup().findByName(edu.getName() + ":" + "test", false, new QueryOptions().secondLevelCache(false)));
    assertEquals(initialFirings + 3, RuleEngine.ruleFirings);
    assertEquals("edu3:edu2:edu-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));
  }
  
  /**
   * 
   */
  public void testPackedSubjectUpdateOnGroupNameChange() {
    
    Group adminGroup = edu.addChildGroup("admin", "admin");
    
    AttributeAssign attributeAssign = edu.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.stemCreate.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.assignStemPrivilegeToStemId.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "g:gsa :::::: edu:admin");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "create");


    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    // now rename group..
    adminGroup.setExtension("admin-renamed");
    adminGroup.store();
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    Stem newStem = edu.addChildStem("test1", "test1");   
    assertTrue(newStem.hasCreate(adminGroup.toSubject()));
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("g:gsa :::::: edu:admin-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleThenEnumArg0"));

    // now move group..
    edu.move(edu2);
    
    // make sure rule is still working
    GrouperCacheUtils.clearAllCaches();
    newStem = edu.addChildStem("test2", "test2");   
    assertTrue(newStem.hasCreate(adminGroup.toSubject()));
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("g:gsa :::::: edu2:edu:admin-renamed", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleThenEnumArg0"));
  }
  
  /**
   * 
   */
  public void testNameMatchesSqlLikeStringUpdateOnStemNameChange() {
    
    AttributeAssign attributeAssign = edu.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumArg0Name(), "edu:%");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "bogus");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "bogus");

    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    // now rename stem..
    edu.setExtension("edu-renamed");
    edu.store();
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    try {
      edu.addChildGroup("test", "test");
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertNull(GrouperDAOFactory.getFactory().getGroup().findByName(edu.getName() + ":" + "test", false, new QueryOptions().secondLevelCache(false)));
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("edu-renamed:%", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfConditionEnumArg0"));

    // now move stem..
    edu.move(edu2);
    
    // again make sure the rule is still working
    try {
      edu.addChildGroup("test", "test");
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertNull(GrouperDAOFactory.getFactory().getGroup().findByName(edu.getName() + ":" + "test", false, new QueryOptions().secondLevelCache(false)));
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("edu2:edu-renamed:%", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfConditionEnumArg0"));

    // now move the parent stem..
    edu2.move(edu3);
    
    // again make sure the rule is still working
    edu = StemFinder.findByUuid(grouperSession, edu.getUuid(), true);
    try {
      edu.addChildGroup("test", "test");
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    
    assertNull(GrouperDAOFactory.getFactory().getGroup().findByName(edu.getName() + ":" + "test", false, new QueryOptions().secondLevelCache(false)));
    assertEquals(initialFirings + 3, RuleEngine.ruleFirings);
    assertEquals("edu3:edu2:edu-renamed:%", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfConditionEnumArg0"));
  }
  
  /**
   * 
   */
  public void testIfOwnerNameUpdateOnGroupNameChange() {
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Group ruleGroup = edu.addChildGroup("ruleGroup", "ruleGroup");
    Group mustBeInGroup = edu.addChildGroup("mustBeInGroup", "mustBeInGroup");
    
    AttributeAssign attributeAssign = ruleGroup.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfOwnerNameName(), mustBeInGroup.getName());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "bogus");
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "bogus");

    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    // now rename group..
    mustBeInGroup.setExtension("mustBeInGroup2");
    mustBeInGroup.store();
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    try {
      ruleGroup.addMember(member0.getSubject());
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    assertFalse(ruleGroup.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("edu:mustBeInGroup2", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfOwnerName"));

    // now move group..
    mustBeInGroup.move(edu2);
    
    // again make sure the rule is still working
    try {
      ruleGroup.addMember(member0.getSubject());
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    assertFalse(ruleGroup.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("edu2:mustBeInGroup2", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfOwnerName"));

    // now move the stem..
    edu2.move(edu);
    
    // again make sure the rule is still working
    try {
      ruleGroup.addMember(member0.getSubject());
      fail("Should throw RuleVeto");
    } catch (RuleVeto e) {
      // good
    }
    assertFalse(ruleGroup.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 3, RuleEngine.ruleFirings);
    assertEquals("edu:edu2:mustBeInGroup2", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleIfOwnerName"));
  }

  /**
   * 
   */
  public void testCheckOwnerNameUpdateOnGroupNameChange() {
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Group ruleGroup = edu.addChildGroup("ruleGroup", "ruleGroup");
    Group mustBeInGroup = edu.addChildGroup("mustBeInGroup", "mustBeInGroup");
    
    ruleGroup.addMember(member0.getSubject());
    mustBeInGroup.addMember(member0.getSubject());
    
    AttributeAssign attributeAssign = ruleGroup.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), grouperSession.getSubject().getSourceId());
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), grouperSession.getSubject().getId());
    
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerNameName(), mustBeInGroup.getName());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
  
    // should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    // now rename group..
    mustBeInGroup.setExtension("mustBeInGroup2");
    mustBeInGroup.store();
    
    // make sure rule is still working
    long initialFirings = RuleEngine.ruleFirings;
    mustBeInGroup.deleteMember(member0);
    assertFalse(ruleGroup.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
    assertEquals("edu:mustBeInGroup2", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));

    // add back members
    ruleGroup.addMember(member0.getSubject());
    mustBeInGroup.addMember(member0.getSubject());
    
    // now move group..
    mustBeInGroup.move(edu2);
    
    // again make sure the rule is still working
    mustBeInGroup.deleteMember(member0);
    assertFalse(ruleGroup.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
    assertEquals("edu2:mustBeInGroup2", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));
    
    // add back members
    ruleGroup.addMember(member0.getSubject());
    mustBeInGroup.addMember(member0.getSubject());
    
    // now move the stem..
    edu2.move(edu);
    
    // again make sure the rule is still working
    mustBeInGroup = GroupFinder.findByUuid(grouperSession, mustBeInGroup.getId(), true);
    mustBeInGroup.deleteMember(member0);
    assertFalse(ruleGroup.hasMember(member0.getSubject()));
    assertEquals(initialFirings + 3, RuleEngine.ruleFirings);
    assertEquals("edu:edu2:mustBeInGroup2", AttributeAssignValueContainer.attributeValueString(RuleEngine.allRulesAttributeAssignValueContainers(null).get(attributeAssign), "etc:attribute:rules:ruleCheckOwnerName"));
  }
}
