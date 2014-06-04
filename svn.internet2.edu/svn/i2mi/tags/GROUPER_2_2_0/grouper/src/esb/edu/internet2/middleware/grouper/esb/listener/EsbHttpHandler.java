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
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * Class the processes data recieved on HTTP(S) port, extracts the payload and passes it to {@link EsbListener} 
 * for processing, returning an http result code and human readable result string to calling client
 *
 */
public class EsbHttpHandler extends AbstractHandler {

  private GrouperSession grouperSession;

  private EsbListener esbListener;

  private static final Log LOG = GrouperUtil.getLog(EsbHttpHandler.class);

  public void handle(String target, HttpServletRequest request,
      HttpServletResponse response, int dispatch)
      throws IOException, ServletException {
    Request base_request = (request instanceof Request) ? (Request) request
        : HttpConnection.getCurrentConnection().getRequest();
    base_request.setHandled(true);
    int content_length = request.getContentLength();
    if (content_length < 0) {
      if (LOG.isDebugEnabled())
        LOG.debug("Invalid content received, ignoring");
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      response.flushBuffer();
    } else {
      byte[] data = new byte[content_length];
      DataInputStream in = new DataInputStream(request.getInputStream());
      in.readFully(data);
      in.close();
      String jsonString = new String(data);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Event received by HTTP server " + jsonString);
      }
      // start a session and store it - should this be a staticSession?
      if (this.grouperSession == null)
        this.grouperSession = GrouperSession.startRootSession();
      if (this.esbListener == null)
        this.esbListener = new EsbListener();
      String result = esbListener.processEvent(jsonString, grouperSession);
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().print(result);
      response.flushBuffer();
      if (LOG.isDebugEnabled()) {
        LOG.debug("Result " + result);
      }
    }
  }

}
