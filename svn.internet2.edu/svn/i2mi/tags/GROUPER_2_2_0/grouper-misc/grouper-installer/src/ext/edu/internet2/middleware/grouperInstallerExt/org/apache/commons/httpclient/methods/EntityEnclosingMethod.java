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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/methods/EntityEnclosingMethod.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.ChunkedOutputStream;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Header;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpConnection;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpException;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpState;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpVersion;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.ProtocolException;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * This abstract class serves as a foundation for all HTTP methods 
 * that can enclose an entity within requests 
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 *
 * @since 2.0beta1
 * @version $Revision: 1.1 $
 */
public abstract class EntityEnclosingMethod extends ExpectContinueMethod {

    // ----------------------------------------- Static variables/initializers

    /**
     * The content length will be calculated automatically. This implies
     * buffering of the content.
     * @deprecated Use {@link InputStreamRequestEntity#CONTENT_LENGTH_AUTO}.
     */
    public static final long CONTENT_LENGTH_AUTO = InputStreamRequestEntity.CONTENT_LENGTH_AUTO;

    /**
     * The request will use chunked transfer encoding. Content length is not
     * calculated and the content is not buffered.<br>
     * @deprecated Use {@link #setContentChunked(boolean)}.
     */
    public static final long CONTENT_LENGTH_CHUNKED = -1;

    /** LOG object for this class. */
    private static final Log LOG = LogFactory.getLog(EntityEnclosingMethod.class);

    /** The unbuffered request body, if any. */
    private InputStream requestStream = null;

    /** The request body as string, if any. */
    private String requestString = null;

    private RequestEntity requestEntity;
    
    /** Counts how often the request was sent to the server. */
    private int repeatCount = 0;

    /** The content length of the <code>requestBodyStream</code> or one of
     *  <code>CONTENT_LENGTH_AUTO</code> and <code>CONTENT_LENGTH_CHUNKED</code>.
     * 
     * @deprecated
     */
    private long requestContentLength = InputStreamRequestEntity.CONTENT_LENGTH_AUTO;
    
    private boolean chunked = false;

    // ----------------------------------------------------------- Constructors

    /**
     * No-arg constructor.
     *
     * @since 2.0
     */
    public EntityEnclosingMethod() {
        super();
        setFollowRedirects(false);
    }

    /**
     * Constructor specifying a URI.
     *
     * @param uri either an absolute or relative URI
     *
     * @since 2.0
     */
    public EntityEnclosingMethod(String uri) {
        super(uri);
        setFollowRedirects(false);
    }

    /**
     * Returns <tt>true</tt> if there is a request body to be sent.
     * 
     * <P>This method must be overridden by sub-classes that implement
     * alternative request content input methods
     * </p>
     * 
     * @return boolean
     * 
     * @since 2.0beta1
     */
    protected boolean hasRequestContent() {
        LOG.trace("enter EntityEnclosingMethod.hasRequestContent()");
        return (this.requestEntity != null) 
            || (this.requestStream != null) 
            || (this.requestString != null);
    }

    /**
     * Clears the request body.
     * 
     * <p>This method must be overridden by sub-classes that implement
     * alternative request content input methods.</p>
     * 
     * @since 2.0beta1
     */
    protected void clearRequestBody() {
        LOG.trace("enter EntityEnclosingMethod.clearRequestBody()");
        this.requestStream = null;
        this.requestString = null;
        this.requestEntity = null;
    }

    /**
     * Generates the request body.   
     * 
     * <p>This method must be overridden by sub-classes that implement
     * alternative request content input methods.</p>
     * 
     * @return request body as an array of bytes. If the request content 
     *          has not been set, returns <tt>null</tt>.
     * 
     * @since 2.0beta1
     */
    protected byte[] generateRequestBody() {
        LOG.trace("enter EntityEnclosingMethod.renerateRequestBody()");
        return null;
    }

    protected RequestEntity generateRequestEntity() {
        
        byte[] requestBody = generateRequestBody();
        if (requestBody != null) {
            // use the request body, if it exists.
            // this is just for backwards compatability
            this.requestEntity = new ByteArrayRequestEntity(requestBody);
        } else if (this.requestStream != null) {
            this.requestEntity = new InputStreamRequestEntity(
                requestStream, 
                requestContentLength);
            this.requestStream = null;
        } else if (this.requestString != null) {
            String charset = getRequestCharSet(); 
            try {
                this.requestEntity = new StringRequestEntity(
                        requestString, null, charset);
            } catch (UnsupportedEncodingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(charset + " not supported");
                }
                try {
                    this.requestEntity = new StringRequestEntity(
                            requestString, null, null);
                } catch (UnsupportedEncodingException ignore) {
                }
            }
        }

