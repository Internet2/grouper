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
import  edu.internet2.middleware.grouper.Field;
import  edu.internet2.middleware.grouper.FieldType;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import  edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import  org.apache.commons.lang.builder.*;

/** 
 * Basic {@link Field} DTO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldDTO.java,v 1.2 2007-04-18 14:31:59 blair Exp $    
 * @since   1.2.0
 */
public class FieldDTO implements GrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String    groupTypeUUID;
  private String    id;
  private boolean   isNullable;
  private String    name;
  private String    readPrivilege;
  private String    type;
  private String    uuid;
  private String    writePrivilege;

    
  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if ( !(other instanceof FieldDTO) ) {
      return false;
    }
    FieldDTO that = (FieldDTO) other;
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
     .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public String getGroupTypeUuid() {
    return this.groupTypeUUID;
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
  public boolean getIsNullable() {
    return this.isNullable;
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
  public String getReadPrivilege() {
    return this.readPrivilege;
  }

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
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
  public String getWritePrivilege() {
    return this.writePrivilege;
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
  public FieldDTO setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUUID = groupTypeUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setReadPrivilege(Privilege readPrivilege) {
    this.setReadPrivilege( readPrivilege.getName() );
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setType(FieldType type) {
    this.setType( type.toString() );
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setWritePrivilege(Privilege writePrivilege) {
    this.setWritePrivilege( writePrivilege.getName() );
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDTO setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupTypeUuid",  this.getGroupTypeUuid()  )
      .append( "isNullable",     this.getIsNullable()     )
      .append( "name",           this.getName()           )
      .append( "readPrivilege",  this.getReadPrivilege()  )
      .append( "type",           this.getType()           )
      .append( "uuid",           this.getUuid()           )
      .append( "writePrivilege", this.getWritePrivilege() )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // FIXME 20070416 access
  public static FieldDTO getDTO(FieldDAO dao) {
    return new FieldDTO()
      .setGroupTypeUuid( dao.getGroupTypeUuid() )
      .setId( dao.getId() )
      .setIsNullable( dao.getIsNullable() )
      .setName( dao.getName() )
      .setReadPrivilege( dao.getReadPrivilege() )
      .setType( dao.getType() )
      .setUuid( dao.getUuid() )
      .setWritePrivilege( dao.getWritePrivilege() )
      ;
  } 


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  // FIXME 20070416 access
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getField()
      .setGroupTypeUuid( this.getGroupTypeUuid() )
      .setId( this.getId() )
      .setIsNullable( this.getIsNullable() )
      .setName( this.getName() )
      .setReadPrivilege( this.getReadPrivilege() )
      .setType( this.getType() )
      .setUuid( this.getUuid() )
      .setWritePrivilege( this.getWritePrivilege() )
      ;
  }

} 

