/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

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
    public UnionFactor(Integer version, edu.internet2.middleware.grouper.Member node_a_id, edu.internet2.middleware.grouper.Member node_b_id) {
        super(version, node_a_id, node_b_id);
    }

    /** default constructor */
    public UnionFactor() {
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("node_a_id", getNode_a_id())
            .append("node_b_id", getNode_b_id())
            .toString();
    }

}
