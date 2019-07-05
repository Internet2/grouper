package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceLogEntry.INITIATE_ACTION;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    for (Group group: groups) {
      List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: instances) {
        if (instance.getWorkflowInstanceState().equals(EXCEPTION_STATE)) {
          continue;
        }
        
        // if last email state is same as the state this instance is in, no need to do anything
        // basically, we are still waiting for somebody to take action
        String lastEmailedState = instance.getWorkflowInstanceLastEmailedState();
        String currentState = instance.getWorkflowInstanceState();
        if (StringUtils.isBlank(lastEmailedState) || !lastEmailedState.equals(currentState)) {
          instancesNeedingEmail.add(instance);
        }
      }
      
    }
    
    return instancesNeedingEmail;
    
  }
  
  
  private void updateInstances(Set<GrouperWorkflowInstance> instancesNeedingEmail, GrouperSession grouperSession) {
    
    for (GrouperWorkflowInstance instance: instancesNeedingEmail) {
      
      String currentState = instance.getWorkflowInstanceState();
      GrouperWorkflowConfig parentWorkflowConfig = instance.getGrouperWorkflowConfig(); 
        
      if (currentState.equals(COMPLETE_STATE)) {
        GrouperWorkflowApprovalState completeState = parentWorkflowConfig.getWorkflowApprovalStates().getStateByName(COMPLETE_STATE);
        
        boolean addedSubjectToGroup = false;
        Subject subjectToAdd = null;
            
        for (GrouperWorkflowApprovalAction action: completeState.getActions()) {
          String actionName = action.getActionName();
          String arg = action.getActionArg0();
          if (actionName.equals("assignToGroup")) {
            Group assignToGroup = GroupFinder.findByUuid(grouperSession, arg, false);
            if (assignToGroup == null) {
              LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" assignToGroup group not found. Group id is "+arg);
              //TODO maybe email admin of the group
              continue;
            }
            
            GrouperWorkflowInstanceLogEntry workflowInstanceLogEntry = instance.getGrouperWorkflowInstanceLogEntries()
              .getLogEntryByActionName(INITIATE_ACTION);
            
            String subjetWhoInitiated = workflowInstanceLogEntry.getSubjectId();
            subjectToAdd = SubjectFinder.findById(subjetWhoInitiated, false);
            if (subjectToAdd == null) {
              LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
              //TODO maybe email admin of the group
              continue;
            }
            assignToGroup.addMember(subjectToAdd);
            addedSubjectToGroup = true;
          }
        }
            
        if (addedSubjectToGroup && subjectToAdd != null) {
          //TODO send email to people that they are in the group
          
          instance.setWorkflowInstanceLastUpdatedMillisSince1970(new Date().getTime());
          DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
          String currentDate = dateFormat.format(new Date());
          instance.setWorkflowInstanceLastEmailedDate(currentDate);
          instance.setWorkflowInstanceLastEmailedState(COMPLETE_STATE);
          
          GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
          logEntry.setAction("addedSubjectToGroup");
          logEntry.setState(COMPLETE_STATE);
          logEntry.setSubjectId(subjectToAdd.getId());
          logEntry.setSubjectSourceId(subjectToAdd.getSourceId());
          logEntry.setMillisSince1970(new Date().getTime());
          instance.getGrouperWorkflowInstanceLogEntries().getLogEntries().add(logEntry);
          GrouperWorkflowInstanceService.saveWorkflowInstanceAttributes(instance, instance.getOwnerGrouperObject());
        }
            
      } else {
        //TODO send email to next state people
        GrouperWorkflowApprovalStates approvalStates = instance.getGrouperWorkflowConfig().getWorkflowApprovalStates();
        GrouperWorkflowApprovalState nextState = approvalStates.stateAfter(currentState);
        instance.setWorkflowInstanceState(nextState.getStateName());
        instance.setWorkflowInstanceLastUpdatedMillisSince1970(new Date().getTime());
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = dateFormat.format(new Date());
        instance.setWorkflowInstanceLastEmailedDate(currentDate);
        instance.setWorkflowInstanceLastEmailedState(nextState.getStateName());
        
        GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
        logEntry.setAction("workflowStateChange");
        logEntry.setState(nextState.getStateName());
        logEntry.setMillisSince1970(new Date().getTime());
        instance.getGrouperWorkflowInstanceLogEntries().getLogEntries().add(logEntry);
        GrouperWorkflowInstanceService.saveWorkflowInstanceAttributes(instance, instance.getOwnerGrouperObject());
        }
          
    }
  }

}
