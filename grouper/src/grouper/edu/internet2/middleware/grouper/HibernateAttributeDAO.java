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
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;

/**
 * Stub Hibernate Group {@link Attribute} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateAttributeDAO.java,v 1.1 2007-02-08 16:25:25 blair Exp $
 * @since   1.2.0
 */
class HibernateAttributeDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateAttributeDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  attrName;
  private String  groupUUID;
  private String  id;
  private String  value;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof HibernateAttributeDAO)) {
      return false;
    }
    HibernateAttributeDAO that = (HibernateAttributeDAO) other;
    return new EqualsBuilder()
      .append( this.getAttrName(),  that.getAttrName()  )
      .append( this.getGroupUuid(), that.getGroupUuid() )
      .append( this.getValue(),     that.getValue()     )
      .isEquals();
  } // public boolean equals(other)
  
  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getAttrName()  )
      .append( this.getGroupUuid() )
      .append( this.getValue()     )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "attrName",  this.getAttrName()  )
      .append( "groupUuid", this.getGroupUuid() )
      .append( "id",        this.getId()        )
      .append( "value",     this.getValue()     )
      .toString();
  } // public String toString()


  // GETTERS //

  protected String getAttrName() {
    return this.attrName;
  }
  protected String getGroupUuid() {
    return this.groupUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected String getValue() {
    return this.value;
  }


  // SETTERS //

  protected void setAttrName(String attrName) {
    this.attrName = attrName;
  }
  protected void setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setValue(String value) {
    this.value = value;
  }

} // class HibernateAttributeDAO extends HibernateDAO 

