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

import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * A composite membership definition within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Composite.java,v 1.2 2006-05-23 19:10:22 blair Exp $
 *     
*/
public class Composite extends Owner implements Serializable {

  // FIXME What about privs?

  // Private Class Constants //
  private static final EventLog EL    = new EventLog();
  private static final String   ERR_U = "unable to update composite membership: ";
  private static final Log      LOG   = LogFactory.getLog(Composite.class);

  // Hibernate Properties //
  private Owner         left  = null;
  private Owner         owner = null;
  private Owner         right = null;
  private CompositeType type  = null;


  // Constructors //
  private Composite() {
    // Default constructor for Hibernate
  } // private Composite()

  protected Composite(GrouperSession s, Owner o, Owner l, Owner r, CompositeType type) 
    throws  ModelException
  {
    this.setSessionNew(   s                     ); // FIXME
    this.setCreator_id(   s.getMember()         );
    this.setCreate_time(  new Date().getTime()  );
    this.setUuid(         GrouperUuid.getUuid() );
    this.setOwner(        o                     );
    this.setLeft(         l                     );
    this.setRight(        r                     );
    this.setType(         type                  );
    CompositeValidator.validate(this);
  } // protected Composite(s, o, l, r, type)  
  

  // Public Instance Methods //
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

  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getUuid()        )
      .append(this.getCreator_id()  )
      .toHashCode()
      ;
  } // public int hashCode()

  public void setModified() {
    // As composites can only be created and deleted at this time,
    // marking as modified is irrelevant. 
  } // public void setModified()

  public String toString() {
    return  new ToStringBuilder(this)
      .append(  "type"  , this.getType().toString()       )
      .append(  "group" , this.getOwnerGroup().getName()  )
      .append(  "left"  , this.getLeftGroup().getName()   )
      .append(  "right" , this.getRightGroup().getName()  )
      .toString();
  } // public String toString()


  // Protected Class Methods //
  protected static void update(Owner o) {
    Iterator iter = CompositeFinder.isFactor(o).iterator();
    while (iter.hasNext()) {
      Composite c = (Composite) iter.next();
      c.update();
    }
  } // protected static void update(o)


  // Protected Instance Methods //
  protected void update() {
    //  TODO  Assuming this is actually correct I am sure it can be
    //        improved upon.  At least it isn't as bad as the first
    //        (functional) approach taken.  Or even the second, third
    //        or fourth approaches!
    try {
      GrouperSession  rs  = GrouperSessionFinder.getRootSession();
      this.setSession(rs);
      CompositeType   t   = this.getType();
      Group           g   = this.getOwnerGroup();
      Group           l   = this.getLeftGroup();
      Group           r   = this.getRightGroup();
      MemberOf        mof = MemberOf.addComposite(rs, g, this);

      Set cur     = g.getMemberships();       // Current mships
      // TODO Improve. Sanify.  And so forth.
      // This one is a little more complicated.  What we want is to get
      // the list of memberships that the composite should have.  We
      // retrieve that with `mof.getEffSaves()`.  However, the
      // memberships in that set will never be equal to current set of
      // memberships as each membership will have a new uuid.
      //
      // **sigh**
      Set evaled  = new LinkedHashSet();
      Iterator i  = mof.getEffSaves().iterator();
      while (i.hasNext()) {
        Membership ms = (Membership) i.next();
        try {
          Membership exists = MembershipFinder.findImmediateMembership(
            ms.getOwner_id(), ms.getMember_id(), ms.getField()
          );
          exists.setSession(rs);
          evaled.add(exists);
        }
        catch (MembershipNotFoundException eMNF) {
          evaled.add(ms);
        }        
      }
      Set deletes = new LinkedHashSet(cur);     // deletes  = cur - evaled
      deletes.removeAll(evaled);
      Set adds    = new LinkedHashSet(evaled);  // adds     = evaled - cur
      adds.removeAll(cur);

      if ( (adds.size() > 0) || (deletes.size() > 0) ) {
        HibernateHelper.saveAndDelete(adds, deletes);
        Composite._update(deletes);
        Composite._update(adds);
        //  FIXME LOG! additions + deletions
      }
    }
    catch (HibernateException eH) {
      // FIXME  Log!
      throw new RuntimeException(ERR_U + eH.getMessage(), eH);
    }
    catch (ModelException eM) {
      // FIXME  Log!
      throw new RuntimeException(ERR_U + eM.getMessage(), eM);
    }
  } // protected void update()


  // Private Class Methods
  private static void _update(Set mships) {
    Set       updates = new LinkedHashSet();
    Iterator  iterMS  = mships.iterator();
    while (iterMS.hasNext()) {
      Membership ms = (Membership) iterMS.next();
      updates.add( ms.getOwner_id() ); 
    }
    Iterator  iterU   = updates.iterator();
    while (iterU.hasNext()) {
      Owner     o     = (Owner) iterU.next();
      Iterator  iter  = CompositeFinder.isFactor(o).iterator();
      while (iter.hasNext()) {
        Composite c = (Composite) iter.next();
        c.update();
      }
    }
  } // private static void _update(mships)


  // Getters //
  protected Owner getLeft() {
    return this.left;
  }
  protected Group getLeftGroup() {
    // TODO Should this through an exception upon failure?
    Group g = (Group) this.left;
    g.setSession( this.getSession() );
    return g;
  }
  protected Owner getOwner() {
    return this.owner;
  }
  protected Group getOwnerGroup() {
    Group g = (Group) this.owner;
    g.setSession( this.getSession() );
    return g;
  }
  protected Owner getRight() {
    return this.right;
  }
  protected Group getRightGroup() {
    // TODO Should this through an exception upon failure?
    Group g = (Group) this.right;
    g.setSession( this.getSession() );
    return g;
  }
  protected CompositeType getType() {
    return this.type;
  }


  // Setters //
  private void setLeft(Owner l) {
    this.left = l;
  }
  private void setOwner(Owner o) {
    this.owner = o;
  }
  private void setRight(Owner r) {
    this.right = r;
  }
  private void setType(CompositeType type) {
    this.type = type;
  }

}

