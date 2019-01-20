package edu.internet2.middleware.grouper.pspng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;

import edu.internet2.middleware.subject.Subject;

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

/** This class implements group-membership changes into an Attribute of 
 * the User objects that are members of the group.
 * 
 * @author bert
 *
 */
public class LdapAttributeProvisioner extends LdapProvisioner<LdapAttributeProvisionerConfiguration> {
  
  public LdapAttributeProvisioner(String provisionerName, LdapAttributeProvisionerConfiguration config, boolean fullSyncMode) {
    super(provisionerName, config, fullSyncMode);
  }

  
  public static Class<? extends ProvisionerConfiguration> getPropertyClass() {
    return LdapAttributeProvisionerConfiguration.class;
  }

  /**
   * This adds/removes values from the given user. This is a helper class to package
   * up the modification into the necessary LDAP ModifyRequest.
   * 
   * @param ldapUser
   * @param modType
   * @param valuesToChange
   */
  protected void scheduleUserModification(LdapUser ldapUser, AttributeModificationType modType, Collection<String> valuesToChange) {
    String attributeName = config.getProvisionedAttributeName();
    
    for ( String value : valuesToChange )
      // ADD/REMOVE <value> to/from <attribute> of <user>
      LOG.info("Will change LDAP: {} {} {} {} of {}", 
          new Object[] {modType, value,
          modType == AttributeModificationType.ADD ? "to" : "from",
          attributeName, ldapUser});
      
    scheduleLdapModification(
        new ModifyRequest(
            ldapUser.getLdapObject().getDn(), 
            new AttributeModification(modType, new LdapAttribute(attributeName, valuesToChange.toArray(new String[0])))));
  }
  
  
  @Override
  protected void addMembership(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup,
      Subject subject, LdapUser ldapUser) throws PspException {

    if ( ldapUser == null ) {
      LOG.warn("{}: Skipping addMembership: LdapUser does not exist for subject {}", getDisplayName(), subject.getId());
      return;
    }
    
    String attributeValue = getAttributeValueForGroup(grouperGroupInfo);
    
    scheduleUserModification(ldapUser, AttributeModificationType.ADD, Arrays.asList(attributeValue));
  }

  @Override
  protected void deleteMembership(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup,
      Subject subject, LdapUser ldapUser) throws PspException {

    if ( ldapUser == null ) {
      LOG.warn("{}: Skipping deleteMembership: LdapUser does not exist for subject {}", getDisplayName(), subject.getId());
      return;
    }
    
    String attributeValue = getAttributeValueForGroup(grouperGroupInfo);

    scheduleUserModification(ldapUser, AttributeModificationType.REMOVE, Arrays.asList(attributeValue));
  }

  @Override
  protected void doFullSync(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup,
      Set<Subject> correctSubjects, Map<Subject, LdapUser> tsUserMap, Set<LdapUser> correctTSUsers,
      JobStatistics stats)
      throws PspException {

    stats.totalCount.set(correctTSUsers.size());
    String attributeName = config.getProvisionedAttributeName();
    String attributeValue = getAttributeValueForGroup(grouperGroupInfo);
    
    List<LdapObject> currentMatches_ldapObjects = getLdapSystem().performLdapSearchRequest(
        config.getUserSearchBaseDn(), SearchScope.SUBTREE, 
        Arrays.asList(config.getUserSearchAttributes()), attributeName + "={0}",
        attributeValue);
    
    List<LdapUser> currentMatches = new ArrayList<LdapUser>(currentMatches_ldapObjects.size());
    for ( LdapObject ldapObject : currentMatches_ldapObjects )
      currentMatches.add(new LdapUser(ldapObject));

    LOG.info("{}: Full-sync comparison for {}: Target-subject count: Correct/Actual: {}/{}",
      new Object[] {getDisplayName(), grouperGroupInfo, correctTSUsers.size(), currentMatches.size()});

    LOG.debug("{}: Full-sync comparison: Correct: {}", getDisplayName(), correctTSUsers);
    LOG.debug("{}: Full-sync comparison: Actual: {}", getDisplayName(), currentMatches);


    // EXTRA MATCHES = CURRENT_MATCHES - CORRECT_MATCHES
    Set<LdapUser> extraMatches = new HashSet<LdapUser>(currentMatches);
    extraMatches.removeAll(correctTSUsers);

    stats.deleteCount.set(extraMatches.size());
    
    for (LdapUser extraMatch : extraMatches)
      scheduleUserModification(extraMatch, AttributeModificationType.REMOVE, Arrays.asList(attributeValue));
            
    // MISSING MATCHES = CORRECT_MATCHES - CURRENT_MATCHES
    Set<LdapUser> missingMatches = new HashSet<LdapUser>((Set<LdapUser>)correctTSUsers);
    missingMatches.removeAll(currentMatches);

    stats.insertCount.set(missingMatches.size());

    for (LdapUser missingMatch : missingMatches)
      scheduleUserModification(missingMatch, AttributeModificationType.ADD, Arrays.asList(attributeValue));

    LOG.info("{}: Brief full-sync summary: Correct={}, Current={}, Extra={}, Missing={}", 
        new Object[] {getDisplayName(), correctSubjects.size(), currentMatches_ldapObjects.size(),
        extraMatches.size(), missingMatches.size()});
    
  }


