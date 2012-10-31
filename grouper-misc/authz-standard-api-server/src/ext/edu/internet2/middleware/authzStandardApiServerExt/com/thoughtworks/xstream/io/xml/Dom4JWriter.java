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
 * Created on 07. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.AbstractDocumentWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;


public class Dom4JWriter extends AbstractDocumentWriter {

    private final DocumentFactory documentFactory;

    /**
     * @since 1.2.1
     */
    public Dom4JWriter(
                       final Branch root, final DocumentFactory factory,
                       final XmlFriendlyReplacer replacer) {
        super(root, replacer);
        documentFactory = factory;
    }

    /**
     * @since 1.2.1
     */
    public Dom4JWriter(final DocumentFactory factory, final XmlFriendlyReplacer replacer) {
        this(null, factory, replacer);
    }

    /**
     * @since 1.2.1
     */
    public Dom4JWriter(final DocumentFactory documentFactory) {
        this(documentFactory, new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2.1
     */
    public Dom4JWriter(final Branch root, final XmlFriendlyReplacer replacer) {
        this(root, new DocumentFactory(), replacer);
    }

    public Dom4JWriter(final Branch root) {
        this(root, new DocumentFactory(), new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2.1
     */
    public Dom4JWriter() {
        this(new DocumentFactory(), new XmlFriendlyReplacer());
    }

    protected Object createNode(final String name) {
        final Element element = documentFactory.createElement(escapeXmlName(name));
        final Branch top = top();
        if (top != null) {
            top().add(element);
        }
        return element;
    }

    public void setValue(final String text) {
        top().setText(text);
    }

    public void addAttribute(final String key, final String value) {
        ((Element)top()).addAttribute(escapeXmlName(key), value);
    }

    private Branch top() {
        return (Branch)getCurrent();
    }
}
