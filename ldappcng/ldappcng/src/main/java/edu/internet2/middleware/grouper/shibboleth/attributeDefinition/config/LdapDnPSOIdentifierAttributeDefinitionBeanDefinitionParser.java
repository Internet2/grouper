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

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.LdapDnPSOIdentifierAttributeDefinition;
import edu.internet2.middleware.ldappc.spml.config.LdappcNamespaceHandler;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition.BaseAttributeDefinitionBeanDefinitionParser;

/** Spring bean definition parser for configuring a {@link LdapDnPSOIdentifierAttributeDefinition}. */
public class LdapDnPSOIdentifierAttributeDefinitionBeanDefinitionParser extends
    BaseAttributeDefinitionBeanDefinitionParser {

  /** Schema type name. */
  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "LdapDnPSOIdentifier");

  /** The logger. */
  private final Logger LOG = LoggerFactory.getLogger(LdapDnPSOIdentifierAttributeDefinitionBeanDefinitionParser.class);

  /** {@inheritDoc} */
  protected Class getBeanClass(Element element) {
    return LdapDnPSOIdentifierAttributeDefinitionFactoryBean.class;
  }

  /** {@inheritDoc} */
  protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
      BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
    super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

    String base = pluginConfig.getAttributeNS(null, "base");
    LOG.debug("Setting base of element '{}' to: '{}'", pluginConfig.getLocalName(), base);
    pluginBuilder.addPropertyValue("base", base);

    String structure = pluginConfig.getAttributeNS(null, "structure");
    LOG.debug("Setting structure of element '{}' to: '{}'", pluginConfig.getLocalName(), structure);
    pluginBuilder.addPropertyValue("structure", structure);

    String rdnAttributeName = pluginConfig.getAttributeNS(null, "rdnAttributeName");
    LOG.debug("Setting rdnAttributeName of element '{}' to: '{}'", pluginConfig.getLocalName(), rdnAttributeName);
    pluginBuilder.addPropertyValue("rdnAttributeName", rdnAttributeName);
  }
}
