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
 * Created on 28. June 2006 by Guilherme Silveira
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.persistence;

import java.util.AbstractSet;
import java.util.Iterator;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.persistence.StreamStrategy;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.persistence.XmlMap;

/**
 * A persistent set implementation.
 * 
 * @author Guilherme Silveira
 */
public class XmlSet extends AbstractSet {

	private final XmlMap map;

	public XmlSet(StreamStrategy streamStrategy) {
		this.map = new XmlMap(streamStrategy);
	}

	public Iterator iterator() {
		return map.values().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean add(Object o) {
		if (map.containsValue(o)) {
			return false;
		} else {
			// not-synchronized!
			map.put(findEmptyKey(), o);
			return true;
		}
	}

	private String findEmptyKey() {
		long i = System.currentTimeMillis();
		while (map.containsKey("" + i)) {
			i++;
		}
		return "" + i;
	}

}
