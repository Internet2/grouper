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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomUiVariableName;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiEngine;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiTextType;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(UiV2CustomUi.class);

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
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      CustomUiContainer customUiContainer = grouperRequestContainer.getCustomUiContainer();
      customUiContainer.setLeaveGroupButtonPressed(true);
      customUiContainer.setEnroll(false);

      customUiGroupLogic(request, member.getSubject());   

      if (customUiContainer.isManageMembership()) {
        group.deleteMember(member, false);
        
        customUiContainer.setHasComputedLogic(false);
        customUiGroupLogic(request, member.getSubject());   
      }

      // true if a GSH script or compiled template ran, since it may have changed memberships the screen reads
      boolean scriptRan = false;
      {
        String gshScript = (String)customUiContainer.getTextTypeToText().get(CustomUiTextType.gshScript.name());
        if (!StringUtils.isBlank(gshScript)) {
          customUiContainer.gshRunScript(group, member.getSubject(), loggedInSubject, gshScript);
          scriptRan = true;
        }
        
      }

      customUiContainer.getCustomUiEngine().sendEmail(customUiContainer.overrideMap());

      recomputeLogicAfterScript(request, member.getSubject(), customUiContainer, scriptRan);

      String redirectToUrl = (String)customUiContainer.getTextTypeToText().get(CustomUiTextType.redirectToUrl.name());
      if (!StringUtils.isBlank(redirectToUrl)) {
        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newScript("location.href = '" + GrouperUiUtils.escapeJavascript(redirectToUrl, true) + "'"));
        return;
      }
      
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
    customUiContainer.setManagerAction(true);
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      lookupGroup(request);

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
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      CustomUiContainer customUiContainer = grouperRequestContainer.getCustomUiContainer();

      if (customUiContainer.isManagerAction() && !customUiContainer.isManager()) {
        throw new RuntimeException("Not manager! " + SubjectHelper.getPretty(loggedInSubject));
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String redirectToUrl = (String)customUiContainer.getTextTypeToText().get(CustomUiTextType.redirectToUrl.name());
      if (!StringUtils.isBlank(redirectToUrl)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("location.href = '" + GrouperUiUtils.escapeSingleQuotes(redirectToUrl) + "'"));
        return;
      }

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
        
        // this runs as root so users without VIEW can use a custom UI; only allow groups that
        // actually have an enabled custom UI, and fail without revealing the group name
        Group group = retrieveCustomUiGroupOrFail(inner_grouperSession, groupId);
        
        if (group.getTypeOfGroup() == TypeOfGroup.entity) {
          throw new RuntimeException("Not implemented for entities!");
        }

        groupContainer.setGuiGroup(new GuiGroup(group));      
        return group;
      }
    });
  }

  /**
   * Find the group for the custom UI screens. Those screens look the group up in a root session,
   * since a custom UI is meant for users who may not have VIEW on the group. So only a group that
   * has an enabled custom UI configuration is returned. For any other group, or a uuid that does
   * not exist, this throws an exception whose message holds only the caller-supplied id and is the
   * same in both cases, so the error text does not reveal the group name or whether it exists.
   * (Before this check, a group with no custom UI made CustomUiEngine throw an error containing
   * the full group name, which the UI showed to the user.)
   * @param rootSession root grouper session
   * @param groupId group uuid from the request
   * @return the group, never null
   */
  static Group retrieveCustomUiGroupOrFail(GrouperSession rootSession, String groupId) {

    if (StringUtils.isBlank(groupId)) {
      throw new RuntimeException("Cant find groupId!");
    }

    Group group = GroupFinder.findByUuid(rootSession, groupId, false);

    // same message for "no such group" and "group has no enabled custom UI" so this endpoint
    // is not an existence or group name oracle
    if (group == null || StringUtils.isBlank(CustomUiEngine.retrieveCustomUiConfigurationConfigId(group, true))) {
      throw new RuntimeException("Cant find group with custom UI: " + groupId);
    }
    return group;
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
      // working on someone else: the custom UI engine must be initialized before the
      // manager check since isManager() reads the custom UI config from the engine
      ensureCustomUiEngine(request);
      if (!customUiContainer.isManager()) {
        throw new RuntimeException("Not manager! " + SubjectHelper.getPretty(loggedInSubject));
      }
      member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
    }
    customUiContainer.setMember(member);
    return member;
  }

  /**
   * make sure the custom UI engine (and its config) is initialized so that authorization
   * checks (e.g. isManager()) can run before the full group logic executes. This only loads
   * the custom UI config, which depends on the group and not on the operated-on subject.
   * customUiGroupLogic() runs later and rebuilds the engine for the actual operated-on subject.
   * @param request
   */
  private void ensureCustomUiEngine(final HttpServletRequest request) {

    final CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();

    if (customUiContainer.getCustomUiEngine() != null) {
      return;
    }

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    final Map<String, String> urlParamVariables = new HashMap<String, String>();
    Enumeration<String> parameterNames = request.getParameterNames();
    if (parameterNames != null) {
      while (parameterNames.hasMoreElements()) {
        String parameterName = parameterNames.nextElement();
        // only names like cu_someName: the name is later written into Groovy source run as root
        if (CustomUiVariableName.isUrlParameterVariableName(parameterName)) {
          urlParamVariables.put(parameterName, request.getParameter(parameterName));
        }
      }
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession inner_grouperSession) throws GrouperSessionException {

        Group group = lookupGroup(request);

        CustomUiEngine customUiEngine = new CustomUiEngine();
        customUiEngine.setUrlParameters(urlParamVariables);
        customUiContainer.setCustomUiEngine(customUiEngine);

        customUiEngine.processGroupStep1(group, loggedInSubject, loggedInSubject);

        return null;
      }
    });
  }

  /**
   * @param request
   * @param subject
   */
  public void customUiGroupLogic(final HttpServletRequest request, final Subject subject) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    final CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();

    if (customUiContainer.isHasComputedLogic()) {
      return;
    }

    final Map<String, String> urlParamVariables = new HashMap<String, String>();
    Enumeration<String> parameterNames = request.getParameterNames();
    if (parameterNames != null) {
      while (parameterNames.hasMoreElements()) {
        String parameterName = parameterNames.nextElement();
        // only names like cu_someName: the name is later written into Groovy source run as root
        if (CustomUiVariableName.isUrlParameterVariableName(parameterName)) {
          urlParamVariables.put(parameterName, request.getParameter(parameterName));
        }
      }
    }

    final Group[] group = new Group[1];
    final CustomUiEngine[] customUiEngine = new CustomUiEngine[1];
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession inner_grouperSession) throws GrouperSessionException {

        group[0] = lookupGroup(request);
        
        customUiEngine[0] = new CustomUiEngine();
        
        customUiEngine[0].setUrlParameters(urlParamVariables);
        
        customUiContainer.setCustomUiEngine(customUiEngine[0]);

        customUiEngine[0].processGroupStep1(group[0], loggedInSubject, subject);

        customUiEngine[0].userQueryVariables().put("cu_managerIsLoggedIn", customUiContainer.isManager());
        customUiEngine[0].userQueryVariables().put("cu_grouperEnroll", customUiContainer.isEnroll());

        return null;
      }
    
    });

    if (customUiContainer.isCanAssignVariables()) {
      if (urlParamVariables.size() > 0) {
        boolean needsReset = true;
        final Map<String, Object> userQueryVariables = customUiContainer.getCustomUiEngine().userQueryVariables();
        for (String parameterName : urlParamVariables.keySet()) {
          String urlParamValue = urlParamVariables.get(parameterName);
          Object currentValue = userQueryVariables.get(parameterName);
          if (currentValue instanceof Boolean) {
            userQueryVariables.put(parameterName, GrouperUtil.booleanValue(urlParamValue));
          } else if (currentValue instanceof Long) {
            userQueryVariables.put(parameterName, GrouperUtil.longValue(urlParamValue));
          } else if (currentValue instanceof String) {
            userQueryVariables.put(parameterName, urlParamValue);
          } else {
            String newValue = urlParamValue;
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
        if (needsReset) {
          customUiContainer.resetCache();
        }
      }
    }
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession inner_grouperSession) throws GrouperSessionException {
        
        customUiEngine[0].processGroupStep2(group[0], loggedInSubject, subject);

        return null;
      }
    
    });

    
    if (customUiContainer.isCanSeeScreenState()) {
      customUiContainer.getCustomUiEngine().generateCustomUiTextResultsAll(customUiContainer.overrideMap());
    }
    
    customUiContainer.setHasComputedLogic(true);
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
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      CustomUiContainer customUiContainer = grouperRequestContainer.getCustomUiContainer();
      customUiContainer.setJoinGroupButtonPressed(true);
      customUiContainer.setEnroll(true);

      customUiGroupLogic(request, member.getSubject());   

      if (customUiContainer.isManageMembership()) {
        group.addMember(member.getSubject(), false);
        
        customUiContainer.setHasComputedLogic(false);
        customUiGroupLogic(request, member.getSubject());   
      }
      
      // true if a GSH script or compiled template ran, since it may have changed memberships the screen reads
      boolean scriptRan = false;
      {
        String gshScript = (String)customUiContainer.getTextTypeToText().get(CustomUiTextType.gshScript.name());
        if (!StringUtils.isBlank(gshScript)) {
          customUiContainer.gshRunScript(group, member.getSubject(), loggedInSubject, gshScript);
          scriptRan = true;
        }
        
      }

      customUiContainer.getCustomUiEngine().sendEmail(customUiContainer.overrideMap());

      recomputeLogicAfterScript(request, member.getSubject(), customUiContainer, scriptRan);

      //cu_grouperEnroll=true,
      String redirectToUrl = (String)customUiContainer.getTextTypeToText().get(CustomUiTextType.redirectToUrl.name());
      if (!StringUtils.isBlank(redirectToUrl)) {
        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newScript("location.href = '" + GrouperUiUtils.escapeSingleQuotes(redirectToUrl) + "'"));
        return;
      }

      customUiGroup(request, response);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * After the join or leave button, the screen variables (cu_*) were computed before the GSH script or
   * compiled template ran.  A script commonly changes memberships the queries read (e.g. it adds the
   * person to a trusted group that a cuQuery checks), so without recomputing, the page would redraw
   * from the pre-click values and show the old state until the user refreshes.  Recompute so the
   * redrawn page (and any redirectToUrl gate) reflects what the script did.  The button-pressed
   * variables (cu_joinGroupButtonPressed etc) are kept, since they come from the container, not the
   * queries.
   * @param request
   * @param subject the person the screen is about
   * @param customUiContainer
   * @param scriptRan true if a script or compiled template ran; if not, nothing changed after the last compute
   */
  private void recomputeLogicAfterScript(HttpServletRequest request, Subject subject,
      CustomUiContainer customUiContainer, boolean scriptRan) {
    if (!scriptRan) {
      return;
    }
    customUiContainer.setHasComputedLogic(false);
    customUiGroupLogic(request, subject);
  }
  
}

