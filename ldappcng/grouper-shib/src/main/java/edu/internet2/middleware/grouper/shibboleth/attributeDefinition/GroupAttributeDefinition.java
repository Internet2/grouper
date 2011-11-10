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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;

/** An {@link AttributeDefinition} which returns {@link Group} attributes. */
public class GroupAttributeDefinition extends BaseGrouperAttributeDefinition {

  /** The logger. */
  private static Logger LOG = LoggerFactory.getLogger(GroupAttributeDefinition.class);

  /** {@inheritDoc} */
  protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

    LOG.debug("Group attribute definition '{}' - Resolve principal '{}'", getId(), principalName);

    Collection<?> dependencyValues = getValuesFromAllDependencies(resolutionContext);

    if (LOG.isTraceEnabled()) {
      for (Object dependencyValue : dependencyValues) {
        LOG.trace("Group attribute definition '{}' - Resolve principal '{}' dependency value '{}'", new Object[] {
            getId(), principalName, dependencyValue });
      }
    }

    BaseAttribute attribute = new BasicAttribute(getId());

    for (Object dependencyValue : dependencyValues) {
      if (dependencyValue instanceof Group) {
        BaseAttribute groupAttribute = buildAttribute((Group) dependencyValue);
        attribute.getValues().addAll(groupAttribute.getValues());
      }
    }

    if (LOG.isTraceEnabled()) {
      for (Object value : attribute.getValues()) {
        LOG.trace("Group attribute definition '{}' - Resolve principal '{}' value '{}'", new Object[] { getId(),
            principalName, value });
      }
    }

    return attribute;
  }

  /**
   * Return an attribute representing the {@link group}.
   * 
   * @param member the member
   * @return the attribute
   */
  protected BaseAttribute buildAttribute(Group group) {

    BaseAttribute attribute = new BasicAttribute(getId());

    for (AttributeIdentifier attributeIdentifier : getAttributeIdentifiers()) {
      if (attributeIdentifier.getSource().equals(SubjectFinder.internal_getGSA().getId())) {
        attribute.getValues().add(group.getAttributeOrFieldValue(attributeIdentifier.getId(), false, false));
      }
    }

    return attribute;
  }

}
