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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.parser;

import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.DebugInfo;
import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.JexlInfo;

/**
 * Base class for parser nodes - holds an 'image' of the token for later use.
 *
 * @since 2.0
 */
public abstract class JexlNode extends SimpleNode implements JexlInfo {
    /** A marker interface for literals.
     * @param <T> the literal type
     */
    public interface Literal<T> {
        T getLiteral();
    }
    /** token value. */
    public String image;

    public JexlNode(int id) {
        super(id);
    }

    public JexlNode(Parser p, int id) {
        super(p, id);
    }

    /** {@inheritDoc} */
    public DebugInfo debugInfo() {
        JexlNode node = this;
        while (node != null) {
            if (node.value instanceof DebugInfo) {
                return (DebugInfo) node.value;
            }
            node = node.jjtGetParent();
        }
        return null;
    }

    /** {@inheritDoc} */
    public String debugString() {
        DebugInfo info = debugInfo();
        return info != null ? info.debugString() : "";
    }

    /**
     * Whether this node is a constant node
     * Its value can not change after the first evaluation and can be cached indefinitely.
     * @return true if constant, false otherwise
     */
    public final boolean isConstant() {
        return isConstant(this instanceof JexlNode.Literal<?>);
    }

    protected boolean isConstant(boolean literal) {
        if (literal) {
            if (children != null) {
                for (JexlNode child : children) {
                    if (child instanceof ASTReference) {
                        boolean is = child.isConstant(true);
                        if (!is) {
                            return false;
                        }
                    } else if (child instanceof ASTMapEntry) {
                        boolean is = child.isConstant(true);
                        if (!is) {
                            return false;
                        }
                    } else if (!child.isConstant()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
