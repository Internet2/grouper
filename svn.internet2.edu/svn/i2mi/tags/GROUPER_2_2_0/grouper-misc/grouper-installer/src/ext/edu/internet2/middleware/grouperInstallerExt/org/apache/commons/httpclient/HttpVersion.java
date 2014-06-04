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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/HttpVersion.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

/**
 *  <p>HTTP version, as specified in RFC 2616.</p>
 *  <p>
 *  HTTP uses a "&lt;major&gt;.&lt;minor&gt;" numbering scheme to indicate
 *  versions of the protocol. The protocol versioning policy is intended to
 *  allow the sender to indicate the format of a message and its capacity for
 *  understanding further HTTP communication, rather than the features
 *  obtained via that communication. No change is made to the version
 *  number for the addition of message components which do not affect
 *  communication behavior or which only add to extensible field values.
 *  The &lt;minor&gt; number is incremented when the changes made to the
 *  protocol add features which do not change the general message parsing
 *  algorithm, but which may add to the message semantics and imply
 *  additional capabilities of the sender. The &lt;major&gt; number is
 *  incremented when the format of a message within the protocol is
 *  changed. See RFC 2145 [36] for a fuller explanation.
 *  </p>
 *  <p>
 *  The version of an HTTP message is indicated by an HTTP-Version field
 *  in the first line of the message.
 *  </p>
 *  <pre>
 *     HTTP-Version   = "HTTP" "/" 1*DIGIT "." 1*DIGIT
 *  </pre>
 *  <p>
 *   Note that the major and minor numbers MUST be treated as separate
 *   integers and that each MAY be incremented higher than a single digit.
 *   Thus, HTTP/2.4 is a lower version than HTTP/2.13, which in turn is
 *   lower than HTTP/12.3. Leading zeros MUST be ignored by recipients and
 *   MUST NOT be sent.
 *  </p>
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 1.1 $ $Date: 2008-11-30 10:57:19 $
 *
 * @since 3.0
 */
public class HttpVersion implements Comparable {

    /** Major version number of the HTTP protocol */
    private int major = 0;

    /** Minor version number of the HTTP protocol */
    private int minor = 0;

    /** HTTP protocol version 0.9 */
    public static final HttpVersion HTTP_0_9 = new HttpVersion(0, 9);  

    /** HTTP protocol version 1.0 */
    public static final HttpVersion HTTP_1_0 = new HttpVersion(1, 0);  

    /** HTTP protocol version 1.1 */
    public static final HttpVersion HTTP_1_1 = new HttpVersion(1, 1);  
    
    /**
     * Create an HTTP protocol version designator.
     *
     * @param major   the major version number of the HTTP protocol
     * @param minor   the minor version number of the HTTP protocol
     * 
     * @throws IllegalArgumentException if either major or minor version number is negative
     */
    public HttpVersion(int major, int minor) {
        if (major < 0) {
            throw new IllegalArgumentException("HTTP major version number may not be negative");
        }
        this.major = major;
        if (minor < 0) {
            throw new IllegalArgumentException("HTTP minor version number may not be negative");
        }
        this.minor = minor;
    }
    
    /**
     * Returns the major version number of the HTTP protocol.
     * 
     * @return the major version number.
     */
    public int getMajor() {
        return major;
    }

    /**
     * Returns the minor version number of the HTTP protocol.
     * 
     * @return the minor version number.
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.major * 100000 + this.minor;
    }
        
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpVersion)) {
            return false;
        }
        return equals((HttpVersion)obj);  
    }

    /**
     * Compares this HTTP protocol version with another one.
     * 
     * @param anotherVer the version to be compared with.
     *  
     * @return a negative integer, zero, or a positive integer as this version is less than, 
     *    equal to, or greater than the specified version.     
     */
    public int compareTo(HttpVersion anotherVer) {
        if (anotherVer == null) {
            throw new IllegalArgumentException("Version parameter may not be null"); 
        }
        int delta = getMajor() - anotherVer.getMajor();
        if (delta == 0) {
            delta = getMinor() - anotherVer.getMinor();
        }
        return delta;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return compareTo((HttpVersion)o);
    }

    /**
     * Test if the HTTP protocol version is equal to the given number.
     * 
     * @return <tt>true</tt> if HTTP protocol version is given to the given number, 
     *         <tt>false</tt> otherwise.
     */
    public boolean equals(HttpVersion version) {
        return compareTo(version) == 0;  
    }

    /**
     * Test if the HTTP protocol version is greater or equal to the given number.
     * 
     * @return <tt>true</tt> if HTTP protocol version is greater or equal given to the 
     *         given number, <tt>false</tt> otherwise.
     */
    public boolean greaterEquals(HttpVersion version) {
        return compareTo(version) >= 0;
    }

    /**
     * Test if the HTTP protocol version is less or equal to the given number.
     * 
     * @return <tt>true</tt> if HTTP protocol version is less or equal to given to the 
     *         given number, <tt>false</tt> otherwise.
     */
    public boolean lessEquals(HttpVersion version) {
        return compareTo(version) <= 0;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/"); 
        buffer.append(this.major); 
        buffer.append('.'); 
        buffer.append(this.minor); 
        return buffer.toString();
    }

    /**
     * Parses the textual representation of the given HTTP protocol version.
     * 
     * @return HTTP protocol version.
     * 
     * @throws ProtocolException if the string is not a valid HTTP protocol version. 
     */
    public static HttpVersion parse(final String s) throws ProtocolException {
        if (s == null) {
            throw new IllegalArgumentException("String may not be null");
        }
        if (!s.startsWith("HTTP/")) {
            throw new ProtocolException("Invalid HTTP version string: " + s);
        }
        int major, minor;
        
        int i1 = "HTTP/".length();
        int i2 = s.indexOf(".", i1);
        if (i2 == -1) {
            throw new ProtocolException("Invalid HTTP version number: " + s);
        }
        try {
            major = Integer.parseInt(s.substring(i1, i2)); 
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid HTTP major version number: " + s);
        }
        i1 = i2 + 1;
        i2 = s.length();
        try {
            minor = Integer.parseInt(s.substring(i1, i2)); 
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid HTTP minor version number: " + s);
        }
        return new HttpVersion(major, minor);
    }

}
