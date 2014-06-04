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

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.FastStack;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.AttributeNameIterator;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Base class that contains common functionality across HierarchicalStreamReader implementations
 * that need to read from a pull parser.
 *
 * @author Joe Walnes
 * @author James Strachan
 */
public abstract class AbstractPullReader extends AbstractXmlReader {

    protected static final int START_NODE = 1;
    protected static final int END_NODE = 2;
    protected static final int TEXT = 3;
    protected static final int COMMENT = 4;
    protected static final int OTHER = 0;

    private final FastStack elementStack = new FastStack(16);

    private final FastStack lookahead = new FastStack(4);
    private final FastStack lookback = new FastStack(4);
    private boolean marked;

    private static class Event {
        int type;
        String value;
    }

    /**
     * @since 1.2
     */
    protected AbstractPullReader(XmlFriendlyReplacer replacer) {
        super(replacer);
    }
    

    /**
     * Pull the next event from the stream.
     *
     * <p>This MUST return {@link #START_NODE}, {@link #END_NODE}, {@link #TEXT}, {@link #COMMENT},
     * {@link #OTHER} or throw {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.StreamException}.</p>
     *
     * <p>The underlying pull parser will most likely return its own event types. These must be
     * mapped to the appropriate events.</p>
     */
    protected abstract int pullNextEvent();

    /**
     * Pull the name of the current element from the stream.
     */
    protected abstract String pullElementName();

    /**
     * Pull the contents of the current text node from the stream.
     */
    protected abstract String pullText();

    public boolean hasMoreChildren() {
        mark();
        while (true) {
            switch (readEvent().type) {
                case START_NODE:
                    reset();
                    return true;
                case END_NODE:
                    reset();
                    return false;
                default:
                    continue;
            }
        }
    }

    public void moveDown() {
        int currentDepth = elementStack.size();
        while (elementStack.size() <= currentDepth) {
            move();
            if (elementStack.size() < currentDepth) {
                throw new RuntimeException(); // sanity check
            }
        }
    }

    public void moveUp() {
        int currentDepth = elementStack.size();
        while (elementStack.size() >= currentDepth) {
            move();
        }
    }

    private void move() {
        switch (readEvent().type) {
            case START_NODE:
                elementStack.push(pullElementName());
                break;
            case END_NODE:
                elementStack.pop();
                break;
        }
    }

    private Event readEvent() {
        if (marked) {
            if (lookback.hasStuff()) {
                return (Event) lookahead.push(lookback.pop());
            } else {
                return (Event) lookahead.push(readRealEvent());
            }
        } else {
            if (lookback.hasStuff()) {
                return (Event) lookback.pop();
            } else {
                return readRealEvent();
            }
        }
    }

    private Event readRealEvent() {
        Event event = new Event();
        event.type = pullNextEvent();
        if (event.type == TEXT) {
            event.value = pullText();
        } else if (event.type == START_NODE) {
            event.value = pullElementName();
        }
        return event;
    }

    public void mark() {
        marked = true;
    }

    public void reset() {
        while(lookahead.hasStuff()) {
            lookback.push(lookahead.pop());
        }
        marked = false;
    }

    public String getValue() {
        // we should collapse together any text which
        // contains comments

        // lets only use a string buffer when we get 2 strings
        // to avoid copying strings
        String last = null;
        StringBuffer buffer = null;

        mark();
        Event event = readEvent();
        while (true) {
            if (event.type == TEXT) {
                String text = event.value;
                if (text != null && text.length() > 0) {
                    if (last == null) {
                        last = text;
                    } else {
                        if (buffer == null) {
                            buffer = new StringBuffer(last);
                        }
                        buffer.append(text);
                    }
                }
            } else if (event.type != COMMENT) {
                break;
            }
            event = readEvent();
        }
        reset();
        if (buffer != null) {
            return buffer.toString();
        } else {
            return (last == null) ? "" : last;
        }
    }

    public Iterator getAttributeNames() {
        return new AttributeNameIterator(this);
    }

    public String getNodeName() {
        return unescapeXmlName((String) elementStack.peek());
    }

    public HierarchicalStreamReader underlyingReader() {
        return this;
    }

}
