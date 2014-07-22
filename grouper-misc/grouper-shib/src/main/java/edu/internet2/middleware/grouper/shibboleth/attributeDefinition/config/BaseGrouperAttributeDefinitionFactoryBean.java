/**
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
 */
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

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.BaseGrouperAttributeDefinition;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition.BaseAttributeDefinitionFactoryBean;

/** Spring factory bean for {@link BaseGrouperAttributeDefinition}s. */
public abstract class BaseGrouperAttributeDefinitionFactoryBean extends BaseAttributeDefinitionFactoryBean {

  /** The attribute identifiers. */
  private List<AttributeIdentifier> attributeIdentifiers;

  /**
   * Return the attribute identifiers.
   * 
   * @return the attribute identifiers
   */
  public List<AttributeIdentifier> getAttributeIdentifiers() {
    return attributeIdentifiers;
  }

  /**
   * Set the attribute identifiers.
   * 
   * @param attributeIdentifiers the attribute identifiers
   */
  public void setAttributeIdentifiers(List<AttributeIdentifier> attributeIdentifiers) {
    this.attributeIdentifiers = attributeIdentifiers;
  }

  /**
   * {@inheritDoc}
   */
  protected void populateAttributeDefinition(BaseGrouperAttributeDefinition definition) {
    super.populateAttributeDefinition(definition);
    definition.setAttributeIdentifiers(attributeIdentifiers);
  }

}
