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
 * Created on 03. September 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.JDOMFactory;

import java.util.List;


/**
 * @author Laurent Bihanic
 */
public class JDomWriter extends AbstractDocumentWriter {

    private final JDOMFactory documentFactory;

    /**
     * @since 1.2
     */
    public JDomWriter(
                      final Element container, final JDOMFactory factory,
                      final XmlFriendlyReplacer replacer) {
        super(container, replacer);
        documentFactory = factory;
    }

    public JDomWriter(final Element container, final JDOMFactory factory) {
        this(container, factory, new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2.1
     */
    public JDomWriter(final JDOMFactory factory, final XmlFriendlyReplacer replacer) {
        this(null, factory, replacer);
    }

    public JDomWriter(final JDOMFactory factory) {
        this(null, factory);
    }

    /**
     * @since 1.2.1
     */
    public JDomWriter(final Element container, final XmlFriendlyReplacer replacer) {
        this(container, new DefaultJDOMFactory(), replacer);
    }

    public JDomWriter(final Element container) {
        this(container, new DefaultJDOMFactory());
    }

    public JDomWriter() {
        this(new DefaultJDOMFactory());
    }

    protected Object createNode(final String name) {
        final Element element = documentFactory.element(escapeXmlName(name));
        final Element parent = top();
        if (parent != null) {
            parent.addContent(element);
        }
        return element;
    }

    public void setValue(final String text) {
        top().addContent(documentFactory.text(text));
    }

    public void addAttribute(final String key, final String value) {
        top().setAttribute(documentFactory.attribute(escapeXmlName(key), value));
    }

    private Element top() {
        return (Element)getCurrent();
    }

    /**
     * @deprecated since 1.2.1, use {@link #getTopLevelNodes()} instead
     */
    public List getResult() {
        return getTopLevelNodes();
    }
}
