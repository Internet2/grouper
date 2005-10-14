package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.Date;
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
    private Date createTime;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String displayExtension;

    /** nullable persistent field */
    private String displayName;

    /** nullable persistent field */
    private String extension;

    /** nullable persistent field */
    private Date modifyTime;

    /** nullable persistent field */
    private String name;

    /** persistent field */
    private String uuid;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member creator;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member modifier;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Stem parentStem;

    /** full constructor */
    public Group(Date createTime, String description, String displayExtension, String displayName, String extension, Date modifyTime, String name, String uuid, Integer version, edu.internet2.middleware.grouper.Member creator, edu.internet2.middleware.grouper.Member modifier, edu.internet2.middleware.grouper.Stem parentStem) {
        this.createTime = createTime;
        this.description = description;
        this.displayExtension = displayExtension;
        this.displayName = displayName;
        this.extension = extension;
        this.modifyTime = modifyTime;
        this.name = name;
        this.uuid = uuid;
        this.version = version;
        this.creator = creator;
        this.modifier = modifier;
        this.parentStem = parentStem;
    }

    /** default constructor */
    public Group() {
    }

    /** minimal constructor */
    public Group(Date createTime, String uuid) {
        this.createTime = createTime;
        this.uuid = uuid;
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
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
    public String getDisplayExtension() {
        return this.displayExtension;
    }

    public void setDisplayExtension(String displayExtension) {
        this.displayExtension = displayExtension;
    }

    /** 
     * Get displayName.
     *       
     */
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Date getModifyTime() {
        return this.modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
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
    public edu.internet2.middleware.grouper.Member getCreator() {
        return this.creator;
    }

    public void setCreator(edu.internet2.middleware.grouper.Member creator) {
        this.creator = creator;
    }

    /** 
     * Get last modifier.
     *       
     */
    public edu.internet2.middleware.grouper.Member getModifier() {
        return this.modifier;
    }

    public void setModifier(edu.internet2.middleware.grouper.Member modifier) {
        this.modifier = modifier;
    }

    /** 
     * Get parent stem.
     *       
     */
    public edu.internet2.middleware.grouper.Stem getParentStem() {
        return this.parentStem;
    }

    public void setParentStem(edu.internet2.middleware.grouper.Stem parentStem) {
        this.parentStem = parentStem;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("displayName", getDisplayName())
            .append("name", getName())
            .append("uuid", getUuid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Group) ) return false;
        Group castOther = (Group) other;
        return new EqualsBuilder()
            .append(this.getUuid(), castOther.getUuid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUuid())
            .toHashCode();
    }

}
