/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.postProcessor;

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
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.grouperClientMail.GrouperClientEmail;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.identity.GrouperKimSaveMembershipProperties;
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

            if (!GrouperClientUtils.isBlank(grouperKimSaveMembershipProperties.getEdocliteFieldPrefix())) {
              
              String eDocliteFieldPrefix = grouperKimSaveMembershipProperties.getEdocliteFieldPrefix();
              
              OUTER200: for (int i=0;i<200;i++) {
                
                String groupName = null;
                try {
                  //this xpath will give the value element for the field with name rightBranchCheckbox
                  //in the version tag where current is true
                  groupName = xpath.evaluate(
                      "/documentContent/applicationContent/data/version[@current = \"true\"]" +
                      "/field[@name = \"" + eDocliteFieldPrefix + i + "\"]/value", root);

                } catch (NullPointerException npe) {
                  //ignore this
                } catch (XPathException xpe) {
                  //this means we are probably done here
                  break OUTER200;
                }

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
                
                GcAddMember gcAddMember = new GcAddMember().addSubjectLookup(
                    new WsSubjectLookup(wsSubject.getId(), wsSubject.getSourceId(), null));
                gcAddMember.assignGroupName(groupName);
                WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
                
                // based on result, add report
                if (sendingEmail) {
                  report.append("Group addMember: ").append(wsAddMemberResults.getWsGroupAssigned().getName());
                  report.append(" - ").append(wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode()).append("\n\n");
                }

                
              }
              
              
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

  
  
}
