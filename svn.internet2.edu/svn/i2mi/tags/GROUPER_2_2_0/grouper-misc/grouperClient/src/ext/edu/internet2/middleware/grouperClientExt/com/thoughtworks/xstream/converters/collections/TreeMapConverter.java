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
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 08. May 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts a java.util.TreeMap to XML, and serializes
 * the associated java.util.Comparator. The converter
 * assumes that the entries in the XML are already sorted 
 * according the comparator.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class TreeMapConverter extends MapConverter {

    public TreeMapConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return type.equals(TreeMap.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        TreeMap treeMap = (TreeMap) source;
        Comparator comparator = treeMap.comparator();
        if (comparator == null) {
            writer.startNode("no-comparator");
            writer.endNode();
        } else {
            writer.startNode("comparator");
            writer.addAttribute("class", mapper().serializedClass(comparator.getClass()));
            context.convertAnother(comparator);
            writer.endNode();
        }
        super.marshal(source, writer, context);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveDown();
        SortedMap sortedMap;
        TreeMap result;
        if (reader.getNodeName().equals("comparator")) {
            String comparatorClass = reader.getAttribute("class");
            Comparator comparator = (Comparator) context.convertAnother(null, mapper().realClass(comparatorClass));
            sortedMap = new PresortedMap(comparator);
            result = new TreeMap(comparator);
        } else if (reader.getNodeName().equals("no-comparator")) {
            sortedMap = new PresortedMap();
            result = new TreeMap();
        } else {
            throw new ConversionException("TreeMap does not contain <comparator> element");
        }
        reader.moveUp();
        super.populateMap(reader, context, sortedMap);
        result.putAll(sortedMap); //  // internal optimization will not call comparator
        return result;
    }
    
    private static class PresortedMap implements SortedMap {

        private static class ArraySet extends ArrayList implements Set {
        }

        private final ArraySet set = new ArraySet();
        private final Comparator comparator;
        
        PresortedMap() {
            this(null);
        }

        PresortedMap(Comparator comparator) {
            this.comparator = comparator;
        }

        public Comparator comparator() {
            return comparator;
        }

        public Set entrySet() {
            return set;
        }

        public Object firstKey() {
            throw new UnsupportedOperationException();
        }

        public SortedMap headMap(Object toKey) {
            throw new UnsupportedOperationException();
        }

        public Set keySet() {
            Set keySet = new ArraySet();
            for (final Iterator iterator = set.iterator(); iterator.hasNext();) {
                final Entry entry = (Entry)iterator.next();
                keySet.add(entry.getKey());
            }
            return keySet;
        }

        public Object lastKey() {
            throw new UnsupportedOperationException();
        }

        public SortedMap subMap(Object fromKey, Object toKey) {
            throw new UnsupportedOperationException();
        }

        public SortedMap tailMap(Object fromKey) {
            throw new UnsupportedOperationException();
        }

        public Collection values() {
            Set values = new ArraySet();
            for (final Iterator iterator = set.iterator(); iterator.hasNext();) {
                final Entry entry = (Entry)iterator.next();
                values.add(entry.getValue());
            }
            return values;
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            return false;
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public Object get(Object key) {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            return set.isEmpty();
        }

        public Object put(final Object key, final Object value) {
            set.add(new Entry(){

                public Object getKey() {
                    return key;
                }

                public Object getValue() {
                    return value;
                }

                public Object setValue(Object value) {
                    throw new UnsupportedOperationException();
                }});
            return null;
        }

        public void putAll(Map m) {
            for (final Iterator iter = m.entrySet().iterator(); iter.hasNext();) {
                set.add(iter.next());
            }
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return set.size();
        }
        
    }
}
