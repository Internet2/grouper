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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.Rdn;

import org.apache.commons.cli.ParseException;
import org.apache.directory.shared.ldap.ldif.LdifUtils;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.ChildGroupFilter;
import edu.internet2.middleware.grouper.filter.GroupAttributeExactFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.NullFilter;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.filter.UnionFilter;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.ldap.OrganizationalUnit;
import edu.internet2.middleware.ldappc.synchronize.GroupEntrySynchronizer;
import edu.internet2.middleware.ldappc.synchronize.StringMembershipSynchronizer;
import edu.internet2.middleware.ldappc.util.ExternalSort;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.subject.Subject;

/**
 * Initiates provisioning.
 */
public final class Ldappc extends TimerTask {

  /**
   * Logger.
   */
  private static final Logger LOG = GrouperUtil.getLogger(Ldappc.class);

  /**
   * The command line options.
   */
  private LdappcOptions options;

  /**
   * The xml configuration.
   */
  private LdappcConfig configuration;

  /**
   * The ldap context.
   */
  private LdapContext ldapContext;

  /**
   * Subject cache to eliminate extra LDAP lookups.
   */
  private SubjectCache subjectCache;

  /**
   * The root DN used when calculating DNs.
   */
  protected Name rootDn;

  /**
   * The grouper session.
   */
  private GrouperSession grouperSession;

  /**
   * Number of records of membership updates to sort in memory. This value (200,000) is a
   * good compromise between speed and memory.
   */
  private static final int SORT_BATCH_SIZE = 200000;

  /**
   * Writer used during dryRun or calculate modes.
   */
  private BufferedWriter writer;

  public Ldappc(LdappcOptions options) {
    this(options, null, null);
  }

  public Ldappc(LdappcOptions options, LdappcConfig configuration, LdapContext ldapContext) {

    this.options = options;
    this.configuration = configuration;
    this.ldapContext = ldapContext;

    initialize();
  }

