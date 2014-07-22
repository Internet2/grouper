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
    result = new UserAuditQuery().executeReportExtended();
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    Subject subject = SubjectFinder.findByIdOrIdentifier(SubjectTestHelper.SUBJ0_ID, true);
    Member member = MemberFinder.findBySubject(grouperSession,subject, true);
    
    result = new UserAuditQuery().loggedInMember(member).executeReport();
    
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testMembershipQuery() {
    String result = new UserAuditQuery().addAuditTypeCategory("membership").executeReport();

  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testMembershipMemberQuery() {
    String result = new UserAuditQuery().addAuditTypeCategory("membership")
      .addAuditTypeFieldValue("memberId", "123").executeReport();

  }


  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testStemQuery() {
    String result = new UserAuditQuery().addAuditTypeCategory("stem")
      .addAuditTypeFieldValue("stemId", "123").executeReport();

  }

}
