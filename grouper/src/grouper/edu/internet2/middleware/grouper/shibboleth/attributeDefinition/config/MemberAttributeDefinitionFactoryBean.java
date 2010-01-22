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

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.MemberAttributeDefinition;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition.BaseAttributeDefinitionFactoryBean;

public class MemberAttributeDefinitionFactoryBean extends BaseAttributeDefinitionFactoryBean {

  private List<AttributeIdentifier> attributes;

  public List<AttributeIdentifier> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<AttributeIdentifier> attributes) {
    this.attributes = attributes;
  }

  protected Object createInstance() throws Exception {
    MemberAttributeDefinition definition = new MemberAttributeDefinition();
    populateAttributeDefinition(definition);
    definition.setAttributes(attributes);
    return definition;
  }

  public Class getObjectType() {
    return MemberAttributeDefinition.class;
  }

}
