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
 * Created on 15. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core;

import java.util.Iterator;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.MapBackedDataHolder;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.alias.ClassMapper;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.DataHolder;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.ErrorWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.util.FastStack;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.util.PrioritizedList;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper.Mapper;


public class TreeUnmarshaller implements UnmarshallingContext {

    private Object root;
    protected HierarchicalStreamReader reader;
    private ConverterLookup converterLookup;
    private Mapper mapper;
    private FastStack types = new FastStack(16);
    private DataHolder dataHolder;
    private final PrioritizedList validationList = new PrioritizedList();

    public TreeUnmarshaller(
        Object root, HierarchicalStreamReader reader, ConverterLookup converterLookup,
        Mapper mapper) {
        this.root = root;
        this.reader = reader;
        this.converterLookup = converterLookup;
        this.mapper = mapper;
    }

    /**
     * @deprecated As of 1.2, use
     *             {@link #TreeUnmarshaller(Object, HierarchicalStreamReader, ConverterLookup, Mapper)}
     */
    public TreeUnmarshaller(
        Object root, HierarchicalStreamReader reader, ConverterLookup converterLookup,
        ClassMapper classMapper) {
        this(root, reader, converterLookup, (Mapper)classMapper);
    }

    public Object convertAnother(Object parent, Class type) {
        return convertAnother(parent, type, null);
    }

    public Object convertAnother(Object parent, Class type, Converter converter) {
        type = mapper.defaultImplementationOf(type);
        if (converter == null) {
            converter = converterLookup.lookupConverterForType(type);
        } else {
            if (!converter.canConvert(type)) {
                ConversionException e = new ConversionException(
                    "Explicit selected converter cannot handle type");
                e.add("item-type", type.getName());
                e.add("converter-type", converter.getClass().getName());
                throw e;
            }
        }
        return convert(parent, type, converter);
    }

    protected Object convert(Object parent, Class type, Converter converter) {
        try {
            types.push(type);
            Object result = converter.unmarshal(reader, this);
            types.popSilently();
            return result;
        } catch (ConversionException conversionException) {
            addInformationTo(conversionException, type);
            throw conversionException;
        } catch (RuntimeException e) {
            ConversionException conversionException = new ConversionException(e);
            addInformationTo(conversionException, type);
            throw conversionException;
        }
    }

    private void addInformationTo(ErrorWriter errorWriter, Class type) {
        errorWriter.add("class", type.getName());
        errorWriter.add("required-type", getRequiredType().getName());
        reader.appendErrors(errorWriter);
    }

    public void addCompletionCallback(Runnable work, int priority) {
        validationList.add(work, priority);
    }

    public Object currentObject() {
        return types.size() == 1 ? root : null;
    }

    public Class getRequiredType() {
        return (Class)types.peek();
    }

    public Object get(Object key) {
        lazilyCreateDataHolder();
        return dataHolder.get(key);
    }

    public void put(Object key, Object value) {
        lazilyCreateDataHolder();
        dataHolder.put(key, value);
    }

    public Iterator keys() {
        lazilyCreateDataHolder();
        return dataHolder.keys();
    }

    private void lazilyCreateDataHolder() {
        if (dataHolder == null) {
            dataHolder = new MapBackedDataHolder();
        }
    }

    public Object start(DataHolder dataHolder) {
        this.dataHolder = dataHolder;
        String classAttribute = reader.getAttribute(mapper.aliasForAttribute("class"));
        Class type;
        if (classAttribute == null) {
            type = mapper.realClass(reader.getNodeName());
        } else {
            type = mapper.realClass(classAttribute);
        }
        Object result = convertAnother(null, type);
        Iterator validations = validationList.iterator();
        while (validations.hasNext()) {
            Runnable runnable = (Runnable)validations.next();
            runnable.run();
        }
        return result;
    }

    protected Mapper getMapper() {
        return this.mapper;
    }

}
