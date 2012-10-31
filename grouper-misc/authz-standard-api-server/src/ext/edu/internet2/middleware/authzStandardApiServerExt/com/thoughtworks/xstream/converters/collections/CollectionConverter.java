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
 * Created on 01. October 2003 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.JVM;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts most common Collections (Lists and Sets) to XML, specifying a nested
 * element for each item.
 * <p/>
 * <p>Supports java.util.ArrayList, java.util.HashSet,
 * java.util.LinkedList, java.util.Vector and java.util.LinkedHashSet.</p>
 *
 * @author Joe Walnes
 */
public class CollectionConverter extends AbstractCollectionConverter {

    public CollectionConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return type.equals(ArrayList.class)
                || type.equals(HashSet.class)
                || type.equals(LinkedList.class)
                || type.equals(Vector.class)
                || (JVM.is14() && type.getName().equals("java.util.LinkedHashSet"));
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Collection collection = (Collection) source;
        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
            Object item = iterator.next();
            writeItem(item, context, writer);
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Collection collection = (Collection) createCollection(context.getRequiredType());
        populateCollection(reader, context, collection);
        return collection;
    }

    protected void populateCollection(HierarchicalStreamReader reader, UnmarshallingContext context, Collection collection) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object item = readItem(reader, context, collection);
            collection.add(item);
            reader.moveUp();
        }
    }

}
