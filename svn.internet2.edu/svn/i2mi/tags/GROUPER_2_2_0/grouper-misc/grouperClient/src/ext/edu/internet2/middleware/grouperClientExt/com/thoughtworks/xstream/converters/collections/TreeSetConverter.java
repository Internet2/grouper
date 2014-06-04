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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts a java.util.TreeSet to XML, and serializes
 * the associated java.util.Comparator. The converter
 * assumes that the elements in the XML are already sorted 
 * according the comparator.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class TreeSetConverter extends CollectionConverter {

    public TreeSetConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return type.equals(TreeSet.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        TreeSet treeSet = (TreeSet) source;
        Comparator comparator = treeSet.comparator();
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
        final SortedSet sortedSet;
        final TreeSet result;
        if (reader.getNodeName().equals("comparator")) {
            String comparatorClass = reader.getAttribute("class");
            Comparator comparator = (Comparator) context.convertAnother(null, mapper().realClass(comparatorClass));
            sortedSet = new PresortedSet(comparator);
            result = new TreeSet(comparator);
        } else if (reader.getNodeName().equals("no-comparator")) {
            sortedSet = new PresortedSet();
            result = new TreeSet();
        } else {
            throw new ConversionException("TreeSet does not contain <comparator> element");
        }
        reader.moveUp();
        super.populateCollection(reader, context, sortedSet);
        result.addAll(sortedSet); // internal optimization will not call comparator
        return result;
    }

    private static class PresortedSet implements SortedSet {
        private final List list = new ArrayList();
        private final Comparator comparator;

        PresortedSet() {
            this(null);
        }

        PresortedSet(Comparator comparator) {
            this.comparator = comparator;
        }

        public boolean add(Object e) {
            return this.list.add(e);
        }

        public boolean addAll(Collection c) {
            return this.list.addAll(c);
        }

        public void clear() {
            this.list.clear();
        }

        public boolean contains(Object o) {
            return this.list.contains(o);
        }

        public boolean containsAll(Collection c) {
            return this.list.containsAll(c);
        }

        public boolean equals(Object o) {
            return this.list.equals(o);
        }

        public int hashCode() {
            return this.list.hashCode();
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        public Iterator iterator() {
            return this.list.iterator();
        }

        public boolean remove(Object o) {
            return this.list.remove(o);
        }

        public boolean removeAll(Collection c) {
            return this.list.removeAll(c);
        }

        public boolean retainAll(Collection c) {
            return this.list.retainAll(c);
        }

        public int size() {
            return this.list.size();
        }

        public List subList(int fromIndex, int toIndex) {
            return this.list.subList(fromIndex, toIndex);
        }

        public Object[] toArray() {
            return this.list.toArray();
        }

        public Object[] toArray(Object[] a) {
            return this.list.toArray(a);
        }

        public Comparator comparator() {
            return comparator;
        }

        public Object first() {
            return list.isEmpty() ? null : list.get(0);
        }

        public SortedSet headSet(Object toElement) {
            throw new UnsupportedOperationException();
        }

        public Object last() {
            return list.isEmpty() ? null : list.get(list.size() - 1);
        }

        public SortedSet subSet(Object fromElement, Object toElement) {
            throw new UnsupportedOperationException();
        }

        public SortedSet tailSet(Object fromElement) {
            throw new UnsupportedOperationException();
        }
    }
}
