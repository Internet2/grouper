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

import org.apache.commons.lang.StringUtils;
import org.ldaptive.*;
import org.ldaptive.io.LdifReader;

import edu.internet2.middleware.subject.Subject;



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

    if ( ldapUser == null ) {
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
      String membershipAttributeValue = evaluateJexlExpression(config.getMemberAttributeValueFormat(), subject, ldapUser, grouperGroupInfo, ldapGroup);
      if ( membershipAttributeValue != null ) {
        scheduleGroupModification(grouperGroupInfo, ldapGroup, AttributeModificationType.ADD, Arrays.asList(membershipAttributeValue));
      }
    }
  }


  protected void scheduleGroupModification(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup, AttributeModificationType modType, Collection<String> membershipValuesToChange) {
    uncacheGroup(grouperGroupInfo, ldapGroup);

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

    if ( ldapUser == null ) {
      LOG.warn("{}: Skipping removing membership from group {} because ldap user does not exist: {}",
          new Object[]{getDisplayName(), grouperGroupInfo, subject});
      return;
    }

    // TODO: Look in memory cache to see if change is necessary:
    // a) User object's group-listing attribute
    // or b) if the group-membership attribute is being fetched

    String membershipAttributeValue = evaluateJexlExpression(config.getMemberAttributeValueFormat(), subject, ldapUser, grouperGroupInfo, ldapGroup);

    if ( membershipAttributeValue != null ) {
      scheduleGroupModification(grouperGroupInfo, ldapGroup, AttributeModificationType.REMOVE, Arrays.asList(membershipAttributeValue));
    }
  }

  /**
   * Get a string set that is case-insensitive or case-sensitive
   * depending on the provisioner's configuration.isMemberAttributeCaseSensitive
   */
  protected Set<String> getStringSet() {
    if ( config.isMemberAttributeCaseSensitive() )
      return new HashSet<String>();
    else
      return new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
  }

  /**
   * Get a string set that is case-insensitive or case-sensitive,
   * depending on the provisioner's configuration.isMemberAttributeCaseSensitive.
   *
   * The returned set will contain the values provided
   */
  protected Set<String> getStringSet(Collection<String> values ) {
    Set<String> result = getStringSet();
    if ( values != null ) {
      result.addAll(values);
    }

    return result;
  }

  @Override
  protected void doFullSync(
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
          return;
        }
      }

      ldapGroup  = createGroup(grouperGroupInfo, correctSubjects);
      stats.insertCount.set(correctSubjects.size());

      cacheGroup(grouperGroupInfo, ldapGroup);
      return;
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
      stats.deleteCount.set(membershipValues.size());
    }

    Set<String> correctMembershipValues = getStringSet();

    for ( Subject correctSubject: correctSubjects ) {
      String membershipAttributeValue = evaluateJexlExpression(config.getMemberAttributeValueFormat(), correctSubject, tsUserMap.get(correctSubject), grouperGroupInfo, ldapGroup);

      if ( membershipAttributeValue != null ) {
        correctMembershipValues.add(membershipAttributeValue);
      }
    }

    Collection<String> currentMembershipValues = getStringSet(ldapGroup.getLdapObject().getStringValues(config.getMemberAttributeName()));

    LOG.info("{}: Full-sync comparison for {}: Target-subject count: Correct/Actual: {}/{}",
            new Object[] {getDisplayName(), grouperGroupInfo, correctMembershipValues.size(), currentMembershipValues.size()});

    LOG.debug("{}: Full-sync comparison: Correct: {}", getDisplayName(), correctMembershipValues);
    LOG.debug("{}: Full-sync comparison: Actual: {}", getDisplayName(), currentMembershipValues);

    // EXTRA = CURRENT - CORRECT
    {
      Collection<String> extraValues = getStringSet(currentMembershipValues);
      extraValues.removeAll(correctMembershipValues);

      stats.deleteCount.set(extraValues.size());

      LOG.info("{}: Group {} has {} extra values",
          new Object[] {getDisplayName(), grouperGroupInfo, extraValues.size()});
      if ( extraValues.size() > 0 )
        scheduleGroupModification(grouperGroupInfo, ldapGroup, AttributeModificationType.REMOVE, extraValues);
    }

    // MISSING = CORRECT - CURRENT
    {
      Collection<String> missingValues = getStringSet(correctMembershipValues);
      missingValues.removeAll(currentMembershipValues);

      stats.insertCount.set(missingValues.size());

      LOG.info("{}: Group {} has {} missing values",
          new Object[]{getDisplayName(), grouperGroupInfo, missingValues.size()});
      if ( missingValues.size() > 0 )
        scheduleGroupModification(grouperGroupInfo, ldapGroup, AttributeModificationType.ADD, missingValues);
    }
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
      String ldifFromTemplate = getGroupLdifFromTemplate(grouperGroupInfo);
      LdapEntry ldapEntryFromTemplate = getLdapEntryFromLdif(ldifFromTemplate);

      ensureLdapOusExist(ldapEntryFromTemplate.getDn(), false);
      if ( getLdapSystem().makeLdapObjectCorrect(ldapEntryFromTemplate, existingLdapGroup.ldapObject.ldapEntry) ) {
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
	protected void doFullSync_cleanupExtraGroups(
			Set<GrouperGroupInfo> groupsForThisProvisioner,
			Map<GrouperGroupInfo, LdapGroup> ldapGroups,
            JobStatistics stats) throws PspException {

    // Grab all the DNs that match the groupsForThisProvisioner
    Set<String> desiredGroupDns = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    for ( LdapGroup ldapGroup  : ldapGroups.values() )
      desiredGroupDns.add( ldapGroup.getLdapObject().getDn());


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
    List<LdapObject> searchResult
            = getLdapSystem().performLdapSearchRequest(new SearchRequest(baseDn, filterString,
            getLdapAttributesToFetch()));

    List<LdapObject> groupsToDelete = new ArrayList<LdapObject>();

    for ( LdapObject existingGroup : searchResult ) {
      String existingGroupDn = existingGroup.getDn();
      if ( !desiredGroupDns.contains(existingGroupDn) ) {
        groupsToDelete.add(existingGroup);
      }
    }

    LOG.info("{}: There are {} groups that we should delete", getDisplayName(), groupsToDelete.size());

    int numMembershipsBeingDeleted = 0;

    for ( LdapObject groupToRemove : groupsToDelete ) {
      numMembershipsBeingDeleted += groupToRemove.getStringValues(config.getMemberAttributeName()).size();
      getLdapSystem().performLdapDelete(groupToRemove.getDn());
    }

    stats.deleteCount.addAndGet(numMembershipsBeingDeleted);
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
        LdapUser ldapUser = getTargetSystemUser(subject);
        if ( ldapUser != null ) {
          String membershipAttributeValue = evaluateJexlExpression(config.getMemberAttributeValueFormat(), subject, ldapUser, grouperGroup, null);
          if ( membershipAttributeValue != null ) {
            membershipValues.add(membershipAttributeValue);
          }
        }
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
      return fetchTargetSystemGroup(grouperGroup);
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
   * @return
   * @throws PspException
   */
  private String getGroupLdifFromTemplate(GrouperGroupInfo grouperGroup) throws PspException {
    String ldif = config.getGroupCreationLdifTemplate();
    ldif = ldif.replaceAll("\\|\\|", "\n");
    ldif = evaluateJexlExpression(ldif, null, null, grouperGroup, null);
    return ldif;
  }

  @Override
  protected Map<GrouperGroupInfo, LdapGroup> fetchTargetSystemGroups(
      Collection<GrouperGroupInfo> grouperGroupsToFetch) throws PspException {
    if ( grouperGroupsToFetch.size() > config.getGroupSearch_batchSize() )
      throw new IllegalArgumentException("LdapGroupProvisioner.fetchTargetSystemGroups: invoked with too many groups to fetch");
    
    // If this is a full-sync provisioner, then we want to make sure we get the member attribute of the
    // group so we see all members.
    String[] returnAttributes = getLdapAttributesToFetch();

    if ( config.isBulkGroupSearchingEnabled() ) {
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
                new SearchRequest(config.getGroupSearchBaseDn(),
                        combinedLdapFilter.toString(),
                        returnAttributes));
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

      for (LdapObject unmatchedFetchResult : unmatchedFetchResults) {
        LOG.warn("{}: Bulk fetch failed (returned unmatchable group data). "
                        + "This can be caused by searching for a DN with escaping or by singleGroupSearchFilter ({}) that are not included "
                        + "in groupSearchAttributes ({})?): {}",
                new Object[]{getDisplayName(), config.getSingleGroupSearchFilter(), config.getGroupSearchAttributes(), unmatchedFetchResult.getDn()});
        LOG.warn("{}: Slower fetching will be attempted", getDisplayName());
      }

      // We're done if everything matched up
      if ( unmatchedFetchResults.size() == 0 ) {
        return result;
      }
      else {
        // Fall through to the one-by-one group searching below. This is slower, but doesn't require the
        // result-matching step that just failed
      }
    }

    // Do simple ldap searching
    Map<GrouperGroupInfo, LdapGroup> result = new HashMap<GrouperGroupInfo, LdapGroup>();

    for (GrouperGroupInfo grouperGroup : grouperGroupsToFetch) {
      SearchFilter groupLdapFilter = getGroupLdapFilter(grouperGroup);
      try {
        LOG.debug("{}: Searching for group {} with:: {}",
                new Object[]{getDisplayName(), grouperGroup, groupLdapFilter});

        // Actually do the search
        List<LdapObject> searchResult = getLdapSystem().performLdapSearchRequest(
                new SearchRequest(config.getGroupSearchBaseDn(),
                        groupLdapFilter,
                        returnAttributes));

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
    String result = evaluateJexlExpression(config.getSingleGroupSearchFilter(), null, null, grouperGroup, null);
    if ( StringUtils.isEmpty(result) )
      throw new RuntimeException("Group searching requires singleGroupSearchFilter to be configured correctly");

    // If the filter contains '||', then this filter is requesting parameter substitution
    String filterPieces[] = result.split("\\|\\|");
    SearchFilter filter = new SearchFilter(filterPieces[0]);
    for (int i=1; i<filterPieces.length; i++)
      filter.setParameter(i-1, filterPieces[i].trim());
 
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
    
    getLdapSystem().performLdapDelete(dn);;
  }
}
