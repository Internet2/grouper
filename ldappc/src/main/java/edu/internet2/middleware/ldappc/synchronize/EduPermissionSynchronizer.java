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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.MultiErrorException;
import edu.internet2.middleware.ldappc.SignetProvisionerConfiguration;
import edu.internet2.middleware.ldappc.SignetProvisionerOptions;
import edu.internet2.middleware.ldappc.ldap.EduPermission;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * This synchronizes permissions stored in the directory as <i>eduPermission</i>
 * entries.
 */
public class EduPermissionSynchronizer extends PermissionSynchronizer
{
    /**
     * Set to hold the DNs of eduPermission objects that need to be deleted
     */
    private Set<Name> deletes = new HashSet<Name>();

    /**
     * Set to hold the DNs of eduPermission objects that have already been
     * processed
     */
    private Set<Name> processed = new HashSet();

    /**
     * Constructs a <code>EduPermissionSynchronizer</code>
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
    public EduPermissionSynchronizer(LdapContext ctx, Name subject,
            SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options,
            SubjectCache subjectCache)
    {
        super(ctx, subject, configuration, options, subjectCache);
    }

    /**
     * This commits any changes not already committed to the directory.
     * 
     * @throws MultiErrorException
     *             thrown if one or more NamingExceptions are encountered
     * @see edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer#commit()
     */
    protected void commit() throws MultiErrorException
    {
        //
        // Init vars
        //
        Vector<Exception> namingExceptions = new Vector();
        LdapContext context = getContext();

        //
        // Delete any eduPermission objects that are no longer valid
        //
        for (Name dn : deletes) {
            try
            {
                LdapUtil.delete(context, dn);
            }
            catch(NamingException ne)
            {
                ErrorLog.error(getClass(), "Failed to delete " + dn + " for "
                        + getSubject() + " :: " + ne.getMessage());
                namingExceptions.add(ne);
            }
        }

        //
        // If necessary, thrown an exception
        //
        if (namingExceptions.size() > 0)
        {
            throw new MultiErrorException((Exception[]) namingExceptions
                    .toArray(new Exception[0]));
        }
    }

    /**
     * Perform any initialization prior to processing the set of permissions.
     */
    protected void initialize() throws NamingException
    {
        //
        // Clear any existing data and initialize variables
        //
        deletes.clear();
        processed.clear();

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
        // Build the query filter, search controls and find all existing
        // permissions
        //
        String filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
                + EduPermission.OBJECT_CLASS + ")";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        searchControls.setCountLimit(0);

        NamingEnumeration searchEnum = getContext().search(getSubject(),
                filter, searchControls);

        //
        // Populate deletes with DNs of existing eduPermission objects. It
        // is assumed that everything needs to be deleted.
        //
        NameParser parser = getContext().getNameParser(LdapUtil.EMPTY_NAME);
        while(searchEnum.hasMore())
        {
            SearchResult searchResult = (SearchResult) searchEnum.next();
            if (searchResult.isRelative())
            {
                //
                // Build the eduPermDn
                //
                Name eduPermDn = parser.parse(searchResult.getName());
                eduPermDn = eduPermDn.addAll(0, getSubject());

                //
                // Add eduPermDn to the deletes list
                //
                deletes.add(eduPermDn);
            }
            else
            {
                //
                // Log it and continue on.
                //
                ErrorLog.error(this.getClass(),
                        "Unable to handle LDAP URL references: "
                                + searchResult.getName());
            }
        }
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
     * @see edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer#performInclude(Privilege,
     *      int)
     */
    protected void performInclude(Privilege privilege, Function function, int status) throws NamingException
    {
        //
        // Sort limit values by limit ids
        //
        Map<String, BasicAttribute> limitMap = buildLimitMap(privilege.getLimitValues());

        //
        // Build the common values of the privilege DN
        //
        Permission permission = privilege.getPermission();
        String safeSubsystemId = LdapUtil.makeLdapNameSafe(permission.getSubsystem().getId());
        String safeFunctionId = LdapUtil.makeLdapNameSafe(function.getId());
        String safePermissionId = LdapUtil.makeLdapNameSafe(permission.getId());

        //
        // Get the limit values and iterate over those
        //
        for (String limitId : limitMap.keySet()) {
            //
            // Build the privilege DN made up of eduPermissionSubsytemId,
            // eduPermissionId, and eduPermissionLimitId.
            //
            Name eduPermDn = (Name) getSubject().clone();
            eduPermDn.add(EduPermission.Attribute.EDU_PERMISSION_SUBSYSTEM_ID + "=" + safeSubsystemId
                    + LdapUtil.MULTIVALUED_RDN_DELIMITER
                    + EduPermission.Attribute.EDU_PERMISSION_FUNCTION_ID + "=" + safeFunctionId
                    + LdapUtil.MULTIVALUED_RDN_DELIMITER
                    + EduPermission.Attribute.EDU_PERMISSION_ID + "=" + safePermissionId
                    + LdapUtil.MULTIVALUED_RDN_DELIMITER
                    + EduPermission.Attribute.EDU_PERMISSION_LIMIT_ID + "=" + LdapUtil.makeLdapNameSafe(limitId));

            //
            // Act based on whether or not the eduPermission DN exists in
            // deletes
            //
            if (deletes.remove(eduPermDn))
            {
                //
                // If status is new, modified or unknown, make sure entry is
                // current.
                //
                if (status == STATUS_NEW || status == STATUS_MODIFIED
                        || status == STATUS_UNKNOWN)
                {
                    updateEduPermission(eduPermDn, limitId, privilege,
                            (BasicAttribute) limitMap.get(limitId));
                }
            }
            else
            {
                //
                // If a eduPermDn is encountered again, simply log an error and
                // continue. This is a degenerate case that can occur, but
                // should NOT.
                //
                if (processed.contains(eduPermDn))
                {
                    ErrorLog.error(getClass(), "eduPermission identified by "
                            + eduPermDn + " has already been encountered,"
                            + " and will not be processed another time.");
                    continue;
                }

                //
                // Add a new eduPermission entry
                //
                addEduPermission(eduPermDn, limitId, safeFunctionId,
                        privilege, (BasicAttribute) limitMap.get(limitId));
            }

            //
            // Update processed with DN
            //
            processed.add(eduPermDn);
        }
    }

