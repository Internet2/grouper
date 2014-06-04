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
package edu.cmu.it.apps.billing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.api.GcAssignPermissions;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetPermissionAssignments;
import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignPermissionsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipAnyLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * main class
 * @author mchyzer
 *
 */
public class CmuBilling {

  /**
   * logger
   */
  static Log log = GrouperClientUtils.retrieveLog(GrouperClient.class);

  /** should java exit on error? */
  public static boolean exitOnError = true;
  

  /**
   * @param args
   */
  public static void main(String[] args) {
    String operation = null;
    try {
      if (GrouperClientUtils.length(args) == 0) {
        usage();
        return;
      }
      
      //map of all command line args
      Map<String, String> argMap = GrouperClientUtils.argMap(args);
      
      Map<String, String> argMapNotUsed = new LinkedHashMap<String, String>(argMap);

      boolean debugMode = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "debug", false, false);
      
      GrouperClientLog.assignDebugToConsole(debugMode);
      
      operation = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "operation", true);
      
      String result = null;
      
      if (GrouperClientUtils.equals(operation, "canReadBill")) {
        
        result = canReadBill(argMap, argMapNotUsed);
        
      } else if (GrouperClientUtils.equals(operation, "assignUniversityAdmin")) {
        
        result = assignUniversityAdmin(argMap, argMapNotUsed);
        
      } else if (GrouperClientUtils.equals(operation, "assignLocalAdmin")) {
        
        result = assignLocalAdmin(argMap, argMapNotUsed);
        
      } else if (GrouperClientUtils.equals(operation, "assignDelegate")) {
        
        result = assignDelegate(argMap, argMapNotUsed);
        
      } else {
        System.err.println("Error: invalid operation: '" + operation + "', for usage help, run this program with no arguments" );
        if (exitOnError) {
          System.exit(1);
        }
        throw new RuntimeException("Invalid usage");
      }
      
      System.out.print("\n" + result);

