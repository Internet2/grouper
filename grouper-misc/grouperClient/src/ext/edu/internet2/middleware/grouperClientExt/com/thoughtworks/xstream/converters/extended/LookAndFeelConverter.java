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
 * Copyright (C) 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 08.12.2007 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.extended;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

import javax.swing.LookAndFeel;

import java.io.NotSerializableException;


/**
 * A converter for Swing LookAndFeel implementations. The JDK's implementations are serializable
 * for historical reasons but will throw a {@link NotSerializableException} in their writeObject
 * method. Therefore XStream will use an implementation based on the ReflectionConverter.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class LookAndFeelConverter extends ReflectionConverter {

    /**
     * Constructs a LookAndFeelConverter.
     * 
     * @param mapper the mapper
     * @param reflectionProvider the reflection provider
     * @since 1.3
     */
    public LookAndFeelConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
        super(mapper, reflectionProvider);
    }

    public boolean canConvert(Class type) {
        return LookAndFeel.class.isAssignableFrom(type);
    }
}
