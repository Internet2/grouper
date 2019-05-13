/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 *
 */
public class UiV2LocalEntity {

  /**
   * 
   */
  public UiV2LocalEntity() {
  }

  /**
   * new localentity (show create screen)
   * @param request
   * @param response
   */
  public void newLocalEntity(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      //see if there is a stem id for this
      String objectStemId = request.getParameter("objectStemId");
      
      Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
      
      if (!StringUtils.isBlank(objectStemId) && pattern.matcher(objectStemId).matches()) {
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setObjectStemId(objectStemId);
        
      }
      
      UiV2Stem.retrieveStemHelper(request, false, false, false).getStem();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/localEntity/newLocalEntity.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * new local entity submit
   * @param request
   * @param response
   */
  public void newLocalEntitySubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GrouperSession grouperSession = null;

    Group group = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      final boolean editIdChecked = GrouperUtil.booleanValue(request.getParameter("nameDifferentThanId[]"), false);
      final String displayExtension = request.getParameter("displayExtension");
      final String extension = editIdChecked ? request.getParameter("extension") : displayExtension;
      final String description = request.getParameter("description");
      final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
      final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);

      final TypeOfGroup typeOfGroup = TypeOfGroup.entity;
      
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      if (StringUtils.isBlank(parentFolderId)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateRequiredParentStemId")));
        return;
      }
      
