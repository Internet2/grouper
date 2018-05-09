package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperDeprovisioningOverallConfiguration {

  /**
   * 
   * @param stemOrFolder
   * @return the configuration
   */
  public static GrouperDeprovisioningOverallConfiguration retrieveConfiguration(GrouperObject stemOrFolder) {
    
    if (stemOrFolder == null) {
      throw new NullPointerException("stemOrFolder is null");
    }
    
    if ((!(stemOrFolder instanceof Group)) && (!(stemOrFolder instanceof Stem))) {
      throw new RuntimeException("stemOrFolder needs to be a stem or group: " + stemOrFolder.getClass() + ", " + stemOrFolder);
    }
    
    //get all attributes and assignments for all realms on a group or folder
    Set<AttributeAssign> attributeAssigns = new AttributeAssignFinder().addAttributeDefNameId(
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameValueDef().getId())
      .assignIncludeAssignmentsOnAssignments(true).findAttributeAssigns();
    
    Set<String> attributeAssignIds = new HashSet<String>();
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      attributeAssignIds.add(attributeAssign.getId());
      
    }
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration 
      = new GrouperDeprovisioningOverallConfiguration();

    //go through the base attributes on the owner
    AttributeDefName baseAttributeDefName = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDef();
    
    Set<AttributeAssign> baseAssigns = new HashSet<AttributeAssign>();
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      if (StringUtils.equals(attributeAssign.getAttributeDefNameId(), baseAttributeDefName.getId())) {
        baseAssigns.add(attributeAssign);
      }
    }

    // get all values
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder()
        .assignAttributeAssignIds(attributeAssignIds).findAttributeAssignValuesResult();

    Map<String, Map<String, String>> realmToAttributeDefNameToValueString = new HashMap<String, Map<String, String>>();

    //TODO remove redundant assignments if multiple assignments to same realm (and log error)
    //  TODO if inherited and direct, remove the inherited
    //  TODO if multiple of the same type, then remove the older ones

    for (AttributeAssign attributeAssign : baseAssigns) {

      Map<String, String> attributeDefNameAndValueStrings = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(attributeAssign.getId());

      boolean foundRealm = false;
      
      //find the realms
      for (String nameOfAttributeDefName : attributeDefNameAndValueStrings.keySet() ) {
        if (StringUtils.equals(nameOfAttributeDefName, GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameRealm().getName())) {
          foundRealm = true;
          String realm = attributeDefNameAndValueStrings.get(nameOfAttributeDefName);
          if (realmToAttributeDefNameToValueString.containsKey(realm)) {
            //TODO here is a redundant assignment... oops
          } else {
            realmToAttributeDefNameToValueString.put(realm, attributeDefNameAndValueStrings);

            GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
            
            grouperDeprovisioningOverallConfiguration.realmToConfiguration.put(realm, grouperDeprovisioningConfiguration);
            
            //lets get the base attribute assign
            grouperDeprovisioningConfiguration.setAttributeAssignBase(attributeAssign);

          }
        }
      }
      
      if (!foundRealm) {
        //TODO remove this assignment and log it since not valid.  Every deprovisioning assignment needs a realm
      }
      
    }

    //loop through realms and setup the configuration
    for (String realm : realmToAttributeDefNameToValueString.keySet()) {
      
      Map<String, String> nameOfAttributeDefNameToValue = realmToAttributeDefNameToValueString.get(realm);
      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.realmToConfiguration.get(realm);
      
      if (grouperDeprovisioningConfiguration == null) {
        //not sure why this would be null
        continue;
      }
      
      GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      GrouperDeprovisioningAttributeValue newGrouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      
      grouperDeprovisioningConfiguration.setOriginalConfig(grouperDeprovisioningAttributeValue);
      grouperDeprovisioningConfiguration.setNewConfig(newGrouperDeprovisioningAttributeValue);
      
      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName()));
      //start with same value
      newGrouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
      
      grouperDeprovisioningAttributeValue.setAutoChangeLoaderString(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName()));
      newGrouperDeprovisioningAttributeValue.setAutoChangeLoaderString(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());

      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName()));
      newGrouperDeprovisioningAttributeValue.setAutoselectForRemoval(grouperDeprovisioningAttributeValue.getAutoselectForRemoval());
      
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

      grouperDeprovisioningAttributeValue.setRealmString(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameRealm().getName()));
      newGrouperDeprovisioningAttributeValue.setRealmString(grouperDeprovisioningAttributeValue.getRealmString());

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
    return grouperDeprovisioningOverallConfiguration;
  }

  /**
   * 
   * @param realm
   * @return how many changes made
   */
  public int storeConfigurationForRealm(String realm) {
    boolean newConfiguration = false;
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = this.realmToConfiguration.get(realm);
    
    // not there who cares
    if (grouperDeprovisioningConfiguration == null) {
      return 0;
    }
    
    GrouperDeprovisioningAttributeValue newConfig = grouperDeprovisioningConfiguration.getNewConfig();
    GrouperDeprovisioningAttributeValue originalConfig = grouperDeprovisioningConfiguration.getOriginalConfig();

    if (newConfig == null
        && originalConfig == null) {
      return 0;
    }
    
    if (originalConfig == null) {
      newConfiguration = true;
    }
    
    if (newConfig == null) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().deleteAndStore();
      return 1;
    }

    int changeCount = 0;

    //it has both new and old, lets compare and do the saves
    if (!StringUtils.equals(originalConfig.getAllowAddsWhileDeprovisionedString(), newConfig.getAllowAddsWhileDeprovisionedString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName(), newConfig.getAllowAddsWhileDeprovisionedString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getAutoChangeLoaderString(), newConfig.getAutoChangeLoaderString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName(), newConfig.getAutoChangeLoaderString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getAutoselectForRemoval(), newConfig.getAutoselectForRemoval())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName(), newConfig.getAutoselectForRemoval());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getDeprovisionString(), newConfig.getDeprovisionString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName(), newConfig.getDeprovisionString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getDirectAssignmentString(), newConfig.getDirectAssignmentString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), newConfig.getDirectAssignmentString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getEmailAddressesString(), newConfig.getEmailAddressesString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName(), newConfig.getEmailAddressesString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getEmailBodyString(), newConfig.getEmailBodyString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName(), newConfig.getEmailBodyString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getEmailSubjectString(), newConfig.getEmailSubjectString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailSubject().getName(), newConfig.getEmailSubjectString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getInheritedFromFolderIdString(), newConfig.getInheritedFromFolderIdString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName(), newConfig.getInheritedFromFolderIdString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getMailToGroupString(), newConfig.getMailToGroupString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName(), newConfig.getMailToGroupString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getRealmString(), newConfig.getRealmString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameRealm().getName(), newConfig.getRealmString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getSendEmailString(), newConfig.getSendEmailString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName(), newConfig.getSendEmailString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getShowForRemovalString(), newConfig.getShowForRemovalString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName(), newConfig.getShowForRemovalString());
      changeCount++;
    }
    if (!StringUtils.equals(originalConfig.getStemScopeString(), newConfig.getStemScopeString())) {
      grouperDeprovisioningConfiguration.getAttributeAssignBase().getAttributeValueDelegate().assignValue(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName(), newConfig.getStemScopeString());
      changeCount++;
    }
    return changeCount;
  }
  
  /**
   * realm label to a configuration object
   */
  private Map<String, GrouperDeprovisioningConfiguration> realmToConfiguration = new TreeMap<String, GrouperDeprovisioningConfiguration>();
  
  /**
   * map of realm label to the configuration for that realm
   * @return the map
   */
  public Map<String, GrouperDeprovisioningConfiguration> getRealmToConfiguration() {
    return this.realmToConfiguration;
  }
  
  /**
   * Group or stem with configuration
   */
  private GrouperObject originalOwner;
  
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

 
  
}