        return this.requestEntity;
    }
    
    /**
     * Entity enclosing requests cannot be redirected without user intervention
     * according to RFC 2616.
     *
     * @return <code>false</code>.
     *
     * @since 2.0
     */
    public boolean getFollowRedirects() {
        return false;
    }


    /**
     * Entity enclosing requests cannot be redirected without user intervention 
     * according to RFC 2616.
     *
     * @param followRedirects must always be <code>false</code>
     */
    public void setFollowRedirects(boolean followRedirects) {
        if (followRedirects == true) {
            throw new IllegalArgumentException("Entity enclosing requests cannot be redirected without user intervention");
        }
        super.setFollowRedirects(false);
    }

    /**
     * Sets length information about the request body.
     *
     * <p>
     * Note: If you specify a content length the request is unbuffered. This
     * prevents redirection and automatic retry if a request fails the first
     * time. This means that the HttpClient can not perform authorization
     * automatically but will throw an Exception. You will have to set the
     * necessary 'Authorization' or 'Proxy-Authorization' headers manually.
     * </p>
     *
     * @param length size in bytes or any of CONTENT_LENGTH_AUTO,
     *        CONTENT_LENGTH_CHUNKED. If number of bytes or CONTENT_LENGTH_CHUNKED
     *        is specified the content will not be buffered internally and the
     *        Content-Length header of the request will be used. In this case
     *        the user is responsible to supply the correct content length.
     *        If CONTENT_LENGTH_AUTO is specified the request will be buffered
     *        before it is sent over the network.
     * 
     * @deprecated Use {@link #setContentChunked(boolean)} or 
     * {@link #setRequestEntity(RequestEntity)}
     */
    public void setRequestContentLength(int length) {
        LOG.trace("enter EntityEnclosingMethod.setRequestContentLength(int)");
        this.requestContentLength = length;
    }

    /**
     * Returns the request's charset.  The charset is parsed from the request entity's 
     * content type, unless the content type header has been set manually. 
     * 
     * @see RequestEntity#getContentType()
     * 
     * @since 3.0
     */
    public String getRequestCharSet() {
        if (getRequestHeader("Content-Type") == null) {
            // check the content type from request entity
            // We can't call getRequestEntity() since it will probably call
            // this method.
            if (this.requestEntity != null) {
                return getContentCharSet(
                    new Header("Content-Type", requestEntity.getContentType()));
            } else {
                return super.getRequestCharSet();
            }
        } else {
            return super.getRequestCharSet();
        }
    }

    /**
     * Sets length information about the request body.
     *
     * <p>
     * Note: If you specify a content length the request is unbuffered. This
     * prevents redirection and automatic retry if a request fails the first
     * time. This means that the HttpClient can not perform authorization
     * automatically but will throw an Exception. You will have to set the
     * necessary 'Authorization' or 'Proxy-Authorization' headers manually.
     * </p>
     *
     * @param length size in bytes or any of CONTENT_LENGTH_AUTO,
     *        CONTENT_LENGTH_CHUNKED. If number of bytes or CONTENT_LENGTH_CHUNKED
     *        is specified the content will not be buffered internally and the
     *        Content-Length header of the request will be used. In this case
     *        the user is responsible to supply the correct content length.
     *        If CONTENT_LENGTH_AUTO is specified the request will be buffered
     *        before it is sent over the network.
     * 
     * @deprecated Use {@link #setContentChunked(boolean)} or 
     * {@link #setRequestEntity(RequestEntity)}
     */
    public void setRequestContentLength(long length) {
        LOG.trace("enter EntityEnclosingMethod.setRequestContentLength(int)");
        this.requestContentLength = length;
    }

    /**
     * Sets whether or not the content should be chunked.
     * 
     * @param chunked <code>true</code> if the content should be chunked
     * 
     * @since 3.0
     */
    public void setContentChunked(boolean chunked) {
        this.chunked = chunked;
    }
    
    /**
     * Returns the length of the request body.
     *
     * @return number of bytes in the request body
     */
    protected long getRequestContentLength() {
        LOG.trace("enter EntityEnclosingMethod.getRequestContentLength()");

        if (!hasRequestContent()) {
            return 0;
        }
        if (this.chunked) {
            return -1;
        }
        if (this.requestEntity == null) {
            this.requestEntity = generateRequestEntity(); 
        }
        return (this.requestEntity == null) ? 0 : this.requestEntity.getContentLength();
    }

    /**
     * Populates the request headers map to with additional 
     * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Header headers} to be submitted to 
     * the given {@link HttpConnection}.
     *
     * <p>
     * This implementation adds tt>Content-Length</tt> or <tt>Transfer-Encoding</tt>
     * headers.
     * </p>
     *
     * <p>
     * Subclasses may want to override this method to to add additional
     * headers, and may choose to invoke this implementation (via
     * <tt>super</tt>) to add the "standard" headers.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs. Some transport exceptions
     *                     can be recovered from.
     * @throws HttpException  if a protocol exception occurs. Usually protocol exceptions 
     *                    cannot be recovered from.
     *
     * @see #writeRequestHeaders
     * 
     * @since 3.0
     */
    protected void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter EntityEnclosingMethod.addRequestHeaders(HttpState, "
            + "HttpConnection)");

        super.addRequestHeaders(state, conn);
        addContentLengthRequestHeader(state, conn);

        // only use the content type of the request entity if it has not already been
        // set manually
        if (getRequestHeader("Content-Type") == null) {
            RequestEntity requestEntity = getRequestEntity();
            if (requestEntity != null && requestEntity.getContentType() != null) {
                setRequestHeader("Content-Type", requestEntity.getContentType());
            }
        }
    }
    
    /**
     * Generates <tt>Content-Length</tt> or <tt>Transfer-Encoding: Chunked</tt>
     * request header, as long as no <tt>Content-Length</tt> request header
     * already exists.
     *
     * @param state current state of http requests
     * @param conn the connection to use for I/O
     *
     * @throws IOException when errors occur reading or writing to/from the
     *         connection
     * @throws HttpException when a recoverable error occurs
     */
    protected void addContentLengthRequestHeader(HttpState state,
                                                 HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter EntityEnclosingMethod.addContentLengthRequestHeader("
                  + "HttpState, HttpConnection)");

        if ((getRequestHeader("content-length") == null) 
            && (getRequestHeader("Transfer-Encoding") == null)) {
            long len = getRequestContentLength();
            if (len < 0) {
                if (getEffectiveVersion().greaterEquals(HttpVersion.HTTP_1_1)) {
                    addRequestHeader("Transfer-Encoding", "chunked");
                } else {
                    throw new ProtocolException(getEffectiveVersion() + 
                        " does not support chunk encoding");
                }
            } else {
                addRequestHeader("Content-Length", String.valueOf(len));
            }
        }
    }

    /**
     * Sets the request body to be the specified inputstream.
     *
     * @param body Request body content as {@link java.io.InputStream}
     * 
     * @deprecated use {@link #setRequestEntity(RequestEntity)}
     */
    public void setRequestBody(InputStream body) {
        LOG.trace("enter EntityEnclosingMethod.setRequestBody(InputStream)");
        clearRequestBody();
        this.requestStream = body;
    }

    /**
     * Sets the request body to be the specified string.
     * The string will be submitted, using the encoding
     * specified in the Content-Type request header.<br>
     * Example: <code>setRequestHeader("Content-type", "text/xml; charset=UTF-8");</code><br>
     * Would use the UTF-8 encoding.
     * If no charset is specified, the 
     * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpConstants#DEFAULT_CONTENT_CHARSET default}
     * content encoding is used (ISO-8859-1).
     *
     * @param body Request body content as a string
     * 
     * @deprecated use {@link #setRequestEntity(RequestEntity)}
     */
    public void setRequestBody(String body) {
        LOG.trace("enter EntityEnclosingMethod.setRequestBody(String)");
        clearRequestBody();
        this.requestString = body;
    }

    /**
     * Writes the request body to the given {@link HttpConnection connection}.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @return <tt>true</tt>
     *
     * @throws IOException if an I/O (transport) error occurs. Some transport exceptions
     *                     can be recovered from.
     * @throws HttpException  if a protocol exception occurs. Usually protocol exceptions 
     *                    cannot be recovered from.
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace(
            "enter EntityEnclosingMethod.writeRequestBody(HttpState, HttpConnection)");
        
        if (!hasRequestContent()) {
            LOG.debug("Request body has not been specified");
            return true;
        }
        if (this.requestEntity == null) {
            this.requestEntity = generateRequestEntity(); 
        }
        if (requestEntity == null) {
            LOG.debug("Request body is empty");
            return true;
        }

        long contentLength = getRequestContentLength();

        if ((this.repeatCount > 0) && !requestEntity.isRepeatable()) {
            throw new ProtocolException(
                "Unbuffered entity enclosing request can not be repeated.");
        }

        this.repeatCount++;

        OutputStream outstream = conn.getRequestOutputStream();
        
        if (contentLength < 0) {
            outstream = new ChunkedOutputStream(outstream);
        }
        
        requestEntity.writeRequest(outstream);
        
        // This is hardly the most elegant solution to closing chunked stream
        if (outstream instanceof ChunkedOutputStream) {
            ((ChunkedOutputStream) outstream).finish();
        }
        
        outstream.flush();
        
        LOG.debug("Request body sent");
        return true;
    }

    /**
     * Recycles the HTTP method so that it can be used again.
     * Note that all of the instance variables will be reset
     * once this method has been called. This method will also
     * release the connection being used by this HTTP method.
     * 
     * @see #releaseConnection()
     * 
     * @deprecated no longer supported and will be removed in the future
     *             version of HttpClient
     */
    public void recycle() {
        LOG.trace("enter EntityEnclosingMethod.recycle()");
        clearRequestBody();
        this.requestContentLength = InputStreamRequestEntity.CONTENT_LENGTH_AUTO;
        this.repeatCount = 0;
        this.chunked = false;
        super.recycle();
    }

    /**
     * @return Returns the requestEntity.
     * 
     * @since 3.0
     */
    public RequestEntity getRequestEntity() {
        return generateRequestEntity();
    }

    /**
     * @param requestEntity The requestEntity to set.
     * 
     * @since 3.0
     */
    public void setRequestEntity(RequestEntity requestEntity) {
        clearRequestBody();
        this.requestEntity = requestEntity;
    }

}
