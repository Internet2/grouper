package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * A member within the Groups Registry.
 * @author blair christensen.
 *     
*/
public class Member implements Serializable {

    /** identifier field */
    private String id;

    /** persistent field */
    private String subject_id;

    /** persistent field */
    private String subject_source;

    /** persistent field */
    private String subject_type;

    /** persistent field */
    private String member_id;

    /** full constructor */
    public Member(String subject_id, String subject_source, String subject_type, String member_id) {
        this.subject_id = subject_id;
        this.subject_source = subject_source;
        this.subject_type = subject_type;
        this.member_id = member_id;
    }

    /** default constructor */
    public Member() {
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /** 
     * Get Subject ID.
     *       
     */
    private String getSubject_id() {
        return this.subject_id;
    }

    private void setSubject_id(String subject_id) {
        this.subject_id = subject_id;
    }

    /** 
     * Get Subject source.
     *       
     */
    private String getSubject_source() {
        return this.subject_source;
    }

    private void setSubject_source(String subject_source) {
        this.subject_source = subject_source;
    }

    /** 
     * Get Subject type.
     *       
     */
    private String getSubject_type() {
        return this.subject_type;
    }

    private void setSubject_type(String subject_type) {
        this.subject_type = subject_type;
    }

    /** 
     * Get Member UUID.
     *       
     */
    private String getMember_id() {
        return this.member_id;
    }

    private void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("subject_id", getSubject_id())
            .append("subject_source", getSubject_source())
            .append("subject_type", getSubject_type())
            .append("member_id", getMember_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Member) ) return false;
        Member castOther = (Member) other;
        return new EqualsBuilder()
            .append(this.getSubject_id(), castOther.getSubject_id())
            .append(this.getSubject_source(), castOther.getSubject_source())
            .append(this.getSubject_type(), castOther.getSubject_type())
            .append(this.getMember_id(), castOther.getMember_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getSubject_id())
            .append(getSubject_source())
            .append(getSubject_type())
            .append(getMember_id())
            .toHashCode();
    }

}
