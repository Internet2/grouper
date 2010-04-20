/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Run the <code>ProvisioningServiceProvider</code> from the command line.
 */
public class PSPCLI extends TimerTask {

  /** Logger. */
  private static final Logger LOG = GrouperUtil.getLogger(PSPCLI.class);

  /** The provisioning service provider. */
  private PSP psp;

  /**
   * Run this application.
   * 
   * @param args
   *          the command line arguments
   */
  public static void main(String[] args) {

    PSPOptions options = new PSPOptions(args);

    try {
      if (args.length == 0) {
        options.printUsage();
        return;
      }

      options.parseCommandLineOptions();

      PSPCLI pspCLI = new PSPCLI(options);

      if (options.getInterval() == 0) {
        pspCLI.run();
      } else {
        pspCLI.schedule();
      }

    } catch (ParseException e) {
      options.printUsage();
      System.err.println(e.getMessage());
      e.printStackTrace();
    } catch (ResourceException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Constructor. Load the <code>PSP</code> based on the given <code>PSPOptions</code>.
   * 
   * @param options
   *          the <code>PSPOptions</code>
   * @throws ResourceException
   *           if the <code>PSP</code> could not be instantiated
   */
  public PSPCLI(PSPOptions options) throws ResourceException {
    psp = PSP.getPSP(options);
  }

  /**
   * Print SPML <code>Response</code>s for every SPML <code>Request</code>.
   * 
   * {@inheritDoc}
   */
  public void run() {
    try {
      LOG.info("Starting {}", PSPOptions.NAME);
      LOG.debug("Starting {} with options {}", PSPOptions.NAME, psp.getPspOptions());

      StopWatch sw = new StopWatch();
      sw.start();

      for (Request request : psp.getPspOptions().getRequests()) {
        if (psp.getPspOptions().isPrintRequests()) {
          psp.getPspOptions().getPrintStream().print(psp.toXML(request));
        }
        Response response = psp.execute(request);
        psp.getPspOptions().getPrintStream().print(psp.toXML(response));
      }
      psp.getPspOptions().getPrintStream().close();

      sw.stop();
      LOG.info("End of {} execution : {} ms", PSPOptions.NAME, sw.getTime());

    } catch (IOException e) {
      LOG.error("Unable to write SPML.", e);
      cancel();
    }
  }

  /**
   * Schedule this <code>TimerTask</code>.
   */
  public void schedule() {
    Timer timer = new Timer();
    timer.schedule(this, 0, 1000 * psp.getPspOptions().getInterval());
  }

}
