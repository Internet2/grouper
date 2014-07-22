/**
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
 */
/*
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 13. April 2006 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Field;

/**
 * A wrapper implementation for the ReflectionProvider.
 *
 * @author J&ouml;rg Schaible
 * @since 1.2
 */
public class ReflectionProviderWrapper implements ReflectionProvider {

    final protected ReflectionProvider wrapped;

    public ReflectionProviderWrapper(ReflectionProvider wrapper) {
        this.wrapped = wrapper;
    }

    public boolean fieldDefinedInClass(String fieldName, Class type) {
        return this.wrapped.fieldDefinedInClass(fieldName, type);
    }

    public Field getField(Class definedIn, String fieldName) {
        return this.wrapped.getField(definedIn, fieldName);
    }

    public Class getFieldType(Object object, String fieldName, Class definedIn) {
        return this.wrapped.getFieldType(object, fieldName, definedIn);
    }

    public Object newInstance(Class type) {
        return this.wrapped.newInstance(type);
    }

    public void visitSerializableFields(Object object, Visitor visitor) {
        this.wrapped.visitSerializableFields(object, visitor);
    }

    public void writeField(Object object, String fieldName, Object value, Class definedIn) {
        this.wrapped.writeField(object, fieldName, value, definedIn);
    }

}
