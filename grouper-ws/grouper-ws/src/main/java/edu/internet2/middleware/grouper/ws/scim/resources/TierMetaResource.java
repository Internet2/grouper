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

@Schema(id="urn:tier:params:scim:schemas:extension:TierMetaExtension",
        name="TierMetaExtension", description = "Tier metadata extension for result status")
public class TierMetaResource extends BaseScimResource {

  @Attribute(description = "Grouper result code",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private String resultCode;

  @Attribute(description = "Grouper time to respond in milliseconds",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private Long responseDurationMillis;

  public TierMetaResource() {
  }

  public String getResultCode() {
    return resultCode;
  }

  public TierMetaResource setResultCode(String resultCode) {
    this.resultCode = resultCode;
    return this;
  }

  public Long getResponseDurationMillis() {
    return responseDurationMillis;
  }

  public TierMetaResource setResponseDurationMillis(Long responseDurationMillis) {
    this.responseDurationMillis = responseDurationMillis;
    return this;
  }
}
