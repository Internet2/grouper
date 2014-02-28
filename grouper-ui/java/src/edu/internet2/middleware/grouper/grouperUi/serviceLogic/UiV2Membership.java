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
import edu.internet2.middleware.grouper.membership.MembershipType;
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
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setTraceMembershipFromSubject(true);
      }

      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);

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

      if (membershipUnallowedCount > 0) {
        membershipGuiContainer.setPathCountNotAllowed(membershipUnallowedCount);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupPathsNotAllowed")));
      }

      if (GrouperUtil.length(membershipPathsAllowed) == 0) {

        if (membershipUnallowedCount > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupNoPathsAllowed")));
          
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupNoPaths")));
          
        }
      }
      


      
      //<p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
      //<p style="margin-left:20px;"><i class="icon-circle-arrow-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
      //<p style="margin-left:40px;"><i class="icon-circle-arrow-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
      //<p style="margin-left:60px"><i class="icon-circle-arrow-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
      //<hr />
      boolean firstPath = true;
      // loop through each membership path
      for (MembershipPath membershipPath : membershipPathsAllowed) {
        
        if (!firstPath) {
          result.append("<hr />\n");
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
              Group factor = membershipPathNode.getOtherFactor();
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

  /**
   * trace group privileges
   * @param request
   * @param response
   */
  public void traceGroupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    Subject subject = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
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
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceNoPrivilegesFound")));
        
        return;
      }
            
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setTraceMembershipFromSubject(true);
      }
  
      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);
  
      MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(group, member);
      
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
  
      if (membershipUnallowedCount > 0) {
        membershipGuiContainer.setPathCountNotAllowed(membershipUnallowedCount);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupPathsNotAllowed")));
      }
  
      if (GrouperUtil.length(membershipPathsAllowed) == 0) {
  
        if (membershipUnallowedCount > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupNoPathsAllowed")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupNoPaths")));
        }
      }
      
      //<p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
      //<p style="margin-left:20px;"><i class="icon-circle-arrow-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
      //<p style="margin-left:40px;"><i class="icon-circle-arrow-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
      //<p style="margin-left:60px"><i class="icon-circle-arrow-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
      //<hr />
      boolean firstPath = true;
      // loop through each membership path
      for (MembershipPath membershipPath : membershipPathsAllowed) {
        
        if (!firstPath) {
          result.append("<hr />\n");
        }
        
        int pathLineNumber = 0;
        membershipGuiContainer.setLineNumber(pathLineNumber);
        
        //get privs like ADMIN, READ
        {
          StringBuilder privilegeNames = new StringBuilder();
          boolean first = true;
          for (Field field : GrouperUtil.nonNull(membershipPath.getFields())) {
            if (!first) {
              privilegeNames.append(", ");
            }
            
            String textKey = "priv." + field.getName() + "Upper";

            String privilegeLabel = TextContainer.retrieveFromRequest().getText().get(textKey);
            
            privilegeNames.append(privilegeLabel);
            
            first = false;
          }
          
          membershipGuiContainer.setPrivilegeLabelsString(privilegeNames.toString());
        }

        result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePrivilegesLine")).append("\n");
        if (membershipPath.getMembershipType() == MembershipType.IMMEDIATE) {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathFirstLine")).append("\n");
        } else {
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTracePathFirstLine")).append("\n");
        }
        pathLineNumber++;
        membershipGuiContainer.setLineNumber(pathLineNumber);
        
        boolean firstNode = true;
  
        //loop through each node in the path
        for (int i = 0; i < GrouperUtil.length(membershipPath.getMembershipPathNodes()); i++) {
          
          MembershipPathNode membershipPathNode = membershipPath.getMembershipPathNodes().get(i);
          
          Group ownerGroup = membershipPathNode.getOwnerGroup();
  
          if (!firstNode) {
  
            if (membershipPathNode.isComposite()) {
              
              //dont know what branch of the composite we are on... so 
              Group factor = membershipPathNode.getOtherFactor();
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
              
              //if last line
              if (i == GrouperUtil.length(membershipPath.getMembershipPathNodes()) - 1) {
                result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathLastLine")).append("\n");
                
              } else {
                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupMemberOf")).append("\n");
              }
              
              
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
          "/WEB-INF/grouperUi2/membership/tracePrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

}
