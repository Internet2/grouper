package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import org.apache.commons.lang.StringUtils;
import org.ldaptive.*;
import org.ldaptive.io.LdifReader;

import edu.internet2.middleware.subject.Subject;
import static edu.internet2.middleware.grouper.pspng.PspUtils.*;



/**
 * This class is the workhorse for provisioning LDAP groups from
 * grouper.
 *
 * @author bert
 *
 */
public class LdapGroupProvisioner extends LdapProvisioner<LdapGroupProvisionerConfiguration> {

  public LdapGroupProvisioner(String provisionerName, LdapGroupProvisionerConfiguration config, boolean fullSyncMode) {
    super(provisionerName, config, fullSyncMode);

    LOG.debug("Constructing LdapGroupProvisioner: {}", provisionerName);
  }

  public static Class<? extends ProvisionerConfiguration> getPropertyClass() {
    return LdapGroupProvisionerConfiguration.class;
  }


  @Override
  protected void addMembership(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup,
      Subject subject, LdapUser ldapUser) throws PspException {

    // TODO: Look in memory cache to see if change is necessary:
    // a) User object's group-listing attribute
    // or b) if the group-membership attribute is being fetched

    if ( ldapUser == null && config.needsTargetSystemUsers() ) {
      LOG.warn("{}: Skipping adding membership to group {} because ldap user does not exist: {}",
          new Object[]{getDisplayName(), grouperGroupInfo, subject});
      return;
    }

    if ( ldapGroup == null ) {
      // Create the group if it hasn't been created yet. List the user so that creation can be combined
      // with membership addition

      // This will normally occur when the schema requires members and group-creation is delayed until
      // the this method is being called to add the first member
      ldapGroup = createGroup(grouperGroupInfo, Arrays.asList(subject));
      cacheGroup(grouperGroupInfo, ldapGroup);
    }
    else {
      String membershipAttributeValue = evaluateJexlExpression("MemberAttributeValue", config.getMemberAttributeValueFormat(), subject, ldapUser, grouperGroupInfo, ldapGroup);
      if ( membershipAttributeValue != null ) {
        scheduleGroupModification(grouperGroupInfo, ldapGroup, AttributeModificationType.ADD, Arrays.asList(membershipAttributeValue));
        JobStatistics jobStatistics = this.getJobStatistics();
        if (jobStatistics != null) {
          jobStatistics.insertCount.addAndGet(1);
        }
      }
    }
  }


  protected void scheduleGroupModification(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup, AttributeModificationType modType, Collection<String> membershipValuesToChange) {
    String attributeName = config.getMemberAttributeName();

    for ( String value : membershipValuesToChange )
      // ADD/REMOVE <value> to/from <attribute> of <group>
      LOG.info("Will change LDAP: {} {} {} {} of {}",
          new Object[] {modType, value,
          modType == AttributeModificationType.ADD ? "to" : "from",
          attributeName, ldapGroup});

    scheduleLdapModification(
        new ModifyRequest(
            ldapGroup.getLdapObject().getDn(),
            new AttributeModification(
                modType,
                new LdapAttribute(attributeName, membershipValuesToChange.toArray(new String[0])))));
  }

  @Override
  protected void deleteMembership(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup ,
      Subject subject, LdapUser ldapUser) throws PspException {
    if ( ldapGroup  == null ) {
      LOG.warn("{}: Ignoring request to remove {} from a group that doesn't exist: {}",
          new Object[]{getDisplayName(), subject.getId(), grouperGroupInfo});
      return;
    }

    if ( ldapUser == null && config.needsTargetSystemUsers() ) {
      LOG.warn("{}: Skipping removing membership from group {} because ldap user does not exist: {}",
          new Object[]{getDisplayName(), grouperGroupInfo, subject});
      return;
    }

    // TODO: Look in memory cache to see if change is necessary:
    // a) User object's group-listing attribute
    // or b) if the group-membership attribute is being fetched

    String membershipAttributeValue = evaluateJexlExpression("MemberAttributeValue", config.getMemberAttributeValueFormat(), subject, ldapUser, grouperGroupInfo, ldapGroup);

    if ( membershipAttributeValue != null ) {
      JobStatistics jobStatistics = this.getJobStatistics();
      if (jobStatistics != null) {
        jobStatistics.deleteCount.addAndGet(1);
      }
      scheduleGroupModification(grouperGroupInfo, ldapGroup, AttributeModificationType.REMOVE, Arrays.asList(membershipAttributeValue));
    }
  }

