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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;
import edu.internet2.middleware.grouperVoot.beans.VootPerson;
import edu.internet2.middleware.grouperVoot.messages.VootErrorResponse;
import edu.internet2.middleware.grouperVoot.messages.VootGetGroupsResponse;
import edu.internet2.middleware.grouperVoot.messages.VootGetMembersResponse;
import edu.internet2.middleware.grouperVoot.restLogic.VootWsRest;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to test the main service logic for the VOOT connector for Grouper.
 */
public abstract class VootTest extends GrouperTest {

  protected final static String[] GROUP_NAMES = {
    "testVoot:group0", "testVoot:group1", "testVoot:group2", "testVoot:group3", "testVoot:group4",
    "testVoot:group5", "testVoot:group6", "testVoot:group7", "testVoot:group8", "testVoot:group9",
    "testVoot:group10", "testVoot:group11"
  };
  
  protected final static String[] GROUP_DESCRIPTIONS = {
    "11 Group0 for testing VOOT", "10 Group1 for testing VOOT", "09 Group2 for testing VOOT", "08 Group3 for testing VOOT", "07 Group4 for testing VOOT",
    "06 Group5 for testing VOOT", "05 Group6 for testing VOOT", "04 Group7 for testing VOOT", "03 Group8 for testing VOOT", "02 Group9 for testing VOOT",
    "01 Group10 for testing VOOT", "00 Group11 for testing VOOT"
  };
  
  /**
   * Default constructor to initialize VOOT test cases. 
   */
  public VootTest() {
    super();
  }

  /**
   * Constructor with a test name as parameter.
   * @param name the name of this test execution on the test runner.
   */
  public VootTest(String name) {
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
  protected abstract void createRegistryToTestVOOT();
  
  /**
   * Method the realize the call to VOOT groups api.
   * @param grouperSession the grouper session to be used for calling
   * @param userUrl the url to call
   * @return the object obtained from VOOT call
   */
  protected Object callGroupsAPI(String userUrl) {
    return callGroupsAPI(userUrl, null, null, 0, -1);
  }
  
  /**
   * Method the realize the call to VOOT groups api.
   * @param grouperSession the grouper session to be used for calling
   * @param userUrl the url to call
   * @param sortBy the field name to be used for sorting or null of no sorting.
   * @param start the first element in the result set (0 means start from beginning).
   * @param count the number of elements in the result set (-1 or 0 means find all).
   * @return the object obtained from VOOT call
   */
  protected Object callGroupsAPI(String userUrl, String search, String sortBy, int start, int count) {
    // URL: /groups/test.subject.0
    List<String> urlStrings = (userUrl != null) ? GrouperUtil.toList("groups", userUrl) : GrouperUtil.toList("groups");

    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    if (search != null) urlParamMap.put("search", new String[]{ search });
    if (sortBy != null) urlParamMap.put("sortBy", new String[]{ sortBy });
    if (start != 0) urlParamMap.put("startIndex", new String[]{ Integer.toString(start) });
    if (count != -1) urlParamMap.put("count", new String[]{ Integer.toString(count) });
    
    // main business logic method
    return vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
  }
  
  /**
   * Method the realize the call to VOOT people api.
   * @param userUrl the url to call
   * @param groupUrl the url to call
   * @param sortBy the field name to be used for sorting or null of no sorting.
   * @param start the first element in the result set (0 means start from beginning).
   * @param count the number of elements in the result set (-1 or 0 means find all).
   * @return the object obtained from VOOT call
   */
  protected Object callPeopleAPI(String userUrl, String groupUrl) {
    return callPeopleAPI(userUrl, groupUrl, null, 0, -1);
  }
  
  /**
   * Method the realize the call to VOOT people api.
   * @param userUrl the url to call
   * @param groupUrl the url to call
   * @return the object obtained from VOOT call
   */  
  protected Object callPeopleAPI(String userUrl, String groupUrl, String sortBy, int start, int count) {
    List<String> urlStrings = GrouperUtil.toList("people", userUrl, groupUrl);

    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    if (sortBy != null) urlParamMap.put("sortBy", new String[]{ sortBy });
    if (start != 0) urlParamMap.put("startIndex", new String[]{ Integer.toString(start) });
    if (count != -1) urlParamMap.put("count", new String[]{ Integer.toString(count) });
    
    // main business logic method
    return vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);
  }
  
