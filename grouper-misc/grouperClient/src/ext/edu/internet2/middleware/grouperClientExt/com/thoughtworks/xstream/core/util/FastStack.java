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
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 02. September 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util;

/**
 * An array-based stack implementation.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public final class FastStack {

    private Object[] stack;
    private int pointer;

    public FastStack(int initialCapacity) {
        stack = new Object[initialCapacity];
    }

    public Object push(Object value) {
        if (pointer + 1 >= stack.length) {
            resizeStack(stack.length * 2);
        }
        stack[pointer++] = value;
        return value;
    }

    public void popSilently() {
        stack[--pointer] = null;
    }

    public Object pop() {
        final Object result = stack[--pointer]; 
        stack[pointer] = null; 
        return result;
    }

    public Object peek() {
        return pointer == 0 ? null : stack[pointer - 1];
    }

    public int size() {
        return pointer;
    }

    public boolean hasStuff() {
        return pointer > 0;
    }

    public Object get(int i) {
        return stack[i];
    }

    private void resizeStack(int newCapacity) {
        Object[] newStack = new Object[newCapacity];
        System.arraycopy(stack, 0, newStack, 0, Math.min(pointer, newCapacity));
        stack = newStack;
    }

    public String toString() {
        StringBuffer result = new StringBuffer("[");
        for (int i = 0; i < pointer; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(stack[i]);
        }
        result.append(']');
        return result.toString();
    }
}
