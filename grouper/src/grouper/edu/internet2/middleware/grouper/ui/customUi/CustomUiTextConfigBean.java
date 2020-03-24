/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * text config bean
 */
public class CustomUiTextConfigBean implements Comparable<CustomUiTextConfigBean> {

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }
  
  /**
   * if json is enabled, default true
   */
  private Boolean enabled;
  
  /**
   * if json is enabled, default true
   * @return the enabled
   */
  public Boolean getEnabled() {
    return this.enabled;
  }
  
  /**
   * if json is enabled, default true
   * @param enabled the enabled to set
   */
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }



  public static void main(String[] args) {
    CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.header.name());
//    customUiTextConfigBean.setDefaultText(true);
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_header']}");

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.helpLink.name());
//    customUiTextConfigBean.setDefaultText(true);
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_helplink']}");
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.managerInstructions.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setScript("${cu_o365twoStepAllowedToManage}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_manager']}");
//    customUiTextConfigBean.setEndIfMatches(true);

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.emailBccGroupName.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setText("penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepAllowedToAdmin");
//    customUiTextConfigBean.setEndIfMatches(true);

//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.emailBody.name());
//  customUiTextConfigBean.setIndex(0);
//  customUiTextConfigBean.setScript("${cu_grouperEnroll}");
//  customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enroll_emailBody']}");
//  customUiTextConfigBean.setEndIfMatches(true);

  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.emailToUser.name());
  customUiTextConfigBean.setIndex(0);
  customUiTextConfigBean.setText("true");

//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.emailSubject.name());
//  customUiTextConfigBean.setIndex(0);
//  customUiTextConfigBean.setText("${cu_grouperEnroll ? textContainer.text['penn_o365twoStep_enroll_emailSubject'] : textContainer.text['penn_o365twoStep_unenroll_emailSubject']}");
//  customUiTextConfigBean.setEndIfMatches(true);

//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.emailBody.name());
//  customUiTextConfigBean.setIndex(10);
//  customUiTextConfigBean.setScript("${!cu_grouperEnroll}");
//  customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_unenroll_emailBody']}");
//  customUiTextConfigBean.setEndIfMatches(true);

//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.canSeeUserEnvironment.name());
//  customUiTextConfigBean.setIndex(0);
//  customUiTextConfigBean.setText("${cu_o365twoStepAllowedToManage}");
//  customUiTextConfigBean.setEndIfMatches(true);

//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.canSeeScreenState.name());
//  customUiTextConfigBean.setIndex(0);
//  customUiTextConfigBean.setText("false");
//  customUiTextConfigBean.setEndIfMatches(true);

//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.canAssignVariables.name());
//  customUiTextConfigBean.setIndex(0);
//  customUiTextConfigBean.setText("${cu_o365twoStepAllowedToManage}");
//  customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
//    customUiTextConfigBean.setIndex(10);
//    customUiTextConfigBean.setScript("${cu_o365twoStepRequiredToEnroll}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_requiredToEnroll']}");
//    customUiTextConfigBean.setEndIfMatches(true);

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
//    customUiTextConfigBean.setIndex(20);
//    customUiTextConfigBean.setScript("${ !cu_o365twoStepRequiredToEnroll && cu_o365twoStepEnrolled && cu_o365hasMailbox && cu_o365twoStepCanEnrollUnenroll}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_willBeRequiredToEnroll']}");
//    customUiTextConfigBean.setEndIfMatches(true);

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
//    customUiTextConfigBean.setIndex(30);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_twoStepUsers}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_needsTwoStep']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
//    customUiTextConfigBean.setIndex(40);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_o365hasMailbox}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_needsO365']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
//    customUiTextConfigBean.setIndex(50);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && cu_twoStepUsers && cu_o365hasMailbox}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_notEnrolledButCanEnroll']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//  customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
//  customUiTextConfigBean.setIndex(60);
//  customUiTextConfigBean.setScript("${!cu_o365twoStepRequiredToEnroll}");
//  customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_notRequiredCannotEnroll']}");
//  customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepEnrolled && cu_o365twoStepInAzureError}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolledErrorInAzure']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(10);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepEnrolled && !cu_o365twoStepInAzure}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolled']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(20);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepEnrolled && (cu_o365twoStepInLdapError || cu_o365twoStepInLdap) && cu_o365twoStepInAzure}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolledPendingInLdapAndAzure']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(30);
//    customUiTextConfigBean.setScript("${!cu_o365twoStepEnrolled && cu_o365twoStepInAzure}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolledPendingNotInLdapButInAzure']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(40);
//    customUiTextConfigBean.setScript("${cu_o365twoStepEnrolled && cu_o365twoStepInAzureError}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_enrolledErrorInAzure']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(50);
//    customUiTextConfigBean.setScript("${cu_o365twoStepEnrolled && cu_o365twoStepInAzure}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_enrolled']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(60);
//    customUiTextConfigBean.setScript("${cu_o365twoStepEnrolled && (cu_o365twoStepInLdapError || !cu_o365twoStepInLdap) && !cu_o365twoStepInAzure}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_enrolledPendingInLdapAndAzure']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollmentLabel.name());
//    customUiTextConfigBean.setIndex(70);
//    customUiTextConfigBean.setScript("${cu_o365twoStepEnrolled && !cu_o365twoStepInAzure}");
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollLabel_enrolledPendingNotInLdapButInAzure']}");
//    customUiTextConfigBean.setEndIfMatches(true);

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollButtonShow.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setText("${!cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && cu_twoStepUsers && cu_o365hasMailbox}");
//    customUiTextConfigBean.setEndIfMatches(true);

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.enrollButtonText.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_enrollButtonText']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    
//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.unenrollButtonShow.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setText("${cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_o365twoStepRequiredToEnroll}");
//    customUiTextConfigBean.setEndIfMatches(true);

