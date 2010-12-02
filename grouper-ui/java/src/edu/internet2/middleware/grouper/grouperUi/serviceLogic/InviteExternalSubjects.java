package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttrFramework;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectInviteBean;
import edu.internet2.middleware.grouper.grouperUi.beans.inviteExternalSubjects.InviteExternalSubjectsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperEmail;
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
    
    GuiHideShow.init("inviteExternalSubjectEmails", true, null, null, false);

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //setup the container
    final InviteExternalSubjectsContainer inviteExternalSubjectsContainer = new InviteExternalSubjectsContainer();
    inviteExternalSubjectsContainer.storeToRequest();

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
  public static boolean filterGroup(Group group) {
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
  public void submit(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {

    if (!this.allowedToInvite()) {
      return;
    }
    
    //validate
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    //setup the container
    final InviteExternalSubjectsContainer inviteExternalSubjectsContainer = new InviteExternalSubjectsContainer();
    inviteExternalSubjectsContainer.storeToRequest();
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

            //marshal data
            //lets normalize the email addresses
            final String emailAddressesToInvite = GrouperUtil.normalizeEmailAddresses(
                StringUtils.trimToNull(httpServletRequest.getParameter("emailAddressesToInvite")));

            final String emailSubject = StringUtils.trimToNull(httpServletRequest.getParameter("emailSubject"));
            
            final String messageToUsers = StringUtils.trimToNull(httpServletRequest.getParameter("messageToUsers"));
            
            final String ccEmailAddress = GrouperUtil.normalizeEmailAddresses(
                StringUtils.trimToNull(httpServletRequest.getParameter("ccEmailAddress")));
            
            String groupToAssign0 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign0"));
            String groupToAssign1 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign1"));
            String groupToAssign2 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign2"));
            String groupToAssign3 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign3"));
            String groupToAssign4 = StringUtils.trimToNull(httpServletRequest.getParameter("groupToAssign4"));

            String inviteBy = StringUtils.trimToNull(httpServletRequest.getParameter("inviteBy"));
            final String loginIdsToInvite = GrouperUtil.normalizeEmailAddresses(
                StringUtils.trimToNull(httpServletRequest.getParameter("loginIdsToInvite")));
            
            boolean inviteByEmailBoolean = StringUtils.isBlank(inviteBy) || StringUtils.equals(inviteBy, "emailAddress");
            
            if (!inviteByEmailBoolean && !StringUtils.equals(inviteBy, "identifier")) {
              throw new RuntimeException("Invalid inviteBy: " + inviteBy);
            }
            
            if (!inviteByEmailBoolean && !inviteExternalSubjectsContainer.isAllowInviteByIdentifier()) {
              guiResponseJs.addAction(GuiScreenAction.newAlert(TagUtils.navResourceString("inviteExternalSubjects.errorNotAllowedToInviteByIdentifer")));
              return null;
            }
            
            GrouperSession grouperSession = null;
          
            try {
              grouperSession = GrouperSession.start(loggedInSubject);
              
              Subject subject = grouperSession.getSubject();
              
              if (inviteByEmailBoolean && StringUtils.isBlank(emailAddressesToInvite)) {
                guiResponseJs.addAction(GuiScreenAction.newAlert(TagUtils.navResourceString("inviteExternalSubjects.emailAddressRequired")));
                return null;
              }
              
              if (inviteByEmailBoolean) {
                for (String emailAddress : GrouperUtil.splitTrim(emailAddressesToInvite, ";")) {
                  
                  if (!GrouperUtil.validEmail(emailAddress)) {
                    
                    String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.emailAddressInvalid");
                    errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(emailAddress, true));
                    guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
                    return null;
                  }
                }

                if (!StringUtils.isBlank(ccEmailAddress)) {
                  for (String emailAddress : GrouperUtil.splitTrim(ccEmailAddress, ";")) {
                    
                    if (!GrouperUtil.validEmail(emailAddress)) {
                      
                      String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.emailAddressInvalid");
                      errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(emailAddress, true));
                      guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
                      return null;
            
                    }
                  }
                }
                
              }
              
              Set<Group> groupObjectsToAssignFinal = new LinkedHashSet<Group>();
              Set<String> groupsToAssign = GrouperUtil.toSet(groupToAssign0, groupToAssign1, groupToAssign2, groupToAssign3, groupToAssign4);
              Set<String> groupsToAssignFinal = new HashSet<String>();
              StringBuilder groupIdsToAssign = new StringBuilder();
              StringBuilder groupNamesToAssign = new StringBuilder();
              
              for (String groupToAssignUuid : groupsToAssign) {
                
                if (StringUtils.isBlank(groupToAssignUuid)) {
                  continue;
                }
                
                Group group = GroupFinder.findByUuid(grouperSession, groupToAssignUuid, false);
                if (group == null || filterGroup(group)) {
                  String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupUuid");
                  errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(groupToAssignUuid, true));
                  guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
                  return null;
                  
                }
                if (!group.hasUpdate(subject) && !group.hasAdmin(subject)) {
                  
                  String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupPrivileges");
                  errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(group.getDisplayName(), true));
                  guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
                  return null;
                }
                groupsToAssignFinal.add(group.getUuid());
                if (groupIdsToAssign.length() > 0) {
                  groupIdsToAssign.append(", ");
                }
                groupIdsToAssign.append(group.getUuid());
                
                if (groupNamesToAssign.length() > 0) {
                  groupNamesToAssign.append(", ");
                }
                groupNamesToAssign.append(group.getName());
                groupObjectsToAssignFinal.add(group);
                
              }
        
              Member member = MemberFinder.findBySubject(grouperSession, subject, true);
              StringBuilder subjectsToStringToInvite = new StringBuilder();
              StringBuilder messagesToScreen = new StringBuilder();

              if (inviteByEmailBoolean) {
                final ExternalSubjectInviteBean externalSubjectInviteBean = new ExternalSubjectInviteBean();
                if (!StringUtils.isBlank(ccEmailAddress)) {
                  externalSubjectInviteBean.setEmailsWhenRegistered(GrouperUtil.splitTrimToSet(ccEmailAddress, ";"));
                }
                externalSubjectInviteBean.setMemberId(member.getUuid());
                
                externalSubjectInviteBean.setGroupIds(groupsToAssignFinal);
                
                //send the invite as root
                String error = (String)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
                  
                  @Override
                  public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
                    return ExternalSubjectAttrFramework.inviteExternalUsers(GrouperUtil.splitTrimToSet(emailAddressesToInvite, ";"), 
                        externalSubjectInviteBean, emailSubject, messageToUsers);      

                  }
                });
                  
                  
                
                if (!StringUtils.isBlank(error)) {
                  String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.errorInvitingUsers");
                  errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(error, true));
                  guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
                  return null;
                  
                }
              } else {
                
                //invite by identifier
                Set<Subject> subjectsToInvite = new HashSet<Subject>();
                
                //these have already been normalized by semicolon
                for (String loginId : GrouperUtil.splitTrim(loginIdsToInvite, ";")) {
                  
                  //lets see if this is already a subject
                  //is it an externalSubject?
                  Subject subjectToAssign = SubjectFinder.findByIdOrIdentifierAndSource(loginId, ExternalSubject.sourceId(), false);
                  if (subjectToAssign == null) {
                    subjectToAssign = SubjectFinder.findByIdOrIdentifier(loginId, false);
                  }
                  if (subjectToAssign == null) {
                    //if it is still null, then it doesnt exist... lets validate it
                    final ExternalSubject externalSubject = new ExternalSubject();
                    externalSubject.setIdentifier(loginId);
                    try {
                      externalSubject.validateIdentifier();
                    } catch (Exception e) {
                      
                      LOG.warn("Invalid identifier: " + loginId, e);
  
                      String message = TagUtils.navResourceString("externalSubjectSelfRegister.invalidIdentifierInvite");
                      
                      //note there is a java way to do this... hmmmm...
                      message = StringUtils.replace(message, "{0}", loginId);

                      if (messagesToScreen.length() > 0) {
                        messagesToScreen.append("<br /><br />");
                      }
                      messagesToScreen.append(message);
                      
                      continue;
                    }
                    
                    //lets store this, without validation... as root
                    //send the invite as root
                    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
                      
                      @Override
                      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
                        externalSubject.store(null, null, false);
                        return null;
                      }
                    });
                    
                    String message = TagUtils.navResourceString("inviteExternalSubjects.successCreatedExternalSubject");
                    
                    message = StringUtils.replace(message, "{0}", loginId);

                    if (messagesToScreen.length() > 0) {
                      messagesToScreen.append("<br /><br />");
                    }
                    messagesToScreen.append(message);
                    
                    subjectToAssign = SubjectFinder.findByIdAndSource(externalSubject.getUuid(), ExternalSubject.sourceId(), true);

                  } else {
                    String message = TagUtils.navResourceString("inviteExternalSubjects.successExistedExternalSubject");
                    
                    message = StringUtils.replace(message, "{0}", loginId);

                    if (messagesToScreen.length() > 0) {
                      messagesToScreen.append("<br /><br />");
                    }
                    messagesToScreen.append(message);
                    
                    
                  }
                  
                  //now we have the subject
                  subjectsToStringToInvite.append(GrouperUtil.subjectToString(subjectToAssign));

                  subjectsToInvite.add(subjectToAssign);
                  
                  //lets assign to groups
                  for (Group group : groupObjectsToAssignFinal) {
                    
                    try {
                      boolean added = group.addMember(subjectToAssign, false);
                      
                      String navKey = null;

                      if (added) {
                        //inviteExternalSubjects.successAssignedSubjectToGroup=Success: entity {0} was assigned to group {1}
                        navKey = "inviteExternalSubjects.successAssignedSubjectToGroup";
                      } else {
                        //inviteExternalSubjects.noteSubjectAlreadyInGroup=Note: entity {0} was already a member of group {1}
                        navKey = "inviteExternalSubjects.noteSubjectAlreadyInGroup";
                      }
                      String message = TagUtils.navResourceString(navKey);
                      
                      message = StringUtils.replace(message, "{0}", loginId);
                      message = StringUtils.replace(message, "{1}", group.getDisplayName());

                      if (messagesToScreen.length() > 0) {
                        messagesToScreen.append("<br /><br />");
                      }
                      messagesToScreen.append(message);
                      
                    } catch (Exception e) {
                      LOG.error("Cant add subject to group: " + loginId + ", " + GrouperUtil.subjectToString(subjectToAssign) + ", " + group.getName(), e);
                      
                      // Error: could not assign entity: {0} to group: {1}. {2}
                      String message = TagUtils.navResourceString("inviteExternalSubjects.errorAssigningGroup");
                      
                      //note there is a java way to do this... hmmmm...
                      message = StringUtils.replace(message, "{0}", loginId);
                      message = StringUtils.replace(message, "{1}", group.getDisplayName());
                      message = StringUtils.replace(message, "{2}", e.getMessage());

                      if (messagesToScreen.length() > 0) {
                        messagesToScreen.append("<br /><br />");
                      }

                      messagesToScreen.append(message);
                      continue;
                      
                    }
                  }
                  

                }
              }
              
              if (messagesToScreen.length() == 0) {
                messagesToScreen.append(TagUtils.navResourceString("inviteExternalSubjects.successInvitingUsers"));
              }
              
              StringBuilder emailBody  = new StringBuilder();
              
              if (inviteByEmailBoolean) {
                if (!StringUtils.isBlank(emailAddressesToInvite)) {
                  emailBody.append("Email addresses to invite: ").append(emailAddressesToInvite).append("\n");
                }
                if (!StringUtils.isBlank(ccEmailAddress)) {
                  emailBody.append("Email addresses to notify once registered: ").append(ccEmailAddress).append("\n");
                }
                if (!StringUtils.isBlank(emailSubject)) {
                  emailBody.append("Email subject: ").append(emailSubject).append("\n");
                }
                if (!StringUtils.isBlank(messageToUsers)) {
                  emailBody.append("Message to users: ").append(messageToUsers).append("\n");
                }
                long expireMillis = ExternalSubjectAttrFramework.expireMillisAfter1970();
                if (expireMillis > 0) {
                  emailBody.append("Invitation expires on: ").append(new Timestamp(expireMillis).toString()).append("\n");
                }
                
              } else {
                
                if (!StringUtils.isBlank(loginIdsToInvite)) {
                  emailBody.append("Login IDs to invite: ").append(loginIdsToInvite).append("\n");
                }
                
              }
              if (groupNamesToAssign.length() > 0) {
                emailBody.append("Group Names (ID Path) to assign: ").append(groupNamesToAssign).append("\n");
              }
              if (groupIdsToAssign.length() > 0) {
                emailBody.append("Group UUIDs to assign: ").append(groupIdsToAssign).append("\n");
              }
              String stringMessageToScreen = messagesToScreen.toString();
              stringMessageToScreen = StringUtils.replace(stringMessageToScreen, "<br />", "\n");
              stringMessageToScreen = StringUtils.replace(stringMessageToScreen, "\n\n", "\n");
              emailBody.append("Message to screen: ").append(stringMessageToScreen);

              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {

                if (inviteByEmailBoolean) {
                  //EXTERNAL_SUBJECT_INVITE_EMAIL: "emailsSentTo", "inviterMemberId", "groupIdsAssigned", "groupNamesAssigned")),

                  AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJECT_INVITE_EMAIL, 
                      "emailsSentTo", emailAddressesToInvite,
                      "inviterMemberId", member.getUuid(),
                      "groupIdsAssigned", groupIdsToAssign.length() == 0 ? null : groupIdsToAssign.toString(),
                      "groupNamesAssigned", groupNamesToAssign.length() == 0 ? null : groupNamesToAssign.toString());
                  auditEntry.setDescription(emailBody.toString());
                  auditEntry.saveOrUpdate(true);
                  
                } else {
                  //EXTERNAL_SUBJECT_INVITE_IDENTIFIER: "identifiers", "inviterMemberId", "groupIdsAssigned", "groupNamesAssigned"));
                  
                  AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJECT_INVITE_IDENTIFIER, 
                      "identifiers", loginIdsToInvite,
                      "inviterMemberId", member.getUuid(),
                      "groupIdsAssigned", groupIdsToAssign.length() == 0 ? null : groupIdsToAssign.toString(),
                      "groupNamesAssigned", groupNamesToAssign.length() == 0 ? null : groupNamesToAssign.toString());
                  auditEntry.setDescription(emailBody.toString());
                  auditEntry.saveOrUpdate(true);
                  
                }
              }
              
              //send an audit to admins perhaps
              {
                String emailAddressesForAdmins = TagUtils.mediaResourceString("inviteExternalMembers.emailAdminsAddressesAfterActions");
                if (!StringUtils.isBlank(emailAddressesForAdmins)) {
                  String loggedInSubjectDescription = loggedInSubject.getDescription();
                  if (StringUtils.isBlank(loggedInSubjectDescription)) {
                    loggedInSubjectDescription = loggedInSubject.getName();
                  }
                  emailBody.insert(0,"Hey,\n\nThe Grouper external subject invite screen was used by "
                      + GrouperUtil.subjectToString(loggedInSubject) + " - " + loggedInSubjectDescription + "\n\n");
                  emailBody.append("\nRegards.");
                  new GrouperEmail().setBody(emailBody.toString())
                    .setSubject("Grouper external person invitation").setTo(emailAddressesForAdmins).send();
                }
              }
              
              guiResponseJs.addAction(GuiScreenAction.newAlert(messagesToScreen.toString()));

              guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
                "/WEB-INF/grouperUi/templates/inviteExternalSubjects/inviteExternalSubjects.jsp"));
        
            } finally {
              GrouperSession.stopQuietly(grouperSession); 
            }
            return null;
          }
        });
  }
}
