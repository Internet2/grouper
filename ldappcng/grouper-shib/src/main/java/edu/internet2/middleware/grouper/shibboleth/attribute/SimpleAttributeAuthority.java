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

package edu.internet2.middleware.grouper.shibboleth.attribute;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.shibboleth.common.attribute.AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeRequestException;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.ShibbolethAttributeFilteringEngine;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethAttributeResolver;
import edu.internet2.middleware.shibboleth.common.config.BaseService;
import edu.internet2.middleware.shibboleth.common.profile.provider.SAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.service.ServiceException;

/**
 * A simple Attribute Authority which optionally filters attributes as they are returned
 * from the underlying Shibboleth Attribute Resolver.
 */
public class SimpleAttributeAuthority extends BaseService implements AttributeAuthority<SAMLProfileRequestContext> {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleAttributeAuthority.class);

  /** the resolver */
  private ShibbolethAttributeResolver attributeResolver;

  /** the filtering engine */
  private ShibbolethAttributeFilteringEngine filteringEngine;

  /**
   * Creates a Simple Attribute Authority.
   * 
   * @param resolver
   *          the ShibbolethAttributeResolver
   */
  public SimpleAttributeAuthority(ShibbolethAttributeResolver resolver) {
    super();
    attributeResolver = resolver;
  }

  /**
   * Returns the attribute filtering engine.
   * 
   * @return filtering engine
   */
  public ShibbolethAttributeFilteringEngine getFilteringEngine() {
    return filteringEngine;
  }

  /**
   * Set the attribute filtering engine.
   * 
   * @param engine
   *          the AttributeFilteringEngine
   */
  public void setFilteringEngine(ShibbolethAttributeFilteringEngine engine) {
    filteringEngine = engine;
  }

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> getAttributes(SAMLProfileRequestContext requestContext)
      throws AttributeRequestException {

    String principalName = requestContext.getPrincipalName();

    String msg = "get attributes '" + principalName + "' aa '" + this.getId() + "'";
    LOG.debug("{}", msg);
    if (LOG.isTraceEnabled()) {
      if (requestContext.getRequestedAttributesIds() != null) {
        for (String attrId : requestContext.getRequestedAttributesIds()) {
          LOG.trace("resolve {} requested attribute '{}'", msg, attrId);
        }
      }
    }

    // Resolve attributes
    Map<String, BaseAttribute> attributes = attributeResolver.resolveAttributes(requestContext);

    // Filter resulting attributes
    if (filteringEngine != null) {
      attributes = filteringEngine.filterAttributes(attributes, requestContext);
    }

    if (LOG.isDebugEnabled()) {
      for (String key : attributes.keySet()) {
        for (Object value : attributes.get(key).getValues()) {
          LOG.debug("{} returned '{}' : {}", new Object[] { msg, key, value });
        }
      }
    }

    return attributes;
  }

  /** {@inheritDoc} */
  protected void onNewContextCreated(ApplicationContext newServiceContext) throws ServiceException {

  }

}