  @Override
  protected boolean doFullSync(
      GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup ,
      Set<Subject> correctSubjects, Map<Subject, LdapUser> tsUserMap,
      Set<LdapUser> correctTSUsers,
      JobStatistics stats) throws PspException {

    stats.totalCount.set(correctSubjects.size());

    // Looking for bug
    // Make sure the group we've been passed has been fetched with the membership attribute
    if ( ldapGroup != null )
      ldapGroup.getLdapObject().getStringValues(config.getMemberAttributeName());

    // If the group does not exist yet, then create it with all the correct members
    if ( ldapGroup  == null ) {

      // If the schema requires member attribute, then don't do anything if there aren't any members
      if ( config.areEmptyGroupsSupported() ) {
        if ( correctSubjects.size() == 0 ) {
          LOG.info("{}: Nothing to do because empty group already not present in ldap system", getDisplayName() );
          return false;
        }
      }

      ldapGroup  = createGroup(grouperGroupInfo, correctSubjects);
      stats.insertCount.addAndGet(correctSubjects.size());

      // Make note of the group if it was created
      if ( ldapGroup != null ) {
        cacheGroup(grouperGroupInfo, ldapGroup);
      }
      return true;
    } else {
        // The LDAP group exists, let's make sure the non-membership attributes are still accurate
        ldapGroup = updateGroupFromTemplate(grouperGroupInfo, ldapGroup);
        cacheGroup(grouperGroupInfo, ldapGroup);
    }

    // Delete an empty group if the schema requires a membership
    if ( !config.areEmptyGroupsSupported() && correctSubjects.size() == 0 ) {
      LOG.info("{}: Deleting empty group because schema requires its member attribute", getDisplayName());
      deleteGroup(grouperGroupInfo, ldapGroup);

      // Update stats with the number of values removed by group deletion
      Collection<String> membershipValues = ldapGroup.getLdapObject().getStringValues(config.getMemberAttributeName());
      stats.deleteCount.addAndGet(membershipValues.size());

      return true;
    }

    Set<String> correctMembershipValues = getStringSet(config.isMemberAttributeCaseSensitive());

    for ( Subject correctSubject: correctSubjects ) {
      String membershipAttributeValue = evaluateJexlExpression("MemberAttributeValue", config.getMemberAttributeValueFormat(), correctSubject, tsUserMap.get(correctSubject), grouperGroupInfo, ldapGroup);

      if ( membershipAttributeValue != null ) {
        correctMembershipValues.add(membershipAttributeValue);
      }
    }

    Collection<String> currentMembershipValues = getStringSet(config.isMemberAttributeCaseSensitive(), ldapGroup.getLdapObject().getStringValues(config.getMemberAttributeName()));

    // If configured to ignore the null or empty DN on the membership attribute do
    // so but only if the membership attribute is "member". This may be extended to
    // other membership attributes over time but currently focuses on the use case
    // where the objectClass is groupOfNames and the membership attribute that requires
    // DN syntax is member.
    if(config.allowEmptyDnAttributeValues()) {
        if(config.getMemberAttributeName().equals("member")) {
            currentMembershipValues.removeIf(v -> v.equals(""));
        }
    }

    LOG.info("{}: Full-sync comparison for {}: Target-subject count: Correct/Actual: {}/{}",
            new Object[] {getDisplayName(), grouperGroupInfo, correctMembershipValues.size(), currentMembershipValues.size()});

    LOG.debug("{}: Full-sync comparison: Correct: {}", getDisplayName(), correctMembershipValues);
    LOG.debug("{}: Full-sync comparison: Actual: {}", getDisplayName(), currentMembershipValues);

    // EXTRA = CURRENT - CORRECT
      Collection<String> extraValues = subtractStringCollections(
              config.isMemberAttributeCaseSensitive(), currentMembershipValues, correctMembershipValues);

      stats.deleteCount.addAndGet(extraValues.size());

      LOG.info("{}: Group {} has {} extra values",
          new Object[] {getDisplayName(), grouperGroupInfo, extraValues.size()});
      if ( extraValues.size() > 0 ) {
        getLdapSystem().performLdapModify(
                new ModifyRequest(
                        ldapGroup.dn,
                        new AttributeModification(
                                AttributeModificationType.REMOVE,
                                new LdapAttribute(config.getMemberAttributeName(),extraValues.toArray(new String[0])))),
                config.isMemberAttributeCaseSensitive(),
                true);
      }

    // MISSING = CORRECT - CURRENT
      Collection<String> missingValues = subtractStringCollections(
              config.isMemberAttributeCaseSensitive(), correctMembershipValues, currentMembershipValues);

      stats.insertCount.addAndGet(missingValues.size());

      LOG.info("{}: Group {} has {} missing values",
          new Object[]{getDisplayName(), grouperGroupInfo, missingValues.size()});
      if ( missingValues.size() > 0 ) {
        getLdapSystem().performLdapModify(
                new ModifyRequest(
                        ldapGroup.dn,
                        new AttributeModification(
                                AttributeModificationType.ADD,
                                new LdapAttribute(config.getMemberAttributeName(),missingValues.toArray(new String[0])))),
                config.isMemberAttributeCaseSensitive(),
                true);

    }

    return extraValues.size()>0 || missingValues.size()>0;
  }

