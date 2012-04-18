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
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: XppDomReader.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
 */
public class XppDomReader extends AbstractDocumentReader {

    private Xpp3Dom currentElement;

    public XppDomReader(Xpp3Dom xpp3Dom) {
        super(xpp3Dom);
    }

    /**
     * @since 1.2
     */
    public XppDomReader(Xpp3Dom xpp3Dom, XmlFriendlyReplacer replacer) {
        super(xpp3Dom, replacer);
    }
    
    public String getNodeName() {
        return unescapeXmlName(currentElement.getName());
    }

    public String getValue() {
        String text = null;

        try {
            text = currentElement.getValue();
        } catch (Exception e) {
            // do nothing.
        }

        return text == null ? "" : text;
    }

    public String getAttribute(String attributeName) {
        return currentElement.getAttribute(attributeName);
    }

    public String getAttribute(int index) {
        return currentElement.getAttribute(currentElement.getAttributeNames()[index]);
    }

    public int getAttributeCount() {
        return currentElement.getAttributeNames().length;
    }

    public String getAttributeName(int index) {
        return unescapeXmlName(currentElement.getAttributeNames()[index]);
    }

    protected Object getParent() {
        return currentElement.getParent();
    }

    protected Object getChild(int index) {
        return currentElement.getChild(index);
    }

    protected int getChildCount() {
        return currentElement.getChildCount();
    }

    protected void reassignCurrentElement(Object current) {
        this.currentElement = (Xpp3Dom) current;
    }

}
