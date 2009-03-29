/*
 * @author mchyzer
 * $Id: HibernateSessionTest.java,v 1.1.2.2 2009-03-29 03:56:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;

/**
 *
 */
public class HibernateSessionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new HibernateSessionTest("testPagingSorting"));
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
  public void testPagingSorting() throws Exception {
    
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");

    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ1);
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectFinder.findAllSubject());
    i2.addMember(SubjectFinder.findRootSubject());

    QueryPaging queryPaging = QueryPaging.page(3, 1, true);
    
    List<Member> members = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember from Member as theMember, Membership as theMembership "
      + "where theMembership.ownerUuid = :ownerId and theMember.uuid = theMembership.memberUuid").setString("ownerId", i2.getUuid())
      .sort(QuerySort.asc("theMember.subjectIdDb")).paging(queryPaging).list(Member.class);
    
    assertEquals("GrouperAll, GrouperSystem, test.subject.0", Member.subjectIds(members));
    
    assertEquals(5, queryPaging.getTotalRecordCount());
    assertEquals(2, queryPaging.getNumberOfPages());
    
//    List<Group> groups = HibernateSession.byHqlStatic()
//      .createQuery("select g from Attribute as a, Group as g, Field as field where a.groupUuid = g.uuid " +
//          "and field.name = 'name' and a.value = :value and field.typeString = 'attribute' and a.fieldId = field.uuid")
          
    
    
  }

}
