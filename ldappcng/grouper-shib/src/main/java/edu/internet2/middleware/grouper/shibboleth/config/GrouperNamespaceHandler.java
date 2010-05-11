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

import edu.internet2.middleware.grouper.shibboleth.attribute.config.SimpleAttributeAuthorityBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.GroupAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.MemberAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config.SubjectAttributeDefinitionBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.GroupDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.MemberDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.StemDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.AndGroupQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.ExactAttributeGroupQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.MinusGroupQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.OrGroupQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.StemNameGroupQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifierBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.ClasspathPropertyReplacementResourceFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.SourceIdentifierBeanDefinitionParser;
import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

public class GrouperNamespaceHandler extends BaseSpringNamespaceHandler {

  public static final String NAMESPACE = "http://grouper.internet2.edu/shibboleth/2.0";

  public void init() {

    registerBeanDefinitionParser(GroupDataConnectorBeanDefinitionParser.TYPE_NAME,
        new GroupDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(MemberDataConnectorBeanDefinitionParser.TYPE_NAME,
        new MemberDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(StemDataConnectorBeanDefinitionParser.TYPE_NAME,
        new StemDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(AttributeIdentifierBeanDefinitionParser.TYPE_NAME,
        new AttributeIdentifierBeanDefinitionParser());

    registerBeanDefinitionParser(AttributeIdentifierBeanDefinitionParser.SUBJECT_ID_TYPE_NAME,
        new AttributeIdentifierBeanDefinitionParser());

    registerBeanDefinitionParser(MemberAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new MemberAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(GroupAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new GroupAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(SubjectAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
        new SubjectAttributeDefinitionBeanDefinitionParser());

    registerBeanDefinitionParser(AndGroupQueryFilterBeanDefinitionParser.TYPE_NAME,
        new AndGroupQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(ExactAttributeGroupQueryFilterBeanDefinitionParser.TYPE_NAME,
        new ExactAttributeGroupQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(OrGroupQueryFilterBeanDefinitionParser.TYPE_NAME,
        new OrGroupQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(StemNameGroupQueryFilterBeanDefinitionParser.TYPE_NAME,
        new StemNameGroupQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(SimpleAttributeAuthorityBeanDefinitionParser.TYPE_NAME,
        new SimpleAttributeAuthorityBeanDefinitionParser());

    registerBeanDefinitionParser(SourceIdentifierBeanDefinitionParser.TYPE_NAME,
        new SourceIdentifierBeanDefinitionParser());

    registerBeanDefinitionParser(MinusGroupQueryFilterBeanDefinitionParser.TYPE_NAME,
        new MinusGroupQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(ClasspathPropertyReplacementResourceFilterBeanDefinitionParser.TYPE_NAME,
        new ClasspathPropertyReplacementResourceFilterBeanDefinitionParser());
  }
}