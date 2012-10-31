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
 * Created on 15. March 2007 by Joerg Schaible
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.TreeUnmarshaller;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.util.FastStack;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Abstract base class for a TreeUnmarshaller, that resolves refrences.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @since 1.2
 */
public abstract class AbstractReferenceUnmarshaller extends TreeUnmarshaller {

    private Map values = new HashMap();
    private FastStack parentStack = new FastStack(16);

    public AbstractReferenceUnmarshaller(Object root, HierarchicalStreamReader reader,
                                     ConverterLookup converterLookup, Mapper mapper) {
        super(root, reader, converterLookup, mapper);
    }

    protected Object convert(Object parent, Class type, Converter converter) {
        if (parentStack.size() > 0) { // handles circular references
            Object parentReferenceKey = parentStack.peek();
            if (parentReferenceKey != null) {
                if (!values.containsKey(parentReferenceKey)) { // see AbstractCircularReferenceTest.testWeirdCircularReference()
                    values.put(parentReferenceKey, parent);
                }
            }
        }
        String reference = reader.getAttribute(getMapper().aliasForAttribute("reference"));
        if (reference != null) {
            return values.get(getReferenceKey(reference));
        } else {
            Object currentReferenceKey = getCurrentReferenceKey();
            parentStack.push(currentReferenceKey);
            Object result = super.convert(parent, type, converter);
            if (currentReferenceKey != null) {
                values.put(currentReferenceKey, result);
            }
            parentStack.popSilently();
            return result;
        }
    }
    
    protected abstract Object getReferenceKey(String reference);
    protected abstract Object getCurrentReferenceKey();
}
