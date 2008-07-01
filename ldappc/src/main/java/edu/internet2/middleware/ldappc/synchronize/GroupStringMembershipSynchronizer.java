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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.ldappc.EntryNotFoundException;
import edu.internet2.middleware.ldappc.GrouperProvisioner;
import edu.internet2.middleware.ldappc.GrouperProvisionerConfiguration;
import edu.internet2.middleware.ldappc.GrouperProvisionerOptions;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.LdappcException;
import edu.internet2.middleware.ldappc.LdappcRuntimeException;
import edu.internet2.middleware.ldappc.MultiErrorException;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.subject.Subject;

/**
 * This synchronizes groups stored in the directory as entries
 */
public class GroupStringMembershipSynchronizer extends GrouperSynchronizer
{
    private String            groupNameString;
    private BufferedWriter    updateWriter;
    private AttributeModifier members;

    /**
     * Constructs a <code>GroupStringMembershipSynchronizer</code>
     * 
     * @param ctx
     *            Ldap context to be used for synchronizing
     * @param groupNameString
     *            the string to be provisioned as the name of the group
     * @param updateWriter
     *            the Writer to which to write the updates
     * @param configuration
     *            Grouper provisioning configuration
     * @param options
     *            Grouper provisioning options
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     * @throws NamingException
     * @throws LdappcConfigurationException
     */
    public GroupStringMembershipSynchronizer(LdapContext ctx, String groupNameString,
            BufferedWriter updateWriter, GrouperProvisionerConfiguration configuration,
            GrouperProvisionerOptions options, SubjectCache subjectCache)
            throws NamingException, LdappcConfigurationException
    {
        //
        // Call super constructor
        //
        super(ctx, configuration, options, subjectCache);

        //
        // Init various objects
        //
        this.groupNameString = groupNameString;
        this.updateWriter = updateWriter;
        members = new AttributeModifier(configuration.getMemberGroupsListAttribute());
    }

