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
/* ========================================================================
 * Copyright (c) 2009-2011 The University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
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
 * Ldap Subject implementation.
 */
public class LdapSubject extends SubjectImpl {
	
   private static Log log = LogFactory.getLog(LdapSubject.class);
   
   /*
    * @param id
    * @param name
    * @param description
    * @param typeName
    * @param sourceId
    */
   protected LdapSubject(String id, String name, String description,
   		String type, String sourceId) {
                super(id, name, description, type, sourceId);
   	log.debug("LdapSubject Name = "  + name);
   }

   /*
    * @param id
    * @param name
    * @param description
    * @param typeName
    * @param sourceId
    * @param attributes
    */
   protected LdapSubject(String id, String name, String description,
   		String type, String sourceId, Map<String, Set<String>> attributes) {
                super(id, name, description, type, sourceId, attributes);
        }

  /* have we tried to get all the attributes */
  private boolean attributesGotten = false;

  /** 
   * @param v  have attributes been acquired
   */
  public void setAttributesGotten(boolean v) {
      attributesGotten = v;
  }

  /**
   * Try to get more attributes.
   */
  private void getAllAttributes() {
    if (!this.attributesGotten) {
      try {
        ((LdapSourceAdapter)this.getSource()).getAllAttributes(this);
      } finally {
        this.attributesGotten = true;
      }
    }
  }

  /* Name and descripton won't be null, but might be empty */

  @Override
  public String getName() {
    if (super.getName().length()>0) return super.getName();
    this.getAllAttributes();
    return super.getName();
  }

  @Override
  public String getDescription() {
    if (super.getDescription().length()>0) return super.getDescription();
    log.debug("getting all (description), gotten = " + attributesGotten);
    this.getAllAttributes();
    log.debug("got all (description), gotten=" + attributesGotten + ", desc=" + super.getDescription());
    return super.getDescription();
  }

  /* Other attrs will be null */

  @Override
  public Map<String, Set<String>> getAttributes() {
    this.getAllAttributes();
    return super.getAttributes();
  }

  @Override
  public String getAttributeValue(String name1) {
    this.getAllAttributes();
    return super.getAttributeValue(name1);
  }
  @Override
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    this.getAllAttributes();
    return super.getAttributeValueOrCommaSeparated(attributeName);
  }
  @Override
  public Set<String> getAttributeValues(String name1) {
    this.getAllAttributes();
    return super.getAttributeValues(name1);
  }
  @Override
  public String getAttributeValueSingleValued(String attributeName) {
    this.getAllAttributes();
    return super.getAttributeValueSingleValued(attributeName);
  }

}
