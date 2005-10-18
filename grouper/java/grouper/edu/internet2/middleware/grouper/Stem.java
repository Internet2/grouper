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
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * A namespace within the Groups Registry.
 * @author blair christensen.
 *     
*/
public class Stem implements Serializable {

    /** identifier field */
    private String id;

    /** nullable persistent field */
    private String create_source;

    /** persistent field */
    private Date create_time;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String display_extension;

    /** nullable persistent field */
    private String display_name;

    /** nullable persistent field */
    private String extension;

    /** nullable persistent field */
    private String modify_source;

    /** nullable persistent field */
    private Date modify_time;

    /** nullable persistent field */
    private String name;

    /** persistent field */
    private String uuid;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member creator_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member modifier_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Stem parent_stem;

    /** persistent field */
    private Set child_groups;

    /** persistent field */
    private Set child_stems;

    /** full constructor */
    public Stem(String create_source, Date create_time, String description, String display_extension, String display_name, String extension, String modify_source, Date modify_time, String name, String uuid, Integer version, edu.internet2.middleware.grouper.Member creator_id, edu.internet2.middleware.grouper.Member modifier_id, edu.internet2.middleware.grouper.Stem parent_stem, Set child_groups, Set child_stems) {
        this.create_source = create_source;
        this.create_time = create_time;
        this.description = description;
        this.display_extension = display_extension;
        this.display_name = display_name;
        this.extension = extension;
        this.modify_source = modify_source;
        this.modify_time = modify_time;
        this.name = name;
        this.uuid = uuid;
        this.version = version;
        this.creator_id = creator_id;
        this.modifier_id = modifier_id;
        this.parent_stem = parent_stem;
        this.child_groups = child_groups;
        this.child_stems = child_stems;
    }

    /** default constructor */
    public Stem() {
    }

    /** minimal constructor */
    public Stem(Date create_time, String uuid, Set child_groups, Set child_stems) {
        this.create_time = create_time;
        this.uuid = uuid;
        this.child_groups = child_groups;
        this.child_stems = child_stems;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get create source.
     *       
     */
    private String getCreate_source() {
        return this.create_source;
    }

    private void setCreate_source(String create_source) {
        this.create_source = create_source;
    }

    /** 
     * Get create time.
     *       
     */
    private Date getCreate_time() {
        return this.create_time;
    }

    private void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    /** 
     * Get description.
     *       
     */
    private String getDescription() {
        return this.description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    /** 
     * Get displayExtension.
     *       
     */
    private String getDisplay_extension() {
        return this.display_extension;
    }

    private void setDisplay_extension(String display_extension) {
        this.display_extension = display_extension;
    }

    /** 
     * Get displayName.
     *       
     */
    private String getDisplay_name() {
        return this.display_name;
    }

    private void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    /** 
     * Get extension.
     *       
     */
    private String getExtension() {
        return this.extension;
    }

    private void setExtension(String extension) {
        this.extension = extension;
    }

    /** 
     * Get modify source.
     *       
     */
    private String getModify_source() {
        return this.modify_source;
    }

    private void setModify_source(String modify_source) {
        this.modify_source = modify_source;
    }

    private Date getModify_time() {
        return this.modify_time;
    }

    private void setModify_time(Date modify_time) {
        this.modify_time = modify_time;
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

    /** 
     * Get creator.
     *       
     */
    private edu.internet2.middleware.grouper.Member getCreator_id() {
        return this.creator_id;
    }

    private void setCreator_id(edu.internet2.middleware.grouper.Member creator_id) {
        this.creator_id = creator_id;
    }

    /** 
     * Get modifier.
     *       
     */
    private edu.internet2.middleware.grouper.Member getModifier_id() {
        return this.modifier_id;
    }

    private void setModifier_id(edu.internet2.middleware.grouper.Member modifier_id) {
        this.modifier_id = modifier_id;
    }

    /** 
     * Get parent stem.
     *       
     */
    private edu.internet2.middleware.grouper.Stem getParent_stem() {
        return this.parent_stem;
    }

    private void setParent_stem(edu.internet2.middleware.grouper.Stem parent_stem) {
        this.parent_stem = parent_stem;
    }

    /** 
     * Get child groups.
     *       
     */
    private Set getChild_groups() {
        return this.child_groups;
    }

    private void setChild_groups(Set child_groups) {
        this.child_groups = child_groups;
    }

    /** 
     * Get child stems.
     *       
     */
    private Set getChild_stems() {
        return this.child_stems;
    }

    private void setChild_stems(Set child_stems) {
        this.child_stems = child_stems;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("display_name", getDisplay_name())
            .append("name", getName())
            .append("uuid", getUuid())
            .append("creator_id", getCreator_id())
            .append("modifier_id", getModifier_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Stem) ) return false;
        Stem castOther = (Stem) other;
        return new EqualsBuilder()
            .append(this.getUuid(), castOther.getUuid())
            .append(this.getCreator_id(), castOther.getCreator_id())
            .append(this.getModifier_id(), castOther.getModifier_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUuid())
            .append(getCreator_id())
            .append(getModifier_id())
            .toHashCode();
    }

}
