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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/auth/AuthChallengeProcessor.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
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

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.auth;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params.HttpParams;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * This class provides utility methods for processing HTTP www and proxy authentication 
 * challenges.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 3.0
 */
public final class AuthChallengeProcessor {

    private static final Log LOG = LogFactory.getLog(AuthChallengeProcessor.class);

    private HttpParams params = null;
    
    /**
     * Creates an authentication challenge processor with the given {@link HttpParams HTTP 
     * parameters}
     * 
     * @param params the {@link HttpParams HTTP parameters} used by this processor
     */
    public AuthChallengeProcessor(final HttpParams params) {
        super();
        if (params == null) {
            throw new IllegalArgumentException("Parameter collection may not be null");
        }
        this.params = params;
    }

    /**
     * Determines the preferred {@link AuthScheme authentication scheme} that can be used 
     * to respond to the given collection of challenges.
     * 
     * @param challenges the collection of authentication challenges
     * 
     * @return the preferred {@link AuthScheme authentication scheme}
     * 
     * @throws AuthChallengeException if the preferred authentication scheme 
     * cannot be determined or is not supported
     */
    public AuthScheme selectAuthScheme(final Map challenges) throws AuthChallengeException {
        if (challenges == null) {
            throw new IllegalArgumentException("Challenge map may not be null"); 
        }
        Collection authPrefs = (Collection) this.params.getParameter(
            AuthPolicy.AUTH_SCHEME_PRIORITY);
        if (authPrefs == null || authPrefs.isEmpty()) {
            authPrefs = AuthPolicy.getDefaultAuthPrefs();    
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Supported authentication schemes in the order of preference: " 
                + authPrefs);
        }
        AuthScheme authscheme = null;
        String challenge = null;
        Iterator item = authPrefs.iterator();
        while (item.hasNext()) {
            String id = (String) item.next();
            challenge = (String) challenges.get(id.toLowerCase()); 
            if (challenge != null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(id + " authentication scheme selected");
                }
                try {
                    authscheme = AuthPolicy.getAuthScheme(id);
                } catch (IllegalStateException e) {
                    throw new AuthChallengeException(e.getMessage());
                }
                break;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Challenge for " + id + " authentication scheme not available");
                    // Try again
                }
            }
        }
        if (authscheme == null) {
            // If none selected, something is wrong
            throw new AuthChallengeException(
                "Unable to respond to any of these challenges: "
                    + challenges);
        }
        return authscheme;
    }

    /**
     * Processes the given collection of challenges and updates the 
     * {@link AuthState state} of the authentication process.
     * 
     * @param challenges the collection of authentication challenges
     * 
     * @return the {@link AuthScheme authentication scheme} used to 
     * process the challenge
     * 
     * @throws AuthChallengeException if authentication challenges cannot be 
     * successfully processed or the preferred authentication scheme cannot
     * be determined
     */
    public AuthScheme processChallenge(final AuthState state, final Map challenges)
        throws MalformedChallengeException, AuthenticationException
    {    
        if (state == null) {
            throw new IllegalArgumentException("Authentication state may not be null"); 
        }
        if (challenges == null) {
            throw new IllegalArgumentException("Challenge map may not be null"); 
        }
        
        if (state.isPreemptive() || state.getAuthScheme() == null) {
            // Authentication not attempted before
            state.setAuthScheme(selectAuthScheme(challenges));
        }
        AuthScheme authscheme = state.getAuthScheme();
        String id = authscheme.getSchemeName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using authentication scheme: " + id);
        }
        String challenge = (String) challenges.get(id.toLowerCase());
        if (challenge == null) {
            throw new AuthenticationException(id + 
                " authorization challenge expected, but not found");
        }
        authscheme.processChallenge(challenge);
        LOG.debug("Authorization challenge processed");
        return authscheme;
    }
}
