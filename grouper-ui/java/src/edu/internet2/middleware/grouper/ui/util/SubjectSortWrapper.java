/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author Kate
 * $Id: SubjectSortWrapper.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.util;

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
      
      this.screenLabel = GrouperUiUtils.convertSubjectToLabelConfigured(this.wrappedSubject);
      this.screenLabel = StringUtils.defaultString(this.screenLabel);
    }
    
    return this.screenLabel;
  }
  
  /**
   * screen label (sort by this)
   * @return the screen label
   */
  public String getScreenLabelLong() {
    
    if (this.screenLabel == null) {
      
      this.screenLabel = GrouperUiUtils.convertSubjectToLabelConfigured(this.wrappedSubject);
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
  public String getAttributeValue(String name) {
    return this.wrappedSubject.getAttributeValue(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set getAttributeValues(String name) {
    return this.wrappedSubject.getAttributeValues(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map getAttributes() {
    return this.wrappedSubject.getAttributes();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  public String getDescription() {
    return this.wrappedSubject.getDescription();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId() {
    return this.wrappedSubject.getId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  public String getName() {
    return this.wrappedSubject.getName();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    return this.wrappedSubject.getSource();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return this.wrappedSubject.getType();
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (!(o instanceof SubjectSortWrapper)) {
      return -1;
    }
    SubjectSortWrapper subjectSortWrapper = (SubjectSortWrapper)o;
    return this.getScreenLabel().compareToIgnoreCase(subjectSortWrapper.getScreenLabel());
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    return this.wrappedSubject.getAttributeValueOrCommaSeparated(attributeName);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String)
   */
  public String getAttributeValueSingleValued(String attributeName) {
    return this.wrappedSubject.getAttributeValueSingleValued(attributeName);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSourceId()
   */
  public String getSourceId() {
    return this.wrappedSubject.getSourceId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getTypeName()
   */
  public String getTypeName() {
    return this.wrappedSubject.getTypeName();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String, boolean)
   */
  @Override
  public String getAttributeValue(String attributeName, boolean excludeInternalAttributes) {
    return this.wrappedSubject.getAttributeValue(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String, boolean)
   */
  @Override
  public String getAttributeValueOrCommaSeparated(String attributeName,
      boolean excludeInternalAttributes) {
    return this.wrappedSubject.getAttributeValueOrCommaSeparated(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String, boolean)
   */
  @Override
  public String getAttributeValueSingleValued(String attributeName,
      boolean excludeInternalAttributes) {
    return this.wrappedSubject.getAttributeValueSingleValued(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String, boolean)
   */
  @Override
  public Set<String> getAttributeValues(String attributeName,
      boolean excludeInternalAttributes) {
    return this.wrappedSubject.getAttributeValues(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes(boolean)
   */
  @Override
  public Map<String, Set<String>> getAttributes(boolean excludeInternalAttributes) {
    return this.wrappedSubject.getAttributes(excludeInternalAttributes);
  }

}
