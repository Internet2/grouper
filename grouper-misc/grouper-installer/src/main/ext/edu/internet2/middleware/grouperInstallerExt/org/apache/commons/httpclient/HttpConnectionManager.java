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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/HttpConnectionManager.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
 * $Revision: 1.1 $
 * $Date: 2008-11-30 10:57:19 $
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

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * An interface for classes that manage HttpConnections.
 * 
 * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpConnection
 * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpClient#HttpClient(HttpConnectionManager)
 *
 * @author Michael Becke
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0
 */
public interface HttpConnectionManager {

    /**
     * Gets an HttpConnection for a given host configuration. If a connection is
     * not available this method will block until one is.
     *
     * The connection manager should be registered with any HttpConnection that
     * is created.
     *
     * @param hostConfiguration the host configuration to use to configure the
     * connection
     * 
     * @return an HttpConnection for the given configuration
     * 
     * @see HttpConnection#setHttpConnectionManager(HttpConnectionManager)
     */
    HttpConnection getConnection(HostConfiguration hostConfiguration);

    /**
     * Gets an HttpConnection for a given host configuration. If a connection is
     * not available, this method will block for at most the specified number of
     * milliseconds or until a connection becomes available.
     *
     * The connection manager should be registered with any HttpConnection that
     * is created.
     *
     * @param hostConfiguration the host configuration to use to configure the
     * connection
     * @param timeout - the time (in milliseconds) to wait for a connection to
     * become available, 0 to specify an infinite timeout
     * 
     * @return an HttpConnection for the given configuraiton
     * 
     * @throws HttpException if no connection becomes available before the
     * timeout expires
     * 
     * @see HttpConnection#setHttpConnectionManager(HttpConnectionManager)
     * 
     * @deprecated Use #getConnectionWithTimeout(HostConfiguration, long) 
     */
    HttpConnection getConnection(HostConfiguration hostConfiguration, long timeout)
        throws HttpException;

    /**
     * Gets an HttpConnection for a given host configuration. If a connection is
     * not available, this method will block for at most the specified number of
     * milliseconds or until a connection becomes available.
     *
     * The connection manager should be registered with any HttpConnection that
     * is created.
     *
     * @param hostConfiguration the host configuration to use to configure the
     * connection
     * @param timeout - the time (in milliseconds) to wait for a connection to
     * become available, 0 to specify an infinite timeout
     * 
     * @return an HttpConnection for the given configuraiton
     * 
     * @throws ConnectionPoolTimeoutException if no connection becomes available before the
     * timeout expires
     * 
     * @see HttpConnection#setHttpConnectionManager(HttpConnectionManager)
     * 
     * @since 3.0
     */
    HttpConnection getConnectionWithTimeout(HostConfiguration hostConfiguration, long timeout)
        throws ConnectionPoolTimeoutException;

    /**
     * Releases the given HttpConnection for use by other requests.
     *
     * @param conn - The HttpConnection to make available.
     */
    void releaseConnection(HttpConnection conn);

    /**
     * Closes connections that have been idle for at least the given amount of time.  Only
     * connections that are currently owned, not checked out, are subject to idle timeouts.
     * 
     * @param idleTimeout the minimum idle time, in milliseconds, for connections to be closed
     * 
     * @since 3.0
     */
    void closeIdleConnections(long idleTimeout);
    
    /**
     * Returns {@link HttpConnectionManagerParams parameters} associated 
     * with this connection manager.
     * 
     * @since 3.0
     * 
     * @see HttpConnectionManagerParams
     */
    HttpConnectionManagerParams getParams();

    /**
     * Assigns {@link HttpConnectionManagerParams parameters} for this 
     * connection manager.
     * 
     * @since 3.0
     * 
     * @see HttpConnectionManagerParams
     */
    void setParams(final HttpConnectionManagerParams params);
}
