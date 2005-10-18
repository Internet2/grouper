package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Schema specification for a Group type.
 * @author blair christensen.
 *     
*/
public class Type implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private Integer version;

    /** persistent field */
    private Set fields;

    /** full constructor */
    public Type(String name, Integer version, Set fields) {
        this.name = name;
        this.version = version;
        this.fields = fields;
    }

    /** default constructor */
    public Type() {
    }

    /** minimal constructor */
    public Type(String name, Set fields) {
        this.name = name;
        this.fields = fields;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get type name.
     *       
     */
    private String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    private Integer getVersion() {
        return this.version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    /** 
     * Get fields.
     *       
     */
    private Set getFields() {
        return this.fields;
    }

    private void setFields(Set fields) {
        this.fields = fields;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("fields", getFields())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Type) ) return false;
        Type castOther = (Type) other;
        return new EqualsBuilder()
            .append(this.getName(), castOther.getName())
            .append(this.getFields(), castOther.getFields())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(getFields())
            .toHashCode();
    }

}
