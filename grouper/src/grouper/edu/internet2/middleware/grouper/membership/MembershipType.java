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
 * $Id: MembershipType.java,v 1.1 2009-12-07 07:31:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of membership
 */
public enum MembershipType {

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   */
  IMMEDIATE("immediate") {

    /**
     * query clause for this membership type, e.g. = 'immediate'
     */
    public String queryClause() {
      return " = 'immediate' ";
    }
  },
  
  /**
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   */
  EFFECTIVE("effective") {

    /**
     * query clause for this membership type, e.g. = 'immediate'
     */
    public String queryClause() {
      return " = 'effective' ";
    }
  },
  
  /**
   * composite memberships are due to union, intersection, minus
   * A member of a group (aka composite member) has either or both of
   * an immediate (direct) membership, or an effective (indirect) membership 
   */
  COMPOSITE("composite") {

    /**
     * query clause for this membership type, e.g. = 'immediate'
     */
    public String queryClause() {
      return " = 'composite' ";
    }
  },
  
  /**
   * everything except immediate
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   */
  NONIMMEDIATE("nonImmediate") {

    /**
     * query clause for this membership type, e.g. = 'immediate'
     */
    public String queryClause() {
      return " != 'immediate' ";
    }
  };
  
  /**
   * query clause for this membership type, e.g. = 'immediate'
   * @return the query clause
   */
  public abstract String queryClause();
  
  /**
   * construct with type
   * @param theType
   */
  private MembershipType(String theType) {
    this.typeString = theType; 
  }
  
  /** type */
  private String typeString;

  /**
   * return the type string
   * @return the type
   */
  public String getTypeString() {
    return this.typeString;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static MembershipType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    return GrouperUtil.enumValueOfIgnoreCase(MembershipType.class, 
        string, exceptionOnNull);
  }

  
}
