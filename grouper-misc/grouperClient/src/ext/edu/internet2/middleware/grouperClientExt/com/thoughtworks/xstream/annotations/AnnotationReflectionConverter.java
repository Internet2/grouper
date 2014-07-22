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
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 02. March 2006 by Mauro Talevi
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.Mapper;


/**
 * ReflectionConverter which uses an AnnotationProvider to marshall and unmarshall fields based
 * on the annotated converters.
 * 
 * @author Guilherme Silveira
 * @author Mauro Talevi
 * @deprecated since 1.3, build into {@link ReflectionConverter}
 */
@Deprecated
public class AnnotationReflectionConverter extends ReflectionConverter {

    private final AnnotationProvider annotationProvider;

    private final Map<Class<? extends Converter>, Converter> cachedConverters;

    @Deprecated
    public AnnotationReflectionConverter(
                                         Mapper mapper, ReflectionProvider reflectionProvider,
                                         AnnotationProvider annotationProvider) {
        super(mapper, reflectionProvider);
        this.annotationProvider = annotationProvider;
        this.cachedConverters = new HashMap<Class<? extends Converter>, Converter>();
    }

    protected void marshallField(final MarshallingContext context, Object newObj, Field field) {
        XStreamConverter annotation = annotationProvider.getAnnotation(
            field, XStreamConverter.class);
        if (annotation != null) {
            Class<? extends Converter> type = annotation.value();
            ensureCache(type);
            context.convertAnother(newObj, cachedConverters.get(type));
        } else {
            context.convertAnother(newObj);
        }
    }

    private void ensureCache(Class<? extends Converter> type) {
        if (!this.cachedConverters.containsKey(type)) {
            cachedConverters.put(type, newInstance(type));
        }
    }

    protected Object unmarshallField(
                                     final UnmarshallingContext context, final Object result,
                                     Class type, Field field) {
        XStreamConverter annotation = annotationProvider.getAnnotation(
            field, XStreamConverter.class);
        if (annotation != null) {
            Class<? extends Converter> converterType = annotation.value();
            ensureCache(converterType);
            return context.convertAnother(result, type, cachedConverters.get(converterType));
        } else {
            return context.convertAnother(result, type);
        }
    }

    /**
     * Instantiates a converter using its default constructor.
     * 
     * @param converterType the converter type to instantiate
     * @return the new instance
     */
    private Converter newInstance(Class<? extends Converter> converterType) {
        Converter converter;
        // TODO: We need a separate exception for runtime initialization.
        try {
            converter = converterType.getConstructor().newInstance();
        } catch (InvocationTargetException e) {
            throw new ObjectAccessException("Cannot construct " + converterType.getName(), e
                .getCause());
        } catch (InstantiationException e) {
            throw new ObjectAccessException("Cannot construct " + converterType.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ObjectAccessException("Cannot construct " + converterType.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new ObjectAccessException("Cannot construct " + converterType.getName(), e);
        }
        return converter;
    }

}
