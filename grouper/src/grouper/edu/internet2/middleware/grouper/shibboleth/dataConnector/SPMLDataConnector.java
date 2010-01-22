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

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.provider.SpmlProvider;
import edu.internet2.middleware.ldappc.spml.request.LdapFilterQueryClause;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ResolutionPlugIn;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine.CharacterEscapingStrategy;

public class SPMLDataConnector extends BaseDataConnector {

  private static final Logger LOG = GrouperUtil.getLogger(SPMLDataConnector.class);

  public static final String PRINCIPAL = "${principal}";

  private String base;

  private SpmlProvider provider;

  private ReturnData returnData;

  private Scope scope;

  /** Template engine used to change filter template into actual filter. */
  private TemplateEngine filterCreator;

  /** Name the filter template is registered under within the template engine. */
  private String filterTemplateName;

  /** Template that produces the query to use. */
  private String filterTemplate;

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public TemplateEngine getTemplateEngine() {
    return filterCreator;
  }

  public void setTemplateEngine(TemplateEngine filterCreator) {
    this.filterCreator = filterCreator;
    registerTemplate();
  }

  public String getFilterTemplate() {
    return filterTemplate;
  }

  public void setFilterTemplate(String filterTemplate) {
    this.filterTemplate = filterTemplate;
  }

  public SpmlProvider getProvider() {
    return provider;
  }

  public void setProvider(SpmlProvider provider) {
    this.provider = provider;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public ReturnData getReturnData() {
    return returnData;
  }

  public void setReturnData(ReturnData returnData) {
    this.returnData = returnData;
  }

  public void initialize() {
    LOG.debug("initialize");
  }

  protected void registerTemplate() {
    filterTemplateName = "grouper.dc.spml." + getId();
    filterCreator.registerTemplate(filterTemplateName, filterTemplate);
  }

  public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "'" + principalName + "' dc '" + this.getId() + "'";
    LOG.debug("resolve {}", msg);
    if (LOG.isDebugEnabled()) {
      for (String attrId : resolutionContext.getAttributeRequestContext().getRequestedAttributesIds()) {
        LOG.trace("resolve {} requested attribute '{}'", msg, attrId);
      }
    }

    Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

    // TODO correct way to check for null dependencies ?
    boolean hasDependencyValues = false;
    for (String dependencyId : this.getDependencyIds()) {
      ResolutionPlugIn plugin = resolutionContext.getResolvedPlugins().get(dependencyId);
      if (plugin instanceof DataConnector) {
        Map<String, BaseAttribute> values = ((DataConnector) plugin).resolve(resolutionContext);
        if (!values.isEmpty()) {
          hasDependencyValues = true;
          break;
        }
      } else if (plugin instanceof AttributeDefinition) {
        BaseAttribute attribute = ((AttributeDefinition) plugin).resolve(resolutionContext);
        if (!attribute.getValues().isEmpty()) {
          hasDependencyValues = true;
          break;
        }
      } else {
        LOG.error("Unable to locate resolution plugin {}", dependencyId);
      }
    }

    if (!hasDependencyValues) {
      LOG.debug("resolve {} no dependency values", msg);
      return attributes;
    }

    // TODO escaping strategy
    String filter = filterCreator.createStatement(filterTemplateName, resolutionContext, getDependencyIds(),
        new LDAPValueEscapingStrategy());
    LOG.debug("resolve {} search filter '{}'", msg, filter);

    // TODO correct way to shortcut search ?
    if (filter.equals(filterTemplate)) {
      LOG.debug("resolve {} unable to evaluate filter template", msg);
      return attributes;
    }

    // TODO custom filter
    LdapFilterQueryClause filterQueryClause = new LdapFilterQueryClause();
    filterQueryClause.setFilter(filter);

    // base psoid
    PSOIdentifier basePsoID = new PSOIdentifier();
    basePsoID.setID(this.getBase());

    // search query
    Query query = new Query();
    query.setBasePsoID(basePsoID);
    query.addQueryClause(filterQueryClause);

    // search request
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setReturnData(returnData);
    searchRequest.setQuery(query);

    // execute search
    Response searchResponse = getProvider().execute(searchRequest);

    if (searchResponse.getStatus().equals(StatusCode.FAILURE)) {
      LOG.debug("resolve {} search failed {}", searchResponse.getError());
      return attributes;
    }

    if (!(searchResponse instanceof SearchResponse)) {
      LOG.error("resolve {} Unable to resolve attributes, expected a SearchResponse but received {}", msg,
          searchResponse.getClass());
      throw new AttributeResolutionException("Unable to resolve attributes, expected a SearchResponse");
    }

    // TODO pending status ?

    // TODO return data
    
    if (searchResponse.getStatus().equals(StatusCode.SUCCESS)) {
      SearchResponse sr = (SearchResponse) searchResponse;
      for (PSO pso : sr.getPSOs()) {
        // TODO identifier attr name
        BasicAttribute<PSOIdentifier> basicAttribute = new BasicAttribute<PSOIdentifier>("identifier");
        basicAttribute.setValues(Arrays.asList(new PSOIdentifier[] { pso.getPsoID() }));
        attributes.put(basicAttribute.getId(), basicAttribute);
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("resolve {} attributes {}", msg, attributes.size());
      for (String key : attributes.keySet()) {
        for (Object value : attributes.get(key).getValues()) {
          LOG.debug("resolve {} '{}' : {}", new Object[] { msg, key, PSPUtil.getString(value) });
        }
      }
    }

    return attributes;
  }

  public void validate() throws AttributeResolutionException {
  }

  /**
   * Escapes values that will be included within an LDAP filter.
   */
  protected class LDAPValueEscapingStrategy implements CharacterEscapingStrategy {

    /** {@inheritDoc} */
    public String escape(String value) {
      return value.replace("*", "\\*").replace("(", "\\(").replace(")", "\\)").replace("\\", "\\");
    }
  }

}
