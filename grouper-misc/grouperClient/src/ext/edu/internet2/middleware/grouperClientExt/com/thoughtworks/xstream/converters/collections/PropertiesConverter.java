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
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 23. February 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.collections;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.Fields;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Special converter for java.util.Properties that stores
 * properties in a more compact form than java.util.Map.
 * <p/>
 * <p>Because all entries of a Properties instance
 * are Strings, a single element is used for each property
 * with two attributes; one for key and one for value.</p>
 * <p>Additionally, default properties are also serialized, if they are present.</p>
 *
 * @author Joe Walnes
 * @author Kevin Ring
 */
public class PropertiesConverter implements Converter {

    private final static Field defaultsField = Fields.find(Properties.class, "defaults");
    private final boolean sort;

    public PropertiesConverter() {
        this(false);
    }

    public PropertiesConverter(boolean sort) {
        this.sort = sort;
    }

    public boolean canConvert(Class type) {
        return Properties.class == type;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Properties properties = (Properties) source;
        Map map = sort ? (Map)new TreeMap(properties) : (Map)properties;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            writer.startNode("property");
            writer.addAttribute("name", entry.getKey().toString());
            writer.addAttribute("value", entry.getValue().toString());
            writer.endNode();
        }
        Properties defaults = (Properties) Fields.read(defaultsField, properties);
        if (defaults != null) {
            writer.startNode("defaults");
            marshal(defaults, writer, context);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Properties properties = new Properties();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if (reader.getNodeName().equals("defaults")) {
                Properties defaults = (Properties) unmarshal(reader, context);
                Fields.write(defaultsField, properties, defaults);
            } else {
                String name = reader.getAttribute("name");
                String value = reader.getAttribute("value");
                properties.setProperty(name, value);
            }
            reader.moveUp();
        }
        return properties;
    }

}
