package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.MembershipGuiContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.membership.MembershipPathGroup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

/**
 * operations on memberships
 * @author mchyzer
 *
 */
public class UiV2Membership {

  /**
   * get the field from the request
   * @param request
   * @return the field or null if not found
   */
  public static Field retrieveFieldHelper(HttpServletRequest request, boolean displayErrorIfProblem) {
  
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    MembershipGuiContainer guiMembershipContainer = grouperRequestContainer.getMembershipGuiContainer();

    Field field = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String fieldId = request.getParameter("fieldId");
    String fieldName = request.getParameter("field");

    if (StringUtils.isBlank(fieldName)) {
      fieldName = request.getParameter("fieldName");
    }
    
    boolean addedError = false;
    
    if (StringUtils.isBlank(fieldId) && StringUtils.isBlank(fieldName)) {
      if (!displayErrorIfProblem) {
        return null;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("membershipCantFindFieldId")));
      addedError = true;
    }
    
    if (!StringUtils.isBlank(fieldId)) {
      field = FieldFinder.findById(fieldId, false);
    } else if (!StringUtils.isBlank(fieldName)) {
      field = FieldFinder.find(fieldName, false);
    }
    
    if (field != null) {
      guiMembershipContainer.setField(field);      

    } else {
      
      if (!addedError) {
        if (!displayErrorIfProblem) {
          return null;
        }
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("membershipCantFindField")));
        addedError = true;
      }
      
    }
  
    //go back to the main screen, cant find group
    if (addedError) {
      if (displayErrorIfProblem) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      }
    }

    return field;
  }
  
  /**
   * trace membership
   * @param request
   * @param response
   */
  public void traceMembership(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    Subject subject = null;
    Field field = null;

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();

      if (group == null) {
        return;
      }
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);

      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("membershipTraceNoMembershipFound")));
        
        return;
      }
      
      field = UiV2Membership.retrieveFieldHelper(request, true);
      
      if (field == null) {
        return;
      }

      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);

      MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyze(group, member, field);
      
      String traceMembershipString = membershipPathGroup.toString();
      
      grouperRequestContainer.getMembershipGuiContainer().setTraceMembershipsString(traceMembershipString);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceMembership.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

}
