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
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/java/org/apache/commons/httpclient/util/ParameterFormatter.java $
 * $Revision: 1.1 $
 * $Date: 2008-11-30 10:57:27 $
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

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.NameValuePair;

/**
 * <p>
 *  This formatter produces a textual representation of attribute/value pairs. It 
 *  comforms to the generic grammar and formatting rules outlined in the 
 *  <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2.1">Section 2.1</a>
 *  and  
 *  <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.6">Section 3.6</a>
 *  of <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.txt">RFC 2616</a>
 * </p>
 * <h>2.1 Augmented BNF</h>
 * <p>
 *  Many HTTP/1.1 header field values consist of words separated by LWS or special 
 *  characters. These special characters MUST be in a quoted string to be used within 
 *  a parameter value (as defined in section 3.6).
 * <p>
 * <pre>
 * token          = 1*<any CHAR except CTLs or separators>
 * separators     = "(" | ")" | "<" | ">" | "@"
 *                | "," | ";" | ":" | "\" | <">
 *                | "/" | "[" | "]" | "?" | "="
 *                | "{" | "}" | SP | HT
 * </pre>
 * <p>
 *  A string of text is parsed as a single word if it is quoted using double-quote marks.
 * </p>
 * <pre>
 * quoted-string  = ( <"> *(qdtext | quoted-pair ) <"> )
 * qdtext         = <any TEXT except <">>
 * </pre>
 * <p>
 *  The backslash character ("\") MAY be used as a single-character quoting mechanism only 
 *  within quoted-string and comment constructs.
 * </p>
 * <pre>
 * quoted-pair    = "\" CHAR
 * </pre>
 * <h>3.6 Transfer Codings</h>
 * <p>
 *  Parameters are in the form of attribute/value pairs.
 * </p>
 * <pre>
 * parameter               = attribute "=" value
 * attribute               = token
 * value                   = token | quoted-string
 * </pre>
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 3.0
 */
public class ParameterFormatter {
    
    /**
     * Special characters that can be used as separators in HTTP parameters.
     * These special characters MUST be in a quoted string to be used within
     * a parameter value 
     */
    private static final char[] SEPARATORS   = {
            '(', ')', '<', '>', '@', 
            ',', ';', ':', '\\', '"', 
            '/', '[', ']', '?', '=',
            '{', '}', ' ', '\t'
            };
    
    /**
     * Unsafe special characters that must be escaped using the backslash
     * character
     */
    private static final char[] UNSAFE_CHARS = {
            '"', '\\'
            };
    
    /**
     * This flag determines whether all parameter values must be enclosed in 
     * quotation marks, even if they do not contain any special characters
     */
    private boolean alwaysUseQuotes = true;
    
    /** Default ParameterFormatter constructor */
    public ParameterFormatter() {
        super();
    }
    
    private static boolean isOneOf(char[] chars, char ch) {
        for (int i = 0; i < chars.length; i++) {
            if (ch == chars[i]) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isUnsafeChar(char ch) {
        return isOneOf(UNSAFE_CHARS, ch);
    }
    
    private static boolean isSeparator(char ch) {
        return isOneOf(SEPARATORS, ch);
    }

    /**
     * Determines whether all parameter values must be enclosed in quotation 
     * marks, even if they do not contain any special characters
     * 
     * @return <tt>true</tt> if all parameter values must be enclosed in 
     * quotation marks, <tt>false</tt> otherwise
     */
    public boolean isAlwaysUseQuotes() {
        return alwaysUseQuotes;
    }
    
    /**
     * Defines whether all parameter values must be enclosed in quotation 
     * marks, even if they do not contain any special characters
     * 
     * @param alwaysUseQuotes
     */
    public void setAlwaysUseQuotes(boolean alwaysUseQuotes) {
        this.alwaysUseQuotes = alwaysUseQuotes;
    }
    
    /**
     * Formats the given parameter value using formatting rules defined
     * in RFC 2616 
     * 
     * @param buffer output buffer 
     * @param value the parameter value to be formatted
     * @param alwaysUseQuotes <tt>true</tt> if the parameter value must 
     * be enclosed in quotation marks, even if it does not contain any special 
     * characters<tt>, false</tt> only if the parameter value contains 
     * potentially unsafe special characters
     */
    public static void formatValue(
            final StringBuffer buffer, final String value, boolean alwaysUseQuotes) {
        if (buffer == null) {
            throw new IllegalArgumentException("String buffer may not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value buffer may not be null");
        }
        if (alwaysUseQuotes) {
            buffer.append('"');
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (isUnsafeChar(ch)) {
                    buffer.append('\\');
                }
                buffer.append(ch);
            }
            buffer.append('"');
        } else {
            int offset = buffer.length();
            boolean unsafe = false;
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (isSeparator(ch)) {
                    unsafe = true;
                }
                if (isUnsafeChar(ch)) {
                    buffer.append('\\');
                }
                buffer.append(ch);
            }
            if (unsafe) {
                buffer.insert(offset, '"');
                buffer.append('"');
            }
        }
    }
    
    /**
     * Produces textual representaion of the attribute/value pair using 
     * formatting rules defined in RFC 2616
     *  
     * @param buffer output buffer 
     * @param param the parameter to be formatted
     */
    public void format(final StringBuffer buffer, final NameValuePair param) {
        if (buffer == null) {
            throw new IllegalArgumentException("String buffer may not be null");
        }
        if (param == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        }
        buffer.append(param.getName());
        String value = param.getValue();
        if (value != null) {
            buffer.append("=");
            formatValue(buffer, value, this.alwaysUseQuotes);
        }
    }
    
    /**
     * Produces textual representaion of the attribute/value pair using 
     * formatting rules defined in RFC 2616
     *  
     * @param param the parameter to be formatted
     * 
     * @return RFC 2616 conformant textual representaion of the 
     * attribute/value pair
     */
    public String format(final NameValuePair param) {
        StringBuffer buffer = new StringBuffer();
        format(buffer, param);
        return buffer.toString();
    }

}
