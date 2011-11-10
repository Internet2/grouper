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

import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.spml.provider.BaseSpmlProvider;
import edu.internet2.middleware.shibboleth.common.config.service.AbstractReloadableServiceBeanDefinitionParser;

/** Spring bean definition parser for configuring a {@link BaseSpmlProvider}. */
public abstract class BaseSpmlProviderBeanDefinitionParser extends AbstractReloadableServiceBeanDefinitionParser {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(BaseSpmlProviderBeanDefinitionParser.class);

  /** {@inheritDoc} */
  protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
    super.doParse(configElement, parserContext, builder);

    String id = configElement.getAttributeNS(null, "id");
    LOG.debug("Setting id to '{}'", id);
    builder.addPropertyValue("id", id);

    if (configElement.hasAttributeNS(null, "logSpml")) {
      Attr attr = configElement.getAttributeNodeNS(null, "logSpml");
      builder.addPropertyValue("logSpml", XMLHelper.getAttributeValueAsBoolean(attr));
    }

    if (configElement.hasAttributeNS(null, "logLdif")) {
      Attr attr = configElement.getAttributeNodeNS(null, "logLdif");
      builder.addPropertyValue("logLdif", XMLHelper.getAttributeValueAsBoolean(attr));
    }

    if (configElement.hasAttributeNS(null, "pathToOutputFile")) {
      String pathToOutputFile = configElement.getAttributeNS(null, "pathToOutputFile");
      builder.addPropertyValue("pathToOutputFile", pathToOutputFile);
    }

    if (configElement.hasAttributeNS(null, "writeRequests")) {
      Attr attr = configElement.getAttributeNodeNS(null, "writeRequests");
      builder.addPropertyValue("writeRequests", XMLHelper.getAttributeValueAsBoolean(attr));
    }

    if (configElement.hasAttributeNS(null, "writeResponses")) {
      Attr attr = configElement.getAttributeNodeNS(null, "writeResponses");
      builder.addPropertyValue("writeResponses", XMLHelper.getAttributeValueAsBoolean(attr));
    }
  }
}