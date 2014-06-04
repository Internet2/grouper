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
 * Converts a short primitive or java.lang.Short wrapper to
 * a String.
 *
 * @author Joe Walnes
 */
public class ShortConverter extends AbstractSingleValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(short.class) || type.equals(Short.class);
    }

    public Object fromString(String str) {
    	int value = Integer.decode(str).intValue();
    	if(value < Short.MIN_VALUE || value > 0xFFFF) {
    		throw new NumberFormatException("For input string: \"" + str + '"');
    	}
        return new Short((short)value);
    }

}
