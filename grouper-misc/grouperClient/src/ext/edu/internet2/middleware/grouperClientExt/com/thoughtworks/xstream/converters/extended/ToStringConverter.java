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
 * Created on 07. July 2006 by Mauro Talevi
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.extended;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * Convenient converter for classes with natural string representation.
 * 
 * Converter for classes that adopt the following convention:
 *   - a constructor that takes a single string parameter
 *   - a toString() that is overloaded to issue a string that is meaningful
 *
 * @author Paul Hammant
 */
public class ToStringConverter extends AbstractSingleValueConverter {
    private final Class clazz;
    private final Constructor ctor;

    public ToStringConverter(Class clazz) throws NoSuchMethodException {
        this.clazz = clazz;
        ctor = clazz.getConstructor(new Class[] {String.class});
    }
    public boolean canConvert(Class type) {
        return type.equals(clazz);
    }
    public String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public Object fromString(String str) {
        try {
            return ctor.newInstance(new Object[] {str});
        } catch (InstantiationException e) {
            throw new ConversionException("Unable to instantiate single String param constructor", e);
        } catch (IllegalAccessException e) {
            throw new ConversionException("Unable to access single String param constructor", e);
        } catch (InvocationTargetException e) {
            throw new ConversionException("Unable to target single String param constructor", e.getTargetException());
        }
    }
}
