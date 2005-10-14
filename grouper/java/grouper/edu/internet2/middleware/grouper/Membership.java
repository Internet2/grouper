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
    private String container_id;

    /** persistent field */
    private String member_id;

    /** persistent field */
    private String list_id;

    /** persistent field */
    private String via_id;

    /** persistent field */
    private int count;

    /** nullable persistent field */
    private Integer version;

    /** full constructor */
    public Membership(String container_id, String member_id, String list_id, String via_id, int count, Integer version) {
        this.container_id = container_id;
        this.member_id = member_id;
        this.list_id = list_id;
        this.via_id = via_id;
        this.count = count;
        this.version = version;
    }

    /** default constructor */
    public Membership() {
    }

    /** minimal constructor */
    public Membership(String container_id, String member_id, String list_id, String via_id, int count) {
        this.container_id = container_id;
        this.member_id = member_id;
        this.list_id = list_id;
        this.via_id = via_id;
        this.count = count;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get membership container.
     *       
     */
    public String getContainer_id() {
        return this.container_id;
    }

    public void setContainer_id(String container_id) {
        this.container_id = container_id;
    }

    /** 
     * Get member.
     *       
     */
    public String getMember_id() {
        return this.member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    /** 
     * Get membership list.
     *       
     */
    public String getList_id() {
        return this.list_id;
    }

    public void setList_id(String list_id) {
        this.list_id = list_id;
    }

    /** 
     * Get membership via container.
     *       
     */
    public String getVia_id() {
        return this.via_id;
    }

    public void setVia_id(String via_id) {
        this.via_id = via_id;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("container_id", getContainer_id())
            .append("member_id", getMember_id())
            .append("list_id", getList_id())
            .append("via_id", getVia_id())
            .append("count", getCount())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Membership) ) return false;
        Membership castOther = (Membership) other;
        return new EqualsBuilder()
            .append(this.getContainer_id(), castOther.getContainer_id())
            .append(this.getMember_id(), castOther.getMember_id())
            .append(this.getList_id(), castOther.getList_id())
            .append(this.getVia_id(), castOther.getVia_id())
            .append(this.getCount(), castOther.getCount())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getContainer_id())
            .append(getMember_id())
            .append(getList_id())
            .append(getVia_id())
            .append(getCount())
            .toHashCode();
    }

}
