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

package edu.internet2.middleware.grouper.shibboleth.dataConnector.config;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spmlsearch.Scope;

import edu.internet2.middleware.grouper.shibboleth.dataConnector.SPMLDataConnector;
import edu.internet2.middleware.ldappc.spml.provider.SpmlProvider;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;

/**
 * Spring bean factory that produces {@link SPMLDataConnector}s.
 */
public class SPMLDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

  /** Template engine used to construct filter queries. */
  private TemplateEngine templateEngine;

  private String base;

  private String filterTemplate;

  private SpmlProvider provider;

  private ReturnData returnData;

  private Scope scope;

  /** Whether results should be cached. */
  private CacheManager cacheManager;

  /** Maximum number of queries to keep in the cache. */
  private int maximumCachedElements;

  /** Length of time, in milliseconds, elements are cached. */
  private long cacheElementTtl;

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
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

  public ReturnData getReturnData() {
    return returnData;
  }

  public void setReturnData(ReturnData returnData) {
    this.returnData = returnData;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public TemplateEngine getTemplateEngine() {
    return templateEngine;
  }

  public void setTemplateEngine(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  /**
   * Gets the manager for the results cache.
   * 
   * @return manager for the results cache
   */
  public CacheManager getCacheManager() {
    return cacheManager;
  }

  /**
   * Sets the manager for the results cache.
   * 
   * @param manager manager for the results cache
   */
  public void setCacheManager(CacheManager manager) {
    cacheManager = manager;
  }

  /**
   * Gets the time to live, in milliseconds, for cache elements.
   * 
   * @return time to live, in milliseconds, for cache elements
   */
  public long getCacheElementTimeToLive() {
    return cacheElementTtl;
  }

  /**
   * Sets the time to live, in milliseconds, for cache elements.
   * 
   * @param ttl time to live, in milliseconds, for cache elements
   */
  public void setCacheElementTimeToLive(long ttl) {
    cacheElementTtl = ttl;
  }

  /**
   * Gets the maximum number of elements that will be cached.
   * 
   * @return maximum number of elements that will be cached
   */
  public int getMaximumCachedElements() {
    return maximumCachedElements;
  }

  /**
   * Sets the maximum number of elements that will be cached.
   * 
   * @param max maximum number of elements that will be cached
   */
  public void setMaximumCachedElements(int max) {
    maximumCachedElements = max;
  }

  protected Object createInstance() throws Exception {

    Cache resultsCache = null;
    if (cacheManager != null) {
      resultsCache = cacheManager.getCache(getPluginId());
      if (resultsCache == null) {
        long ttlInSeconds = cacheElementTtl / 1000;
        resultsCache = new Cache(getPluginId(), maximumCachedElements, false, false, ttlInSeconds, ttlInSeconds);
        cacheManager.addCache(resultsCache);
      }
    }

    SPMLDataConnector connector = new SPMLDataConnector(resultsCache);
    populateDataConnector(connector);
    connector.setBase(base);
    connector.setFilterTemplate(filterTemplate);
    connector.setProvider(provider);
    connector.setReturnData(returnData);
    connector.setScope(scope);
    connector.setTemplateEngine(templateEngine);
    connector.initialize();
    return connector;
  }

  public Class getObjectType() {
    return SPMLDataConnector.class;
  }

}
