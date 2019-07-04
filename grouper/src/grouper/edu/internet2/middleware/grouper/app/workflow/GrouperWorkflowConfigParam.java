package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

public class GrouperWorkflowConfigParam {
  
  private String paramName;
  
  private String label;
  
  private String type;
  
  private List<String> editableInStates;
  
  private boolean required;

  
  public String getParamName() {
    return paramName;
  }
  
  public void setParamName(String paramName) {
    this.paramName = paramName;
  }
  
  
  public String getLabel() {
    return label;
  }

  
  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public List<String> getEditableInStates() {
    return editableInStates;
  }
  
  public void setEditableInStates(List<String> editableInStates) {
    this.editableInStates = editableInStates;
  }
  
  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }
  
}
