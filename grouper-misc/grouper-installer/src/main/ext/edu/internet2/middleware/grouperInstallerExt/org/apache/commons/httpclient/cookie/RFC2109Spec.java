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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/cookie/RFC2109Spec.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.cookie;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Cookie;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.NameValuePair;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.ParameterFormatter;

/**
 * <p>RFC 2109 specific cookie management functions
 *
 * @author  B.C. Holmes
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:dsale@us.britannica.com">Doug Sale</a>
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author Sean C. Sullivan
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @since 2.0 
 */

public class RFC2109Spec extends CookieSpecBase {

    private final ParameterFormatter formatter;

    /**
     * Cookie Response Header  name for cookies processed
     * by this spec.
     */
    public final static String SET_COOKIE_KEY = "set-cookie";

    /** Default constructor */
    public RFC2109Spec() {
        super();
        this.formatter = new ParameterFormatter();
        this.formatter.setAlwaysUseQuotes(true);
    }

    /**
      * Parse RFC 2109 specific cookie attribute and update the corresponsing
      * {@link Cookie} properties.
      *
      * @param attribute {@link NameValuePair} cookie attribute from the
      * <tt>Set- Cookie</tt>
      * @param cookie {@link Cookie} to be updated
      * @throws MalformedCookieException if an exception occurs during parsing
      */
    public void parseAttribute(
        final NameValuePair attribute, final Cookie cookie)
        throws MalformedCookieException {
          
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute may not be null.");
        }
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null.");
        }
        final String paramName = attribute.getName().toLowerCase();
        final String paramValue = attribute.getValue();

        if (paramName.equals("path")) {
            if (paramValue == null) {
                throw new MalformedCookieException(
                    "Missing value for path attribute");
            }
            if (paramValue.trim().equals("")) {
                throw new MalformedCookieException(
                    "Blank value for path attribute");
            }
            cookie.setPath(paramValue);
            cookie.setPathAttributeSpecified(true);
        } else if (paramName.equals("version")) {

            if (paramValue == null) {
                throw new MalformedCookieException(
                    "Missing value for version attribute");
            }
            try {
               cookie.setVersion(Integer.parseInt(paramValue));
            } catch (NumberFormatException e) {
                throw new MalformedCookieException("Invalid version: " 
                    + e.getMessage());
            }

        } else {
            super.parseAttribute(attribute, cookie);
        }
    }

    /**
      * Performs RFC 2109 compliant {@link Cookie} validation
      *
      * @param host the host from which the {@link Cookie} was received
      * @param port the port from which the {@link Cookie} was received
      * @param path the path from which the {@link Cookie} was received
      * @param secure <tt>true</tt> when the {@link Cookie} was received using a
      * secure connection
      * @param cookie The cookie to validate
      * @throws MalformedCookieException if an exception occurs during
      * validation
      */
    public void validate(String host, int port, String path, 
        boolean secure, final Cookie cookie) throws MalformedCookieException {
            
        LOG.trace("enter RFC2109Spec.validate(String, int, String, "
            + "boolean, Cookie)");
            
        // Perform generic validation
        super.validate(host, port, path, secure, cookie);
        // Perform RFC 2109 specific validation
        
        if (cookie.getName().indexOf(' ') != -1) {
            throw new MalformedCookieException("Cookie name may not contain blanks");
        }
        if (cookie.getName().startsWith("$")) {
            throw new MalformedCookieException("Cookie name may not start with $");
        }
        
        if (cookie.isDomainAttributeSpecified() 
            && (!cookie.getDomain().equals(host))) {
                
            // domain must start with dot
            if (!cookie.getDomain().startsWith(".")) {
                throw new MalformedCookieException("Domain attribute \"" 
                    + cookie.getDomain() 
                    + "\" violates RFC 2109: domain must start with a dot");
            }
            // domain must have at least one embedded dot
            int dotIndex = cookie.getDomain().indexOf('.', 1);
            if (dotIndex < 0 || dotIndex == cookie.getDomain().length() - 1) {
                throw new MalformedCookieException("Domain attribute \"" 
                    + cookie.getDomain() 
                    + "\" violates RFC 2109: domain must contain an embedded dot");
            }
            host = host.toLowerCase();
            if (!host.endsWith(cookie.getDomain())) {
                throw new MalformedCookieException(
                    "Illegal domain attribute \"" + cookie.getDomain() 
                    + "\". Domain of origin: \"" + host + "\"");
            }
            // host minus domain may not contain any dots
            String hostWithoutDomain = host.substring(0, host.length() 
                - cookie.getDomain().length());
            if (hostWithoutDomain.indexOf('.') != -1) {
                throw new MalformedCookieException("Domain attribute \"" 
                    + cookie.getDomain() 
                    + "\" violates RFC 2109: host minus domain may not contain any dots");
            }
        }
    }

    /**
     * Performs domain-match as defined by the RFC2109.
     * @param host The target host.
     * @param domain The cookie domain attribute.
     * @return true if the specified host matches the given domain.
     * 
     * @since 3.0
     */
    public boolean domainMatch(String host, String domain) {
        boolean match = host.equals(domain) 
            || (domain.startsWith(".") && host.endsWith(domain));

        return match;
    }

    /**
     * Return a name/value string suitable for sending in a <tt>"Cookie"</tt>
     * header as defined in RFC 2109 for backward compatibility with cookie
     * version 0
     * @param buffer The string buffer to use for output
     * @param param The parameter.
     * @param version The cookie version 
     */
    private void formatParam(final StringBuffer buffer, final NameValuePair param, int version) {
        if (version < 1) {
            buffer.append(param.getName());
            buffer.append("=");
            if (param.getValue() != null) {
                buffer.append(param.getValue());   
            }
        } else {
            this.formatter.format(buffer, param);
        }
    }

    /**
     * Return a string suitable for sending in a <tt>"Cookie"</tt> header 
     * as defined in RFC 2109 for backward compatibility with cookie version 0
     * @param buffer The string buffer to use for output
     * @param cookie The {@link Cookie} to be formatted as string
     * @param version The version to use.
     */
    private void formatCookieAsVer(final StringBuffer buffer, final Cookie cookie, int version) {
        String value = cookie.getValue();
        if (value == null) {
            value = "";
        }
        formatParam(buffer, new NameValuePair(cookie.getName(), value), version);
        if ((cookie.getPath() != null) && cookie.isPathAttributeSpecified()) {
          buffer.append("; ");
          formatParam(buffer, new NameValuePair("$Path", cookie.getPath()), version);
        }
        if ((cookie.getDomain() != null) 
            && cookie.isDomainAttributeSpecified()) {
            buffer.append("; ");
            formatParam(buffer, new NameValuePair("$Domain", cookie.getDomain()), version);
        }
    }

    /**
     * Return a string suitable for sending in a <tt>"Cookie"</tt> header as
     * defined in RFC 2109
     * @param cookie a {@link Cookie} to be formatted as string
     * @return a string suitable for sending in a <tt>"Cookie"</tt> header.
     */
    public String formatCookie(Cookie cookie) {
        LOG.trace("enter RFC2109Spec.formatCookie(Cookie)");
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        int version = cookie.getVersion();
        StringBuffer buffer = new StringBuffer();
        formatParam(buffer, 
                new NameValuePair("$Version", Integer.toString(version)), 
                version);
        buffer.append("; ");
        formatCookieAsVer(buffer, cookie, version);
        return buffer.toString();
    }

    /**
     * Create a RFC 2109 compliant <tt>"Cookie"</tt> header value containing all
     * {@link Cookie}s in <i>cookies</i> suitable for sending in a <tt>"Cookie"
     * </tt> header
     * @param cookies an array of {@link Cookie}s to be formatted
     * @return a string suitable for sending in a Cookie header.
     */
    public String formatCookies(Cookie[] cookies) {
        LOG.trace("enter RFC2109Spec.formatCookieHeader(Cookie[])");
        int version = Integer.MAX_VALUE;
        // Pick the lowerest common denominator
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        final StringBuffer buffer = new StringBuffer();
        formatParam(buffer, 
                new NameValuePair("$Version", Integer.toString(version)), 
                version);
        for (int i = 0; i < cookies.length; i++) {
            buffer.append("; ");
            formatCookieAsVer(buffer, cookies[i], version);
        }
        return buffer.toString();
    }

}
