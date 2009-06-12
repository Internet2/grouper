/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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

package edu.internet2.middleware.ldappc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.LdapUtil;

/**
 * Initiates provisioning.
 */
public final class Ldappc extends TimerTask {

  private static final Logger LOG = GrouperUtil.getLogger(Ldappc.class);

  private ProvisionerOptions options;

  private ProvisionerConfiguration configuration;

  private LdapContext ldapContext;

  private Provisioner provisioner;

  public Ldappc(ProvisionerOptions options) {
    this(options, null, null);
  }

  public Ldappc(ProvisionerOptions options, ProvisionerConfiguration configuration,
      LdapContext ldapContext) {

    this.options = options;

    if (configuration == null) {
      configuration = new ConfigManager(options.getConfigManagerLocation());
    }
    this.configuration = configuration;

    try {
      if (ldapContext == null) {
        ldapContext = LdapUtil.getLdapContext(configuration.getLdapContextParameters(),
            null);
      }
      this.ldapContext = ldapContext;
    } catch (NamingException e) {
      throw new LdappcException(e);
    }

    provisioner = new Provisioner(configuration, options, ldapContext);
  }

  public static void main(String[] args) {

    try {
      LOG.debug("Starting Ldappc with the following arguments: {}", Arrays.asList(args));

      ProvisionerOptions options = new ProvisionerOptions();

      try {
        if (args.length == 0) {
          options.printUsage();
          return;
        }
        options.init(args);
      } catch (ParseException e) {
        options.printUsage();
        System.err.println(e.getMessage());
        return;
      } catch (java.text.ParseException e) {
        options.printUsage();
        System.err.println(e.getMessage());
        return;
      }

      LOG.info("Starting Ldappc");

      Ldappc ldappc = new Ldappc(options);

      if (options.getInterval() == 0) {
        ldappc.run();
      } else {
        Timer timer = new Timer();
        timer.schedule(ldappc, 0, 1000 * options.getInterval());
      }

      LOG.info("End of Ldappc execution.");

    } catch (LdappcException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.TimerTask#run()
   */
  public void run() {

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LDAPPC, false, true);

    LOG.info("***** Starting Provisioning *****");

    Date now = new Date();

    try {

      if (LOG.isInfoEnabled()) {
        for (String source : configuration.getSourceSubjectHashEstimates().keySet()) {
          LOG.info("Estimate({}) = {}", source, configuration
              .getSourceSubjectHashEstimate(source));
        }
      }

      switch (options.getMode()) {

        case PROVISION:

          provisioner.provision();
          break;

        case CALCULATE:

          File file = provisioner.calculate();

          BufferedReader in = new BufferedReader(new FileReader(file));
          String str;
          while ((str = in.readLine()) != null) {
            System.out.println(str);
          }
          in.close();
          break;

        case DRYRUN:

          System.out.println("not yet implemented");
          break;
      }

      options.setLastModifyTime(now);

      if (LOG.isInfoEnabled()) {
        int subjectIDLookups = provisioner.getSubjectCache().getSubjectIdLookups();
        int subjectIDTableHits = provisioner.getSubjectCache().getSubjectIdTableHits();
        LOG.info("Subject ID Lookups: {}", subjectIDLookups);
        LOG.info("Subject Table Hits: {}", subjectIDTableHits);
        // Compute hit ratio percent, rounded to nearest tenth percent.
        double ratio = Math.round(((double) subjectIDTableHits) / subjectIDLookups
            * 1000.0) / 10.0;
        LOG.info("Subject hit ratio: {} %", ratio);
      }

    } catch (Exception e) {
      LOG.error("Grouper Provision Failed", e);
      cancel();
    } finally {
      try {
        if (!(options.isTest()) && ldapContext != null) {
          ldapContext.close();
        }
      } catch (NamingException e) {
        // May have already been closed.
      }
    }
  }
}
