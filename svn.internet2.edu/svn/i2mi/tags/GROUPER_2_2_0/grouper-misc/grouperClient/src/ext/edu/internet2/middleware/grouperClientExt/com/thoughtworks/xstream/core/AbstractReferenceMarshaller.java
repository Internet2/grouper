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
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.ObjectIdDictionary;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.path.Path;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.path.PathTracker;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.path.PathTrackingWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Abstract base class for a TreeMarshaller, that can build references.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @since 1.2
 */
public abstract class AbstractReferenceMarshaller extends TreeMarshaller {

    private ObjectIdDictionary references = new ObjectIdDictionary();
    private ObjectIdDictionary implicitElements = new ObjectIdDictionary();
    private PathTracker pathTracker = new PathTracker();
    private Path lastPath;

    public AbstractReferenceMarshaller(HierarchicalStreamWriter writer,
                                   ConverterLookup converterLookup,
                                   Mapper mapper) {
        super(writer, converterLookup, mapper);
        this.writer = new PathTrackingWriter(writer, pathTracker);
    }

    public void convert(Object item, Converter converter) {
        if (getMapper().isImmutableValueType(item.getClass())) {
            // strings, ints, dates, etc... don't bother using references.
            converter.marshal(item, writer, this);
        } else {
            Path currentPath = pathTracker.getPath();
            Object existingReferenceKey = references.lookupId(item);
            if (existingReferenceKey != null) {
                writer.addAttribute(getMapper().aliasForAttribute("reference"), createReference(currentPath, existingReferenceKey));
            } else if (implicitElements.lookupId(item) != null) {
                throw new ReferencedImplicitElementException(item, currentPath);
            } else {
                Object newReferenceKey = createReferenceKey(currentPath);
                if (lastPath == null || !currentPath.isAncestor(lastPath)) {
                    fireValidReference(newReferenceKey);
                    lastPath = currentPath;
                    references.associateId(item, newReferenceKey);
                } else {
                    implicitElements.associateId(item, newReferenceKey);
                }
                converter.marshal(item, writer, this);
            }
        }
    }
    
    protected abstract String createReference(Path currentPath, Object existingReferenceKey);
    protected abstract Object createReferenceKey(Path currentPath);
    protected abstract void fireValidReference(Object referenceKey);
    
    public static class ReferencedImplicitElementException extends ConversionException {
        /**
         * @deprecated since 1.2.1
         */
        public ReferencedImplicitElementException(final String msg) {
            super(msg);
        }
        public ReferencedImplicitElementException(final Object item, final Path path) {
            super("Cannot reference implicit element");
            add("implicit-element", item.toString());
            add("referencing-element", path.toString());
        }
    }
}
