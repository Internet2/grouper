/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean that represents s attributes
 */
public class GrouperDeprovisioningAttributeValue {

  /**
   * 
   */
  public GrouperDeprovisioningAttributeValue() {
    super();
    
  }


  /**
   * configuration for the attribute value
   */
  private GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration;
   
  /**
   * @return the grouperDeprovisioningConfiguration
   */
  public GrouperDeprovisioningConfiguration getGrouperDeprovisioningConfiguration() {
    return this.grouperDeprovisioningConfiguration;
  }
  
  /**
   * @param grouperDeprovisioningConfiguration1 the grouperDeprovisioningConfiguration to set
   */
  public void setGrouperDeprovisioningConfiguration(
      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration1) {
    this.grouperDeprovisioningConfiguration = grouperDeprovisioningConfiguration1;
  }

  /**
   * set strings to defaults to reduce the number of assignment values we need
   */
  public void flattenValues() {
    
    if (!this.isAllowAddsWhileDeprovisioned()) {
      this.allowAddsWhileDeprovisionedString = null;
    }
    
    if (this.isAutoChangeLoader() == GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.autoChangeLoader", true)) {
      this.autoChangeLoaderString = null;
    }
    
    if (this.isAutoselectForRemoval()) {
      this.autoselectForRemovalString = null;
    }
    
    if (this.isDeprovision()) {
      this.deprovisionString = null;
    }

    // true for direct, false for inherited, blank for not assigned
    this.directAssignmentString = this.isDirectAssignment() ? "true" : "false";

    if (!this.isSendEmail()) {
      this.sendEmailString = null;
    }
    if (this.isShowForRemoval()) {
      this.showForRemovalString = null;
    }
    
  }
  
  
  /**
   * If allows adds to group of people who are deprovisioned
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   */
  private String allowAddsWhileDeprovisionedString;

  
  /**
   * If allows adds to group of people who are deprovisioned
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   * @return the allowAddsWhileDeprovisionedString
   */
  public String getAllowAddsWhileDeprovisionedString() {
    return this.allowAddsWhileDeprovisionedString;
  }

  /**
   * if allow adds while deprovisioned
   * @return true / false
   */
  public boolean isAllowAddsWhileDeprovisioned() {
    return GrouperUtil.booleanValue(this.allowAddsWhileDeprovisionedString, false);
  }
  
  /**
   * 
   * @param allowAddsWhileDeprovisioned true if show for removal
   */
  public void setAllowAddsWhileDeprovisioned(Boolean allowAddsWhileDeprovisioned) {
    this.allowAddsWhileDeprovisionedString = (allowAddsWhileDeprovisioned == null || !allowAddsWhileDeprovisioned) ? null : "true";
  }

  /**
   * If allows adds to group of people who are deprovisioned
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   * @param allowAddsWhileDeprovisionedString1 the allowAddsWhileDeprovisionedString to set
   */
  public void setAllowAddsWhileDeprovisionedString(String allowAddsWhileDeprovisionedString1) {
    this.allowAddsWhileDeprovisionedString = allowAddsWhileDeprovisionedString1;
  }

  /**
   * If this is a loader job, if being in a deprovisioned group means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   */
  private String autoChangeLoaderString;
  
  /**
   * 
   * @return true if auto change loader based on config
   */
  public boolean isAutoChangeLoader() {
    
    boolean defaultAutoChangeLoader = GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.autoChangeLoader", true);

    return GrouperUtil.booleanValue(this.autoChangeLoaderString, defaultAutoChangeLoader);
    
  }
  
  /**
   * 
   * @param autochangeLoader true if autochange loader
   */
  public void setAutoChangeLoader(Boolean autochangeLoader) {

    boolean defaultAutoChangeLoader = GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.autoChangeLoader", true);
    
    this.autoChangeLoaderString = (autochangeLoader == null || autochangeLoader == defaultAutoChangeLoader) ? null : Boolean.toString(autochangeLoader);
  }

  
  /**
   * If this is a loader job, if being in a deprovisioned group means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   * @return the autoChangeLoaderString
   */
  public String getAutoChangeLoaderString() {
    return this.autoChangeLoaderString;
  }
  
