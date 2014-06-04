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
 * Copyright (c) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. March 2007 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.StreamException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.QNameMap;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.StaxReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.StaxWriter;


/**
 * Simple XStream driver wrapping Jettison's Mapped reader and writer. Serializes object from
 * and to JSON.
 * 
 * @author Dejan Bosanac
 */
public class JettisonMappedXmlDriver implements HierarchicalStreamDriver {

    private final MappedXMLOutputFactory mof;
    private final MappedXMLInputFactory mif;

    public JettisonMappedXmlDriver() {
        final HashMap nstjsons = new HashMap();
        mof = new MappedXMLOutputFactory(nstjsons);
        mif = new MappedXMLInputFactory(nstjsons);
    }

    public HierarchicalStreamReader createReader(final Reader reader) {
        try {
            return new StaxReader(new QNameMap(), mif.createXMLStreamReader(reader));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamReader createReader(final InputStream input) {
        try {
            return new StaxReader(new QNameMap(), mif.createXMLStreamReader(input));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamWriter createWriter(final Writer writer) {
        try {
            return new StaxWriter(new QNameMap(), mof.createXMLStreamWriter(writer));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamWriter createWriter(final OutputStream output) {
        try {
            return new StaxWriter(new QNameMap(), mof.createXMLStreamWriter(output));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

}
