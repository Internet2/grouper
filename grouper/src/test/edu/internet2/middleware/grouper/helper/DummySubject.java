/*
 * @author mchyzer
 * $Id: DummySubject.java,v 1.2 2009-03-22 05:41:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 *
 */
public class DummySubject implements Subject {

  /** */
  private String subjectId;
  
  /** */
  private Source source;
  
  /**
   * 
   * @param theSubjectId
   * @param theSource
   */
  public DummySubject(String theSubjectId, Source theSource) {
    this.subjectId = theSubjectId;
    this.source = theSource;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String arg0) {
    return this.subjectId;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set<String> getAttributeValues(String arg0) {
    
    Set<String> results =new HashSet<String>();
    results.add(this.subjectId);
    return results;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map<String, Set<String>> getAttributes() {
    Map<String, Set<String>> map = new  HashMap<String, Set<String>>();
    map.put("loginid", GrouperUtil.toSet(this.subjectId));
    map.put("name", GrouperUtil.toSet(this.subjectId));
    map.put("description", GrouperUtil.toSet(this.subjectId));
    return map;
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
    return this.source;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return SubjectTypeEnum.PERSON;
  }

}
