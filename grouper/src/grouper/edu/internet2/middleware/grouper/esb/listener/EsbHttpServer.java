/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 * Class to start a simple HTTP/HTTPS server to listen
 * for incoming event notifications to process as changes in the
 * Grouper registry. Server is started as a quartz job
 * <p>
 * SSL and basic auth supported for security. All configuration in grouper-loader.properties
 * <p>
 * Security: every request to this listener adds or removes memberships of any group as
 * GrouperSystem (see {@link EsbHttpHandler} and {@link EsbListener}), so the listener must
 * never accept an unauthenticated request.  It therefore fails closed:
 * <ul>
 * <li>it does not start unless esb.listeners.http.authConfigFile is set and the Jetty
 * HashUserRealm file loads (it used to start without authentication when the setting was
 * blank, and to carry on without a realm when the file did not load);</li>
 * <li>the membership handler is wrapped inside the Jetty SecurityHandler, so a request that
 * fails Basic authentication never reaches it (it used to be a sibling handler, and Jetty
 * runs every sibling, so the membership change happened even when authentication failed);</li>
 * <li>an extra guard inside the SecurityHandler refuses any request that arrives without an
 * authenticated user, in case a request path does not match the security constraint.</li>
 * </ul>
 * Every user in the realm file with role user or grouper may still change the membership of
 * any group as root; there is no per-group control.  Only list trusted systems in that file.
 *
 */
@DisallowConcurrentExecution
public class EsbHttpServer implements Job {

  private static final Log LOG = GrouperUtil.getLog(EsbHttpServer.class);

  /** realm name in the Basic authentication challenge */
  static final String REALM_NAME = "Grouper";

  /**
   * Build the handler chain for the listener: SecurityHandler (Basic auth against the realm
   * file) wrapping an authenticated-user guard wrapping the membership handler.
   * Returns null when the listener must not start (no auth file, or it does not load);
   * the reason is logged as an error.
   * @param authConfigFile Jetty HashUserRealm file (username: password,role,...)
   * @return the handler to install on the server, or null if the listener must not start
   */
  static Handler buildHandler(String authConfigFile) {
    return buildHandler(authConfigFile, new EsbHttpHandler());
  }

  /**
   * Build the handler chain around the given application handler.  Package visible so the
   * unit test can check the chain with a stub handler.
   * @param authConfigFile Jetty HashUserRealm file (username: password,role,...)
   * @param applicationHandler handler that does the work, only reached when authenticated
   * @return the handler to install on the server, or null if the listener must not start
   */
  static Handler buildHandler(String authConfigFile, Handler applicationHandler) {

    // fail closed: no auth file means no listener (running without authentication used to be
    // allowed, and lets anyone who can reach the port change any membership as root)
    if (authConfigFile == null || authConfigFile.trim().equals("")) {
      LOG.error("ESB HTTP listener not started: esb.listeners.http.authConfigFile is required "
          + "(a Jetty HashUserRealm file).  The listener no longer runs without authentication.");
      return null;
    }

    // fail closed: if the realm file does not load, do not start (it used to print the stack
    // trace and start anyway)
    HashUserRealm hashUserRealm = null;
    try {
      hashUserRealm = new HashUserRealm(REALM_NAME, authConfigFile);
    } catch (IOException e) {
      LOG.error("ESB HTTP listener not started: cannot load esb.listeners.http.authConfigFile: "
          + authConfigFile, e);
      return null;
    } catch (RuntimeException re) {
      LOG.error("ESB HTTP listener not started: cannot load esb.listeners.http.authConfigFile: "
          + authConfigFile, re);
      return null;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Requiring basic auth");
    }
    Constraint constraint = new Constraint();
    constraint.setName(Constraint.__BASIC_AUTH);
    constraint.setRoles(new String[] { "user", "grouper" });
    constraint.setAuthenticate(true);

    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec("/*");

    SecurityHandler sh = new SecurityHandler();
    sh.setUserRealm(hashUserRealm);
    sh.setConstraintMappings(new ConstraintMapping[] { cm });

    // the application handler is a CHILD of the security handler, not a sibling: SecurityHandler
    // (a HandlerWrapper) only calls its child when the constraint is satisfied
    RequireAuthenticatedUserHandler requireAuthenticatedUserHandler = new RequireAuthenticatedUserHandler();
    requireAuthenticatedUserHandler.setHandler(applicationHandler);
    sh.setHandler(requireAuthenticatedUserHandler);
    return sh;
  }

  /**
   * Guard inside the SecurityHandler: only pass the request on when Jetty has an authenticated
   * user for it, else answer 401 and stop.  Defense in depth so the membership handler is never
   * reached without authentication, whatever the constraint mapping does.
   */
  static class RequireAuthenticatedUserHandler extends HandlerWrapper {

    /**
     * @see org.mortbay.jetty.handler.HandlerWrapper#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    @Override
    public void handle(String target, HttpServletRequest request,
        HttpServletResponse response, int dispatch) throws IOException, ServletException {
      if (request.getUserPrincipal() == null) {
        Request baseRequest = (request instanceof Request) ? (Request) request
            : HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
        LOG.error("ESB HTTP listener refused a request without an authenticated user from "
            + request.getRemoteAddr());
        if (!response.isCommitted()) {
          response.setHeader("WWW-Authenticate", "basic realm=\"" + REALM_NAME + "\"");
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return;
      }
      super.handle(target, request, response, dispatch);
    }
  }

  /**
   * Method to start the Jetty server
   * @param jobDataMap
   */
  private void startServer(JobDataMap jobDataMap) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Initialising HTTP server");
    }
    int port = Integer.parseInt(jobDataMap.getString("port"));
    String bindAddress = jobDataMap.getString("bindAddress");
    String authConfigFile = jobDataMap.getString("authConfigFile");
    String keystore = jobDataMap.getString("keystore");

    // build (and validate) the authenticated handler chain first, so nothing listens on the
    // port when authentication is not configured
    Handler handler = buildHandler(authConfigFile);
    if (handler == null) {
      return;
    }

    Server server = new Server();
    if (keystore == null || keystore.equals("")) {
      LOG.info("Starting with HTTP (non-encrypted) protocol");
      SelectChannelConnector connector = new SelectChannelConnector();
      connector.setHost(bindAddress);
      connector.setPort(port);
      server.addConnector(connector);
    } else {
      LOG.info("Starting with HTTPS (encrypted) protocol");
      SslSocketConnector sslConnector = new SslSocketConnector();
      sslConnector.setHost(bindAddress);
      sslConnector.setPort(port);
      sslConnector.setKeystore(jobDataMap.getString("keystore"));
      sslConnector.setKeyPassword(jobDataMap.getString("keyPassword"));
      sslConnector.setTruststore(jobDataMap.getString("trustStore"));
      sslConnector.setTrustPassword(jobDataMap.getString("trustPassword"));
      sslConnector.setPassword(jobDataMap.getString("password"));
      server.addConnector(sslConnector);
    }

    server.setHandler(handler);
    try {
      server.start();
      LOG.info("HTTP server started on address " + bindAddress + " port " + port);
      server.join();
    } catch (Exception e) {
      LOG.error("ESB HTTP listener error", e);
    }

  }

  /**
   * Method called by quartz to start the server
   */
  public void execute(JobExecutionContext context)
      throws JobExecutionException {
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    this.startServer(jobDataMap);
  }
}
