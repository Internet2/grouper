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

package edu.internet2.middleware.ldappc.spml.config;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.spml.provider.LdapTargetProvider;

/** Spring bean definition parser for configuring a {@link LdapTargetProvider}. */
public class LdapTargetProviderBeanDefinitionParser extends BaseSpmlProviderBeanDefinitionParser {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(LdapTargetProviderBeanDefinitionParser.class);

  /** Schema type name. */
  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "LdapPoolProvider");

  /** {@inheritDoc} */
  protected Class getBeanClass(Element element) {
    return LdapTargetProvider.class;
  }

  /** {@inheritDoc} */
  protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(configElement, parserContext, builder);

    String ldapPoolId = configElement.getAttributeNS(null, "ldapPoolId");
    LOG.debug("Setting ldapPoolId to '{}'", ldapPoolId);
    builder.addPropertyValue("ldapPoolId", ldapPoolId);
  }
}