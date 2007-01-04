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
 * A group attribute within the Groups registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Attribute.java,v 1.13 2007-01-04 17:17:45 blair Exp $
 */
class Attribute {

  // HIBERNATE PROPERTIES //
  private Field   field;
  private Group   group;
  private String  id;
  private String  value     = GrouperConfig.EMPTY_STRING;
  private int     version;


  // CONSTRUCTORS //

  /**
   * For Hibernate.
   */
  public Attribute() {
    super();
  } // public Attribute()

  protected Attribute(Group g, Field f, String val) {
    this.setField(  f   );
    this.setGroup(  g   );
    this.setValue(  val );
  } // protected Attribute(g, f, val)


  // PUBLIC INSTANCE METHODS //
  public String toString() {
    return new ToStringBuilder(this)
           .append("value", getValue())
           .append("group", getGroup())
           .append("field", getField())
           .toString();
  }

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Attribute)) {
      return false;
    }
    Attribute otherAttr = (Attribute) other;
    return new EqualsBuilder()
      .append(  this.getValue(), otherAttr.getValue() )
      .append(  this.getGroup(), otherAttr.getGroup() )
      .append(  this.getField(), otherAttr.getField() )
      .isEquals();
  } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
      .append(getValue())
      .append(getGroup())
      .append(getField())
      .toHashCode();
  } // public int hashCode()

  // GETTERS //
  protected Field getField() {
    return this.field;
  }
  protected Group getGroup() {
    return this.group;
  }
  private String getId() {
    return this.id;
  }
  protected String getValue() {
    return this.value;
  }
  private int getVersion() {
    return this.version;
  }


  // SETTERS //
  private void setField(Field field) {
    this.field = field;
  }
  private void setGroup(Group group) {
    this.group = group;
  }
  private void setId(String id) {
    this.id = id;
  }
  protected void setValue(String value) {
    this.value = value;
  }
  private void setVersion(int version) {
    this.version = version;
  }

}
