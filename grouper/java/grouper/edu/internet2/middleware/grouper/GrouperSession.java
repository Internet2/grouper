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
    private String session_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member member_id;

    /** full constructor */
    public GrouperSession(Date start_time, String session_id, edu.internet2.middleware.grouper.Member member_id) {
        this.start_time = start_time;
        this.session_id = session_id;
        this.member_id = member_id;
    }

    /** default constructor */
    public GrouperSession() {
    }

    /** minimal constructor */
    public GrouperSession(Date start_time, String session_id) {
        this.start_time = start_time;
        this.session_id = session_id;
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get start time.
     *       
     */
    private Date getStart_time() {
        return this.start_time;
    }

    private void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    private String getSession_id() {
        return this.session_id;
    }

    private void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    /** 
     * Get member.
     *       
     */
    private edu.internet2.middleware.grouper.Member getMember_id() {
        return this.member_id;
    }

    private void setMember_id(edu.internet2.middleware.grouper.Member member_id) {
        this.member_id = member_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("member_id", getMember_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof GrouperSession) ) return false;
        GrouperSession castOther = (GrouperSession) other;
        return new EqualsBuilder()
            .append(this.getMember_id(), castOther.getMember_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getMember_id())
            .toHashCode();
    }

}
