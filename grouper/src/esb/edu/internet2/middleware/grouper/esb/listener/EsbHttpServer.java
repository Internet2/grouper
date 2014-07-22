/**
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
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
 *
 */

public class EsbHttpServer implements Job {

  private static final Log LOG = GrouperUtil.getLog(EsbHttpServer.class);

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

    if (authConfigFile != null && !(authConfigFile.equals(""))) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Requiring basic auth");
      }
      Constraint constraint = new Constraint();
      constraint.setName(Constraint.__BASIC_AUTH);
      ;
      constraint.setRoles(new String[] { "user", "grouper" });
      constraint.setAuthenticate(true);

      ConstraintMapping cm = new ConstraintMapping();
      cm.setConstraint(constraint);
      cm.setPathSpec("/*");

      SecurityHandler sh = new SecurityHandler();
      try {
        sh.setUserRealm(new HashUserRealm("Grouper", authConfigFile));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      sh.setConstraintMappings(new ConstraintMapping[] { cm });

      Handler[] handlers = new Handler[] { sh, new EsbHttpHandler() };
      server.setHandlers(handlers);

    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Not requiring basic auth");
      }
      server.setHandler(new EsbHttpHandler());
    }
    try {
      server.start();
      LOG.info("HTTP server started on address " + bindAddress + " port " + port);
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Method called by quartz to start the server
   */
  public void execute(JobExecutionContext context)
      throws JobExecutionException {
    // TODO Auto-generated method stub
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    this.startServer(jobDataMap);
  }
}
