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

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get create source.
     *       
     */
    public String getCreate_source() {
        return this.create_source;
    }

    public void setCreate_source(String create_source) {
        this.create_source = create_source;
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

    /** 
     * Get modify source.
     *       
     */
    public String getModify_source() {
        return this.modify_source;
    }

    public void setModify_source(String modify_source) {
        this.modify_source = modify_source;
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
     * Get child groups.
     *       
     */
    public Set getChild_groups() {
        return this.child_groups;
    }

    public void setChild_groups(Set child_groups) {
        this.child_groups = child_groups;
    }

    /** 
     * Get child stems.
     *       
     */
    public Set getChild_stems() {
        return this.child_stems;
    }

    public void setChild_stems(Set child_stems) {
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
