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
import javax.naming.NamingException;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.LdappcException;
import edu.internet2.middleware.ldappc.GrouperProvisionerConfiguration;
import edu.internet2.middleware.ldappc.GrouperProvisionerOptions;


import edu.internet2.middleware.ldappc.util.LdapUtil;


/**
 * This synchronizes memberships stored in the directory as strings in an
 * attribute.
 */
public class StringMembershipSynchronizer extends MembershipSynchronizer
{
    /**
     * Holds the membership listing attribute modifications
     */
    private AttributeModifier membershipMods;

    /**
     * Holds the object class attribute modifications
     */
    private AttributeModifier objectClassMods;

    /**
     * Name of the group naming attribute
     */
    private String groupNamingAttribute;

    /**
     * Constructs a <code>StringMembershipSynchronizer</code>
     * 
     * @param ctx
     *            Ldap context to use for provisioning
     * @param subject
     *            DN of the subject whose memberships are being provisioned
     * @param configuration
     *            Signet provisioning configuration
     * @param options
     *            Signet provisioning options
     */
    public StringMembershipSynchronizer(LdapContext ctx, Name subject,
            GrouperProvisionerConfiguration configuration,
            GrouperProvisionerOptions options) throws NamingException,
            LdappcConfigurationException
    {
        //
        // Call super constructor
        //
        super(ctx, subject, configuration, options);

        //
        // Try to get the membership listing string attribute name as it is
        // needed to initialize instance variables
        //
        String listAttrName = configuration.getMemberGroupsListAttribute();
        if (listAttrName == null)
        {
            throw new LdappcConfigurationException(
                    "The name of the attribute to store membership group strings is null.");
        }
        
        //
        // Initialize the instance attributes
        //
        objectClassMods = new AttributeModifier(LdapUtil.OBJECT_CLASS_ATTRIBUTE);
        membershipMods = new AttributeModifier(listAttrName,configuration.getMemberGroupsListEmptyValue());
        
        //
        // Get the group naming attribute
        //
        groupNamingAttribute = configuration.getMemberGroupsNamingAttribute();
        if (groupNamingAttribute == null)
        {
            throw new LdappcConfigurationException(
                    "The name of the group naming attribute is null.");
        }
    }

    /**
     * This identifies the underlying group as one that must remain or, if need
     * be, must be added to the subject's LDAP entry. If the group has already
     * been provisioned to the entry, it will remain within the subject's LDAP
     * entry.
     * 
     * @param group
     *            Group to be included
     * @param status
     *            Either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
     *            {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     * @see edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer#performInclude(Group,
     *      int)
     */
    protected void performInclude(Group group, int status)
            throws NamingException, LdappcException
    {
        //
        // Try to get the value of the group naming attribute
        //
        String groupNameString = null;
        try
        {
            groupNameString = group.getAttribute(groupNamingAttribute);
        }
        catch(AttributeNotFoundException anfe)
        {
            throw new LdappcException("Attribute [" + groupNamingAttribute
                    + "] not found for " + group.getName(), anfe);
        }

        //
        // Store the group name string in the attribute modifier.
        // (status doesn't improve things here so ignore it)
        //
        membershipMods.store(groupNameString);
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
        membershipMods.clear();
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
                new String[] { membershipMods.getAttributeName(),
                        objectClassMods.getAttributeName() });

        //
        // Initialize the membership listing attribute modifier
        //
        Attribute attribute = attributes.get(membershipMods.getAttributeName());
        membershipMods.init(attribute);

        //
        // Populate the objectClass modifier if needed
        //
        objectClassMods.init(null);
        String stringObjectClass = getConfiguration()
                .getMemberGroupsListObjectClass();
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
     * @see edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer#commit()
     */
    protected void commit() throws NamingException, LdappcException
    {
        //
        // Determine how many modifications are to be performed
        //
        ModificationItem[] objectClassModItems = objectClassMods
                .getModifications();
        ModificationItem[] membershipModItems = membershipMods
                .getModifications();
        int modCnt = objectClassModItems.length + membershipModItems.length;

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
            // 2. Add the membership listing attribute modifications
            //
            int modIndex = 0;

            for(int i = 0; i < objectClassModItems.length; i++)
            {
                mods[modIndex] = objectClassModItems[i];
                modIndex++;
            }

            for(int i = 0; i < membershipModItems.length; i++)
            {
                mods[modIndex] = membershipModItems[i];
                modIndex++;
            }

            //
            // Perform the modifications
            //
            getContext().modifyAttributes(getSubject(), mods);
        }
    }
}