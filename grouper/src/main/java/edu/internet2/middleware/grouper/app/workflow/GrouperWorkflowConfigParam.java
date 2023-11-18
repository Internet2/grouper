package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

public class GrouperWorkflowConfigParam {
  
  /**
   * name of the param
   */
  private String paramName;
  
  /**
   * label to show on the screen
   */
  private String label;
  
  /**
   * type of field (textarea, text, checkbox)
   */
  private String type;
  
  /**
   * states in which this field is editable
   */
  private List<String> editableInStates;
  
  /**
   * is the field required or not
   */
  private boolean required;

  
  /**
   * name of the param
   * @return
   */
  public String getParamName() {
    return paramName;
  }
  
  /**
   * name of the param
   * @param paramName
   */
  public void setParamName(String paramName) {
    this.paramName = paramName;
  }
  
  /**
   * label to show on the screen
   * @return
   */
  public String getLabel() {
    return label;
  }

  /**
   * label to show on the screen
   * @param label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * type of field (textarea, text, checkbox)
   * @return
   */
  public String getType() {
    return type;
  }
  
  /**
   * type of field (textarea, text, checkbox)
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * states in which this field is editable
   * @return
   */
  public List<String> getEditableInStates() {
    return editableInStates;
  }
  
  /**
   * states in which this field is editable
   * @param editableInStates
   */
  public void setEditableInStates(List<String> editableInStates) {
    this.editableInStates = editableInStates;
  }
  
  /**
   * is the field required or not
   * @return
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * is the field required or not
   * @param required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }
  
}
