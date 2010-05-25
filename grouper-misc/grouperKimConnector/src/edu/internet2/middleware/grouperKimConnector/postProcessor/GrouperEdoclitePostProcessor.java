/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.postProcessor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.kuali.rice.kew.edl.EDocLitePostProcessor;
import org.kuali.rice.kew.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kew.postprocessor.ProcessDocReport;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignPermissions;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembershipAnyLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.grouperClientMail.GrouperClientEmail;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimServiceUtils;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimSubject;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * provision groups
 */
public class GrouperEdoclitePostProcessor extends EDocLitePostProcessor {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GrouperEdoclitePostProcessor.class);

  /**
   * when the document goes to final, provision the group
   * @see org.kuali.rice.kew.edl.EDocLitePostProcessor#doRouteStatusChange(org.kuali.rice.kew.postprocessor.DocumentRouteStatusChange)
   */
  @Override
  public ProcessDocReport doRouteStatusChange(DocumentRouteStatusChange event)
      throws Exception {
    ProcessDocReport processDocReport =  super.doRouteStatusChange(event);
    
    doRouteStatusChangeHelper(event, processDocReport);
    
    return processDocReport;
  }

  /**
   * @param grouperKimSaveMembershipProperties
   * @param root
   * @param xpath
   * @return actions
   */
  private static Set<String> processPermissionActions(
      GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties,
      Element root, XPath xpath) {
    
    Set<String> actions = new HashSet<String>();
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getActionsForPermissions())) {
      actions.addAll(GrouperClientUtils.splitTrimToList(grouperKimSaveMembershipProperties.getActionsForPermissions(), ","));
    }
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldPrefixActionsForPermissions())) {
      
      String prefixAction = grouperKimSaveMembershipProperties.getEdocliteFieldPrefixActionsForPermissions();
      
      List<String> allowedActions = null;
      if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getAllowedActionsForPermissions())) {
        allowedActions = GrouperClientUtils.splitTrimToList(grouperKimSaveMembershipProperties.getAllowedActionsForPermissions(), ",");
      }
      
      for (int i=0;i<200;i++) {
        String action = xpathValue(xpath, root, prefixAction + i);
        
        if (GrouperClientUtils.isBlank(action)) {
          continue;
        }
        
        action = GrouperClientUtils.trim(action);
        
        //see if not in the list, if there is a list
        if (GrouperClientUtils.length(allowedActions) > 0) {
          if (!allowedActions.contains(action)) {
            throw new RuntimeException("Not allowed to access action: '" + action + "'");
          }
        }
  
        actions.add(action);
      }
    }
    return actions;
  }
  
  /**
   * get the xpath value from document
   * @param xpath
   * @param root
   * @param fieldName
   * @return the value
   */
  private static String xpathValue(XPath xpath, Element root, String fieldName) {
    try {
      //this xpath will give the value element for the group name
      return xpath.evaluate(
          "/documentContent/applicationContent/data/version[@current = \"true\"]" +
          "/field[@name = \"" + fieldName + "\"]/value", root);

    } catch (XPathException xpe) {
      throw new RuntimeException("Problem looking for field: '" + fieldName + "'");
    }
  }


  /**
   * @param event
   * @param processDocReport
   */
  public static void doRouteStatusChangeHelper(DocumentRouteStatusChange event,
      ProcessDocReport processDocReport) {
    //if not an exception
    if (processDocReport.isSuccess()) {
      
      //if going to final state
      if (GrouperClientUtils.equals(KEWConstants.ROUTE_HEADER_FINAL_CD, event.getNewRouteStatus())) {
        boolean sendingEmail = false;
        StringBuilder report = new StringBuilder();
        GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties = null;
        String docTypeName = null;
        try {
          
          DocumentRouteHeaderValue documentRouteHeaderValue = KEWServiceLocator
            .getRouteHeaderService().getRouteHeader(event.getRouteHeaderId());
          docTypeName = documentRouteHeaderValue.getDocumentType().getName();
          
          grouperKimSaveMembershipProperties = 
            GrouperKimSaveMembershipProperties.grouperKimSaveMembershipProperties(docTypeName);
          
          if (grouperKimSaveMembershipProperties != null) {
            
            Person person = KIMServiceLocator.getPersonService().getPerson(documentRouteHeaderValue.getInitiatorWorkflowId());
            String entityId = person.getEntityId();
            sendingEmail = !GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEmailAdmins());
            
            GrouperKimSubject grouperKimSubject = GrouperKimServiceUtils.convertEntityIdToSubject(entityId);
            
            WsSubject wsSubject = grouperKimSubject.getWsSubject();
            String[] attributeNames = grouperKimSubject.getSubjectAttributeNames();
            
            DocumentRouteHeaderValue val = KEWServiceLocator.getRouteHeaderService().getRouteHeader(event.getRouteHeaderId());
            Document document = null;
            
            try {
              document = getEDLContent(val);
            } catch (Exception e) {
              throw new RuntimeException("Cant find document from route header id: " + event.getRouteHeaderId(), e);
            }
            
            Element root = document.getDocumentElement();
            XPath xpath = XPathFactory.newInstance().newXPath();

            //Here is the doc: routeContext.getDocumentContent().getDocContent()
            //<documentContent>
            //  <applicationContent>
            //    <data edlName="sampleRouteSplitLogicJoin.doctype">
            //      <version current="false" date="Mon Feb 15 14:54:20 EST 2010" version="0" />
            //      <version current="false" date="Mon Feb 15 14:54:24 EST 2010" version="1">
            //        <field name="rightBranchCheckbox">
            //          <value>true</value>
            //        </field>
            //      </version>
            //     <version current="true" date="Mon Feb 15 14:56:25 EST 2010" version="5">
            //        <field name="rightBranchCheckbox">
            //          <value>true</value>
            //        </field>
            //      </version>
            //    </data>
            //  </applicationContent>
            //</documentContent>

            //add subject info to email report
            if (sendingEmail) {
              report.append(GrouperKimUtils.convertWsSubjectToStringForLog(wsSubject, attributeNames));
              report.append("\n\n");
            }

            Timestamp enabledTime = null;
            {
              String edocliteEnabledField = grouperKimSaveMembershipProperties.getEdocliteFieldGroupEnabledDate();
              String enabledString = null;
              
              if (!GrouperClientUtils.isBlank(edocliteEnabledField)) {
                enabledString = xpathValue(xpath, root, edocliteEnabledField);
              }
              if (!GrouperClientUtils.isBlank(enabledString)) {
                enabledTime = GrouperClientUtils.toTimestamp(GrouperClientUtils.stringToDate2(enabledString));
              }
            }
            Timestamp disabledTime = null;
            {
              String edocliteDisabledField = grouperKimSaveMembershipProperties.getEdocliteFieldGroupDisabledDate();
              String disabledString = null;
              
              if (!GrouperClientUtils.isBlank(edocliteDisabledField)) {
                disabledString = xpathValue(xpath, root, edocliteDisabledField);
              }
              if (!GrouperClientUtils.isBlank(disabledString)) {
                disabledTime = GrouperClientUtils.toTimestamp(GrouperClientUtils.stringToDate2(disabledString));
              }
            }
            
            if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldPrefix())) {
              
              processAddMemberToGroupByEdoclitePrefix(sendingEmail, report,
                  grouperKimSaveMembershipProperties, wsSubject, root, xpath, enabledTime, disabledTime);
              
              
            }
  
            if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getAddMembershipToGroups())) {
  
              String[] groupsToAdd = GrouperClientUtils.splitTrim(grouperKimSaveMembershipProperties.getAddMembershipToGroups(), ",");
              
              
              for (String groupToAdd : groupsToAdd) {
                addMemberToGroup(sendingEmail, report, wsSubject, groupToAdd, enabledTime, disabledTime);
                
              }
            }
            
            if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getRemoveMembershipFromGroups())) {
  
              String[] groupsToRemove = GrouperClientUtils.splitTrim(grouperKimSaveMembershipProperties.getRemoveMembershipFromGroups(), ",");
              for (String groupToRemove : groupsToRemove) {
                removeMemberFromGroup(sendingEmail, report, wsSubject, groupToRemove);
              }
            }
            
            //lets see if there is a role/permissions
            if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getRoleForPermissions())
                || !GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldRoleForPermissions())) {
              
              String roleName = processPermissionRoleName(grouperKimSaveMembershipProperties, root, xpath);
             
              String operation = processPermissionOperation(grouperKimSaveMembershipProperties, root, xpath);
              
              Set<String> actions = processPermissionActions(grouperKimSaveMembershipProperties, root, xpath);
              
              Set<String> permissionNames = processPermissionNames(grouperKimSaveMembershipProperties, root, xpath);
              
              //provision role
              addMemberToGroup(sendingEmail, report, wsSubject, roleName, enabledTime, disabledTime);
              
              addMemberPermissions(sendingEmail, report, wsSubject, roleName, operation, actions, permissionNames, enabledTime, disabledTime);
              
              //end role/permissions provisioning
            }
            
            
            if (sendingEmail) {
              new GrouperClientEmail().setBody(report.toString()).setSubject("Grouper Rice auto-provision for document: " + docTypeName)
                .setTo(grouperKimSaveMembershipProperties.getEmailAdmins()).send();
            }
            
          }
        } catch (RuntimeException e) {
          
          //lets still send an email so people know what is going on...
          if (sendingEmail && grouperKimSaveMembershipProperties != null) {
            try {
              report.append("\n\n").append(GrouperClientUtils.getFullStackTrace(e));
              new GrouperClientEmail().setBody(report.toString()).setSubject("Grouper Rice auto-provision ERROR for document: " + docTypeName)
                .setTo(grouperKimSaveMembershipProperties.getEmailAdmins()).send();
              
            } catch (Exception e2) {
              LOG.error("error though not preempting other exception", e2);
              //dont rethrow in case preempting existing exception
            }
            
          }
          throw e;
        }
      }
    }
  }

  /**
   * @param sendingEmail
   * @param report
   * @param grouperKimSaveMembershipProperties
   * @param wsSubject
   * @param root
   * @param xpath
   * @param enabledDate 
   * @param disabledDate 
   */
  private static void processAddMemberToGroupByEdoclitePrefix(boolean sendingEmail,
      StringBuilder report,
      GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties,
      WsSubject wsSubject, Element root, XPath xpath, Date enabledDate, Date disabledDate) {
    String eDocliteFieldPrefix = grouperKimSaveMembershipProperties.getEdocliteFieldPrefix();
    
    for (int i=0;i<200;i++) {
      
      String groupName = xpathValue(xpath, root, eDocliteFieldPrefix + i);

      if (GrouperClientUtils.isBlank(groupName)) {
        continue;
      }
      
      groupName = GrouperClientUtils.trim(groupName);
      
      if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEnteredGroupNamePrefix())) {
        
        groupName = grouperKimSaveMembershipProperties.getEnteredGroupNamePrefix() + groupName;
        
      }
      
      //make sure its ok
      if (!grouperKimSaveMembershipProperties.allowedToAccessGroup(groupName)) {
        throw new RuntimeException("Not allowed to access groupName: '" + groupName + "'");
      }
      
      addMemberToGroup(sendingEmail, report, wsSubject, groupName, enabledDate, disabledDate);
                      
    }
  }

  /**
   * @param sendingEmail
   * @param report
   * @param wsSubject
   * @param groupToRemove
   */
  private static void removeMemberFromGroup(boolean sendingEmail, StringBuilder report,
      WsSubject wsSubject, String groupToRemove) {
    String groupName = groupToRemove.contains(":") ? groupToRemove : null;
    String groupId = groupToRemove.contains(":") ? null : groupToRemove;
    GcDeleteMember gcDeleteMember = new GcDeleteMember().addSubjectLookup(
        new WsSubjectLookup(wsSubject.getId(), wsSubject.getSourceId(), null));
    if (!GrouperClientUtils.isBlank(groupName)) {
      gcDeleteMember.assignGroupName(groupName);
    }
    if (!GrouperClientUtils.isBlank(groupId)) {
      gcDeleteMember.assignGroupUuid(groupId);
    }
    WsDeleteMemberResults wsDeleteMemberResults = gcDeleteMember.execute();
    
    // based on result, add report
    if (sendingEmail) {
      report.append("Group removeMember: ").append(wsDeleteMemberResults.getWsGroup().getName());
      report.append(" - ").append(wsDeleteMemberResults.getResults()[0].getResultMetadata().getResultCode()).append("\n\n");
    }
  }

  /**
   * @param sendingEmail
   * @param report
   * @param wsSubject
   * @param groupToAdd
   * @param enabledDate 
   * @param disabledDate 
   */
  private static void addMemberToGroup(boolean sendingEmail, StringBuilder report,
      WsSubject wsSubject, String groupToAdd, Date enabledDate, Date disabledDate) {
    String groupName = groupToAdd.contains(":") ? groupToAdd : null;
    String groupId = groupToAdd.contains(":") ? null : groupToAdd;
    GcAddMember gcAddMember = new GcAddMember().addSubjectLookup(
        new WsSubjectLookup(wsSubject.getId(), wsSubject.getSourceId(), null));
    if (!GrouperClientUtils.isBlank(groupName)) {
      gcAddMember.assignGroupName(groupName);
    }
    if (!GrouperClientUtils.isBlank(groupId)) {
      gcAddMember.assignGroupUuid(groupId);
    }
    
    gcAddMember.assignDisabledTime(GrouperClientUtils.toTimestamp(disabledDate));
    gcAddMember.assignEnabledTime(GrouperClientUtils.toTimestamp(enabledDate));
    
    WsAddMemberResults wsAddMemberResults = gcAddMember.execute();

    if (enabledDate != null || disabledDate != null) {
      //lets get the 
    }
    
    // based on result, add report
    if (sendingEmail) {
      report.append("Group addMember: ").append(wsAddMemberResults.getWsGroupAssigned().getName());
      report.append(" - ").append(wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode());
      if (disabledDate != null) {
        report.append(", disabledDate: ").append(disabledDate);
      }
      if (enabledDate != null) {
        report.append(", enabledDate: ").append(disabledDate);
      }
      
      report.append("\n\n");
    }
  }

  /**
   * @param grouperKimSaveMembershipProperties
   * @param root
   * @param xpath
   * @return roleName
   */
  private static String processPermissionRoleName(
      GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties,
      Element root, XPath xpath) {
    String roleName = null;
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getRoleForPermissions())) {
      roleName = grouperKimSaveMembershipProperties.getRoleForPermissions();
    }
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldRoleForPermissions())) {
      //this xpath will give the value element for the role to use in the assignment
      roleName = xpathValue(xpath, root, grouperKimSaveMembershipProperties.getEdocliteFieldRoleForPermissions());

      //see if what was entered is allowed
      if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getAllowedRolesForPermissions())) {
        
        List<String> allowedRolesForPermissions = GrouperClientUtils.splitTrimToList(grouperKimSaveMembershipProperties
            .getAllowedRolesForPermissions(), ",");
        if (!allowedRolesForPermissions.contains(roleName)) {
          throw new RuntimeException("Role is not in list of allowed roles: " 
              + roleName + ", allowed: " + grouperKimSaveMembershipProperties.getAllowedRolesForPermissions());
        }
      }
      
    }
    if (GrouperClientUtils.isBlank(roleName)) {
      //this means problem, cant assign permissions without a role
      throw new RuntimeException("Problem finding role in document: " + grouperKimSaveMembershipProperties.getEdocliteFieldRoleForPermissions());
    }
    return roleName;
  }

  /**
   * @param grouperKimSaveMembershipProperties
   * @param root
   * @param xpath
   * @return operation
   */
  private static String processPermissionOperation(
      GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties,
      Element root, XPath xpath) {
    String operation = grouperKimSaveMembershipProperties.getOperationForPermissions();
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldOperationForPermissions())) {
      
      //this xpath will give the value element for the operation to use in the assignment
      operation = xpathValue(xpath, root, grouperKimSaveMembershipProperties.getEdocliteFieldOperationForPermissions());

      //see if what was entered is allowed
      if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getAllowedOperationsForPermissions())) {
        
        List<String> allowedOperationsForPermissions = GrouperClientUtils.splitTrimToList(grouperKimSaveMembershipProperties
            .getAllowedOperationsForPermissions(), ",");
        if (!allowedOperationsForPermissions.contains(operation)) {
          throw new RuntimeException("Operation is not in list of allowed operations: " 
              + operation + ", allowed: " + grouperKimSaveMembershipProperties.getAllowedOperationsForPermissions());
        }
      }
      
      
    }
    return operation;
  }

  /**
   * @param grouperKimSaveMembershipProperties
   * @param root
   * @param xpath
   * @return permissions
   */
  private static Set<String> processPermissionNames(
      GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties,
      Element root, XPath xpath) {
    
    Set<String> permissionNames = new HashSet<String>();
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getPermissions())) {
      permissionNames.addAll(GrouperClientUtils.splitTrimToList(grouperKimSaveMembershipProperties.getPermissions(), ","));
    }
    
    if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldPrefixForPermissions())) {
      
      String edocLiteFieldForPermissions = grouperKimSaveMembershipProperties.getEdocliteFieldPrefixForPermissions();
      
      List<String> allowedPermissions = null;
      if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getAllowedPermissions())) {
        allowedPermissions = GrouperClientUtils.splitTrimToList(grouperKimSaveMembershipProperties.getAllowedPermissions(), ",");
      }

      String permissionRegex = grouperKimSaveMembershipProperties.getPermissionsRegex();
      
      String permissionNamePrefix = grouperKimSaveMembershipProperties.getEnteredPermissionNamePrefix();
      
      for (int i=0;i<200;i++) {

        //this xpath will give the value element for the operation to use in the assignment
        String permissionName = xpathValue(xpath, root,edocLiteFieldForPermissions);
        
        if (GrouperClientUtils.isBlank(permissionName)) {
          continue;
        }
        
        permissionName = GrouperClientUtils.trim(permissionName);
        
        //see if not in the list, if there is a list
        if (GrouperClientUtils.length(allowedPermissions) > 0) {
          if (!allowedPermissions.contains(permissionName)) {
            throw new RuntimeException("Not allowed to access permissionName: '" + permissionName + "'");
          }
        }
        
        //see if allowed by regex
        if (!GrouperClientUtils.isBlank(permissionRegex) && !permissionName.matches(permissionRegex)) {
          throw new RuntimeException("Not allowed to access permissionName: '" + permissionName + "', due to regex: '" + permissionRegex + "'");
        }
        
        //prepend prefix if applicable
        if (!GrouperClientUtils.isBlank(permissionNamePrefix)) {
          permissionName = permissionNamePrefix + permissionName;
        }
        
        permissionNames.add(permissionName);
      }
    }
    return permissionNames;
  }

  /**
   * @param sendingEmail
   * @param report
   * @param wsSubject
   * @param roleNameToAdd 
   * @param operation 
   * @param actions 
   * @param permissionNames 
   * @param enabledDate 
   * @param disabledDate 
   */
  private static void addMemberPermissions(boolean sendingEmail, StringBuilder report,
      WsSubject wsSubject, String roleNameToAdd, String operation, Set<String> actions, 
      Set<String> permissionNames, Date enabledDate, Date disabledDate) {
    String roleName = roleNameToAdd.contains(":") ? roleNameToAdd : null;
    String roleId = roleNameToAdd.contains(":") ? null : roleNameToAdd;

    WsMembershipAnyLookup wsMembershipAnyLookup = new WsMembershipAnyLookup();
    wsMembershipAnyLookup.setWsGroupLookup(new WsGroupLookup(roleName, roleId));
    wsMembershipAnyLookup.setWsSubjectLookup(new WsSubjectLookup(wsSubject.getId(), wsSubject.getSourceId(), null));

    GcAssignPermissions gcAssignPermissions = new GcAssignPermissions().addSubjectRoleLookup(wsMembershipAnyLookup);
    
    for (String action : GrouperClientUtils.nonNull(actions)) {
      gcAssignPermissions.addAction(action);
    }
    
    gcAssignPermissions.assignPermissionAssignOperation(operation);

    for (String permissionName : permissionNames) {
      gcAssignPermissions.addPermissionDefNameName(permissionName);
    }
    gcAssignPermissions.assignPermissionType("role_subject");
    
    //TODO start/end date on group (above), start/end date on permissions, delegatable
//    WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
    
//    // based on result, add report
//    if (sendingEmail) {
//      report.append("Group addMember: ").append(wsAddMemberResults.getWsGroupAssigned().getName());
//      report.append(" - ").append(wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode()).append("\n\n");
//    }
  }
}
