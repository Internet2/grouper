/*
 * @author mchyzer
 * $Id: UserAuditQueryTest.java,v 1.5 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class UserAuditQueryTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UserAuditQueryTest("testQueries"));
  }
  
  /**
   * @param name
   */
  public UserAuditQueryTest(String name) {
    super(name);
  }

  /**
   * test queries
   */
  public void testQueries() {
    
    String result = new UserAuditQuery().executeReport();
    System.out.println(result);
    System.out.println("\n\n\n(extended)\n\n");
    result = new UserAuditQuery().executeReportExtended();
    System.out.println(result);
    
    System.out.println("\n\n\n(query for test subject)\n\n");
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    Subject subject = SubjectFinder.findByIdOrIdentifier(SubjectTestHelper.SUBJ0_ID, true);
    Member member = MemberFinder.findBySubject(grouperSession,subject, true);
    
    result = new UserAuditQuery().loggedInMember(member).executeReport();
    System.out.println(result);
    
  }

  /**
   * 
   */
  public void testMembershipQuery() {
    String result = new UserAuditQuery().addAuditTypeCategory("membership").executeReport();
    System.out.println(result);

  }
  
  /**
   * 
   */
  public void testMembershipMemberQuery() {
    String result = new UserAuditQuery().addAuditTypeCategory("membership")
      .addAuditTypeFieldValue("memberId", "123").executeReport();
    System.out.println(result);

  }


  /**
   * 
   */
  public void testStemQuery() {
    String result = new UserAuditQuery().addAuditTypeCategory("stem")
      .addAuditTypeFieldValue("stemId", "123").executeReport();
    System.out.println(result);

  }

}
