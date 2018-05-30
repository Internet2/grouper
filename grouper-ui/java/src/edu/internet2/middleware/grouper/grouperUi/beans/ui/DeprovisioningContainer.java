package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeValue;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningLogic;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningOverallConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDeprovisioningMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class DeprovisioningContainer {

  /**
   * get the new deprovisioning settings
   * @return the new configuration 
   */
  public GrouperDeprovisioningAttributeValue getGrouperDeprovisioningAttributeValueNew() {
    if (StringUtils.isBlank(this.affiliation)) {
      return null;
    }
    this.grouperDeprovisioningOverallConfiguration = this.getGrouperDeprovisioningOverallConfiguration();
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = this.grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(this.affiliation);
    if (grouperDeprovisioningConfiguration == null) {
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(this.grouperDeprovisioningOverallConfiguration);
      this.grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put(this.affiliation, grouperDeprovisioningConfiguration);
    }
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    
    // if theres no configuration, or if the configuration is inherited, then clear it out
    if (grouperDeprovisioningAttributeValue == null || !grouperDeprovisioningAttributeValue.isDirectAssignment()) {
      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setAffiliationString(this.affiliation);
      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);
    }
    
    //set some defaults
//    if (StringUtils.isBlank(grouperDeprovisioningAttributeValue.getDeprovisionString())) {
//      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
//    }
    
    return grouperDeprovisioningAttributeValue;
  }
  
  /**
   * get the grouper deprovisioning attribute values for all affiliations
   * @return the map
   */
  public Map<String, GrouperDeprovisioningAttributeValue> getGrouperDeprovisioningAttributeValuesAll() {
    
    Map<String, GrouperDeprovisioningAttributeValue> affiliationNameToConfiguration = new TreeMap<String, GrouperDeprovisioningAttributeValue>();
    
    this.attributeAssignableHelper();
    
    for (String theAffiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
      
      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = this.grouperDeprovisioningOverallConfiguration == null ? null 
          : this.grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(theAffiliation);
      
      GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration == null ? null 
          : grouperDeprovisioningConfiguration.getOriginalConfig();

      if (grouperDeprovisioningAttributeValue == null) {
        grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
        grouperDeprovisioningAttributeValue.setAffiliationString(theAffiliation);
      }
      
      affiliationNameToConfiguration.put(theAffiliation, grouperDeprovisioningAttributeValue);
      
    }
    
    return affiliationNameToConfiguration;
  }
  

  /**
   * if this object or any parent object has deprovisioning
   * @return if there is deprovisioning
   */
  public boolean isHasDeprovisioningOnThisObjectOrParent() {
    this.attributeAssignableHelper();
    if (this.grouperDeprovisioningOverallConfiguration == null || GrouperUtil.length(this.grouperDeprovisioningOverallConfiguration) == 0) {
      return false;
    }
    for (GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration : this.grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().values()) {
      if (!StringUtils.isBlank(grouperDeprovisioningConfiguration.getOriginalConfig().getAffiliationString())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * overall configuration for this user and this object (group, folder, attributeDef)
   */
  private GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration;
  
  
  /**
   * @return the grouperDeprovisioningOverallConfiguration
   */
  public GrouperDeprovisioningOverallConfiguration getGrouperDeprovisioningOverallConfiguration() {
    this.attributeAssignableHelper();
    return this.grouperDeprovisioningOverallConfiguration;
  }

  
  /**
   * access that a user has
   */
  private Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningMembershipSubjectContainers;
  
  /**
   * access that a user has
   * @return the containers
   */
  public Set<GuiDeprovisioningMembershipSubjectContainer> getGuiDeprovisioningMembershipSubjectContainers() {
    this.attributeAssignableHelper();
    return this.guiDeprovisioningMembershipSubjectContainers;
  }

  /**
   * access that a user has
   * @param guiDeprovisioningMembershipSubjectContainers1
   */
  public void setGuiDeprovisioningMembershipSubjectContainers(
      Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningMembershipSubjectContainers1) {
    this.guiDeprovisioningMembershipSubjectContainers = guiDeprovisioningMembershipSubjectContainers1;
  }

  /**
   * get sources to pick which source
   * @return the sources
   */
  public Set<Source> getSources() {
    
    return GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision();
  }

  /**
   * gui members who are deprovisioned
   */
  private Set<GuiMember> deprovisionedGuiMembers;

  /**
   * gui members who are deprovisioned
   * @return the gui members
   */
  public Set<GuiMember> getDeprovisionedGuiMembers() {
    return this.deprovisionedGuiMembers;
  }

  /**
   * gui members who are deprovisioned
   * @param deprovisionedGuiMembers1
   */
  public void setDeprovisionedGuiMembers(Set<GuiMember> deprovisionedGuiMembers1) {
    this.deprovisionedGuiMembers = deprovisionedGuiMembers1;
  }

  /**
   * make sure deprovisioning is enabled and allowed
   */
  public void assertDeprovisioningEnabledAndAllowed() {
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      throw new RuntimeException("Deprovisioning is disabled");
    }
    
    if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer().isAllowedToDeprovision()) {
      throw new RuntimeException("Not allowed to deprovision");
    }

  }
  
  /**
   * if deprovisioning is even enabled in the config
   * @return true if enabled
   */
  public boolean isDeprovisioningEnabled() {
    return GrouperDeprovisioningSettings.deprovisioningEnabled();
  }
  
  /**
   * if the deprovisioning assignment is directly assigned to the stem
   */
  private boolean directStemDeprovisioningAssignment = false;

  /**
   * 
   */
  private boolean setupAttributes = false;

  /**
   * 
   * @return true if can read
   */
  public boolean isCanReadDeprovisioning() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }
    
    GuiAttributeDef guiAttributeDef = GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().getGuiAttributeDef();
    
    if (guiAttributeDef != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().isCanRead()) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @return true if can write
   */
  public boolean isCanWriteDeprovisioning() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanUpdate()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }

    
    GuiAttributeDef guiAttributeDef = GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().getGuiAttributeDef();
    
    if (guiAttributeDef != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().isCanUpdate()) {
        return true;
      }
    }

    return false;
  }

  
  /**
   * need to setup stuff about attestation
   */
  private void attributeAssignableHelper() {

    if (this.setupAttributes) {
      return;
    }
    boolean hasError = false;
    try {
    
      if (!this.isCanReadDeprovisioning() )  {
        return;
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          DeprovisioningContainer.this.attributeAssignableHelperAsGrouperSystem();
          
          return null;
        }
      });
  
    } catch (RuntimeException re) {
      hasError = true;
      throw re;
    } finally {
      if (!hasError) {
        this.setupAttributes = true;
      }
    }
  }
  
  /**
   * run the helper logic as grouper system
   */
  private void attributeAssignableHelperAsGrouperSystem() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      Group group = guiGroup.getGroup();

      this.grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group);

    }

    GuiAttributeDef guiAttributeDef = GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().getGuiAttributeDef();
    
    if (guiAttributeDef != null) {
      AttributeDef attributeDef = guiAttributeDef.getAttributeDef();

      this.grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(attributeDef);

    }

    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();

    if (guiStem != null) {
      
      Stem stem = guiStem.getStem();
      
      this.grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem);
      
    }

  }

  /**
   * 
   * @return if direct to stem
   */
  public boolean isDirectStemDeprovisioningAssignment() {
    this.attributeAssignableHelper();
    return this.directStemDeprovisioningAssignment;
  }

  /**
   * if this user is allowed to deprovision
   * @return true if allowed to deprovision
   */
  public boolean isAllowedToDeprovision() {

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return GrouperDeprovisioningLogic.allowedToDeprovision(loggedInSubject);
  }
  
  /**  set of affiliations current logged in user has access to **/
  private Set<GuiDeprovisioningAffiliation> guiDeprovisioningAffiliationsUserCanDeprovision;
  
  /**
   * @return set of affiliations current logged in user has access to
   */
  public Set<GuiDeprovisioningAffiliation> getGuiDeprovisioningAffiliationsUserCanDeprovision() {
    
    if (this.guiDeprovisioningAffiliationsUserCanDeprovision == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      Set<GrouperDeprovisioningAffiliation> affiliationsForUserManager = new TreeSet<GrouperDeprovisioningAffiliation>(
          GrouperDeprovisioningAffiliation.retrieveAffiliationsForUserManager(loggedInSubject).values());
      
      Set<GuiDeprovisioningAffiliation> guiAffiliations = GuiDeprovisioningAffiliation.convertFromGrouperDeprovisioningAffiliations(affiliationsForUserManager);
      
      this.guiDeprovisioningAffiliationsUserCanDeprovision = guiAffiliations;
    
    }
    return this.guiDeprovisioningAffiliationsUserCanDeprovision;
  }
  
  /**  set of affiliations all **/
  private Set<GuiDeprovisioningAffiliation> guiDeprovisioningAffiliationsAll;
  
  /**
   * @return set of affiliations all
   */
  public Set<GuiDeprovisioningAffiliation> getGuiDeprovisioningAffiliationsAll() {
    
    if (this.guiDeprovisioningAffiliationsAll == null) {
      
      Map<String, GrouperDeprovisioningAffiliation> affiliationNameToAffiliation = GrouperDeprovisioningAffiliation.retrieveAllAffiliations();
      
      Set<GuiDeprovisioningAffiliation> guiAffiliations = GuiDeprovisioningAffiliation.convertFromGrouperDeprovisioningAffiliations(affiliationNameToAffiliation.values());
      
      this.guiDeprovisioningAffiliationsAll = guiAffiliations;
    
    }
    return this.guiDeprovisioningAffiliationsAll;
  }
  
  /** affiliation user is currently working on **/
  private String affiliation;

  /**
   * affiliation user is currently working on
   * @return affil
   */
  public String getAffiliation() {
    return this.affiliation;
  }

  /**
   * affiliation user is currently working on
   * @param affiliation1
   */
  public void setAffiliation(String affiliation1) {
    this.affiliation = affiliation1;
  }
  
}
