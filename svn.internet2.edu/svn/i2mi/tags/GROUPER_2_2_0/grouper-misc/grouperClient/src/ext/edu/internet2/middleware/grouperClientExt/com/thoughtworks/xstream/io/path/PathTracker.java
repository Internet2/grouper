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
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.path;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the current {@link Path} as a stream is moved through.
 *
 * <p>Can be linked to a <a href="../HierarchicalStreamWriter.html">HierarchicalStreamWriter</a> or
 * <a href="../HierarchicalStreamReader.html">HierarchicalStreamReader</a> by wrapping them with a
 * <a href="PathTrackingWriter.html">PathTrackingWriter</a> or
 * <a href="PathTrackingReader.html">PathTrackingReader</a>.</p>
 *
 * <h3>Example</h3>
 *
 * <pre>
 * PathTracker tracker = new PathTracker();
 * tracker.pushElement("table");
 * tracker.pushElement("tr");
 * tracker.pushElement("td");
 * tracker.pushElement("form");
 * tracker.popElement("form");
 * tracker.popElement("td");
 * tracker.pushElement("td");
 * tracker.pushElement("div");
 *
 * Path path = tracker.getPath(); // returns "/table/tr/td[2]/div"
 * </pre>
 *
 * @see Path
 * @see PathTrackingReader
 * @see PathTrackingWriter
 *
 * @author Joe Walnes
 */
public class PathTracker {

    private int pointer;
    private int capacity;
    private String[] pathStack;
    private Map[] indexMapStack;

    private Path currentPath;

    public PathTracker() {
        this(16);
    }

    /**
     * @param initialCapacity Size of the initial stack of nodes (one level per depth in the tree). Note that this is
     *                        only for optimizations - the stack will resize itself if it exceeds its capacity. If in doubt,
     *                        use the other constructor.
     */
    public PathTracker(int initialCapacity) {
        this.capacity = Math.max(1, initialCapacity);
        pathStack = new String[capacity];
        indexMapStack = new Map[capacity];
    }

    /**
     * Notify the tracker that the stream has moved into a new element.
     *
     * @param name Name of the element
     */
    public void pushElement(String name) {
        if (pointer + 1 >= capacity) {
            resizeStacks(capacity * 2);
        }
        pathStack[pointer] = name;
        Map indexMap = indexMapStack[pointer];
        if (indexMap == null) {
            indexMap = new HashMap();
            indexMapStack[pointer] = indexMap;
        }
        if (indexMap.containsKey(name)) {
            indexMap.put(name, new Integer(((Integer) indexMap.get(name)).intValue() + 1));
        } else {
            indexMap.put(name, new Integer(1));
        }
        pointer++;
        currentPath = null;
    }

    /**
     * Notify the tracker that the stream has moved out of an element.
     */
    public void popElement() {
        indexMapStack[pointer] = null;
        currentPath = null;
        pointer--;
    }

    /**
     * @deprecated Use {@link #getPath()} instead.
     */
    public String getCurrentPath() {
        return getPath().toString();
    }

    private void resizeStacks(int newCapacity) {
        String[] newPathStack = new String[newCapacity];
        Map[] newIndexMapStack = new Map[newCapacity];
        int min = Math.min(capacity, newCapacity);
        System.arraycopy(pathStack, 0, newPathStack, 0, min);
        System.arraycopy(indexMapStack, 0, newIndexMapStack, 0, min);
        pathStack = newPathStack;
        indexMapStack = newIndexMapStack;
        capacity = newCapacity;
    }

    /**
     * Current Path in stream.
     */
    public Path getPath() {
        if (currentPath == null) {
            String[] chunks = new String[pointer + 1];
            chunks[0] = "";
            for (int i = 0; i < pointer; i++) {
                Integer integer = ((Integer) indexMapStack[i].get(pathStack[i]));
                int index = integer.intValue();
                if (index > 1) {
                    StringBuffer chunk = new StringBuffer(pathStack[i].length() + 6);
                    chunk.append(pathStack[i]).append('[').append(index).append(']');
                    chunks[i + 1] = chunk.toString();
                } else {
                    chunks[i + 1] = pathStack[i];
                }
            }
            currentPath = new Path(chunks);
        }
        return currentPath;
    }
}
