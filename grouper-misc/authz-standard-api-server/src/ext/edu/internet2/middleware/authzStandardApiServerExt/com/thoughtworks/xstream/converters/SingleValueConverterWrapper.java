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
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 20. February 2006 by Mauro Talevi
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.SingleValueConverter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Wrapper to convert a  {@link edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.SingleValueConverter} into a
 * {@link edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter}.
 *
 * @author J&ouml;rg Schaible
 * @see edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter
 * @see edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.SingleValueConverter
 */
public class SingleValueConverterWrapper implements Converter, SingleValueConverter {

    private final SingleValueConverter wrapped;

    public SingleValueConverterWrapper(SingleValueConverter wrapped) {
        this.wrapped = wrapped;
    }

    public boolean canConvert(Class type) {
        return wrapped.canConvert(type);
    }

    public String toString(Object obj) {
        return wrapped.toString(obj);
    }

    public Object fromString(String str) {
        return wrapped.fromString(str);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.setValue(toString(source));
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return fromString(reader.getValue());
    }

}
