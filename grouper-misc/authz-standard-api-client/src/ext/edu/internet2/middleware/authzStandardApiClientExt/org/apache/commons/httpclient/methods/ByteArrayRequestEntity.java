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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/edu.internet2.middleware.authzStandardApiClientExt.org/apache/commons/httpclient/methods/ByteArrayRequestEntity.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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
package edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.methods;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A RequestEntity that contains an array of bytes.
 * 
 * @since 3.0
 */
public class ByteArrayRequestEntity implements RequestEntity {

    /** The content */
    private byte[] content;
    
    /** The content type */
    private String contentType;

    /**
     * Creates a new entity with the given content.
     * @param content The content to set.
     */
    public ByteArrayRequestEntity(byte[] content) {
        this(content, null);
    }
    
    /**
     * Creates a new entity with the given content and content type.
     * @param content The content to set.
     * @param contentType The content type to set or <code>null</code>.
     */
    public ByteArrayRequestEntity(byte[] content, String contentType) {
        super();
        if (content == null) {
            throw new IllegalArgumentException("The content cannot be null");
        }
        this.content = content;
        this.contentType = contentType;
    }

    /**
     * @return <code>true</code>
     */
    public boolean isRepeatable() {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.methods.RequestEntity#getContentType()
     */
    public String getContentType() {
        return contentType;
    }
    
    /* (non-Javadoc)
     * @see edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.RequestEntity#writeRequest(java.io.OutputStream)
     */
    public void writeRequest(OutputStream out) throws IOException {
        out.write(content);
    }

    /**
     * @return The length of the content.
     */
    public long getContentLength() {
        return content.length;
    }

    /**
     * @return Returns the content.
     */
    public byte[] getContent() {
        return content;
    }

}
