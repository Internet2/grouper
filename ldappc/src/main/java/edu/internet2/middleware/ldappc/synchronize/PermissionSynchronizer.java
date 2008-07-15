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
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.PrivilegeImpl;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;

/**
 * This defines the common functionality required by all permission
 * synchronizers.
 */
public abstract class PermissionSynchronizer extends SignetSynchronizer
{
    /**
     * DN of the subject whose permissions are being synchronized
     */
    private Name subject;

    /**
     * Signet permissions function queries
     */
    private Set functionQueries;

    /**
     * Signet permissions subsystem queries
     */
    private Set subsystemQueries;

    /**
     * Constructs a <code>PermissionSynchronizer</code>
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
    public PermissionSynchronizer(LdapContext ctx, Name subject,
            SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options,
            SubjectCache subjectCache)
    {
        super(ctx,configuration,options, subjectCache);
        setSubject(subject);

        //
        // Initialize items used only internally
        //
        functionQueries = configuration.getPermissionsFunctionQueries();
        subsystemQueries = configuration.getPermissionsSubsystemQueries();
    }

    /**
     * Get the DN of the subject
     * 
     * @return DN of the subject
     */
    public Name getSubject()
    {
        return subject;
    }

    /**
     * Set the DN of the subject
     * 
     * @param subject
     *            DN of the subject
     */
    protected void setSubject(Name subject)
    {
        this.subject = subject;
    }

    /**
     * Determines if <code>permission</code> is included for processing. The
     * <code>permission</code> must be active, and satisfy at least one of
     * either the function or subsystem queries defined by the Signet
     * configuration. If no queries are defined, then all active permissions are
     * included.
     * 
     * @param permission
     *            Permission
     * @return <code>true</code> if the permission is to be included, and
     *         <code>false</code> otherwise.
     */
    protected boolean isIncluded(Permission permission, Function function)
    {
        //
        // Init the return value
        //
        boolean isIncluded = false;

        //
        // Get the permissions status.
        // IF NOT RETURNED ASSUMED IT IS ACTIVE
        //
        Status status = permission.getStatus();
        if (status == null)
        {
            status = Status.ACTIVE;
        }

        //
        // Make sure the permission is active
        //
        if (Status.ACTIVE.equals(status))
        {
            //
            // If no queries are defined, then the permission is included.
            // Else make sure at least one querie is satisfied
            //
            if (subsystemQueries.size() == 0 && functionQueries.size() == 0)
            {
                isIncluded = true;
            }
            else
            {
                //
                // See if a subsystem query is satisfied
                //
                Subsystem subsystem = permission.getSubsystem();
                if (subsystemQueries.contains(subsystem.getId()))
                {
                    isIncluded = true;
                }

                //
                // If not already included, see if at least one function query
                // is satisfied
                //
                if (!isIncluded)
                {
                    //
                    // See if the permission function is listed
                    // in the function queries
                    if (functionQueries.contains(function.getId()))
                    {
                        isIncluded = true;
                    }
                }
            }
        }

        return isIncluded;
    }

    /**
     * This identifies the underlying permission as one must be included in the
     * subject's entry. The permission is processed based on its status. The
     * privilege is necessary to provide data necessary to identify the
     * privilege in the directory.
     * 
     * @param privilege
     *            Privilege holding the permission to be included
     * @param status
     *            Either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
     *            {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected abstract void performInclude(Privilege privilege, int status)
            throws NamingException, LdappcException;

    /**
     * Perform any initialization prior to processing the set of permissions.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected abstract void initialize() throws NamingException,
            LdappcException;

    /**
     * Synchronizes the privileges underlying permissions with those in the
     * directory. The privileges are necessary to provide the data needed to
     * identify the permission in the directory.
     * 
     * @param privileges
     *            Set of privileges
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    public void synchronize(Set<AssignmentImpl> assignments) throws NamingException, LdappcException
    {
        //
        // Initialize the process
        //
        initialize();

        //
        // Get the set of privileges and iterate over them
        //
        for (Assignment assignment : assignments) {
            for (Privilege privilege : (Set<Privilege>) PrivilegeImpl.getPrivileges(assignment)) {
                //
                // Get the permission
                //
                Permission permission = privilege.getPermission();

                //
                // If the permission is included, process it indicating whether or
                // not it should be provisioned
                //
                if (isIncluded(permission, assignment.getFunction()))
                {
                    performInclude(privilege, determineStatus(permission));
                }
            }
        }

        //
        // Commit the modifications to the directory
        //
        commit();
    }

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