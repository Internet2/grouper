package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperWorkflowDaemonJob implements Job {

  @Override
  public void execute(JobExecutionContext arg0) throws JobExecutionException {
    // TODO Auto-generated method stub
    
  }
  
  
  //TODO call function in instance service
  private Set<Group> findGroupsWithWorkflowInstance() {
    
    if (!GrouperWorkflowSettings.workflowEnabled()) {
      return new HashSet<Group>();
    }
    
    Set<Group> groups = new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
      .assignNameOfAttributeDefName(workflowStemName()+":"+GROUPER_WORKFLOW_INSTANCE_STATE)
      .findGroups();
    
    return groups;
  }
  
  
  private void filterGroupsNeedingEmail(Set<Group> groups) {
    
    Set<Group> groupsNeedingEmail = new HashSet<Group>();
    
    for (Group group: groups) {
      
      List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: instances) {
        
        // if last email state is same as the state this instance is in, no need to do anything
        // basically, we are still waiting for somebody to take action
        String lastEmailedState = instance.getWorkflowInstanceLastEmailedState();
        String currentState = instance.getWorkflowInstanceState();
        if (!lastEmailedState.equals(currentState)) {
          GrouperWorkflowApprovalStates approvalStates = instance.getGrouperWorkflowConfig().getWorkflowApprovalStates();
          GrouperWorkflowApprovalState nextState = approvalStates.stateAfter(currentState);
          
          //TODO send email to next state people
          
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
          GrouperWorkflowInstanceService.saveWorkflowInstanceAttributes(instance, group);
        }
      }
      
    }
    
  }
  
  

}
