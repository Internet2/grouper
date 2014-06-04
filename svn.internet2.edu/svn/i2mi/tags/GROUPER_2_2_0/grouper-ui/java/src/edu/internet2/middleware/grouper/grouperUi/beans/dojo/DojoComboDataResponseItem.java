/**
 * @author mchyzer
 * $Id: DojoComboDataResponseItem.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.dojo;


/**
 * one item of the response
 */
public class DojoComboDataResponseItem {

  /**
   * @param identifier1
   * @param label1
   */
  public DojoComboDataResponseItem(String identifier1, String label1, String htmlLabel1) {
    super();
    this.id = identifier1;
    this.name = label1;
    this.htmlLabel = htmlLabel1;
  }

  /**
   * id of record
   */
  private String id;
  
  /**
   * name of record
   */
  private String name;
  
  /**
   * html label
   */
  private String htmlLabel;
  
  /**
   * html label
   * @return html label
   */
  public String getHtmlLabel() {
    return this.htmlLabel;
  }

  /**
   * html label
   * @param htmlLabel1
   */
  public void setHtmlLabel(String htmlLabel1) {
    this.htmlLabel = htmlLabel1;
  }

  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * @param identifier1 the id to set
   */
  public void setId(String identifier1) {
    this.id = identifier1;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * @param label1 the name to set
   */
  public void setName(String label1) {
    this.name = label1;
  }
  
}
