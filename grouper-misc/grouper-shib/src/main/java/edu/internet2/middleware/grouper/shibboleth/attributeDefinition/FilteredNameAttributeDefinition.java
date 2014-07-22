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

import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.BaseAttributeDefinition;

/** An {@link AttributeDefinition} which returns filtered string values. */
public class FilteredNameAttributeDefinition<T> extends BaseAttributeDefinition {

  /** The logger. */
  private static Logger LOG = LoggerFactory.getLogger(FilteredNameAttributeDefinition.class);

  /** Filters the values returned. */
  private Filter<String> filter;

  /** {@inheritDoc} */
  protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

    LOG.debug("Filtered name attribute definition '{}' - Resolve principal '{}'", getId(), principalName);

    Collection<?> dependencyValues = getValuesFromAllDependencies(resolutionContext);

    BaseAttribute<String> attribute = new BasicAttribute<String>(getId());

    for (Object dependencyValue : dependencyValues) {
      if (filter.matches(dependencyValue.toString())) {
        attribute.getValues().add(dependencyValue.toString());
      }
    }

    if (LOG.isTraceEnabled()) {
      for (Object value : attribute.getValues()) {
        LOG.trace("Filtered name attribute definition '{}' - Resolve principal '{}' value '{}'", new Object[] {
            getId(), principalName, value });
      }
    }

    return attribute;
  }

  /**
   * Get the filter.
   * 
   * @return the filter
   */
  public Filter<String> getFilter() {
    return filter;
  }

  /**
   * Set the filter.
   * 
   * @param filter the Filter
   */
  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  /** {@inheritDoc} */
  public void validate() throws AttributeResolutionException {

  }
}
