/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml.definitions;

import java.util.Map;

import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class PSOAttributeDefinition {

  private String ref;

  private String name;

  // TODO handle multiValued
  private boolean isMultiValued;

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isMultiValued() {
    return isMultiValued;
  }

  public void setIsMultiValued(boolean isMultiValued) {
    this.isMultiValued = isMultiValued;
  }

  public DSMLAttr getAttribute(Map<String, BaseAttribute> attributes) throws DSMLProfileException {

    if (!attributes.containsKey(ref)) {
      return null;
    }

    BaseAttribute<String> attribute = attributes.get(ref);

    DSMLValue[] dsmlValues = null;

    DSMLAttr dsmlAttr = new DSMLAttr(this.getName(), dsmlValues);

    if (this.isMultiValued()) {
      for (String value : attribute.getValues()) {
        dsmlAttr.addValue(new DSMLValue(value));
      }
    } else {
      dsmlAttr.addValue(new DSMLValue(attribute.getValues().iterator().next().toString()));
    }

    return dsmlAttr;
  }
}
