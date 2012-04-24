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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class PSOAttributeDefinition {

  private String ref;

  private String name;

  private boolean isMultiValued;

  private boolean retainAll;

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

  public boolean isRetainAll() {
    return retainAll;
  }

  public void setRetainAll(boolean retainAll) {
    this.retainAll = retainAll;
  }

  public DSMLAttr getAttribute(Map<String, BaseAttribute<?>> attributes) throws DSMLProfileException {

    if (!attributes.containsKey(ref)) {
      return null;
    }

    BaseAttribute<?> attribute = attributes.get(ref);

    DSMLValue[] dsmlValues = null;

    DSMLAttr dsmlAttr = new DSMLAttr(this.getName(), dsmlValues);

    if (this.isMultiValued()) {
      for (Object value : attribute.getValues()) {
        dsmlAttr.addValue(new DSMLValue(value.toString()));
      }
    } else {
      dsmlAttr.addValue(new DSMLValue(attribute.getValues().iterator().next().toString()));
    }

    return dsmlAttr;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("name", name);
    toStringBuilder.append("ref", ref);
    toStringBuilder.append("retainAll", retainAll);
    toStringBuilder.append("isMultiValued", isMultiValued);
    return toStringBuilder.toString();
  }
}
