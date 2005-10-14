package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * A group attribute within the Groups registry.
 * @author blair christensen.
 *     
*/
public class Attribute implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String group_id;

    /** persistent field */
    private String field_id;

    /** persistent field */
    private String value;

    /** nullable persistent field */
    private Integer version;

    /** full constructor */
    public Attribute(String group_id, String field_id, String value, Integer version) {
        this.group_id = group_id;
        this.field_id = field_id;
        this.value = value;
        this.version = version;
    }

    /** default constructor */
    public Attribute() {
    }

    /** minimal constructor */
    public Attribute(String group_id, String field_id, String value) {
        this.group_id = group_id;
        this.field_id = field_id;
        this.value = value;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get attribute group.
     *       
     */
    public String getGroup_id() {
        return this.group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    /** 
     * Get attribute field.
     *       
     */
    public String getField_id() {
        return this.field_id;
    }

    public void setField_id(String field_id) {
        this.field_id = field_id;
    }

    /** 
     * Get attribute value.
     *       
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("group_id", getGroup_id())
            .append("field_id", getField_id())
            .append("value", getValue())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Attribute) ) return false;
        Attribute castOther = (Attribute) other;
        return new EqualsBuilder()
            .append(this.getGroup_id(), castOther.getGroup_id())
            .append(this.getField_id(), castOther.getField_id())
            .append(this.getValue(), castOther.getValue())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getGroup_id())
            .append(getField_id())
            .append(getValue())
            .toHashCode();
    }

}
