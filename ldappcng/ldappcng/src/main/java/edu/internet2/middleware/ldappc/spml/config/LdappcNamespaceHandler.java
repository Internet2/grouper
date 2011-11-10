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

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.LdapDnPSOIdentifierAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.PSOIdentifierAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.SPMLDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

public class LdappcNamespaceHandler extends BaseSpringNamespaceHandler {

  public static final String NAMESPACE = "http://grouper.internet2.edu/ldappc";

  public void init() {

    registerBeanDefinitionParser(PSPBeanDefinitionParser.TYPE_NAME, new PSPBeanDefinitionParser());

    registerBeanDefinitionParser(ConfigBeanDefinitionParser.TYPE_NAME, new ConfigBeanDefinitionParser());

    registerBeanDefinitionParser(TargetDefinitionBeanDefinitionParser.TYPE_NAME,
        new TargetDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(PSODefinitionBeanDefinitionParser.TYPE_NAME, new PSODefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(PSOIdentifierDefinitionBeanDefinitionParser.TYPE_NAME,
        new PSOIdentifierDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(PSOAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new PSOAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(PSOReferencesDefinitionBeanDefinitionParser.TYPE_NAME,
        new PSOReferencesDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(PSOReferenceDefinitionBeanDefinitionParser.TYPE_NAME,
        new PSOReferenceDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(LdapTargetProviderBeanDefinitionParser.TYPE_NAME,
        new LdapTargetProviderBeanDefinitionParser());

    registerBeanDefinitionParser(IdentifyingAttributeBeanDefinitionParser.TYPE_NAME,
        new IdentifyingAttributeBeanDefinitionParser());

    registerBeanDefinitionParser(PSOIdentifierAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new PSOIdentifierAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(LdapDnPSOIdentifierAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new LdapDnPSOIdentifierAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(SPMLDataConnectorBeanDefinitionParser.TYPE_NAME,
        new SPMLDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(AlternateIdentifierDefinitionBeanDefinitionParser.TYPE_NAME,
        new AlternateIdentifierDefinitionBeanDefinitionParser());
  }
}