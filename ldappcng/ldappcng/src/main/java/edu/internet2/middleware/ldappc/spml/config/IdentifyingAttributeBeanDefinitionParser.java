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

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.spml.definitions.IdentifyingAttribute;

public class IdentifyingAttributeBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(IdentifyingAttributeBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "identifyingAttribute");

  protected Class getBeanClass(Element element) {
    return IdentifyingAttribute.class;
  }

  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, builder);

    String name = element.getAttributeNS(null, "name");
    LOG.debug("Setting name of element '{}' to '{}'", element.getLocalName(), name);
    builder.addPropertyValue("name", name);

    String value = element.getAttributeNS(null, "value");
    LOG.debug("Setting value of element '{}' to '{}'", element.getLocalName(), value);
    builder.addPropertyValue("value", value);
  }

  /**
   * TODO
   * 
   * targetId:objectID:identifierRef:name
   * 
   * @param element
   * @return
   */
  protected static String resolveId(Element element) {
    String name = element.getAttributeNS(null, "name");
    Element identifierElement = (Element) element.getParentNode();
    return PSOIdentifierDefinitionBeanDefinitionParser.resolveId(identifierElement)
        + ConfigBeanDefinitionParser.ID_DELIMITER + name;
  }

  protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
      throws BeanDefinitionStoreException {
    return resolveId(element);
  }

}
