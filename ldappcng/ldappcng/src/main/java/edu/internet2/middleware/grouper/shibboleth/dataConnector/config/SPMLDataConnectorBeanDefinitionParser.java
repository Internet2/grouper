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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.util.BasicStringEnumConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.spml.config.LdappcNamespaceHandler;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorBeanDefinitionParser;

public class SPMLDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser {

  private final Logger LOG = LoggerFactory.getLogger(SPMLDataConnectorBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "SPMLDataConnector");

  public static final QName FILTER_ELEMENT_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "FilterTemplate");

  protected Class<?> getBeanClass(Element element) {
    return SPMLDataConnectorFactoryBean.class;
  }

  protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
      BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
    super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

    String base = pluginConfig.getAttributeNS(null, "base");
    LOG.debug("Data connector {} base: {}", pluginId, base);
    pluginBuilder.addPropertyValue("base", base);

    String returnDataString = pluginConfig.getAttributeNS(null, "returnData");
    LOG.debug("Data connector {} returnData: {}", pluginId, returnDataString);
    ReturnData returnData = (ReturnData) BasicStringEnumConstant.getConstant(ReturnData.class, returnDataString);
    pluginBuilder.addPropertyValue("returnData", returnData);

    String scopeString = pluginConfig.getAttributeNS(null, "scope");
    LOG.debug("Data connector {} scope: {}", pluginId, scopeString);
    Scope scope = (Scope) BasicStringEnumConstant.getConstant(Scope.class, scopeString);
    pluginBuilder.addPropertyValue("scope", scope);

    String provider = pluginConfig.getAttributeNS(null, "provider");
    LOG.debug("Data connector {} provider: {}", pluginId, provider);
    pluginBuilder.addPropertyReference("provider", provider);

    String filterTemplate = pluginConfigChildren.get(FILTER_ELEMENT_NAME).get(0).getTextContent();
    filterTemplate = DatatypeHelper.safeTrimOrNullString(filterTemplate);
    LOG.debug("Data connector {} LDAP filter template: {}", pluginId, filterTemplate);
    pluginBuilder.addPropertyValue("filterTemplate", filterTemplate);

    String templateEngineRef = pluginConfig.getAttributeNS(null, "templateEngine");
    pluginBuilder.addPropertyReference("templateEngine", templateEngineRef);

    processCacheConfig(pluginId, pluginConfig, pluginBuilder);
  }

  /**
   * Processes the cache configuration directives.
   * 
   * @param pluginId ID of the plugin
   * @param pluginConfig configuration element for the plugin
   * @param pluginBuilder builder for the plugin
   */
  protected void processCacheConfig(String pluginId, Element pluginConfig, BeanDefinitionBuilder pluginBuilder) {
    boolean cacheResults = false;
    String cacheManagerId = "shibboleth.CacheManager";
    long cacheElementTtl = 4 * 60 * 60 * 1000;
    int maximumCachedElements = 500;

    List<Element> cacheConfigs = XMLHelper.getChildElementsByTagNameNS(pluginConfig, LdappcNamespaceHandler.NAMESPACE,
        "ResultCache");

    if (cacheConfigs != null && !cacheConfigs.isEmpty()) {

      Element cacheConfig = cacheConfigs.get(0);

      cacheResults = true;

      if (cacheConfig.hasAttributeNS(null, "cacheManagerRef")) {
        cacheManagerId = DatatypeHelper.safeTrim(cacheConfig.getAttributeNS(null, "cacheManagerRef"));
      }

      if (cacheConfig.hasAttributeNS(null, "elementTimeToLive")) {
        cacheElementTtl = SpringConfigurationUtils.parseDurationToMillis("elementTimeToLive on data connector "
            + pluginId, cacheConfig.getAttributeNS(null, "elementTimeToLive"), 0);
      }

      if (cacheConfig.hasAttributeNS(null, "maximumCachedElements")) {
        maximumCachedElements = Integer.parseInt(DatatypeHelper.safeTrim(cacheConfig.getAttributeNS(null,
            "maximumCachedElements")));
      }
    }

    if (pluginConfig.hasAttributeNS(null, "cacheResults")) {
      LOG.warn("Data connection {}: use of 'cacheResults' attribute is deprecated.  Use <ResultCache> instead.",
          pluginId);
      cacheResults = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null, "cacheResults"));
    }

    if (cacheResults) {
      LOG.debug("Data connector {} is caching results: {}", pluginId, cacheResults);

      pluginBuilder.addPropertyReference("cacheManager", cacheManagerId);

      LOG.debug("Data connector {} cache element time to live: {}ms", pluginId, cacheElementTtl);
      pluginBuilder.addPropertyValue("cacheElementTimeToLive", cacheElementTtl);

      LOG.debug("Data connector {} maximum number of caches elements: {}", pluginId, maximumCachedElements);
      pluginBuilder.addPropertyValue("maximumCachedElements", maximumCachedElements);
    }

  }
}
