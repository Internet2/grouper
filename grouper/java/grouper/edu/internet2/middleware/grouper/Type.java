package edu.internet2.middleware.grouper;

import java.io.Serializable;
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

    /** persistent field */
    private String fields;

    /** nullable persistent field */
    private Integer version;

    /** full constructor */
    public Type(String name, String fields, Integer version) {
        this.name = name;
        this.fields = fields;
        this.version = version;
    }

    /** default constructor */
    public Type() {
    }

    /** minimal constructor */
    public Type(String name, String fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get type name.
     *       
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 
     * Get fields.
     *       
     */
    public String getFields() {
        return this.fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
