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
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeNames;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegateOptions;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResults;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueResult;
import edu.internet2.middleware.grouper.attr.value.AttributeValueResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotAllowed;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 *
 */
public class AttributeAssignTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignTest("testFindOwnersGroup"));
    
//    GrouperStartup.startup();
//    
//    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
//    
//    String hql = "select theGroup, aa, aav from Group theGroup "
//        + " join AttributeAssign aa on theGroup.id = aa.ownerGroupId "
//        + " join AttributeDefName adn on aa.attributeDefNameId = adn.id "
//        + " left outer join AttributeAssignValue aav on aav.attributeAssignId = aa.id "
//        + " where aa.attributeAssignTypeDb = 'group' and aa.enabledDb = 'T' ";
//    hql = "select theGroup, aa from Group theGroup "
//        + " join AttributeAssign aa on theGroup.id = aa.ownerGroupId ";
//    hql = "select theGroup, aa from Group theGroup, AttributeAssign aa where theGroup.id = aa.ownerGroupId ";
//    
//    hql = "select count(aa) from Group theGroup, AttributeAssign aa, AttributeDefName adn, AttributeDef ad where aa.attributeDefNameId = adn.id and theGroup.id = aa.ownerGroupId and ad.id = adn.attributeDefId and aa.attributeAssignTypeDb = 'group' and aa.enabledDb = 'T' and adn.id in ('abc')";
//    
//    Set<Object[]> results = byHqlStatic.createQuery(hql).listSet(Object[].class);
//    for (Object[] result : results) {
//      for (Object col : result) {
//        System.out.println(col);
//      }
//      System.out.println("");
//    }
  }
  
  /**
   * 
   */
  public AttributeAssignTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /** top stem */
  private Role role;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
    this.role = this.top.addChildRole("role", "role");
    this.role.addMember(SubjectTestHelper.SUBJ0, false);
    this.role.addMember(SubjectTestHelper.SUBJ1, false);
    this.role.addMember(SubjectTestHelper.SUBJ2, false);
    this.role.addMember(SubjectTestHelper.SUBJ7, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "-1");
  }

  /**
   * 
   */
  public void testFindAttributeAssigns() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem a1Stem = new StemSave(grouperSession).assignName("a1").assignCreateParentStemsIfNotExist(true).save();
    Stem b1Stem = new StemSave(grouperSession).assignName("b1").assignCreateParentStemsIfNotExist(true).save();
    Stem a1a2Stem = new StemSave(grouperSession).assignName("a1:a2").assignCreateParentStemsIfNotExist(true).save();
    Stem a1a2a3Stem = new StemSave(grouperSession).assignName("a1:a2:a3").assignCreateParentStemsIfNotExist(true).save();
    
    Group a1a2a3xGroup = new GroupSave(grouperSession).assignName("a1:a2:a3:x").assignCreateParentStemsIfNotExist(true).save();
    
    {
      AttributeAssign attributeAssign = a1Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation1");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "a");
    }
    
    {
      AttributeAssign attributeAssign = a1Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation2");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "b");
    }

    {
      AttributeAssign attributeAssign = b1Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation1");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "c");
    }

    {
      AttributeAssign attributeAssign = a1a2Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation1");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "d");
    }

    {
      AttributeAssign attributeAssign = a1a2Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation2");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "e");
    }

    {
      AttributeAssign attributeAssign = a1a2a3Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation1");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "f");
    }

    {
      AttributeAssign attributeAssign = a1a2a3Stem.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation2");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "g");
    }

    {
      AttributeAssign attributeAssign = a1a2a3xGroup.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation1");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "h");
    }

    {
      AttributeAssign attributeAssign = a1a2a3xGroup.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName(), "affiliation2");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
      attributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), "i");
    }

    AttributeAssignable configObject = a1a2a3xGroup.getAttributeDelegate().getAttributeOrAncestorAttribute(
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName(), true, 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getId(), "affiliation1", GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getId(), "true");
    
    assertNotNull(configObject);
    assertTrue(configObject instanceof Stem);
    assertEquals(a1Stem.getName(), ((Stem)configObject).getName());

    configObject = a1a2a3xGroup.getAttributeDelegate().getAttributeOrAncestorAttribute(
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName(), true, 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getId(), "affiliation2", GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getId(), "true");
    
    assertNotNull(configObject);
    assertTrue(configObject instanceof Stem);
    assertEquals(a1a2Stem.getName(), ((Stem)configObject).getName());

  }
  
  /**
   * attribute def
   */
  public void testHibernateFail() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    
    try {
      attributeAssign.saveOrUpdate(true);
      fail("Should throw exception");
    } catch (AttributeAssignNotAllowed aana) {
      //good
    }
    
  }

  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    
    //should work now
    attributeAssign.saveOrUpdate(true);
  }

  /**
   * attribute def
   */
  public void testDelegation() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName0 = this.top.addChildAttributeDefName(attributeDef, "testName0", "test name0");
    AttributeDefName attributeDefName1 = this.top.addChildAttributeDefName(attributeDef, "testName1", "test name1");
    AttributeDefName attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName2", "test name2");

    //assign a direct assignment to the user
    //subj0 can delegate
    //subj1 cannot delegate
    //subj2 can grant delegation
    AttributeAssignResult attributeAssignResult0 = this.role.getPermissionRoleDelegate()
      .assignSubjectRolePermission(attributeDefName0, SubjectTestHelper.SUBJ0, PermissionAllowed.ALLOWED);
    this.role.getPermissionRoleDelegate()
      .assignSubjectRolePermission(attributeDefName1, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
    AttributeAssignResult attributeAssignResult2 = this.role.getPermissionRoleDelegate()
    .assignSubjectRolePermission(attributeDefName2, SubjectTestHelper.SUBJ2, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign0 = attributeAssignResult0.getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    attributeAssign0.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign0.saveOrUpdate(true);
    
    attributeAssign2.setAttributeAssignDelegatable(AttributeAssignDelegatable.GRANT);
    attributeAssign2.saveOrUpdate(true);
    
    Member member7 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ7, true);
    
    //lets try to delegate with permissions
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
        member7, true, null);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    PermissionEntry permissionEntry = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName0.getId()).iterator().next();
    assertTrue(permissionEntry != null);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //try to grant as delegatable, cant
    try {
      AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
      attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
      attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
          member7, true, attributeAssignDelegateOptions);
      fail();
    } catch (Exception e) {
      //e.printStackTrace();
      //ok
    }
    
    //ungrant
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
        member7, false, null);
    assertEquals(0, GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName0.getId()).size());

    //#############################################
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    //try to delegate, cant
    try {
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName1, 
          member7, true, null);
      fail(); 
    } catch (Exception e) {
      //ok
    }

    //#############################################
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
    attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
    attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);

    //try to grant, can
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName2, 
        member7, true, attributeAssignDelegateOptions);

    permissionEntry = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName2.getId()).iterator().next();
    assertEquals(AttributeAssignDelegatable.TRUE, permissionEntry.getAttributeAssignDelegatable());
  }

  /**
   * attribute def
   */
  public void testHibernateDelegate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign.saveOrUpdate(true);
    
    
    
  }

  /**
   * make an example attribute assign for testing
   * @return an example attribute assign
   */
  public static AttributeAssign exampleAttributeAssign() {
    AttributeAssign attributeAssign = new AttributeAssign();
    attributeAssign.setAttributeAssignActionId("attributeAssignActionId");
    attributeAssign.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign.setAttributeAssignType(AttributeAssignType.any_mem);
    attributeAssign.setAttributeDefNameId("attributeDefNameId");
    attributeAssign.setContextId("contextId");
    attributeAssign.setCreatedOnDb(5L);
    attributeAssign.setDisabledTimeDb(7L);
    attributeAssign.setDisallowedDb("T");
    attributeAssign.setEnabledTimeDb(8L);
    attributeAssign.setHibernateVersionNumber(3L);
    attributeAssign.setLastUpdatedDb(6L);
    attributeAssign.setNotes("notes");
    attributeAssign.setOwnerAttributeAssignId("ownerAttributeAssignId");
    attributeAssign.setOwnerAttributeDefId("ownerAttributeDefId");
    attributeAssign.setOwnerGroupId("ownerGroupId");
    attributeAssign.setOwnerMemberId("ownerMemberId");
    attributeAssign.setOwnerMembershipId("ownerMembershipId");
    attributeAssign.setOwnerStemId("ownerStemId");
    attributeAssign.setId("uuid");
    
    return attributeAssign;
  }
  
  /**
   * make an example attribute assign from db for testing
   * @return an example group
   */
  public static AttributeAssign exampleAttributeAssignDb() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    if (attributeAssign == null) {
      AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
      attributeAssign = attributeAssignResult.getAttributeAssign();
    }
    return attributeAssign;
  }

  
  /**
   * retrieve example group from db for testing
   * @return an example group
   */
  public static AttributeAssign exampleRetrieveAttributeAssignDb() {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:testAttributeAssignDefName", true);
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:groupTestAttrAssign", true);
    return new AttributeAssignFinder().addOwnerGroupId(group.getId()).addAttributeDefNameId(attributeDefName.getId()).findAttributeAssigns().iterator().next();
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeDefName attributeDefInsertName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefInsertName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrInsertAssign").assignName("test:groupTestAttrInsertAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefInsertName);
    AttributeAssign attributeAssignOriginal = attributeAssignResult.getAttributeAssign();
    
    //do this because last membership update isnt there, only in db
    attributeAssignOriginal = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    AttributeAssign attributeAssignCopy = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    AttributeAssign atrtibuteAssignCopy2 = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    attributeAssignCopy.delete();
    
    //lets insert the original
    atrtibuteAssignCopy2.xmlSaveBusinessProperties(null);
    atrtibuteAssignCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeAssignCopy = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    
    assertFalse(attributeAssignCopy == attributeAssignOriginal);
    assertFalse(attributeAssignCopy.xmlDifferentBusinessProperties(attributeAssignOriginal));
    assertFalse(attributeAssignCopy.xmlDifferentUpdateProperties(attributeAssignOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = null;
    AttributeAssign exampleAttributeAssign = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();
      
      attributeAssign.setContextId("abc");
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setContextId(exampleAttributeAssign.getContextId());
      attributeAssign.xmlSaveUpdateProperties();

      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
      
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setCreatedOnDb(99L);
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setCreatedOnDb(exampleAttributeAssign.getCreatedOnDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setLastUpdatedDb(99L);
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setLastUpdatedDb(exampleAttributeAssign.getLastUpdatedDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setHibernateVersionNumber(99L);
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setHibernateVersionNumber(exampleAttributeAssign.getHibernateVersionNumber());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setAttributeAssignActionId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setAttributeAssignActionId(exampleAttributeAssign.getAttributeAssignActionId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setAttributeDefNameId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setAttributeDefNameId(exampleAttributeAssign.getAttributeDefNameId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      //this is set based on times...
      attributeAssign.setEnabledDb("F");
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setEnabledDb(exampleAttributeAssign.getEnabledDb());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setEnabledTimeDb(99L);
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setEnabledTimeDb(exampleAttributeAssign.getEnabledTimeDb());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setId(exampleAttributeAssign.getId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setNotes("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setNotes(exampleAttributeAssign.getNotes());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerAttributeAssignId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerAttributeAssignId(exampleAttributeAssign.getOwnerAttributeAssignId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerAttributeDefId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerAttributeDefId(exampleAttributeAssign.getOwnerAttributeDefId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerGroupId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerGroupId(exampleAttributeAssign.getOwnerGroupId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerMemberId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerMemberId(exampleAttributeAssign.getOwnerMemberId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerMembershipId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerMembershipId(exampleAttributeAssign.getOwnerMembershipId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerStemId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerStemId(exampleAttributeAssign.getOwnerStemId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testRetrieveMultiple() {
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    //this one is the same
    AttributeAssign attributeAssign1 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null, null).getAttributeAssign();
    attributeAssign1.saveOrUpdate(true);

    //this one has different notes
    AttributeAssign attributeAssign2 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null, null).getAttributeAssign();
    attributeAssign2.setNotes("abc");
    attributeAssign2.saveOrUpdate(true);
    
    //this one has different notes and date
    AttributeAssign attributeAssign3 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null, null).getAttributeAssign();
    attributeAssign3.setNotes("abc");
    attributeAssign3.setEnabledTimeDb(9L);
    attributeAssign3.saveOrUpdate(true);
    
    //get by id
    attributeAssign = attributeAssign1.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssign1.getId(), attributeAssign.getId());

    attributeAssign = attributeAssign2.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssign2.getId(), attributeAssign.getId());

    attributeAssign = attributeAssign3.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssign3.getId(), attributeAssign.getId());

    
  }

  /**
   * 
   */
  public void testFindGroupAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //test subject 0 can GROUP_ATTR_READ and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can GROUP_ATTR_READ not read
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);

    //test subject 2 can read not GROUP_ATTR_READ
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read or GROUP_ATTR_READ

    //test subject 4 can read and read
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can update and read
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 6 can admin and read
    group.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can GROUP_ATTR_READ and update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can GROUP_ATTR_READ and admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can GROUP_ATTR_READ and view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);

    
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }

    //Search for group, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());

    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());

    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);

    assertEquals(0, attributeAssigns.size());

    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);

    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);

    assertEquals(0, attributeAssigns.size());


    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());

    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can GROUP_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    
    //test subject 1 can GROUP_ATTR_READ not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));

    //test subject 3 can not read or GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());

    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 4 cannot read the attribute assignment on assignment
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, true);
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(0, attributeAssigns.size());

    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 7 can GROUP_ATTR_READ and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 8 can GROUP_ATTR_READ and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 8 cannot read the attribute assignment on assignment
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    //test subject 9 can GROUP_ATTR_READ and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    group.delete();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * 
   */
  public void testFindStemAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToStemAssn(true);
    attributeDef2.store();

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  

    //test subject 0 can STEM_ATTR_READ and read
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can STEM_ATTR_READ not read
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);

    //test subject 2 can read not STEM_ATTR_READ
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read or STEM_ATTR_READ

    //test subject 4 can CREATE and read
    stem.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.CREATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can STEM and read
    stem.grantPriv(SubjectTestHelper.SUBJ5, NamingPrivilege.STEM);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can STEM_ATTR_READ and update
    stem.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can STEM_ATTR_READ and admin
    stem.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can STEM_ATTR_READ and view
    stem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);

    
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can STEM_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    
    //test subject 1 can STEM_ATTR_READ not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not STEM_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));

    //test subject 3 can not read or STEM_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());

    //test subject 4 can CREATE and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 4 cannot read the attribute assignment on assignment
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, true);
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(0, attributeAssigns.size());

    //test subject 5 can STEM and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 7 can STEM_ATTR_READ and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 8 can STEM_ATTR_READ and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 9 can STEM_ATTR_READ and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    stem.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindMemberAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToMemberAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);

    //test subject 0 can read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read

    //test subject 7 can update
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = member.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
  }

  /**
   * 
   */
  public void testFindAttrDefAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToAttributeDefAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    AttributeDef attributeDefAssignTo = AttributeDefTest.exampleAttributeDefDb("test", "testAttributeDefAssignTo");
    

    //test subject 0 can ATTR_DEF_ATTR_READ and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can ATTR_DEF_ATTR_READ not read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
  
    //test subject 2 can read not ATTR_DEF_ATTR_READ
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or ATTR_DEF_ATTR_READ
  
    //test subject 4 can read and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can ATTR_DEF_ATTR_READ and update
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can ATTR_DEF_ATTR_READ and admin
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can ATTR_DEF_ATTR_READ and view
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = attributeDefAssignTo.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for group, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can ATTR_DEF_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue("" + attributeAssigns.size(), attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 1 can ATTR_DEF_ATTR_READ not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not ATTR_DEF_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or ATTR_DEF_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 0);
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 0);
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 4 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 0);
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 0);

    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 0);
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 0);
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can ATTR_DEF_ATTR_READ and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can ATTR_DEF_ATTR_READ and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    
    //test subject 9 can ATTR_DEF_ATTR_READ and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();

    attributeDef.delete();
    
    attributeDef2.delete();
    
  }

  /**
   * 
   */
  public void testFindMembershipAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToImmMembershipAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
    
    //test subject 0 can read and read
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read not read
    group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
  
    //test subject 2 can read not read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or read
  
    //test subject 4 can view and read
    group1.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group1.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    group1.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can read and update
    group1.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can read and admin
    group1.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can read and view
    group1.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
      
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for membership, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 1 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can view and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can read and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can read and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, true);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can read and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    //make sure this cascades right
    group1.deleteMember(SubjectTestHelper.SUBJ0);
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * note, this wont find immediate ones, only the any kind which works on effective memberships or immediate memberships
   */ 
  public void testFindAnyMembershipAttributeAssignments() {

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToEffMembershipAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign").assignName("test:anyMembershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign2").assignName("test:anyMembershipTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //add one group to another to make effective membership and add attribute to that membership
    group1.addMember(group2.toSubject());
    group2.addMember(SubjectTestHelper.SUBJ0);
    
    //test subject 0 can read and read
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read not read
    group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
  
    //test subject 2 can read not read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or read
  
    //test subject 4 can view and read
    group1.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group1.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    group1.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can read and update
    group1.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can read and admin
    group1.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can read and view
    group1.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, false);

    Membership membership = (Membership)MembershipFinder.findMemberships(GrouperUtil.toSet(group1.getId()), 
        GrouperUtil.toSet(member.getUuid()), null, null, FieldFinder.find("members", true), null, null, null, null, null).iterator().next()[0];
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegateEffMship().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }

    //Search for membership, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), "abc")), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey("abc", member.getUuid())), null, true, false);
  
    assertEquals(0, attributeAssigns.size());

    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 1 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can view and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can read and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can read and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can read and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssign.delete();
    
  }

  /**
   * 
   */
  public void testFindGroupAttributeAssignmentsByValue2() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();

    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();

    //test subject 0 can GROUP_ATTR_READ and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can GROUP_ATTR_READ not read
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
  
    //test subject 2 can read not GROUP_ATTR_READ
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or GROUP_ATTR_READ
  
    //test subject 4 can read and read
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group2.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group3.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    group.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can GROUP_ATTR_READ and update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can GROUP_ATTR_READ and admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can GROUP_ATTR_READ and view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeValueResult attributeValueResult = group.getAttributeValueDelegate().assignValuesString(attributeDefName.getName(), GrouperUtil.toSet("group1_a", "group1_b"), false);
    
    AttributeAssign attributeAssign = attributeValueResult.getAttributeAssignResult().getAttributeAssign();
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssignOnAssign2 = attributeAssignResult2.getAttributeAssign();

    
    AttributeValueResult attributeValueResult2 = group2.getAttributeValueDelegate().assignValuesString(attributeDefName.getName(), GrouperUtil.toSet("group2_a", "group2_b"), false);
    
    AttributeAssign attributeAssign2 = attributeValueResult2.getAttributeAssignResult().getAttributeAssign();

    AttributeValueResult attributeValueResult3 = group3.getAttributeValueDelegate().assignValuesString(attributeDefName.getName(), GrouperUtil.toSet("group3_a", "group3_b"), false);
    
    AttributeAssign attributeAssign3 = attributeValueResult3.getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignValueResult attributeAssignValueResult3 = attributeAssign3.getValueDelegate().addValue("group3_a");
    
    AttributeAssign attributeAssign3a = attributeAssignValueResult3.getAttributeAssignValue().getAttributeAssign();
  
    assertEquals(attributeAssign3.getId(), attributeAssign3a.getId());
    
    //Search for group, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find them
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 3 && attributeAssigns.contains(attributeAssign) 
        && attributeAssigns.contains(attributeAssign2) && attributeAssigns.contains(attributeAssign3) );
  
    //search by value without attributeDefName is error
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findGroupAttributeAssignments(null, null, null, null, null, true, false, null, AttributeDefValueType.string,
            "group1_a");
      fail("need to pass in attributeDefName or something");
    } catch (Exception e) {
      //this is ok
    }

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, 
        false, null, AttributeDefValueType.string,
        "group1_a");
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.string,
          "group1_a");

    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.string,
          "group1_a");

    assertEquals(0, attributeAssigns.size());

    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false,
          null, AttributeDefValueType.string,
          "group1_a");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, 
          null, null, null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.string,
          "group1_a");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false,
            null, AttributeDefValueType.string,
            "group1_a");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false,
          null, AttributeDefValueType.string,
          "group1_a");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false,
          null, AttributeDefValueType.string,
          "group1_a");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignOnAssign2);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can GROUP_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false,
          null, AttributeDefValueType.string,
          "group1_a");
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, true,
          null, AttributeDefValueType.string,
          "group1_a");
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) 
        && attributeAssigns.contains(attributeAssignOnAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignOnAssign2));
  
    
    //test subject 1 can GROUP_ATTR_READ not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 4 cannot read the attribute assignment on assignment
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, true, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(0, attributeAssigns.size());
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can GROUP_ATTR_READ and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can GROUP_ATTR_READ and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, true, null, AttributeDefValueType.string,
          "group1_a");
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    
    //test subject 9 can GROUP_ATTR_READ and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(group.getId()), null, true, false, null, AttributeDefValueType.string,
          "group1_a");
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();

    //this attribute has the same value twice
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, 
          null, null, true, false, null, AttributeDefValueType.string,
          "group3_a");
    assertEquals(1, attributeAssigns.size());
    
    assertTrue(attributeAssigns.contains(attributeAssign3));
    
    group.delete();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * 
   */
  public void testFindByIds() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.setMultiAssignable(true);
    attributeDef.store();
    
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Set<AttributeAssign> attributeAssignInputs = new HashSet<AttributeAssign>();
    
    Set<String> ids = new HashSet<String>();
    
    for (int i=0;i<350;i++) {
      AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().addAttribute(attributeDefName);
      AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
      attributeAssignInputs.add(attributeAssign);
      ids.add(attributeAssign.getId());
    }
    
    //add one more to see it not fail
    ids.add("abc");
    
    Set<AttributeAssign> attributeAssignsFound = GrouperDAOFactory.getFactory().getAttributeAssign().findByIds(ids, null, true);
    
    assertEquals(350, GrouperUtil.length(attributeAssignsFound));
    
    for (AttributeAssign attributeAssign : attributeAssignInputs) {
      assertTrue(attributeAssignsFound.contains(attributeAssign));
    }
  }
  
  /**
   * 
   */
  public void testFindStemAttributeAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToStemAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.integer);
    attributeDef2.setMultiValued(true);

    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Stem stem2 = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();

    Stem stem3 = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();

    //test subject 0 can stem and read
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM, false);
    stem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM, false);
    stem3.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can stem not read
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    stem3.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    //test subject 2 can read not stem
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read

    //test subject 4 can STEM_ATTR_READ and read
    stem.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can CREATE and read
    stem.grantPriv(SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE);
    stem2.grantPriv(SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE);
    stem3.grantPriv(SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can update
    stem.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM);
    stem3.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can admin
    stem.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM);
    stem3.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);

    //test subject 9 can view
    stem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM);
    stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = stem.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "15").getAttributeAssignResult();
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = stem2.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "16").getAttributeAssignResult();
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    AttributeAssignResult attributeAssignResult_3 = stem3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "17").getAttributeAssignResult();
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();

    attributeAssign_3.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, false, null, AttributeDefValueType.string, "15");
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");

    assertEquals(0, attributeAssigns.size());

    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, null, AttributeDefValueType.integer, "15");

    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //attribute def type
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.service, AttributeDefValueType.integer, "15");

    assertEquals(0, attributeAssigns.size());
    
    //attribute def type
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.attr, AttributeDefValueType.integer, "15");

    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 0);
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
        null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 0);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
            null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can STEM and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //test subject 1 can STEM not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());

    //test subject 2 can read not STEM
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());

    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can STEM_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 5 can CREATE and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 7 can STEM and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    stem.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindStemAttributeAssignmentsAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToStemAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.integer);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Stem stem2 = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Stem stem3 = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:stemTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
    
    //test subject 0 can read
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    stem3.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read the assignment, but not the assignment on assignment
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 2 can read the assignment on assignment, but not the assignment
    stem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 3 can not read
    
    //subject 4 can not STEM_ATTR_READ the stem, but can read the assignments
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can update
    stem.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 8 can admin
    stem.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    stem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "15")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = stem2.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "16")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_3 = stem3.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "17")
      .getAttributeAssignResult().getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignmentsOnAssignments(
          null, null, null, null, null, null, null, null, null, false, null, null, null, null, true);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, cant find by string
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, AttributeDefValueType.string, "15", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, AttributeDefType.service, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, AttributeDefType.attr, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and no value, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, null, null, false, null, null, null, null, false);
  
    assertEquals(3, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
            null, null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, null, AttributeDefValueType.integer, "15", false, null, null, null, null, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    Set<AttributeAssign> attributeAssignAssignsBase = GrouperUtil.toSet(attributeAssignValue);
    Set<AttributeDef> attributeDefAssignsBase = GrouperUtil.toSet(attributeDef2);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, AttributeDefType.attr, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, AttributeDefType.service, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
    
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //###################################
    //test subject 1 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(0, attributeDefs.size());
  
    //###################################
    //test subject 2 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //subject 4 can not STEM_ATTR_READ the stem, but can read the assignments
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    stem.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindMemberAttributeAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.setValueType(AttributeDefValueType.floating);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToMemberAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.floating);
    attributeDef2.setMultiValued(true);

    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ9, true);
  
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJA, true);

    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJR, true);

    //test subject 0 can read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read
  
    //test subject 7 can update
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = member.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "1.5").getAttributeAssignResult();
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = member2.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "1.6").getAttributeAssignResult();
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    AttributeAssignResult attributeAssignResult_3 = member3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "1.7").getAttributeAssignResult();
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();

    attributeAssign_3.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, false, null, AttributeDefValueType.string, "1.5");
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");

    assertEquals(0, attributeAssigns.size());

    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");

    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, null, true, false, AttributeDefType.attr, AttributeDefValueType.floating, "1.5");

    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, null, true, false, AttributeDefType.service, AttributeDefValueType.floating, "1.5");

    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.floating, "1.5");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, false, null, AttributeDefValueType.floating, "1.5");
    
    assertTrue(attributeAssigns.size() == 0);
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
        null, null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertTrue(attributeAssigns.size() == 0);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
            null, null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, false, null, AttributeDefValueType.floating, "1.5");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindMemberAttributeAssignmentsAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToMemberAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.floating);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ9, true);
  
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJA, true);
  
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJR, true);
  
    //test subject 0 can read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read the assignment, but not the assignment on assignment
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 2 can read the assignment on assignment, but not the assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 3 can not read
  
    //test subject 7 can update
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 8 can admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = member.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "1.5")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = member2.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "1.6")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_3 = member3.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "1.7")
      .getAttributeAssignResult().getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignmentsOnAssignments(
          null, null, null, null, null, null, null, null, null, false, null, null, null, null, true);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, cant find by string
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, AttributeDefValueType.string, "1.5", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, AttributeDefType.attr, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, AttributeDefType.service, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
  
    //search by attributeDefId and no value, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, null, null, false, null, null, null, null, false);
  
    assertEquals(3, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
            null, null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, null, AttributeDefValueType.floating, "1.5", false, null, null, null, null, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    Set<AttributeAssign> attributeAssignAssignsBase = GrouperUtil.toSet(attributeAssignValue);
    Set<AttributeDef> attributeDefAssignsBase = GrouperUtil.toSet(attributeDef2);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //###################################
    //test subject 1 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(0, attributeDefs.size());
  
    //###################################
    //test subject 2 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindGroupAttributeAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.integer);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    //test subject 0 can admin and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can admin not read
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);

    //test subject 2 can read not admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read

    //test subject 4 can read and read
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can update and read
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group2.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group3.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);

    //test subject 9 can view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = group.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "15").getAttributeAssignResult();
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = group2.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "16").getAttributeAssignResult();
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    AttributeAssignResult attributeAssignResult_3 = group3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "17").getAttributeAssignResult();
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, false, null, AttributeDefValueType.string, "15");
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.attr, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.service, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 0);
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
        null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
            null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //test subject 1 can admin not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());

    //test subject 2 can read not admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());

    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 7 can admin and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    group.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindGroupAttributeAssignmentsAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    //test subject 0 can read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read the assignment, but not the assignment on assignment
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 2 can read the assignment on assignment, but not the assignment
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 3 can not read
    
    //subject 4 can not GROUP_ATTR_READ the group, but can read the assignments
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 8 can admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "15a")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = group2.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "16a")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_3 = group3.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "17a")
      .getAttributeAssignResult().getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(
          null, null, null, null, null, null, null, null, null, false, null, null, null, null, true);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, cant find by string
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, AttributeDefValueType.memberId, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, AttributeDefType.service, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, AttributeDefType.attr, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and no value, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, null, null, false, null, null, null, null, false);
  
    assertEquals(3, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
            null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    Set<AttributeAssign> attributeAssignAssignsBase = GrouperUtil.toSet(attributeAssignValue);
    Set<AttributeDef> attributeDefAssignsBase = GrouperUtil.toSet(attributeDef2);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, AttributeDefType.attr, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, AttributeDefType.service, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
    
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //###################################
    //test subject 1 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(0, attributeDefs.size());
  
    //###################################
    //test subject 2 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 cannot view the group, but can read the attributes
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(group.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    group.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindAttrDefAttributeAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.setValueType(AttributeDefValueType.timestamp);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToAttributeDefAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.timestamp);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    AttributeDef attributeDefOwner = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:attributeDefTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    AttributeDef attributeDefOwner2 = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:attributeDefTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    AttributeDef attributeDefOwner3 = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:attributeDefTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    //test subject 0 can admin and read
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can admin not read
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
  
    //test subject 2 can read not admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read
  
    //test subject 4 can read and read
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can ATTR_DEF_ATTR_READ and read
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 7 can update
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
  
    //test subject 9 can view
    attributeDefOwner.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDefOwner2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDefOwner3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = attributeDefOwner.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "2010/01/02").getAttributeAssignResult();
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = attributeDefOwner2.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "2010/01/03").getAttributeAssignResult();
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    AttributeAssignResult attributeAssignResult_3 = attributeDefOwner3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "2010/01/04").getAttributeAssignResult();
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), 
          null, true, false, null, AttributeDefValueType.string, "2010/01/02");
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/09");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.attr, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.service, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
    
    assertTrue(attributeAssigns.size() == 0);
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
        null, null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertTrue(attributeAssigns.size() == 0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
            null, null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, false, null, AttributeDefValueType.timestamp, "2010/01/02");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //test subject 1 can admin not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 2 can read not admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
    
    //test subject 6 can ATTR_DEF_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 7 can admin and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), 
          null, true, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefOwner.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    attributeDefOwner.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindAttrDefAttributeAssignmentsAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToAttributeDefAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.memberId);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
    String memberId = MemberFinder.findBySubject(this.grouperSession,SubjectTestHelper.SUBJ1, true).getUuid();
    String memberId2 = MemberFinder.findBySubject(this.grouperSession,SubjectTestHelper.SUBJ2, true).getUuid();
    String memberId3 = MemberFinder.findBySubject(this.grouperSession,SubjectTestHelper.SUBJ3, true).getUuid();
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    AttributeDef ownerAttributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:attrDefTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    AttributeDef ownerAttributeDef2 = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:attrDefTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    AttributeDef ownerAttributeDef3 = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:attrDefTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    //test subject 0 can read
    ownerAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    ownerAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    ownerAttributeDef3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read the assignment, but not the assignment on assignment
    ownerAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 2 can read the assignment on assignment, but not the assignment
    ownerAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 3 can not read
    
    //subject 4 can not view the group, but can read the assignments
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
  
    //test subject 7 can update
    ownerAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 8 can admin
    ownerAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    ownerAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    ownerAttributeDef3.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = ownerAttributeDef.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), memberId)
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = ownerAttributeDef2.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), memberId2)
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_3 = ownerAttributeDef3.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), memberId3)
      .getAttributeAssignResult().getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(
          null, null, null, null, null, null, null, null, null, false, null, null, null, null, true);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, cant find by wrong id
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, AttributeDefValueType.memberId, memberId2, false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, cant find by string
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, AttributeDefValueType.string, memberId2, false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, AttributeDefType.service, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, AttributeDefType.attr, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and no value, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, null, null, false, null, null, null, null, false);
  
    assertEquals(3, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    //get sasignments from assignments
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.memberId, memberId, true, null, null, null, null, false);
  
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));

    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
            null, null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, null, AttributeDefValueType.memberId, memberId, false, null, null, null, null, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    Set<AttributeAssign> attributeAssignAssignsBase = GrouperUtil.toSet(attributeAssignValue);
    Set<AttributeDef> attributeDefAssignsBase = GrouperUtil.toSet(attributeDef2);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, AttributeDefType.attr, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, AttributeDefType.service, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
    
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //###################################
    //test subject 1 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(0, attributeDefs.size());
  
    //###################################
    //test subject 2 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 cannot view the group, but can read the attributes
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(ownerAttributeDef.getUuid()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    ownerAttributeDef.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindMembershipAttributeAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToImmMembershipAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.integer);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    group.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(SubjectTestHelper.SUBJ3);
    
    Membership membership = MembershipFinder.findImmediateMembership(this.grouperSession, group, SubjectTestHelper.SUBJ1, true);
  
    Membership membership2 = MembershipFinder.findImmediateMembership(this.grouperSession, group2, SubjectTestHelper.SUBJ2, true);
  
    Membership membership3 = MembershipFinder.findImmediateMembership(this.grouperSession, group3, SubjectTestHelper.SUBJ3, true);
  
    //test subject 0 can admin and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can admin not read
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
  
    //test subject 2 can read not admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read
  
    //test subject 4 can read and read
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group2.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group3.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
  
    //test subject 9 can view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "15").getAttributeAssignResult();
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = membership2.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "16").getAttributeAssignResult();
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    AttributeAssignResult attributeAssignResult_3 = membership3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "17").getAttributeAssignResult();
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, false, null, AttributeDefValueType.string, "15");
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.attr, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.service, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 0);
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
        null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
            null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //test subject 1 can admin not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 2 can read not admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 7 can admin and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    membership.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindMembershipAttributeAssignmentsAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToImmMembershipAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    group.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(SubjectTestHelper.SUBJ3);
    
    Membership membership = MembershipFinder.findImmediateMembership(this.grouperSession, group, SubjectTestHelper.SUBJ1, true);
  
    Membership membership2 = MembershipFinder.findImmediateMembership(this.grouperSession, group2, SubjectTestHelper.SUBJ2, true);
  
    Membership membership3 = MembershipFinder.findImmediateMembership(this.grouperSession, group3, SubjectTestHelper.SUBJ3, true);

  
    //test subject 0 can read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read the assignment, but not the assignment on assignment
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 2 can read the assignment on assignment, but not the assignment
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 3 can not read
    
    //subject 4 can not view the group, but can read the assignments
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 8 can admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "15a")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = membership2.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "16a")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_3 = membership3.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "17a")
      .getAttributeAssignResult().getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(
          null, null, null, null, null, null, null, null, null, false, null, null, null, null, true);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, cant find by string
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getUuid()), 
          null, true, null, AttributeDefValueType.memberId, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, AttributeDefType.service, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, AttributeDefType.attr, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, 
          GrouperUtil.toSet("abc"), null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and no value, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, null, null, false, null, null, null, null, false);
  
    assertEquals(3, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
            null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    Set<AttributeAssign> attributeAssignAssignsBase = GrouperUtil.toSet(attributeAssignValue);
    Set<AttributeDef> attributeDefAssignsBase = GrouperUtil.toSet(attributeDef2);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, AttributeDefType.attr, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, AttributeDefType.service, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
    
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //###################################
    //test subject 1 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(0, attributeDefs.size());
  
    //###################################
    //test subject 2 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 cannot view the group, but can read the attributes
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    membership.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindAnyMembershipAttributeAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToEffMembershipAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.integer);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group groupA = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssignA").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Group group2a = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2a").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2a").save();

    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    Group group3a = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3a").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
    
    group.addMember(groupA.toSubject());
    groupA.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(group2a.toSubject());
    group2a.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(group3a.toSubject());
    group3a.addMember(SubjectTestHelper.SUBJ3);
    
