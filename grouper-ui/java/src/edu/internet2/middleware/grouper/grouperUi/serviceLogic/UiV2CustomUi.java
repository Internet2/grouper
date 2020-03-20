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

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomUiContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiEngine;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiUserType;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * operations in the group screen
 * @author mchyzer
 *
 */
public class UiV2CustomUi {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    //GrouperStartup.startup();

    Map<String, Object> variableMap = new LinkedHashMap<String, Object>();
    
    variableMap.put("cuo365twoStepRequiredDate", "2020/05/12");
    variableMap.put("textContainer", TextContainer.retrieveFromRequest());

//    String helplinkRaw = "<a href=\"https://www.isc.upenn.edu/how-to/penno365-office-365-proplus\">${textContainer.text['grouper.help'] }</a>";
//
//    String newString = GrouperUtil.substituteExpressionLanguage(helplinkRaw, variableMap, true, false, false);
//
//    System.out.println(newString);
//    
//    
//    String penn_o365twoStep_helplink = "${textContainer.text['penn_o365twoStep_helplink']}";
//    
//    newString = GrouperUtil.substituteExpressionLanguage(penn_o365twoStep_helplink, variableMap, true, false, false);
//
//    System.out.println(newString);
//
//    penn_o365twoStep_helplink = "${textContainer.text[\"penn_o365twoStep_helplink\"]}";
//    
//    newString = GrouperUtil.substituteExpressionLanguage(penn_o365twoStep_helplink, variableMap, true, false, false);
//
//    System.out.println(newString);

    String penn_o365twoStep_instructions_willBeRequiredToEnroll = "${textContainer.text['penn_o365twoStep_instructions_willBeRequiredToEnroll']}";

    String newString = GrouperUtil.substituteExpressionLanguage(penn_o365twoStep_instructions_willBeRequiredToEnroll, variableMap, true, false, false);

