package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Group math factors.
 * @author blair christensen.
 *     
*/
public class IntersectionFactor extends Factor implements Serializable {

    /** full constructor */
    public IntersectionFactor(Integer version, edu.internet2.middleware.grouper.Member node_a_id, edu.internet2.middleware.grouper.Member node_b_id) {
        super(version, node_a_id, node_b_id);
    }

    /** default constructor */
    public IntersectionFactor() {
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("node_a_id", getNode_a_id())
            .append("node_b_id", getNode_b_id())
            .toString();
    }

}