  protected String getAttributeValueForGroup(GrouperGroupInfo grouperGroupInfo) throws PspException {
    return evaluateJexlExpression("ProvisionedAttributeValue", config.getProvisionedAttributeValueFormat(), null, null, grouperGroupInfo, null);
  }
  
  @Override
	protected void doFullSync_cleanupExtraGroups(JobStatistics stats) throws PspException {

      String attribute = config.getProvisionedAttributeName();

      // This will either be
      //   null or empty: No cleanup will occur
      //   *: Attribute is entirely determined by grouper and will be completely scrubbed
      //   a true prefix: Only values that start with that prefix will be inspected/scrubbed

      String allValuesPrefix = config.getAllProvisionedValuesPrefix();
      if ( StringUtils.isEmpty(allValuesPrefix) ) {
        LOG.error("{}: Unable to cleanup extra groups without allProvisionedValuesPrefix being defined. Set the prefix to * if you want grouper to fully control the attribute.", getDisplayName());
        return;
      }

      String allValuesLdapFilter;
      Pattern allValuesPattern;

      if ( allValuesPrefix.equals("*") ) {
          allValuesLdapFilter = String.format("%s=*", attribute);
          allValuesPattern = Pattern.compile(".*");
      }
      else {
          allValuesLdapFilter = String.format("%s=%s*", attribute, PspJexlUtils.escapeLdapFilter(allValuesPrefix));
          allValuesPattern = Pattern.compile(Pattern.quote(allValuesPrefix) + ".*");
      }

      LOG.debug("{}: Looking for all grouper-sourced values of {}.", getDisplayName(), attribute);

      Set<String> allValuesOfAttribute = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      List<LdapObject> usersWithGrouperValues 
        = getLdapSystem().performLdapSearchRequest(config.getUserSearchBaseDn(), SearchScope.SUBTREE, 
            Arrays.asList(attribute), allValuesLdapFilter);
      
      // We're going to go through all the values of all the ldap objects. 
      // We're going to save all those values that come from grouper (because they match 'pattern')
      Set<String> grouperSourcedValuesUsedInTargetSystem = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      for ( LdapObject user : usersWithGrouperValues ) {
        Collection<String> values = user.getStringValues(attribute);
        allValuesOfAttribute.addAll(values);

        for ( String value : values ) {
          if ( allValuesPattern.matcher(value).matches() )
            grouperSourcedValuesUsedInTargetSystem.add(value);
        }
      }

      // Now we go through all the grouper groups and see what their attribute values
      // would be. 
      Set<String> valuesDefinedByGrouperGroups = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      for ( GrouperGroupInfo groupInfo : getAllGroupsForProvisioner() ) {
        String value = getAttributeValueForGroup(groupInfo);
        valuesDefinedByGrouperGroups.add(value);
      }

      LOG.debug("All values of attributes found in LDAP: {}", allValuesOfAttribute);
      LOG.debug("Values of attribute found in LDAP that match grouper-responsible pattern: {}", grouperSourcedValuesUsedInTargetSystem);
      LOG.debug("Values of attribute corresponding to existing grouper groups: {}", valuesDefinedByGrouperGroups);
      
      // Now we know what values are used by current groups and what values are in the ldap server
      // Subtract these and we know what values to remove
      Set<String> extraValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      extraValues.addAll(grouperSourcedValuesUsedInTargetSystem);
      extraValues.removeAll(valuesDefinedByGrouperGroups);
      
      LOG.info("{}: There are {} values that should be purged from the target system", getDisplayName(), extraValues.size());
      LOG.debug("{}: Values that should be purged: {}", getDisplayName(), extraValues);
      
      for (String extraValue : extraValues ) {
        LOG.info("{}: Purging attribute value {}", getDisplayName(), extraValue);
        purgeAttributeValue(attribute, extraValue, stats);
      }
	}



  @Override
  protected LdapGroup createGroup(GrouperGroupInfo grouperGroup, Collection<Subject> initialMembers)
      throws PspException {
    // We don't use LdapGroups, so there is nothing to do
    return null;
  }



  @Override
  protected void deleteGroup(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup)
      throws PspException {
    String attributeName = config.getProvisionedAttributeName();
    String attributeValue = getAttributeValueForGroup(grouperGroupInfo);

    purgeAttributeValue(attributeName, attributeValue, new JobStatistics());
  }
  

  protected void purgeAttributeValue(String attributeName, String valueToPurge, JobStatistics stats) throws PspException {
    SearchFilter filter = new SearchFilter(attributeName + "={0}");
    filter.setParameter(0, valueToPurge);
    
    List<LdapObject> objectsWithAttribute = getLdapSystem().performLdapSearchRequest(
        new SearchRequest(config.getUserSearchBaseDn(), filter));
    
    LOG.info("{}: There are {} ldap objects with attribute value={}", 
        new Object[] {getDisplayName(), objectsWithAttribute.size(), valueToPurge});

    stats.deleteCount.addAndGet(objectsWithAttribute.size());

    for ( LdapObject objectWithAttribute : objectsWithAttribute )
      scheduleUserModification(new LdapUser(objectWithAttribute), AttributeModificationType.REMOVE, Arrays.asList(valueToPurge));
  }

  
  @Override
  protected Map<GrouperGroupInfo, LdapGroup> fetchTargetSystemGroups(
      Collection<GrouperGroupInfo> grouperGroups) throws PspException {
    // We don't use LdapGroups, so just return an empty map;
    return Collections.EMPTY_MAP;
  }
}
