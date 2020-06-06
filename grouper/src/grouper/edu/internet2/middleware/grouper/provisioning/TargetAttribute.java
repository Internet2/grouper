package edu.internet2.middleware.grouper.provisioning;


/**
 * name value pair could be multi valued
 * @author mchyzer
 *
 */
public class TargetAttribute {

  /**
   * name of attribute
   */
  private String name;
  
  /**
   * value could be multi valued
   */
  private Object value;

  /**
   * name of attribute
   * @return name of attribute
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of attribute
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * value could be multi valued
   * @return value
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * value could be multi valued
   * @param value1
   */
  public void setValue(Object value1) {
    this.value = value1;
  }
  
}
