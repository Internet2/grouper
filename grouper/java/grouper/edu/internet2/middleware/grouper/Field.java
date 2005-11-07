package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Schema specification for a Group attribute or list.
 * @author blair christensen.
 *     
*/
public class Field implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String field_name;

    /** full constructor */
    public Field(String field_name) {
        this.field_name = field_name;
    }

    /** default constructor */
    public Field() {
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getField_name() {
        return this.field_name;
    }

    private void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Field) ) return false;
        Field castOther = (Field) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

}
