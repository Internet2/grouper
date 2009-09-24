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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.shibboleth.config.AttributeIdentifierBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.config.GrouperNamespaceHandler;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition.BaseAttributeDefinitionBeanDefinitionParser;

public class SubjectAttributeDefinitionBeanDefinitionParser extends BaseAttributeDefinitionBeanDefinitionParser {

  public static final QName TYPE_NAME = new QName(GrouperNamespaceHandler.NAMESPACE, "Subject");

  protected Class getBeanClass(Element element) {
    return SubjectAttributeDefinitionFactoryBean.class;
  }

  protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
      BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
    super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

    pluginBuilder.addPropertyValue("attributes", SpringConfigurationUtils.parseInnerCustomElements(pluginConfigChildren
        .get(AttributeIdentifierBeanDefinitionParser.TYPE_NAME), parserContext));
  }
}
