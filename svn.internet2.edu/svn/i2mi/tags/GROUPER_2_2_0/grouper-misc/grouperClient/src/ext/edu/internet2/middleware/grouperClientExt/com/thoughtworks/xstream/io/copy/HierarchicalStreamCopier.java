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
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 04. June 2006 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.copy;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Tool for copying the contents of one HierarichalStreamReader to a HierarichalStreamWriter.
 * <p/>
 * This is useful for transforming the output of one format to another (e.g. binary to XML)
 * without needing to know details about the classes and avoiding the overhead of serialization.
 *
 * <h3>Example</h3>
 * <pre>
 * HierarchicalStreamReader reader = new BinaryStreamReader(someBinaryInput);
 * HierarchicalStreamWriter writer = new PrettyPrintWriter(someXmlOutput);
 * HierarchicalStreamCopier copier = new HierarchicalStreamCopier();
 * copier.copy(reader, writer);
 * </pre>
 *
 * @author Joe Walnes
 * @since 1.2
 */
public class HierarchicalStreamCopier {
    public void copy(HierarchicalStreamReader source, HierarchicalStreamWriter destination) {
        destination.startNode(source.getNodeName());
        int attributeCount = source.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            destination.addAttribute(source.getAttributeName(i), source.getAttribute(i));
        }
        String value = source.getValue();
        if (value != null && value.length() > 0) {
            destination.setValue(value);
        }
        while (source.hasMoreChildren()) {
            source.moveDown();
            copy(source, destination);
            source.moveUp();
        }
        destination.endNode();
    }
}
