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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.CompositeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
 * @version $Id: Composite.java,v 1.64 2009-01-27 12:09:24 mchyzer Exp $
 * @since   1.0
 */
@SuppressWarnings("serial")
public class Composite extends GrouperAPI implements Hib3GrouperVersioned {

  /** table for composites */
  public static final String TABLE_GROUPER_COMPOSITES = "grouper_composites";

  /** id col in db */
  public static final String COLUMN_ID = "id";

  /** uuid col in db */
  public static final String COLUMN_UUID = "uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_UUID = "old_uuid";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: factorOwnerUUID */
  public static final String FIELD_FACTOR_OWNER_UUID = "factorOwnerUUID";

  /** constant for field name for: leftFactorUUID */
  public static final String FIELD_LEFT_FACTOR_UUID = "leftFactorUUID";

  /** constant for field name for: rightFactorUUID */
  public static final String FIELD_RIGHT_FACTOR_UUID = "rightFactorUUID";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_FACTOR_OWNER_UUID, FIELD_LEFT_FACTOR_UUID, 
      FIELD_RIGHT_FACTOR_UUID, FIELD_TYPE, FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DB_VERSION, FIELD_FACTOR_OWNER_UUID, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_LEFT_FACTOR_UUID, FIELD_RIGHT_FACTOR_UUID, FIELD_TYPE, 
      FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  private long    createTime;
  /** */
  private String  creatorUUID;
  /** */
  private String  factorOwnerUUID;
  /** */
  private String  leftFactorUUID;
  /** */
  private String  rightFactorUUID;
  /** */
  private String  type;
  /** */
  private String  uuid;

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
    return this._getGroup( this.getLeftFactorUuid() );
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
    return this._getGroup( this.getFactorOwnerUuid() );
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
    return this._getGroup( this.getRightFactorUuid() );
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
    return CompositeType.getInstance( this.type );
  } // public CompositeType getType()

  /**
   * simple getter for type for db
   * @return type db
   */
  public String getTypeDb() {
    return this.type;
  }
  
  /**
   * @return string
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
   * @param g 
   * @since   1.2.0
   */
  protected static void internal_update(Group g) {
    Set factorOwners = _updateWhereGroupIsFactor(g);
    try {
      _updateWhereFactorOwnersAreImmediateMembers(g, factorOwners);
    }
    catch (GroupNotFoundException eShouldNotHappen) {
      LOG.fatal("error processing composite updates: " + eShouldNotHappen.getMessage() );  
    }
    catch (StemNotFoundException eShouldNotHappen) {
      LOG.fatal("error processing composite updates: " + eShouldNotHappen.getMessage() );  
    }
    catch (SchemaException eShouldNotHappen) {
      LOG.fatal("error processing composite updates: " + eShouldNotHappen.getMessage() );  
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Composite.class);



  // PROTECTED INSTANCE METHODS //

  /**
   * @return name
   * 
   */
  public String getName() {
    return this.getClass().getName();
  } 

  /**
   * left name
   * @return left name
   */
  public String internal_getLeftName() {
    return this._getName( this.getLeftFactorUuid(), E.COMP_NULL_LEFT_GROUP );
  } 

  /**
   * owner name
   * @return the owner name
   */
  public String internal_getOwnerName() {
    return this._getName( this.getFactorOwnerUuid(), E.COMP_NULL_OWNER_GROUP );
  }

  /**
   * right name
   * @return right name
   */
  public String internal_getRightName() {
    return this._getName( this.getRightFactorUuid(), E.COMP_NULL_RIGHT_GROUP );
  }

  /**
   * Update effective memberships where a) the modified group b) is a factor and c) the factor owner
   * is an immediate member elsewhere.
   * @param   g     The original modified group.
   * @param factorOwners 
   * @throws  GroupNotFoundException
   * @throws SchemaException 
   * @throws StemNotFoundException 
   * @since   1.2.0
   */
  private static void _updateWhereFactorOwnersAreImmediateMembers(Group g, Set factorOwners) 
    throws  GroupNotFoundException, SchemaException, StemNotFoundException
  {
    Group           factorOwner;
    String          factorOwnerUuid;

    Iterator it = factorOwners.iterator();
      while (it.hasNext()) {
        factorOwnerUuid = (String) it.next();
        factorOwner     = GrouperDAOFactory.getFactory().getGroup().findByUuid(factorOwnerUuid) ;
        _updateWhereFactorOwnerIsImmediateMember(factorOwner);
      }
  }

  /**
   * Update effective memberships where the factor owner is an immediate member.
   * @param factorOwner 
   * @throws  GroupNotFoundException
   * @throws SchemaException 
   * @throws StemNotFoundException 
   * @since   1.2.0
   */
  private static void _updateWhereFactorOwnerIsImmediateMember(Group factorOwner)
    throws  GroupNotFoundException, SchemaException, StemNotFoundException
  {
    Member       _m        = (Member) factorOwner.toMember();
    DefaultMemberOf mof;
    Membership   _ms;

    // Find everywhere where the factor owner is an immediate member, delete the
    // membership and then recreate it.
    Iterator it = GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMember(
      factorOwner.toMember().getUuid()).iterator();
    while (it.hasNext()) {
      _ms     = (Membership) it.next();
      Field f = FieldFinder.find(_ms.getListName());
      if (!FieldType.NAMING.equals(f.getType())) {
        Group msOwner =  _ms.getGroup() ;

        // TODO 20070524 ideally i wouldn't delete and then re-add the membership.  bad programmer.  
        //               i *should* identify where there have been changes and then only
        //               update *those* memberships.
        mof = new DefaultMemberOf();
        mof.deleteImmediate( GrouperSession.staticGrouperSession(), msOwner, _ms, _m );
        GrouperDAOFactory.getFactory().getMembership().update(mof);

        mof = new DefaultMemberOf();
        mof.addImmediate( GrouperSession.staticGrouperSession(), msOwner, f, _m );
        GrouperDAOFactory.getFactory().getMembership().update(mof);

        // TODO 20070524 do i need to call "Composite.internal_update(msOwner)"?  i
        //               certainly hope not and so far the tests suggest no. 

        // 20080813 - Looks like we do need to call this actually....
        Composite.internal_update(msOwner);
      } else {
        Stem msOwner = _ms.getStem();

        mof = new DefaultMemberOf();
        mof.deleteImmediate(GrouperSession.staticGrouperSession(), msOwner, _ms, _m);
        GrouperDAOFactory.getFactory().getMembership().update(mof);

        mof = new DefaultMemberOf();
        mof.addImmediate(GrouperSession.staticGrouperSession(), msOwner, f, _m);
        GrouperDAOFactory.getFactory().getMembership().update(mof);
      }
    }
  }

  /**
   * Update composites where modified group is a factor.
   * @param g 
   * @return  <i>Set</i> of factor owner UUIDs for use by {@link #_updateWhereFactorOwnersAreImmediateMembers(Group, Set)}.
   * @since   1.2.0
   */
  private static Set _updateWhereGroupIsFactor(Group g) {
    Composite c;
    Set       factorOwners  = new LinkedHashSet();
    Iterator  it            = GrouperDAOFactory.getFactory().getComposite().findAsFactor( g ).iterator();
    while (it.hasNext()) {
      c =  (Composite)it.next() ;
      factorOwners.add( c.getFactorOwnerUuid() );
      c._update();
    }
    return factorOwners; // TODO 20070524 aesthetically this is inappropriate
  }


  /**
   * 
   * @param uuid
   * @return group
   * @throws GroupNotFoundException
   */
  private Group _getGroup(String uuid) 
    throws  GroupNotFoundException {
    Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) ;
    GrouperSession.staticGrouperSession().getMember().canView(g);
    return g;
  } 

  /**
   * 
   * @param uuid
   * @param msg
   * @return name
   */
  private String _getName(String uuid, String msg) {
    try {
      Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) ;
      return g.getName();
    }
    catch (GroupNotFoundException eGNF) {
      LOG.error( msg + Quote.single( this.getUuid() ) + ": " + eGNF.getMessage() );
      return GrouperConfig.EMPTY_STRING;
    }
  } 

  /**
   * 
   */
  private void _update() {
    //  TODO  20070321 Assuming this is actually correct I am sure it can be
    //        improved upon.  At least it isn't as bad as the first
    //        (functional) approach taken.  Or even the second, third
    //        or fourth approaches!
    try {
      StopWatch sw  = new StopWatch();
      sw.start();

      Group     g   = GrouperDAOFactory.getFactory().getGroup().findByUuid( this.getFactorOwnerUuid() ) ;
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.addComposite( GrouperSession.staticGrouperSession(), g, this );
  
      Set cur       = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField( 
        g.getUuid(), Group.getDefaultList()  // current mships
      );
      Set shouldBeforeFilter = mof.getEffectiveSaves();    // What mships should be before filtering

      Set<Membership> should = new LinkedHashSet();    // What mships should be after filtering

      // filter out memberships that have a different owner than the composite group.
      Iterator<Membership> shouldIterator = shouldBeforeFilter.iterator();
      while (shouldIterator.hasNext()) {
        Membership shouldMembership = shouldIterator.next();
        if (shouldMembership.getOwnerGroupId().equals(this.getFactorOwnerUuid()) &&
          shouldMembership.getType().equals(Membership.COMPOSITE)) {
          should.add(shouldMembership);
        }
      }

      Set<Membership> deletes   = new LinkedHashSet(cur);     // deletes  = cur - should
      deletes.removeAll(should);

      Set<Membership> adds      = new LinkedHashSet(should);  // adds     = should - cur
      adds.removeAll(cur);
      Map modified  = new HashMap();
      modified      = mof.identifyGroupsAndStemsToMarkAsModified( modified, adds.iterator() );
      modified      = mof.identifyGroupsAndStemsToMarkAsModified( modified, deletes.iterator() );
      Set modGroups = new LinkedHashSet( ( (Map) modified.get("groups") ).values() );
      Set modStems  = new LinkedHashSet( ( (Map) modified.get("stems") ).values() );

      if ( adds.size() > 0 || deletes.size() > 0 || modGroups.size() > 0 || modStems.size() > 0 ) {
        GrouperDAOFactory.getFactory().getComposite().update(adds, deletes, modGroups, modStems);
        sw.stop();
        //EventLog.compositeUpdate(this, adds, deletes, sw);
        //_updateComposites( GrouperSession.staticGrouperSession(), deletes);
        //_updateComposites( GrouperSession.staticGrouperSession(), adds);
        Composite.internal_update(g);
      }
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.COMP_UPDATE + eGNF.getMessage();
      LOG.error(msg);
    }
    catch (IllegalStateException eIS)   {
      String msg = E.COMP_UPDATE + eIS.getMessage();
      LOG.error(msg);
    }
  }

  /**
   * @param other 
   * @return if one equals the other
   * @since   1.2.0
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
      .append( this.factorOwnerUUID, otherComposite.factorOwnerUUID )
      .append( this.leftFactorUUID, otherComposite.leftFactorUUID )
      .append( this.rightFactorUUID, otherComposite.rightFactorUUID )
      .append( this.type, otherComposite.type )
      .isEquals();
  }

  /**
   * @return create time
   * @since   1.2.0
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @return creator uuid
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @return factor owner uuid
   * @since   1.2.0
   */
  public String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
  }

  /**
   * @return left factor uuid
   * @since   1.2.0
   */
  public String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }

  /**
   * @return right factor uuid
   * @since   1.2.0
   */
  public String getRightFactorUuid() {
    return this.rightFactorUUID;
  }

  /**
   * @return uuid
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @return hashcode
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.factorOwnerUUID )
      .append( this.leftFactorUUID )
      .append( this.rightFactorUUID )
      .append( this.type )
      .toHashCode();
  } // public int hashCode()

  /**
   * @param createTime 
   * @since   1.2.0
   */
  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  /**
   * @param creatorUUID 
   * @since   1.2.0
   */
  public void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }

  /**
   * @param factorOwnerUUID 
   * @since   1.2.0
   */
  public void setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
  }

  /**
   * @param leftFactorUUID 
   * @since   1.2.0
   */
  public void setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
  }

  /**
   * @param rightFactorUUID 
   * @since   1.2.0
   */
  public void setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
  }

  /**
   * @param type 
   * @since   1.2.0
   */
  public void setTypeDb(String type) {
    this.type = type;
  }

  /**
   * @param uuid 
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * @return string
   * @since   1.2.0
   */
  public String toStringDto() {
    return new ToStringBuilder(this)
      .append( "createTime",      this.getCreateTime()        )
      .append( "creatorUuid",     this.getCreatorUuid()       )
      .append( "factorUuid",      this.getFactorOwnerUuid()   )
      .append( "leftFactorUuid",  this.getLeftFactorUuid()    )
      .append( "ownerUuid",       this.getUuid()              )
      .append( "rightFactorUuid", this.getRightFactorUuid()   )
      .append( "type",            this.getType()              )
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_COMMIT_DELETE, HooksCompositeBean.class, 
        this, Composite.class);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_DELETE, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    super.onPostSave(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_COMMIT_INSERT, HooksCompositeBean.class, 
        this, Composite.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_INSERT, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_POST_INSERT, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {
    super.onPostUpdate(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_COMMIT_UPDATE, HooksCompositeBean.class, 
        this, Composite.class);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_UPDATE, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_PRE_DELETE, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_PRE_DELETE, false, false);
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_PRE_INSERT, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_PRE_INSERT, false, false);
  
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_PRE_UPDATE, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_PRE_UPDATE, false, false);
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public Composite dbVersion() {
    return (Composite)this.dbVersion;
  }

  /**
   * note, these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public Composite clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

} 