  /**
   * If this is a loader job, if being in a deprovisioned group means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   * @param autoChangeLoaderString1 the autoChangeLoaderString to set
   */
  public void setAutoChangeLoaderString(String autoChangeLoaderString1) {
    this.autoChangeLoaderString = autoChangeLoaderString1;
  }

  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   */
  private String autoselectForRemovalString;
  
  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   * @return the autoselectForRemoval
   */
  public String getAutoselectForRemovalString() {
    return this.autoselectForRemovalString;
  }
  
  /**
   * 
   * @return if autoselect for removal
   */
  public boolean isAutoselectForRemoval() {
    return GrouperUtil.booleanValue(this.autoselectForRemovalString, true);
  }
  
  
  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   * @param autoselectForRemoval1 the autoselectForRemoval to set
   */
  public void setAutoselectForRemovalString(String autoselectForRemoval1) {
    this.autoselectForRemovalString = autoselectForRemoval1;
  }


  /**
   * true|false, true to deprovision, false to not deprovision (default to true). 
   * Note, if this is set on a daemon job, then it will not deprovision any group 
   * in the loader job (they will be marked as such)
   */
  private String deprovisionString;
  

  /**
   * true|false, true to deprovision, false to not deprovision (default to true). 
   * Note, if this is set on a daemon job, then it will not deprovision any group 
   * in the loader job (they will be marked as such)
   * @return the deprovisionString
   */
  public String getDeprovisionString() {
    return this.deprovisionString;
  }

  /**
   * 
   * @param deprovision
   */
  public void setDeprovision(Boolean deprovision) {
    this.deprovisionString = deprovision == null ? null : (deprovision ? null: "false");
  }
  
  /**
   * if this is set then require a group name or email list
   * note this is not persisted in the database
   */
  private Boolean emailManagers;
  
  /**
   * 
   * @param theEmailManagers
   */
  public void setEmailManagers(Boolean theEmailManagers) {
    this.emailManagers = theEmailManagers;
  }
  
