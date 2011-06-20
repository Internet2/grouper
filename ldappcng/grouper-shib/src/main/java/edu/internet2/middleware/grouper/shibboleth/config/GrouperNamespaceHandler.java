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
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.FindGroupByNameDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.FindMemberBySubjectIdOrIdentifierDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.config.FindStemByNameDataConnectorBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.AndMatchQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.GroupsByExactAttributeMatchQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.MinusMatchQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.OrMatchQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.filter.provider.GroupsInStemMatchQueryFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifierBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.ClasspathPropertyReplacementResourceFilterBeanDefinitionParser;
import edu.internet2.middleware.grouper.shibboleth.util.SourceIdentifierBeanDefinitionParser;
import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

public class GrouperNamespaceHandler extends BaseSpringNamespaceHandler {

  public static final String NAMESPACE = "http://grouper.internet2.edu/shibboleth/2.0";

  public void init() {

    registerBeanDefinitionParser(FindGroupByNameDataConnectorBeanDefinitionParser.TYPE_NAME,
        new FindGroupByNameDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(FindMemberBySubjectIdOrIdentifierDataConnectorBeanDefinitionParser.TYPE_NAME,
        new FindMemberBySubjectIdOrIdentifierDataConnectorBeanDefinitionParser());

    registerBeanDefinitionParser(FindStemByNameDataConnectorBeanDefinitionParser.TYPE_NAME,
        new FindStemByNameDataConnectorBeanDefinitionParser());

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

    registerBeanDefinitionParser(AndMatchQueryFilterBeanDefinitionParser.TYPE_NAME,
        new AndMatchQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(GroupsByExactAttributeMatchQueryFilterBeanDefinitionParser.TYPE_NAME,
        new GroupsByExactAttributeMatchQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(OrMatchQueryFilterBeanDefinitionParser.TYPE_NAME,
        new OrMatchQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(GroupsInStemMatchQueryFilterBeanDefinitionParser.TYPE_NAME,
        new GroupsInStemMatchQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(SimpleAttributeAuthorityBeanDefinitionParser.TYPE_NAME,
        new SimpleAttributeAuthorityBeanDefinitionParser());

    registerBeanDefinitionParser(SourceIdentifierBeanDefinitionParser.TYPE_NAME,
        new SourceIdentifierBeanDefinitionParser());

    registerBeanDefinitionParser(MinusMatchQueryFilterBeanDefinitionParser.TYPE_NAME,
        new MinusMatchQueryFilterBeanDefinitionParser());

    registerBeanDefinitionParser(ClasspathPropertyReplacementResourceFilterBeanDefinitionParser.TYPE_NAME,
        new ClasspathPropertyReplacementResourceFilterBeanDefinitionParser());
  }
}