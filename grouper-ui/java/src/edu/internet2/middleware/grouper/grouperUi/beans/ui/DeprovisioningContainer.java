package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningJob;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDeprovisioningMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

public class DeprovisioningContainer {

  /**
   * access that a user has
   */
  private Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningMembershipSubjectContainers;
  
  /**
   * access that a user has
   * @return
   */
  public Set<GuiDeprovisioningMembershipSubjectContainer> getGuiDeprovisioningMembershipSubjectContainers() {
    return guiDeprovisioningMembershipSubjectContainers;
  }

  /**
   * access that a user has
   * @param guiDeprovisioningMembershipSubjectContainers
   */
  public void setGuiDeprovisioningMembershipSubjectContainers(
      Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningMembershipSubjectContainers) {
    this.guiDeprovisioningMembershipSubjectContainers = guiDeprovisioningMembershipSubjectContainers;
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
   * if this user is allowed to deprovision
   * @return true if allowed to deprovision
   */
  public boolean isAllowedToDeprovision() {

    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    String error = GrouperUiFilter.requireUiGroup("uiV2.deprovisioning.must.be.in.group", loggedInSubject, false);

    if (StringUtils.isBlank(error)) {
      return true;
    }
    
    return false;

  }
  
  /**  set of realms current logged in user has access to **/
  private Set<GuiDeprovisioningRealm> realms;
  
  /**
   * @return set of realms current logged in user has access to
   */
  public Set<GuiDeprovisioningRealm> getRealms() {
    return realms;
  }
  
  /**
   * @param set of realms current logged in user has access to
   */
  public void setRealms(Set<GuiDeprovisioningRealm> realms) {
    this.realms = realms;
  }
  
  /** realm user is currently working on **/
  private String realm;

  /**
   * realm user is currently working on
   * @return
   */
  public String getRealm() {
    return realm;
  }

  /**
   * realm user is currently working on
   * @param realm
   */
  public void setRealm(String realm) {
    this.realm = realm;
  }
  
}
