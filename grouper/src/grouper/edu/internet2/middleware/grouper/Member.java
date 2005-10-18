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
 * A member within the Groups Registry.
 * @author blair christensen.
 *     
*/
public class Member implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String subject_id;

    /** persistent field */
    private String subject_source;

    /** persistent field */
    private String subject_type;

    /** persistent field */
    private String uuid;

    /** nullable persistent field */
    private Integer version;

    /** full constructor */
    public Member(String subject_id, String subject_source, String subject_type, String uuid, Integer version) {
        this.subject_id = subject_id;
        this.subject_source = subject_source;
        this.subject_type = subject_type;
        this.uuid = uuid;
        this.version = version;
    }

    /** default constructor */
    public Member() {
    }

    /** minimal constructor */
    public Member(String subject_id, String subject_source, String subject_type, String uuid) {
        this.subject_id = subject_id;
        this.subject_source = subject_source;
        this.subject_type = subject_type;
        this.uuid = uuid;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get Subject ID.
     *       
     */
    private String getSubject_id() {
        return this.subject_id;
    }

    private void setSubject_id(String subject_id) {
        this.subject_id = subject_id;
    }

    /** 
     * Get Subject source.
     *       
     */
    private String getSubject_source() {
        return this.subject_source;
    }

    private void setSubject_source(String subject_source) {
        this.subject_source = subject_source;
    }

    /** 
     * Get Subject type.
     *       
     */
    private String getSubject_type() {
        return this.subject_type;
    }

    private void setSubject_type(String subject_type) {
        this.subject_type = subject_type;
    }

    /** 
     * Get UUID.
     *       
     */
    private String getUuid() {
        return this.uuid;
    }

    private void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private Integer getVersion() {
        return this.version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("subject_id", getSubject_id())
            .append("subject_source", getSubject_source())
            .append("subject_type", getSubject_type())
            .append("uuid", getUuid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Member) ) return false;
        Member castOther = (Member) other;
        return new EqualsBuilder()
            .append(this.getSubject_id(), castOther.getSubject_id())
            .append(this.getSubject_source(), castOther.getSubject_source())
            .append(this.getSubject_type(), castOther.getSubject_type())
            .append(this.getUuid(), castOther.getUuid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getSubject_id())
            .append(getSubject_source())
            .append(getSubject_type())
            .append(getUuid())
            .toHashCode();
    }

}
