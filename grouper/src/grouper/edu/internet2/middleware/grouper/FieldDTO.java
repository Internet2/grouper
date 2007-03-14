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
import  org.apache.commons.lang.builder.*;

/** 
 * Basic {@link Field} DTO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldDTO.java,v 1.2 2007-03-14 19:31:47 blair Exp $    
 * @since   1.2.0
 */
class FieldDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String    fieldUUID;
  private String    groupTypeUUID;
  private String    id;
  private boolean   isNullable;
  private String    name;
  private String    readPrivilege;
  private String    type;
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
      .append( this.getFieldUuid(), that.getFieldUuid() )
     .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getFieldUuid() )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "fieldUuid",      this.getFieldUuid()      )
      .append( "groupTypeUuid",  this.getGroupTypeUuid()  )
      .append( "isNullable",     this.getIsNullable()     )
      .append( "name",           this.getName()           )
      .append( "readPrivilege",  this.getReadPrivilege()  )
      .append( "type",           this.getType()           )
      .append( "writePrivilege", this.getWritePrivilege() )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static FieldDTO getDTO(HibernateFieldDAO dao) {
    FieldDTO dto = new FieldDTO();
    dto.setFieldUuid( dao.getFieldUuid() );
    dto.setGroupTypeUuid( dao.getGroupTypeUuid() );
    dto.setId( dao.getId() );
    dto.setIsNullable( dao.getIsNullable() );
    dto.setName( dao.getName() );
    dto.setReadPrivilege( dao.getReadPrivilege() );
    dto.setType( dao.getType() );
    dto.setWritePrivilege( dao.getWritePrivilege() );
    return dto;
  } // protected static GroupTypeDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateFieldDAO getDAO() {
    HibernateFieldDAO dao = new HibernateFieldDAO();
    dao.setFieldUuid( this.getFieldUuid() );
    dao.setGroupTypeUuid( this.getGroupTypeUuid() );
    dao.setId( this.getId() );
    dao.setIsNullable( this.getIsNullable() );
    dao.setName( this.getName() );
    dao.setReadPrivilege( this.getReadPrivilege() );
    dao.setType( this.getType() );
    dao.setWritePrivilege( this.getWritePrivilege() );
    return dao;
  } // protected HibernateFieldDAO getDAO()



  // GETTERS //

  protected String getFieldUuid() {
    return this.fieldUUID;
  }
  protected String getGroupTypeUuid() {
    return this.groupTypeUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected boolean getIsNullable() {
    return this.isNullable;
  }
  protected String getName() {
    return this.name;
  }
  protected String getReadPrivilege() {
    return this.readPrivilege;
  }
  protected String getType() {
    return this.type;
  }
  protected String getWritePrivilege() {
    return this.writePrivilege;
  }


  // SETTERS //

  protected void setFieldUuid(String fieldUUID) {
    this.fieldUUID = fieldUUID;
  }
  protected void setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUUID = groupTypeUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setReadPrivilege(Privilege readPrivilege) {
    this.setReadPrivilege( readPrivilege.getName() );
  }
  protected void setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
  }
  protected void setType(FieldType type) {
    this.setType( type.toString() );
  }
  protected void setType(String type) {
    this.type = type;
  }
  protected void setWritePrivilege(Privilege writePrivilege) {
    this.setWritePrivilege( writePrivilege.getName() );
  }
  protected void setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
  }

} // class FieldDTO extends BaseGrouperDTO

