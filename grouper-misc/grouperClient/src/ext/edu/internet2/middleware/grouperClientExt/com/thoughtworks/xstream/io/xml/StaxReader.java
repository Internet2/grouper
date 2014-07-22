/**
 * Copyright 2014 Internet2
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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ErrorWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.StreamException;

/**
 * A reader using the StAX API.
 *
 * @author James Strachan
 * @version $Revision: 1.1 $
 */
public class StaxReader extends AbstractPullReader {

    private final QNameMap qnameMap;
    private final XMLStreamReader in;

    public StaxReader(QNameMap qnameMap, XMLStreamReader in) {
        this(qnameMap, in, new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2
     */
    public StaxReader(QNameMap qnameMap, XMLStreamReader in, XmlFriendlyReplacer replacer) {
        super(replacer);
        this.qnameMap = qnameMap;
        this.in = in;
        moveDown();
    }
    
    protected int pullNextEvent() {
        try {
            switch(in.next()) {
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.START_ELEMENT:
                    return START_NODE;
                case XMLStreamConstants.END_DOCUMENT:
                case XMLStreamConstants.END_ELEMENT:
                    return END_NODE;
                case XMLStreamConstants.CHARACTERS:
                    return TEXT;
                case XMLStreamConstants.COMMENT:
                    return COMMENT;
                default:
                    return OTHER;
            }
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    protected String pullElementName() {
        // let the QNameMap handle any mapping of QNames to Java class names
        QName qname = in.getName();
        return qnameMap.getJavaClassName(qname);
    }

    protected String pullText() {
        return in.getText();
    }

    public String getAttribute(String name) {
        return in.getAttributeValue(null, name);
    }

    public String getAttribute(int index) {
        return in.getAttributeValue(index);
    }

    public int getAttributeCount() {
        return in.getAttributeCount();
    }

    public String getAttributeName(int index) {
        return unescapeXmlName(in.getAttributeLocalName(index));
    }

    public void appendErrors(ErrorWriter errorWriter) {
        errorWriter.add("line number", String.valueOf(in.getLocation().getLineNumber()));
    }

    public void close() {
        try {
            in.close();
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

}
