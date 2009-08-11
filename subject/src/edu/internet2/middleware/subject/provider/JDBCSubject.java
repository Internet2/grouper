/*--
$Id: JDBCSubject.java,v 1.6 2009-08-11 21:58:37 mchyzer Exp $
$Date: 2009-08-11 21:58:37 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;

/**
 * JDBC Subject implementation.
 */
public class JDBCSubject implements Subject {

  /** */
  private static Log log = LogFactory.getLog(JDBCSubject.class);

  /** */
  protected String id;

  /** */
  protected String name;

  /** */
  protected String description;

  /** */
  protected SubjectType type;

  
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
  protected Map<String, Set<String>> attributes;

  /** Public default constructor. It allows subclassing of JDBCSubject! */
  public JDBCSubject() {
    this.id = null;
    this.name = null;
    this.description = null;
    this.type = null;
    this.attributes = null;
  }

  /**
   * Constructor called by SourceManager.
   * @param id1 
   * @param name1 
   * @param description1 
   * @param type1 
   * @param adapter1 
   */
  public JDBCSubject(String id1, String name1, String description1, SubjectType type1,
      JDBCSourceAdapter adapter1) {
    log.debug("Name = " + name1);
    this.id = id1;
    this.name = name1;
    this.type = type1;
    this.description = description1;
    this.sourceId = adapter1 == null ? null : adapter1.getId();

  }

  /**
   * Constructor that takes the subject's attributes. Needed because the
   * setAttributes() method is protected.
   * @param id1 The subject ID
   * @param name1 The subject name
   * @param description1 The subject description
   * @param type1 The subject type
   * @param adapter1 The SourceAdapter
   * @param attributes1 The subject attributes
   */
  public JDBCSubject(String id1, String name1, String description1, SubjectType type1,
      JDBCSourceAdapter adapter1, Map<String, Set<String>> attributes1) {
    this(id1, name1, description1, type1, adapter1);
    setAttributes(attributes1);
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
    return this.type;
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
      return values.toArray(new String[0])[0];
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getAttributeValues(String name1) {
    if (this.attributes == null) {
      log.error("No attributes.");
    }
    return this.attributes.get(name1);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, Set<String>> getAttributes() {
    if (this.attributes == null) {
      //this.adapter.loadAttributes(this);
    }
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
  protected void setAttributes(Map<String, Set<String>> attributes1) {
    this.attributes = attributes1;
  }

}
