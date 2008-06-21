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

/**
 * Basic Hibernate <code>Group</code> and <code>GroupType</code> tuple DTO implementation.
 * @author  blair christensen.
 * @version $Id: GroupTypeTupleDTO.java,v 1.2 2008-06-21 04:16:12 mchyzer Exp $
 * @since   @HEAD@
 */
public class GroupTypeTupleDTO extends GrouperDefaultDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  groupUUID;
  private String  id;
  private String  typeUUID;


  // PUBLIC CLASS METHODS //

  /**
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupTypeTupleDTO)) {
      return false;
    }
    GroupTypeTupleDTO that = (GroupTypeTupleDTO) other;
    return new EqualsBuilder()
      .append( this.getGroupUuid(), that.getGroupUuid() )
      .append( this.getTypeUuid(),  that.getTypeUuid()  )
      .isEquals();
  }
  
  /**
   * @since   @HEAD@
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getGroupUuid() )
      .append( this.getTypeUuid()  )
      .toHashCode();
  }
  
  /**
   * @since   @HEAD@
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupUuid", this.getGroupUuid() )
      .append( "typeUuid",  this.getTypeUuid()  )
      .toString();
  }


  // PROTECTED CLASS METHODS //

  public String getGroupUuid() {
    return this.groupUUID;
  }
  public String getId() {
    return this.id;
  }
  public String getTypeUuid() {
    return this.typeUUID;
  }


  // SETTERS //

  public GroupTypeTupleDTO setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
    return this;
  }
  public GroupTypeTupleDTO setId(String id) {
    this.id = id;
    return this;
  }
  public GroupTypeTupleDTO setTypeUuid(String typeUUID) {
    this.typeUUID = typeUUID;
    return this;
  }

} 