    /**
     * This builds a mapping from Limit ID to all of its values. This
     * transforms the set of LimitValues to a mapping where the key is the Limit
     * ID and the value is a BasicAttribute whose ID is
     * {@link edu.internet2.middleware.ldappc.ldap.EduPermission.Attribute#EDU_PERMISSION_LIMIT}
     * and whose values come from the associated LimitValues.
     * 
     * @param limitValues
     *            Set of LimitValues
     * @return Limit id to BasicAttribute map
     */
    protected Map buildLimitMap(Set<LimitValue> limitValues)
    {
        HashMap limitMap = new HashMap();

        if (limitValues != null)
        {
            for (LimitValue limitValue : limitValues) {
                //
                // Get the limit id and value
                //
                String id = limitValue.getLimit().getId();
                String value = limitValue.getValue();

                //
                // If the id is not already mapped, add it to the map
                //
                if (!limitMap.containsKey(id))
                {
                    limitMap.put(id, new BasicAttribute(
                            EduPermission.Attribute.EDU_PERMISSION_LIMIT));
                }

                //
                // Add the value
                //
                ((BasicAttribute) limitMap.get(id)).add(value);
            }
        }

        return limitMap;
    }

    /**
     * This updates an existing eduPermission entry for the subject. This is a
     * helper method defined for internal use only as most of the parameters are
     * prebuilt and related.
     * 
     * @param eduPermissionDn
     *            DN of the existing entry
     * @param limitId
     *            Limit id
     * @param privilege
     *            Privilege being updated
     * @param limits
     *            BasicAttribute whose ID is
     *            EduPermission.Attribute.EDU_PERMISSION_LIMIT and values are
     *            the limit values for the privilege
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private void updateEduPermission(Name eduPermissionDn, String limitId,
            Privilege privilege, BasicAttribute limits) throws NamingException
    {
        //
        // Get the existing values
        //
        Attributes attributes = getContext().getAttributes(
                eduPermissionDn,
                new String[] { EduPermission.Attribute.EDU_PERMISSION_SCOPE_ID,
                        EduPermission.Attribute.EDU_PERMISSION_SCOPE_NAME,
                        EduPermission.Attribute.EDU_PERMISSION_LIMIT });

        //
        // Init vector to hold the attribute modifiers
        //
        Vector<AttributeModifier> attributeModifiers = new Vector();

        //
        // Get the privilege's scope
        //
        TreeNode scope = privilege.getScope();

        //
        // Build and populate the scope id modifier
        //
        AttributeModifier scopeIdModifier = new AttributeModifier(EduPermission.Attribute.EDU_PERMISSION_SCOPE_ID);

        scopeIdModifier.init(attributes.get(scopeIdModifier.getAttributeName()));

        String scopeId = scope.getId();
        if (scopeId != null)
        {
            scopeIdModifier.store(scopeId);
        }

        //
        // Store the scope id modifier in the attribute modifiers list
        //
        attributeModifiers.add(scopeIdModifier);

        //
        // Build and populate the scope name modifier
        //
        AttributeModifier scopeNameModifier = new AttributeModifier(EduPermission.Attribute.EDU_PERMISSION_SCOPE_NAME);

        scopeNameModifier.init(attributes.get(scopeNameModifier.getAttributeName()));

        String scopeName = scope.getName();
        if (scopeName != null)
        {
            scopeNameModifier.store(scopeName);
        }

        //
        // Store the scope name modifier in the attribute modifiers list
        //
        attributeModifiers.add(scopeNameModifier);

        //
        // Build and populate the limit values modifier
        //
        AttributeModifier limitValuesModifier = new AttributeModifier(EduPermission.Attribute.EDU_PERMISSION_LIMIT);

        limitValuesModifier.init(attributes.get(limitValuesModifier.getAttributeName()));

        NamingEnumeration limitValues = limits.getAll();
        while(limitValues.hasMore())
        {
            limitValuesModifier.store((String) limitValues.next());
        }

        //
        // Store the limit values modifier in the attribute modifiers list
        //
        attributeModifiers.add(limitValuesModifier);

        //
        // Get all of the modifications
        //
        Vector<ModificationItem> modifications = new Vector();
        for (AttributeModifier mod : attributeModifiers)
        {
            ModificationItem[] items = mod.getModifications();
            for(int i = 0; i < items.length; i++)
            {
                modifications.add(items[i]);
            }
        }

        //
        // Build the modification item array
        //
        ModificationItem[] modificationItems = new ModificationItem[modifications.size()];
        for(int i = 0; i < modificationItems.length; i++)
        {
            modificationItems[i] = (ModificationItem) modifications.get(i);
        }

        //
        // Modify the entry
        //
        getContext().modifyAttributes(eduPermissionDn, modificationItems);
    }

    /**
     * This adds a new eduPermission entry to the subject's LDAP entry. This is
     * a helper method defined for internal use only as most of the parameters
     * are prebuilt and related.
     * 
     * @param eduPermDn
     *            DN of the new entry
     * @param limitId
     *            Limit id
     * @param functionId
     *            Function id
     * @param privilege
     *            Privilege being added
     * @param limits
     *            BasicAttribute whose ID is
     *            EduPermission.Attribute.EDU_PERMISSION_LIMIT and values are
     *            the limit values for the privilege
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private void addEduPermission(Name eduPermDn, String limitId,
            String functionId, Privilege privilege, BasicAttribute limits) throws NamingException
    {
        //
        // Get the associated permission
        //
        Permission permission = privilege.getPermission();

        //
        // Build an attribute to hold the values
        //
        Attributes attributes = new BasicAttributes(true);

        //
        // Add the object class attribute
        //
        attributes.put(LdapUtil.OBJECT_CLASS_ATTRIBUTE, EduPermission.OBJECT_CLASS);

        //
        // Add the RDN attributes
        //
        attributes.put(EduPermission.Attribute.EDU_PERMISSION_SUBSYSTEM_ID, permission.getSubsystem().getId());
        attributes.put(EduPermission.Attribute.EDU_PERMISSION_FUNCTION_ID, functionId);
        attributes.put(EduPermission.Attribute.EDU_PERMISSION_ID, permission.getId());
        attributes.put(EduPermission.Attribute.EDU_PERMISSION_LIMIT_ID, limitId);

        //
        // Add the scope values
        //
        TreeNode scope = privilege.getScope();
        attributes.put(EduPermission.Attribute.EDU_PERMISSION_SCOPE_ID, scope.getId());
        if (scope.getName() != null)
        {
            attributes.put(EduPermission.Attribute.EDU_PERMISSION_SCOPE_NAME, scope.getName());
        }

        //
        // Add the limit values using the pre-built attribute
        //
        attributes.put(limits);

        //
        // Build the entry
        //
        getContext().createSubcontext(eduPermDn, attributes);
    }
}
