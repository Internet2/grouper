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
import com.unboundid.scim2.common.types.Group;
import com.unboundid.scim2.common.types.Member;

import java.time.LocalDateTime;


@Schema(id="urn:tier:params:scim:schemas:Membership",
        name="TierMembershipExtension", description = "Resource for representing Membership schema data")
public class TierMembershipResource extends BaseScimResource {

  @Attribute(description = "Membership Type",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER,
          canonicalValues = {"composite", "immediate", "effective"})
  private String membershipType;

  @Attribute(description = "membership enabled?",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private Boolean enabled;

  @Attribute(description = "membership id",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.ALWAYS,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private String id;

  @Attribute(description = "Owner group of this membership",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private Group owner;

  @Attribute(description = "Member (Group or User) of this membership",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private Member member;


  @Attribute(description = "membership enabled time",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private LocalDateTime enabledTime;

  @Attribute(description = "membership disabled time",
          isRequired = false,
          isCaseExact = true,
          mutability = AttributeDefinition.Mutability.READ_ONLY,
          returned = AttributeDefinition.Returned.DEFAULT,
          uniqueness = AttributeDefinition.Uniqueness.SERVER)
  private LocalDateTime disabledTime;

  public TierMembershipResource() {
  }

  public String getMembershipType() {
    return membershipType;
  }

  public TierMembershipResource setMembershipType(String membershipType) {
    this.membershipType = membershipType;
    return this;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public TierMembershipResource setEnabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public Group getOwner() {
    return owner;
  }

  public TierMembershipResource setOwner(Group owner) {
    this.owner = owner;
    return this;
  }

  public Member getMember() {
    return member;
  }

  public TierMembershipResource setMember(Member member) {
    this.member = member;
    return this;
  }

  public LocalDateTime getEnabledTime() {
    return enabledTime;
  }

  public TierMembershipResource setEnabledTime(LocalDateTime enabledTime) {
    this.enabledTime = enabledTime;
    return this;
  }

  public LocalDateTime getDisabledTime() {
    return disabledTime;
  }

  public TierMembershipResource setDisabledTime(LocalDateTime disabledTime) {
    this.disabledTime = disabledTime;
    return this;
  }
}
