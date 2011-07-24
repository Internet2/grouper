/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap;



/**
 * value of an attribute assign
 */
public class WsAttributeAssignValue {

  /** id of this attribute assignment */
  private String id;
  
  /** internal value */
  private String valueSystem;

  /** formatted value */
  private String valueFormatted;
  
  /**
   * internal value
   * @return internal value
   */
  public String getValueSystem() {
    return this.valueSystem;
  }

  /**
   * internal value
   * @param valueSystem1
   */
  public void setValueSystem(String valueSystem1) {
    this.valueSystem = valueSystem1;
  }

  /**
   * value formatted
   * @return value formatted
   */
  public String getValueFormatted() {
    return this.valueFormatted;
  }

  /**
   * value formatted
   * @param valueFormatted1
   */
  public void setValueFormatted(String valueFormatted1) {
    this.valueFormatted = valueFormatted1;
  }

  /**
   * id of this attribute assignment
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id of this attribute assignment
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * 
   */
  public WsAttributeAssignValue() {
    //default constructor
  }
}
