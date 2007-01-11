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

import java.util.Iterator;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingEnumeration;

import javax.naming.NamingException;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.LdappcException;
import edu.internet2.middleware.ldappc.SignetProvisionerConfiguration;
import edu.internet2.middleware.ldappc.SignetProvisionerOptions;

import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Privilege;

/**
 * This synchronizes permissions stored in the directory as strings in an
 * attribute.
 */
public class StringPermissionSynchronizer extends PermissionSynchronizer
{
    /**
     * Delimiter to use in the string representation of the permission
     */
    public static final String DELIMITER = ":";

    /**
     * Holds the permission listing attribute modifications
     */
    private AttributeModifier permissionMods;

    /**
     * Holds the objectClass modifications
     */
    private AttributeModifier objectClassMods;

    /**
     * Constructs a <code>StringPermissionSynchronizer</code>
     * 
     * @param ctx
     *            Ldap context to use for provisioning
     * @param subject
     *            DN of the subject whose permissions are being provisioned
     * @param configuration
     *            Signet provisioning configuration
     * @param options
     *            Signet provisioning options
     */
    public StringPermissionSynchronizer(LdapContext ctx, Name subject,
            SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options) throws NamingException,
            LdappcConfigurationException
    {
        //
        // Call super constructor
        //
        super(ctx, subject, configuration, options);

        //
        // Try to get the permissions listing string attribute name as it is
        // needed to initialize instance variables
        //
        String listAttrName = configuration
                .getPermissionsListingStringAttribute();
        if (listAttrName == null)
        {
            throw new LdappcConfigurationException(
                    "The name of the attribute to store permission strings is null.");
        }

        //
        // Build the permission string "empty value" value. It must be prefixed
        // so it will be deleted if provisioned permissions are added
        //
        String emptyValue = configuration
                .getPermissionsListingStringEmptyValue();
        if (emptyValue != null)
        {
            emptyValue = configuration.getPermissionsListingStringPrefix()
                    + DELIMITER
                    + configuration.getPermissionsListingStringEmptyValue();
        }

        //
        // Initialize the instance attributes
        //
        objectClassMods = new AttributeModifier(LdapUtil.OBJECT_CLASS_ATTRIBUTE);
        permissionMods = new AttributeModifier(listAttrName, emptyValue);
    }

    /**
     * This identifies the underlying permission as one that must remain or, if
     * need be, must be added to the subject's LDAP entry. If the permission has
     * already been provisioned to the entry, it will remain within the
     * subject's LDAP entry.The privilege is necessary to provide data necessary
     * to identify the privilege in the directory.
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
     * @see edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer#performInclude(Privilege,
     *      int)
     */
    protected void performInclude(Privilege privilege, int status)
            throws NamingException, LdappcException
    {
        //
        // Get the associated permission
        //
        Permission permission = privilege.getPermission();

        //
        // Build the common prefix for all values
        // <prefix>:<SubsystemId>:<PermissionId>:<ScopeId> of the total string
        // <prefix>:<SubsystemId>:<PermissionId>:<ScopeId>:<LimitId>:<Limit>
        //
        String commonPrefix = getConfiguration()
                .getPermissionsListingStringPrefix()
                + DELIMITER
                + permission.getSubsystem().getId()
                + DELIMITER
                + permission.getId() + DELIMITER + privilege.getScope().getId();

        //
        // Get the limit values and iterate over those
        //
        Set limitValues = privilege.getLimitValues();
        Iterator limitValsIterator = limitValues.iterator();
        while(limitValsIterator.hasNext())
        {
            //
            // Get the next value
            //
            LimitValue limitValue = (LimitValue) limitValsIterator.next();

            //
            // Build the string
            //
            String permStr = commonPrefix + DELIMITER
                    + limitValue.getLimit().getId() + DELIMITER
                    + limitValue.getValue();

            //
            // Store the permission string in the attribute modifier
            // (status doesn't improve things here so ignore it)
            //
            permissionMods.store(permStr);
        }
    }

    /**
     * Perform any initialization prior to processing the set of permissions.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     * 
     * @see edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer#initialize()
     */
    protected void initialize() throws NamingException, LdappcException
    {
        //
        // Clear any existing values
        //
        permissionMods.clear();
        objectClassMods.clear();

        //
        // Populate with current values from the subject entry
        //
        populate();
    }

    /**
     * This populates this object with the current values from the subject's
     * LDAP entry.
     * 
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     */
    protected void populate() throws NamingException
    {
        //
        // Get the existing values
        //
        Attributes attributes = getContext().getAttributes(
                getSubject(),
                new String[] { permissionMods.getAttributeName(),
                        objectClassMods.getAttributeName() });

        //
        // Initialize the permission listing attribute modifier
        //
        Attribute attribute = attributes.get(permissionMods.getAttributeName());
        permissionMods.init(attribute);

        //
        // Keep (i.e., store) all values, if any, that don't start with the
        // prefix
        // followed by the the delimiter
        //
        if (attribute != null)
        {
            //
            // Need to make comparison case insensitive so make prefix lowercase
            //
            String prefix = getConfiguration()
                    .getPermissionsListingStringPrefix()
                    + DELIMITER;
            prefix = prefix.toLowerCase();

            //
            // Compare prefix with the existing values
            //
            NamingEnumeration values = attribute.getAll();
            while(values.hasMore())
            {
                String value = (String) values.next();
                if (value != null && !(value.toLowerCase().startsWith(prefix)))
                {
                    permissionMods.store(value);
                }
            }
        }

        //
        // Populate the objectClass modifier if needed
        //
        objectClassMods.init(null);
        String stringObjectClass = getConfiguration()
                .getPermissionsListingStringObjectClass();
        if (stringObjectClass != null)
        {
            attribute = attributes.get(objectClassMods.getAttributeName());
            objectClassMods.init(attribute);
            objectClassMods.retainAll();
            objectClassMods.store(stringObjectClass);
        }
    }

    /**
     * This commits any changes not already committed to the directory.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     * 
     * @see edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer#commit()
     */
    protected void commit() throws NamingException, LdappcException
    {
        //
        // Determine how many modifications are to be performed
        //
        ModificationItem[] permissionModItems = permissionMods
                .getModifications();
        ModificationItem[] objectClassModItems = objectClassMods
                .getModifications();
        int modCnt = permissionModItems.length + objectClassModItems.length;

        //
        // Perform modifications if needed
        //
        if (modCnt > 0)
        {
            //
            // Build the array for the modification items
            //
            ModificationItem[] mods = new ModificationItem[modCnt];

            //
            // Add the modifications in the following order
            // 1. Add the object class modifications
            // 2. Add the permission listing attribute modifications
            //
            int modIndex = 0;

            for(int i = 0; i < objectClassModItems.length; i++)
            {
                mods[modIndex] = objectClassModItems[i];
                modIndex++;
            }

            for(int i = 0; i < permissionModItems.length; i++)
            {
                mods[modIndex] = permissionModItems[i];
                modIndex++;
            }

            //
            // Perform the modifications
            //
            getContext().modifyAttributes(getSubject(), mods);
        }
    }
}