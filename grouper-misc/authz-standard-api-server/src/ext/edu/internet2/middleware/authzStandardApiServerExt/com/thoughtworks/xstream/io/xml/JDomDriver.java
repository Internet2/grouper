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
 * Created on 03. September 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.JDomReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.StreamException;

/**
 * @author Laurent Bihanic
 */
public class JDomDriver extends AbstractXmlDriver {

    public JDomDriver() {
        super(new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2
     */
    public JDomDriver(XmlFriendlyReplacer replacer) {
        super(replacer);
    }

    public HierarchicalStreamReader createReader(Reader reader) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(reader);
            return new JDomReader(document, xmlFriendlyReplacer());
        } catch (IOException e) {
            throw new StreamException(e);
        } catch (JDOMException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamReader createReader(InputStream in) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(in);
            return new JDomReader(document, xmlFriendlyReplacer());
        } catch (IOException e) {
            throw new StreamException(e);
        } catch (JDOMException e) {
            throw new StreamException(e);
        }
    }

    public HierarchicalStreamWriter createWriter(Writer out) {
        return new PrettyPrintWriter(out, xmlFriendlyReplacer());
    }

    public HierarchicalStreamWriter createWriter(OutputStream out) {
        return new PrettyPrintWriter(new OutputStreamWriter(out));
    }

}

