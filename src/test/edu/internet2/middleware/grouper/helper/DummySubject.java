/*
 * @author mchyzer
 * $Id: DummySubject.java,v 1.3 2009-08-12 04:52:21 mchyzer Exp $
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
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 *
 */
public class DummySubject implements Subject {

  /** */
  private String subjectId;
  
  /** */
  private String sourceId;
  
  /**
   * 
   * @param theSubjectId
   * @param theSource
   */
  public DummySubject(String theSubjectId, Source theSource) {
    this.subjectId = theSubjectId;
    this.sourceId = theSource == null ? null : theSource.getId();
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
    return this.sourceId == null ? null : SourceManager.getInstance().getSource(this.sourceId);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return SubjectTypeEnum.PERSON;
  }

}
