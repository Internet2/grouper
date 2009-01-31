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

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * <p>
 * This class uses two types of data for determining what data is to be
 * provisioned for Groups to the LDAP database: one for determining the general
 * categories of data to be provisioned (eg. groups, memberships, or
 * permissions) and another to determine the specific details of how the data is
 * to be provisioned (e.g., according to some query criteria).
 * <p>
 * The former class uses a class that implements the GrouperProvisionerOptions
 * interface, which will be the InputOptions class when accessing the API via
 * the Ldappc program. For other programs, used instead of Ldappc, that use the
 * API, another implementation of the GrouperProvisionerOptions can be
 * substituted for the InputOptions class.
 * </p>
 * <p>
 * The latter class uses a class that implements the
 * GrouperProvisionerConfiguration interface, which will be the ConfigManager
 * class when accessing the API via the Ldappc program. For other programs, used
 * instead of Ldappc, that use the API, another implementation of the
 * GrouperProvisionerConfiguration interface can be substituted for the
 * ConfigManager class.
 * </p>
 */
class LdappcGrouperProvisioner
{
    /**
     * The directory context.
     */
    private LdapContext                     ldapContext;

    /**
     * The command line input arguments that determine what data is to be
     * provisioned.
     */
    private GrouperProvisionerOptions       options;

    /**
     * The grouper provisioner configuration.
     */
    private GrouperProvisionerConfiguration configuration;

    /**
     * Cache of already found subjects.
     */
    private SubjectCache                    subjectCache;

    /**
     * Constructor.
     * 
     * @param options
     *            input that determines what data is to be provisioned.
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     */
    public LdappcGrouperProvisioner(GrouperProvisionerOptions options, SubjectCache subjectCache)
    {
        this.options = options;
        this.configuration = ConfigManager.getInstance();
        this.subjectCache = subjectCache;

        // Build the LDAP context
        try
        {
            ldapContext = LdapUtil.getLdapContext(configuration.getLdapContextParameters(), null);
        }
        catch (NamingException ne)
        {
            throw new LdappcRuntimeException(ne);
        }
    }

    /**
     * Perform provisioning of groups. Provisions groups based on the options
     * and the configuration
     */
    public void provisionGroups()
    {
        //
        // Build a grouper provisioner and set the values
        //
        GrouperProvisioner grouperProvisioner = new GrouperProvisioner(configuration, options, ldapContext,
                subjectCache);
        //
        // Provision groups.
        //

        // One could handle this exception more explictly by catching
        // QueryException and SchemaException.
        try
        {
            grouperProvisioner.provision();
        }
        catch (NameNotFoundException nnfe)
        {
            ErrorLog.fatal(this.getClass(), "Grouper Provision Failed: " + nnfe.getMessage() + "  Exception data: "
                    + nnfe.toString());
        }
        catch (Exception e)
        {
            ErrorLog.fatal(this.getClass(), "Grouper Provision Failed: " + e.getMessage());
        }
        finally
        {
            try
            {
                if (null != ldapContext)
                {
                    ldapContext.close();
                }
            }
            catch (NamingException e)
            {
                // May have already been closed.
            }
        }
    }
}
