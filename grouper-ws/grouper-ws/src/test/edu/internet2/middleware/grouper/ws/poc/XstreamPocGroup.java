/*
 * @author mchyzer
 * $Id: XstreamPocGroup.java,v 1.1.2.1 2009-02-23 18:39:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;


/**
 * poc class for groups
 */
public class XstreamPocGroup {

  public XstreamPocGroup(String theName, XstreamPocMember[] theMembers) {
    this.name = theName;
    this.members = theMembers;
  }

  public XstreamPocGroup() {
    
  }

  private String name;
  
  private XstreamPocMember[] members;

  
  public String getName() {
    return this.name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public XstreamPocMember[] getMembers() {
    return this.members;
  }

  
  public void setMembers(XstreamPocMember[] members) {
    this.members = members;
  }
  
}
