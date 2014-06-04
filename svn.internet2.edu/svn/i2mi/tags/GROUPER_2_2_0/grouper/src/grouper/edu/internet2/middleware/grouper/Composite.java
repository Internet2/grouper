/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.CompositeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportComposite;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;

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
 * @version $Id: Composite.java,v 1.71 2009-12-07 07:31:08 mchyzer Exp $
 * @since   1.0
 */
@SuppressWarnings("serial")
public class Composite extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, XmlImportable<Composite> {

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
    return CompositeType.valueOfIgnoreCase( this.type );
  }

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
   * used when calling getLeftGroup(), getRightGroup(), or getOwnerGroup()
   * @param uuid
   * @return group
   * @throws GroupNotFoundException
   */
  private Group _getGroup(String uuid) 
    throws  GroupNotFoundException {
    Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid, true) ;
    if (!GrouperSession.staticGrouperSession().getMember().canView(g)) {
      throw new GroupNotFoundException("Cant view group: " + g.getUuid());
    }
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
      Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid, true) ;
      return g.getName();
    }
    catch (GroupNotFoundException eGNF) {
      //CH 20090308 why does this just not throw the exception?
      LOG.error( msg + Quote.single( this.getUuid() ) + ": " + eGNF.getMessage() );
      return GrouperConfig.EMPTY_STRING;
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

    // fix composites
    Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().findAsFactorOrHasMemberOfFactor(this.getFactorOwnerUuid());
    Set<String> groupIds = new LinkedHashSet<String>();

    if (composites.size() > 0) {
      groupIds = Membership.fixComposites(composites, this.getFactorOwnerUuid(), membersDeletedOnPreDelete);
    }
    
    // update last_membership_change
    if (membersDeletedOnPreDelete.size() > 0) {
      groupIds.add(this.getFactorOwnerUuid());
    }
    
    Membership.updateLastMembershipChangeDuringMembersListUpdate(groupIds);
    
    membersDeletedOnPreDelete = null;
    
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
    
    // add the composite memberships
    Set<String> membersList = new LinkedHashSet<String>();
    if (this.getType().equals(CompositeType.COMPLEMENT)) {
      membersList.addAll(this.evaluateAddCompositeMembershipComplement());
    } else if (this.getType().equals(CompositeType.INTERSECTION)) {
      membersList.addAll(this.evaluateAddCompositeMembershipIntersection());
    } else if (this.getType().equals(CompositeType.UNION)) {
      membersList.addAll(this.evaluateAddCompositeMembershipUnion());
    } else {
      throw new IllegalStateException(E.MOF_CTYPE
          + this.getType().toString());
    }
    
    GrouperDAOFactory.getFactory().getMembership().save(this.createNewCompositeMembershipObjects(membersList));
    
    // fix composites
    Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().findAsFactorOrHasMemberOfFactor(this.getFactorOwnerUuid());
    Set<String> groupIds = new LinkedHashSet<String>();

    if (composites.size() > 0) {
      groupIds = Membership.fixComposites(composites, this.getFactorOwnerUuid(), membersList);
    }
    
    // update last_membership_change
    if (membersList.size() > 0) {
      groupIds.add(this.getFactorOwnerUuid());
    }
    
    Membership.updateLastMembershipChangeDuringMembersListUpdate(groupIds);

    // update group set object to specify type of composite
    GroupSet selfGroupSet = 
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(this.getFactorOwnerUuid(), Group.getDefaultList().getUuid());
    selfGroupSet.setType(MembershipType.COMPOSITE.getTypeString());
    GrouperDAOFactory.getFactory().getGroupSet().update(selfGroupSet);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_INSERT, HooksCompositeBean.class, 
        this, Composite.class, VetoTypeGrouper.COMPOSITE_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.COMPOSITE, 
        CompositeHooks.METHOD_COMPOSITE_POST_COMMIT_INSERT, HooksCompositeBean.class, 
        this, Composite.class);
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

  /** we're using this to save members deleted during a composite delete onPreDelete so it can be used again onPostDelete */
  private Set<String> membersDeletedOnPreDelete;

  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    // delete the composite memberships
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndFieldAndType( 
        this.getFactorOwnerUuid(), Group.getDefaultList(), "composite", false);

    membersDeletedOnPreDelete = new LinkedHashSet<String>();
    Iterator<Membership> iter = mships.iterator();
    while (iter.hasNext()) {
      membersDeletedOnPreDelete.add(iter.next().getMemberUuid());
    }
    
    GrouperDAOFactory.getFactory().getMembership().delete(mships);
    
    // update the membership type of the group set to 'immediate'
    GroupSet selfGroupSet = 
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(this.getFactorOwnerUuid(), Group.getDefaultList().getUuid());
    selfGroupSet.setType(MembershipType.IMMEDIATE.getTypeString());
    GrouperDAOFactory.getFactory().getGroupSet().update(selfGroupSet);
    
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

  /** context id of the transaction */
  private String contextId;

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }
  


  /**
   * @return the set of members
   */
  private Set<String> evaluateAddCompositeMembershipComplement() {

    Set<String> memberUUIDs = GrouperDAOFactory.getFactory().getMember()
      ._internal_membersComplement(this.getLeftFactorUuid(),
          this.getRightFactorUuid());

    return memberUUIDs;
  } 

  /**
   * @return the set of members
   */
  private Set<String> evaluateAddCompositeMembershipIntersection() {
    
    Set<String> memberUUIDs = GrouperDAOFactory.getFactory().getMember()
      ._internal_membersIntersection(this.getLeftFactorUuid(),
        this.getRightFactorUuid());

    return memberUUIDs;
  }

  /**
   * @return the set of members
   */
  private Set<String> evaluateAddCompositeMembershipUnion() {
    
    Set<String> memberUUIDs = GrouperDAOFactory.getFactory().getMember()
      ._internal_membersUnion(this.getLeftFactorUuid(),
      this.getRightFactorUuid());

    return memberUUIDs;
  }
  
  /**
   * @param memberUUIDs
   * @return set
   */
  private Set<Membership> createNewCompositeMembershipObjects(Set<String> memberUUIDs) {
    Set<Membership> mships = new LinkedHashSet<Membership>();
    Iterator<String> it = memberUUIDs.iterator();
    while (it.hasNext()) {
      Membership ms = internal_createNewCompositeMembershipObject(this.getFactorOwnerUuid(), it.next(), this.getUuid());
      mships.add(ms);
    }
    return mships;
  }


  /**
   * 
   * @param ownerGroupId
   * @param memberUuid
   * @param viaCompositeId
   * @return membership
   */
  public static Membership internal_createNewCompositeMembershipObject(String ownerGroupId, String memberUuid, String viaCompositeId) {
    Membership ms = new Membership();
    ms.setCreatorUuid(GrouperSession.staticGrouperSession().getMember().getUuid());
    ms.setDepth(0);
    ms.setFieldId(Group.getDefaultList().getUuid());
    ms.setMemberUuid(memberUuid);
    ms.setOwnerGroupId(ownerGroupId);
    ms.setType(MembershipType.COMPOSITE.getTypeString());
    ms.setViaCompositeId(viaCompositeId);

    return ms;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(Composite existingRecord) {
    existingRecord.setFactorOwnerUuid(this.factorOwnerUUID);
    existingRecord.setLeftFactorUuid(this.leftFactorUUID);
    existingRecord.setRightFactorUuid(this.rightFactorUUID);
    existingRecord.setTypeDb(this.type);
    existingRecord.setUuid(this.getUuid());
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(Composite other) {
    if (!StringUtils.equals(this.factorOwnerUUID, other.factorOwnerUUID)) {
      return true;
    }
    if (!StringUtils.equals(this.leftFactorUUID, other.leftFactorUUID)) {
      return true;
    }
    if (!StringUtils.equals(this.rightFactorUUID, other.rightFactorUUID)) {
      return true;
    }
    if (!StringUtils.equals(this.type, other.type)) {
      return true;
    }
    if (!StringUtils.equals(this.uuid, other.uuid)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(Composite other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (this.createTime != other.createTime) {
      return true;
    }
    if (!StringUtils.equals(this.creatorUUID, other.creatorUUID)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public Composite xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(this.uuid, this.factorOwnerUUID, this.leftFactorUUID, this.rightFactorUUID, this.type, false,
        new QueryOptions().secondLevelCache(false));
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Composite xmlSaveBusinessProperties(Composite existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      Group owner = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.factorOwnerUUID, true);
      Group left = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.leftFactorUUID, true);
      Group right = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.rightFactorUUID, true);
      existingRecord = owner.internal_addCompositeMember(GrouperSession.staticGrouperSession(), this.getType(), left, right, this.uuid);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    return existingRecord;

  }

  /**
   * store this object to the DB.
   */
  public void store() {    
    GrouperDAOFactory.getFactory().getComposite().update(this);
  }
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getComposite().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportComposite xmlToExportComposite(GrouperVersion grouperVersion) {
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportComposite xmlExportComposite = new XmlExportComposite();
    
    xmlExportComposite.setContextId(this.getContextId());
    xmlExportComposite.setCreateTime(GrouperUtil.dateStringValue(new Date(this.getCreateTime())));
    xmlExportComposite.setCreatorId(this.getCreatorUuid());
    xmlExportComposite.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportComposite.setLeftFactor(this.getLeftFactorUuid());
    xmlExportComposite.setOwner(this.getFactorOwnerUuid());
    xmlExportComposite.setRightFactor(this.getRightFactorUuid());
    xmlExportComposite.setType(this.getTypeDb());
    xmlExportComposite.setUuid(this.getUuid());
    
    return xmlExportComposite;
  }
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getUuid();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setUuid(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("Composite: " + this.getUuid() + ", ");

//    XmlExportUtils.toStringComposite(stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

} 

