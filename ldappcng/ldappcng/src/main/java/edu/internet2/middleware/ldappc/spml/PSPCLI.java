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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;

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

  /** The number of provisioning iterations performed. */
  private int iterations = 0;

  /**
   * Run this application.
   * 
   * @param args the command line arguments
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
   * @param options the <code>PSPOptions</code>
   * @throws ResourceException if the <code>PSP</code> could not be instantiated
   * @throws IOException if output cannot be written
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

      StopWatch sw = new StopWatch();
      sw.start();

      for (Request request : psp.getPspOptions().getRequests()) {
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

      // cancel if the number of desired iterations have been run
      if (psp.getPspOptions().getIterations() > 0 && iterations++ >= psp.getPspOptions().getIterations()) {
        LOG.info("Finish {} execution : {} provisioning cycles performed.", PSPOptions.NAME, iterations);
        timer.cancel();
      }

      // log cache statistics
      if (LOG.isDebugEnabled()) {
        for (String stats : PSPCLI.getAllCacheStats()) {
          LOG.debug(stats);
        }
      }

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

  /**
   * Return ehcache statistics.
   * <ul>
   * <li>cache hit ratio 0% 0 hits 20 miss : ImmediateMembershipEntry
   * <li>cache hit ratio 0% 0 hits 45 miss : edu.internet2.middleware.grouper.Field
   * <ul>
   * 
   * @return the statistics
   */
  public static List<String> getAllCacheStats() {

    Map<String, String> name2stats = new TreeMap<String, String>();

    // sort cache managers by name
    List<CacheManager> cacheManagers = new ArrayList<CacheManager>(CacheManager.ALL_CACHE_MANAGERS);
    for (CacheManager cacheManager : cacheManagers) {
      for (String cacheName : cacheManager.getCacheNames()) {
        Statistics stats = cacheManager.getCache(cacheName).getStatistics();
        long h = stats.getCacheHits();
        long m = stats.getCacheMisses();

        if (h + m != 0) {
          String ratio = h + m == 0 ? "0%" : MessageFormat.format("{0,number,percent}", 1. * h / (h + m));
          String out = String.format("cache hit ratio %4s %6d hits %6d miss : %s", ratio, h, m, cacheName);
          // TODO probably should not assume cache names are unique
          name2stats.put(cacheName, out);
        }
      }
    }

    List<String> out = new ArrayList<String>();
    for (String cacheName : name2stats.keySet()) {
      out.add(name2stats.get(cacheName));
    }
    return out;
  }

}
