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
 * Copyright (c) 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 20. September 2007 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection.ObjectAccessException;

import java.beans.PropertyEditor;


/**
 * Wrapper around {@link PropertyEditor} that can be called by multiple threads concurrently.
 * <p>
 * A PropertyEditor is not thread safe. To make best use of resources, the PropertyEditor
 * provides a dynamically sizing pool of instances, each of which will only be called by a
 * single thread at a time.
 * </p>
 * <p>
 * The pool has a maximum capacity, to limit overhead. If all instances in the pool are in use
 * and another is required, it shall block until one becomes available.
 * </p>
 * 
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class ThreadSafePropertyEditor {

    private final Class editorType;
    private final Pool pool;

    public ThreadSafePropertyEditor(Class type, int initialPoolSize, int maxPoolSize) {
        if (!PropertyEditor.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName()
                + " is not a "
                + PropertyEditor.class.getName());
        }
        editorType = type;
        pool = new Pool(initialPoolSize, maxPoolSize, new Pool.Factory() {
            public Object newInstance() {
                try {
                    return editorType.newInstance();
                } catch (InstantiationException e) {
                    throw new ObjectAccessException("Could not call default constructor of "
                        + editorType.getName(), e);
                } catch (IllegalAccessException e) {
                    throw new ObjectAccessException("Could not call default constructor of "
                        + editorType.getName(), e);
                }
            }

        });
    }

    public String getAsText(Object object) {
        PropertyEditor editor = fetchFromPool();
        try {
            editor.setValue(object);
            return editor.getAsText();
        } finally {
            pool.putInPool(editor);
        }
    }

    public Object setAsText(String str) {
        PropertyEditor editor = fetchFromPool();
        try {
            editor.setAsText(str);
            return editor.getValue();
        } finally {
            pool.putInPool(editor);
        }
    }

    private PropertyEditor fetchFromPool() {
        PropertyEditor editor = (PropertyEditor)pool.fetchFromPool();
        return editor;
    }
}
