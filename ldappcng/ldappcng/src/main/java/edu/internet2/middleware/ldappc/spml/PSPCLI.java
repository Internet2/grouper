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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.request.BulkProvisioningRequest;

/**
 * Run the <code>ProvisioningServiceProvider</code> from the command line.
 */
public class PSPCLI extends TimerTask {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PSPCLI.class);

  /** The provisioning service provider. */
  private PSP psp;

  /** The timer for scheduling runs, Quartz would be nice too. */
  private Timer timer;

  /** Where output is written to. */
  private BufferedWriter writer;

  /** The lastModifyTime. */
  private Date lastModifyTime;

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
    } catch (IOException e) {
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
   * @throws IOException
   *           if output cannot be written
   */
  public PSPCLI(PSPOptions options) throws ResourceException, IOException {
    psp = PSP.getPSP(options);

    if (GrouperUtil.isBlank(psp.getPspOptions().getOutputFile())) {
      writer = new BufferedWriter(new OutputStreamWriter(System.out));
    } else {
      writer = new BufferedWriter(new FileWriter(psp.getPspOptions().getOutputFile(), true));
    }
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

      Date now = new Date();

      StopWatch sw = new StopWatch();
      sw.start();

      // initialize lastModifyTime if supplied as option
      if (lastModifyTime == null && psp.getPspOptions().getLastModifyTime() != null) {
        lastModifyTime = psp.getPspOptions().getLastModifyTime();
      }

      for (Request request : psp.getPspOptions().getRequests()) {
        // set updated since for bulk requests
        if (request instanceof BulkProvisioningRequest && lastModifyTime != null) {
          ((BulkProvisioningRequest) request).setUpdatedSince(lastModifyTime);
        }
        // print requests if so configured
        if (psp.getPspOptions().isPrintRequests()) {
          writer.write(psp.toXML(request));
        }
        // execute request
        Response response = psp.execute(request);
        // print response
        writer.write(psp.toXML(response));
      }

      writer.flush();

      sw.stop();
      LOG.info("End of {} execution : {} ms", PSPOptions.NAME, sw.getTime());

      // update last modified time
      lastModifyTime = now;

    } catch (IOException e) {
      LOG.error("Unable to write SPML.", e);
      timer.cancel();
    }
  }

  /**
   * Schedule this <code>TimerTask</code>.
   */
  public void schedule() {
    timer = new Timer();
    timer.schedule(this, 0, 1000 * psp.getPspOptions().getInterval());
  }

  /**
   * Return the {@link PSP}.
   * 
   * @return the {@link PSP}.
   */
  public PSP getPSP() {
    return psp;
  }

  /**
   * Get the timer
   * 
   * @return the {@link Timer}
   */
  public Timer getTimer() {
    return timer;
  }

}
