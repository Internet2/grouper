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
 * @version $Id: GroupDTO.java,v 1.2 2007-02-14 17:06:28 blair Exp $
 */
class GroupDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private Map     attributes;
  private String  createSource;
  private long    createTime;
  private String  creatorUUID;
  private String  id;
  private String  modifierUUID;
  private String  modifySource;
  private long    modifyTime;
  private String  parentUUID;
  private Set     types;
  private String  uuid;


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
    GroupDTO that = (GroupDTO) other;
    // TODO 20070201 should i use more than this?
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
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
  // TODO 20070125 this doesn't fit with everything else
  protected static Collection getDTO(Collection c) {
    Set       groups  = new LinkedHashSet();
    Iterator  it      = c.iterator();
    while ( it.hasNext() ) {
      groups.add( getDTO( (HibernateGroupDAO) it.next() ) );
    }
    return groups;
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  // TODO 20070125 this doesn't fit with everything else
  protected static GroupDTO getDTO(HibernateGroupDAO dao) {
    GroupDTO dto = new GroupDTO();
    dto.setAttributes( dao.getAttributes() );
    dto.setCreateSource( dao.getCreateSource() );
    dto.setCreateTime( dao.getCreateTime() );
    dto.setCreatorUuid( dao.getCreatorUuid() );
    dto.setId( dao.getId() );
    dto.setModifierUuid( dao.getModifierUuid() );
    dto.setModifySource( dao.getModifySource() );
    dto.setModifyTime( dao.getModifyTime() );
    dto.setUuid( dao.getUuid() );
    dto.setParentUuid( dao.getParentUuid() );
    dto.setTypes( dao.getTypes() );
    return dto;
  } // protected static GroupDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //
  
  // @since   1.2.0
  // TODO 20070125 is this the direction i want to take?
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



  // GETTERS //

  protected Map getAttributes() {
    return this.attributes;
  }
  protected String getCreateSource() {
    return this.createSource;
  }
  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected String getModifierUuid() {
    return this.modifierUUID;
  }
  protected String getModifySource() {
    return this.modifySource;
  }
  protected long getModifyTime() {
    return this.modifyTime;
  }
  protected String getParentUuid() {
    return this.parentUUID;
  }
  protected Set getTypes() {
    return this.types;
  }
  protected String getUuid() {
    return this.uuid;
  }


  // SETTERS //

  protected void setAttributes(Map attributes) {
    this.attributes = attributes;
  }
  protected void setCreateSource(String createSource) {
    this.createSource = createSource;
  }
  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
  }
  protected void setModifySource(String modifySource) {
    this.modifySource = modifySource;
  }
  protected void setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
  }
  protected void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  }
  protected void setTypes(Set types) {
    this.types = types;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

} // class GroupDTO extends BaseGrouperDTO

