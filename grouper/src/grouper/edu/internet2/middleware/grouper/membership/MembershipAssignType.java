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
/**
 * @author mchyzer
 * $Id: PrivilegeAssignType.java 8245 2012-04-24 13:45:50Z mchyzer $
 */
package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.Membership;


/**
 * how this subject has this membership
 */
public enum MembershipAssignType {

  /**
   * immediately assigned
   */
  IMMEDIATE {

    @Override
    public boolean isImmediate() {
      return true;
    }
    @Override
    public boolean isNonImmediate() {
      return false;
    }
  },

  /**
   * effectively assigned
   */
  EFFECTIVE {

    @Override
    public boolean isImmediate() {
      return false;
    }
    @Override
    public boolean isNonImmediate() {
      return true;
    }
  },
  
  /**
   * has immediate and effective memberships
   */
  IMMEDIATE_AND_EFFECTIVE {

    @Override
    public boolean isImmediate() {
      return true;
    }
    @Override
    public boolean isNonImmediate() {
      return true;
    }
  };
  
  /**
   * if immediate
   * @return true/false
   */
  public abstract boolean isImmediate();
  
  /**
   * if non immediate
   * @return true/false
   */
  public abstract boolean isNonImmediate();
  
  /**
   * name as javabean property
   * @return name
   */
  public String getName() {
    return this.name();
  }
  
  /**
   * convert a privilege to a type
   * @param existingAssignType
   * @param membership
   * @return the type
   */
  public static MembershipAssignType convertMembership(MembershipAssignType existingAssignType, Membership membership) {

    MembershipAssignType membershipAssignType = convertMembership(membership);
    return convert(existingAssignType, membershipAssignType);
  }

  /**
   * convert a privilege to a type
   * @param membershipAssignType
   * @param anotherAssignType
   * @return the type
   */
  public static MembershipAssignType convert(MembershipAssignType membershipAssignType, MembershipAssignType anotherAssignType) {

    if (membershipAssignType == IMMEDIATE_AND_EFFECTIVE || anotherAssignType == IMMEDIATE_AND_EFFECTIVE) {
      return IMMEDIATE_AND_EFFECTIVE;
    }
    if (membershipAssignType == null) {
      return anotherAssignType;
    }
    if (membershipAssignType == IMMEDIATE && anotherAssignType == IMMEDIATE) {
      return IMMEDIATE;
    }
    
    if (membershipAssignType == EFFECTIVE && anotherAssignType == EFFECTIVE) {
      return EFFECTIVE;
    }
    return IMMEDIATE_AND_EFFECTIVE;
  }


  /**
   * convert a membership to a type
   * @param membership
   * @return the type
   */
  public static MembershipAssignType convertMembership(Membership membership) {
    if (membership.getTypeEnum() == MembershipType.IMMEDIATE) {
      return IMMEDIATE;
    }
    return EFFECTIVE;
  }
  
}
