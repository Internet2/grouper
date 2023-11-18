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
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/java/org/apache/commons/httpclient/methods/StringRequestEntity.java $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HeaderElement;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.NameValuePair;

/**
 * A RequestEntity that contains a String.
 * 
 * @since 3.0
 */
public class StringRequestEntity implements RequestEntity {

    /** The content */
    private byte[] content;
    
    /** The charset */
    private String charset;

    /** The content type (i.e. text/html; charset=EUC-JP). */
    private String contentType;
    
    /**
     * <p>Creates a new entity with the given content. This constructor 
     * will use the default platform charset to convert the content string 
     * and will provide no content type.</p>
     *  
     * @see #StringRequestEntity(String, String, String)
     * 
     * @param content The content to set.
     * 
     * @deprecated use {@link #StringRequestEntity(String, String, String)} instead
     */
    public StringRequestEntity(String content) {
        super();
        if (content == null) {
            throw new IllegalArgumentException("The content cannot be null");
        }
        this.contentType = null;
        this.charset = null;
        this.content = content.getBytes();
    }

    /**
     * Creates a new entity with the given content, content type, and charset.
     *  
     * @param content The content to set.
     * @param contentType The type of the content, or <code>null</code>.  The value retured 
     *   by {@link #getContentType()}.  If this content type contains a charset and the charset
     *   parameter is null, the content's type charset will be used.
     * @param charset The charset of the content, or <code>null</code>.  Used to convert the 
     *   content to bytes.  If the content type does not contain a charset and charset is not null,
     *   then the charset will be appended to the content type.
     */
    public StringRequestEntity(String content, String contentType, String charset) 
        throws UnsupportedEncodingException {
        super();
        if (content == null) {
            throw new IllegalArgumentException("The content cannot be null");
        }
        
        this.contentType = contentType;
        this.charset = charset;
        
        // resolve the content type and the charset
        if (contentType != null) {
            HeaderElement[] values = HeaderElement.parseElements(contentType);
            NameValuePair charsetPair = null;
            for (int i = 0; i < values.length; i++) {
                if ((charsetPair = values[i].getParameterByName("charset")) != null) {
                    // charset found
                    break;
                }
            }
            if (charset == null && charsetPair != null) {
                // use the charset from the content type
                this.charset = charsetPair.getValue();
            } else if (charset != null && charsetPair == null) {
                // append the charset to the content type
                this.contentType = contentType + "; charset=" + charset; 
            }
        }
        if (this.charset != null) {
            this.content = content.getBytes(this.charset);
        } else {
            this.content = content.getBytes();
        }
    }

    /* (non-Javadoc)
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods.RequestEntity#getContentType()
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return <code>true</code>
     */
    public boolean isRepeatable() {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.RequestEntity#writeRequest(java.io.OutputStream)
     */
    public void writeRequest(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        out.write(this.content);
        out.flush();
    }

    /**
     * @return The length of the content.
     */
    public long getContentLength() {
        return this.content.length;
    }

    /**
     * @return Returns the content.
     */
    public String getContent() {
        if (this.charset != null) {
            try {
                return new String(this.content, this.charset);
            } catch (UnsupportedEncodingException e) {
                return new String(this.content);
            }
        } else {
            return new String(this.content);
        }
    }

    /**
     * @return Returns the charset used to convert the content to bytes. <code>null</code> if
     * no charset as been specified.
     */
    public String getCharset() {
        return charset;
    }
}