  /**
   * This method compares the existing LdapGroup to how the groupCreationTemplate might have
   * changed due to group changes (eg, a changed group name) or due to template changes
   * @param grouperGroupInfo
   * @param existingLdapGroup
   * @return An up-to-date LdapGroup: either existingLdapGroup if no changes were needed, or a newly-read group
   */
  protected LdapGroup updateGroupFromTemplate(GrouperGroupInfo grouperGroupInfo, LdapGroup existingLdapGroup) throws PspException {
    LOG.debug("{}: Making sure (non-membership) attributes of group are up to date: {}", getDisplayName(), existingLdapGroup.dn);

    try {
      String ldifFromTemplate = getGroupLdifFromTemplate(grouperGroupInfo, config.removeNullDnFromGroupLdifCreationTemplate());
      LdapEntry ldapEntryFromTemplate = getLdapEntryFromLdif(ldifFromTemplate);

      ensureLdapOusExist(ldapEntryFromTemplate.getDn(), false);
      if ( getLdapSystem().makeLdapObjectCorrect(ldapEntryFromTemplate, existingLdapGroup.ldapObject.ldapEntry, config.isMemberAttributeCaseSensitive()) ) {
        LdapGroup result = fetchTargetSystemGroup(grouperGroupInfo);
        return result;
      }
      else {
        return existingLdapGroup;
      }
    }
    catch (PspException e) {
      LOG.error("{}: Problem checking and updating group's template attributes", getDisplayName(), e);
      throw e;
    }
    catch (IOException e) {
      LOG.error("{}: Problem checking and updating group's tempalte attributes", getDisplayName(), e);
      throw new PspException("IO Exception while checking and updating group's template attributes", e);
    }
  }



