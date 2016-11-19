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

package edu.internet2.middleware.grouper.membership;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Test {@link MembershipFinder}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: TestMembershipFinder.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class TestMembershipFinder extends GrouperTest {

  /**
   * 
   */
  public TestMembershipFinder() {
    super();
  }

  /**
   * 
   * @param name
   */
  public TestMembershipFinder(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembershipFinder("testFindAttrUpdatePrivileges"));
  }

  /**
   * 
   */
  public void setUp() {
    super.setUp();
  }

  /**
   * 
   */
  public void tearDown() {
    super.tearDown();
  }

  /**
   * 
   */
  public void testFindStemPrivileges() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stemA = new StemSave(grouperSession).assignName("stemA").assignCreateParentStemsIfNotExist(true).save();
    Stem stemB = new StemSave(grouperSession).assignName("stemB").assignCreateParentStemsIfNotExist(true).save();
    
    stemA.revokePriv(SubjectFinder.findRootSubject(), NamingPrivilege.STEM, false);
    stemB.revokePriv(SubjectFinder.findRootSubject(), NamingPrivilege.STEM, false);

    stemA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ6, NamingPrivilege.STEM, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ATTR_READ, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_UPDATE, false);
    stemA.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.STEM_ATTR_UPDATE, false);

    stemB.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.CREATE, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.CREATE, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM, false);
    stemB.grantPriv(SubjectTestHelper.SUBJA, NamingPrivilege.STEM, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_READ, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_UPDATE, false);
    stemB.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_UPDATE, false);

    //membership, stem, member
    //get them all for a stem
    Set<Object[]> result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).findMembershipsMembers();
    assertEquals(10, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ2, NamingPrivilege.STEM);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ6, NamingPrivilege.STEM);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ATTR_READ);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_UPDATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ8, NamingPrivilege.STEM_ATTR_UPDATE);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true).findMembershipsMembers();
    assertEquals(10, GrouperUtil.length(result));

    //check by calling user, security
    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //this one doesnt have privs
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true).findMembershipsMembers();
    assertEquals(0, GrouperUtil.length(result));
    
    GrouperSession.stopQuietly(grouperSession);

    //this subject does have privs on the stem
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true).findMembershipsMembers();
    assertEquals(10, GrouperUtil.length(result));

    //get the first page of two members
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, true);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).findMembershipsMembers();
    
    assertEquals(3, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    
    assertEquals(9, queryOptions.getQueryPaging().getTotalRecordCount());

    //get the result object by subject
    List<MembershipSubjectContainer> membershipSubjectContainers = new ArrayList<MembershipSubjectContainer>(new MembershipFinder()
      .assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).findMembershipResult().getMembershipSubjectContainers());
    
    assertEquals(2, GrouperUtil.length(membershipSubjectContainers));

    assertEquals(SubjectTestHelper.SUBJ0_ID, membershipSubjectContainers.get(0).getSubject().getId());
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(0).getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, membershipSubjectContainers.get(1).getSubject().getId());
    
    
    assertEquals(9, queryOptions.getQueryPaging().getTotalRecordCount());
    
    //get the second page of two members
    queryOptions = new QueryOptions();
    queryOptions.paging(2, 2, false);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).findMembershipsMembers();
    
    assertEquals(2, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ2, NamingPrivilege.STEM);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ATTR_READ);
    
    assertNull(queryOptions.getCount());

    //do paging, and filter for a search string in the subject, all test subjects have test
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignScopeForMember("test").findMembershipsMembers();

    assertEquals(2, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ2, NamingPrivilege.STEM);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ATTR_READ);

    assertNull(queryOptions.getCount());

    //no test subjects have xxx
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignScopeForMember("xxx").findMembershipsMembers();
    
    assertEquals(0, GrouperUtil.length(result));

    queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, false);

    //dont split scope, must have test and 2, no subject has this string
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignScopeForMember("test 2").findMembershipsMembers();
    
    assertEquals(0, GrouperUtil.length(result));

    //do split scope, one subject has test and 2
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignScopeForMember("test 2")
        .assignSplitScopeForMember(true).findMembershipsMembers();

    assertEquals(1, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ2, NamingPrivilege.STEM);

    //subjects the have create or stem
    queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, true);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignFieldName(Field.FIELD_NAME_CREATORS).assignHasFieldForMember(true)
        .findMembershipsMembers();
    
    assertEquals(3, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    
    assertEquals(3, queryOptions.getQueryPaging().getTotalRecordCount());

    //subjects the have create or stem, second page
    queryOptions = new QueryOptions();
    queryOptions.paging(2, 2, true);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignFieldName(Field.FIELD_NAME_CREATORS).assignHasFieldForMember(true)
        .findMembershipsMembers();
    
    assertEquals(1, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE);
    
    assertEquals(3, queryOptions.getQueryPaging().getTotalRecordCount());

    //subjects the have immediate privileges
    queryOptions = new QueryOptions();
    queryOptions.paging(2, 2, true);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignFieldName(Field.FIELD_NAME_CREATORS).assignHasFieldForMember(true)
        .assignMembershipType(MembershipType.IMMEDIATE).assignHasMembershipTypeForMember(true)
        .findMembershipsMembers();

    assertEquals(1, GrouperUtil.length(result));

    assertHasPrivilege(result, stemA, SubjectTestHelper.SUBJ5, NamingPrivilege.CREATE);

    assertEquals(3, queryOptions.getQueryPaging().getTotalRecordCount());

    //subjects the have composite privileges
    queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, true);
    
    result = new MembershipFinder().assignStemIds(GrouperUtil.toSet(stemA.getId())).assignCheckSecurity(true)
        .assignQueryOptionsForMember(queryOptions).assignFieldName(Field.FIELD_NAME_CREATORS).assignHasFieldForMember(true)
        .assignMembershipType(MembershipType.COMPOSITE).assignHasMembershipTypeForMember(true)
        .findMembershipsMembers();
    
    assertEquals(0, GrouperUtil.length(result));

    assertEquals(0, queryOptions.getQueryPaging().getTotalRecordCount());

  }

  /**
   * 
   */
  public void testFindUpdatePrivileges() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignName("theStem:theGroup").assignCreateParentStemsIfNotExist(true).save();

    Group[] groups = new Group[26];
    
    for (int i=0;i<26;i++) {
      groups[i] = new GroupSave(grouperSession).assignName("stemA:group" + (char)('A' + i)).assignCreateParentStemsIfNotExist(true).save();
      
      if (i>=0 && i <= 3) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.ADMIN, false);

      }
      if (i>=3 && i <= 6) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.UPDATE, false);

      }
      if (i>=6 && i <= 9) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.READ, false);

      }
      if (i>=9 && i <= 12) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.VIEW, false);

      }
      if (i>=12 && i <= 15) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.OPTIN, false);

      }
      if (i>=15 && i <= 18) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.OPTOUT, false);

      }
      if (i>=18 && i <= 21) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.GROUP_ATTR_READ, false);

      }
      if (i>=21 && i <= 24) {
        groups[i].grantPriv(group.toSubject(), AccessPrivilege.GROUP_ATTR_UPDATE, false);

      }
      
    }
    

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
      .assignHasFieldForGroup(false)
      .assignEnabled(true)
      .assignHasMembershipTypeForGroup(true)
      .assignQueryOptionsForGroup(queryOptions)
      .assignSplitScopeForGroup(true);
  
    membershipFinder.assignFieldName("admins");
  
    //membershipFinder.assignScopeForGroup("%");

    //#############################  first page
    
    //membership, stem, member
    //get them all for a stem
    Set<Object[]> result = membershipFinder.findMembershipsMembers();
    assertEquals(2, GrouperUtil.length(result));

    assertHasPrivilege(result, groups[0], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[1], group.toSubject(), AccessPrivilege.ADMIN);

    //############################# second page
    
    queryOptions.paging(2, 2, true);

    result = membershipFinder.findMembershipsMembers();
    assertEquals(2, GrouperUtil.length(result));

    assertHasPrivilege(result, groups[2], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[3], group.toSubject(), AccessPrivilege.ADMIN);

    //############################# indirect privilege all

    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignFieldsByName(GrouperUtil.toSet("admins", "updaters"));

    result = membershipFinder.findMembershipsMembers();
    assertEquals(8, GrouperUtil.length(result));

    assertHasPrivilege(result, groups[0], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[1], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[3], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[3], group.toSubject(), AccessPrivilege.UPDATE);
    assertHasPrivilege(result, groups[4], group.toSubject(), AccessPrivilege.UPDATE);

    //############################# indirect privilege all

    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignFieldsByName(GrouperUtil.toSet("updaters"));
    membershipFinder.assignIncludeInheritedPrivileges(true);
    
    result = membershipFinder.findMembershipsMembers();
    assertEquals(8, GrouperUtil.length(result));

    assertHasPrivilege(result, groups[0], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[1], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[3], group.toSubject(), AccessPrivilege.ADMIN);
    assertHasPrivilege(result, groups[3], group.toSubject(), AccessPrivilege.UPDATE);
    assertHasPrivilege(result, groups[4], group.toSubject(), AccessPrivilege.UPDATE);

    //############################# indirect privilege all membership subject containers

    membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
      .assignHasFieldForGroup(false)
      .assignEnabled(true)
      .assignHasMembershipTypeForGroup(true)
      .assignQueryOptionsForGroup(queryOptions)
      .assignSplitScopeForGroup(true);
    
    membershipFinder.assignFieldName("updaters");

    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignIncludeInheritedPrivileges(true);

    List<MembershipSubjectContainer> membershipSubjectContainers = new ArrayList<MembershipSubjectContainer>(
        membershipFinder.findMembershipResult().getMembershipSubjectContainers());
    
    assertEquals(GrouperUtil.toStringForLog(membershipSubjectContainers), 7, GrouperUtil.length(membershipSubjectContainers));

    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(0)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[0].getName(), membershipSubjectContainers.get(0)
        .getGroupOwner().getName());

    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(1)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[1].getName(), membershipSubjectContainers.get(1)
        .getGroupOwner().getName());

    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(2)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[2].getName(), membershipSubjectContainers.get(2)
        .getGroupOwner().getName());

    assertEquals(MembershipAssignType.IMMEDIATE_AND_EFFECTIVE, membershipSubjectContainers.get(3)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[3].getName(), membershipSubjectContainers.get(3)
        .getGroupOwner().getName());

    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(4)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[4].getName(), membershipSubjectContainers.get(4)
        .getGroupOwner().getName());

    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(5)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[5].getName(), membershipSubjectContainers.get(5)
        .getGroupOwner().getName());

    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(6)
        .getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType());
    assertEquals(groups[6].getName(), membershipSubjectContainers.get(6)
        .getGroupOwner().getName());

    
  }


  public void test_findMembers_nullGroup() {
    try {
      MembershipFinder.findMembers(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findMembers_nullField() {
    try {
      MembershipFinder.findMembers( new Group(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findMembers_findZeroMembers() 
    throws  GroupAddException,  
            InsufficientPrivilegeException,
            SessionException,
            StemAddException
  {
    Group g = StemFinder.findRootStem( GrouperSession.start( SubjectFinder.findRootSubject() ) )
                .addChildStem("top", "top")
                .addChildGroup("child", "child")
                ;
    assertEquals( 0, MembershipFinder.findMembers( g, Group.getDefaultList() ).size() );
  }

  public void test_findMembers_findOneMember() 
    throws  GroupAddException,  
            InsufficientPrivilegeException,
            MemberAddException,
            SessionException,
            StemAddException
  {
    GrouperSession  s = GrouperSession.start( SubjectFinder.findRootSubject() );
    Group           g = StemFinder.findRootStem(s)
                          .addChildStem("top", "top")
                          .addChildGroup("child", "child")
                          ;
    g.addMember( SubjectFinder.findRootSubject() );
    assertEquals( 1, MembershipFinder.findMembers( g, Group.getDefaultList() ).size() );
  }

  /**
   * 
   */
  public void testFindCreatePrivileges() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignName("theStem:theGroup").assignCreateParentStemsIfNotExist(true).save();
  
    Stem[] stems = new Stem[26];
    
    for (int i=0;i<26;i++) {
      stems[i] = new StemSave(grouperSession).assignName("stemA:stem" + (char)('A' + i)).assignCreateParentStemsIfNotExist(true).save();
      
      if (i>=0 && i <= 3) {
        stems[i].grantPriv(group.toSubject(), NamingPrivilege.STEM, false);
      }
      if (i>=3 && i <= 6) {
        stems[i].grantPriv(group.toSubject(), NamingPrivilege.CREATE, false);
      }
      if (i>=6 && i <= 9) {
        stems[i].grantPriv(group.toSubject(), NamingPrivilege.STEM_ATTR_READ, false);
      }
      if (i>=9 && i <= 12) {
        stems[i].grantPriv(group.toSubject(), NamingPrivilege.STEM_ATTR_UPDATE, false);
      }
      
    }
    
  
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
      //.assignHasFieldForGroup(false)
      .assignEnabled(true)
      //.assignHasMembershipTypeForGroup(true)
      .assignQueryOptionsForStem(queryOptions)
      .assignSplitScopeForStem(true);
  
    membershipFinder.assignFieldName("stemmers");
  
    //membershipFinder.assignScopeForGroup("%");
  
    //#############################  first page
    
    //membership, stem, member
    //get them all for a stem
    Set<Object[]> result = membershipFinder.findMembershipsMembers();
    assertEquals(2, GrouperUtil.length(result));
  
    assertHasPrivilege(result, stems[0], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[1], group.toSubject(), NamingPrivilege.STEM);
  
    //############################# second page
    
    queryOptions.paging(2, 2, true);
  
    result = membershipFinder.findMembershipsMembers();
    assertEquals(2, GrouperUtil.length(result));
  
    assertHasPrivilege(result, stems[2], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[3], group.toSubject(), NamingPrivilege.STEM);
  
    //############################# indirect privilege all
  
    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignFieldsByName(GrouperUtil.toSet("stemmers", "creators"));
  
    result = membershipFinder.findMembershipsMembers();
    assertEquals(8, GrouperUtil.length(result));
  
    assertHasPrivilege(result, stems[0], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[1], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[3], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[3], group.toSubject(), NamingPrivilege.CREATE);
    assertHasPrivilege(result, stems[4], group.toSubject(), NamingPrivilege.CREATE);
  
    //############################# indirect privilege all
  
    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignFieldsByName(GrouperUtil.toSet("creators"));
    membershipFinder.assignIncludeInheritedPrivileges(true);
    
    result = membershipFinder.findMembershipsMembers();
    assertEquals(8, GrouperUtil.length(result));
  
    assertHasPrivilege(result, stems[0], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[1], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[3], group.toSubject(), NamingPrivilege.STEM);
    assertHasPrivilege(result, stems[3], group.toSubject(), NamingPrivilege.CREATE);
    assertHasPrivilege(result, stems[4], group.toSubject(), NamingPrivilege.CREATE);
  
    //############################# indirect privilege all membership subject containers
  
    membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
      .assignHasFieldForStem(false)
      .assignEnabled(true)
      .assignHasMembershipTypeForStem(true)
      .assignQueryOptionsForStem(queryOptions)
      .assignSplitScopeForStem(true);
    
    membershipFinder.assignFieldName("creators");
  
    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignIncludeInheritedPrivileges(true);
  
    List<MembershipSubjectContainer> membershipSubjectContainers = new ArrayList<MembershipSubjectContainer>(
        membershipFinder.findMembershipResult().getMembershipSubjectContainers());
    
    assertEquals(GrouperUtil.toStringForLog(membershipSubjectContainers), 7, GrouperUtil.length(membershipSubjectContainers));
  
    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(0)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[0].getName(), membershipSubjectContainers.get(0)
        .getStemOwner().getName());
  
    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(1)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[1].getName(), membershipSubjectContainers.get(1)
        .getStemOwner().getName());
  
    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(2)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[2].getName(), membershipSubjectContainers.get(2)
        .getStemOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE_AND_EFFECTIVE, membershipSubjectContainers.get(3)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[3].getName(), membershipSubjectContainers.get(3)
        .getStemOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(4)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[4].getName(), membershipSubjectContainers.get(4)
        .getStemOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(5)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[5].getName(), membershipSubjectContainers.get(5)
        .getStemOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(6)
        .getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType());
    assertEquals(stems[6].getName(), membershipSubjectContainers.get(6)
        .getStemOwner().getName());
  
    
  }

  /**
   * 
   */
  public void testFindAttrUpdatePrivileges() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignName("theStem:theGroup").assignCreateParentStemsIfNotExist(true).save();
  
    AttributeDef[] attributeDefs = new AttributeDef[26];
    
    for (int i=0;i<26;i++) {
      attributeDefs[i] = new AttributeDefSave(grouperSession).assignName("stemA:attrDef" + (char)('A' + i)).assignCreateParentStemsIfNotExist(true).save();
      
      if (i>=0 && i <= 3) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);
  
      }
      if (i>=3 && i <= 6) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
  
      }
      if (i>=6 && i <= 9) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_READ, false);
  
      }
      if (i>=9 && i <= 12) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_VIEW, false);
  
      }
      if (i>=12 && i <= 15) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
  
      }
      if (i>=15 && i <= 18) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_OPTOUT, false);
  
      }
      if (i>=18 && i <= 21) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
  
      }
      if (i>=21 && i <= 24) {
        attributeDefs[i].getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
  
      }
      
    }
    
  
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
      .assignHasFieldForAttributeDef(false)
      .assignEnabled(true)
      .assignHasMembershipTypeForAttributeDef(true)
      .assignQueryOptionsForAttributeDef(queryOptions)
      .assignSplitScopeForAttributeDef(true);
  
    membershipFinder.assignFieldName("attrAdmins");
  
    //membershipFinder.assignScopeForAttributeDef("%");
  
    //#############################  first page
    
    //membership, stem, member
    //get them all for a stem
    Set<Object[]> result = membershipFinder.findMembershipsMembers();
    assertEquals(2, GrouperUtil.length(result));
  
    assertHasPrivilege(result, attributeDefs[0], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[1], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
  
    //############################# second page
    
    queryOptions.paging(2, 2, true);
  
    result = membershipFinder.findMembershipsMembers();
    assertEquals(2, GrouperUtil.length(result));
  
    assertHasPrivilege(result, attributeDefs[2], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[3], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
  
    //############################# indirect privilege all
  
    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignFieldsByName(GrouperUtil.toSet("attrAdmins", "attrUpdaters"));
  
    result = membershipFinder.findMembershipsMembers();
    assertEquals(8, GrouperUtil.length(result));
  
    assertHasPrivilege(result, attributeDefs[0], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[1], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[3], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[3], group.toSubject(), AttributeDefPrivilege.ATTR_UPDATE);
    assertHasPrivilege(result, attributeDefs[4], group.toSubject(), AttributeDefPrivilege.ATTR_UPDATE);
  
    //############################# indirect privilege all
  
    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignFieldsByName(GrouperUtil.toSet("attrUpdaters"));
    membershipFinder.assignIncludeInheritedPrivileges(true);
    
    result = membershipFinder.findMembershipsMembers();
    assertEquals(8, GrouperUtil.length(result));
  
    assertHasPrivilege(result, attributeDefs[0], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[1], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[3], group.toSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    assertHasPrivilege(result, attributeDefs[3], group.toSubject(), AttributeDefPrivilege.ATTR_UPDATE);
    assertHasPrivilege(result, attributeDefs[4], group.toSubject(), AttributeDefPrivilege.ATTR_UPDATE);
  
    //############################# indirect privilege all membership subject containers
  
    membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
      .assignHasFieldForAttributeDef(false)
      .assignEnabled(true)
      .assignHasMembershipTypeForAttributeDef(true)
      .assignQueryOptionsForAttributeDef(queryOptions)
      .assignSplitScopeForAttributeDef(true);
    
    membershipFinder.assignFieldName("attrUpdaters");
  
    queryOptions.paging(10, 1, true);
    
    membershipFinder.assignIncludeInheritedPrivileges(true);

    List<MembershipSubjectContainer> membershipSubjectContainers = new ArrayList<MembershipSubjectContainer>(
        membershipFinder.findMembershipResult().getMembershipSubjectContainers());

    assertEquals(GrouperUtil.toStringForLog(membershipSubjectContainers), 7, GrouperUtil.length(membershipSubjectContainers));

    assertEquals(GrouperUtil.toStringForLog(membershipSubjectContainers), 
        MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(0)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[0].getName(), membershipSubjectContainers.get(0)
        .getAttributeDefOwner().getName());
  
    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(1)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[1].getName(), membershipSubjectContainers.get(1)
        .getAttributeDefOwner().getName());
  
    assertEquals(MembershipAssignType.EFFECTIVE, membershipSubjectContainers.get(2)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[2].getName(), membershipSubjectContainers.get(2)
        .getAttributeDefOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE_AND_EFFECTIVE, membershipSubjectContainers.get(3)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[3].getName(), membershipSubjectContainers.get(3)
        .getAttributeDefOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(4)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[4].getName(), membershipSubjectContainers.get(4)
        .getAttributeDefOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(5)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[5].getName(), membershipSubjectContainers.get(5)
        .getAttributeDefOwner().getName());
  
    assertEquals(MembershipAssignType.IMMEDIATE, membershipSubjectContainers.get(6)
        .getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType());
    assertEquals(attributeDefs[6].getName(), membershipSubjectContainers.get(6)
        .getAttributeDefOwner().getName());
  
    
  }
  
  /**
   * 
   */
  public void testPrivileges() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group = new GroupSave(grouperSession).assignName("theStem:theGroup").assignCreateParentStemsIfNotExist(true).save();
    Subject subj0 = SubjectFinder.findById("test.subject.0", true);
    Subject subj1 = SubjectFinder.findById("test.subject.1", true);
    Subject subj2 = SubjectFinder.findById("test.subject.2", true);
    Subject subj3 = SubjectFinder.findById("test.subject.3", true);
    
    group.addMember(subj0);
    group.addMember(subj1);
    group.addMember(subj2);
    group.addMember(subj3);
    
    group.grantPriv(subj0, AccessPrivilege.READ);
    group.grantPriv(subj1, AccessPrivilege.OPTIN);
    group.grantPriv(subj2, AccessPrivilege.OPTOUT);
    group.grantPriv(subj3, AccessPrivilege.VIEW);
    
    GrouperSession grouperSession2 = GrouperSession.start(subj0);
    
    assertEquals(1, new MembershipFinder().assignSubjectHasMembershipForGroup(subj0).addSubject(subj0).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(1, new MembershipFinder().assignSubjectHasMembershipForGroup(subj1).addSubject(subj1).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(1, new MembershipFinder().assignSubjectHasMembershipForGroup(subj2).addSubject(subj2).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(1, new MembershipFinder().assignSubjectHasMembershipForGroup(subj3).addSubject(subj3).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());

    GrouperSession.stopQuietly(grouperSession2);
    
    GrouperSession grouperSession3 = GrouperSession.start(subj1);
    
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj0).addSubject(subj0).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(1, new MembershipFinder().assignSubjectHasMembershipForGroup(subj1).addSubject(subj1).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj2).addSubject(subj2).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj3).addSubject(subj3).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());

    GrouperSession.stopQuietly(grouperSession3);

    GrouperSession grouperSession4 = GrouperSession.start(subj2);
    
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj0).addSubject(subj0).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj1).addSubject(subj1).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(1, new MembershipFinder().assignSubjectHasMembershipForGroup(subj2).addSubject(subj2).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj3).addSubject(subj3).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());

    GrouperSession.stopQuietly(grouperSession4);
    
    GrouperSession grouperSession5 = GrouperSession.start(subj3);
    
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj0).addSubject(subj0).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj1).addSubject(subj1).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj2).addSubject(subj2).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());
    assertEquals(0, new MembershipFinder().assignSubjectHasMembershipForGroup(subj3).addSubject(subj3).assignCheckSecurity(true).assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES).assignEnabled(true).findMembershipResult().getMembershipSubjectContainers().size());

    GrouperSession.stopQuietly(grouperSession5);
  }

}

