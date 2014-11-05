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

/**
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
package edu.internet2.middleware.grouperVoot;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to test the parameter passing to main service logic for the VOOT connector for Grouper.
 */
public class VootParamsLogicTest extends VootTest {

  /**
   * Main method to execute all tests.
   * @param args parameters passed to main (ignored).
   */
  public static void main(String[] args) {
    TestRunner.run(new VootParamsLogicTest("testPaginateGroupsFirstPage"));
    TestRunner.run(new VootParamsLogicTest("testPaginateGroupsSecondPage"));
    TestRunner.run(new VootParamsLogicTest("testPaginateGroupsLastPage"));
    TestRunner.run(new VootParamsLogicTest("testPaginatePeopleFirstPage"));
    TestRunner.run(new VootParamsLogicTest("testPaginatePeopleSecondPage"));
    TestRunner.run(new VootParamsLogicTest("testPaginatePeopleLastPage"));
    TestRunner.run(new VootParamsLogicTest("testSortGroupsById"));
    TestRunner.run(new VootParamsLogicTest("testSortGroupsByName"));
    TestRunner.run(new VootParamsLogicTest("testSortGroupsByDescription"));
    TestRunner.run(new VootParamsLogicTest("testSortGroupsByVootMembershipRole"));
    TestRunner.run(new VootParamsLogicTest("testSortPeopleById"));
    TestRunner.run(new VootParamsLogicTest("testSortPeopleByDisplayName"));
    TestRunner.run(new VootParamsLogicTest("testSortPeopleByVootMembershipRole"));
    TestRunner.run(new VootParamsLogicTest("testSearchGroupsWithResults"));
    TestRunner.run(new VootParamsLogicTest("testSearchGroupsWithOutResults"));
    TestRunner.run(new VootParamsLogicTest("testSearchAllGroups"));
  }
  
  /**
   * Default constructor to initialize VOOT test cases. 
   */
  public VootParamsLogicTest() {
    super();
  }

  /**
   * Constructor with a test name as parameter.
   * @param name the name of this test execution on the test runner.
   */
  public VootParamsLogicTest(String name) {
    super(name);
  }
  
