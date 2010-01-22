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
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.shibboleth.common.config.service.AbstractReloadableServiceBeanDefinitionParser;

public class PSPBeanDefinitionParser extends AbstractReloadableServiceBeanDefinitionParser {

  private static final Logger LOG = GrouperUtil.getLogger(PSPBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "ProvisioningServiceProvider");

  protected Class getBeanClass(Element element) {
    return PSP.class;
  }

  protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(configElement, parserContext, builder);

    String id = configElement.getAttributeNS(null, "id");
    LOG.debug("Setting id to '{}'", id);
    builder.addPropertyValue("id", id);

    String attributeAuthorityId = configElement.getAttributeNS(null, "authority");
    LOG.debug("Setting attributeAuthority to '{}'", attributeAuthorityId);
    builder.addPropertyReference("attributeAuthority", attributeAuthorityId);
  }
}