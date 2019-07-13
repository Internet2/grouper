package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.REJECTED_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceLogEntry.INITIATE_ACTION;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
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
    
    Map<Subject, Set<GrouperWorkflowInstance>> addressObjects = new HashMap<Subject, Set<GrouperWorkflowInstance>>();
    
    for (GrouperWorkflowInstance instance: instancesNeedingEmail) {
      
      String currentState = instance.getWorkflowInstanceState();
      GrouperWorkflowConfig parentWorkflowConfig = instance.getGrouperWorkflowConfig(); 
        
      if (currentState.equals(COMPLETE_STATE)) {
        GrouperWorkflowApprovalState completeState = parentWorkflowConfig.getWorkflowApprovalStates().getStateByName(COMPLETE_STATE);
        
        boolean addedSubjectToGroup = false;
        Subject subjectToAdd = null;
            
        for (GrouperWorkflowApprovalAction action: completeState.getActions()) {
          String actionName = action.getActionName();
          String groupId = action.getActionArg0();
          if (actionName.equals("assignToGroup")) {
            Group assignToGroup = GroupFinder.findByUuid(grouperSession, groupId, false);
            if (assignToGroup == null) {
              LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" assignToGroup group not found. Group id is "+groupId);
              instance.setWorkflowInstanceState(EXCEPTION_STATE);
              instance.setWorkflowInstanceError("assignToGroup group id: "+groupId+" not found. It might have been deleted.");
              //TODO maybe email admin of the group
              break;
            }
            
            GrouperWorkflowInstanceLogEntry workflowInstanceLogEntry = instance.getGrouperWorkflowInstanceLogEntries()
              .getLogEntryByActionName(INITIATE_ACTION);
            
            String subjetWhoInitiated = workflowInstanceLogEntry.getSubjectId();
            subjectToAdd = SubjectFinder.findById(subjetWhoInitiated, false);
            if (subjectToAdd == null) {
              LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
              instance.setWorkflowInstanceState(EXCEPTION_STATE);
              instance.setWorkflowInstanceError("subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
              //TODO maybe email admin of the group
              break;
            }
            assignToGroup.addMember(subjectToAdd);
            addedSubjectToGroup = true;
          }
        }
            
        if (addedSubjectToGroup && subjectToAdd != null) {
          
          String subject = GrouperTextContainer.retrieveFromRequest().getText().get("workflowRequestApprovedSubject");
          String body = GrouperTextContainer.retrieveFromRequest().getText().get("workflowRequestApprovedBody");
          String appNameShort = GrouperTextContainer.retrieveFromRequest().getText().get("app.name.short");
          
          if (StringUtils.isBlank(subject)) {
            subject = "$$app.name.short$$ electronic form: $$formName$$ - approved";
          }
          if (StringUtils.isBlank(body)) {
            body = "Dear $$subjectName$$, \\n\\n Your request has been approved in the $$app.name.short$$ electronic form: $$formName$$ - $$formDescripton$$. Your request is complete.";
          }
          
          subject = subject.replace("app.name.short", appNameShort);
          subject = subject.replace("$$formName$$", parentWorkflowConfig.getWorkflowConfigName());
          
          body = body.replace("$$subjectName", subjectToAdd.getName());
          body = body.replace("app.name.short", appNameShort);
          body = body.replace("$$formName$$", parentWorkflowConfig.getWorkflowConfigName());
          body = body.replace("$$formDescripton$$", parentWorkflowConfig.getWorkflowConfigDescription());
          
          String emailAddress = GrouperEmailUtils.getEmail(subjectToAdd);
          if (StringUtils.isNotBlank(emailAddress)) {
            new GrouperEmail().setBody(body.toString()).setSubject(subject).setTo(emailAddress).send();
          } else {
            LOG.warn("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" Subject with id: "+subjectToAdd.getId()+" does not have an email address.");
          }
          
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
        }
            
      } else if (currentState.equals(REJECTED_STATE)) {
        
        GrouperWorkflowInstanceLogEntry workflowInstanceLogEntry = instance.getGrouperWorkflowInstanceLogEntries()
            .getLogEntryByActionName(INITIATE_ACTION);
          
          String subjetWhoInitiated = workflowInstanceLogEntry.getSubjectId();
          Subject rejectedSubject = SubjectFinder.findById(subjetWhoInitiated, false);
          if (rejectedSubject == null) {
            LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
            instance.setWorkflowInstanceState(EXCEPTION_STATE);
            instance.setWorkflowInstanceError("subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiated);
          } else {
            String subject = GrouperTextContainer.retrieveFromRequest().getText().get("workflowRequestRejectedSubject");
            String body = GrouperTextContainer.retrieveFromRequest().getText().get("workflowRequestRejectedBody");
            String appNameShort = GrouperTextContainer.retrieveFromRequest().getText().get("app.name.short");
            
            if (StringUtils.isBlank(subject)) {
              subject = "$$app.name.short$$ electronic form: $$formName$$ - rejected";
            }
            if (StringUtils.isBlank(body)) {
              body = "Dear $$subjectName$$, \\n\\n Your request has been rejected in the $$app.name.short$$ electronic form: $$formName$$ - $$formDescripton$$. The people who disapproved will followup with you. \\n\\n Thanks!";
            }
            
            subject = subject.replace("app.name.short", appNameShort);
            subject = subject.replace("$$formName$$", parentWorkflowConfig.getWorkflowConfigName());
            
            // Dear $$subjectName$$, \n\n Your request has been rejected at step: $$workflowState$$ in the $$app.name.short$$ electronic form: $$formName$$ - $$formDescripton$$. The people who disapproved will followup with you. \n\n Thanks!
            body = body.replace("$$subjectName", rejectedSubject.getName());
            body = body.replace("app.name.short", appNameShort);
            body = body.replace("$$formName$$", parentWorkflowConfig.getWorkflowConfigName());
            body = body.replace("$$formDescripton$$", parentWorkflowConfig.getWorkflowConfigDescription());
            
            String emailAddress = GrouperEmailUtils.getEmail(rejectedSubject);
            if (StringUtils.isNotBlank(emailAddress)) {
              new GrouperEmail().setBody(body.toString()).setSubject(subject).setTo(emailAddress).send();
            } else {
              LOG.warn("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" Subject with id: "+rejectedSubject.getId()+" does not have an email address.");
            }
            
            instance.setWorkflowInstanceLastUpdatedMillisSince1970(new Date().getTime());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String currentDate = dateFormat.format(new Date());
            instance.setWorkflowInstanceLastEmailedDate(currentDate);
            instance.setWorkflowInstanceLastEmailedState(REJECTED_STATE);
            
            GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
            logEntry.setAction("rejectedSubject");
            logEntry.setState(REJECTED_STATE);
            logEntry.setSubjectId(rejectedSubject.getId());
            logEntry.setSubjectSourceId(rejectedSubject.getSourceId());
            logEntry.setMillisSince1970(new Date().getTime());
            instance.getGrouperWorkflowInstanceLogEntries().getLogEntries().add(logEntry);
          }
        
      } else {
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
        
        String approversManagerGroupId = nextState.getApproverManagersOfGroupId();
        if (StringUtils.isNotBlank(approversManagerGroupId)) {
          Group managersGroup = GroupFinder.findByUuid(grouperSession, approversManagerGroupId, false);
          if (managersGroup != null) {
            Set<Subject> admins = managersGroup.getAdmins();
            Set<Subject> updaters = managersGroup.getUpdaters();
            
            Set<Subject> subjectsReceivingEmailForThisInstance = new HashSet<Subject>();
            subjectsReceivingEmailForThisInstance.addAll(updaters);
            subjectsReceivingEmailForThisInstance.addAll(admins);
            
            for (Subject sub: subjectsReceivingEmailForThisInstance) {
              if (addressObjects.containsKey(sub)) {            
                addressObjects.get(sub).add(instance);
              } else {
                Set<GrouperWorkflowInstance> instances = new HashSet<>();
                instances.add(instance);
                addressObjects.put(sub, instances);
              }
            }
            
          } else {
            LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" approverManagersOfGroupId "+approversManagerGroupId+" does not exist");
          }
        }
        
        String approversGroupId = nextState.getApproverGroupId();
        if (StringUtils.isNotBlank(approversGroupId)) {
          Group approversGroup = GroupFinder.findByUuid(grouperSession, approversGroupId, false);
          if (approversGroup != null) {
            Set<Member> members = approversGroup.getMembers();
            
            for (Member member: members) {
              Subject sub = member.getSubject();
              if (addressObjects.containsKey(sub)) {                
                addressObjects.get(sub).add(instance);
              } else {
                Set<GrouperWorkflowInstance> instances = new HashSet<>();
                instances.add(instance);
                addressObjects.put(sub, instances);
              }
            }
            
          } else {
            LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" approversGroupId "+approversGroupId+" does not exist");
          }
        }
        
        String approverSubjectId = nextState.getApproverSubjectId();
        if (StringUtils.isNotBlank(approverSubjectId)) {
          Subject approverSubject = SubjectFinder.findById(approverSubjectId, false);
          if (approverSubject != null) {
            if (addressObjects.containsKey(approverSubject)) {                
              addressObjects.get(approverSubject).add(instance);
            } else {
              Set<GrouperWorkflowInstance> instances = new HashSet<>();
              instances.add(instance);
              addressObjects.put(approverSubject, instances);
            }
          } else {
            LOG.error("For workflow config id: "+parentWorkflowConfig.getWorkflowConfigId()+" approverSubjectId "+approverSubjectId+" does not exist");
          }
        }
        
        
      }
      
      GrouperWorkflowInstanceService.saveWorkflowInstanceAttributes(instance, instance.getOwnerGrouperObject());
          
    }
    
    
    String waitingForApprovalSubject = GrouperTextContainer.retrieveFromRequest().getText().get("workflowRequestWaitingForApprovalSubject");
    String waitingForApprovalBody = GrouperTextContainer.retrieveFromRequest().getText().get("workflowRequestWaitingForApprovaldBody");
    String appNameShort = GrouperTextContainer.retrieveFromRequest().getText().get("app.name.short");
    
    if (StringUtils.isBlank(waitingForApprovalSubject)) {
      waitingForApprovalSubject = "$$app.name.short$$ electronic forms - waiting for approval";
    }
    
    if (StringUtils.isBlank(waitingForApprovalBody)) {
      waitingForApprovalBody = "Dear $$subjectName$$, \\n\\n $$app.name.short$$ electronic forms waiting for your approval:";
    }
    
    waitingForApprovalSubject = waitingForApprovalSubject.replace("$$app.name.short$$", appNameShort);
    
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    
    if (StringUtils.isBlank(uiUrl)) {
      LOG.error("grouper.properties grouper.ui.url is blank/null. Please fix that first. No waiting for approval emails have been sent.");
      return;
    }
    
    for (Map.Entry<Subject, Set<GrouperWorkflowInstance>> entry: addressObjects.entrySet()) {
      Subject subject = entry.getKey();
      Set<GrouperWorkflowInstance> instances = entry.getValue();
      
      String waitingForApprovalBodyPerSubject = waitingForApprovalBody.replace("$$subjectName$$", subject.getName());
      waitingForApprovalBodyPerSubject = waitingForApprovalBodyPerSubject.replace("$$app.name.short$$", appNameShort);
      
      StringBuilder waitingEmailBody = new StringBuilder(waitingForApprovalBodyPerSubject);
      
      int index = 1;
      for (GrouperWorkflowInstance instance: instances) {
        GrouperWorkflowConfig parentConfig = instance.getGrouperWorkflowConfig();
        waitingEmailBody.append("\n\n");
        waitingEmailBody.append(String.valueOf(index));
        waitingEmailBody.append(". ");
        waitingEmailBody.append(parentConfig.getWorkflowConfigName());
        waitingEmailBody.append(" - ");
        waitingEmailBody.append(parentConfig.getWorkflowConfigDescription());
        waitingEmailBody.append("\n");
        waitingEmailBody.append(uiUrl);
        waitingEmailBody.append("grouperUi/app/UiV2Main.index?operation=UiV2GrouperWorkflow.viewInstance&attributeAssignId="+instance.getAttributeAssignId());
        index++;
      }
      
      String emailAddress = GrouperEmailUtils.getEmail(subject);
      emailAddress = "erviveksachdeva@gmail.com";
      if (StringUtils.isBlank(emailAddress)) {
        LOG.warn(" Subject with id: "+subject.getId()+" does not have an email address.");
      } else {
        new GrouperEmail().setBody(waitingEmailBody.toString()).setSubject(waitingForApprovalBodyPerSubject).setTo(emailAddress).send();
      }
      
    }
    
  }
  
}