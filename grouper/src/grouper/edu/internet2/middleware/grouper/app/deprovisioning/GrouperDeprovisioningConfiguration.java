package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 * configuration on an object
 */
public class GrouperDeprovisioningConfiguration {

  /**
   * grouper deprovisioning overall configuration
   */
  private GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
  
  /**
   * @return the grouperDeprovisioningOverallConfiguration
   */
  public GrouperDeprovisioningOverallConfiguration getGrouperDeprovisioningOverallConfiguration() {
    return this.grouperDeprovisioningOverallConfiguration;
  }
  
  /**
   * @param grouperDeprovisioningOverallConfiguration1 the grouperDeprovisioningOverallConfiguration to set
   */
  public void setGrouperDeprovisioningOverallConfiguration(
      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration1) {
    this.grouperDeprovisioningOverallConfiguration = grouperDeprovisioningOverallConfiguration1;
  }

  /**
   * if retrieve configs all at once, put the inherited config here
   */
  private GrouperDeprovisioningConfiguration inheritedConfig = null;
  
  /**
   * if retrieve configs all at once, put the inherited config here
   * @return the inheritedConfig
   */
  public GrouperDeprovisioningConfiguration getInheritedConfig() {
    return this.inheritedConfig;
  }
  
  /**
   * if retrieve configs all at once, put the inherited config here
   * @param inheritedConfig1 the inheritedConfig to set
   */
  public void setInheritedConfig(GrouperDeprovisioningConfiguration inheritedConfig1) {
    this.inheritedConfig = inheritedConfig1;
  }

  /**
   * base existing attribute assign for this configuration
   */
  private AttributeAssign attributeAssignBase = null;
  
  /**
   * base existing attribute assign for this configuration
   * @return the attribute assign
   */
  public AttributeAssign getAttributeAssignBase() {
    return this.attributeAssignBase;
  }

  /**
   * base existing attribute assign for this configuration
   * @param attributeAssignBase1
   */
  public void setAttributeAssignBase(AttributeAssign attributeAssignBase1) {
    this.attributeAssignBase = attributeAssignBase1;
  }

  /**
   * Stem that is the inherited owner
   */
  private Stem inheritedOwner;

  
  /**
   * @return the inheritedOwner
   */
  public Stem getInheritedOwner() {
    return this.inheritedOwner;
  }

  
  /**
   * @param inheritedOwner1 the inheritedOwner to set
   */
  public void setInheritedOwner(Stem inheritedOwner1) {
    this.inheritedOwner = inheritedOwner1;
  }

  /**
   * original config in the database
   */
  private GrouperDeprovisioningAttributeValue originalConfig;
  
  
  /**
   * @return the originalConfig
   */
  public GrouperDeprovisioningAttributeValue getOriginalConfig() {
    return this.originalConfig;
  }

  
  /**
   * @param originalConfig1 the originalConfig to set
   */
  public void setOriginalConfig(GrouperDeprovisioningAttributeValue originalConfig1) {
    this.originalConfig = originalConfig1;
  }

  /**
   * new config after calculations
   */
  private GrouperDeprovisioningAttributeValue newConfig;
  
  /**
   * new config after calculations
   * @return the newConfig
   */
  public GrouperDeprovisioningAttributeValue getNewConfig() {
    return this.newConfig;
  }

  
  /**
   * new config after calculations
   * @param newConfig1 the newConfig to set
   */
  public void setNewConfig(GrouperDeprovisioningAttributeValue newConfig1) {
    this.newConfig = newConfig1;
  }
  
