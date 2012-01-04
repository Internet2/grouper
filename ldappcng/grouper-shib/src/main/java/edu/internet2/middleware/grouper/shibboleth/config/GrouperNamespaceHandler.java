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

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.GroupAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.MemberAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.SubjectAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.GroupDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.MemberDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.StemDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.AndFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.GroupExactAttributeFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.GroupInStemFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.MemberSourceFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.MinusFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.OrFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.StemInStemFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.StemNameExactFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifierBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.ClasspathPropertyReplacementResourceFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.SubjectIdentifierBeanDefinitionParser;
import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

/** Spring namespace handler for the Grouper attribute resolver namespace. */
public class GrouperNamespaceHandler extends BaseSpringNamespaceHandler {

  /** Namespace for this handler. */
  public static final String NAMESPACE = "http://grouper.internet2.edu/shibboleth/2.0";

  /** {@inheritDoc} */
  public void init() {

    registerBeanDefinitionParser(GroupDataConnectorBeanDefinitionParser.TYPE_NAME,
        new GroupDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(MemberDataConnectorBeanDefinitionParser.TYPE_NAME,
        new MemberDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(StemDataConnectorBeanDefinitionParser.TYPE_NAME,
        new StemDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(AttributeIdentifierBeanDefinitionParser.TYPE_NAME,
        new AttributeIdentifierBeanDefinitionParser());

    registerBeanDefinitionParser(SubjectIdentifierBeanDefinitionParser.TYPE_NAME,
        new SubjectIdentifierBeanDefinitionParser());

    registerBeanDefinitionParser(MemberAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new MemberAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(GroupAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new GroupAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(SubjectAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new SubjectAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(AndFilterBeanDefinitionParser.TYPE_NAME, new AndFilterBeanDefinitionParser());

    registerBeanDefinitionParser(GroupExactAttributeFilterBeanDefinitionParser.TYPE_NAME,
        new GroupExactAttributeFilterBeanDefinitionParser());

    registerBeanDefinitionParser(OrFilterBeanDefinitionParser.TYPE_NAME, new OrFilterBeanDefinitionParser());

    registerBeanDefinitionParser(GroupInStemFilterBeanDefinitionParser.TYPE_NAME,
        new GroupInStemFilterBeanDefinitionParser());

    registerBeanDefinitionParser(StemInStemFilterBeanDefinitionParser.TYPE_NAME,
        new StemInStemFilterBeanDefinitionParser());

    registerBeanDefinitionParser(StemNameExactFilterBeanDefinitionParser.TYPE_NAME,
        new StemNameExactFilterBeanDefinitionParser());

    registerBeanDefinitionParser(MinusFilterBeanDefinitionParser.TYPE_NAME, new MinusFilterBeanDefinitionParser());

    registerBeanDefinitionParser(ClasspathPropertyReplacementResourceFilterBeanDefinitionParser.TYPE_NAME,
        new ClasspathPropertyReplacementResourceFilterBeanDefinitionParser());

    registerBeanDefinitionParser(MemberSourceFilterBeanDefinitionParser.TYPE_NAME,
        new MemberSourceFilterBeanDefinitionParser());

  }
}