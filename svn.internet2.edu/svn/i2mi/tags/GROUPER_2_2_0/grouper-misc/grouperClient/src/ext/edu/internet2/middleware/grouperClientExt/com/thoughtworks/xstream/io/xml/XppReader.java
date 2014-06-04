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
 * Created on 08. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ErrorWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.StreamException;

/**
 * XStream reader that pulls directly from the stream using the XmlPullParser API.
 *
 * @author Joe Walnes
 */
public class XppReader extends AbstractPullReader {

    private final XmlPullParser parser;
    private final BufferedReader reader;

    public XppReader(Reader reader) {
        this(reader, new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2
     */
    public XppReader(Reader reader, XmlFriendlyReplacer replacer) {
        super(replacer);
        try {
            parser = createParser();
            this.reader = new BufferedReader(reader);
            parser.setInput(this.reader);
            moveDown();
        } catch (XmlPullParserException e) {
            throw new StreamException(e);
        }
    }
    
    /**
     * To use another implementation of org.xmlpull.v1.XmlPullParser, override this method.
     */
    protected XmlPullParser createParser() {
        return new MXParser();
    }

    protected int pullNextEvent() {
        try {
            switch(parser.next()) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.START_TAG:
                    return START_NODE;
                case XmlPullParser.END_DOCUMENT:
                case XmlPullParser.END_TAG:
                    return END_NODE;
                case XmlPullParser.TEXT:
                    return TEXT;
                case XmlPullParser.COMMENT:
                    return COMMENT;
                default:
                    return OTHER;
            }
        } catch (XmlPullParserException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    protected String pullElementName() {
        return parser.getName();
    }

    protected String pullText() {
        return parser.getText();
    }

    public String getAttribute(String name) {
        return parser.getAttributeValue(null, name);
    }

    public String getAttribute(int index) {
        return parser.getAttributeValue(index);
    }

    public int getAttributeCount() {
        return parser.getAttributeCount();
    }

    public String getAttributeName(int index) {
        return unescapeXmlName(parser.getAttributeName(index));
    }

    public void appendErrors(ErrorWriter errorWriter) {
        errorWriter.add("line number", String.valueOf(parser.getLineNumber()));
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

}
