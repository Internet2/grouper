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

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.alias.ClassMapper;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper that caches which names map to which classes. Prevents repetitive searching and class loading.
 *
 * @author Joe Walnes
 */
public class CachingMapper extends MapperWrapper {

    private transient Map realClassCache;

    public CachingMapper(Mapper wrapped) {
        super(wrapped);
        readResolve();
    }

    /**
     * @deprecated As of 1.2, use {@link #CachingMapper(Mapper)}
     */
    public CachingMapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
        readResolve();
    }

    public Class realClass(String elementName) {
        WeakReference reference = (WeakReference) realClassCache.get(elementName);
        if (reference != null) {
            Class cached = (Class) reference.get();
            if (cached != null) {
                return cached;
            }
        }
        
        Class result = super.realClass(elementName);
        realClassCache.put(elementName, new WeakReference(result));
        return result;
    }

    private Object readResolve() {
        realClassCache = Collections.synchronizedMap(new HashMap(128));
        return this;
    }

}
