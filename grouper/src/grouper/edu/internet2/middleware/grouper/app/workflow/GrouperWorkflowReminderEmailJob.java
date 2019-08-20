package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.REJECTED_STATE;

import java.text.DateFormat;
import java.text.ParseException;
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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperWorkflowReminderEmailJob extends OtherJobBase {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowReminderEmailJob.class);
  
  /**
   * run reminder job
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
    String jobName = "OTHER_JOB_workflowEmailReminder";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperWorkflowReminderEmailJob().run(otherJobInput);
  }

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {

    GrouperSession.startRootSession();
    Set<Group> groupsWithWorkflowInstance = GrouperWorkflowInstanceService.findGroupsWithWorkflowInstance();
    
    Set<GrouperWorkflowInstance> instancesNeedingEmail = instancesNeedingEmail(groupsWithWorkflowInstance);
    
    Map<Subject, Set<GrouperWorkflowInstance>> addressObjects = buildEmailObjects(instancesNeedingEmail);
    
    GrouperWorkflowEmailService.sendWaitingForApprovalEmail(addressObjects);
    
    return null;
  }
  
  private Set<GrouperWorkflowInstance> instancesNeedingEmail(Set<Group> groups) {
    
    Set<GrouperWorkflowInstance> instancesNeedingEmail = new HashSet<GrouperWorkflowInstance>();
    List<String> statesToIgnore = Arrays.asList(EXCEPTION_STATE, COMPLETE_STATE, INITIATE_STATE, REJECTED_STATE);
    
    for (Group group: groups) {
      List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: instances) {
        
        if (statesToIgnore.contains(instance.getWorkflowInstanceState()) || 
            !instance.getGrouperWorkflowConfig().isWorkflowConfigSendEmail() ) {
          continue;
        }
        
        // if last email state is same as the state this instance is in and last emailed date
        // is not null , send another reminder email
        String lastEmailedState = instance.getWorkflowInstanceLastEmailedState();
        String currentState = instance.getWorkflowInstanceState();
        String lastEmailDateString = instance.getWorkflowInstanceLastEmailedDate();
        
        if (StringUtils.isNotBlank(lastEmailedState) && lastEmailedState.equals(currentState)
            && StringUtils.isNotBlank(lastEmailDateString)) {
          
          DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
          try {
            Date dateLastEmailSent = dateFormat.parse(lastEmailDateString);
            Long millisNow = new Date().getTime();
            Long millisLastSentEmail = dateLastEmailSent.getTime();
            if ( (millisNow - millisLastSentEmail) > 23 * 60 * 60 * 1000) {
              instancesNeedingEmail.add(instance);
            }
          } catch (ParseException e) {
            LOG.error("For workflow instance id: "+instance.getWorkflowInstanceUuid()+" error parsing "+lastEmailDateString+" into date object");
          }          
        }
      }
      
    }
    
    return instancesNeedingEmail;
    
  }
  
  private Map<Subject, Set<GrouperWorkflowInstance>> buildEmailObjects(Set<GrouperWorkflowInstance> instancesNeedingEmail) {
    
    Map<Subject, Set<GrouperWorkflowInstance>> addressObjects = new HashMap<Subject, Set<GrouperWorkflowInstance>>();
    
    for (GrouperWorkflowInstance instance: instancesNeedingEmail) {
      GrouperWorkflowConfig parentWorkflowConfig = instance.getGrouperWorkflowConfig();
      GrouperWorkflowApprovalState currentState = parentWorkflowConfig.getWorkflowApprovalStates().getStateByName(instance.getWorkflowInstanceState());
      List<Subject> approvers = GrouperWorkflowInstanceService.getApprovers(currentState);
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
    
    return addressObjects;
    
  }
  
}
