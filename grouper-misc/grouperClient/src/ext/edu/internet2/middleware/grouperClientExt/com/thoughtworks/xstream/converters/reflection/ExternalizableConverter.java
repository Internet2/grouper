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
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 24. August 2004 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.CustomObjectInputStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputValidation;
import java.util.Map;

/**
 * Converts any object that implements the java.io.Externalizable interface, allowing compatability with native Java
 * serialization.
 *
 * @author Joe Walnes
 */
public class ExternalizableConverter implements Converter {

    private Mapper mapper;

    public ExternalizableConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public boolean canConvert(Class type) {
        return Externalizable.class.isAssignableFrom(type);
    }

    public void marshal(Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        try {
            Externalizable externalizable = (Externalizable) source;
            CustomObjectOutputStream.StreamCallback callback = new CustomObjectOutputStream.StreamCallback() {
                public void writeToStream(Object object) {
                    if (object == null) {
                        writer.startNode("null");
                        writer.endNode();
                    } else {
                        ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper.serializedClass(object.getClass()), object.getClass());
                        context.convertAnother(object);
                        writer.endNode();
                    }
                }

                public void writeFieldsToStream(Map fields) {
                    throw new UnsupportedOperationException();
                }

                public void defaultWriteObject() {
                    throw new UnsupportedOperationException();
                }

                public void flush() {
                    writer.flush();
                }

                public void close() {
                    throw new UnsupportedOperationException("Objects are not allowed to call ObjecOutput.close() from writeExternal()");
                }
            };
            CustomObjectOutputStream objectOutput = CustomObjectOutputStream.getInstance(context, callback);
            externalizable.writeExternal(objectOutput);
            objectOutput.popCallback();
        } catch (IOException e) {
            throw new ConversionException("Cannot serialize " + source.getClass().getName() + " using Externalization", e);
        }
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final Class type = context.getRequiredType();
        try {
            final Externalizable externalizable = (Externalizable) type.newInstance();
            CustomObjectInputStream.StreamCallback callback = new CustomObjectInputStream.StreamCallback() {
                public Object readFromStream() {
                    reader.moveDown();
                    Object streamItem = context.convertAnother(externalizable, mapper.realClass(reader.getNodeName()));
                    reader.moveUp();
                    return streamItem;
                }

                public Map readFieldsFromStream() {
                    throw new UnsupportedOperationException();
                }

                public void defaultReadObject() {
                    throw new UnsupportedOperationException();
                }

                public void registerValidation(ObjectInputValidation validation, int priority) throws NotActiveException {
                    throw new NotActiveException("stream inactive");
                }

                public void close() {
                    throw new UnsupportedOperationException("Objects are not allowed to call ObjectInput.close() from readExternal()");
                }
            };
            CustomObjectInputStream objectInput = CustomObjectInputStream.getInstance(context, callback);
            externalizable.readExternal(objectInput);
            objectInput.popCallback();
            return externalizable;
        } catch (InstantiationException e) {
            throw new ConversionException("Cannot construct " + type.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new ConversionException("Cannot construct " + type.getClass(), e);
        } catch (IOException e) {
            throw new ConversionException("Cannot externalize " + type.getClass(), e);
        } catch (ClassNotFoundException e) {
            throw new ConversionException("Cannot externalize " + type.getClass(), e);
        }
    }
}