  @Override
	protected void doFullSync_cleanupExtraGroups(JobStatistics stats) throws PspException {

    // (1) Get all the groups that are in LDAP
    String filterString = config.getAllGroupSearchFilter();
    if ( StringUtils.isEmpty(filterString) ) {
      LOG.error("{}: Cannot cleanup extra groups without a configured all-group search filter", getDisplayName());
      return;
    }

    String baseDn = config.getGroupSearchBaseDn();

    if ( StringUtils.isEmpty(baseDn)) {
      LOG.error("{}: Cannot cleanup extra groups without a configured group-search base dn", getDisplayName());
      return;
    }

    // Get all the LDAP Groups that match the filter
    List<LdapObject> allProvisionedGroups
            = getLdapSystem().performLdapSearchRequest(
            -1, baseDn, SearchScope.SUBTREE,
                    Arrays.asList(getLdapAttributesToFetch()), filterString);


    // See what LDAP Groups match the correct list of groups

    Collection<GrouperGroupInfo> groupsThatShouldBeProvisioned = getAllGroupsForProvisioner();
    Map<GrouperGroupInfo, LdapGroup> ldapGroupsThatShouldBeProvisioned = fetchTargetSystemGroupsInBatches(groupsThatShouldBeProvisioned);

    Set<String> correctGroupDNs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    for(LdapGroup correctLdapGroup : ldapGroupsThatShouldBeProvisioned.values()) {
      String correctLdapGroupDn = correctLdapGroup.getLdapObject().getDn();
      correctGroupDNs.add(correctLdapGroupDn);
    }


    List<LdapObject> groupsToDelete = new ArrayList<LdapObject>();
    for (LdapObject aProvisionedGroup : allProvisionedGroups) {
      if ( ! correctGroupDNs.contains(aProvisionedGroup.getDn()) ) {
        groupsToDelete.add(aProvisionedGroup);
      }
    }

    LOG.info("{}: There are {} groups that we should delete", getDisplayName(), groupsToDelete.size());

    for ( LdapObject groupToRemove : groupsToDelete ) {
      int numMembershipsBeingDeleted = groupToRemove.getStringValues(config.getMemberAttributeName()).size();
      stats.deleteCount.addAndGet(numMembershipsBeingDeleted);

      getLdapSystem().performLdapDelete(groupToRemove.getDn());
    }

  }


  @Override
  protected LdapGroup createGroup(GrouperGroupInfo grouperGroup, Collection<Subject> initialMembers) throws PspException {
    if ( !config.areEmptyGroupsSupported() && initialMembers.size() == 0 ) {
      LOG.warn("Not Creating LDAP group because empty groups are not supported: {}", grouperGroup);
      return null;
    }

    LOG.info("Creating LDAP group for GrouperGroup: {} ", grouperGroup);
    String ldif = getGroupLdifFromTemplate(grouperGroup);

    // If initialMembers were specified, then add the ldif necessary to include them
    if ( initialMembers != null && initialMembers.size() > 0 ) {

      // Find all the values for the membership attribute
      Collection<String> membershipValues = new HashSet<String>(initialMembers.size());

      for ( Subject subject : initialMembers ) {
        LdapUser ldapUser;
        String membershipAttributeValue = null;
        if (!config.needsTargetSystemUsers()) {
          membershipAttributeValue = evaluateJexlExpression("MemberAttributeValue", config.getMemberAttributeValueFormat(), subject, null, grouperGroup, null);
        }
        else {
          ldapUser = getTargetSystemUser(subject);
          if ( ldapUser != null ) {
            membershipAttributeValue = evaluateJexlExpression("MemberAttributeValue", config.getMemberAttributeValueFormat(), subject, ldapUser, grouperGroup, null);
          }
        }

        if ( membershipAttributeValue != null ) {
          membershipValues.add(membershipAttributeValue);
        }
      }

      JobStatistics jobStatistics = this.getJobStatistics();
      if (jobStatistics != null) {
        jobStatistics.insertCount.addAndGet(membershipValues.size());
      }

      StringBuilder ldifForMemberships = new StringBuilder();
      for ( String attributeValue : membershipValues ) {
        ldifForMemberships.append(String.format("%s: %s\n", config.getMemberAttributeName(), attributeValue));
      }
      ldif = ldif.concat("\n");
      ldif = ldif.concat(ldifForMemberships.toString());
    }

    Connection conn = getLdapSystem().getLdapConnection();
    try {
      LOG.debug("{}: LDIF for new group (with partial DN): {}", getDisplayName(), ldif.replaceAll("\\n", "||"));
      LdapEntry ldifEntry = getLdapEntryFromLdif(ldif);

      // Check to see if any attributes ended up without any values/
      for ( String attributeName : ldifEntry.getAttributeNames() ) {

        // If the attribute value requires DN syntax and we allow the null DN
        // (an empty DN) then continue and examine the next attribute.
        if(config.allowEmptyDnAttributeValues()) {
            List<String> attributeDnSyntaxList = Arrays.asList(config.getAttributesNeededingDnEscaping());
            if(attributeDnSyntaxList.contains(attributeName)) {
                LOG.debug("{}: attribute {} requires DN syntax but is allowed to hold the null DN", getDisplayName(), attributeName);
                continue;
            }
        }
        LdapAttribute attribute = ldifEntry.getAttribute(attributeName);
        if ( LdapSystem.attributeHasNoValues(attribute) ) {
          LOG.warn("{}: LDIF for new group did not define any values for {}", getDisplayName(), attributeName);
          ldifEntry.removeAttribute(attributeName);
        }
      }
      LOG.debug("{}: Adding group: {}", getDisplayName(), ldifEntry);
      
      performLdapAdd(ldifEntry);
      
      // Read the group that was just created
      LOG.debug("Reading group that was just added to ldap server: {}", grouperGroup);
      LdapGroup result = fetchTargetSystemGroup(grouperGroup);

      if ( result == null ) {
        LOG.error("{}: Group could not be found after it was created: {}", getDisplayName(), grouperGroup);
      }
      return result;
    } catch (PspException e) {
      LOG.error("Problem while creating new group: {}", ldif, e);
      throw e;
    } catch ( IOException e ) {
      LOG.error("IO problem while creating group: {}", ldif, e);
      throw new PspException("IO problem while creating group: %s", e.getMessage());
    }
    finally {
      conn.close();
    }
  }

