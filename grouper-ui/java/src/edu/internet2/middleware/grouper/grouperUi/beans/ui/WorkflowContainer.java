package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceService;
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
  
  private List<GuiGrouperWorkflowInstance> workflowInstances = new ArrayList<GuiGrouperWorkflowInstance>();
  
  /**
   * instance user is viewing
   */
  private GuiGrouperWorkflowInstance workflowInstance;
  
  
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
    
    final Group group = UiV2Group.retrieveGroupHelper(GrouperUiFilter.retrieveHttpServletRequest(), AccessPrivilege.ADMIN, false).getGroup();
   
    Boolean loggedInSubjectCanConfigureWorkflow = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, loggedInSubject);
      }
        
    });
      
    return loggedInSubjectCanConfigureWorkflow;
    
  }
  
  /**
   * can logged in use view electronic forms menu item
   * @return
   */
  public boolean isCanViewElectronicForm() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    final Group group = UiV2Group.retrieveGroupHelper(GrouperUiFilter.retrieveHttpServletRequest(), AccessPrivilege.ADMIN, false).getGroup();
   
    Boolean loggedInSubjectCanViewWorkflow = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GrouperWorkflowConfigService.canSubjectViewWorkflow(group, loggedInSubject);
      }
        
    });
      
    return loggedInSubjectCanViewWorkflow;
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
    return htmlForm;
  }
  
  public void setHtmlForm(String htmlForm) {
    this.htmlForm = htmlForm;
  }

  /**
   * can logged in user edit workflow html form field
   * @return
   */
  public boolean isCanEditWorkflowFormField() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    Boolean canEditHtmlFormField = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group group = GroupFinder.findByName(grouperSession, GrouperWorkflowSettings.workflowEditorsGroup(), false);
        if (group == null) {
          group = GroupFinder.findByUuid(grouperSession, GrouperWorkflowSettings.workflowEditorsGroup(), false);
        }
        if (group != null && group.hasMember(loggedInSubject)) {
          return true;
        }
        return false;
      }
    });
    
    return canEditHtmlFormField;
    
  }


  public List<GuiGrouperWorkflowInstance> getWorkflowInstances() {
    return workflowInstances;
  }

  public void setWorkflowInstances(List<GuiGrouperWorkflowInstance> workflowInstances) {
    this.workflowInstances = workflowInstances;
  }

  public GuiGrouperWorkflowInstance getWorkflowInstance() {
    return workflowInstance;
  }

  public void setWorkflowInstance(GuiGrouperWorkflowInstance workflowInstance) {
    this.workflowInstance = workflowInstance;
  }

  /**
   * can logged in user approve or disapprove the instance
   * @return
   */
  public boolean isCanApproveDisapprove() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    boolean canApproveDisApprove = (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            return GrouperWorkflowInstanceService.canInstanceBeApproved(workflowInstance.getGrouperWorkflowInstance(), loggedInSubject);
          }
    });
    return canApproveDisApprove;
  }
  
}
