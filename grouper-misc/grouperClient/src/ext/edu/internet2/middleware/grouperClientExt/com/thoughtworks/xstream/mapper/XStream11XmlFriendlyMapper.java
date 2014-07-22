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
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03. May 2006 by Mauro Talevi
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper;


/**
 * Mapper that ensures that all names in the serialization stream are read in an XML friendly way.
 * <ul>
 * <li><b>_</b> (underscore) chars appearing in class names are replaced with <b>$<br> (dollar)</li>
 * <li><b>_DOLLAR_</b> string appearing in field names are replaced with <b>$<br> (dollar)</li>
 * <li><b>__</b> string appearing in field names are replaced with <b>_<br> (underscore)</li>
 * <li><b>default</b> is the prefix for class names with no package.</li>
 * </ul>
 * 
 * @author Joe Walnes
 * @author Mauro Talevi
 */
public class XStream11XmlFriendlyMapper extends AbstractXmlFriendlyMapper {

    public XStream11XmlFriendlyMapper(Mapper wrapped) {
        super(wrapped);
    }

    public Class realClass(String elementName) {
        return super.realClass(unescapeClassName(elementName));
    }

    public String realMember(Class type, String serialized) {
        return unescapeFieldName(super.realMember(type, serialized));
    }

    public String mapNameFromXML(String xmlName) {
        return unescapeFieldName(xmlName);
    }

    
}
