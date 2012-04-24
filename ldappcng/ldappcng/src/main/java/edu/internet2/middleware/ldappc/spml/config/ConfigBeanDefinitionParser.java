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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

public class ConfigBeanDefinitionParser implements BeanDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigBeanDefinitionParser.class);

  public static final QName TYPE_NAME = new QName(LdappcNamespaceHandler.NAMESPACE, "ldappc");

  public static final String TARGET_TOK = "${target}";

  public static final String ID_DELIMITER = ":";

  public BeanDefinition parse(Element config, ParserContext context) {

    // rewrite
    Element newConfig = this.rewriteConfig(config);

    // targets elements
    List<Element> targetsElements = XMLHelper.getChildElementsByTagNameNS(newConfig, LdappcNamespaceHandler.NAMESPACE,
        "targets");

    // load target elements
    for (Element targetsElement : targetsElements) {
      Map<QName, List<Element>> configChildren = XMLHelper.getChildElements(targetsElement);
      List<Element> targets = configChildren.get(TargetDefinitionBeanDefinitionParser.TYPE_NAME);
      SpringConfigurationUtils.parseInnerCustomElements(targets, context);
    }

    return null;
  }

  /**
   * For every target element, rewrite object element by adding the targetId attribute
   * equal to the parent target's id attribute.
   * 
   * TODO I'm sure there's a better way.
   * 
   * @param config
   * @return rewritten config
   */
  public Element rewriteConfig(Element config) {
    LOG.trace("original config :\n{}", XMLHelper.prettyPrintXML(config));

    // clone original config element
    Element newConfig = (Element) config.cloneNode(true);

    List<Element> targetsElements = XMLHelper.getChildElementsByTagNameNS(newConfig, LdappcNamespaceHandler.NAMESPACE,
        "targets");

    // for every targets element
    for (Element targetsElement : targetsElements) {

      // target elements
      List<Element> targets = XMLHelper.getChildElementsByTagNameNS(targetsElement, LdappcNamespaceHandler.NAMESPACE,
          TargetDefinitionBeanDefinitionParser.TYPE_NAME.getLocalPart());

      // object elements
      List<Element> oldObjects = XMLHelper.getChildElementsByTagNameNS(targetsElement,
          LdappcNamespaceHandler.NAMESPACE, PSODefinitionBeanDefinitionParser.TYPE_NAME.getLocalPart());

      for (Element target : targets) {

        String targetId = target.getAttributeNS(null, "id");

        for (Element oldObject : oldObjects) {

          Element newObject = (Element) oldObject.cloneNode(true);
          newObject.setAttribute("targetId", targetId);
          LOG.debug("rewrote object '{}' with targetId '{}'", newObject.getAttributeNS(null, "id"), targetId);

          // NOPE add targetId to identifier element
          Element identifier = XMLHelper.getChildElementsByTagNameNS(newObject, LdappcNamespaceHandler.NAMESPACE,
              PSOIdentifierDefinitionBeanDefinitionParser.TYPE_NAME.getLocalPart()).get(0);
          // identifier.setAttribute("targetId", targetId);

          // rewrite id
          String ref = identifier.getAttribute("ref");
          if (ref.contains(TARGET_TOK)) {
            String rewrittenId = ref.replace(TARGET_TOK, targetId);
            LOG.debug("rewrote ref '{}' as '{}'", ref, rewrittenId);
            identifier.setAttribute("ref", rewrittenId);
          }

          // TODO rewrite more than just this attribute

          target.appendChild(newObject);
        }
      }

      for (int i = 0; i < oldObjects.size(); i++) {
        // object to remove
        Element object = oldObjects.get(i);
        Node parent = object.getParentNode();

        // remove old object
        parent.removeChild(object);
      }
    }

    LOG.trace("rewritten config :\n{}", XMLHelper.prettyPrintXML(newConfig));
    return newConfig;
  }
}
