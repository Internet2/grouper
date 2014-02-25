package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.MembershipGuiContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.membership.MembershipPath;
import edu.internet2.middleware.grouper.membership.MembershipPathGroup;
import edu.internet2.middleware.grouper.membership.MembershipPathNode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
      
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();

      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);

      MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyze(group, member, field);
      
      StringBuilder result = new StringBuilder();
      
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();
      for (MembershipPath membershipPath : GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths())) {
        if (membershipPath.isPathAllowed()) {
          membershipPathsAllowed.add(membershipPath);
        } else {
          membershipUnallowedCount++;
        }
      }

      //TODO show a message about the number of 
      
      //<p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
      //<p style="margin-left:20px;"><i class="icon-circle-arrow-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
      //<p style="margin-left:40px;"><i class="icon-circle-arrow-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
      //<p style="margin-left:60px"><i class="icon-circle-arrow-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
      //<hr />
      boolean firstPath = true;
      // loop through each membership path
      for (MembershipPath membershipPath : membershipPathsAllowed) {
        
        if (!firstPath) {
          result.append("<br /><hr /><br />\n");
        }
        
        int pathLineNumber = 0;
        membershipGuiContainer.setLineNumber(pathLineNumber);
        result.append(TextContainer.retrieveFromRequest().getText().get("membershipTracePathFirstLine")).append("\n");
        pathLineNumber++;
        membershipGuiContainer.setLineNumber(pathLineNumber);
        
        boolean firstNode = true;

        //loop through each node in the path
        for (MembershipPathNode membershipPathNode : membershipPath.getMembershipPathNodes()) {
          
          Group ownerGroup = membershipPathNode.getOwnerGroup();

          if (!firstNode) {

            if (membershipPathNode.isComposite()) {
              
              //dont know what branch of the composite we are on... so 
              Group factor = ownerGroup.equals(membershipPathNode.getLeftCompositeFactor()) 
                  ? membershipPathNode.getRightCompositeFactor() : membershipPathNode.getLeftCompositeFactor();
              membershipGuiContainer.setGuiGroupFactor(new GuiGroup(factor));
              switch(membershipPathNode.getCompositeType()) {
                case UNION:
                  
                  result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfUnion")).append("\n");
                  break;
                case INTERSECTION:

                  result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfIntersection")).append("\n");
                  break;
                case COMPLEMENT:
                  
                  result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfMinus")).append("\n");
                  break;
                default:
                  throw new RuntimeException("Not expecting composite type: " + membershipPathNode.getCompositeType());  
              }
              
            } else {
              result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupMemberOf")).append("\n");
              
            }

            pathLineNumber++;
            membershipGuiContainer.setLineNumber(pathLineNumber);

          }
          
          membershipGuiContainer.setGuiGroupCurrent(new GuiGroup(ownerGroup));

          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupLine")).append("\n");

          firstNode = false;
          pathLineNumber++;
          membershipGuiContainer.setLineNumber(pathLineNumber);
        }
        
        firstPath = false;
      }
      
      grouperRequestContainer.getMembershipGuiContainer().setTraceMembershipsString(result.toString());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceMembership.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

}
