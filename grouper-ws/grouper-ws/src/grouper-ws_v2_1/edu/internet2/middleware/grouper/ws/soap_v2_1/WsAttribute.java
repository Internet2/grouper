/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;

/**
 * attribute sent back to caller
 * @author mchyzer
 *
 */
public class WsAttribute {

  /**
   * name of attribute 
   */
  private String name;

  /**
   * value of attribute 
   */
  private String value;

  /**
   * name of attribute
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * value of attribute
   * @return the value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * name of attribute
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * @param value1 the value to set
   */
  public void setValue(String value1) {
    this.value = value1;
  }

}
