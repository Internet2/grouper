/*
 * @author mchyzer
 * $Id: XstreamPocMember.java,v 1.3 2009-04-13 20:24:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;


/**
 * poc class for members
 */
public class XstreamPocMember {

  /**
   * 
   * @param theName
   * @param theDescription
   */
  public XstreamPocMember(String theName, String theDescription) {
    this.name = theName;
    this.description = theDescription;
  }
  
  /**
   * 
   */
  public XstreamPocMember() {
    //empty
  }
  
  /** */
  private String name;
  
  /** */
  private String description;

  /**
   * 
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * 
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * 
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * 
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
}
