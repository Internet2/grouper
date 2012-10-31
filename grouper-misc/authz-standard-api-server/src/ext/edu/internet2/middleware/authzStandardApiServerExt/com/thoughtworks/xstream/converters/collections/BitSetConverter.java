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
 * Created on 07. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.collections;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.BitSet;
import java.util.StringTokenizer;

/**
 * Converts a java.util.BitSet to XML, as a compact
 * comma delimited list of ones and zeros.
 *
 * @author Joe Walnes
 */
public class BitSetConverter implements Converter {

    public boolean canConvert(Class type) {
        return type.equals(BitSet.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        BitSet bitSet = (BitSet) source;
        StringBuffer buffer = new StringBuffer();
        boolean seenFirst = false;
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                if (seenFirst) {
                    buffer.append(',');
                } else {
                    seenFirst = true;
                }
                buffer.append(i);
            }
        }
        writer.setValue(buffer.toString());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        BitSet result = new BitSet();
        StringTokenizer tokenizer = new StringTokenizer(reader.getValue(), ",", false);
        while (tokenizer.hasMoreTokens()) {
            int index = Integer.parseInt(tokenizer.nextToken());
            result.set(index);
        }
        return result;
    }
}
