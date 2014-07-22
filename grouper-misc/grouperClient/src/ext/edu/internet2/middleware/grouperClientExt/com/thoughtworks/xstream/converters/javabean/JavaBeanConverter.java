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
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 12. April 2005 by Joe Walnes
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.javabean;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.alias.ClassMapper;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;

/**
 * Can convert any bean with a public default constructor. BeanInfo are not
 * taken into consideration, this class looks for bean patterns for simple
 * properties
 */
public class JavaBeanConverter implements Converter {

    /*
     * TODO:
     *  - support indexed properties
     */
    private Mapper mapper;
    private BeanProvider beanProvider;
    /**
     * @deprecated since 1.2, no necessity for field anymore.
     */
    private String classAttributeIdentifier;

    public JavaBeanConverter(Mapper mapper) {
        this(mapper, new BeanProvider());
    }

    public JavaBeanConverter(Mapper mapper, BeanProvider beanProvider) {
        this.mapper = mapper;
        this.beanProvider = beanProvider;
    }

    /**
     * @deprecated As of 1.3, use {@link #JavaBeanConverter(Mapper)} and {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream#aliasAttribute(String, String)}
     */
    public JavaBeanConverter(Mapper mapper, String classAttributeIdentifier) {
        this(mapper, new BeanProvider());
        this.classAttributeIdentifier = classAttributeIdentifier;
    }

    /**
     * @deprecated As of 1.2, use {@link #JavaBeanConverter(Mapper)} and {@link edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream#aliasAttribute(String, String)}
     */
    public JavaBeanConverter(ClassMapper classMapper, String classAttributeIdentifier) {
        this((Mapper)classMapper, classAttributeIdentifier);
    }

    /**
     * Only checks for the availability of a public default constructor.
     * If you need stricter checks, subclass JavaBeanConverter
     */
    public boolean canConvert(Class type) {
        return beanProvider.canInstantiate(type);
    }

    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final String classAttributeName = classAttributeIdentifier != null ? classAttributeIdentifier : mapper.attributeForAlias("class");
        beanProvider.visitSerializableProperties(source, new BeanProvider.Visitor() {
            public void visit(String propertyName, Class fieldType, Class definedIn, Object newObj) {
                if (newObj != null && mapper.shouldSerializeMember(definedIn, propertyName)) {
                    writeField(propertyName, fieldType, newObj, definedIn);
                }
            }

            private void writeField(String propertyName, Class fieldType, Object newObj, Class definedIn) {
                String serializedMember = mapper.serializedMember(source.getClass(), propertyName);
				ExtendedHierarchicalStreamWriterHelper.startNode(writer, serializedMember, fieldType);
                Class actualType = newObj.getClass();

                Class defaultType = mapper.defaultImplementationOf(fieldType);
                if (!actualType.equals(defaultType)) {
                    writer.addAttribute(classAttributeName, mapper.serializedClass(actualType));
                }
                context.convertAnother(newObj);

                writer.endNode();
            }

        });
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final Object result = instantiateNewInstance(context);

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String propertyName = mapper.realMember(result.getClass(), reader.getNodeName());

            boolean propertyExistsInClass = beanProvider.propertyDefinedInClass(propertyName, result.getClass());

            if (propertyExistsInClass) {
                Class type = determineType(reader, result, propertyName);
                Object value = context.convertAnother(result, type);
                beanProvider.writeProperty(result, propertyName, value);
            } else if (mapper.shouldSerializeMember(result.getClass(), propertyName)) {
                throw new ConversionException("Property '" + propertyName + "' not defined in class " + result.getClass().getName());
            }

            reader.moveUp();
        }

        return result;
    }

    private Object instantiateNewInstance(UnmarshallingContext context) {
        Object result = context.currentObject();
        if (result == null) {
            result = beanProvider.newInstance(context.getRequiredType());
        }
        return result;
    }

    private Class determineType(HierarchicalStreamReader reader, Object result, String fieldName) {
        final String classAttributeName = classAttributeIdentifier != null ? classAttributeIdentifier : mapper.attributeForAlias("class");
        String classAttribute = reader.getAttribute(classAttributeName);
        if (classAttribute != null) {
            return mapper.realClass(classAttribute);
        } else {
            return mapper.defaultImplementationOf(beanProvider.getPropertyType(result, fieldName));
        }
    }

    /**
     * @deprecated since 1.3
     */
    public static class DuplicateFieldException extends ConversionException {
        public DuplicateFieldException(String msg) {
            super(msg);
        }
    }
}
