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
 * Copyright (C) 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 24. April 2005 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import java.util.Iterator;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ErrorWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.FastStack;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.AttributeNameIterator;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;

public abstract class AbstractDocumentReader extends AbstractXmlReader implements DocumentReader {

    private FastStack pointers = new FastStack(16);
    private Object current;

    protected AbstractDocumentReader(Object rootElement) {
        this(rootElement, new XmlFriendlyReplacer());
    }

    /**
    * @since 1.2
    */ 
    protected AbstractDocumentReader(Object rootElement, XmlFriendlyReplacer replacer) {
        super(replacer);
        this.current = rootElement;
        pointers.push(new Pointer());
        reassignCurrentElement(current);
    }
    
    protected abstract void reassignCurrentElement(Object current);
    protected abstract Object getParent();
    protected abstract Object getChild(int index);
    protected abstract int getChildCount();

    private static class Pointer {
        public int v;
    }

    public boolean hasMoreChildren() {
        Pointer pointer = (Pointer) pointers.peek();

        if (pointer.v < getChildCount()) {
            return true;
        } else {
            return false;
        }
    }

    public void moveUp() {
        current = getParent();
        pointers.popSilently();
        reassignCurrentElement(current);
    }

    public void moveDown() {
        Pointer pointer = (Pointer) pointers.peek();
        pointers.push(new Pointer());

        current = getChild(pointer.v);

        pointer.v++;
        reassignCurrentElement(current);
    }

    public Iterator getAttributeNames() {
        return new AttributeNameIterator(this);
    }

    public void appendErrors(ErrorWriter errorWriter) {
    }

    /**
     * @deprecated As of 1.2, use {@link #getCurrent() }
     */
    public Object peekUnderlyingNode() {
        return current;
    }
    
    public Object getCurrent() {
        return this.current;
    }

    public void close() {
        // don't need to do anything
    }

    public HierarchicalStreamReader underlyingReader() {
        return this;
    }
}
