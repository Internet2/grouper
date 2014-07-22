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
 * @author mchyzer
 * $Id: HibUtilsTest.java,v 1.1 2009-12-28 06:08:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class HibUtilsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(HibUtilsTest.class);
    TestRunner.run(new HibUtilsTest("testFindBySubjectsInGroup"));
  }
  
  /**
   * @param name
   */
  public HibUtilsTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testConvertToSubjectInClause() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignGroupNameToEdit("stem:group").assignName("stem:group")
      .assignCreateParentStemsIfNotExist(true).save();
  
    Subject subjectChecker = SubjectTestHelper.SUBJ0;
    
    Subject subjectMember1 = SubjectTestHelper.SUBJ1;
    Subject subjectMember2 = SubjectTestHelper.SUBJ2;
    
    group.grantPriv(subjectChecker, AccessPrivilege.READ);
    
    String hqlStart = "select gm from Member gm where ";
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    Set<Subject> subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ3, subjectMember1, subjectMember2); 
    
    String hql = hqlStart + HibUtils.convertToSubjectInClause(subjects, byHqlStatic, "gm") + " order by gm.subjectIdDb ";
    byHqlStatic.createQuery(hql);
    List<Member> members = byHqlStatic.list(Member.class);
    
    assertEquals(0, members.size());
    
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ2);
    
    members = byHqlStatic.list(Member.class);
    
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, members.get(0).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, members.get(1).getSubjectId());
    
    
    subjects = GrouperUtil.toSet(subjectMember1); 
    
    byHqlStatic = HibernateSession.byHqlStatic();

    hql = hqlStart + HibUtils.convertToSubjectInClause(subjects, byHqlStatic, "gm") + " order by gm.subjectIdDb ";
    
    byHqlStatic.createQuery(hql);
    members = byHqlStatic.list(Member.class);
    
    assertEquals(1, members.size());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, members.get(0).getSubjectId());
    
    RegistrySubject.add( grouperSession, "GrouperSystem", "person", "Grouper system" );
    
    Subject jdbcGrouperSystem = SubjectFinder.findById("GrouperSystem", "person", "jdbc", true);
    
    
    //try with the same subject id as another source
    group.addMember(jdbcGrouperSystem);
    group.addMember(SubjectFinder.findRootSubject());

    byHqlStatic = HibernateSession.byHqlStatic();
    
    subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ3, subjectMember1, subjectMember2, jdbcGrouperSystem); 
    
    hql = hqlStart + HibUtils.convertToSubjectInClause(subjects, byHqlStatic, "gm") + " order by gm.subjectIdDb ";
    byHqlStatic.createQuery(hql);
    members = byHqlStatic.list(Member.class);
    
    assertEquals(3, members.size());
    
    assertEquals("GrouperSystem", members.get(0).getSubjectId());
    assertEquals("jdbc", members.get(0).getSubjectSourceId());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, members.get(1).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, members.get(2).getSubjectId());

  }

  
  /**
   * 
   */
  public void testFindBySubjectsInGroup() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignGroupNameToEdit("stem:group").assignName("stem:group")
      .assignCreateParentStemsIfNotExist(true).save();
  
    Subject subjectChecker = SubjectTestHelper.SUBJ0;
    
    Subject subjectMember1 = SubjectTestHelper.SUBJ1;
    Subject subjectMember2 = SubjectTestHelper.SUBJ2;
    
    group.grantPriv(subjectChecker, AccessPrivilege.READ);
    
    Set<Subject> subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ3, subjectMember1, subjectMember2); 

    Set<Member> members = MemberFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);

    assertEquals(0, GrouperUtil.length(members));
    
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ2);
    
    members = MemberFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);
    
    
    Member member = (Member)GrouperUtil.get(members, 0);
    assertEquals(SubjectTestHelper.SUBJ1_ID, member.getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, ((Member)GrouperUtil.get(members, 1)).getSubjectId());
    
    subjects = GrouperUtil.toSet(subjectMember1); 
    
    members = MemberFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);
    
    assertEquals(1, members.size());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, member.getSubjectId());
    
    RegistrySubject.add( grouperSession, "GrouperSystem", "person", "Grouper system" );
    
    Subject jdbcGrouperSystem = SubjectFinder.findById("GrouperSystem", "person", "jdbc", true);
    
    //try with the same subject id as another source
    group.addMember(jdbcGrouperSystem);
    group.addMember(SubjectFinder.findRootSubject());

    subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ3, subjectMember1, subjectMember2, jdbcGrouperSystem); 
    
    members = MemberFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);
    
    assertEquals(3, members.size());
    
    member = (Member)GrouperUtil.get(members, 0);
    assertEquals("GrouperSystem", member.getSubjectId());
    assertEquals("jdbc", member.getSubjectSourceId());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, ((Member)GrouperUtil.get(members, 1)).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, ((Member)GrouperUtil.get(members, 2)).getSubjectId());

    Hib3MemberDAO.MEMBER_SUBJECT_BATCH_SIZE = 1;
    
    //should get same results
    subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ3, subjectMember1, subjectMember2, jdbcGrouperSystem); 
    
    members = MemberFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);
    
    assertEquals(3, members.size());
    
    member = (Member)GrouperUtil.get(members, 0);
    assertEquals("GrouperSystem", member.getSubjectId());
    assertEquals("jdbc", member.getSubjectSourceId());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, ((Member)GrouperUtil.get(members, 1)).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, ((Member)GrouperUtil.get(members, 2)).getSubjectId());

    Set<Subject> subjectResults = SubjectFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);
    
    assertEquals(3, subjectResults.size());
    
    Subject subject = (Subject)GrouperUtil.get(subjectResults, 0);
    assertEquals("GrouperSystem", subject.getId());
    assertEquals("jdbc", subject.getSourceId());
    
    assertEquals(SubjectTestHelper.SUBJ1_ID, ((Subject)GrouperUtil.get(subjectResults, 1)).getId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, ((Subject)GrouperUtil.get(subjectResults, 2)).getId());

  }

}
