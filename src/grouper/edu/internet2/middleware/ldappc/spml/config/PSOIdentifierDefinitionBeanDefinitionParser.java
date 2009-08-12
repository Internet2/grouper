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
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.definitions.PSOIdentifierDefinition;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

public class PSOIdentifierDefinitionBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = GrouperUtil.getLogger(PSOIdentifierDefinitionBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "identifier");

  protected Class getBeanClass(Element element) {
    return PSOIdentifierDefinition.class;
  }

  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, builder);

    String ref = element.getAttributeNS(null, "ref");
    LOG.debug("Setting ref of element '{}' to '{}'", element.getLocalName(), ref);
    builder.addPropertyValue("ref", ref);

    String baseId = element.getAttributeNS(null, "baseId");
    LOG.debug("Setting baseId of element '{}' to '{}'", element.getLocalName(), baseId);
    builder.addPropertyValue("baseId", baseId);

    Element objectElement = (Element) element.getParentNode();
    String targetId = objectElement.getAttributeNS(null, "targetId");
    LOG.debug("Setting targetId of element '{}' to '{}'", element.getLocalName(), targetId);
    builder.addPropertyReference("targetDefinition", targetId);

    Map<QName, List<Element>> configChildren = XMLHelper.getChildElements(element);

    builder.addPropertyValue("identifyingAttribute", SpringConfigurationUtils.parseInnerCustomElement(configChildren
        .get(IdentifyingAttributeBeanDefinitionParser.TYPE_NAME).get(0), parserContext));
  }

  /**
   * TODO
   * 
   * targetId:objectID:ref
   * 
   * @param element
   * @return
   */
  protected static String resolveId(Element element) {
    String ref = element.getAttributeNS(null, "ref");
    Element objectElement = (Element) element.getParentNode();
    return PSODefinitionBeanDefinitionParser.resolveId(objectElement) + ConfigBeanDefinitionParser.ID_DELIMITER + ref;
  }

  protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
      throws BeanDefinitionStoreException {
    return resolveId(element);
  }
}
