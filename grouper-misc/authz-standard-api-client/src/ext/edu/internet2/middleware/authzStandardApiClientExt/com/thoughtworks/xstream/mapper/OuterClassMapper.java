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
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 31. January 2005 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.mapper;

import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.alias.ClassMapper;

/**
 * Mapper that uses a more meaningful alias for the field in an inner class (this$0) that refers to the outer class.
 *
 * @author Joe Walnes
 */
public class OuterClassMapper extends MapperWrapper {

    private final String alias;

    public OuterClassMapper(Mapper wrapped) {
        this(wrapped, "outer-class");
    }

    public OuterClassMapper(Mapper wrapped, String alias) {
        super(wrapped);
        this.alias = alias;
    }

    /**
     * @deprecated As of 1.2, use {@link #OuterClassMapper(Mapper)}
     */
    public OuterClassMapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    /**
     * @deprecated As of 1.2, use {@link #OuterClassMapper(Mapper, String)}
     */
    public OuterClassMapper(ClassMapper wrapped, String alias) {
        this((Mapper)wrapped, alias);
    }

    public String serializedMember(Class type, String memberName) {
        if (memberName.equals("this$0")) {
            return alias;
        } else {
            return super.serializedMember(type, memberName);
        }
    }

    public String realMember(Class type, String serialized) {
        if (serialized.equals(alias)) {
            return "this$0";
        } else {
            return super.realMember(type, serialized);
        }
    }
}
