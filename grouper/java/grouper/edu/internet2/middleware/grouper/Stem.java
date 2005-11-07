package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.Date;
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
    private String stem_description;

    /** nullable persistent field */
    private String display_extension;

    /** nullable persistent field */
    private String display_name;

    /** nullable persistent field */
    private String stem_extension;

    /** nullable persistent field */
    private String modify_source;

    /** nullable persistent field */
    private Date modify_time;

    /** nullable persistent field */
    private String stem_name;

    /** persistent field */
    private String parent_stem;

    /** persistent field */
    private String stem_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member creator_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member modifier_id;

    /** full constructor */
    public Stem(String create_source, Date create_time, String stem_description, String display_extension, String display_name, String stem_extension, String modify_source, Date modify_time, String stem_name, String parent_stem, String stem_id, edu.internet2.middleware.grouper.Member creator_id, edu.internet2.middleware.grouper.Member modifier_id) {
        this.create_source = create_source;
        this.create_time = create_time;
        this.stem_description = stem_description;
        this.display_extension = display_extension;
        this.display_name = display_name;
        this.stem_extension = stem_extension;
        this.modify_source = modify_source;
        this.modify_time = modify_time;
        this.stem_name = stem_name;
        this.parent_stem = parent_stem;
        this.stem_id = stem_id;
        this.creator_id = creator_id;
        this.modifier_id = modifier_id;
    }

    /** default constructor */
    public Stem() {
    }

    /** minimal constructor */
    public Stem(Date create_time, String parent_stem, String stem_id) {
        this.create_time = create_time;
        this.parent_stem = parent_stem;
        this.stem_id = stem_id;
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
    private String getStem_description() {
        return this.stem_description;
    }

    private void setStem_description(String stem_description) {
        this.stem_description = stem_description;
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
    private String getStem_extension() {
        return this.stem_extension;
    }

    private void setStem_extension(String stem_extension) {
        this.stem_extension = stem_extension;
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
    private String getStem_name() {
        return this.stem_name;
    }

    private void setStem_name(String stem_name) {
        this.stem_name = stem_name;
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
    private String getStem_id() {
        return this.stem_id;
    }

    private void setStem_id(String stem_id) {
        this.stem_id = stem_id;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("display_name", getDisplay_name())
            .append("stem_name", getStem_name())
            .append("stem_id", getStem_id())
            .append("creator_id", getCreator_id())
            .append("modifier_id", getModifier_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Stem) ) return false;
        Stem castOther = (Stem) other;
        return new EqualsBuilder()
            .append(this.getStem_id(), castOther.getStem_id())
            .append(this.getCreator_id(), castOther.getCreator_id())
            .append(this.getModifier_id(), castOther.getModifier_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getStem_id())
            .append(getCreator_id())
            .append(getModifier_id())
            .toHashCode();
    }

}
