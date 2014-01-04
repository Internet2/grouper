package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.subject.Subject;

/**
 * implement GrouperObject for subjects
 * @author mchyzer
 *
 */
public class GrouperObjectSubjectWrapper implements GrouperObject {

  /**
   * subject
   */
  private Subject subject;
  
  /**
   * 
   * @param subject
   */
  public GrouperObjectSubjectWrapper(Subject subject) {
    super();
    this.subject = subject;
  }

  /**
   * 
   */
  public GrouperObjectSubjectWrapper() {
    super();
  }

  /**
   * subject
   * @return subject
   */
  public Subject getSubject() {
    return this.subject;
  }

  /**
   * subject
   * @param subject1
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  /**
   * description
   */
  @Override
  public String getDescription() {
    return this.subject.getDescription();
  }

  /**
   * display name
   */
  @Override
  public String getDisplayName() {
    return this.subject.getName();
  }

  /**
   * name
   */
  @Override
  public String getName() {
    return this.subject.getName();
  }
  
}
