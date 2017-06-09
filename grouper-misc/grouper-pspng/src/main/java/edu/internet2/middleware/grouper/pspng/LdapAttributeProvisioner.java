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
  
  public LdapAttributeProvisioner(String provisionerName, LdapAttributeProvisionerConfiguration config) {
    super(provisionerName, config);
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
    uncacheUser(null, ldapUser); 
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
      LOG.warn("{}: Skipping addMembership: LdapUser does not exist for subject {}", getName(), subject.getId());
      return;
    }
    
    String attributeValue = evaluateJexlExpression(config.getProvisionedAttributeValueFormat(), subject, ldapUser, grouperGroupInfo, null);
    
    scheduleUserModification(ldapUser, AttributeModificationType.ADD, Arrays.asList(attributeValue));
  }

  @Override
  protected void deleteMembership(GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup,
      Subject subject, LdapUser ldapUser) throws PspException {

    if ( ldapUser == null ) {
      LOG.warn("{}: Skipping deleteMembership: LdapUser does not exist for subject {}", getName(), subject.getId());
      return;
    }
    
    String attributeValue = evaluateJexlExpression(config.getProvisionedAttributeValueFormat(), subject, ldapUser, grouperGroupInfo, null);

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
        new Object[] {getName(), correctSubjects.size(), currentMatches_ldapObjects.size(),
        extraMatches.size(), missingMatches.size()});
    
  }


  protected String getAttributeValueForGroup(GrouperGroupInfo grouperGroupInfo) {
    return evaluateJexlExpression(config.getProvisionedAttributeValueFormat(), null, null, grouperGroupInfo, null);
  }
  
  @Override
	protected void doFullSync_cleanupExtraGroups(
			Set<GrouperGroupInfo> groupsForThisProvisioner,
			Map<GrouperGroupInfo, LdapGroup> ldapGroups,
            JobStatistics stats) throws PspException {
      String allValuesPrefix = config.getAllProvisionedValuesPrefix();
      if ( StringUtils.isEmpty(allValuesPrefix) ) {
        LOG.error("{}: Unable to cleanup extra groups without allProvisionedValuesPrefix being defined", getName());
        return;
      }
      
      String attribute = config.getProvisionedAttributeName();

      LOG.debug("{}: Looking for all grouper-sourced values of {}.", getName(), attribute);
      
      List<LdapObject> usersWithGrouperValues 
        = getLdapSystem().performLdapSearchRequest(config.getUserSearchBaseDn(), SearchScope.SUBTREE, 
            Arrays.asList(attribute), attribute+"="+allValuesPrefix+"*");
      
      // We're going to go through all the values of all the ldap objects. 
      // We're going to save all those values that come from grouper (because they match 'pattern')
      Pattern pattern = Pattern.compile(allValuesPrefix + ".*");
      Set<String> grouperSourcedValuesUsedInTargetSystem = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      for ( LdapObject user : usersWithGrouperValues ) {
        Collection<String> values = user.getStringValues(attribute);
        for ( String value : values ) {
          if ( pattern.matcher(value).matches() )
            grouperSourcedValuesUsedInTargetSystem.add(value);
        }
      }

      // Now we go through all the grouper groups and see what their attribute values
      // would be. 
      Set<String> valuesDefinedByGrouperGroups = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      for ( GrouperGroupInfo groupInfo : groupsForThisProvisioner ) {
        String value = getAttributeValueForGroup(groupInfo);
        valuesDefinedByGrouperGroups.add(value);
      }
      
      
      // Now we know what values are used by current groups and what values are in the ldap server
      // Subtract these and we know what values to remove
      Set<String> extraValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      extraValues.addAll(grouperSourcedValuesUsedInTargetSystem);
      extraValues.removeAll(valuesDefinedByGrouperGroups);
      
      LOG.info("{}: There are {} values that should be purged from the target system", getName(), extraValues.size());
      
      for (String extraValue : extraValues ) {
        LOG.info("{}: Purging attribute value {}", getName(), extraValue);
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
        new Object[] {getName(), objectsWithAttribute.size(), valueToPurge});

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
