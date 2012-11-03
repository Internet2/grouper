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
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.alias.ClassMapper;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.ConverterRegistry;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.core.util.PrioritizedList;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * The default implementation of converters lookup.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Guilherme Silveira
 */
public class DefaultConverterLookup implements ConverterLookup, ConverterRegistry {

    private final PrioritizedList converters = new PrioritizedList();
    private transient Map typeToConverterMap = Collections.synchronizedMap(new HashMap());

    public DefaultConverterLookup() {
    }

    /**
     * @deprecated since 1.3, use {@link #DefaultConverterLookup()}
     */
    public DefaultConverterLookup(Mapper mapper) {
    }

    /**
     * @deprecated since 1.2, use {@link #DefaultConverterLookup(Mapper)}
     */
    public DefaultConverterLookup(ClassMapper classMapper) {
    }

    public Converter lookupConverterForType(Class type) {
        Converter cachedConverter = (Converter) typeToConverterMap.get(type);
        if (cachedConverter != null) return cachedConverter;
        Iterator iterator = converters.iterator();
        while (iterator.hasNext()) {
            Converter converter = (Converter) iterator.next();
            if (converter.canConvert(type)) {
                typeToConverterMap.put(type, converter);
                return converter;
            }
        }
        throw new ConversionException("No converter specified for " + type);
    }
    
    public void registerConverter(Converter converter, int priority) {
        converters.add(converter, priority);
        for (Iterator iter = this.typeToConverterMap.keySet().iterator(); iter.hasNext();) {
            Class type = (Class) iter.next();
            if (converter.canConvert(type)) {
                iter.remove();
            }
        }
    }
    
    private Object readResolve() {
        typeToConverterMap = Collections.synchronizedMap(new HashMap());
        return this;
    }

}
