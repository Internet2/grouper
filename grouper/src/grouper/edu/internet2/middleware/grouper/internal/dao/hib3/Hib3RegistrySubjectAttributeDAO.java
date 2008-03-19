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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** 
 * Hibernate representation of the JDBC <code>SubjectAttribute</code> table.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3RegistrySubjectAttributeDAO.java,v 1.1.4.1 2008-03-19 18:46:10 mchyzer Exp $
 * @since   @HEAD@
 */
class Hib3RegistrySubjectAttributeDAO implements Serializable {

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
  public Hib3RegistrySubjectAttributeDAO() {
    super();
  }

  // @since   @HEAD@
  protected Hib3RegistrySubjectAttributeDAO(
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
    if ( !(other instanceof Hib3RegistrySubjectAttributeDAO) ) return false;
    Hib3RegistrySubjectAttributeDAO castOther = (Hib3RegistrySubjectAttributeDAO) other;
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
  private Hib3RegistrySubjectAttributeDAO setName(String name) {
    this.name = name;
    return this;
  }
  // @since   @HEAD@
  private Hib3RegistrySubjectAttributeDAO setSearchValue(String value) {
    this.searchValue = value;
    return this;
  }
  // @since   @HEAD@
  private Hib3RegistrySubjectAttributeDAO setSubjectId(String subjectId) {
    this.subjectId = subjectId;
    return this;
  }
  // @since   @HEAD@
  private Hib3RegistrySubjectAttributeDAO setValue(String value) {
    this.value = value;
    return this;
  }

}