  /**
   * Method to create a registry with all the required users and groups
   * to test all VOOT calls with this test suite.
   * 
   * These tests have to prove pagination and sorting, so a significant number
   * of groups were required.
   * We create then 12 groups where:
   * <ul>
   * <li><b>Group 0</b>: is a group containing 10 subjects (to test pagination on group members)</li>
   * <li><b>Group 1</b> to <b>Group 11</b>: are groups where Subject0 is member</li>
   * </ul>
   */
  @Override
  protected void createRegistryToTestVOOT() {
    // Setup data as root user
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // Create all groups for managing the different membership relations
    Group[] groups = new Group[12];
    for (int i = 0 ; i < groups.length; ++i) {
      groups[i] = new GroupSave(grouperSession).assignName(GROUP_NAMES[i])
          .assignDescription(GROUP_DESCRIPTIONS[i]).assignCreateParentStemsIfNotExist(true).save();
    }
    
    // Setting memberships
    for (int i = 0 ; i < groups.length; ++i) {
      groups[i].addMember(SubjectTestHelper.SUBJ0, false);
      groups[i].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    }
    
    groups[0].addMember(SubjectTestHelper.SUBJ1, false);
    groups[0].addMember(SubjectTestHelper.SUBJ2, false);
    groups[0].addMember(SubjectTestHelper.SUBJ3, false);
    groups[0].addMember(SubjectTestHelper.SUBJ4, false);
    groups[0].addMember(SubjectTestHelper.SUBJ5, false);
    groups[0].addMember(SubjectTestHelper.SUBJ6, false);
    groups[0].addMember(SubjectTestHelper.SUBJ7, false);
    groups[0].addMember(SubjectTestHelper.SUBJ8, false);
    groups[0].addMember(SubjectTestHelper.SUBJ9, false);
    groups[3].addMember(SubjectTestHelper.SUBJ1, false);
    
    // Setting administrator rights
    groups[0].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    groups[0].grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN, false);
    groups[0].grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN, false);
    groups[0].grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN, false);
    groups[1].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    
    // Setting update rights
    groups[0].grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE, false);
    groups[0].grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.UPDATE, false);
    groups[2].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE, false);
    
    // Stop root session
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?startIndex=0&count=5
   * Note: running this will delete all data in the registry!
   */
  public void testPaginateGroupsFirstPage() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = null;
    int start = 0;
    int count = 5;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);

    //analyze the result
    int groupCount = 12;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, count, groups, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?startIndex=5&count=5
   * Note: running this will delete all data in the registry!
   */
  public void testPaginateGroupsSecondPage() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = null;
    int start = 5;
    int count = 5;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);
  
    //analyze the result
    int groupCount = 12;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, count, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?startIndex=10&count=5
   * Note: running this will delete all data in the registry!
   */
  public void testPaginateGroupsLastPage() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = null;
    int start = 10;
    int count = 5;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);

    //analyze the result
    int groupCount = 12;
    int countGroups = 2;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, countGroups, groups, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?startIndex=0&count=4
   * Note: running this will delete all data in the registry!
   */
  public void testPaginatePeopleFirstPage() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = null;
    int start = 0;
    int count = 4;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 10;
    Subject[] subjects = new Subject[]{
      SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
      SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6, SubjectTestHelper.SUBJ7, SubjectTestHelper.SUBJ8, SubjectTestHelper.SUBJ9
    };
    String[] roles = new String[]{
      "member", "admin", "admin", "admin", "admin",
      "manager", "manager", "member", "member", "member"
    };
    validateMembers(resultObject, memberCount, sortBy, start, count, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?startIndex=4&count=4
   * Note: running this will delete all data in the registry!
   */
  public void testPaginatePeopleSecondPage() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = null;
    int start = 4;
    int count = 4;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 10;
    Subject[] subjects = new Subject[]{
      SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
      SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6, SubjectTestHelper.SUBJ7, SubjectTestHelper.SUBJ8, SubjectTestHelper.SUBJ9
    };
    String[] roles = new String[]{
      "member", "admin", "admin", "admin", "admin",
      "manager", "manager", "member", "member", "member"
    };
    validateMembers(resultObject, memberCount, sortBy, start, count, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?startIndex=8&count=4
   * Note: running this will delete all data in the registry!
   */
  public void testPaginatePeopleLastPage() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = null;
    int start = 8;
    int count = 4;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 10;
    int countMembers = 2;
    Subject[] subjects = new Subject[]{
      SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
      SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6, SubjectTestHelper.SUBJ7, SubjectTestHelper.SUBJ8, SubjectTestHelper.SUBJ9
    };
    String[] roles = new String[]{
      "member", "admin", "admin", "admin", "admin",
      "manager", "manager", "member", "member", "member"
    };
    validateMembers(resultObject, memberCount, sortBy, start, countMembers, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?orderBy=id
   * Note: running this will delete all data in the registry!
   */
  public void testSortGroupsById() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "id";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);
  
    //analyze the result
    int groupCount = 12;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?orderBy=name
   * Note: running this will delete all data in the registry!
   */
  public void testSortGroupsByName() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "name";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);
  
    //analyze the result
    int groupCount = 12;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?orderBy=description
   * Note: running this will delete all data in the registry!
   */
  public void testSortGroupsByDescription() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "description";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);
  
    //analyze the result
    int groupCount = 12;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject1 and calls the URL:
   * /groups/@me?orderBy=voot_membershib_role
   * Note: running this will delete all data in the registry!
   */
  public void testSortGroupsByVootMembershipRole() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "voot_membership_role";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);
  
    //analyze the result
    int groupCount = 4;
    int[] groups = new int[]{ 0, 1, 2, 3 };
    String[] roles = new String[]{ "admin", "admin", "manager", "member" };
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?orderBy=id
   * Note: running this will delete all data in the registry!
   */
  public void testSortPeopleById() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "id";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 10;
    Subject[] subjects = new Subject[]{
      SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
      SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6, SubjectTestHelper.SUBJ7, SubjectTestHelper.SUBJ8, SubjectTestHelper.SUBJ9
    };
    String[] roles = new String[]{
      "member", "admin", "admin", "admin", "admin",
      "manager", "manager", "member", "member", "member"
    };
    validateMembers(resultObject, memberCount, sortBy, start, memberCount, subjects, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?orderBy=displayName
   * Note: running this will delete all data in the registry!
   */
  public void testSortPeopleByDisplayName() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "displayName";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 10;
    Subject[] subjects = new Subject[]{
      SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
      SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6, SubjectTestHelper.SUBJ7, SubjectTestHelper.SUBJ8, SubjectTestHelper.SUBJ9
    };
    String[] roles = new String[]{
      "member", "admin", "admin", "admin", "admin",
      "manager", "manager", "member", "member", "member"
    };
    validateMembers(resultObject, memberCount, sortBy, start, memberCount, subjects, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?orderBy=voot_membership_role
   * Note: running this will delete all data in the registry!
   */
  public void testSortPeopleByVootMembershipRole() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "voot_membership_role";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 10;
    Subject[] subjects = new Subject[]{
      SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
      SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6, SubjectTestHelper.SUBJ7, SubjectTestHelper.SUBJ8, SubjectTestHelper.SUBJ9
    };
    String[] roles = new String[]{
      "member", "admin", "admin", "admin", "admin",
      "manager", "manager", "member", "member", "member"
    };
    validateMembers(resultObject, memberCount, sortBy, start, memberCount, subjects, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups?search=testVoot:group1
   * Note: running this will delete all data in the registry!
   */
  public void testSearchGroupsWithResults() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String search = "testVoot:group1";
    String sortBy = null;
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI(null, search, null, start, count);
  
    //analyze the result
    int groupCount = 3;
    int[] groups = new int[]{ 1, 10, 11 };
    String[] roles = new String[]{ "member", "member", "member" };
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups?search=unexistent:group
   * Note: running this will delete all data in the registry!
   */
  public void testSearchGroupsWithOutResults() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String search = "unexistent:group";
    String sortBy = null;
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI(null, search, null, start, count);
  
    //analyze the result
    int groupCount = 0;
    int[] groups =  null;
    String[] roles = null;
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups?search=
   * Note: running this will delete all data in the registry!
   */
  public void testSearchAllGroups() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String search = "";
    String sortBy = null;
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI(null, search, null, start, count);
  
    //analyze the result
    int groupCount = 12;
    int[] groups = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    String[] roles = new String[]{
      "member", "member", "member", "member", "member", "member",
      "member", "member", "member", "member", "member", "member"
    };
    validateGroups(resultObject, groupCount, sortBy, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
}
