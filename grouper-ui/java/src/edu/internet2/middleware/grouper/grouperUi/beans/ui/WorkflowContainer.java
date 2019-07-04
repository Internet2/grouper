package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstance;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Group;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class WorkflowContainer {
  
  /**
   * config user is currently working on
   */
  private GuiGrouperWorkflowConfig guiGrouperWorkflowConfig;
  
  /**
   * gui workflow configs for a group
   */
  private List<GuiGrouperWorkflowConfig> guiWorkflowConfigs = new ArrayList<GuiGrouperWorkflowConfig>();
  
  /**
   * list of errors when add/edit form is submitted 
   */
  private List<String> errors = new ArrayList<String>();
  
  private List<GrouperWorkflowInstance> instancesWaitingForApproval = new ArrayList<GrouperWorkflowInstance>();
  
  /**
   * instance user is viewing
   */
  private GrouperWorkflowInstance workflowInstance;
  
  
  /**
   * html form to store/show
   */
  private String htmlForm;
  
  /**
   * can logged in user configure workflow
   * @return
   */
  public boolean isCanConfigureWorkflow() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    Boolean loggedInSubjectMemberOfWorkflowEditor = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group group = GroupFinder.findByName(GrouperSession.startRootSession(), GrouperWorkflowSettings.workflowEditorsGroup(), true);
        if (group.hasMember(loggedInSubject)) {
          return true;
        }
        return false;
      }
    });
    
    if (loggedInSubjectMemberOfWorkflowEditor) {
      return true;
    }
    
    Group group = UiV2Group.retrieveGroupHelper(GrouperUiFilter.retrieveHttpServletRequest(), AccessPrivilege.ADMIN, false).getGroup();
    
    return group != null;
    
  }

  /**
   * 
   * @return gui workflow configs for a group
   */
  public List<GuiGrouperWorkflowConfig> getGuiWorkflowConfigs() {
    return guiWorkflowConfigs;
  }

  /**
   * gui workflow configs for a group
   * @param guiWorkflowConfigs
   */
  public void setGuiWorkflowConfigs(List<GuiGrouperWorkflowConfig> guiWorkflowConfigs) {
    this.guiWorkflowConfigs = guiWorkflowConfigs;
  }

  public GuiGrouperWorkflowConfig getGuiGrouperWorkflowConfig() {
    return guiGrouperWorkflowConfig;
  }

  public void setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig guiGrouperWorkflowConfig) {
    this.guiGrouperWorkflowConfig = guiGrouperWorkflowConfig;
  }

  /**
   * 
   * @return all the configured types
   */
  public List<String> getAllConfigTypes() {
    return GrouperWorkflowSettings.configTypes();
  }

  /**
   * 
   * @return list of errors when add/edit form is submitted
   */
  public List<String> getErrors() {
    return errors;
  }

  /**
   * list of errors when add/edit form is submitted
   * @param errors
   */
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
  
  /**
   *
   * @return html form to show when a group membership is requested or when approver goes to approve
   */
  public String getHtmlForm() {
//    GrouperWorkflowConfig workflowConfig = guiGrouperWorkflowConfig.getGrouperWorkflowConfig();
//    if (StringUtils.isBlank(workflowConfig.getWorkflowConfigForm())) {      
//      return workflowConfig.buildHtmlFromParams(false);
//    }
//    return workflowConfig.buildHtmlFromParams(false);
//    //return workflowConfig.getWorkflowConfigForm();
    return htmlForm;
  }
  
  public void setHtmlForm(String htmlForm) {
    this.htmlForm = htmlForm;
  }

  public boolean isCanEditWorkflowFormField() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    Group group = GroupFinder.findByName(GrouperSession.startRootSession(), GrouperWorkflowSettings.workflowEditorsGroup(), true);
    if (group.hasMember(loggedInSubject)) {
      return true;
    }
    
    return false;
    
  }

  public List<GrouperWorkflowInstance> getInstancesWaitingForApproval() {
    return instancesWaitingForApproval;
  }

  public void setInstancesWaitingForApproval(List<GrouperWorkflowInstance> instancesWaitingForApproval) {
    this.instancesWaitingForApproval = instancesWaitingForApproval;
  }

  public GrouperWorkflowInstance getWorkflowInstance() {
    return workflowInstance;
  }

  public void setWorkflowInstance(GrouperWorkflowInstance workflowInstance) {
    this.workflowInstance = workflowInstance;
  }
  
}
