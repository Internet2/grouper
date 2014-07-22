/**
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
 */
/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. March 2005 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util;

/**
 * ClassLoader that refers to another ClassLoader, allowing a single instance to be passed around the codebase that
 * can later have its destination changed.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @since 1.1.1
 */
public class ClassLoaderReference extends ClassLoader {

    private transient ClassLoader reference;

    public ClassLoaderReference(ClassLoader reference) {
        this.reference = reference;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return reference.loadClass(name);
    }

    public ClassLoader getReference() {
        return reference;
    }

    public void setReference(ClassLoader reference) {
        this.reference = reference;
    }
    
    private Object writeReplace() {
        return new Replacement();
    }
    
    static class Replacement {
        
        private Object readResolve() {
            return new ClassLoaderReference(new CompositeClassLoader());
        }
        
    };
}
