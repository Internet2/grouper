package edu.internet2.middleware.grouper.app.workflow;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowEmailService {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowEmailService.class);
  
  
  /**
   * send approve, reject email
   * @param subjectKey
   * @param bodyKey
   * @param workflowConfig
   * @param subjects
   */
  public static void sendApproveRejectEmail(String subjectKey, String bodyKey, 
      GrouperWorkflowConfig workflowConfig, Set<Subject> subjects) {
    
    String subject = GrouperTextContainer.retrieveFromRequest().getText().get(subjectKey);
    String body = GrouperTextContainer.retrieveFromRequest().getText().get(bodyKey);
    String appNameShort = GrouperTextContainer.retrieveFromRequest().getText().get("app.name.short");
    
    subject = subject.replace("$$app.name.short$$", appNameShort);
    subject = subject.replace("$$formName$$", workflowConfig.getWorkflowConfigName());
    
    body = body.replace("$$app.name.short$$", appNameShort);
    body = body.replace("$$formName$$", workflowConfig.getWorkflowConfigName());
    body = body.replace("$$formDescripton$$", workflowConfig.getWorkflowConfigDescription());
    
    for (Subject sub: subjects) {
      body = body.replace("$$subjectName$$", sub.getName());
      String emailAddress = GrouperEmailUtils.getEmail(sub);
      if (StringUtils.isNotBlank(emailAddress)) {
        new GrouperEmail().setBody(body.toString()).setSubject(subject).setTo(emailAddress).send();
      } else {
        LOG.warn("For workflow config id: "+workflowConfig.getWorkflowConfigId()+" Subject with id: "+sub.getId()+" does not have an email address.");
      }
    }
    
  }
  
  /**
   * send waiting for approval email
   * @param addressObjects
   */
  public static void sendWaitingForApprovalEmail(Map<Subject, Set<GrouperWorkflowInstance>> addressObjects) {
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
      if (StringUtils.isBlank(emailAddress)) {
        LOG.warn(" Subject with id: "+subject.getId()+" does not have an email address.");
      } else {
        new GrouperEmail().setBody(waitingEmailBody.toString()).setSubject(waitingForApprovalSubject).setTo(emailAddress).send();
      }
      
    }
  }

}
