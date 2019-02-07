/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembership;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.MembershipGuiContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.membership.MembershipContainer;
import edu.internet2.middleware.grouper.membership.MembershipPath;
import edu.internet2.middleware.grouper.membership.MembershipPathGroup;
import edu.internet2.middleware.grouper.membership.MembershipPathNode;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
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
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "membership")) {
        membershipGuiContainer.setTraceMembershipFromMembership(true);
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
        
        Subject currentSubject = subject;

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
          
          Membership membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), ownerGroup, currentSubject, false);
          membershipGuiContainer.setGuiMembershipCurrent(new GuiMembership(membership));
          
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupLine")).append("\n");
          
          firstNode = false;
          pathLineNumber++;
          membershipGuiContainer.setLineNumber(pathLineNumber);
          
          currentSubject = ownerGroup.toSubject();
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
  
      List<MembershipPath> allMembershipPaths = new ArrayList<MembershipPath>();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(group, member);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
      
      //lets try with every entity too
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(group, everyEntitySubject);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
            
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();

      for (MembershipPath membershipPath : allMembershipPaths) {
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
      
      tracePrivilegesHelper(membershipPathsAllowed, true, false, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/tracePrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * trace the 
   * @param membershipPathsAllowed
   * @param isGroup
   * @param isStem
   * @param isAttributeDef
   */
  public void tracePrivilegesHelper(List<MembershipPath> membershipPathsAllowed, boolean isGroup, boolean isStem, boolean isAttributeDef) {
    
    StringBuilder result = new StringBuilder();
    Subject everyEntitySubject = SubjectFinder.findAllSubject();

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
    
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
        for (Field field : GrouperUtil.nonNull(membershipPath.getFieldsIncludingImplied())) {
          if (!first) {
            privilegeNames.append(", ");
          }
          
          String textKey = "priv." + field.getName() + "Upper";

          String privilegeLabel = TextContainer.retrieveFromRequest().getText().get(textKey);
          
          privilegeNames.append(privilegeLabel);
          
          first = false;
        }
        
        membershipGuiContainer.setPrivilegeIncludingImpliedLabelsString(privilegeNames.toString());
      }

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
        if (SubjectHelper.eq(everyEntitySubject, membershipPath.getMember().getSubject())) {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathEveryEntityFirstLine")).append("\n");
        } else {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathFirstLine")).append("\n");
        }
      } else {
        if (SubjectHelper.eq(everyEntitySubject, membershipPath.getMember().getSubject())) {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTraceMembershipPathEveryEntityFirstLine")).append("\n");
        } else {
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTracePathFirstLine")).append("\n");
        }
      }
      pathLineNumber++;
      membershipGuiContainer.setLineNumber(pathLineNumber);
      
      boolean firstNode = true;
      
      //loop through each node in the path
      for (int i = 0; i < GrouperUtil.length(membershipPath.getMembershipPathNodes()); i++) {
        
        MembershipPathNode membershipPathNode = membershipPath.getMembershipPathNodes().get(i);
        
        Group ownerGroup = membershipPathNode.getOwnerGroup();
        Stem ownerStem = membershipPathNode.getOwnerStem();
        AttributeDef ownerAttributeDef = membershipPathNode.getOwnerAttributeDef();
 
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
        
        if (ownerGroup != null) {
          membershipGuiContainer.setGuiGroupCurrent(new GuiGroup(ownerGroup));
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupLine")).append("\n");
        } else if (ownerStem != null) {
          membershipGuiContainer.setGuiStemCurrent(new GuiStem(ownerStem));
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceStemLine")).append("\n");
        } else if (ownerAttributeDef != null) {
          membershipGuiContainer.setGuiAttributeDefCurrent(new GuiAttributeDef(ownerAttributeDef));
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceAttributeDefLine")).append("\n");
        }
 
 
        firstNode = false;
        pathLineNumber++;
        membershipGuiContainer.setLineNumber(pathLineNumber);
      }
      
      firstPath = false;
    }
    
    grouperRequestContainer.getMembershipGuiContainer().setTraceMembershipsString(result.toString());
  }

  /**
   * trace stem privileges
   * @param request
   * @param response
   */
  public void traceStemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    Subject subject = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
  
      if (stem == null) {
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
      grouperRequestContainer.getStemContainer().getGuiStem().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);
  
      List<MembershipPath> allMembershipPaths = new ArrayList<MembershipPath>();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(stem, member);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
      
      //lets try with every entity too
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(stem, everyEntitySubject);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
            
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();
  
      for (MembershipPath membershipPath : allMembershipPaths) {
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
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceStemNoPaths")));
        }
      }
      
      tracePrivilegesHelper(membershipPathsAllowed, false, true, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceStemPrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * trace attribute definition privileges
   * @param request
   * @param response
   */
  public void traceAttributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    Subject subject = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, 
          AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
  
      if (attributeDef == null) {
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
      grouperRequestContainer.getAttributeDefContainer().getGuiAttributeDef().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);
  
      List<MembershipPath> allMembershipPaths = new ArrayList<MembershipPath>();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(attributeDef, member);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
      
      //lets try with every entity too
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(attributeDef, everyEntitySubject);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
            
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();
  
      for (MembershipPath membershipPath : allMembershipPaths) {
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
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceAttributeDefNoPaths")));
        }
      }
      
      tracePrivilegesHelper(membershipPathsAllowed, false, false, true);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceAttributeDefPrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * save a membership
   * @param request
   * @param response
   */
  public void saveMembership(HttpServletRequest request, HttpServletResponse response) {

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
            TextContainer.retrieveFromRequest().getText().get("membershipEditNoMembershipFound")));
        
        return;
      }
      
      field = UiV2Membership.retrieveFieldHelper(request, true);
      
      if (field == null) {
        return;
      }      
      
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();

      membershipGuiContainer.setField(field);
      
      
      String hasMembershipString = request.getParameter("hasMembership[]");
      boolean hasMembership = GrouperUtil.booleanValue(hasMembershipString, false);


      Date startDate = null;
      try {
        String startDateString = request.getParameter("startDate");
        startDate = GrouperUtil.stringToTimestamp(startDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#member-start-date",
            TextContainer.retrieveFromRequest().getText().get("membershipEditFromDateInvalid")));
        return;
      }

      Date endDate = null;
      try {
        String endDateString = request.getParameter("endDate");
        endDate = GrouperUtil.stringToTimestamp(endDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#member-end-date",
            TextContainer.retrieveFromRequest().getText().get("membershipEditToDateInvalid")));
        return;
      }
      
      if (grouperRequestContainer.getGroupContainer().isCanAdmin()) { 
        boolean privOptins = GrouperUtil.booleanValue(request.getParameter("privilege_optins[]"), false);
        boolean privOptouts = GrouperUtil.booleanValue(request.getParameter("privilege_optouts[]"), false);
        boolean privViewers = GrouperUtil.booleanValue(request.getParameter("privilege_viewers[]"), false);
        boolean privReaders = GrouperUtil.booleanValue(request.getParameter("privilege_readers[]"), false);
        boolean privAdmins = GrouperUtil.booleanValue(request.getParameter("privilege_admins[]"), false);
        boolean privUpdaters = GrouperUtil.booleanValue(request.getParameter("privilege_updaters[]"), false);
        boolean privGroupAttrReaders = GrouperUtil.booleanValue(request.getParameter("privilege_groupAttrReaders[]"), false);
        boolean privGroupAttrUpdaters = GrouperUtil.booleanValue(request.getParameter("privilege_groupAttrUpdaters[]"), false);      
    
        if (!group.addOrEditMember(subject, false, hasMembership, privAdmins, privUpdaters, privReaders, privViewers, 
            privOptins, privOptouts, privGroupAttrReaders, privGroupAttrUpdaters, startDate, endDate, true)) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("membershipEditNoChange")));
          return;
        }
      } else {
        if (!group.addOrEditMember(subject, false, hasMembership, startDate, endDate, true)) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("membershipEditNoChange")));
          return;
        }
      }

      String backTo = request.getParameter("backTo");
      if (StringUtils.equals(backTo, "subject")) {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Subject.viewSubject&memberId=" + member.getId() + "');"));
              
      } else if (StringUtils.equals(backTo, "group")) {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "');"));

      } else {
        throw new RuntimeException("not expecting backTo: " + backTo);
      }

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("membershipEditSaveSuccess")));


    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit membership
   * @param request
   * @param response
   */
  public void editMembership(HttpServletRequest request, HttpServletResponse response) {
    
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
            TextContainer.retrieveFromRequest().getText().get("membershipEditNoMembershipFound")));
        
        return;
      }
      
      field = UiV2Membership.retrieveFieldHelper(request, true);
      
      if (field == null) {
        return;
      }      
      
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();

      membershipGuiContainer.setField(field);
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setEditMembershipFromSubject(true);
      }
  
      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);

      // get the privileges for this subject
      MembershipResult membershipResult = new MembershipFinder().assignFieldType(FieldType.ACCESS)
          .addMemberId(member.getId()).addGroup(group).findMembershipResult();
      
      GuiMembershipSubjectContainer guiMembershipSubjectContainer = GuiMembershipSubjectContainer.convertOneFromFinder(membershipResult);
      
      if (guiMembershipSubjectContainer != null) {
        MembershipSubjectContainer privilegeMembershipSubjectContainer = guiMembershipSubjectContainer.getMembershipSubjectContainer();
        privilegeMembershipSubjectContainer.considerAccessPrivilegeInheritance();
        //reset the gui
        guiMembershipSubjectContainer = new GuiMembershipSubjectContainer(privilegeMembershipSubjectContainer);
        
        membershipGuiContainer.setPrivilegeGuiMembershipSubjectContainer(guiMembershipSubjectContainer);

      }
      
      membershipResult = new MembershipFinder().addField(field).addMemberId(member.getId()).addGroup(group).findMembershipResult();
      guiMembershipSubjectContainer = GuiMembershipSubjectContainer.convertOneFromFinder(membershipResult);
      
      if (guiMembershipSubjectContainer != null) {

        membershipGuiContainer.setGuiMembershipSubjectContainer(guiMembershipSubjectContainer);
        
        MembershipContainer membershipContainer = guiMembershipSubjectContainer.getMembershipSubjectContainer()
            .getMembershipContainers().get(Group.getDefaultList().getName());
        
        if (membershipContainer != null) {

          Membership immediateMembership = membershipContainer.getImmediateMembership();
          if (immediateMembership != null) {  
            membershipGuiContainer.setDirectGuiMembership(new GuiMembership(immediateMembership));
          }
          membershipGuiContainer.setDirectMembership(membershipContainer.getMembershipAssignType().isImmediate());
          membershipGuiContainer.setIndirectMembership(membershipContainer.getMembershipAssignType().isNonImmediate());
        }

      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/editMembership.jsp"));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
}
