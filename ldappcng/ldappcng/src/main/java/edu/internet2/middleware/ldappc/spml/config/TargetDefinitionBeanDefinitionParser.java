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
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

public class TargetDefinitionBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(TargetDefinitionBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "target");

  protected Class getBeanClass(Element element) {
    // NOPE return PSOTargetDefinitionFactoryBean.class;
    return TargetDefinition.class;
  }

  /** {@inheritDoc} */
  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, builder);

    String id = element.getAttributeNS(null, "id");
    LOG.debug("Setting id of element '{}' to: '{}'", element.getLocalName(), id);
    builder.addPropertyValue("id", id);

    String provider = element.getAttributeNS(null, "provider");
    LOG.debug("Setting provider of element '{}' to: '{}'", element.getLocalName(), provider);
    builder.addPropertyReference("provider", provider);

    Map<QName, List<Element>> configChildren = XMLHelper.getChildElements(element);
    builder.addPropertyValue("psoDefinitions", SpringConfigurationUtils.parseInnerCustomElements(configChildren
        .get(PSODefinitionBeanDefinitionParser.TYPE_NAME), parserContext));
    
    String bundleModifications = element.getAttributeNS(null, "bundleModifications");
    LOG.debug("Setting bundleModifications of target '{}' to: '{}'", id, bundleModifications);
    builder.addPropertyValue("bundleModifications", bundleModifications);        
  }
}
