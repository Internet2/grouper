/*
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/params/HttpParamsFactory.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
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
 * A factory for getting the default set of parameters to use when creating an instance of 
 * <code>HttpParams</code>.
 * 
 * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params.DefaultHttpParams#setHttpParamsFactory(HttpParamsFactory)
 * 
 * @since 3.0
 */
public interface HttpParamsFactory {

    /**
     * Gets the default parameters.  This method may be called more than once and is not required
     * to always return the same value.
     * 
     * @return an instance of HttpParams
     */
    HttpParams getDefaultParams();

}
