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
 * Copyright (C) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 13. June 2006 by Guilherme Silveira
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.persistence;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A persistent map. Its values are actually serialized as xml files. If you
 * need an application-wide synchronized version of this map, try the respective
 * Collections methods.
 * 
 * @author Guilherme Silveira
 */
public class XmlMap extends AbstractMap {

	private final StreamStrategy streamStrategy;

	public XmlMap(StreamStrategy streamStrategy) {
		this.streamStrategy = streamStrategy;
	}

	public int size() {
		return streamStrategy.size();
	}

	public Object get(Object key) {
		// faster lookup
		return streamStrategy.get(key);
	}

	public Object put(Object key, Object value) {
		return streamStrategy.put(key,value);
	}

	public Object remove(Object key) {
		return streamStrategy.remove(key);
	}

	public Set entrySet() {
		return new XmlMapEntries();
	}

	class XmlMapEntries extends AbstractSet {

		public int size() {
			return XmlMap.this.size();
		}

		public boolean isEmpty() {
			return XmlMap.this.isEmpty();
		}

		public Iterator iterator() {
			return streamStrategy.iterator();
		}

	}

}
