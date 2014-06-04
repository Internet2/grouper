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
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 15. February 2006 by Mauro Talevi
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.basic;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * Base abstract implementation of  {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.SingleValueConverter}.
 * <p/>
 * <p>Subclasses should implement methods canConvert(Class) and fromString(String) for the conversion.</p>
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.SingleValueConverter
 */
public abstract class AbstractSingleValueConverter implements SingleValueConverter {

    public abstract boolean canConvert(Class type);

    public String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public abstract Object fromString(String str);

}
