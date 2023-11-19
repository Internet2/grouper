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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/params/HttpConnectionManagerParams.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
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

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HostConfiguration;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * This class represents a collection of HTTP protocol parameters applicable to 
 * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpConnectionManager HTTP connection managers}. 
 * Protocol parameters may be linked together to form a hierarchy. If a particular 
 * parameter value has not been explicitly defined in the collection itself, its 
 * value will be drawn from the parent collection of parameters.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Michael Becke
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 3.0
 */
public class HttpConnectionManagerParams extends HttpConnectionParams {

    /** 
     * Defines the maximum number of connections allowed per host configuration. 
     * These values only apply to the number of connections from a particular instance 
     * of HttpConnectionManager.
     * <p>
     * This parameter expects a value of type {@link java.util.Map}.  The value
     * should map instances of {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HostConfiguration}
     * to {@link Integer integers}.  The default value can be specified using
     * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HostConfiguration#ANY_HOST_CONFIGURATION}.
     * </p>
     */
    public static final String MAX_HOST_CONNECTIONS = "http.connection-manager.max-per-host";

    /** 
     * Defines the maximum number of connections allowed overall. This value only applies
     * to the number of connections from a particular instance of HttpConnectionManager.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    public static final String MAX_TOTAL_CONNECTIONS = "http.connection-manager.max-total";
    
    /**
     * Sets the default maximum number of connections allowed for a given
     * host config.
     *
     * @param maxHostConnections The default maximum.
     * 
     * @see #MAX_HOST_CONNECTIONS
     */
    public void setDefaultMaxConnectionsPerHost(int maxHostConnections) {
        setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, maxHostConnections);
    }

    /**
     * Sets the maximum number of connections to be used for the given host config.
     * 
     * @param hostConfiguration The host config to set the maximum for.  Use 
     * {@link HostConfiguration#ANY_HOST_CONFIGURATION} to configure the default value
     * per host.
     * @param maxHostConnections The maximum number of connections, <code>> 0</code>
     * 
     * @see #MAX_HOST_CONNECTIONS
     */
    public void setMaxConnectionsPerHost(
        HostConfiguration hostConfiguration,
        int maxHostConnections) {
        
        if (maxHostConnections <= 0) {
            throw new IllegalArgumentException("maxHostConnections must be greater than 0");
        }
        
        Map currentValues = (Map) getParameter(MAX_HOST_CONNECTIONS);
        // param values are meant to be immutable so we'll make a copy
        // to modify
        Map newValues = null;
        if (currentValues == null) {
            newValues = new HashMap();
        } else {
            newValues = new HashMap(currentValues);
        }
        newValues.put(hostConfiguration, new Integer(maxHostConnections));
        setParameter(MAX_HOST_CONNECTIONS, newValues);
    }
    
    /**
     * Gets the default maximum number of connections allowed for a given
     * host config.
     *
     * @return The default maximum.
     * 
     * @see #MAX_HOST_CONNECTIONS
     */
    public int getDefaultMaxConnectionsPerHost() {
        return getMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION);
    }

    /**
     * Gets the maximum number of connections to be used for a particular host config.  If
     * the value has not been specified for the given host the default value will be
     * returned.
     * 
     * @param hostConfiguration The host config.
     * @return The maximum number of connections to be used for the given host config.
     * 
     * @see #MAX_HOST_CONNECTIONS
     */
    public int getMaxConnectionsPerHost(HostConfiguration hostConfiguration) {
        
        Map m = (Map) getParameter(MAX_HOST_CONNECTIONS);
        if (m == null) {
            // MAX_HOST_CONNECTIONS have not been configured, using the default value
            return MultiThreadedHttpConnectionManager.DEFAULT_MAX_HOST_CONNECTIONS;
        } else {
            Integer max = (Integer) m.get(hostConfiguration);
            if (max == null && hostConfiguration != HostConfiguration.ANY_HOST_CONFIGURATION) {
                // the value has not been configured specifically for this host config,
                // use the default value
                return getMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION);
            } else {
                return (
                        max == null 
                        ? MultiThreadedHttpConnectionManager.DEFAULT_MAX_HOST_CONNECTIONS 
                        : max.intValue()
                    );
            }
        }
    }

    /**
     * Sets the maximum number of connections allowed.
     *
     * @param maxTotalConnections The maximum number of connections allowed.
     * 
     * @see #MAX_TOTAL_CONNECTIONS
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
        setIntParameter(
            HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS,
            maxTotalConnections);
    }

    /**
     * Gets the maximum number of connections allowed.
     *
     * @return The maximum number of connections allowed.
     * 
     * @see #MAX_TOTAL_CONNECTIONS
     */
    public int getMaxTotalConnections() {
        return getIntParameter(
            HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS,
            MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS);
    }

}
