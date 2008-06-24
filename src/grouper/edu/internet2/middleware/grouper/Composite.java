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
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
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
 * 
 * A composite group is composed of two groups and a set operator 
 * (stored in grouper_composites table)
 * (e.g. union, intersection, etc).  A composite group has no immediate members.
 * All subjects in a composite group are effective members.
 * 
 * <p/>
 * @author  blair christensen.
 * @version $Id: Composite.java,v 1.50 2008-06-24 06:07:03 mchyzer Exp $
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
   * Identify memberships (composites and not) where updated need to be performed.
   * @since   1.2.0
   */
  protected static void internal_update(Group g) {
    Set factorOwners = _updateWhereGroupIsFactor(g);
    try {
      _updateWhereFactorOwnersAreImmediateMembers(g, factorOwners);
    }
    catch (GroupNotFoundException eShouldNotHappen) {
      ErrorLog.fatal( Composite.class, "error processing composite updates: " + eShouldNotHappen.getMessage() );  
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
  private static void _updateComposites(GrouperSession s, final Set mships) {
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          Set groupsToUpdate  = new LinkedHashSet();
          // first find the owning group uuid for each membership
          MembershipDTO _ms;
          Iterator      it  = mships.iterator();
          while (it.hasNext()) {
            _ms = (MembershipDTO) it.next();
            try {
              groupsToUpdate.add( GrouperDAOFactory.getFactory().getGroup().findByUuid( _ms.getOwnerUuid() ) );
            } catch (GroupNotFoundException gnfe) {
              throw new GrouperSessionException(gnfe);
            }
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
              c.setDTO( (CompositeDTO) itFactor.next() );
              c._update();
            }
          }
          return null;
        }
        
      });
    }
    catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GroupNotFoundException) {
        throw new IllegalStateException( gse.getCause().getMessage(), gse.getCause() );
      }
      throw new IllegalStateException( gse.getMessage(), gse );
    }
  } 

  /**
   * Update effective memberships where a) the modified group b) is a factor and c) the factor owner
   * is an immediate member elsewhere.
   * @param   g     The original modified group.
   * @throws  GroupNotFoundException
   * @since   1.2.0
   */
  private static void _updateWhereFactorOwnersAreImmediateMembers(Group g, Set factorOwners) 
    throws  GroupNotFoundException
  {
    Group           factorOwner;
    String          factorOwnerUuid;

    Iterator it = factorOwners.iterator();
      while (it.hasNext()) {
        factorOwnerUuid = (String) it.next();
        factorOwner     = new Group();
        factorOwner.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid(factorOwnerUuid) );
        _updateWhereFactorOwnerIsImmediateMember(factorOwner);
      }
  }

  /**
   * Update effective memberships where the factor owner is an immediate member.
   * @throws  GroupNotFoundException
   * @since   1.2.0
   */
  private static void _updateWhereFactorOwnerIsImmediateMember(Group factorOwner)
    throws  GroupNotFoundException
  {
    MemberDTO       _m        = (MemberDTO) factorOwner.toMember().getDTO();
    DefaultMemberOf mof;
    MembershipDTO   _ms;
    Group           msOwner;

    // Find everywhere where the factor owner is an immediate member, delete the
    // membership and then recreate it.
    Iterator it = GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(
      factorOwner.toMember().getUuid(), Group.getDefaultList()
    ).iterator();
    while (it.hasNext()) {
      _ms     = (MembershipDTO) it.next();
      msOwner = new Group();
      msOwner.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid( _ms.getOwnerUuid() ) );

      // TODO 20070524 ideally i wouldn't delete and then re-add the membership.  bad programmer.  
      //               i *should* identify where there have been changes and then only
      //               update *those* memberships.
      mof = new DefaultMemberOf();
      mof.deleteImmediate( GrouperSession.staticGrouperSession(), msOwner, _ms, _m );
      GrouperDAOFactory.getFactory().getMembership().update(mof);

      mof = new DefaultMemberOf();
      mof.addImmediate( GrouperSession.staticGrouperSession(), msOwner, Group.getDefaultList(), _m );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
    
      // TODO 20070524 do i need to call "Composite.internal_update(msOwner)"?  i
      //               certainly hope not and so far the tests suggest no. 
    }
  }

  /**
   * Update composites where modified group is a factor.
   * @return  <i>Set</i> of factor owner UUIDs for use by {@link #_updateWhereFactorOwnersAreImmediateMembers(Group, Set)}.
   * @since   1.2.0
   */
  private static Set _updateWhereGroupIsFactor(Group g) {
    Composite c;
    Set       factorOwners  = new LinkedHashSet();
    Iterator  it            = GrouperDAOFactory.getFactory().getComposite().findAsFactor( (GroupDTO) g.getDTO() ).iterator();
    while (it.hasNext()) {
      c = new Composite();
      c.setDTO( (CompositeDTO) it.next() );
      factorOwners.add( c._getDTO().getFactorOwnerUuid() );
      c._update();
    }
    return factorOwners; // TODO 20070524 aesthetically this is inappropriate
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
    GrouperSession.staticGrouperSession().getMember().canView(g);
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
      mof.addComposite( GrouperSession.staticGrouperSession(), g, this );
  
      Set cur       = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( 
        ( (GroupDTO) g.getDTO() ).getUuid(), Group.getDefaultList()  // current mships
      );
      Set should    = mof.getEffectiveSaves();    // What mships should be
      Set deletes   = new LinkedHashSet(cur);     // deletes  = cur - should
      deletes.removeAll(should);
      Set adds      = new LinkedHashSet(should);  // adds     = should - cur
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
        _updateComposites( GrouperSession.staticGrouperSession(), deletes);
        _updateComposites( GrouperSession.staticGrouperSession(), adds);
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
  } 

} 

