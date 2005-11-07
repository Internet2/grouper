package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * A list membership in the Groups Registry.
 * @author blair christensen.
 *     
*/
public class Membership implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String group_id;

    /** persistent field */
    private String member_id;

    /** persistent field */
    private String list_id;

    /** nullable persistent field */
    private String via_id;

    /** persistent field */
    private int depth;

    /** full constructor */
    public Membership(String group_id, String member_id, String list_id, String via_id, int depth) {
        this.group_id = group_id;
        this.member_id = member_id;
        this.list_id = list_id;
        this.via_id = via_id;
        this.depth = depth;
    }

    /** default constructor */
    public Membership() {
    }

    /** minimal constructor */
    public Membership(String group_id, String member_id, String list_id, int depth) {
        this.group_id = group_id;
        this.member_id = member_id;
        this.list_id = list_id;
        this.depth = depth;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getGroup_id() {
        return this.group_id;
    }

    private void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    private String getMember_id() {
        return this.member_id;
    }

    private void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    private String getList_id() {
        return this.list_id;
    }

    private void setList_id(String list_id) {
        this.list_id = list_id;
    }

    private String getVia_id() {
        return this.via_id;
    }

    private void setVia_id(String via_id) {
        this.via_id = via_id;
    }

    private int getDepth() {
        return this.depth;
    }

    private void setDepth(int depth) {
        this.depth = depth;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Membership) ) return false;
        Membership castOther = (Membership) other;
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
