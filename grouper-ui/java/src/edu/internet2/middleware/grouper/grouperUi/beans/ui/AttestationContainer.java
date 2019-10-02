/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstance;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportSettings;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class AttestationContainer {

  /**
   * if we should set certified date to today
   */
  private boolean editAttestationResetCertifiedToToday;

  /**
   * if we should set certified date to today
   * @return true if should set certified
   */
  public boolean isEditAttestationResetCertifiedToToday() {
    return this.editAttestationResetCertifiedToToday;
  }

  /**
   * if we should set certified date to today
   * @param editAttestationResetCertifiedToToday1
   */
  public void setEditAttestationResetCertifiedToToday(
      boolean editAttestationResetCertifiedToToday1) {
    this.editAttestationResetCertifiedToToday = editAttestationResetCertifiedToToday1;
  }

  /**
   * if the stem scope is "sub" for this attestation
   */
  private Boolean editAttestationStemScopeSub;

  /**
   * if the stem scope is "sub" for this attestation
   * @return true if for all subgroups
   */
  public Boolean getEditAttestationStemScopeSub() {
    return this.editAttestationStemScopeSub;
  }

  /**
   * if the stem scope is "sub" for this attestation
   * @param editAttestationStemScopeSub2
   */
  public void setEditAttestationStemScopeSub(Boolean editAttestationStemScopeSub2) {
    this.editAttestationStemScopeSub = editAttestationStemScopeSub2;
  }

  /**
   * if email settings should be shown
   */
  private boolean editAttestationShowEmailSettings;
  
  /**
   * if email settings should be shown
   * @return if email settings should be shown
   */
  public boolean isEditAttestationShowEmailSettings() {
    return this.editAttestationShowEmailSettings;
  }

  /**
   * if should show has attestation
   */
  private boolean editAttestationShowHasAttestation;
  
  
  
  
  /**
   * if should show has attestation
   * @return the editAttestationShowHasAttestation
   */
  public boolean isEditAttestationShowHasAttestation() {
    return this.editAttestationShowHasAttestation;
  }

  
  /**
   * if should show has attestation
   * @param editAttestationShowHasAttestation2 the editAttestationShowHasAttestation to set
   */
  public void setEditAttestationShowHasAttestation(boolean editAttestationShowHasAttestation2) {
    this.editAttestationShowHasAttestation = editAttestationShowHasAttestation2;
  }

  /**
   * if email settings should be shown
   * @param editAttestationShowEmailSettings1
   */
  public void setEditAttestationShowEmailSettings(
      boolean editAttestationShowEmailSettings1) {
    this.editAttestationShowEmailSettings = editAttestationShowEmailSettings1;
  }

  /**
   * if default certify in the edit context
   */
  private boolean editAttestationDefaultCertify;
  
  
  /**
   * if default certify in the edit context
   */
  public boolean isEditAttestationDefaultCertify() {
    return editAttestationDefaultCertify;
  }
  
  /**
   * if default certify in the edit context
   */
  public void setEditAttestationDefaultCertify(boolean editAttestationDefaultCertify1) {
    this.editAttestationDefaultCertify = editAttestationDefaultCertify1;
  }

  /**
   * if should email group managers (default to true)
   */
  private boolean editAttestationEmailGroupManagers;

  /**
   * if should email group managers (default to true)
   * @return if should email group managers
   */
  public boolean isEditAttestationEmailGroupManagers() {
    return this.editAttestationEmailGroupManagers;
  }

  /**
   * if should email group managers (default to true)
   * @param editAttestationEmailGroupManagers1
   */
  public void setEditAttestationEmailGroupManagers(
      boolean editAttestationEmailGroupManagers1) {
    this.editAttestationEmailGroupManagers = editAttestationEmailGroupManagers1;
  }

  /**
   * if should show textarea for email addresses
   */
  private boolean editAttestationShowEmailAddresses;

  /**
   * if should show textarea for email addresses
   * @return should show email addresses
   */
  public boolean isEditAttestationShowEmailAddresses() {
    return this.editAttestationShowEmailAddresses;
  }

  /**
   * 
   * @param editAttestationShowEmailAddresses1
   */
  public void setEditAttestationShowEmailAddresses(
      boolean editAttestationShowEmailAddresses1) {
    this.editAttestationShowEmailAddresses = editAttestationShowEmailAddresses1;
  }

  /**
   * custom email addresses
   */
  private String editAttestationEmailAddresses;

  /**
   * custom email addresses
   * @return custom email addresses
   */
  public String getEditAttestationEmailAddresses() {
    this.attributeAssignableHelper();
    return this.editAttestationEmailAddresses;
  }

  /**
   * custom email addresses
   * @param editAttestationEmailAddresses1
   */
  public void setEditAttestationEmailAddresses(String editAttestationEmailAddresses1) {
    this.editAttestationEmailAddresses = editAttestationEmailAddresses1;
  }
  
  /**
   * if should show textarea for type
   */
  private boolean editAttestationShowType;

  /**
   * if should show textarea for type
   * @return should show type
   */
  public boolean isEditAttestationShowType() {
    return this.editAttestationShowType;
  }

  /**
   * 
   * @param editAttestationShowType1
   */
  public void setEditAttestationShowType(
      boolean editAttestationShowType1) {
    this.editAttestationShowType = editAttestationShowType1;
  }

  /**
   * custom type
   */
  private String editAttestationType;

  /**
   * custom type
   * @return custom type
   */
  public String getEditAttestationType() {
    this.attributeAssignableHelper();
    return this.editAttestationType;
  }

  /**
   * custom type
   * @param editAttestationType1
   */
  public void setEditAttestationType(String editAttestationType1) {
    this.editAttestationType = editAttestationType1;
  }
  
  /**
   * if should show report
   */
  private boolean editAttestationShowReportConfiguration;

  /**
   * if should show report
   * @return should show report
   */
  public boolean isEditAttestationShowReportConfiguration() {
    return this.editAttestationShowReportConfiguration;
  }

  /**
   * 
   * @param editAttestationShowReportConfiguration1
   */
  public void setEditAttestationShowReportConfiguration(
      boolean editAttestationShowReportConfiguration1) {
    this.editAttestationShowReportConfiguration = editAttestationShowReportConfiguration1;
  }

  /**
   * 
   */
  private GrouperReportConfigurationBean editAttestationReportConfiguration;

  /**
   * @return report
   */
  public GrouperReportConfigurationBean getEditAttestationReportConfiguration() {
    this.attributeAssignableHelper();
    return this.editAttestationReportConfiguration;
  }

  /**
   * @param editAttestationReportConfiguration1
   */
  public void setEditAttestationReportConfiguration(GrouperReportConfigurationBean editAttestationReportConfiguration1) {
    this.editAttestationReportConfiguration = editAttestationReportConfiguration1;
  }
  
  /**
   * if should show authorized group
   */
  private boolean editAttestationShowAuthorizedGroup;

  /**
   * if should show authorized group
   * @return should show authorized group
   */
  public boolean isEditAttestationShowAuthorizedGroup() {
    return this.editAttestationShowAuthorizedGroup;
  }

  /**
   * 
   * @param editAttestationShowAuthorizedGroup1
   */
  public void setEditAttestationShowAuthorizedGroup(
      boolean editAttestationShowAuthorizedGroup1) {
    this.editAttestationShowAuthorizedGroup = editAttestationShowAuthorizedGroup1;
  }
  
  /**
   * if should show folder scope
   */
  private boolean editAttestationShowFolderScope;
  
  /**
   * if should show folder scope
   * @return the editAttestationShowFolderScope
   */
  public boolean isEditAttestationShowFolderScope() {
    return editAttestationShowFolderScope;
  }

  
  /**
   * if should show folder scope
   * @param editAttestationShowFolderScope the editAttestationShowFolderScope to set
   */
  public void setEditAttestationShowFolderScope(boolean editAttestationShowFolderScope) {
    this.editAttestationShowFolderScope = editAttestationShowFolderScope;
  }
  

  /**
   * 
   */
  private Group editAttestationAuthorizedGroup;

  /**
   * @return authorized group
   */
  public Group getEditAttestationAuthorizedGroup() {
    this.attributeAssignableHelper();
    return this.editAttestationAuthorizedGroup;
  }

  /**
   * @param editAttestationAuthorizedGroup1
   */
  public void setEditAttestationAuthorizedGroup(Group editAttestationAuthorizedGroup1) {
    this.editAttestationAuthorizedGroup = editAttestationAuthorizedGroup1;
  }
  
  /**
   * default recertify days
   * @return default configured recertify days
   */
  public int getDefaultRecertifyDays() {
    this.attributeAssignableHelper();
    return GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
  }

  /**
   * default to true
   * @return true if send email
   */
  public boolean isSendEmail() {

    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return true;
    }

    String attestationSendEmail = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName());
    return GrouperUtil.booleanValue(attestationSendEmail, true);
  }

  /**
   * default to true
   * @return true if has attestation
   */
  public boolean isHasAttestation() {

    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return true;
    }

    String attestationHasAttestation = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    return GrouperUtil.booleanValue(attestationHasAttestation, true);
  }

  /**
   * default to true
   * @return true if email group managers
   */
  public boolean isEmailGroupManagers() {

    return StringUtils.isBlank(this.getEmailAddresses());
    
  }

  /**
   * email addresses to send
   * @return email addresses
   */
  public String getEmailAddresses() {

    this.attributeAssignableHelper();
    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName());
    return attestationEmailAddresses;
  }
  

  /**
   * type
   * @return type
   */
  public String getType() {

    this.attributeAssignableHelper();
    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    String attestationType = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameType().getName());
    return attestationType;
  }
  

  /**
   * report configuration
   * @return report
   */
  public GrouperReportConfigurationBean getReportConfiguration() {

    this.attributeAssignableHelper();
    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    String attestationReportConfigurationId = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameReportConfigurationId().getName());
    
    if (attestationReportConfigurationId == null) {
      return null;
    }
    
    GrouperReportConfigurationBean reportConfiguration = GrouperReportConfigService.getGrouperReportConfigBean(attestationReportConfigurationId);
    return reportConfiguration;
  }
  
  /**
   * @return report instance id
   */
  public String getMostRecentReportInstanceAssignId() {
    this.attributeAssignableHelper();
    final AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        String attestationReportConfigurationId = attributeAssign.getAttributeValueDelegate()
            .retrieveValueString(
                GrouperAttestationJob.retrieveAttributeDefNameReportConfigurationId().getName());
        
        if (attestationReportConfigurationId == null) {
          return null;
        }

        if (attributeAssign.getOwnerStemId() != null) {
          GrouperReportInstance instance = GrouperReportInstanceService.getMostRecentReportInstance(attributeAssign.getOwnerStem(), attestationReportConfigurationId);
          if (instance != null) {
            return instance.getAttributeAssignId();
          }
        }
        
        return null;
      }
    });
  }
  
  /**
   * authorized group
   * @return authorized group
   */
  public Group getAuthorizedGroup() {

    this.attributeAssignableHelper();
    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    String attestationAuthorizedGroupId = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameAuthorizedGroupId().getName());
    
    if (attestationAuthorizedGroupId == null) {
      return null;
    }
    
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), attestationAuthorizedGroupId, false);
    
    return group;
  }
  
  /**
   * @return true if scope is sub
   */
  public Boolean getStemScopeSub() {
    this.attributeAssignableHelper();
    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return true;
    }

    String attestationStemScope = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName());

    return attestationStemScope == null 
        || StringUtils.equalsIgnoreCase(attestationStemScope, Scope.SUB.toString());
  }


  /**
   * get recertify days
   * @return configured recertify days
   */
  public Integer getRecertifyDays() {
    AttributeAssign attributeAssign = this.getAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    String daysUntilRecertify = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());

    return GrouperUtil.intObjectValue(daysUntilRecertify, true);
  }

  /**
   * attestations for misc page
   */
  private List<GuiAttestation> guiAttestations;

  /**
   * attestations for misc page
   * @return the attestations
   */
  public List<GuiAttestation> getGuiAttestations() {
    return this.guiAttestations;
  }

  /**
   * attestations for misc page
   * @param guiAttestations1
   */
  public void setGuiAttestations(List<GuiAttestation> guiAttestations1) {
    this.guiAttestations = guiAttestations1;
  }

  /**
   * fields for attestation
   */
  private GuiAttestation guiAttestation;

  /**
   * fields for attestation
   * @return gui attestation
   */
  public GuiAttestation getGuiAttestation() {
    this.attributeAssignableHelper();
    return this.guiAttestation;
  }

  /**
   * fields for attestation
   * @param guiAttestation1
   */
  public void setGuiAttestation(GuiAttestation guiAttestation1) {
    this.guiAttestation = guiAttestation1;
  }
  
  private GuiAttestation parentGuiAttestation;
  
  /**
   * fields for attestation
   * @return parent gui attestation
   */
  public GuiAttestation getParentGuiAttestation() {
    return this.parentGuiAttestation;
  }

  /**
   * fields for attestation
   * @param parentGuiAttestation1
   */
  public void setParentGuiAttestation(GuiAttestation parentGuiAttestation1) {
    this.parentGuiAttestation = parentGuiAttestation1;
  }
  

  /**
   * 
   */
  public AttestationContainer() {
  }

  /**
   * when in edit mode if assigned
   */
  private boolean editAttestationIsAssigned;

  /**
   * when in edit mode if assigned
   * @return true if assigned
   */
  public boolean isEditAttestationIsAssigned() {
    return this.editAttestationIsAssigned;
  }

  /**
   * when in edit mode if assigned
   * @param editAttestationIsAssigned1
   */
  public void setEditAttestationIsAssigned(boolean editAttestationIsAssigned1) {
    this.editAttestationIsAssigned = editAttestationIsAssigned1;
  }

  /**
   * if not using default recertify days, these are the custom ones in edit mode
   */
  private Integer editAttestationCustomRecertifyDays;

  /**
   * if not using default recertify days, these are the custom ones in edit mode
   */
  public Integer getEditAttestationCustomRecertifyDays() {
    this.attributeAssignableHelper();
    return this.editAttestationCustomRecertifyDays;
  }

  /**
   * if not using default recertify days, these are the custom ones in edit mode
   */
  public void setEditAttestationCustomRecertifyDays(Integer editCustomRecertifyDays1) {
    this.editAttestationCustomRecertifyDays = editCustomRecertifyDays1;
  }

  /**
   * if not using default recertify days, these are the custom ones
   */
  private int customRecertifyDays;

  /**
   * if not using default recertify days, these are the custom ones
   */
  public int getCustomRecertifyDays() {
    this.attributeAssignableHelper();
    return this.customRecertifyDays;
  }

  /**
   * if not using default recertify days, these are the custom ones
   * @param customRecertifyDays1
   */
  public void setCustomRecertifyDays(int customRecertifyDays1) {
    this.customRecertifyDays = customRecertifyDays1;
  }

  /**
   * if email should be sent
   */
  private boolean editAttestationSendEmail;
  
  /**
   * if email should be sent
   * @return if should send email
   */
  public boolean isEditAttestationSendEmail() {
    return this.editAttestationSendEmail;
  }

  /**
   * if email should be sent
   * @param editAttestationSendEmail1
   */
  public void setEditAttestationSendEmail(boolean editAttestationSendEmail1) {
    this.editAttestationSendEmail = editAttestationSendEmail1;
  }

  /**
   * if has attestation
   */
  private boolean editAttestationHasAttestation;
  
  /**
   * if has attestation
   * @return if should send email
   */
  public boolean isEditAttestationHasAttestation() {
    return this.editAttestationHasAttestation;
  }

  /**
   * if has attestation
   * @param editAttestationHasAttestation1
   */
  public void setEditAttestationHasAttestation(boolean editAttestationHasAttestation1) {
    this.editAttestationHasAttestation = editAttestationHasAttestation1;
  }


  /**
   * if should show the edit email field (and other fields)
   */
  private boolean editAttestationShowSendEmail;
  
  /**
   * if should show the edit email field (and other fields)
   * @return if should show email
   */
  public boolean isEditAttestationShowSendEmail() {
    return this.editAttestationShowSendEmail;
  }

  /**
   * if should show the edit email field (and other fields)
   * @param editAttestationShowSendEmail1
   */
  public void setEditAttestationShowSendEmail(boolean editAttestationShowSendEmail1) {
    this.editAttestationShowSendEmail = editAttestationShowSendEmail1;
  }

  /**
   * if the attestation assignment is directly assigned to the group
   */
  private boolean directGroupAttestationAssignment;
  
  /**
   * if attestation is on ancestor stem
   * @return true if attestation is on ancestor stem
   */
  public boolean isAncestorStemAttestationAssignment() {
    this.attributeAssignableHelper();
    return this.hasAttestationConfigured && !this.directGroupAttestationAssignment && !this.directStemAttestationAssignment;
  }
  
  /**
   * if the attestation assignment is directly assigned to the stem
   */
  private boolean directStemAttestationAssignment;

  /**
   * attribute assign to the group object
   */
  private AttributeAssign groupAttributeAssignable = null;
  
  /**
   * attribute assign to stem object
   */
  private AttributeAssign stemAttributeAssignable = null;

  /**
   * 
   * @return the stem or group attribute assignable
   */
  public AttributeAssign getAttributeAssignable() {
    if (this.isDirectGroupAttestationAssignment()) {
      return this.getGroupAttributeAssignable();
    }
    return this.getStemAttributeAssignable();
  }
  
  /**
   * get the group assignable
   * @return the assignable
   */
  public AttributeAssign getGroupAttributeAssignable() {
    this.attributeAssignableHelper();
    return groupAttributeAssignable;
  }

  /**
   * get the stem direct assignable
   * @return the assignable
   */
  public AttributeAssign getStemAttributeAssignable() {
    this.attributeAssignableHelper();
    return stemAttributeAssignable;
  }

  /**
   * get the stem inherited assignable
   * @return assignable
   */
  public AttributeAssign getStemInheritedAttributeAssignable() {
    this.attributeAssignableHelper();
    return stemInheritedAttributeAssignable;
  }

  /**
   * attribute assign to parent stem object
   */
  private AttributeAssign stemInheritedAttributeAssignable = null;

  /**
   * if the object has attestation direct or inherited
   */
  private boolean hasAttestationConfigured = false;

  /**
   * if can read ancestor attestation
   */
  private Boolean canReadAncestorAttestation = null;
  
  /**
   * if can read ancestor attestation
   * @return true if can read
   */
  public boolean isCanReadAncestorAttestation() {

    this.attributeAssignableHelper();

    if (this.canReadAncestorAttestation == null) {
      final Stem ancestorStem = this.getParentStemWithAttestation();
      
      if (ancestorStem == null) {
        this.canReadAncestorAttestation = false;
        return this.canReadAncestorAttestation;
      }
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canReadAncestorAttestation = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return ancestorStem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false);
            }
          });
    }    
    return this.canReadAncestorAttestation;

  }
  
  /**
   * 
   * @return true if can read
   */
  public boolean isCanReadAttestation() {
    
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
    
    // check if this is a report that the user can attest
    boolean isCanAttestReport = this.isCanAttestReport();
    if (isCanAttestReport) {
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @return true if can run daemon
   */
  public boolean isCanRunDaemon() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    return false;
  }

  /**
   * 
   * @return true if can write
   */
  public boolean isCanWriteAttestation() {
    
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
    return false;
  }
  
  /**
   * 
   * @return true if can write
   */
  public boolean isCanAttestReport() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();

    if (guiStem != null) {
      
      final Stem stem = guiStem.getStem();

      return (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

              AttributeAssign attributeAssign = stem.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
              if (attributeAssign != null) {
                String attestationType = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName());

                if ("report".equals(attestationType)) {
                  boolean isRoot = PrivilegeHelper.isWheelOrRoot(loggedInSubject);
                  if (isRoot) {
                    return true;
                  }
  
                  String attestationAuthorizedGroupId = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameAuthorizedGroupId().getName());
                  Group attestationAuthorizedGroup = null;
                  if (attestationAuthorizedGroupId != null) {
                    attestationAuthorizedGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), attestationAuthorizedGroupId, false);
                  }
                  if (attestationAuthorizedGroup != null) {
                    return attestationAuthorizedGroup.hasMember(loggedInSubject);
                  }
                }
              }
              
              return false;
            }
          });
    }
    
    return false;
  }

  /**
   * 
   */
  private boolean setupAttributes = false;

  /**
   * parent stem with attestation
   */
  private Stem parentStemWithAttestation;
  
  /**
   * parent gui stem with attestation
   */
  private GuiStem parentGuiStemWithAttestation;

  /**
   * parent gui stem with attestation
   * @return the gui stem
   */
  public GuiStem getParentGuiStemWithAttestation() {
    
    this.attributeAssignableHelper();
    
    //theres no gui if there is no stem
    if (this.parentStemWithAttestation == null) {
      return null;
    }
    
    //if there is no gui and there is a stem, create it
    if (this.parentGuiStemWithAttestation == null) {
      this.parentGuiStemWithAttestation = new GuiStem(this.parentStemWithAttestation);
    }
    
    //if the gui is pointing to the wrong place, replace it
    if (this.parentGuiStemWithAttestation != null && this.parentStemWithAttestation.equals(this.parentGuiStemWithAttestation.getStem())) {
      this.parentGuiStemWithAttestation = new GuiStem(this.parentStemWithAttestation);
    }
    
    return this.parentGuiStemWithAttestation;
  }

  /**
   * parent stem with attestation
   * @return parent stem
   */
  public Stem getParentStemWithAttestation() {
    this.attributeAssignableHelper();
    return this.parentStemWithAttestation;
  }

  /**
   * run the helper logic as grouper system
   */
  private void attributeAssignableHelperAsGrouperSystem() {
    
    Stem parentStem = null;
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      Group group = guiGroup.getGroup();
      
      this.groupAttributeAssignable = 
        group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);

      parentStem = group.getParentStem();
      
      if (this.groupAttributeAssignable != null) {
        // the assignable might not be direct
        String attestationDirectAssignment = this.groupAttributeAssignable
            .getAttributeValueDelegate().retrieveValueString(
                GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
        if (GrouperUtil.booleanValue(attestationDirectAssignment, false)) { 
          // group has direct attestation, don't use stem attributes at all.
          this.directGroupAttestationAssignment = true;
          this.hasAttestationConfigured = true;
        }
      }
    }

    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();

    if (guiStem != null) {
      Stem stem = guiStem.getStem();
      this.stemAttributeAssignable = 
          stem.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
      if (this.stemAttributeAssignable != null) {
        this.directStemAttestationAssignment = true;
        this.hasAttestationConfigured = true;
      }
      if (parentStem == null) {
        parentStem = stem;
      }
    }

    if (parentStem != null) {
      
      AttributeAssign ancestorAttributeAssign = GrouperAttestationJob.findParentFolderAssign(parentStem);
      if (ancestorAttributeAssign != null) {
        Stem ancestorStem = ancestorAttributeAssign.getOwnerStem();
        
        this.parentStemWithAttestation = ancestorStem;
        this.stemInheritedAttributeAssignable = ancestorAttributeAssign;
        hasAttestationConfigured = true;
      }
    }
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
    
      if (!this.isCanReadAttestation() )  {
        return;
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          AttestationContainer.this.attributeAssignableHelperAsGrouperSystem();
          
          return null;
        }
      });
  
    } catch (RuntimeException re) {
      hasError = true;
      throw re;
    } finally {
      if (!hasError) {
        setupAttributes = true;
      }
    }
  }
  
  /**
   * 
   * @return if direct to group
   */
  public boolean isDirectGroupAttestationAssignment() {
    this.attributeAssignableHelper();
    return this.directGroupAttestationAssignment;
  }
  
  /**
   * 
   * @return if direct to group
   */
  public boolean isDirectStemAttestationAssignment() {
    this.attributeAssignableHelper();
    return this.directStemAttestationAssignment;
  }
  
  /**
   * if has attestation
   * @return true if has
   */
  public boolean isHasAttestationConfigured() {
    this.attributeAssignableHelper();
    return this.hasAttestationConfigured;
  }
  
  /**
   * report configurations on folder
   * @return reports
   */
  public List<GrouperReportConfigurationBean> getAllReportConfigurationsOnFolder() {
    final GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    if (!GrouperReportSettings.grouperReportsEnabled()) {
      return new ArrayList<GrouperReportConfigurationBean>();
    }
    
    @SuppressWarnings("unchecked")
    List<GrouperReportConfigurationBean> grouperReportConfigsSecure = (List<GrouperReportConfigurationBean>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        List<GrouperReportConfigurationBean> grouperReportConfigsAll = GrouperReportConfigService.getGrouperReportConfigs(guiStem.getStem());
        List<GrouperReportConfigurationBean> grouperReportConfigsSecure = new ArrayList<GrouperReportConfigurationBean>();
        
        for (GrouperReportConfigurationBean configBean : grouperReportConfigsAll) {
          if (configBean.isCanRead(loggedInSubject)) {              
            grouperReportConfigsSecure.add(configBean);
          }
        }
        
        return grouperReportConfigsSecure;
      }
      
    });
    
    return grouperReportConfigsSecure;
  }
  
  /**
   * @return return direct if this is a direct assignment, otherwise look at the parent
   */
  public String getDirectOrParentAttestationDaysUntilRecertify() {
    if (this.isDirectGroupAttestationAssignment()) {
      return this.guiAttestation.getGrouperAttestationDaysUntilRecertify();
    } else if (this.isAncestorStemAttestationAssignment() && this.parentGuiAttestation != null) {
      return this.parentGuiAttestation.getGrouperAttestationDaysUntilRecertify();
    } else {
      return null;
    }
  }
  
  /**
   * @return return direct if this is a direct assignment, otherwise look at the parent
   */
  public Boolean getDirectOrParentGrouperAttestationSendEmail() {
    if (this.isDirectGroupAttestationAssignment()) {
      return this.guiAttestation.getGrouperAttestationSendEmail();
    } else if (this.isAncestorStemAttestationAssignment() && this.parentGuiAttestation != null) {
      return this.parentGuiAttestation.getGrouperAttestationSendEmail();
    } else {
      return null;
    }
  }
  
  /**
   * @return return direct if this is a direct assignment, otherwise look at the parent
   */
  public String getDirectOrParentGrouperAttestationEmailAddresses() {
    if (this.isDirectGroupAttestationAssignment()) {
      return this.guiAttestation.getGrouperAttestationEmailAddresses();
    } else if (this.isAncestorStemAttestationAssignment() && this.parentGuiAttestation != null) {
      return this.parentGuiAttestation.getGrouperAttestationEmailAddresses();
    } else {
      return null;
    }
  }
}
