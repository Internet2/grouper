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
    private int count;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Group group_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member member_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Field list_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Group via_id;

    /** full constructor */
    public Membership(int count, Integer version, edu.internet2.middleware.grouper.Group group_id, edu.internet2.middleware.grouper.Member member_id, edu.internet2.middleware.grouper.Field list_id, edu.internet2.middleware.grouper.Group via_id) {
        this.count = count;
        this.version = version;
        this.group_id = group_id;
        this.member_id = member_id;
        this.list_id = list_id;
        this.via_id = via_id;
    }

    /** default constructor */
    public Membership() {
    }

    /** minimal constructor */
    public Membership(int count) {
        this.count = count;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get membership hop count.
     *       
     */
    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /** 
     * Get group.
     *       
     */
    public edu.internet2.middleware.grouper.Group getGroup_id() {
        return this.group_id;
    }

    public void setGroup_id(edu.internet2.middleware.grouper.Group group_id) {
        this.group_id = group_id;
    }

    /** 
     * Get member.
     *       
     */
    public edu.internet2.middleware.grouper.Member getMember_id() {
        return this.member_id;
    }

    public void setMember_id(edu.internet2.middleware.grouper.Member member_id) {
        this.member_id = member_id;
    }

    /** 
     * Get list.
     *       
     */
    public edu.internet2.middleware.grouper.Field getList_id() {
        return this.list_id;
    }

    public void setList_id(edu.internet2.middleware.grouper.Field list_id) {
        this.list_id = list_id;
    }

    /** 
     * Get via group.
     *       
     */
    public edu.internet2.middleware.grouper.Group getVia_id() {
        return this.via_id;
    }

    public void setVia_id(edu.internet2.middleware.grouper.Group via_id) {
        this.via_id = via_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("count", getCount())
            .append("group_id", getGroup_id())
            .append("member_id", getMember_id())
            .append("list_id", getList_id())
            .append("via_id", getVia_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Membership) ) return false;
        Membership castOther = (Membership) other;
        return new EqualsBuilder()
            .append(this.getCount(), castOther.getCount())
            .append(this.getGroup_id(), castOther.getGroup_id())
            .append(this.getMember_id(), castOther.getMember_id())
            .append(this.getList_id(), castOther.getList_id())
            .append(this.getVia_id(), castOther.getVia_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getCount())
            .append(getGroup_id())
            .append(getMember_id())
            .append(getList_id())
            .append(getVia_id())
            .toHashCode();
    }

}
