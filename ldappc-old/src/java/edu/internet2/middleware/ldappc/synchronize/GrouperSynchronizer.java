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

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Date;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.ldappc.GrouperProvisionerConfiguration;
import edu.internet2.middleware.ldappc.GrouperProvisionerOptions;

/**
 * This defines the common functionality needed by all Grouper synchronizers.
 */
public abstract class GrouperSynchronizer extends Synchronizer
{
    /**
     * Indicates the group is new since the last modification date
     */
    public static final int STATUS_NEW = 0;

    /**
     * Indicates the group has been modified since the last modification date
     */
    public static final int STATUS_MODIFIED = 1;

    /**
     * Indicates the group has not been modified since the last modification
     * date
     */
    public static final int STATUS_UNCHANGED = 2;

    /**
     * Indicates a last modification date was not provided so the group's status
     * is unknown
     */
    public static final int STATUS_UNKNOWN = 3;

    /**
     * Grouper configuration for provisioning
     */
    private GrouperProvisionerConfiguration configuration;

    /**
     * Grouper options for provisioning
     */
    private GrouperProvisionerOptions options;
    
    /**
     * Constructs a <code>GroupSynchronizer</code>
     * 
     * @param context
     *            Ldap context to be used for synchronizing
     * @param configuration
     *            Grouper provisioning configuration
     * @param options
     *            Grouper provisioning options
     */
    public GrouperSynchronizer(LdapContext context, GrouperProvisionerConfiguration configuration, GrouperProvisionerOptions options )
    {
        super(context);
        setConfiguration(configuration);
        setOptions(options);
    }

    /**
     * Get the Grouper provisioner configuration
     * 
     * @return Grouper provisioner configuration
     */
    public GrouperProvisionerConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Set the Grouper provisioner configuration
     * 
     * @param configuration
     *            Grouper provisioner configuration
     */
    protected void setConfiguration(GrouperProvisionerConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Get the Grouper provisioner options
     * 
     * @return Grouper provisioner options
     */
    public GrouperProvisionerOptions getOptions()
    {
        return options;
    }

    /**
     * Set the Grouper provisioner options
     * 
     * @param options
     *            Grouper provisioner options
     */
    protected void setOptions(GrouperProvisionerOptions options)
    {
        this.options = options;
    }

    /**
     * Determines the status of the group based on the lastModifyTime provided
     * in the GrouperOptions.
     * 
     * @param group
     *            Group
     * @return Status of the group, either {@link #STATUS_NEW},
     *         {@link #STATUS_MODIFIED}, {@link #STATUS_UNCHANGED} or
     *         {@link #STATUS_UNKNOWN}.
     */
    protected int determineStatus(Group group)
    {
        //
        // Init variables
        //
        int status = STATUS_UNKNOWN;
        Date lastModifyTime = options.getLastModifyTime();

        //
        // If lastModifyTime provided, update status based on it
        //
        if (lastModifyTime != null)
        {
            //
            // Get the group create and modify time. Neither should be null, but
            // if either is leave status as UNKNOWN
            //
            Date groupCreateTime = group.getCreateTime();
            Date groupModifyTime = group.getModifyTime();
            
            if (groupCreateTime != null && groupModifyTime != null)
            {
                //
                // Set status to be new if permission created on or after the
                // lastModifyTime
                //
                if (lastModifyTime.compareTo(groupCreateTime) < 1)
                {
                    status = STATUS_NEW;
                }
                else
                {
                    if (lastModifyTime.compareTo(groupModifyTime) < 1)
                    {
                        status = STATUS_MODIFIED;
                    }
                    else
                    {
                        status = STATUS_UNCHANGED;
                    }
                }
            }
        }

        return status;
    }
}