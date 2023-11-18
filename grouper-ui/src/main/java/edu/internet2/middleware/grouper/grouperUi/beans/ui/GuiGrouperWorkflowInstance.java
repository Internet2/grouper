package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;


public class GuiGrouperWorkflowInstance {
  
  /**
   * parent grouper workflow config
   */
  private GrouperWorkflowInstance grouperWorkflowInstance;
  
  /**
   * gui initiator subject
   */
  private GuiSubject guiInitiatorSubject;
  
  private GuiGrouperWorkflowInstance(GrouperWorkflowInstance grouperWorkflowInstance) {
    this.grouperWorkflowInstance = grouperWorkflowInstance;
  }

  public GrouperWorkflowInstance getGrouperWorkflowInstance() {
    return grouperWorkflowInstance;
  }

  /**
   * convert from grouper workflow instance to gui grouper workflow instance
   * @param grouperWorkflowInstance
   * @return
   */
  public static GuiGrouperWorkflowInstance convertFromGrouperWorkflowInstance(GrouperWorkflowInstance grouperWorkflowInstance) {
    GuiGrouperWorkflowInstance guiGrouperWorkflowInstance = new GuiGrouperWorkflowInstance(grouperWorkflowInstance);
    guiGrouperWorkflowInstance.guiInitiatorSubject = new GuiSubject(grouperWorkflowInstance.getInitiatorSubject());
    return guiGrouperWorkflowInstance;
  }
  
  /**
   * convert from list of configs to list of gui configs
   * @param grouperWorkflowConfigs
   * @return
   */
  public static List<GuiGrouperWorkflowInstance> convertFromGrouperWorkflowInstances(List<GrouperWorkflowInstance> grouperWorkflowInstances) {
    
    List<GuiGrouperWorkflowInstance> result = new ArrayList<GuiGrouperWorkflowInstance>();
    
    for (GrouperWorkflowInstance workflowInstance: grouperWorkflowInstances) {
      result.add(convertFromGrouperWorkflowInstance(workflowInstance));
    }
    
    return result;
  }

  public GuiSubject getGuiInitiatorSubject() {
    return guiInitiatorSubject;
  }

  public void setGuiInitiatorSubject(GuiSubject guiInitiatorSubject) {
    this.guiInitiatorSubject = guiInitiatorSubject;
  }

  
  
  
}
