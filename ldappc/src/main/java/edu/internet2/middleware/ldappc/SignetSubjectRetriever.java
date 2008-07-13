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

import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import  edu.internet2.middleware.signet.Signet;
import  edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.subjsrc.SignetAppSource;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

import java.util.List;
import java.util.Vector;

/**
 * Class for finding subjects.
 * @author Gil Singer
 */
public class SignetSubjectRetriever 
{
    /**
     * Constructor
     */
    public SignetSubjectRetriever()
    {
    }
    
    /**
     * Gets all PrivilegedSubjects. Should probably be changed to return a
     * type-safe Collection.
     * 
     * @return a List of all of the PrivilegedSubjects accessible to
     *         Signet, including those who have no privileges. Never returns null:
     *         in the case of zero PrivilegedSubjects, this method will
     *         return an empty List.
     */
    public List getAllPrivilegedSubjects()
    {
        Signet signet = null;
        try
        {
            signet = new Signet();
        }
        catch (SignetRuntimeException sre)
        {
            LdappcConfigurationException ace = new LdappcConfigurationException(
                    "Failed to create Signet instance: Signet database may not be running.",
                    sre);
            sre.printStackTrace();
            throw(ace);
        }


        Vector<SignetSubject> privSubj = null;
        
        try
        {
            // There are 3 types of Subject Sources in Signet:
            // 1. The built-in Signet Super-Subject Source (SIGNET_SOURCE_ID, above)
            // 2. The Signet Persistent Source (Signet's DB can be used as a Subject Source!)
            // 3. All other Subject Sources
            privSubj = new Vector<SignetSubject>(signet.getPersistentDB().getSubjects());
        }
        catch (SignetRuntimeException sre)
        {
            ErrorLog.error(this.getClass(), sre.getMessage());
        }
        
        return privSubj;
    }
} 

