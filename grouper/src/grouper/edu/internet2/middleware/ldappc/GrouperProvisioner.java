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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.doomdark.uuid.UUIDGenerator;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.ChildGroupFilter;
import edu.internet2.middleware.grouper.filter.GroupAttributeExactFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.NullFilter;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.filter.UnionFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.synchronize.GroupEntrySynchronizer;
import edu.internet2.middleware.ldappc.synchronize.GroupStringMembershipSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.GroupSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.StringMembershipSynchronizer;
import edu.internet2.middleware.ldappc.util.ExternalSort;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * This class provisions Grouper data.
 */
public class GrouperProvisioner extends Provisioner
{
    private static final Log LOG = GrouperUtil.getLog(GrouperProvisioner.class);
    
    /**
     * Number of records of membership updates to sort in memory. This value
     * (200,000) is a good compromise between speed and memory.
     */
    private static final int                SORT_BATCH_SIZE = 200000;

    /**
     * Provisioning configuration.
     */
    private GrouperProvisionerConfiguration configuration;

    /**
     * Provisioning options.
     */
    private GrouperProvisionerOptions       options;

    /**
     * LDAP context for provisioning.
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
    public GrouperProvisioner(GrouperProvisionerConfiguration configuration, GrouperProvisionerOptions options,
            LdapContext ldapCtx, SubjectCache subjectCache)
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
    public void provision() throws QueryException, SchemaException, NamingException, AttributeNotFoundException,
            SessionException, MultiErrorException, LdappcException
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
                provisionGroups(groups);
            }

            //
            // If provisioning memberships do so
            //
            if (options.getDoMemberships())
            {
                provisionMemberships(groups);
            }
        }
        finally
        {
            //
            // Stop the Grouper session; log any errors
            //
            if (!sessionControl.stopSession())
            {
                ErrorLog.error(this.getClass(), "Failed to stop Grouper session");
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
     * @throws QueryException
     *             thrown if grouper query can't be constructed.
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
                groupFilter = new UnionFilter(groupFilter, new GroupAttributeExactFilter(name, value, rootStem));
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
                Stem stem = StemFinder.findByName(session, stemName);
                groupFilter = new UnionFilter(groupFilter, new ChildGroupFilter(stem));
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
     * @param groups
     *            Set of Groups to be provisioned
     * 
     * @throws javax.naming.NamingException
     *             thrown if an error occured interacting with the directory.
     * @throws LdappcConfigurationException
     *             thrown if an Ldappc configuration error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected void provisionGroups(Set groups) throws NamingException, LdappcException
    {
        //
        // Get the Name of the root ou
        //
        String rootDnStr = configuration.getGroupDnRoot();
        if (rootDnStr == null)
        {
            throw new LdappcConfigurationException("Group root DN is not defined.");
        }

        Name rootDn = ldapCtx.getNameParser(LdapUtil.EMPTY_NAME).parse(rootDnStr);

        //
        // Synchronize the root
        //
        GroupSynchronizer synchronizer = new GroupEntrySynchronizer(ldapCtx, rootDn, configuration, options,
                getSubjectCache());
        synchronizer.synchronize(groups);
    }

    /**
     * Provision the memberships from a set of Groups.
     * 
     * @param groups
     *            Set of Groups to be provisioned
     * 
     * @throws NamingException
     *             thrown if an error occured interacting with the directory.
     * @throws MultiErrorException
     *             thrown if we catch exceptions while doing provisioning.
     */
    protected void provisionMemberships(Set<Group> groups) throws NamingException, MultiErrorException
    {
        //
        // Initialize a vector to hold all caught exceptions that should be
        // reported, but not immediately thrown
        //
        Vector<Exception> caughtExceptions = new Vector<Exception>();

        //
        // Build the set of all subjects with memberships
        //
        // DebugLog.info("Collecting existing subjects with memberships");
        Set<Name> existingSubjectDns = new HashSet<Name>();
        Set<String> existingObjectDns = new HashSet<String>();
        buildSourceSubjectDnSet(existingSubjectDns, existingObjectDns);

        //
        // File for writing memberships to when provisioning memberships. Each
        // line of the file contains tab-delimited data consisting of the
        // subject DN, the modify operator, and the group name string to be
        // provisioned. This can then be sorted and read, batched by subject DN,
        // to efficiently update the memberships for each subject.
        //
        File updatesFile = getUpdatesFile();

        // DebugLog.info("Collecting grouper memberships");
        BufferedWriter updatesWriter = openMembershipWriter(updatesFile);
        String groupNamingAttribute = configuration.getMemberGroupsNamingAttribute();
        if (groupNamingAttribute == null)
        {
            throw new LdappcConfigurationException("The name of the group naming attribute is null.");
        }

        //
        // Iterate over the groups.
        //
        for (Group group : groups)
        {
            //
            // Get the value corresponding to the group to be provisioned as a
            // membership. Skip if not available for this group.
            //
            String groupNameString = getGroupNameString(group, groupNamingAttribute);
            if (groupNameString == null)
            {
                continue;
            }

            //
            // Iterate over the members in the group.
            //
            Set<String> grouperMemberships = new HashSet<String>();
            for (Member member : (Set<Member>) group.getMembers())
            {
                //
                // Look for subject in the directory
                //
                Name subjectDn = getSubjectDn(member);
                if (subjectDn != null)
                {
                    //
                    // Add subject DN to members and remove it from the list of
                    // existing subjects.
                    //
                    grouperMemberships.add(subjectDn.toString());
                    existingSubjectDns.remove(subjectDn);
                }
            }

            //
            // Get the membership synchronizer and synchronize by writing
            // updates to the updates file.
            //
            GroupStringMembershipSynchronizer synchronizer = new GroupStringMembershipSynchronizer(ldapCtx,
                    groupNameString, updatesWriter, configuration, options, getSubjectCache());
            try
            {
                synchronizer.synchronize(grouperMemberships);
            }
            catch (Exception e)
            {
                logThrowableError(e, "Unable to synchronize memberships for " + group.getName());
                caughtExceptions
                        .add(new LdappcException("Unable to synchronize memberships for " + group.getName(), e));
                continue;
            }
        }

        //
        // Close the memberships file.
        //
        try
        {
            updatesWriter.close();
        }
        catch (IOException e)
        {
            throw new LdappcRuntimeException("Unable to close membershps file", e);
        }

        //
        // Sort the memberships file.
        //
        try
        {
            ExternalSort.sort(updatesFile.getAbsolutePath(), SORT_BATCH_SIZE);
        }
        catch (IOException e)
        {
            throw new LdappcRuntimeException("Unable to sort membershps file", e);
        }

        //
        // Read the updates and make the changes to the LDAP objects.
        //
        performActualMembershipUpdates(updatesFile, existingObjectDns);

        //
        // Clear the memberships from any subject not processed above.
        //
        try
        {
            // DebugLog.info("Clearing old memberships");
            clearSubjectEntryMemberships(existingSubjectDns);
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
            throw new MultiErrorException((Exception[]) caughtExceptions.toArray(new Exception[0]));
        }
    }

