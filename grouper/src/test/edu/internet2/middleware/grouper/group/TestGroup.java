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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.group;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntityFinder;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperStaleStateException;
import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.AddMissingGroupSets;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Group}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroup.java,v 1.4 2009-08-11 20:18:09 mchyzer Exp $
 */
public class TestGroup extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new TestGroup("testNoLocking"));
    //TestRunner.run(TestGroup.class);
    TestRunner.run(new TestGroup("testReadonlyViewonlyAdmin"));
    //TestRunner.run(TestGroup.class);
  }

  /**
   * 
   */
  public void testDeleteGroupWithUpdateOnAttribute() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    String groupName = "test:testGroup";
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(groupName).save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    
    //attribute
    String nameOfAttributeDef = "test:testAttributeDef";
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(nameOfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignValueType(AttributeDefValueType.string).save();
    
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    String nameOfAttributeDefName = "test:testAttributeNameDef";
    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignCreateParentStemsIfNotExist(true)
        .assignName(nameOfAttributeDefName).save();

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group.getAttributeDelegate().assignAttribute(attributeDefName);
    
    group.delete();
    
    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  /**
   * 
   */
  public void testDeleteGroupWhenGroupIsUsedOnObjectsWithNoAccess() {
    
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  
      GrouperSession grouperSession = GrouperSession.startRootSession();
  
      Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
      Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
      Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
      Stem stem = group1.getParentStem();
  
      group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      
      group2.addMember(group1.toSubject());
      group3.grantPriv(group1.toSubject(), AccessPrivilege.VIEW);
      stem.grantPriv(group1.toSubject(), NamingPrivilege.STEM_ATTR_READ);
      
      GrouperSession.stopQuietly(grouperSession);
      
      grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
          
      group1.delete();
      
      GrouperSession.stopQuietly(grouperSession);
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.create.grant.all.read");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.create.grant.all.view");
    }
  }
  
  /**
   * 
   */
  public void testFindGroupsReadonly() {
    
    int numberOfStemsInTree = 100;
    int numberOfGroupsInTree = 100;    
    int numberOfAttributeDefsInTree = 100;    
    int numberOfAttributeDefNamesInTree = 100;    

    GrouperSession grouperSession = GrouperSession.startRootSession();

    final Group sysadminViewersGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("etc:sysadminViewersGroup").save();
    final Group sysadminReadersGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("etc:sysadminReadersGroup").save();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.group", "etc:sysadminViewersGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.group", "etc:sysadminReadersGroup");

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();

    EhcacheController.ehcacheController().flushCache();

    
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    //subject 0 is a viewer, 1 is a reader, 2 is nothing, 3 is a member
    sysadminViewersGroup.addMember(SubjectTestHelper.SUBJ0);
    sysadminReadersGroup.addMember(SubjectTestHelper.SUBJ1);
    
    String groupName = "test:testGroup";
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(groupName).save();
    group.addMember(SubjectTestHelper.SUBJ3);

    String stemName = "test2";
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(stemName).save();

    String stemNameTest = "test";
    Stem stemTest = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(stemNameTest).save();

    //group has attribute
    String group2Name = "test:test2Group";
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(group2Name).save();
    group2.addMember(SubjectTestHelper.SUBJ3);

    //attribute
    String nameOfAttributeDef = "test:testAttributeDef";
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(nameOfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignValueType(AttributeDefValueType.string).save();
    String nameOfAttributeDefName = "test:testAttributeNameDef";
    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignCreateParentStemsIfNotExist(true)
        .assignName(nameOfAttributeDefName).save();

    String name2OfAttributeDef = "test:test2AttributeDef";
    AttributeDef attributeDef2 = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(name2OfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignValueType(AttributeDefValueType.string).save();

    String name2OfAttributeDefName = "test:test2AttributeNameDef";
    AttributeDefName attributeDefName2 = new AttributeDefNameSave(grouperSession, attributeDef2).assignCreateParentStemsIfNotExist(true)
        .assignName(name2OfAttributeDefName).save();

    group2.getAttributeDelegate().assignAttribute(attributeDefName2);

    //stem has an attribute
    String name3OfAttributeDef = "test:test3AttributeDef";
    AttributeDef attributeDef3 = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(name3OfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToStem(true).assignValueType(AttributeDefValueType.string).save();

    String name3OfAttributeDefName = "test:test3AttributeNameDef";
    AttributeDefName attributeDefName3 = new AttributeDefNameSave(grouperSession, attributeDef3).assignCreateParentStemsIfNotExist(true)
        .assignName(name3OfAttributeDefName).save();

    stem.getAttributeDelegate().assignAttribute(attributeDefName3);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //############ SUBJ 0 can view, not read, can search
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    {
      Set<Stem> childrenStems = rootStem.getChildStems(Scope.ONE, QueryOptions.create("displayExtension", true, 1, numberOfStemsInTree));
      
      assertContainsStem(childrenStems, stemTest, "subj 0 can view all, should be able to view 'test'");
      
      assertContainsStem(childrenStems, stemTest, "subj 0 can view all, should be able to view 'test'");
      assertContainsStem(childrenStems, stem, "subj 0 can view all, should be able to view 'test2'");
      
      Set<Group> childrenGroups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, 
          QueryOptions.create("displayExtension", true, 1, numberOfGroupsInTree));
      
      assertContainsGroup(childrenGroups, group, "subj 0 can view all, should be able to view 'test:testGroup'");
      assertContainsGroup(childrenGroups, group2, "subj 0 can view all, should be able to view 'test:test2Group'");
      
      Set<AttributeDef> childrenAttributeDefs = new AttributeDefFinder()
        .assignQueryOptions(QueryOptions.create("extension", true, 1, numberOfAttributeDefsInTree*3))
        .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignParentStemId(rootStem.getId()).assignStemScope(Scope.SUB).findAttributes();

      assertContainsAttributeDef(childrenAttributeDefs, attributeDef, "subj 0 can view all, should be able to view 'test:testAttributeDef'");
      assertContainsAttributeDef(childrenAttributeDefs, attributeDef2, "subj 0 can view all, should be able to view 'test:test2AttributeDef'");
      assertContainsAttributeDef(childrenAttributeDefs, attributeDef3, "subj 0 can view all, should be able to view 'test:test3AttributeDef'");

      Set<AttributeDefName> childrenAttributeDefNames = new AttributeDefNameFinder()
        .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, numberOfAttributeDefsInTree*3))
        .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignParentStemId(rootStem.getId()).assignStemScope(Scope.SUB).findAttributeNames();

      assertContainsAttributeDefName(childrenAttributeDefNames, attributeDefName, "subj 0 can view all, should be able to view 'test:testAttributeNameDef'");
      assertContainsAttributeDefName(childrenAttributeDefNames, attributeDefName2, "subj 0 can view all, should be able to view 'test:test2AttributeNameDef'");
      assertContainsAttributeDefName(childrenAttributeDefNames, attributeDefName3, "subj 0 can view all, should be able to view 'test:test3AttributeNameDef'");

    }
    

    //############ SUBJ 1 can view, and read, can search
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    {
      Set<Stem> childrenStems = rootStem.getChildStems(Scope.ONE, QueryOptions.create("displayExtension", true, 1, numberOfStemsInTree));
      
      assertContainsStem(childrenStems, stemTest, "subj 1 can read all, should be able to view 'test'");
      assertContainsStem(childrenStems, stem, "subj 1 can read all, should be able to view 'test2'");
      
      Set<Group> childrenGroups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, 
          QueryOptions.create("displayExtension", true, 1, numberOfGroupsInTree));
      
      assertContainsGroup(childrenGroups, group, "subj 1 can read all, should be able to view 'test:testGroup'");
      assertContainsGroup(childrenGroups, group2, "subj 1 can read all, should be able to view 'test:test2Group'");
      
      Set<AttributeDef> childrenAttributeDefs = new AttributeDefFinder()
        .assignQueryOptions(QueryOptions.create("extension", true, 1, numberOfAttributeDefsInTree*3))
        .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignParentStemId(rootStem.getId()).assignStemScope(Scope.SUB).findAttributes();

      assertContainsAttributeDef(childrenAttributeDefs, attributeDef, "subj 1 can read all, should be able to view 'test:testAttributeDef'");
      assertContainsAttributeDef(childrenAttributeDefs, attributeDef2, "subj 1 can read all, should be able to view 'test:test2AttributeDef'");
      assertContainsAttributeDef(childrenAttributeDefs, attributeDef3, "subj 1 can read all, should be able to view 'test:test3AttributeDef'");

      Set<AttributeDefName> childrenAttributeDefNames = new AttributeDefNameFinder()
        .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, numberOfAttributeDefNamesInTree*3))
        .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignParentStemId(rootStem.getId()).assignStemScope(Scope.SUB).findAttributeNames();

      assertContainsAttributeDefName(childrenAttributeDefNames, attributeDefName, "subj 1 can read all, should be able to view 'test:testAttributeNameDef'");
      assertContainsAttributeDefName(childrenAttributeDefNames, attributeDefName2, "subj 1 can read all, should be able to view 'test:test2AttributeNameDef'");
      assertContainsAttributeDefName(childrenAttributeDefNames, attributeDefName3, "subj 1 can read all, should be able to view 'test:test3AttributeNameDef'");

    }

    //############ SUBJ 2 cant view, or read, or search
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    {
      Set<Stem> childrenStems = rootStem.getChildStems(Scope.ONE, QueryOptions.create("displayExtension", true, 1, numberOfStemsInTree));
      
      assertNotContainsStem(childrenStems, stemTest, "subj 2 cant view all, should not be able to view 'test'");
      
      assertNotContainsStem(childrenStems, stemTest, "subj 1 can read all, should be able to view 'test'");
      assertNotContainsStem(childrenStems, stem, "subj 1 can read all, should be able to view 'test2'");
      
      
      Set<Group> childrenGroups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, 
          QueryOptions.create("displayExtension", true, 1, numberOfGroupsInTree));
      
      assertNotContainsGroup(childrenGroups, group, "subj 2 cant view all, should not be able to view 'test:testGroup'");
      assertNotContainsGroup(childrenGroups, group2, "subj 2 cant view all, should not be able to view 'test:test2Group'");
      
      Set<AttributeDef> childrenAttributeDefs = new AttributeDefFinder()
        .assignQueryOptions(QueryOptions.create("extension", true, 1, numberOfAttributeDefsInTree))
        .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignParentStemId(rootStem.getId()).assignStemScope(Scope.SUB).findAttributes();

      assertNotContainsAttributeDef(childrenAttributeDefs, attributeDef, "subj 2 cant view all, should not be able to view 'test:testAttributeDef'");
      assertNotContainsAttributeDef(childrenAttributeDefs, attributeDef2, "subj 2 cant view all, should not be able to view 'test:test2AttributeDef'");
      assertNotContainsAttributeDef(childrenAttributeDefs, attributeDef3, "subj 2 cant view all, should not be able to view 'test:test3AttributeDef'");

      Set<AttributeDefName> childrenAttributeDefNames = new AttributeDefNameFinder()
        .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, numberOfAttributeDefNamesInTree))
        .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignParentStemId(rootStem.getId()).assignStemScope(Scope.SUB).findAttributeNames();

      assertNotContainsAttributeDefName(childrenAttributeDefNames, attributeDefName, "subj 2 cant view all, should not be able to view 'test:testAttributeNameDef'");
      assertNotContainsAttributeDefName(childrenAttributeDefNames, attributeDefName2, "subj 2 cant view all, should not be able to view 'test:test2AttributeNameDef'");
      assertNotContainsAttributeDefName(childrenAttributeDefNames, attributeDefName3, "subj 2 cant view all, should not be able to view 'test:test3AttributeNameDef'");

    }

    
    
  }
  
  /**
   * 
   */
  public void testDeleteWhenAdminButNotUpdateOnOtherOwnerGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    Group groupToDelete = new GroupSave(grouperSession).assignName("test:groupToDelete").assignCreateParentStemsIfNotExist(true).save();
    groupToDelete.grantPriv(testSubject0, AccessPrivilege.ADMIN, false);

    Group groupWithMembership = new GroupSave(grouperSession).assignName("test:groupWithMembership").save();

    groupWithMembership.addMember(groupToDelete.toSubject());
    groupWithMembership.grantPriv(groupToDelete.toSubject(), AccessPrivilege.VIEW, false);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(testSubject0);
    
    groupToDelete.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testFindGroupsInStemWithoutPrivilege() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").save();
    
    GrouperDAOFactory.getFactory().getGroup().findGroupsInStemWithoutPrivilege(grouperSession, stem.getId(),
        Scope.ONE, grouperSession.getSubject(), AccessPrivilege.ADMIN, null, false, null);
  }
  
  /**
   * https://bugs.internet2.edu/jira/browse/GRP-1420
   * 
   */
  public void testDeleteMemberAudit() {
    //    Load the sample quick start data
    //    Log in as GrouperSystem
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //    Create test group qsuob:test:AdminAccess
    Group adminAccess = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("qsuob:test:AdminAccess").save();
    
    //    Grant admin to "test.1" (Barry Benson)
    adminAccess.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    
    //    In tomcat, add "babe" to the tomcat-users.xml if needed
    //    In a different browser, login as "babe"
    //    Go to qsuob:test:AdminAccess
    GrouperSession.stopQuietly(grouperSession);
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //    Add test.1 (Barry Windsor) as member
    adminAccess.addMember(SubjectTestHelper.SUBJ1);
    
    //    Remove Barry Windsor as member
    adminAccess.deleteMember(SubjectTestHelper.SUBJ1);
    
    //    Go to Recent Activity
    UserAuditQuery query = new UserAuditQuery();

    QueryOptions queryOptions = new QueryOptions();
    query.setQueryOptions(queryOptions);

    queryOptions.sortDesc("lastUpdatedDb");
    queryOptions.paging(1, 1, false);
    
    query.addAuditTypeFieldValue("groupId", adminAccess.getId());

    List<AuditEntry> auditEntries = query.execute();
    auditEntries.get(0);
        
  }
  
  /**
   * 
   */
  public void testDeleteComposite() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    //    grouperSession = GrouperSession.startRootSession();
    //    suffix = "6";
    //    folderName = "test";
    //    subject = "test.subject.0";
    //    folder = new StemSave(grouperSession).assignName(folderName).save();
    //    grantPriv(folderName, subject, NamingPrivilege.STEM);
    //    compositeLeft = new GroupSave(grouperSession).assignName(folderName + ":compositeLeft" + suffix).save();
    //    grantPriv(folderName + ":compositeLeft" + suffix, subject, AccessPrivilege.READ);
    //    compositeRight = new GroupSave(grouperSession).assignName(folderName + ":compositeRight" + suffix).save();
    //    grantPriv(folderName + ":compositeRight" + suffix, subject, AccessPrivilege.READ);
    //    grouperSession = GrouperSession.start(SubjectFinder.findById(subject));
    //    compositeOwner = new GroupSave(grouperSession).assignName(folderName + ":compositeOwner" + suffix).save();
    //    addComposite(folderName + ":compositeOwner" + suffix,CompositeType.COMPLEMENT,folderName + ":compositeLeft" + suffix,folderName + ":compositeRight" + suffix);
    //    delGroup(folderName + ":compositeOwner" + suffix);

    GrouperSession grouperSession = GrouperSession.startRootSession();
    String suffix = "6";
    String folderName = "test";
    Subject subject = SubjectFinder.findById("test.subject.0", true);
    Stem folder = new StemSave(grouperSession).assignName(folderName).save();
    folder.grantPriv(subject, NamingPrivilege.STEM, false);
    Group compositeLeft = new GroupSave(grouperSession).assignName(folderName + ":compositeLeft" + suffix).save();
    compositeLeft.grantPriv(subject, AccessPrivilege.READ, false);
    Group compositeRight = new GroupSave(grouperSession).assignName(folderName + ":compositeRight" + suffix).save();
    compositeRight.grantPriv(subject, AccessPrivilege.READ, false);
    grouperSession = GrouperSession.start(subject);
    Group compositeOwner = new GroupSave(grouperSession).assignName(folderName + ":compositeOwner" + suffix).save();
    compositeOwner.addCompositeMember(CompositeType.COMPLEMENT, compositeLeft, compositeRight);
    compositeOwner.delete();
    
  }
  
  /**
   * 
   */
  public void testAddOrEditMember() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    final Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup").save();
    
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    final Field field = Group.getDefaultList();

    //if something changed
    //############ dont add a non existing membership
    final Subject subject1 = SubjectTestHelper.SUBJ1;
    final Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject1, true);
    boolean changed = group.addOrEditMember(subject1, false, false, null, null, true);
    
    assertFalse(changed);
    
    Membership membership = (Membership)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {

        //see if member
        return GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
                group.getUuid(), member1.getUuid(), field, MembershipType.IMMEDIATE.getTypeString(), false, false);
        
      }
    });

    assertNull(membership);

    //############ set a date on a non existing membership
    final Subject subject2 = SubjectTestHelper.SUBJ2;
    final Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject2, true);
    
    Date startDate = new Date();
    changed = group.addOrEditMember(SubjectTestHelper.SUBJ2, false, false, null, startDate, true);
    
    assertTrue(changed);
    
    membership = (Membership)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
        return GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
            group.getUuid(), member2.getUuid(), field, MembershipType.IMMEDIATE.getTypeString(), false, false);
      }
    });

    assertNotNull(membership);

    //############ add a non existing membership
    
    final Subject subject3 = SubjectTestHelper.SUBJ3;
    final Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject3, true);

    changed = group.addOrEditMember(SubjectTestHelper.SUBJ3, false, true, null, null, true);
    
    assertTrue(changed);
    
    membership = (Membership)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
        return GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
            group.getUuid(), member3.getUuid(), field, MembershipType.IMMEDIATE.getTypeString(), false, false);
      }
    });

    assertNotNull(membership);

    //############ edit an existing membership
    
    changed = group.addOrEditMember(SubjectTestHelper.SUBJ3, false, true, startDate, null, true);
    
    assertTrue(changed);
    
    membership = (Membership)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
        return GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
            group.getUuid(), member3.getUuid(), field, MembershipType.IMMEDIATE.getTypeString(), false, false);
      }
    });

    assertNotNull(membership);
    
    assertTrue(GrouperUtil.equals(membership.getEnabledTime(), startDate));
    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
  
  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroup.class);


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s; 

  
  public TestGroup(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    super.tearDown();
  }

  /**
   * 
   */
  public void testGroupSave() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    //we have an existing group
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("someNewStem:someGroup").save();
    
    //create a group with an assigned uuid
    String uuid = GrouperUuid.getUuid();
    
    Group group1 = new GroupSave(grouperSession).assignName("someNewStem:someGroup1").assignUuid(uuid).save();

    assertEquals(uuid, group1.getUuid());
    
    Group group2 = new GroupSave(grouperSession).assignName("someNewStem:someGroup2").assignUuid(group.getUuid()).save();
    
    assertEquals(group.getUuid(), group2.getUuid());
    assertEquals("someNewStem:someGroup2", group2.getName());
    
  }
  
  public void testCreateAssignTypesAttributes() {
    GrouperSession grouperSession = null;
    try {
      
      grouperSession = GrouperSession.startRootSession();

      //add some types and attributes
      final GroupType groupType = GroupType.createType(grouperSession, "aType", false);
      final GroupType groupType2 = GroupType.createType(grouperSession, "aType2", false);
      final GroupType groupType3 = GroupType.createType(grouperSession, "aType3", false);
      final String attribute1name = "attr_1";
      groupType.addAttribute(grouperSession, attribute1name, false);
      groupType.addAttribute(grouperSession, "attr_2", false);
      groupType2.addAttribute(grouperSession, "attr2_1", false);
      groupType2.addAttribute(grouperSession, "attr2_2", false);
      groupType3.addAttribute(grouperSession, "attr3_1", false);
      groupType3.addAttribute(grouperSession, "attr3_2", false);

      
      
      GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
        
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignCreateParentStemsIfNotExist(true).assignName("someNewStem:someGroup").save();
          group.addType(groupType);
          group.setAttribute(attribute1name, "123");
          return null;
        }
      });

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * test that privileges are ok for member list
   */
  public void testGetMembersAccess() {

    Group group = new GroupSave(this.s).assignName("someStem:someGroup").assignCreateParentStemsIfNotExist(true).save();
    Group optinsGroup = new GroupSave(this.s).assignName("someStem:someOptinsGroup").assignCreateParentStemsIfNotExist(true).save();
    
    optinsGroup.addMember(SubjectTestHelper.SUBJ2);
    
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group.grantPriv(optinsGroup.toSubject(), AccessPrivilege.OPTIN);
    
    Field updatersField = FieldFinder.find("updaters", true);
    Field optinsField = FieldFinder.find("optins", true);
    Set<Member> updaters = group.getMembers(updatersField);
    
    assertEquals(GrouperUtil.toStringForLog(updaters), 1, GrouperUtil.length(updaters));

    assertEquals(SubjectTestHelper.SUBJ1_ID, updaters.iterator().next().getSubjectId());

    assertTrue(group.hasMember(SubjectTestHelper.SUBJ1, updatersField ));
    assertTrue(group.hasImmediateMember(SubjectTestHelper.SUBJ1, updatersField ));

    assertTrue(group.hasMember(SubjectTestHelper.SUBJ2, optinsField ));
    assertTrue(group.hasImmediateMember(optinsGroup.toSubject(), optinsField ));
    assertTrue(group.hasEffectiveMember(SubjectTestHelper.SUBJ2, optinsField ));

    updaters = MembershipFinder.findMembers(group, updatersField);

    assertEquals(GrouperUtil.toStringForLog(updaters), 1, GrouperUtil.length(updaters));

    assertEquals(SubjectTestHelper.SUBJ1_ID, updaters.iterator().next().getSubjectId());

    Member member1 = MemberFinder.findBySubject(this.s, SubjectTestHelper.SUBJ1, false);
    Member member2 = MemberFinder.findBySubject(this.s, SubjectTestHelper.SUBJ2, false);
    Member memberOptinsGroup = optinsGroup.toMember();
    
    Set<Group> groups = member1.getGroups(updatersField);
    
    assertEquals(1, GrouperUtil.length(groups));
    assertEquals(group.getName(), groups.iterator().next().getName());

    groups = member2.getGroups(optinsField);

    assertEquals(1, GrouperUtil.length(groups));
    assertEquals(group.getName(), groups.iterator().next().getName());
    
    groups = member1.getImmediateGroups(updatersField);
    
    assertEquals(1, GrouperUtil.length(groups));
    assertEquals(group.getName(), groups.iterator().next().getName());
    
    groups = member2.getEffectiveGroups(optinsField);
    
    assertEquals(1, GrouperUtil.length(groups));
    assertEquals(group.getName(), groups.iterator().next().getName());
    
    
  }
  
  /**
   * 
   */
  public void testAttributeDef() {
    i2.getAttributeDelegate().retrieveAttributes();
  }
  
  /**
   * make sure there are no group sets for group members, or read, update, optin, optout.
   * should only be admin, and view.
   */
  public void testEntity() {
    
    //init the stem
    new StemSave(s).assignCreateParentStemsIfNotExist(true)
      .assignName("test")
      .save();


    //count the groupsets
    int originalGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");

    Entity entity = new EntitySave(s).assignCreateParentStemsIfNotExist(true)
      .assignName("test:testEntity")
      .save();
    
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    
    assertEquals(originalGroupSetCount + 2, newGroupSetCount);
        
    //fix group sets, should still be same
    new AddMissingGroupSets().showResults(true).addAllMissingGroupSets();
    newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    
    assertEquals(originalGroupSetCount + 2, newGroupSetCount);
    
    Group entityGroup = (Group)entity;
    
    try {
      entityGroup.addMember(SubjectFinder.findRootSubject());
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    assertFalse(entity.hasView(SubjectFinder.findAllSubject()));
    
    //make one where default is view
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("entities.create.grant.all.view", "true");
    try {
      
      Entity entity2 = new EntitySave(s).assignCreateParentStemsIfNotExist(true)
        .assignName("test:testEntity2")
        .save();
      
      assertTrue(entity2.hasView(SubjectFinder.findAllSubject()));
      
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("entities.create.grant.all.view", "false");
    }

    entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.VIEW, false);
    entity.revokePriv(SubjectFinder.findRootSubject(), AccessPrivilege.VIEW, false);
    entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.ADMIN, false);
    entity.revokePriv(SubjectFinder.findRootSubject(), AccessPrivilege.ADMIN, false);
    try {
      entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.READ, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.UPDATE, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.OPTIN, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.OPTOUT, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.GROUP_ATTR_READ, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findRootSubject(), AccessPrivilege.GROUP_ATTR_UPDATE, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    ((Group)entity).setTypeOfGroup(TypeOfGroup.group);
    try {
      entity.store();
      fail("Shouldnt be able to change type of group");
    } catch (Exception e) {
      //good
    }
    
    //reset the fields
    entity = new EntityFinder().addName(entity.getName()).findEntity(true);
    
    Group group = new GroupSave(s).assignName("test:anotherGroup").assignCreateParentStemsIfNotExist(true).save();
    group.setTypeOfGroup(TypeOfGroup.entity);
    try {
      group.store();
      fail("Shouldnt be able to change type of entity");
    } catch (Exception e) {
      //good
    }
    
    Group group1 = new GroupSave(s).assignName("test:anotherGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(s).assignName("test:anotherGroup2").assignCreateParentStemsIfNotExist(true).save();
    
    
    try {
      
      ((Group)entity).addCompositeMember(CompositeType.COMPLEMENT, group1, group2);
      
      fail("Cannot change an entity to composite");
    } catch (Exception e) {
      //good
    }
    
    Group group1a = new GroupSave(s).assignName("test:anotherGroup1a").assignCreateParentStemsIfNotExist(true).save();
    Group group2a = new GroupSave(s).assignName("test:anotherGroup2a").assignCreateParentStemsIfNotExist(true).save();
    
    
    try {
      
      group1a.addCompositeMember(CompositeType.COMPLEMENT, (Group)entity, group2a);
      
      fail("Cannot change an entity to composite");
    } catch (Exception e) {
      //good
    }
    
  }
  
  /**
   * 
   */
  public void testOptimisticLocking() {
    
    //lets not assume that hibernate is set a certain way:
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("dao.optimisticLocking", "true");
      
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();
      
      final Group group1 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });
      
      final Group group2 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true, new QueryOptions().secondLevelCache(false));
        }
        
      });

      //now update one, should be ok
      group1.setDescription("Hello");
      group1.store();
      
      //now update the other, should not be ok
      try {
        group2.setDescription("Good bye");
        group2.store();
        fail("Should throw this exception");
      } catch (GrouperStaleStateException sose) {
        //good
      } catch (GrouperStaleObjectStateException sose) {
        //good
      }
      
      group1.delete();
      
    } finally {
      
      //put hibernate back the way it was
      GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
      
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();

    }
    
  }

  /**
   * 
   */
  public void testNoLocking() {
    
    //lets not assume that hibernate is set a certain way:
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("dao.optimisticLocking", "false");
      
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();
      
      final Group group1 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });
      
      final Group group2 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });

      //now update one, should be ok
      group1.setDescription("Hello");
      group1.store();
      
      //should not throw the stale exception
      group2.setDescription("Good bye");
      group2.store();
      
      
    } finally {
      
      //put hibernate back the way it was
      
      GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();
      
    }
    
    
    
  }

  /**
   * 
   */
  public void testGetParentStem() {
    LOG.info("testGetParentStem");
    Stem parent = i2.getParentStem();
    Assert.assertNotNull("group has parent", parent);
    Assert.assertTrue("parent == edu", parent.equals(edu));
    Assert.assertTrue(
      "root has STEM on parent", parent.hasStem(s.getSubject())
    );
    Assert.assertTrue(
        "root has STEM on parent", parent.hasStemAdmin(s.getSubject())
      );
  } 

  /**
   * 
   */
  public void testGetTypes() {
    LOG.info("testGetTypes");
    GroupType testType = GroupType.createType(s, "testType");
    i2.addType(testType);
    Set types = i2.getTypes();
    Assert.assertTrue("has 1 type/" + types.size(), types.size() == 1);
  } // public void testGetTypes()

  /**
   * 
   */
  public void testGetTypesSecurity() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GroupType testType1 = GroupType.createType(s, "testType1");
    GroupType testType2 = GroupType.createType(s, "testType2");
    i2.addType(testType1);
    i2.addType(testType2);
    assertEquals(2, i2.getTypes().size());
    
    i2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    i2.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.GROUP_ATTR_READ);
    i2.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.GROUP_ATTR_READ);
    
    testType1.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, true);
    testType1.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ3, AttributeDefPrivilege.ATTR_READ, true);
    testType1.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, true);
    testType2.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, true);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertEquals(0, i2.getTypes().size());
    assertEquals(2, i2.getTypes(false).size());
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    assertEquals(0, i2.getTypes().size());
    assertEquals(2, i2.getTypes(false).size());
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    assertEquals(0, i2.getTypes().size());
    assertEquals(2, i2.getTypes(false).size());
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    assertEquals(1, i2.getTypes().size());
    assertEquals("testType1", i2.getTypes().iterator().next().getName());
    assertEquals(2, i2.getTypes(false).size());
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    assertEquals(2, i2.getTypes().size());
    assertEquals(2, i2.getTypes(false).size());
    GrouperSession.stopQuietly(grouperSession);
  }
  
  public void testAddChildGroupWithBadExtnOrDisplayExtn() {
    LOG.info("testAddChildGroupWithBadExtnOrDisplayExtn");
    try {
      try {
        edu.addChildGroup(null, "test");
        Assert.fail("added group with null extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        edu.addChildGroup("", "test");
        Assert.fail("added group with empty extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        edu.addChildGroup("a:test", "test");
        Assert.fail("added group with colon-containing extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
      try {
        edu.addChildGroup("test", null);
        Assert.fail("added group with null displayExtn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        edu.addChildGroup("test", "");
        Assert.fail("added group with empty displayextn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        edu.addChildGroup("test", "a:test");
        Assert.fail("added group with colon-containing displayExtn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAddChildGroupWithBadExtnOrDisplayExtn()

  public void testSetBadGroupExtension() {
    LOG.info("testSetBadGroupExtension");
    try {
      try {
        i2.setExtension(null);
        i2.store();
        Assert.fail("set null extn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        i2.setExtension("");
        i2.store();
        Assert.fail("set empty extn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        i2.setExtension("a:test");
        Assert.fail("set colon-containing extn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadGroupExtension()

  public void testSetBadGroupDisplayExtension() {
    LOG.info("testSetBadGroupDisplayExtension");
    try {
      try {
        i2.setDisplayExtension(null);
        i2.store();
        Assert.fail("set null displayExtn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        i2.setDisplayExtension("");
        i2.store();
        Assert.fail("set empty displayExtn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        i2.setDisplayExtension("a:test");
        i2.store();
        Assert.fail("set colon-containing displayExtn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadGroupDisplayExtension()

  public void testGetAndHasPrivs() {
    LOG.info("testGetAndHasPrivs");
    try {
      Subject         subj  = SubjectTestHelper.SUBJ0;
      Subject         subj1 = SubjectTestHelper.SUBJ1;
      Subject         all   = SubjectTestHelper.SUBJA;
      GrouperSession  s     = SessionHelper.getRootSession();
      Group           uofc  = edu.addChildGroup("uofc", "uofc");
      GroupHelper.addMember(uofc, subj, "members");
      GroupHelper.addMember(i2, uofc.toSubject(), "members");
      MemberFinder.findBySubject(s, subj, true);
      PrivHelper.grantPriv(s, i2,   all,  AccessPrivilege.OPTIN);
      PrivHelper.grantPriv(s, i2,   subj,  AccessPrivilege.GROUP_ATTR_READ);
      PrivHelper.grantPriv(s, i2,   subj,  AccessPrivilege.GROUP_ATTR_UPDATE);
      PrivHelper.grantPriv(s, uofc, subj, AccessPrivilege.UPDATE);

      // Get access privs
      Assert.assertTrue("admins/i2      == 0",  i2.getAdmins().size()   == 0);
      Assert.assertTrue("optins/i2      == 1",  i2.getOptins().size()   == 1);
      Assert.assertTrue("optouts/i2     == 0",  i2.getOptouts().size()  == 0);
      Assert.assertTrue("readers/i2     == 1",  i2.getReaders().size()  == 1);
      Assert.assertTrue("updaters/i2    == 0",  i2.getUpdaters().size() == 0);
      Assert.assertTrue("viewers/i2     == 1",  i2.getViewers().size()  == 1);
      Assert.assertTrue("groupAttrReaders/i2    == 1",  i2.getGroupAttrReaders().size() == 1);
      Assert.assertTrue("groupAttrUpdaters/i2     == 1",  i2.getGroupAttrUpdaters().size()  == 1);

      Assert.assertTrue("admins/uofc    == 0",  uofc.getAdmins().size()   == 0);
      Assert.assertTrue("optins/uofc    == 0",  uofc.getOptins().size()   == 0);
      Assert.assertTrue("optouts/uofc   == 0",  uofc.getOptouts().size()  == 0);
      Assert.assertTrue("readers/uofc   == 1",  uofc.getReaders().size()  == 1);
      Assert.assertTrue("updaters/uofc  == 1",  uofc.getUpdaters().size() == 1);
      Assert.assertTrue("viewers/uofc   == 1",  uofc.getViewers().size()  == 1);
      Assert.assertTrue("groupAttrReaders/i2    == 0",  uofc.getGroupAttrReaders().size() == 0);
      Assert.assertTrue("groupAttrUpdaters/i2     == 0",  uofc.getGroupAttrUpdaters().size()  == 0);
      
      // Has access privs
      Assert.assertTrue("admin/i2/subj0",     !i2.hasAdmin(subj)      );
      Assert.assertTrue("admin/i2/subj1",     !i2.hasAdmin(subj1)     );
      Assert.assertTrue("admin/i2/subjA",     !i2.hasAdmin(all)       );

      Assert.assertTrue("optin/i2/subj0",     i2.hasOptin(subj)       );
      Assert.assertTrue("optin/i2/subj1",     i2.hasOptin(subj1)      );
      Assert.assertTrue("optin/i2/subjA",     i2.hasOptin(all)        );

      Assert.assertTrue("optout/i2/subj0",    !i2.hasOptout(subj)     );
      Assert.assertTrue("optout/i2/subj1",    !i2.hasOptout(subj1)    );
      Assert.assertTrue("optout/i2/subjA",    !i2.hasOptout(all)      );

      Assert.assertTrue("read/i2/subj0",      i2.hasRead(subj)        );
      Assert.assertTrue("read/i2/subj1",      i2.hasRead(subj1)       );
      Assert.assertTrue("read/i2/subjA",      i2.hasRead(all)         );

      Assert.assertTrue("update/i2/subj0",    !i2.hasUpdate(subj)     );
      Assert.assertTrue("update/i2/subj1",    !i2.hasUpdate(subj1)    );
      Assert.assertTrue("update/i2/subjA",    !i2.hasUpdate(all)      );

      Assert.assertTrue("view/i2/subj0",      i2.hasView(subj)        );
      Assert.assertTrue("view/i2/subj1",      i2.hasView(subj1)       );
      Assert.assertTrue("view/i2/subjA",      i2.hasView(all)         );
      
      Assert.assertTrue("groupAttrRead/i2/subj0",      i2.hasGroupAttrRead(subj)        );
      Assert.assertTrue("groupAttrRead/i2/subj1",      !i2.hasGroupAttrRead(subj1)       );
      Assert.assertTrue("groupAttrRead/i2/subjA",      !i2.hasGroupAttrRead(all)         );

      Assert.assertTrue("groupAttrUpdate/i2/subj0",      i2.hasGroupAttrUpdate(subj)        );
      Assert.assertTrue("groupAttrUpdate/i2/subj1",      !i2.hasGroupAttrUpdate(subj1)       );
      Assert.assertTrue("groupAttrUpdate/i2/subjA",      !i2.hasGroupAttrUpdate(all)         );
      
      Assert.assertTrue("admin/uofc/subj0",   !uofc.hasAdmin(subj)    );
      Assert.assertTrue("admin/uofc/subj1",   !uofc.hasAdmin(subj1)   );
      Assert.assertTrue("admin/uofc/subjA",   !uofc.hasAdmin(all)     );

      Assert.assertTrue("optin/uofc/subj0",   !uofc.hasOptin(subj)    );
      Assert.assertTrue("optin/uofc/subj1",   !uofc.hasOptin(subj1)   );
      Assert.assertTrue("optin/uofc/subjA",   !uofc.hasOptin(all)     );

      Assert.assertTrue("optout/uofc/subj0",  !uofc.hasOptout(subj)   );
      Assert.assertTrue("optout/uofc/subj1",  !uofc.hasOptout(subj1)  );
      Assert.assertTrue("optout/uofc/subjA",  !uofc.hasOptout(all)    );

      Assert.assertTrue("read/uofc/subj0",    uofc.hasRead(subj)      );
      Assert.assertTrue("read/uofc/subj1",    uofc.hasRead(subj1)     );
      Assert.assertTrue("read/uofc/subjA",    uofc.hasRead(all)       );

      Assert.assertTrue("update/uofc/subj0",  uofc.hasUpdate(subj)    );
      Assert.assertTrue("update/uofc/subj1",  !uofc.hasUpdate(subj1)  );
      Assert.assertTrue("update/uofc/subjA",  !uofc.hasUpdate(all)    );

      Assert.assertTrue("view/uofc/subj0",    uofc.hasView(subj)      );
      Assert.assertTrue("view/uofc/subj1",    uofc.hasView(subj1)     );
      Assert.assertTrue("view/uofc/subjA",    uofc.hasView(all)       );
      
      Assert.assertTrue("groupAttrRead/uofc/subj0",      !uofc.hasGroupAttrRead(subj)        );
      Assert.assertTrue("groupAttrRead/uofc/subj1",      !uofc.hasGroupAttrRead(subj1)       );
      Assert.assertTrue("groupAttrRead/uofc/subjA",      !uofc.hasGroupAttrRead(all)         );

      Assert.assertTrue("groupAttrUpdate/uofc/subj0",      !uofc.hasGroupAttrUpdate(subj)        );
      Assert.assertTrue("groupAttrUpdate/uofc/subj1",      !uofc.hasGroupAttrUpdate(subj1)       );
      Assert.assertTrue("groupAttrUpdate/uofc/subjA",      !uofc.hasGroupAttrUpdate(all)         );

      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testSetDescription() {
    LOG.info("testSetDescription");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      String          orig  = i2.getDescription(); 
      String          set   = "this is a group"; 
      i2.setDescription(set);
      i2.store();
      Assert.assertTrue("!orig",  !i2.getDescription().equals(orig));
      Assert.assertTrue("set",    i2.getDescription().equals(set));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetDescription()

  /**
   * 
   */
  public void testSetInvalidDescription() {
    LOG.info("testSetInvalidDescription");
    s     = SessionHelper.getRootSession();
    i2 =  new GroupSave(s).assignGroupNameToEdit("edu:i2").assignCreateParentStemsIfNotExist(true).save();
    String          set   = "this is a group"; 
    set = StringUtils.repeat(set, 100);
    i2.setDescription(set);
    try {
      i2.store();
    } catch (GrouperValidationException gve) {
      assertEquals(Group.VALIDATION_GROUP_DESCRIPTION_TOO_LONG_KEY, gve.getGrouperValidationKey());
      assertEquals(1024, gve.getMaxLength().intValue());
      assertEquals(1500, gve.getCurrentLength().intValue());
    }

    //try with group save
    try {
      new GroupSave(s).assignGroupNameToEdit(i2.getName()).assignDescription(set).save();
    } catch (GrouperValidationException gve) {
      assertEquals(Group.VALIDATION_GROUP_DESCRIPTION_TOO_LONG_KEY, gve.getGrouperValidationKey());
      assertEquals(1024, gve.getMaxLength().intValue());
      assertEquals(1500, gve.getCurrentLength().intValue());
    }
    
    
    s.stop();
  } // public void testSetDescription()

  // Tests
  
  public void testToMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    GroupHelper.toMember(i2);
  } // public void testToMember()

  /**
   * 
   */
  public void testSetExtensionSame() {
    LOG.info("testSetDescription");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      
      Group i3    = StemHelper.addChildGroup(edu, "i3", "internet3");
      
      i3.setExtension("i2");
      
      try {
        i3.store();
        fail("Cant set extension to an existing one");
      } catch (GroupModifyAlreadyExistsException mdaee) {
        //good
      }
      
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }

  }
 
  /**
   * 
   */
  public void testGetAllGroupsSecure() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    QueryOptions queryOptions = new QueryOptions().paging(
        200, 1, true).sortAsc("theGroup.displayNameDb");
    Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
    
    groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), null);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
    
    groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), null);

    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  
  /**
   * make an example group for testing
   * @return an example group
   */
  public static Group exampleGroup() {
    Group group = new Group();
    group.setAlternateNameDb("alternateName");
    group.setContextId("contextId");
    group.setCreateTimeLong(5L);
    group.setCreatorUuid("creatorId");
    group.setDescription("description");
    group.setDisplayExtensionDb("displayExtension");
    group.setDisplayNameDb("displayName");
    group.setExtensionDb("extension");
    group.setHibernateVersionNumber(3L);
    group.setIdIndex(12345L);
    group.setLastMembershipChangeDb(4L);
    group.setModifierUuid("modifierId");
    group.setModifyTimeLong(6L);
    group.setNameDb("name");
    group.setParentUuid("parentUuid");
    group.setTypeOfGroupDb("role");
    group.setUuid("uuid");
    
    return group;
  }
  
  /**
   * make an example group from db for testing
   * @return an example group
   */
  public static Group exampleGroupDb() {
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTest").assignName("test:groupTest").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    return group;
  }

  
  /**
   * retrieve example group from db for testing
   * @return an example group
   */
  public static Group exampleRetrieveGroupDb() {
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:groupTest", true);
    return group;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Group groupOriginal = new GroupSave(GrouperSession.staticGrouperSession()).assignGroupNameToEdit("test:groupInsert")
      .assignName("test:groupInsert").assignCreateParentStemsIfNotExist(true).save();
    
    //not sure why I need to sleep, but the last membership update gets messed up...
    GrouperUtil.sleep(1000);
    
    //do this because last membership update isnt there, only in db
    groupOriginal = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    Group groupCopy = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    Group groupCopy2 = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    groupCopy.delete();
    
    //lets insert the original
    groupCopy2.xmlSaveBusinessProperties(null);
    groupCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    groupCopy = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    
    assertFalse(groupCopy == groupOriginal);
    assertFalse(groupCopy.xmlDifferentBusinessProperties(groupOriginal));
    assertFalse(groupCopy.xmlDifferentUpdateProperties(groupOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = null;
    Group exampleGroup = null;

    
    //TEST UPDATE PROPERTIES
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();
      
      group.setContextId("abc");
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setContextId(exampleGroup.getContextId());
      group.xmlSaveUpdateProperties();

      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
      
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setCreateTimeLong(99);
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setCreateTimeLong(exampleGroup.getCreateTimeLong());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setCreatorUuid("abc");
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setCreatorUuid(exampleGroup.getCreatorUuid());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();
      
      group.setModifierUuid("abc");
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setModifierUuid(exampleGroup.getModifierUuid());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setModifyTimeLong(99);
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setModifyTimeLong(exampleGroup.getModifyTimeLong());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

    }

    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setHibernateVersionNumber(99L);
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setHibernateVersionNumber(exampleGroup.getHibernateVersionNumber());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setAlternateNameDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setAlternateNameDb(exampleGroup.getAlternateNameDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setDescriptionDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setDescriptionDb(exampleGroup.getDescriptionDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setDisplayExtensionDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setDisplayExtensionDb(exampleGroup.getDisplayExtensionDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setDisplayNameDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setDisplayNameDb(exampleGroup.getDisplayNameDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setExtensionDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setExtensionDb(exampleGroup.getExtensionDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setNameDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setNameDb(exampleGroup.getNameDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setParentUuid("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setParentUuid(exampleGroup.getParentUuid());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }

    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setTypeOfGroup(TypeOfGroup.role);
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setTypeOfGroup(exampleGroup.getTypeOfGroup());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }

    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setUuid("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setUuid(exampleGroup.getUuid());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
  }

  /**
   * 
   */
  public void testAlternateName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    String groupName = "test:testGroup";
    String groupName2 = "test:testGroup2";
    String groupName3 = "test:testGroup3";
    String groupName4 = "test:testGroup4";
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(groupName).save();
    
    assertNull(group.getAlternateName());

    String uuid = group.getUuid();
    
    // rename and set alternate name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName2).save();
    
    assertEquals(uuid, group.getUuid());
    assertEquals(groupName2, group.getName());
    assertEquals(groupName, group.getAlternateName());
    
    // rename and set alternate name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName3).save();
    
    assertEquals(uuid, group.getUuid());
    assertEquals(groupName3, group.getName());
    assertEquals(groupName2, group.getAlternateName());

    // rename and don't set alternate name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName4).assignSetAlternateNameIfRename(false).save();
    
    assertEquals(uuid, group.getUuid());
    assertEquals(groupName4, group.getName());
    assertEquals(groupName2, group.getAlternateName());
   
    // delete alternate name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName4).save();
    
    assertEquals(uuid, group.getUuid());
    assertEquals(groupName4, group.getName());
    assertNull(group.getAlternateName());
   
    // add alternate name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName4).assignAlternateName("x:y").save();
    
    assertEquals(uuid, group.getUuid());
    assertEquals(groupName4, group.getName());
    assertEquals("x:y", group.getAlternateName());
    
    // change alternate name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName4).assignAlternateName("x:y:z").save();
    
    assertEquals(uuid, group.getUuid());
    assertEquals(groupName4, group.getName());
    assertEquals("x:y:z", group.getAlternateName());
    
    // bad alternate name
    try {
      group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignUuid(uuid).assignName(groupName4).assignAlternateName("x").save();
      fail("should have thrown exception");
    } catch (GroupModifyException e) {
      // good
    }
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
}

