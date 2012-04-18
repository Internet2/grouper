/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright 2002-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.parser;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlContext;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.util.Coercion;

/**
 * LT : a < b.
 * 
 * Follows A.3.6.1 of the JSTL 1.0 specification
 * 
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:proyal@apache.org">Peter Royal</a>
 * @version $Id: ASTLTNode.java,v 1.1 2008-11-30 10:57:25 mchyzer Exp $
 */
public class ASTLTNode extends SimpleNode {
    /**
     * Create the node given an id.
     * 
     * @param id node id.
     */
    public ASTLTNode(int id) {
        super(id);
    }

    /**
     * Create a node with the given parser and id.
     * 
     * @param p a parser.
     * @param id node id.
     */
    public ASTLTNode(Parser p, int id) {
        super(p, id);
    }

    /** {@inheritDoc} */
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    /** {@inheritDoc} */
    public Object value(JexlContext jc) throws Exception {
        /*
         * now get the values
         */

        Object left = ((SimpleNode) jjtGetChild(0)).value(jc);
        Object right = ((SimpleNode) jjtGetChild(1)).value(jc);

        if ((left == right) || (left == null) || (right == null)) {
            return Boolean.FALSE;
        } else if (Coercion.isFloatingPoint(left)
                || Coercion.isFloatingPoint(right)) {
            double leftDouble = Coercion.coerceDouble(left).doubleValue();
            double rightDouble = Coercion.coerceDouble(right).doubleValue();

            return leftDouble < rightDouble ? Boolean.TRUE : Boolean.FALSE;
        } else if (Coercion.isNumberable(left) || Coercion.isNumberable(right)) {
            long leftLong = Coercion.coerceLong(left).longValue();
            long rightLong = Coercion.coerceLong(right).longValue();

            return leftLong < rightLong ? Boolean.TRUE : Boolean.FALSE;
        } else if (left instanceof String || right instanceof String) {
            String leftString = left.toString();
            String rightString = right.toString();

            return leftString.compareTo(rightString) < 0 ? Boolean.TRUE
                    : Boolean.FALSE;
        } else if (left instanceof Comparable) {
            return ((Comparable) left).compareTo(right) < 0 ? Boolean.TRUE
                    : Boolean.FALSE;
        } else if (right instanceof Comparable) {
            return ((Comparable) right).compareTo(left) > 0 ? Boolean.TRUE
                    : Boolean.FALSE;
        }

        throw new Exception("Invalid comparison : LT ");
    }
}
