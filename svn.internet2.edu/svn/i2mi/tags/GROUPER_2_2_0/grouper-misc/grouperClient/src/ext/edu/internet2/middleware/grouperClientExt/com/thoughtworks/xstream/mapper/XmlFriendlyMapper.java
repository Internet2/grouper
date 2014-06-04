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
 * Copyright (C) 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. January 2005 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.alias.ClassMapper;

/**
 * Mapper that ensures that all names in the serialization stream are XML friendly.
 * The replacement chars and strings are:
 * <ul>
 * <li><b>$</b> (dollar) chars appearing in class names are replaced with <b>_</b> (underscore) chars.<br></li>
 * <li><b>$</b> (dollar) chars appearing in field names are replaced with <b>_DOLLAR_</b> string.<br></li>
 * <li><b>_</b> (underscore) chars appearing in field names are replaced with <b>__</b> (double underscore) string.<br></li>
 * <li><b>default</b> as the prefix for class names with no package.</li>
 * </ul>
 * 
 * @author Joe Walnes
 * @author Mauro Talevi
 * @deprecated since 1.3, use {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.XmlFriendlyReader}
 */
public class XmlFriendlyMapper extends AbstractXmlFriendlyMapper {

    /**
     * @deprecated since 1.3, use {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.XmlFriendlyReader}
     */
    public XmlFriendlyMapper(Mapper wrapped) {
        super(wrapped);
    }
    
    /**
     * @deprecated since 1.2, use {@link #XmlFriendlyMapper(Mapper)}
     */
    public XmlFriendlyMapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    public String serializedClass(Class type) {
        return escapeClassName(super.serializedClass(type));
    }

    public Class realClass(String elementName) {
        return super.realClass(unescapeClassName(elementName));
    }

    public String serializedMember(Class type, String memberName) {
        return escapeFieldName(super.serializedMember(type, memberName));
    }

    public String realMember(Class type, String serialized) {
        return unescapeFieldName(super.realMember(type, serialized));
    }

    public String mapNameToXML(String javaName) {
        return escapeFieldName(javaName);
    }

    public String mapNameFromXML(String xmlName) {
        return unescapeFieldName(xmlName);
    }

}
