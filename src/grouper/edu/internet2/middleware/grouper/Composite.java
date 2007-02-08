/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import  java.util.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * A composite membership definition within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Composite.java,v 1.30 2007-02-08 16:25:25 blair Exp $
 * @since   1.0
 */
public class Composite extends GrouperAPI {

  // PUBLIC INSTANCE METHODS //
  /**
   * @since 1.0
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Composite)) {
      return false;
    }
    return this.getDTO().equals( ( (Composite) other ).getDTO() );
  } // public boolean equals(other)

  /**
   * Return this {@link Composite}'s left factor.
   * <pre class="eg">
   * try {
   *   Group left = c.getLeftGroup();
   * }
   * catch (GroupNotFoundException eGNF) {
   *   // unable to retrieve group
   * }
   * </pre>
   * @return  Left factor {@link Group}.
   * @throws  GroupNotFoundException
   * @since   1.0
   */
  public Group getLeftGroup() 
    throws  GroupNotFoundException
  {
    // XXX
    Group g = new Group();
    g.setDTO( HibernateGroupDAO.findByUuid( this.getDTO().getLeftFactorUuid() ) );
    g.setSession( this.getSession() );
    s.getMember().canView(g);
    return g;
  } // public Group getLeftGroup()

  /**
   * Return this {@link Composite}'s owner.
   * <pre class="eg">
   * try {
   *   Group owner = c.geOwnerGroup();
   * }
   * catch (GroupNotFoundException eGNF) {
   *   // unable to retrieve group
   * }
   * </pre>
   * @return  Owner {@link Group}.
   * @throws  GroupNotFoundException
   * @since   1.0
   */
  public Group getOwnerGroup() 
    throws  GroupNotFoundException
  {
    Group g = new Group();
    g.setDTO( HibernateGroupDAO.findByUuid( this.getDTO().getFactorOwnerUuid() ) );
    g.setSession( this.getSession() );
    s.getMember().canView(g);
    return g;
  } // public Group getOwnerGroup()

  /**
   * Return this {@link Composite}'s right factor.
   * <pre class="eg">
   * try {
   *   Group right = c.getRightGroup();
   * }
   * catch (GroupNotFoundException eGNF) {
   *   // unable to retrieve group
   * }
   * </pre>
   * @return  Right factor {@link Group}.
   * @throws  GroupNotFoundException
   * @since   1.0
   */
  public Group getRightGroup() 
    throws  GroupNotFoundException
  {
    Group g = new Group();
    g.setDTO( HibernateGroupDAO.findByUuid( this.getDTO().getRightFactorUuid() ) );
    g.setSession( this.getSession() );
    s.getMember().canView(g);
    return g;
  } // public Group getLeftGroup()

  /**
   * Return this composite's type.
   * <pre class="eg">
   * CompositeType type = c.getType();
   * </pre>
   * @return  {@link CompositeType} of this {@link Composite}.
   * @since   1.0
   */
  public CompositeType getType() {
    return CompositeType.getInstance( this.getDTO().getType() );
  } // public CompositeType getType()

  /**
   */
  public String getUuid() {
    return this.getDTO().getUuid();
  } // public String getUuid()

  /**
   * @since   1.0
   */
  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * @since 1.0
   */
  public String toString() {
    // TODO 20070125 replace with call to DTO?
    return  new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append( "type",  this.getType()                                     )
      .append( "owner", U.internal_q( CompositeHelper.getOwnerName(this) ) )
      .append( "left",  U.internal_q( CompositeHelper.getLeftName(this)  ) )
      .append( "right", U.internal_q( CompositeHelper.getRightName(this) ) )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // TODO 20070125 revisit these methods once initial daoification is complete

  // @since   1.2.0
  protected static void internal_update(Group g) {
    Composite c;
    Iterator  it  = HibernateCompositeDAO.findAsFactor( g.getDTO() ).iterator();
    while (it.hasNext()) {
      c = new Composite();
      c.setDTO( (CompositeDTO) it.next() );
      c.setSession( g.getSession() );
      c._update();
    }
  } // protected static void internal_update(g)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected CompositeDTO getDTO() {
    return (CompositeDTO) super.getDTO();
  } // protected CompositeDTO getDTO()
 
  // TODO 20070125 revisit these methods once initial daoification is complete

  // @since   1.1.0
  protected String getName() {
    return this.getClass().getName();
  } // protected String getName()

  // @since   1.2.0
  protected void internal_setModified() {
    // As composites can only be created and deleted at this time,
    // marking as modified is irrelevant. 
  } // protected void internal_setModified()


  // PRIVATE CLASS METHODS //

  // @since   1.0
  private static void _update(Set mships) {
    Set         updates = new LinkedHashSet();
    Membership  ms;
    Iterator    iterMS  = mships.iterator();
    while (iterMS.hasNext()) {
      // TODO 20070125 !!!
      Object obj = iterMS.next();
      if (obj instanceof MembershipDTO) {
        Membership tmp = new Membership();
        tmp.setDTO( (MembershipDTO) obj );
        updates.add( MembershipHelper.getOwner(tmp) );
      }
      else {
        updates.add( MembershipHelper.getOwner( (Membership) obj ) );
      }
    }
    Group     g;
    Iterator  iter;
    Composite c;
    Iterator  iterU = updates.iterator();
    while (iterU.hasNext()) {
      g     = (Group) iterU.next();
      iter  = HibernateCompositeDAO.findAsFactor( g.getDTO() ).iterator();
      while (iter.hasNext()) {
        c = new Composite();
        c.setDTO( (CompositeDTO) iter.next() );
        c._update();
      }
    }
  } // private static void _update(mships)


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private void _update() {
    //  TODO  20061011 Assuming this is actually correct I am sure it can be
    //        improved upon.  At least it isn't as bad as the first
    //        (functional) approach taken.  Or even the second, third
    //        or fourth approaches!
    try {
      StopWatch sw  = new StopWatch();
      sw.start();
      // TODO 20070208 THIS. SHOULD. NOT. BE. NECESSARY.
      //GrouperSession rs  = this.getSession().getDTO().getRootSession();
      GrouperSession rs = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.setSession(rs);
      Group           g   = this.getOwnerGroup();
      MemberOf        mof = MemberOf.internal_addComposite(rs, g, this);

      Set cur     = g.getMemberships();         // Current mships
      Set should  = mof.internal_getEffSaves(); // What mships should be
      Set deletes = new LinkedHashSet(cur);     // deletes  = cur - should
      deletes.removeAll(should);
      Set adds    = new LinkedHashSet(should);  // adds     = should - cur
      adds.removeAll(cur);

      if ( (adds.size() > 0) || (deletes.size() > 0) ) {
        HibernateCompositeDAO.update(adds, deletes);
        sw.stop();
        EventLog.compositeUpdate(this, adds, deletes, sw);
        Composite._update(deletes);
        Composite._update(adds);
      }
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.COMP_UPDATE + eGNF.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
    catch (ModelException eM) {
      String msg = E.COMP_UPDATE + eM.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
    catch (SessionException eS) {
      String msg = E.COMP_UPDATE + eS.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
  } // private void _update()

} // public class Composite extends GrouperAPI

