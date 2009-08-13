/**
 * @author Kate
 * $Id: SubjectWrapper.java,v 1.1 2009-08-13 17:56:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;


/**
 * subject implementation around a member, if the subject is not found, handle gracefully
 */
public class SubjectWrapper implements Subject, Serializable {

  /**
   * default constructor
   */
  public SubjectWrapper() {
    
  }

  /**
   * 
   */
  private Member member;
  
  /**
   * construct from member
   * @param theMember
   */
  public SubjectWrapper(Member theMember) {
    this.member = theMember;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String name) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set getAttributeValues(String name) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map getAttributes() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  public String getDescription() {
    return "Cant find subject: " + this.member.getSubjectSourceId() + ": " + this.member.getSubjectId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId() {
    return this.member.getSubjectId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  public String getName() {
    return "Cant find subject: " + this.member.getSubjectSourceId() + ": " + this.member.getSubjectId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    return  this.member.getSubjectSource();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return this.member.getSubjectType();
  }

}
