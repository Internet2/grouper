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

import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.shibboleth.config.GrouperNamespaceHandler;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.MatchQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifierBeanDefinitionParser;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorBeanDefinitionParser;

/** Spring bean definition parser for configuring Grouper data connectors. */
public abstract class BaseGrouperDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser {

  /** {@inheritDoc} */
  protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
      BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {

    List<Element> fieldIdentifiers = XMLHelper.getChildElementsByTagNameNS(pluginConfig,
        GrouperNamespaceHandler.NAMESPACE, AttributeIdentifierBeanDefinitionParser.TYPE_NAME.getLocalPart());
    pluginBuilder.addPropertyValue("fieldIdentifiers",
        SpringConfigurationUtils.parseInnerCustomElements(fieldIdentifiers, parserContext));

    // TOOD is this the correct way to handle a single custom child element ?
    List<Element> matchQueryFilters = XMLHelper.getChildElementsByTagNameNS(pluginConfig,
        GrouperNamespaceHandler.NAMESPACE, MatchQueryFilterBeanDefinitionParser.MATCH_FILTER_TYPE_NAME.getLocalPart());
    if (!matchQueryFilters.isEmpty()) {
      pluginBuilder.addPropertyValue("matchQueryFilter",
          SpringConfigurationUtils.parseInnerCustomElement(matchQueryFilters.get(0), parserContext));
    }

    List<Element> subjectIdentifiers = XMLHelper.getChildElementsByTagNameNS(pluginConfig,
        GrouperNamespaceHandler.NAMESPACE, AttributeIdentifierBeanDefinitionParser.SUBJECT_ID_TYPE_NAME.getLocalPart());
    if (!subjectIdentifiers.isEmpty()) {
      pluginBuilder.addPropertyValue("subjectIdentifier",
          SpringConfigurationUtils.parseInnerCustomElement(subjectIdentifiers.get(0), parserContext));
    }
  }
}
