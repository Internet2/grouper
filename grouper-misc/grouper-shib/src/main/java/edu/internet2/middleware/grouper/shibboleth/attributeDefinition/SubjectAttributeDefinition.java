/**
 * Copyright 2014 Internet2
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
import java.util.LinkedHashSet;
import java.util.Set;

import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.subject.Subject;

/** An {@link AttributeDefinition} which returns {@link Subject} attributes. */
public class SubjectAttributeDefinition extends BaseGrouperAttributeDefinition {

  /** The logger. */
  private static Logger LOG = LoggerFactory.getLogger(SubjectAttributeDefinition.class);

  /** {@inheritDoc} */
  protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

    LOG.debug("Subject attribute definition '{}' - Resolve principal '{}'", getId(), principalName);

    Collection<?> dependencyValues = getValuesFromAllDependencies(resolutionContext);

    if (LOG.isTraceEnabled()) {
      for (Object dependencyValue : dependencyValues) {
        LOG.trace("Subject attribute definition '{}' - Resolve principal '{}' dependency value '{}'", new Object[] {
            getId(), principalName, dependencyValue });
      }
    }

    BaseAttribute<String> attribute = new BasicAttribute<String>(getId());

    for (Object dependencyValue : dependencyValues) {
      if (dependencyValue instanceof Subject) {
        BaseAttribute subjectAttribute = buildAttribute((Subject) dependencyValue);
        attribute.getValues().addAll(subjectAttribute.getValues());
      }
    }

    if (LOG.isTraceEnabled()) {
      for (Object value : attribute.getValues()) {
        LOG.trace("Subject attribute definition '{}' - Resolve principal '{}' value '{}'", new Object[] { getId(),
            principalName, value });
      }
    }

    return attribute;
  }

  /**
   * Return an attribute representing the {@link Subject}.
   * 
   * @param member the member
   * @return the attribute
   */
  protected BaseAttribute<String> buildAttribute(Subject subject) {

    BaseAttribute<String> attribute = new BasicAttribute<String>(getId());

    for (AttributeIdentifier attributeIdentifier : getAttributeIdentifiers()) {
      if (subject.getSourceId().equals(attributeIdentifier.getSource())) {
        attribute.getValues().addAll(SubjectAttributeDefinition.getValues(subject, attributeIdentifier.getId()));
      }
    }

    return attribute;
  }

  /**
   * Return the possibly empty values of a {@link Subject} attribute.
   * 
   * @param subject the subject
   * @param attributeName the name of the attribute
   * @return the possibly empty values
   */
  public static Set<String> getValues(Subject subject, String attributeName) {

    Set<String> values = new LinkedHashSet<String>();

    if (attributeName.equalsIgnoreCase("id")) {
      values.add(subject.getId());
    } else if (attributeName.equalsIgnoreCase("name")) {
      if (!DatatypeHelper.isEmpty(subject.getName())) {
        values.add(subject.getName());
      }
    } else if (attributeName.equalsIgnoreCase("description")) {
      if (!DatatypeHelper.isEmpty(subject.getDescription())) {
        values.add(subject.getDescription());
      }
    } else {
      Set<String> subjectValues = subject.getAttributeValues(attributeName);
      if (subjectValues != null) {
        for (String subjectValue : subjectValues) {
          if (!DatatypeHelper.isEmpty(subjectValue)) {
            values.add(subjectValue);
          }
        }
      }
    }

    return values;
  }
}
