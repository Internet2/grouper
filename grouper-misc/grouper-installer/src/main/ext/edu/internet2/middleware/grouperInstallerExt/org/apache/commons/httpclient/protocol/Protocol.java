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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/protocol/Protocol.java,v 1.1 2008-11-30 10:57:26 mchyzer Exp $
 * $Revision: 1.1 $
 * $Date: 2008-11-30 10:57:26 $
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
package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.util.LangUtils;

/**
 * A class to encapsulate the specifics of a protocol.  This class class also
 * provides the ability to customize the set and characteristics of the
 * protocols used.
 * 
 * <p>One use case for modifying the default set of protocols would be to set a
 * custom SSL socket factory.  This would look something like the following:
 * <pre> 
 * Protocol myHTTPS = new Protocol( "https", new MySSLSocketFactory(), 443 );
 * 
 * Protocol.registerProtocol( "https", myHTTPS );
 * </pre>
 *
 * @author Michael Becke 
 * @author Jeff Dever
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *  
 * @since 2.0 
 */
public class Protocol {

    /** The available protocols */
    private static final Map PROTOCOLS = Collections.synchronizedMap(new HashMap());

    /**
     * Registers a new protocol with the given identifier.  If a protocol with
     * the given ID already exists it will be overridden.  This ID is the same
     * one used to retrieve the protocol from getProtocol(String).
     * 
     * @param id the identifier for this protocol
     * @param protocol the protocol to register
     * 
     * @see #getProtocol(String)
     */
    public static void registerProtocol(String id, Protocol protocol) {

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (protocol == null) {
            throw new IllegalArgumentException("protocol is null");
        }

        PROTOCOLS.put(id, protocol);
    }

    /**
     * Unregisters the protocol with the given ID.
     * 
     * @param id the ID of the protocol to remove
     */
    public static void unregisterProtocol(String id) {

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        PROTOCOLS.remove(id);
    }

    /**
     * Gets the protocol with the given ID.
     * 
     * @param id the protocol ID
     * 
     * @return Protocol a protocol
     * 
     * @throws IllegalStateException if a protocol with the ID cannot be found
     */
    public static Protocol getProtocol(String id) 
        throws IllegalStateException {

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        Protocol protocol = (Protocol) PROTOCOLS.get(id);

        if (protocol == null) {
            protocol = lazyRegisterProtocol(id);
        }

        return protocol;
    } 

    /**
     * Lazily registers the protocol with the given id.
     * 
     * @param id the protocol ID
     * 
     * @return the lazily registered protocol
     * 
     * @throws IllegalStateException if the protocol with id is not recognized
     */
    private static Protocol lazyRegisterProtocol(String id) 
        throws IllegalStateException {

        if ("http".equals(id)) {
            final Protocol http 
                = new Protocol("http", DefaultProtocolSocketFactory.getSocketFactory(), 80);
            Protocol.registerProtocol("http", http);
            return http;
        }

        if ("https".equals(id)) {
            final Protocol https 
                = new Protocol("https", SSLProtocolSocketFactory.getSocketFactory(), 443);
            Protocol.registerProtocol("https", https);
            return https;
        }

        throw new IllegalStateException("unsupported protocol: '" + id + "'");
    }
    

    /** the scheme of this protocol (e.g. http, https) */
    private String scheme;
    
    /** The socket factory for this protocol */
    private ProtocolSocketFactory socketFactory;
    
    /** The default port for this protocol */
    private int defaultPort;
    
    /** True if this protocol is secure */
    private boolean secure;
  
    /**
     * Constructs a new Protocol. Whether the created protocol is secure depends on
     * the class of <code>factory</code>.
     * 
     * @param scheme the scheme (e.g. http, https)
     * @param factory the factory for creating sockets for communication using
     * this protocol
     * @param defaultPort the port this protocol defaults to
     */
    public Protocol(String scheme, ProtocolSocketFactory factory, int defaultPort) {
        
        if (scheme == null) {
            throw new IllegalArgumentException("scheme is null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("socketFactory is null");
        }
        if (defaultPort <= 0) {
            throw new IllegalArgumentException("port is invalid: " + defaultPort);
        }
        
        this.scheme = scheme;
        this.socketFactory = factory;
        this.defaultPort = defaultPort;
        this.secure = (factory instanceof SecureProtocolSocketFactory);
    }
    
    /**
     * Constructs a new Protocol. Whether the created protocol is secure depends on
     * the class of <code>factory</code>.
     *
     * @param scheme the scheme (e.g. http, https)
     * @param factory the factory for creating sockets for communication using
     * this protocol
     * @param defaultPort the port this protocol defaults to
     * @deprecated Use the constructor that uses ProtocolSocketFactory, this version of
     * the constructor is only kept for backwards API compatibility.
     */
    public Protocol(String scheme, 
        SecureProtocolSocketFactory factory, int defaultPort) {
        this(scheme, (ProtocolSocketFactory) factory, defaultPort);   
    }    
    
    /**
     * Returns the defaultPort.
     * @return int
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Returns the socketFactory.  If secure the factory is a
     * SecureProtocolSocketFactory.
     * @return SocketFactory
     */
    public ProtocolSocketFactory getSocketFactory() {
        return socketFactory;
    }

    /**
     * Returns the scheme.
     * @return The scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns true if this protocol is secure
     * @return true if this protocol is secure
     */
    public boolean isSecure() {
        return secure;
    }
    
    /**
     * Resolves the correct port for this protocol.  Returns the given port if
     * valid or the default port otherwise.
     * 
     * @param port the port to be resolved
     * 
     * @return the given port or the defaultPort
     */
    public int resolvePort(int port) {
        return port <= 0 ? getDefaultPort() : port;
    }

    /**
     * Return a string representation of this object.
     * @return a string representation of this object.
     */
    public String toString() {
        return scheme + ":" + defaultPort;
    }
    
    /**
     * Return true if the specified object equals this object.
     * @param obj The object to compare against.
     * @return true if the objects are equal.
     */
    public boolean equals(Object obj) {
        
        if (obj instanceof Protocol) {
            
            Protocol p = (Protocol) obj;
            
            return (
                defaultPort == p.getDefaultPort()
                && scheme.equalsIgnoreCase(p.getScheme())
                && secure == p.isSecure()
                && socketFactory.equals(p.getSocketFactory()));
            
        } else {
            return false;
        }
        
    }

    /**
     * Return a hash code for this object
     * @return The hash code.
     */
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.defaultPort);
        hash = LangUtils.hashCode(hash, this.scheme.toLowerCase());
        hash = LangUtils.hashCode(hash, this.secure);
        hash = LangUtils.hashCode(hash, this.socketFactory);
        return hash;
    }
}
