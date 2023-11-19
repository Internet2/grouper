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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/methods/multipart/StringPart.java,v 1.1 2008-11-30 10:57:27 mchyzer Exp $
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

import java.io.OutputStream;
import java.io.IOException;


import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.EncodingUtil;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * Simple string parameter for a multipart post
 *
 * @author <a href="mailto:mattalbright@yahoo.com">Matthew Albright</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * @since 2.0
 */
public class StringPart extends PartBase {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(StringPart.class);

    /** Default content encoding of string parameters. */
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    /** Default charset of string parameters*/
    public static final String DEFAULT_CHARSET = "US-ASCII";

    /** Default transfer encoding of string parameters*/
    public static final String DEFAULT_TRANSFER_ENCODING = "8bit";

    /** Contents of this StringPart. */
    private byte[] content;
    
    /** The String value of this part. */
    private String value;

    /**
     * Constructor.
     *
     * @param name The name of the part
     * @param value the string to post
     * @param charset the charset to be used to encode the string, if <code>null</code> 
     * the {@link #DEFAULT_CHARSET default} is used
     */
    public StringPart(String name, String value, String charset) {
        
        super(
            name,
            DEFAULT_CONTENT_TYPE,
            charset == null ? DEFAULT_CHARSET : charset,
            DEFAULT_TRANSFER_ENCODING
        );
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        }
        if (value.indexOf(0) != -1) {
            // See RFC 2048, 2.8. "8bit Data"
            throw new IllegalArgumentException("NULs may not be present in string parts");
        }
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param name The name of the part
     * @param value the string to post
     */
    public StringPart(String name, String value) {
        this(name, value, null);
    }
    
    /**
     * Gets the content in bytes.  Bytes are lazily created to allow the charset to be changed
     * after the part is created.
     * 
     * @return the content in bytes
     */
    private byte[] getContent() {
        if (content == null) {
            content = EncodingUtil.getBytes(value, getCharSet());
        }
        return content;
    }
    
    /**
     * Writes the data to the given OutputStream.
     * @param out the OutputStream to write to
     * @throws IOException if there is a write error
     */
    protected void sendData(OutputStream out) throws IOException {
        LOG.trace("enter sendData(OutputStream)");
        out.write(getContent());
    }
    
    /**
     * Return the length of the data.
     * @return The length of the data.
     * @throws IOException If an IO problem occurs
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods.multipart.Part#lengthOfData()
     */
    protected long lengthOfData() throws IOException {
        LOG.trace("enter lengthOfData()");
        return getContent().length;
    }
    
    /* (non-Javadoc)
     * @see edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods.multipart.BasePart#setCharSet(java.lang.String)
     */
    public void setCharSet(String charSet) {
        super.setCharSet(charSet);
        this.content = null;
    }

}
