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
 * Copyright (C) 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 21.09.2007 by Joerg Schaible
 */
package edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.extended;

import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;


/**
 * A Converter for the XML Schema datatype <a
 * href="http://www.w3.org/TR/xmlschema-2/#duration">duration</a> and the Java type
 * {@link javax.xml.datatype.Duration Duration}.
 * 
 * @author John Kristian
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class DurationConverter extends AbstractSingleValueConverter {
    private final DatatypeFactory factory;

    public DurationConverter() throws DatatypeConfigurationException {
        this(DatatypeFactory.newInstance());
    }

    public DurationConverter(DatatypeFactory factory) {
        this.factory = factory;
    }

    public boolean canConvert(Class c) {
        return factory != null && Duration.class.isAssignableFrom(c);
    }

    public Object fromString(String s) {
        return factory.newDuration(s);
    }
}
