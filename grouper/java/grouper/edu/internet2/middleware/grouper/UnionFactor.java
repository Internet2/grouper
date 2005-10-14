package edu.internet2.middleware.grouper;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Group math factors.
 * @author blair christensen.
 *     
*/
public class UnionFactor extends Factor implements Serializable {

    /** full constructor */
    public UnionFactor(String node_a_id, String node_b_id, Integer version) {
        super(node_a_id, node_b_id, version);
    }

    /** default constructor */
    public UnionFactor() {
    }

    /** minimal constructor */
    public UnionFactor(String node_a_id, String node_b_id) {
      super(node_a_id, node_b_id);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("node_a_id", getNode_a_id())
            .append("node_b_id", getNode_b_id())
            .toString();
    }

}
