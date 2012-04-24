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

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.spml.PSP;
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

  private static final Logger LOG = LoggerFactory.getLogger(SPMLDataConnector.class);

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

  /** Cache of past search results. */
  private Cache resultsCache;

  public final String ID_ATTRIBUTE = "psoID";

  public SPMLDataConnector(Cache cache) {
    this.resultsCache = cache;
  }

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
    if (LOG.isTraceEnabled()) {
      for (String attrId : resolutionContext.getAttributeRequestContext().getRequestedAttributesIds()) {
        LOG.trace("resolve {} requested attribute '{}'", msg, attrId);
      }
    }

    Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

    // TODO correct way to check for null dependencies ?
    boolean hasDependencyValues = false;
    for (String dependencyId : this.getDependencyIds()) {
      ResolutionPlugIn<?> plugin = resolutionContext.getResolvedPlugins().get(dependencyId);
      if (plugin instanceof DataConnector) {
        Map<String, BaseAttribute> values = ((DataConnector) plugin).resolve(resolutionContext);
        if (!values.isEmpty()) {
          hasDependencyValues = true;
          break;
        }
      } else if (plugin instanceof AttributeDefinition) {
        BaseAttribute<?> attribute = ((AttributeDefinition) plugin).resolve(resolutionContext);
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
    query.setScope(scope);

    // search request
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setReturnData(returnData);
    searchRequest.setQuery(query);
    searchRequest.setRequestID(PSPUtil.uniqueRequestId());

    // create SearchRequest object just for cache retrieval, inelegant; the searchRequest has a unique requestID
    SearchRequest cacheRequest = new SearchRequest();
    cacheRequest.setReturnData(returnData);
    cacheRequest.setQuery(query);

    // attempt to get attributes from the cache
    attributes = retrieveAttributesFromCache(cacheRequest);

    // results not found in the cache
    if (attributes == null) {
      attributes = retrieveAttributesFromTarget(searchRequest);
      cacheResult(cacheRequest, attributes);
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

  private void buildAttributes(Map<String, BaseAttribute> attributes, PSO pso) {
    // identifier
    if (!attributes.containsKey(ID_ATTRIBUTE)) {
      attributes.put(ID_ATTRIBUTE, new BasicAttribute<PSOIdentifier>(ID_ATTRIBUTE));
    }
    attributes.get(ID_ATTRIBUTE).getValues().add(pso.getPsoID());

    // data
    if (returnData.equals(ReturnData.DATA)) {
      Extensible data = pso.getData();
      if (data != null) {
        // data
        Map<String, DSMLAttr> dsmlAttrs = PSP.getDSMLAttrMap(data);
        for (DSMLAttr dsmlAttr : dsmlAttrs.values()) {
          if (!attributes.containsKey(dsmlAttr.getName())) {
            attributes.put(dsmlAttr.getName(), new BasicAttribute<PSOIdentifier>(dsmlAttr.getName()));
          }
          for (DSMLValue dsmlValue : dsmlAttr.getValues()) {
            attributes.get(dsmlAttr.getName()).getValues().add(dsmlValue.getValue());
          }
        }
      }
    }

    // everything
    if (returnData.equals(ReturnData.EVERYTHING)) {
      Map<String, List<Reference>> references = PSP.getReferences(pso.getCapabilityData());
      for (String typeOfReference : references.keySet()) {
        if (!attributes.containsKey(typeOfReference)) {
          attributes.put(typeOfReference, new BasicAttribute<PSOIdentifier>(typeOfReference));
        }
        for (Reference reference : references.get(typeOfReference)) {
          attributes.get(typeOfReference).getValues().add(reference.getToPsoID().getID());
        }
      }
    }
  }

  protected void cacheResult(SearchRequest searchRequest, Map<String, BaseAttribute> attributes) {
    if (resultsCache == null) {
      return;
    }

    LOG.debug("SPML data connector {} - Caching attributes from search '{}'", getId(), PSPUtil.toString(searchRequest));
    resultsCache.put(new Element(searchRequest, attributes));
  }

  protected Map<String, BaseAttribute> retrieveAttributesFromCache(SearchRequest searchRequest) {
    if (resultsCache == null) {
      return null;
    }

    LOG.debug("SPML data connector {} - Checking cache for search results {}", getId(), PSPUtil.toString(searchRequest));
    Element cachedResult = resultsCache.get(searchRequest);
    if (cachedResult != null && !cachedResult.isExpired()) {
      LOG.debug("SPML data connector {} - Returning attributes from cache {}", getId(), PSPUtil.toString(searchRequest));
      return (Map<String, BaseAttribute>) cachedResult.getObjectValue();
    }

    LOG.debug("SPML data connector {} - No results cached for search filter '{}'", getId(), PSPUtil.toString(searchRequest));
    return null;
  }

  protected Map<String, BaseAttribute> retrieveAttributesFromTarget(SearchRequest searchRequest)
      throws AttributeResolutionException {

    String msg = "retrieve attributes from target";

    Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

    // execute search
    LOG.debug("{}", PSPUtil.toString(searchRequest));
    Response response = getProvider().execute(searchRequest);
    LOG.debug("{}", PSPUtil.toString(response));

    if (!(response instanceof SearchResponse)) {
      LOG.error("resolve {} Unable to resolve attributes, expected a SearchResponse but received {}", msg,
          response.getClass());
      throw new AttributeResolutionException("Unable to resolve attributes, expected a SearchResponse");
    }

    SearchResponse searchResponse = (SearchResponse) response;

    // TODO proper handling of status=failure ?
    if (response.getStatus().equals(StatusCode.FAILURE)) {
      LOG.error("Unable to resolve " + msg + " " + response.getError() + " "
          + Arrays.asList(response.getErrorMessages()));
      throw new AttributeResolutionException("Unable to resolve " + msg + " " + response.getError());
    }

    // TODO proper handling of status=pending ?
    if (searchResponse.getStatus().equals(StatusCode.PENDING)) {
      LOG.error("Unable to resolve " + msg + " " + ErrorCode.UNSUPPORTED_EXECUTION_MODE);
      throw new AttributeResolutionException("Unable to resolve " + msg + " " + ErrorCode.UNSUPPORTED_EXECUTION_MODE);
    }

    if (searchResponse.getStatus().equals(StatusCode.SUCCESS)) {
      for (PSO pso : searchResponse.getPSOs()) {
        buildAttributes(attributes, pso);
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
