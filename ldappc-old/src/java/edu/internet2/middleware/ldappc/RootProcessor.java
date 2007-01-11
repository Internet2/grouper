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

import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

/**
 * Class for Checking that a root context exists, or if not, to create it.
 */

public class RootProcessor
{

    /**
     * The configuration manager
     */
    private ConfigManager configManager;

    /**
     * The directory context
     */
    private DirContext ctx; 

    public RootProcessor()
    {
        //
        // set up environment to access the server
        //

        configManager = ConfigManager.getInstance();
        Hashtable ldapParameters = configManager.getLdapContextParameters();
        try
        { 
            ctx = new InitialDirContext(ldapParameters);
        }
        catch(NamingException ne)
        { 
            ErrorLog.fatal(this.getClass(),"Could not create inital directory context -- naming exception: "
                    + ne.getMessage());
        }
    }
    
    /**
     * Method for Checking that a context exists
     * @param contextId a context DN such as "ou=grouper,dc=my-domain,dc=com".
     * @return true if the context exists
     */            
    public boolean contextExists(String contextId)
    {
        boolean exists = false;
        try 
        {
            ctx.lookup(contextId);
            exists = true;
        } 
        catch ( NamingException e ) 
        {
        }
        return exists;
    }
}


