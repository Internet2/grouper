/**
 * @author Kate
 * $Id: SubjectSortWrapper.java,v 1.1 2009-08-07 07:36:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.util;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;


/**
 *
 */
public class SubjectSortWrapper implements Subject, Comparable {

  /** wrapped subject */
  private Subject wrappedSubject;
  
  /**
   * screen label (sort by this)
   */
  private String screenLabel;

  /**
   * screen label (sort by this)
   * @return the screen label
   */
  public String getScreenLabel() {
    
    if (this.screenLabel == null) {
      
      this.screenLabel = GuiUtils.convertSubjectToLabelConfigured(this.wrappedSubject);
      this.screenLabel = StringUtils.defaultString(this.screenLabel);
    }
    
    return this.screenLabel;
  }
  
  /**
   * wrapped subject
   * @param subject
   */
  public SubjectSortWrapper(Subject subject) {
    this.wrappedSubject = subject;
  }
  
  /**
   * return the wrapped subject
   * @return the wrapped subject
   */
  public Subject getWrappedSubject() {
    return this.wrappedSubject;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  @Override
  public String getAttributeValue(String name) {
    return this.wrappedSubject.getAttributeValue(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  @Override
  public Set getAttributeValues(String name) {
    return this.wrappedSubject.getAttributeValues(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  @Override
  public Map getAttributes() {
    return this.wrappedSubject.getAttributes();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  @Override
  public String getDescription() {
    return this.wrappedSubject.getDescription();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  @Override
  public String getId() {
    return this.wrappedSubject.getId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  @Override
  public String getName() {
    return this.wrappedSubject.getName();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  @Override
  public Source getSource() {
    return this.wrappedSubject.getSource();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  @Override
  public SubjectType getType() {
    return this.wrappedSubject.getType();
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Object o) {
    if (!(o instanceof SubjectSortWrapper)) {
      return -1;
    }
    SubjectSortWrapper subjectSortWrapper = (SubjectSortWrapper)o;
    return this.getScreenLabel().compareToIgnoreCase(subjectSortWrapper.getScreenLabel());
  }

}
