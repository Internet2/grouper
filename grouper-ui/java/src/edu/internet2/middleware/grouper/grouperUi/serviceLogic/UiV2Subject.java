package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;


public class UiV2Subject {

//  /**
//   * get the subject from the request
//   * @param request
//   * @param requirePrivilege (view is automatic)
//   * @return the subject or null if not found
//   */
//  private static Subject retrieveSubjectHelper(HttpServletRequest request, Privilege requirePrivilege) {
//  
//    //initialize the bean
//    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//    
//    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
//
//    SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();
//
//    Subject subject = null;
//  
//    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//
//    String sourceId = request.getParameter("sourceId");
//    String subjectId = request.getParameter("subjectId");
//    String subjectIdentifier = request.getParameter("subjectIdentifier");
//    String subjectIdOrIdentifier = request.getParameter("subjectIdOrIdentifier");
//    String memberId = request.getParameter("memberId");
//    
//    boolean addedError = false;
//    
//    if (!StringUtils.isBlank(subjectId)) {
//      subject = SubjectFinder.findById(subjectId, false);
//    } else if (!StringUtils.isBlank(subjectIdentifier)) {
//      subject = SubjectFinder.findByIdentifier(subjectIdentifier, false);
//    } else if (!StringUtils.isBlank(subjectIdOrIdentifier)) {
//      subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
//    } else if (!StringUtils.isBlank(memberId)) {
//      Member member = MemberFinder.findByUuid(grouperSession, memberId, false);
//      if (member != null) {
//        subject = member.getSubject();
//      }
//    } else {
//      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//          TextContainer.retrieveFromRequest().getText().get("groupCantFindGroupId")));
//      addedError = true;
//    }
//  
//    if (subject != null) {
//      subjectContainer.setGuiGroup(new GuiGroup(subject));      
//      boolean privsOk = true;
//
//      if (requirePrivilege != null) {
//        if (requirePrivilege.equals(AccessPrivilege.ADMIN)) {
//          if (!subjectContainer.isCanAdmin()) {
//            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminGroup")));
//            addedError = true;
//            privsOk = false;
//          }
//        } else if (requirePrivilege.equals(AccessPrivilege.VIEW)) {
//          if (!subjectContainer.isCanView()) {
//            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToViewGroup")));
//            addedError = true;
//            privsOk = false;
//          }
//        } else if (requirePrivilege.equals(AccessPrivilege.READ)) {
//          if (!subjectContainer.isCanRead()) {
//            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToReadGroup")));
//            addedError = true;
//            privsOk = false;
//          }
//        } else if (requirePrivilege.equals(AccessPrivilege.UPDATE)) {
//          if (!subjectContainer.isCanUpdate()) {
//            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToUpdateGroup")));
//            addedError = true;
//            privsOk = false;
//          }
//        }  
//      }
//      
//      if (privsOk) {
//        result.setGroup(subject);
//      }
//
//    } else {
//      
//      if (!addedError && (!StringUtils.isBlank(groupId) || !StringUtils.isBlank(groupName) || !StringUtils.isBlank(groupIndex))) {
//        result.setAddedError(true);
//        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//            TextContainer.retrieveFromRequest().getText().get("groupCantFindGroup")));
//        addedError = true;
//      }
//      
//    }
//    result.setAddedError(addedError);
//  
//    //go back to the main screen, cant find group
//    if (addedError) {
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
//    }
//
//    return result;
//  }
//
//  /**
//   * view group
//   * @param request
//   * @param response
//   */
//  public void viewGroup(HttpServletRequest request, HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    
//    GrouperSession grouperSession = null;
//  
//    Group group = null;
//  
//    try {
//  
//      grouperSession = GrouperSession.start(loggedInSubject);
//  
//      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
//      
//      if (group == null) {
//        return;
//      }
//  
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/group/viewGroup.jsp"));
//
//      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
//        filterHelper(request, response, group);
//      }
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//    
//  }

  
}
