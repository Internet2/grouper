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
  
  private GrouperWorkflowConfig grouperWorkflowConfig;
  
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
  
}
