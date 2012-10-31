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
 * Copyright (C) 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 12. February 2008 by Joerg Schaible
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.enums;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;


/**
 * A single value converter for arbitrary enums. Converter is internally automatically
 * instantiated for enum types.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class EnumSingleValueConverter extends AbstractSingleValueConverter {

    private final Class enumType;

    public EnumSingleValueConverter(Class type) {
        if (!Enum.class.isAssignableFrom(type) && type != Enum.class) {
            throw new IllegalArgumentException("Converter can only handle defined enums");
        }
        enumType = type;
    }

    public boolean canConvert(Class type) {
        return enumType.isAssignableFrom(type);
    }

    public Object fromString(String str) {
        return Enum.valueOf(enumType, str);
    }
}
