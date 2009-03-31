/*
 * @author mchyzer
 * $Id: SimpleSubject.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 * simple subject based on subjectId and sourceId
 */
public class SimpleSubject implements Subject {

  /** */
  private String subjectId;
  
  /** */
  private String sourceId;
  
  /**
   * 
   * @param theSubjectId
   * @param theSource
   */
  public SimpleSubject(String theSubjectId, String theSource) {
    this.subjectId = theSubjectId;
    this.sourceId = theSource;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String arg0) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set getAttributeValues(String arg0) {
    
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
    return this.subjectId;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId() {
    return this.subjectId;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  public String getName() {
    return this.subjectId;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    try { 
      return SubjectFinder.getSource(this.sourceId);
    } catch(SourceUnavailableException sue) {
      throw new RuntimeException(sue);
    }
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    //not sure if we know the type, but just assume it is a person
    return SubjectTypeEnum.PERSON;
  }

}
