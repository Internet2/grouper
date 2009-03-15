/*
 * @author mchyzer
 * $Id: XstreamPocMember.java,v 1.2 2009-03-15 08:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;


/**
 * poc class for members
 */
public class XstreamPocMember {

  public XstreamPocMember(String theName, String theDescription) {
    this.name = theName;
    this.description = theDescription;
  }
  
  public XstreamPocMember() {
    
  }
  
  private String name;
  
  private String description;

  
  public String getName() {
    return this.name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public String getDescription() {
    return this.description;
  }

  
  public void setDescription(String description) {
    this.description = description;
  }
  
}
