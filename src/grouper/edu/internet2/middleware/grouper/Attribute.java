/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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


import  java.io.Serializable;
import  org.apache.commons.lang.builder.*;


/** 
 * A group attribute within the Groups registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Attribute.java,v 1.6 2006-01-31 20:44:05 blair Exp $
 */
class Attribute implements Serializable {

  // Hibernate Properties
  private Field   field;
  private Group   group;
  private String  id;
  private String  value     = new String();
  private int     version;


  // Constructors

  // For Hibernate
  public Attribute() {
    super();
  } // public Attribute()

  protected Attribute(Group g, Field f, String val) {
    this.field  = f;
    this.group  = g;
    this.value  = val;
  } // protected Attribute(g, f, val)


  // Public Instance Methods
  public String toString() {
    return new ToStringBuilder(this)
           .append("value", getValue())
           .append("group", getGroup())
           .append("field", getField())
           .toString();
  }

  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof Attribute) ) return false;
    Attribute castOther = (Attribute) other;
    return new EqualsBuilder()
           .append(this.getValue(), castOther.getValue())
           .append(this.getGroup(), castOther.getGroup())
           .append(this.getField(), castOther.getField())
           .isEquals();
  }

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getValue())
           .append(getGroup())
           .append(getField())
           .toHashCode();
  }

  // Hibernate Accessors

  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  protected String getValue() {
    return this.value;
  }

  protected void setValue(String value) {
    this.value = value;
  }

  protected Group getGroup() {
    return this.group;
  }

  private void setGroup(Group group) {
    this.group = group;
  }

  protected Field getField() {
    return this.field;
  }

  private void setField(Field field) {
    this.field = field;
  }

  private int getVersion() {
    return this.version;
  }

  private void setVersion(int version) {
    this.version = version;
  }

}
