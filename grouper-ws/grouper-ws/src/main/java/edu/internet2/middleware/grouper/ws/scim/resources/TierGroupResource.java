/****
 * Copyright 2022 Internet2
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
 ***/

package edu.internet2.middleware.grouper.ws.scim.resources;

import com.unboundid.scim2.common.BaseScimResource;
import com.unboundid.scim2.common.annotations.Attribute;
import com.unboundid.scim2.common.annotations.Schema;
import com.unboundid.scim2.common.types.AttributeDefinition;


@Schema(id="urn:grouper:params:scim:schemas:extension:TierGroupExtension",
        name="TierGroupExtension", description = "Tier extension for group")
public class TierGroupResource extends BaseScimResource {

  @Attribute(description = "Grouper id path",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private String systemName;

  @Attribute(description = "The group description",
          isRequired = false,
          mutability = AttributeDefinition.Mutability.READ_WRITE,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.NONE)
  private String description;

  @Attribute(description = "The group id index",
          isRequired = false,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.NONE)
  private Long idIndex;

  public TierGroupResource() {
  }

  public String getSystemName() {
    return systemName;
  }

  public TierGroupResource setSystemName(String systemName) {
    this.systemName = systemName;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public TierGroupResource setDescription(String description) {
    this.description = description;
    return this;
  }

  public Long getIdIndex() {
    return idIndex;
  }

  public TierGroupResource setIdIndex(Long idIndex) {
    this.idIndex = idIndex;
    return this;
  }
}
