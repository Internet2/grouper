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

package edu.internet2.middleware.grouper;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link RegistrySubject} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistrySubjectDTO.java,v 1.1 2007-03-06 17:02:43 blair Exp $
 */
class RegistrySubjectDTO extends BaseGrouperDTO {

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
  } // public boolean equals(other)

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
  public String toString() {
    return new ToStringBuilder(this)
      .append( "name",        this.getName() )
      .append( "subjectId",   this.getId()   )
      .append( "subjectType", this.getType() )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateRegistrySubjectDAO getDAO() {
    HibernateRegistrySubjectDAO dao = new HibernateRegistrySubjectDAO();
    dao.setName( this.getName() );
    dao.setId( this.getId() );
    dao.setType( this.getType() );
    return dao;
  } // protected HibernateRegistrySubjectDAO getDAO()


  // GETTERS //

  protected String getId() {
    return this.id;
  }
  protected String getName() {
    return this.name;
  }
  protected String getType() {
    return this.type;
  }
    

  // SETTERS //

  protected void setId(String id) {
    this.id = id;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setType(String type) {
    this.type = type;
  }

} // class RegistrySubjectDTO extends BaseGrouperDTO

