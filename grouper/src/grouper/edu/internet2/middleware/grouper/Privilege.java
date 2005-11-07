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
 * Schema specification for Access and Naming privileges.
 * @author blair christensen.
 *     
*/
class Privilege implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String name;

    /** persistent field */
    private boolean is_access;

    /** persistent field */
    private boolean is_naming;

    /** nullable persistent field */
    private Integer version;

    /** full constructor */
    public Privilege(String name, boolean is_access, boolean is_naming, Integer version) {
        this.name = name;
        this.is_access = is_access;
        this.is_naming = is_naming;
        this.version = version;
    }

    /** default constructor */
    public Privilege() {
    }

    /** minimal constructor */
    public Privilege(String name, boolean is_access, boolean is_naming) {
        this.name = name;
        this.is_access = is_access;
        this.is_naming = is_naming;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get name.
     *       
     */
    private String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /** 
     * Get whether this is an Access privilege.
     *       
     */
    private boolean isIs_access() {
        return this.is_access;
    }

    private void setIs_access(boolean is_access) {
        this.is_access = is_access;
    }

    /** 
     * Get whether this is a Naming privilege.
     *       
     */
    private boolean isIs_naming() {
        return this.is_naming;
    }

    private void setIs_naming(boolean is_naming) {
        this.is_naming = is_naming;
    }

    private Integer getVersion() {
        return this.version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("is_access", isIs_access())
            .append("is_naming", isIs_naming())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Privilege) ) return false;
        Privilege castOther = (Privilege) other;
        return new EqualsBuilder()
            .append(this.getName(), castOther.getName())
            .append(this.isIs_access(), castOther.isIs_access())
            .append(this.isIs_naming(), castOther.isIs_naming())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(isIs_access())
            .append(isIs_naming())
            .toHashCode();
    }

}
