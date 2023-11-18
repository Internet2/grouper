/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum CustomUiTextType {

  /**
   * gruop name of email bcc
   */
  emailBccGroupName,
  
  /**
   * if can see user environment
   */
  canSeeUserEnvironment,
  
  /**
   * if can assign screen state
   */
  canSeeScreenState,
  
  /**
   * if can assign variables, as user of app
   */
  canAssignVariables,
  
  /**
   * email body
   */
  emailBody,
  
  /**
   * email subject
   */
  emailSubject,

  /**
   * email to user, true or false
   */
  emailToUser,
  
  /**
   * logo link full with img and a href
   */
  logo,
  
  /**
   * help link in upper right including a href
   */
  helpLink,
  
  /**
   * h1 part
   */
  header,
  
  /**
   * part1 below h1
   */
  instructions1,
  
  /**
   * manager instructions
   */
  managerInstructions,
  
  /**
   * e.g. Enrollment status: enrolled
   */
  enrollmentLabel,
  
  /**
   * boolean if should show the enrollment button
   */
  enrollButtonShow,
  
  /**
   * text of enrollment button
   */
  enrollButtonText,
  
  /**
   * boolean if should show the unenrollment button
   */
  unenrollButtonShow,
  
  /**
   * boolean if should add/remove member
   */
  manageMembership,
  
  /**
   * gshScriptToRun (e.g. on leave or join group)
   * variables are: 
   *   subject: the subject being acted on
   *   group: group being acted on
   *   subjectLoggedIn: subject logged in might be acted on
   *   all cu_ variables of type string, boolean, numeric
   *   
   */
  gshScript,
  
  /**
   * redirect to URL
   */
  redirectToUrl,
  
  /**
   * text of unenrollment button
   */
  unenrollButtonText;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static CustomUiTextType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(CustomUiTextType.class, 
        string, exceptionOnNull);
  
  }

  /**
   * make a javabean
   * @return the name
   */
  public String getName() {
    return name();
  }
}
