/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * <p/>
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.14 2006-12-27 18:22:21 blair Exp $    
 */
public class Field implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = 2072790175332537149L;


  // HIBERNATE PROPERTIES //
  private String    field_name;
  private FieldType field_type;
  private GroupType group_type;
  private String    id;
  private boolean   nullable;
  private Privilege read_priv;
  private Privilege write_priv;

    
  // CONSTRUCTORS //
  
  // For Hibernate.
  // @since   1.2.0
  private Field() {
    super();
  } // private Field()

  protected Field(
    String field, FieldType type, Privilege read, Privilege write, boolean nullable
  ) 
  {
    this.setField_name(field);
    this.setField_type(type);
    this.setNullable(nullable);
    this.setRead_priv(read);
    this.setWrite_priv(write);
  } // protected Field(field, type, read, write)


  // PUBLIC INSTANCE METHODS //
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

  public GroupType getGroupType() {
    return this.getGroup_type();
  } // public GroupType getGroupType()

  public FieldType getType() {
    return this.getField_type();
  } // public FieldType getType()

  public String getName() {
    return this.getField_name();
  } // public String getName()

  public Privilege getReadPriv() {
    return this.getRead_priv();
  } // public Privilege getReadPriv()

  public boolean getRequired() {
    if (this.getNullable() == true) {
      return false;
    }
    return true;
  } // public boolean isRequired()

  public Privilege getWritePriv() {
    return this.getWrite_priv();
  } // public Privilege getWritePriv()

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getField_name()        )
           .append(getField_type()        )
           .toHashCode();
  } // public int hashCode()

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append("name"        , this.getField_name()  )
      .append("group type"  , this.getGroup_type()  )
      .append("field type"  , this.getField_type()  )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //
  // @since 1.0
  protected boolean internal_isInUse() 
    throws  SchemaException
  {
    return HibernateFieldDAO.isInUse(this);
  } // protected boolean internal_isInUse()


  // GETTERS //
  private String getField_name() {
    return this.field_name;
  }
  private FieldType getField_type() {
    return this.field_type;
  }
  private GroupType getGroup_type() {
    return this.group_type;
  }
  private String getId() {
    return this.id;
  }
  private boolean getNullable() {
    return this.nullable;
  }
  private Privilege getRead_priv() {
    return this.read_priv;
  }
  private Privilege getWrite_priv() {
    return this.write_priv;
  }


  // SETTERS //
  private void setField_name(String field_name) {
    this.field_name = field_name;
  }
  private void setField_type(FieldType type) {
    this.field_type = type;
  }
  protected void setGroup_type(GroupType type) {
    this.group_type = type;
  }
  private void setId(String id) {
    this.id = id;
  }
  private void setNullable(boolean nullable) {
    this.nullable = nullable;
  }
  private void setRead_priv(Privilege read) {
    this.read_priv = read;
  }
  private void setWrite_priv(Privilege write) {
    this.write_priv = write;
  }

}

