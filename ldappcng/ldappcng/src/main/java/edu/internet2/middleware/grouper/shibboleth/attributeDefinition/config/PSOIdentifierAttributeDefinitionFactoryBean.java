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

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.PSOIdentifierAttributeDefinition;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition.BaseAttributeDefinitionFactoryBean;

/** Spring bean factory that produces {@link PSOIdentifierAttributeDefinition}s. */
public class PSOIdentifierAttributeDefinitionFactoryBean extends BaseAttributeDefinitionFactoryBean {

  /** {@inheritDoc} */
  protected Object createInstance() throws Exception {
    PSOIdentifierAttributeDefinition definition = new PSOIdentifierAttributeDefinition();
    populateAttributeDefinition(definition);
    return definition;
  }

  /** {@inheritDoc} */
  public Class getObjectType() {
    return PSOIdentifierAttributeDefinition.class;
  }

}
