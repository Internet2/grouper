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
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 12. April 2006 by Joerg Schaible
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.XomReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.StreamException;

public class XomDriver extends AbstractXmlDriver {

    private final Builder builder;

    public XomDriver() {
        this(new Builder());
    }

    public XomDriver(Builder builder) {
        this(builder, new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2
     */
    public XomDriver(XmlFriendlyReplacer replacer) {
        this(new Builder(), replacer);        
    }
    
    /**
     * @since 1.2
     */
    public XomDriver(Builder builder, XmlFriendlyReplacer replacer) {
        super(replacer);    
        this.builder = builder;
    }

    protected Builder getBuilder() {
        return this.builder;
    }

    public HierarchicalStreamReader createReader(Reader text) {
        try {
            Document document = builder.build(text);
            return new XomReader(document, xmlFriendlyReplacer());
        } catch (ValidityException e) {
            throw new StreamException(e);
        } catch (ParsingException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamReader createReader(InputStream in) {
        try {
            Document document = builder.build(in);
            return new XomReader(document, xmlFriendlyReplacer());
        } catch (ValidityException e) {
            throw new StreamException(e);
        } catch (ParsingException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamWriter createWriter(final Writer out) {
        return new PrettyPrintWriter(out, xmlFriendlyReplacer());
    }

    public HierarchicalStreamWriter createWriter(final OutputStream out) {
        return new PrettyPrintWriter(new OutputStreamWriter(out), xmlFriendlyReplacer());
    }
}
