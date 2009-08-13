/*--
$Id: BaseSubject.java,v 1.2 2009-08-13 21:12:02 mchyzer Exp $
$Date: 2009-08-13 21:12:02 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;

/**
 * Base Subject implementation.  Subclass this to change behavior
 */
public class BaseSubject implements Subject {

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return toStringStatic(this);
  }


  /**
   * toString
   * @param subject
   * @return string
   */
  public static String toStringStatic(Subject subject) {
    String name = subject.getName();
    return "Subject id: " + subject.getId() + ", sourceId: " + subject.getSourceId() + 
      (StringUtils.isBlank(name) ? "" : (", name: " + name));
  }

  /** */
  private static Log log = LogFactory.getLog(BaseSubject.class);

  /** */
  private String id;

  /** */
  private String name;

  /** */
  private String description;

  /** */
  private String typeName;

  
  /**
   * sourceId
   * @return the sourceId
   */
  public String getSourceId() {
    return this.sourceId;
  }

  
  /**
   * sourceId
   * @param sourceId1 the sourceId to set
   */
  protected void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /** sourceId */
  private String sourceId;
  
  /** */
  private Map<String, Set<String>> attributes;

  /**
   * Constructor called by SourceManager.
   * @param id1 
   * @param name1 
   * @param description1 
   * @param typeName1 
   * @param sourceId1 
   * @param attributes1 
   */
  public BaseSubject(String id1, String name1, String description1, String typeName1,
      String sourceId1, Map<String, Set<String>> attributes1) {
    log.debug("Name = " + name1);
    this.id = id1;
    this.name = name1;
    this.typeName = typeName1;
    this.description = description1;
    this.sourceId = sourceId1;
    this.attributes = attributes1;
  }

  /**
   * {@inheritDoc}
   */
  public String getId() {
    return this.id;
  }

  /**
   * {@inheritDoc}
   */
  public SubjectType getType() {
    return SubjectTypeEnum.valueOf(this.typeName);
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   */

  public String getDescription() {
    return this.description;
  }

  /**
   * {@inheritDoc}
   */
  public String getAttributeValue(String name1) {
    if (this.attributes == null) {
      log.error("No attributes.");
    }
    Set<String> values = this.attributes.get(name1);
    if (values != null) {
      if (values.size() > 1) {
        throw new RuntimeException(
            "This is not a single valued attribute, it is multivalued: '" + name1 + "', " + values.size());
      }
      return values.iterator().next();
    }
    return "";
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getAttributeValues(String name1) {
    return this.attributes.get(name1);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, Set<String>> getAttributes() {
    return this.attributes;
  }

  /**
   * {@inheritDoc}
   */
  public Source getSource() {
    return SourceManager.getInstance().getSource(this.sourceId);
  }

  /**
   * 
   * @param attributes1
   */
  public void setAttributes(Map<String, Set<String>> attributes1) {
    this.attributes = attributes1;
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getTypeName()
   */
  public String getTypeName() {
    return null;
  }


  
  /**
   * @param id1 the id to set
   */
  protected void setId(String id1) {
    this.id = id1;
  }


  
  /**
   * @param name1 the name to set
   */
  protected void setName(String name1) {
    this.name = name1;
  }


  
  /**
   * @param description1 the description to set
   */
  protected void setDescription(String description1) {
    this.description = description1;
  }


  
  /**
   * @param typeName1 the typeName to set
   */
  protected void setTypeName(String typeName1) {
    this.typeName = typeName1;
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return equalsStatic(this, obj);
  }


  /**
   * @param subject 
   * @param obj
   * @return true if equal
   */
  public static boolean equalsStatic(Subject subject, Object obj) {
    if (!(obj instanceof Subject)) {
      return false;
    }
    Subject otherObj = (Subject) obj;
    return StringUtils.equals(subject.getId(), otherObj.getId()) 
      && StringUtils.equals(subject.getSourceId(), otherObj.getSourceId());
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return hashcodeStatic(this);
  }


  /**
   * @param subject 
   * @return hash code
   */
  public static int hashcodeStatic(Subject subject) {
    return new HashCodeBuilder().append(subject.getId()).append(subject.getSourceId()).toHashCode();
  }

  
  
}
