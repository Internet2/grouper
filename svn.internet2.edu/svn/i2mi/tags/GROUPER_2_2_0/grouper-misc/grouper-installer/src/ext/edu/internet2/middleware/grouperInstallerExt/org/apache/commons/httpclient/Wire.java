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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/Wire.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.LogFactory;

/**
 * Logs data to the wire LOG.
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0beta1
 */
class Wire {

    public static Wire HEADER_WIRE = new Wire(LogFactory.getLog("httpclient.wire.header"));
    
    public static Wire CONTENT_WIRE = new Wire(LogFactory.getLog("httpclient.wire.content"));
    
    /** Log for any wire messages. */
    private Log log;
    
    private Wire(Log log) {
        this.log = log;
    }
    
    private void wire(String header, InputStream instream)
      throws IOException {
        StringBuffer buffer = new StringBuffer();
        int ch;
        while ((ch = instream.read()) != -1) {
            if (ch == 13) {
                buffer.append("[\\r]");
            } else if (ch == 10) {
                    buffer.append("[\\n]\"");
                    buffer.insert(0, "\"");
                    buffer.insert(0, header);
                    log.debug(buffer.toString());
                    buffer.setLength(0);
            } else if ((ch < 32) || (ch > 127)) {
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
            } else {
                buffer.append((char) ch);
            }
        } 
        if (buffer.length() > 0) {
            buffer.append("\"");
            buffer.insert(0, "\"");
            buffer.insert(0, header);
            log.debug(buffer.toString());
        }
    }


    public boolean enabled() {
        return log.isDebugEnabled();
    }    
    
    public void output(InputStream outstream)
      throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", outstream);
    }

    public void input(InputStream instream)
      throws IOException {
        if (instream == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", instream);
    }

    public void output(byte[] b, int off, int len)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", new ByteArrayInputStream(b, off, len));
    }

    public void input(byte[] b, int off, int len)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", new ByteArrayInputStream(b, off, len));
    }

    public void output(byte[] b)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", new ByteArrayInputStream(b));
    }

    public void input(byte[] b)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", new ByteArrayInputStream(b));
    }

    public void output(int b)
      throws IOException {
        output(new byte[] {(byte) b});
    }

    public void input(int b)
      throws IOException {
        input(new byte[] {(byte) b});
    }

    public void output(final String s)
      throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        output(s.getBytes());
    }

    public void input(final String s)
      throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        input(s.getBytes());
    }
}
