/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;

import  java.io.Serializable;
import  java.util.*;
import  org.apache.commons.lang.builder.*;


/** 
 * A list membership in the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Membership.java,v 1.1.2.10 2005-11-07 00:31:15 blair Exp $
 */
public class Membership implements Serializable {

  // Hibernate Properties
  private int     depth;
  // TODO private Group   group_id;
  private String  group_id;
  private String  id;
  // TODO private Member  member_id;
  private String  member_id;
  // TODO private Field   list_id;
  private String  list_id;
  // TODO private Group   via_id;
  private String  via_id;

  
  // Transient Private Instance Variables
  private transient GrouperSession s;


  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Membership() {
    // Nothing
  }

  // Creating a new membership
  protected Membership(GrouperSession s, Group g, Member m, String field) {
    // Attach session
    this.s = s;
    // Set group
    // TODO this.setGroup_id(g);
    this.setGroup_id(g.getUuid());
    // Set member
    // TODO this.setMember_id(m);
    this.setMember_id(m.getUuid());
    // Set field  
    // TOOD this.setList_id( FieldFinder.getField(field) );
    this.setList_id(field);
  } // protected Membership(s, g, m, field)


  // Public Instance Methods

  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof Membership) ) return false;
    Membership castOther = (Membership) other;
    return new EqualsBuilder()
           .append(this.getDepth(), castOther.getDepth())
           .append(this.getGroup_id(), castOther.getGroup_id())
           .append(this.getMember_id(), castOther.getMember_id())
           .append(this.getList_id(), castOther.getList_id())
           .append(this.getVia_id(), castOther.getVia_id())
           .isEquals();
  }

  /**
   * Get child memberships of this membership.
   * <pre class="eg">
   * Set children = ms.getChildMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   */
  public Set getChildMemberships() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get this membership's group.
   * <pre class="eg">
   * Group g = ms.getGroup();
   * </pre>
   * @return  A {@link Group}
   */
  public Group getGroup() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get this membership's list.
   * <pre class="eg">
   * String list = g.getList();
   * </pre>
   * @return  List of this membership.
   */
  public String getList() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get this membership's member.
   * <pre class="eg">
   * Member m = ms.getMember();
   * </pre>
   * @return  A {@link Member}
   */
  public Member getMember() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get parent membership of this membership.
   * <pre class="eg">
   * try {
   *   Membership parent = ms.getParentMembership();
   * }
   * catch (MembershipNotFoundException e) {
   *   // Unable to retrieve parent membership
   * }
   * </pre>
   * @return  A {@link Membership}
   * @throws  MembershipNotFoundException
   */
  public Membership getParentMembership() 
    throws MembershipNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }
 
  /**
   * Get this membership's via group.
   * <pre class="eg">
   * try {
   *   Group via = ms.getViaGroup();
   * }
   * catch (GroupNotFoundException e) {
   *   // Unable to retrieve via group
   * }
   * </pre>
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public Group getViaGroup() 
    throws GroupNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getDepth())
           .append(getGroup_id())
           .append(getMember_id())
           .append(getList_id())
           .append(getVia_id())
           .toHashCode();
  }

  public String toString() {
    return new ToStringBuilder(this)
           .append("depth", getDepth())
           .append("group_id", getGroup_id())
           .append("member_id", getMember_id())
           .append("list_id", getList_id())
           .append("via_id", getVia_id())
           .toString();
  }

  // Hibernate Accessors

  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private int getDepth() {
    return this.depth;
  }

  private void setDepth(int depth) {
    this.depth = depth;
  }

  // TODO private Group getGroup_id() {
  private String getGroup_id() {
    return this.group_id;
  }

  // TODO private void setGroup_id(Group group_id) {
  private void setGroup_id(String group_id) {
    this.group_id = group_id;
  }

  // TODO private Member getMember_id() {
  private String getMember_id() {
    return this.member_id;
  }

  // TODO private void setMember_id(Member member_id) {
  private void setMember_id(String member_id) {
    this.member_id = member_id;
  }

  // TODO private Field getList_id() {
  private String getList_id() {
    return this.list_id;
  }

  // TODO private void setList_id(Field list_id) {
  private void setList_id(String list_id) {
    this.list_id = list_id;
  }

  // TODO private Group getVia_id() {
  private String getVia_id() {
    return this.via_id;
  }

  // TODO private void setVia_id(Group via_id) {
  private void setVia_id(String via_id) {
    this.via_id = via_id;
  }

}
