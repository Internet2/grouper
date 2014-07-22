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
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
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

import java.util.HashSet;
import java.util.Set;

/**
 * Mapper that specifies which types are basic immutable types. Types that are marked as immutable will be written
 * multiple times in the serialization stream without using references.
 *
 * @author Joe Walnes
 */
public class ImmutableTypesMapper extends MapperWrapper {

    private final Set immutableTypes = new HashSet();

    public ImmutableTypesMapper(Mapper wrapped) {
        super(wrapped);
    }

    /**
     * @deprecated As of 1.2, use {@link #ImmutableTypesMapper(Mapper)}
     */
    public ImmutableTypesMapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    public void addImmutableType(Class type) {
        immutableTypes.add(type);
    }

    public boolean isImmutableValueType(Class type) {
        if (immutableTypes.contains(type)) {
            return true;
        } else {
            return super.isImmutableValueType(type);
        }
    }

}
