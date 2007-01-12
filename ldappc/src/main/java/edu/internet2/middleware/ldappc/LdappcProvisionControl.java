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

import edu.internet2.middleware.ldappc.InputOptions;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

import java.util.TimerTask;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Controls provisioning for the Ldappc application.
 * It determines if Grouper and/or Signet data is to be provisioned
 * and passes the command line input options on to the classes
 * that do the provisioning.  The classes that are called will
 * handle obtaining the corresponding type of configuration data.
 */
public class LdappcProvisionControl extends TimerTask
{
    /**
     * The command line input arguments that determine what data
     * is to be provisioned.
     */
    private InputOptions options;

    /**
     * Constuctor
     * @param options  The command line input arguments that determine what data
     * is to be provisioned.
     */
    public LdappcProvisionControl(InputOptions options)
    {
        this.options = options;
    }

    /**
     * Being the provisioning process.  Determine what is to be 
     * provisioned base on the input options and call Grouper-specific
     * and/or Signet-specific methods for provisioning. 
     */
    public void run()
    {
        DebugLog.info(this.getClass(), "***** Starting Provisioning *****");
        Date now = (new GregorianCalendar()).getTime();
        //
        // Provision Grouper information if requested
        //
        if ( options.getDoGroups() || options.getDoMemberships() )
        {
            LdappcGrouperProvisioner a2lgp = new LdappcGrouperProvisioner(options);
             a2lgp.provisionGroups();
        }
        //
        // Provision Signet information if requested
        //
        if ( options.getDoPermissions() )
        {
            LdappcSignetProvisioner a2lsp = new LdappcSignetProvisioner(options);
            a2lsp.provisionPermissions();
        }
        
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

