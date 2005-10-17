package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Grouper API session.
 * @author blair christensen.
 *     
*/
public class GrouperSession implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private Date start_time;

    /** persistent field */
    private String uuid;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member member_id;

    /** full constructor */
    public GrouperSession(Date start_time, String uuid, Integer version, edu.internet2.middleware.grouper.Member member_id) {
        this.start_time = start_time;
        this.uuid = uuid;
        this.version = version;
        this.member_id = member_id;
    }

    /** default constructor */
    public GrouperSession() {
    }

    /** minimal constructor */
    public GrouperSession(Date start_time, String uuid) {
        this.start_time = start_time;
        this.uuid = uuid;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get start time.
     *       
     */
    public Date getStart_time() {
        return this.start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
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
     * Get member.
     *       
     */
    public edu.internet2.middleware.grouper.Member getMember_id() {
        return this.member_id;
    }

    public void setMember_id(edu.internet2.middleware.grouper.Member member_id) {
        this.member_id = member_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("uuid", getUuid())
            .append("member_id", getMember_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof GrouperSession) ) return false;
        GrouperSession castOther = (GrouperSession) other;
        return new EqualsBuilder()
            .append(this.getUuid(), castOther.getUuid())
            .append(this.getMember_id(), castOther.getMember_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUuid())
            .append(getMember_id())
            .toHashCode();
    }

}
