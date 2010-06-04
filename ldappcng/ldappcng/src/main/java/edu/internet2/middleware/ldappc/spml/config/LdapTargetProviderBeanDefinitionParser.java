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
import edu.internet2.middleware.shibboleth.common.config.service.AbstractServiceBeanDefinitionParser;

public class LdapTargetProviderBeanDefinitionParser extends AbstractServiceBeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(LdapTargetProviderBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "LdapPoolProvider");

  protected Class getBeanClass(Element element) {
    return LdapTargetProvider.class;
  }

  protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(configElement, parserContext, builder);

    String id = configElement.getAttributeNS(null, "id");
    LOG.debug("Setting id to '{}'", id);
    builder.addPropertyValue("id", id);

    String ldapPoolId = configElement.getAttributeNS(null, "ldapPoolId");
    LOG.debug("Setting ldapPoolId to '{}'", ldapPoolId);
    builder.addPropertyValue("ldapPoolId", ldapPoolId);
    
    String logLdif = configElement.getAttributeNS(null, "logLdif");
    LOG.debug("Setting logLdif to '{}'", logLdif);
    builder.addPropertyValue("logLdif", logLdif);
    
    String logSpml = configElement.getAttributeNS(null, "logSpml");
    LOG.debug("Setting logSpml to '{}'", logSpml);
    builder.addPropertyValue("logSpml", logSpml);
  }
}