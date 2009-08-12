/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.io.Serializable;

import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;

/**
 * one action for screen (response has many actions in a list
 * @author mchyzer
 */
public class GuiScreenAction implements Serializable {

  /** the place in object model to assign */
  private String assignmentName;
  
  /** the object to assign */
  private Object assignmentObject;

  /** alert to show on screen */
  private String alert;
  
  /** dialog to show on screen */
  private String dialog;
  
  
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
  private String innerHtml;
  
  /** hide show name to show */
  private String hideShowNameToShow;
  
  /** hide show name to hide */
  private String hideShowNameToHide;
  
  /** hide show name to toggle */
  private String hideShowNameToToggle;

  /** if the current modal window should be closed */
  private Boolean closeModal;
  
  
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
   * @return the htmlReplaceHtml
   */
  public String getInnerHtml() {
    return this.innerHtml;
  }

  
  /**
   * @param htmlReplaceHtml1 the htmlReplaceHtml to set
   */
  public void setInnerHtml(String htmlReplaceHtml1) {
    this.innerHtml = htmlReplaceHtml1;
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
    String jspContents = GuiUtils.convertJspToString(jspName);
    
    guiScreenAction.setInnerHtmlJqueryHandle(htmlReplaceJqueryHandle1);
    guiScreenAction.setInnerHtml(jspContents);
    
    return guiScreenAction;
  }
  
  /**
   * construct with JSP name
   * @param jspName e.g. /WEB-INF/templates/something.jsp
   * @return the action
   */
  public static GuiScreenAction newAlertFromJsp(String jspName) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    String jspContents = GuiUtils.convertJspToString(jspName);
    
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
    String jspContents = GuiUtils.convertJspToString(jspName);
    
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
   * construct with the name and object
   * @param htmlReplaceJqueryHandle1 e.g. #someId
   * @param html is html to put on screen
   * @return the action
   */
  public static GuiScreenAction newInnerHtml(String htmlReplaceJqueryHandle1, String html) {
    GuiScreenAction guiScreenAction = new GuiScreenAction();
    
    guiScreenAction.setInnerHtmlJqueryHandle(htmlReplaceJqueryHandle1);
    guiScreenAction.setInnerHtml(html);
    
    return guiScreenAction;
  }


}
