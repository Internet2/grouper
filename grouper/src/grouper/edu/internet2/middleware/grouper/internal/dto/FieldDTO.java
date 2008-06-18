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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Privilege;

/** 
 * Basic <code>Field</code> DTO.
 * @author  blair christensen.
 * @version $Id: FieldDTO.java,v 1.5.6.1 2008-06-18 09:22:21 mchyzer Exp $    
 * @since   1.2.0
 */
public class FieldDTO extends GrouperDefaultDTO {

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

} 