    /**
     * This identifies the underlying group as one that must remain or, if need
     * be, must be added to the subject's LDAP entry. If the group has already
     * been provisioned to the entry, it will remain within the subject's LDAP
     * entry.
     * 
     * @param member
     *            member to be included
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected void performInclude(String member) throws NamingException, LdappcException
    {
        members.store(member);
    }

    /**
     * Perform any initialization prior to processing the set of groups.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected void initialize() throws NamingException, LdappcException
    {
        //
        // Clear existing data
        //
        members.clear();
        populate();
    }

    /**
     * This populates this object with the current values from the root's LDAP
     * entry that are either organizational units or have a object class type of
     * the group entry.
     * 
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     */
    protected void populate() throws NamingException
    {
        Set<String> memberships = new HashSet<String>();

        String listAttrName = getConfiguration().getMemberGroupsListAttribute();
        if (listAttrName == null)
        {
            throw new LdappcConfigurationException(
                    "The name of the attribute to store membership group strings is null.");
        }

        // DebugLog.info("Updating memberships for group " + groupNameString);
        for (Map.Entry<String, LdapSearchFilter> entry : getConfiguration()
                .getSourceSubjectLdapFilters().entrySet())
        {
            String sourceId = entry.getKey();
            LdapSearchFilter ldapSearchFilter = entry.getValue();

            // set search result attributes
            String[] searchAttributes = new String[1];
            searchAttributes[0] = LdapUtil.OBJECT_CLASS_ATTRIBUTE;

            // set search controls
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(searchAttributes);
            searchControls.setReturningObjFlag(false);
            searchControls.setSearchScope(ldapSearchFilter.getScope());
            searchControls.setTimeLimit(600000); // ten minutes

            //
            // Get the member list attribute name and object class
            //
            String listAttribute = getConfiguration().getMemberGroupsListAttribute();
            if (listAttribute == null)
            {
                throw new LdappcConfigurationException("Member groups list attribute is null");
            }

            String listObjectClass = getConfiguration().getMemberGroupsListObjectClass();

            //
            // Update the ldap query filter by replacing "{0}" from the ldap
            // search
            // filter with "*"
            //
            String ldapFilter = LdapUtil
                    .convertParameterToAsterisk(ldapSearchFilter.getFilter(), 0);

            //
            // Build the actual filter expression for performing the search
            //
            String filterExpr = ldapFilter + "(" + listAttribute + "=" + groupNameString + ")";
            if (listObjectClass != null)
            {
                filterExpr = filterExpr + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
                        + listObjectClass + ")";
            }
            filterExpr = "(&" + filterExpr + ")";

            // lookup subjectids for group
            NamingEnumeration answer = null;
            try
            {
                answer = getContext()
                        .search(ldapSearchFilter.getBase(), filterExpr, searchControls);
            }
            catch (Exception e)
            {
                throw new LdappcRuntimeException("unable to determine LDAP members for group "
                        + groupNameString, e);
            }

            try
            {
                while (answer.hasMore())
                {
                    SearchResult searchResult = (SearchResult) answer.next();

                    //
                    // If name is NOT relative, throw an exception
                    //
                    if (!searchResult.isRelative())
                    {
                        throw new EntryNotFoundException(
                                "Unable to resolve the reference found using " + filterExpr);
                    }

                    //
                    // Build the subject's DN.
                    //
                    NameParser parser = getContext().getNameParser(LdapUtil.EMPTY_NAME);
                    Name baseName = parser.parse(ldapSearchFilter.getBase());
                    Name subjectDn = parser.parse(searchResult.getName());
                    subjectDn = subjectDn.addAll(0, baseName);

                    // Add the DN to the memberships.
                    memberships.add(subjectDn.toString());
                }
                answer.close();
            }
            catch (Exception e)
            {
                throw new LdappcRuntimeException("getIsMemberOfMembers retrieval failed: ", e);
            }
        }

        members.init(memberships);
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
    protected void commit(Vector<Exception> caughtExceptions) throws NamingException, LdappcException
    {
        ModificationItem[] mods = members.getModifications();
        for (ModificationItem modItem : mods)
        {
            NamingEnumeration valueEnum = modItem.getAttribute().getAll();
            while (valueEnum.hasMore())
            {
                String value = (String) valueEnum.next();
                try
                {
                    updateWriter.write(value + "\t" + modItem.getModificationOp() + "\t"
                            + groupNameString + "\n");
                }
                catch (IOException e)
                {
                    caughtExceptions.add(e);
                }
            }
        }
        // DebugLog.info("Updated memberships for group " + groupNameString);
    }

    /**
     * Synchronizes the group set with that in the directory.
     * 
     * @param groups
     *            Set of Groups
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     * @throws MultiErrorException
     *             thrown if one or more exceptions occurred that did not need
     *             to stop all processing
     * @throws LdappcException
     *             thrown if an error occurs
     */
    public void synchronize(Set<String> groups) throws NamingException, LdappcException
    {
        //
        // Initialize the process
        //
        initialize();

        //
        // Create a vector to catch exceptions that don't need to stop
        // procesing
        //
        Vector<Exception> caughtExceptions = new Vector<Exception>();

        //
        // Iterate over the set of groups
        //
        for (String member : groups)
        {
            //
            // Process the member
            //
            performInclude(member);
        }

        //
        // Commit the modifications to the directory
        //
        commit(caughtExceptions);

        //
        // If there were caughtExceptions throw a multiple error exception
        //
        if (caughtExceptions.size() > 0)
        {
            throw new MultiErrorException("Non-fatal errors occurred processing the members for "
                    + groupNameString, (Exception[]) caughtExceptions.toArray(new Exception[0]));
        }
    }

    /**
     * Builds an error data string based on the objects provided.
     * 
     * @param subject
     *            Subject
     * @return data string for error message
     */
    protected String getErrorData(Subject subject)
    {
        return "SUBJECT[" + subjectCache.getSubjectData(subject) + "]";
    }

    /**
     * Builds an error data string based on the objects provided.
     * 
     * @param group
     *            Group
     * @return data string for error message
     */
    protected String getErrorData(Group group)
    {
        return "GROUP[" + GrouperProvisioner.getGroupData(group) + "]";
    }

    /**
     * Builds an error data string based on the objects provided.
     * 
     * @param member
     *            Member
     * @return member data string
     */
    protected String getErrorData(Member member)
    {
        return "MEMBER[" + GrouperProvisioner.getMemberData(member) + "]";
    }
}
