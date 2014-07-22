/**
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
 */
/*--
$Id: JNDISubject.java,v 1.6 2009-10-23 04:04:22 mchyzer Exp $
$Date: 2009-10-23 04:04:22 $

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

import java.util.Map;
import java.util.Set;

/**
 * JNDI Subject implementation.  This will lazy load attributes only if needed
 */
public class JNDISubject extends SubjectImpl {

  /**
   * @param id1
   * @param name1
   * @param description1
   * @param typeName1
   * @param sourceId1
   */
  public JNDISubject(String id1, String name1, String description1, String typeName1,
      String sourceId1) {
    super(id1, name1, description1, typeName1, sourceId1);
    
  }

  /**
   * @param id1
   * @param name1
   * @param description1
   * @param typeName1
   * @param sourceId1
   * @param attributes1
   */
  public JNDISubject(String id1, String name1, String description1, String typeName1,
      String sourceId1, Map<String, Set<String>> attributes1) {
    super(id1, name1, description1, typeName1, sourceId1, attributes1);
    
  }

  /** if we have initted the attributes */
  private boolean attributesInitted = false;

  /**
   * 
   */
  private void initAttributesIfNeeded() {
    if (!this.attributesInitted && this.getSource() instanceof JNDISourceAdapterLegacy) {
      try {
        ((JNDISourceAdapterLegacy)this.getSource()).loadAttributes(this);
      } finally {
        this.attributesInitted = true;
      }
    }
  }

  /**
   * @see edu.internet2.middleware.subject.provider.SubjectImpl#getAttributes()
   */
  @Override
  public Map<String, Set<String>> getAttributes() {
    this.initAttributesIfNeeded();
    return super.getAttributes();
  }

  /**
   * @see edu.internet2.middleware.subject.provider.SubjectImpl#getAttributeValue(java.lang.String)
   */
  @Override
  public String getAttributeValue(String name1) {
    this.initAttributesIfNeeded();
    return super.getAttributeValue(name1);
  }

  /**
   * @see edu.internet2.middleware.subject.provider.SubjectImpl#getAttributeValueOrCommaSeparated(java.lang.String)
   */
  @Override
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    this.initAttributesIfNeeded();
    return super.getAttributeValueOrCommaSeparated(attributeName);
  }

  /**
   * @see edu.internet2.middleware.subject.provider.SubjectImpl#getAttributeValues(java.lang.String)
   */
  @Override
  public Set<String> getAttributeValues(String name1) {
    this.initAttributesIfNeeded();
    return super.getAttributeValues(name1);
  }

  /**
   * @see edu.internet2.middleware.subject.provider.SubjectImpl#getAttributeValueSingleValued(java.lang.String)
   */
  @Override
  public String getAttributeValueSingleValued(String attributeName) {
    this.initAttributesIfNeeded();
    return super.getAttributeValueSingleValued(attributeName);
  }
  
}
