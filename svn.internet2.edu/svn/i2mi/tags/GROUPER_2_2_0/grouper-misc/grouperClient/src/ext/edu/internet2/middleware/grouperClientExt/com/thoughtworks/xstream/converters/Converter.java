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
 * Copyright (C) 2003, 2004 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 26. September 2003 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter implementations are responsible marshalling Java objects
 * to/from textual data.
 * <p/>
 * <p>If an exception occurs during processing, a {@link ConversionException}
 * should be thrown.</p>
 * <p/>
 * <p>If working with the high level {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream} facade,
 * you can register new converters using the XStream.registerConverter() method.</p>
 * <p/>
 * <p>If working with the lower level API, the
 * {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConverterLookup} implementation is
 * responsible for looking up the appropriate converter.</p>
 * <p/>
 * <p>Converters for object that can store all information in a single value
 * should implement {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.SingleValueConverter}.
 * <p>{@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter}
 * provides a starting point.</p>
 * <p/>
 * <p>{@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter}
 * provides a starting point for objects that hold a collection of other objects
 * (such as Lists and Maps).</p>
 *
 * @author Joe Walnes
 * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream
 * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConverterLookup
 * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter
 * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter
 */
public interface Converter extends ConverterMatcher {

    /**
     * Convert an object to textual data.
     *
     * @param source  The object to be marshalled.
     * @param writer  A stream to write to.
     * @param context A context that allows nested objects to be processed by XStream.
     */
    void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context);

    /**
     * Convert textual data back into an object.
     *
     * @param reader  The stream to read the text from.
     * @param context
     * @return The resulting object.
     */
    Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context);

}
