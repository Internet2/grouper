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
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. January 2005 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.InitializationException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.alias.ClassMapper;


/**
 * Mapper that resolves default implementations of classes. For example,
 * mapper.serializedClass(ArrayList.class) will return java.util.List. Calling
 * mapper.defaultImplementationOf(List.class) will return ArrayList.
 * 
 * @author Joe Walnes
 */
public class DefaultImplementationsMapper extends MapperWrapper {

    private final Map typeToImpl = new HashMap();
    private transient Map implToType = new HashMap();

    public DefaultImplementationsMapper(Mapper wrapped) {
        super(wrapped);
        addDefaults();
    }

    /**
     * @deprecated As of 1.2, use {@link #DefaultImplementationsMapper(Mapper)}
     */
    public DefaultImplementationsMapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    protected void addDefaults() {
        // null handling
        addDefaultImplementation(null, Mapper.Null.class);
        // register primitive types
        addDefaultImplementation(Boolean.class, boolean.class);
        addDefaultImplementation(Character.class, char.class);
        addDefaultImplementation(Integer.class, int.class);
        addDefaultImplementation(Float.class, float.class);
        addDefaultImplementation(Double.class, double.class);
        addDefaultImplementation(Short.class, short.class);
        addDefaultImplementation(Byte.class, byte.class);
        addDefaultImplementation(Long.class, long.class);
    }

    public void addDefaultImplementation(Class defaultImplementation, Class ofType) {
        if (defaultImplementation != null && defaultImplementation.isInterface()) {
            throw new InitializationException(
                "Default implementation is not a concrete class: "
                    + defaultImplementation.getName());
        }
        typeToImpl.put(ofType, defaultImplementation);
        implToType.put(defaultImplementation, ofType);
    }

    public String serializedClass(Class type) {
        Class baseType = (Class)implToType.get(type);
        return baseType == null ? super.serializedClass(type) : super.serializedClass(baseType);
    }

    public Class defaultImplementationOf(Class type) {
        if (typeToImpl.containsKey(type)) {
            return (Class)typeToImpl.get(type);
        } else {
            return super.defaultImplementationOf(type);
        }
    }

    private Object readResolve() {
        implToType = new HashMap();
        for (final Iterator iter = typeToImpl.keySet().iterator(); iter.hasNext();) {
            final Object type = iter.next();
            implToType.put(typeToImpl.get(type), type);
        }
        return this;
    }

}
