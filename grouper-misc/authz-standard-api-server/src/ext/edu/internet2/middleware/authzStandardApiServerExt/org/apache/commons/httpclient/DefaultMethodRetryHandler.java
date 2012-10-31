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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/DefaultMethodRetryHandler.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

package edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient;

import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient.HttpConnection;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient.HttpMethod;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient.HttpMethodBase;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient.HttpRecoverableException;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient.MethodRetryHandler;

/**
 * The default MethodRetryHandler used by HttpMethodBase.
 * 
 * @author Michael Becke
 * 
 * @see HttpMethodBase#setMethodRetryHandler(MethodRetryHandler)
 * 
 * @deprecated use {@link edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler}
 */
public class DefaultMethodRetryHandler implements MethodRetryHandler {

    /** the number of times a method will be retried */
    private int retryCount;
    
    /** Whether or not methods that have successfully sent their request will be retried */
    private boolean requestSentRetryEnabled;
    
    /**
     */
    public DefaultMethodRetryHandler() {
        this.retryCount = 3;
        this.requestSentRetryEnabled = false;
    }
    
    /** 
     * Used <code>retryCount</code> and <code>requestSentRetryEnabled</code> to determine
     * if the given method should be retried.
     * 
     * @see MethodRetryHandler#retryMethod(HttpMethod, HttpConnection, HttpRecoverableException, int, boolean)
     */
    public boolean retryMethod(
        HttpMethod method,
        HttpConnection connection,
        HttpRecoverableException recoverableException,
        int executionCount,
        boolean requestSent
    ) {
        return ((!requestSent || requestSentRetryEnabled) && (executionCount <= retryCount));
    }
    /**
     * @return <code>true</code> if this handler will retry methods that have 
     * successfully sent their request, <code>false</code> otherwise
     */
    public boolean isRequestSentRetryEnabled() {
        return requestSentRetryEnabled;
    }

    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * @param requestSentRetryEnabled a flag indicating if methods that have 
     * successfully sent their request should be retried
     */
    public void setRequestSentRetryEnabled(boolean requestSentRetryEnabled) {
        this.requestSentRetryEnabled = requestSentRetryEnabled;
    }

    /**
     * @param retryCount the maximum number of times a method can be retried
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

}
