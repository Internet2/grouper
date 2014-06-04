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

package edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.introspection;

/**
 * Interface used for regular method invocation.
 * Ex.
 * <code>
 * ${foo.bar()}
 * </code>
 * 
 * @since 1.0
 */
public interface JexlMethod {
    /**
     * Invocation method, called when the method invocation should be performed
     * and a value returned.

     * @param obj the object
     * @param params method parameters.
     * @return the result
     * @throws Exception on any error.
     */
    Object invoke(Object obj, Object[] params) throws Exception;

    /**
     * Attempts to reuse this JexlMethod, checking that it is compatible with
     * the actual set of arguments.
     * Related to isCacheable since this method is often used with cached JexlMethod instances.
     * @param obj the object to invoke the method upon
     * @param name the method name
     * @param params the method arguments
     * @return the result of the method invocation that should be checked by tryFailed to determine if it succeeded
     * or failed.
     */
    Object tryInvoke(String name, Object obj, Object[] params);

    /**
     * Checks whether a tryInvoke failed or not.
     * @param rval the value returned by tryInvoke
     * @return true if tryInvoke failed, false otherwise
     */
    boolean tryFailed(Object rval);

    /**
     * Specifies if this JexlMethod is cacheable and able to be reused for this
     * class of object it was returned for.
     * 
     * @return true if can be reused for this class, false if not
     */
    boolean isCacheable();

    /**
     * returns the return type of the method invoked.
     * @return return type
     */
    Class<?> getReturnType();
}