  /**
   * This returns an LdapEntry from the provided ldif. NOTE: The DN of the LDIF is extended
   * with the configuration's groupCreationBaseDn.
   *
   * @param ldif
   * @return
   * @throws IOException
   */
  private LdapEntry getLdapEntryFromLdif(String ldif) throws IOException {
    Reader reader = new StringReader(ldif);
    LdifReader ldifReader = new LdifReader(reader);
    SearchResult ldifResult = ldifReader.read();
    LdapEntry ldifEntry = ldifResult.getEntry();

    // Update DN to be relative to groupCreationBaseDn
    String actualDn = String.format("%s,%s", ldifEntry.getDn(),config.getGroupCreationBaseDn());
    ldifEntry.setDn(actualDn);
    return ldifEntry;
  }

  /**
   * Fills in the GroupCreationLdifTemplate for the provided group
   * @param grouperGroup
   * @param stripMembershipAttributeWithNullDn
   * @return
   * @throws PspException
   */
  private String getGroupLdifFromTemplate(GrouperGroupInfo grouperGroup, boolean stripMembershipAttributeWithNullDn) throws PspException {
    String ldif = config.getGroupCreationLdifTemplate();

    if(stripMembershipAttributeWithNullDn) {
        LOG.debug("Stripping membership attribute {} with null DN value. LDIF string before is: {}", config.getMemberAttributeName(), ldif);
        ldif = ldif.replaceAll(config.getMemberAttributeName() + ":\\|\\|", "");
        LOG.debug("LDIF string after is: {}", ldif);
    }

    ldif = ldif.replaceAll("\\|\\|", "\n");
    ldif = evaluateJexlExpression("GroupTemplate", ldif, null, null, grouperGroup, null);
    ldif = sanityCheckDnAttributesOfLdif(ldif, "Group ldif for %s", grouperGroup);

    return ldif;
  }

  private String getGroupLdifFromTemplate(GrouperGroupInfo grouperGroup) throws PspException {
      return getGroupLdifFromTemplate(grouperGroup, false);
  }

