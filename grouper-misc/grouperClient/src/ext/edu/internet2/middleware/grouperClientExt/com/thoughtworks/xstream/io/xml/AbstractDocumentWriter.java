/**
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
 */
/*
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 18. October 2007 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.FastStack;


/**
 * A generic {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter} for DOM writer
 * implementations. The implementation manages a list of top level DOM nodes. Every time the
 * last node is closed on the node stack, the next started node is added to the list. This list
 * can be retrieved using the {@link DocumentWriter#getTopLevelNodes()} method.
 * 
 * @author Laurent Bihanic
 * @author J&ouml;rg Schaible
 * @since 1.2.1
 */
public abstract class AbstractDocumentWriter extends AbstractXmlWriter implements DocumentWriter {

    private final List result = new ArrayList();
    private final FastStack nodeStack = new FastStack(16);

    /**
     * Constructs an AbstractDocumentWriter.
     * 
     * @param container the top level container for the nodes to create (may be
     *            <code>null</code>)
     * @param replacer the object that creates XML-friendly names
     * @since 1.2.1
     */
    public AbstractDocumentWriter(final Object container, final XmlFriendlyReplacer replacer) {
        super(replacer);
        if (container != null) {
            nodeStack.push(container);
            result.add(container);
        }
    }

    public final void startNode(final String name) {
        final Object node = createNode(name);
        nodeStack.push(node);
    }

    /**
     * Create a node. The provided node name is not yet XML friendly. If {@link #getCurrent()}
     * returns <code>null</code> the node is a top level node.
     * 
     * @param name the node name
     * @return the new node
     * @since 1.2.1
     */
    protected abstract Object createNode(String name);

    public final void endNode() {
        endNodeInternally();
        final Object node = nodeStack.pop();
        if (nodeStack.size() == 0) {
            result.add(node);
        }
    }

    /**
     * Called when a node ends. Hook for derived implementations.
     * 
     * @since 1.2.1
     */
    public void endNodeInternally() {
    }

    /**
     * @since 1.2.1
     */
    protected final Object getCurrent() {
        return nodeStack.peek();
    }

    public List getTopLevelNodes() {
        return result;
    }

    public void flush() {
        // don't need to do anything
    }

    public void close() {
        // don't need to do anything
    }
}
