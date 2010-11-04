package edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister;


/**
 * represents a row of the screen, field or attribute
 * @author mchyzer
 *
 */
public class RegisterField {

  /** system name of the field */
  private String systemName;
  
  /** if this is a field or if it is an attribute */
  private boolean fieldNotAttribute;
  
  /** label on the screen */
  private String label;
  
  /** tooltip on the screen */
  private String tooltip;

  /** if this is required */
  private boolean required;

  /** value of field */
  private String value;
  
  /** param name in request */
  private String paramName;
  
  /**
   * param name in request
   * @return param name
   */
  public String getParamName() {
    return this.paramName;
  }

  /**
   * param name in request
   * @param paramName1
   */
  public void setParamName(String paramName1) {
    this.paramName = paramName1;
  }

  /**
   * value of field
   * @return value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * value of field
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * if readonly
   */
  private boolean readonly = false;
  
  /**
   * if readonly
   * @return if readonly
   */
  public boolean isReadonly() {
    return readonly;
  }

  /**
   * if readonly
   * @param readonly1
   */
  public void setReadonly(boolean readonly1) {
    this.readonly = readonly1;
  }

  /**
   * system name of the field
   * @return system name
   */
  public String getSystemName() {
    return this.systemName;
  }

  /**
   * system name of the field
   * @param systemName1
   */
  public void setSystemName(String systemName1) {
    this.systemName = systemName1;
  }

  /**
   * if this is a field or if it is an attribute
   * @return field or attribute
   */
  public boolean isFieldNotAttribute() {
    return this.fieldNotAttribute;
  }

  /**
   * if this is a field or if it is an attribute
   * @param fieldNotAttribute1
   */
  public void setFieldNotAttribute(boolean fieldNotAttribute1) {
    this.fieldNotAttribute = fieldNotAttribute1;
  }

  /**
   * label on the screen
   * @return label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * label on the screen
   * @param label1
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * tooltip on the screen
   * @return tooltip
   */
  public String getTooltip() {
    return this.tooltip;
  }

  /**
   * tooltip on the screen
   * @param tooltip1
   */
  public void setTooltip(String tooltip1) {
    this.tooltip = tooltip1;
  }

  /**
   * if this is required
   * @return if required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * if this is required
   * @param required1
   */
  public void setRequired(boolean required1) {
    this.required = required1;
  }

  
  
}
