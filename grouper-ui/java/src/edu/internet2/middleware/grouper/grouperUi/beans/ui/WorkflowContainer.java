package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;

public class WorkflowContainer {
  
  // config user is currently working on
  private GrouperWorkflowConfig workflowConfig;
  
  private List<GuiGrouperWorkflowConfig> guiWorkflowConfigs = new ArrayList<GuiGrouperWorkflowConfig>();
  
  private List<GrouperWorkflowConfig> grouperWorkflowConfigs = new ArrayList<GrouperWorkflowConfig>();

  //TODO move the logic to core and read from grouper.properties file
  private List<String> allConfigTypes = Arrays.asList("grouper");
  
  public List<GrouperWorkflowConfig> getGrouperWorkflowConfigs() {
    return grouperWorkflowConfigs;
  }

  public void setGrouperWorkflowConfigs(List<GrouperWorkflowConfig> grouperWorkflowConfigs) {
    this.grouperWorkflowConfigs = grouperWorkflowConfigs;
  }
  
  public boolean isCanWrite() {
    return true;
  }

  public List<GuiGrouperWorkflowConfig> getGuiWorkflowConfigs() {
    return guiWorkflowConfigs;
  }

  public void setGuiWorkflowConfigs(List<GuiGrouperWorkflowConfig> guiWorkflowConfigs) {
    this.guiWorkflowConfigs = guiWorkflowConfigs;
  }

  public GrouperWorkflowConfig getWorkflowConfig() {
    return workflowConfig;
  }

  public void setWorkflowConfig(GrouperWorkflowConfig workflowConfig) {
    this.workflowConfig = workflowConfig;
  }

  public List<String> getAllConfigTypes() {
    return allConfigTypes;
  }

  public void setAllConfigTypes(List<String> allConfigTypes) {
    this.allConfigTypes = allConfigTypes;
  }
  
}
