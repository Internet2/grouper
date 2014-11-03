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
 * Class to test the main service logic for the VOOT connector for Grouper.
 */
public class VootServiceLogicTest extends VootTest {

  /**
   * Main method to execute all tests.
   * @param args parameters passed to main (ignored).
   */
  public static void main(String[] args) {
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0GroupMe"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0GroupSubject0"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0GroupSubject1"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0GroupSubject2"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleMeGroup1"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleMeGroup3"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleMeGroup0"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject0Group1"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject0Group3"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject0Group0"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject1Group2"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject1Group3"));
    TestRunner.run(new VootServiceLogicTest("testLoginMePeopleSubject0Group10"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject0Group10"));
    TestRunner.run(new VootServiceLogicTest("testLoginSubject0PeopleSubject1Group11"));
  }
  
  /**
   * Default constructor to initialize VOOT test cases. 
   */
  public VootServiceLogicTest() {
    super();
  }

  /**
   * Constructor with a test name as parameter.
   * @param name the name of this test execution on the test runner.
   */
  public VootServiceLogicTest(String name) {
    super(name);
  }
  
  /**
   * Method to create a registry with all the required users and groups
   * to test all VOOT calls with this test suite.
   * 
   * To test all the combinations, the membership and read access to groups
   * will be assigned to two different subjects with the following logic:
   * <ul>
   * <li><b>Group 0</b>: Subject0 is not member, Subject1 is not member, Subject0 does not have read access, Subject1 does not have read access</li>
   * <li><b>Group 1</b>: Subject0 is admin member, Subject1 is not member, Subject0 has read access, Subject1 does not have read access</li>
   * <li><b>Group 2</b>: Subject0 is not member, Subject1 is admin member, Subject0 does not have read access, Subject1 has read access</li>
   * <li><b>Group 3</b>: Subject0 is member, Subject1 is member, Subject0 has read access, Subject1 has read access</li>
   * <li><b>Group 4</b>: Subject0 is admin member, Subject1 is not member, Subject0 does not have read access, Subject1 does not have read access</li>
   * <li><b>Group 5</b>: Subject0 is not member, Subject1 is admin member, Subject0 does not have read access, Subject1 does not have read access</li>
   * <li><b>Group 6</b>: Subject0 is member, Subject1 is member, Subject0 does not have read access, Subject1 does not have read access</li>
   * <li><b>Group 7</b>: Subject0 is not member, Subject1 is not member, Subject0 has read access, Subject1 does not have read access</li>
   * <li><b>Group 8</b>: Subject0 is not member, Subject1 is not member, Subject0 does not have read access, Subject1 has read access</li>
   * <li><b>Group 9</b>: Subject0 is not member, Subject1 is not member, Subject0 has read access, Subject1 has read access</li>
   * <li><b>Group 10</b>: Subject0 is updater member, Subject1 is not member, Subject0 has read access, Subject1 does not have read access</li>
   * <li><b>Group 11</b>: Subject0 is not member, Subject1 is updater member, Subject0 does not have access, Subject 1 has read access</li>
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
    groups[1].addMember(SubjectTestHelper.SUBJ0, false);
    groups[2].addMember(SubjectTestHelper.SUBJ1, false);
    groups[3].addMember(SubjectTestHelper.SUBJ0, false);
    groups[3].addMember(SubjectTestHelper.SUBJ1, false);
    groups[4].addMember(SubjectTestHelper.SUBJ0, false);
    groups[5].addMember(SubjectTestHelper.SUBJ1, false);
    groups[6].addMember(SubjectTestHelper.SUBJ0, false);
    groups[6].addMember(SubjectTestHelper.SUBJ1, false);
    groups[10].addMember(SubjectTestHelper.SUBJ0, false);
    groups[11].addMember(SubjectTestHelper.SUBJ1, false);
    
    // Setting read access
    groups[1].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    groups[2].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    groups[3].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    groups[3].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    groups[7].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    groups[8].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    groups[9].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    groups[9].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    
    // Setting administrator rights
    groups[1].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    groups[2].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    groups[4].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    groups[5].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    
    // Setting update rights
    groups[10].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    groups[11].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE, false);

    // Stop root session
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0GroupMe() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me");

    //analyze the result
    int groupCount = 5;
    int[] groups = new int[]{ 1, 3, 4, 6, 10 };
    String[] roles = new String[]{ "admin", "member", "admin", "member", "manager" };
    validateGroups(resultObject, groupCount, null, 0, groupCount, groups, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/test.subject.0
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0GroupSubject0() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI(SubjectTestHelper.SUBJ0_ID);

    //analyze the result
    int groupCount = 5;
    int[] groups = new int[]{ 1, 3, 4, 6, 10 };
    String[] roles = new String[]{ "admin", "member", "admin", "member", "manager" };
    validateGroups(resultObject, groupCount, null, 0, groupCount, groups, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/test.subject.1
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0GroupSubject1() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI(SubjectTestHelper.SUBJ1_ID);

    //analyze the result
    int groupCount = 5;
    int[] groups = new int[]{ 2, 3, 5, 6, 11 };
    String[] roles = new String[]{ "admin", "member", "admin", "member", "manager" };
    validateGroups(resultObject, groupCount, null, 0, groupCount, groups, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/test.subject.2
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0GroupSubject2() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI(SubjectTestHelper.SUBJ2_ID);

    //analyze the result
    int groupCount = 0;
    int[] groups = null;
    String[] roles = null;
    validateGroups(resultObject, groupCount, null, 0, groupCount, groups, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group1
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleMeGroup1() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0, true);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[1]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0 };
    String[] roles = new String[]{ "admin" };
    validateMembers(resultObject, 1, null, 0, 1, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group3
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleMeGroup3() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[3]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1 };
    String[] roles = new String[]{ "member", "member" };
    validateMembers(resultObject, 2, null, 0, 2, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleMeGroup0() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0, true);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0]);

    //analyze the result
    Subject[] subjects = null;
    String[] roles = null;
    validateMembers(resultObject, 0, null, 0, 0, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.0/testVoot:group1
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject0Group1() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ0_ID, GROUP_NAMES[1]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0 };
    String[] roles = new String[]{ "admin" };
    validateMembers(resultObject, 1, null, 0, 1, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.0/testVoot:group3
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject0Group3() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ0_ID, GROUP_NAMES[3]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1 };
    String[] roles = new String[]{ "member", "member" };
    validateMembers(resultObject, 2, null, 0, 2, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.0/testVoot:group0
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject0Group0() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0, true);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ0_ID, GROUP_NAMES[0]);

    //analyze the result
    Subject[] subjects = null;
    String[] roles = null;
    validateMembers(resultObject, 0, null, 0, 0, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.1/testVoot:group1
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject1Group2() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ1_ID, GROUP_NAMES[2]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ1 };
    String[] roles = new String[]{ "admin" };
    validateMembers(resultObject, 1, null, 0, 1, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.1/testVoot:group3
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject1Group3() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ1_ID, GROUP_NAMES[3]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1 };
    String[] roles = new String[]{ "member", "member" };
    validateMembers(resultObject, 2, null, 0, 2, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group10
   * Note: running this will delete all data in the registry!
   */
  public void testLoginMePeopleSubject0Group10() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[10]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0 };
    String[] roles = new String[]{ "manager" };
    validateMembers(resultObject, 1, null, 0, 1, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.0/testVoot:group10
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject0Group10() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ0_ID, GROUP_NAMES[10]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0 };
    String[] roles = new String[]{ "manager" };
    validateMembers(resultObject, 1, null, 0, 1, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/test.subject.1/testVoot:group11
   * Note: running this will delete all data in the registry!
   */
  public void testLoginSubject0PeopleSubject1Group11() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI(SubjectTestHelper.SUBJ1_ID, GROUP_NAMES[11]);

    //analyze the result
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ1 };
    String[] roles = new String[]{ "manager" };
    validateMembers(resultObject, 1, null, 0, 1, subjects, roles);

    GrouperSession.stopQuietly(grouperSession);
  }
  
}
