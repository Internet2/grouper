package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.stem.StemSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 * 
 */
public class GrouperDeprovisioningOverallConfiguration {

  /**
   * 
   */
  public GrouperDeprovisioningOverallConfiguration() {
    super();
    
  }

  /**
   * 
   * @param affiliation
   * @return true if has configuration for affiliation
   */
  public boolean hasConfigurationForAffiliation(String affiliation) {
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = this.getAffiliationToConfiguration().get(affiliation);

    if (grouperDeprovisioningConfiguration == null) {
      return false;
    }
    
    return grouperDeprovisioningConfiguration.getOriginalConfig() != null;

  }
  
  /**
   * is should show for removal
   * @return if show for removal
   */
  public boolean isShowForRemoval() {
    return true;
  }
  
  /**
   * 
   * @return true if auto select for removal
   */
  public boolean isAutoselectForRemoval() {
    return true;
  }

  /**
   * 
   * @param stem
   * @param includeStemConfigs
   * @return the configuration
   */
  public static Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> retrieveConfigurationForStem(Stem stem, boolean includeStemConfigs) {

    Set<Group> childGroups = stem.getChildGroups(Scope.SUB);
    Set<AttributeDef> childAttributeDefs = new AttributeDefFinder().assignParentStemId(stem.getId()).assignStemScope(Scope.SUB).findAttributes();

    Set<GrouperObject> grouperObjects = new HashSet<GrouperObject>();
    grouperObjects.addAll(childGroups);
    grouperObjects.addAll(childAttributeDefs);

    if (includeStemConfigs) {
      // get all stems below and above this one
      Set<String> stemIds = new HashSet<String>();
      stemIds.add(stem.getId());
      for (StemSet stemSet : GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stem.getId())) {
        stemIds.add(stemSet.getThenHasStemId());
      }
      for (StemSet stemSet : GrouperDAOFactory.getFactory().getStemSet().findByThenHasStemId(stem.getId())) {
        stemIds.add(stemSet.getIfHasStemId());
      }

      Set<Stem> stems = StemFinder.findByUuids(GrouperSession.staticGrouperSession(), stemIds, null);
      Map<String, Stem> stemIdToStem = new HashMap<String, Stem>();
      for (Stem theStem : stems) {
        stemIdToStem.put(theStem.getId(), theStem);
      }

      grouperObjects.addAll(stems);
    }

    Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> grouperDeprovisioningOverallConfigurationMap 
      = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObjects);
    
    if (includeStemConfigs) {
      
      OUTER: for (GrouperObject grouperObject : grouperDeprovisioningOverallConfigurationMap.keySet()) {
        
        GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = grouperDeprovisioningOverallConfigurationMap.get(grouperObject);
        
        for (String affiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
          
          GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);
          
          if (grouperDeprovisioningConfiguration != null) {
            // if direct then we dont need the parent stem
            GrouperDeprovisioningAttributeValue originalConfig = grouperDeprovisioningConfiguration.getOriginalConfig();
            if (originalConfig != null && originalConfig.isDirectAssignment()) {
              continue;
            }
          }

          // not direct, see what the parent stem is
          // note we have the assign id of the parent, but it might not be right, so look it up
          boolean isDirectParent = true;
          Stem parent = null;
          if ((!(grouperObject instanceof Stem)) || (!((Stem)grouperObject).isRootStem() )) {
            parent = grouperObject.getParentStem();
          } 
          while (true && parent != null) {

            GrouperDeprovisioningOverallConfiguration stemOverallConfiguration = grouperDeprovisioningOverallConfigurationMap.get(parent);

            if (stemOverallConfiguration != null) {

              GrouperDeprovisioningConfiguration stemDeprovisioningConfiguration = stemOverallConfiguration.getAffiliationToConfiguration().get(affiliation);

              if (stemDeprovisioningConfiguration != null) {

                GrouperDeprovisioningAttributeValue originalConfig = stemDeprovisioningConfiguration.getOriginalConfig();
                if (originalConfig != null && originalConfig.isDirectAssignment()) {

                  // make sure the stem scope is correct
                  if (isDirectParent || stemDeprovisioningConfiguration.getOriginalConfig().getStemScope() == Scope.SUB) {

                    if (grouperDeprovisioningConfiguration == null) {
                      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
                      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
                      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put(affiliation, grouperDeprovisioningConfiguration);
                    }
                    grouperDeprovisioningConfiguration.setInheritedConfig(stemDeprovisioningConfiguration);
                    //we done
                    continue OUTER;
                  }
                }
              }
            }

            if (parent.isRootStem()) {
              break;
            }
            parent = parent.getParentStem();
            isDirectParent = false;
          }

        }
        
      }
      
    }
    
    return grouperDeprovisioningOverallConfigurationMap;
  }
  
  /**
   * 
   * @param groupOrFolderOrAttributeDef
   * @return the configuration
   */
  public static GrouperDeprovisioningOverallConfiguration retrieveConfiguration(GrouperObject groupOrFolderOrAttributeDef) {
    Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> configMap = retrieveConfiguration(GrouperUtil.toSet(groupOrFolderOrAttributeDef));
    return configMap.get(groupOrFolderOrAttributeDef);
  }

  /**
   * 
   * @param groupsOrFoldersOrAttributeDefs
   * @return the configuration
   */
  public static Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> retrieveConfiguration(Set<GrouperObject> groupsOrFoldersOrAttributeDefs) {
    
    if (GrouperUtil.length(groupsOrFoldersOrAttributeDefs) == 0) {
      throw new NullPointerException("groupsOrFolders is empty");
    }

    Map<String, GrouperObject> groupMap = new HashMap<String, GrouperObject>();
    Map<String, GrouperObject> stemMap = new HashMap<String, GrouperObject>();
    Map<String, GrouperObject> attributeDefMap = new HashMap<String, GrouperObject>();

    for (GrouperObject grouperObject : groupsOrFoldersOrAttributeDefs) {

      if ((!(grouperObject instanceof Group)) && (!(grouperObject instanceof Stem)) && (!(grouperObject instanceof AttributeDef))) {
        throw new RuntimeException("groupOrFolder needs to be a stem or group or attribute def: " + grouperObject.getClass() + ", " + grouperObject);
      }

      if (grouperObject instanceof Group) {
        groupMap.put(((Group)grouperObject).getId(), grouperObject);
      }
      
      if (grouperObject instanceof Stem) {
        stemMap.put(((Stem)grouperObject).getId(), grouperObject);
      }

      if (grouperObject instanceof AttributeDef) {
        attributeDefMap.put(((AttributeDef)grouperObject).getId(), grouperObject);
      }

    }

        
//    Set<AttributeAssign> attributeAssigns = new LinkedHashSet<AttributeAssign>();

    //find all parents
    //GrouperDAOFactory.getFactory().getStemSet().findByThenHasStemId(this.parentUuid);

    Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> result = new HashMap<GrouperObject, GrouperDeprovisioningOverallConfiguration>();

    if (GrouperUtil.length(groupMap) > 0) {
//      //get all attributes and assignments for all affiliations on a group or folder
//      AttributeAssignFinder attributeAssignFinder = new AttributeAssignFinder().addAttributeDefNameId(
//          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId())
//        .assignIncludeAssignmentsOnAssignments(true);
//      attributeAssignFinder.assignOwnerGroupIds(groupIds);
//      attributeAssigns.addAll(attributeAssignFinder.findAttributeAssigns());
      
      AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerGroupIdsOfAssignAssign(groupMap.keySet())
        .addAttributeDefNameId(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId())
        .assignAttributeCheckReadOnAttributeDef(false)
        .findAttributeAssignValuesResult();
      retrieveConfigurationHelper(groupMap.values(), attributeAssignValueFinderResult, result);

    }
    if (GrouperUtil.length(stemMap) > 0) {
//      //get all attributes and assignments for all affiliations on a group or folder
//      AttributeAssignFinder attributeAssignFinder = new AttributeAssignFinder().addAttributeDefNameId(
//          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId())
//        .assignIncludeAssignmentsOnAssignments(true);
//      attributeAssignFinder.assignOwnerStemIds(stemIds);
//      attributeAssigns.addAll(attributeAssignFinder.findAttributeAssigns());

      AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerStemIdsOfAssignAssign(stemMap.keySet())
          .addAttributeDefNameId(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId())
          .assignAttributeCheckReadOnAttributeDef(false)
          .findAttributeAssignValuesResult();
      retrieveConfigurationHelper(stemMap.values(), attributeAssignValueFinderResult, result);

    }
    
    if (GrouperUtil.length(attributeDefMap) > 0) {
//    //get all attributes and assignments for all affiliations on a group or folder
//    AttributeAssignFinder attributeAssignFinder = new AttributeAssignFinder().addAttributeDefNameId(
//        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId())
//      .assignIncludeAssignmentsOnAssignments(true);
//    attributeAssignFinder.assignOwnerStemIds(stemIds);
//    attributeAssigns.addAll(attributeAssignFinder.findAttributeAssigns());

      AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerAttributeDefIdsOfAssignAssign(attributeDefMap.keySet())
        .addAttributeDefNameId(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId())
        .assignAttributeCheckReadOnAttributeDef(false)
        .findAttributeAssignValuesResult();
      retrieveConfigurationHelper(attributeDefMap.values(), attributeAssignValueFinderResult, result);

    }

    return result;
  }

  /**
   * @param groupsOrFoldersOrAttributeDefs
   * @param attributeAssignValueFinderResult
   * @param result
   */
  private static void retrieveConfigurationHelper(Collection<GrouperObject> groupsOrFoldersOrAttributeDefs,
      AttributeAssignValueFinderResult attributeAssignValueFinderResult,
      Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> result) {
    for (GrouperObject groupOrFolderOrAttributeDef : groupsOrFoldersOrAttributeDefs) {
      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration 
        = new GrouperDeprovisioningOverallConfiguration();
      result.put(groupOrFolderOrAttributeDef, grouperDeprovisioningOverallConfiguration);
      
      grouperDeprovisioningOverallConfiguration.setOriginalOwner(groupOrFolderOrAttributeDef);
      
      //get the values
      Map<String, Map<String, String>> attributeAssignIdToattributeDefNameToValue = null;
      if (groupOrFolderOrAttributeDef instanceof Group) {
        attributeAssignIdToattributeDefNameToValue = attributeAssignValueFinderResult.retrieveAssignIdsToAttributeDefNamesAndValueStrings(((Group)groupOrFolderOrAttributeDef).getId());
      } else if (groupOrFolderOrAttributeDef instanceof Stem) {
        attributeAssignIdToattributeDefNameToValue = attributeAssignValueFinderResult.retrieveAssignIdsToAttributeDefNamesAndValueStrings(((Stem)groupOrFolderOrAttributeDef).getId());
      } else if (groupOrFolderOrAttributeDef instanceof AttributeDef) {
        attributeAssignIdToattributeDefNameToValue = attributeAssignValueFinderResult.retrieveAssignIdsToAttributeDefNamesAndValueStrings(((AttributeDef)groupOrFolderOrAttributeDef).getId());
      } else {
        throw new RuntimeException("Wont happen");
      }
    
      Map<String, Map<String, String>> affiliationToAttributeDefNameToValueString = new HashMap<String, Map<String, String>>();
      Map<String, AttributeAssign> affiliationToAttributeAssign = new HashMap<String, AttributeAssign>();

      for (String attributeAssignId : attributeAssignIdToattributeDefNameToValue.keySet()) {

        Map<String, String> attributeDefNameAndValueStrings = attributeAssignIdToattributeDefNameToValue.get(attributeAssignId);

        AttributeAssign attributeAssignNew = attributeAssignValueFinderResult
            .getMapAttributeAssignIdToAttributeAssign().get(attributeAssignId);
        
        String affiliation = attributeDefNameAndValueStrings.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());

        if (!StringUtils.isBlank(affiliation)) {

          if (affiliationToAttributeDefNameToValueString.containsKey(affiliation)) {

            LOG.error("Multiple deprovisioning configurations found.  Deleting one: " + groupOrFolderOrAttributeDef);

            AttributeAssign attributeAssignExisting = affiliationToAttributeAssign.get(affiliation);
            
            // see if new is newer than old
            if (GrouperUtil.defaultIfNull(GrouperUtil.defaultIfNull(attributeAssignNew.getLastUpdatedDb(), attributeAssignNew.getCreatedOnDb()), 0L)
                > GrouperUtil.defaultIfNull(GrouperUtil.defaultIfNull(attributeAssignExisting.getLastUpdatedDb(), attributeAssignExisting.getCreatedOnDb()), 0L)) {
              
              attributeAssignExisting.delete();
              
            } else {
              attributeAssignNew.delete();
              continue;
            }
          }              
            
          affiliationToAttributeDefNameToValueString.put(affiliation, attributeDefNameAndValueStrings);
          affiliationToAttributeAssign.put(affiliation, attributeAssignNew);
        } else {
          // has no affiliation!!!!
          LOG.error("Cant find affiliation for deprovisioning, deleting: " + groupOrFolderOrAttributeDef);
          attributeAssignNew.delete();
        }
        
        
        GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
        grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
        
        grouperDeprovisioningOverallConfiguration.affiliationToConfiguration.put(affiliation, grouperDeprovisioningConfiguration);
              
        //lets get the base attribute assign
        grouperDeprovisioningConfiguration.setAttributeAssignBase(attributeAssignNew);
      }
      
      //loop through affiliations and setup the configuration
      for (String affiliation : affiliationToAttributeDefNameToValueString.keySet()) {
        
        Map<String, String> nameOfAttributeDefNameToValue = affiliationToAttributeDefNameToValueString.get(affiliation);
        GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.affiliationToConfiguration.get(affiliation);
        
        if (grouperDeprovisioningConfiguration == null) {
          //not sure why this would be null
          continue;
        }

        GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
        grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
        GrouperDeprovisioningAttributeValue newGrouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
        newGrouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
        
        grouperDeprovisioningConfiguration.setOriginalConfig(grouperDeprovisioningAttributeValue);
        grouperDeprovisioningConfiguration.setNewConfig(newGrouperDeprovisioningAttributeValue);
        
        grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName()));
        //start with same value
        newGrouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());

        grouperDeprovisioningAttributeValue.setAutoChangeLoaderString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName()));
        newGrouperDeprovisioningAttributeValue.setAutoChangeLoaderString(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());

        grouperDeprovisioningAttributeValue.setAutoselectForRemovalString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName()));
        newGrouperDeprovisioningAttributeValue.setAutoselectForRemovalString(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
        
        grouperDeprovisioningAttributeValue.setDeprovisionString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName()));
        newGrouperDeprovisioningAttributeValue.setDeprovisionString(grouperDeprovisioningAttributeValue.getDeprovisionString());
  
        grouperDeprovisioningAttributeValue.setDirectAssignmentString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName()));
        newGrouperDeprovisioningAttributeValue.setDirectAssignmentString(grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    
        grouperDeprovisioningAttributeValue.setEmailAddressesString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName()));
        newGrouperDeprovisioningAttributeValue.setEmailAddressesString(grouperDeprovisioningAttributeValue.getEmailAddressesString());
  
        grouperDeprovisioningAttributeValue.setEmailBodyString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName()));
        newGrouperDeprovisioningAttributeValue.setEmailBodyString(grouperDeprovisioningAttributeValue.getEmailBodyString());
  
        grouperDeprovisioningAttributeValue.setEmailSubjectString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailSubject().getName()));
        newGrouperDeprovisioningAttributeValue.setEmailSubjectString(grouperDeprovisioningAttributeValue.getEmailSubjectString());
  
        grouperDeprovisioningAttributeValue.setInheritedFromFolderIdString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName()));
        newGrouperDeprovisioningAttributeValue.setInheritedFromFolderIdString(grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
  
        grouperDeprovisioningAttributeValue.setMailToGroupString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName()));
        newGrouperDeprovisioningAttributeValue.setMailToGroupString(grouperDeprovisioningAttributeValue.getMailToGroupString());
  
        grouperDeprovisioningAttributeValue.setAffiliationString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName()));
        newGrouperDeprovisioningAttributeValue.setAffiliationString(grouperDeprovisioningAttributeValue.getAffiliationString());
  
        grouperDeprovisioningAttributeValue.setSendEmailString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName()));
        newGrouperDeprovisioningAttributeValue.setSendEmailString(grouperDeprovisioningAttributeValue.getSendEmailString());
  
        grouperDeprovisioningAttributeValue.setShowForRemovalString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName()));
        newGrouperDeprovisioningAttributeValue.setShowForRemovalString(grouperDeprovisioningAttributeValue.getShowForRemovalString());
  
        grouperDeprovisioningAttributeValue.setStemScopeString(
            nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName()));
        newGrouperDeprovisioningAttributeValue.setStemScopeString(grouperDeprovisioningAttributeValue.getStemScopeString());
  
      }
      
      //see if any were not set
      for (String affiliationLabel : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
        if (grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliationLabel) == null) {
          //if there is no configuration, set one anyways
          GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration  = new GrouperDeprovisioningConfiguration();
          grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
          
          GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
          grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
          grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);
          grouperDeprovisioningAttributeValue.setAffiliationString(affiliationLabel);
          //by default do not deprovision (if not specified)
          grouperDeprovisioningAttributeValue.setDeprovision(false);

          grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put(affiliationLabel, grouperDeprovisioningConfiguration);
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("overall config: " + grouperDeprovisioningOverallConfiguration);
      }
    }
  }

  /**
   * affiliation label to a configuration object
   */
  private Map<String, GrouperDeprovisioningConfiguration> affiliationToConfiguration = new TreeMap<String, GrouperDeprovisioningConfiguration>();
  
  /**
   * map of affiliation label to the configuration for that affiliation
   * @return the map
   */
  public Map<String, GrouperDeprovisioningConfiguration> getAffiliationToConfiguration() {
    return this.affiliationToConfiguration;
  }
  
  /**
   * Group or stem with configuration
   */
  private GrouperObject originalOwner;
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningOverallConfiguration.class);
  
  /**
   * @return the originalOwner
   */
  public GrouperObject getOriginalOwner() {
    return this.originalOwner;
  }

  
  /**
   * @param originalOwner1 the originalOwner to set
   */
  public void setOriginalOwner(GrouperObject originalOwner1) {
    this.originalOwner = originalOwner1;
  }

  /**
   * if allow adds while deprovisioned
   * @return true / false
   */
  public boolean isAllowAddsWhileDeprovisioned() {
    return false;
  }

  /**
   * 
   * @return true if auto change loader based on config
   */
  public boolean isAutoChangeLoader() {
    return true;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this);
    try {
      // Bypass privilege checks.  If the group is loaded it is viewable.
      toStringBuilder
        .append( "originalOwner", this.originalOwner == null ? "null" : this.originalOwner.getName());
      
      for (String affiliation: GrouperUtil.nonNull(this.affiliationToConfiguration).keySet()) {
        toStringBuilder
        .append( "affiliation_" + affiliation, this.affiliationToConfiguration.get(affiliation));
        
      }
    } catch (Exception e) {
      //ignore, did all we could
    }
    return toStringBuilder.toString();

  }

  
}
