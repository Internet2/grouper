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

import java.util.Date;

import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.SignetProvisionerConfiguration;
import edu.internet2.middleware.ldappc.SignetProvisionerOptions;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This defines the common functionality required by all Signet
 * synchronizers.
 */
public abstract class SignetSynchronizer extends Synchronizer
{
    /**
     * Indicates the permission is new since the last modification date.
     */
    public static final int STATUS_NEW = 0;

    /**
     * Indicates the permission has been modified since the last modification
     * date.
     */
    public static final int STATUS_MODIFIED = 1;

    /**
     * Indicates the permission has not been modified since the last
     * modification date.
     */
    public static final int STATUS_UNCHANGED = 2;

    /**
     * Indicates a last modification date was not provided so the permission's
     * status is unknown.
     */
    public static final int STATUS_UNKNOWN = 3;

    /**
     * Signet configuration for provisioning.
     */
    private SignetProvisionerConfiguration configuration;

    /**
     * Signet options for provisioning.
     */
    private SignetProvisionerOptions options;

    /**
     * Constructs a <code>PermissionSynchronizer</code>.
     * 
     * @param ctx
     *            Ldap context to be used for synchronizing
     * @param configuration
     *            Signet provisioning configuration
     * @param options
     *            Signet provisioning options
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     */
    public SignetSynchronizer(LdapContext ctx,
            SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options,
            SubjectCache subjectCache)
    {
        super(ctx, subjectCache);
        setConfiguration(configuration);
        setOptions(options);
    }

    /**
     * Get the Signet provisioner configuration.
     * 
     * @return Signet provisioner configuration
     */
    public SignetProvisionerConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Set the Signet provisioner configuration.
     * 
     * @param configuration
     *            Signet provisioner configuration
     */
    protected void setConfiguration(SignetProvisionerConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Get the Signet provisioner options.
     * 
     * @return Signet provisioner options
     */
    public SignetProvisionerOptions getOptions()
    {
        return options;
    }

    /**
     * Set the Signet provisioner options.
     * 
     * @param options
     *            Signet provisioner options
     */
    protected void setOptions(SignetProvisionerOptions options)
    {
        this.options = options;
    }
}
