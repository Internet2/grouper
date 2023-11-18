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
package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient;

/**
 * Signals that the response content was larger than anticipated. 
 * 
 * @author Ortwin Gluck
 */
public class HttpContentTooLargeException extends HttpException {
    private int maxlen;

    public HttpContentTooLargeException(String message, int maxlen) {
        super(message);
        this.maxlen = maxlen;
    }
    
    /**
     * @return the maximum anticipated content length in bytes.
     */
    public int getMaxLength() {
        return maxlen;
    }
}
