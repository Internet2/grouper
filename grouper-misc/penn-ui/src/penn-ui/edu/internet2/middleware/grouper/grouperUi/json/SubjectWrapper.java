/**
 * @author Kate
 * $Id: SubjectWrapper.java,v 1.1 2009-08-05 00:57:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;


/**
 *
 */
public class SubjectWrapper implements Subject {

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
  @Override
  public String getAttributeValue(String name) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  @Override
  public Set getAttributeValues(String name) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  @Override
  public Map getAttributes() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  @Override
  public String getDescription() {
    return "Cant find subject: " + this.member.getSubjectSourceId() + ": " + this.member.getSubjectId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  @Override
  public String getId() {
    return this.member.getSubjectId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  @Override
  public String getName() {
    return "Cant find subject: " + this.member.getSubjectSourceId() + ": " + this.member.getSubjectId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  @Override
  public Source getSource() {
    return  this.member.getSubjectSource();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  @Override
  public SubjectType getType() {
    return this.member.getSubjectType();
  }

}