  public static void main(String[] args) {

    try {
      LOG.debug("Starting Ldappc with the following arguments: {}", Arrays.asList(args));

      LdappcOptions options = new LdappcOptions();

      try {
        if (args.length == 0) {
          options.printUsage();
          return;
        }
        options.init(args);
      } catch (ParseException e) {
        options.printUsage();
        System.err.println(e.getMessage());
        e.printStackTrace();
        return;
      } catch (java.text.ParseException e) {
        options.printUsage();
        System.err.println(e.getMessage());
        e.printStackTrace();
        return;
      }

      LOG.info("Starting Ldappc");

      Ldappc ldappc = new Ldappc(options);

      if (options.getInterval() == 0) {
        ldappc.run();
      } else {
        Timer timer = new Timer();
        timer.schedule(ldappc, 0, 1000 * options.getInterval());
      }

      LOG.info("End of Ldappc execution.");

    } catch (LdappcException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.TimerTask#run()
   */
  public void run() {

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LDAPPC, false, true);

    LOG.info("***** Starting Provisioning *****");

    Date now = new Date();

    try {

      if (LOG.isInfoEnabled()) {
        for (String source : configuration.getSourceSubjectHashEstimates().keySet()) {
          LOG.info("Estimate({}) = {}", source, configuration
              .getSourceSubjectHashEstimate(source));
        }
      }

      switch (options.getMode()) {

        case PROVISION:

          provision();
          break;

        case CALCULATE:

          calculate();
          break;

        case DRYRUN:

          dryRun();
          break;
      }

      options.setLastModifyTime(now);

      if (LOG.isInfoEnabled()) {
        int subjectIDLookups = getSubjectCache().getSubjectIdLookups();
        int subjectIDTableHits = getSubjectCache().getSubjectIdTableHits();
        LOG.info("Subject ID Lookups: {}", subjectIDLookups);
        LOG.info("Subject Table Hits: {}", subjectIDTableHits);
        // Compute hit ratio percent, rounded to nearest tenth percent.
        double ratio = Math.round(((double) subjectIDTableHits) / subjectIDLookups
            * 1000.0) / 10.0;
        LOG.info("Subject hit ratio: {} %", ratio);
      }

    } catch (Exception e) {
      LOG.error("Grouper Provision Failed", e);
      cancel();
    } finally {
      try {
        if (!(options.isTest()) && ldapContext != null) {
          ldapContext.close();
        }
      } catch (NamingException e) {
        // May have already been closed.
      }
    }
  }

  public void initialize() {

    if (configuration == null) {
      configuration = new ConfigManager(options.getConfigManagerLocation(), options
          .getPropertiesFileLocation());
    }

    try {
      if (ldapContext == null) {
        ldapContext = LdapUtil.getLdapContext(configuration.getLdapContextParameters(),
            null);
      }
    } catch (NamingException e) {
      throw new LdappcException(e);
    }

    subjectCache = new SubjectCache(this);

    //
    // Get the Name of the root ou
    //
    String rootDnStr = configuration.getGroupDnRoot();
    if (rootDnStr == null) {
      throw new ConfigurationException("Group root DN is not defined.");
    }

    try {
      rootDn = getContext().getNameParser(LdapUtil.EMPTY_NAME).parse(rootDnStr);
    } catch (NamingException e) {
      throw new ConfigurationException("Unable to parse root DN.", e);
    }

    GrouperStartup.startup();
    Subject subject = SubjectFinder.findById(options.getSubjectId(), true);
    grouperSession = GrouperSession.start(subject);
  }

  /**
   * Provision Grouper data to a directory.
   * 
   * @throws NamingException
   * @throws LdappcException
   * @throws IOException
   */
  public void provision() throws LdappcException, NamingException, IOException {

    //
    // Find the set of Groups to be provisioned
    //
    Set<Group> groups = buildGroupSet();

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

    //
    // Stop the Grouper session
    //
    grouperSession.stop();
  }

  /**
   * Calculate provisioning and write to a file. No changes are made to the target
   * directory.
   * 
   * @return File the ldif file
   * @throws IOException
   * @throws ConfigurationException
   * @throws NamingException
   */
  public File calculate() throws IOException, ConfigurationException, NamingException {

    File file = new File(options.getOutputFileLocation());

    if (!file.createNewFile()) {
      throw new LdappcException("File '" + options.getOutputFileLocation()
          + "' already exists.");
    }

    BufferedWriter writer = LdapUtil.openWriter(file);

    GroupEntrySynchronizer synchronizer = new GroupEntrySynchronizer(this);

    //
    // Find the set of Groups to be provisioned
    //
    Set<Group> groups = buildGroupSet();

    //
    // If provisioning Groups, do so
    //
    if (options.getDoGroups()) {

      // stems
      if (GroupDNStructure.bushy.equals(getConfig().getGroupDnStructure())) {
        Set<Name> stemDns = new TreeSet<Name>();
        for (Group group : groups) {
          stemDns.addAll(calculateStemDns(group));
        }
        for (Name stemDn : stemDns) {
          BasicAttributes attributes = calculateStemAttributes(stemDn);
          writer.write(LdifUtils.convertToLdif(attributes, new LdapDN(stemDn)) + "\n");
        }
      }

      // groups
      for (Group group : groups) {
        writer.write(synchronizer.calculateLdif(group) + "\n");
      }
    }

    //
    // If provisioning memberships do so
    //
    if (options.getDoMemberships()) {
      File membershipFile = buildMembershipFile(groups);
      parseMembershipUpdates(membershipFile, writer);
    }

    writer.close();

    //
    // Stop the Grouper session
    //
    grouperSession.stop();

    return file;
  }

  /**
   * Write provisioning changes to a file. No changes are made to the target directory.
   * 
   * @return
   * @throws IOException
   * @throws NamingException
   * @throws LdappcException
   */
  public File dryRun() throws IOException, NamingException, LdappcException {

    File file = new File(options.getOutputFileLocation());

    if (!file.createNewFile()) {
      throw new LdappcException("File '" + options.getOutputFileLocation()
          + "' already exists.");
    }

    writer = LdapUtil.openWriter(file);

    provision();

    writer.close();

    return file;
  }

  /**
   * This builds the set of Groups to be provisioned.
   * 
   * @return {@link java.util.Set} of Groups, possibly empty, to be provisioned.
   */
  private Set<Group> buildGroupSet() {

    //
    // Find the root stem for building filters
    //
    Stem rootStem = StemFinder.findRootStem(grouperSession);

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
        Stem stem = StemFinder.findByName(grouperSession, stemName, true);
        groupFilter = new UnionFilter(groupFilter, new ChildGroupFilter(stem));
      } catch (StemNotFoundException snfe) {
        LOG.error(snfe.getMessage(), snfe);
      }
    }

