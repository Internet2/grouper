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
 * Created on 03. March 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.extended;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.basic.ByteConverter;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.core.util.Base64Encoder;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts a byte array to a single Base64 encoding string.
 *
 * @author Joe Walnes
 */
public class EncodedByteArrayConverter implements Converter {

    private static final Base64Encoder base64 = new Base64Encoder();
    private static final ByteConverter byteConverter = new ByteConverter();

    public boolean canConvert(Class type) {
        return type.isArray() && type.getComponentType().equals(byte.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.setValue(base64.encode((byte[]) source));
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String data = reader.getValue(); // needs to be called before hasMoreChildren.
        if (!reader.hasMoreChildren()) {
            return base64.decode(data);
        } else {
            // backwards compatability ... try to unmarshal byte arrays that haven't been encoded
            return unmarshalIndividualByteElements(reader, context);
        }
    }

    private Object unmarshalIndividualByteElements(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List bytes = new ArrayList(); // have to create a temporary list because don't know the size of the array
        boolean firstIteration = true;
        while (firstIteration || reader.hasMoreChildren()) { // hangover from previous hasMoreChildren
            reader.moveDown();
            //bytes.add(byteConverter.unmarshal(reader, context));
            bytes.add(byteConverter.fromString(reader.getValue()));
            reader.moveUp();
            firstIteration = false;
        }
        // copy into real array
        byte[] result = new byte[bytes.size()];
        int i = 0;
        for (Iterator iterator = bytes.iterator(); iterator.hasNext();) {
            Byte b = (Byte) iterator.next();
            result[i] = b.byteValue();
            i++;
        }
        return result;
    }

}
