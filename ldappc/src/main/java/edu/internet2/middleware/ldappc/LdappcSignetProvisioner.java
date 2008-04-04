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


import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.logging.ExceptionHandler;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This class uses two types of data for determining what data is to be
 * provisioned for Permissions to the LDAP database: one for determining the
 * general categories of data to be provisioned (eg. groups, memberships, or
 * permissions) and another to determine the specific details of how the data is
 * to be provisioned (e.g., according to some query criteria).
 * <p>
 * The former class uses a class that implements the SignetProvisionerOptions
 * interface, which will be the InputOptions class when accessing the API via
 * the Ldappc program. For other programs, used instead of Ldappc, that use the
 * API, another implementation of the SignetProvisionerOptions can be
 * substituted for the InputOptions class.
 * </p>
 * <p>
 * The latter class uses a class that implements the
 * SignetProvisionerConfiguration interface, which will be the ConfigManager
 * class when accessing the API via the Ldappc program. For other programs, used
 * instead of Ldappc, that use the API, another implementation of the
 * SignetProvisionerConfiguration interface can be substituted for the
 * ConfigManager class.
 * </p>
 */
public class LdappcSignetProvisioner
{
    /**
     * The directory context
     */

    private LdapContext                    ldapContext;

    /**
     * The command line input arguments that determine what data is to be
     * provisioned.
     */
    private SignetProvisionerOptions       options;

    /**
     * The grouper provisioner configuration
     */
    private SignetProvisionerConfiguration configuration;

    private SubjectCache                   subjectCache;

    /**
     * Constructor
     * 
     * @param options
     *            input that determines what data is to be provisioned.
     * @param subjectCache TODO
     */
    public LdappcSignetProvisioner(SignetProvisionerOptions options,
            SubjectCache subjectCache)
    {
        this.options = options;

        // Get the SignetProvisionerConfiguration

        if (options.getConfigManagerLocation() != null)
        {
            ConfigManager.loadSingleton(options.getConfigManagerLocation());
        }
        configuration = ConfigManager.getInstance();

        // Build the LDAP context
        try
        {
            ldapContext = LdapUtil.getLdapContext();
        }
        catch (NamingException ne)
        {
            throw new LdappcRuntimeException(ne);
        }
        
        this.subjectCache = subjectCache;
    }

    /**
     * Perform provisioning of permissions. Provisions permissions based on the
     * options and the configuration
     */
    public void provisionPermissions()
    {
        //
        // Build a grouper provisioner and set the values
        //
        SignetProvisioner signetProvisioner = new SignetProvisioner(
                configuration, options, ldapContext, subjectCache);

        //
        // Provision permissions.
        //

        // TODO: Handle exceptions properly:
        // Need to handle QueryException and SchemaException.
        try
        {
            signetProvisioner.provision();
        }
        catch (LdappcConfigurationException ace)
        {
            ExceptionHandler.logExceptionMessages(ace, ExceptionHandler.FATAL,
                    false);
            ErrorLog.fatal(this.getClass(),
                    "Signet Provision Failed with configuration exception:\n"
                            + ace.getMessage());
        }
        catch (LdappcRuntimeException are)
        {
            ExceptionHandler.logExceptionMessages(are, ExceptionHandler.FATAL,
                    false);
            ErrorLog.fatal(this.getClass(),
                    "Signet Provision Failed with ldappc runtime exception:\n"
                            + are.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ExceptionHandler.logExceptionMessages(e, ExceptionHandler.FATAL,
                    false);
            ErrorLog.fatal(this.getClass(), "Signet Provision Failed: "
                    + e.getMessage());
        }
        finally
        {
            try
            {
                if (null != ldapContext) ldapContext.close();
            }
            catch (Exception e2)
            {
            }
        }
    }
}