    //
    // Build and execute the query
    //
    GrouperQuery query = GrouperQuery.createQuery(grouperSession, groupFilter);

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
   * @throws LdappcException
   *           thrown if an error occurs
   */
  private void provisionGroups(Set groups) throws NamingException, LdappcException {

    //
    // Synchronize the root
    //
    GroupEntrySynchronizer synchronizer = new GroupEntrySynchronizer(this);
    synchronizer.synchronize(groups);
  }

  /**
   * Provision the memberships based on the given set of Groups.
   * 
   * @param groups
   *          Set of Groups to be provisioned
   * 
   * @throws NamingException
   *           thrown if an error occured interacting with the directory.
   * @throws IOException
   *           thrown if an error occurrs writing to the memberships file.
   */
  private void provisionMemberships(Set<Group> groups) throws NamingException,
      IOException {

    //
    // Build the set of all subjects with memberships
    //
    // DebugLog.info("Collecting existing subjects with memberships");
    Set<Name> existingSubjectDns = new HashSet<Name>();
    buildSourceSubjectDnSet(existingSubjectDns);
    LOG.debug("found " + existingSubjectDns.size() + " existing subjectDns");

    // 
    // Build membership updates file
    //
    File updatesFile = buildMembershipFile(groups);

    //
    // Read the updates and make the changes to the LDAP objects.
    //
    Set<Name> subjectDNs = performActualMembershipUpdates(updatesFile);

    //
    // Remove provisioned subjects from list of existing subjects
    //
    for (Name subjectDN : subjectDNs) {
      existingSubjectDns.remove(subjectDN);
    }

    //
    // Clear the memberships from any subject not processed above.
    //
    LOG.debug("Clearing old memberships");
    clearSubjectEntryMemberships(existingSubjectDns);
  }

