/**
 * Copyright 2014 Internet2
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
 */
/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 29. May 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.extended;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for Throwable (and Exception) that retains stack trace, for JDK1.4 only.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley (binkley)</a>
 * @author Joe Walnes
 */
public class ThrowableConverter implements Converter {
    
    private Converter defaultConverter;

    public ThrowableConverter(Converter defaultConverter) {
        this.defaultConverter = defaultConverter;
    }

    public boolean canConvert(final Class type) {
        return Throwable.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Throwable throwable = (Throwable) source;
        if (throwable.getCause() == null) {
            try {
                throwable.initCause(null);
            } catch (IllegalStateException e) {
                // ignore, initCause failed, cause was already set
            }
        }
        throwable.getStackTrace(); // Force stackTrace field to be lazy loaded by special JVM native witchcraft (outside our control).
        defaultConverter.marshal(throwable, writer, context);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return defaultConverter.unmarshal(reader, context);
    }
}
