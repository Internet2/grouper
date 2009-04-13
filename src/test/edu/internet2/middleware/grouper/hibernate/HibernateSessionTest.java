/*
 * @author mchyzer
 * $Id: HibernateSessionTest.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession; 
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class HibernateSessionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new HibernateSessionTest("testResultSize"));
  }
  

  /**
   * @param name
   */
  public HibernateSessionTest(String name) {
    super(name);
  }

  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testResultSize() throws Exception {
    
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group i3 = StemHelper.addChildGroup(edu, "i3", "internet2");
    Group i4 = StemHelper.addChildGroup(edu, "i4", "internet2");
    Group i5 = StemHelper.addChildGroup(edu, "i5", "internet2");
    Group i6 = StemHelper.addChildGroup(edu, "i6", "internet2");
    Group i7 = StemHelper.addChildGroup(edu, "i7", "internet2");

    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ1);
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectFinder.findAllSubject());
    i2.addMember(SubjectFinder.findRootSubject());
    
    //page the members in a group
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    List<Member> members = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember from Member as theMember, Membership as theMembership, Field theField "
      + "where theMembership.ownerUuid = :ownerId and theMember.uuid = theMembership.memberUuid" +
          " and theMembership.fieldId = theField.uuid and theField.typeString = 'list' and theField.name = 'members'")
          .setString("ownerId", i2.getUuid())
      .options(queryOptions).list(Member.class);
    
    assertEquals(0, members.size());
    assertEquals(5L, queryOptions.getCount().longValue());
    
    members = HibernateSession.byCriteriaStatic().options(queryOptions).list(Member.class, null);
    
    assertEquals(0, members.size());
    assertTrue(5 < queryOptions.getCount().longValue());
    
    Set<Member> memberSet = i2.getMembers(Group.getDefaultList(), queryOptions);
    
    assertEquals(0, memberSet.size());
    assertEquals(5L, queryOptions.getCount().longValue());
    
    
  }
  
  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testPagingSorting() throws Exception {
    
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group i3 = StemHelper.addChildGroup(edu, "i3", "internet2");
    Group i4 = StemHelper.addChildGroup(edu, "i4", "internet2");
    Group i5 = StemHelper.addChildGroup(edu, "i5", "internet2");
    Group i6 = StemHelper.addChildGroup(edu, "i6", "internet2");
    Group i7 = StemHelper.addChildGroup(edu, "i7", "internet2");

    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ1);
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectFinder.findAllSubject());
    i2.addMember(SubjectFinder.findRootSubject());

    i3.addMember(SubjectTestHelper.SUBJ1);
    i3.addMember(SubjectTestHelper.SUBJ4);

    QueryPaging queryPaging = QueryPaging.page(3, 1, true);
    
    //page the members in a group
    List<Member> members = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember from Member as theMember, Membership as theMembership, Field theField "
      + "where theMembership.ownerUuid = :ownerId and theMember.uuid = theMembership.memberUuid" +
      		" and theMembership.fieldId = theField.uuid and theField.typeString = 'list' and theField.name = 'members'")
      		.setString("ownerId", i2.getUuid())
      .options(new QueryOptions().sortAsc("theMember.subjectIdDb").paging(queryPaging)).list(Member.class);
    
    assertEquals("GrouperAll, GrouperSystem, test.subject.0", Member.subjectIds(members));
    
    assertEquals(5, queryPaging.getTotalRecordCount());
    assertEquals(2, queryPaging.getNumberOfPages());
    
    //intersection in one query
    List<String> memberUuids = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember.uuid from Member theMember, " +
      		"Membership theMembership, Membership theMembership2, Field theField " +
      		"where theMembership.ownerUuid = :group1uuid and theMembership2.ownerUuid = :group2uuid " +
      		"and theMember.uuid = theMembership.memberUuid and theMember.uuid = theMembership2.memberUuid " +
      		"and theMembership.fieldId = theField.uuid and theMembership2.fieldId = theField.uuid " +
      		"and theField.typeString = 'list' and theField.name = 'members'")
      		.setString("group1uuid", i2.getUuid())
      		.setString("group2uuid", i3.getUuid())
          .list(String.class);

    assertEquals(1, memberUuids.size());
    assertEquals(MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1).getUuid(), memberUuids.get(0));

    //complement in one query
    memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid from Member theMember, " +
        "Membership theMembership, Field theField " +
        "where theMembership.ownerUuid = :group1uuid " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMembership.fieldId = theField.uuid " +
        "and theField.typeString = 'list' and theField.name = 'members' " +
        "and not exists (select theMembership2.memberUuid from Membership theMembership2 " +
        "where theMembership2.memberUuid = theMember.uuid and theMembership.fieldId = theField.uuid " +
        "and theMembership2.ownerUuid = :group2uuid) ")
        .setString("group1uuid", i3.getUuid())
        .setString("group2uuid", i2.getUuid())
        .list(String.class);
  
    assertEquals(1, memberUuids.size());
    assertEquals(MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4).getUuid(), memberUuids.get(0));
    
    //union in one query
    memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid from Member theMember, " +
        "Membership theMembership, Membership theMembership2, Field theField " +
        "where theMembership.ownerUuid = :group1uuid and theMembership2.ownerUuid = :group2uuid " +
        "and (theMember.uuid = theMembership.memberUuid or theMember.uuid = theMembership2.memberUuid) " +
        "and theMembership.fieldId = theField.uuid and theMembership2.fieldId = theField.uuid " +
        "and theField.typeString = 'list' and theField.name = 'members'")
        .setString("group1uuid", i2.getUuid())
        .setString("group2uuid", i3.getUuid())
        .list(String.class);
  
    assertEquals(6, memberUuids.size());
    
    i2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    i3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    i4.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i5.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i6.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i7.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    List<String> uuids = GrouperUtil.toList(i2.getUuid(), i4.getUuid(), i5.getUuid(), i6.getUuid(), i7.getUuid());
    
    Collections.sort(uuids);
    
    queryPaging = QueryPaging.page(3, 1, true);
    
    List<Group> groups = HibernateSession.byHqlStatic()
    .createQuery("select distinct g from Group as g, Membership as m, Field as f, Attribute as a " +
    		"where a.groupUuid = g.uuid and g.parentUuid = :parent " +
    		"and m.ownerUuid = g.uuid and m.fieldId = f.uuid and f.typeString = 'access' " +
    		"and (m.memberUuid = :sessionMemberId or m.memberUuid = :grouperAllUuid)")
    		.setString("parent", edu.getUuid())
        .setString("sessionMemberId", MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1).getUuid())
        .setString("grouperAllUuid", MemberFinder.findBySubject(grouperSession, SubjectFinder.findAllSubject()).getUuid())
        .options(new QueryOptions().paging(queryPaging).sortAsc("g.uuid"))
        .list(Group.class);
    
    assertEquals(3, groups.size());
    assertEquals(5, queryPaging.getTotalRecordCount());
    assertEquals(uuids.get(0), groups.get(0).getUuid());
    assertEquals(uuids.get(1), groups.get(1).getUuid());
    assertEquals(uuids.get(2), groups.get(2).getUuid());
    
    
    
  }

}
