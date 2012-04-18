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
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 01. October 2004 by James Strachan
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml;

import javax.xml.namespace.QName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mapping of {@link QName} instances to Java class names
 * allowing class aliases and namespace aware mappings of QNames to class names.
 *
 * @author James Strachan
 * @version $Revision: 1.1 $
 */
public class QNameMap {

    // lets make the mapping a no-op unless we specify some mapping
    private Map qnameToJava;
    private Map javaToQName;
    private String defaultPrefix = "";
    private String defaultNamespace = "";

    /**
     * Returns the Java class name that should be used for the given QName.
     * If no explicit mapping has been made then the localPart of the QName is used
     * which is the normal default in XStream.
     */
    public String getJavaClassName(QName qname) {
        if (qnameToJava != null) {
            String answer = (String) qnameToJava.get(qname);
            if (answer != null) {
                return answer;
            }
        }
        return qname.getLocalPart();
    }

    /**
     * Returns the Java class name that should be used for the given QName.
     * If no explicit mapping has been made then the localPart of the QName is used
     * which is the normal default in XStream.
     */
    public QName getQName(String javaClassName) {
        if (javaToQName != null) {
            QName answer = (QName) javaToQName.get(javaClassName);
            if (answer != null) {
                return answer;
            }
        }
        return new QName(defaultNamespace, javaClassName, defaultPrefix);
    }

    /**
     * Registers the mapping of the Java class name to the QName
     */
    public synchronized void registerMapping(QName qname, String javaClassName) {
        if (javaToQName == null) {
            javaToQName = Collections.synchronizedMap(new HashMap());
        }
        if (qnameToJava == null) {
            qnameToJava = Collections.synchronizedMap(new HashMap());
        }
        javaToQName.put(javaClassName, qname);
        qnameToJava.put(qname, javaClassName);
    }

    /**
     * Registers the mapping of the type to the QName
     */
    public synchronized void registerMapping(QName qname, Class type) {
        registerMapping(qname, type.getName());
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }
}