      failOnArgsNotUsed(argMapNotUsed);
      
    } catch (Exception e) {
      System.err.println("Error with cmu client, check the logs: " + e.getMessage());
      log.fatal(e.getMessage(), e);
      if (exitOnError) {
        System.exit(1);
      }
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      try {
        log.debug("Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
      } catch (Exception e) {}
      GrouperClientLog.assignDebugToConsole(false);
    }

  }
  
  /** timing gate */
  private static long startTime = System.currentTimeMillis();

  /**
   * @param argMapNotUsed
   */
  public static void failOnArgsNotUsed(Map<String, String> argMapNotUsed) {
    if (argMapNotUsed.size() > 0) {
      boolean failOnExtraParams = GrouperClientUtils.propertiesValueBoolean(
          "grouperClient.failOnExtraCommandLineArgs", true, true);
      String error = "Invalid command line arguments: " + argMapNotUsed.keySet();
      if (failOnExtraParams) {
        throw new RuntimeException(error);
      }
      log.error(error);
    }
  }

  /**
   * print usage
   */
  public static void usage() {
    String usage = "Check access:\nWindows: java -cp cmuBilling.jar;grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      +	"--operation=canReadBill --studentToCheck=studentId --personWithAccess=personId\n"
      + "Unix: java -cp cmuBilling.jar;grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=canReadBill --studentToCheck=studentId --personWithAccess=personId\n\n"
      + "Assign university admin:\nWindows: java -cp cmuBilling.jar;grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=assignUniversityAdmin --personWithAccess=personId\n"
      + "Unix: java -cp cmuBilling.jar:grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=assignUniversityAdmin --personWithAccess=personId\n\n"
      + "Assign local admin:\nWindows: java -cp cmuBilling.jar;grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=assignLocalAdmin --personWithAccess=personId --orgName=edu:cmu:community:resources:orgs:UNIV:USCH:02XX:BIOL:BIOT:0136\n"
      + "Unix: java -cp cmuBilling.jar:grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=assignLocalAdmin --personWithAccess=personId --orgName=edu:cmu:community:resources:orgs:UNIV:USCH:02XX:BIOL:BIOT:0136\n\n"
      + "Assign delegate:\nWindows: java -cp cmuBilling.jar;grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=assignDelegate --personWithAccess=personId --studentId=studentId\n"
      + "Unix: java -cp cmuBilling.jar:grouperClient.jar edu.cmu.it.apps.billing.CmuBilling " 
      + "--operation=assignDelegate --personWithAccess=personId --studentId=studentId\n\n"
      ;
    System.err.println(usage);
  }

  /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String canReadBill(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String studentToCheckId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "studentToCheck", false);
      String personWithAccessId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "personWithAccess", false);
      StringBuilder result = new StringBuilder();
      
      WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = retrievePermissionAssignments(personWithAccessId);
      
      boolean canReadBill = false;

      //see if superadmin
      if (!canReadBill) {
        boolean hasUniversityAdmin = hasPermission(wsGetPermissionAssignmentsResults, 
            "edu:cmu:it:apps:billing:permissions:allBills");
        result.append("Has allBills permission? " + hasUniversityAdmin + "\n");
        canReadBill = hasUniversityAdmin;
      }
      
      //see if self
      if (!canReadBill) {
        boolean isSelf = GrouperClientUtils.equals(studentToCheckId, personWithAccessId);
        result.append("Is checking own bill? " + isSelf + "\n");
        if (isSelf) {
          boolean hasCheckOwnBill = hasPermission(wsGetPermissionAssignmentsResults, 
              "edu:cmu:it:apps:billing:permissions:myOwnBills");
          result.append("Has checkOwnBill permission? " + hasCheckOwnBill + "\n");
          canReadBill = hasCheckOwnBill;
        }
        
      }

      //see if delegate
      if (!canReadBill) {
        canReadBill = hasStudentDelegate(studentToCheckId, result,
            wsGetPermissionAssignmentsResults);
      }

      //see if in org via local admin
      if (!canReadBill) {

        //see what the user has
        Set<String> orgIds = permissionExtensionsByPrefix(wsGetPermissionAssignmentsResults, 
            "edu:cmu:it:apps:billing:roles:localBillingAdministrator",
            "edu:cmu:community:resources:orgs");

        if (GrouperClientUtils.length(orgIds) == 0) {
          result.append("Person is not local admin on any orgs\n");
        } else {
          result.append("Person is local admin on orgs: " + GrouperClientUtils.join(orgIds.iterator(), ", ") + "\n");
          
          //lets see what orgs the student is in
          Set<String> majorIds = inGroupExtensions(studentToCheckId, "edu:cmu:community:student:majors");
          
          if (GrouperClientUtils.length(majorIds) == 0) {
            result.append("Student does not have a major\n");
          } else {
            result.append("Student has majors: " + GrouperClientUtils.join(majorIds.iterator(), ", ") + "\n");
            
            //see if there is overlap
            orgIds.retainAll(majorIds);
            
            canReadBill = orgIds.size() > 0;
            
          }
          
        }
      }
      
      result.append("Can read bill? " + canReadBill + "\n");
      return result.toString();
    }

  /**
   * see if has student delegate
   * @param studentToCheckId
   * @param result
   * @param wsGetPermissionAssignmentsResults
   * @return true if does, false if not
   */
  private static boolean hasStudentDelegate(String studentToCheckId,
      StringBuilder result,
      WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults) {

    boolean hasStudentDelegate = hasPermission(wsGetPermissionAssignmentsResults, 
        "edu:cmu:it:apps:billing:permissions:billsViaDelegate");
    result.append("Has studentDelegate permission? " + hasStudentDelegate + "\n");
    if (hasStudentDelegate) {
      
      //get the attribute values of "delegateTo" on the checkOwnBill permission
      Set<String> delegateTo = permissionAttributeValues(wsGetPermissionAssignmentsResults, 
          "edu:cmu:it:apps:billing:permissions:billsViaDelegate", 
          "edu:cmu:it:apps:billing:attributes:delegateId");
      
      if (GrouperClientUtils.length(delegateTo) == 0) {
        result.append("Person has not been delegated to by any student\n");
      } else {
        result.append("Person has been assigned delegate from: " + GrouperClientUtils.join(delegateTo.iterator(), ", ") + "\n");
        return delegateTo.contains(studentToCheckId);
      }
      
    }
    return false;
  }

  /**
   * get the billing permissions for the application
   * @param personWithAccessId
   * @return
   */
  private static WsGetPermissionAssignmentsResults retrievePermissionAssignments(
      String personWithAccessId) {
    //lets get the relevant permissions for the person with access for all roles in the application
    GcGetPermissionAssignments gcGetPermissionAssignments = new GcGetPermissionAssignments();
    
    //we only need the "read" action
    gcGetPermissionAssignments.addAction("read");
    
    //check for the roles that the application uses
    gcGetPermissionAssignments.addRoleName("edu:cmu:it:apps:billing:roles:universityBillingAdministrator")
      .addRoleName("edu:cmu:it:apps:billing:roles:student")
      .addRoleName("edu:cmu:it:apps:billing:roles:studentDelegate")
      .addRoleName("edu:cmu:it:apps:billing:roles:localBillingAdministrator");
    
    //filter on "active" permissions, not one that are active in future, or expired
    gcGetPermissionAssignments.assignEnabled("T");
    
    //include attributes on assignment
    gcGetPermissionAssignments.assignIncludeAttributeAssignments(true);
    gcGetPermissionAssignments.assignIncludeAssignmentsOnAssignments(true);
    
    //check for the personWithAccess
    gcGetPermissionAssignments.addSubjectLookup(new WsSubjectLookup(personWithAccessId, "jdbc", null));

    //run the web service call
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = gcGetPermissionAssignments.execute();
    return wsGetPermissionAssignmentsResults;
  }

    /**
     * get the permission extensions that start with a certain prefix 
     * @param wsGetPermissionAssignmentsResults 
     * @param roleName 
     * @param permissionNamePrefix 
     * @return the extensions
     */
    private static Set<String> permissionExtensionsByPrefix(
        WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults, 
        String roleName, String permissionNamePrefix) {

      Set<String> result = new TreeSet<String>();
      
      for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(
          wsGetPermissionAssignmentsResults.getWsPermissionAssigns(), WsPermissionAssign.class)) {

        String permissionName = GrouperClientUtils.defaultString(wsPermissionAssign.getAttributeDefNameName());

        if (GrouperClientUtils.equals(roleName, wsPermissionAssign.getRoleName())
            && permissionName.startsWith(permissionNamePrefix)) {
          
          //get the last part which is the extension
          String permissionExtension = GrouperClientUtils.substringAfterLast(permissionName, ":");
          result.add(permissionExtension);
        }

      }
      return result;

    }
        
    /**
     * see the groupExtensions that the person is in
     * @param personId to check
     * @param folderName is the stem to start from
     * @return the group extensions
     */
    private static Set<String> inGroupExtensions(String personId, String folderName) {
      GcGetGroups gcGetGroups = new GcGetGroups();
      
      //the person we are checking
      gcGetGroups.addSubjectLookup(new WsSubjectLookup(personId, "jdbc", null));

      //only enabled memberships
      gcGetGroups.assignEnabled(true);
      
      //only the member list of the group
      gcGetGroups.assignFieldName("members");
      
      //look at all groups that are decendants of the folder
      gcGetGroups.assignStemScope(StemScope.ALL_IN_SUBTREE);

      gcGetGroups.assignWsStemLookup(new WsStemLookup(folderName, null));
      
      WsGetGroupsResults wsGetGroupsResults = gcGetGroups.execute();
      
      //we are only doing one query, so we only have one result
      WsGetGroupsResult wsGetGroupsResult = wsGetGroupsResults.getResults()[0];
      
      Set<String> result = new TreeSet<String>();
      
      for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGetGroupsResult.getWsGroups(), WsGroup.class)) {
        
        result.add(wsGroup.getExtension());
        
      }
      return result;
    }

    /**
     * see if the permission is there, if no, return it
     * @param wsGetPermissionAssignmentsResults
     * @param permissionName
     * @return the assignment or null if not found
     */
    private static WsPermissionAssign findPermissionId(
        WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults,
        String permissionName) {

      for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(
          wsGetPermissionAssignmentsResults.getWsPermissionAssigns(), WsPermissionAssign.class)) {
        
        if (GrouperClientUtils.equals(wsPermissionAssign.getAttributeDefNameName(), permissionName)) {
          return wsPermissionAssign;
        }
      }
      return null;
    }
    
    /**
     * get the permission attribute values
     * @param wsGetPermissionAssignmentsResults
     * @param permissionName
     * @param attributeName
     * @return the attribute values for that attribute (single valued multi assigned attribute)
     */
    private static Set<String> permissionAttributeValues(
        WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults, 
        String permissionName, String attributeName) {
      
      Set<String> result = new TreeSet<String>();
      Set<String> permissionAssignIds = new HashSet<String>();
      
      for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(
          wsGetPermissionAssignmentsResults.getWsPermissionAssigns(), WsPermissionAssign.class)) {
        
        if (GrouperClientUtils.equals(wsPermissionAssign.getAttributeDefNameName(), permissionName)) {
          permissionAssignIds.add(wsPermissionAssign.getAttributeAssignId());
        }
      }

      for (String permissionAssignId : permissionAssignIds) {
        //get the value of the attribute
        for (WsAttributeAssign wsAttributeAssign : 
            GrouperClientUtils.nonNull(wsGetPermissionAssignmentsResults.getWsAttributeAssigns(), WsAttributeAssign.class)) {
          
          if (GrouperClientUtils.equals(wsAttributeAssign.getOwnerAttributeAssignId(), permissionAssignId)) {
            if (GrouperClientUtils.equals(wsAttributeAssign.getAttributeDefNameName(), attributeName)) {
              
              for (WsAttributeAssignValue wsAttributeAssignValue 
                  : GrouperClientUtils.nonNull(wsAttributeAssign.getWsAttributeAssignValues(),
                      WsAttributeAssignValue.class)) {
                
                result.add(wsAttributeAssignValue.getValueSystem());
                
              }
              
            }
          }
          
        }
      }
      return result;
    }
    
    /**
     * 
     * @param wsGetPermissionAssignmentsResults
     * @param permissionName
     * @return true if has permission
     */
    private static boolean hasPermission(WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults,
        String permissionName) {
      for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(
          wsGetPermissionAssignmentsResults.getWsPermissionAssigns(), WsPermissionAssign.class)) {
        
        if (GrouperClientUtils.equals(wsPermissionAssign.getAttributeDefNameName(), permissionName)) {
          return true;
        }
        
      }
      return false;
    }

    /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String assignUniversityAdmin(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String personWithAccessId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "personWithAccess", false);
      StringBuilder result = new StringBuilder();
      
      WsAddMemberResults wsAddMemberResults = new GcAddMember()
        .addSubjectLookup(new WsSubjectLookup(personWithAccessId, "jdbc", null))
        .assignGroupName("edu:cmu:it:apps:billing:roles:universityBillingAdministrator_systemOfRecord").execute();
      
      String resultCode = wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode();
      
      result.append("Assign university admin: " + resultCode + "\n");
      return result.toString();
    }

    /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String assignLocalAdmin(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String personWithAccessId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "personWithAccess", false);
      String orgName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "orgName", false);
      StringBuilder result = new StringBuilder();

      //first make sure has the local admin role
      WsAddMemberResults wsAddMemberResults = new GcAddMember()
        .addSubjectLookup(new WsSubjectLookup(personWithAccessId, "jdbc", null))
        .assignGroupName("edu:cmu:it:apps:billing:roles:localBillingAdministrator_systemOfRecord").execute();
      
      String resultCode = wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode();
      
      result.append("Assign local admin role: " + resultCode + "\n");
  
      //adding a permission on the user and group
      WsMembershipAnyLookup wsMembershipAnyLookup = new WsMembershipAnyLookup();
      wsMembershipAnyLookup.setWsGroupLookup(new WsGroupLookup("edu:cmu:it:apps:billing:roles:localBillingAdministrator", null));
      wsMembershipAnyLookup.setWsSubjectLookup(new WsSubjectLookup(personWithAccessId, "jdbc", null));
      
      try {
        WsAssignPermissionsResults wsAssignPermissionsResults = new GcAssignPermissions()
          .addAction("read").addPermissionDefNameName(orgName)
          .addSubjectRoleLookup(wsMembershipAnyLookup).assignPermissionAssignOperation("assign_permission")
          .assignPermissionType("role_subject").execute();
  
        String changed = wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged();
        
        result.append("Assign org " + orgName + ", changed? " + changed + "\n");
        
        return result.toString();
      } catch (RuntimeException re) {
        GrouperClientUtils.injectInException(re, "Failure might be due to subject not being an employee...");
        throw re;
      }
    }

    /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String assignDelegate(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String personWithAccessId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "personWithAccess", false);
      String studentId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "studentId", false);
      StringBuilder result = new StringBuilder();

      //see if it already exists
      WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = retrievePermissionAssignments(personWithAccessId);
      
      boolean canReadBill = hasStudentDelegate(studentId, result,
          wsGetPermissionAssignmentsResults);

      result.append("Already is delegate? " + canReadBill + "\n");
      if (!canReadBill) {

        
        //first make sure has the local admin role
        WsAddMemberResults wsAddMemberResults = new GcAddMember()
          .addSubjectLookup(new WsSubjectLookup(personWithAccessId, "jdbc", null))
          .assignGroupName("edu:cmu:it:apps:billing:roles:studentDelegate_systemOfRecord").execute();
        
        String resultCode = wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode();
        
        result.append("Assign student delegate role: " + resultCode + "\n");

        //see if has permission
        String studentDelegatePermissionName = "edu:cmu:it:apps:billing:permissions:billsViaDelegate";
        WsPermissionAssign wsPermissionAssign = findPermissionId(wsGetPermissionAssignmentsResults, 
            studentDelegatePermissionName);
        
        result.append("Already had permission: studentDelegate: " + (wsPermissionAssign != null) + "\n");
        
        String permissionAssignId = wsPermissionAssign == null ? null : wsPermissionAssign.getAttributeAssignId();
        
        //if not there, we need to assign
        if (GrouperClientUtils.isBlank(permissionAssignId)) {
        
          //assign that permission
          
          //adding a permission on the user and group
          WsMembershipAnyLookup wsMembershipAnyLookup = new WsMembershipAnyLookup();
          wsMembershipAnyLookup.setWsGroupLookup(new WsGroupLookup("edu:cmu:it:apps:billing:roles:studentDelegate", null));
          wsMembershipAnyLookup.setWsSubjectLookup(new WsSubjectLookup(personWithAccessId, "jdbc", null));
          
          WsAssignPermissionsResults wsAssignPermissionsResults = new GcAssignPermissions()
            .addAction("read").addPermissionDefNameName(studentDelegatePermissionName)
            .addSubjectRoleLookup(wsMembershipAnyLookup).assignPermissionAssignOperation("assign_permission")
            .assignPermissionType("role_subject").execute();
          
          WsAttributeAssign wsAttributeAssign = wsAssignPermissionsResults
            .getWsAssignPermissionResults()[0].getWsAttributeAssigns()[0];
          
          permissionAssignId = wsAttributeAssign.getId();
          
          result.append("Assigned permission studentDelegate\n");
          
        }
        
        //now we have the id of the attribute assign, add an attribute on that assignment
        WsAttributeAssignLookup wsPermissionAssignLookup = new WsAttributeAssignLookup();
        wsPermissionAssignLookup.setUuid(permissionAssignId);
        
        WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
        wsAttributeAssignValue.setValueSystem(studentId);
        
        WsAssignAttributesResults wsAssignAttributesResults = new GcAssignAttributes()
          .addAction("read").addOwnerAttributeAssignLookup(wsPermissionAssignLookup)
          .addAttributeDefNameName("edu:cmu:it:apps:billing:attributes:delegateId").addValue(wsAttributeAssignValue)
          .assignAttributeAssignType("any_mem_asgn").assignAttributeAssignOperation("add_attr")
          .assignAttributeAssignValueOperation("assign_value").execute();
        
        String changed = wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged();
        String valueChanged = wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged();
        
        result.append("Assigned delegate for student: changed? " + changed + ", delegateId changed: " + valueChanged + "\n");
        
      }
      
      return result.toString();
    }
    
}
