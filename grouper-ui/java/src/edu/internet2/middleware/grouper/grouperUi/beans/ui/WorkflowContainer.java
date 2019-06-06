package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkflowContainer {
  
  // config user is currently working on
  private GuiGrouperWorkflowConfig guiGrouperWorkflowConfig;
  
  // gui workflow configs for a group
  private List<GuiGrouperWorkflowConfig> guiWorkflowConfigs = new ArrayList<GuiGrouperWorkflowConfig>();
  
  //TODO move the logic to core and read from grouper.properties file
  private List<String> allConfigTypes = Arrays.asList("grouper");
  
  public boolean isCanWrite() {
    return true;
  }

  public List<GuiGrouperWorkflowConfig> getGuiWorkflowConfigs() {
    return guiWorkflowConfigs;
  }

  public void setGuiWorkflowConfigs(List<GuiGrouperWorkflowConfig> guiWorkflowConfigs) {
    this.guiWorkflowConfigs = guiWorkflowConfigs;
  }

  public GuiGrouperWorkflowConfig getGuiGrouperWorkflowConfig() {
    return guiGrouperWorkflowConfig;
  }

  public void setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig guiGrouperWorkflowConfig) {
    this.guiGrouperWorkflowConfig = guiGrouperWorkflowConfig;
  }

  public List<String> getAllConfigTypes() {
    return allConfigTypes;
  }

  public void setAllConfigTypes(List<String> allConfigTypes) {
    this.allConfigTypes = allConfigTypes;
  }
  
}
