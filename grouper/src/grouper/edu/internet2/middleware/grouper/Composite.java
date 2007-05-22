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
import  edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import  edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  edu.internet2.middleware.grouper.internal.util.Quote;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * A composite membership definition within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Composite.java,v 1.45 2007-05-22 14:09:44 blair Exp $
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
    return this._getGroup( this._getDTO().getLeftFactorUuid() );
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
    return this._getGroup( this._getDTO().getFactorOwnerUuid() );
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
    return this._getGroup( this._getDTO().getRightFactorUuid() );
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
    return CompositeType.getInstance( this._getDTO().getType() );
  } // public CompositeType getType()

  /**
   */
  public String getUuid() {
    return this._getDTO().getUuid();
  } // public String getUuid()

  /**
   * @since   1.0
   */
  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * @since   1.0
   */
  public String toString() {
    return  new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append( "type",  this.getType()                               )
      .append( "owner", Quote.single( this.internal_getOwnerName() ) )
      .append( "left",  Quote.single( this.internal_getLeftName()  ) )
      .append( "right", Quote.single( this.internal_getRightName() ) )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  /**
   * Identify composites where <code>g</code> is a factor and update memberships as necessary.
   * @since   1.2.0
   */
  protected static void internal_update(Group g) {
    Composite c;
    Iterator  it  = GrouperDAOFactory.getFactory().getComposite().findAsFactor( (GroupDTO) g.getDTO() ).iterator();
    while (it.hasNext()) {
      c = new Composite();
      c.setDTO( (CompositeDTO) it.next() );
      c.setSession( g.getSession() );
      c._update();
    }
  }


  // PROTECTED INSTANCE METHODS //

  // @since   1.1.0
  protected String getName() {
    return this.getClass().getName();
  } // protected String getName()

  // @since   1.2.0
  protected String internal_getLeftName() {
    return this._getName( this._getDTO().getLeftFactorUuid(), E.COMP_NULL_LEFT_GROUP );
  } 

  // @since   1.2.0
  protected String internal_getOwnerName() {
    return this._getName( this._getDTO().getFactorOwnerUuid(), E.COMP_NULL_OWNER_GROUP );
  }

  // @since   1.2.0
  protected String internal_getRightName() {
    return this._getName( this._getDTO().getRightFactorUuid(), E.COMP_NULL_RIGHT_GROUP );
  }

  // @since   1.2.0
  protected void internal_setModified() {
    // As composites can only be created and deleted at this time,
    // marking as modified is irrelevant. 
  } // protected void internal_setModified()


  // PRIVATE CLASS METHODS //

  /**
   * Given a set of memberships, extract the owner group and locate where each is the
   * member of a factor so that all factors can be updated as appropriate.
   * @since   1.2.0
   */
  private static void _update(GrouperSession s, Set mships) {
    try {
      Set groupsToUpdate  = new LinkedHashSet();
      // first find the owning group uuid for each membership
      MembershipDTO _ms;
      Iterator      it  = mships.iterator();
      while (it.hasNext()) {
        _ms = (MembershipDTO) it.next();
        groupsToUpdate.add( GrouperDAOFactory.getFactory().getGroup().findByUuid( _ms.getOwnerUuid() ) );
      }
      // and then find where each group is a factor and update 
      Composite     c;
      CompositeDAO  dao       = GrouperDAOFactory.getFactory().getComposite();
      Iterator      itFactor;
      it = groupsToUpdate.iterator();
      while (it.hasNext()) {
        itFactor = dao.findAsFactor( (GroupDTO) it.next() ).iterator();
        while (itFactor.hasNext()) {
          c = new Composite();
          c.setSession(s);
          c.setDTO( (CompositeDTO) itFactor.next() );
          c._update();
        }
      }
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( eGNF.getMessage(), eGNF );
    }
  } 


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private CompositeDTO _getDTO() {
    return (CompositeDTO) super.getDTO();
  } 
  
  // @since   1.2.0
  private Group _getGroup(String uuid) 
    throws  GroupNotFoundException
  {
    Group g = new Group();
    g.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) );
    g.setSession( this.getSession() );
    s.getMember().canView(g);
    return g;
  } 

  // @since   1.2.0
  private String _getName(String uuid, String msg) {
    try {
      Group g = new Group();
      g.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) );
      return g.getName();
    }
    catch (GroupNotFoundException eGNF) {
      ErrorLog.error( Composite.class, msg + Quote.single( this.getUuid() ) + ": " + eGNF.getMessage() );
      return GrouperConfig.EMPTY_STRING;
    }
  } 

  // @since   1.2.0
  private void _update() {
    //  TODO  20070321 Assuming this is actually correct I am sure it can be
    //        improved upon.  At least it isn't as bad as the first
    //        (functional) approach taken.  Or even the second, third
    //        or fourth approaches!
    try {
      StopWatch sw  = new StopWatch();
      sw.start();

      Group     g   = new Group();
      DefaultMemberOf  mof = new DefaultMemberOf();
      g.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid( this._getDTO().getFactorOwnerUuid() ) ); 
      mof.addComposite( this.getSession(), g, this );
  
      Set cur       = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( 
        ( (GroupDTO) g.getDTO() ).getUuid(), Group.getDefaultList()  // current mships
      );
      Set should    = mof.getEffectiveSaves();     // What mships should be
      Set deletes   = new LinkedHashSet(cur);         // deletes  = cur - should
      deletes.removeAll(should);
      Set adds      = new LinkedHashSet(should);      // adds     = should - cur
      adds.removeAll(cur);
      Map modified  = new HashMap();
      modified      = mof.identifyGroupsAndStemsToMarkAsModified( modified, adds.iterator() );
      modified      = mof.identifyGroupsAndStemsToMarkAsModified( modified, deletes.iterator() );
      Set modGroups = new LinkedHashSet( ( (Map) modified.get("groups") ).values() );
      Set modStems  = new LinkedHashSet( ( (Map) modified.get("stems") ).values() );

      if ( adds.size() > 0 || deletes.size() > 0 || modGroups.size() > 0 || modStems.size() > 0 ) {
        GrouperDAOFactory.getFactory().getComposite().update(adds, deletes, modGroups, modStems);
        sw.stop();
        EventLog.compositeUpdate(this, adds, deletes, sw);
        Composite._update( this.getSession(), deletes);
        Composite._update( this.getSession(), adds);
      }
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.COMP_UPDATE + eGNF.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
    catch (IllegalStateException eIS)   {
      String msg = E.COMP_UPDATE + eIS.getMessage();
      ErrorLog.error(Composite.class, msg);
    }
  } // private void _update()

} 

