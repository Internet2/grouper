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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/methods/multipart/PartBase.java,v 1.1 2008-11-30 10:57:27 mchyzer Exp $
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
 
package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods.multipart;


/**
 * Provides setters and getters for the basic Part properties.
 * 
 * @author Michael Becke
 */
public abstract class PartBase extends Part {

    /** Name of the file part. */
    private String name;
        
    /** Content type of the file part. */
    private String contentType;

    /** Content encoding of the file part. */
    private String charSet;
    
    /** The transfer encoding. */
    private String transferEncoding;

    /**
     * Constructor.
     * 
     * @param name The name of the part
     * @param contentType The content type, or <code>null</code>
     * @param charSet The character encoding, or <code>null</code> 
     * @param transferEncoding The transfer encoding, or <code>null</code>
     */
    public PartBase(String name, String contentType, String charSet, String transferEncoding) {

        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
        this.contentType = contentType;
        this.charSet = charSet;
        this.transferEncoding = transferEncoding;
    }

    /**
     * Returns the name.
     * @return The name.
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods.multipart.Part#getName()
     */
    public String getName() { 
        return this.name; 
    }

    /**
     * Returns the content type of this part.
     * @return String The name.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Return the character encoding of this part.
     * @return String The name.
     */
    public String getCharSet() {
        return this.charSet;
    }

    /**
     * Returns the transfer encoding of this part.
     * @return String The name.
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * Sets the character encoding.
     * 
     * @param charSet the character encoding, or <code>null</code> to exclude the character 
     * encoding header
     */
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    /**
     * Sets the content type.
     * 
     * @param contentType the content type, or <code>null</code> to exclude the content type header
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the part name.
     * 
     * @param name
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
    }

    /**
     * Sets the transfer encoding.
     * 
     * @param transferEncoding the transfer encoding, or <code>null</code> to exclude the 
     * transfer encoding header
     */
    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

}
