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
 * @version $Id: Membership.java,v 1.1.2.5 2005-10-20 19:29:12 blair Exp $
 *     
*/
public class Membership implements Serializable {

  // Hibernate Properties
  private String  id;
  private int     count;
  private Group   group_id;
  private Member  member_id;
  private Field   list_id;
  private Integer version;
  private Group   via_id;

  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Membership() {
    // Nothing
  }

  // Public Instance Methods

  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof Membership) ) return false;
    Membership castOther = (Membership) other;
    return new EqualsBuilder()
           .append(this.getCount(), castOther.getCount())
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
   * Membership parent = ms.getParentMembership();
   * </pre>
   * @return  A {@link Membership}
   */
  public Membership getParentMembership() {
    throw new RuntimeException("Not implemented");
  }
 
  /**
   * Get this membership's via group.
   * <pre class="eg">
   * Group via = ms.getViaGroup();
   * </pre>
   * @return  A {@link Group}
   */
  public Group getViaGroup() {
    throw new RuntimeException("Not implemented");
  }

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getCount())
           .append(getGroup_id())
           .append(getMember_id())
           .append(getList_id())
           .append(getVia_id())
           .toHashCode();
  }

  public String toString() {
    return new ToStringBuilder(this)
           .append("count", getCount())
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

  private int getCount() {
    return this.count;
  }

  private void setCount(int count) {
    this.count = count;
  }

  private Integer getVersion() {
    return this.version;
  }

  private void setVersion(Integer version) {
    this.version = version;
  }

  private Group getGroup_id() {
    return this.group_id;
  }

  private void setGroup_id(Group group_id) {
    this.group_id = group_id;
  }

  private Member getMember_id() {
    return this.member_id;
  }

  private void setMember_id(Member member_id) {
        this.member_id = member_id;
    }

  private Field getList_id() {
    return this.list_id;
  }

  private void setList_id(Field list_id) {
    this.list_id = list_id;
  }

  private Group getVia_id() {
    return this.via_id;
  }

  private void setVia_id(Group via_id) {
    this.via_id = via_id;
  }

}
