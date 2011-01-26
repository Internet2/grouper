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

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.BaseAttributeDefinition;
import edu.internet2.middleware.subject.Subject;

public class SubjectAttributeDefinition extends BaseAttributeDefinition {

  private static Logger LOG = LoggerFactory.getLogger(SubjectAttributeDefinition.class);

  private List<AttributeIdentifier> attributes;

  public void setAttributes(List<AttributeIdentifier> attributes) {
    this.attributes = attributes;
  }

  protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "resolve '" + principalName + "' ad '" + this.getId() + "'";
    LOG.debug("{}", msg);

    BasicAttribute<String> attribute = new BasicAttribute<String>(this.getId());

    Collection<?> values = this.getValuesFromAllDependencies(resolutionContext);
    if (LOG.isDebugEnabled()) {
      for (Object value : values) {
        LOG.debug("{} values from dependencies '{}'", msg, value);
      }
    }

    if (values == null) {
      LOG.debug("{} no dependency values", msg);
      return attribute;
    }

    for (Object value : values) {
      if (!(value instanceof Subject)) {
        LOG.error("{} Unable to resolve attribute, dependency value is not a Subject : {}", msg, value.getClass());
        throw new AttributeResolutionException("Unable to resolve attribute, dependency value is not a Subject");
      }

      Subject subject = (Subject) value;

      String sourceId = subject.getSource().getId();

      for (AttributeIdentifier attr : attributes) {
        if (sourceId.equals(attr.getSource())) {
          // TODO this should be fixed
          if (attr.getId().equalsIgnoreCase("id")) {
            attribute.getValues().add(subject.getId());
          } else if (attr.getId().equalsIgnoreCase("name")) {
            attribute.getValues().add(subject.getName());
          } else if (attr.getId().equalsIgnoreCase("description")) {
            attribute.getValues().add(subject.getDescription());
          } else {
            attribute.getValues().addAll(subject.getAttributeValues(attr.getId()));
          }
        }
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("{} values {}", msg, attribute.getValues().size());
      for (Object value : attribute.getValues()) {
        LOG.debug("{} value '{}'", msg, value);
      }
    }

    return attribute;
  }

  public void validate() throws AttributeResolutionException {
  }
}