  @Override
  protected Map<GrouperGroupInfo, LdapGroup> fetchTargetSystemGroups(
      Collection<GrouperGroupInfo> grouperGroupsToFetch) throws PspException {
    if ( grouperGroupsToFetch.size() > config.getGroupSearch_batchSize() )
      throw new IllegalArgumentException("LdapGroupProvisioner.fetchTargetSystemGroups: invoked with too many groups to fetch");
    
    // If this is a full-sync provisioner, then we want to make sure we get the member attribute of the
    // group so we see all members.
    String[] returnAttributes = getLdapAttributesToFetch();

    if ( grouperGroupsToFetch.size() > 1 && config.isBulkGroupSearchingEnabled() ) {
      StringBuilder combinedLdapFilter = new StringBuilder();

      // Start the combined ldap filter as an OR-query
      combinedLdapFilter.append("(|");

      for (GrouperGroupInfo grouperGroup : grouperGroupsToFetch) {
        SearchFilter f = getGroupLdapFilter(grouperGroup);
        String groupFilterString = f.format();

        // Wrap the subject's filter in (...) if it doesn't start with (
        if (groupFilterString.startsWith("("))
          combinedLdapFilter.append(groupFilterString);
        else
          combinedLdapFilter.append('(').append(groupFilterString).append(')');
      }
      combinedLdapFilter.append(')');

      // Actually do the search
      List<LdapObject> searchResult;

      LOG.debug("{}: Searching for {} groups with:: {}",
              new Object[]{getDisplayName(), grouperGroupsToFetch.size(), combinedLdapFilter});

      try {
        searchResult = getLdapSystem().performLdapSearchRequest(
                -1, config.getGroupSearchBaseDn(), SearchScope.SUBTREE,
                Arrays.asList(returnAttributes),
                combinedLdapFilter.toString());
      } catch (PspException e) {
        LOG.error("Problem fetching groups with filter '{}' on base '{}'",
                new Object[]{combinedLdapFilter, config.getGroupSearchBaseDn(), e});
        throw e;
      }

      LOG.debug("{}: Group search returned {} groups", getDisplayName(), searchResult.size());

      // Now we have a bag of LdapObjects, but we don't know which goes with which grouperGroup.
      // We're going to go through the Grouper Groups and their filters and compare
      // them to the Ldap data we've fetched into memory.
      Map<GrouperGroupInfo, LdapGroup> result = new HashMap<GrouperGroupInfo, LdapGroup>();

      Set<LdapObject> matchedFetchResults = new HashSet<LdapObject>();

      // For every group we tried to bulk fetch, find the matching LdapObject that came back
      for (GrouperGroupInfo groupToFetch : grouperGroupsToFetch) {
        SearchFilter f = getGroupLdapFilter(groupToFetch);

        for (LdapObject aFetchedLdapObject : searchResult) {
          if (aFetchedLdapObject.matchesLdapFilter(f)) {
            result.put(groupToFetch, new LdapGroup(aFetchedLdapObject));
            matchedFetchResults.add(aFetchedLdapObject);
            break;
          }
        }
      }

      Set<LdapObject> unmatchedFetchResults = new HashSet<LdapObject>(searchResult);
      unmatchedFetchResults.removeAll(matchedFetchResults);

      // We're done if everything matched up
      if ( unmatchedFetchResults.size() == 0 ) {
        return result;
      }
      else {
        for (LdapObject unmatchedFetchResult : unmatchedFetchResults) {
          LOG.warn("{}: Bulk fetch failed (returned unmatchable group data). "
                          + "This can be caused by searching for a DN with escaping or by singleGroupSearchFilter ({}) that are not included "
                          + "in groupSearchAttributes ({})?): {}",
                  new Object[]{getDisplayName(), config.getSingleGroupSearchFilter(), config.getGroupSearchAttributes(), unmatchedFetchResult.getDn()});
        }
        LOG.warn("{}: Slower fetching will be attempted", getDisplayName());

        // Fall through to the one-by-one group searching below. This is slower, but doesn't require the
        // result-matching step that just failed
      }
    }

    // Do simple ldap searching
    Map<GrouperGroupInfo, LdapGroup> result = new HashMap<GrouperGroupInfo, LdapGroup>();

    for (GrouperGroupInfo grouperGroup : grouperGroupsToFetch) {
      SearchFilter groupLdapFilter = null;
      if (grouperGroup == null) {
        continue;
      }
      try {
        groupLdapFilter = getGroupLdapFilter(grouperGroup);
      } catch (DeletedGroupException dge) {
        LOG.debug("{}: " + dge.getMessage(), getDisplayName());
        // cant find, just let full sync deal with it
        continue;
      }
      try {
        LOG.debug("{}: Searching for group {} with:: {}",
                new Object[]{getDisplayName(), grouperGroup, groupLdapFilter});

        // Actually do the search
        List<LdapObject> searchResult = getLdapSystem().performLdapSearchRequest(
                -1, config.getGroupSearchBaseDn(), SearchScope.SUBTREE,
                Arrays.asList(returnAttributes),
                groupLdapFilter);

        if (searchResult.size() == 1) {
          LdapObject ldapObject = searchResult.iterator().next();
          LOG.debug("{}: Group search returned {}", getDisplayName(), ldapObject.getDn());
          result.put(grouperGroup, new LdapGroup(ldapObject));
        }
        else if ( searchResult.size() > 1 ){
          LOG.error("{}: Search for group {} with '{}' returned multiple matches: {}",
                  new Object[]{getDisplayName(), grouperGroup, groupLdapFilter, searchResult});
          throw new PspException("Search for ldap group returned multiple matches");
        }
        else if ( searchResult.size() == 0 ) {
          // No match found ==> result will not include an entry for this grouperGroup
          LOG.debug("{}: Group search did not return any results", getDisplayName());
        }
      } catch (PspException e) {
        LOG.error("{}: Problem fetching group with filter '{}' on base '{}'",
                new Object[]{getDisplayName(), groupLdapFilter, config.getGroupSearchBaseDn(), e});
        throw e;
      }
    }

    return result;
}

