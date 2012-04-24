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

package edu.internet2.middleware.grouper.member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Test {@link Member}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMember.java,v 1.5 2009-12-17 06:57:57 mchyzer Exp $
 */
public class TestMember extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMember("testResolveSubjects"));
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TestMember.class);

  /**
   * 
   * @param name
   */
  public TestMember(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testResolveSubjects() {
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
        
    Group group = new GroupSave(grouperSession)
      .assignName("test:testGroup")
      .assignCreateParentStemsIfNotExist(true).save();

    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(SubjectTestHelper.SUBJ5);
    group.addMember(SubjectTestHelper.SUBJ6);
    group.addMember(SubjectTestHelper.SUBJ7);
    group.addMember(SubjectTestHelper.SUBJ8);
    group.addMember(SubjectTestHelper.SUBJ9);
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    
    long initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;

    Set<Member> members = group.getMembers();
    
    Member.resolveSubjects(members, false);
    
    //should be one query
    long queryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    //3, one for members, fields, subjects
    assertEquals("Query count: " + (queryCount - initialQueryCount), initialQueryCount+3, queryCount);

    //###################  SHOULD BE THERE
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();

    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    
    for (Member member : members) {
      member.getSubject().getName();
    }
    
    queryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    assertEquals("Query count: " + (queryCount - initialQueryCount), initialQueryCount, queryCount);
  }
  
  public void testGetSource() {
    LOG.info("testGetSource");
    GrouperSession s = SessionHelper.getRootSession();
    try {
      // I'm not sure what to test on source retrieval
      Member  m   = s.getMember();
      Source  src = m.getSubjectSource();
      Assert.assertNotNull("src !null", src);
      Assert.assertTrue("src instanceof Source", src instanceof Source);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetSource()

  /**
   * 
   */
  public void testGetMembersBySource() {
    Subject         subj  = SubjectTestHelper.SUBJ0;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    Member          member = MemberFinder.findBySubject(s, subj, true);
    Member          groupMember = MemberFinder.findBySubject(s, uofc.toSubject(), true);
    
    
    Set<Member> members = i2.getMembers(Group.getDefaultList(), GrouperUtil.toSet(SourceManager.getInstance().getSource("jdbc")), null);
    assertEquals(1, members.size());
    assertEquals(member, members.iterator().next());
    
    
    members = i2.getMembers(Group.getDefaultList(), GrouperUtil.toSet(SourceManager.getInstance().getSource("g:gsa")), null);
    assertEquals(1, members.size());
    assertEquals(groupMember, members.iterator().next());
    
    members = i2.getMembers(Group.getDefaultList(), GrouperUtil.toSet(SourceManager.getInstance().getSource("g:gsa"),
        SourceManager.getInstance().getSource("jdbc")), null);
    assertEquals(2, members.size());
    
    members = i2.getMembers(Group.getDefaultList(), GrouperUtil.convertSources("g:gsa, jdbc"), null);
    assertEquals(2, members.size());
    
    members = i2.getImmediateMembers(Group.getDefaultList(), GrouperUtil.toSet(SourceManager.getInstance().getSource("g:gsa")), null);
    assertEquals(1, members.size());
    assertEquals(groupMember, members.iterator().next());
    
    
  }

  /**
   * 
   */
  public void testNonImmediateMembershipsAndGroups() {
    Subject         subj  = SubjectTestHelper.SUBJ0;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           comp1    = edu.addChildGroup("comp1", "comp1");
    Group           compLeft    = edu.addChildGroup("compLeft", "compRight");
    Group           compRight    = edu.addChildGroup("compRight", "compRight");
    
    comp1.addCompositeMember(CompositeType.INTERSECTION, compLeft, compRight);
    
    compLeft.addMember(subj);  
    compRight.addMember(subj);  
    
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    Member          member     = MemberFinder.findBySubject(s, subj, true);
    
    Set<Member> members = compLeft.getNonImmediateMembers();
    assertEquals(0, members.size());
    
    members = comp1.getNonImmediateMembers();
    assertEquals(1, members.size());
    assertEquals(member, members.iterator().next());
    
    members = uofc.getNonImmediateMembers();
    assertEquals(0, members.size());
    assertFalse(uofc.hasNonImmediateMember(subj));
    
    members = i2.getNonImmediateMembers();
    assertEquals(1, members.size());
    assertEquals(member, members.iterator().next());
    assertTrue(i2.hasNonImmediateMember(subj));
    
    //########################################
    
    Set<Membership> memberships = compLeft.getNonImmediateMemberships();
    assertEquals(0, memberships.size());
    
    memberships = comp1.getNonImmediateMemberships();
    assertEquals(1, memberships.size());
    assertEquals(member.getUuid(), memberships.iterator().next().getMemberUuid());
    
    memberships = uofc.getNonImmediateMemberships();
    assertEquals(0, memberships.size());
    
    memberships = i2.getNonImmediateMemberships();
    assertEquals(1, memberships.size());
    assertEquals(member.getUuid(), memberships.iterator().next().getMemberUuid());

    //########################################
    assertTrue(member.isNonImmediateMember(comp1));
    assertTrue(member.isNonImmediateMember(i2));
    assertFalse(member.isNonImmediateMember(compLeft));
    assertFalse(member.isNonImmediateMember(uofc));
    
    Set<Group> groups = member.getNonImmediateGroups();
    
    assertEquals(2, groups.size());
    assertTrue(groups.contains(comp1));
    assertTrue(groups.contains(i2));
    
    memberships = member.getNonImmediateMemberships();
    assertEquals(2, memberships.size());
    
  }
  
  /**
   * 
   */
  public void testGetGroupsWithField() {
    Subject subj0 = SubjectTestHelper.SUBJ0;
    Subject subj1 = SubjectTestHelper.SUBJ1;
    
    GrouperSession s = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(s);
    
    Member member0 = MemberFinder.findBySubject(s, subj0, true);
    
    GroupType groupType = GroupType.createType(s, "customType", false);
    groupType.addList(s, "customList1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    groupType.addList(s, "customList2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    groupType.addList(s, "customList3", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    
    Stem top = root.addChildStem("top", "top");
    Group group1 = top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    Group group3 = top.addChildGroup("group3", "group3");
    Group group4 = top.addChildGroup("group4", "group4");
    Group group5 = top.addChildGroup("group5", "group5");
    Group group6 = top.addChildGroup("group6", "group6");
    
    group1.addType(groupType);
    group2.addType(groupType);
    group3.addType(groupType);
    group4.addType(groupType);
    group5.addType(groupType);
    group6.addType(groupType);
    
    group1.addMember(subj0);
    group2.addMember(subj0, FieldFinder.find("customList1", true));
    group3.grantPriv(subj0, AccessPrivilege.UPDATE);
    group4.addMember(subj0, FieldFinder.find("customList3", true));
    group5.addMember(subj0, FieldFinder.find("customList3", true));
    group6.addMember(subj1, FieldFinder.find("customList1", true));
    
    Set<Group> groups = member0.getGroups(FieldFinder.find("members", true));
    assertEquals(1, groups.size());
    assertEquals(group1.getUuid(), groups.iterator().next().getUuid());
    
    groups = member0.getGroups(FieldFinder.find("customList1", true));
    assertEquals(1, groups.size());
    assertEquals(group2.getUuid(), groups.iterator().next().getUuid());

    groups = member0.getGroups(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true));
    assertEquals(1, groups.size());
    assertEquals(group3.getUuid(), groups.iterator().next().getUuid());
    
    groups = member0.getGroups(FieldFinder.find("customList2", true));
    assertEquals(0, groups.size());

    groups = member0.getGroups(FieldFinder.find("customList3", true));
    assertEquals(2, groups.size());
  }
  
  /**
   * 
   */
  public void testGetGroupsComplex() {
    Subject         subj  = SubjectTestHelper.SUBJ0;
    Subject         subj1  = SubjectTestHelper.SUBJ1;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Stem            eduSub   = edu.addChildStem("eduSub", "eduSub");
    Stem            edu2   = root.addChildStem("edu2", "edu2");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           i2sub    = eduSub.addChildGroup("i2sub", "i2sub");
    Group           edu2i2sub    = edu2.addChildGroup("edu2i2sub", "edu2i2sub");
    Group           comp1    = edu.addChildGroup("comp1", "comp1");
    Group           compLeft    = edu.addChildGroup("compLeft", "compRight");
    Group           compRight    = edu.addChildGroup("compRight", "compRight");
    
    comp1.addCompositeMember(CompositeType.INTERSECTION, compLeft, compRight);
    
    compLeft.addMember(subj);  
    compRight.addMember(subj);  
    
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    Member          member     = MemberFinder.findBySubject(s, subj, true);
    Member          member1     = MemberFinder.findBySubject(s, subj1, true);
    
    i2sub.addMember(subj1);
    edu2i2sub.addMember(subj1);
    
    Set<Group> groups = member.getImmediateGroups();
    assertEquals(3, groups.size());
    assertTrue(groups.contains(compLeft));
    assertTrue(groups.contains(compRight));
    assertTrue(groups.contains(uofc));
    
    groups = member.getNonImmediateGroups();
    assertEquals(2, groups.size());
    assertTrue(groups.contains(comp1));
    assertTrue(groups.contains(i2));

    groups = member1.getEffectiveGroups();
    assertEquals(0, groups.size());
    
    groups = member1.getImmediateGroups(Group.getDefaultList(), "whatever", null, null, null, true);
    assertEquals(0, groups.size());
    
    groups = member1.getImmediateGroups(Group.getDefaultList(), "edu:eduSub", null, null, null, true);
    assertEquals(1, groups.size());
    assertEquals("edu:eduSub:i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());

    groups = member1.getImmediateGroups(Group.getDefaultList(), "edu2", null, null, null, true);
    assertEquals(1, groups.size());
    assertEquals("edu2:edu2i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());

    groups = member1.getImmediateGroups(Group.getDefaultList(), "edu2", null, null, null, false);
    assertEquals(0, groups.size());
    
    try {
      groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, null, null, true);
      fail("Need stemScope");
    } catch (Exception e) {
      //good
    }

    groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, Scope.ONE, null, true);
    assertEquals(0, groups.size());

    groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu2, Scope.ONE, null, true);
    assertEquals(1, groups.size());
    assertEquals("edu2:edu2i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());

    groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, Scope.SUB, null, true);
    assertEquals(1, groups.size());
    assertEquals("edu:eduSub:i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());

    groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu2, Scope.SUB, null, true);
    assertEquals(1, groups.size());
    assertEquals("edu2:edu2i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    QueryOptions queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("name");
    groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
    assertEquals(1, groups.size());
    assertEquals("edu:compLeft", ((Group)GrouperUtil.get(groups, 0)).getName());
    assertEquals(3, queryOptions.getQueryPaging().getTotalRecordCount());
    
    queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("displayName");
    groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
    assertEquals(1, groups.size());

    queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("extension");
    groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
    assertEquals(1, groups.size());

    queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("displayExtension");
    groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
    assertEquals(1, groups.size());

    queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("non existent column");
    try {
      groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
      fail("Column doesnt exist");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * see if a result array has a group in there, count the times
   * @param result
   * @param group
   * @return count of times group is there
   */
  @SuppressWarnings("unused")
  private static int membershipArrayHasGroup(Set<Object[]> result, Group group) {
    int count = 0;
    for (Object[] row : GrouperUtil.nonNull(result)) {
      Membership currentMembership = (Membership)row[0];
      Group currentGroup = (Group)row[1];
      Member currentMember = (Member)row[2];
      if (group.getName().equals(currentGroup.getName())) {
        count++;
      }
    }
    return count;
  }
  
  /**
   * see if a result array has a group in there, count the times
   * @param result
   * @param member
   * @return count of times group is there
   */
  private static int membershipArrayHasMember(Set<Object[]> result, Member member) {
    int count = 0;
    for (Object[] row : GrouperUtil.nonNull(result)) {
      Member currentMember = (Member)row[2];
      if (member.getUuid().equals(currentMember.getUuid())) {
        count++;
      }
    }
    return count;
  }

  /**
   * <pre>
   * edu(f)
   * edu:comp1
   * edu:compLeft
   * edu:compRight
   * edu:eduSub(f)
   * edu:eduSub:i2sub
   * edu:i2
   * edu:uofc
   * edu2(f)
   * 
   * 
   * </pre>
   */
  public void testGetMembershipsComplex2() {
    GrouperSession  grouperSession     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(grouperSession);
    Stem            edu   = root.addChildStem("edu", "edu");
    
    List<Group> groups = new ArrayList<Group>();
    List<String> groupIds = new ArrayList<String>();

    List<Member> members = new ArrayList<Member>();
    List<String> memberIds = new ArrayList<String>();
    
    for (int i=0;i<150;i++) {
      String id = "testId" + i;
      Subject subject = SubjectFinder.findById(id, false);
      if (subject == null) {
        subject = RegistrySubject.add( grouperSession, id, "person", "name_" + id );
        subject = SubjectFinder.findById(id, true);
      }
      Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      members.add(member);
      memberIds.add(member.getUuid());
    }

    List<Membership> memberships = new ArrayList<Membership>();
    List<String> membershipIds = new ArrayList<String>();
    
    for (int i=0;i<150;i++) {
      Group group = edu.addChildGroup("i" + i, "i" + i);
      groups.add(group);
      groupIds.add(group.getId());
      String id = "testId" + i;
      Subject subject = SubjectFinder.findById(id, true);
      group.addMember(subject);
      Membership membership = group.getImmediateMembership(Group.getDefaultList(), subject, true, true);
      memberships.add(membership);
      membershipIds.add(membership.getUuid());
    }
    
    //Set<Group> groups = member.getImmediateGroups();
    Set<Object[]> results = GrouperDAOFactory.getFactory().getMembership()
      .findAllByGroupOwnerOptions(null, 
        memberIds, null, null, null, null, null, null, null, true);
    
    assertEquals(150, results.size());
    assertEquals(1, membershipArrayHasGroup(results, groups.get(0)));
    
  }
  
  /**
   * <pre>
   * edu(f)
   * edu:comp1
   * edu:compLeft
   * edu:compRight
   * edu:eduSub(f)
   * edu:eduSub:i2sub
   * edu:i2
   * edu:uofc
   * edu2(f)
   * 
   * 
   * </pre>
   */
  public void testGetMembershipsSources() {
    Subject         subj  = SubjectTestHelper.SUBJ0;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");

    //Set<Group> groups = member.getImmediateGroups();
    Set<String> results = GrouperDAOFactory.getFactory().getMembership().findSourceIdsByGroupOwnerOptions(uofc.getId(), 
        MembershipType.IMMEDIATE, null, null);
    
    assertEquals(1, results.size());
    assertEquals("jdbc", results.iterator().next());
    
    results = GrouperDAOFactory.getFactory().getMembership().findSourceIdsByGroupOwnerOptions(uofc.getId(), 
        MembershipType.EFFECTIVE, null, null);
    
    assertEquals(0, results.size());
    
    results = GrouperDAOFactory.getFactory().getMembership().findSourceIdsByGroupOwnerOptions(uofc.getId(), 
        MembershipType.EFFECTIVE, Group.getDefaultList(), null);
    
    assertEquals(0, results.size());
    
    results = GrouperDAOFactory.getFactory().getMembership().findSourceIdsByGroupOwnerOptions(uofc.getId(), 
        MembershipType.IMMEDIATE, null, false);
    
    assertEquals(0, results.size());
    
    results = GrouperDAOFactory.getFactory().getMembership().findSourceIdsByGroupOwnerOptions(uofc.getId(), 
        MembershipType.IMMEDIATE, null, true);
    
    assertEquals(1, results.size());
    assertEquals("jdbc", results.iterator().next());

    GroupHelper.addMember(uofc, SubjectFinder.findRootSubject(), "members");
    
    results = GrouperDAOFactory.getFactory().getMembership().findSourceIdsByGroupOwnerOptions(uofc.getId(), 
        MembershipType.IMMEDIATE, null, true);
    
    assertEquals(2, results.size());
    assertTrue(results.contains("jdbc"));
    assertTrue(results.contains(SubjectFinder.findRootSubject().getSourceId()));
    
  }
  
  
  /**
   * <pre>
   * edu(f)
   * edu:comp1
   * edu:compLeft
   * edu:compRight
   * edu:eduSub(f)
   * edu:eduSub:i2sub
   * edu:i2
   * edu:uofc
   * edu2(f)
   * 
   * 
   * </pre>
   */
  public void testGetMembershipsComplex() {
    Subject         subj  = SubjectTestHelper.SUBJ0;
    Subject         subj1  = SubjectTestHelper.SUBJ1;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Stem            eduSub   = edu.addChildStem("eduSub", "eduSub");
    Stem            edu2   = root.addChildStem("edu2", "edu2");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           i2sub    = eduSub.addChildGroup("i2sub", "i2sub");
    Group           edu2i2sub    = edu2.addChildGroup("edu2i2sub", "edu2i2sub");
    Group           comp1    = edu.addChildGroup("comp1", "comp1");
    Group           compLeft    = edu.addChildGroup("compLeft", "compRight");
    Group           compRight    = edu.addChildGroup("compRight", "compRight");
    
    comp1.addCompositeMember(CompositeType.INTERSECTION, compLeft, compRight);
    
    compLeft.addMember(subj);  
    compRight.addMember(subj);  
    
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    Member          member     = MemberFinder.findBySubject(s, subj, true);
    Member          member1     = MemberFinder.findBySubject(s, subj1, true);
    
    i2sub.addMember(subj1);
    edu2i2sub.addMember(subj1);

    //Set<Group> groups = member.getImmediateGroups();
    Set<Object[]> results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member.getUuid()), null, MembershipType.IMMEDIATE, null, null, null, null, null, true);
    
    assertEquals(3, results.size());
    assertEquals(1, membershipArrayHasGroup(results, compLeft));
    assertEquals(1, membershipArrayHasGroup(results, compRight));
    assertEquals(1, membershipArrayHasGroup(results, uofc));
    
    //groups = member.getNonImmediateGroups();
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member.getUuid()), null, MembershipType.NONIMMEDIATE, null, null, null, null, null, true);

    assertEquals(2, results.size());
    assertEquals(1, membershipArrayHasGroup(results, comp1));
    assertEquals(1, membershipArrayHasGroup(results, i2));
    
    //groups = member1.getEffectiveGroups();
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.EFFECTIVE, null, null, null, null, null, true);
    assertEquals(0, results.size());

    //groups = member1.getImmediateGroups(Group.getDefaultList(), "whatever", null, null, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, null, null, "whatever", null, null, true);
    assertEquals(0, results.size());

    //groups = member1.getImmediateGroups(Group.getDefaultList(), "edu:eduSub", null, null, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, null, null, "edu:eduSub", null, null, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasGroup(results, i2sub));

    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, null, null, "edu:eduSub", null, null, null);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasGroup(results, i2sub));

    //groups = member1.getImmediateGroups(Group.getDefaultList(), "edu2", null, null, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, null, null, "edu2", null, null, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasGroup(results, edu2i2sub));

    //groups = member1.getImmediateGroups(Group.getDefaultList(), "edu2", null, null, null, false);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, null, null, "edu2", null, null, false);
    assertEquals(0, results.size());

    try {
      results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
          GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, null, null, null, edu, null, false);
      fail("Need stem scope");
    } catch (Exception e) {
      //good
    }
    
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, Scope.ONE, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, Group.getDefaultList(), null, null, edu, Scope.ONE, false);
    assertEquals(0, results.size());
    
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu2, Scope.ONE, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, Group.getDefaultList(), null, null, edu2, Scope.ONE, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasGroup(results, edu2i2sub));

    
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, Scope.SUB, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, Group.getDefaultList(), null, null, edu, Scope.SUB, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasGroup(results, i2sub));

    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu2, Scope.SUB, null, true);
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        GrouperUtil.toList(member1.getUuid()), null, MembershipType.IMMEDIATE, Group.getDefaultList(), null, null, edu2, Scope.SUB, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasGroup(results, edu2i2sub));

    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(GrouperUtil.toList(edu2i2sub.getUuid()), 
        null, null, MembershipType.IMMEDIATE, Group.getDefaultList(), null, null, edu2, Scope.SUB, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasMember(results, member1));

    String membershipId = ((Membership)results.iterator().next()[0]).getUuid();
  
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        null, GrouperUtil.toList(membershipId), MembershipType.IMMEDIATE, Group.getDefaultList(), null, null, edu2, Scope.SUB, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasMember(results, member1));
    assertEquals(1, membershipArrayHasGroup(results, edu2i2sub));
  
    results = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(null, 
        null, GrouperUtil.toList(membershipId), MembershipType.IMMEDIATE, Group.getDefaultList(), 
        GrouperUtil.toSet(SourceManager.getInstance().getSource("g:gsa")), null, edu2, Scope.SUB, true);
    assertEquals(0, results.size());
    
    results = MembershipFinder.findMemberships(null, 
        null, GrouperUtil.toList(membershipId), MembershipType.IMMEDIATE, Group.getDefaultList(), 
        GrouperUtil.toSet(SourceManager.getInstance().getSource("jdbc")), null, edu2, Scope.SUB, true);
    assertEquals(1, results.size());
    assertEquals(1, membershipArrayHasMember(results, member1));
    assertEquals(1, membershipArrayHasGroup(results, edu2i2sub));
    
  }
  
  
  
  /**
   * 
   */
  public void testGetMembershipsAndGroups() {
    LOG.info("testGetMembershipsAndGroups");
    try {
      Subject         subj  = SubjectTestHelper.SUBJ0;
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      Group           i2    = edu.addChildGroup("i2", "i2");
      Group           uofc  = edu.addChildGroup("uofc", "uofc");
      GroupHelper.addMember(uofc, subj, "members");
      GroupHelper.addMember(i2, uofc.toSubject(), "members");
      Member          m     = MemberFinder.findBySubject(s, subj, true);

      Field f = FieldFinder.find("members", true);

      // Get mships (by field and without)
      Assert.assertTrue(
        "getMship/!field", m.getMemberships().size() == 2
      );
      Assert.assertTrue(
        "getMship/field", m.getMemberships(f).size() == 2
      );

      // Get effective mships (by field and without)

      // Get immediate mships (by field and without)
      Assert.assertTrue(
        "getImmMship/!field", m.getImmediateMemberships().size() == 1
      );
      Iterator iterIMNF = m.getImmediateMemberships().iterator();
      while (iterIMNF.hasNext()) {
        Membership ms = (Membership) iterIMNF.next();
        Assert.assertTrue("IMNF owner", ms.getGroup().equals(uofc));
        Assert.assertTrue("IMNF member", ms.getMember().equals(m));
        Assert.assertTrue("IMNF field", ms.getList().equals(f));
        Assert.assertTrue("IMNF depth", ms.getDepth() == 0);
      } 
      Assert.assertTrue(
        "getImmMship/field", m.getImmediateMemberships(f).size() == 1
      );
      Iterator iterIMF = m.getImmediateMemberships(f).iterator();
      while (iterIMF.hasNext()) {
        Membership ms = (Membership) iterIMF.next();
        Assert.assertTrue("IMF owner", ms.getGroup().equals(uofc));
        Assert.assertTrue("IMF member", ms.getMember().equals(m));
        Assert.assertTrue("IMF field", ms.getList().equals(f));
        Assert.assertTrue("IMF depth", ms.getDepth() == 0);
      } 

      // Get effective mships (by field and without)
      Assert.assertTrue(
        "getEffMship/!field", m.getEffectiveMemberships().size() == 1
      );
      Iterator iterEMNF = m.getEffectiveMemberships().iterator();
      while (iterEMNF.hasNext()) {
        Membership ms = (Membership) iterEMNF.next();
        Assert.assertTrue("EMNF owner", ms.getGroup().equals(i2));
        Assert.assertTrue("EMNF member", ms.getMember().equals(m));
        Assert.assertTrue("EMNF field", ms.getList().equals(f));
        Assert.assertTrue("EMNF depth", ms.getDepth() == 1);
        Assert.assertTrue("EMNF via", ms.getViaGroup().equals(uofc));
      }
      Assert.assertTrue(
        "getEffMship/field", m.getEffectiveMemberships(f).size() == 1
      );
      Iterator iterEMF = m.getEffectiveMemberships(f).iterator();
      while (iterEMF.hasNext()) {
        Membership ms = (Membership) iterEMF.next();
        Assert.assertTrue("EMF owner", ms.getGroup().equals(i2));
        Assert.assertTrue("EMF member", ms.getMember().equals(m));
        Assert.assertTrue("EMF field", ms.getList().equals(f));
        Assert.assertTrue("EMF depth", ms.getDepth() == 1);
        Assert.assertTrue("EMF via", ms.getViaGroup().equals(uofc));
      }

      // Get groups
      Assert.assertTrue("groups == 2", m.getGroups().size() == 2);
      Iterator iterG = m.getGroups().iterator();
      while (iterG.hasNext()) {
        Group g = (Group) iterG.next();
        if      (g.equals(i2)) {  
          Assert.assertTrue("imm group: i2", true);
        }
        else if (g.equals(uofc)) {
          Assert.assertTrue("imm group: uofc", true);
        }
        else {
          Assert.fail("unknown imm group: " + g.getName());
        }
      }
      // Get immediate groups
      Assert.assertTrue("imm groups == 1", m.getImmediateGroups().size() == 1);
      Iterator iterIG = m.getImmediateGroups().iterator();
      while (iterIG.hasNext()) {
        Group g = (Group) iterIG.next();
        if (g.equals(uofc)) {
          Assert.assertTrue("imm group: uofc", true);
        }
        else {
          Assert.fail("unknown imm group: " + g.getName());
        }
      }
      // Get non immediate groups
      Assert.assertTrue("nonimm groups == 1", m.getNonImmediateGroups().size() == 1);
      iterIG = m.getNonImmediateGroups().iterator();
      while (iterIG.hasNext()) {
        Group g = (Group) iterIG.next();
        if (g.equals(i2)) {
          Assert.assertTrue("imm group: i2", true);
        }
        else {
          Assert.fail("unknown imm group: " + g.getName());
        }
      }

      // Get effective groups
      Assert.assertTrue("eff groups == 1", m.getEffectiveGroups().size() == 1);
      Iterator iterEG = m.getEffectiveGroups().iterator();
      while (iterEG.hasNext()) {
        Group g = (Group) iterEG.next();
        if (g.equals(i2)) {
          Assert.assertTrue("eff group: i2", true);
        }
        else {
          Assert.fail("unknown eff group: " + g.getName());
        }
      }

      // Is member
      Assert.assertTrue("isMem/i2/!field",    m.isMember(i2));
      Assert.assertTrue("isMem/i2/field",     m.isMember(i2, f));
      Assert.assertTrue("isMem/uofc/!field",  m.isMember(uofc));
      Assert.assertTrue("isMem/uofc/field",   m.isMember(uofc, f));
      // Is immediate member
      Assert.assertTrue("isImm/i2/!field",    !m.isImmediateMember(i2));
      Assert.assertTrue("isImm/i2/field",     !m.isImmediateMember(i2, f));
      Assert.assertTrue("isImm/uofc/!field",  m.isImmediateMember(uofc));
      Assert.assertTrue("isImm/uofc/field",   m.isImmediateMember(uofc, f));
      // Is effective member
      Assert.assertTrue("isEff/i2/!field",    m.isEffectiveMember(i2));
      Assert.assertTrue("isEff/i2/field",     m.isEffectiveMember(i2, f));
      Assert.assertTrue("isEff/uofc/!field",  !m.isEffectiveMember(uofc));
      Assert.assertTrue("isEff/uofc/field",   !m.isEffectiveMember(uofc, f));

      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetMembershipsAndGroups()

  public void testGetAndHasPrivs()
    throws  GrantPrivilegeException,
            GroupAddException,
            InsufficientPrivilegeException,
            MemberNotFoundException,
            SchemaException,
            SessionException,
            StemAddException
  {
    LOG.info("testGetAndHasPrivs");

    Subject         subj  = SubjectTestHelper.SUBJ0;
    Subject         all   = SubjectTestHelper.SUBJA;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    Member          m     = MemberFinder.findBySubject(s, subj, true);
    root.grantPriv( subj, NamingPrivilege.CREATE );
    edu.grantPriv( all, NamingPrivilege.STEM );
    i2.grantPriv( all, AccessPrivilege.OPTIN );
    uofc.grantPriv( subj, AccessPrivilege.UPDATE );

    // Get naming privs
    assertEquals( "getPrivs/root", 1, m.getPrivs(root).size() );
    assertEquals( "getprivs/edu", 1, m.getPrivs(edu).size() ); 

    // Get access privs
    assertEquals( "getprivs/i2", 3, m.getPrivs(i2).size() );
    assertEquals( "getprivs/uofc", 3, m.getPrivs(uofc).size() );

    // Has naming privs
    Assert.assertTrue("hasCreate == 1",   m.hasCreate().size() == 1);
    Assert.assertTrue("hasCreate: root",  m.hasCreate(root));
    Assert.assertTrue("!hasCreate: edu",  !m.hasCreate(edu));

    Assert.assertTrue("hasStem == 1",     m.hasStem().size() == 1);
    Assert.assertTrue("!hasStem: root",   !m.hasStem(root));
    Assert.assertTrue("hasStem: edu",     m.hasStem(edu));

    // Has access privs
    Assert.assertTrue("hasAdmin == 0",    m.hasAdmin().size() == 0);
    Assert.assertTrue("!hasAdmin: i2",    !m.hasAdmin(i2));
    Assert.assertTrue("!hasAdmin: uofc",  !m.hasAdmin(uofc));

    Assert.assertTrue("hasOptin == 1",    m.hasOptin().size() == 1);
    Assert.assertTrue("hasOptin: i2",     m.hasOptin(i2));
    Assert.assertTrue("!hasOptin: uofc",  !m.hasOptin(uofc));

    Assert.assertTrue("hasOptout == 0",   m.hasOptout().size() == 0);
    Assert.assertTrue("!hasOptout: i2",   !m.hasOptout(i2));
    Assert.assertTrue("!hasOptout: uofc", !m.hasOptout(uofc));

    assertTrue("hasRead >= 2", m.hasRead().size() >= 2);
    Assert.assertTrue("hasRead: i2",      m.hasRead(i2));
    Assert.assertTrue("hasRead: uofc",    m.hasRead(uofc));

    Assert.assertTrue("hasUpdate == 1",   m.hasUpdate().size() == 1);
    Assert.assertTrue("!hasUpdate: i2",   !m.hasUpdate(i2));
    Assert.assertTrue("hasUpdate: uofc",  m.hasUpdate(uofc));

    Assert.assertTrue("hasView >= 2",     m.hasView().size() >= 2);
    Assert.assertTrue("hasView: i2",      m.hasView(i2));
    Assert.assertTrue("hasView: uofc",    m.hasView(uofc));

    s.stop();
  }

  public void testFailCanAdminWhenNoPriv() {
    LOG.info("testFailCanAdminWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot admin", m.canAdmin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanAdminWhenNoPriv()

  public void testFailCanAdminWhenNull() {
    LOG.info("testFailCanAdminWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAdmin(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanAdminWhenNull()

  public void testFailCanCreateWhenNoPriv() {
    LOG.info("testFailCanCreateWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 0, 1);
      Stem    a   = r.getStem("a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot create", m.canCreate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanCreateWhenNoPriv()

  /**
   * 
   */
  public void testFailCanCreateWhenNull() {
    LOG.info("testFailCanCreateWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canCreate(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testFailCanOptinWhenNoPriv() {
    LOG.info("testFailCanOptinWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot optin", m.canOptin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptinWhenNoPriv()

  public void testFailCanOptinWhenNull() {
    LOG.info("testFailCanOptinWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canOptin(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptinWhenNull()

  public void testFailCanOptoutWhenNoPriv() {
    LOG.info("testFailCanOptoutWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot optout", m.canOptout(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptoutWhenNoPriv()

  public void testFailCanOptoutWhenNull() {
    LOG.info("testFailCanOptoutWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canOptout(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptoutWhenNull()

  public void testFailCanReadWhenNoPriv() {
    LOG.info("testFailCanReadWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      a.revokePriv(AccessPrivilege.READ); // Revoke READ from all subjects
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot read", m.canRead(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadWhenNoPriv()

  public void testFailCanReadWhenNull() {
    LOG.info("testFailCanReadWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAttrRead(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadWhenNull()

  public void testFailCanStemWhenNoPriv() {
    LOG.info("testFailCanStemWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 0, 1);
      Stem    a   = r.getStem("a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot stem", m.canStem(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanStemWhenNoPriv()

  public void testFailCanStemWhenNull() {
    LOG.info("testFailCanStemWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canStem(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanStemWhenNull()

  public void testFailCanUpdateWhenNoPriv() {
    LOG.info("testFailCanUpdateWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot update", m.canUpdate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanUpdateWhenNoPriv()

  public void testFailCanUpdateWhenNull() {
    LOG.info("testFailCanUpdateWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAttrUpdate(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanUpdateWhenNull()

  public void testFailCanViewWhenNoPriv() {
    LOG.info("testFailCanViewWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      a.revokePriv(AccessPrivilege.VIEW); // Revoke VIEW from all subjects
      a.revokePriv(AccessPrivilege.READ); // Revoke READ from all subjects
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot view", m.canView(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanViewWhenNoPriv()

  public void testFailCanViewWhenNull() {
    LOG.info("testFailCanViewWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAttrView(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanViewWhenNull()

  /**
   * 
   * @throws Exception
   */
  public void testFindBySubjectId() throws Exception {
    GrouperDAOFactory.getFactory().getMember().findBySubject("GrouperSystem", true);
    GrouperDAOFactory.getFactory().getMember().findBySubject("GrouperSystem", "g:isa", true);
    try {
      GrouperDAOFactory.getFactory().getMember().findBySubject("sflkjlksjflksjdlksd", true);
      fail("Shouldnt find this");
    } catch (MemberNotFoundException snfe) {
      //good
    }
  }

  public void testPassCanAdminWhenRoot() {
    LOG.info("testPassCanAdminWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can admin", m.canAdmin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanAdminWhenRoot()

  public void testPassCanCreateWhenRoot() {
    LOG.info("testPassCanCreateWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 0, 0);
      Stem    a   = r.getStem("a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can create", m.canCreate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanCreateWhenRoot()

  public void testPassCanOptinWhenRoot() {
    LOG.info("testPassCanOptinWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can g", m.canOptin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanOptinWhenRoot()

  public void testPassCanOptoutWhenRoot() {
    LOG.info("testPassCanOptoutWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can optout", m.canOptout(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanOptoutWhenRoot()

  public void testPassCanReadWhenRoot() {
    LOG.info("testPassCanReadWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can read", m.canRead(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanReadWhenRoot()

  public void testPassCanStemWhenRoot() {
    LOG.info("testPassCanStemWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 0, 0);
      Stem    a   = r.getStem("a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can stem", m.canStem(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanStemWhenRoot()

  public void testPassCanUpdateWhenRoot() {
    LOG.info("testPassCanUpdateWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can update", m.canUpdate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanUpdateWhenRoot()

  public void testPassCanViewWhenRoot() {
    LOG.info("testPassCanViewWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can view", m.canView(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanViewWhenRoot()

  public void testSetAllSubjectId() {
    LOG.info("testSetAllSubjectId");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findAllSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId on GrouperAll");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetAllSubjectId()

  public void testSetRootSubjectId() {
    LOG.info("testSetRootSubjectId");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findRootSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId on GrouperSystem");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetRootSubjectId()

  public void testSetSubjectIdNotRoot() {
    LOG.info("testSetSubjectIdNotRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 2);
      Subject   subjA = r.getSubject("a");
      Subject   subjB = r.getSubject("b");
      r.rs.stop();
  
      GrouperSession  nrs   = GrouperSession.start(subjA);
      Member          m     = MemberFinder.findBySubject(nrs, subjB, true);
      String          orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId when not root-like");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        nrs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectIdNotRoot

  public void testSetSubjectIdRoot() {
    LOG.info("testSetSubjectIdRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 1);
      Subject   subjA = r.getSubject("a");
      Member    m     = MemberFinder.findBySubject(r.rs, subjA, true);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        assertTrue(true);
        T.string("subjectId", orig.toUpperCase(), m.getSubjectId());
      }
      catch (InsufficientPrivilegeException eIP) {
        fail("did not change subjectId: " + eIP.getMessage());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectIdRoot

  public void testSetSubjectSourceIdFailAsNonRoot() {
    LOG.info("testSetSubjectSourceIdFailAsNonRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 2);
      Subject   subjA = r.getSubject("a");
      Subject   subjB = r.getSubject("b");
      r.rs.stop();
  
      GrouperSession  nrs   = GrouperSession.start(subjA);
      Member          m     = MemberFinder.findBySubject(nrs, subjB, true);
      String          orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id when not root-like");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        nrs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailAsNonRoot

  public void testSetSubjectSourceIdFailNullValue() {
    LOG.info("testSetSubjectSourceIdFailNullValue");
    try {
      R       r     = R.populateRegistry(0, 0, 1);
      Subject subjA = r.getSubject("a");
      Member  m     = MemberFinder.findBySubject(r.rs, subjA, true);
      try {
        m.setSubjectSourceId(null);
        m.store();
        fail("unexpectedly changed subject source id when value null");
      }
      catch (IllegalArgumentException eIA) {
        assertTrue(true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailNullValue

  public void testSetSubjectSourceIdFailOnGrouperAll() {
    LOG.info("testSetSubjectSourceIdFailOnGrouperAll");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findAllSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id on GrouperAll");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailOnGrouperAll()

  public void testSetSubjectSourceIdFailOnGrouperSystem() {
    LOG.info("testSetSubjectSourceIdFailOnGrouperSystem");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findRootSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id on GrouperSystem");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailOnGrouperSystem()

  public void testSetSubjectSourceIdOk() {
    LOG.info("testSetSubjectSourceIdOk");
    try {
      R         r     = R.populateRegistry(0, 0, 1);
      Subject   subjA = r.getSubject("a");
      Member    m     = MemberFinder.findBySubject(r.rs, subjA, true);
      String    orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        assertTrue(true);
        T.string("subject source id", orig.toUpperCase(), m.getSubjectSourceId());
      }
      catch (InsufficientPrivilegeException eIP) {
        fail("did not change subject source id: " + eIP.getMessage());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdOk() 

  
  /**
   * make an example member for testing
   * @return an example member
   */
  public static Member exampleMember() {
    
    Member member = new Member();
    member.setContextId("contextId");
    member.setHibernateVersionNumber(3L);
    member.setSubjectId("subjectId");
    member.setSubjectSourceId("subjectSourceId");
    member.setSubjectTypeId("subjectTypeId");
    member.setUuid("uuid");
    return member;
  }
  
  /**
   * make an example mmeber for testing
   * @return an example member
   */
  public static Member exampleMemberDb() {
    
    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject("subjectTestId", "sourceTestId", false);
    
    if (member == null) {
      member = new Member();
      member.setSubjectIdDb("subjectTestId");
      member.setSubjectSourceIdDb("sourceTestId");
      member.setSubjectTypeId("testTypeId");
      member.setUuid(GrouperUuid.getUuid());
      GrouperDAOFactory.getFactory().getMember().create(member);
      
    }
    return member;
  }

  
  /**
   * make an example stem for testing
   * @return an example stem
   */
  public static Member exampleRetrieveMemberDb() {
    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject("subjectTestId", "sourceTestId", false);
    return member;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Member memberOriginal = new Member();
    memberOriginal.setSubjectIdDb("subjectInsertId");
    memberOriginal.setSubjectSourceIdDb("sourceInsertId");
    memberOriginal.setSubjectTypeId("insertTypeId");
    memberOriginal.setUuid(GrouperUuid.getUuid());
    GrouperDAOFactory.getFactory().getMember().create(memberOriginal);
    
    Member memberCopy =   GrouperDAOFactory.getFactory().getMember().findBySubject("subjectInsertId", "sourceInsertId", false);
    Member memberCopy2 =  GrouperDAOFactory.getFactory().getMember().findBySubject("subjectInsertId", "sourceInsertId", false);
    HibernateSession.byObjectStatic().delete(memberCopy);
    
    //lets insert the original
    memberCopy2.xmlSaveBusinessProperties(null);
    memberCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    memberCopy = GrouperDAOFactory.getFactory().getMember().findBySubject("subjectInsertId", "sourceInsertId", false);
    
    assertFalse(memberCopy == memberOriginal);
    assertFalse(memberCopy.xmlDifferentBusinessProperties(memberOriginal));
    assertFalse(memberCopy.xmlDifferentUpdateProperties(memberOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Member member = null;
    Member exampleMember = null;

    //lets do an insert
    
    //TEST UPDATE PROPERTIES
    {
      member = exampleMemberDb();
      exampleMember = member.clone();
      
      member.setContextId("abc");
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertTrue(member.xmlDifferentUpdateProperties(exampleMember));

      member.setContextId(exampleMember.getContextId());
      member.xmlSaveUpdateProperties();

      member = exampleRetrieveMemberDb();
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));
      
    }
    
    {
      member = exampleMemberDb();
      exampleMember = member.clone();

      member.setHibernateVersionNumber(99L);
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertTrue(member.xmlDifferentUpdateProperties(exampleMember));

      member.setHibernateVersionNumber(exampleMember.getHibernateVersionNumber());
      member.xmlSaveUpdateProperties();
      
      member = exampleRetrieveMemberDb();
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      member = exampleMemberDb();
      exampleMember = member.clone();

      member.setSubjectId("abc");
      
      assertTrue(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));

      member.setSubjectId(exampleMember.getSubjectId());
      member.xmlSaveBusinessProperties(exampleMember.clone());
      member.xmlSaveUpdateProperties();
      
      member = exampleRetrieveMemberDb();
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));
    
    }
    
    {
      member = exampleMemberDb();
      exampleMember = member.clone();

      member.setSubjectSourceIdDb("abc");
      
      assertTrue(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));

      member.setSubjectSourceIdDb(exampleMember.getSubjectSourceIdDb());
      member.xmlSaveBusinessProperties(exampleMember.clone());
      member.xmlSaveUpdateProperties();
      
      member = exampleRetrieveMemberDb();
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));
    
    }
    
    {
      member = exampleMemberDb();
      exampleMember = member.clone();

      member.setSubjectTypeId("abc");
      
      assertTrue(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));

      member.setSubjectTypeId(exampleMember.getSubjectTypeId());
      member.xmlSaveBusinessProperties(exampleMember.clone());
      member.xmlSaveUpdateProperties();
      
      member = exampleRetrieveMemberDb();
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));
    
    }
    
    {
      member = exampleMemberDb();
      exampleMember = member.clone();

      member.setUuid("abc");
      
      assertTrue(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));

      member.setUuid(exampleMember.getUuid());
      member.xmlSaveBusinessProperties(exampleMember.clone());
      member.xmlSaveUpdateProperties();
      
      member = exampleRetrieveMemberDb();
      
      assertFalse(member.xmlDifferentBusinessProperties(exampleMember));
      assertFalse(member.xmlDifferentUpdateProperties(exampleMember));
    
    }
  }

}

