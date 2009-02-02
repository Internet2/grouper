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

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.LdappcException;
import edu.internet2.middleware.ldappc.SignetProvisionerConfiguration;
import edu.internet2.middleware.ldappc.SignetProvisionerOptions;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This defines the common functionality required by all permission
 * synchronizers.
 */
public abstract class PermissionSynchronizer extends SignetSynchronizer
{
    /**
     * DN of the subject whose permissions are being synchronized.
     */
    private Name subject;

    /**
     * Signet permissions function queries.
     */
    private Set  functionQueries;

    /**
     * Signet permissions subsystem queries.
     */
    private Set  subsystemQueries;

    /**
     * Constructs a <code>PermissionSynchronizer</code>.
     * 
     * @param ctx
     *            Ldap context to be used for synchronizing
     * @param subject
     *            DN of the subject whose permissions are being synchronized
     * @param configuration
     *            Signet provisioning configuration
     * @param options
     *            Signet provisioning options
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     */
    public PermissionSynchronizer(LdapContext ctx, Name subject, SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options, SubjectCache subjectCache)
    {
        super(ctx, configuration, options, subjectCache);
        setSubject(subject);

        //
        // Initialize items used only internally
        //
        functionQueries = configuration.getPermissionsFunctionQueries();
        subsystemQueries = configuration.getPermissionsSubsystemQueries();
    }

    /**
//     * Get the DN of the subject.
     * 
     * @return DN of the subject
     */
    public Name getSubject()
    {
        return subject;
    }

    /**
     * Set the DN of the subject.
     * 
     * @param subject
     *            DN of the subject
     */
    protected void setSubject(Name subject)
    {
        this.subject = subject;
    }

    /**
     * Perform any initialization prior to processing the set of permissions.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected abstract void initialize() throws NamingException, LdappcException;

    /**
     * Commits any outstanding changes to the directory. This is called by
     * {@link #synchronize(Set)} after processing all of the privileges.
     * 
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected abstract void commit() throws NamingException, LdappcException;
}
