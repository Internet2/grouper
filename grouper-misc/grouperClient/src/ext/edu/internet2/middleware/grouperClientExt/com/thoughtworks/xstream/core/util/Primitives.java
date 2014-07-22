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
 * Copyright (c) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 11. October 2006 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for primitives.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.2.1
 */
public final class Primitives {
    private final static Map BOX = new HashMap();
    private final static Map UNBOX = new HashMap();
    
    static {
        final Class[][] boxing = new Class[][]{
            { byte.class, Byte.class},
            { char.class, Character.class},
            { short.class, Short.class},
            { int.class, Integer.class},
            { long.class, Long.class},
            { float.class, Float.class},
            { double.class, Double.class},
            { boolean.class, Boolean.class},
            { void.class, Void.class},
        };
        for (int i = 0; i < boxing.length; i++) {
            BOX.put(boxing[i][0], boxing[i][1]);
            UNBOX.put(boxing[i][1], boxing[i][0]);
        }
    }
    
    static public Class box(final Class type) {
        return (Class)BOX.get(type);
    }
    
    static public Class unbox(final Class type) {
        return (Class)UNBOX.get(type);
    }
}
