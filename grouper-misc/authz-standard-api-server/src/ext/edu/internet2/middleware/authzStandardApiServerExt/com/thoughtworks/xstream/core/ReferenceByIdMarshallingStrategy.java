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
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 16. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.AbstractTreeMarshallingStrategy;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.ReferenceByIdMarshaller;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.ReferenceByIdUnmarshaller;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.TreeMarshaller;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.TreeUnmarshaller;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.ConverterLookup;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper.Mapper;

public class ReferenceByIdMarshallingStrategy extends AbstractTreeMarshallingStrategy {

    protected TreeUnmarshaller createUnmarshallingContext(Object root,
        HierarchicalStreamReader reader, ConverterLookup converterLookup, Mapper mapper) {
        return new ReferenceByIdUnmarshaller(root, reader, converterLookup, mapper);
    }

    protected TreeMarshaller createMarshallingContext(
        HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper) {
        return new ReferenceByIdMarshaller(writer, converterLookup, mapper);
    }
}
