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

package edu.internet2.middleware.grouper;
import  java.util.Collection;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Group} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupDTO.java,v 1.8 2007-03-28 17:00:06 blair Exp $
 */
class GroupDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private Map               attributes;
  private String            createSource;
  private long              createTime      = 0; // default to the epoch
  private String            creatorUUID;
  private HibernateGroupDAO dao;
  private String            id;
  private String            modifierUUID;
  private String            modifySource;
  private long              modifyTime      = 0; // default to the epoch
  private String            parentUUID;
  private Set               types;
  private String            uuid;

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
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()

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


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Collection getDTO(Collection c) {
    Set       groups  = new LinkedHashSet();
    Iterator  it      = c.iterator();
    while ( it.hasNext() ) {
      groups.add( getDTO( (HibernateGroupDAO) it.next() ) );
    }
    return groups;
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  protected static GroupDTO getDTO(HibernateGroupDAO dao) {
    GroupDTO dto = new GroupDTO();
    dto._setDAO(dao);
    return dto;
  } // protected static GroupDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //
  
  // @since   1.2.0
  protected HibernateGroupDAO getDAO() {
    HibernateGroupDAO dao = new HibernateGroupDAO();
    dao.setAttributes( this.getAttributes() );
    dao.setCreateSource( this.getCreateSource() );
    dao.setCreateTime( this.getCreateTime() );
    dao.setCreatorUuid( this.getCreatorUuid() );
    dao.setId( this.getId() );
    dao.setModifierUuid( this.getModifierUuid() );
    dao.setModifySource( this.getModifySource() );
    dao.setModifyTime( this.getModifyTime() );
    dao.setUuid( this.getUuid() );
    dao.setParentUuid( this.getParentUuid() );
    dao.setTypes( this.getTypes() );
    return dao;
  } // protected HibernateGroupDAO getDAO()


  // PRIVATE INSTANCE METHODS //

  // TODO 20070215 these methods should be renamed and moved to `GrouperDTO`
  
  // @since   1.2.0
  private void _setDAO(HibernateGroupDAO dao) {
    this.dao = dao;
  } // private void _setDAO(dao)


  // GETTERS //

  // TODO 20070215 smarter, more DRY, lazy-loading, please

  protected Map getAttributes() {
    if (this.attributes == null && this.dao != null) {
      this.attributes = this.dao.getAttributes();
    }
    return this.attributes;
  }
  protected String getCreateSource() {
    if (this.createSource == null && this.dao != null) {
      this.createSource = this.dao.getCreateSource();
    }
    return this.createSource;
  }
  protected long getCreateTime() {
    if (this.createTime == GrouperConfig.EPOCH && this.dao != null) {
      this.createTime = this.dao.getCreateTime();
    }
    return this.createTime;
  }
  protected String getCreatorUuid() {
    if (this.creatorUUID == null && this.dao != null) {
      this.creatorUUID = this.dao.getCreatorUuid();
    }
    return this.creatorUUID;
  }
  protected String getId() {
    if (this.id == null && this.dao != null) {
      this.id = this.dao.getId();
    }
    return this.id;
  }
  protected String getModifierUuid() {
    if (this.modifierUUID == null && this.dao != null) {
      this.modifierUUID = this.dao.getModifierUuid();
    }
    return this.modifierUUID;
  }
  protected String getModifySource() {
    if (this.modifySource == null && this.dao != null) {
      this.modifySource = this.dao.getModifySource();
    }
    return this.modifySource;
  }
  protected long getModifyTime() {
    if (this.modifyTime == GrouperConfig.EPOCH && this.dao != null) {
      this.modifyTime = this.dao.getModifyTime();
    }
    return this.modifyTime;
  }
  protected String getParentUuid() {
    if (this.parentUUID == null && this.dao != null) {
      this.parentUUID = this.dao.getParentUuid();
    }
    return this.parentUUID;
  }
  protected Set getTypes() {
    if (this.types == null && this.dao != null) {
      this.types = this.dao.getTypes();
    }
    return this.types;
  }
  protected String getUuid() {
    if (this.uuid == null && this.dao != null) {
      this.uuid = this.dao.getUuid();
    }
    return this.uuid;
  }


  // SETTERS //

  protected GroupDTO setAttributes(Map attributes) {
    this.attributes = attributes;
    return this;
  }
  protected GroupDTO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }
  protected GroupDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  protected GroupDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;  
  }
  protected GroupDTO setId(String id) {
    this.id = id;
    return this;
  }
  protected GroupDTO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }
  protected GroupDTO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }
  protected GroupDTO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }
  protected GroupDTO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }
  protected GroupDTO setTypes(Set types) {
    this.types = types;
    return this;
  }
  protected GroupDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

} // class GroupDTO extends BaseGrouperDTO

