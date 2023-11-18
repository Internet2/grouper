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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/methods/HeadMethod.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods;

import java.io.IOException;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpConnection;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpException;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpMethodBase;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpState;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.ProtocolException;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * Implements the HTTP HEAD method.
 * <p>
 * The HTTP HEAD method is defined in section 9.4 of 
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * <blockquote>
 * The HEAD method is identical to GET except that the server MUST NOT
 * return a message-body in the response. The metainformation contained
 * in the HTTP headers in response to a HEAD request SHOULD be identical
 * to the information sent in response to a GET request. This method can
 * be used for obtaining metainformation about the entity implied by the
 * request without transferring the entity-body itself. This method is
 * often used for testing hypertext links for validity, accessibility,
 * and recent modification.
 * </blockquote>
 * </p>
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:oleg@ural.ru">oleg Kalnichevski</a>
 *
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public class HeadMethod extends HttpMethodBase {
    //~ Static variables/initializers

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HeadMethod.class);

    //~ Constructors

    /**
     * No-arg constructor.
     * 
     * @since 1.0
     */
    public HeadMethod() {
        setFollowRedirects(true);
    }

    /**
     * Constructor specifying a URI.
     *
     * @param uri either an absolute or relative URI
     * 
     * @since 1.0
     */
    public HeadMethod(String uri) {
        super(uri);
        setFollowRedirects(true);
    }

    //~ Methods

    /**
     * Returns <tt>"HEAD"</tt>.
     * 
     * @return <tt>"HEAD"</tt>
     * 
     * @since 2.0
     */
    public String getName() {
        return "HEAD";
    }

    /**
     * Recycles the HTTP method so that it can be used again.
     * Note that all of the instance variables will be reset
     * once this method has been called. This method will also
     * release the connection being used by this HTTP method.
     * 
     * @see #releaseConnection()
     * 
     * @since 1.0
     * 
     * @deprecated no longer supported and will be removed in the future
     *             version of HttpClient
     */
    public void recycle() {
        super.recycle();
        setFollowRedirects(true);
    }

    /**
     * Overrides {@link HttpMethodBase} method to <i>not</i> read a response
     * body, despite the presence of a <tt>Content-Length</tt> or
     * <tt>Transfer-Encoding</tt> header.
     * 
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs. Some transport exceptions
     *                     can be recovered from.
     * @throws HttpException  if a protocol exception occurs. Usually protocol exceptions 
     *                    cannot be recovered from.
     *
     * @see #readResponse
     * @see #processResponseBody
     * 
     * @since 2.0
     */
    protected void readResponseBody(HttpState state, HttpConnection conn)
    throws HttpException, IOException {
        LOG.trace(
            "enter HeadMethod.readResponseBody(HttpState, HttpConnection)");
        
        int bodyCheckTimeout = 
            getParams().getIntParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, -1);

        if (bodyCheckTimeout < 0) {
            responseBodyConsumed();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Check for non-compliant response body. Timeout in " 
                 + bodyCheckTimeout + " ms");    
            }
            boolean responseAvailable = false;
            try {
                responseAvailable = conn.isResponseAvailable(bodyCheckTimeout);
            } catch (IOException e) {
                LOG.debug("An IOException occurred while testing if a response was available,"
                    + " we will assume one is not.", 
                    e);
                responseAvailable = false;
            }
            if (responseAvailable) {
                if (getParams().isParameterTrue(HttpMethodParams.REJECT_HEAD_BODY)) {
                    throw new ProtocolException(
                        "Body content may not be sent in response to HTTP HEAD request");
                } else {
                    LOG.warn("Body content returned in response to HTTP HEAD");    
                }
                super.readResponseBody(state, conn);
            }
        }

    }
    
    /**
     * Returns non-compliant response body check timeout.
     * 
     * @return The period of time in milliseconds to wait for a response 
     *         body from a non-compliant server. <tt>-1</tt> returned when 
     *         non-compliant response body check is disabled
     * 
     * @deprecated Use {@link HttpMethodParams}
     * 
     * @see #getParams()
     * @see HttpMethodParams
     * @see HttpMethodParams#HEAD_BODY_CHECK_TIMEOUT
     */
    public int getBodyCheckTimeout() {
        return getParams().getIntParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, -1);
    }

    /**
     * Sets non-compliant response body check timeout.
     * 
     * @param timeout The period of time in milliseconds to wait for a response 
     *         body from a non-compliant server. <tt>-1</tt> can be used to 
     *         disable non-compliant response body check
     *  
     * @deprecated Use {@link HttpMethodParams}
     * 
     * @see #getParams()
     * @see HttpMethodParams
     * @see HttpMethodParams#HEAD_BODY_CHECK_TIMEOUT
     */
    public void setBodyCheckTimeout(int timeout) {
        getParams().setIntParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, timeout);
    }

}
