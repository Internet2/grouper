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
    private Set attributes;

    /** persistent field */
    private Set memberships;

    /** full constructor */
    public Group(Date create_time, String description, String display_extension, String display_name, String extension, Date modify_time, String name, String uuid, Integer version, edu.internet2.middleware.grouper.Member creator_id, edu.internet2.middleware.grouper.Member modifier_id, edu.internet2.middleware.grouper.Stem parent_stem, Set attributes, Set memberships) {
        this.create_time = create_time;
        this.description = description;
        this.display_extension = display_extension;
        this.display_name = display_name;
        this.extension = extension;
        this.modify_time = modify_time;
        this.name = name;
        this.uuid = uuid;
        this.version = version;
        this.creator_id = creator_id;
        this.modifier_id = modifier_id;
        this.parent_stem = parent_stem;
        this.attributes = attributes;
        this.memberships = memberships;
    }

    /** default constructor */
    public Group() {
    }

    /** minimal constructor */
    public Group(Date create_time, String uuid, Set attributes, Set memberships) {
        this.create_time = create_time;
        this.uuid = uuid;
        this.attributes = attributes;
        this.memberships = memberships;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get create time.
     *       
     */
    public Date getCreate_time() {
        return this.create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    /** 
     * Get description.
     *       
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** 
     * Get displayExtension.
     *       
     */
    public String getDisplay_extension() {
        return this.display_extension;
    }

    public void setDisplay_extension(String display_extension) {
        this.display_extension = display_extension;
    }

    /** 
     * Get displayName.
     *       
     */
    public String getDisplay_name() {
        return this.display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    /** 
     * Get extension.
     *       
     */
    public String getExtension() {
        return this.extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Date getModify_time() {
        return this.modify_time;
    }

    public void setModify_time(Date modify_time) {
        this.modify_time = modify_time;
    }

    /** 
     * Get name.
     *       
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 
     * Get UUID.
     *       
     */
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /** 
     * Get creator.
     *       
     */
    public edu.internet2.middleware.grouper.Member getCreator_id() {
        return this.creator_id;
    }

    public void setCreator_id(edu.internet2.middleware.grouper.Member creator_id) {
        this.creator_id = creator_id;
    }

    /** 
     * Get modifier.
     *       
     */
    public edu.internet2.middleware.grouper.Member getModifier_id() {
        return this.modifier_id;
    }

    public void setModifier_id(edu.internet2.middleware.grouper.Member modifier_id) {
        this.modifier_id = modifier_id;
    }

    /** 
     * Get parent stem.
     *       
     */
    public edu.internet2.middleware.grouper.Stem getParent_stem() {
        return this.parent_stem;
    }

    public void setParent_stem(edu.internet2.middleware.grouper.Stem parent_stem) {
        this.parent_stem = parent_stem;
    }

    /** 
     * Get attributes.
     *       
     */
    public Set getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Set attributes) {
        this.attributes = attributes;
    }

    /** 
     * Get memberships.
     *       
     */
    public Set getMemberships() {
        return this.memberships;
    }

    public void setMemberships(Set memberships) {
        this.memberships = memberships;
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
        if ( !(other instanceof Group) ) return false;
        Group castOther = (Group) other;
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
