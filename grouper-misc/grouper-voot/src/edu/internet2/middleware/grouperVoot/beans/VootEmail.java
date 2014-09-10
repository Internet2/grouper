package edu.internet2.middleware.grouperVoot.beans;

/**
 * voot email class in person
 * @author mchyzer
 *
 */
public class VootEmail {
  
  /** type of email e.g. 'type' */
  private String type;
  
  /** value of email e.g. john@smith.edu */
  private String value;

  /**
   * type of email e.g. 'type'
   * @return type
   */
  public String getType() {
    return this.type;
  }

  /**
   * type of email e.g. 'type'
   * @param type1
   */
  public void setType(String type1) {
    this.type = type1;
  }

  /**
   * value of email e.g. john@smith.edu
   * @return value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * value of email e.g. john@smith.edu
   * @param value1
   */
  public void setValue(String value1) {
    this.value = value1;
  }
  
  
  
}
