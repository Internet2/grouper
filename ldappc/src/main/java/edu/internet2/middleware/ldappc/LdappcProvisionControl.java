/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package edu.internet2.middleware.ldappc;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimerTask;

import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * Controls provisioning for the Ldappc application. It determines if Grouper
 * and/or Signet data is to be provisioned and passes the command line input
 * options on to the classes that do the provisioning. The classes that are
 * called will handle obtaining the corresponding type of configuration data.
 */
public class LdappcProvisionControl extends TimerTask
{
    /**
     * Configuration data from configuration file.
     */
    private ProvisionerConfiguration configuration;

    /**
     * The command line input arguments that determine what data is to be
     * provisioned.
     */
    private InputOptions             options;

    /**
     * Subject cache to eliminate extra LDAP lookups.
     */
    private SubjectCache             subjectCache = new SubjectCache();

    /**
     * Constuctor.
     * 
     * @param options
     *            The command line input arguments that determine what data is
     *            to be provisioned.
     */
    public LdappcProvisionControl(InputOptions options)
    {
        this.options = options;

        // Get the GrouperProvisionerConfiguration

        if (options.getConfigManagerLocation() != null)
        {
            ConfigManager.loadSingleton(options.getConfigManagerLocation());
        }
        configuration = ConfigManager.getInstance();

        for (String source : configuration.getSourceSubjectHashEstimates().keySet())
        {
            DebugLog.info("Estimate(" + source + ") = " + configuration.getSourceSubjectHashEstimate(source));
        }
    }

    /**
     * Being the provisioning process. Determine what is to be provisioned base
     * on the input options and call Grouper-specific and/or Signet-specific
     * methods for provisioning.
     */
    public void run()
    {
        DebugLog.info(this.getClass(), "***** Starting Provisioning *****");
        Date now = (new GregorianCalendar()).getTime();

        subjectCache.init(configuration);

        //
        // Provision Grouper information if requested
        //
        if (options.getDoGroups() || options.getDoMemberships())
        {
            LdappcGrouperProvisioner provisioner = new LdappcGrouperProvisioner(options, subjectCache);
            provisioner.provisionGroups();
        }

        //
        // Provision Signet information if requested
        //
        if (options.getDoPermissions())
        {
            LdappcSignetProvisioner provisioner = new LdappcSignetProvisioner(options, subjectCache);
            provisioner.provisionPermissions();
        }

        int subjectIDLookups = subjectCache.getSubjectIdLookups();
        int subjectIDTableHits = subjectCache.getSubjectIdTableHits();

        DebugLog.info("Subject ID Lookups: " + subjectIDLookups);
        DebugLog.info("Subject Table Hits: " + subjectIDTableHits);
        // Compute hit ratio percent, rounded to nearest tenth percent.
        double ratio = Math.round(((double) subjectIDTableHits) / subjectIDLookups * 1000.0) / 10.0;
        DebugLog.info("Subject hit ratio:  " + ratio + "%");

        //
        // Cancel if a fatal error has occurred.
        //
        if (ErrorLog.getFatalOccurred())
        {
            System.out.println("A FATAL ERROR occurred when running Ldappc.  "
                    + "\nCheck error log file, correct the problem, and resubmit.");
            cancel();
        }
        else
        {
            // Reset the lastModifyTime
            options.setLastModifyTime(now);
        }

    }
}
