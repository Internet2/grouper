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

package edu.internet2.middleware.grouper.shibboleth.config;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.shibboleth.Attribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class AttributeBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final Logger LOG = GrouperUtil.getLogger(AttributeBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(GrouperNamespaceHandler.NAMESPACE, "Attribute");

  protected Class getBeanClass(Element element) {
    return Attribute.class;
  }

  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(element, parserContext, builder);

    String id = element.getAttributeNS(null, "id");
    LOG.debug("Setting id of element '{}' to '{}'", element.getLocalName(), id);
    builder.addPropertyValue("id", id);

    String source = element.getAttributeNS(null, "source");
    LOG.debug("Setting source of element '{}' to '{}'", element.getLocalName(), source);
    builder.addPropertyValue("source", source);
  }

  protected boolean shouldGenerateId() {
    return true;
  }
}