    /**
     * Get the membership value to be provisioned into an LDAP membership for a
     * group.
     * 
     * @param group
     *            Group for which to get membership string.
     * @param groupNamingAttribute
     *            attribute name to be provisioned as a membership.
     * @return value of the membership attribute.
     */
    private String getGroupNameString(Group group, String groupNamingAttribute)
    {
        String groupNameString = null;

        try
        {
            groupNameString = group.getAttribute(groupNamingAttribute);
            if (groupNameString == null)
            {
                String errorData = getErrorData(null, null, null);
                Throwable e = new LdappcRuntimeException("Group " + group.getName() + " has no " + groupNamingAttribute
                        + " attribute and cannot be provisioned as a membership");
                logThrowableWarning(e, errorData);
            }
        }
        catch (AttributeNotFoundException e1)
        {
            Throwable e = new LdappcRuntimeException("Group " + group.getName() + " has no " + groupNamingAttribute
                    + " attribute and cannot be provisioned as a membership", e1);
            logThrowableWarning(e, "");
        }

        return groupNameString;
    }

    /**
     * Get the subjectDn for a member. Remove the subject from the existing
     * subjectDns set.
     * 
     * @param member
     *            Member for which to retrieve subject DN
     * @return the subject DN for the Member.
     */
    private Name getSubjectDn(Member member)
    {
        Name subjectDn = null;

        try
        {
            // Must use alternate mechanism to get subject if search
            // attr is not the subject ID. Alternate method hits the
            // subject API and is rather slower.
            if ("id".equals(configuration.getSourceSubjectNamingAttribute(member.getSubjectSourceId())))
            {
                subjectDn = getSubjectCache().findSubjectDn(ldapCtx, configuration, member.getSubjectSourceId(), member
                        .getSubjectId());
            }
            else
            {
                Subject subject = member.getSubject();
                subjectDn = getSubjectCache().findSubjectDn(ldapCtx, configuration, subject);
            }
        }
        catch (Exception e)
        {
            //
            // If not found, simply log the error and return null.
            //
            Subject subject = null;
            try
            {
                subject = member.getSubject();
            }
            catch (SubjectNotFoundException e1)
            {
                // Ignore exception.
            }
            String errorData = getErrorData(member, subject, null);
            logThrowableWarning(e, errorData);
        }

        return subjectDn;
    }

