/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
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
  private boolean editAttestationStemScopeSub;

  /**
   * if the stem scope is "sub" for this attestation
   * @return true if for all subgroups
   */
  public boolean isEditAttestationStemScopeSub() {
    return this.editAttestationStemScopeSub;
  }

  /**
   * if the stem scope is "sub" for this attestation
   * @param editAttestationStemScopeSub2
   */
  public void setEditAttestationStemScopeSub(boolean editAttestationStemScopeSub2) {
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

    AttributeAssign attributeAssign = this.getGroupAttributeAssignable();

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
    AttributeAssign attributeAssign = this.getGroupAttributeAssignable();

    if (attributeAssign == null) {
      return null;
    }

    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName());
    return attestationEmailAddresses;
  }

  /**
   * get recertify days
   * @return configured recertify days
   */
  public Integer getRecertifyDays() {
    AttributeAssign attributeAssign = this.getGroupAttributeAssignable();

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
    return this.hasAttestation && !this.directGroupAttestationAssignment && !this.directStemAttestationAssignment;
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
  private boolean hasAttestation = false;

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
          this.hasAttestation = true;
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
        this.hasAttestation = true;
      }
      if (parentStem == null) {
        parentStem = stem.getParentStemOrNull();
      }
    }

    if (parentStem != null) {

      Stem ancestorStem = (Stem)parentStem.getAttributeDelegate().getAttributeOrAncestorAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
      if (ancestorStem != null) {
        AttributeAssign ancestorAssign = ancestorStem.getAttributeDelegate().retrieveAssignment(null, 
            GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
        String attestationStemScope = ancestorAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName());
        
        //if we are blank (default to sub) or sub or the parent is same as ancestor (then ONE)
        if (StringUtils.isBlank(attestationStemScope) || Scope.SUB == Scope.valueOfIgnoreCase(attestationStemScope, true)
            || ancestorStem.equals(parentStem)) {
          
          this.parentStemWithAttestation = ancestorStem;
          this.stemInheritedAttributeAssignable = ancestorAssign;
          hasAttestation = true;
        }
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
  public boolean isHasAttestation() {
    this.attributeAssignableHelper();
    return this.hasAttestation;
  }
  
}
