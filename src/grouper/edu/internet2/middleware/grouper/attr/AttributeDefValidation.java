/**
 * 
 */
package edu.internet2.middleware.grouper.attr;


/**
 * validation of an attribute value
 * @author mchyzer
 *
 */
public class AttributeDefValidation {

  /** id of object */
  private String id;
  
  /** id of the validation def */
  private String validationDefId;
  
  /** argument 0 if applicable */
  private String argument0;
  
  /** argument 1 if applicable */
  private String argument1;

  /**
   * id of object
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id of object
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * id of the validation def
   * @return id
   */
  public String getValidationDefId() {
    return this.validationDefId;
  }

  /**
   * id of the validation def
   * @param validationDefId1
   */
  public void setValidationDefId(String validationDefId1) {
    this.validationDefId = validationDefId1;
  }

  /**
   * id of the validation def
   * @return
   */
  public String getArg0() {
    return this.argument0;
  }

  /**
   * id of the validation def
   * @param _arg0
   */
  public void setArg0(String _arg0) {
    this.argument0 = _arg0;
  }

  /**
   * id of the validation def
   * @return arg1
   */
  public String getArg1() {
    return this.argument1;
  }

  /**
   * id of the validation def
   * @param _arg1
   */
  public void setArg1(String _arg1) {
    this.argument1 = _arg1;
  }
  
}