    /**
     * Generate the updates file name and return a File.
     * 
     * @return File for membership updates.
     */
    private File getUpdatesFile()
    {
        //
        // Generate random filename.
        //
        String filename = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();

        //
        // Get the directory for the temporary file. Make sure it exists and is
        // a directory. Update the filename accordingly.
        //
        String tempDir = configuration.getMemberGroupsListTemporaryDirectory();
        if (tempDir != null)
        {
            File tempFile = new File(tempDir);
            if (!tempFile.exists())
            {
                tempFile.mkdirs();
            }
            else if (!tempFile.isDirectory())
            {
                throw new LdappcConfigurationException("Temporary directory " + tempDir + " is not a directory");
            }
            filename = tempDir + "/" + filename;
        }
        File updatesFile = new File(filename);
        return updatesFile;
    }

    /**
     * Read the updates from the file and perform the updates in LDAP.
     * 
     * @param updatesFile
     *            File containing the updates.
     * @param existingObjectDns
     *            Set containing subject DNs that have the list object class.
     */
    private void performActualMembershipUpdates(File updatesFile, Set<String> existingObjectDns)
    {
        //
        // Re-open the sorted memberships file for reading.
        //
        BufferedReader updatesReader = openMembershipReader(updatesFile);

        //
        // Read the memberships from the file, batching by subject DN.
        // Synchronize the memberships for each subject.
        //
        // DebugLog.info("Beginning memberships updates");
        try
        {
            Set<String> adds = new HashSet<String>();
            Set<String> dels = new HashSet<String>();
            Set<String> reps = new HashSet<String>();

            String currentSubjectDn = null;
            for (String s = null; (s = updatesReader.readLine()) != null;)
            {
                String[] parts = s.split("\t");
                String subjectDn = parts[0];
                String modOp = parts[1];
                String groupNameString = parts[2];

                if (!subjectDn.equals(currentSubjectDn))
                {
                    if (currentSubjectDn != null)
                    {
                        updateSubject(currentSubjectDn, !existingObjectDns.contains(subjectDn), adds, dels, reps);
                    }
                    adds.clear();
                    dels.clear();
                    reps.clear();
                    currentSubjectDn = subjectDn;
                }

                if ("1".equals(modOp))
                {
                    adds.add(groupNameString);
                }
                else if ("2".equals(modOp))
                {
                    reps.add(groupNameString);
                }
                else
                {
                    dels.add(groupNameString);
                }
            }
            if (currentSubjectDn != null)
            {
                updateSubject(currentSubjectDn, !existingObjectDns.contains(currentSubjectDn), adds, dels, reps);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            //
            // Close and delete the memberships file.
            //
            updatesReader.close();
            updatesFile.delete();
        }
        catch (IOException e)
        {
            throw new LdappcRuntimeException("IOException reading membership file", e);
        }
    }

    /**
     * Update memberships for an individual subject in LDAP, given memberships
     * to add, delete, or replace. The replacements will not be present if there
     * are any adds or deletes.
     * 
     * @param objectDN
     *            Subject object to update.
     * @param addMemberObjectClass
     *            <code>true</code> if subject needs list object class added.
     * @param adds
     *            Set of memberships to add.
     * @param dels
     *            Set of memberships to delete.
     * @param reps
     *            Set of memberships to replace existing memberships with.xd
     */
    private void updateSubject(String objectDN, boolean addMemberObjectClass, Set<String> adds, Set<String> dels,
            Set<String> reps)
    {
        if (adds.size() == 0 && dels.size() == 0 && reps.size() == 0)
        {
            return;
        }

        int size = (addMemberObjectClass ? 1 : 0) + (adds.size() > 0 ? 1 : 0) + (dels.size() > 0 ? 1 : 0)
                + (reps.size() > 0 ? 1 : 0);

        ModificationItem[] modItems = new ModificationItem[size];
        int modIndex = 0;

        String listAttribute = configuration.getMemberGroupsListAttribute();
        String listObjectClass = configuration.getMemberGroupsListObjectClass();

        if (addMemberObjectClass)
        {
            Attribute attrs = new BasicAttribute(LdapUtil.OBJECT_CLASS_ATTRIBUTE);
            attrs.add(listObjectClass);
            modItems[modIndex++] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs);
        }
        if (adds.size() > 0)
        {
            Attribute attrs = new BasicAttribute(listAttribute);
            for (String groupName : adds)
            {
                attrs.add(groupName);
            }
            modItems[modIndex++] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs);
        }
        if (dels.size() > 0)
        {
            Attribute attrs = new BasicAttribute(listAttribute);
            for (String groupName : dels)
            {
                attrs.add(groupName);
            }
            modItems[modIndex++] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrs);
        }
        if (reps.size() > 0)
        {
            Attribute attrs = new BasicAttribute(listAttribute);
            for (String groupName : reps)
            {
                attrs.add(groupName);
            }
            modItems[modIndex++] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs);
        }

        try
        {
            LOG.debug("update subject '" + objectDN + "' " + Arrays.asList(modItems));
            ldapCtx.modifyAttributes(objectDN, modItems);
        }
        catch (NamingException e)
        {
            e.printStackTrace();
            System.out.println("Printing ModItems array:");
            for (ModificationItem modItem : modItems)
            {
                System.out.println("op = " + modItem.getModificationOp() + ", id = " + modItem.getAttribute().getID());
                Attribute attr = modItem.getAttribute();
                for (int i = 0; i < attr.size(); i++)
                {
                    try
                    {
                        System.out.println("    " + attr.get(i));
                    }
                    catch (NamingException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            System.out.println("Done printing ModItems array:");
        }
    }

    /**
     * Open the membership file for writing.
     * 
     * @param membershipsFile
     *            File to write memberships to.
     * @return BufferedWriter for the file.
     * @throws LdappcRuntimeException
     *             thrown if the file cannot be opened.
     */
    private BufferedWriter openMembershipWriter(File membershipsFile) throws LdappcRuntimeException
    {
        BufferedWriter membershipsWriter = null;
        try
        {
            membershipsWriter = new BufferedWriter(new FileWriter(membershipsFile));
        }
        catch (Exception e)
        {
            membershipsWriter = null;
            throw new LdappcRuntimeException("Unable to open membership file: " + membershipsFile, e);
        }
        return membershipsWriter;
    }

    /**
     * Open the membership file for reading.
     * 
     * @param membershipsFile
     *            File to read memberships from.
     * 
     * @return BufferedReader for the file.
     * 
     * @throws LdappcRuntimeException
     *             thrown if the file cannot be opened.
     */
    private BufferedReader openMembershipReader(File membershipsFile) throws LdappcRuntimeException
    {
        BufferedReader membershipsReader = null;
        try
        {
            membershipsReader = new BufferedReader(new FileReader(membershipsFile));
        }
        catch (FileNotFoundException e)
        {
            throw new LdappcRuntimeException("Unable to open membership file", e);
        }
        return membershipsReader;
    }

    /**
     * Returns a Membership Synchronizer based on the configuration and options.
     * 
     * @param subjectDn
     *            Subject DN
     * 
     * @return Membership synchronizer
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private MembershipSynchronizer getMembershipSynchronizer(String subjectDn) throws NamingException
    {
        //
        // Only one type now but this allows for building others based on
        // configuration or options
        //
        return new StringMembershipSynchronizer(ldapCtx, subjectDn, configuration, options, getSubjectCache());
    }

    /**
     * Builds the subject DN set. The subject DN set is the union of each set
     * created from each source id using the subject source identifier Ldap
     * filter provided by the <code>configuration</code>. Each entry that is
     * identified by the filter and that has the member group listing attribute
     * populated is included in the subject DN set.
     * 
     * @param subjectDns
     *            Set of subject DNs to populate
     * @param subjectObjectDns
     *            Set of DNs containing list object class to populate
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    protected void buildSourceSubjectDnSet(Set<Name> subjectDns, Set<String> subjectObjectDns) throws NamingException
    {
        //
        // Get the source to subject ldap filter mapping from the configuration
        //
        Map<String, LdapSearchFilter> sourceFilterMap = configuration.getSourceSubjectLdapFilters();

        //
        // Iterate over the sourceFilterMap to build list of subjects for each
        // source
        //
        for (LdapSearchFilter filter : sourceFilterMap.values())
        {
            //
            // Add the subjectDns for this source
            //
            addSubjectDnSet(subjectDns, subjectObjectDns, filter);
        }
    }

    /**
     * Adds identified subject DNs the given set of subject DNs. The subject DN
     * set added is created by identifying all DNs satisfying the
     * LdapSearchFilter and having the listing attribute populated.
     * 
     * @param subjectDns
     *            Set of subject DNs
     * @param subjectObjectDns
     *            Set of DNs with list object class
     * @param filter
     *            Ldap search filter defined for a source
     * @throws NamingException
     *             thrown if a Naming error occurs.
     */
    private void addSubjectDnSet(Set<Name> subjectDns, Set<String> subjectObjectDns, LdapSearchFilter filter) throws NamingException
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
            throw new LdappcConfigurationException("Member groups list attribute is null");
        }

        String listObjectClass = configuration.getMemberGroupsListObjectClass();

        //
        // Update the ldap query filter by replacing "{0}" from the ldap search
        // filter with "*"
        //
        String ldapFilter = LdapUtil.convertParameterToAsterisk(filter.getFilter(), 0);

        //
        // Build the actual filter expression for performing the search
        //
        String filterExpr = ldapFilter;
        if (listObjectClass == null)
        {
            filterExpr = filterExpr + "(" + listAttribute + "=*)";
        }
        else
        {
            String subExpr = "(" + listAttribute + "=*)";
            subExpr = subExpr + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "=" + listObjectClass + ")";
            filterExpr = filterExpr + "(|" + subExpr + ")";
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
        NamingEnumeration searchResults = ldapCtx.search(baseDn, filterExpr, searchControls);

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

            boolean hasObjectClass = true;
            if (listObjectClass != null)
            {
                hasObjectClass = searchResult.getAttributes().get(LdapUtil.OBJECT_CLASS_ATTRIBUTE).contains(
                        listObjectClass);
            }

            if (searchResult.getAttributes().get(listAttribute) != null)
            {
                //
                // Save the subject dn
                //
                subjectDns.add(subjectDn);
            }
            if (hasObjectClass)
            {
                subjectObjectDns.add(subjectDn.toString());
            }
        }
    }

    /**
     * Clears the membership listings from subject entries.
     * 
     * @param subjectDnSet
     *            Set of subject DNs whose memberships are to be cleared
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private void clearSubjectEntryMemberships(Set<Name> subjectDnSet) throws NamingException
    {
        //
        // Define an empty set that is used below
        //
        Set<String> emptySet = new HashSet<String>();

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
                MembershipSynchronizer synchronizer = getMembershipSynchronizer(subjectDn.toString());
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
            errorData += "[ SUBJECT " + getSubjectCache().getSubjectData(subject) + " ]";
        }
        if (subjectDn != null)
        {
            errorData += "[ SUBJECT DN = " + subjectDn + " ]";
        }

        return errorData;
    }

    /**
     * Returns member data string.
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
            memberData = "[ UUID = " + member.getUuid() + " ][ SUBJECT ID = " + member.getSubjectId()
                    + " ][ SUBJECT SOURCE ID = " + member.getSubjectSourceId() + " ]";
        }
        return memberData;
    }

    /**
     * Returns group data string.
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
            grpData = "[ DISPLAY NAME = " + group.getDisplayName() + " ][NAME = " + group.getName() + "][UID = "
                    + group.getUuid() + "]";
        }
        return grpData;
    }
}
