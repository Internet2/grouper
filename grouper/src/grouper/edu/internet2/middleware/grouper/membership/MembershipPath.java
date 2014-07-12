/*******************************************************************************
 * Copyright 2014 Internet2
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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * path of a membership for a subject in a group, group privilege, stem privilege, or attribute privilege.
 * these are distinct paths from start to finish, if there are multiple subpaths, each will expand
 * into a full path
 * 
 * @author mchyzer
 *
 */
public class MembershipPath implements Comparable<MembershipPath> {

  /**
   * field for this path, generally one for list, or multiple for privileges
   */
  private Set<Field> fields;
  
  /**
   * fields assigned, and the fields those fields imply
   */
  private Set<Field> fieldsIncludingImplied;
  
  
  /**
   * fields assigned, and the fields those fields imply
   * @return the fieldsIncludingImplied
   */
  public Set<Field> getFieldsIncludingImplied() {
    if (this.fieldsIncludingImplied == null) {
      this.fieldsIncludingImplied = new TreeSet<Field>();
    }
    return this.fieldsIncludingImplied;
  }

  
  /**
   * fields assigned, and the fields those fields imply
   * @param fieldsIncludingImplied the fieldsIncludingImplied to set
   */
  public void setFieldsIncludingImplied(Set<Field> fieldsIncludingImplied) {
    this.fieldsIncludingImplied = fieldsIncludingImplied;
  }

  /**
   * field for this path, generally one for list, or multiple for privileges
   * @return the fields
   */
  public Set<Field> getFields() {
    if (this.fields == null) {
      this.fields = new TreeSet<Field>();
    }
    return this.fields;
  }

  /**
   * field for this path, generally one for list, or multiple for privileges
   * @param fields
   */
  public void setFields(Set<Field> fields) {
    this.fields = fields;
  }

  /**
   * default constructor
   */
  public MembershipPath() {
  }

  /**
   * construct with fields
   * @param member1
   * @param membershipPathNodes1
   * @param theMembershipType
   */
  public MembershipPath(Member member1, List<MembershipPathNode> membershipPathNodes1, MembershipType theMembershipType) {
    super();
    this.member = member1;
    this.membershipPathNodes = membershipPathNodes1;
    this.membershipType = theMembershipType;
  }

  /**
   * member in the membership
   */
  private Member member;
  
  /**
   * member in the membership
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * member in the membership
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }

  /**
   * see if it is the same path except the field
   * @param that
   * @return if same path
   */
  public boolean equalsExceptFields(MembershipPath that) {
    boolean equals = new EqualsBuilder()
      .append(this.member, that.member)
      .append(this.membershipType, that.membershipType)
      .append(this.pathAllowed, that.pathAllowed)
      .isEquals();
    if (!equals) {
      return false;
    }
    
    //compare the list of nodes
    if (!GrouperUtil.equalsList(this.membershipPathNodes, that.membershipPathNodes)) {
      return false;
    }
    
    return true;
  }
  
  /**
   * ordered list of nodes for this membership path
   */
  private List<MembershipPathNode> membershipPathNodes;
  
  /**
   * IMMEDIATE or EFFECTIVE, etc
   */
  private MembershipType membershipType;

  /**
   * if path allowed to be seen by grouper session
   */
  private boolean pathAllowed;

  /**
   * ordered list of nodes for this membership path
   * @return list of membership path nodes
   */
  public List<MembershipPathNode> getMembershipPathNodes() {
    return this.membershipPathNodes;
  }

  /**
   * ordered list of nodes for this membership path
   * @param membershipPathNodes1
   */
  public void setMembershipPathNodes(List<MembershipPathNode> membershipPathNodes1) {
    this.membershipPathNodes = membershipPathNodes1;
  }
  
  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();

    if (!this.pathAllowed) {
      result.append("(not allowed): ");
    }

    result.append(this.member.getSubjectId());
    
    if (GrouperUtil.length(this.fields) != 0) {
      boolean first = true;
      result.append(" (fields: ");
      for (Field field : this.fields) {
        if (!first) {
          result.append(", ");
        }
        result.append(field.getName());
        first = false;
      }
      result.append("): ");
    }
    
    for (int i=0;i<GrouperUtil.length(this.membershipPathNodes); i++) {
      
      result.append(" -> ");
      result.append(this.membershipPathNodes.get(i));
      
    }
    return result.toString();
  }

  /**
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(MembershipPath membershipPath) {
    
    if (membershipPath == null) {
      return -1;
    }

    if (this.member == null) {
      return 1;
    }
    
    //not sure why members would be different, check anyways
    int compare = this.member.compareTo(membershipPath.member);
    if (compare != 0) {
      return compare;
    }
    
    //fewer paths wins
    compare = new Integer(GrouperUtil.length(this.membershipPathNodes))
      .compareTo(GrouperUtil.length(membershipPath.membershipPathNodes));
    if (compare != 0) {
      return compare;
    }

    //go through the groups and compare
    for (int i=0;i<GrouperUtil.length(this.membershipPathNodes); i++) {
      compare = this.membershipPathNodes.get(i).compareTo(membershipPath.membershipPathNodes.get(i));
      if (compare != 0) {
        return compare;
      }
    }
    
    //uh... must be the same?
    return 0;
  }

  /**
   * IMMEDIATE or EFFECTIVE, etc
   * @return membership type
   */
  public MembershipType getMembershipType() {
    return this.membershipType;
  }

  /**
   * IMMEDIATE or EFFECTIVE, etc
   * @param membershipType1
   */
  public void setMembershipType(MembershipType membershipType1) {
    this.membershipType = membershipType1;
  }

  /**
   * if path allowed to be seen by user
   * @return path allowed
   */
  public boolean isPathAllowed() {
    return this.pathAllowed;
  }

  /**
   * if path allowed to be seen by user
   * @param thePathAllowed1
   */
  public void setPathAllowed(boolean thePathAllowed1) {
    this.pathAllowed = thePathAllowed1;
  }  
}