    System.out.println(newString);

//    String rawString = "To improve Penn's data security, you will be required to enroll in Two-Step Verification for O365 on <b>${cuo365twoStepRequiredDate.toString()}</b>.";
//    
//    newString = GrouperUtil.substituteExpressionLanguage(rawString, variableMap, true, false, false);
//    
//    System.out.println(newString);

  }
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2CustomUi.class);

  public void sendEmail(boolean enroll) {
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//    CustomUiContainer customUiContainer = grouperRequestContainer.getCustomUiContainer();
//    customUiContainer.getCustomUiEngine().getCustomUiOverallBean().getEmailBccToGroupName();
//    customUiContainer.getCustomUiEngine().getCustomUiOverallBean().getEmailToUser();
//    customUiContainer.getCustomUiEngine().getCustomUiOverallBean().getEmailUnenrollBody();
//    customUiContainer.getCustomUiEngine().getCustomUiOverallBean().getEmailUnenrollSubject();
    
    try {
      new GrouperEmail().setBody("Dear Katherine Wilson,\n\nYou have just " + (enroll ? "enrolled in" : "unenrolled from") + " Two-Step Verification for PennO365.\n\nIf you have questions please open a ticket.\n\nThanks!\nISC").setSubject("PennO365 Two-Step Verification " + (enroll ? "" : "un") + "enrollment").setTo("mchyzer@isc.upenn.edu").send();
    } catch (Exception e) {
      LOG.error("Error sending email", e);
    }

  }
  
  /**
   * leave the current group
   * @param request
   * @param response
   */
  public void leaveGroup(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.OPTOUT).getGroup();

      if (group == null) {
        return;
      }
      
      Member member = lookupMember(request);
      
      group.deleteMember(member, false);
      
      sendEmail(false);
      
      customUiGroup(request, response);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  

  /**
   * custom ui group
   * @param request
   * @param response
   */
  public void customUiGroupSubjectSubmit(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    CustomUiContainer customUiContainer = grouperRequestContainer.getCustomUiContainer();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      lookupGroup(request);
      
      if (!customUiContainer.isManager()) {
        throw new RuntimeException("Not manager! " + SubjectHelper.getPretty(loggedInSubject));
      }

      String subjectString = request.getParameter("groupAddMemberComboName");
      
      Subject subject = null;
      
      if (subjectString != null && subjectString.contains("||")) {
        String sourceId = GrouperUtil.prefixOrSuffix(subjectString, "||", true);
        String subjectId = GrouperUtil.prefixOrSuffix(subjectString, "||", false);
        subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);

      } else {
        subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
          
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("customUiCantFindSubject")));
        return;
      }      

      Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      
      customUiContainer.setMember(member);

      customUiGroup(request, response);

    } catch (RuntimeException re) {
      re.printStackTrace();
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }



  /**
   * custom ui group
   * @param request
   * @param response
   */
  public void customUiGroup(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      lookupGroup(request);
      
      Member member = lookupMember(request);

      customUiGroupLogic(request, member.getSubject());   

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      // replace outer too
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#theTopContainer", 
          "/WEB-INF/grouperUi2/index/indexCustomUiTopContainer.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/customUiGroup.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   * @param request
   * @return group
   */
  private Group lookupGroup(final HttpServletRequest request) {

    final GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
    if (groupContainer.getGuiGroup() != null) {
      return groupContainer.getGuiGroup().getGroup();
    }

    return (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession inner_grouperSession) throws GrouperSessionException {

        String groupId = request.getParameter("groupId");
        
        if (StringUtils.isBlank(groupId)) {
          throw new RuntimeException("Cant find groupId!");
        }
        Group group = GroupFinder.findByUuid(inner_grouperSession, groupId, true);
        
        if (group.getTypeOfGroup() == TypeOfGroup.entity) {
          throw new RuntimeException("Not implemented for entities!");
        }

        groupContainer.setGuiGroup(new GuiGroup(group));      
        return group;
      }
    });
  }

  /**
   * 
   * @param request
   * @return member
   */
  private Member lookupMember(final HttpServletRequest request) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    CustomUiContainer customUiContainer = grouperRequestContainer.getCustomUiContainer();

    if (customUiContainer.getMember() != null) {
      return customUiContainer.getMember();
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    final Member loggedInMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), loggedInSubject, true);

    String memberId = request.getParameter("memberId");
    if (StringUtils.isBlank(memberId)) {
      memberId = loggedInMember.getId();
    }
    Member member = null;

    if (StringUtils.equals(loggedInMember.getId(), memberId)) {
      member = loggedInMember;
    } else {
      if (!customUiContainer.isManager()) {
        throw new RuntimeException("Not manager! " + SubjectHelper.getPretty(loggedInSubject));
      }
      // working on someone else
      member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
    }
    customUiContainer.setMember(member);
    return member;
  }
  
  /**
   * @param request
   * @param subject
   */
  public void customUiGroupLogic(final HttpServletRequest request, final Subject subject) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    final CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession inner_grouperSession) throws GrouperSessionException {

        Group group = lookupGroup(request);
        
        CustomUiEngine customUiEngine = new CustomUiEngine();
        
        customUiContainer.setCustomUiEngine(customUiEngine);
        
        customUiEngine.processGroup(group, subject, loggedInSubject);

        return null;
      }
    
    });

    if (customUiContainer.isCanChangeVariables()) {
      Enumeration<String> parameterNames = request.getParameterNames();
      if (parameterNames != null) {
        boolean needsReset = false;
        final Map<String, Object> userQueryVariables = customUiContainer.getCustomUiEngine().userQueryVariables(CustomUiUserType.user);
        while (parameterNames.hasMoreElements()) {
          String parameterName = parameterNames.nextElement();
          if (parameterName.startsWith("cu_")) {
            needsReset = true;
            Object currentValue = userQueryVariables.get(parameterName);
            if (currentValue instanceof Boolean) {
              userQueryVariables.put(parameterName, GrouperUtil.booleanValue(request.getParameter(parameterName)));
            } else if (currentValue instanceof Long) {
              userQueryVariables.put(parameterName, GrouperUtil.longValue(request.getParameter(parameterName)));
            } else if (currentValue instanceof String) {
              userQueryVariables.put(parameterName, request.getParameter(parameterName));
            } else {
              String newValue = request.getParameter(parameterName);
              boolean isBoolean = false;
              try {
                GrouperUtil.booleanValue(newValue);
                isBoolean = true;
              } catch (Exception e) {
                // ignore
              }
              userQueryVariables.put(parameterName, isBoolean ? GrouperUtil.booleanValue(newValue) : newValue);
            }
          }
        }
        if (needsReset) {
          customUiContainer.resetCache();
        }
      }
    }
    
  }

  /**
   * leave the current group
   * @param request
   * @param response
   */
  public void joinGroup(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.OPTIN).getGroup();

      if (group == null) {
        return;
      }
    
      Member member = lookupMember(request);
      
      group.addMember(member.getSubject(), false);
      
      sendEmail(true);
      
      customUiGroup(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  
}