      final Stem parentFolder = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();

      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateCantFindParentStemId")));
        return;
        
      }
      
      if (StringUtils.isBlank(displayExtension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupName",
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateErrorDisplayExtensionRequired")));
        return;
        
      }

      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupId",
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateErrorExtensionRequired")));
        return;
        
      }

      if (parentFolder.isRootStem()) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateCantCreateInRoot")));
        return;
        
      }

      final String groupName = parentFolder.getName() + ":" + extension;
      
      //search as an admin to see if the group exists
      group = (Group)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          return GroupFinder.findByName(theGrouperSession, groupName, false);
        }
      });

      if (group != null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            editIdChecked ? "#groupId" : "#groupName",
            TextContainer.retrieveFromRequest().getText().get("groupCreateCantCreateAlreadyExists")));
        return;
      }
      
      try {

        //create the group
        group = new GroupSave(grouperSession).assignName(groupName).assignSaveMode(SaveMode.INSERT)
            .assignDisplayExtension(displayExtension).assignDescription(description).assignTypeOfGroup(typeOfGroup)
            .assignPrivAllAttrRead(attrReadChecked).assignPrivAllView(viewChecked)
            .save();

      } catch (GrouperValidationException gve) {
        UiV2Group.handleGrouperValidationException(guiResponseJs, gve);
        return;

        
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for local entity create: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateInsufficientPrivileges")));
        return;

        
      } catch (Exception sde) {
        
        LOG.warn("Error creating local entity: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
        
        if (GrouperUiUtils.vetoHandle(guiResponseJs, sde)) {
          return;
        }
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));

        return;

      }

      //go to the view group screen
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Subject.viewSubject&sourceId=grouperEntities&subjectId=" + group.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("localEntityCreateSuccess")));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * view local entity privileges
   * @param request
   * @param response
   */
  public void localEntityPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/localEntity/localEntityPrivileges.jsp"));
      filterPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button for privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      filterPrivilegesHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed for privileges, or paging or sorting, or view Group privileges or something
   * @param request
   * @param response
   * @param group 
   */
  private void filterPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //if filtering text in subjects
    String privilegeFilterText = request.getParameter("privilegeFilterText");
    
    String privilegeFieldName = request.getParameter("privilegeField");
    
    Field privilegeField = null;
    if (!StringUtils.isBlank(privilegeFieldName)) {
      privilegeField = FieldFinder.find(privilegeFieldName, true);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
  
    GuiPaging guiPaging = grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addGroupId(group.getId()).assignCheckSecurity(true)
      .assignFieldType(FieldType.ACCESS)
      .assignEnabled(true)
      .assignHasFieldForMember(true)
      .assignHasMembershipTypeForMember(true)
      .assignQueryOptionsForMember(queryOptions)
      .assignSplitScopeForMember(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }
  
    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
    }
  
    if (!StringUtils.isBlank(privilegeFilterText)) {
      membershipFinder.assignScopeForMember(privilegeFilterText);
    }
  
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    //inherit from grouperAll or Groupersystem or privilege inheritance
    MembershipSubjectContainer.considerAccessPrivilegeInheritance(results);
  
    grouperRequestContainer.getGroupContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPrivilegeFilterResultsId", 
        "/WEB-INF/grouperUi2/localEntity/localEntityPrivilegeContents.jsp"));
  
  }

  /**
   * delete group (show confirm screen)
   * @param request
   * @param response
   */
  public void localEntityDelete(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/localEntity/localEntityDelete.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * hit submit on the delete group screen
   * @param request
   * @param response
   */
  public void localEntityDeleteSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
    
      if (group == null) {
        return;
      }
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }
      
      String stemId = group.getParentUuid();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      try {
  
        //delete the group
        group.delete();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for local entity delete: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Subject.viewSubject&sourceId=grouperEntities&subjectId=" + group.getId() + "')"));
    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("localEntityDeleteInsufficientPrivileges")));
        return;
  
      } catch (GroupDeleteException sde) {
        
        LOG.warn("Error deleting local entity: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Subject.viewSubject&sourceId=grouperEntities&subjectId=" + group.getId() + "')"));
    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("localEntityErrorCantDelete")));
  
        return;
  
      }
      
      //go to the view stem screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stemId + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("localEntityDeleteSuccess")));
      
      GrouperUserDataApi.recentlyUsedGroupRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * edit a local entity, show the edit screen
   * @param request
   * @param response
   */
  public void localEntityEdit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().setShowBreadcrumbLinkSeparator(false);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/localEntity/localEntityEdit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit local entity submit
   * @param request
   * @param response
   */
  public void localEntityEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      final GrouperSession GROUPER_SESSION = grouperSession;
      
      final String extension = request.getParameter("extension");
      final String displayExtension = request.getParameter("displayExtension");
      final String description = request.getParameter("description");
      final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
      final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);
      final boolean cannotAddSelf = GrouperUtil.booleanValue(request.getParameter("groupCreateCannotAddSelfName"), false);
      
      group.setTypeOfGroup(TypeOfGroup.entity);
      
      if (StringUtils.isBlank(displayExtension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupName",
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateErrorExtensionRequired")));
        return;
        
      }
  
      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupId",
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateErrorExtensionRequired")));
        return;
        
      }
  
      try {
  
        //create the group
        GroupSave groupSave = new GroupSave(GROUPER_SESSION).assignUuid(group.getId())
            .assignSaveMode(SaveMode.UPDATE)
            .assignName(group.getParentStemName() + ":" + extension)
            .assignDisplayExtension(displayExtension).assignDescription(description).assignTypeOfGroup(TypeOfGroup.entity)
            .assignPrivAllAttrRead(attrReadChecked)
            .assignPrivAllView(viewChecked);
        group = groupSave.save();
  
        boolean madeChange = groupSave.getSaveResultType() != SaveResultType.NO_CHANGE;
        
        GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
        if (groupContainer.isCannotAddSelfUserCanEdit()) {
          if (cannotAddSelf && !groupContainer.isCannotAddSelfAssignedToGroup()) {
            MembershipCannotAddSelfToGroupHook.cannotAddSelfAssign(group);
            madeChange = true;
          } else if (!cannotAddSelf && groupContainer.isCannotAddSelfAssignedToGroup()) {
            MembershipCannotAddSelfToGroupHook.cannotAddSelfRevoke(group);
            madeChange = true;
          }
        }
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Subject.viewSubject&sourceId=grouperEntities&subjectId=" + group.getId() + "')"));
    
        //lets show a success message on the new screen
        if (!madeChange) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("localEntityEditNoChangeNote")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("localEntityEditSuccess")));
        }
      
  
      } catch (GrouperValidationException gve) {
        UiV2Group.handleGrouperValidationException(guiResponseJs, gve);
        return;
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group edit: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("localEntityCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error edit local entity: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
  
        if (GrouperUiUtils.vetoHandle(guiResponseJs, sde)) {
          return;
        }
  
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("localEntityEditTitle") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));
  
        return;
  
      }
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * submit button on add member form pressed
   * @param request
   * @param response
   */
  public void addMemberSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (group == null) {
        return;
      }
    
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      String subjectString = request.getParameter("entityAddMemberComboName");
  
      subject = null;
      
      if (subjectString != null && subjectString.contains("||")) {
        String sourceId = GrouperUtil.prefixOrSuffix(subjectString, "||", true);
        String subjectId = GrouperUtil.prefixOrSuffix(subjectString, "||", false);
        subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
  
      } else {
        try {
          subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
        } catch (SubjectNotUniqueException snue) {
          //ignore
        }
          
      }
  
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberCantFindSubject")));
        return;
      }      
  
      Boolean defaultPrivs = null;
      
      {
        String privilegeOptionsValue = request.getParameter("privilege-options[]");
        
        if (StringUtils.equals(privilegeOptionsValue, "default")) {
          defaultPrivs = true;
        } else if (StringUtils.equals(privilegeOptionsValue, "custom")) {
          defaultPrivs = false;
        } else {
          throw new RuntimeException("For privilege-options expecting default or custom but was: '" + privilegeOptionsValue + "'");
        }
      }
      
      boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_admins[]"), false);
      boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
      boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);
      boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrUpdaters[]"), false);
      
      if (!defaultPrivs && !adminChecked && !viewChecked
          && !attrReadChecked && !attrUpdateChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#groupPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberPrivRequired")));
        return;
        
      }
  
      boolean madeChanges = group.addOrEditMember(subject, defaultPrivs, false, adminChecked, 
          false, false, viewChecked, false, false, attrReadChecked, 
          attrUpdateChecked, null, null, false);
      
      if (madeChanges) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("localEntityAddMemberMadeChangesSuccess")));
  
        //what subscreen are we on?
        String groupRefreshPart = request.getParameter("groupRefreshPart");
        if (StringUtils.equals(groupRefreshPart, "privileges")) {
          filterPrivilegesHelper(request, response, group);
        }
  
      } else {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("localEntityAddMemberNoChangesSuccess")));
  
      }
  
      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * assign or remove a privilege from a user, and redraw the filter screen... put a success at top
   * @param request
   * @param response
   */
  public void assignPrivilege(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group entity = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (entity == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String memberId = request.getParameter("memberId");
  
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      
      Privilege privilege = AccessPrivilege.listToPriv(fieldName);
      
      if (privilege == null || !Privilege.isEntity(privilege)) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        entity.grantPriv(member.getSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        entity.revokePriv(member.getSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      //reset the data (not really necessary, just in case)
      groupContainer.setPrivilegeGuiMembershipSubjectContainers(null);
      
      filterPrivilegesHelper(request, response, entity);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, entity);
  
      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, member);
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, re)) {
        return;
      }
      throw re;
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * submit the main form on the privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void assignPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group entity = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (entity == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      if (subject == null) {
        return;
      }

      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      //UiV2Group.assignPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}
      
      String groupPrivilegeBatchUpdateOperation = request.getParameter("groupPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(groupPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + groupPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      boolean readersUpdaters = StringUtils.equals(fieldName, "readersUpdaters");
      
      //lets see how many are on a page
      int pageSize = GrouperPagingTag2.pageSize(request);
      
      //lets loop and get all the checkboxes
      Set<Member> members = new LinkedHashSet<Member>();
      
      //loop through all the checkboxes and collect all the members
      for (int i=0;i<pageSize;i++) {
        String memberId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
          members.add(member);
        }
      }
  
      if (GrouperUtil.length(members) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemErrorEntityRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        return;
      }
      
      int changes = 0;
  
      Privilege[] privileges = null;
      if (assignAll) {
        if (assign) {
          privileges = new Privilege[]{AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS)};
        } else {
          privileges = new Privilege[]{
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_READERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_UPDATERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_VIEWERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS)
          };
        }
      } else {
        if (readersUpdaters) {
          throw new RuntimeException("Why readers updaters????");
        }
        privileges = new Privilege[]{AccessPrivilege.listToPriv(fieldName)};
      }
      
      int count = 0;
      for (Member member : members) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += entity.grantPriv(member.getSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += entity.revokePriv(member.getSubject(), privilege, false) ? 1 : 0;
          }
        }
        
        if (count++ < 5) {
          GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
              loggedInSubject, member);
  
        }
        
      }
      
      //reset the data (not really necessary, just in case)
      groupContainer.setPrivilegeGuiMembershipSubjectContainers(null);
  
      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupSuccessGrantedPrivileges" : "groupSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupNoteNoGrantedPrivileges" : "groupNoteNoRevokedPrivileges")));
        
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
  
      // TODO 20180101 MCH: I think this should be "canAdmin" not "hasAdmin"
      if (entity.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false)) {
        filterPrivilegesHelper(request, response, entity);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Main.indexMain')"));
      }
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, entity);
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2LocalEntity.class);

}
