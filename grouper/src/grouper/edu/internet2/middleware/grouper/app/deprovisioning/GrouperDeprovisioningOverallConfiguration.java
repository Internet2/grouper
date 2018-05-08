package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        GrouperDeprovisioningJob.retrieveAttributeDefNameValueDef().getId())
      .assignIncludeAssignmentsOnAssignments(true).findAttributeAssigns();
    
    Set<String> attributeAssignIds = new HashSet<String>();
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      attributeAssignIds.add(attributeAssign.getId());
      
    }
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration 
      = new GrouperDeprovisioningOverallConfiguration();

    //go through the base attributes on the owner
    AttributeDefName baseAttributeDefName = GrouperDeprovisioningJob.retrieveAttributeDefNameDef();
    
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
        if (StringUtils.equals(nameOfAttributeDefName, GrouperDeprovisioningJob.retrieveAttributeDefNameRealm().getName())) {
          foundRealm = true;
          String realm = attributeDefNameAndValueStrings.get(nameOfAttributeDefName);
          if (realmToAttributeDefNameToValueString.containsKey(realm)) {
            //TODO here is a redundant assignment... oops
          } else {
            realmToAttributeDefNameToValueString.put(realm, attributeDefNameAndValueStrings);
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
      
      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      
      GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      
      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName()));

      grouperDeprovisioningAttributeValue.setAutoChangeLoaderString(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameAutoChangeLoader()));

      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(
          nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameAutoSelectForRemoval()));
      
      grouperDeprovisioningAttributeValue.setDeprovisionString(
      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameDeprovision()));
  
      grouperDeprovisioningAttributeValue.setDirectAssignmentString(
      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameDirectAssignment()));
  
      grouperDeprovisioningAttributeValue.setEmailAddressesString(
      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameEmailAddresses()));
  
      grouperDeprovisioningAttributeValue.setEmailBodyString(
      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameEmailBody()));
  
      grouperDeprovisioningAttributeValue.setEmailSubjectString(
      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameEmailSubject()));
  
      grouperDeprovisioningAttributeValue.setMailToGroupString(
      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefNameMailToGroup()));
  
//      grouperDeprovisioningAttributeValue.set
//      nameOfAttributeDefNameToValue.get(GrouperDeprovisioningJob.retrieveAttributeDefName
                                      
    }
    return null;
  }

  private Map<String, GrouperDeprovisioningConfiguration> realmToConfiguration;
  
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
