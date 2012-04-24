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

import edu.internet2.middleware.ldappc.spml.definitions.PSOReferenceDefinition;

public class PSOReferenceDefinitionBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(PSOReferenceDefinitionBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "reference");

  protected Class getBeanClass(Element element) {
    return PSOReferenceDefinition.class;
  }

  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, builder);

    String ref = element.getAttributeNS(null, "ref");
    LOG.debug("Setting ref of element '{}' to '{}'", element.getLocalName(), ref);
    builder.addPropertyValue("ref", ref);

    // determine the targetID
    // FUTURE support other targets
    Element referencesElement = (Element) element.getParentNode();
    Element objectElement = (Element) referencesElement.getParentNode();
    String targetId = objectElement.getAttributeNS(null, "targetId");

    // the PSODefinition reference should be of the form objectId:targetId
    String toObject = element.getAttributeNS(null, "toObject");
    String toPSODefinition = targetId + ConfigBeanDefinitionParser.ID_DELIMITER + toObject;
    LOG.debug("Setting toPSODefinition of element '{}' to: '{}'", element.getLocalName(), toPSODefinition);
    builder.addPropertyReference("toPSODefinition", toPSODefinition);
    
    String onNotFound = element.getAttributeNS(null, "onNotFound");
    LOG.debug("Setting onNotFound of element '{}' to: '{}'", element.getLocalName(), onNotFound);
    builder.addPropertyValue("onNotFound", onNotFound);
    
    String multipleResults = element.getAttributeNS(null, "multipleResults");
    LOG.debug("Setting multipleResults of element '{}' to: '{}'", element.getLocalName(), multipleResults);
    builder.addPropertyValue("multipleResults", multipleResults);
  }

  protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
      throws BeanDefinitionStoreException {
    String ref = element.getAttributeNS(null, "ref");
    Element referencesElement = (Element) element.getParentNode();
    return PSOReferencesDefinitionBeanDefinitionParser.resolveId(referencesElement)
        + ConfigBeanDefinitionParser.ID_DELIMITER + ref;
  }
}
