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
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.basic;


/**
 * Converts a boolean primitive or java.lang.Boolean wrapper to
 * a String.
 *
 * @author Joe Walnes
 * @author David Blevins
 */
public class BooleanConverter extends AbstractSingleValueConverter {

    public static final BooleanConverter TRUE_FALSE = new BooleanConverter("true", "false", false);

    public static final BooleanConverter YES_NO = new BooleanConverter("yes", "no", false);

    public static final BooleanConverter BINARY = new BooleanConverter("1", "0", true);

    private final String positive;
    private final String negative;
    private final boolean caseSensitive;

    public BooleanConverter(final String positive, final String negative, final boolean caseSensitive) {
        this.positive = positive;
        this.negative = negative;
        this.caseSensitive = caseSensitive;
    }

    public BooleanConverter() {
        this("true", "false", false);
    }

    public boolean shouldConvert(final Class type, final Object value) {
        return true;
    }

    public boolean canConvert(final Class type) {
        return type.equals(boolean.class) || type.equals(Boolean.class);
    }

    public Object fromString(final String str) {
        if (caseSensitive) {
            return positive.equals(str) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            return positive.equalsIgnoreCase(str) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    public String toString(final Object obj) {
        final Boolean value = (Boolean) obj;
        return obj == null ? null : value.booleanValue() ? positive : negative;
    }
}