  /**
   * 
   * @return how many changes made
   */
  public int storeConfiguration() {
        
    GrouperDeprovisioningAttributeValue newConfig = this.getNewConfig();
    GrouperDeprovisioningAttributeValue originalConfig = this.getOriginalConfig();

    if (newConfig == null
        && originalConfig == null) {
      return 0;
    }

    if (newConfig == null) {
      this.getAttributeAssignBase().deleteAndStore();
      return 1;
    }

    if (originalConfig == null) {
      originalConfig = new GrouperDeprovisioningAttributeValue();
      this.setOriginalConfig(originalConfig);
    }
    
    //make sure things are set to null if default
    newConfig.flattenValues();
    originalConfig.flattenValues();
    
    int[] changeCount = new int[]{0};

    //it has both new and old, lets compare and do the saves
    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned(), 
        originalConfig.getAllowAddsWhileDeprovisionedString(),
        newConfig.getAllowAddsWhileDeprovisionedString(), changeCount);
    originalConfig.setAllowAddsWhileDeprovisionedString(newConfig.getAllowAddsWhileDeprovisionedString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader(), 
        originalConfig.getAutoChangeLoaderString(),
        newConfig.getAutoChangeLoaderString(), changeCount);
    originalConfig.setAutoChangeLoaderString(newConfig.getAutoChangeLoaderString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval(),
        originalConfig.getAutoselectForRemovalString(),
        newConfig.getAutoselectForRemovalString(), changeCount);
    originalConfig.setAutoselectForRemovalString(newConfig.getAutoselectForRemovalString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision(), 
        originalConfig.getDeprovisionString(),
        newConfig.getDeprovisionString(), changeCount);
    originalConfig.setDeprovisionString(newConfig.getDeprovisionString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment(), 
        originalConfig.getDirectAssignmentString(),
        newConfig.getDirectAssignmentString(), changeCount);
    originalConfig.setDirectAssignmentString(newConfig.getDirectAssignmentString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses(), 
        originalConfig.getEmailAddressesString(),
        newConfig.getEmailAddressesString(), changeCount);
    originalConfig.setEmailAddressesString(newConfig.getEmailAddressesString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody(), 
        originalConfig.getEmailBodyString(),
        newConfig.getEmailBodyString(), changeCount);
    originalConfig.setEmailBodyString(newConfig.getEmailBodyString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailSubject(), 
        originalConfig.getEmailSubjectString(),
        newConfig.getEmailSubjectString(), changeCount);
    originalConfig.setEmailSubjectString(newConfig.getEmailSubjectString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId(), 
        originalConfig.getInheritedFromFolderIdString(),
        newConfig.getInheritedFromFolderIdString(), changeCount);
    originalConfig.setInheritedFromFolderIdString(newConfig.getInheritedFromFolderIdString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup(), 
        originalConfig.getMailToGroupString(),
        newConfig.getMailToGroupString(), changeCount);
    originalConfig.setMailToGroupString(newConfig.getMailToGroupString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameRealm(), 
        originalConfig.getRealmString(),
        newConfig.getRealmString(), changeCount);
    originalConfig.setRealmString(newConfig.getRealmString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail(), 
        originalConfig.getSendEmailString(),
        newConfig.getSendEmailString(), changeCount);
    originalConfig.setSendEmailString(newConfig.getSendEmailString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval(), 
        originalConfig.getShowForRemovalString(),
        newConfig.getShowForRemovalString(), changeCount);
    originalConfig.setShowForRemovalString(newConfig.getShowForRemovalString());

    updateAttribute( 
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope(), 
        originalConfig.getStemScopeString(),
        newConfig.getStemScopeString(), changeCount);
    originalConfig.setStemScopeString(newConfig.getStemScopeString());

    return changeCount[0];
  }

  /**
   * update an attribute if it needs it
   * @param attributeDefName
   * @param originalValue
   * @param newValue
   * @param changeCount
   */
  private void updateAttribute(
      AttributeDefName attributeDefName, String originalValue, String newValue, int[] changeCount) {
    
    // not changed
    if (StringUtils.equals(StringUtils.trimToNull(originalValue), StringUtils.trimToNull(newValue))) {
      return;
    }
    
    changeCount[0]++;
    if (newValue == null) {
      if (this.getAttributeAssignBase() != null) {
        this.getAttributeAssignBase().getAttributeValueDelegate().deleteValue(attributeDefName.getName(), originalValue);
      }
    } else {
      
      //if there is not base assign, then add one
      if (this.getAttributeAssignBase() == null) {
        GrouperObject grouperObject = this.getGrouperDeprovisioningOverallConfiguration().getOriginalOwner();
        AttributeAssignable attributeAssignable = (AttributeAssignable)grouperObject;
        AttributeAssign attributeAssign = attributeAssignable.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
        this.setAttributeAssignBase(attributeAssign);
      }
      
      this.getAttributeAssignBase().getAttributeValueDelegate().assignValue(attributeDefName.getName(), newValue);
    }
  }


}
