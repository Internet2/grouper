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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a map in a context.
 * <p>Each entry in the map is considered a variable name, value pair.</p>
 */
public class MapContext implements JexlContext {
    /**
     * The wrapped variable map.
     */
    protected final Map<String, Object> map;

    /**
     * Creates a MapContext on an automatically allocated underlying HashMap.
     */
    public MapContext() {
        this(null);
    }

    /**
     * Creates a MapContext wrapping an existing user provided map.
     * @param vars the variable map
     */
    public MapContext(Map<String, Object> vars) {
        super();
        map = vars == null ? new HashMap<String, Object>() : vars;
    }

    /** {@inheritDoc} */
    public boolean has(String name) {
        return map.containsKey(name);
    }

    /** {@inheritDoc} */
    public Object get(String name) {
        return map.get(name);
    }

    /** {@inheritDoc} */
    public void set(String name, Object value) {
        map.put(name, value);
    }
}
