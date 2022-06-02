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

package edu.internet2.middleware.grouper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.framework.Assert;
import junit.textui.TestRunner;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemFinder.java,v 1.9 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestStemFinder extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStemFinder("testChainingStemFinderExcludingAlternateNames"));
  }
  
  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStemFinder.class);


  public TestStemFinder(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * find stems with two attributes and values (assigned to an assignment)
   * e.g. typeTagDirect=T and typeTagType=service and subject can ADMIN
   */
  public void testFindByAttributeValueOfAssignment() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDefMarker = new AttributeDefSave(grouperSession).assignAttributeDefNameToEdit("test:typeTagDef")
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
        .assignToStem(true)
        .assignValueType(AttributeDefValueType.marker).save();

    AttributeDefName attributeDefNameMarker = new AttributeDefNameSave(grouperSession, attributeDefMarker)
      .assignAttributeDefNameNameToEdit("test:typeTagMarker").assignCreateParentStemsIfNotExist(true).save();

    AttributeDef attributeDefData = new AttributeDefSave(grouperSession).assignAttributeDefNameToEdit("test:typeTagData")
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
        .assignToStemAssn(true)
        .assignValueType(AttributeDefValueType.string).save();

    AttributeDefName attributeDefNameDirect = new AttributeDefNameSave(grouperSession, attributeDefData)
      .assignAttributeDefNameNameToEdit("test:typeTagDirect").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameType = new AttributeDefNameSave(grouperSession, attributeDefData)
    .assignAttributeDefNameNameToEdit("test:typeTagType").assignCreateParentStemsIfNotExist(true).save();

    // stem 0 has a direct T and a type of service, it should find this one, SUBJ0 can ADMIN, YES FOUND
    Stem stem0 = new StemSave(grouperSession).assignName("test:stem0").assignCreateParentStemsIfNotExist(true).save();
    
    {
      AttributeAssign attributeAssign = stem0.getAttributeDelegate().assignAttribute(attributeDefNameMarker).getAttributeAssign();
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagDirect", "T");
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagType", "service");
      stem0.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);
      stem0.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);

    }

    // stem 1 is not direct, but is a service, and subj0 can admin, NOT FOUND
    Stem stem1 = new StemSave(grouperSession).assignName("test:stem1").assignCreateParentStemsIfNotExist(true).save();
    
    {
      AttributeAssign attributeAssign = stem1.getAttributeDelegate().assignAttribute(attributeDefNameMarker).getAttributeAssign();
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagDirect", "F");
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagType", "service");
      stem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);
    }

    // stem 2 is yes direct and not type service, subj0 can admin, NOT FOUND
    Stem stem2 = new StemSave(grouperSession).assignName("test:stem2").assignCreateParentStemsIfNotExist(true).save();
    
    {
      AttributeAssign attributeAssign = stem2.getAttributeDelegate().assignAttribute(attributeDefNameMarker).getAttributeAssign();
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagDirect", "T");
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagType", "ref");
      stem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);

    }

    // stem 3 is no direct and not a service, subj0 can admin, NOT FOUND
    Stem stem3 = new StemSave(grouperSession).assignName("test:stem3").assignCreateParentStemsIfNotExist(true).save();
    
    {
      AttributeAssign attributeAssign = stem3.getAttributeDelegate().assignAttribute(attributeDefNameMarker).getAttributeAssign();
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagDirect", "F");
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagType", "ref");
      stem3.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);

    }
    
    // stem 4 is yes direct and yes a service, but cant ADMIN from subj 0, NOT FOUND
    Stem stem4 = new StemSave(grouperSession).assignName("test:stem4").assignCreateParentStemsIfNotExist(true).save();
    
    {
      AttributeAssign attributeAssign = stem4.getAttributeDelegate().assignAttribute(attributeDefNameMarker).getAttributeAssign();
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagDirect", "T");
      attributeAssign.getAttributeValueDelegate().assignValueString("test:typeTagType", "service");
      stem4.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
      stem4.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);

    }

    // ##############################################
    // root session should be able to find all if checking read on attribute def
    // dont check the privileges of the attributes
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(true)
      .assignNameOfAttributeDefName(attributeDefNameDirect.getName()).addAttributeValuesOnAssignment("T")
      .assignNameOfAttributeDefName2(attributeDefNameType.getName()).addAttributeValuesOnAssignment2("service")
      .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    assertEquals(2, GrouperUtil.length(stems));

    assertEquals(stem0.getName(), stems.get(0).getName());
    assertEquals(stem4.getName(), stems.get(1).getName());

    // ##############################################
    // root session should be able to find all if not checking read
    // dont check the privileges of the attributes
    stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
      .assignNameOfAttributeDefName(attributeDefNameDirect.getName()).addAttributeValuesOnAssignment("T")
      .assignNameOfAttributeDefName2(attributeDefNameType.getName()).addAttributeValuesOnAssignment2("service")
      .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    assertEquals(2, GrouperUtil.length(stems));

    assertEquals(stem0.getName(), stems.get(0).getName());
    assertEquals(stem4.getName(), stems.get(1).getName());

    // ##############################################
    // test.subject.0 session should be able to find 1
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
    .assignNameOfAttributeDefName(attributeDefNameDirect.getName()).addAttributeValuesOnAssignment("T")
      .assignNameOfAttributeDefName2(attributeDefNameType.getName()).addAttributeValuesOnAssignment2("service")
      .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    assertEquals(1, GrouperUtil.length(stems));

    assertEquals(stem0.getName(), stems.get(0).getName());

    // ##############################################
    // test.subject.1 session should be able to find none
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
    .assignNameOfAttributeDefName(attributeDefNameDirect.getName()).addAttributeValuesOnAssignment("T")
      .assignNameOfAttributeDefName2(attributeDefNameType.getName()).addAttributeValuesOnAssignment2("service")
      .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    assertEquals(0, GrouperUtil.length(stems));


    // ##############################################
    // test.subject.0 session should be able to find 0 if checking read on attribute
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(true)
    .assignNameOfAttributeDefName(attributeDefNameDirect.getName()).addAttributeValuesOnAssignment("T")
      .assignNameOfAttributeDefName2(attributeDefNameType.getName()).addAttributeValuesOnAssignment2("service")
      .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    assertEquals(0, GrouperUtil.length(stems));

    
    // ##############################################
    // test.subject.0 session should be able to find 1 if now has permissions on attribute def

    grouperSession = GrouperSession.startRootSession();
    attributeDefMarker.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    attributeDefData.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(true)
    .assignNameOfAttributeDefName(attributeDefNameDirect.getName()).addAttributeValuesOnAssignment("T")
      .assignNameOfAttributeDefName2(attributeDefNameType.getName()).addAttributeValuesOnAssignment2("service")
      .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    assertEquals(1, GrouperUtil.length(stems));

    assertEquals(stem0.getName(), stems.get(0).getName());

    
    
  }
  
  /**
   * 
   */
  public void testFindByUuids() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Set<String> uuids = new HashSet<String>();
    
    for (int i=0;i<1000;i++) {
      Stem stem = StemHelper.addChildStem(root, "edu" + i, "educational" + i);
      uuids.add(stem.getId());
    }
    
    Set<Stem> stems = StemFinder.findByUuids(s, uuids, new QueryOptions().secondLevelCache(false));
    assertEquals(1000, GrouperUtil.length(stems));
  }
  
  /**
   * 
   */
  public void testFindByIdIndex() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");

    Stem found = StemFinder.findByIdIndex(edu.getIdIndex(), true, null);
    
    assertEquals(found.getName(), edu.getName());
    
    found = StemFinder.findByIdIndex(12345656L, false, null);
    
    assertNull(found);
    
    try {
      StemFinder.findByIdIndex(12345678L, true, null);
      fail("shouldnt get here");
    } catch (StemNotFoundException gnfe) {
      //good
    }
    
    
  } // public void testFindByIdIndex()


  // Tests

  /**
   * 
   */
  public void testFindByNameSecure() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.store();

    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN);
    stem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    
    Stem stem1 = new StemSave(grouperSession).assignName("stem1").assignCreateParentStemsIfNotExist(true).save();
    stem1.store();

    stem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);
    stem1.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_VIEW);

    Set<Object[]> membershipsOwnersMembers = null;
    
    // as GrouperSession
    Set<Stem> stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(3, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(rootStem, stem, stem1), "Should find both stems and root since subject0 has privs on both stems and can see ancestor stems");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem, stem1), "Should find both stems since subject0 has privs on both stems");
    
    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ0).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(2, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSet(
        new Object[] {new MembershipFinder().assignFieldName("stemAdmins").assignFieldType(FieldType.NAMING).addMemberId(member0.getId()).addStem(stem).findMembership(true), stem, member0},
        new Object[] {new MembershipFinder().assignFieldName("stemAdmins").assignFieldType(FieldType.NAMING).addMemberId(member0.getId()).addStem(stem1).findMembership(true), stem1, member0}
        ), membershipsOwnersMembers);
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ1).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(rootStem, stem), "Should find first stem and root since subject1 has privs on first stem and can see ancestor stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ1).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find one stem since subject0 has privs on the stem");

    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ1).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(1, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSetObject(
        new Object[] {new MembershipFinder().assignFieldName("stemAdmins").assignFieldType(FieldType.NAMING).addMemberId(member1.getId()).addStem(stem).findMembership(true), stem, member1}
        ), membershipsOwnersMembers);

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ2).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(rootStem, stem), "Should find first stem and root since subject2 has privs on the first stem and can see ancestor stems");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ2).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ2).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(1, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSetObject(
        new Object[] {new MembershipFinder().assignFieldName("creators").assignFieldType(FieldType.NAMING).addMemberId(member2.getId()).addStem(stem).findMembership(true), stem, member2}
        ), membershipsOwnersMembers);

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ3).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(rootStem, stem1), "Should find second stem and root since subject3 has privs on second stem and can see ancestor stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ3).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ3).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(1, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSetObject(
        new Object[] {new MembershipFinder().assignFieldName("stemViewers").assignFieldType(FieldType.NAMING).addMemberId(member3.getId()).addStem(stem1).findMembership(true), stem1, member3}
        ), membershipsOwnersMembers);

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ4).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ4).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    GrouperSession.stopQuietly(grouperSession);

    // as subj0
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(3, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(rootStem, stem, stem1), "Should find both stems since subject0 has privs on both stems and subj0 cant stemAdmin the root stem");
    
    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem, stem1), "Should find both stems since subject0 has privs on both stems");
    
    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ0).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(2, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSet(
        new Object[] {new MembershipFinder().assignFieldName("stemAdmins").assignFieldType(FieldType.NAMING).addMemberId(member0.getId()).addStem(stem).findMembership(true), stem, member0},
        new Object[] {new MembershipFinder().assignFieldName("stemAdmins").assignFieldType(FieldType.NAMING).addMemberId(member0.getId()).addStem(stem1).findMembership(true), stem1, member0}
        ), membershipsOwnersMembers);

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ1).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find first stem since subject1 has privs on first stem and stem0 has stemAdmin on first stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ1).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find one stem since subject0 has privs on the stem");

    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ1).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(1, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSetObject(
        new Object[] {new MembershipFinder().assignFieldName("stemAdmins").assignFieldType(FieldType.NAMING).addMemberId(member1.getId()).addStem(stem).findMembership(true), stem, member1}
        ), membershipsOwnersMembers);

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ2).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find first stem since subject2 has privs on the first stem and subject0 can stemAdmin on first stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ2).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    membershipsOwnersMembers = new MembershipFinder().addSubject(SubjectTestHelper.SUBJ2).assignCheckSecurity(true).assignFieldType(FieldType.NAMING).assignEnabled(true).assignHasFieldForStem(true)
        .assignHasMembershipTypeForStem(true).assignSplitScopeForStem(true).findMembershipResult().getMembershipsOwnersMembers();
    
    assertEquals(1, GrouperUtil.length(membershipsOwnersMembers));
    assertEqualsObjectArrays(GrouperUtil.toSetObject(
        new Object[] {new MembershipFinder().assignFieldName("creators").assignFieldType(FieldType.NAMING).addMemberId(member2.getId()).addStem(stem).findMembership(true), stem, member2}
        ), membershipsOwnersMembers);

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ3).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem1), "Should find second stem since subject3 has privs on second stem and subject0 can stemAdmin second stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ3).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ4).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ4).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    GrouperSession.stopQuietly(grouperSession);
    
    // as subj1
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find one stem since subject0 has privs on both stems but subject1 can only stemAdmin one stem");
    
    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem, stem1), "Should find both stems since subject0 has privs on both stems");
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ1).findStems();
    
    assertEquals(2, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem, rootStem), "Should find first stem and root since subject1 has privs on first stem and subject1 has can see ancestor stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ1).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find one stem since subject0 has privs on the stem");

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ2).findStems();
    
    assertEquals(1, GrouperUtil.length(stems));

    assertContainsStems(stems, GrouperUtil.toSet(stem), "Should find first stem since subject2 has privs on the first stem and subject1 can stemAdmin on first stem");

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ2).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ3).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ3).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ4).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ4).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

    GrouperSession.stopQuietly(grouperSession);
    
    // as subj2
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(0, GrouperUtil.length(stems));

