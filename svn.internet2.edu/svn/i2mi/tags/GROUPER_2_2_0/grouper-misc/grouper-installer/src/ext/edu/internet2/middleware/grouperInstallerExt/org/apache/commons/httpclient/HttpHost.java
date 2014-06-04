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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/HttpHost.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.protocol.Protocol;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.LangUtils;

/**
 * Holds all of the variables needed to describe an HTTP connection to a host. This includes 
 * remote host, port and protocol.
 * 
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Laura Werner
 * 
 * @since 3.0 
 */
public class HttpHost implements Cloneable {

    /** The host to use. */
    private String hostname = null;

    /** The port to use. */
    private int port = -1;

    /** The protocol */
    private Protocol protocol = null;

    /**
     * Constructor for HttpHost.
     *   
     * @param hostname the hostname (IP or DNS name). Can be <code>null</code>.
     * @param port the port. Value <code>-1</code> can be used to set default protocol port
     * @param protocol the protocol. Value <code>null</code> can be used to set default protocol
     */
    public HttpHost(final String hostname, int port, final Protocol protocol) {
        super();
        if (hostname == null) {
            throw new IllegalArgumentException("Host name may not be null");
        }
        if (protocol == null) {
            throw new IllegalArgumentException("Protocol may not be null");
        }
        this.hostname = hostname;
        this.protocol = protocol;
        if (port >= 0) {
            this.port = port;
        } else {
            this.port = this.protocol.getDefaultPort();
        }
    }

    /**
     * Constructor for HttpHost.
     *   
     * @param hostname the hostname (IP or DNS name). Can be <code>null</code>.
     * @param port the port. Value <code>-1</code> can be used to set default protocol port
     */
    public HttpHost(final String hostname, int port) {
        this(hostname, port, Protocol.getProtocol("http"));
    }
    
    /**
     * Constructor for HttpHost.
     *   
     * @param hostname the hostname (IP or DNS name). Can be <code>null</code>.
     */
    public HttpHost(final String hostname) {
        this(hostname, -1, Protocol.getProtocol("http"));
    }
    
    /**
     * URI constructor for HttpHost.
     *   
     * @param uri the URI.
     */
    public  HttpHost(final URI uri) throws URIException {
        this(uri.getHost(), uri.getPort(), Protocol.getProtocol(uri.getScheme()));
    }

    /**
     * Copy constructor for HttpHost
     * 
     * @param httphost the HTTP host to copy details from
     */
    public HttpHost (final HttpHost httphost) {
        super();
        init(httphost);
    }

    private void init(final HttpHost httphost) {
        this.hostname = httphost.hostname;
        this.port = httphost.port;
        this.protocol = httphost.protocol;
    }

    /**
     * @throws CloneNotSupportedException 
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        HttpHost copy = (HttpHost) super.clone();
        copy.init(this);
        return copy;
    }    
    
    /**
     * Returns the host name (IP or DNS name).
     * 
     * @return the host name (IP or DNS name), or <code>null</code> if not set
     */
    public String getHostName() {
        return this.hostname;
    }

    /**
     * Returns the port.
     * 
     * @return the host port, or <code>-1</code> if not set
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the protocol.
     * @return The protocol.
     */
    public Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * Return the host uri.
     * 
     * @return The host uri.
     */
    public String toURI() {
        StringBuffer buffer = new StringBuffer(50);        
        buffer.append(this.protocol.getScheme());
        buffer.append("://");
        buffer.append(this.hostname);
        if (this.port != this.protocol.getDefaultPort()) {
            buffer.append(':');
            buffer.append(this.port);
        }
        return buffer.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(50);        
        buffer.append(toURI());
        return buffer.toString();
    }    
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object o) {
        
        if (o instanceof HttpHost) {
            // shortcut if we're comparing with ourselves
            if (o == this) { 
                return true;
            }
            HttpHost that = (HttpHost) o;
            if (!this.hostname.equalsIgnoreCase(that.hostname)) {
                return false;
            }
            if (this.port != that.port) {
                return false;
            }
            if (!this.protocol.equals(that.protocol)) {
                return false;
            }
            // everything matches
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.hostname);
        hash = LangUtils.hashCode(hash, this.port);
        hash = LangUtils.hashCode(hash, this.protocol);
        return hash;
    }

}
