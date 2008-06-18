/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.dto;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.HookVeto;
import edu.internet2.middleware.grouper.hooks.VetoTypeGrouper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Basic <code>Group</code> DTO.
 * @author  blair christensen.
 * @version $Id: GroupDTO.java,v 1.5.6.1 2008-06-18 09:22:21 mchyzer Exp $
 */
public class GroupDTO extends GrouperDefaultDTO {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributes */
  public static final String FIELD_ATTRIBUTES = "attributes";

  /** constant for field name for: createSource */
  public static final String FIELD_CREATE_SOURCE = "createSource";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: modifierUUID */
  public static final String FIELD_MODIFIER_UUID = "modifierUUID";

  /** constant for field name for: modifySource */
  public static final String FIELD_MODIFY_SOURCE = "modifySource";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: parentUUID */
  public static final String FIELD_PARENT_UUID = "parentUUID";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//


  /** save the state when retrieving from DB */
  private GroupDTO dbVersion = null;

  
  // TODO 20070531 review lazy-loading to improve consistency + performance

  // PRIVATE INSTANCE VARIABLES //
  private Map       attributes;
  private String    createSource;
  private long      createTime      = 0; // default to the epoch
  private String    creatorUUID;
  private String    id;
  private String    modifierUUID;
  private String    modifySource;
  private long      modifyTime      = 0; // default to the epoch
  private String    parentUUID;
  private Set       types;
  private String    uuid;

  /** constant for prefix of field diffs for attributes: attribute__ */
  public static final String ATTRIBUTE_PREFIX = "attribute__";

  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupDTO)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.getUuid(), ( (GroupDTO) other ).getUuid() )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public Map getAttributes() {
    if (this.attributes == null) {
      this.attributes = GrouperDAOFactory.getFactory().getGroup().findAllAttributesByGroup( this.getUuid() );
    }
    return this.attributes;
  }

  /**
   * @since   1.2.0
   */
  public String getCreateSource() {
    return this.createSource;
  }

  /**
   * @since   1.2.0
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getModifySource() {
    return this.modifySource;
  }

  /**
   * @since   1.2.0
   */
  public long getModifyTime() {
    return this.modifyTime;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
  }

  /**
   * @since   1.2.0
   */
  public Set getTypes() {
    if (this.types == null) {
      this.types = GrouperDAOFactory.getFactory().getGroup()._findAllTypesByGroup( this.getUuid() );
    }
    return this.types;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public GroupDTO setAttributes(Map attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;  
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setTypes(Set types) {
    this.types = types;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "attributes",   this.getAttributes()   )
      .append( "createSource", this.getCreateSource() )
      .append( "createTime",   this.getCreateTime()   )
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "modifierUuid", this.getModifierUuid() )
      .append( "modifySource", this.getModifySource() )
      .append( "modifyTime",   this.getModifyTime()   )
      .append( "ownerUuid",    this.getUuid()         )
      .append( "parentUuid",   this.getParentUuid()   )
      .append( "types",        this.getTypes()        )
      .toString();
  } // public String toString()


  // PUBLIC CLASS METHODS //


  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO#dbVersionDifferent()
   */
  @Override
  boolean dbVersionDifferent() {
    Set<String> differentFields = dbVersionDifferentFields();
    return differentFields.size() > 0;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dto.GrouperDefaultDTO#dbVersionDifferentFields()
   */
  @Override
  Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        GrouperUtil.toSet(FIELD_ATTRIBUTES, FIELD_CREATE_SOURCE, FIELD_CREATE_TIME, 
            FIELD_CREATOR_UUID, FIELD_ID, FIELD_MODIFIER_UUID, FIELD_MODIFY_SOURCE,
            FIELD_MODIFY_TIME, FIELD_PARENT_UUID, FIELD_UUID), ATTRIBUTE_PREFIX);
    return result;
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = new GroupDTO();
    this.dbVersion.attributes = this.attributes == null ? null : new HashMap<String,String>(this.attributes);
    this.dbVersion.createSource = this.createSource;
    this.dbVersion.createTime = this.createTime;
    this.dbVersion.creatorUUID = this.creatorUUID;
    this.dbVersion.id = this.id;
    this.dbVersion.modifierUUID = this.modifierUUID;
    this.dbVersion.modifySource = this.modifySource;
    this.dbVersion.modifyTime = this.modifyTime;
    this.dbVersion.parentUUID = this.parentUUID;
    this.dbVersion.uuid = this.uuid;
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  public GroupDTO getDbVersion() {
    return this.dbVersion;
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    GrouperDAOFactory.getFactory().getGroup()._updateAttributes(hibernateSession, true, this);
    super.onPostUpdate(hibernateSession);

  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    GrouperDAOFactory.getFactory().getGroup()._updateAttributes(hibernateSession, false, this);
    super.onPostSave(hibernateSession);
  }

  @Override
  public boolean onDelete(Session hs) 
    throws  CallbackException {
    GrouperDAOFactory.getFactory().getGroup().putInExistsCache( this.getUuid(), false );
    return Lifecycle.NO_VETO;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dto.GrouperDefaultDTO#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    //see if there is a hook class
    GroupHooks groupHooks = (GroupHooks)GrouperHookType.GROUP.hooksInstance();
    
    if (groupHooks != null) {
      HooksGroupPreInsertBean hooksGroupPreInsertBean = new HooksGroupPreInsertBean(new HooksContext(), this);
      try {
        groupHooks.groupPreInsert(hooksGroupPreInsertBean);
      } catch (HookVeto hv) {
        hv.assignVetoType(VetoTypeGrouper.GROUP_PRE_INSERT, false);
        throw hv;
      }
    }
    
  }

  // @since   @HEAD@
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    GrouperDAOFactory.getFactory().getGroup().putInExistsCache( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  }


} 

