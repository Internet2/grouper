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
 * Basic Hibernate <code>Attribute</code> DTO interface.
 * @author  blair christensen.
 * @version $Id: AttributeDTO.java,v 1.1.2.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   @HEAD@
 */
public class AttributeDTO extends GrouperDefaultDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  attrName;
  private String  groupUUID;
  private String  id;
  private String  value;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AttributeDTO)) {
      return false;
    }
    AttributeDTO that = (AttributeDTO) other;
    return new EqualsBuilder()
      .append( this.getAttrName(),  that.getAttrName()  )
      .append( this.getGroupUuid(), that.getGroupUuid() )
      .append( this.getValue(),     that.getValue()     )
      .isEquals();
  } // public boolean equals(other)
  
  /**
   * @since   @HEAD@
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getAttrName()  )
      .append( this.getGroupUuid() )
      .append( this.getValue()     )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   @HEAD@
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "attrName",  this.getAttrName()  )
      .append( "groupUuid", this.getGroupUuid() )
      .append( "id",        this.getId()        )
      .append( "value",     this.getValue()     )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   @HEAD@
  public String getAttrName() {
    return this.attrName;
  }
  // @since   @HEAD@
  public String getGroupUuid() {
    return this.groupUUID;
  }
  // @since   @HEAD@
  public String getId() {
    return this.id;
  }
  // @since   @HEAD@
  public String getValue() {
    return this.value;
  }
  // @since   @HEAD@
  public AttributeDTO setAttrName(String attrName) {
    this.attrName = attrName;
    return this;
  }
  // @since   @HEAD@
  public AttributeDTO setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
    return this;
  }
  // @since   @HEAD@
  public AttributeDTO setId(String id) {
    this.id = id;
    return this;
  }
  // @since   @HEAD@
  public AttributeDTO setValue(String value) {
    this.value = value;
    return this;
  }

} 

