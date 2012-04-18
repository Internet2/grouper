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
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 15. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.alias.ClassMapper;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.path.Path;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

public class ReferenceByIdMarshaller extends AbstractReferenceMarshaller {

    private final IDGenerator idGenerator;

    public static interface IDGenerator {
        String next();
    }

    public ReferenceByIdMarshaller(HierarchicalStreamWriter writer,
                                   ConverterLookup converterLookup,
                                   Mapper mapper,
                                   IDGenerator idGenerator) {
        super(writer, converterLookup, mapper);
        this.idGenerator = idGenerator;
    }

    public ReferenceByIdMarshaller(HierarchicalStreamWriter writer,
                                   ConverterLookup converterLookup,
                                   Mapper mapper) {
        this(writer, converterLookup, mapper, new SequenceGenerator(1));
    }

    /**
     * @deprecated As of 1.2, use {@link #ReferenceByIdMarshaller(HierarchicalStreamWriter, ConverterLookup, Mapper, IDGenerator)}
     */
    public ReferenceByIdMarshaller(HierarchicalStreamWriter writer,
                                   ConverterLookup converterLookup,
                                   ClassMapper classMapper,
                                   IDGenerator idGenerator) {
        this(writer, converterLookup, (Mapper)classMapper, idGenerator);
    }

    /**
     * @deprecated As of 1.2, use {@link #ReferenceByIdMarshaller(HierarchicalStreamWriter, ConverterLookup, Mapper)}
     */
    public ReferenceByIdMarshaller(HierarchicalStreamWriter writer,
                                   ConverterLookup converterLookup,
                                   ClassMapper classMapper) {
        this(writer, converterLookup, (Mapper)classMapper);
    }

    protected String createReference(Path currentPath, Object existingReferenceKey) {
        return existingReferenceKey.toString();
    }

    protected Object createReferenceKey(Path currentPath) {
        return idGenerator.next();
    }

    protected void fireValidReference(Object referenceKey) {
        writer.addAttribute(getMapper().aliasForAttribute("id"), referenceKey.toString());
    }
}
