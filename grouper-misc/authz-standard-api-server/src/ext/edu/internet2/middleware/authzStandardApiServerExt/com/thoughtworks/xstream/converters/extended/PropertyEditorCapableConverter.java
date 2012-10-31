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
 * Copyright (C) 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 20.09.2007 by Joerg Schaible
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.extended;

import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.SingleValueConverter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.util.ThreadSafePropertyEditor;

import java.beans.PropertyEditor;


/**
 * A SingleValueConverter that can utilize a {@link PropertyEditor} implementation used for a
 * specific type. The converter ensures that the editors can be used concurrently.
 * 
 * @author Jukka Lindstr&ouml;m
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class PropertyEditorCapableConverter implements SingleValueConverter {

    private final ThreadSafePropertyEditor editor;
    private final Class type;

    public PropertyEditorCapableConverter(final Class propertyEditorType, final Class type) {
        this.type = type;
        editor = new ThreadSafePropertyEditor(propertyEditorType, 2, 5);
    }

    public boolean canConvert(final Class type) {
        return this.type == type;
    }

    public Object fromString(final String str) {
        return editor.setAsText(str);
    }

    public String toString(final Object obj) {
        return editor.getAsText(obj);
    }

}
