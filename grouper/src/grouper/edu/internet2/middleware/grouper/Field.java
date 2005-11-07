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

import java.io.Serializable;
import org.apache.commons.lang.builder.*;


/** 
 * Schema specification for a Group attribute or list.
 * <p />
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.1.2.8 2005-11-07 00:31:15 blair Exp $    
 */
class Field implements Serializable {

    // TODO Should I have a singleton for each field?

    // Hibernate Properties
    private String    field_name;;
    private String    id;
    private boolean   is_list;
    private Privilege read_privilege_id;
    private GroupType type_id;
    private Privilege write_privilege_id;

    
    // Constructors
    
    // For Hibernate
    public Field() {
      super();
    }

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
             .append(
                this.getField_name(),         otherField.getField_name()
              )
             .append(
                this.isIs_list(),             otherField.isIs_list()
              )
             .append(
                this.getType_id(),            otherField.getType_id()
              )
             .append(
                this.getRead_privilege_id(),  otherField.getRead_privilege_id() 
              )
             .append(
                this.getWrite_privilege_id(), otherField.getWrite_privilege_id()
              )
             .isEquals();
    } // public boolean equals(other)

    public int hashCode() {
      return new HashCodeBuilder()
             .append(getField_name()        )
             .append(isIs_list()            )
             .append(getType_id()           )
             .append(getRead_privilege_id() )
             .append(getWrite_privilege_id())
             .toHashCode();
    } // public int hashCode()

    public String toString() {
      return new ToStringBuilder(this)
             .append("name",                getField_name()         )
             .append("is_list",             isIs_list()             )
             .append("type_id",             getType_id()            )
             .append("read_privilege_id",   getRead_privilege_id()  )
             .append("write_privilege_id",  getWrite_privilege_id() )
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

    private boolean isIs_list() {
        return this.is_list;
    }

    private void setIs_list(boolean is_list) {
        this.is_list = is_list;
    }

    private GroupType getType_id() {
        return this.type_id;
    }

    private void setType_id(GroupType type_id) {
        this.type_id = type_id;
    }

    private Privilege getRead_privilege_id() {
        return this.read_privilege_id;
    }

    private void setRead_privilege_id(Privilege read_privilege_id) {
        this.read_privilege_id = read_privilege_id;
    }

    private Privilege getWrite_privilege_id() {
        return this.write_privilege_id;
    }

    private void setWrite_privilege_id(Privilege write_privilege_id) {
        this.write_privilege_id = write_privilege_id;
    }

}
