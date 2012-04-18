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
package edu.internet2.middleware.grouper.esb;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.esb.listener.EsbListener;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * Class to processes data received on servlet interface, extracts the payload
 * and passes it to {@link EsbListener} for processing, returning an http result
 * code and human readable result string to calling client
 * 
 * @author rob
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("serial")
public class EsbHttpHandler extends HttpServlet {

    /**
     * Field LOG.
     */
    private static final Log LOG = GrouperUtil.getLog(EsbHttpHandler.class);

    /**
     * Method doPost.
     * 
     * @param request
     *            HttpServletRequest
     * @param response
     *            HttpServletResponse
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            if (request.getContentLength() < 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid content received, ignoring");
                }
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                response.flushBuffer();
            } else {
                final byte[] data = new byte[request.getContentLength()];
                final DataInputStream in = new DataInputStream(
                        request.getInputStream());
                in.readFully(data);
                in.close();
                final String jsonString = new String(data);
                final GrouperSession grouperSession = GrouperSession
                        .startRootSession();
                final EsbListener esbListener = new EsbListener();
                final String result = esbListener.processEvent(jsonString,
                        grouperSession);
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(result);
                response.flushBuffer();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Result " + result);
                }
                grouperSession.stop();

            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw e;

        }

    }

}
