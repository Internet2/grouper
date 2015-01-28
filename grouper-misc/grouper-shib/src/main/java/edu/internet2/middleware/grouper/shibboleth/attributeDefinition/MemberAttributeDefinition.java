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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.subject.Subject;

/** An {@link AttributeDefinition} which returns {@link Member} attributes. */
public class MemberAttributeDefinition extends BaseGrouperAttributeDefinition {

  /** The logger. */
  private static Logger LOG = LoggerFactory.getLogger(MemberAttributeDefinition.class);

  /** {@inheritDoc} */
  protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

    LOG.debug("Member attribute definition '{}' - Resolve principal '{}'", getId(), principalName);

    Collection<?> dependencyValues = getValuesFromAllDependencies(resolutionContext);

    if (LOG.isTraceEnabled()) {
      for (Object dependencyValue : dependencyValues) {
        LOG.trace("Member attribute definition '{}' - Resolve principal '{}' dependency value '{}'", new Object[] {
            getId(), principalName, dependencyValue });
      }
    }

    BaseAttribute attribute = new BasicAttribute(getId());

    for (Object dependencyValue : dependencyValues) {
      if (dependencyValue instanceof Member) {
        BaseAttribute memberAttribute = buildAttribute((Member) dependencyValue);
        attribute.getValues().addAll(memberAttribute.getValues());
      }
    }

    if (LOG.isTraceEnabled()) {
      for (Object value : attribute.getValues()) {
        LOG.trace("Member attribute definition '{}' - Resolve principal '{}' value '{}'", new Object[] { getId(),
            principalName, value });
      }
    }

    return attribute;
  }

  /**
   * Return an attribute representing the {@link Member}.
   * 
   * @param member the member
   * @return the attribute
   */
  protected BaseAttribute buildAttribute(Member member) {

    BaseAttribute attribute = new BasicAttribute(getId());

    for (AttributeIdentifier attributeIdentifer : getAttributeIdentifiers()) {
      // subject attributes
      Subject subject = member.getSubject();
      if (subject.getSourceId().equals(attributeIdentifer.getSource())) {
        attribute.getValues().addAll(SubjectAttributeDefinition.getValues(subject, attributeIdentifer.getId()));
      }
    }

    return attribute;
  }
}
