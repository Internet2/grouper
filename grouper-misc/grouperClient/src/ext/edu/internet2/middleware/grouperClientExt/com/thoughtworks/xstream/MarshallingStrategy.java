/*
 * Copyright (C) 2004, 2006 Joe Walnes.
 * Copyright (C) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 16. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.alias.ClassMapper;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.DataHolder;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.DefaultConverterLookup;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

public interface MarshallingStrategy {

    Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder, ConverterLookup converterLookup, Mapper mapper);
    void marshal(HierarchicalStreamWriter writer, Object obj, ConverterLookup converterLookup, Mapper mapper, DataHolder dataHolder);
    
    /**
     * @deprecated As of 1.2, use {@link #unmarshal(Object, HierarchicalStreamReader, DataHolder, ConverterLookup, Mapper)}
     */
    Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder, DefaultConverterLookup converterLookup, ClassMapper classMapper);

    /**
     * @deprecated As of 1.2, use {@link #marshal(HierarchicalStreamWriter, Object, ConverterLookup, Mapper, DataHolder)}
     */
    void marshal(HierarchicalStreamWriter writer, Object obj, DefaultConverterLookup converterLookup, ClassMapper classMapper, DataHolder dataHolder);

}
