/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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
import java.util.Date;
import java.util.HashSet;
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

import org.doomdark.uuid.UUIDGenerator;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
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
import edu.internet2.middleware.ldappc.exception.ConfigurationException;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.synchronize.GroupEntrySynchronizer;
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
public class Provisioner {

  private static final Logger LOG = GrouperUtil.getLogger(Provisioner.class);

  /**
   * Number of records of membership updates to sort in memory. This value (200,000) is a
   * good compromise between speed and memory.
   */
  private static final int SORT_BATCH_SIZE = 200000;

  /**
   * Provisioning configuration.
   */
  private ProvisionerConfiguration configuration;

  /**
   * Provisioning options.
   */
  private ProvisionerOptions options;

  /**
   * LDAP context for provisioning.
   */
  private LdapContext ldapCtx;

  /**
   * Subject cache to eliminate extra LDAP lookups.
   */
  private SubjectCache subjectCache;

  /**
   * Constructs a <code>GrouperProvisioner</code> with the given provisioning
   * configuration, options and Ldap context.
   * 
   * @param configuration
   *          GrouperProvisionerConfiguration providing all configuration data.
   * @param options
   *          GrouperProvisionerOptions providing values for the provisioning options.
   * @param ldapCtx
   *          the Ldap context to use for provisioning
   * @param subjectCache
   *          Subject cache to speed subject retrieval
   */
  public Provisioner(ProvisionerConfiguration configuration, ProvisionerOptions options,
      LdapContext ldapCtx) {
    this.configuration = configuration;
    this.options = options;
    this.ldapCtx = ldapCtx;
    subjectCache = new SubjectCache(this);
  }

  /**
   * This provisions Grouper data to a directory. This uses provisioning options to
   * determine the Grouper data to provision to the directory, and the provisioning
   * configuration to determine how the data is represented in the directory.
   * 
   * @throws edu.internet2.middleware.grouper.SessionException
   *           thrown if a Grouper session error occurs
   * @throws edu.internet2.middleware.grouper.SchemaException
   *           thrown if a Grouper Registry schema error occurs
   * @throws javax.naming.NamingException
   *           thrown if an error occured interacting with the directory.
   * @throws edu.internet2.middleware.grouper.AttributeNotFoundException
   *           thrown if a group attribute is not found.
   */
  public void provision() throws QueryException, SchemaException, NamingException,
      AttributeNotFoundException, SessionException, LdappcException {

    Subject subject = SubjectFinder.findById(options.getSubjectId(), false);

    GrouperSession grouperSession = GrouperSession.start(subject);

    //
    // Try to provision data
    //
    try {
      //
      // Find the set of Groups to be provisioned
      //
      Set<Group> groups = buildGroupSet(grouperSession);

      //
      // If provisioning Groups, do so
      //
      if (options.getDoGroups()) {
        provisionGroups(groups);
      }

      //
      // If provisioning memberships do so
      //
      if (options.getDoMemberships()) {
        provisionMemberships(groups);
      }
    } finally {
      //
      // Stop the Grouper session; log any errors
      //
      grouperSession.stop();
    }
  }

  /**
   * This builds the set of Groups to be provisioned.
   * 
   * @param session
   *          Grouper session for querying Grouper
   * @return {@link java.util.Set} of Groups, possibly empty, to be provisioned.
   * @throws edu.internet2.middleware.grouper.QueryException
   *           thrown if a Grouper Query error occurs
   */
  protected Set<Group> buildGroupSet(GrouperSession session) throws QueryException {
    return buildQueryGroupList(session);
  }

