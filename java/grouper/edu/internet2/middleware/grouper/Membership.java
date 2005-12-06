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
 * @version $Id: Membership.java,v 1.9 2005-12-06 17:40:21 blair Exp $
 */
public class Membership implements Serializable {

  // Hibernate Properties
  private int       depth;
  private Field     field;
  private String    id;
  private Member    member_id;
  private String    owner_id;
  private String    via_id;

  
  // Private Transient Instance Variables
  private transient GrouperSession s;


  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Membership() {
    // Nothing
  }

  // Create new membership
  protected Membership(
    GrouperSession s, String oid, Member m, Field f
  ) 
  {
    // Attach session
    this.s = s;
    // Set owner
    this.setOwner_id(oid);
    // Set member
    this.setMember_id(m);
    // Set field  
    this.setField(f);
  } // protected Membership(s, oid, m, f)

  // Creating a new (effective) membership
  protected Membership(
    GrouperSession s, String gid, Member m,
    Field f         , String vid, int depth
  )
  {
    this(s, gid, m, f);
    this.setVia_id(vid);
    this.setDepth(depth); 
  } // protected Membership(s, gid, m, f, vid, depth)


  // Public Instance Methods

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Membership)) {
      return false;
    }
    Membership otherMembership = (Membership) other;
    return new EqualsBuilder()
           .append(this.getDepth()    , otherMembership.getDepth()    )
           .append(this.getOwner_id() , otherMembership.getOwner_id() )
           .append(this.getMember_id(), otherMembership.getMember_id())
           .append(this.getField()    , otherMembership.getField()    )
           .append(this.getVia_id()   , otherMembership.getVia_id()   )
           .isEquals();
  } // public boolean equals(other)

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
  public Group getGroup() 
    throws GroupNotFoundException
  {
    // TODO Cache group?
    // TODO Check field
    return GroupFinder.findByUuid(this.s, this.getOwner_id());
  } // public Group getGroup()

  /**
   * Get this membership's list.
   * <pre class="eg">
   * String list = g.getList();
   * </pre>
   * @return  The {@link Field} type of this membership.
   */
  public Field getList() {
    return this.getField();
  } // public Field getList()

  /**
   * Get this membership's member.
   * <pre class="eg">
   * Member m = ms.getMember();
   * </pre>
   * @return  A {@link Member}
   * @throws  MemberNotFoundException
   */
  public Member getMember() 
    throws MemberNotFoundException
  {
    return this.getMember_id();
    //return MemberFinder.findByUuid(this.s, this.getMember_id());
  } // public Member getMember()

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
    if (this.getVia_id() == null) {
      throw new GroupNotFoundException(
        "no via group for immediate memberships"
      );
    }
    return GroupFinder.findByUuid(this.s, this.getVia_id());
  } // public Group getViaGroup()

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getDepth()     )
           .append(getOwner_id()  )
           .append(getMember_id() )
           .append(getField()     )
           .append(getVia_id()    )
           .toHashCode();
  } // public int hashCode()

  public String toString() {
    Object  owner = this.getOwner_id();
    Object  via   = this.getVia_id();
    try {
      Group g = this.getGroup();
      owner   = g.getName() + "/group"; 
    }
    catch (GroupNotFoundException eNGF) {
      try {
        Stem ns = this.getStem();
        owner   = ns.getName() + "/stem";
      }
      catch (StemNotFoundException eSNF) {
        // ignore
      }
    }
    try {
      Group g = this.getViaGroup();
      via     = g.getName();
    }
    catch (GroupNotFoundException eGNF) {
      /// ignore
    }
    return new ToStringBuilder(this) 
      .append("owner"   , owner           )
      .append("member"  , getMember_id()  )
      .append("list"    , getField()      )
      .append("via"     , via             )
      .append("depth"   , getDepth()      )
      .toString();
  } // public String toString()


  // Protected Class Methods

  private static String _getOid(Object o) {
    if      (o.getClass().equals(Group.class)) {
      return ( (Group) o ).getUuid();
    }
    else if (o.getClass().equals(Stem.class)) {
      return ( (Stem) o ).getUuid();
    }
    throw new RuntimeException(
      "class cannot contain membership: " + o.getClass()
    );
  } // private static String _getOid(o)

  protected static Membership addMembership(
    GrouperSession s, Object o, Member m, Field f
  )
    throws MemberAddException
  {
    String oid = _getOid(o);

    Membership ms = null;
    try {
      // Does the membership already exist?
      ms = MembershipFinder.findImmediateMembership(s, oid, m, f);
      throw new MemberAddException(
        "membership already exists"
      );
    }
    catch (MembershipNotFoundException eMNF) {
      // Membership doesn't exist.  Create it.
      ms = new Membership(s, oid, m, f);
    }
    if (ms == null) {
      throw new MemberAddException("unable to add member");
    }
    return ms;
  } // protected static Membership addMembership(s, o, m, f)
    
  protected static List setSession(GrouperSession s, List l) {
    List      mships  = new ArrayList();
    Iterator  iter    = l.iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static List setSession(s, l)


  // Protected Instance Methods

  protected Stem getStem() 
    throws StemNotFoundException
  {
    // TODO Cache stem?
    // TODO Check field
    return StemFinder.findByUuid(this.s, this.getOwner_id());
  } // public Stem getStem()

  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)


  // Hibernate Accessors

  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  // TODO private int getDepth() {
  public int getDepth() {
    return this.depth;
  }

  private void setDepth(int depth) {
    this.depth = depth;
  }

  // TODO private String getOwner_id() {
  protected String getOwner_id() {
    return this.owner_id;
  }

  private void setOwner_id(String owner_id) {
    this.owner_id = owner_id;
  }

  // TODO private String getMember_id() {
  //protected String getMember_id() {
  private Member getMember_id() {
    return this.member_id;
  }

  //private void setMember_id(String member_id) {
  private void setMember_id(Member member_id) {
    this.member_id = member_id;
  }

  private Field getField() {
    return this.field;
  }

  private void setField(Field f) {
    this.field = f;
  }

  // TODO private String getVia_id() {
  // TODO Map to _Group_?
  protected String getVia_id() {
    return this.via_id;
  }

  private void setVia_id(String via_id) {
    this.via_id = via_id;
  }

}
