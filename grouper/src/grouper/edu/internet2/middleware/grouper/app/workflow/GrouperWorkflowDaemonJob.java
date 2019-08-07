package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.REJECTED_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.ADD_SUBJECT_TO_GROUP_ACTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_ACTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.WORKFLOW_STATE_CHANGE_ACTION;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperWorkflowDaemonJob extends OtherJobBase {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowDaemonJob.class);
  
  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_workflowDaemom";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperWorkflowDaemonJob().run(otherJobInput);
  }

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    GrouperSession session = GrouperSession.startRootSession();
    Set<Group> groupsWithWorkflowInstance = GrouperWorkflowInstanceService.findGroupsWithWorkflowInstance();
    Set<GrouperWorkflowInstance> instancesNeedingEmail = instancesNeedingEmail(groupsWithWorkflowInstance);
    
    updateInstances(instancesNeedingEmail, session);
    
    return null;
  }
  
  private Set<GrouperWorkflowInstance> instancesNeedingEmail(Set<Group> groups) {
    
    Set<GrouperWorkflowInstance> instancesNeedingEmail = new HashSet<GrouperWorkflowInstance>();
    List<String> statesToIgnore = Arrays.asList(EXCEPTION_STATE);
    
    for (Group group: groups) {
      List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: instances) {
        if (statesToIgnore.contains(instance.getWorkflowInstanceState())) {
          continue;
        }
        
        // if last email state is same as the state this instance is in, no need to do anything
        // basically, we are still waiting for somebody to take action or the requester subject has already
        // been added to the group
        String lastEmailedState = instance.getWorkflowInstanceLastEmailedState();
        String currentState = instance.getWorkflowInstanceState();
        if (StringUtils.isBlank(lastEmailedState) || !lastEmailedState.equals(currentState)) {
          instancesNeedingEmail.add(instance);
        }
      }
      
    }
    
    return instancesNeedingEmail;
    
  }
  
  private boolean addSubjectToGroup(GrouperWorkflowInstance instance, Subject subject,
      GrouperSession grouperSession) {
    
    GrouperWorkflowConfig parentWorkflowConfig = instance.getGrouperWorkflowConfig();
    GrouperWorkflowApprovalState completeState = 
        parentWorkflowConfig.getWorkflowApprovalStates().getStateByName(COMPLETE_STATE);
    boolean assignToGroupActionExists = false;
    List<GrouperWorkflowApprovalAction> actions = completeState.getActions() != null? completeState.getActions(): new ArrayList<GrouperWorkflowApprovalAction>();
    for (GrouperWorkflowApprovalAction action: actions) {
      String actionName = action.getActionName();
      String groupId = action.getActionArg0();
      if (actionName.equals("assignToGroup")) {
        assignToGroupActionExists = true;
        Group assignToGroup = GroupFinder.findByUuid(grouperSession, groupId, false);
        if (assignToGroup == null) {
          assignToGroup = GroupFinder.findByName(grouperSession, groupId, false);
        }
        if (assignToGroup == null) {
          LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" assignToGroup group not found. Group id is "+groupId);
          instance.setWorkflowInstanceState(EXCEPTION_STATE);
          instance.setWorkflowInstanceError("assignToGroup group id: "+groupId+" not found. It might have been deleted.");
          break;
        }
        
        assignToGroup.addMember(subject, false);
        return true;
      }
    }
    
    // just add member to group of which the config is hanging off
    if (!assignToGroupActionExists) {
      parentWorkflowConfig.getOwnerGroup().addMember(subject, false);
      return true;
    }
    
    return false;
    
  }
  
  private void updateInstances(Set<GrouperWorkflowInstance> instancesNeedingEmail, GrouperSession grouperSession) {
    
    Map<Subject, Set<GrouperWorkflowInstance>> addressObjects = new HashMap<Subject, Set<GrouperWorkflowInstance>>();
    
    for (GrouperWorkflowInstance instance: instancesNeedingEmail) {
      
      String currentState = instance.getWorkflowInstanceState();
      GrouperWorkflowConfig parentWorkflowConfig = instance.getGrouperWorkflowConfig(); 
        
      if (currentState.equals(COMPLETE_STATE)) {
        Subject subjectToAdd = GrouperWorkflowInstanceService.subjectWhoInitiatedWorkflow(instance);
        
        if (subjectToAdd == null) {
          LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists.");
          instance.setWorkflowInstanceState(EXCEPTION_STATE);
          instance.setWorkflowInstanceError("subject that requested to join the group no longer exists.");
        } else {
          boolean addedSubjectToGroup = addSubjectToGroup(instance, subjectToAdd, grouperSession);
              
          if (addedSubjectToGroup) {
            
            Set<Subject> subjectsApproved = new HashSet<Subject>();
            subjectsApproved.add(subjectToAdd);
            GrouperWorkflowEmailService.sendApproveRejectEmail("workflowRequestApprovedSubject", 
                "workflowRequestApprovedBody", parentWorkflowConfig, subjectsApproved);
            
            instance.setWorkflowInstanceLastUpdatedMillisSince1970(new Date().getTime());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String currentDate = dateFormat.format(new Date());
            instance.setWorkflowInstanceLastEmailedDate(currentDate);
            instance.setWorkflowInstanceLastEmailedState(COMPLETE_STATE);
            
            GrouperWorkflowInstanceLogEntry logEntry = GrouperWorkflowInstanceLogEntry.createLogEntry(subjectToAdd, new Date(), 
                COMPLETE_STATE, ADD_SUBJECT_TO_GROUP_ACTION);
            instance.getGrouperWorkflowInstanceLogEntries().getLogEntries().add(logEntry);
          }
        }
            
      } else if (currentState.equals(REJECTED_STATE)) {
        
        GrouperWorkflowInstanceLogEntry workflowInstanceLogEntry = instance.getGrouperWorkflowInstanceLogEntries()
            .getLogEntryByActionName(INITIATE_ACTION);
          
          String subjetWhoInitiated = workflowInstanceLogEntry.getSubjectId();
          Subject subjectRejected = SubjectFinder.findById(subjetWhoInitiated, false);
          if (subjectRejected == null) {
            LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
            instance.setWorkflowInstanceState(EXCEPTION_STATE);
            instance.setWorkflowInstanceError("subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
          } else {
            
            Set<Subject> subjectsRejected = new HashSet<Subject>();
            subjectsRejected.add(subjectRejected);
            GrouperWorkflowEmailService.sendApproveRejectEmail("workflowRequestRejectedSubject", "workflowRequestRejectedBody", parentWorkflowConfig, subjectsRejected);
            
            instance.setWorkflowInstanceLastUpdatedMillisSince1970(new Date().getTime());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String currentDate = dateFormat.format(new Date());
            instance.setWorkflowInstanceLastEmailedDate(currentDate);
            instance.setWorkflowInstanceLastEmailedState(REJECTED_STATE);
            
          }
        
      } else {
        GrouperWorkflowApprovalStates approvalStates = parentWorkflowConfig.getWorkflowApprovalStates();
        GrouperWorkflowApprovalState nextState = approvalStates.stateAfter(currentState);
        instance.setWorkflowInstanceState(nextState.getStateName());
        instance.setWorkflowInstanceLastUpdatedMillisSince1970(new Date().getTime());
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = dateFormat.format(new Date());
        instance.setWorkflowInstanceLastEmailedDate(currentDate);
        instance.setWorkflowInstanceLastEmailedState(nextState.getStateName());
        
        GrouperWorkflowInstanceLogEntry logEntry = GrouperWorkflowInstanceLogEntry.createLogEntry(null, new Date(), nextState.getStateName(), WORKFLOW_STATE_CHANGE_ACTION);
        instance.getGrouperWorkflowInstanceLogEntries().getLogEntries().add(logEntry);
        
        if (!parentWorkflowConfig.isWorkflowConfigSendEmail()) {
          continue;
        }
        
        List<Subject> approvers = GrouperWorkflowInstanceService.getApprovers(nextState);
        if (approvers.size() == 0) {
          LOG.error("No approvers found for workflow config id: "+parentWorkflowConfig.getWorkflowConfigId());
        }
        for (Subject approverSubject: approvers) {
          if (addressObjects.containsKey(approverSubject)) {           
            addressObjects.get(approverSubject).add(instance);
          } else {
            Set<GrouperWorkflowInstance> instances = new HashSet<>();
            instances.add(instance);
            addressObjects.put(approverSubject, instances);
          }
        }
        
      }
      
      GrouperWorkflowInstanceService.saveOrUpdateWorkflowInstance(instance, instance.getOwnerGrouperObject());
          
    }
    
    GrouperWorkflowEmailService.sendWaitingForApprovalEmail(addressObjects);
    
  }
  
}