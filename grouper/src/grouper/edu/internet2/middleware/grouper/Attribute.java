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
    private String value;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Group group_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Field field_id;

    /** full constructor */
    public Attribute(String value, Integer version, edu.internet2.middleware.grouper.Group group_id, edu.internet2.middleware.grouper.Field field_id) {
        this.value = value;
        this.version = version;
        this.group_id = group_id;
        this.field_id = field_id;
    }

    /** default constructor */
    public Attribute() {
    }

    /** minimal constructor */
    public Attribute(String value) {
        this.value = value;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get attribute value.
     *       
     */
    private String getValue() {
        return this.value;
    }

    private void setValue(String value) {
        this.value = value;
    }

    private Integer getVersion() {
        return this.version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    /** 
     * Get group.
     *       
     */
    private edu.internet2.middleware.grouper.Group getGroup_id() {
        return this.group_id;
    }

    private void setGroup_id(edu.internet2.middleware.grouper.Group group_id) {
        this.group_id = group_id;
    }

    /** 
     * Get field.
     *       
     */
    private edu.internet2.middleware.grouper.Field getField_id() {
        return this.field_id;
    }

    private void setField_id(edu.internet2.middleware.grouper.Field field_id) {
        this.field_id = field_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("value", getValue())
            .append("group_id", getGroup_id())
            .append("field_id", getField_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Attribute) ) return false;
        Attribute castOther = (Attribute) other;
        return new EqualsBuilder()
            .append(this.getValue(), castOther.getValue())
            .append(this.getGroup_id(), castOther.getGroup_id())
            .append(this.getField_id(), castOther.getField_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getValue())
            .append(getGroup_id())
            .append(getField_id())
            .toHashCode();
    }

}