  /**
   * File for writing memberships to when provisioning memberships. Each line of the file
   * contains tab-delimited data consisting of the subject DN and the group name string to
   * be provisioned. This can then be sorted and read, batched by subject DN, to
   * efficiently update the memberships for each subject.
   * 
   * @param groups
   * @return
   * @throws NamingException
   * @throws IOException
   */
  private File buildMembershipFile(Set<Group> groups) throws NamingException, IOException {

    File updatesFile = getTempFile();

    BufferedWriter updatesWriter = LdapUtil.openWriter(updatesFile);
    String groupNamingAttribute = configuration.getMemberGroupsNamingAttribute();
    if (groupNamingAttribute == null) {
      throw new ConfigurationException("The name of the group naming attribute is null.");
    }

    for (Group group : groups) {
      //
      // Get the value corresponding to the group to be provisioned as a
      // membership. Skip if not available for this group.
      //
      String groupNameString = getGroupNameString(group, groupNamingAttribute);
      if (groupNameString == null) {
        continue;
      }

      for (Member member : group.getMembers()) {
        Name subjectDn = subjectCache.findSubjectDn(member);
        if (subjectDn != null) {
          updatesWriter.write(subjectDn.toString() + "\t" + groupNameString + "\n");
          // existingSubjectDns.remove(subjectDn);
        }
      }
    }

    updatesWriter.close();

    ExternalSort.sort(updatesFile.getAbsolutePath(), SORT_BATCH_SIZE);

    return updatesFile;
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
   * Create a temporary file in the configured temporary directory.
   * 
   * @return File the file
   * @throws IOException
   */
  private File getTempFile() throws IOException {

    File tempFile = null;

    //
    // Get the directory for the temporary file. Make sure it exists and is
    // a directory. Update the filename accordingly.
    //
    String tempDir = configuration.getMemberGroupsListTemporaryDirectory();
    if (tempDir != null) {
      tempFile = new File(tempDir);
      if (!tempFile.exists()) {
        tempFile.mkdirs();
      } else if (!tempFile.isDirectory()) {
        throw new ConfigurationException("Temporary directory " + tempDir
            + " is not a directory");
      }
    }

    return File.createTempFile("ldappc", ".tmp", tempFile);
  }

  /**
   * Read the memberships file and write an LDIF representation to the given writer.
   * 
   * @param updatesFile
   * @param writer
   * @throws NamingException
   */
  private void parseMembershipUpdates(File updatesFile, BufferedWriter writer)
      throws NamingException {

    //
    // Re-open the sorted memberships file for reading.
    //
    BufferedReader updatesReader = LdapUtil.openReader(updatesFile);

    //
    // Read the memberships from the file, batching by subject DN.

    try {
      Set<String> groups = new TreeSet<String>();

      String currentSubjectDn = null;
      for (String s = null; (s = updatesReader.readLine()) != null;) {
        String[] parts = s.split("\t", 2);
        String subjectDn = parts[0];
        String groupNameString = parts[1];

        if (!subjectDn.equals(currentSubjectDn)) {
          if (currentSubjectDn != null) {
            StringMembershipSynchronizer synchronizer = getMembershipSynchronizer(currentSubjectDn);
            writer.write(synchronizer.calculateLdif(groups));
          }
          currentSubjectDn = subjectDn;
          groups.clear();
        }
        groups.add(groupNameString);
      }
      if (currentSubjectDn != null) {
        StringMembershipSynchronizer synchronizer = getMembershipSynchronizer(currentSubjectDn);
        writer.write(synchronizer.calculateLdif(groups));
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
   * Read the updates from the file and perform the updates in LDAP.
   * 
   * @param updatesFile
   *          File containing the updates.
   * @throws NamingException
   */
  private Set<Name> performActualMembershipUpdates(File updatesFile)
      throws NamingException {

    Set<Name> subjectDNs = new HashSet<Name>();

    NameParser parser = getContext().getNameParser(LdapUtil.EMPTY_NAME);

    //
    // Re-open the sorted memberships file for reading.
    //
    BufferedReader updatesReader = LdapUtil.openReader(updatesFile);

    //
    // Read the memberships from the file, batching by subject DN.
    // Synchronize the memberships for each subject.
    //
    try {
      Set<String> groups = new HashSet<String>();

      String currentSubjectDn = null;
      for (String s = null; (s = updatesReader.readLine()) != null;) {
        String[] parts = s.split("\t", 2);
        String subjectDn = parts[0];
        String groupNameString = parts[1];

        subjectDNs.add(parser.parse(subjectDn));

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

    return subjectDNs;
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
  private void buildSourceSubjectDnSet(Set<Name> subjectDns) throws NamingException {
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
    NameParser parser = getContext().getNameParser(LdapUtil.EMPTY_NAME);
    Name baseDn = parser.parse(filter.getBase());

    //
    // perform the search
    //
    LOG.debug("search base '" + baseDn + "' filter '" + filter + "' attrs "
        + Arrays.asList(searchControls.getReturningAttributes()));
    NamingEnumeration searchResults = getContext().search(baseDn, filterExpr,
        searchControls);

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
  private String getErrorData(Member member, Subject subject, Name subjectDn) {
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
    return ldapContext;
  }

  /**
   * Get the Grouper provisioner configuration.
   * 
   * @return Grouper provisioner configuration
   */
  public LdappcConfig getConfig() {
    return configuration;
  }

  /**
   * Get the Grouper provisioner options.
   * 
   * @return Grouper provisioner options
   */
  public LdappcOptions getOptions() {
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

  public Name getRootDn() {
    return rootDn;
  }

  /**
   * This calculates the DN of the given group.
   * 
   * @param group
   *          Group
   * @return DN for the associated LDAP entry
   * @throws NamingException
   *           thrown if a Naming error occurs.
   * @throws LdappcException
   *           thrown if the RDN attribute is not defined for the group.
   */
  public Name calculateGroupDn(Group group) throws NamingException, LdappcException {
    //
    // Initialize return value
    //
    Name groupDn = null;

    //
    // If DN structure is bushy, build stem Ou's and initialize the group DN
    // with the parent OU DN. Else, initialize the group DN with the root
    // DN.
    //
    if (GroupDNStructure.bushy.equals(configuration.getGroupDnStructure())) {
      groupDn = calculateStemDn(group);
    } else {
      groupDn = (Name) rootDn.clone();
    }

    //
    // Get the group's rdn value
    //
    String rdnString = null;
    if (GroupDNStructure.flat.equals(configuration.getGroupDnStructure())) {
      if (LdappcConfig.GROUPER_NAME_ATTRIBUTE.equals(configuration
          .getGroupDnGrouperAttribute())) {
        rdnString = group.getName();
      } else {
        String attr = group.getAttributeOrFieldValue(configuration
            .getGroupDnGrouperAttribute(), true, false);
        if (attr != null) {
          rdnString = attr;
        } else {
          rdnString = group.getUuid();
        }
      }
    } else {
      //
      // Structure must be bushy so use the extension
      //
      rdnString = group.getExtension();
    }

    //
    // Add the rdn to the group Dn
    //
    groupDn = groupDn.add(configuration.getGroupDnRdnAttribute() + "="
        + LdapUtil.makeLdapNameSafe(rdnString));

    return groupDn;
  }

  /**
   * Calculates the group's parent OU DN.
   * 
   * @param group
   *          Group
   * @return OU DN under which the group entry must be created.
   * @throws javax.naming.NamingException
   *           thrown if a Naming exception occured.
   */
  public Name calculateStemDn(Group group) throws NamingException {

    List<Name> names = calculateStemDns(group);

    return names.get(names.size() - 1);
  }

  /**
   * Calculates all parent OU DNs for the given group.
   * 
   * @param group
   * @return a List of OU DNs
   * @throws NamingException
   */
  public List<Name> calculateStemDns(Group group) throws NamingException {

    ArrayList<Name> names = new ArrayList<Name>();

    //
    // Initialize the stemDn to be the root DN. This stemDn
    // is updated for each element of the group's stem below
    //
    Name stemDn = (Name) rootDn.clone();

    //
    // Get the group's parent stem, and tokenize it's name to build
    // the ou's for the group.
    //
    Stem stem = group.getParentStem();
    StringTokenizer stemTokens = new StringTokenizer(stem.getName(), Stem.DELIM);
    while (stemTokens.hasMoreTokens()) {
      //
      // Get next stem token for the rdn value making sure it is Ldap name
      // safe
      //
      String rdnString = stemTokens.nextToken();
      String rdnValue = LdapUtil.makeLdapNameSafe(rdnString);

      //
      // Build the new name (keep adding on to previous)
      //
      stemDn = stemDn.add(OrganizationalUnit.Attribute.OU + "=" + rdnValue);

      names.add(stemDn);
    }

    return names;
  }

  /**
   * Calculate a stem's attributes, essentially ou = stem name.
   * 
   * @param stemDn
   *          the DN of the stem
   * @return the stem's attributes
   * @throws InvalidNameException
   */
  public BasicAttributes calculateStemAttributes(Name stemDn) throws InvalidNameException {

    BasicAttributes attributes = new BasicAttributes(true);
    attributes.put(new BasicAttribute(LdapUtil.OBJECT_CLASS_ATTRIBUTE,
        OrganizationalUnit.OBJECT_CLASS));

    Rdn rdn = new Rdn(stemDn.get(stemDn.size() - 1));
    attributes.put(OrganizationalUnit.Attribute.OU, rdn.getValue().toString());

    return attributes;
  }

  public BufferedWriter getWriter() {
    return writer;
  }
}
