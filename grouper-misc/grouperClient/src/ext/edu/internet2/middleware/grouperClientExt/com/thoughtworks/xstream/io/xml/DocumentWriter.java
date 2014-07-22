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

import java.util.List;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * A generic interface for all {@link HierarchicalStreamWriter} implementations generating a
 * DOM.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.2.1
 */
public interface DocumentWriter extends HierarchicalStreamWriter {

    /**
     * Retrieve a {@link List} with the top elements. In the standard use case this list will
     * only contain a single element. Additional elements can only occur, if
     * {@link HierarchicalStreamWriter#startNode(String)} of the implementing
     * {@link HierarchicalStreamWriter} was called multiple times with an empty node stack. Such
     * a situation occurs calling
     * {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream#marshal(Object, HierarchicalStreamWriter)}
     * multiple times directly.
     * 
     * @return a {@link List} with top nodes
     * @since 1.2.1
     */
    List getTopLevelNodes();
}
