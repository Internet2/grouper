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
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperDAOFactory;

/** 
 * Basic <code>GroupType</code> DTO.
 * @author  blair christensen.
 * @version $Id: GroupTypeDTO.java,v 1.4.6.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   1.2.0
 */
public class GroupTypeDTO extends GrouperDefaultDTO {

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
 

  // PROTECTED INSTANCE METHODS //

  /**
   * @since   1.2.0
   */ 
  public Set<FieldDTO> getFields() {
    if (this.fields == null) {
      this.fields = GrouperDAOFactory.getFactory().getField().findAllFieldsByGroupType( this.getUuid() );
    }
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

} 

