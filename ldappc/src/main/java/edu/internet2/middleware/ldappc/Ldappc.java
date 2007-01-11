/*
    Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
    Copyright 2004-2006 The University Of Chicago
  
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

import java.util.Timer;

/**
 * Class for starting the Ldappc program.
 * For the design of this program, 
 * @see edu.internet2.middleware.ldappc.documentation.Design
 * @author Gil Singer 
 */
public class Ldappc {

    /**
     * The version of Ldappc
     */ 
    public static final String VERSION_NUMBER= "1.0";

    /**
     * The date this version of Ldappc was created.
     */ 
    public static final String VERSION_DATE = "2007-01-11";


    /**
     * Main program for transferring Grouper and Signet repository information into an LDAP directory.
     *
     * @param args May be any of the following in any order, where (Opt) implies optional;
     * however, when a key of -xxx is followed by a name, a value must be present following the key. 
     * <no arguments>                  Display the following list of available arguments to standard output. 
     * -subject        subjectId       The SubjectId is used to establish Grouper API and Signet API sessions
     * -groups                         (Opt) When present, group information will be provisioned
     * -memberships                    (Opt) When present, membership information will be provisioned
     * -permissions                    (Opt) When present, permissions information will be provisioned
     * -lastModifyTime lastModifyTime  (Opt) DateTime representation to select only objects changed since then
     * -interval       interval        (Opt) Number of seconds between polling intervals
     */
    public static void main(String[] args) 
    {
        DebugLog.info(Ldappc.class, "Starting the Ldappc Program");

        // 
        // Process the user arguments.
        //

        InputOptions options  = new InputOptions(args);

        long intervalInMsec =1000*options.getInterval();

        //
        // If an error occurred, log it before terminating.
        //

        if (options.isFatal())
        {
            String msg = "A fatal error occurred in Ldappc -- see the error log file";
            System.out.println(msg);
            DebugLog.info(Ldappc.class, msg);
            ErrorLog.warn(Ldappc.class, "A fatal error occurred in Ldappc while processing input options; "
                    + "check earlier messages.");
        }     
        else
        {
            //
            // Create provision control
            //
            LdappcProvisionControl pc = new LdappcProvisionControl(options);

            //
            // 
            //
            
            if (intervalInMsec == 0)
            {
                pc.run();
            }
            else
            {
                Timer provisionerTimer= new Timer();
                // Start immediately (2nd arg is 0)
                // Rerun every intervalInMsec milliseconds
                provisionerTimer.schedule(pc, 0, intervalInMsec); 
            }
        }

        DebugLog.info(Ldappc.class, "End of Ldappc execution.");
    }
    
}
