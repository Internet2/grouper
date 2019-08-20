package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;


public class GuiGrouperWorkflowConfig {
  
  /**
   * parent grouper workflow config
   */
  private GrouperWorkflowConfig grouperWorkflowConfig;
  
  /**
   * json beautified config params
   */
  private String workflowConfigParams;
  
  /**
   * json beautified config approval states
   */
  private String workflowApprovalStates;
  
  /**
   * gui group on which workflow is configured
   */
  private GuiGroup guiGroup;
  
  private GuiGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig) {
    this.grouperWorkflowConfig = grouperWorkflowConfig;
  }

  public GrouperWorkflowConfig getGrouperWorkflowConfig() {
    return grouperWorkflowConfig;
  }

  /**
   * convert from grouper workflow config to gui grouper workflow config
   * @param grouperWorkflowConfig
   * @return
   */
  public static GuiGrouperWorkflowConfig convertFromGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig) {
    GuiGrouperWorkflowConfig guiGrouperWorkflowConfig = new GuiGrouperWorkflowConfig(grouperWorkflowConfig);
    
    String viewersGroupId = grouperWorkflowConfig.getWorkflowConfigViewersGroupId();
    if (StringUtils.isNotBlank(viewersGroupId)) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), viewersGroupId, true);
      guiGrouperWorkflowConfig.guiGroup = new GuiGroup(group); 
    }
    
    try {
      guiGrouperWorkflowConfig.workflowConfigParams = grouperWorkflowConfig.getWorkflowConfigParamsString();
    } catch (Exception e) {
      throw new RuntimeException("could not convert config params to string", e);
    }
    
    try {
      guiGrouperWorkflowConfig.workflowApprovalStates = grouperWorkflowConfig.getWorkflowConfigApprovalsString();
    } catch(Exception e) {
      throw new RuntimeException("could not convert approval states to string", e);
    }
    
    return guiGrouperWorkflowConfig;
  }
  
  /**
   * convert from list of configs to list of gui configs
   * @param grouperWorkflowConfigs
   * @return
   */
  public static List<GuiGrouperWorkflowConfig> convertFromGrouperWorkflowConfigs(List<GrouperWorkflowConfig> grouperWorkflowConfigs) {
    
    List<GuiGrouperWorkflowConfig> result = new ArrayList<GuiGrouperWorkflowConfig>();
    
    for (GrouperWorkflowConfig workflowConfig: grouperWorkflowConfigs) {
      result.add(convertFromGrouperWorkflowConfig(workflowConfig));
    }
    
    return result;
  }

  /**
   * gui group on which workflow is configured
   * @return
   */
  public GuiGroup getGuiGroup() {
    return guiGroup;
  }

  /**
   * json beautified config params
   * @return
   */
  public String getWorkflowConfigParams() {
    return workflowConfigParams;
  }

  /**
   * json beautified config approval states
   * @return
   */
  public String getWorkflowApprovalStates() {
    return workflowApprovalStates;
  }
  
}
