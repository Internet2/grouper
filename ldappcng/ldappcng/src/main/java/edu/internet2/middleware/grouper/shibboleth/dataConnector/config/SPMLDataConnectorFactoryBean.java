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

  protected Object createInstance() throws Exception {
    SPMLDataConnector connector = new SPMLDataConnector();
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