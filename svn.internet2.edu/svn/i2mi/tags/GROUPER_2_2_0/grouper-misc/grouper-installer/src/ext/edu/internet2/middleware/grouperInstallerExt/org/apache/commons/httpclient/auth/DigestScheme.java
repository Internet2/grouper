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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/auth/DigestScheme.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpClientError;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpMethod;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.NameValuePair;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.EncodingUtil;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.ParameterFormatter;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Digest authentication scheme as defined in RFC 2617.
 * Both MD5 (default) and MD5-sess are supported.
 * Currently only qop=auth or no qop is supported. qop=auth-int
 * is unsupported. If auth and auth-int are provided, auth is
 * used.
 * </p>
 * <p>
 * Credential charset is configured via the 
 * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params.HttpMethodParams#CREDENTIAL_CHARSET credential
 * charset} parameter.  Since the digest username is included as clear text in the generated 
 * Authentication header, the charset of the username must be compatible with the 
 * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params.HttpMethodParams#HTTP_ELEMENT_CHARSET http element 
 * charset}.
 * </p>
 * TODO: make class more stateful regarding repeated authentication requests
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Gl?ck
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */

public class DigestScheme extends RFC2617Scheme {
    
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(DigestScheme.class);

    /**
     * Hexa values used when creating 32 character long digest in HTTP DigestScheme
     * in case of authentication.
     * 
     * @see #encode(byte[])
     */
    private static final char[] HEXADECIMAL = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 
        'e', 'f'
    };
    
    /** Whether the digest authentication process is complete */
    private boolean complete;
    
    //TODO: supply a real nonce-count, currently a server will interprete a repeated request as a replay  
    private static final String NC = "00000001"; //nonce-count is always 1
    private static final int QOP_MISSING = 0;
    private static final int QOP_AUTH_INT = 1;
    private static final int QOP_AUTH = 2;

    private int qopVariant = QOP_MISSING;
    private String cnonce;

    private final ParameterFormatter formatter;
    /**
     * Default constructor for the digest authetication scheme.
     * 
     * @since 3.0
     */
    public DigestScheme() {
        super();
        this.complete = false;
        this.formatter = new ParameterFormatter();
    }

    /**
     * Gets an ID based upon the realm and the nonce value.  This ensures that requests
     * to the same realm with different nonce values will succeed.  This differentiation
     * allows servers to request re-authentication using a fresh nonce value.
     * 
     * @deprecated no longer used
     */
    public String getID() {
        
        String id = getRealm();
        String nonce = getParameter("nonce");
        if (nonce != null) {
            id += "-" + nonce;
        }
        
        return id;
    }

    /**
     * Constructor for the digest authetication scheme.
     * 
     * @param challenge authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @deprecated Use parameterless constructor and {@link AuthScheme#processChallenge(String)} 
     *             method
     */
    public DigestScheme(final String challenge) 
      throws MalformedChallengeException {
        this();
        processChallenge(challenge);
    }

    /**
     * Processes the Digest challenge.
     *  
     * @param challenge the challenge string
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @since 3.0
     */
    public void processChallenge(final String challenge) 
      throws MalformedChallengeException {
        super.processChallenge(challenge);
        
        if (getParameter("realm") == null) {
            throw new MalformedChallengeException("missing realm in challange");
        }
        if (getParameter("nonce") == null) {
            throw new MalformedChallengeException("missing nonce in challange");   
        }
        
        boolean unsupportedQop = false;
        // qop parsing
        String qop = getParameter("qop");
        if (qop != null) {
            StringTokenizer tok = new StringTokenizer(qop,",");
            while (tok.hasMoreTokens()) {
                String variant = tok.nextToken().trim();
                if (variant.equals("auth")) {
                    qopVariant = QOP_AUTH;
                    break; //that's our favourite, because auth-int is unsupported
                } else if (variant.equals("auth-int")) {
                    qopVariant = QOP_AUTH_INT;               
                } else {
                    unsupportedQop = true;
                    LOG.warn("Unsupported qop detected: "+ variant);   
                }     
            }
        }        
        
        if (unsupportedQop && (qopVariant == QOP_MISSING)) {
            throw new MalformedChallengeException("None of the qop methods is supported");   
        }
        
        cnonce = createCnonce();   
        this.complete = true;
    }

    /**
     * Tests if the Digest authentication process has been completed.
     * 
     * @return <tt>true</tt> if Digest authorization has been processed,
     *   <tt>false</tt> otherwise.
     * 
     * @since 3.0
     */
    public boolean isComplete() {
        String s = getParameter("stale");
        if ("true".equalsIgnoreCase(s)) {
            return false;
        } else {
            return this.complete;
        }
    }

    /**
     * Returns textual designation of the digest authentication scheme.
     * 
     * @return <code>digest</code>
     */
    public String getSchemeName() {
        return "digest";
    }

    /**
     * Returns <tt>false</tt>. Digest authentication scheme is request based.
     * 
     * @return <tt>false</tt>.
     * 
     * @since 3.0
     */
    public boolean isConnectionBased() {
        return false;    
    }

    /**
     * Produces a digest authorization string for the given set of 
     * {@link Credentials}, method name and URI.
     * 
     * @param credentials A set of credentials to be used for athentication
     * @param method the name of the method that requires authorization. 
     * @param uri The URI for which authorization is needed. 
     * 
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return a digest authorization string
     * 
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpMethod#getName()
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpMethod#getPath()
     * 
     * @deprecated Use {@link #authenticate(Credentials, HttpMethod)}
     */
    public String authenticate(Credentials credentials, String method, String uri)
      throws AuthenticationException {

        LOG.trace("enter DigestScheme.authenticate(Credentials, String, String)");

        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
             "Credentials cannot be used for digest authentication: " 
              + credentials.getClass().getName());
        }
        getParameters().put("methodname", method);
        getParameters().put("uri", uri);
        String digest = createDigest(
            usernamepassword.getUserName(),
            usernamepassword.getPassword());
        return "Digest " + createDigestHeader(usernamepassword.getUserName(), digest);
    }

    /**
     * Produces a digest authorization string for the given set of 
     * {@link Credentials}, method name and URI.
     * 
     * @param credentials A set of credentials to be used for athentication
     * @param method The method being authenticated
     * 
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return a digest authorization string
     * 
     * @since 3.0
     */
    public String authenticate(Credentials credentials, HttpMethod method)
    throws AuthenticationException {

        LOG.trace("enter DigestScheme.authenticate(Credentials, HttpMethod)");

        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
                    "Credentials cannot be used for digest authentication: " 
                    + credentials.getClass().getName());
        }
        getParameters().put("methodname", method.getName());
        StringBuffer buffer = new StringBuffer(method.getPath());
        String query = method.getQueryString();
        if (query != null) {
            if (query.indexOf("?") != 0) {
                buffer.append("?");
            }
            buffer.append(method.getQueryString());
        }
        getParameters().put("uri", buffer.toString());
        String charset = getParameter("charset");
        if (charset == null) {
            getParameters().put("charset", method.getParams().getCredentialCharset());
        }
        String digest = createDigest(
            usernamepassword.getUserName(),
            usernamepassword.getPassword());
        return "Digest " + createDigestHeader(usernamepassword.getUserName(),
                digest);
    }
    
    /**
     * Creates an MD5 response digest.
     * 
     * @param uname Username
     * @param pwd Password
     * @param charset The credential charset
     * 
     * @return The created digest as string. This will be the response tag's
     *         value in the Authentication HTTP header.
     * @throws AuthenticationException when MD5 is an unsupported algorithm
     */
    private String createDigest(final String uname, final String pwd) throws AuthenticationException {

        LOG.trace("enter DigestScheme.createDigest(String, String, Map)");

        final String digAlg = "MD5";

        // Collecting required tokens
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String qop = getParameter("qop");
        String method = getParameter("methodname");
        String algorithm = getParameter("algorithm");
        // If an algorithm is not specified, default to MD5.
        if (algorithm == null) {
            algorithm = "MD5";
        }
        // If an charset is not specified, default to ISO-8859-1.
        String charset = getParameter("charset");
        if (charset == null) {
            charset = "ISO-8859-1";
        }

        if (qopVariant == QOP_AUTH_INT) {
            LOG.warn("qop=auth-int is not supported");
            throw new AuthenticationException(
                "Unsupported qop in HTTP Digest authentication");   
        }

        MessageDigest md5Helper;

        try {
            md5Helper = MessageDigest.getInstance(digAlg);
        } catch (Exception e) {
            throw new AuthenticationException(
              "Unsupported algorithm in HTTP Digest authentication: "
               + digAlg);
        }

        // 3.2.2.2: Calculating digest
        StringBuffer tmp = new StringBuffer(uname.length() + realm.length() + pwd.length() + 2);
        tmp.append(uname);
        tmp.append(':');
        tmp.append(realm);
        tmp.append(':');
        tmp.append(pwd);
        // unq(username-value) ":" unq(realm-value) ":" passwd
        String a1 = tmp.toString();
        //a1 is suitable for MD5 algorithm
        if(algorithm.equals("MD5-sess")) {
            // H( unq(username-value) ":" unq(realm-value) ":" passwd )
            //      ":" unq(nonce-value)
            //      ":" unq(cnonce-value)

            String tmp2=encode(md5Helper.digest(EncodingUtil.getBytes(a1, charset)));
            StringBuffer tmp3 = new StringBuffer(tmp2.length() + nonce.length() + cnonce.length() + 2);
            tmp3.append(tmp2);
            tmp3.append(':');
            tmp3.append(nonce);
            tmp3.append(':');
            tmp3.append(cnonce);
            a1 = tmp3.toString();
        } else if(!algorithm.equals("MD5")) {
            LOG.warn("Unhandled algorithm " + algorithm + " requested");
        }
        String md5a1 = encode(md5Helper.digest(EncodingUtil.getBytes(a1, charset)));

        String a2 = null;
        if (qopVariant == QOP_AUTH_INT) {
            LOG.error("Unhandled qop auth-int");
            //we do not have access to the entity-body or its hash
            //TODO: add Method ":" digest-uri-value ":" H(entity-body)      
        } else {
            a2 = method + ":" + uri;
        }
        String md5a2 = encode(md5Helper.digest(EncodingUtil.getAsciiBytes(a2)));

        // 3.2.2.1
        String serverDigestValue;
        if (qopVariant == QOP_MISSING) {
            LOG.debug("Using null qop method");
            StringBuffer tmp2 = new StringBuffer(md5a1.length() + nonce.length() + md5a2.length());
            tmp2.append(md5a1);
            tmp2.append(':');
            tmp2.append(nonce);
            tmp2.append(':');
            tmp2.append(md5a2);
            serverDigestValue = tmp2.toString();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using qop method " + qop);
            }
            String qopOption = getQopVariantString();
            StringBuffer tmp2 = new StringBuffer(md5a1.length() + nonce.length()
                + NC.length() + cnonce.length() + qopOption.length() + md5a2.length() + 5);
            tmp2.append(md5a1);
            tmp2.append(':');
            tmp2.append(nonce);
            tmp2.append(':');
            tmp2.append(NC);
            tmp2.append(':');
            tmp2.append(cnonce);
            tmp2.append(':');
            tmp2.append(qopOption);
            tmp2.append(':');
            tmp2.append(md5a2); 
            serverDigestValue = tmp2.toString();
        }

        String serverDigest =
            encode(md5Helper.digest(EncodingUtil.getAsciiBytes(serverDigestValue)));

        return serverDigest;
    }

    /**
     * Creates digest-response header as defined in RFC2617.
     * 
     * @param uname Username
     * @param digest The response tag's value as String.
     * 
     * @return The digest-response as String.
     */
    private String createDigestHeader(final String uname, final String digest) 
        throws AuthenticationException {

        LOG.trace("enter DigestScheme.createDigestHeader(String, Map, "
            + "String)");

        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String opaque = getParameter("opaque");
        String response = digest;
        String algorithm = getParameter("algorithm");

        List params = new ArrayList(20);
        params.add(new NameValuePair("username", uname));
        params.add(new NameValuePair("realm", realm));
        params.add(new NameValuePair("nonce", nonce));
        params.add(new NameValuePair("uri", uri));
        params.add(new NameValuePair("response", response));
        
        if (qopVariant != QOP_MISSING) {
            params.add(new NameValuePair("qop", getQopVariantString()));
            params.add(new NameValuePair("nc", NC));
            params.add(new NameValuePair("cnonce", this.cnonce));
        }
        if (algorithm != null) {
            params.add(new NameValuePair("algorithm", algorithm));
        }    
        if (opaque != null) {
            params.add(new NameValuePair("opaque", opaque));
        }

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            NameValuePair param = (NameValuePair) params.get(i);
            if (i > 0) {
                buffer.append(", ");
            }
            boolean noQuotes = "nc".equals(param.getName()) ||
                               "qop".equals(param.getName());
            this.formatter.setAlwaysUseQuotes(!noQuotes);
            this.formatter.format(buffer, param);
        }
        return buffer.toString();
    }

    private String getQopVariantString() {
        String qopOption;
        if (qopVariant == QOP_AUTH_INT) {
            qopOption = "auth-int";   
        } else {
            qopOption = "auth";
        }
        return qopOption;            
    }

    /**
     * Encodes the 128 bit (16 bytes) MD5 digest into a 32 characters long 
     * <CODE>String</CODE> according to RFC 2617.
     * 
     * @param binaryData array containing the digest
     * @return encoded MD5, or <CODE>null</CODE> if encoding failed
     */
    private static String encode(byte[] binaryData) {
        LOG.trace("enter DigestScheme.encode(byte[])");

        if (binaryData.length != 16) {
            return null;
        } 

        char[] buffer = new char[32];
        for (int i = 0; i < 16; i++) {
            int low = (int) (binaryData[i] & 0x0f);
            int high = (int) ((binaryData[i] & 0xf0) >> 4);
            buffer[i * 2] = HEXADECIMAL[high];
            buffer[(i * 2) + 1] = HEXADECIMAL[low];
        }

        return new String(buffer);
    }


    /**
     * Creates a random cnonce value based on the current time.
     * 
     * @return The cnonce value as String.
     * @throws HttpClientError if MD5 algorithm is not supported.
     */
    public static String createCnonce() {
        LOG.trace("enter DigestScheme.createCnonce()");

        String cnonce;
        final String digAlg = "MD5";
        MessageDigest md5Helper;

        try {
            md5Helper = MessageDigest.getInstance(digAlg);
        } catch (NoSuchAlgorithmException e) {
            throw new HttpClientError(
              "Unsupported algorithm in HTTP Digest authentication: "
               + digAlg);
        }

        cnonce = Long.toString(System.currentTimeMillis());
        cnonce = encode(md5Helper.digest(EncodingUtil.getAsciiBytes(cnonce)));

        return cnonce;
    }
}
