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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Schema specification for a Group attribute or list.
 * @author blair christensen.
 *     
*/
class Field implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String name;

    /** persistent field */
    private boolean is_list;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Type type_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Privilege read_privilege_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Privilege write_privilege_id;

    /** full constructor */
    public Field(String name, boolean is_list, Integer version, edu.internet2.middleware.grouper.Type type_id, edu.internet2.middleware.grouper.Privilege read_privilege_id, edu.internet2.middleware.grouper.Privilege write_privilege_id) {
        this.name = name;
        this.is_list = is_list;
        this.version = version;
        this.type_id = type_id;
        this.read_privilege_id = read_privilege_id;
        this.write_privilege_id = write_privilege_id;
    }

    /** default constructor */
    public Field() {
    }

    /** minimal constructor */
    public Field(String name, boolean is_list) {
        this.name = name;
        this.is_list = is_list;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get field name.
     *       
     */
    private String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /** 
     * Get whether field is a list.
     *       
     */
    private boolean isIs_list() {
        return this.is_list;
    }

    private void setIs_list(boolean is_list) {
        this.is_list = is_list;
    }

    private Integer getVersion() {
        return this.version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    private edu.internet2.middleware.grouper.Type getType_id() {
        return this.type_id;
    }

    private void setType_id(edu.internet2.middleware.grouper.Type type_id) {
        this.type_id = type_id;
    }

    /** 
     * Get read privilege.
     *       
     */
    private edu.internet2.middleware.grouper.Privilege getRead_privilege_id() {
        return this.read_privilege_id;
    }

    private void setRead_privilege_id(edu.internet2.middleware.grouper.Privilege read_privilege_id) {
        this.read_privilege_id = read_privilege_id;
    }

    /** 
     * Get write privilege.
     *       
     */
    private edu.internet2.middleware.grouper.Privilege getWrite_privilege_id() {
        return this.write_privilege_id;
    }

    private void setWrite_privilege_id(edu.internet2.middleware.grouper.Privilege write_privilege_id) {
        this.write_privilege_id = write_privilege_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("is_list", isIs_list())
            .append("type_id", getType_id())
            .append("read_privilege_id", getRead_privilege_id())
            .append("write_privilege_id", getWrite_privilege_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Field) ) return false;
        Field castOther = (Field) other;
        return new EqualsBuilder()
            .append(this.getName(), castOther.getName())
            .append(this.isIs_list(), castOther.isIs_list())
            .append(this.getType_id(), castOther.getType_id())
            .append(this.getRead_privilege_id(), castOther.getRead_privilege_id())
            .append(this.getWrite_privilege_id(), castOther.getWrite_privilege_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(isIs_list())
            .append(getType_id())
            .append(getRead_privilege_id())
            .append(getWrite_privilege_id())
            .toHashCode();
    }

}
