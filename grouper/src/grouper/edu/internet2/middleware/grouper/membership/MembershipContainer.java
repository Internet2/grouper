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
package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.Membership;



/**
 * @author mchyzer
 * $Id: membershipContainerImpl.java 8245 2012-04-24 13:45:50Z mchyzer $
 */

/**
 * encapsulates a member in a field of an owner
 */
public class MembershipContainer {

  /**
   * immediate membership or null if none
   */
  private Membership immediateMembership;
    
  /**
   * immediate membership or null if none
   * @return immediate membership or null if none
   */
  public Membership getImmediateMembership() {
    return this.immediateMembership;
  }

  /**
   * immediate membership or null if none
   * @param immediateMembership1
   */
  public void setImmediateMembership(Membership immediateMembership1) {
    this.immediateMembership = immediateMembership1;
  }

  /**
   * 
   * @param themembershipName
   * @param membershipAssignType
   */
  public MembershipContainer(String themembershipName,
      MembershipAssignType membershipAssignType) {
    super();
    this.fieldName = themembershipName;
    this.membershipAssignType = membershipAssignType;
  }

  /**
   * 
   */
  public MembershipContainer() {
  }

  /** membership name */
  private String fieldName;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.membershipContainer#getmembershipName()
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /** membership assign type */
  private MembershipAssignType membershipAssignType;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.membershipContainer#getmembershipAssignType()
   */
  public MembershipAssignType getMembershipAssignType() {
    return this.membershipAssignType;
  }

  
  /**
   * @param fieldName1 the membership to set
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }

  
  /**
   * @param membershipAssignType1 the membershipAssignType to set
   */
  public void setMembershipAssignType(MembershipAssignType membershipAssignType1) {
    this.membershipAssignType = membershipAssignType1;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("membership: ");
    if (this.fieldName == null) {
      result.append("null");
    } else {
      result.append(this.fieldName);
    }
    result.append(", type: ");
    if (this.membershipAssignType == null) {
      result.append("null");
    } else {
      result.append(this.membershipAssignType.name());
    }
    return result.toString();
  }

}
