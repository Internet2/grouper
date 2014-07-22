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
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 29. September 2004 by James Strachan
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.StreamException;

/**
 * A driver using the StAX API
 *
 * @author James Strachan
 * @version $Revision: 1.1 $
 */
public class StaxDriver extends AbstractXmlDriver {

    private static boolean libraryPresent;

    private QNameMap qnameMap;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;

    public StaxDriver() {
        this.qnameMap = new QNameMap();
    }

    public StaxDriver(QNameMap qnameMap) {
        this(qnameMap, false);
    }

    /**
     * @deprecated since 1.2, use an explicit call to {@link #setRepairingNamespace(boolean)}
     */
    public StaxDriver(QNameMap qnameMap, boolean repairingNamespace) {
        this(qnameMap, new XmlFriendlyReplacer());
        setRepairingNamespace(repairingNamespace);
    }

    /**
     * @since 1.2
     */
    public StaxDriver(QNameMap qnameMap, XmlFriendlyReplacer replacer) {
        super(replacer);
        this.qnameMap = qnameMap;
    }
    
    /**
     * @since 1.2
     */
    public StaxDriver(XmlFriendlyReplacer replacer) {
        this(new QNameMap(), replacer);
    }

    public HierarchicalStreamReader createReader(Reader xml) {
        loadLibrary();
        try {
            return createStaxReader(createParser(xml));
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamReader createReader(InputStream in) {
        loadLibrary();
        try {
            return createStaxReader(createParser(in));
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    private void loadLibrary() {
        if (!libraryPresent) {
            try {
                Class.forName("javax.xml.stream.XMLStreamReader");
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("StAX API is not present. Specify another driver." +
                        " For example: new XStream(new DomDriver())");
            }
            libraryPresent = true;
        }
    }

    public HierarchicalStreamWriter createWriter(Writer out) {
        try {
            return createStaxWriter(getOutputFactory().createXMLStreamWriter(out));
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamWriter createWriter(OutputStream out) {
        try {
            return createStaxWriter(getOutputFactory().createXMLStreamWriter(out));
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public AbstractPullReader createStaxReader(XMLStreamReader in) {
        return new StaxReader(qnameMap, in, xmlFriendlyReplacer());
    }

    public StaxWriter createStaxWriter(XMLStreamWriter out, boolean writeStartEndDocument) throws XMLStreamException {
        return new StaxWriter(qnameMap, out, writeStartEndDocument, isRepairingNamespace(), xmlFriendlyReplacer());
    }

    public StaxWriter createStaxWriter(XMLStreamWriter out) throws XMLStreamException {
        return createStaxWriter(out, true);
    }


    // Properties
    //-------------------------------------------------------------------------
    public QNameMap getQnameMap() {
        return qnameMap;
    }

    public void setQnameMap(QNameMap qnameMap) {
        this.qnameMap = qnameMap;
    }

    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = XMLInputFactory.newInstance();
        }
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = XMLOutputFactory.newInstance();
        }
        return outputFactory;
    }

    public boolean isRepairingNamespace() {
        return Boolean.TRUE.equals(getOutputFactory().getProperty(
                XMLOutputFactory.IS_REPAIRING_NAMESPACES));
    }

    /**
     * @since 1.2
     */
    public void setRepairingNamespace(boolean repairing) {
        getOutputFactory().setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES,
                repairing ? Boolean.TRUE : Boolean.FALSE);
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected XMLStreamReader createParser(Reader xml) throws XMLStreamException {
        return getInputFactory().createXMLStreamReader(xml);
    }

    protected XMLStreamReader createParser(InputStream xml) throws XMLStreamException {
        return getInputFactory().createXMLStreamReader(xml);
    }
}
