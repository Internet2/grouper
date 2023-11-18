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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/params/HostParams.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
 * $Revision: 1.1 $
 * $Date: 2008-11-30 10:57:20 $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params;



/**
 * This class represents a collection of HTTP protocol parameters applicable to 
 * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HostConfiguration instances of HostConfiguration}. 
 * Protocol parameters may be linked together to form a hierarchy. If a particular 
 * parameter value has not been explicitly defined in the collection itself, its 
 * value will be drawn from the parent collection of parameters.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 3.0
 */
public class HostParams extends DefaultHttpParams {

    /**
     * Defines the request headers to be sent per default with each request.
     * <p>
     * This parameter expects a value of type {@link java.util.Collection}. The 
     * collection is expected to contain {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Header}s. 
     * </p>
     */
    public static final String DEFAULT_HEADERS = "http.default-headers"; 

    /**
     * Creates a new collection of parameters with the collection returned
     * by {@link #getDefaultParams()} as a parent. The collection will defer
     * to its parent for a default value if a particular parameter is not 
     * explicitly set in the collection itself.
     * 
     * @see #getDefaultParams()
     */
    public HostParams() {
        super();
    }

    /**
     * Creates a new collection of parameters with the given parent. 
     * The collection will defer to its parent for a default value 
     * if a particular parameter is not explicitly set in the collection
     * itself.
     * 
     * @param defaults the parent collection to defer to, if a parameter
     * is not explictly set in the collection itself.
     *
     * @see #getDefaultParams()
     */
    public HostParams(HttpParams defaults) {
        super(defaults);
    }
    
    /**
     * Sets the virtual host name.
     * 
     * @param hostname The host name
     */
    public void setVirtualHost(final String hostname) {
        setParameter(HttpMethodParams.VIRTUAL_HOST, hostname);
    }

    /**
     * Returns the virtual host name.
     * 
     * @return The virtual host name
     */
    public String getVirtualHost() {
        return (String) getParameter(HttpMethodParams.VIRTUAL_HOST);
    }
        
}
