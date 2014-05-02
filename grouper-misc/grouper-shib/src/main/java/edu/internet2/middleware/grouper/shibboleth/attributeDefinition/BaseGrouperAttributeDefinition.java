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
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition;

import java.util.List;

import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.BaseAttributeDefinition;

/** An {@link AttributeDefinition} which returns attributes from Grouper objects. */
public abstract class BaseGrouperAttributeDefinition extends BaseAttributeDefinition {

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

  /** {@inheritDoc} */
  public void validate() throws AttributeResolutionException {

  }
}
