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
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * A composite membership definition within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Composite.java,v 1.19 2006-09-05 19:46:34 blair Exp $
 * @since   1.0
 */
public class Composite extends Owner {

  // HIBERNATE PROPERTIES //
  private Owner         left  = null;
  private Owner         owner = null;
  private Owner         right = null;
  private CompositeType type  = null;


  // CONSTRUCTORS //

  // Default constructor for Hibernate.
  // @since   1.0
  protected Composite() {
    super();
  } // protected Composite()

  protected Composite(GrouperSession s, Owner o, Owner l, Owner r, CompositeType type) 
    throws  ModelException
  {
    // TODO I had a FIXME here without any context.  I wonder what I had in mind?
    this.setSessionNew(   s                     ); 
    this.setCreator_id(   s.getMember()         );
    this.setCreate_time(  new Date().getTime()  );
    this.setUuid(         GrouperUuid.getUuid() );
    this.setOwner(        o                     );
    this.setLeft(         l                     );
    this.setRight(        r                     );
    this.setType(         type                  );
    CompositeValidator.validate(this);
  } // protected Composite(s, o, l, r, type)  
  

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
    Composite otherComposite = (Composite) other;
    return new EqualsBuilder()
      .append(this.getUuid()        , otherComposite.getUuid()       )
      .append(this.getCreator_id()  , otherComposite.getCreator_id() )
      .isEquals();
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
    Group g = (Group) this.getLeft();
    try {
      Validator.valueNotNull(g, E.GROUP_NULL);
      GrouperSession s = this.getSession();
      g.setSession(s);
      s.getMember().canView(g);
      return g;
    }
    catch (Exception e) {
      throw new GroupNotFoundException(e);
    }
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
    Group g = (Group) this.getOwner();
    try {
      Validator.valueNotNull(g, E.GROUP_NULL);
      GrouperSession s = this.getSession();
      g.setSession(s);
      s.getMember().canView(g);
      return g;
    }
    catch (Exception e) {
      throw new GroupNotFoundException(e);
    }
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
    Group g = (Group) this.getRight();
    try {
      Validator.valueNotNull(g, E.GROUP_NULL);
      GrouperSession s = this.getSession();
      g.setSession(s);
      s.getMember().canView(g);
      return g;
    }
    catch (Exception e) {
      throw new GroupNotFoundException(e);
    }
  } // public Group getLeftGroup()

  /**
   * @since 1.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getUuid()        )
      .append(this.getCreator_id()  )
      .toHashCode()
      ;
  } // public int hashCode()

  /**
   * @since 1.0
   */
  public String toString() {
    return  new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append(  "type"  , this.getType().toString() )
      .append(  "owner" , U.q(this.getOwnerName() ) )
      .append(  "left"  , U.q(this.getLeftName()  ) )
      .append(  "right" , U.q(this.getRightName() ) )
      .toString();
  } // public String toString()


  // Protected Class Methods //
  // @since 1.0
  protected static void update(Owner o) {
    Composite c;
    Iterator  iter  = CompositeFinder.findAsFactorNoPriv(o).iterator();
    while (iter.hasNext()) {
      c = (Composite) iter.next();
      c.update();
    }
  } // protected static void update(o)


  // PROTECTED INSTANCE METHODS //

  // @since 1.0
  protected String getLeftName() {
    try {
      Group g = (Group) this.getLeft();
      Validator.valueNotNull(g, E.GROUP_NULL);
      return g.getName();
    }
    catch (NullPointerException eNP) {
      ErrorLog.error(Composite.class, E.COMP_NULL_LEFT_GROUP + U.q(this.getUuid()));
      return GrouperConfig.EMPTY_STRING;
    }
  } // protected String getLeftName()

  // @since 1.0
  protected String getOwnerName() {
    try {
      Group g = (Group) this.getOwner();
      Validator.valueNotNull(g, E.GROUP_NULL);
      return g.getName();
    }
    catch (NullPointerException eNP) {
      ErrorLog.error(Composite.class, E.COMP_NULL_OWNER_GROUP + U.q(this.getUuid()));
      return GrouperConfig.EMPTY_STRING;
    }
  } // protected String getOwnerName()

  // @since 1.0
  protected String getRightName() {
    try {
      Group g = (Group) this.getRight();
      Validator.valueNotNull(g, E.GROUP_NULL);
      return g.getName();
    }
    catch (NullPointerException eNP) {
      ErrorLog.error(Composite.class, E.COMP_NULL_RIGHT_GROUP + U.q(this.getUuid()));
      return GrouperConfig.EMPTY_STRING;
    }
  } // protected String getRightName()

  // @since   1.0
  protected void setModified() {
    // As composites can only be created and deleted at this time,
    // marking as modified is irrelevant. 
  } // protected void setModified()

  // @since 1.0
  protected void update() {
    //  TODO  Assuming this is actually correct I am sure it can be
    //        improved upon.  At least it isn't as bad as the first
    //        (functional) approach taken.  Or even the second, third
    //        or fourth approaches!
    try {
      StopWatch sw  = new StopWatch();
      sw.start();
      GrouperSession rs  = this.getSession().getRootSession();
      this.setSession(rs);
      Group           g   = this.getOwnerGroup();
      MemberOf        mof = MemberOf.addComposite(rs, g, this);

      Set cur     = g.getMemberships();         // Current mships
      Set should  = mof.getEffSaves();          // What mships should be
      Set deletes = new LinkedHashSet(cur);     // deletes  = cur - should
      deletes.removeAll(should);
      Set adds    = new LinkedHashSet(should);  // adds     = should - cur
      adds.removeAll(cur);

      if ( (adds.size() > 0) || (deletes.size() > 0) ) {
        HibernateHelper.saveAndDelete(adds, deletes);
        EventLog.compositeUpdate(this, adds, deletes, sw);
        sw.stop();
        Composite._update(deletes);
        Composite._update(adds);
      }
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.COMP_UPDATE + eGNF.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
    catch (HibernateException eH) {
      String msg = E.COMP_UPDATE + eH.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
    catch (ModelException eM) {
      String msg = E.COMP_UPDATE + eM.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
  } // protected void update()


  // PRIVATE CLASS METHODS //
  // @since 1.0
  private static void _update(Set mships) {
    Set         updates = new LinkedHashSet();
    Membership  ms;
    Iterator    iterMS  = mships.iterator();
    while (iterMS.hasNext()) {
      ms = (Membership) iterMS.next();
      updates.add( ms.getOwner_id() ); 
    }
    Owner     o;
    Iterator  iter;
    Composite c;
    Iterator  iterU = updates.iterator();
    while (iterU.hasNext()) {
      o     = (Owner) iterU.next();
      iter  = CompositeFinder.findAsFactorNoPriv(o).iterator();
      while (iter.hasNext()) {
        c = (Composite) iter.next();
        c.update();
      }
    }
  } // private static void _update(mships)


  // GETTERS //
  // @since 1.0
  protected Owner getLeft() {
    return this.left;
  }
  // @since 1.0
  protected Owner getOwner() {
    return this.owner;
  }
  // @since 1.0
  protected Owner getRight() {
    return this.right;
  }
  /**
   * Return this composite's type.
   * <pre class="eg">
   * CompositeType type = c.getType();
   * </pre>
   * @return  {@link CompositeType} of this {@link Composite}.
   * @since   1.0
   */
  public CompositeType getType() {
    return this.type;
  } // public CompositeType getType()


  // SETTERS //
  // @since 1.0
  private void setLeft(Owner l) {
    this.left = l;
  }
  // @since 1.0
  private void setOwner(Owner o) {
    this.owner = o;
  }
  // @since 1.0
  private void setRight(Owner r) {
    this.right = r;
  }
  // @since 1.0
  private void setType(CompositeType type) {
    this.type = type;
  }

}