  /**
   * 
   * @return true if deprovision
   */
  public boolean isDeprovision() {

    // default to true, deprovision if there is configuration set
    return GrouperUtil.booleanValue(this.deprovisionString, true);
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
        .append( "deprovision", this.isDeprovision())
        .append("directAssignment", this.isDirectAssignment());
      
      if (!StringUtils.isBlank(this.allowAddsWhileDeprovisionedString)) {
        toStringBuilder.append("allowAddsWhileDeprovisioned", this.isAllowAddsWhileDeprovisioned());
      }
      if (!StringUtils.isBlank(this.autoChangeLoaderString)) {
        toStringBuilder.append("autoChangeLoader", this.isAutoChangeLoader());
      }
      if (!StringUtils.isBlank(this.autoselectForRemovalString)) {
        toStringBuilder.append("autoselectForRemoval", this.isAutoselectForRemoval());
      }
      if (!StringUtils.isBlank(this.directAssignmentString)) {
        toStringBuilder.append("directAssignment", this.isDirectAssignment());
      }
      if (!StringUtils.isBlank(this.emailAddressesString)) {
        toStringBuilder.append("emailAddresses", this.emailAddressesString);
      }
      if (!StringUtils.isBlank(this.emailBodyString)) {
        toStringBuilder.append("emailBody", this.emailBodyString);
      }
      if (!StringUtils.isBlank(this.sendEmailString)) {
        toStringBuilder.append("sendEmail", this.isSendEmail());
      }
      if (!StringUtils.isBlank(this.getEmailSubjectString())) {
        toStringBuilder.append("emailSubjectString", this.isSendEmail());
      }
      if (!StringUtils.isBlank(this.getInheritedFromFolderIdString())) {
        toStringBuilder.append("inheritedFromFolderIdString", this.getInheritedFromFolderIdString());
      }
      if (!StringUtils.isBlank(this.getMailToGroupString())) {
        toStringBuilder.append("mailToGroupString", this.getMailToGroupString());
      }
      if (!StringUtils.isBlank(this.getShowForRemovalString())) {
        toStringBuilder.append("showForRemovalString", this.isShowForRemoval());
      }
      if (!StringUtils.isBlank(this.getStemScopeString())) {
        toStringBuilder.append("stemScope", this.getStemScope());
      }
      if (!StringUtils.isBlank(this.getCertifiedMillisString())) {
        toStringBuilder.append("certifiedDate", this.getCertifiedDate());
      }
      if (!StringUtils.isBlank(this.getLastEmailedDateString())) {
        toStringBuilder.append("lastEmailed", this.getLastEmailedDate());
      }
        
    } catch (Exception e) {
      //ignore, did all we could
    }
    return toStringBuilder.toString();

  }
  
  /**
   * true|false, true to deprovision, false to not deprovision (default to true). 
   * Note, if this is set on a daemon job, then it will not deprovision any group 
   * in the loader job (they will be marked as such)
   * @param deprovisionString1 the deprovisionString to set
   */
  public void setDeprovisionString(String deprovisionString1) {
    this.deprovisionString = deprovisionString1;
  }

  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * true for direct, false for inherited, blank for not assigned
   */
  private String directAssignmentString;
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * true for direct, false for inherited, blank for not assigned
   * @return the directAssignmentString
   */
  public String getDirectAssignmentString() {
    return this.directAssignmentString;
  }
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * @return the directAssignmentString
   */
  public boolean isDirectAssignment() {
    return GrouperUtil.booleanValue(this.directAssignmentString, false);
  }
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * @param directAssignmentString1 the directAssignmentString to set
   */
  public void setDirectAssignmentString(String directAssignmentString1) {
    this.directAssignmentString = directAssignmentString1;
  }
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * @param directAssignment the directAssignmentString to set
   */
  public void setDirectAssignment(boolean directAssignment) {
    if (directAssignment) {
      this.directAssignmentString = "true";
    } else {
      this.directAssignmentString = null;
    }
  }


  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   */
  private String emailAddressesString;


  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   * @return the emailAddressesString
   */
  public String getEmailAddressesString() {
    return this.emailAddressesString;
  }


  
  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   * @param emailAddressesString1 the emailAddressesString to set
   */
  public void setEmailAddressesString(String emailAddressesString1) {
    this.emailAddressesString = emailAddressesString1;
  }

  /**
   * custom email body for emails, if blank use the default configured body. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  private String emailBodyString;
  
  /**
   * custom email body for emails, if blank use the default configured body. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @return the emailBodyString
   */
  public String getEmailBodyString() {
    return this.emailBodyString;
  }
  
  /**
   * custom email body for emails, if blank use the default configured body. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @param emailBodyString1 the emailBodyString to set
   */
  public void setEmailBodyString(String emailBodyString1) {
    this.emailBodyString = emailBodyString1;
  }

  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   */
  private String inheritedFromFolderIdString;

  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   * @return inherited from folder id
   */
  public String getInheritedFromFolderIdString() {
    return this.inheritedFromFolderIdString;
  }

  /**
   * if this is a stem attribute, this is the stem
   * @return the ownerStem
   */
  public Stem getInheritedFromFolder() {
    
    return this.inheritedFromFolderIdString == null ? null : GrouperDAOFactory.getFactory().getStem().findByUuid(this.inheritedFromFolderIdString, false);

  }


  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   * @param inheritedFromFolderIdString1
   */
  public void setInheritedFromFolderIdString(String inheritedFromFolderIdString1) {
    this.inheritedFromFolderIdString = inheritedFromFolderIdString1;
  }

  /**
   * custom subject for emails, if blank use the default configured subject. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  private String emailSubjectString;
  

  
  /**
   * custom subject for emails, if blank use the default configured subject. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @return the emailSubjectString
   */
  public String getEmailSubjectString() {
    return this.emailSubjectString;
  }


  
  /**
   * custom subject for emails, if blank use the default configured subject. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @param emailSubjectString1 the emailSubjectString to set
   */
  public void setEmailSubjectString(String emailSubjectString1) {
    this.emailSubjectString = emailSubjectString1;
  }


  /**
   * Group ID which holds people to email members of that group to send deprovisioning messages (mutually exclusive with deprovisioningEmailAddresses)
   */
  private String mailToGroupString;
  
  
  /**
   * @return the mailToGroupString
   */
  public String getMailToGroupString() {
    return this.mailToGroupString;
  }


  
  /**
   * @param mailToGroupString1 the mailToGroupString to set
   */
  public void setMailToGroupString(String mailToGroupString1) {
    this.mailToGroupString = mailToGroupString1;
  }

  /**
   * Affiliation configured in the grouper.properties
   */
  private String affiliationString;
  
  
  /**
   * @return the affiliationString
   */
  public String getAffiliationString() {
    return this.affiliationString;
  }


  
  /**
   * @param affiliationString1 the affiliationString to set
   */
  public void setAffiliationString(String affiliationString1) {
    this.affiliationString = affiliationString1;
  }

  /**
   * true|false, default to false. Set this to true for objects where the system of record is outside of grouper or where manual removal is preferred
   */
  private String sendEmailString;
  
  /**
   * 
   * @return true if send email
   */
  public boolean isSendEmail() {
    return GrouperUtil.booleanValue(this.sendEmailString, false);
  }
  
  /**
   * 
   * @return true if show for removal
   */
  public boolean isShowForRemoval() {
    return GrouperUtil.booleanValue(this.showForRemovalString, true);
  }
  
  /**
   * 
   * @param showForRemoval true if show for removal
   */
  public void setShowForRemoval(Boolean showForRemoval) {
    this.showForRemovalString = (showForRemoval == null || showForRemoval) ? null : "false";
  }
  
  /**
   * 
   * @param autoselectForRemoval true if show for removal
   */
  public void setAutoselectForRemoval(Boolean autoselectForRemoval) {
    this.autoselectForRemovalString = (autoselectForRemoval == null || autoselectForRemoval) ? null : "false";
  }

  /**
   * @return the sendEmailString
   */
  public String getSendEmailString() {
    return this.sendEmailString;
  }


  
  /**
   * @param sendEmailString1 the sendEmailString to set
   */
  public void setSendEmailString(String sendEmailString1) {
    this.sendEmailString = sendEmailString1;
  }


  /**
   * If the deprovisioning screen should show this object if the user as an assignment.
   * can be: blank, true, or false.  If blank, will default to true unless auto change loader is false.
   */
  private String showForRemovalString;
  

  
  /**
   * @return the showForRemovalString
   */
  public String getShowForRemovalString() {
    return this.showForRemovalString;
  }


  
  /**
   * @param showForRemovalString1 the showForRemovalString to set
   */
  public void setShowForRemovalString(String showForRemovalString1) {
    this.showForRemovalString = showForRemovalString1;
  }

  /**
   * yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day
   */
  private String lastEmailedDateString;
  
  /**
   * yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day
   * @return the deprovisioningLastEmailedDateString
   */
  public String getLastEmailedDateString() {
    return this.lastEmailedDateString;
  }
  
  /**
   * yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day
   * @param deprovisioningLastEmailedDateString1 the deprovisioningLastEmailedDateString to set
   */
  public void setLastEmailedDateString(String deprovisioningLastEmailedDateString1) {
    this.lastEmailedDateString = deprovisioningLastEmailedDateString1;
  }

  /**
   * 
   * @param date
   */
  public void setLastEmailedDate(Date date) {
    
    this.lastEmailedDateString = date == null ? null : new SimpleDateFormat("yyyy/MM/dd").format(new Date());

  }
  
  /**
   * yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day
   * @return the deprovisioningLastEmailedDateString
   */
  public Date getLastEmailedDate() {
    if (StringUtils.isBlank(this.lastEmailedDateString)) {
      return null;
    }
    try {
      return new SimpleDateFormat("yyyy/MM/dd").parse(this.lastEmailedDateString);
    } catch (ParseException e) {
      LOG.error("Could not convert '"+this.lastEmailedDateString+"' to date. " + this, e);
      
      //uh... just ignore
      return null;
    }

  }
  
  /**
   * (String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   */
  private String certifiedMillisString;

  /**
   * (String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   * @return the deprovisioningCertifiedMillisString
   */
  public String getCertifiedMillisString() {
    return this.certifiedMillisString;
  }
  
  /**
   * number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   * @return the deprovisioningCertifiedMillisString
   */
  public Long getCertifiedMillis() {
    return GrouperUtil.longObjectValue(this.certifiedMillisString, true);
  }
  
  /**
   * date that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   * @return the deprovisioningCertifiedMillisString
   */
  public Date getCertifiedDate() {
    
    Long deprovisioningCertifiedMillis = this.getCertifiedMillis();
    
    Date deprovisioningCertifiedDate = deprovisioningCertifiedMillis == null ? null : new Date(deprovisioningCertifiedMillis);
    
    return deprovisioningCertifiedDate;
  }
  
  /**
   * (String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   * @param deprovisioningCertifiedMillisString1 the deprovisioningCertifiedMillisString to set
   */
  public void setCertifiedMillisString(String deprovisioningCertifiedMillisString1) {
    this.certifiedMillisString = deprovisioningCertifiedMillisString1;
  }

  /**
   * (String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   * @param deprovisioningCertifiedMillis the deprovisioningCertifiedMillisString to set
   */
  public void setCertifiedMillis(Long deprovisioningCertifiedMillis) {
    this.certifiedMillisString = GrouperUtil.stringValue(deprovisioningCertifiedMillis);
  }

  /**
   * (String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   * @param deprovisioningCertifiedDate the deprovisioningCertified date to set
   */
  public void setCertifiedDate(Date deprovisioningCertifiedDate) {
    this.setCertifiedMillis(deprovisioningCertifiedDate == null ? null : deprovisioningCertifiedDate.getTime());
  }

  /**
   * one|sub, if in folder only or in folder and all subfolders (default to sub)
   */
  private String stemScopeString;
  
  /**
   * if this is set then require a group name 
   * note this is not persisted in the database
   */
  private Boolean emailGroupMembers;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningAttributeValue.class);

  
  /**
   * @return the stemScopeString
   */
  public String getStemScopeString() {
    return this.stemScopeString;
  }

  /**
   * get the stem scope if assigned to a stem
   * @return the scope
   */
  public Scope getStemScope() {
    if (StringUtils.isBlank(this.stemScopeString)) {
      return Scope.SUB;
    }
    return Scope.valueOfIgnoreCase(this.stemScopeString, true);
  }
  
  /**
   * @param stemScopeString1 the stemScopeString to set
   */
  public void setStemScopeString(String stemScopeString1) {
    this.stemScopeString = stemScopeString1;
  }

  /**
   * send email to managers of object.  if not sending to email list or group, then must be managers
   * @return true if send email to managers
   */
  public boolean isEmailManagers() {
    if (!this.isSendEmail()) {
      return false;
    }
    
    if (this.emailManagers != null) {
      return this.emailManagers;
    }
    
    return StringUtils.isBlank(this.emailAddressesString) && StringUtils.isBlank(this.mailToGroupString);
  }
  
  /**
   * if should send email, default false
   * @param theSendEmail
   */
  public void setSendEmail(boolean theSendEmail) {
    if (theSendEmail) {
      this.sendEmailString = "true";
    } else {
      this.sendEmailString = null;
    }

  }
  
  /**
   * set the scope and honor the defaults
   * @param scope
   */
  public void setStemScope(Scope scope) {
    if (scope != null && scope == Scope.ONE) {
      this.stemScopeString = scope.name();
    } else {
      this.stemScopeString = null;
    }
  }

  /**
   * send email to managers of object.  if not sending to email list or group, then must be managers
   * @return true if send email to managers
   */
  public boolean isEmailGroupMembers() {
    if (!this.isSendEmail()) {
      return false;
    }
    
    if (this.emailGroupMembers != null) {
      return this.emailGroupMembers;
    }
    
    return StringUtils.isBlank(this.emailAddressesString) && !this.isEmailManagers();
  }

  /**
   * 
   * @param theEmailGroupMembers
   */
  public void setEmailGroupMembers(Boolean theEmailGroupMembers) {
    this.emailGroupMembers = theEmailGroupMembers;
  }
}
