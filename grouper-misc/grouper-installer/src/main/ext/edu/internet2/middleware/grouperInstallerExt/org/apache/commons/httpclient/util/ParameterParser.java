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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/util/ParameterParser.java,v 1.1 2008-11-30 10:57:27 mchyzer Exp $
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
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.NameValuePair;

/**
 * A simple parser intended to parse sequences of name/value pairs.
 * Parameter values are exptected to be enclosed in quotes if they 
 * contain unsafe characters, such as '=' characters or separators.
 * Parameter values are optional and can be omitted. 
 * 
 * <p>
 *  <code>param1 = value; param2 = "anything goes; really"; param3</code>
 * </p>
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 3.0
 */
public class ParameterParser {
    
    /** String to be parsed */
    private char[] chars = null;
    
    /** Current position in the string */    
    private int pos = 0;

    /** Maximum position in the string */    
    private int len = 0;

    /** Start of a token */
    private int i1 = 0;

    /** End of a token */
    private int i2 = 0;
    
    /** Default ParameterParser constructor */
    public ParameterParser() {
        super();
    }


    /** Are there any characters left to parse? */
    private boolean hasChar() {
        return this.pos < this.len;
    }

    
    /** A helper method to process the parsed token. */
    private String getToken(boolean quoted) {
        // Trim leading white spaces
        while ((i1 < i2) && (Character.isWhitespace(chars[i1]))) {
            i1++;
        }
        // Trim trailing white spaces
        while ((i2 > i1) && (Character.isWhitespace(chars[i2 - 1]))) {
            i2--;
        }
        // Strip away quotes if necessary
        if (quoted) {
            if (((i2 - i1) >= 2) 
                && (chars[i1] == '"') 
                && (chars[i2 - 1] == '"')) {
                i1++;
                i2--;
            }
        }
        String result = null;
        if (i2 >= i1) {
            result = new String(chars, i1, i2 - i1);
        }
        return result;
    }


    /** Is given character present in the array of characters? */
    private boolean isOneOf(char ch, char[] charray) {
        boolean result = false;
        for (int i = 0; i < charray.length; i++) {
            if (ch == charray[i]) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    
    /** Parse out a token until any of the given terminators
     * is encountered. */
    private String parseToken(final char[] terminators) {
        char ch;
        i1 = pos;
        i2 = pos;
        while (hasChar()) {
            ch = chars[pos];
            if (isOneOf(ch, terminators)) {
                break;
            }
            i2++;
            pos++;
        }
        return getToken(false);
    }
    
    
    /** Parse out a token until any of the given terminators
     * is encountered. Special characters in quoted tokens
     * are escaped. */
    private String parseQuotedToken(final char[] terminators) {
        char ch;
        i1 = pos;
        i2 = pos;
        boolean quoted = false;
        boolean charEscaped = false;
        while (hasChar()) {
            ch = chars[pos];
            if (!quoted && isOneOf(ch, terminators)) {
                break;
            }
            if (!charEscaped && ch == '"') {
                quoted = !quoted;
            }
            charEscaped = (!charEscaped && ch == '\\');
            i2++;
            pos++;

        }
        return getToken(true);
    }
    
    /** 
     * Extracts a list of {@link NameValuePair}s from the given string.
     *
     * @param str the string that contains a sequence of name/value pairs
     * @return a list of {@link NameValuePair}s
     * 
     */
    public List parse(final String str, char separator) {

        if (str == null) {
            return new ArrayList();
        }
        return parse(str.toCharArray(), separator);
    }

    /** 
     * Extracts a list of {@link NameValuePair}s from the given array of 
     * characters.
     *
     * @param chars the array of characters that contains a sequence of 
     * name/value pairs
     * 
     * @return a list of {@link NameValuePair}s
     */
    public List parse(final char[] chars, char separator) {

        if (chars == null) {
            return new ArrayList();
        }
        return parse(chars, 0, chars.length, separator);
    }


    /** 
     * Extracts a list of {@link NameValuePair}s from the given array of 
     * characters.
     *
     * @param chars the array of characters that contains a sequence of 
     * name/value pairs
     * @param offset - the initial offset.
     * @param length - the length.
     * 
     * @return a list of {@link NameValuePair}s
     */
    public List parse(final char[] chars, int offset, int length, char separator) {

        if (chars == null) {
            return new ArrayList();
        }
        List params = new ArrayList();
        this.chars = chars;
        this.pos = offset;
        this.len = length;
        
        String paramName = null;
        String paramValue = null;
        while (hasChar()) {
            paramName = parseToken(new char[] {'=', separator});
            paramValue = null;
            if (hasChar() && (chars[pos] == '=')) {
                pos++; // skip '='
                paramValue = parseQuotedToken(new char[] {separator});
            }
            if (hasChar() && (chars[pos] == separator)) {
                pos++; // skip separator
            }
            if (paramName != null && !(paramName.equals("") && paramValue == null)) {
                params.add(new NameValuePair(paramName, paramValue));
            }
        }        
        return params;
    }
}
