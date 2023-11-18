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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/auth/RFC2617Scheme.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
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

import java.util.Map;

/**
 * <p>
 * Abstract authentication scheme class that lays foundation for all
 * RFC 2617 compliant authetication schemes and provides capabilities common 
 * to all authentication schemes defined in RFC 2617.
 * </p>
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
*/
public abstract class RFC2617Scheme implements AuthScheme {

    /**
     * Authentication parameter map.
     */
    private Map params = null;

    /**
     * Default constructor for RFC2617 compliant authetication schemes.
     * 
     * @since 3.0
     */
    public RFC2617Scheme() {
        super();
    }

    /**
     * Default constructor for RFC2617 compliant authetication schemes.
     * 
     * @param challenge authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @deprecated Use parameterless constructor and {@link AuthScheme#processChallenge(String)} 
     *             method
     */
    public RFC2617Scheme(final String challenge) throws MalformedChallengeException {
        super();
        processChallenge(challenge);
    }

    /**
     * Processes the given challenge token. Some authentication schemes
     * may involve multiple challenge-response exchanges. Such schemes must be able 
     * to maintain the state information when dealing with sequential challenges 
     * 
     * @param challenge the challenge string
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @since 3.0
     */
    public void processChallenge(final String challenge) throws MalformedChallengeException {
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException(
              "Invalid " + getSchemeName() + " challenge: " + challenge); 
        }
        this.params = AuthChallengeParser.extractParams(challenge);
    }

    /**
     * Returns authentication parameters map. Keys in the map are lower-cased.
     * 
     * @return the map of authentication parameters
     */
    protected Map getParameters() {
        return this.params;
    }

    /**
     * Returns authentication parameter with the given name, if available.
     * 
     * @param name The name of the parameter to be returned
     * 
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null"); 
        }
        if (this.params == null) {
            return null;
        }
        return (String) this.params.get(name.toLowerCase());
    }

    /**
     * Returns authentication realm. The realm may not be null.
     * 
     * @return the authentication realm
     */
    public String getRealm() {
        return getParameter("realm");
    }
    
    /**
     * Returns a String identifying the authentication challenge.  This is
     * used, in combination with the host and port to determine if
     * authorization has already been attempted or not.  Schemes which
     * require multiple requests to complete the authentication should
     * return a different value for each stage in the request.
     * 
     * <p>Additionally, the ID should take into account any changes to the
     * authentication challenge and return a different value when appropriate.
     * For example when the realm changes in basic authentication it should be
     * considered a different authentication attempt and a different value should
     * be returned.</p>
     * 
     * <p>This method simply returns the realm for the challenge.</p>
     * 
     * @return String a String identifying the authentication challenge.  The
     * returned value may be null.
     * 
     * @deprecated no longer used
     */
    public String getID() {
        return getRealm();
    }
}
