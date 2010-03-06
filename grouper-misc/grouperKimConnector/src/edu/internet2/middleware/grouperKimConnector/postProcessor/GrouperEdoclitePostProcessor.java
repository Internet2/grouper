/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.postProcessor;

import org.kuali.rice.kew.edl.EDocLitePostProcessor;
import org.kuali.rice.kew.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kew.postprocessor.ProcessDocReport;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.KIMServiceLocator;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.grouperClientMail.GrouperClientEmail;
import edu.internet2.middleware.grouperKimConnector.identity.GrouperKimSaveMembershipProperties;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimServiceUtils;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimSubject;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * provision groups
 */
public class GrouperEdoclitePostProcessor extends EDocLitePostProcessor {

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
   * @param event
   * @param processDocReport
   */
  public static void doRouteStatusChangeHelper(DocumentRouteStatusChange event,
      ProcessDocReport processDocReport) {
    //if not an exception
    if (processDocReport.isSuccess()) {
      
      //if going to final state
      if (GrouperClientUtils.equals(KEWConstants.ROUTE_HEADER_FINAL_CD, event.getNewRouteStatus())) {
        
        DocumentRouteHeaderValue documentRouteHeaderValue = KEWServiceLocator
          .getRouteHeaderService().getRouteHeader(event.getRouteHeaderId());
        String docTypeName = documentRouteHeaderValue.getDocumentType().getName();
        
        GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties = 
          GrouperKimSaveMembershipProperties.grouperKimSaveMembershipProperties(docTypeName);
        
        if (grouperKimSaveMembershipProperties != null) {
          
          Person person = KIMServiceLocator.getPersonService().getPerson(documentRouteHeaderValue.getInitiatorWorkflowId());
          String entityId = person.getEntityId();
          boolean sendingEmail = !GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEmailAdmins());
          
          GrouperKimSubject grouperKimSubject = GrouperKimServiceUtils.convertEntityIdToSubject(entityId);
          
          WsSubject wsSubject = grouperKimSubject.getWsSubject();
          String[] attributeNames = grouperKimSubject.getSubjectAttributeNames();
          StringBuilder report = new StringBuilder();
          
          //add subject info to email report
          if (sendingEmail) {
            report.append(GrouperKimUtils.convertWsSubjectToStringForLog(wsSubject, attributeNames));
            report.append("\n\n");
          }
          

          if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getAddMembershipToGroups())) {

            String[] groupsToAdd = GrouperClientUtils.splitTrim(grouperKimSaveMembershipProperties.getAddMembershipToGroups(), ",");
            for (String groupToAdd : groupsToAdd) {
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
              WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
              
              // based on result, add report
              if (sendingEmail) {
                report.append("Group addMember: ").append(wsAddMemberResults.getWsGroupAssigned().getName());
                report.append(" - ").append(wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode()).append("\n\n");
              }
              
            }
          }
          
          if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getRemoveMembershipFromGroups())) {

            String[] groupsToRemove = GrouperClientUtils.splitTrim(grouperKimSaveMembershipProperties.getRemoveMembershipFromGroups(), ",");
            for (String groupToRemove : groupsToRemove) {
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
          }
          
          if (sendingEmail) {
            new GrouperClientEmail().setBody(report.toString()).setSubject("Grouper Rice auto-provision for document: " + docTypeName)
              .setTo(grouperKimSaveMembershipProperties.getEmailAdmins()).send();
          }
          
        }
        
      }
    }
  }

  
  
}