//    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.unenrollButtonText.name());
//    customUiTextConfigBean.setIndex(0);
//    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_unenrollButtonText']}");
//    customUiTextConfigBean.setEndIfMatches(true);
    

    System.out.println(GrouperUtil.jsonConvertTo(customUiTextConfigBean, false));


  }

  /**
   * true if this is the default if nothing else matches
   */
  private Boolean defaultText;
  
  
  /**
   * true if this is the default if nothing else matches
   * @return the defaultText
   */
  public Boolean getDefaultText() {
    return this.defaultText;
  }


  
  /**
   * true if this is the default if nothing else matches
   * @param defaultText the defaultText to set
   */
  public void setDefaultText(Boolean defaultText) {
    this.defaultText = defaultText;
  }


  /**
   * 
   */
  public CustomUiTextConfigBean() {
  }

  /**
   * enum of text type
   */
  private String customUiTextType;

  
  /**
   * @return the customUiTextType
   */
  public String getCustomUiTextType() {
    return this.customUiTextType;
  }

  
  /**
   * @param customUiTextType the customUiTextType to set
   */
  public void setCustomUiTextType(String customUiTextType) {
    this.customUiTextType = customUiTextType;
  }

  /**
   * index to order the rules if they end and need to end in order
   */
  private Integer index;


  
  /**
   * index to order the rules if they end and need to end in order
   * @return the index
   */
  public Integer getIndex() {
    return this.index;
  }


  
  /**
   * index to order the rules if they end and need to end in order
   * @param index the index to set
   */
  public void setIndex(Integer index) {
    this.index = index;
  }
  
  /**
   * dont evaluate other rules if this one matches
   */
  private Boolean endIfMatches;


  
  /**
   * dont evaluate other rules if this one matches
   * @return the endIfMatches
   */
  public Boolean getEndIfMatches() {
    return this.endIfMatches;
  }


  
  /**
   * dont evaluate other rules if this one matches
   * @param endIfMatches the endIfMatches to set
   */
  public void setEndIfMatches(Boolean endIfMatches) {
    this.endIfMatches = endIfMatches;
  }
  
  /**
   * script to execute to see if we should display this text.
   */
  private String script;
  
  /**
   * script to execute to see if we should display this text.
   * @return the script
   */
  public String getScript() {
    return this.script;
  }
  
  /**
   * script to execute to see if we should display this text.
   * @param script the script to set
   */
  public void setScript(String script) {
    this.script = script;
  }
  
  /**
   * text (not needed if this is a boolean variable).  Note, could be from externalized text.
   */
  private String text;
  
  /**
   * text (not needed if this is a boolean variable).  Note, could be from externalized text
   * @return the text
   */
  public String getText() {
    return this.text;
  }
  
  /**
   * text (not needed if this is a boolean variable).  Note, could be from externalized text
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }




  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(CustomUiTextConfigBean other) {
    if (other == null) {
      return 1;
    }
    if (this == other) {
      return 0;
    }
    Integer thisIndex = this.index;
    Integer otherIndex = other.index;
    if (thisIndex == otherIndex) {
      return 0;
    }
    if (thisIndex == null) {
      return -1;
    }
    if (otherIndex == null) {
      return 1;
    }
    return thisIndex.compareTo(otherIndex);
  }

  
}
