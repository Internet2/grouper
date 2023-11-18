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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.TestStem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class UserAuditQueryTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new UserAuditQueryTest("testPagingCursor"));
//    QueryOptions queryOptions = new QueryOptions().paging(5, 1, true);
//    List<AuditEntry> auditEntries = new UserAuditQuery().addAuditTypeCategory("group").setQueryOptions(queryOptions).execute();
//    for (AuditEntry auditEntry : auditEntries) {
//      System.out.println(auditEntry);
//    }
  }
  
  /**
   * @param name
   */
  public UserAuditQueryTest(String name) {
    super(name);
  }
  
  protected void setUp () {
    super.setUp();

  }

  protected void tearDown () {
    super.tearDown();
  }

  /**
   * test paging v2
   */
  public void testPagingCursor() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    TestStem.loadLotsOfData(true, false, false, false, 2, 2, 2, 1, 2, 1, 2, 3, 2, 1);
    
    // see how many records
    List<AuditEntry> auditEntriesAll = new UserAuditQuery().addAuditTypeCategory("group").setQueryOptions(QueryOptions.create(null, null, 1, 10000)).execute();
    
    Set<String> auditEntriesAllIds = new HashSet<String>();
    for (AuditEntry auditEntry : auditEntriesAll) {
      auditEntriesAllIds.add(auditEntry.getId());
    }

    List<AuditEntry> auditEntriesPaged = new ArrayList<AuditEntry>();

    String lastId = null;
    while(true) {
      QueryOptions queryOptions = new QueryOptions().sortAsc("id").pagingCursor(7, lastId, false, false);
      List<AuditEntry> auditEntries = new UserAuditQuery().addAuditTypeCategory("group").setQueryOptions(queryOptions).execute();
      auditEntriesPaged.addAll(auditEntries);
      if (auditEntries.size() < 7) {
        break;
      }
      // get the last one and get records after that one
      lastId = auditEntries.get(auditEntries.size()-1).getId();
    }

    Set<String> auditEntriesPagedIds = new HashSet<String>();
    for (AuditEntry auditEntry : auditEntriesPaged) {
      auditEntriesPagedIds.add(auditEntry.getId());
    }
    
    assertEquals(auditEntriesAllIds.size(), auditEntriesPagedIds.size());

    auditEntriesAllIds.removeAll(auditEntriesPagedIds);
    
    assertEquals(0, auditEntriesAllIds.size());
    
    // try finding groups
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
    Set<Group> groupsAll = new GroupFinder().assignParentStemId(testStem.getId()).assignStemScope(Scope.SUB).findGroups();
    Set<Group> groupsPaged = new HashSet<Group>();

    lastId = null;
    while(true) {
      
      QueryOptions queryOptions = new QueryOptions().sortAsc("id").pagingCursor(7, lastId, false, false);
      
      List<Group> groups = new ArrayList<Group>(
          new GroupFinder().assignParentStemId(testStem.getId()).assignStemScope(Scope.SUB).assignQueryOptions(queryOptions).findGroups());
      
      groupsPaged.addAll(groups);
      if (groups.size() < 7) {
        break;
      }
      // get the last one and get records after that one
      lastId = groups.get(groups.size()-1).getId();
    }
    assertEquals(groupsAll.size(), groupsPaged.size());
    groupsAll.removeAll(groupsPaged);
    assertEquals(0, groupsAll.size());
    
    GrouperSession.stopQuietly(grouperSession);
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
