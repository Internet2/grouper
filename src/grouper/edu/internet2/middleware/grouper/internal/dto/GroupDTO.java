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
import  edu.internet2.middleware.grouper.GrouperConfig;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import  java.util.Map;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * Basic <code>Group</code> DTO.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: GroupDTO.java,v 1.5 2007-05-31 18:52:26 blair Exp $
 */
public class GroupDTO implements GrouperDTO {

  // TODO 20070531 review lazy-loading to improve consistency + performance

  // PRIVATE INSTANCE VARIABLES //
  private Map       attributes;
  private String    createSource;
  private long      createTime      = 0; // default to the epoch
  private String    creatorUUID;
  private GroupDAO  dao;
  private String    id;
  private String    modifierUUID;
  private String    modifySource;
  private long      modifyTime      = 0; // default to the epoch
  private String    parentUUID;
  private Set       types;
  private String    uuid;

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
    if (this.attributes == null && this.dao != null) {
      this.attributes = this.dao.getAttributes();
    }
    return this.attributes;
  }

  /**
   * @since   1.2.0
   */
  public String getCreateSource() {
    if (this.createSource == null && this.dao != null) {
      this.createSource = this.dao.getCreateSource();
    }
    return this.createSource;
  }

  /**
   * @since   1.2.0
   */
  public long getCreateTime() {
    if (this.createTime == GrouperConfig.EPOCH && this.dao != null) {
      this.createTime = this.dao.getCreateTime();
    }
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    if (this.creatorUUID == null && this.dao != null) {
      this.creatorUUID = this.dao.getCreatorUuid();
    }
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getGroup()
      .setAttributes( this.getAttributes() )
      .setCreateSource( this.getCreateSource() )
      .setCreateTime( this.getCreateTime() )
      .setCreatorUuid( this.getCreatorUuid() )
      .setId( this.getId() )
      .setModifierUuid( this.getModifierUuid() )
      .setModifySource( this.getModifySource() )
      .setModifyTime( this.getModifyTime() )
      .setUuid( this.getUuid() )
      .setParentUuid( this.getParentUuid() )
      .setTypes( this.getTypes() )
      ;
  }
  
  /**
   * @since   1.2.0
   */
  public String getId() {
    if (this.id == null && this.dao != null) {
      this.id = this.dao.getId();
    }
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getModifierUuid() {
    if (this.modifierUUID == null && this.dao != null) {
      this.modifierUUID = this.dao.getModifierUuid();
    }
    return this.modifierUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getModifySource() {
    if (this.modifySource == null && this.dao != null) {
      this.modifySource = this.dao.getModifySource();
    }
    return this.modifySource;
  }

  /**
   * @since   1.2.0
   */
  public long getModifyTime() {
    if (this.modifyTime == GrouperConfig.EPOCH && this.dao != null) {
      this.modifyTime = this.dao.getModifyTime();
    }
    return this.modifyTime;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    if (this.parentUUID == null && this.dao != null) {
      this.parentUUID = this.dao.getParentUuid();
    }
    return this.parentUUID;
  }

  /**
   * @since   1.2.0
   */
  public Set getTypes() {
    if (this.types == null && this.dao != null) {
      this.types = this.dao.getTypes();
    }
    return this.types;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    if (this.uuid == null && this.dao != null) {
      this.uuid = this.dao.getUuid();
    }
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
   * @since   1.2.0
   */
  public static GroupDTO getDTO(GroupDAO dao) {
    GroupDTO dto = new GroupDTO();
    dto._setDAO(dao);
    return dto;
  }


  // PRIVATE INSTANCE METHODS //
  
  // @since   1.2.0
  private void _setDAO(GroupDAO dao) {
    this.dao = dao;
  } // private void _setDAO(dao)

} 

