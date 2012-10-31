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
 * Created on 07. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.AbstractDocumentReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.ErrorWriter;

import org.dom4j.Document;
import org.dom4j.Element;

public class Dom4JReader extends AbstractDocumentReader {

    private Element currentElement;

    public Dom4JReader(Element rootElement) {
        this(rootElement, new XmlFriendlyReplacer());
    }

    public Dom4JReader(Document document) {
        this(document.getRootElement());
    }

    /**
     * @since 1.2
     */
    public Dom4JReader(Element rootElement, XmlFriendlyReplacer replacer) {
        super(rootElement, replacer);
    }

    /**
     * @since 1.2
     */
    public Dom4JReader(Document document, XmlFriendlyReplacer replacer) {
        this(document.getRootElement(), replacer);
    }
    
    public String getNodeName() {
        return unescapeXmlName(currentElement.getName());
    }

    public String getValue() {
        return currentElement.getText();
    }

    public String getAttribute(String name) {
        return currentElement.attributeValue(name);
    }

    public String getAttribute(int index) {
        return currentElement.attribute(index).getValue();
    }

    public int getAttributeCount() {
        return currentElement.attributeCount();
    }

    public String getAttributeName(int index) {
        return unescapeXmlName(currentElement.attribute(index).getQualifiedName());
    }

    protected Object getParent() {
        return currentElement.getParent();
    }

    protected Object getChild(int index) {
        return currentElement.elements().get(index);
    }

    protected int getChildCount() {
        return currentElement.elements().size();
    }

    protected void reassignCurrentElement(Object current) {
        currentElement = (Element) current;
    }

    public void appendErrors(ErrorWriter errorWriter) {
        errorWriter.add("xpath", currentElement.getPath());
    }

}
