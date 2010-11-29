package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttrFramework;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectInviteBean;
import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.ExternalRegisterContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * logic for the invite process of external subjects
 * @author mchyzer
 *
 */
public class InviteExternalSubjects {

  /** logger */
  private static final Log LOG = LogFactory.getLog(InviteExternalSubjects.class);

  /**
   * invite external subjects
   * @param request
   * @param response
   */
  public void inviteExternalSubject(HttpServletRequest request, HttpServletResponse response) {

    if (!this.allowedToInvite()) {
      return;
    }
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //setup the container
    final ExternalRegisterContainer externalRegisterContainer = new ExternalRegisterContainer();
    externalRegisterContainer.storeToRequest();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/inviteExternalSubjects/inviteExternalSubjects.jsp"));

  }
  
  /**
   * filter groups to pick one to assign
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void groupToAssignFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    if (!this.allowedToInvite()) {
      return;
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Set<Group> groups = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("inviteExternalSubjects.errorNotEnoughGroupChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("inviteExternalMembers.groupComboboxResultSize", 200), 1, true).sortAsc("theGroup.displayNameDb");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%" + searchTerm + "%", grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
        
        //remove filtered groups
        Iterator<Group> iterator = GrouperUtil.nonNull(groups).iterator();
        while (iterator.hasNext()) {
          Group currentGroup = iterator.next();
          if (filterGroup(currentGroup)) {
            iterator.remove();
          }
        }
        
        if (GrouperUtil.length(groups) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("inviteExternalSubjects.errorNoGroupsFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {
  
        String value = group.getUuid();
        String label = GrouperUiUtils.escapeHtml(group.getDisplayName(), true);
        String imageName = GrouperUiUtils.imageFromSubjectSource("g:gsa");
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("inviteExternalSubjects.errorTooManyGroups", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for groups: '" + GrouperUiUtils.escapeHtml(searchTerm, true) + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for groups: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * if we should filter this group
   * @param group
   * @return true if filter, false if not
   */
  private static boolean filterGroup(Group group) {
    boolean allowWheel = TagUtils.mediaResourceBoolean("inviteExternalMembers.allowWheelInInvite", false);
    boolean useWheel = GrouperConfig.getPropertyBoolean("groups.wheel.use", false);
    String wheelName = GrouperConfig.getProperty("groups.wheel.group");
    
    if (!allowWheel && useWheel && !StringUtils.isBlank(wheelName) && StringUtils.equals(wheelName, group.getName())) {
      return true;
    }

    return false;
  }
  
  /**
   * see if the user is allowed to register
   * @param displayErrorIfNotProblem if we should display an error (i.e. the first screen, not the second if editing)
   * @return true if ok, false if not, also might have a side effect of an error message
   */
  private boolean allowedToInvite() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    if (!TagUtils.mediaResourceBoolean("inviteExternalMembers.enableInvitation", false)) {
      String message = TagUtils.navResourceString("inviteExternalSubjects.notAllowed");
      guiResponseJs.addAction(GuiScreenAction.newAlert(message));
      return false;
    }
    return true;
  }
  /**
   * filter groups to pick one to assign
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void submit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    if (!this.allowedToInvite()) {
      return;
    }

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //marshal data
    String emailAddressesToInvite = StringUtils.trimToNull(httpServletRequest.getParameter("emailAddressesToInvite"));

    String emailSubject = StringUtils.trimToNull(httpServletRequest.getParameter("emailSubject"));
    
    String messageToUsers = StringUtils.trimToNull(httpServletRequest.getParameter("messageToUsers"));
    
    String ccEmailAddress = StringUtils.trimToNull(httpServletRequest.getParameter("ccEmailAddress"));
    
    String groupToAssign0 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign0"));
    String groupToAssign1 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign1"));
    String groupToAssign2 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign2"));
    String groupToAssign3 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign3"));
    String groupToAssign4 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign4"));

    //validate
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Subject subject = grouperSession.getSubject();
      
      if (StringUtils.isBlank(emailAddressesToInvite)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(TagUtils.navResourceString("inviteExternalSubjects.emailAddressRequired")));
        return;
      }
      
      //lets normalize the email addresses
      emailAddressesToInvite = GrouperUtil.normalizeEmailAddresses(emailAddressesToInvite);
      for (String emailAddress : GrouperUtil.splitTrim(emailAddressesToInvite, ";")) {
        
        if (!GrouperUtil.validEmail(emailAddress)) {
          
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.emailAddressInvalid");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(emailAddress, true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return;
        }
      }
      //lets normalize the email addresses
      ccEmailAddress = GrouperUtil.normalizeEmailAddresses(ccEmailAddress);
      if (!StringUtils.isBlank(ccEmailAddress)) {
        for (String emailAddress : GrouperUtil.splitTrim(ccEmailAddress, ";")) {
          
          if (!GrouperUtil.validEmail(emailAddress)) {
            
            String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.emailAddressInvalid");
            errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(emailAddress, true));
            guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
            return;
  
          }
        }
      }
      
      Set<String> groupsToAssign = GrouperUtil.toSet(groupToAssign0, groupToAssign1, groupToAssign2, groupToAssign3, groupToAssign4);
      Set<String> groupsToAssignFinal = new HashSet<String>();
      for (String groupToAssignUuid : groupsToAssign) {
        
        if (StringUtils.isBlank(groupToAssignUuid)) {
          continue;
        }
        
        Group group = GroupFinder.findByUuid(grouperSession, groupToAssignUuid, false);
        if (group == null || filterGroup(group)) {
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupUuid");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(groupToAssignUuid, true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return;
          
        }
        if (!group.hasUpdate(subject) && !group.hasAdmin(subject)) {
          
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupPrivileges");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(group.getDisplayName(), true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return;
        }
        groupsToAssignFinal.add(group.getUuid());
      }

      ExternalSubjectInviteBean externalSubjectInviteBean = new ExternalSubjectInviteBean();
      if (!StringUtils.isBlank(ccEmailAddress)) {
        externalSubjectInviteBean.setEmailsWhenRegistered(GrouperUtil.splitTrimToSet(ccEmailAddress, ";"));
      }
      Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      externalSubjectInviteBean.setMemberId(member.getUuid());
      
      externalSubjectInviteBean.setGroupIds(groupsToAssignFinal);
      
      //send the invite
      String error = ExternalSubjectAttrFramework.inviteExternalUsers(GrouperUtil.splitTrimToSet(emailAddressesToInvite, ";"), 
          externalSubjectInviteBean, emailSubject, messageToUsers);      
      
      if (!StringUtils.isBlank(error)) {
        String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.errorInvitingUsers");
        errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(error, true));
        guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
        return;
        
      }

      String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.successInvitingUsers");
      guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/inviteExternalSubjects/inviteExternalSubjects.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
}
