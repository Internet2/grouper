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
 * Copyright (C) 2003, 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03. October 2003 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts an array of objects or primitives to XML, using
 * a nested child element for each item.
 *
 * @author Joe Walnes
 */
public class ArrayConverter extends AbstractCollectionConverter {

    public ArrayConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return type.isArray();
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        int length = Array.getLength(source);
        for (int i = 0; i < length; i++) {
            Object item = Array.get(source, i);
            writeItem(item, context, writer);
        }

    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // read the items from xml into a list
        List items = new ArrayList();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object item = readItem(reader, context, null); // TODO: arg, what should replace null?
            items.add(item);
            reader.moveUp();
        }
        // now convertAnother the list into an array
        // (this has to be done as a separate list as the array size is not
        //  known until all items have been read)
        Object array = Array.newInstance(context.getRequiredType().getComponentType(), items.size());
        int i = 0;
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            Array.set(array, i++, iterator.next());
        }
        return array;
    }
}
