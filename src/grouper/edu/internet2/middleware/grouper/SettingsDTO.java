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
 * Basic {@link Settings} DTO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SettingsDTO.java,v 1.1 2007-02-08 16:25:25 blair Exp $
 * @since   1.2.0
 */
class SettingsDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  id;
  private int     schemaVersion;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if ( !(other instanceof SettingsDTO) ) {
      return false;
    }
    SettingsDTO that = (SettingsDTO) other;
    return new EqualsBuilder()
      .append( this.getSchemaVersion(), that.getSchemaVersion() )
     .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getSchemaVersion() )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "schemaVersion", this.getSchemaVersion() )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // TODO 20070207 this doesn't fit with everything else
  protected static SettingsDTO getDTO(HibernateSettingsDAO dao) {
    SettingsDTO dto = new SettingsDTO();
    dto.setId( dao.getId() );
    dto.setSchemaVersion( dao.getSchemaVersion() );
    return dto;
  } // protected static GroupTypeDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateSettingsDAO getDAO() {
    HibernateSettingsDAO dao = new HibernateSettingsDAO();
    dao.setId( this.getId() );
    dao.setSchemaVersion( this.getSchemaVersion() );
    return dao;
  } // protected HibernateSettingsDAO getDAO()



  // GETTERS //

  // GETTERS //

  protected String getId() {
    return this.id;
  }
  protected int getSchemaVersion() {
    return this.schemaVersion;
  }


  // SETTERS //

  protected void setId(String id) {
    this.id = id;
  }
  protected void setSchemaVersion(int schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

} // class SettingsDTO extends BaseGrouperDTO

