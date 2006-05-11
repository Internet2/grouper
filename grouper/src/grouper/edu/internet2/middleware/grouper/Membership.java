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
 * @version $Id: Membership.java,v 1.24.2.8 2006-05-11 17:14:22 blair Exp $
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
  private Owner       owner_id;
  private Membership  parent_membership;
  private Status      status;
  private String      uuid;
  private Owner       via_id;

  
  // Private Transient Instance Variables
  private transient GrouperSession  s;


  // Constructors //
  private Membership() {
    // Default constructor for Hibernate.
  }
  protected Membership(Owner o, Member m, Field f)
    throws  ModelException
  {
    this.setOwner_id(           o                     );
    this.setMember_id(          m                     );
    this.setField(              f                     );
    this.setUuid(               GrouperUuid.getUuid() );
    this.setDepth(              0                     );
    this.setVia_id(             null                  );
    this.setParent_membership(  null                  );
    this.setSession(            o.getSession()        );
    MembershipValidator.validateImmediate(this);
  } // protected Membership(o, m, f)

  protected Membership(Owner o, Member m, Field f, Composite via)
    throws  ModelException
  {
    this.setOwner_id(           o                     );
    this.setMember_id(          m                     );
    this.setField(              f                     );
    this.setUuid(               GrouperUuid.getUuid() );
    this.setDepth(              0                     );
    this.setVia_id(             via                   );
    this.setParent_membership(  null                  );
    this.setSession(            o.getSession()        );
    MembershipValidator.validateComposite(this);
  } // protected Membership(o, m, f, via)


  // Public Instance Methods //

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
    return MembershipFinder.findChildMemberships(this.getSession(), this);
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
    if (this.getOwner_id() instanceof Group) {
      Group g = (Group) this.getOwner_id();
      g.setSession(this.getSession());
      return g;
    }
    throw new GroupNotFoundException();
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
    GrouperSession.validate(this.getSession());
    Member m = this.getMember_id();
    if (m == null) {
      String msg = "unable to get member";
      GrouperLog.debug(LOG, this.getSession(), msg);
      throw new MemberNotFoundException(msg);
    }
    m.setSession(this.getSession());
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
    parent.setSession(this.getSession());
    return parent;
  } // public Membership getParentMembership()

  /**
   * Get this membership's via.
   * <pre calss="eg">
   * try {
   *   Owner via = ms.getVia();
   * }
   * catch (OwnerNotFoundException eONF) {
   *   // Unable to retrieve via
   * }
   * </pre>
   * @return  An {@link Owner}.
   * @throws  OwnerNotFoundException
   */
  public Owner getVia()
    throws  OwnerNotFoundException
  {
    if (this.getVia_id() != null) {
      Owner via = this.getVia_id();
      via.setSession(this.getSession());
      return via;
    }
    throw new OwnerNotFoundException();
  } // public Owner getVia()
 
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
    Group via = (Group) this.getVia_id();
    if ( (via != null) && (via instanceof Group) ) {
      via.setSession(this.getSession());
      return via;
    }
    throw new GroupNotFoundException();
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
    GrouperSession.validate(this.getSession());
    return new ToStringBuilder(this) 
      .append("owner"   , this.getOwner_id()  )
      .append("member"  , this.getMember_id() )
      .append("list"    , this.getField()     )
      .append("via"     , this.getVia_id()    )
      .append("depth"   , this.getDepth()     )
      .toString();
  } // public String toString()


  // Protected Class Methods //
  protected static void addCompositeMembership(
    GrouperSession s, Owner o, Composite c
  )
    throws  MemberAddException,
            ModelException
  {
    GrouperSessionValidator.validate(s);
    try {
      MemberOf mof = MemberOf.addComposite(s, o, c);
      HibernateHelper.saveAndDelete(mof.getSaves(), mof.getDeletes());
      // TODO EL.addEffMembers(s, o, subj, f, effs);
    }
    catch (HibernateException eH) {
      throw new MemberAddException(eH.getMessage(), eH);
    }    
  } // protected static void addCompositeMembership(s, o, c)

  protected static void addImmediateMembership(
    GrouperSession s, Owner o, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSessionValidator.validate(s);
      Member      m   = PrivilegeResolver.getInstance().canViewSubject(s, subj);
      Membership  imm = new Membership(o, m, f);
      MemberOf    mof = MemberOf.addImmediate(s, o, imm, m);
      HibernateHelper.saveAndDelete(mof.getSaves(), mof.getDeletes());
      EL.addEffMembers(s, o, subj, f, mof.getEffSaves());
    }
    catch (Exception e) {
      throw new MemberAddException(e.getMessage(), e);
    }    
  } // protected static void addImmediateMembership(s, o, subj, f)

  protected static void delCompositeMembership(GrouperSession s, Owner o, Composite c)
    throws  MemberDeleteException,
            ModelException
  {
    GrouperSessionValidator.validate(s);
    try {
      MemberOf  mof = MemberOf.delComposite(s, o, c);
      HibernateHelper.saveAndDelete(mof.getSaves(), mof.getDeletes());
      // TODO EL.delEffMembers(s, o, subj, f, effs);
    }
    catch (HibernateException eH) {
      throw new MemberDeleteException(eH.getMessage(), eH);
    }    
  } // protected static void delCompositeMembership(s, o, c)

  protected static void delImmediateMembership(
    GrouperSession s, Owner o, Subject subj, Field f
  )
    throws  MemberDeleteException
  {
    try {
      GrouperSessionValidator.validate(s); 
      // The objects that will need deleting and saving
      Set     deletes = new LinkedHashSet();
      Set     saves   = new LinkedHashSet();
      // Who we're deleting
      Member  m       = PrivilegeResolver.getInstance().canViewSubject(s, subj);

      o.setModified();
      saves.add(o);

      // Find memberships to delete
      Membership  imm = MembershipFinder.findImmediateMembership(o, m, f);
      imm.setSession(s);
      Set effs = _membershipsToDelete(s, imm);
      deletes.addAll(effs);
      deletes.add(imm);

      // And then commit changes to registry
      HibernateHelper.saveAndDelete(saves, deletes);
      EL.delEffMembers(s, o, subj, f, effs);
    }
    catch (Exception e) {
      throw new MemberDeleteException(e.getMessage(), e);
    } 
  } // protected static void delImmediateMembership(s, o, subj, f)

  protected static Set deleteAllField(GrouperSession s, Owner o, Field f) 
    throws  MemberDeleteException,
            SchemaException
  {
    try {
      GrouperSessionValidator.validate(s);
      GrouperSession orig = s;
      GrouperSession root = GrouperSessionFinder.getRootSession();
      o.setSession(root);

      Set deletes = new LinkedHashSet();

      // If o == group, deal with its immediate memberships
      if (o instanceof Group) {
        Iterator iterIs = ( (Group) o).toMember().getImmediateMemberships(f).iterator();
        while (iterIs.hasNext()) {
          Membership  ms    = (Membership) iterIs.next();
          Set         effs  = _membershipsToDelete(s, ms);
          deletes.addAll(effs);
          deletes.add(ms);
        }
      }

      // Now deal with immediate members
      Iterator iterHas = MembershipFinder.findImmediateMemberships(root, o, f).iterator();
      while (iterHas.hasNext()) {
        Membership  ms    = (Membership) iterHas.next();
        Set         effs  = _membershipsToDelete(s, ms);
        deletes.addAll(effs);
        deletes.add(ms);
      }

      o.setSession(orig);
      return deletes;
    }
    catch (ModelException eM) {
      throw new MemberDeleteException(eM.getMessage(), eM);
    }
  } // protected static Set deleteAllField(s, o, f)

  protected static Set deleteAllFieldType(GrouperSession s, Owner o, FieldType type) 
    throws  MemberDeleteException,
            SchemaException
  {
    try {
      GrouperSessionValidator.validate(s);
      GrouperSession orig = s;
      GrouperSession root = GrouperSessionFinder.getRootSession();
      o.setSession(root);

      Set deletes = new LinkedHashSet();

      Iterator iter = FieldFinder.findAllByType(type).iterator();
      while (iter.hasNext()) {
        Field f = (Field) iter.next();
        deletes.addAll( deleteAllField(s, o, f) );
      }

      o.setSession(orig);
      return deletes;
    }
    catch (ModelException eM) {
      throw new MemberDeleteException(eM.getMessage(), eM);
    }
  } // protected static Set deleteAllFieldType(s, o, f)


  // Protected Instance Methods //
  // FIXME No RTE
  protected GrouperSession getSession() {
    try {
      GrouperSessionValidator.validate(this.s);
      return this.s;
    }
    catch (ModelException eM) {
      eM.printStackTrace();
      throw new RuntimeException(eM.getMessage(), eM);
    }
  } // protected GrouperSession getSession()

  protected Stem getStem() 
    throws StemNotFoundException
  {
    if (this.getOwner_id() instanceof Stem) {
      Stem ns = (Stem) this.getOwner_id();
      ns.setSession(this.getSession());
      return ns;
    }
    throw new StemNotFoundException();
  } // public Stem getStem()

  // FIXME Deprecate
  protected static Membership newEffectiveMembership(
    GrouperSession s, Membership ms, Membership hasMS, int offset
  )
    throws  GroupNotFoundException,
            MemberNotFoundException
  { 
    Membership eff = new Membership();
    eff.s = s;
    eff.setUuid(      GrouperUuid.getUuid()         );  // assign uuid
    eff.setOwner_id(  ms.getOwner_id()              );  // original owner
    eff.setMember_id( hasMS.getMember()             );  // hasMember m
    eff.setField(     ms.getList()                  );  // original f
    eff.setDepth(                                       // increment the depth
      ms.getDepth() + hasMS.getDepth() + offset         
    );
    if (hasMS.getDepth() == 0) {
      eff.setVia_id(  hasMS.getOwner_id()           );  // hasMember m was immediate
    }
    else {
      eff.setVia_id(  hasMS.getVia_id()             );  // hasMember m was effective
    } 
    eff.setParent_membership(ms);                       // ms is parent membership
    GrouperLog.debug(LOG, s, "newEffectiveMembership: " + eff);
    return eff;       
  } // protected static Membership newEffectiveMembership(s, ms, hasMS)


  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
    Owner   o = this.getOwner_id();
    Member  m = this.getMember_id();
    Owner   v = this.getVia_id();
    if (o != null) {
      o.setSession(this.s);
      this.setOwner_id(o);
    }
    if (m != null) {
      m.setSession(this.s);
      this.setMember_id(m);
    }
    if (v != null) {
      v.setSession(this.s);
      this.setVia_id(v);
    }
  } // protected void setSession(s)


  // Private Class Methods //
  // TODO REFACTOR/DRY
  private static Set _membershipsToDelete(GrouperSession s, Membership imm) 
    throws  MemberDeleteException
  {
    Set     mships  = new LinkedHashSet();
    Field   f       = imm.getList();
    try {
      Member m = imm.getMember();
      // Find effective memberships
      // As many of the memberships are likely to be transient, we
      // need to retrieve the persistent version of each before
      // passing it along to be deleted by HibernateHelper.  
      // FIXME Actually, do we still need to now that HibernateHelper
      //       tries to untransient (at least select items).  
      //       But, even if the above is not true this code is still
      //       hateful.
      Session   hs    = HibernateHelper.getSession();
      MemberOf  mof   = MemberOf.delImmediate(s, imm.getOwner_id(), imm, m);
      Iterator  iter  = mof.getEffDeletes().iterator();
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
          mships.add(eff);
        }
      }
      hs.close();
    }
    catch (Exception e) {
      throw new MemberDeleteException(e.getMessage(), e);
    }
    return mships;
  } // private static Set _membershipsToDelete(s, imm)

 
  // Getters // 
  private long getCreate_time() {
    return this.create_time;
  }
  private Member getCreator_id() {
    return this.creator_id;
  }
  public int getDepth() {
    return this.depth;
  }
  protected Field getField() {
    return this.field;
  }
  protected String getId() {
    return this.id;
  }
  protected Member getMember_id() {
    return this.member_id;
  }
  protected Owner getOwner_id() {
    return this.owner_id;
  }
  protected Membership getParent_membership() {
    return this.parent_membership;
  }
  private Status getStatus() {
    return this.status;
  }
  protected String getUuid() {
    return this.uuid;
  }
  protected Owner getVia_id() {
    return this.via_id;
  }


  // Setters //
  private void setDepth(int depth) {
    this.depth = depth;
  }
  private void setField(Field f) {
    this.field = f;
  }
  private void setId(String id) {
    this.id = id;
  }
  private void setMember_id(Member member_id) {
    this.member_id = member_id;
  }
  private void setOwner_id(Owner o) {
    this.owner_id = o;
  }
  private void setParent_membership(Membership parent) {
    this.parent_membership = parent;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }
  private void setCreator_id(Member m) {
    this.creator_id = m;
  }
  private void setStatus(Status s) {
    this.status = s;
  }
  private void setVia_id(Owner via) {
    this.via_id = via;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

}
