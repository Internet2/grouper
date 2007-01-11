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

package edu.internet2.middleware.ldappcTest.dbBuilder;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.naming.directory.DirContext;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Group;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.StemDeleteException;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;

import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.LdappcRuntimeException;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;

import edu.internet2.middleware.ldappc.StemProcessor;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;

/**
 * Class for removing the database additions that
 * were added by the DatabaseBuilderTest class
 * 
 * @author Gil Singer 
 */
public class DatabaseCleaner
{
    /**
     * the grouper session
     */
    private GrouperSession grouperSession;

    /**
     * the stem processor
     */
    private StemProcessor stemProcessor;

    /**
     * LDAP context for provisioning
     */
    private LdapContext ldapContext;

    /**
     * Constructor
     */
    public DatabaseCleaner() 
    {
        // Build the LDAP context
        try
        {
            ldapContext = LdapUtil.getLdapContext();
        }
        catch(NamingException ne)
        {
            throw new LdappcRuntimeException(ne);
        }
        GrouperSessionControl grouperSessionControl = new GrouperSessionControl();
        boolean started = grouperSessionControl.startSession("GrouperSystem");
        if (!started)
        {
            ErrorLog.error(this.getClass(), "Database cleaner could not start grouper session");
        }
        grouperSession = grouperSessionControl.getSession();

        stemProcessor = new StemProcessor(grouperSession);
    }

    public void clean()
    {
        boolean ignoreNotFound = true;
        stemProcessor.deleteGroupByName("testStem1:testStem2:testStem3:testGroup31", ignoreNotFound);
        stemProcessor.deleteStemByName("testStem1:testStem2:testStem3", ignoreNotFound);
        stemProcessor.deleteGroupByName("testStem1:testStem2:testGroup21", ignoreNotFound);
        stemProcessor.deleteStemByName("testStem1:testStem2", ignoreNotFound);
        stemProcessor.deleteStemByName("testStem1", ignoreNotFound);
        stemProcessor.deleteGroupByName("topLevelStem1:topLevelGroup1", ignoreNotFound);
        stemProcessor.deleteStemByName("topLevelStem1", ignoreNotFound);
        
        // Can prune testgrouper only, not grouper, as it contains data not created by 
        // the test cases.  
     
        DirContext ctxToPrune = null;
        String dnTestRootOu = null;
        try
        {
            // e.g.: DN_TEST_ROOT_OU = "ou=testgrouper,dc=my-domain,dc=com";
            dnTestRootOu = ConfigManager.getInstance().getGroupDnRoot();
            ctxToPrune = (DirContext) ldapContext.lookup(dnTestRootOu);
            LdapUtil.prune(ctxToPrune);
        }
        catch(NamingException ne)
        {
            ErrorLog.error(this.getClass(), "Could not look up context or prune a group: "
                    + dnTestRootOu + "\n" +  ne.getMessage());
        }

        
        // Above does not seem to work for topLevelStem1; therefore using:
        // This does not work either:
  
        try
        {
            Stem stem1del = StemFinder.findByName(grouperSession, "topLevelStem1");
            stem1del.delete();            
        }
        catch(StemNotFoundException snfe)
        {
            if (!ignoreNotFound)
            {
                ErrorLog.error(this.getClass(), "Trying to delete topLevelStem1:" + snfe.getMessage());
            }
        }
        catch(InsufficientPrivilegeException snfe)
        {
            if (!ignoreNotFound)
            {
                ErrorLog.error(this.getClass(), "Trying to delete topLevelStem1:" + snfe.getMessage());
            }
        }
        catch(StemDeleteException snfe)
        {
            if (!ignoreNotFound)
            {
                ErrorLog.error(this.getClass(), "Trying to delete topLevelStem1:" + snfe.getMessage());
            }
        }
        

    }
    public static void main(String[] args) 
    {
        DebugLog.info(Ldappc.class, "Starting the Ldappc DatabaseCleaner Program");
        
        //
        // Do the database cleaning after all test cases that use the
        // data created by DatabaseBuilderTest.
        //
        
        DatabaseCleaner cleaner = new DatabaseCleaner();
        cleaner.clean();
    }    
}
