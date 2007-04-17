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

package edu.internet2.middleware.grouper.internal.dto;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import  edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link GroupType} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeDTO.java,v 1.1 2007-04-17 14:17:29 blair Exp $
 * @since   1.2.0
 */
public class GroupTypeDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  creatorUUID;
  private long    createTime;
  private Set     fields;
  private boolean isAssignable  = true;
  private boolean isInternal    = false;
  private String  id;
  private String  name;
  private String  uuid;


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
      .append( this.getUuid(), that.getUuid() )
      .isEquals();
  } // public boolean equals(other)
 
  /**
   * @since   1.2.0
   */ 
  public String getCreatorUuid() {
    return this.creatorUUID;
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
  public Set getFields() {
    return this.fields;
  }
 
  /**
   * @since   1.2.0
   */ 
  public boolean getIsAssignable() {
    return this.isAssignable;
  }
 
  /**
   * @since   1.2.0
   */ 
  public boolean getIsInternal() {
    return this.isInternal;
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
  public String getName() {
    return this.name;
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
  public GroupTypeDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setFields(Set fields) {
    this.fields = fields;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setId(String id) {
    this.id = id;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setName(String name) {
    this.name = name;
    return this;
  }
  
  /**
   * @since   1.2.0
   */ 
  public GroupTypeDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

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
      .append( "uuid",         this.getUuid()         )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // FIXME 20070416
  public static GroupTypeDTO getDTO(GroupTypeDAO dao) {
    return new GroupTypeDTO()
      .setCreateTime( dao.getCreateTime() )
      .setCreatorUuid( dao.getCreatorUuid() )
      .setFields( dao.getFields() )
      .setId( dao.getId() )
      .setIsAssignable( dao.getIsAssignable() )
      .setIsInternal( dao.getIsInternal() )
      .setName( dao.getName() )
      .setUuid( dao.getUuid() )
      ;
  } 


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  // FIXME 20070416
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getGroupType()
      .setCreateTime( this.getCreateTime() )
      .setCreatorUuid( this.getCreatorUuid() )
      .setFields( this.getFields() )
      .setId( this.getId() )
      .setIsAssignable( this.getIsAssignable() )
      .setIsInternal( this.getIsInternal() )
      .setName( this.getName() )
      .setUuid( this.getUuid() )
      ;
  }

} 

