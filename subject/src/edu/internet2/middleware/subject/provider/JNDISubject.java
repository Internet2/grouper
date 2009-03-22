/*--
$Id: JNDISubject.java,v 1.3 2009-03-22 02:49:26 mchyzer Exp $
$Date: 2009-03-22 02:49:26 $

Copyright 2005 Internet2.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
/*
 * JNDISubject.java
 * 
 * Created on March 6, 2006
 * 
 * Author Ellen Sluss
 */
package edu.internet2.middleware.subject.provider;

import java.util.Set;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;

/**
 * JNDI Subject implementation.
 */
public class JNDISubject implements Subject {

  /** */
  private static Log log = LogFactory.getLog(JNDISubject.class);

  /** */
  private JNDISourceAdapter adapter;

  /** */
  private String id;

  /** */
  private String name;

  /** */
  private String description = null;

  /** */
  private SubjectType type = null;

  /** */
  private Map<String, Set<String>> attributes = null;

  /**
   * Constructor called by SourceManager.
   * @param id1 
   * @param name1 
   * @param description1 
   * @param type1 
   * @param adapter1 
   */
  protected JNDISubject(String id1, String name1, String description1, SubjectType type1,
      JNDISourceAdapter adapter1) {
    log.debug("Name = " + name1);
    this.id = id1;
    this.name = name1;
    this.type = type1;
    this.description = description1;
    this.adapter = adapter1;
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
   * 
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String name1) {
    if (this.attributes == null) {
      this.adapter.loadAttributes(this);
    }
    Set<String> values = this.attributes.get(name1);
    if (values != null) {
      return values.toArray(new String[0])[0];
    }
    return null;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set<String> getAttributeValues(String name1) {
    if (this.attributes == null) {
      this.adapter.loadAttributes(this);
    }
    return this.attributes.get(name1);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, Set<String>> getAttributes() {
    if (this.attributes == null) {
      this.adapter.loadAttributes(this);
    }
    return this.attributes;
  }

  /**
   * {@inheritDoc}
   */
  public Source getSource() {
    return this.adapter;
  }

  /**
   * 
   * @param attributes1
   */
  protected void setAttributes(Map<String, Set<String>> attributes1) {
    this.attributes = attributes1;
  }
}
