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

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.WriterWrapper;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.XmlFriendlyWriter;

/**
 * Wrapper for HierarchicalStreamWriter that tracks the path (a subset of XPath) of the current node that is being written.
 *
 * @see PathTracker
 * @see Path
 *
 * @author Joe Walnes
 */
public class PathTrackingWriter extends WriterWrapper {

    private final PathTracker pathTracker;
    private final boolean isXmlFriendly;

    public PathTrackingWriter(HierarchicalStreamWriter writer, PathTracker pathTracker) {
        super(writer);
        this.isXmlFriendly = writer.underlyingWriter() instanceof XmlFriendlyWriter;
        this.pathTracker = pathTracker;
    }

    public void startNode(String name) {
        pathTracker.pushElement(isXmlFriendly ? ((XmlFriendlyWriter)wrapped.underlyingWriter()).escapeXmlName(name) : name);
        super.startNode(name); 
    }

    public void startNode(String name, Class clazz) {
        pathTracker.pushElement(isXmlFriendly ? ((XmlFriendlyWriter)wrapped.underlyingWriter()).escapeXmlName(name) : name);
        super.startNode(name, clazz);
    }

    public void endNode() {
        super.endNode();
        pathTracker.popElement();
    }

}
