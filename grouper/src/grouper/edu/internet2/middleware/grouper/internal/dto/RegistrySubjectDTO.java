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

package edu.internet2.middleware.grouper.internal.dto;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** 
 * Basic <code>RegistrySubject</code> DTO.
 * @author  blair christensen.
 * @version $Id: RegistrySubjectDTO.java,v 1.4.6.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   1.2.0
 */
public class RegistrySubjectDTO extends GrouperDefaultDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  id;
  private String  name;
  private String  type;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof RegistrySubjectDTO)) {
      return false;
    }
    RegistrySubjectDTO that = (RegistrySubjectDTO) other;
    return new EqualsBuilder()
      .append( this.getName(), that.getName() )
      .append( this.getId(),   that.getId()   )
      .append( this.getType(), that.getType() )
      .isEquals();
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
  public String getType() {
    return this.type;
  }

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getName() )
      .append( this.getId()   )
      .append( this.getType() )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDTO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDTO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "name",        this.getName() )
      .append( "subjectId",   this.getId()   )
      .append( "subjectType", this.getType() )
      .toString();
  }

} 

