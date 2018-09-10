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

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RDN;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.ldaptive.*;
import org.ldaptive.io.LdifReader;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;



/**
 * This (abstract) class consolidates the common aspects of provisioning to LDAP-based targets.
 * This includes
 *   -Configuring and building (ldaptive) LDAP connection pools
 *   -PersonSubject-to-LdapObject searching and caching.
 *   -Consolidating/Batching ldap modifications into as few modifications as possible.
 *   
 * @author Bert Bee-Lindgren
 *
 */
public abstract class LdapProvisioner <ConfigurationClass extends LdapProvisionerConfiguration> 
extends Provisioner<ConfigurationClass, LdapUser, LdapGroup>
{
  // Used to save a list of LDAP MODIFICATIONS in a ProvisioningWorkItem
  private static final String LDAP_MOD_LIST = "LDAP_MODS";

  final GrouperCache<Subject, LdapObject> userCache_subject2User;

  private Set<DN> existingOUs = new HashSet<DN>();
  protected LdapSystem ldapSystem;
  
  /**
   * LDAP ResultCodes that might occur from a schema-related violation, for example when
   * the last member is removed from an LdapGroup that requires a member
   */
  public static Set<ResultCode> schemaRelatedLdapErrors = new HashSet<>();
  static {
    schemaRelatedLdapErrors.add(ResultCode.CONSTRAINT_VIOLATION);
    schemaRelatedLdapErrors.add(ResultCode.LDAP_NOT_SUPPORTED);
    schemaRelatedLdapErrors.add(ResultCode.UNWILLING_TO_PERFORM);
    schemaRelatedLdapErrors.add(ResultCode.OBJECT_CLASS_VIOLATION);
  }
  
  public LdapProvisioner(String provisionerName, ConfigurationClass config, boolean fullSyncMode)
  {
    super(provisionerName, config, fullSyncMode);
    
    LOG.debug("Constructing LdapProvisioner: {}", provisionerName);

    // These use getDisplayName instead of
    userCache_subject2User 
      = new GrouperCache<Subject, LdapObject>(String.format("PSP-%s-LdapUserCache", getDisplayName()),
          config.getLdapUserCacheSize(),
          false,
          config.getLdapUserCacheTime_secs(),
          config.getLdapUserCacheTime_secs(),
          false);

    // Make sure we can connect
    try {
      if (!getLdapSystem().test()) {
        throw new RuntimeException("Unable to make ldap connection");
      }
    } catch (PspException e) {
      LOG.error("{}: Unable to make ldap connection", getDisplayName(), e);
      throw new RuntimeException("Unable to make ldap connection");
    }
  }

  
  /**
   * Find the subjects in the ldap server.
   * 
   * If account-creation is enabled with createMissingAccounts, this will create missing entries.
   * @param subjectsToFetch
   * @return
   */
  protected Map<Subject, LdapUser> fetchTargetSystemUsers( Collection<Subject> subjectsToFetch) 
      throws PspException {
    LOG.info("Fetching {} users from target system", subjectsToFetch.size());
    
    if ( subjectsToFetch.size() > config.getUserSearch_batchSize() )
      throw new IllegalArgumentException("LdapProvisioner.fetchTargetSystemUsers: invoked with too many subjects to fetch");
    
    StringBuilder combinedLdapFilter = new StringBuilder();
    
    // Start the combined ldap filter as an OR-query
    combinedLdapFilter.append("(|");
    
    for ( Subject subject : subjectsToFetch ) {
      SearchFilter f = getUserLdapFilter(subject);
      
      String filterString = f.format();
      
      // Wrap the subject's filter in (...) if it doesn't start with (
      if ( filterString.startsWith("(") )
        combinedLdapFilter.append(filterString);
      else
        combinedLdapFilter.append('(').append(filterString).append(')');
    }
    combinedLdapFilter.append(')');

    // Actually do the search
    List<LdapObject> searchResult;
    
    try {
      searchResult = getLdapSystem().performLdapSearchRequest(
        new SearchRequest(config.getUserSearchBaseDn(), 
              combinedLdapFilter.toString(), 
              config.getUserSearchAttributes()));
      
      LOG.info("Read {} user objects from directory", searchResult.size());
    }
    catch (PspException e) {
      LOG.error("Problem searching for subjects with filter {} on base {}", 
          new Object[] {combinedLdapFilter, config.getUserSearchBaseDn(), e} );
      throw e;
    }
    
    // Now we have a bag of LdapObjects, but we don't know which goes with which subject.
    // Generally, we're going to go through the Subjects and their filters and compare
    // them to the Ldap data we've fetched into memory.
    // 
    // This is complicated a bit because Ldaptive doesn't have a way to run a filter in memory
    // against an LdapObject. Therefore, we're going to use unboundid classes to do
    // some of this work.
    Map<Subject, LdapUser> result = new HashMap<Subject, LdapUser>();

    Set<LdapObject> matchedFetchResults = new HashSet<LdapObject>();
    
    // For every subject we tried to bulk fetch, find the matching LdapObject that came back
    for ( Subject subjectToFetch : subjectsToFetch ) {
      SearchFilter f = getUserLdapFilter(subjectToFetch);
          
      for ( LdapObject aFetchedLdapObject : searchResult ) {
        if ( aFetchedLdapObject.matchesLdapFilter(f)) {
          result.put(subjectToFetch, new LdapUser(aFetchedLdapObject));
          matchedFetchResults.add(aFetchedLdapObject);
          break;
        }
      }
    }

    Set<LdapObject> unmatchedFetchResults = new HashSet<LdapObject>(searchResult);
    unmatchedFetchResults.removeAll(matchedFetchResults);
    
    for ( LdapObject unmatchedFetchResult : unmatchedFetchResults )
      LOG.error("{}: User data from ldap server was not matched with a grouper subject "
          + "(perhaps attributes are used in userSearchFilter ({}) that are not included "
          + "in userSearchAttributes ({})?): {}",
          new Object[] {getDisplayName(), config.getUserSearchFilter(), config.getUserSearchAttributes(),
          unmatchedFetchResult.getDn()});
    
    return result;
  }


  protected SearchFilter getUserLdapFilter(Subject subject) throws PspException  {
    String result = evaluateJexlExpression("UserSearchFilter", config.getUserSearchFilter(), subject, null, null, null);
    if ( StringUtils.isEmpty(result) )
      throw new RuntimeException("User searching requires userSearchFilter to be configured correctly");
    
    // If the filter contains '||', then this filter is requesting parameter substitution
    String filterPieces[] = result.split("\\|\\|");
    SearchFilter filter = new SearchFilter(filterPieces[0]);
    for (int i=1; i<filterPieces.length; i++)
      filter.setParameter(i-1, filterPieces[i].trim());
    
    LOG.debug("{}: User LDAP filter for subject {}: {}",
        new Object[]{getDisplayName(), subject.getId(), filter});
    return filter;
  }
  
  @Override
  protected LdapUser createUser(Subject personSubject) throws PspException {
    GrouperUtil.assertion(config.isCreatingMissingUsersEnabled(), "Can't create users unless createMissingUsers is enabled");
    GrouperUtil.assertion(StringUtils.isNotEmpty(config.getUserCreationLdifTemplate()), "Can't create users unless userCreationLdifTemplate is defined");
    GrouperUtil.assertion(StringUtils.isNotEmpty(config.getUserCreationBaseDn()), "Can't create users unless userCreationBaseDn is defined");
    
    LOG.info("Creating LDAP account for Subject: {} ", personSubject);
    String ldif = config.getUserCreationLdifTemplate();
    ldif = ldif.replaceAll("\\|\\|", "\n");
    ldif = evaluateJexlExpression("UserTemplate", ldif, personSubject, null, null, null);
    
    Connection conn = getLdapSystem().getLdapConnection();
    try {
      Reader reader = new StringReader(ldif);
      LdifReader ldifReader = new LdifReader(reader);
      SearchResult ldifResult = ldifReader.read();
      LdapEntry ldifEntry = ldifResult.getEntry();
      
      // Update DN to be relative to userCreationBaseDn
      String actualDn = String.format("%s,%s", ldifEntry.getDn(),config.getUserCreationBaseDn());
      ldifEntry.setDn(actualDn);

      performLdapAdd(ldifEntry);
      
      // Read the acount that was just created
      LOG.debug("Reading account that was just added to ldap server: {}", personSubject);
      return fetchTargetSystemUser(personSubject);
    } catch (PspException e) {
      LOG.error("Problem while creating new user: {}: {}", ldif, e);
      throw e;
    } catch ( IOException e ) {
      LOG.error("Problem while processing ldif to create new user: {}", ldif, e);
      throw new PspException("LDIF problem creating user: %s", e.getMessage());
    }
    finally {
      conn.close();
    }
  }

  @Override
  protected void populateJexlMap(Map<String, Object> variableMap, Subject subject,
      LdapUser ldapUser, GrouperGroupInfo grouperGroupInfo, LdapGroup ldapGroup) {
    
    super.populateJexlMap(variableMap, subject, ldapUser, grouperGroupInfo, ldapGroup);
    
    if ( ldapGroup != null )
      variableMap.put("ldapGroup", ldapGroup.getLdapObject());
    if ( ldapUser != null )
      variableMap.put("ldapUser", ldapUser.getLdapObject());
  }
  /**
   * Note that the given {@link ProvisioningWorkItem} needs the given {@link ModifyRequest} done.
   * 
   * These are not done right away so that multiple modifications can be implemented together
   * in batches. For example, LDAP servers can generally process a single ldap modification that
   * adds 10 values to an attribute MUCH faster than processing 10 single-value Modify-Add operations.
   * @param operation
   */
  protected void scheduleLdapModification(ModifyRequest operation) {
    ProvisioningWorkItem workItem = getCurrentWorkItem();
    LOG.info("{}: Scheduling ldap modification: {}", getDisplayName(), operation);
    
    workItem.addValueToProvisioningData(LDAP_MOD_LIST, operation);
  }
  
  /**
   * This implements the LDAP Modifications that were scheduled with schedulLdapModification.
   * Those scheduled changes are stored within the ProvisioningWorkItems that are passed around.
   * 
   * In order to be fast, we first try to coalesce the changes across ProvisioningWorkItems.
   * 
   * If all the fancy, coalescing LDAP-implementation fails, each workItem's LDAP operations
   *  will be done individually so problems will be tracked down to specific workItem(s).
   */
  @Override
  public void finishProvisioningBatch(List<ProvisioningWorkItem> workItems) throws PspException {
    try {
      MDC.put("step", "coalesced");
      makeCoalescedLdapChanges(workItems);

      // They all worked, so mark them all as successful
      for ( ProvisioningWorkItem workItem : workItems )
        workItem.markAsSuccess("Modification complete");
      
    } catch (PspException e1) {
      LOG.warn("(THIS WILL BE RETRIED) Optimized, coalesced ldap provisioning failed: {}", e1.getMessage());
      LOG.warn("RETRYING: Performing slower, unoptimized ldap provisioning after optimized provisioning failed");
      
        for ( ProvisioningWorkItem workItem : workItems ) {
          try {
            MDC.put("step", "ldap_retry:"+workItem.getMdcLabel());
            makeIndividualLdapChanges(workItem);
            workItem.markAsSuccess("Modification complete");
          } catch (PspException e2) {
            LOG.error("Simple ldap provisioning failed for {}", workItem, e2);
            workItem.markAsFailure("Modification failed: %s", e2.getMessage());
          }
      }
    }
    finally {
      MDC.remove("step");
    }
    
    super.finishProvisioningBatch(workItems);
  }

  /**
   * This ldap implementation is made complicated by strong desires to be fast, 
   * specifically:
   * + Pull changes made to common objects changed by several WorkItems together so they
   * can be made in single ldap operations.
   * + Chunk these changes into reasonable-sized pieces
   * 
   * Note, this involves the following steps:
   * 1) Find all the ModifyRequests for a dn
   * 2) Pull apart the ModifyRequests apart and find the AttributeModifications they contain
   * 3) For Modify-Add and Modify-Delete operations, pull out all the added and deleted values
   * and put them together in as few Modify-Add and Modify-Delete operations as is reasonable
   * 4) Combine each attribute's changes into a list of values to ADD and a list of values to DELETE
   * 5) Break long lists of attribute values into bite-sized chunks
   * 6) Make a new Modification request for each dn that contains all the AttributeModifications
   * for that dn

 * @param workItems
 * @throws PspException
 */
  private void makeCoalescedLdapChanges(List<ProvisioningWorkItem> workItems) throws PspException {
    LOG.debug("{}: Making coalescedLdapChanges", getDisplayName());

    // Assemble and execute all the LDAP_MOD_LIST values saved up in workItems
    MultiMap dn2Mods = new MultiValueMap();
    
    // Sort all the necessary operations by the DN that they modify
    for ( ProvisioningWorkItem workItem : workItems ) {
      List<ModifyRequest> mods = (List) workItem.getProvisioningDataValues(LDAP_MOD_LIST);
      
      // Obviously there is nothing to coalesce if no mods were necessary for this work item
      if ( mods == null ) 
        continue;
      
      LOG.info("{}: WorkItem {} needs {} ldap modifications", 
          new Object[]{getDisplayName(), workItem, mods.size()} );
      for ( ModifyRequest mod : mods ) {
        LOG.debug("{}: Mod for WorkItem: {}", getDisplayName(), getLoggingSummary(mod));
        dn2Mods.put(mod.getDn(), mod);
      }
    }
    
    // Now loop through the DNs that need to be modified
    for ( String dn : (Collection<String>) dn2Mods.keySet() ) {
      // These are all the modifications that were assembled across our provisioning batch
      Collection<ModifyRequest> modsForDn = (Collection<ModifyRequest>) dn2Mods.get(dn);

      // This will hold the actual operations that are necessary.
      // This is a List of List<LDAP Modifications that should be done together>
      //
      // There will be more than one element in coalescedOperations when a DN has operations
      // that are so large that they need to be broken into bite-sized chunks
      List<List<AttributeModification>> coalescedOperations = new ArrayList<List<AttributeModification>>();
      
      // We know we're going to need at least one operation (since this DN was in dn2Mods), 
      // so put an empty list of mods here to keep things simpler below
      coalescedOperations.add(new ArrayList<AttributeModification>());
      
      // Sort all the attribute values modified by these modifications into one DEL & ADD
      // list for each attribute. 
      MultiMap attribute2ValuesToAdd = new MultiValueMap();
      MultiMap attribute2ValuesToDel = new MultiValueMap();
      
      for ( ModifyRequest mod : modsForDn ) {
        for ( AttributeModification attributeMod : mod.getAttributeModifications() ) {
          LdapAttribute attribute = attributeMod.getAttribute();
          
          switch (attributeMod.getAttributeModificationType() ) {
            case ADD: 
              for ( String value : attribute.getStringValues() )
                attribute2ValuesToAdd.put(attribute.getName(), value);
              break;
            case REMOVE: 
              for ( String value : attribute.getStringValues() )
                attribute2ValuesToDel.put(attribute.getName(), value);
              break;
            case REPLACE: 
            default:
              // We don't know how to combine these, so just do the operation as is
              // in our first eventual operation
              coalescedOperations.get(0).add(attributeMod);  
          }
        }
      }
      
      int maxValues = config.getMaxValuesToChangePerOperation();
      
      // Create a single value-removal for each attribute that needs values removed
      // Loop through the attributes that had values removed from them
      //
      // NOTE: We're doing removals first so that if an value is both added and removed
      // (because there were 2+ workItems that conflicted with each other), the ADD will be
      // done last and will 'stick.' If the removal is supposed to be the one that sticks, then
      // it will be taken care of at full-sync time.
      
      // TODO: Figure out what workItems conflict and make sure those groups are full-sync'ed
      // first

      for ( String attributeName : (Collection<String>) attribute2ValuesToDel.keySet() ) {
        Collection<String> valuesToRemove = (Collection<String>) attribute2ValuesToDel.get(attributeName);
        if (valuesToRemove == null ) {
          valuesToRemove = Collections.EMPTY_LIST;
        }

        Collection<String> valuesToAdd = (Collection<String>) attribute2ValuesToAdd.get(attributeName);
        if ( valuesToAdd == null ) {
          valuesToAdd = Collections.EMPTY_LIST;
        }
        
        // Find the intersection between the values to add and remove
        Set<String> valuesWithConflictingOperations = new HashSet<String>(valuesToRemove);
        valuesWithConflictingOperations.retainAll(valuesToAdd);
        
        if ( valuesWithConflictingOperations.size() > 0 ) {
          LOG.warn("Found {} conflicting ldap operations in event batch. Scheduling a full sync on affected groups", valuesWithConflictingOperations.size());

          Set<GrouperGroupInfo> groupsNeedingFullSync = new HashSet<>();
          
          // Go through all the conflicting values and find the groups involved in the conflicts
          for ( String conflictingProvisioningAttributeValue : valuesWithConflictingOperations ) {
            // Look for the workItem that needed these values to be provisioned
            for ( ProvisioningWorkItem workItem : workItems ) {
              if ( isWorkItemMakingChange(workItem, dn, attributeName, conflictingProvisioningAttributeValue) ) {
                groupsNeedingFullSync.add(workItem.getGroupInfo(this));
              }
            }
          }
        }
      }
        

      for ( String attributeName : (Collection<String>) attribute2ValuesToDel.keySet() ) {
        Collection<String> values = (Collection<String>) attribute2ValuesToDel.get(attributeName);
        List<List<String>> valueChunks = PspUtils.chopped(values, maxValues);
        
        for (int i=0; i<valueChunks.size(); i++) {
          List<String> valueChunk = valueChunks.get(i);
          
          LdapAttribute attribute = new LdapAttribute(attributeName, GrouperUtil.toArray(valueChunk, String.class));
          AttributeModification mod = new AttributeModification(AttributeModificationType.REMOVE, attribute);
          
          // Grow our list of operations if necessary
          if ( coalescedOperations.size() <= i )
            coalescedOperations.add(new ArrayList<AttributeModification>());
          
          coalescedOperations.get(i).add(mod);
        }
      }
      

      
      // Create a single value-add for each attribute that needs values added
      // Loop through the attributes that had values added to them
      for ( String attributeName : (Collection<String>) attribute2ValuesToAdd.keySet() ) {
        Collection<String> values = (Collection<String>) attribute2ValuesToAdd.get(attributeName);
        List<List<String>> valueChunks = PspUtils.chopped(values, maxValues);
        
        for (int i=0; i<valueChunks.size(); i++) {
          List<String> valueChunk = valueChunks.get(i);
          
          LdapAttribute attribute = new LdapAttribute(attributeName, GrouperUtil.toArray(valueChunk, String.class));
          AttributeModification mod = new AttributeModification(AttributeModificationType.ADD, attribute);
          
          // Grow our list of operations if necessary
          if ( coalescedOperations.size() <= i )
            coalescedOperations.add(new ArrayList<AttributeModification>());
          
          coalescedOperations.get(i).add(mod);
        }
      }
      
      Connection conn = getLdapSystem().getLdapConnection();
      try {
        for ( List<AttributeModification> operation : coalescedOperations ) {
          ModifyRequest mod = new ModifyRequest(dn, GrouperUtil.toArray(operation, AttributeModification.class));
          try {
            conn.open();
            
            LOG.info("Performing LDAP modification: {}", getLoggingSummary(mod) );
            conn.getProviderConnection().modify(mod);
          } catch (LdapException e) {
            LOG.warn("(THIS WILL BE RETRIED) Problem doing coalesced ldap modification: {} / {}: {}",
                new Object[]{dn, mod, e.getMessage()});
            throw new PspException("Coalesced LDAP Modification failed: %s",e.getMessage());
          } 
        }
      } finally {
        conn.close();
      }
    }
  }


  protected boolean isWorkItemMakingChange(
      ProvisioningWorkItem workItem,
      String dn, String attributeName, String provisioningAttributeValue) {
    
    @SuppressWarnings("unchecked")
    List<ModifyRequest> modRequests = (List) workItem.getProvisioningDataValues(LDAP_MOD_LIST);
    
    // This is complicated and nested because of the data structures involved, but it boils down to looking 
    // through all the ldap changes and compare the following: DN, AttributeName, AttributeValue
    for ( ModifyRequest modRequest : modRequests ) {
      // Does the DN match?
      if ( dn.equalsIgnoreCase(modRequest.getDn()) ) {
        // Go through the attribute changes within the modRequest...
        for ( AttributeModification attributeMod : modRequest.getAttributeModifications()) {
          if ( attributeMod.getAttribute().getName().equalsIgnoreCase(attributeName) ) {
            for ( String modValue : attributeMod.getAttribute().getStringValues() ) {
              if ( modValue.equalsIgnoreCase(provisioningAttributeValue) ) {
                
                // Everything matches, so this is a match
                
                return true;
              }
            }
          }
        }
      }
    }
    
    return false;
  }

  
  /**
   * This method is a backup plan to makeCoalescedLdapChanges and takes a simple approach 
   * to ldap provisioning. This is useful for two reasons: 1) It might work around a bug
   * buried in the complexity of coalescing changes; 2) It tells us which workItems 
   * have a problem because each workItem is done separately. 
   * @param workItem
   */
  private void makeIndividualLdapChanges(ProvisioningWorkItem workItem) throws PspException {
    List<ModifyRequest> mods = (List) workItem.getProvisioningDataValues(LDAP_MOD_LIST);
    
    if ( mods == null ) {
      LOG.debug("{}: No ldap changes are necessary for work item {}", getDisplayName(), workItem);
      return;
    }
    
    LOG.debug("{}: Implementing changes for work item {}", getDisplayName(), workItem);
    for ( ModifyRequest mod : mods ) {
      try {
        getLdapSystem().performLdapModify(mod);
      } catch (PspException e) {
        LOG.error("{}: Ldap provisioning failed for {} / {}", new Object[]{getDisplayName(), workItem, mod, e});

        throw e;
      }
    }
  }

  protected LdapSystem getLdapSystem() throws PspException {
    if ( ldapSystem != null )
      return ldapSystem;
    
    // Make sure we only build a single LdapSystem
    synchronized (this) {
      // See if another thread build the LdapSystem while we were waiting
      // for the mutex
      if ( ldapSystem != null )
        return ldapSystem;
      
      ldapSystem = new LdapSystem(config.getLdapPoolName(), config.isActiveDirectory());
      return ldapSystem;
    }
  }

  private String getLoggingSummary(ModifyRequest modForDn) {
    if ( modForDn == null )
      return "no changes";
    
    StringBuilder sb = new StringBuilder();
    // Put the first two DN components into buffer
    sb.append(LdapObject.getDnSummary(modForDn.getDn(), 2));

    for ( AttributeModification attribute : modForDn.getAttributeModifications()) {
      switch (attribute.getAttributeModificationType()) {
        case ADD: sb.append(String.format("[%s: +%d value(s)]",
                      attribute.getAttribute().getName(),
                      attribute.getAttribute().getStringValues().size()));
        break;
        
        case REMOVE: sb.append(String.format("[%s: -%d value(s)]",
            attribute.getAttribute().getName(),
            attribute.getAttribute().getStringValues().size()));
        break;
        
        case REPLACE: sb.append(String.format("[%s: =%d value(s)]",
            attribute.getAttribute().getName(),
            attribute.getAttribute().getStringValues().size()));
        break;
      }
    }
  
    return sb.toString();
  }


  /**
   * Public way to create any missing OUs.
   *
   * @param dnString
   * @param wholeDnIsTheOu false: The top of the DN is not an OU (eg, cn=group,ou=folder1,ou=folder2,dc=example).
   *                       true: The top of the DN is an OU (eg, ou=folder1, ou=folder2, dc=example).
   * @throws PspException
   */
  public void ensureLdapOusExist(String dnString, boolean wholeDnIsTheOu) throws PspException {
    LOG.info("{}: Checking for (and creating) missing OUs in DN: {} (wholeDnIsOu={})",
            new Object[]{getDisplayName(), dnString, wholeDnIsTheOu});

    DN startingDn;
    try {
      startingDn = new DN(dnString);

      if ( wholeDnIsTheOu ) {
        ensureLdapOusExist(startingDn);
      } else {
        ensureLdapOusExist(startingDn.getParent());
      }
    } catch (LDAPException e) {
      LOG.error("Problem parsing DN {}", dnString, e);
      throw new PspException("Problem parsing DN: %s", dnString);
    }

  }


  /**
   * Internal worker function called by ensureLdapOusExist(dnString, wholeDnIsTheOu).
   *
   * This function reads a dn and if it doesn't already exist, then it makes sure the
   * parent dn exists (with a recursive call) and then creates an ou at the dn location
   * by calling createOuInExistingLocation(dn).
   *
   * @param dn
   * @throws PspException
   */
  protected void ensureLdapOusExist(DN dn) throws PspException {
    if ( dn.isNullDN() ) {
      throw new PspException("Never found an existing DN component when creating OUs");
    }


    if ( existingOUs.contains(dn) ) {
      LOG.debug("{}: OU is known to exist: {}", getDisplayName(), dn.toMinimallyEncodedString());
      return;
    }

    LOG.debug("{}: Checking to see if ou exists: {}", getDisplayName(), dn);
    try {
        if ( getLdapSystem().performLdapRead(dn) != null ) {
          // OU already exists
          existingOUs.add(dn);
          return;
        } else {
          // OU doesn't already exist. Make sure parent exists and then create new OU
          ensureLdapOusExist(dn.getParent());
          createOuInExistingLocation(dn);
          existingOUs.add(dn);
        }
    }
    catch (PspException e) {
        LOG.error("{}: Creating OU failed: {}", new Object[]{getDisplayName(), dn, e});
        throw new PspException("Unable to find existing OU nor create new one (%s)", e.getMessage());
    }
  }


  /**
   * This function creates an OU with the provided DN with the OU-Creation ldif template.
   *
   * This function assumes that the parent DN of ouDn exists. In other words, this function
   * will not try to create any parent OUs.
   *
   * @param ouDn
   * @throws PspException
   */
  protected void createOuInExistingLocation(DN ouDn) throws PspException {
    String ouDnString = ouDn.toMinimallyEncodedString();

    LOG.info("{}: Creating OU: {}", getDisplayName(), ouDnString);

    RDN topRDN = ouDn.getRDN();

    // Get the attribute information recorded in the first RDN
    LdapAttribute topRdnAttribute = new LdapAttribute(topRDN.getAttributeNames()[0]);
    topRdnAttribute.addStringValue( topRDN.getAttributeValues());

    String ldif = evaluateJexlExpression("OuTemplate", config.getOuCreationLdifTemplate(),
            null, null,
            null, null,
            "dn", ouDn.toMinimallyEncodedString(),
            "ou", topRdnAttribute.getStringValue());
    ldif = ldif.replaceAll("\\|\\|", "\n");

    try {
      Reader reader = new StringReader(ldif);
      LdifReader ldifReader = new LdifReader(reader);
      SearchResult ldifResult = ldifReader.read();
      LdapEntry ldifEntry = ldifResult.getEntry();

      // Add the current attribute from the RDN if it was not already in the ldif template
      if ( ldifEntry.getAttribute( topRdnAttribute.getName() ) == null ) {
        ldifEntry.addAttribute(topRdnAttribute);
      }

      performLdapAdd(ldifEntry);
    } catch ( IOException e ) {
      LOG.error("{}: Problem while processing ldif to create new OU: {}", new Object[] {getDisplayName(), ldif, e});
      throw new PspException("LDIF problem creating OU: %s", e.getMessage());
    }
  }

  /**
   * Perform an LDAP ADD after making sure the new object's OU exists.
   * @param entryToAdd
   * @throws PspException
   */
  protected void performLdapAdd(LdapEntry entryToAdd) throws PspException {
    LOG.info("{}: Creating LDAP object: {}", getDisplayName(), entryToAdd.getDn());

    ensureLdapOusExist(entryToAdd.getDn(), false);
    ldapSystem.performLdapAdd(entryToAdd);
  }

}
