/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;
import java.util.List;

import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * one action for screen (response has many actions in a list
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiScreenAction implements Serializable {

  /**
   * message type in v2 ui
   */
  public static enum GuiMessageType {
    
    /** green message */
    success,
    
    /** blue message */
    info,
    
    /** red message */
    error;
  }
 
  /**
   * add a new message to the top of a v2 screen
   * @param guiMessageType
   * @param message
   * @return the action
   */
  public static GuiScreenAction newMessage(GuiMessageType guiMessageType, String message) {
    
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setMessage(message);
    guiScreenAction.setMessageType(guiMessageType.name());
    return guiScreenAction;
    
  }
  
  /**
   * add a message (v2)
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * add a message (v2)
   * @param message1
   */
  public void setMessage(String message1) {
    this.message = message1;
  }

  /**
   * add a message type (v2)
   * @return message type
   */
  public String getMessageType() {
    return this.messageType;
  }

  /**
   * add a message type (v2)
   * @param messageType1
   */
  public void setMessageType(String messageType1) {
    this.messageType = messageType1;
  }

  /** the place in object model to assign */
  private String assignmentName;
  
  /** the object to assign */
  private Object assignmentObject;

  /** alert to show on screen */
  private String alert;
  
  /** dialog to show on screen */
  private String dialog;
  
  /** select name to replace options */
  private String optionValuesSelectName;
  
  /** option values to replace (use with optionValuesSelectName) (could be null to blank it out) */
  private List<GuiOption> optionValues;
  
  /**
   * if changing the value of a form field, this is the name
   */
  private String formFieldName;

  /**
   * if changing the value of a form field, these are the values (list of size one for 
   * single values)
   */
  private List<String> formFieldValues;
  
  /**
   * add a message (v2)
   */
  private String message;
  
  /**
   * add a validation message
   */
  private String validationMessage;
  
  /**
   * add a validation message
   * @return validation message
   */
  public String getValidationMessage() {
    return this.validationMessage;
  }

  /**
   * add a validation message
   * @param validationMessage1
   */
  public void setValidationMessage(String validationMessage1) {
    this.validationMessage = validationMessage1;
  }

  /**
   * add a message type (v2)
   */
  private String messageType;
  
  
  
  /**
   * if changing the value of a form field, this is the name
   * @return the formFieldName
   */
  public String getFormFieldName() {
    return this.formFieldName;
  }

  
  /**
   * if changing the value of a form field, this is the name
   * @param formFieldName1 the formFieldName to set
   */
  public void setFormFieldName(String formFieldName1) {
    this.formFieldName = formFieldName1;
  }

  
  /**
   * if changing the value of a form field, these are the values (list of size one for 
   * single values)
   * @return the formFieldValue
   */
  public List<String> getFormFieldValues() {
    return this.formFieldValues;
  }

  
  /**
   * if changing the value of a form field, these are the values (list of size one for 
   * single values)
   * @param formFieldValue1 the formFieldValue to set
   */
  public void setFormFieldValues(List<String> formFieldValue1) {
    this.formFieldValues = formFieldValue1;
  }

  /**
   * option values to replace (use with optionValuesSelectName) (could be null to blank it out)
   * @return the optionValues
   */
  public List<GuiOption> getOptionValues() {
    return this.optionValues;
  }
  
  /**
   * option values to replace (use with optionValuesSelectName) (could be null to blank it out)
   * @param optionValues1 the optionValues to set
   */
  public void setOptionValues(List<GuiOption> optionValues1) {
    this.optionValues = optionValues1;
  }

  /**
   * select name to replace options
   * @return the optionValuesSelectName
   */
  public String getOptionValuesSelectName() {
    return this.optionValuesSelectName;
  }
  
  /**
   * select name to replace options
   * @param optionValuesSelectName1 the optionValuesSelectName to set
   */
  public void setOptionValuesSelectName(String optionValuesSelectName1) {
    this.optionValuesSelectName = optionValuesSelectName1;
  }


  /**
   * dialog to show on screen
   * @return the dialog
   */
  public String getDialog() {
    return this.dialog;
  }

  
  /**
   * dialog to show on screen
   * @param dialog1 the dialog to set
   */
  public void setDialog(String dialog1) {
    this.dialog = dialog1;
  }

  /**
   * alert to show on screen
   * @return the alert
   */
  public String getAlert() {
    return this.alert;
  }
  
  /**
   * alert to show on screen
   * @param alert1 the alert to set
   */
  public void setAlert(String alert1) {
    this.alert = alert1;
  }

  /**
   * run a javascript
   */
  private String script;
  
  /**
   * if replacing html, this is the jquery handle to replace, e.g. #someId
   */
  private String innerHtmlJqueryHandle;
  
  /**
   * this is the html (e.g. from JSP)
   */
  private String html;
  
  /**
   * this is the html (e.g. from JSP)
   * @return html
   */
  public String getHtml() {
    return this.html;
  }

  /**
   * this is the html (e.g. from JSP)
   * @param appendHtml1
   */
  public void setHtml(String appendHtml1) {
    this.html = appendHtml1;
  }


  /**
   * if appending html, this is the jquery handle to append in, e.g. #someId
   * @return handle
   */
  public String getAppendHtmlJqueryHandle() {
    return this.appendHtmlJqueryHandle;
  }


  /**
   * if appending html, this is the jquery handle to append in, e.g. #someId
   * @param appendHtmlJqueryHandle1
   */
  public void setAppendHtmlJqueryHandle(String appendHtmlJqueryHandle1) {
    this.appendHtmlJqueryHandle = appendHtmlJqueryHandle1;
  }

  /** hide show name to show */
  private String hideShowNameToShow;
  
  /**
   * if the alert should be centered or not (default true)
   * @return the alertCentered
   */
  public Boolean getAlertCentered() {
    return this.alertCentered;
  }
  
  /**
   * if the alert should be centered or not (default true)
   * @param alertCentered1 the alertCentered to set
   */
  public void setAlertCentered(Boolean alertCentered1) {
    this.alertCentered = alertCentered1;
  }

  /** hide show name to hide */
  private String hideShowNameToHide;
  
  /** hide show name to toggle */
  private String hideShowNameToToggle;

  /** if the current modal window should be closed */
  private Boolean closeModal;
  
  /** if the alert should be centered or not (default true) */
  private Boolean alertCentered;

  /**
   * if appending html, this is the jquery handle to append in, e.g. #someId
   */
  private String appendHtmlJqueryHandle;
  
  /**
   * hide show name to show
   * @return the hideShowNameToShow
   */
  public String getHideShowNameToShow() {
    return this.hideShowNameToShow;
  }

  
  /**
   * hide show name to show
   * @param hideShowNameToShow1 the hideShowNameToShow to set
   */
  public void setHideShowNameToShow(String hideShowNameToShow1) {
    this.hideShowNameToShow = hideShowNameToShow1;
  }

  
  /**
   * hide show name to hide
   * @return the hideShowNameToHide
   */
  public String getHideShowNameToHide() {
    return this.hideShowNameToHide;
  }

  
  /**
   * hide show name to hide
   * @param hideShowNameToHide1 the hideShowNameToHide to set
   */
  public void setHideShowNameToHide(String hideShowNameToHide1) {
    this.hideShowNameToHide = hideShowNameToHide1;
  }

  
  /**
   * hide show name to toggle
   * @return the hideShowNameToToggle
   */
  public String getHideShowNameToToggle() {
    return this.hideShowNameToToggle;
  }

  
  /**
   * hide show name to toggle
   * @param hideShowNameToToggle1 the hideShowNameToToggle to set
   */
  public void setHideShowNameToToggle(String hideShowNameToToggle1) {
    this.hideShowNameToToggle = hideShowNameToToggle1;
  }

  /**
   * @return the htmlReplaceJqueryHandle
   */
  public String getInnerHtmlJqueryHandle() {
    return this.innerHtmlJqueryHandle;
  }

  
  /**
   * @param htmlReplaceJqueryHandle1 the htmlReplaceJqueryHandle to set
   */
  public void setInnerHtmlJqueryHandle(String htmlReplaceJqueryHandle1) {
    this.innerHtmlJqueryHandle = htmlReplaceJqueryHandle1;
  }

  
  /**
   * run a javascript
   * @return the script
   */
  public String getScript() {
    return this.script;
  }
  
  /**
   * run a javascript
   * @param script1 the script to set
   */
  public void setScript(String script1) {
    this.script = script1;
  }

  /**
   * construct with the name and object
   * @param theAssignmentName
   * @param theAssignmentObject
   * @return the action
   */
  public static GuiScreenAction newAssign(String theAssignmentName, Object theAssignmentObject) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setAssignmentName(theAssignmentName);
    guiScreenAction.setAssignmentObject(theAssignmentObject);
    return guiScreenAction;
  }
  
  /**
   * construct with the name and object
   * @param theSelectName name of the select
   * @param theOptionValues values to replace (could be null to blank it out
   * @return the action
   */
  public static GuiScreenAction newOptionValues(String theSelectName, List<GuiOption> theOptionValues) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setOptionValuesSelectName(theSelectName);
    guiScreenAction.setOptionValues(theOptionValues);
    return guiScreenAction;
  }
  
  /**
   * construct with form field to change by name and value
   * @param theFormFieldName 
   * @param theFormFieldValue 
   * @return the action
   */
  public static GuiScreenAction newFormFieldValue(String theFormFieldName, String theFormFieldValue) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setFormFieldName(theFormFieldName);
    guiScreenAction.setFormFieldValues(GrouperUtil.toList(theFormFieldValue));
    return guiScreenAction;
  }
  
  /**
   * construct with form field to change by name and values (e.g. for checkboxes or multiselect)
   * @param theFormFieldName 
   * @param theFormFieldValues
   * @return the action
   */
  public static GuiScreenAction newFormFieldValues(String theFormFieldName, List<String> theFormFieldValues) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setFormFieldName(theFormFieldName);
    guiScreenAction.setFormFieldValues(theFormFieldValues);
    return guiScreenAction;
  }
  
  /**
   * construct with hide show name to show
   * @param theHideShowName
   * @return the action
   */
  public static GuiScreenAction newHideShowNameToShow(String theHideShowName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setHideShowNameToShow(theHideShowName);
    return guiScreenAction;
  }
  
  /**
   * construct with hide show name to hide
   * @param theHideShowName
   * @return the action
   */
  public static GuiScreenAction newHideShowNameToHide(String theHideShowName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setHideShowNameToHide(theHideShowName);
    return guiScreenAction;
  }
  
  /**
   * construct with hide show name to toggle
   * @param theHideShowName
   * @return the action
   */
  public static GuiScreenAction newHideShowNameToToggle(String theHideShowName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setHideShowNameToToggle(theHideShowName);
    return guiScreenAction;
  }
  
  /**
   * construct with the name and object
   * @param theAlert
   * @return the action
   */
  public static GuiScreenAction newAlert(String theAlert) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setAlert(theAlert);
    return guiScreenAction;
  }
  
  /**
   * construct with the name and object
   * @param theAlert
   * @param theAlertCentered if the alert should be centered
   * @return the action
   */
  public static GuiScreenAction newAlert(String theAlert, boolean theAlertCentered) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setAlert(theAlert);
    guiScreenAction.setAlertCentered(theAlertCentered);
    return guiScreenAction;
  }
  
  /**
   * construct with the name and object
   * @param theScript
   * @return the action
   */
  public static GuiScreenAction newScript(String theScript) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setScript(theScript);
    return guiScreenAction;
  }
  
  /**
   * construct with the name and JSP name
   * @param htmlReplaceJqueryHandle1 e.g. #someId
   * @param jspName e.g. /WEB-INF/templates/something.jsp
   * @return the action
   */
  public static GuiScreenAction newInnerHtmlFromJsp(String htmlReplaceJqueryHandle1, String jspName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    String jspContents = GrouperUiUtils.convertJspToString(jspName);
    
    guiScreenAction.setInnerHtmlJqueryHandle(htmlReplaceJqueryHandle1);
    guiScreenAction.setHtml(jspContents);
    
    return guiScreenAction;
  }
  
  /**
   * construct with the name and JSP name
   * @param htmlAppendJqueryHandle1 e.g. #someId
   * @param jspName e.g. /WEB-INF/templates/something.jsp
   * @return the action
   */
  public static GuiScreenAction newAppendHtmlFromJsp(String htmlAppendJqueryHandle1, String jspName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    String jspContents = GrouperUiUtils.convertJspToString(jspName);
    
    guiScreenAction.setAppendHtmlJqueryHandle(htmlAppendJqueryHandle1);
    guiScreenAction.setHtml(jspContents);
    
    return guiScreenAction;
  }
  
  /**
   * construct with JSP name
   * @param jspName e.g. /WEB-INF/templates/something.jsp
   * @return the action
   */
  public static GuiScreenAction newAlertFromJsp(String jspName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    String jspContents = GrouperUiUtils.convertJspToString(jspName);
    
    guiScreenAction.setAlert(jspContents);
    
    return guiScreenAction;
  }
  
  /**
   * construct with JSP name
   * @param jspName e.g. /WEB-INF/templates/something.jsp
   * @return the action
   */
  public static GuiScreenAction newDialogFromJsp(String jspName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    String jspContents = GrouperUiUtils.convertJspToString(jspName);
    
    guiScreenAction.setDialog(jspContents);
    
    return guiScreenAction;
  }
  
  /**
   * construct with an instruction to close the current modal window if open
   * @return the action
   */
  public static GuiScreenAction newCloseModal() {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setCloseModal(true);
    return guiScreenAction;
  }
  
  /**
   * default constructor
   */
  public GuiScreenAction() {
    super();
  }

  /**
   * the place in object model to assign
   * @return the assignmentName
   */
  public String getAssignmentName() {
    return this.assignmentName;
  }

  /**
   * the place in object model to assign
   * @param assignmentName1 the assignmentName to set
   */
  public void setAssignmentName(String assignmentName1) {
    this.assignmentName = assignmentName1;
  }

  /**
   * the object to assign
   * @return the assignmentObject
   */
  public Object getAssignmentObject() {
    return this.assignmentObject;
  }

  /**
   * the object to assign
   * @param assignmentObject1 the assignmentObject to set
   */
  public void setAssignmentObject(Object assignmentObject1) {
    this.assignmentObject = assignmentObject1;
  }

  /**
   * if the current modal window should be closed
   * @return the closeModal
   */
  public Boolean getCloseModal() {
    return this.closeModal;
  }

  /**
   * if the current modal window should be closed
   * @param closeModal1 the closeModal to set
   */
  public void setCloseModal(Boolean closeModal1) {
    this.closeModal = closeModal1;
  }

  /**
   * add a new message to the top of a v2 screen
   * @param guiMessageType
   * @param message
   * @return the action
   */
  public static GuiScreenAction newValidationMessage(GuiMessageType guiMessageType, 
      String jqueryHandle, String message) {
    
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    guiScreenAction.setValidationMessage(message);
    guiScreenAction.setInnerHtmlJqueryHandle(jqueryHandle);
    guiScreenAction.setMessageType(guiMessageType.name());
    return guiScreenAction;
    
  }

  /**
   * construct with the name and object
   * @param htmlReplaceJqueryHandle1 e.g. #someId
   * @param html is html to put on screen
   * @return the action
   */
  public static GuiScreenAction newAppendHtml(String htmlReplaceJqueryHandle1, String html) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    
    guiScreenAction.setAppendHtmlJqueryHandle(htmlReplaceJqueryHandle1);
    guiScreenAction.setHtml(html);
    
    return guiScreenAction;
  }


  /**
   * construct with the name and object
   * @param htmlReplaceJqueryHandle1 e.g. #someId
   * @param html is html to put on screen
   * @return the action
   */
  public static GuiScreenAction newInnerHtml(String htmlReplaceJqueryHandle1, String html) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    
    guiScreenAction.setInnerHtmlJqueryHandle(htmlReplaceJqueryHandle1);
    guiScreenAction.setHtml(html);
    
    return guiScreenAction;
  }


}
