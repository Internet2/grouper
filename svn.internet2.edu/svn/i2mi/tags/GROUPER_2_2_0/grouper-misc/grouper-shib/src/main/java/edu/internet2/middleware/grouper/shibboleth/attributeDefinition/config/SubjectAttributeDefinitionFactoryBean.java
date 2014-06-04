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

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config;

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.SubjectAttributeDefinition;

/** Spring factory bean for {@link SubjectAttributeDefinition}s. */
public class SubjectAttributeDefinitionFactoryBean extends BaseGrouperAttributeDefinitionFactoryBean {

  /** {@inheritDoc} */
  protected Object createInstance() throws Exception {
    SubjectAttributeDefinition definition = new SubjectAttributeDefinition();
    populateAttributeDefinition(definition);
    return definition;
  }

  /** {@inheritDoc} */
  public Class getObjectType() {
    return SubjectAttributeDefinition.class;
  }
}
