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
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. May 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.extended;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;

import java.lang.reflect.Field;

/**
 * Factory for creating StackTraceElements.
 * Factory for creating StackTraceElements.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley (binkley)</a>
 * @author Joe Walnes
 */
public class StackTraceElementFactory {

    public StackTraceElement nativeMethodElement(String declaringClass, String methodName) {
        return create(declaringClass, methodName, "Native Method", -2);
    }

    public StackTraceElement unknownSourceElement(String declaringClass, String methodName) {
        return create(declaringClass, methodName, "Unknown Source", -1);
    }

    public StackTraceElement element(String declaringClass, String methodName, String fileName) {
        return create(declaringClass, methodName, fileName, -1);
    }

    public StackTraceElement element(String declaringClass, String methodName, String fileName, int lineNumber) {
        return create(declaringClass, methodName, fileName, lineNumber);
    }

    private StackTraceElement create(String declaringClass, String methodName, String fileName, int lineNumber) {
        StackTraceElement result = new Throwable().getStackTrace()[0];
        setField(result, "declaringClass", declaringClass);
        setField(result, "methodName", methodName);
        setField(result, "fileName", fileName);
        setField(result, "lineNumber", new Integer(lineNumber));
        return result;
    }

    private void setField(StackTraceElement element, String fieldName, Object value) {
        try {
            final Field field = StackTraceElement.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(element, value);
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

}
