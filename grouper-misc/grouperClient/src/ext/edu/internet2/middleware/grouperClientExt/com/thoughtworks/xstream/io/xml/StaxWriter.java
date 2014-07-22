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
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 29. September 2004 by James Strachan
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.StreamException;

/**
 * A stream writing that outputs to a StAX stream writer
 *
 * @author James Strachan
 * @version $Revision: 1.1 $
 */
public class StaxWriter extends AbstractXmlWriter {
    
    private final QNameMap qnameMap;
    private final XMLStreamWriter out;
    private final boolean writeEnclosingDocument;
    private boolean namespaceRepairingMode;

    private int tagDepth;

    public StaxWriter(QNameMap qnameMap, XMLStreamWriter out) throws XMLStreamException {
        this(qnameMap, out, true, true);
    }

    /**
     * Allows a StaxWriter to be created for partial XML output
     *
     * @param qnameMap               is the mapper of Java class names to QNames
     * @param out                    the stream to output to
     * @param writeEnclosingDocument a flag to indicate whether or not the start/end document events should be written
     * @throws XMLStreamException if the events could not be written to the output
     */
    public StaxWriter(QNameMap qnameMap, XMLStreamWriter out, boolean writeEnclosingDocument, boolean namespaceRepairingMode) throws XMLStreamException {
        this(qnameMap, out, writeEnclosingDocument, namespaceRepairingMode, new XmlFriendlyReplacer());
    }

    /**
     * Allows a StaxWriter to be created for partial XML output
     *
     * @param qnameMap               is the mapper of Java class names to QNames
     * @param out                    the stream to output to
     * @param writeEnclosingDocument a flag to indicate whether or not the start/end document events should be written
     * @param replacer              the xml-friendly replacer to escape Java names
     * @throws XMLStreamException if the events could not be written to the output
     * @since 1.2
     */
    public StaxWriter(QNameMap qnameMap, XMLStreamWriter out, 
            boolean writeEnclosingDocument, boolean namespaceRepairingMode,
            XmlFriendlyReplacer replacer) throws XMLStreamException {
        super(replacer);
        this.qnameMap = qnameMap;
        this.out = out;
        this.writeEnclosingDocument = writeEnclosingDocument;
        this.namespaceRepairingMode = namespaceRepairingMode;
        if (writeEnclosingDocument) {
            out.writeStartDocument();
        }
    }
    
    public void flush() {
        try {
            out.flush();
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    /**
     * Call this method when you're finished with me
     */
    public void close() {
        try {
            out.close();
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public void addAttribute(String name, String value) {
        try {
            out.writeAttribute(escapeXmlName(name), value);
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public void endNode() {
        try {
            tagDepth--;
            out.writeEndElement();
            if (tagDepth == 0 && writeEnclosingDocument) {
                out.writeEndDocument();
            }
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public void setValue(String text) {
        try {
            out.writeCharacters(text);
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public void startNode(String name) {
        try {
            QName qname = qnameMap.getQName(escapeXmlName(name));
            String prefix = qname.getPrefix();
            String uri = qname.getNamespaceURI();

            // before you ask - yes it really is this complicated to output QNames to StAX
            // handling both repair namespace modes :)

            boolean hasPrefix = prefix != null && prefix.length() > 0;
            boolean hasURI = uri != null && uri.length() > 0;
            boolean writeNamespace = false;

            if (hasURI) {
                if (hasPrefix) {
                    String currentNamespace = out.getNamespaceContext().getNamespaceURI(prefix);
                    if (currentNamespace == null || !currentNamespace.equals(uri)) {
                        writeNamespace = true;
                    }
                }
                else {
                    String defaultNamespace = out.getNamespaceContext().getNamespaceURI("");
                    if (defaultNamespace == null || !defaultNamespace.equals(uri)) {
                        writeNamespace = true;
                    }
                }
            }

            if (hasPrefix) {
                out.setPrefix(prefix, uri);
            }
            else if (hasURI) {
                if (writeNamespace) {
                    out.setDefaultNamespace(uri);
                }
            }
            out.writeStartElement(prefix, qname.getLocalPart(), uri);
            if (hasURI && writeNamespace && !isNamespaceRepairingMode()) {
                if (hasPrefix) {
                    out.writeNamespace(prefix, uri);
                }
                else {
                    out.writeDefaultNamespace(uri);
                }
            }
            tagDepth++;
        }
        catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    /**
     * Is StAX namespace repairing mode on or off?
     */
    public boolean isNamespaceRepairingMode() {
        return namespaceRepairingMode;
    }

}
