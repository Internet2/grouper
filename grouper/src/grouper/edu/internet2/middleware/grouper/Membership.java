/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * A list membership in the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Membership.java,v 1.25 2006-02-15 23:06:49 blair Exp $
 */
public class Membership implements Serializable {

  // Private Class Constants
  private static final String   ERR_IO  = "class cannot contain membership: ";
  private static final String   ERR_NO  = "membership has no owner: ";
  private static final Log      LOG     = LogFactory.getLog(Membership.class);
  private static final EventLog EL      = new EventLog();


  // Hibernate Properties
  private Member      creator_id;
  private long        create_time;
  private int         depth;
  private Field       field;
  private String      id;
  private Member      member_id;
  private String      owner_id;
  private Membership  parent_membership;
  private Status      status;
  private String      uuid;
  private String      via_id;

  
  // Private Transient Instance Variables
  private transient Group           group;
  private transient GrouperSession  s;
  private transient Stem            stem;


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
    // Set UUID
    this.setUuid( GrouperUuid.getUuid() );
    // Set depth
    this.setDepth(0);
    // Set via
    this.setVia_id(null);
    // Set parent membership
    this.setParent_membership(null);
  } // protected Membership(s, oid, m, f)


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
      .append(this.getUuid()      , otherMembership.getUuid()     )
      .append(this.getOwner_id()  , otherMembership.getOwner_id() )
      .append(this.getMember_id() , otherMembership.getMember_id())
      .append(this.getField()     , otherMembership.getField()    )
      .append(this.getVia_id()    , otherMembership.getVia_id()   )
      .append(this.getDepth()     , otherMembership.getDepth()    )
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
    // TODO Ideally I would use a Hibernate mapping for this, but...
    //      * It wasn't working and I didn't have time to debug at the
    //        moment
    //      * I still need to attach sessions
    //      * I still need to filter
    GrouperSession.validate(this.s);
    return MembershipFinder.findChildMemberships(this.s, this);
  } // public Set getChildMemberships()


  /**
   * Get this membership's group.
   * <pre class="eg">
   * Group g = ms.getGroup();
   * </pre>
   * @return  A {@link Group}
   */
  public Group getGroup() 
    throws  GroupNotFoundException
  {
    if (this.group == null) {
      GrouperSession.validate(this.s);
      this.group = GroupFinder.findByUuid(this.s, this.getOwner_id());
    }
    return this.group;
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
    GrouperSession.validate(this.s);
    Member m = this.getMember_id();
    if (m == null) {
      String msg = "unable to get member";
      GrouperLog.debug(LOG, this.s, msg);
      throw new MemberNotFoundException(msg);
    }
    m.setSession(this.s);
    return m;
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
    Membership parent = this.getParent_membership();
    if (parent == null) {
      throw new MembershipNotFoundException("no parent");
    }
    parent.setSession(this.s);
    return parent;
  } // public Membership getParentMembership()
 
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
      .append(this.getOwner_id()  )
      .append(this.getMember_id() )
      .append(this.getField()     )
      .append(this.getVia_id()    )
      .append(this.getDepth()     )
      .toHashCode();
  } // public int hashCode()

  public String toString() {
    GrouperSession.validate(this.s);
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


  protected static void addImmediateMembership(
    GrouperSession s, Object o, Subject subj, Field f
  )
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    GrouperSession.validate(s);
    String msg = "addImmediateMembership '" + f + "'";
    GrouperLog.debug(LOG, s, msg);
    try {
      // The objects that will need saving
      Set     objects = new LinkedHashSet();
      // Who we're adding
      Member  m       = PrivilegeResolver.getInstance().canViewSubject(
        s, subj, msg
      );
      objects.add(m);

      String oid = new String();
      if (o.getClass().equals(Group.class)) {
        ( (Group) o).setModified();
        oid = ( (Group) o).getUuid();
      }
      else {
        ( (Stem) o).setModified();
        oid = ( (Stem) o).getUuid();
      }

      // Create the immediate membership
      Membership  imm = _addMembership(s, o, m, f);
      TxQueue     tx  = new TxMemberAdd(s, oid, m, f);

      // Find effective memberships
      Set effs = _findEffectiveMemberships(s, imm);
      objects.addAll(effs);


      // And then save owner and memberships
      objects.add(imm);
      objects.add(o);
      objects.add(tx);
      HibernateHelper.save(objects);

      EL.addEffMembers(s, o, subj, f, effs);

    }
    catch (MemberAddException eMA) {
      throw eMA;
    }
    catch (Exception e) {
      throw new MemberAddException(e.getMessage());
    }
  } // protected static void addImmediateMembership(s, g, subj, f)


  // TODO REFACTOR/DRY
  protected static void delImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws  InsufficientPrivilegeException,
            MemberDeleteException,
            MembershipNotFoundException,
            SchemaException
  {
    GrouperSession.validate(s);
    String msg = "delImmediateMembership '" + f + "'";
    GrouperLog.debug(LOG, s, msg);
    try {
      // The objects that will need deleting and saving
      Set     deletes = new LinkedHashSet();
      Set     saves   = new LinkedHashSet();
      // Who we're deleting
      Member  m       = PrivilegeResolver.getInstance().canViewSubject(
        s, subj, msg
      );

      g.setModified();
      saves.add(g);

      // Find memberships to delete
      Membership imm = MembershipFinder.findImmediateMembership(s, g, subj, f);
      TxQueue     tx  = new TxMemberDel(s, g.getUuid(), m, f);
      saves.add(tx);

      Set effs = _membershipsToDelete(s, imm);
      deletes.addAll(effs);

      deletes.add(imm);

      // And then commit changes to registry
      HibernateHelper.saveAndDelete(saves, deletes);

      EL.delEffMembers(s, g, subj, f, effs);

    }
    catch (HibernateException eH) {
      msg += ": " + eH.getMessage();
      GrouperLog.debug(LOG, s, msg);
      throw new MemberDeleteException(msg);
    }
    catch (MemberNotFoundException eMNF) {
      msg += ": " + eMNF.getMessage();
      GrouperLog.debug(LOG, s, msg);
      throw new MemberDeleteException(msg);
    }
  } // protected static void delImmediateMembership(s, g, subj, f)

  // TODO REFACTOR/DRY
  protected static void delImmediateMembership(
    GrouperSession s, Stem ns, Subject subj, Field f
  )
    throws  InsufficientPrivilegeException,
            MemberDeleteException,
            MembershipNotFoundException,
            SchemaException
  {
    GrouperSession.validate(s);
    String msg = "delImmediateMembership '" + f + "'";
    GrouperLog.debug(LOG, s, msg);
    try {
      // The objects that will need deleting and saving
      Set     deletes = new LinkedHashSet();
      Set     saves   = new LinkedHashSet();
      // Who we're deleting
      Member  m       = PrivilegeResolver.getInstance().canViewSubject(
        s, subj, msg
      );

      ns.setModified();
      saves.add(ns);

      // Find memberships to delete
      Membership imm = MembershipFinder.findImmediateMembership(ns.getUuid(), m, f);
      TxQueue     tx  = new TxMemberDel(s, ns.getUuid(), m, f);
      saves.add(tx);

      imm.setSession(s);
      deletes.add(imm);
      Set effs = _membershipsToDelete(s, imm);
      deletes.addAll(effs);


      // And then commit changes to registry
      HibernateHelper.saveAndDelete(saves, deletes);

      EL.delEffMembers(s, ns, subj, f, effs);

    }
    catch (HibernateException eH) {
      msg += ": " + eH.getMessage();
      GrouperLog.debug(LOG, s, msg);
      throw new MemberDeleteException(msg);
    }
    catch (MemberNotFoundException eMNF) {
      msg += ": " + eMNF.getMessage();
      GrouperLog.debug(LOG, s, msg);
      throw new MemberDeleteException(msg);
    }
  } // protected static void delImmediateMembership(s, ns, subj, f

  protected static Set deleteAllField(GrouperSession s, Group g, Field f) 
    throws  GroupNotFoundException,
            MemberDeleteException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    GrouperSession.validate(s);
    String msg = "deleteAllField '" + f + "'";
    GrouperLog.debug(LOG, s, msg);

    GrouperSession orig = s;
    GrouperSession root = GrouperSessionFinder.getRootSession();
    g.setSession(root);

    Set deletes = new LinkedHashSet();

    // Remove where this group is an immediate member
    GrouperLog.debug(LOG, s, msg + " finding isMember");
    Iterator iterIS = g.toMember().getImmediateMemberships(f).iterator();
    while (iterIS.hasNext()) {
      Membership  msIS  = (Membership) iterIS.next();
      Set         effs  = _membershipsToDelete(s, msIS);
      GrouperLog.debug(
        LOG, orig, msg + " found isMember: " + effs.size()
      );
      deletes.addAll(effs);
      deletes.add(msIS);
    }
    // ... and then deal with this group's immediate members
    GrouperLog.debug(LOG, s, msg + " finding hasMembers");
    Iterator iterHAS = g.getImmediateMemberships(f).iterator();
    while (iterHAS.hasNext()) {
      Membership  msHAS = (Membership) iterHAS.next();
      Set         effs  = _membershipsToDelete(s, msHAS);
      GrouperLog.debug(
        LOG, orig, msg + " found hasMembers: " + effs.size()
      );
      deletes.addAll(effs);
      deletes.add(msHAS);
    }

    g.setSession(orig);
    GrouperLog.debug(LOG, orig, msg + " total: " + deletes.size());
    return deletes;
  } // protected static Set deleteAllField(s, g, f)

  // TODO REFACTOR/DRY
  protected static Set deleteAllField(GrouperSession s, Stem ns, Field f) 
    throws  GroupNotFoundException,
            MemberDeleteException,
            MemberNotFoundException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    GrouperSession.validate(s);
    String msg = "deleteAllField '" + f + "'";
    GrouperLog.debug(LOG, s, msg);

    GrouperSession orig = s;
    GrouperSession root = GrouperSessionFinder.getRootSession();
    ns.setSession(root);

    Set deletes = new LinkedHashSet();

    // Remove this stem's immediate members
    GrouperLog.debug(LOG, s, msg + " finding hasMembers");
    Iterator iterHAS = MembershipFinder.findImmediateMemberships(
      root, ns.getUuid(), f
    ).iterator();
    while (iterHAS.hasNext()) {
      Membership  msHAS = (Membership) iterHAS.next();
      Set         effs  = _membershipsToDelete(s, msHAS);
      GrouperLog.debug(
        LOG, orig, msg + " found hasMembers: " + effs.size()
      );
      deletes.addAll(effs);
      deletes.add(msHAS);
    }

    ns.setSession(orig);
    GrouperLog.debug(LOG, orig, msg + " total: " + deletes.size());
    return deletes;
  } // protected static Set deleteAllField(s, ns, f)

  protected static Set deleteAllFieldType(
    GrouperSession s, Group g, FieldType type
  ) 
    throws  GroupNotFoundException,
            MemberDeleteException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    GrouperSession.validate(s);
    String msg = "deleteAllFieldType '" + type + "'";
    GrouperLog.debug(LOG, s, msg);

    GrouperSession orig = s;
    GrouperSession root = GrouperSessionFinder.getRootSession();
    g.setSession(root);

    Set deletes = new LinkedHashSet();

    // Find all fields of type list
    Iterator iter = FieldFinder.findAllByType(type).iterator();
    while (iter.hasNext()) {
      Field f     = (Field) iter.next();
      GrouperLog.debug(LOG, orig, msg + " finding '" + f + "'");
      Set   found = deleteAllField(s, g, f);
      GrouperLog.debug(LOG, s, msg + " found: " + found.size());
      deletes.addAll(found);
    }

    g.setSession(orig);
    GrouperLog.debug(LOG, orig, msg + " total: " + deletes.size());
    return deletes;
  } // protected static Set deleteAllFieldType(s, g, type)


  // Protected Instance Methods

  protected Stem getStem() 
    throws StemNotFoundException
  {
    if (this.stem == null) {
      GrouperSession.validate(this.s);
      this.stem = StemFinder.findByUuid(this.s, this.getOwner_id());
    }
    return this.stem;
  } // public Stem getStem()

  protected static Membership newEffectiveMembership(
    GrouperSession s, Membership ms, Membership hasMS, int offset
  )
    throws  GroupNotFoundException,
            MemberNotFoundException
  { 
    Membership eff = new Membership();
    eff.s = s;
    // Set UUID
    eff.setUuid( GrouperUuid.getUuid() );
    try {
      eff.setOwner_id(  ms.getGroup().getUuid()       );  // original g
    }
    catch (GroupNotFoundException eGNF) {
      try {
        eff.setOwner_id( ms.getStem().getUuid()       );
      }
      catch (StemNotFoundException eNSNF) {
        String err = ERR_NO + ms;
        LOG.fatal(err);
        throw new RuntimeException(err);
      }
    }
    eff.setMember_id( hasMS.getMember()             );  // hasMember m
    eff.setField(     ms.getList()                  );  // original f
    eff.setDepth(                                       // increment the depth
      ms.getDepth() + hasMS.getDepth() + offset         
    );
    if (hasMS.getDepth() == 0) {
      eff.setVia_id(  hasMS.getGroup().getUuid()    );  // hasMember m was immediate
    }
    else {
      eff.setVia_id(  hasMS.getViaGroup().getUuid() );  // hasMember m was effective
    } 
    eff.setParent_membership(ms);                       // ms is parent membership
    GrouperLog.debug(LOG, s, "newEffectiveMembership: " + eff);
    return eff;       
  } // protected static Membership newEffectiveMembership(s, ms, hasMS)


  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
    if (this.group != null) {
      this.group.setSession(s);
    }
    if (this.stem != null) {
      this.stem.setSession(s);
    }
  } // protected void setSession(s)


  // Private Class Methods

  private static Membership _addMembership(
    GrouperSession s, Object o, Member m, Field f
  )
    throws MemberAddException
  {
    String oid = _getOid(o);

    Membership ms = null;
    try {
      // Does the membership already exist?
      ms = MembershipFinder.findImmediateMembership(oid, m, f);
      throw new MemberAddException("membership already exists");
    }
    catch (MembershipNotFoundException eMNF) {
      // Membership doesn't exist.  Create it.
      ms = new Membership(s, oid, m, f);
    }
    if (ms == null) {
      throw new MemberAddException("unable to add member");
    }
    return ms;
  } // private static Membership _addMembership(s, o, m, f)
    
  // Find effective memberships
  private static Set _findEffectiveMemberships(
    GrouperSession s, Membership imm
  )
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    GrouperSession.validate(s);
    return MemberOf.doMemberOf(s, imm);
  } // private static _findEffectiveMemberships(s, imm)

  private static String _getOid(Object o) 
    throws  MemberAddException
  {
    if      (o.getClass().equals(Group.class)) {
      return ( (Group) o ).getUuid();
    }
    else if (o.getClass().equals(Stem.class)) {
      return ( (Stem) o ).getUuid();
    }
    String err = ERR_IO + o.getClass();
    LOG.error(err);
    throw new MemberAddException(err);
  } // private static String _getOid(o)

  // TODO REFACTOR/DRY
  private static Set _membershipsToDelete(GrouperSession s, Membership imm) 
    throws  MemberDeleteException
  {
    GrouperSession.validate(s);
    Set     mships  = new LinkedHashSet();
    Field   f       = imm.getList();
    String  msg     = "_membershipsToDelete '" + f + "'";
    GrouperLog.debug(LOG, s, msg);
    try {
      Member m = imm.getMember();
      GrouperLog.debug(LOG, s, msg + " immediate member: " + imm);
      // Find effective memberships
      // As many of the memberships are likely to be transient, we
      // need to retrieve the persistent version of each before
      // passing it along to be deleted by HibernateHelper.  
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = MemberOf.doMemberOf(s, imm).iterator();
      while (iter.hasNext()) {
        Membership  ms    = (Membership) iter.next();
        Set         effs  = MembershipFinder.findEffectiveMemberships(
          ms.getOwner_id(), ms.getMember().getId(), 
          ms.getList(), ms.getVia_id(), ms.getDepth()
        );
        Iterator effsIter = effs.iterator();
        while (effsIter.hasNext()) {
          Membership eff = (Membership) effsIter.next();
          eff.setSession(s);
          GrouperLog.debug(LOG, s, msg + " effective member: " + eff);
          mships.add(eff);
        }
      }
      hs.close();
    }
    catch (Exception e) {
      GrouperLog.debug(LOG, s, msg + ": " + e.getMessage());
      throw new MemberDeleteException(e.getMessage());
    }
    return mships;
  } // private static Set _membershipsToDelete(s, imm)

  
  // Hibernate Accessors

  protected String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  public int getDepth() {
    return this.depth;
  }

  private void setDepth(int depth) {
    this.depth = depth;
  }

  private String getOwner_id() {
    return this.owner_id;
  }

  private void setOwner_id(String owner_id) {
    this.owner_id = owner_id;
  }

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

  private String getVia_id() {
    return this.via_id;
  }

  private void setVia_id(String via_id) {
    this.via_id = via_id;
  }

  private Membership getParent_membership() {
    return this.parent_membership;
  }

  private void setParent_membership(Membership parent) {
    this.parent_membership = parent;
  }

  private Status getStatus() {
    return this.status;
  }
  private void setStatus(Status s) {
    this.status = s;
  }

  private Member getCreator_id() {
    return this.creator_id;
  }
  private void setCreator_id(Member m) {
    this.creator_id = m;
  }
  private long getCreate_time() {
    return this.create_time;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }

  private String getUuid() {
    return this.uuid;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

}
