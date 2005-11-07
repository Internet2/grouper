package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * A group within the Groups Registry.
 * @author blair christensen.
 *     
*/
public class Group implements Serializable {

    /** identifier field */
    private String id;

    /** nullable persistent field */
    private String create_source;

    /** persistent field */
    private Date create_time;

    /** nullable persistent field */
    private String group_description;

    /** nullable persistent field */
    private String display_extension;

    /** nullable persistent field */
    private String display_name;

    /** nullable persistent field */
    private String group_extension;

    /** nullable persistent field */
    private String modify_source;

    /** nullable persistent field */
    private Date modify_time;

    /** nullable persistent field */
    private String group_name;

    /** persistent field */
    private String parent_stem;

    /** persistent field */
    private String group_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member creator_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member modifier_id;

    /** persistent field */
    private Set group_attributes;

    /** full constructor */
    public Group(String create_source, Date create_time, String group_description, String display_extension, String display_name, String group_extension, String modify_source, Date modify_time, String group_name, String parent_stem, String group_id, edu.internet2.middleware.grouper.Member creator_id, edu.internet2.middleware.grouper.Member modifier_id, Set group_attributes) {
        this.create_source = create_source;
        this.create_time = create_time;
        this.group_description = group_description;
        this.display_extension = display_extension;
        this.display_name = display_name;
        this.group_extension = group_extension;
        this.modify_source = modify_source;
        this.modify_time = modify_time;
        this.group_name = group_name;
        this.parent_stem = parent_stem;
        this.group_id = group_id;
        this.creator_id = creator_id;
        this.modifier_id = modifier_id;
        this.group_attributes = group_attributes;
    }

    /** default constructor */
    public Group() {
    }

    /** minimal constructor */
    public Group(Date create_time, String parent_stem, String group_id, Set group_attributes) {
        this.create_time = create_time;
        this.parent_stem = parent_stem;
        this.group_id = group_id;
        this.group_attributes = group_attributes;
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
    private String getGroup_description() {
        return this.group_description;
    }

    private void setGroup_description(String group_description) {
        this.group_description = group_description;
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
    private String getGroup_extension() {
        return this.group_extension;
    }

    private void setGroup_extension(String group_extension) {
        this.group_extension = group_extension;
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
    private String getGroup_name() {
        return this.group_name;
    }

    private void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    private String getParent_stem() {
        return this.parent_stem;
    }

    private void setParent_stem(String parent_stem) {
        this.parent_stem = parent_stem;
    }

    /** 
     * Get UUID.
     *       
     */
    private String getGroup_id() {
        return this.group_id;
    }

    private void setGroup_id(String group_id) {
        this.group_id = group_id;
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
     * Get attributes.
     *       
     */
    private Set getGroup_attributes() {
        return this.group_attributes;
    }

    private void setGroup_attributes(Set group_attributes) {
        this.group_attributes = group_attributes;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("display_name", getDisplay_name())
            .append("group_name", getGroup_name())
            .append("group_id", getGroup_id())
            .append("creator_id", getCreator_id())
            .append("modifier_id", getModifier_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Group) ) return false;
        Group castOther = (Group) other;
        return new EqualsBuilder()
            .append(this.getGroup_id(), castOther.getGroup_id())
            .append(this.getCreator_id(), castOther.getCreator_id())
            .append(this.getModifier_id(), castOther.getModifier_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getGroup_id())
            .append(getCreator_id())
            .append(getModifier_id())
            .toHashCode();
    }

}
