/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * @author vsachdeva
 *
 */
public class WsAuditEntryColumn {
  
  /**
   * column label
   */
  private String label;
  
  /**
   * integer value if any
   */
  private String valueInt;
  
  /**
   * string value if any
   */
  private String valueString;

  /**
   * 
   * @return column label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @param label1
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * @return integer value if any
   */
  public String getValueInt() {
    return this.valueInt;
  }

  /**
   * 
   * @param valueInt1
   */
  public void setValueInt(String valueInt1) {
    this.valueInt = valueInt1;
  }

  /**
   * @return string value if any
   */
  public String getValueString() {
    return this.valueString;
  }

  /**
   * 
   * @param valueString1
   */
  public void setValueString(String valueString1) {
    this.valueString = valueString1;
  }
  
  

}
