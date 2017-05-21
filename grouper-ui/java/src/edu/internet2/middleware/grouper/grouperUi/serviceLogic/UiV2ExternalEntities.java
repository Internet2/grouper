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

import java.sql.Timestamp;
import java.util.HashSet;
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
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttrFramework;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectInviteBean;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.InviteExternalContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * operations in the group screen
 * @author mchyzer
 *
 */
public class UiV2ExternalEntities {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    new GrouperEmail().setBody("This is an email")
      .setSubject("Grouper external person invitation").setTo("mchyzer@yahoo.com").send();
  }
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2ExternalEntities.class);

  
  /**
   * submit an invite
   * @param request
   * @param response
   */
  public void inviteSubmit(final HttpServletRequest request, final HttpServletResponse response) {


    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();


    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession grouperSession = null;

            try {
              grouperSession = GrouperSession.start(loggedInSubject);

              InviteExternalContainer inviteExternalContainer = grouperRequestContainer.getInviteExternalContainer();

              //marshal data
              //lets normalize the email addresses
              final String emailAddressesToInvite = GrouperUtil.normalizeEmailAddresses(
                  StringUtils.trimToNull(request.getParameter("emailAddressesToInvite")));

              final String emailSubject = StringUtils.trimToNull(request.getParameter("emailSubject"));

              final String messageToUsers = StringUtils.trimToNull(request.getParameter("messageToUsers"));

              final String ccEmailAddress = GrouperUtil.normalizeEmailAddresses(
                  StringUtils.trimToNull(request.getParameter("ccEmailAddress")));

              LinkedHashSet<Group> allGroups = new LinkedHashSet<Group>();

              boolean success = inviteSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, allGroups, false, new HashSet<Group>());

              if (!success) {
                return null;
              }

              // email or id
              String inviteBy = StringUtils.trimToNull(request.getParameter("inviteBy"));

              final String loginIdsToInvite = GrouperUtil.normalizeEmailAddresses(
                  StringUtils.trimToNull(request.getParameter("loginIdsToInvite")));

              boolean inviteByEmailBoolean = StringUtils.isBlank(inviteBy) || StringUtils.equals(inviteBy, "email");

              if (!inviteByEmailBoolean && !StringUtils.equals(inviteBy, "id")) {
                throw new RuntimeException("Why is inviteBy something other than id or email? '" + inviteBy + "'");
              }

              if (!inviteByEmailBoolean && !StringUtils.equals(inviteBy, "identifier")) {
                throw new RuntimeException("Invalid inviteBy: " + inviteBy);
              }

              if (!inviteByEmailBoolean && !inviteExternalContainer.isAllowInviteByIdentifier()) {
                throw new RuntimeException("Not allowed to invite by id");
              }

              if (inviteByEmailBoolean && StringUtils.isBlank(emailAddressesToInvite)) {

                guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
                    "#external-invite-emails",
                    TextContainer.retrieveFromRequest().getText().get("inviteExternalEmailAddressesRequired")));

                return null;
              }

              if (inviteByEmailBoolean) {
                for (String emailAddress : GrouperUtil.splitTrim(emailAddressesToInvite, ";")) {

                  if (!GrouperUtil.validEmail(emailAddress)) {

                    grouperRequestContainer.getCommonRequestContainer().setEmailAddress(emailAddress);

                    guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
                        "#external-invite-emails",
                        TextContainer.retrieveFromRequest().getText().get("inviteExternalInvalidEmailAddress")));

                    return null;
                  }
                }
                if (!StringUtils.isBlank(ccEmailAddress)) {
                  for (String emailAddress : GrouperUtil.splitTrim(ccEmailAddress, ";")) {

                    if (!GrouperUtil.validEmail(emailAddress)) {

                      grouperRequestContainer.getCommonRequestContainer().setEmailAddress(emailAddress);

                      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
                          "#external-invite-notify",
                          TextContainer.retrieveFromRequest().getText().get("inviteExternalInvalidEmailAddress")));

                      return null;

                    }
                  }
                }
              }      

              Set<String> groupsToAssignFinal = new HashSet<String>();
              String groupNamesToAssign = allGroups.size() == 0 ? null : GrouperUtil.join(allGroups.iterator(), ", ");
              String groupIdsToAssign = allGroups.size() == 0 ? null : GrouperUtil.join(allGroups.iterator(), ",");

              for (Group group : allGroups) {
                groupsToAssignFinal.add(group.getId());
              }

              Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
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
                  grouperRequestContainer.getCommonRequestContainer().setError(error);

                  guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                      TextContainer.retrieveFromRequest().getText().get("inviteExternalApiErrorInvitingUsers")));

                  return null;
                }
              } else {

                //invite by identifier
                Set<Subject> subjectsToInvite = new HashSet<Subject>();

                //these have already been normalized by semicolon
                for (String loginId : GrouperUtil.splitTrim(loginIdsToInvite, ";")) {

                  inviteExternalContainer.setExternalId(loginId);

                  //lets see if this is already a subject
                  //is it an externalSubject?
                  Subject subjectToAssign = SubjectFinder.findByIdOrIdentifierAndSource(loginId, ExternalSubject.sourceId(), false);
                  if (subjectToAssign == null) {
                    try {
                      subjectToAssign = SubjectFinder.findByIdOrIdentifier(loginId, false);
                    } catch (SubjectNotUniqueException snue) {
                      //ignore
                    }
                  }
                  if (subjectToAssign == null) {
                    //if it is still null, then it doesnt exist... lets validate it
                    final ExternalSubject externalSubject = new ExternalSubject();
                    externalSubject.setIdentifier(loginId);
                    try {
                      externalSubject.validateIdentifier();
                    } catch (Exception e) {

                      LOG.warn("Invalid identifier: " + loginId, e);

                      String message = TextContainer.retrieveFromRequest().getText().get("inviteExternalInvalidIdentifierInvite");

                      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message));

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
                        externalSubject.store(null, null, false, true, false);
                        return null;
                      }
                    });

                    String message = TextContainer.retrieveFromRequest().getText().get("inviteExternalSuccessCreatedExternalSubject");

                    if (messagesToScreen.length() > 0) {
                      messagesToScreen.append("<br /><br />");
                    }
                    messagesToScreen.append(message);

                    subjectToAssign = SubjectFinder.findByIdAndSource(externalSubject.getUuid(), ExternalSubject.sourceId(), true);

                  } else {
                    String message = TextContainer.retrieveFromRequest().getText().get("inviteExternalSubjectsSuccessExistedExternalSubject");

                    if (messagesToScreen.length() > 0) {
                      messagesToScreen.append("<br /><br />");
                    }
                    messagesToScreen.append(message);

                  }

                  //now we have the subject
                  subjectsToStringToInvite.append(GrouperUtil.subjectToString(subjectToAssign));

                  subjectsToInvite.add(subjectToAssign);

                  //lets assign to groups
                  for (Group group : allGroups) {

                    grouperRequestContainer.getCommonRequestContainer().setGuiGroup(new GuiGroup(group));

                    try {
                      boolean added = group.addMember(subjectToAssign, false);

                      String navKey = null;

                      if (added) {
                        //inviteExternalSubjects.successAssignedSubjectToGroup=Success: entity {0} was assigned to group {1}
                        navKey = "inviteExternalSubjectsSuccessAssignedSubjectToGroup";
                      } else {
                        //inviteExternalSubjects.noteSubjectAlreadyInGroup=Note: entity {0} was already a member of group {1}
                        navKey = "inviteExternalSubjectsNoteSubjectAlreadyInGroup";
                      }
                      String message = TextContainer.retrieveFromRequest().getText().get(navKey);

                      if (messagesToScreen.length() > 0) {
                        messagesToScreen.append("<br /><br />");
                      }
                      messagesToScreen.append(message);

                    } catch (Exception e) {
                      LOG.error("Cant add subject to group: " + loginId + ", " + GrouperUtil.subjectToString(subjectToAssign) + ", " + group.getName(), e);

                      // Error: could not assign entity: {0} to group: {1}. {2}
                      String message = TextContainer.retrieveFromRequest().getText().get("inviteExternalErrorAssigningGroup");

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
                messagesToScreen.append(TextContainer.retrieveFromRequest().getText().get("inviteExternalSubjects.successInvitingUsers"));
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
                String emailAddressesForAdmins = GrouperUiConfig.retrieveConfig().propertyValueString("inviteExternalMembers.emailAdminsAddressesAfterActions");
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

              //go to the view group screen
              Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, false).getGroup();

              guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));

              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, messagesToScreen.toString()));

            } finally {
              GrouperSession.stopQuietly(grouperSession); 
            }
            return null;
          }
        });

  }
                  


            
  
  /**
   * modal search form results for add a group to the invite list
   * @param request
   * @param response
   */
  public void inviteSearchGroupFormSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      InviteExternalContainer inviteExternalContainer = grouperRequestContainer.getInviteExternalContainer();
  
      String searchString = request.getParameter("groupFilter");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#groupSearchResultsId", 
            TextContainer.retrieveFromRequest().getText().get("inviteExternalAddGroupNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);

      GuiPaging guiPaging = inviteExternalContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignScope(searchString).assignCompositeOwner(false)
        .assignSplitScope(true).assignQueryOptions(queryOptions);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(groups) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#groupSearchResultsId", 
            TextContainer.retrieveFromRequest().getText().get("inviteExternalSearchNoGroupsFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      
      inviteExternalContainer.setGuiGroupsSearch(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupSearchResultsId", 
          "/WEB-INF/grouperUi2/externalEntities/inviteAddGroupResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * combobox results for search for group for external entities
   * @param request
   * @param response
   */
  public void addGroupFilter(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().groupUpdateFilter(request, response);
  }

  
  /**
   * show the group invite screen
   * @param request
   * @param response
   */
  public void inviteExternal(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("inviteExternalMembers.enableInvitation", false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("inviteExternalNotEnabled")));
        return;
      }
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup();

      //must have a group
      if (group == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("inviteExternalCantFindGroup")));
        return;
      }
      
      if (group != null) {
        GuiGroup guiGroup = new GuiGroup(group);
        if (!guiGroup.isCanInviteExternalUsers()) {
          
          GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setGuiGroup(null);
          
          //thats not good, cant invite externals on this group
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("inviteExternalCantFindGroup")));
          
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/externalEntities/invite.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * invite members screen add group to list
   * @param request
   * @param response
   */
  public void inviteAddGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      LinkedHashSet<Group> allGroups = new LinkedHashSet<Group>();
      HashSet<Group> nonComboGroups = new HashSet<Group>();
      inviteSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, allGroups, true, nonComboGroups);

      if (allGroups.size() >= MAX_GROUPS_TO_INVITE-1) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$('#addAnotherGroupButtonId').hide('slow');"));
      }
      
      //clear out combobox
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('inviteAddGroupComboId').set('displayedValue', ''); " +
          "dijit.byId('inviteAddGroupComboId').set('value', '');"));
  
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#inviteExtraGroupsDivId", 
          "/WEB-INF/grouperUi2/externalEntities/inviteExtraGroups.jsp"));
  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * invite group members screen remove group from list
   * @param request
   * @param response
   */
  public void inviteRemoveGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      LinkedHashSet<Group> allGroups = new LinkedHashSet<Group>();
      
      HashSet<Group> nonComboGroups = new HashSet<Group>();

      inviteSetupExtraGroups(loggedInSubject, request, guiResponseJs, true, false, allGroups, false, nonComboGroups);

      if (nonComboGroups.size() <= 3) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$('#addAnotherGroupButtonId').show('slow');"));
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#inviteExtraGroupsDivId", 
          "/WEB-INF/grouperUi2/externalEntities/inviteExtraGroups.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * setup the extra groups (other than combobox), and maybe move the combobox down
   * @param loggedInSubject
   * @param request
   * @param guiResponseJs 
   * @param considerRemoveGroupId 
   * @param includeCombobox
   * @param allGroups 
   * @param errorOnNullCombobox true if an error should appear if there is nothing in the combobox
   * @param nonComboGroups 
   * @return true if ok, false if not
   */
  private boolean inviteSetupExtraGroups(Subject loggedInSubject, 
      HttpServletRequest request, GuiResponseJs guiResponseJs, boolean considerRemoveGroupId, boolean includeCombobox,
      Set<Group> allGroups, boolean errorOnNullCombobox, Set<Group> nonComboGroups) {
  
    Set<GuiGroup> extraGuiGroups = new LinkedHashSet<GuiGroup>();
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getInviteExternalContainer().setInviteExtraGuiGroups(extraGuiGroups);
    
    String removeGroupId = null;
  
    //if removing a group id
    if (considerRemoveGroupId) {
      removeGroupId = request.getParameter("removeGroupId");
      if (StringUtils.isBlank(removeGroupId)) {
        throw new RuntimeException("Why would removeGroupId be empty????");
      }
    }
  
    //if moving combobox down to extra list or getting all groups
    String comboValue = request.getParameter("inviteAddGroupComboName");
    
    if (StringUtils.isBlank(comboValue)) {
      //if didnt pick one from results
      comboValue = request.getParameter("inviteAddGroupComboNameDisplay");
    }
    
    Group theGroup = StringUtils.isBlank(comboValue) ? null : new GroupFinder()
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignSubject(loggedInSubject)
        .assignFindByUuidOrName(true).assignScope(comboValue).findGroup();
  
    boolean success = true;
    
    if (theGroup == null) {
      if (includeCombobox && errorOnNullCombobox) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#inviteAddGroupComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupImportGroupNotFound")));
        success = false;
      }
      
    } else {
      
      GuiGroup guiGroup = new GuiGroup(theGroup);

      //some groups can or cannot have external users
      if (!guiGroup.isCanInviteExternalUsers()) {

        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportGroupComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("inviteExternalErrorGroupCannotHaveExternalUsers")));
        success = false;
        
      } else {
      
        if (includeCombobox) {
  
          extraGuiGroups.add(guiGroup);
        }
        //always add to all groups
        allGroups.add(theGroup);
      }
    }
  
    //loop through all the hidden fields (max n-1)
    for (int i=0;i<MAX_GROUPS_TO_INVITE-1;i++) {
      String extraGroupId = request.getParameter("extraGroupId_" + i);
      
      //we are at the end
      if (StringUtils.isBlank(extraGroupId)) {
        break;
      }
      
      //might be removing this one
      if (considerRemoveGroupId && StringUtils.equals(removeGroupId, extraGroupId)) {
        continue;
      }
      
      theGroup = new GroupFinder()
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignSubject(loggedInSubject)
        .assignFindByUuidOrName(true).assignScope(extraGroupId).findGroup();

      if (theGroup != null) {
        GuiGroup guiGroup = new GuiGroup(theGroup);
        
        if (guiGroup.isCanInviteExternalUsers()) {
          
          extraGuiGroups.add(guiGroup);
     
          //always add to all groups
          allGroups.add(theGroup);

          //nonComboGroups to see if max size is reached
          nonComboGroups.add(theGroup);
        }
        
      }
  
    }
    
    return success;
  }

  /** max groups to invite */
  private static final int MAX_GROUPS_TO_INVITE = 10;
}