//    Membership membership = MembershipFinder.findMemberships(GrouperUtil.toSet(group.getId()), GrouperUtil.toSet(MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ1, true).getUuid()), null, null, Group.getDefaultList(), null, null, null, null, true);
//
//    Membership membership2 = MembershipFinder.findImmediateMembership(this.grouperSession, group2, SubjectTestHelper.SUBJ2, true);
//  
//    Membership membership3 = MembershipFinder.findImmediateMembership(this.grouperSession, group3, SubjectTestHelper.SUBJ3, true);
//  
    //test subject 0 can admin and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can admin not read
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
  
    //test subject 2 can read not admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read
  
    //test subject 4 can read and read
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group2.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    group3.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
  
    //test subject 9 can view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    Member member1 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ1, true);
    AttributeAssignResult attributeAssignResult = group
      .getAttributeValueDelegateEffMship(member1)
      .assignValue(attributeDefName.getName(), "15").getAttributeAssignResult();
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, true);
    AttributeAssignResult attributeAssignResult_2 = group2
      .getAttributeValueDelegateEffMship(member2)
      .assignValue(attributeDefName.getName(), "16").getAttributeAssignResult();
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    AttributeAssignResult attributeAssignResult_3 = group3
      .getAttributeValueDelegateEffMship(member3)
      .assignValue(attributeDefName.getName(), "17").getAttributeAssignResult();
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeDelegate().assignAttribute(attributeDefName2);
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    MultiKey multiKey1 = new MultiKey(group.getId(), member1.getUuid());
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, false, null, AttributeDefValueType.string, "15");
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(new MultiKey("abc", "123")), null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.attr, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
    .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
        null, null, null, true, false, AttributeDefType.service, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 0);
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
        null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), 
            null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, false, null, AttributeDefValueType.integer, "15");
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, false, null, AttributeDefValueType.integer, "15");
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //test subject 1 can admin not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 2 can read not admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 7 can admin and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(multiKey1), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindAnyMembershipAttributeAssignmentsAssignmentsByValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToEffMembershipAssn(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.setMultiValued(true);
  
    attributeDef2.store();
    
  
    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group groupA = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssignA").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2").save();
  
    Group group2a = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign2a").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description2a").save();
  
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
  
    Group group3a = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupTestAttrAssign3a").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description3").save();
    
    group.addMember(groupA.toSubject());
    groupA.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(group2a.toSubject());
    group2a.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(group3a.toSubject());
    group3a.addMember(SubjectTestHelper.SUBJ3);

    Member member1 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ1, false);
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, false);
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, false);

    
//    Membership membership = MembershipFinder.findImmediateMembership(this.grouperSession, group, SubjectTestHelper.SUBJ1, true);
//  
//    Membership membership2 = MembershipFinder.findImmediateMembership(this.grouperSession, group2, SubjectTestHelper.SUBJ2, true);
//  
//    Membership membership3 = MembershipFinder.findImmediateMembership(this.grouperSession, group3, SubjectTestHelper.SUBJ3, true);
  
  
    //test subject 0 can read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read the assignment, but not the assignment on assignment
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 2 can read the assignment on assignment, but not the assignment
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
    
    //test subject 3 can not read
    
    //subject 4 can not view the group, but can read the assignments
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 8 can admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group2.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    group3.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    MultiKey multiKey1 = new MultiKey(group.getId(), member1.getUuid());

    AttributeAssignResult attributeAssignResult = group.getAttributeDelegateEffMship(member1).assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssign attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "15a")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_2 = group2.getAttributeDelegateEffMship(member2).assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_2 = attributeAssignResult_2.getAttributeAssign();
  
    attributeAssign_2.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "16a")
      .getAttributeAssignResult().getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult_3 = group3.getAttributeDelegateEffMship(member3).assignAttribute(attributeDefName);
    AttributeAssign attributeAssign_3 = attributeAssignResult_3.getAttributeAssign();
  
    attributeAssign_3.getAttributeValueDelegate().assignValue(attributeDefName2.getName(), "17a")
      .getAttributeAssignResult().getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssignValue);
  
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(
          null, null, null, null, null, null, null, null, null, false, null, null, null, null, true);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, cant find by string
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, AttributeDefValueType.memberId, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, AttributeDefType.service, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(0, attributeAssigns.size());
  
    
    //Search for stem, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, AttributeDefType.attr, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    //there is nothing assigned directly to the stem
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, 
          GrouperUtil.toSet(new MultiKey("abc", "123")), null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and no value, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, null, null, false, null, null, null, null, false);
  
    assertEquals(3, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), null, null, 
          GrouperUtil.toSet("assign"), true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet(attributeDef2.getId()), 
          null, null, GrouperUtil.toSet("abc"), true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet(attributeDefName2.getId()), null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, 
          GrouperUtil.toSet("abc"), null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssignValue));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet(attributeAssignValue.getId()), 
            null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(GrouperUtil.toSet("abc"), 
          null, null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, GrouperUtil.toSet("abc"), 
          null, null, null, true, null, AttributeDefValueType.string, "15a", false, null, null, null, null, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    Set<AttributeAssign> attributeAssignAssignsBase = GrouperUtil.toSet(attributeAssignValue);
    Set<AttributeDef> attributeDefAssignsBase = GrouperUtil.toSet(attributeDef2);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, AttributeDefType.attr, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, AttributeDefType.service, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
    
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssignValue));
  
    //###################################
    //test subject 1 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(0, attributeDefs.size());
  
    //###################################
    //test subject 2 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefAssignsBase);
    assertEquals(1, attributeDefs.size());
  
    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 cannot view the group, but can read the attributes
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(1, attributeDefs.size());
  
    
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, true, null, null, null, null, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssignValue));
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertEquals(2, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    assertTrue(attributeAssigns.contains(attributeAssignValue));
  
    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(null, null, null, GrouperUtil.toSet(multiKey1), 
          null, true, null, null, null, false, null, null, null, null, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }
  
  /**
   * 
   */
  public void testFindOwnersGroup() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    GroupSave groupSave = null;
    Group group = null;
    Group ownerGroup = null;
    AttributeDefSave attributeDefSave = null;
    AttributeDef attributeDef = null;
    AttributeDefNameSave attributeDefNameSave = null;
    AttributeDefName attributeDefName = null;
    AttributeAssignSave attributeAssignSave = null;
    AttributeAssignSave attributeAssignOnAssignSave = null;
    boolean problemWithAttributeAssign = false;

    // root folder
    Stem testC = new StemSave(grouperSession).assignName("testC").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC").save();

    // couple subfolders
    Stem testCtestCFolder = new StemSave(grouperSession).assignName("testC:testCfolder").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCfolder").save();
    Stem testCtestCFolder2 = new StemSave(grouperSession).assignName("testC:testCfolder2").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCfolder2").save();

    // couple subgroups
    Group testCtestCGroup = new GroupSave(grouperSession).assignName("testC:testCgroup").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCgroup").assignTypeOfGroup(TypeOfGroup.group).save();
    Group testCtestCGroup2 = new GroupSave(grouperSession).assignName("testC:testCgroup2").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCgroup2").assignTypeOfGroup(TypeOfGroup.group).save();

    // attributes to assign (could assign to anything)
    AttributeDef testCattrDef1 = new AttributeDefSave(grouperSession).assignName("testC:attrDef1").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    AttributeDef testCattrDef2 = new AttributeDefSave(grouperSession).assignName("testC:attrDef2").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignMultiValued(false).save();
    testCattrDef1.getAttributeDefActionDelegate().configureActionList("assign");
    testCattrDef2.getAttributeDefActionDelegate().configureActionList("assign");

    // couple names
    AttributeDefName testCattrDef1name = new AttributeDefNameSave(grouperSession, testCattrDef1).assignName("testC:attrDef1name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef1name").save(); 
    AttributeDefName testCattrDef2name = new AttributeDefNameSave(grouperSession, testCattrDef2).assignName("testC:attrDef2name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef2name").save(); 

    // assign to folders
    AttributeAssign testCtestCFolder_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem)
        .assignAttributeDefName(testCattrDef1name).assignOwnerStem(testCtestCFolder).save();
    AttributeAssign testCtestCFolder_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(testCtestCFolder_testCattrDef1name).save();
    
    AttributeAssign testCtestCFolder2_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem)
        .assignAttributeDefName(testCattrDef2name).assignOwnerStem(testCtestCFolder2).save();

    // assign to groups
    AttributeAssign testCtestCGroup_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group)
        .assignAttributeDefName(testCattrDef1name).assignOwnerGroup(testCtestCGroup).save();
    
    AttributeAssignValue testCtestCGroup_testCattrDef1name_abc = testCtestCGroup.getAttributeValueDelegate().assignValue(testCattrDef1name.getName(), "abc").getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssign testCtestCGroup_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(testCtestCGroup_testCattrDef1name).save();

    AttributeAssignValue testCtestCGroup_testCattrDef1name_testCattrDef2name_def = testCtestCGroup_testCattrDef1name.getAttributeValueDelegate()
        .assignValue(testCattrDef2name.getName(), "def").getAttributeAssignValueResult().getAttributeAssignValue();

    AttributeAssign testCtestCGroup2_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group)
        .assignAttributeDefName(testCattrDef2name).assignOwnerGroup(testCtestCGroup2).save();

    AttributeAssignValue testCtestCGroup2_testCattrDef2name_ghi = testCtestCGroup2.getAttributeValueDelegate().assignValue(testCattrDef2name.getName(), "ghi").getAttributeAssignValueResult().getAttributeAssignValue();

    // query by attribute def name
    AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    AttributeAssignFinderResult attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
    
    // get values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // did get values
    assertEquals("ghi", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());

    // get assignments on assignments
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    List<AttributeAssignFinderResult> attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());

    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCGroup_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());

    // dont send in type, get folders and gruops
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId())
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(4, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());

    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCGroup_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());

    attributeAssignFinderResult = attributeAssignFinderResultList.get(2);
    assertEquals(testCtestCFolder, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(3);
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCFolder_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());

    
    // get assignments on assignments with values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("abc", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());

    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCGroup_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("def", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());

    
    // can do everything
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    testCtestCGroup.grantPriv(testSubject0, AccessPrivilege.ADMIN, false);
    testCtestCGroup2.grantPriv(testSubject0, AccessPrivilege.ADMIN, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    // can do everything but with attr privs
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    testCtestCGroup.grantPriv(testSubject1, AccessPrivilege.GROUP_ATTR_READ, false);
    testCtestCGroup2.grantPriv(testSubject1, AccessPrivilege.GROUP_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_READ, false);
    
    // has attr read on groups but cant read attribute
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    testCtestCGroup.grantPriv(testSubject2, AccessPrivilege.GROUP_ATTR_READ, false);
    testCtestCGroup2.grantPriv(testSubject2, AccessPrivilege.GROUP_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_VIEW, false);
    
    // doesnt have attr read on groups but can read attribute
    Subject testSubject3 = SubjectFinder.findById("test.subject.3", true);
    testCtestCGroup.grantPriv(testSubject3, AccessPrivilege.READ, false);
    testCtestCGroup2.grantPriv(testSubject3, AccessPrivilege.READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject3, AttributeDefPrivilege.ATTR_READ, false);
    
    GrouperSession.stopQuietly(grouperSession);
    
    // ######################################### has admin
    
    GrouperSession.start(testSubject0);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());

    GrouperSession.stopQuietly(grouperSession);
    
    //########################################## can do everything but with attr privs

    GrouperSession.start(testSubject1);

    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());

    GrouperSession.stopQuietly(grouperSession);

    //########################################## has attr read on groups but cant read attribute

    GrouperSession.start(testSubject2);
    
    // check security on attribute but cant read
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));

    // dont check security on attribute
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());

    // dont check security on attribute, filter by group name
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group).assignScope(testCtestCGroup2.getExtension().toLowerCase())
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());

    // dont check security on attribute, filter by not group name
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group).assignScope("XXX")
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    // its not checking on attribute
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));

    GrouperSession.stopQuietly(grouperSession);

    //########################################## doesnt have attr read on groups but can read attribute

    GrouperSession.start(testSubject3);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));

    GrouperSession.stopQuietly(grouperSession);

    //########################################## get assignment on assignment

    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));

    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCGroup_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));

    GrouperSession.stopQuietly(grouperSession);

    //########################################## get assignment on assignment with values

    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals("abc", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());

    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCGroup_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCGroup_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals("def", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());

    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * 
   */
  public void testFindOwnersStem() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");

    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    GroupSave groupSave = null;
    Group group = null;
    Group ownerGroup = null;
    AttributeDefSave attributeDefSave = null;
    AttributeDef attributeDef = null;
    AttributeDefNameSave attributeDefNameSave = null;
    AttributeDefName attributeDefName = null;
    AttributeAssignSave attributeAssignSave = null;
    AttributeAssignSave attributeAssignOnAssignSave = null;
    boolean problemWithAttributeAssign = false;
  
    // root folder
    Stem testC = new StemSave(grouperSession).assignName("testC").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC").save();
  
    // couple subfolders
    Stem testCtestCFolder = new StemSave(grouperSession).assignName("testC:testCfolder").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCfolder").save();
    Stem testCtestCFolder2 = new StemSave(grouperSession).assignName("testC:testCfolder2").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCfolder2").save();
  
    // couple subgroups
    Group testCtestCGroup = new GroupSave(grouperSession).assignName("testC:testCgroup").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCgroup").assignTypeOfGroup(TypeOfGroup.group).save();
    Group testCtestCGroup2 = new GroupSave(grouperSession).assignName("testC:testCgroup2").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCgroup2").assignTypeOfGroup(TypeOfGroup.group).save();
  
    // attributes to assign (could assign to anything)
    AttributeDef testCattrDef1 = new AttributeDefSave(grouperSession).assignName("testC:attrDef1").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    AttributeDef testCattrDef2 = new AttributeDefSave(grouperSession).assignName("testC:attrDef2").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignMultiValued(false).save();
    testCattrDef1.getAttributeDefActionDelegate().configureActionList("assign");
    testCattrDef2.getAttributeDefActionDelegate().configureActionList("assign");
  
    // couple names
    AttributeDefName testCattrDef1name = new AttributeDefNameSave(grouperSession, testCattrDef1).assignName("testC:attrDef1name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef1name").save(); 
    AttributeDefName testCattrDef2name = new AttributeDefNameSave(grouperSession, testCattrDef2).assignName("testC:attrDef2name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef2name").save(); 
  
    // assign to folders
    AttributeAssign testCtestCFolder_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem)
        .assignAttributeDefName(testCattrDef1name).assignOwnerStem(testCtestCFolder).save();

    AttributeAssignValue testCtestCFolder_testCattrDef1name_jkl = testCtestCFolder
        .getAttributeValueDelegate().assignValue(testCattrDef1name.getName(), "jkl").getAttributeAssignValueResult().getAttributeAssignValue();

    AttributeAssign testCtestCFolder_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(testCtestCFolder_testCattrDef1name).save();

    AttributeAssignValue testCtestCFolder_testCattrDef1name_testCattrDef2name_mno = testCtestCFolder_testCattrDef1name.getAttributeValueDelegate()
        .assignValue(testCattrDef2name.getName(), "mno").getAttributeAssignValueResult().getAttributeAssignValue();

    AttributeAssign testCtestCFolder2_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem)
        .assignAttributeDefName(testCattrDef2name).assignOwnerStem(testCtestCFolder2).save();

    AttributeAssignValue testCtestCFolder2_testCattrDef2name_pqr = testCtestCFolder2
        .getAttributeValueDelegate().assignValue(testCattrDef2name.getName(), "pqr").getAttributeAssignValueResult().getAttributeAssignValue();
  
    // query by attribute def name
    AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinder()
      .addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    AttributeAssignFinderResult attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCFolder2, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
    
    // get values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId())
        .assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCFolder2, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // did get values
    assertEquals("pqr", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    // get assignments on assignments
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    List<AttributeAssignFinderResult> attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCFolder, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCFolder_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    // get assignments on assignments with values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCFolder, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("jkl", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCFolder_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("mno", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    
    // can do everything
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    testCtestCFolder.grantPriv(testSubject0, NamingPrivilege.STEM_ADMIN, false);
    testCtestCFolder2.grantPriv(testSubject0, NamingPrivilege.STEM_ADMIN, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    // can do everything but with attr privs
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    testCtestCFolder.grantPriv(testSubject1, NamingPrivilege.STEM_ATTR_READ, false);
    testCtestCFolder2.grantPriv(testSubject1, NamingPrivilege.STEM_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_READ, false);
    
    // has attr read on groups but cant read attribute
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    testCtestCFolder.grantPriv(testSubject2, NamingPrivilege.STEM_ATTR_READ, false);
    testCtestCFolder2.grantPriv(testSubject2, NamingPrivilege.STEM_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_VIEW, false);
    
    // doesnt have attr read on groups but can read attribute
    Subject testSubject3 = SubjectFinder.findById("test.subject.3", true);
    testCtestCFolder.grantPriv(testSubject3, NamingPrivilege.CREATE, false);
    testCtestCFolder2.grantPriv(testSubject3, NamingPrivilege.CREATE, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject3, AttributeDefPrivilege.ATTR_READ, false);
    
    GrouperSession.stopQuietly(grouperSession);
    
    // ######################################### has admin
    
    GrouperSession.start(testSubject0);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCFolder2, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
    
    //########################################## can do everything but with attr privs
  
    GrouperSession.start(testSubject1);
  
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCFolder2, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## has attr read on groups but cant read attribute
  
    GrouperSession.start(testSubject2);
    
    // check security on attribute but cant read
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
  
    // dont check security on attribute
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCFolder2, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## doesnt have attr read on stems but can read attribute
  
    GrouperSession.start(testSubject3);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCFolder, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCFolder_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment with values
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.stem_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCFolder, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals("jkl", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCFolder_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals("mno", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    GrouperSession.stopQuietly(grouperSession);
  
  }

  /**
   * 
   */
  public void testFindOwnersMember() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    GroupSave groupSave = null;
    Group group = null;
    Group ownerGroup = null;
    AttributeDefSave attributeDefSave = null;
    AttributeDef attributeDef = null;
    AttributeDefNameSave attributeDefNameSave = null;
    AttributeDefName attributeDefName = null;
    AttributeAssignSave attributeAssignSave = null;
    AttributeAssignSave attributeAssignOnAssignSave = null;
    boolean problemWithAttributeAssign = false;

    // root folder
    Stem testC = new StemSave(grouperSession).assignName("testC").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC").save();
  
    // attributes to assign (could assign to anything)
    AttributeDef testCattrDef1 = new AttributeDefSave(grouperSession).assignName("testC:attrDef1").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    AttributeDef testCattrDef2 = new AttributeDefSave(grouperSession).assignName("testC:attrDef2").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignMultiValued(false).save();
    testCattrDef1.getAttributeDefActionDelegate().configureActionList("assign");
    testCattrDef2.getAttributeDefActionDelegate().configureActionList("assign");
  
    // couple names
    AttributeDefName testCattrDef1name = new AttributeDefNameSave(grouperSession, testCattrDef1).assignName("testC:attrDef1name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef1name").save(); 
    AttributeDefName testCattrDef2name = new AttributeDefNameSave(grouperSession, testCattrDef2).assignName("testC:attrDef2name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef2name").save(); 
  
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    // assign to folders
    AttributeAssign member_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.member)
        .assignAttributeDefName(testCattrDef1name).assignOwnerMember(member).save();
  
    AttributeAssignValue member_testCattrDef1name_jkl = member
        .getAttributeValueDelegate().assignValue(testCattrDef1name.getName(), "jkl").getAttributeAssignValueResult().getAttributeAssignValue();
  
    AttributeAssign member_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.mem_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(member_testCattrDef1name).save();
  
    AttributeAssignValue member_testCattrDef1name_testCattrDef2name_mno = member_testCattrDef1name.getAttributeValueDelegate()
        .assignValue(testCattrDef2name.getName(), "mno").getAttributeAssignValueResult().getAttributeAssignValue();
  
    AttributeAssign member2_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.member)
        .assignAttributeDefName(testCattrDef2name).assignOwnerMember(member2).save();
  
    AttributeAssignValue member2_testCattrDef2name_pqr = member2
        .getAttributeValueDelegate().assignValue(testCattrDef2name.getName(), "pqr").getAttributeAssignValueResult().getAttributeAssignValue();
  
    // query by attribute def name
    AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinder()
      .addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignCheckAttributeReadOnOwner(true).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    AttributeAssignFinderResult attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(member2, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
    
    // get values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId())
        .assignAttributeAssignType(AttributeAssignType.member)
        .assignCheckAttributeReadOnOwner(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(member2, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // did get values
    assertEquals("pqr", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    // get assignments on assignments
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    List<AttributeAssignFinderResult> attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(member, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(member_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    // get assignments on assignments with values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(member, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("jkl", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(member_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("mno", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    
    // can do everything
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    // can do everything but with attr privs
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_READ, false);
    
    // has attr read on groups but cant read attribute
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_VIEW, false);
        
    GrouperSession.stopQuietly(grouperSession);
    
    // ######################################### has admin
    
    GrouperSession.start(testSubject0);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(member2, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
    
    //########################################## can do everything but with attr privs
  
    GrouperSession.start(testSubject1);
  
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(member2, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## cant read attribute

    GrouperSession.start(testSubject2);

    // check security on attribute but cant read
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignAttributeCheckReadOnAttributeDef(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));

    // dont check security on attribute
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.member)
        .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();

    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(member2, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member2_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.mem_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(member, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(member_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment with values
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.mem_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(member, attributeAssignFinderResult.getOwnerMember());
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals("jkl", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(member_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(member_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals("mno", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    GrouperSession.stopQuietly(grouperSession);
  
  }

  /**
   * 
   */
  public void testFindOwnersAttributeDef() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    GroupSave groupSave = null;
    Group group = null;
    Group ownerGroup = null;
    AttributeDefSave attributeDefSave = null;
    AttributeDef attributeDef = null;
    AttributeDefNameSave attributeDefNameSave = null;
    AttributeDefName attributeDefName = null;
    AttributeAssignSave attributeAssignSave = null;
    AttributeAssignSave attributeAssignOnAssignSave = null;
    boolean problemWithAttributeAssign = false;
  
    // root folder
    Stem testC = new StemSave(grouperSession).assignName("testC").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC").save();
  
    // couple subattributes
    AttributeDef testCattrDef1owner = new AttributeDefSave(grouperSession).assignName("testC:attrDef1owner").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    AttributeDef testCattrDef2owner = new AttributeDefSave(grouperSession).assignName("testC:attrDef2owner").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignMultiValued(false).save();
    testCattrDef1owner.getAttributeDefActionDelegate().configureActionList("assign");
    testCattrDef2owner.getAttributeDefActionDelegate().configureActionList("assign");

    AttributeDefName testCattrDef1nameOwner = new AttributeDefNameSave(grouperSession, testCattrDef1owner).assignName("testC:attrDef1nameOwner").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef1name").save(); 
    AttributeDefName testCattrDef2nameOwner = new AttributeDefNameSave(grouperSession, testCattrDef2owner).assignName("testC:attrDef2nameOwner").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef2name").save(); 

    // attributes to assign (could assign to anything)
    AttributeDef testCattrDef1 = new AttributeDefSave(grouperSession).assignName("testC:attrDef1").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    AttributeDef testCattrDef2 = new AttributeDefSave(grouperSession).assignName("testC:attrDef2").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignMultiValued(false).save();
    testCattrDef1.getAttributeDefActionDelegate().configureActionList("assign");
    testCattrDef2.getAttributeDefActionDelegate().configureActionList("assign");
  
    // couple names
    AttributeDefName testCattrDef1name = new AttributeDefNameSave(grouperSession, testCattrDef1).assignName("testC:attrDef1name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef1name").save(); 
    AttributeDefName testCattrDef2name = new AttributeDefNameSave(grouperSession, testCattrDef2).assignName("testC:attrDef2name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef2name").save(); 
  
    // assign to folders
    AttributeAssign testCattrDef1owner_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignAttributeDefName(testCattrDef1name).assignOwnerAttributeDef(testCattrDef1owner).save();
  
    AttributeAssignValue testCattrDef1owner_testCattrDef1name_jkl = testCattrDef1owner
        .getAttributeValueDelegate().assignValue(testCattrDef1name.getName(), "jkl").getAttributeAssignValueResult().getAttributeAssignValue();
  
    AttributeAssign testCattrDef1owner_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.attr_def_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(testCattrDef1owner_testCattrDef1name).save();
  
    AttributeAssignValue testCattrDef1owner_testCattrDef1name_testCattrDef2name_mno = testCattrDef1owner_testCattrDef1name.getAttributeValueDelegate()
        .assignValue(testCattrDef2name.getName(), "mno").getAttributeAssignValueResult().getAttributeAssignValue();
  
    AttributeAssign testCattrDef2owner_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeDef(testCattrDef2owner).save();
  
    AttributeAssignValue testCattrDef2owner_testCattrDef2name_pqr = testCattrDef2owner
        .getAttributeValueDelegate().assignValue(testCattrDef2name.getName(), "pqr").getAttributeAssignValueResult().getAttributeAssignValue();
  
    // query by attribute def name
    AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinder()
      .addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    AttributeAssignFinderResult attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCattrDef2owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef2owner_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
    
    // get values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId())
        .assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCattrDef2owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef2owner_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // did get values
    assertEquals("pqr", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    // get assignments on assignments
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    List<AttributeAssignFinderResult> attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCattrDef1owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCattrDef1owner_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    // get assignments on assignments with values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCattrDef1owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("jkl", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCattrDef1owner_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("mno", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    
    // can do everything
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    testCattrDef1owner.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    testCattrDef2owner.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    // can do everything but with attr privs
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    testCattrDef1owner.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    testCattrDef2owner.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_READ, false);
    
    // has attr read on groups but cant read attribute
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    testCattrDef1owner.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    testCattrDef2owner.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_VIEW, false);
    
    // doesnt have attr read on groups but can read attribute
    Subject testSubject3 = SubjectFinder.findById("test.subject.3", true);
    testCattrDef1owner.getPrivilegeDelegate().grantPriv(testSubject3, AttributeDefPrivilege.ATTR_READ, false);
    testCattrDef2owner.getPrivilegeDelegate().grantPriv(testSubject3, AttributeDefPrivilege.ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject3, AttributeDefPrivilege.ATTR_READ, false);
    
    GrouperSession.stopQuietly(grouperSession);
    
    // ######################################### has admin
    
    GrouperSession.start(testSubject0);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCattrDef2owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef2owner_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
    
    //########################################## can do everything but with attr privs
  
    GrouperSession.start(testSubject1);
  
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCattrDef2owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef2owner_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## has attr read on groups but cant read attribute
  
    GrouperSession.start(testSubject2);
    
    // check security on attribute but cant read
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
  
    // dont check security on attribute
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCattrDef2owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef2owner_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## doesnt have attr read on attribute defs but can read attribute
  
    GrouperSession.start(testSubject3);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCattrDef1owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCattrDef1owner_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment with values
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.attr_def_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCattrDef1owner, attributeAssignFinderResult.getOwnerAttributeDef());
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals("jkl", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCattrDef1owner_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCattrDef1owner_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals("mno", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    GrouperSession.stopQuietly(grouperSession);
  
  }

  /**
   * 
   */
  public void testFindOwnersMembershipImmediate() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.findAttrAssignments.maxResultSize");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    GroupSave groupSave = null;
    Group group = null;
    Group ownerGroup = null;
    AttributeDefSave attributeDefSave = null;
    AttributeDef attributeDef = null;
    AttributeDefNameSave attributeDefNameSave = null;
    AttributeDefName attributeDefName = null;
    AttributeAssignSave attributeAssignSave = null;
    AttributeAssignSave attributeAssignOnAssignSave = null;
    boolean problemWithAttributeAssign = false;
  
    // root folder
    Stem testC = new StemSave(grouperSession).assignName("testC").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC").save();
  
    // couple subfolders
    Stem testCtestCFolder = new StemSave(grouperSession).assignName("testC:testCfolder").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCfolder").save();
    Stem testCtestCFolder2 = new StemSave(grouperSession).assignName("testC:testCfolder2").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCfolder2").save();
  
    // couple subgroups
    Group testCtestCGroup = new GroupSave(grouperSession).assignName("testC:testCgroup").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCgroup").assignTypeOfGroup(TypeOfGroup.group).save();
    Group testCtestCGroup2 = new GroupSave(grouperSession).assignName("testC:testCgroup2").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:testCgroup2").assignTypeOfGroup(TypeOfGroup.group).save();
  
    testCtestCGroup.addMember(SubjectTestHelper.SUBJ0);
    Membership testCtestCgroupSubject0membership = MembershipFinder.findImmediateMembership(grouperSession, testCtestCGroup, SubjectTestHelper.SUBJ0, true);
    testCtestCGroup2.addMember(SubjectTestHelper.SUBJ1);
    Membership testCtestCgroup2Subject1membership = MembershipFinder.findImmediateMembership(grouperSession, testCtestCGroup2, SubjectTestHelper.SUBJ1, true);
    
    // attributes to assign (could assign to anything)
    AttributeDef testCattrDef1 = new AttributeDefSave(grouperSession).assignName("testC:attrDef1").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    AttributeDef testCattrDef2 = new AttributeDefSave(grouperSession).assignName("testC:attrDef2").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignToAttributeDef(true).assignToAttributeDefAssn(true).assignToEffMembership(true).assignToEffMembershipAssn(true).assignToGroup(true)
        .assignToGroupAssn(true).assignToImmMembership(true).assignToImmMembershipAssn(true).assignToMember(true).assignToMemberAssn(true)
        .assignToStem(true).assignToStemAssn(true).assignToImmMembership(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignMultiValued(false).save();
    testCattrDef1.getAttributeDefActionDelegate().configureActionList("assign");
    testCattrDef2.getAttributeDefActionDelegate().configureActionList("assign");
  
    // couple names
    AttributeDefName testCattrDef1name = new AttributeDefNameSave(grouperSession, testCattrDef1).assignName("testC:attrDef1name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef1name").save(); 
    AttributeDefName testCattrDef2name = new AttributeDefNameSave(grouperSession, testCattrDef2).assignName("testC:attrDef2name").assignCreateParentStemsIfNotExist(true).assignDisplayName("testC:attrDef2name").save(); 
  
    // assign to folders
    AttributeAssign testCtestCFolder_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem)
        .assignAttributeDefName(testCattrDef1name).assignOwnerStem(testCtestCFolder).save();
    AttributeAssign testCtestCFolder_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(testCtestCFolder_testCattrDef1name).save();
    
    AttributeAssign testCtestCFolder2_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.stem)
        .assignAttributeDefName(testCattrDef2name).assignOwnerStem(testCtestCFolder2).save();
  
    // assign to memberships
    AttributeAssign testCtestCgroupSubject0membership_testCattrDef1name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.imm_mem)
        .assignAttributeDefName(testCattrDef1name).assignOwnerMembership(testCtestCgroupSubject0membership).save();
    
    AttributeAssignValue testCtestCgroupSubject0membership_testCattrDef1name_abc = testCtestCgroupSubject0membership.getAttributeValueDelegate().assignValue(testCattrDef1name.getName(), "abc").getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssign testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.imm_mem_asgn)
        .assignAttributeDefName(testCattrDef2name).assignOwnerAttributeAssign(testCtestCgroupSubject0membership_testCattrDef1name).save();
  
    AttributeAssignValue testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name_def = testCtestCgroupSubject0membership_testCattrDef1name.getAttributeValueDelegate()
        .assignValue(testCattrDef2name.getName(), "def").getAttributeAssignValueResult().getAttributeAssignValue();
  
    AttributeAssign testCtestCgroup2Subject1membership_testCattrDef2name = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.imm_mem)
        .assignAttributeDefName(testCattrDef2name).assignOwnerMembership(testCtestCgroup2Subject1membership).save();
  
    AttributeAssignValue testCtestCgroup2Subject1membership_testCattrDef2name_ghi = testCtestCgroup2Subject1membership.getAttributeValueDelegate().assignValue(testCattrDef2name.getName(), "ghi").getAttributeAssignValueResult().getAttributeAssignValue();
  
    // query by attribute def name
    AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.imm_mem)
        .assignCheckAttributeReadOnOwner(true).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    AttributeAssignFinderResult attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCgroup2Subject1membership, attributeAssignFinderResult.getOwnerMembership());
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(SubjectTestHelper.SUBJ1.getId(), attributeAssignFinderResult.getOwnerMember().getSubjectId());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
    
    // get values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // did get values
    assertEquals("ghi", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    // get assignments on assignments
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    List<AttributeAssignFinderResult> attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    // dont send in type, get folders and gruops
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId())
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).findAttributeAssignFinderResults();
      
    assertEquals(4, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(2);
    assertEquals(testCtestCFolder, attributeAssignFinderResult.getOwnerStem());
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(3);
    assertEquals(testCtestCFolder_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCFolder_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertNull(attributeAssignFinderResult.getAttributeAssignValues());
  
    
    // get assignments on assignments with values
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef1name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignIncludeAssignmentsOnAssignments(true).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("abc", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    // get values
    assertEquals("def", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    
    // can do everything
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    testCtestCGroup.grantPriv(testSubject0, AccessPrivilege.ADMIN, false);
    testCtestCGroup2.grantPriv(testSubject0, AccessPrivilege.ADMIN, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    // can do everything but with attr privs
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    testCtestCGroup.grantPriv(testSubject1, AccessPrivilege.GROUP_ATTR_READ, false);
    testCtestCGroup2.grantPriv(testSubject1, AccessPrivilege.GROUP_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject1, AttributeDefPrivilege.ATTR_READ, false);
    
    // has attr read on groups but cant read attribute
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    testCtestCGroup.grantPriv(testSubject2, AccessPrivilege.GROUP_ATTR_READ, false);
    testCtestCGroup2.grantPriv(testSubject2, AccessPrivilege.GROUP_ATTR_READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject2, AttributeDefPrivilege.ATTR_VIEW, false);
    
    // doesnt have attr read on groups but can read attribute
    Subject testSubject3 = SubjectFinder.findById("test.subject.3", true);
    testCtestCGroup.grantPriv(testSubject3, AccessPrivilege.READ, false);
    testCtestCGroup2.grantPriv(testSubject3, AccessPrivilege.READ, false);
    testCattrDef2.getPrivilegeDelegate().grantPriv(testSubject3, AttributeDefPrivilege.ATTR_READ, false);
    
    GrouperSession.stopQuietly(grouperSession);
    
    // ######################################### has admin
    
    GrouperSession.start(testSubject0);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
    
    //########################################## can do everything but with attr privs
  
    GrouperSession.start(testSubject1);
  
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## has attr read on groups but cant read attribute
  
    GrouperSession.start(testSubject2);
    
    // check security on attribute but cant read
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
  
    // dont check security on attribute
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    // dont check security on attribute, filter by group name
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group).assignScope(testCtestCGroup2.getExtension().toLowerCase())
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    // dont check security on attribute, filter by not group name
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group).assignScope("XXX")
        .assignCheckAttributeReadOnOwner(true).assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
  
    // its not checking on attribute
    assertEquals(1, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResult = attributeAssignFinderResults.getAttributeAssignFinderResults().iterator().next();
    assertEquals(testCtestCGroup2, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroup2Subject1membership_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## doesnt have attr read on groups but can read attribute
  
    GrouperSession.start(testSubject3);
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals(0, GrouperUtil.length(attributeAssignFinderResult.getAttributeAssignValues()));
  
    GrouperSession.stopQuietly(grouperSession);
  
    //########################################## get assignment on assignment with values
  
    GrouperSession.startRootSession();
    
    attributeAssignFinderResults = new AttributeAssignFinder().addAttributeDefNameId(testCattrDef2name.getId()).assignAttributeAssignType(AttributeAssignType.group_asgn)
        .assignCheckAttributeReadOnOwner(true).assignQueryOptions(QueryOptions.create("displayName", true, 1, 100)).assignRetrieveValues(true).findAttributeAssignFinderResults();
      
    // gets the base attribute assignment, and the assignment on assignment
    assertEquals(2, GrouperUtil.length(attributeAssignFinderResults.getAttributeAssignFinderResults()));
    attributeAssignFinderResultList = new ArrayList<AttributeAssignFinderResult>(attributeAssignFinderResults.getAttributeAssignFinderResults());
    attributeAssignFinderResult = attributeAssignFinderResultList.get(0);
    assertEquals(testCtestCGroup, attributeAssignFinderResult.getOwnerGroup());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getAttributeAssign());
    // didnt get values
    assertEquals("abc", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    attributeAssignFinderResult = attributeAssignFinderResultList.get(1);
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name, attributeAssignFinderResult.getOwnerAttributeAssign());
    assertEquals(testCtestCgroupSubject0membership_testCattrDef1name_testCattrDef2name, attributeAssignFinderResult.getAttributeAssign());
    //didnt get values
    assertEquals("def", attributeAssignFinderResult.getAttributeAssignValues().iterator().next().getValueString());
  
    GrouperSession.stopQuietly(grouperSession);
  
  }
  
}
