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
import  java.io.Serializable;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * {@link GroupType} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeDTO.java,v 1.1 2007-02-08 16:25:25 blair Exp $
 * @since   1.2.0
 */
class GroupTypeDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  creatorUUID;
  private long    createTime;
  private Set     fields;
  private boolean isAssignable  = true;
  private boolean isInternal    = false;
  private String  id;
  private String  name;
  private String  typeUUID;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupTypeDTO)) {
      return false;
    }
    GroupTypeDTO that = (GroupTypeDTO) other;
    return new EqualsBuilder()
      .append( this.getTypeUuid(), that.getTypeUuid() )
      .isEquals();
  } // public boolean equals(other)
  
  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getTypeUuid() )
      .toHashCode();
  } // public int hashCode()
  
  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "createTime",   this.getCreateTime()   )
      .append( "fields",       this.getFields()       )
      .append( "isAssignable", this.getIsAssignable() )
      .append( "isInternal",   this.getIsInternal()   )
      .append( "name",         this.getName()         )
      .append( "typeUuid",     this.getTypeUuid()     )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // TODO 20070124 this doesn't fit with everything else
  protected static GroupTypeDTO getDTO(HibernateGroupTypeDAO dao) {
    GroupTypeDTO dto = new GroupTypeDTO();
    dto.setCreateTime( dao.getCreateTime() );
    dto.setCreatorUuid( dao.getCreatorUuid() );
    dto.setFields( dao.getFields() );
    dto.setId( dao.getId() );
    dto.setIsAssignable( dao.getIsAssignable() );
    dto.setIsInternal( dao.getIsInternal() );
    dto.setName( dao.getName() );
    dto.setTypeUuid( dao.getTypeUuid() );
    return dto;
  } // protected static GroupTypeDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateGroupTypeDAO getDAO() {
    HibernateGroupTypeDAO dao = new HibernateGroupTypeDAO();
    dao.setCreateTime( this.getCreateTime() );
    dao.setCreatorUuid( this.getCreatorUuid() );
    dao.setFields( this.getFields() );
    dao.setId( this.getId() );
    dao.setIsAssignable( this.getIsAssignable() );
    dao.setIsInternal( this.getIsInternal() );
    dao.setName( this.getName() );
    dao.setTypeUuid( this.getTypeUuid() );
    return dao;
  } // protected HibernateGroupTypeDAO getDAO()


  // GETTERS //

  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected long getCreateTime() {
    return this.createTime;
  }
  protected Set getFields() {
    return this.fields;
  }
  protected boolean getIsAssignable() {
    return this.isAssignable;
  }
  protected boolean getIsInternal() {
    return this.isInternal;
  }
  protected String getId() {
    return this.id;
  }
  protected String getName() {
    return this.name;
  }
  protected String getTypeUuid() {
    return this.typeUUID;
  }


  // SETTERS //

  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setFields(Set fields) {
    this.fields = fields;
  }
  protected void setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
  }
  protected void setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setTypeUuid(String typeUUID) {
    this.typeUUID = typeUUID;
  }

} // class GroupTypeDTO extends BaseGrouperDTO

