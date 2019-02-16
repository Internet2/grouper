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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperVoot.restLogic.VootWsRest;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to test the parameter passing to main service logic for the VOOT connector for Grouper.
 */
public class VootErrorsTest extends VootTest {

  /**
   * Main method to execute all tests.
   * @param args parameters passed to main (ignored).
   */
  public static void main(String[] args) {
    TestRunner.run(new VootErrorsTest("testCallWrongBaseUrl"));
    TestRunner.run(new VootErrorsTest("testCallGroupsWrongUserUrl"));
    TestRunner.run(new VootErrorsTest("testCallPeopleNoPeopleUrl"));
    TestRunner.run(new VootErrorsTest("testCallPeopleNoGroupUrl"));
    TestRunner.run(new VootErrorsTest("testCallPeopleWrongUserUrl"));
    TestRunner.run(new VootErrorsTest("testCallPeopleWrongGroupUrl"));
    TestRunner.run(new VootErrorsTest("testCallGroupsWrongSortByParam"));
    TestRunner.run(new VootErrorsTest("testSortPeopleByDisplayName"));
  }
  
  /**
   * Default constructor to initialize VOOT test cases. 
   */
  public VootErrorsTest() {
    super();
  }

  /**
   * Constructor with a test name as parameter.
   * @param name the name of this test execution on the test runner.
   */
  public VootErrorsTest(String name) {
    super(name);
  }
  
  /**
   * Method to create a registry with all the required users and groups
   * to test all VOOT calls with this test suite.
   */
  @Override
  protected void createRegistryToTestVOOT() {
    // Setup data as root user
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // Create all groups for managing the different membership relations
    Group[] groups = new Group[2];
    for (int i = 0 ; i < groups.length; ++i) {
      groups[i] = new GroupSave(grouperSession).assignName(GROUP_NAMES[i])
          .assignDescription(GROUP_DESCRIPTIONS[i]).assignCreateParentStemsIfNotExist(true).save();
    }
    
    // Setting memberships
    for (int i = 0 ; i < groups.length; ++i) {
      groups[i].addMember(SubjectTestHelper.SUBJ0, false);
      groups[i].grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      
      groups[i].addMember(SubjectTestHelper.SUBJ1, false);
      groups[i].grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    }
    
    // Stop root session
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /wrongurl
   * Note: running this will delete all data in the registry!
   */
  public void testCallWrongBaseUrl() {
    createRegistryToTestVOOT();
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<String> urlStrings = GrouperUtil.toList("wrongurl");
    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
    
    String error = "Wrong params";
    String description = "Pass in a url string: groups or people";
    validateError(resultObject, error, description);
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/wronguser
   * Note: running this will delete all data in the registry!
   */
  public void testCallGroupsWrongUserUrl() {
    createRegistryToTestVOOT();
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<String> urlStrings = GrouperUtil.toList("groups", "wronguser");
    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
    
    String error = "Subject error";
    String description = "Subject not found: wronguser, Empty ArrayList";
    validateError(resultObject, error, description);
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people
   * Note: running this will delete all data in the registry!
   */
  public void testCallPeopleNoPeopleUrl() {
    createRegistryToTestVOOT();
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<String> urlStrings = GrouperUtil.toList("people");
    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
    
    String error = "No username";
    String description = "Error: no userName passed, GET, Empty ArrayList";
    validateError(resultObject, error, description);
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me
   * Note: running this will delete all data in the registry!
   */
  public void testCallPeopleNoGroupUrl() {
    createRegistryToTestVOOT();
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<String> urlStrings = GrouperUtil.toList("people", "@me");
    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
    
    String error = "No group name";
    String description = "Error: no group name passed, GET, Empty ArrayList";
    validateError(resultObject, error, description);
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/wronguser
   * Note: running this will delete all data in the registry!
   */
  public void testCallPeopleWrongUserUrl() {
    createRegistryToTestVOOT();
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<String> urlStrings = GrouperUtil.toList("people", "wronguser");
    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
    
    String error = "No group name";
    String description = "Error: no group name passed, GET, Empty ArrayList";
    validateError(resultObject, error, description);
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/wronggroup
   * Note: running this will delete all data in the registry!
   */
  public void testCallPeopleWrongGroupUrl() {
    createRegistryToTestVOOT();
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<String> urlStrings = GrouperUtil.toList("people", "@me", "wronggroup");
    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
    
    String error = "Group not found";
    String description = "Cannot find group with name: 'wronggroup'";
    validateError(resultObject, error, description);
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /groups/@me?sortBy=wrongfield
   * Note: running this will delete all data in the registry!
   */
  public void testCallGroupsWrongSortByParam() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "wrongfield";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callGroupsAPI("@me", null, sortBy, start, count);
  
    //analyze the result
    int groupCount = 2;
    int[] groups = new int[]{ 0, 1 };
    String[] roles = new String[]{ "member", "member" };
    validateGroups(resultObject, groupCount, null, start, groupCount, groups, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * Method that logs in with Subject0 and calls the URL:
   * /people/@me/testVoot:group0?orderBy=wrongfield
   * Note: running this will delete all data in the registry!
   */
  public void testSortPeopleByDisplayName() {
    createRegistryToTestVOOT();
    
    //start session as logged in user to web service
    String sortBy = "wrongfield";
    int start = 0;
    int count = -1;
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Object resultObject = callPeopleAPI("@me", GROUP_NAMES[0], sortBy, start, count);

    //analyze the result
    int memberCount = 2;
    Subject[] subjects = new Subject[]{ SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1 };
    String[] roles = new String[]{ "member", "member" };
    validateMembers(resultObject, memberCount, null, start, memberCount, subjects, roles);
  
    GrouperSession.stopQuietly(grouperSession);
  }
}
