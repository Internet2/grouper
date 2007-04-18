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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  org.apache.commons.lang.builder.*;

/**
 * Stub Hibernate Group {@link Attribute} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateAttributeDAO.java,v 1.2 2007-04-18 15:56:59 blair Exp $
 * @since   1.2.0
 */
class HibernateAttributeDAO extends HibernateDAO {

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


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected String getAttrName() {
    return this.attrName;
  }
  // @since   1.2.0
  protected String getGroupUuid() {
    return this.groupUUID;
  }
  // @since   1.2.0
  protected String getId() {
    return this.id;
  }
  // @since   1.2.0
  protected String getValue() {
    return this.value;
  }
  // @since   1.2.0
  protected HibernateAttributeDAO setAttrName(String attrName) {
    this.attrName = attrName;
    return this;
  }
  // @since   1.2.0
  protected HibernateAttributeDAO setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
    return this;
  }
  // @since   1.2.0
  protected HibernateAttributeDAO setId(String id) {
    this.id = id;
    return this;
  }
  // @since   1.2.0
  protected HibernateAttributeDAO setValue(String value) {
    this.value = value;
    return this;
  }

} 

