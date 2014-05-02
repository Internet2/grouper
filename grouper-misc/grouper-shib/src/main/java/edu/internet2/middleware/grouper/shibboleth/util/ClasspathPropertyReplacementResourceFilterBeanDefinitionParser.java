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
 * Copyright 2008 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.grouper.shibboleth.util;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.shibboleth.config.GrouperNamespaceHandler;
import edu.internet2.middleware.shibboleth.common.config.resource.PropertyReplacementResourceFilterBeanDefinitionParser;

/**
 * Allows the {@link PropertyReplacementResourceFilter} to read a properties file located
 * on the classpath.
 */
public class ClasspathPropertyReplacementResourceFilterBeanDefinitionParser extends
    PropertyReplacementResourceFilterBeanDefinitionParser {

  /** Schema type. */
  public static final QName TYPE_NAME = new QName(GrouperNamespaceHandler.NAMESPACE, "ClasspathPropertyReplacement");

  /** Class logger. */
  private Logger log = LoggerFactory.getLogger(ClasspathPropertyReplacementResourceFilterBeanDefinitionParser.class);

  /** {@inheritDoc} */
  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    String resource = DatatypeHelper.safeTrim(element.getAttributeNS(null, "propertyFile"));
    log.debug("Property file: {}", resource);

    URL url = getClass().getResource(resource);
    if (url == null) {
      throw new IllegalArgumentException("Classpath resource does not exist: " + resource);
    }

    File propertyFile = new File(url.getPath());
    log.debug("Property path: {}", propertyFile.getAbsolutePath());
    
    builder.addConstructorArgValue(propertyFile);
  }
}
