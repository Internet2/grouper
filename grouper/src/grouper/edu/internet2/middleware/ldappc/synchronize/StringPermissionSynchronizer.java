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
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This synchronizes permissions stored in the directory as strings in an
 * attribute.
 */
public class StringPermissionSynchronizer extends PermissionSynchronizer
{
    /**
     * Delimiter to use in the string representation of the permission.
     */
    public static final String DELIMITER = ":";

    /**
     * Holds the permission listing attribute modifications.
     */
    private AttributeModifier  permissionMods;

    /**
     * Holds the objectClass modifications.
     */
    private AttributeModifier  objectClassMods;

    /**
     * Constructs a <code>StringPermissionSynchronizer</code>.
     * 
     * @param ctx
     *            Ldap context to use for provisioning
     * @param subject
     *            DN of the subject whose permissions are being provisioned
     * @param configuration
     *            Signet provisioning configuration
     * @param options
     *            Signet provisioning options
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     * 
     * @throws NamingException
     *             Thrown when a naming exception occurs.
     * @throws LdappcConfigurationException
     *             Thrown if the configuration file is incorrect.
     */
    public StringPermissionSynchronizer(LdapContext ctx, Name subject, SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options, SubjectCache subjectCache)
            throws NamingException, LdappcConfigurationException
    {
        //
        // Call super constructor
        //
        super(ctx, subject, configuration, options, subjectCache);

        //
        // Try to get the permissions listing string attribute name as it is
        // needed to initialize instance variables
        //
        String listAttrName = configuration.getPermissionsListingStringAttribute();
        if (listAttrName == null)
        {
            throw new LdappcConfigurationException("The name of the attribute to store permission strings is null.");
        }

        //
        // Build the permission string "empty value" value. It must be prefixed
        // so it will be deleted if provisioned permissions are added
        //
        String emptyValue = configuration.getPermissionsListingStringEmptyValue();
        if (emptyValue != null)
        {
            emptyValue = configuration.getPermissionsListingStringPrefix() + DELIMITER
                    + configuration.getPermissionsListingStringEmptyValue();
        }

        //
        // Initialize the instance attributes
        //
        objectClassMods = new AttributeModifier(LdapUtil.OBJECT_CLASS_ATTRIBUTE);
        permissionMods = new AttributeModifier(listAttrName, emptyValue);
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
        Attributes attributes = getContext().getAttributes(getSubject(),
                new String[] { permissionMods.getAttributeName(), objectClassMods.getAttributeName() });

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
            String prefix = getConfiguration().getPermissionsListingStringPrefix() + DELIMITER;
            prefix = prefix.toLowerCase();

            //
            // Compare prefix with the existing values
            //
            NamingEnumeration values = attribute.getAll();
            while (values.hasMore())
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
        objectClassMods.init();
        String stringObjectClass = getConfiguration().getPermissionsListingStringObjectClass();
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
        ModificationItem[] permissionModItems = permissionMods.getModifications();
        ModificationItem[] objectClassModItems = objectClassMods.getModifications();
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

            for (int i = 0; i < objectClassModItems.length; i++)
            {
                mods[modIndex] = objectClassModItems[i];
                modIndex++;
            }

            for (int i = 0; i < permissionModItems.length; i++)
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
