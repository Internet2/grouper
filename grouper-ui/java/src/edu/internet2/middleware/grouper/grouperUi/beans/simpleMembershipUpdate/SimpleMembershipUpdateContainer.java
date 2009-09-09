/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.2 2009-09-09 15:20:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.subject.Subject;



/**
 * bean for simple membership update.  holds all state for this module
 */
public class SimpleMembershipUpdateContainer implements Serializable {

  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("simpleMembershipUpdateContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static SimpleMembershipUpdateContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = (SimpleMembershipUpdateContainer)httpSession
      .getAttribute("simpleMembershipUpdateContainer");
    if (simpleMembershipUpdateContainer == null) {
      throw new NoSessionException(GuiUtils.message("simpleMembershipUpdate.noContainer"));
    }
    return simpleMembershipUpdateContainer;
  }

  /** if can read group */
  private boolean canReadGroup;
  
  /** if can update group */
  private boolean canUpdateGroup;

  /**
   * group object
   */
  private GuiGroup guiGroup;

  /**
   * members in result
   */
  private GuiMember[] guiMembers;

  /**
   * set of name value pairs for screen
   */
  private Map<String, String> subjectDetails = new LinkedHashMap<String, String>();
  
  /**
   * subject for screen
   */
  private Subject subjectForDetails;
  
  /**
   * filter by this, could be the key which is sourceId____subjectId, or just a string literal if nothing is selected
   */
  private String memberFilter;
  
  /**
   * if showing the user what is being filtered, put that here.  This wouldnt be
   * sourceId____subjectId, it would be friendlier
   */
  private String memberFilterForScreen;
  
  /**
   * if showing the user what is being filtered, put that here.  This wouldnt be
   * sourceId____subjectId, it would be friendlier
   * @return the memberFilterForScreen
   */
  public String getMemberFilterForScreen() {
    return this.memberFilterForScreen;
  }
  
  /**
   * if showing the user what is being filtered, put that here.  This wouldnt be
   * sourceId____subjectId, it would be friendlier
   * @param memberFilterForScreen1 the memberFilterForScreen to set
   */
  public void setMemberFilterForScreen(String memberFilterForScreen1) {
    this.memberFilterForScreen = memberFilterForScreen1;
  }

  /**
   * filter by this, could be the key which is sourceId____subjectId, or just a string literal if nothing is selected
   * @return the memberFilter
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  
  /**
   * filter by this, could be the key which is sourceId____subjectId, or just a string literal if nothing is selected
   * @param memberFilter1 the memberFilter to set
   */
  public void setMemberFilter(String memberFilter1) {
    this.memberFilter = memberFilter1;
  }

  /**
   * subject for screen
   * @return the subjectForDetails
   */
  public Subject getSubjectForDetails() {
    return this.subjectForDetails;
  }
  
  /**
   * subject for screen
   * @param subjectForDetails1 the subjectForDetails to set
   */
  public void setSubjectForDetails(Subject subjectForDetails1) {
    this.subjectForDetails = subjectForDetails1;
  }

  /**
   * set of name value pairs for screen
   * @return the subjectDetails
   */
  public Map<String, String> getSubjectDetails() {
    return this.subjectDetails;
  }

  
  /**
   * set of name value pairs for screen
   * @param subjectDetails1 the subjectDetails to set
   */
  public void setSubjectDetails(Map<String, String> subjectDetails1) {
    this.subjectDetails = subjectDetails1;
  }

  /**
   * 
   * @return the group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * group object
   * @param group1
   */
  public void setGuiGroup(GuiGroup group1) {
    this.guiGroup = group1;
  }

  /**
   * 
   * @return if can read group
   */
  public boolean isCanReadGroup() {
    return this.canReadGroup;
  }

  /**
   * if can read group
   * @param canReadGroup1
   */
  public void setCanReadGroup(boolean canReadGroup1) {
    this.canReadGroup = canReadGroup1;
  }

  /**
   * if can update group
   * @return if can update group
   */
  public boolean isCanUpdateGroup() {
    return this.canUpdateGroup;
  }

  /**
   * if can update group
   * @param canUpdateGroup1
   */
  public void setCanUpdateGroup(boolean canUpdateGroup1) {
    this.canUpdateGroup = canUpdateGroup1;
  }

  /**
   * members in result
   * @return members
   */
  public GuiMember[] getGuiMembers() {
    return this.guiMembers;
  }

  /**
   * members in result
   * @param members1
   */
  public void setGuiMembers(GuiMember[] members1) {
    this.guiMembers = members1;
  }
  
}
