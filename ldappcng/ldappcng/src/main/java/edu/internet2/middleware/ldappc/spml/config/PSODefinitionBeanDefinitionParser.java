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

package edu.internet2.middleware.ldappc.spml.config;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

public class PSODefinitionBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(PSODefinitionBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "object");

  protected Class getBeanClass(Element element) {
    return PSODefinition.class;
  }

  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, builder);

    String id = element.getAttributeNS(null, "id");
    LOG.debug("Setting id of element '{}' to '{}'", element.getLocalName(), id);
    builder.addPropertyValue("id", id);

    String authoritative = element.getAttributeNS(null, "authoritative");
    LOG.debug("Setting authoritative of element '{}' to '{}'", element.getLocalName(), authoritative);
    builder.addPropertyValue("authoritative", authoritative);

    Map<QName, List<Element>> configChildren = XMLHelper.getChildElements(element);

    builder.addPropertyValue("psoIdentifierDefinition", SpringConfigurationUtils.parseInnerCustomElement(configChildren
        .get(PSOIdentifierDefinitionBeanDefinitionParser.TYPE_NAME).get(0), parserContext));

    builder.addPropertyValue("attributeDefinitions", SpringConfigurationUtils.parseInnerCustomElements(configChildren
        .get(PSOAttributeDefinitionBeanDefinitionParser.TYPE_NAME), parserContext));

    builder.addPropertyValue("referenceDefinitions", SpringConfigurationUtils.parseInnerCustomElements(configChildren
        .get(PSOReferencesDefinitionBeanDefinitionParser.TYPE_NAME), parserContext));
  }

  /**
   * TODO
   * 
   * targetId:id
   * 
   * @param element
   * @return
   */
  protected static String resolveId(Element element) {
    String id = element.getAttributeNS(null, "id");
    if (!element.hasAttributeNS(null, "targetId")) {
      LOG.error("Object element '{}' must have a targetId", id);
      throw new LdappcException("Object element must have a targetId");
    }
    String targetId = element.getAttributeNS(null, "targetId");
    return targetId + ConfigBeanDefinitionParser.ID_DELIMITER + id;
  }

  protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
      throws BeanDefinitionStoreException {
    return resolveId(element);
  }
}
