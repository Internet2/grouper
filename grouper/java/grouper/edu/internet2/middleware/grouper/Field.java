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
    private String type_id;

    /** persistent field */
    private String name;

    /** persistent field */
    private String read_privilege;

    /** persistent field */
    private String write_privilege;

    /** persistent field */
    private boolean is_list;

    /** nullable persistent field */
    private Integer version;

    /** full constructor */
    public Field(String type_id, String name, String read_privilege, String write_privilege, boolean is_list, Integer version) {
        this.type_id = type_id;
        this.name = name;
        this.read_privilege = read_privilege;
        this.write_privilege = write_privilege;
        this.is_list = is_list;
        this.version = version;
    }

    /** default constructor */
    public Field() {
    }

    /** minimal constructor */
    public Field(String type_id, String name, String read_privilege, String write_privilege, boolean is_list) {
        this.type_id = type_id;
        this.name = name;
        this.read_privilege = read_privilege;
        this.write_privilege = write_privilege;
        this.is_list = is_list;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get group type.
     *       
     */
    public String getType_id() {
        return this.type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    /** 
     * Get field name.
     *       
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 
     * Get read privilege.
     *       
     */
    public String getRead_privilege() {
        return this.read_privilege;
    }

    public void setRead_privilege(String read_privilege) {
        this.read_privilege = read_privilege;
    }

    /** 
     * Get write privilege.
     *       
     */
    public String getWrite_privilege() {
        return this.write_privilege;
    }

    public void setWrite_privilege(String write_privilege) {
        this.write_privilege = write_privilege;
    }

    /** 
     * Get whether field is a list.
     *       
     */
    public boolean isIs_list() {
        return this.is_list;
    }

    public void setIs_list(boolean is_list) {
        this.is_list = is_list;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("type_id", getType_id())
            .append("name", getName())
            .append("read_privilege", getRead_privilege())
            .append("write_privilege", getWrite_privilege())
            .append("is_list", isIs_list())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Field) ) return false;
        Field castOther = (Field) other;
        return new EqualsBuilder()
            .append(this.getType_id(), castOther.getType_id())
            .append(this.getName(), castOther.getName())
            .append(this.getRead_privilege(), castOther.getRead_privilege())
            .append(this.getWrite_privilege(), castOther.getWrite_privilege())
            .append(this.isIs_list(), castOther.isIs_list())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getType_id())
            .append(getName())
            .append(getRead_privilege())
            .append(getWrite_privilege())
            .append(isIs_list())
            .toHashCode();
    }

}