  /**
   * This builds the set of Groups identified by the subordinate stem and attribute value
   * queries defined in the provisioning configuration.
   * 
   * @param session
   *          Grouper session for querying Grouper
   * @return {@link java.util.Set} of Groups, possibly empty, matching the defined
   *         subordinate stem and attribute value queries.
   * @throws QueryException
   *           thrown if grouper query can't be constructed.
   */
  protected Set<Group> buildQueryGroupList(GrouperSession session) throws QueryException {
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
    for (Map.Entry<String, Set<String>> entry : attrValueMap.entrySet()) {
      //
      // Get the attribute name and set of values
      //
      String name = entry.getKey();
      for (String value : entry.getValue()) {
        groupFilter = new UnionFilter(groupFilter, new GroupAttributeExactFilter(name,
            value, rootStem));
      }
    }

    //
    // Build the sub-stem query filters
    //
    Set<String> subStems = configuration.getGroupSubordinateStemQueries();
    for (String stemName : subStems) {
      try {
        Stem stem = StemFinder.findByName(session, stemName, true);
        groupFilter = new UnionFilter(groupFilter, new ChildGroupFilter(stem));
      } catch (StemNotFoundException snfe) {
        LOG.error(snfe.getMessage(), snfe);
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
   *          Set of Groups to be provisioned
   * 
   * @throws javax.naming.NamingException
   *           thrown if an error occured interacting with the directory.
   * @throws ConfigurationException
   *           thrown if an Ldappc configuration error occurs
   * @throws LdappcException
   *           thrown if an error occurs
   */
  protected void provisionGroups(Set groups) throws NamingException, LdappcException {

    //
    // Synchronize the root
    //
    GroupEntrySynchronizer synchronizer = new GroupEntrySynchronizer(this);
    synchronizer.synchronize(groups);
  }

  /**
   * Provision the memberships from a set of Groups.
   * 
   * @param groups
   *          Set of Groups to be provisioned
   * 
   * @throws NamingException
   *           thrown if an error occured interacting with the directory.
   * @throws MultiErrorException
   *           thrown if we catch exceptions while doing provisioning.
   */
  protected void provisionMemberships(Set<Group> groups) throws NamingException {
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
    buildSourceSubjectDnSet(existingSubjectDns);
    LOG.debug("found " + existingSubjectDns.size() + " existing subjectDns");

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
    if (groupNamingAttribute == null) {
      throw new ConfigurationException("The name of the group naming attribute is null.");
    }

    //
    // Iterate over the groups.
    //
    for (Group group : groups) {
      //
      // Get the value corresponding to the group to be provisioned as a
      // membership. Skip if not available for this group.
      //
      String groupNameString = getGroupNameString(group, groupNamingAttribute);
      if (groupNameString == null) {
        continue;
      }

      //
      // Iterate over the members in the group.
      //
      for (Member member : group.getMembers()) {
        //
        // Look for subject in the directory
        //
        Name subjectDn = getSubjectDn(member);
        if (subjectDn != null) {
          //
          // Add subject DN to members and remove it from the list of
          // existing subjects.
          //
          try {
            updatesWriter.write(subjectDn.toString() + "\t" + groupNameString + "\n");
          } catch (IOException e) {
            caughtExceptions.add(e);
          }
          existingSubjectDns.remove(subjectDn);
        }
      }
    }

    //
    // Close the memberships file.
    //
    try {
      updatesWriter.close();
    } catch (IOException e) {
      throw new LdappcException("Unable to close membershps file", e);
    }

    //
    // Sort the memberships file.
    //
    try {
      ExternalSort.sort(updatesFile.getAbsolutePath(), SORT_BATCH_SIZE);
    } catch (IOException e) {
      throw new LdappcException("Unable to sort membershps file", e);
    }

    //
    // Read the updates and make the changes to the LDAP objects.
    //
    performActualMembershipUpdates(updatesFile);

    //
    // Clear the memberships from any subject not processed above.
    //
    LOG.debug("Clearing old memberships");
    clearSubjectEntryMemberships(existingSubjectDns);

  }

  /**
   * Get the membership value to be provisioned into an LDAP membership for a group.
   * 
   * @param group
   *          Group for which to get membership string.
   * @param groupNamingAttribute
   *          attribute name to be provisioned as a membership.
   * @return value of the membership attribute.
   */
  private String getGroupNameString(Group group, String groupNamingAttribute) {
    String groupNameString = null;

    try {
      groupNameString = group.getAttributeOrFieldValue(groupNamingAttribute, false, true);
      if (groupNameString == null) {
        String errorData = getErrorData(null, null, null);
        Throwable e = new LdappcException("Group " + group.getName() + " has no "
            + groupNamingAttribute
            + " attribute and cannot be provisioned as a membership");
        LOG.error(errorData, e);
      }
    } catch (AttributeNotFoundException e1) {
      Throwable e = new LdappcException(
          "Group " + group.getName() + " has no " + groupNamingAttribute
              + " attribute and cannot be provisioned as a membership", e1);
      LOG.error("Attribute not found", e);
    }

    return groupNameString;
  }

  /**
   * Get the subjectDn for a member. Remove the subject from the existing subjectDns set.
   * 
   * @param member
   *          Member for which to retrieve subject DN
   * @return the subject DN for the Member.
   */
  private Name getSubjectDn(Member member) {
    Name subjectDn = null;

    try {
      // Must use alternate mechanism to get subject if search
      // attr is not the subject ID. Alternate method hits the
      // subject API and is rather slower.
      if ("id".equals(configuration.getSourceSubjectNamingAttribute(member
          .getSubjectSourceId()))) {
        subjectDn = getSubjectCache().findSubjectDn(member.getSubjectSourceId(),
            member.getSubjectId());
      } else {
        Subject subject = member.getSubject();
        subjectDn = getSubjectCache().findSubjectDn(subject);
      }
    } catch (Exception e) {
      //
      // If not found, simply log the error and return null.
      //
      Subject subject = null;
      try {
        subject = member.getSubject();
      } catch (SubjectNotFoundException e1) {
        // Ignore exception.
      }
      String errorData = getErrorData(member, subject, null);
      LOG.warn(errorData, e);
      // logThrowableWarning(e, errorData);
    }

    return subjectDn;
  }

  /**
   * Generate the updates file name and return a File.
   * 
   * @return File for membership updates.
   */
  private File getUpdatesFile() {
    //
    // Generate random filename.
    //
    String filename = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();

    //
    // Get the directory for the temporary file. Make sure it exists and is
    // a directory. Update the filename accordingly.
    //
    String tempDir = configuration.getMemberGroupsListTemporaryDirectory();
    if (tempDir != null) {
      File tempFile = new File(tempDir);
      if (!tempFile.exists()) {
        tempFile.mkdirs();
      } else if (!tempFile.isDirectory()) {
        throw new ConfigurationException("Temporary directory " + tempDir
            + " is not a directory");
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
   *          File containing the updates.
   * @param existingObjectDns
   *          Set containing subject DNs that have the list object class.
   */
  private void performActualMembershipUpdates(File updatesFile) {
    //
    // Re-open the sorted memberships file for reading.
    //
    BufferedReader updatesReader = openMembershipReader(updatesFile);

    //
    // Read the memberships from the file, batching by subject DN.
    // Synchronize the memberships for each subject.
    //
    // DebugLog.info("Beginning memberships updates");
    try {
      Set<String> groups = new HashSet<String>();

      String currentSubjectDn = null;
      for (String s = null; (s = updatesReader.readLine()) != null;) {
        String[] parts = s.split("\t", 2);
        String subjectDn = parts[0];
        String groupNameString = parts[1];

        if (!subjectDn.equals(currentSubjectDn)) {
          if (currentSubjectDn != null) {
            updateSubject(currentSubjectDn, groups);
          }
          currentSubjectDn = subjectDn;
          groups.clear();
        }
        groups.add(groupNameString);
      }
      if (currentSubjectDn != null) {
        updateSubject(currentSubjectDn, groups);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      //
      // Close and delete the memberships file.
      //
      updatesReader.close();
      if (!LOG.isDebugEnabled()) {
        updatesFile.delete();
      }
    } catch (IOException e) {
      throw new LdappcException("IOException reading membership file", e);
    }
  }

  /**
   * Update memberships for an individual subject in LDAP, given memberships to add,
   * delete, or replace. The replacements will not be present if there are any adds or
   * deletes.
   * 
   * @param objectDN
   *          Subject object to update.
   * @param addMemberObjectClass
   *          <code>true</code> if subject needs list object class added.
   * @param adds
   *          Set of memberships to add.
   * @param dels
   *          Set of memberships to delete.
   * @param reps
   *          Set of memberships to replace existing memberships with.xd
   */
  private void updateSubject(String objectDN, Set<String> groups) {
    try {
      //
      // Get a membership synchronizer and synchronize with
      // the subject's group memberships
      //
      LOG.debug("synchronizing memberships for '" + objectDN + "'");
      StringMembershipSynchronizer synchronizer = getMembershipSynchronizer(objectDN);
      synchronizer.synchronize(groups);
    } catch (Exception e) {
      LOG.error("An error occurred ", e);
    }
  }

  /**
   * Open the membership file for writing.
   * 
   * @param membershipsFile
   *          File to write memberships to.
   * @return BufferedWriter for the file.
   * @throws LdappcException
   *           thrown if the file cannot be opened.
   */
  private BufferedWriter openMembershipWriter(File membershipsFile)
      throws LdappcException {
    BufferedWriter membershipsWriter = null;
    try {
      membershipsWriter = new BufferedWriter(new FileWriter(membershipsFile));
    } catch (Exception e) {
      membershipsWriter = null;
      throw new LdappcException("Unable to open membership file: " + membershipsFile, e);
    }
    return membershipsWriter;
  }

  /**
   * Open the membership file for reading.
   * 
   * @param membershipsFile
   *          File to read memberships from.
   * 
   * @return BufferedReader for the file.
   * 
   * @throws LdappcException
   *           thrown if the file cannot be opened.
   */
  private BufferedReader openMembershipReader(File membershipsFile)
      throws LdappcException {
    BufferedReader membershipsReader = null;
    try {
      membershipsReader = new BufferedReader(new FileReader(membershipsFile));
    } catch (FileNotFoundException e) {
      throw new LdappcException("Unable to open membership file", e);
    }
    return membershipsReader;
  }

  /**
   * Returns a Membership Synchronizer based on the configuration and options.
   * 
   * @param subjectDn
   *          Subject DN
   * 
   * @return Membership synchronizer
   * @throws NamingException
   *           thrown if a Naming error occurs
   */
  private StringMembershipSynchronizer getMembershipSynchronizer(String subjectDn)
      throws NamingException {
    //
    // Only one type now but this allows for building others based on
    // configuration or options
    //
    return new StringMembershipSynchronizer(this, subjectDn);
  }

  /**
   * Builds the subject DN set. The subject DN set is the union of each set created from
   * each source id using the subject source identifier Ldap filter provided by the
   * <code>configuration</code>. Each entry that is identified by the filter and that has
   * the member group listing attribute populated is included in the subject DN set.
   * 
   * @param subjectDns
   *          Set of subject DNs to populate
   * @param subjectObjectDns
   *          Set of DNs containing list object class to populate
   * @throws NamingException
   *           thrown if a Naming error occurs
   */
  protected void buildSourceSubjectDnSet(Set<Name> subjectDns) throws NamingException {
    //
    // Get the source to subject ldap filter mapping from the configuration
    //
    Map<String, LdapSearchFilter> sourceFilterMap = configuration
        .getSourceSubjectLdapFilters();

    //
    // Iterate over the sourceFilterMap to build list of subjects for each
    // source
    //
    for (LdapSearchFilter filter : sourceFilterMap.values()) {
      //
      // Add the subjectDns for this source
      //
      addSubjectDnSet(subjectDns, filter);
    }
  }

  /**
   * Adds identified subject DNs the given set of subject DNs. The subject DN set added is
   * created by identifying all DNs satisfying the LdapSearchFilter and having the listing
   * attribute populated.
   * 
   * @param subjectDns
   *          Set of subject DNs
   * @param subjectObjectDns
   *          Set of DNs with list object class
   * @param filter
   *          Ldap search filter defined for a source
   * @throws NamingException
   *           thrown if a Naming error occurs.
   */
  private void addSubjectDnSet(Set<Name> subjectDns, LdapSearchFilter filter)
      throws NamingException {

    //
    // Get the member list attribute name and object class
    //
    String listAttribute = configuration.getMemberGroupsListAttribute();
    if (listAttribute == null) {
      throw new ConfigurationException("Member groups list attribute is null");
    }

    //
    // Build the search control
    //
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(filter.getScope());
    searchControls.setCountLimit(0);
    searchControls.setReturningAttributes(new String[] { listAttribute });

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
    if (listObjectClass == null) {
      filterExpr = filterExpr + "(" + listAttribute + "=*)";
    } else {
      String subExpr = "(" + listAttribute + "=*)";
      subExpr = subExpr + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "=" + listObjectClass
          + ")";
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
    LOG.debug("search base '" + baseDn + "' filter '" + filter + "' attrs "
        + Arrays.asList(searchControls.getReturningAttributes()));
    NamingEnumeration searchResults = ldapCtx.search(baseDn, filterExpr, searchControls);

    //
    // Process the search results
    //
    while (searchResults.hasMore()) {
      //
      // Get the search result
      //
      SearchResult searchResult = (SearchResult) searchResults.next();

      //
      // Build the DN for the search result
      //
      Name subjectDn = LdapUtil.getName(parser, searchResult);
      subjectDn = subjectDn.addAll(0, baseDn);

      if (searchResult.getAttributes().get(listAttribute) != null) {
        //
        // Save the subject dn
        //
        subjectDns.add(subjectDn);
      }
    }
  }

  /**
   * Clears the membership listings from subject entries.
   * 
   * @param subjectDnSet
   *          Set of subject DNs whose memberships are to be cleared
   * @throws NamingException
   *           thrown if a Naming error occurs
   */
  private void clearSubjectEntryMemberships(Set<Name> subjectDnSet)
      throws NamingException {
    //
    // Define an empty set that is used below
    //
    Set<String> emptySet = new HashSet<String>();

    //
    // Iterate over the subject DNs
    //
    for (Name subjectDn : subjectDnSet) {
      try {
        //
        // Get a membership synchronizer and synchronize with an empty
        // set.
        // (Doing it this way ensures that required attributes are
        // handled correctly).
        //
        StringMembershipSynchronizer synchronizer = getMembershipSynchronizer(subjectDn
            .toString());
        synchronizer.synchronize(emptySet);
      } catch (Exception e) {
        LOG.error("An error occurred ", e);
      }
    }
  }

  /**
   * Builds an error data string based on the objects provided. It is <b>assumed</b> that
   * all three objects are related.
   * 
   * @param member
   *          Group member
   * @param subject
   *          Subject associated with <code>member</code>
   * @param subjectDn
   *          DN of <code>subject</code>'s LDAP entry
   * @return data string
   */
  protected String getErrorData(Member member, Subject subject, Name subjectDn) {
    String errorData = "MEMBER";

    if (member != null) {
      errorData += getMemberData(member);
    }
    if (subject != null) {
      errorData += "[ SUBJECT " + getSubjectCache().getSubjectData(subject) + " ]";
    }
    if (subjectDn != null) {
      errorData += "[ SUBJECT DN = " + subjectDn + " ]";
    }

    return errorData;
  }

  /**
   * Returns member data string.
   * 
   * @param member
   *          Member
   * @return member data string
   */
  public static String getMemberData(Member member) {
    String memberData = "null";
    if (member != null) {
      memberData = "[ UUID = " + member.getUuid() + " ][ SUBJECT ID = "
          + member.getSubjectId() + " ][ SUBJECT SOURCE ID = "
          + member.getSubjectSourceId() + " ]";
    }
    return memberData;
  }

  /**
   * Returns group data string.
   * 
   * @param group
   *          Group
   * @return group data string
   */
  public static String getGroupData(Group group) {
    String grpData = "null";
    if (group != null) {
      grpData = "[ DISPLAY NAME = " + group.getDisplayName() + " ][NAME = "
          + group.getName() + "][UID = " + group.getUuid() + "]";
    }
    return grpData;
  }

  /**
   * Gets the subject cache.
   * 
   * @return the subject cache
   */
  public SubjectCache getSubjectCache() {
    return subjectCache;
  }

  /**
   * Get the Ldap context.
   * 
   * @return the LDAP context.
   */
  public LdapContext getContext() {
    return ldapCtx;
  }

  /**
   * Get the Grouper provisioner configuration.
   * 
   * @return Grouper provisioner configuration
   */
  public ProvisionerConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Get the Grouper provisioner options.
   * 
   * @return Grouper provisioner options
   */
  public ProvisionerOptions getOptions() {
    return options;
  }

  /**
   * Indicates the group is new since the last modification date.
   */
  public static final int STATUS_NEW = 0;

  /**
   * Indicates the group has been modified since the last modification date.
   */
  public static final int STATUS_MODIFIED = 1;

  /**
   * Indicates the group has not been modified since the last modification date.
   */
  public static final int STATUS_UNCHANGED = 2;

  /**
   * Indicates a last modification date was not provided so the group's status is unknown.
   */
  public static final int STATUS_UNKNOWN = 3;

  /**
   * Determines the status of the group based on the lastModifyTime provided in the
   * GrouperOptions.
   * 
   * @param group
   *          Group
   * @return Status of the group, either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
   *         {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
   */
  public int determineStatus(Group group) {

    Date lastModifyTime = options.getLastModifyTime();
    if (lastModifyTime == null) {
      return STATUS_UNKNOWN;
    }

    Date groupCreateTime = group.getCreateTime();
    if (groupCreateTime == null) {
      return STATUS_UNKNOWN;
    }

    if (lastModifyTime.before(groupCreateTime)) {
      return STATUS_NEW;
    }

    Date groupModifyTime = group.getModifyTime();
    if (groupModifyTime != null && lastModifyTime.before(groupModifyTime)) {
      return STATUS_MODIFIED;
    }

    // some weirdness occurs with getLastMembershipChange()
    if (group.getLastMembershipChange() != null) {
      Date memberModifyTime = new Date(group.getLastMembershipChange().getTime());
      if (lastModifyTime.before(memberModifyTime)) {
        return STATUS_MODIFIED;
      }
    }

    return STATUS_UNCHANGED;
  }
}
