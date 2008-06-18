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
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** 
 * Hibernate representation of the JDBC <code>SubjectAttribute</code> table.
 * @author  blair christensen.
 * @version $Id: RegistrySubjectAttributeDTO.java,v 1.1.2.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   @HEAD@
 */
public class RegistrySubjectAttributeDTO implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = -4979920855853791786L;


  // HIBERNATE PROPERTIES //
  private String name;
  private String searchValue;
  private String subjectId;
  private String value;


  // CONSTRUCTORS //

  /**
   * For Hibernate.
   * @since   @HEAD@
   */
  public RegistrySubjectAttributeDTO() {
    super();
  }

  // @since   @HEAD@
  protected RegistrySubjectAttributeDTO(
    String id, String name, String value, String searchVal
  )
  {
    this.setName(         name      );
    this.setSearchValue(  searchVal );
    this.setSubjectId(    id        );
    this.setValue(        value     );
  } 


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof RegistrySubjectAttributeDTO) ) return false;
    RegistrySubjectAttributeDTO castOther = (RegistrySubjectAttributeDTO) other;
    return new EqualsBuilder()
      .append(this.getSubjectId() , castOther.getSubjectId()  )
      .append(this.getName()      , castOther.getName()       )
      .append(this.getValue()     , castOther.getValue()      )
      .isEquals();
  } 

  /**
   * @since   @HEAD@
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getSubjectId())
      .append(getName()     )
      .append(getValue()    )
      .toHashCode();
  }

  
  // PRIVATE INSTANCE METHODS //
  
  // @since   @HEAD@
  private String getName() {
    return this.name;
  }
  // @since   @HEAD@
  private String getSearchValue() {
    return this.searchValue;
  }
  // @since   @HEAD@
  private String getSubjectId() {
    return this.subjectId;
  }
  // @since   @HEAD@
  private String getValue() {
    return this.value;
  }
  // @since   @HEAD@
  private RegistrySubjectAttributeDTO setName(String name) {
    this.name = name;
    return this;
  }
  // @since   @HEAD@
  private RegistrySubjectAttributeDTO setSearchValue(String value) {
    this.searchValue = value;
    return this;
  }
  // @since   @HEAD@
  private RegistrySubjectAttributeDTO setSubjectId(String subjectId) {
    this.subjectId = subjectId;
    return this;
  }
  // @since   @HEAD@
  private RegistrySubjectAttributeDTO setValue(String value) {
    this.value = value;
    return this;
  }

}

