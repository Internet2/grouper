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
 * Schema specification for a Group attribute or list.
 * <p />
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.1.2.11 2005-11-11 17:07:30 blair Exp $    
 */
public class Field implements Serializable {

  // Hibernate Properties
  private String    field_name;
  private FieldType field_type;
  private String    id;

    
  // Constructors
    
  // For Hibernate
  public Field() {
    super();
  }

  protected Field(String field, FieldType type) {
    this.setField_name(field);
    this.setField_type(type);
  } // protected Field(field, type)

  // TODO Deprecate?
  protected Field(String field) {
    this.setField_name(field);
  } // protected Field(field)


  // Public Instance Methods
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if (!(other instanceof Field)) {
      return false;
    }
    Field otherField = (Field) other;
    return new EqualsBuilder()
           .append(this.getField_name(),  otherField.getField_name())
           .append(this.getField_type(),  otherField.getField_type())
           .isEquals();
  } // public boolean equals(other)

  public FieldType getFieldType() {
    return this.getField_type();
  } // public FieldType getFieldType()

  public String getName() {
    return this.getField_name();
  } // public String getName()

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getField_name()        )
           .append(getField_type()        )
           .toHashCode();
  } // public int hashCode()

  public String toString() {
    return new ToStringBuilder(this)
           .append("name",  getField_name() )
           .append("type",  getField_type() )
           .toString();
  } // public String toString()


  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private String getField_name() {
    return this.field_name;
  }

  private void setField_name(String field_name) {
    this.field_name = field_name;
  }

  private FieldType getField_type() {
    return this.field_type;
  }

  private void setField_type(FieldType type) {
    this.field_type = type;
  }

}

