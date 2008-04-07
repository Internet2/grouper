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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.ChildGroupFilter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperQuery;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.NullFilter;
import edu.internet2.middleware.grouper.QueryException;
import edu.internet2.middleware.grouper.QueryFilter;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.UnionFilter;
import edu.internet2.middleware.grouper.queryFilter.GroupAttributeExactFilter;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.synchronize.GroupEntrySynchronizer;
import edu.internet2.middleware.ldappc.synchronize.GroupSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.StringMembershipSynchronizer;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * This class provisions Grouper data.
 */
public class GrouperProvisioner
        extends Provisioner
{
    /**
     * Provisioning configuration
     */
    private GrouperProvisionerConfiguration configuration;

    /**
     * Provisioning options
     */
    private GrouperProvisionerOptions       options;

    /**
     * LDAP context for provisioning
     */
    private LdapContext                     ldapCtx;

    /**
     * Constructs a <code>GrouperProvisioner</code> with the given
     * provisioning configuration, options and Ldap context.
     * 
     * @param configuration
     *            GrouperProvisionerConfiguration providing all configuration
     *            data.
     * @param options
     *            GrouperProvisionerOptions providing values for the
     *            provisioning options.
     * @param ldapCtx
     *            the Ldap context to use for provisioning
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     */
    public GrouperProvisioner(GrouperProvisionerConfiguration configuration,
            GrouperProvisionerOptions options, LdapContext ldapCtx,
            SubjectCache subjectCache)
    {
        super(subjectCache);
        this.configuration = configuration;
        this.options = options;
        this.ldapCtx = ldapCtx;
    }

    /**
     * This provisions Grouper data to a directory. This uses provisioning
     * options to determine the Grouper data to provision to the directory, and
     * the provisioning configuration to determine how the data is represented
     * in the directory.
     * 
     * @throws edu.internet2.middleware.grouper.SessionException
     *             thrown if a Grouper session error occurs
     * @throws edu.internet2.middleware.grouper.SchemaException
     *             thrown if a Grouper Registry schema error occurs
     * @throws javax.naming.NamingException
     *             thrown if an error occured interacting with the directory.
     * @throws edu.internet2.middleware.grouper.AttributeNotFoundException
     *             thrown if a group attribute is not found.
     * @throws edu.internet2.middleware.ldappc.MultiErrorException
     *             thrown if one or more exceptions occurs that will not stop
     *             processing but should be reported
     */
    public void provision() throws QueryException, SchemaException, NamingException, AttributeNotFoundException, SessionException, MultiErrorException, LdappcException
    {
        //
        // Create a Grouper session control
        //
        GrouperSessionControl sessionControl = new GrouperSessionControl();

        //
        // Start a grouper session
        //
        if (!sessionControl.startSession(options.getSubjectId()))
        {
            //
            // Can't do anything without a Grouper session
            //
            throw new SessionException();
        }

        //
        // Try to provision data
        //
        try
        {
            //
            // Find the set of Groups to be provisioned
            //
            Set<Group> groups = buildGroupSet(sessionControl.getSession());

            //
            // If provisioning Groups, do so
            //
            if (options.getDoGroups())
            {
                provisionGroups(ldapCtx, groups);
            }

            //
            // If provisioning memberships do so
            //
            if (options.getDoMemberships())
            {
                provisionMemberships(ldapCtx, groups);
            }
        }
        finally
        {
            //
            // Stop the Grouper session; log any errors
            //
            if (!sessionControl.stopSession())
            {
                ErrorLog.error(this.getClass(),
                        "Failed to stop Grouper session");
            }
        }
    }

    /**
     * This builds the set of Groups to be provisioned.
     * 
     * @param session
     *            Grouper session for querying Grouper
     * @return {@link java.util.Set} of Groups, possibly empty, to be
     *         provisioned.
     * @throws edu.internet2.middleware.grouper.QueryException
     *             thrown if a Grouper Query error occurs
     */
    protected Set<Group> buildGroupSet(GrouperSession session) throws QueryException
    {
        return buildQueryGroupList(session);
    }

    /**
     * This builds the set of Groups identified by the subordinate stem and
     * attribute value queries defined in the provisioning configuration.
     * 
     * @param session
     *            Grouper session for querying Grouper
     * @return {@link java.util.Set} of Groups, possibly empty, matching the
     *         defined subordinate stem and attribute value queries.
     */
    protected Set<Group> buildQueryGroupList(GrouperSession session) throws QueryException
    {
        //
        // Find the root stem for building filters
        //
        Stem rootStem = StemFinder.findRootStem(session);

        //
        // Init the query filter
        // 
        QueryFilter groupFilter = new NullFilter();

        //
        // Build the attribute value query filters
        //
        Map<String, Set<String>> attrValueMap = configuration.getGroupAttrMatchingQueries();
        for (Map.Entry<String, Set<String>> entry : attrValueMap.entrySet())
        {
            //
            // Get the attribute name and set of values
            //
            String name = entry.getKey();
            for (String value : entry.getValue())
            {
                groupFilter = new UnionFilter(groupFilter,
                        new GroupAttributeExactFilter(name, value, rootStem));
            }
        }

        //
        // Build the sub-stem query filters
        //
        Set<String> subStems = configuration.getGroupSubordinateStemQueries();
        for (String stemName : subStems)
        {
            try
            {
                Stem stem = StemFinder.findByName(session,
                        stemName);
                groupFilter = new UnionFilter(groupFilter,
                        new ChildGroupFilter(stem));
            }
            catch (StemNotFoundException snfe)
            {
                ErrorLog.warn(getClass(), snfe.getMessage());
            }
        }

        //
        // Build and execute the query
        //
        GrouperQuery query = GrouperQuery.createQuery(session, groupFilter);

        return query.getGroups();
    }

    /**
     * Provision the set of Groups.
     * 
     * @param ctx
     *            Ldap context to provision the groups
     * @param groups
     *            Set of Groups to be provisioned
     * @throws javax.naming.NamingException
     *             thrown if an error occured interacting with the directory.
     * @throws LdappcConfigurationException
     *             thrown if an Ldappc configuration error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected void provisionGroups(LdapContext ctx, Set groups) throws NamingException, LdappcException
    {
        //
        // Get the Name of the root ou
        //
        String rootDnStr = configuration.getGroupDnRoot();
        if (rootDnStr == null)
        {
            throw new LdappcConfigurationException(
                    "Group root DN is not defined.");
        }

        Name rootDn = ctx.getNameParser(LdapUtil.EMPTY_NAME).parse(rootDnStr);

        //
        // Synchronize the root
        //
        GroupSynchronizer synchronizer = new GroupEntrySynchronizer(ctx,
                rootDn, configuration, options, subjectCache);
        synchronizer.synchronize(groups);
    }

    /**
     * Provision the memberships from a set of Groups.
     * 
     * @param ctx
     *            Ldap context to provision the groups
     * @param groups
     *            Set of Groups to be provisioned
     * @throws javax.naming.NamingException
     *             thrown if an error occured interacting with the directory.
     */
    protected void provisionMemberships(LdapContext ctx, Set<Group> groups) throws NamingException, MultiErrorException
    {
        //
        // Initialize a vector to hold all caught exceptions that should be
        // reported, but not immediately thrown
        //
        Vector<Exception> caughtExceptions = new Vector<Exception>();

        /*
         * The structure used below is a compromise between memory usage and
         * duration when dealing with large databases. It may need to be altered
         * based on an individual situation.
         */

        //
        // Build the set of all subjects with memberships
        //
        Set<Name> existingSubjectDns = buildSourceSubjectDnSet();

        //
        // Set to hold Uuids of processed members
        //
        Set<String> processedMembers = new HashSet<String>();

        //
        // Map the groups to their set of member's Uuids. This is used to
        // determine membership in a group.
        //
        Map<Group, Set<String>> unprocessedGroups = new HashMap<Group, Set<String>>();
        for (Group group : groups)
        {
            //
            // Build a HashSet for group members
            //
            HashSet<String> memberUuids = new HashSet<String>();

            //
            // Populate the memberUuids (note: this is one of the memory verses
            // time tradeoffs. The entire member set could be stored, but that
            // requires more memory than storing just the Uuid)
            //
            for (Member member : (Set<Member>) group.getMembers())
            {
                memberUuids.add(member.getUuid());
            }

            //
            // Add group and memberUuids to unprocessed groups
            //
            unprocessedGroups.put(group, memberUuids);
        }

        //
        // Loop through the groups in order to process the members
        //
        for (Group group : groups)
        {
            //
            // Process all of the members for this group
            // (NOTE: If member set had been stored above, it could be gotten
            // from the unprocessedGroups map.)
            //
            for (Member member : (Set<Member>) group.getMembers())
            {
                //
                // If the member has already been processed simply continue
                //
                if (processedMembers.contains(member.getUuid()))
                {
                    continue;
                }

                //
                // As the member hasn't been processed before, add the member to
                // the processed members set.
                //
                processedMembers.add(member.getUuid());

                //
                // Get the member's subject
                //
                Subject subject = null;
                try
                {
                    subject = member.getSubject();
                }
                catch (SubjectNotFoundException snfe)
                {
                    //
                    // If subject not found, log it and continue
                    // 
                    String errorData = getErrorData(member, null, null);
                    logThrowableWarning(snfe, errorData);
                    continue;
                }

                //
                // Look for subject in the directory
                //
                Name subjectDn = null;
                try
                {
                    subjectDn = subjectCache.findSubjectDn(ldapCtx, configuration, subject);
                }
                catch (Exception e)
                {
                    //
                    // If not found, simply log the error and continue
                    //
                    String errorData = getErrorData(member, subject, null);
                    logThrowableWarning(e, errorData);
                    continue;
                }

                //
                // Remove the subject DN from set of existing subjects
                //
                existingSubjectDns.remove(subjectDn);

                //
                // Determine the subset of groups which this member belongs
                // Given the looping structure, only need to check the
                // unprocessed groups.
                // (NOTE: The unprocessedGroups map could be changed to just a
                // set rather than a map if member.isMember() was used here.
                // This would reduce memory but increase processing time)
                //
                Set<Group> memberGroups = new HashSet<Group>();
                for (Map.Entry<Group, Set<String>> entry : unprocessedGroups.entrySet())
                {
                    Group unprocGrp = entry.getKey();
                    Set memberSet = entry.getValue();
                    if (memberSet.contains(member.getUuid()))
                    {
                        memberGroups.add(unprocGrp);
                    }
                }

                //
                // Get the membership synchronizer and try to synchronize
                //
                MembershipSynchronizer synchronizer = getMembershipSynchronizer(
                        ctx, subjectDn);
                try
                {
                    synchronizer.synchronize(memberGroups);
                }
                catch (Exception e)
                {
                    String errorData = getErrorData(member, subject, subjectDn);
                    logThrowableError(e, errorData);
                    caughtExceptions.add(new LdappcException(errorData, e));
                    continue;
                }
            }

            //
            // Now that all members have been processed, remove the group from
            // the unprocessed group set
            //
            unprocessedGroups.remove(group);
        }

        //
        // Clear the memberships from any subject not processed above.
        //
        try
        {
            clearSubjectEntryMemberships(ctx, existingSubjectDns);
        }
        catch (Exception e)
        {
            logThrowableError(e);
            caughtExceptions.add(e);
        }

        //
        // Throw any caught exceptions
        //
        if (caughtExceptions.size() > 0)
        {
            throw new MultiErrorException((Exception[]) caughtExceptions
                    .toArray(new Exception[0]));
        }
    }

    /**
     * Returns a Membership Synchronizer based on the configuration and options.
     * 
     * @param ctx
     *            Ldap Context
     * @param subjectDn
     *            Subject DN
     * @return Membership synchronizer
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private MembershipSynchronizer getMembershipSynchronizer(LdapContext ctx,
            Name subjectDn) throws NamingException
    {
        //
        // Only one type now but this allows for building others based on
        // configuration or options
        //
        return new StringMembershipSynchronizer(ctx, subjectDn, configuration,
                options, subjectCache);
    }

    /**
     * Builds the subject DN set. The subject DN set is the union of each set
     * created from each source id using the subject source identifier Ldap
     * filter provided by the <code>configuration</code>. Each entry that is
     * identified by the filter and that has the member group listing attribute
     * populated is included in the subject DN set.
     * 
     * @return Set of subject DNs
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    protected Set<Name> buildSourceSubjectDnSet() throws NamingException
    {
        //
        // Init the map to return
        //
        Set<Name> subjectDns = new HashSet<Name>();

        //
        // Get the source to subject ldap filter mapping from the configuration
        //
        Map<String, LdapSearchFilter> sourceFilterMap = configuration.getSourceSubjectLdapFilters();

        //
        // Iterate over the sourceFilterMap to build list of subjects for each
        // source
        //
        for (Map.Entry<String, LdapSearchFilter> entry : sourceFilterMap.entrySet())
        {
            //
            // Get the source id and associated filter
            //
            String source = entry.getKey();
            LdapSearchFilter filter = entry.getValue();

            //
            // Add the subjectDns for this source
            //
            addSubjectDnSet(subjectDns, filter, source);
        }

        return subjectDns;
    }

    /**
     * Adds identified subject DNs the given set of subject DNs. The subject DN
     * set added is created by identifying all DNs satisfying the
     * LdapSearchFilter and having the listing attribute populated.
     * 
     * @param subjectDns
     *            Set of subject DNs
     * @param filter
     *            Ldap search filter defined for a source
     * @param source
     *            source ID for the subject
     * @throws NamingException
     *             thrown if a Naming error occurs.
     */
    private void addSubjectDnSet(Set<Name> subjectDns, LdapSearchFilter filter, String source) throws NamingException
    {
        //
        // Build the search control
        //
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(filter.getScope());
        searchControls.setCountLimit(0);

        //
        // Get the member list attribute name and object class
        //
        String listAttribute = configuration.getMemberGroupsListAttribute();
        if (listAttribute == null)
        {
            throw new LdappcConfigurationException(
                    "Member groups list attribute is null");
        }

        String listObjectClass = configuration.getMemberGroupsListObjectClass();

        //
        // Update the ldap query filter by replacing "{0}" from the ldap search
        // filter with "*"
        //
        String ldapFilter = LdapUtil.convertParameterToAsterisk(filter
                .getFilter(), 0);

        //
        // Build the actual filter expression for performing the search
        //
        String filterExpr = ldapFilter + "(" + listAttribute + "=*)";
        if (listObjectClass != null)
        {
            filterExpr = filterExpr + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE
                    + "=" + listObjectClass + ")";
        }
        filterExpr = "(&" + filterExpr + ")";

        //
        // Build the base DN
        //
        NameParser parser = ldapCtx.getNameParser(LdapUtil.EMPTY_NAME);
        Name baseDn = parser.parse(filter.getBase());

        //
        // perform the search
        //
        NamingEnumeration searchResults = ldapCtx.search(baseDn, filterExpr,
                searchControls);

        //
        // Process the search results
        //
        while (searchResults.hasMore())
        {
            //
            // Get the search result
            //
            SearchResult searchResult = (SearchResult) searchResults.next();

            //
            // Build the DN for the search result
            //
            Name subjectDn = parser.parse(searchResult.getName());
            subjectDn = subjectDn.addAll(0, baseDn);

            //
            // Save the subject dn
            //
            subjectDns.add(subjectDn);
        }
    }

    /**
     * Clears the membership listings from subject entries.
     * 
     * @param ctx
     *            Ldap context
     * @param subjectDnSet
     *            Set of subject DNs whose memberships are to be cleared
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private void clearSubjectEntryMemberships(LdapContext ctx, Set<Name> subjectDnSet) throws NamingException
    {
        //
        // Define an empty set that is used below
        //
        Set<Group> emptySet = new HashSet<Group>();

        //
        // Iterate over the subject DNs
        //
        for (Name subjectDn : subjectDnSet)
        {
            try
            {
                //
                // Get a membership synchronizer and synchronize with an empty
                // set.
                // (Doing it this way ensures that required attributes are
                // handled correctly).
                //
                MembershipSynchronizer synchronizer = getMembershipSynchronizer(
                        ctx, subjectDn);
                synchronizer.synchronize(emptySet);
            }
            catch (Exception e)
            {
                logThrowableError(e);
            }
        }
    }

    /**
     * Builds an error data string based on the objects provided. It is
     * <b>assumed</b> that all three objects are related.
     * 
     * @param member
     *            Group member
     * @param subject
     *            Subject associated with <code>member</code>
     * @param subjectDn
     *            DN of <code>subject</code>'s LDAP entry
     * @return data string
     */
    protected String getErrorData(Member member, Subject subject, Name subjectDn)
    {
        String errorData = "MEMBER";

        if (member != null)
        {
            errorData += getMemberData(member);
        }
        if (subject != null)
        {
            errorData += "[ SUBJECT " + subjectCache.getSubjectData(subject) + " ]";
        }
        if (subjectDn != null)
        {
            errorData += "[ SUBJECT DN = " + subjectDn + " ]";
        }

        return errorData;
    }

    /**
     * Returns member data string
     * 
     * @param member
     *            Member
     * @return member data string
     */
    public static String getMemberData(Member member)
    {
        String memberData = "null";
        if (member != null)
        {
            memberData = "[ UUID = " + member.getUuid() + " ][ SUBJECT ID = "
                    + member.getSubjectId() + " ][ SUBJECT SOURCE ID = "
                    + member.getSubjectSourceId() + " ]";
        }
        return memberData;
    }

    /**
     * Returns group data string
     * 
     * @param group
     *            Group
     * @return group data string
     */
    public static String getGroupData(Group group)
    {
        String grpData = "null";
        if (group != null)
        {
            grpData = "[ DISPLAY NAME = " + group.getDisplayName()
                    + " ][NAME = " + group.getName() + "][UID = "
                    + group.getUuid() + "]";
        }
        return grpData;
    }
}
