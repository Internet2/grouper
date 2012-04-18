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

import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

public class PSOReferencesDefinitionBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(PSOReferencesDefinitionBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "references");

  protected Class getBeanClass(Element element) {
    return PSOReferencesDefinition.class;
  }

  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, builder);

    String name = element.getAttributeNS(null, "name");
    LOG.debug("Setting name of element '{}' to: '{}'", element.getLocalName(), name);
    builder.addPropertyValue("name", name);

    if (element.hasAttributeNS(null, "emptyValue")) {
      String emptyValue = element.getAttributeNS(null, "emptyValue");
      LOG.debug("Setting emptyValue of element '{}' to: '{}'", element.getLocalName(), emptyValue);
      builder.addPropertyValue("emptyValue", emptyValue);
    }

    Map<QName, List<Element>> configChildren = XMLHelper.getChildElements(element);

    builder.addPropertyValue("psoReferenceDefinitions", SpringConfigurationUtils.parseInnerCustomElements(
        configChildren.get(PSOReferenceDefinitionBeanDefinitionParser.TYPE_NAME), parserContext));
  }

  /**
   * TODO
   * 
   * name:objectId:targetId
   * 
   * @param element
   * @return
   */
  protected static String resolveId(Element element) {
    String name = element.getAttributeNS(null, "name");
    Element objectElement = (Element) element.getParentNode();
    return PSODefinitionBeanDefinitionParser.resolveId(objectElement) + ConfigBeanDefinitionParser.ID_DELIMITER + name;
  }

  protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
      throws BeanDefinitionStoreException {
    return resolveId(element);
  }

}
