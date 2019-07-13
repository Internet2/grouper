package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalStates;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParams;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;


public class GuiGrouperWorkflowConfig {
  
  private GrouperWorkflowConfig grouperWorkflowConfig;
  
  private String workflowConfigParams;
  
  private String workflowApprovalStates;
  
  private GuiGroup guiGroup;
  
  private GuiGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig) {
    this.grouperWorkflowConfig = grouperWorkflowConfig;
  }

  public GrouperWorkflowConfig getGrouperWorkflowConfig() {
    return grouperWorkflowConfig;
  }

  public static GuiGrouperWorkflowConfig convertFromGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig) {
    GuiGrouperWorkflowConfig guiGrouperWorkflowConfig = new GuiGrouperWorkflowConfig(grouperWorkflowConfig);
    
    String viewersGroupId = grouperWorkflowConfig.getWorkflowConfigViewersGroupId();
    if (StringUtils.isNotBlank(viewersGroupId)) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), viewersGroupId, true);
      guiGrouperWorkflowConfig.guiGroup = new GuiGroup(group); 
    }
    
    try {
      guiGrouperWorkflowConfig.workflowConfigParams = grouperWorkflowConfig.getWorkflowConfigParamsString();
      // guiGrouperWorkflowConfig.workflowConfigParams = GrouperWorkflowSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(grouperWorkflowConfig.getConfigParams());
    } catch (Exception e) {
      throw new RuntimeException("could not convert config params to string", e);
    }
    
    GrouperWorkflowApprovalStates approvalStates = grouperWorkflowConfig.getWorkflowApprovalStates();
    try {
      guiGrouperWorkflowConfig.workflowApprovalStates = grouperWorkflowConfig.getWorkflowConfigApprovalsString();
      // guiGrouperWorkflowConfig.workflowApprovalStates = GrouperWorkflowSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(grouperWorkflowConfig.getWorkflowApprovalStates());
    } catch(Exception e) {
      throw new RuntimeException("could not convert approval states to string", e);
    }
    
    return guiGrouperWorkflowConfig;
  }
  
  public static List<GuiGrouperWorkflowConfig> convertFromGrouperWorkflowConfigs(List<GrouperWorkflowConfig> grouperWorkflowConfigs) {
    
    List<GuiGrouperWorkflowConfig> result = new ArrayList<GuiGrouperWorkflowConfig>();
    
    for (GrouperWorkflowConfig workflowConfig: grouperWorkflowConfigs) {
      result.add(convertFromGrouperWorkflowConfig(workflowConfig));
    }
    
    return result;
  }

  public GuiGroup getGuiGroup() {
    return guiGroup;
  }

  public String getWorkflowConfigParams() {
    return workflowConfigParams;
  }

  public String getWorkflowApprovalStates() {
    return workflowApprovalStates;
  }
  
}
