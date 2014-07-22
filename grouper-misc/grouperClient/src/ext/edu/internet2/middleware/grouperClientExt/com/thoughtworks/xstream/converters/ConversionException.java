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
 * Copyright (C) 2003, 2004, 2005 Joe Walnes.
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

import java.util.Iterator;
import java.util.Map;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStreamException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.OrderRetainingMap;

/**
 * Thrown by {@link Converter} implementations when they cannot convert an object
 * to/from textual data.
 *
 * When this exception is thrown it can be passed around to things that accept an
 * {@link ErrorWriter}, allowing them to add diagnostics to the stack trace.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 *
 * @see ErrorWriter
 */
public class ConversionException extends XStreamException implements ErrorWriter {

    private static final String SEPARATOR = "\n-------------------------------";
    private Map stuff = new OrderRetainingMap();

    public ConversionException(String msg, Throwable cause) {
        super(msg, cause);
        if (msg != null) {
            add("message", msg);
        }
        if (cause != null) {
            add("cause-exception", cause.getClass().getName());
            add("cause-message", cause instanceof ConversionException ? ((ConversionException)cause).getShortMessage() :  cause.getMessage());
        }
    }

    public ConversionException(String msg) {
        super(msg);
    }

    public ConversionException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public String get(String errorKey) {
        return (String) stuff.get(errorKey);
    }

    public void add(String name, String information) {
        stuff.put(name, information);
    }

    public Iterator keys() {
        return stuff.keySet().iterator();
    }

    public String getMessage() {
        StringBuffer result = new StringBuffer();
        if (super.getMessage() != null) {
            result.append(super.getMessage());
        }
        if (!result.toString().endsWith(SEPARATOR)) {
            result.append("\n---- Debugging information ----");
        }
        for (Iterator iterator = keys(); iterator.hasNext();) {
            String k = (String) iterator.next();
            String v = get(k);
            result.append('\n').append(k);
            result.append("                    ".substring(k.length()));
            result.append(": ").append(v);
        }
        result.append(SEPARATOR);
        return result.toString();
    }

    public String getShortMessage() {
        return super.getMessage();
    }
}
