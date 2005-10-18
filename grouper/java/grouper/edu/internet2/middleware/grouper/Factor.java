package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Group math factors.
 * @author blair christensen.
 *     
*/
public class Factor implements Serializable {

    /** identifier field */
    private String id;

    /** nullable persistent field */
    private Integer version;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member node_a_id;

    /** nullable persistent field */
    private edu.internet2.middleware.grouper.Member node_b_id;

    /** full constructor */
    public Factor(Integer version, edu.internet2.middleware.grouper.Member node_a_id, edu.internet2.middleware.grouper.Member node_b_id) {
        this.version = version;
        this.node_a_id = node_a_id;
        this.node_b_id = node_b_id;
    }

    /** default constructor */
    public Factor() {
    }

    private String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    private Integer getVersion() {
        return this.version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    /** 
     * Get node a.
     *       
     */
    protected edu.internet2.middleware.grouper.Member getNode_a_id() {
        return this.node_a_id;
    }

    private void setNode_a_id(edu.internet2.middleware.grouper.Member node_a_id) {
        this.node_a_id = node_a_id;
    }

    /** 
     * Get node b.
     *       
     */
    protected edu.internet2.middleware.grouper.Member getNode_b_id() {
        return this.node_b_id;
    }

    private void setNode_b_id(edu.internet2.middleware.grouper.Member node_b_id) {
        this.node_b_id = node_b_id;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("node_a_id", getNode_a_id())
            .append("node_b_id", getNode_b_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Factor) ) return false;
        Factor castOther = (Factor) other;
        return new EqualsBuilder()
            .append(this.getNode_a_id(), castOther.getNode_a_id())
            .append(this.getNode_b_id(), castOther.getNode_b_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getNode_a_id())
            .append(getNode_b_id())
            .toHashCode();
    }

}
