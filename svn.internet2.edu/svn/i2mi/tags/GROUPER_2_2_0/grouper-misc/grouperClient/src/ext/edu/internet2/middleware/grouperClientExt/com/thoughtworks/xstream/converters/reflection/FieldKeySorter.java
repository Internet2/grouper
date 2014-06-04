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
 * Copyright (C) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 10. April 2007 by Guilherme Silveira
 */
package edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.reflection;

import java.util.Map;


/**
 * An interface capable of sorting fields. Implement this interface if you want to customize the
 * field order in which XStream serializes objects.
 * 
 * @author Guilherme Silveira
 * @since 1.2.2
 */
public interface FieldKeySorter {

    /**
     * Sort the fields of a type. The method will be called with the class type that contains
     * all the fields and a Map that retains the order in which the elements have been added.
     * The sequence in which elements are returned by an iterator defines the processing order
     * of the fields. An implementation may create a different Map with similar semantic, add
     * all elements of the original map and return the new one.
     * 
     * @param type the class that contains all the fields
     * @param keyedByFieldKey a Map containing a {@link FieldKey} as key element and a
     *                {@link java.lang.reflect.Field} as value.
     * @return a Map with all the entries of the original Map
     * @since 1.2.2
     */
    Map sort(Class type, Map keyedByFieldKey);

}
