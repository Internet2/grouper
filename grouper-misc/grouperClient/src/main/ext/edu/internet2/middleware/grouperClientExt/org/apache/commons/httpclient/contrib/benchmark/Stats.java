/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/contrib/org/apache/commons/httpclient/contrib/benchmark/Stats.java $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
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
package edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.contrib.benchmark;

/**
 * <p>Benchmark statistics</p>
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision: 480424 $
 */
public class Stats {

    private long startTime = -1;
    private long finishTime = -1;
    private int successCount = 0;
    private int failureCount = 0;
    private String serverName = null;
    private long total = 0;
    private long contentLength = -1;
    
    public Stats() {
        super();
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void finish() {
        this.finishTime = System.currentTimeMillis();
    }

    public long getFinishTime() {
        return this.finishTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getDuration() {
        if (this.startTime < 0 || this.finishTime < 0) {
            throw new IllegalStateException();
        }
        return this.finishTime - this.startTime; 
    }
    
    public void incSuccessCount() {
        this.successCount++;
    }
    
    public void incFailureCount() {
        this.failureCount++;
    }

    public int getFailureCount() {
        return this.failureCount;
    }

    public int getSuccessCount() {
        return this.successCount;
    }

    public long getTotal() {
        return this.total;
    }
    
    public void incTotal(int n) {
        this.total += n;
    }
    
    public long getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }   
    
}
