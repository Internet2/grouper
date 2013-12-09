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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
    TestRunner.run(new TestMembershipFinder("testFindStemPrivileges"));
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
    g.addMember( SubjectFinder.findAllSubject() );
    assertEquals( 1, MembershipFinder.findMembers( g, Group.getDefaultList() ).size() );
  }

}

