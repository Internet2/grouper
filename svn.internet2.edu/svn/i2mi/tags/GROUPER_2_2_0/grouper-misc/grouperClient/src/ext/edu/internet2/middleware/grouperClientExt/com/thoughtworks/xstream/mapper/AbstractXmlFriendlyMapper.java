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
 * Created on 03. May 2006 by Mauro Talevi
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper;


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
 */
public class AbstractXmlFriendlyMapper extends MapperWrapper {

    private char dollarReplacementInClass = '-';
    private String dollarReplacementInField = "_DOLLAR_";
    private String underscoreReplacementInField = "__";
    private String noPackagePrefix = "default";
    
    protected AbstractXmlFriendlyMapper(Mapper wrapped) {
        super(wrapped);
    }
    
    protected String escapeClassName(String className) {
        // the $ used in inner class names is illegal as an xml element getNodeName
        className = className.replace('$', dollarReplacementInClass);

        // special case for classes named $Blah with no package; <-Blah> is illegal XML
        if (className.charAt(0) == dollarReplacementInClass) {
            className = noPackagePrefix + className;
        }

        return className;
    }

    protected String unescapeClassName(String className) {
        // special case for classes named $Blah with no package; <-Blah> is illegal XML
        if (className.startsWith(noPackagePrefix+dollarReplacementInClass)) {
            className = className.substring(noPackagePrefix.length());
        }

        // the $ used in inner class names is illegal as an xml element getNodeName
        className = className.replace(dollarReplacementInClass, '$');

        return className;
    }

    protected String escapeFieldName(String fieldName) {
        StringBuffer result = new StringBuffer();
        int length = fieldName.length();
        for(int i = 0; i < length; i++) {
            char c = fieldName.charAt(i);
            if (c == '$' ) {
                result.append(dollarReplacementInField);
            } else if (c == '_') {
                result.append(underscoreReplacementInField);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }    
    
    protected String unescapeFieldName(String xmlName) {
        StringBuffer result = new StringBuffer();
        int length = xmlName.length();
        for(int i = 0; i < length; i++) {
            char c = xmlName.charAt(i);
            if ( stringFoundAt(xmlName, i,underscoreReplacementInField)) {
                i +=underscoreReplacementInField.length() - 1;
                result.append('_');
            } else if ( stringFoundAt(xmlName, i,dollarReplacementInField)) {
                i +=dollarReplacementInField.length() - 1;
                result.append('$');
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private boolean stringFoundAt(String name, int i, String replacement) {
        if ( name.length() >= i + replacement.length() 
          && name.substring(i, i + replacement.length()).equals(replacement) ){
            return true;
        }
        return false;
    }
    
}