  private String[] getLdapAttributesToFetch() {
    String returnAttributes[] = config.getGroupSearchAttributes();
    if ( fullSyncMode ) {
      LOG.debug("Fetching membership attribute, too");
      // Add the membership attribute to the list of attributes to fetch
      returnAttributes = Arrays.copyOf(returnAttributes, returnAttributes.length + 1);
      returnAttributes[returnAttributes.length-1] = config.getMemberAttributeName();
    } else {
      LOG.debug("Fetching without membership attribute");
    }
    return returnAttributes;
  }


  private SearchFilter getGroupLdapFilter(GrouperGroupInfo grouperGroup) throws PspException {
    String result = evaluateJexlExpression("SingleGroupSearchFilter", config.getSingleGroupSearchFilter(), null, null, grouperGroup, null);
    if ( StringUtils.isEmpty(result) )
      throw new RuntimeException("Group searching requires singleGroupSearchFilter to be configured correctly");

    // If the filter contains '||', then this filter is requesting parameter substitution
    String filterPieces[] = result.split("\\|\\|");
    SearchFilter filter = new SearchFilter(filterPieces[0]);
    // If the filter is not using ldap-filter parameters, check its syntax
    if ( filterPieces.length == 1 ) {
      try {
        // Use unboundid to sanity-check/parse filter
        Filter.create(result);
      }
      catch (LDAPException e) {
        LOG.warn("{}: Group ldap filter was invalid. " +
                        "Perhaps its filter clauses needed to be escaped with utils.escapeLdapFilter or use ldap-filter positional parameters. " +
                        "Group={}. Bad filter={}. ",
                new Object[]{getDisplayName(), grouperGroup, result});

        // We're going to proceed here just in case the filter-checking logic is too
        // sensitive. The ldap server will eventually see the filter and make its own decision
      }
    } else {
      // Set the positional parameters

      for (int i = 1; i < filterPieces.length; i++)
        filter.setParameter(i - 1, filterPieces[i].trim());
    }

    LOG.trace("{}: Filter for group {}: {}",
        new Object[] {getDisplayName(), grouperGroup, filter});

    return filter;
  }


  @Override
  protected void deleteGroup(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup)
      throws PspException {
    if ( ldapGroup == null ) {
      LOG.warn("Nothing to do: Unable to delete group {} because the group wasn't found on target system", grouperGroupInfo);
      return;
    }
    
    String dn = ldapGroup.getLdapObject().getDn();
    
    LOG.info("Deleting group {} by deleting DN {}", grouperGroupInfo, dn);
    JobStatistics jobStatistics = this.getJobStatistics();
    if (jobStatistics != null) {
      jobStatistics.deleteCount.addAndGet(1);
    }
    
    getLdapSystem().performLdapDelete(dn);
  }
}
