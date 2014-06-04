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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/HeaderElement.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.ParameterParser;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * <p>One element of an HTTP header's value.</p>
 * <p>
 * Some HTTP headers (such as the set-cookie header) have values that
 * can be decomposed into multiple elements.  Such headers must be in the
 * following form:
 * </p>
 * <pre>
 * header  = [ element ] *( "," [ element ] )
 * element = name [ "=" [ value ] ] *( ";" [ param ] )
 * param   = name [ "=" [ value ] ]
 *
 * name    = token
 * value   = ( token | quoted-string )
 *
 * token         = 1*&lt;any char except "=", ",", ";", &lt;"&gt; and
 *                       white space&gt;
 * quoted-string = &lt;"&gt; *( text | quoted-char ) &lt;"&gt;
 * text          = any char except &lt;"&gt;
 * quoted-char   = "\" char
 * </pre>
 * <p>
 * Any amount of white space is allowed between any part of the
 * header, element or param and is ignored. A missing value in any
 * element or param will be stored as the empty {@link String};
 * if the "=" is also missing <var>null</var> will be stored instead.
 * </p>
 * <p>
 * This class represents an individual header element, containing
 * both a name/value pair (value may be <tt>null</tt>) and optionally
 * a set of additional parameters.
 * </p>
 * <p>
 * This class also exposes a {@link #parse} method for parsing a
 * {@link Header} value into an array of elements.
 * </p>
 *
 * @see Header
 *
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.com">Oleg Kalnichevski</a>
 * 
 * @since 1.0
 * @version $Revision: 1.1 $ $Date: 2008-11-30 10:57:19 $
 */
public class HeaderElement extends NameValuePair {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public HeaderElement() {
        this(null, null, null);
    }

    /**
      * Constructor.
      * @param name my name
      * @param value my (possibly <tt>null</tt>) value
      */
    public HeaderElement(String name, String value) {
        this(name, value, null);
    }

    /**
     * Constructor with name, value and parameters.
     *
     * @param name my name
     * @param value my (possibly <tt>null</tt>) value
     * @param parameters my (possibly <tt>null</tt>) parameters
     */
    public HeaderElement(String name, String value,
            NameValuePair[] parameters) {
        super(name, value);
        this.parameters = parameters;
    }

    /**
     * Constructor with array of characters.
     *
     * @param chars the array of characters
     * @param offset - the initial offset.
     * @param length - the length.
     * 
     * @since 3.0
     */
    public HeaderElement(char[] chars, int offset, int length) {
        this();
        if (chars == null) {
            return;
        }
        ParameterParser parser = new ParameterParser();
        List params = parser.parse(chars, offset, length, ';');
        if (params.size() > 0) {
            NameValuePair element = (NameValuePair) params.remove(0);
            setName(element.getName());  
            setValue(element.getValue());
            if (params.size() > 0) {
                this.parameters = (NameValuePair[])
                    params.toArray(new NameValuePair[params.size()]);    
            }
        }
    }

    /**
     * Constructor with array of characters.
     *
     * @param chars the array of characters
     * 
     * @since 3.0
     */
    public HeaderElement(char[] chars) {
        this(chars, 0, chars.length);
    }

    // -------------------------------------------------------- Constants

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HeaderElement.class);

    // ----------------------------------------------------- Instance Variables

    /** My parameters, if any. */
    private NameValuePair[] parameters = null;

    // ------------------------------------------------------------- Properties

    /**
     * Get parameters, if any.
     *
     * @since 2.0
     * @return parameters as an array of {@link NameValuePair}s
     */
    public NameValuePair[] getParameters() {
        return this.parameters;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * This parses the value part of a header. The result is an array of
     * HeaderElement objects.
     *
     * @param headerValue  the array of char representation of the header value
     *                     (as received from the web server).
     * @return array of {@link HeaderElement}s.
     * 
     * @since 3.0
     */
    public static final HeaderElement[] parseElements(char[] headerValue) {
            
        LOG.trace("enter HeaderElement.parseElements(char[])");

        if (headerValue == null) {
            return new HeaderElement[] {};
        }
        List elements = new ArrayList(); 
        
        int i = 0;
        int from = 0;
        int len = headerValue.length;
        boolean qouted = false;
        while (i < len) {
            char ch = headerValue[i];
            if (ch == '"') {
                qouted = !qouted;
            }
            HeaderElement element = null;
            if ((!qouted) && (ch == ',')) {
                element = new HeaderElement(headerValue, from, i);
                from = i + 1;
            } else if (i == len - 1) {
                element = new HeaderElement(headerValue, from, len);
            }
            if ((element != null) && (element.getName() != null)) {
                elements.add(element);
            }
            i++;
        }
        return (HeaderElement[])
            elements.toArray(new HeaderElement[elements.size()]);
    }

    /**
     * This parses the value part of a header. The result is an array of
     * HeaderElement objects.
     *
     * @param headerValue  the string representation of the header value
     *                     (as received from the web server).
     * @return array of {@link HeaderElement}s.
     * 
     * @since 3.0
     */
    public static final HeaderElement[] parseElements(String headerValue) {
            
        LOG.trace("enter HeaderElement.parseElements(String)");

        if (headerValue == null) {
            return new HeaderElement[] {};
        }
        return parseElements(headerValue.toCharArray());
    }

    /**
     * This parses the value part of a header. The result is an array of
     * HeaderElement objects.
     *
     * @param headerValue  the string representation of the header value
     *                     (as received from the web server).
     * @return array of {@link HeaderElement}s.
     * @throws HttpException if the above syntax rules are violated.
     * 
     * @deprecated Use #parseElements(String).
     */
    public static final HeaderElement[] parse(String headerValue)
        throws HttpException {
            
        LOG.trace("enter HeaderElement.parse(String)");

        if (headerValue == null) {
            return new HeaderElement[] {};
        }
        return parseElements(headerValue.toCharArray());
    }
         

    /**
     * Returns parameter with the given name, if found. Otherwise null 
     * is returned
     *
     * @param name The name to search by.
     * @return NameValuePair parameter with the given name
     */

    public NameValuePair getParameterByName(String name) {

        LOG.trace("enter HeaderElement.getParameterByName(String)");

        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        } 
        NameValuePair found = null;
        NameValuePair parameters[] = getParameters();
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                NameValuePair current = parameters[ i ];
                if (current.getName().equalsIgnoreCase(name)) {
                    found = current;
                    break;
                }
            }
        }
        return found;
    }

}

