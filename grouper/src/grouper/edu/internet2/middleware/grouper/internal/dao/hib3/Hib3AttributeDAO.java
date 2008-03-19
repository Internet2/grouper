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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Basic Hibernate <code>Attribute</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3AttributeDAO.java,v 1.1.4.1 2008-03-19 18:46:10 mchyzer Exp $
 * @since   @HEAD@
 */
class Hib3AttributeDAO extends Hib3DAO {

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
    if (!(other instanceof Hib3AttributeDAO)) {
      return false;
    }
    Hib3AttributeDAO that = (Hib3AttributeDAO) other;
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
  protected String getAttrName() {
    return this.attrName;
  }
  // @since   @HEAD@
  protected String getGroupUuid() {
    return this.groupUUID;
  }
  // @since   @HEAD@
  protected String getId() {
    return this.id;
  }
  // @since   @HEAD@
  protected String getValue() {
    return this.value;
  }
  // @since   @HEAD@
  protected Hib3AttributeDAO setAttrName(String attrName) {
    this.attrName = attrName;
    return this;
  }
  // @since   @HEAD@
  protected Hib3AttributeDAO setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
    return this;
  }
  // @since   @HEAD@
  protected Hib3AttributeDAO setId(String id) {
    this.id = id;
    return this;
  }
  // @since   @HEAD@
  protected Hib3AttributeDAO setValue(String value) {
    this.value = value;
    return this;
  }

} 

