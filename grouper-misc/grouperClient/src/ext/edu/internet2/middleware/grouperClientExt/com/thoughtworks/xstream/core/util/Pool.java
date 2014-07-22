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
 * Copyright (c) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 10. May 2007 by Joerg Schaible
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util;

/**
 * A simple pool implementation.
 *
 * @author J&ouml;rg Schaible
 * @author Joe Walnes
 */
public class Pool {
    
    public interface Factory {
        public Object newInstance();
    }

    private final int initialPoolSize;
    private final int maxPoolSize;
    private final Factory factory;
    private transient Object[] pool;
    private transient int nextAvailable;
    private transient Object mutex = new Object();

    public Pool(int initialPoolSize, int maxPoolSize, Factory factory) {
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.factory = factory;
    }

    public Object fetchFromPool() {
        Object result;
        synchronized (mutex) {
            if (pool == null) {
                pool = new Object[maxPoolSize];
                for (nextAvailable = initialPoolSize; nextAvailable > 0; ) {
                    putInPool(factory.newInstance());
                }
            }
            while (nextAvailable == maxPoolSize) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted whilst waiting " +
                            "for a free item in the pool : " + e.getMessage());
                }
            }
            result = pool[nextAvailable++];
            if (result == null) {
                result = factory.newInstance();
                putInPool(result);
                ++nextAvailable;
            }
        }
        return result;
    }

    protected void putInPool(Object object) {
        synchronized (mutex) {
            pool[--nextAvailable] = object;
            mutex.notify();
        }
    }
    
    private Object readResolve() {
        mutex = new Object();
        return this;
    }
}
