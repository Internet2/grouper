/*
 * @author mchyzer
 * $Id: XstreamPocGroup.java,v 1.3 2009-04-13 20:24:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;


/**
 * poc class for groups
 */
public class XstreamPocGroup {

  /**
   * 
   */
  @SuppressWarnings("unused")
  private String somethingNotMarshaled = "whatever";
  
  /**
   * 
   * @param theName
   * @param theMembers
   */
  public XstreamPocGroup(String theName, XstreamPocMember[] theMembers) {
    this.name = theName;
    this.members = theMembers;
  }

  /**
   * 
   */
  public XstreamPocGroup() {
    //empty
  }
  
  /** */
  private String name;
  
  /** */
  private XstreamPocMember[] members;

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
   * @return array of members
   */
  public XstreamPocMember[] getMembers() {
    return this.members;
  }

  /**
   * 
   * @param members1
   */
  public void setMembers(XstreamPocMember[] members1) {
    this.members = members1;
  }
  
}