//    stems = new StemFinder().assignPrivileges(GrouperUtil.toSet(NamingPrivilege.STEM_ADMIN)).assignSubject(SubjectTestHelper.SUBJ0).findStems();
//    
//    assertEquals(0, GrouperUtil.length(stems));

    GrouperSession.stopQuietly(grouperSession);
    
  }
  

  /**
   * 
   */
  public void testFindByName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("alternate");
    stem.store();

    Stem stem1 = new StemSave(grouperSession).assignName("stem1").assignCreateParentStemsIfNotExist(true).save();
    stem1.addAlternateName("alternate1");
    stem1.store();

    assertNotNull(StemFinder.findByName(grouperSession, "stem", false));
    assertNotNull(StemFinder.findByName(grouperSession, "alternate", false));
    
    Stem foundStem = new StemFinder().assignFindByUuidOrName(true).assignScope(stem.getName()).findStem();
    
    assertNotNull(foundStem);
    assertEquals(stem.getName(), foundStem.getName());
    
    
    foundStem = new StemFinder().assignFindByUuidOrName(true).assignScope(stem.getUuid()).findStem();
    
    assertNotNull(foundStem);
    assertEquals(stem.getName(), foundStem.getName());
    
    foundStem = new StemFinder().assignFindByUuidOrName(true).assignScope("alternate").findStem();
    
    assertNotNull(foundStem);
    assertEquals(stem.getName(), foundStem.getName());
    
  }
  
  /**
   * 
   */
  public void testFindByCurrentName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("alternate");
    stem.store();
    
    assertNotNull(StemFinder.findByCurrentName(grouperSession, "stem", false, null));
    assertNull(StemFinder.findByCurrentName(grouperSession, "alternate", false, null));
  }
  
  /**
   * 
   */
  public void testFindByAlternateName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("alternate");
    stem.store();
    
    assertNull(StemFinder.findByAlternateName(grouperSession, "stem", false, null));
    assertNotNull(StemFinder.findByAlternateName(grouperSession, "alternate", false, null));
  }
  
  public void testFindRootStem() {
    LOG.info("testFindRootStem");
    StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
  } // public void testFindRootStem()

  public void testFindRootByName() {
    LOG.info("testFindRootByName");
    try {
      GrouperSession  s   = SessionHelper.getRootSession();
      Stem            frs = StemHelper.findRootStem(s);
      Stem            fbn = StemHelper.findByName(s, "");
      Assert.assertTrue("frs == fbn", frs.equals(fbn));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testFindRootByName()

  // TESTS //  
  
  public void testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("lower", "UPPER");
  
      assertEquals(
        "stems found by displayExtension",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension( child.getDisplayExtension() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateDisplayName_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateDisplayName_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("lower", "UPPER");
  
      assertEquals(
        "stems found by displayName",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName( child.getDisplayName() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateDisplayName_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateExtension_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateExtension_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("UPPER", "lower");
  
      assertEquals(
        "stems found by extension",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension( child.getExtension() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateExtension_whenUpperCaseInRegistry

  /**
   * 
   */
  public void testFindByAttributeDefName() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignAttributeDefNameToEdit("test:attrDef")
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
        .assignToStem(true)
        .assignValueType(AttributeDefValueType.string).save();

    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef)
      .assignAttributeDefNameNameToEdit("test:attrDefName").assignCreateParentStemsIfNotExist(true).save();

    Stem stem0 = new StemSave(grouperSession).assignName("test:stem0").assignCreateParentStemsIfNotExist(true).save();
    Stem stem1 = new StemSave(grouperSession).assignName("test:stem1").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2 = new StemSave(grouperSession).assignName("test:stem2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem3 = new StemSave(grouperSession).assignName("test:stem3").assignCreateParentStemsIfNotExist(true).save();
    Stem stem4 = new StemSave(grouperSession).assignName("test:stem4").assignCreateParentStemsIfNotExist(true).save();

    stem0.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    stem1.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "xyz");
    stem3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    stem4.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");

    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    stem0.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    stem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    
    stem0.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem1.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem4.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    
    //subj0 can read most of both
    //subj1 can read the attr
    //subj2 can read the group attrs
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
    
    assertEquals(3, GrouperUtil.length(stems));
    assertEquals("test:stem0", stems.get(0).getName());
    assertEquals("test:stem1", stems.get(1).getName());
    assertEquals("test:stem3", stems.get(2).getName());
    
    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems());
    
    assertEquals(2, GrouperUtil.length(stems));
    assertEquals("test:stem0", stems.get(0).getName());
    assertEquals("test:stem3", stems.get(1).getName());
    
    GrouperSession.stopQuietly(grouperSession);
    

    // #####################
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
    
    assertEquals(0, GrouperUtil.length(stems));
    
    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems());
    
    assertEquals(0, GrouperUtil.length(stems));
    
    GrouperSession.stopQuietly(grouperSession);
    
    // #####################
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());

    assertEquals(0, GrouperUtil.length(stems));

    stems = new ArrayList<Stem>(new StemFinder().assignIdOfAttributeDefName(attributeDefName.getId())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
    
    assertEquals(0, GrouperUtil.length(stems));
    
    stems = new ArrayList<Stem>(new StemFinder().assignIdOfAttributeDefName(attributeDefName.getId())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems());
    
    assertEquals(0, GrouperUtil.length(stems));

    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
  
  // TESTS //  
  
  public void testFindAllByApproximateName_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateName_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("UPPER", "lower");
  
      assertEquals(
        "stems found by name",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateName( child.getName() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateName_whenUpperCaseInRegistry

  public void testChainingStemFinderExcludingAlternateNames() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem-name").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("stem-alternate");
    stem.store();

    Set<Stem> foundStems = new StemFinder().assignScope("stem-n%").findStems();
    assertEquals("find 1 stem by name with scope", foundStems.size(), 1);
    assertContainsStem(foundStems, stem, "Found correct stem by name with scope");

    foundStems = new StemFinder().assignScope("stem-a%").findStems();
    assertEquals("find 1 stem by alternate name with scope", foundStems.size(), 1);
    assertContainsStem(foundStems, stem, "Found correct stem by name with scope");

    foundStems = new StemFinder().assignScope("bogus%").findStems();
    assertEquals("find 0 stems with non-existent name with scope", foundStems.size(), 0);

    /* When excluding alternate name in finder, should find by name but not alternate name */
    foundStems = new StemFinder().assignScope("stem-n%").assignExcludeAlternateNames(true).findStems();
    assertEquals("find 1 stem by name with scope excluding alternate name", foundStems.size(), 1);
    assertContainsStem(foundStems, stem, "Found correct stem by name with scope excluding alternate name");

    foundStems = new StemFinder().assignScope("stem-a%").assignExcludeAlternateNames(true).findStems();
    assertEquals("find 0 stems by alternate name with scope excluding alternate name", foundStems.size(), 0);
  }

}

