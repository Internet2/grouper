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
 * Copyright (C) 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. January 2005 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper.Mapper;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.alias.ClassMapper;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.Converter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.SingleValueConverter;

public abstract class MapperWrapper implements Mapper {

    private final Mapper wrapped;

    public MapperWrapper(Mapper wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @deprecated As of 1.2, use {@link #MapperWrapper(Mapper)}
     */
    public MapperWrapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    public String serializedClass(Class type) {
        return wrapped.serializedClass(type);
    }

    public Class realClass(String elementName) {
        return wrapped.realClass(elementName);
    }

    public String serializedMember(Class type, String memberName) {
        return wrapped.serializedMember(type, memberName);
    }

    public String realMember(Class type, String serialized) {
        return wrapped.realMember(type, serialized);
    }

    public boolean isImmutableValueType(Class type) {
        return wrapped.isImmutableValueType(type);
    }

    public Class defaultImplementationOf(Class type) {
        return wrapped.defaultImplementationOf(type);
    }

    /**
     * @deprecated since 1.2, use aliasForAttribute instead.
     */
    public String attributeForClassDefiningField() {
        return wrapped.attributeForClassDefiningField();
    }

    /**
     * @deprecated since 1.2, use aliasForAttribute instead.
     */
    public String attributeForImplementationClass() {
        return wrapped.attributeForImplementationClass();
    }

    /**
     * @deprecated since 1.2, use aliasForAttribute instead.
     */
    public String attributeForReadResolveField() {
        return wrapped.attributeForReadResolveField();
    }

    /**
     * @deprecated since 1.2, use aliasForAttribute instead.
     */
    public String attributeForEnumType() {
        return wrapped.attributeForEnumType();
    }

    public String aliasForAttribute(String attribute) {
        return wrapped.aliasForAttribute(attribute);
    }

    public String attributeForAlias(String alias) {
        return wrapped.attributeForAlias(alias);
    }

    public String getFieldNameForItemTypeAndName(Class definedIn, Class itemType, String itemFieldName) {
        return wrapped.getFieldNameForItemTypeAndName(definedIn, itemType, itemFieldName);
    }

    public Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName) {
        return wrapped.getItemTypeForItemFieldName(definedIn, itemFieldName);
    }

    public ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType, String fieldName) {
        return wrapped.getImplicitCollectionDefForFieldName(itemType, fieldName);
    }

    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
        return wrapped.shouldSerializeMember(definedIn, fieldName);
    }

    /**
     * @deprecated since 1.3, use {@link #getConverterFromItemType(String, Class, Class)}
     */
    public SingleValueConverter getConverterFromItemType(String fieldName, Class type) {
        return wrapped.getConverterFromItemType(fieldName, type);
    }

    /**
     * @deprecated since 1.3, use {@link #getConverterFromItemType(String, Class, Class)}
     */
    public SingleValueConverter getConverterFromItemType(Class type) {
        return wrapped.getConverterFromItemType(type);
    }

    /**
     * @deprecated since 1.3, use {@link #getConverterFromAttribute(Class, String)}
     */
    public SingleValueConverter getConverterFromAttribute(String name) {
        return wrapped.getConverterFromAttribute(name);
    }

    public Converter getLocalConverter(Class definedIn, String fieldName) {
        return wrapped.getLocalConverter(definedIn, fieldName);
    }

    public Mapper lookupMapperOfType(Class type) {
        return type.isAssignableFrom(getClass()) ? this : wrapped.lookupMapperOfType(type);
    }
    
    public SingleValueConverter getConverterFromItemType(String fieldName, Class type, Class definedIn) {
    	return wrapped.getConverterFromItemType(fieldName, type, definedIn);
    }
    
    /**
     * @deprecated since 1.3, use combination of {@link #serializedMember(Class, String)} and {@link #getConverterFromItemType(String, Class, Class)} 
     */
    public String aliasForAttribute(Class definedIn, String fieldName) {
    	return wrapped.aliasForAttribute(definedIn, fieldName);
    }
    
    /**
     * @deprecated since 1.3, use combination of {@link #realMember(Class, String)} and {@link #getConverterFromItemType(String, Class, Class)} 
     */
    public String attributeForAlias(Class definedIn, String alias) {
    	return wrapped.attributeForAlias(definedIn, alias);
    }
    
    public SingleValueConverter getConverterFromAttribute(Class type, String attribute) {
    	return wrapped.getConverterFromAttribute(type, attribute);
    }

}