  /**
   * Method to validate groups result from VOOT call.
   * @param resultObject the object returned from VOOT call.
   * @param count the number of groups expected to be in the output object.
   * @param groups the groups id (in this class definition) to be searched in the result.
   */
  protected void validateGroups(Object resultObject, int total, String orderBy, int start, int count, int[] groups, String[] roles) {
    assertNotNull(resultObject);
    assertTrue(resultObject.getClass().toString(), resultObject instanceof VootGetGroupsResponse);
    
    VootGetGroupsResponse vootGetGroupsResponse = (VootGetGroupsResponse) resultObject;
    assertEquals(start, vootGetGroupsResponse.getStartIndex().intValue());
    assertEquals(count, vootGetGroupsResponse.getItemsPerPage().intValue());
    assertEquals(total, vootGetGroupsResponse.getTotalResults().intValue());
    assertEquals(count, GrouperUtil.length(vootGetGroupsResponse.getEntry()));
    
    if (groups == null) {
      assertNull(vootGetGroupsResponse.getEntry());
    }
    else {
      assertNotNull(vootGetGroupsResponse.getEntry());
      
      // Check if all groups are present in the response, without considering their order
      int groupCount = 0;
      @SuppressWarnings("unchecked")
      List<VootGroup> groupsList = Arrays.asList(vootGetGroupsResponse.getEntry());
      for (int i = 0; i < total; ++i) {
        VootGroup vootGroup = new VootGroup();
        vootGroup.setId(GROUP_NAMES[groups[i]]);
        vootGroup.setName(GROUP_NAMES[groups[i]]);
        vootGroup.setDescription(GROUP_DESCRIPTIONS[groups[i]]);
        vootGroup.setVoot_membership_role(roles[i]);
        
        if (groupsList.contains(vootGroup)) groupCount++;
      }
      
      assertEquals(count, groupCount);
      
      // Check if all groups are present in the response, also considering their order
      if (orderBy != null) {
        try {
          // Set up introspection for the field specified by orderBy
          Field f = VootGroup.class.getDeclaredField(orderBy);
          f.setAccessible(true);
          
          // Verify sorting
          for (int i = 0; i < total-1; ++i) {
            String value1 = f.get(vootGetGroupsResponse.getEntry()[i]).toString();
            String value2 = f.get(vootGetGroupsResponse.getEntry()[i+1]).toString();
            
            assertTrue(value1.compareTo(value2) <= 0);
          }
        }
        catch (Exception e) {
          assertTrue("Exception in checking sort", false);
        }
      }
    }
  }
  
  /**
   * Method to validate subject result from VOOT call.
   * @param resultObject the object returned from VOOT call.
   * @param subjects the subjects expected to be in the output.
   * @param roles the roles of the subjects in the output.
   */
  protected void validateMembers(Object resultObject, int total, String orderBy, int start, int count, Subject[] subjects, String[] roles) {
    assertNotNull(resultObject);
    assertTrue(resultObject.getClass().toString(), resultObject instanceof VootGetMembersResponse);
    
    int subjectCount = (subjects == null) ? 0 : subjects.length;
    VootGetMembersResponse vootGetMembersResponse = (VootGetMembersResponse) resultObject;
    assertNotNull(vootGetMembersResponse.getEntry());
    
    // Some group may contain GrouperSystem between its members.
    // In case just ignore this user from the validation.
    boolean isGrouperSystemPresent = false;
    for (int i = 0; i < vootGetMembersResponse.getEntry().length; ++i) {
      if ("GrouperSystem".equals(vootGetMembersResponse.getEntry()[i].getId())) {
        isGrouperSystemPresent = true;
      }
    }
    
    if (isGrouperSystemPresent) {
      assertEquals(start, vootGetMembersResponse.getStartIndex().intValue());
      assertEquals(count, vootGetMembersResponse.getItemsPerPage().intValue());
      assertEquals(subjectCount+1, vootGetMembersResponse.getTotalResults().intValue());
      assertEquals(count+1, GrouperUtil.length(vootGetMembersResponse.getEntry()));
    }
    else {
      assertEquals(start, vootGetMembersResponse.getStartIndex().intValue());
      assertEquals(count, vootGetMembersResponse.getItemsPerPage().intValue());
      assertEquals(subjectCount, vootGetMembersResponse.getTotalResults().intValue());
      assertEquals(count, GrouperUtil.length(vootGetMembersResponse.getEntry()));
    }
    
    if (subjects != null) {
      // Check if all groups are present in the response, without considering their order
      int memberCount = 0;
      @SuppressWarnings("unchecked")
      List<VootGroup> membersList = Arrays.asList(vootGetMembersResponse.getEntry());
      for (int i = 0; i < subjectCount; ++i) {
        VootPerson vootPerson = new VootPerson(subjects[i]);
        vootPerson.setVoot_membership_role(roles[i]);
        
        if (membersList.contains(vootPerson)) memberCount++;
      }
      
      assertEquals(count, memberCount);
      
      // Check if all groups are present in the response, also considering their order
      if (orderBy != null) {
        try {
          // Set up introspection for the field specified by orderBy
          Field f = VootPerson.class.getDeclaredField(orderBy);
          f.setAccessible(true);
          
          // Verify sorting
          for (int i = 0; i < total-1; ++i) {
            String value1 = f.get(vootGetMembersResponse.getEntry()[i]).toString();
            String value2 = f.get(vootGetMembersResponse.getEntry()[i+1]).toString();
            
            assertTrue(value1.compareTo(value2) <= 0);
          }
        }
        catch (Exception e) {
          assertTrue("Exception in checking sort", false);
        }
      }
    }
  }
    
  /**
   * Method to validate error result from VOOT call.
   * @param resultObject the object returned from VOOT call.
   * @param error the error name returned from VOOT.
   * @param description the error description returned from VOOT.
   */
  protected void validateError(Object resultObject, String error, String description) {
    assertNotNull(resultObject);
    assertTrue(resultObject.getClass().toString(), resultObject instanceof VootErrorResponse);
    
    VootErrorResponse vootErrorResponse = (VootErrorResponse) resultObject;
    assertEquals(error, vootErrorResponse.getError());
    assertEquals(description, vootErrorResponse.getError_description());
  }
}
