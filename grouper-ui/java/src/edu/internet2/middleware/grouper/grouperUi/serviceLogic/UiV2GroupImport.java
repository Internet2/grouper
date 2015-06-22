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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.ImportSubjectWrapper;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupImportContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SimpleMembershipUpdateImportExport.GrouperImportException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.j2ee.GrouperUiRestServlet;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * operations in the group screen
 * @author mchyzer
 *
 */
public class UiV2GroupImport {

  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2GroupImport.class);

  /**
   * validate import list
   * @param request
   * @param response
   */
  public void groupImportValidateList(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String entityList = StringUtils.defaultString(request.getParameter("entityList"));
      
      //split trim by comma, semi, or whitespace
      entityList = StringUtils.replace(entityList, ",", " ");
      entityList = StringUtils.replace(entityList, ";", " ");
      
      String[] entityIdOrIdentifiers = GrouperUtil.splitTrim(entityList, null, true);

      if (GrouperUtil.length(entityIdOrIdentifiers) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#entityListId",
            TextContainer.retrieveFromRequest().getText().get("groupImportNoEntitiesSpecified")));
        return;

      }
      
      if (GrouperUtil.length(entityIdOrIdentifiers) > 100) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupImportTooManyEntitiesToValidate")));
        return;

      }
      
      //extra source ids and subjects ids
      Set<GuiSubject> extraGuiSubjects = new LinkedHashSet<GuiSubject>();
      
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer().setGroupImportExtraGuiSubjects(extraGuiSubjects);

      String source = request.getParameter("searchEntitySourceName");
      
      List<String> entityIdOrIdentifiersList = new ArrayList<String>(Arrays.asList(GrouperUtil.nonNull(
          entityIdOrIdentifiers, String.class)));
      
      Map<String, Subject> entityIdOrIdentifierMap = null;
      
      if (StringUtils.equals("all", source)) {

        entityIdOrIdentifierMap = SubjectFinder.findByIdsOrIdentifiers(entityIdOrIdentifiersList);
        
      } else {

        entityIdOrIdentifierMap = SubjectFinder.findByIdsOrIdentifiers(entityIdOrIdentifiersList, source);

      }
      
      //lets add all the subjects
      for (Subject subject : GrouperUtil.nonNull(entityIdOrIdentifierMap).values()) {
        extraGuiSubjects.add(new GuiSubject(subject));
      }

      //lets see which are missing
      entityIdOrIdentifiersList.removeAll(GrouperUtil.nonNull(entityIdOrIdentifierMap).keySet());
      
      if (entityIdOrIdentifiersList.size() > 0) {
        GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer().setEntityIdsNotFound(GrouperUtil.join(entityIdOrIdentifiersList.iterator(), ", "));
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupImportEntityIdsNotFound")));

      }

      //clear out combobox
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));

      //select the option for enter in list
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("bulkAddOptions", "input"));

      //fill in the extra subjects
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraMembersDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportExtraSubjects.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }


  }
  

  /**
   * export a group
   * @param request
   * @param response
   */
  public void groupExportSubmit(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      List<String> urlStrings = GrouperUiRestServlet.extractUrlStrings(request);
      
      //groupId=721e4e8ae6e54c4087db092f0a6372f7
      String groupIdString = urlStrings.get(2);
      
      String groupId = GrouperUtil.prefixOrSuffix(groupIdString, "=", false);
      
      group = GroupFinder.findByUuid(grouperSession, groupId, false);

      if (group == null) {
        throw new RuntimeException("Cant find group by id: " + groupId);
      }
      
      GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
      GroupImportContainer groupImportContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer();
      
      groupContainer.setGuiGroup(new GuiGroup(group));
      
      if (!groupContainer.isCanRead()) {
        throw new RuntimeException("Cant read group: " + group.getName());
      }
      
      //ids
      String groupExportOptions = urlStrings.get(3);
      
      boolean exportAll = false;
      if (StringUtils.equals("all", groupExportOptions)) {
        groupImportContainer.setExportAll(true);
        exportAll = true;
      } else if (StringUtils.equals("ids", groupExportOptions)) {
        groupImportContainer.setExportAll(false);
      } else {
        throw new RuntimeException("Not expecting group-export-options value: '" + groupExportOptions + "'");
      }

      
      //groupExportSubjectIds_removeAllMembers.csv
      @SuppressWarnings("unused")
      String fileName = urlStrings.get(4);
      
      if (exportAll) {
        String headersCommaSeparated = GrouperUiConfig.retrieveConfig().propertyValueString(
            "uiV2.group.exportAllSubjectFields");
        
        String exportAllSortField = GrouperUiConfig.retrieveConfig().propertyValueString(
            "uiV2.group.exportAllSortField");
  
        SimpleMembershipUpdateImportExport.exportGroupAllFieldsToBrowser(group, headersCommaSeparated, exportAllSortField, false);
      } else {
        
        SimpleMembershipUpdateImportExport.exportGroupSubjectIdsCsv(group, false);
        
      }
      
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * export group members screen
   * @param request
   * @param response
   */
  public void groupExport(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupExport.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


  /**
   * export group members screen change the type of export
   * @param request
   * @param response
   */
  public void groupExportTypeChange(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      String groupExportOptions = request.getParameter("group-export-options[]");
      
      GroupImportContainer groupImportContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer();
      
      if (StringUtils.equals("all", groupExportOptions)) {
        groupImportContainer.setExportAll(true);
      } else if (StringUtils.equals("ids", groupExportOptions)) {
        groupImportContainer.setExportAll(false);
      } else {
        throw new RuntimeException("Not expecting group-export-options value: '" + groupExportOptions + "'");
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#formActionsDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupExportButtons.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * setup the extra groups (other than combobox), and maybe move the combobox down
   * @param loggedInSubject
   * @param request
   * @param removeGroupId if removing one
   * @param includeCombobox
   * @param allGroups, pass in a blank linked hash set, and all groups will be populated including combobox
   * @param errorOnNullCombobox true if an error should appear if there is nothing in the combobox
   * @return true if ok, false if not
   */
  private boolean groupImportSetupExtraGroups(Subject loggedInSubject, 
      HttpServletRequest request, GuiResponseJs guiResponseJs, boolean considerRemoveGroupId, boolean includeCombobox,
      Set<Group> allGroups, boolean errorOnNullCombobox) {

    Set<GuiGroup> extraGuiGroups = new LinkedHashSet<GuiGroup>();
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer().setGroupImportExtraGuiGroups(extraGuiGroups);
    
    String removeGroupId = null;

    //if removing a group id
    if (considerRemoveGroupId) {
      removeGroupId = request.getParameter("removeGroupId");
      if (StringUtils.isBlank(removeGroupId)) {
        throw new RuntimeException("Why would removeGroupId be empty????");
      }
    }

    //if moving combobox down to extra list or getting all groups
    String comboValue = request.getParameter("groupImportGroupComboName");
    
    if (StringUtils.isBlank(comboValue)) {
      //if didnt pick one from results
      comboValue = request.getParameter("groupImportGroupComboNameDisplay");
    }
    
    Group theGroup = StringUtils.isBlank(comboValue) ? null : new GroupFinder()
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignSubject(loggedInSubject)
        .assignFindByUuidOrName(true).assignScope(comboValue).findGroup();

    boolean success = true;
    
    if (theGroup == null) {
      if (includeCombobox && errorOnNullCombobox) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportGroupComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupImportGroupNotFound")));
        success = false;
      }
      
    } else {
      if (includeCombobox) {
        extraGuiGroups.add(new GuiGroup(theGroup));
      }
      //always add to all groups
      allGroups.add(theGroup);
    }

    //loop through all the hidden fields (max 100)
    for (int i=0;i<100;i++) {
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
      
      extraGuiGroups.add(new GuiGroup(theGroup));

      //always add to all groups
      allGroups.add(theGroup);
      
    }
    return success;
  }
  
  /**
   * submit a group import
   * @param request
   * @param response
   */
  public void groupImportSubmit(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GroupImportContainer groupImportContainer = grouperRequestContainer.getGroupImportContainer();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      Set<Group> groups = new LinkedHashSet<Group>();
      boolean success = groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, groups, false);
      
      if (!success) {
        //error message already shown
        return;
      }

      if (groups.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportGroupComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupImportGroupNotFound")));
        return;
      }
      
      // can be import, input, list
      String bulkAddOption = request.getParameter("bulkAddOptions");
      Map<String, Integer> listInvalidSubjectIdsAndRow = new LinkedHashMap<String, Integer>();
      
      Set<Subject> subjectSet = new LinkedHashSet<Subject>();
      String fileName = null;
      if (StringUtils.equals(bulkAddOption, "import")) {

        GrouperRequestWrapper grouperRequestWrapper = (GrouperRequestWrapper)request;
        
        FileItem importCsvFile = grouperRequestWrapper.getParameterFileItem("importCsvFile");

        if (importCsvFile == null) {
          
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#importCsvFileId",
              TextContainer.retrieveFromRequest().getText().get("groupImportUploadFile")));
          return;
        }
        
        Reader reader = null;
        reader = new InputStreamReader(importCsvFile.getInputStream());
        
        fileName = StringUtils.defaultString(importCsvFile == null ? "" : importCsvFile.getName());

        try {
          subjectSet.addAll(SimpleMembershipUpdateImportExport.parseCsvImportFile(reader, fileName, new ArrayList<String>(), 
              listInvalidSubjectIdsAndRow, true));
        } catch (GrouperImportException gie) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("error in import", gie);
          }
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#importCsvFileId", GrouperUtil.xmlEscape(gie.getMessage())));
          return;
        }
        
      } else if (StringUtils.equals(bulkAddOption, "input")) {

        //combobox
        success = groupImportSetupExtraSubjects(loggedInSubject, request, guiResponseJs, false, true, subjectSet, false);
        
        if (!success) {
          //error message already shown
          return;
        }
        
        if (subjectSet.size() == 0) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#groupAddMemberComboErrorId", 
              TextContainer.retrieveFromRequest().getText().get("groupImportSubjectNotFound")));
          return;
        }


      } else if (StringUtils.equals(bulkAddOption, "list")) {

        String entityList = StringUtils.defaultString(request.getParameter("entityList"));
        
        //split trim by comma, semi, or whitespace
        entityList = StringUtils.replace(entityList, ",", " ");
        entityList = StringUtils.replace(entityList, ";", " ");
        
        String[] entityIdOrIdentifiers = GrouperUtil.splitTrim(entityList, null, true);

        if (GrouperUtil.length(entityIdOrIdentifiers) == 0) {

          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#entityListId",
              TextContainer.retrieveFromRequest().getText().get("groupImportNoEntitiesSpecified")));
          return;

        }
        
        String source = request.getParameter("searchEntitySourceName");
        
        List<String> entityIdOrIdentifiersList = new ArrayList<String>(Arrays.asList(GrouperUtil.nonNull(
            entityIdOrIdentifiers, String.class)));
        
        Map<String, Subject> entityIdOrIdentifierMap = null;
        
        if (StringUtils.equals("all", source)) {

          entityIdOrIdentifierMap = SubjectFinder.findByIdsOrIdentifiers(entityIdOrIdentifiersList);
          
        } else {

          entityIdOrIdentifierMap = SubjectFinder.findByIdsOrIdentifiers(entityIdOrIdentifiersList, source);

        }
        
        //lets add all the subjects
        subjectSet.addAll(GrouperUtil.nonNull(entityIdOrIdentifierMap).values());

        //lets see which are missing
        List<String> originalIdList = new ArrayList<String>(entityIdOrIdentifiersList);

        //lets see which are missing
        entityIdOrIdentifiersList.removeAll(GrouperUtil.nonNull(entityIdOrIdentifierMap).keySet());

        //keep trac of the index of the invalid ids
        for (String invalidId : entityIdOrIdentifiersList) {
          int index = originalIdList.indexOf(invalidId);
          listInvalidSubjectIdsAndRow.put(invalidId, index == -1 ? null : index);
        }
        
      } else {
        throw new RuntimeException("Not expecting bulk add option: " + bulkAddOption);
      }
      
      {
        Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup();
        if (group != null) {
          groupImportContainer.setImportFromGroup(true);
        }
      }
      {
        Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
        if (subject != null) {
          groupImportContainer.setImportFromSubject(true);
        }
      }

      Set<GuiGroup> guiGroups = new LinkedHashSet<GuiGroup>();
      groupImportContainer.setGuiGroups(guiGroups);
      
      Map<String, String> reportByGroupName = new HashMap<String, String>();
      groupImportContainer.setReportForGroupNameMap(reportByGroupName);

      int totalAdded = 0;
      int totalDeleted = 0;
      
      Iterator<Group> groupIterator = groups.iterator();

      boolean importReplaceMembers = GrouperUtil.booleanValue(request.getParameter("replaceExistingMembers"), false);

      //lets go through the groups that were submitted
      while (groupIterator.hasNext()) {

        final Group group = groupIterator.next();

        {
          //remove groups that cannot be viewed
          boolean canView = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                return group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
              }
            });
  
          if (!canView) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,  
                TextContainer.retrieveFromRequest().getText().get("groupImportGroupCantView")));
            groupIterator.remove();
            continue;
          }
        }

        guiGroups.add(new GuiGroup(group));
        StringBuilder report = new StringBuilder("<ul>\n");

        {
          //give error if cant update
          boolean canUpdate = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                return group.canHavePrivilege(loggedInSubject, AccessPrivilege.UPDATE.getName(), false);
              }
            });

          if (!canUpdate) {
            report.append("<li>" +
              TextContainer.retrieveFromRequest().getText().get("groupImportGroupCantUpdate") + "</li></ul>");
            continue;
          }
        }

        //<ul>
        //  <li>Before importing, the membership count was 10 and is now 12.</li>
        //  <li>You successfully added 2 members and deleted 0 members.</li>
        //  <li>2 members were not imported due to errors, as shown below.</li>
        //</ul>
        //<h5>Errors</h5>
        //<ul>
        //  <li><span class="label label-important">Error</span>&nbsp;on row 2. Subject not found: "foo-bar-user"</li>
        //</ul>

        List<Member> existingMembers = new ArrayList<Member>(GrouperUtil.nonNull(group.getImmediateMembers()));
        List<Subject> subjectList = new ArrayList<Subject>(GrouperUtil.nonNull(subjectSet));
        int existingCount = GrouperUtil.length(existingMembers);
        groupImportContainer.setGroupCountOriginal(existingCount);
        
        GrouperUiUtils.removeOverlappingSubjects(existingMembers, subjectList);

        int addedCount = 0;
        int errorsCount = 0;

        StringBuilder errors = new StringBuilder();

        // figure out subject not founds
        if (listInvalidSubjectIdsAndRow.size() > 0) {
          for (String subjectLabel : listInvalidSubjectIdsAndRow.keySet()) {
            int rowNumber = listInvalidSubjectIdsAndRow.get(subjectLabel);
            String errorLine = errorLine(subjectLabel, TextContainer.retrieveFromRequest().getText().get(
                "groupImportProblemFindingSubjectError"), rowNumber);
            errors.append(errorLine).append("\n");
            errorsCount++;
          }
        }
        
        //first lets add some members
        for (int i=0;i<subjectList.size();i++) {
          
          Subject subject = subjectList.get(i);
          
          if (subject instanceof ImportSubjectWrapper) {
            try {
              subject = ((ImportSubjectWrapper)subject).wrappedSubject();
            } catch (Exception e) {
              //ignore
            }
          }
          
          try {
              
            group.addMember(subject, false);
            
            addedCount++;
          } catch (Exception e) {
            
            String errorLine = errorLine(subject, GrouperUtil.xmlEscape(e.getMessage()));
            errors.append(errorLine).append("\n");
            errorsCount++;
            LOG.warn(errorLine, e);
          }
    
        }
    
        boolean didntImportDueToSubjects = errorsCount > 0;
        int deletedCount = 0;
    
        //remove the ones which are already there
        if (importReplaceMembers && !didntImportDueToSubjects) {
          
          for (Member existingMember : existingMembers) {
            
            try {
              group.deleteMember(existingMember, false);
              deletedCount++;
            } catch (Exception e) {
              String errorLine = errorLine(existingMember.getSubject(), GrouperUtil.xmlEscape(e.getMessage()));
              errors.append(errorLine).append("\n");
              errorsCount++;
              LOG.warn(errorLine, e);
            
            }
          }
        }

        if (importReplaceMembers && didntImportDueToSubjects) {
          report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportNoReplaceError")).append("\n");
        }
        
        //this might be a little wasteful, but I think it is a good sanity check
        int newSize = group.getImmediateMembers().size();

        // = Errors
        //groupImportReportErrorLine = <li><span class="label label-important">Error</span>&nbsp;on row ${grouperRequestContainer.groupImportContainer.errorRowNumber}. ${grouperRequestContainer.groupImportContainer.errorText}: "${grouperUtil.xmlEscape(grouperRequestContainer.groupImportContainer.errorSubject)}"</li>

        //set stuff for text to use
        groupImportContainer.setGroupCountAdded(addedCount);
        groupImportContainer.setGroupCountDeleted(deletedCount);
        groupImportContainer.setGroupCountErrors(errorsCount);
        groupImportContainer.setGroupCountOriginal(existingCount);
        groupImportContainer.setGroupCountNew(newSize);
        
        totalAdded += addedCount;
        totalDeleted += deletedCount;
        
        report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportSummary")).append("\n");
        report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportSuccess")).append("\n");
        
        // dont add the error report line if there are no errors 
        if (errorsCount > 0) {
          report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorSummary")).append("\n");
        }
        report.append("</ul>\n");
        
        //only add the errors section if there are errors
        if (errorsCount > 0) {
          report.append("<h5>").append(TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorsTitle")).append("</h5>\n");
          report.append("<ul>\n");
          report.append(errors.toString());
          report.append("</ul>\n");
        }
        
        reportByGroupName.put(group.getName(), report.toString());

        GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            loggedInSubject, group);

      }
      
      if (StringUtils.equals(bulkAddOption, "import")) {
    	  auditImport(subjectSet.size(), fileName, totalAdded, totalDeleted);
      }
      
      //show the report screen
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportReport.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));


    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
    private void auditImport(final int totalSubjects, final String fileName,
			final int totalAdded, final int totalDeleted) {
      HibernateSession.callbackHibernateSession(
		    GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
			    new HibernateHandler() {
				    public Object callback(HibernateHandlerBean hibernateHandlerBean)
					    throws GrouperDAOException {
						  
						AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.IMPORT, "file", fileName, "totalAdded", 
						    String.valueOf(totalAdded), "totalDeleted", String.valueOf(totalDeleted));
						  
						String description = "Found : " + totalSubjects + " subjects in : " + fileName 
								  + " file. \n Added  "+totalAdded+" and deleted "+totalDeleted + " subjects.";
						auditEntry.setDescription(description);
						auditEntry.saveOrUpdate(true);
						  
						return null;
					  }
      });
    }
  
  
  /**
   * get an error line
   * @param subject
   * @param errorEscaped
   * @return the line
   */
  private static String errorLine(Subject subject, String errorEscaped) {

    String subjectLabel = null;
    Integer rowNumber = null;
    if (subject instanceof ImportSubjectWrapper) {
      subjectLabel = ((ImportSubjectWrapper)subject).getSubjectIdOrIdentifier();
      rowNumber = ((ImportSubjectWrapper)subject).getRow();
    } else {
      subjectLabel = subject.getId();
    }
    return errorLine(subjectLabel, errorEscaped, rowNumber);
  }
  
  /**
   * get an error line
   * @param subject
   * @param errorEscaped
   * @param rowNumber
   * @return the line
   */
  private static String errorLine(String subjectLabel, String errorEscaped, Integer rowNumber) {

    GroupImportContainer groupImportContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer();
    groupImportContainer.setErrorText(errorEscaped);

    groupImportContainer.setErrorSubject(subjectLabel);

    if (rowNumber != null) {
      groupImportContainer.setErrorRowNumber(rowNumber);
      return TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorLine");
    }
    
    return TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorLineNoRow");
    
  }
  
  /**
   * modal search form results for add group to import
   * @param request
   * @param response
   */
  public void groupImportGroupSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      String searchString = request.getParameter("addGroupSearch");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupImportAddToGroupNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);

      GuiPaging guiPaging = groupContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignScope(searchString).assignSplitScope(true).assignQueryOptions(queryOptions);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(groups) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupImportAddGroupNotFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      
      groupContainer.setGuiGroups(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addGroupResults", 
          "/WEB-INF/grouperUi2/groupImport/groupImportAddGroupResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  /**
   * import group members screen remove group from list
   * @param request
   * @param response
   */
  public void groupImportRemoveGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, true, false, new LinkedHashSet<Group>(), false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraGroupsDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportExtraGroups.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * import group members screen remove subject from list
   * @param request
   * @param response
   */
  public void groupImportRemoveSubject(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      groupImportSetupExtraSubjects(loggedInSubject, request, guiResponseJs, true, false, new LinkedHashSet<Subject>(), false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraMembersDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportExtraSubjects.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * import group members screen add group to list
   * @param request
   * @param response
   */
  public void groupImportAddGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, new LinkedHashSet<Group>(), true);

      //clear out combobox
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupImportGroupComboId').set('displayedValue', ''); " +
          "dijit.byId('groupImportGroupComboId').set('value', '');"));

      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraGroupsDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportExtraGroups.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * import group members screen
   * @param request
   * @param response
   */
  public void groupImport(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperRequestContainer grouperRequestContainer = new GrouperRequestContainer();
      GroupImportContainer groupImportContainer = grouperRequestContainer.getGroupImportContainer();
      
      String backTo = request.getParameter("backTo");
      
      {
        //this will also put the group in the group container so it can populate the combobox
        Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup();
        if (group != null && StringUtils.equals("group", backTo)) {
          groupImportContainer.setImportFromGroup(true);
        }
      }
      {
        Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
        if (subject != null && StringUtils.equals("subject", backTo)) {
          groupImportContainer.setImportFromSubject(true);
        }
      }

      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImport.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * import group members screen add member to list
   * @param request
   * @param response
   */
  public void groupImportAddMember(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      groupImportSetupExtraSubjects(loggedInSubject, request, guiResponseJs, false, true, new LinkedHashSet<Subject>(), true);
  
      //clear out combobox
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));
  
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraMembersDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportExtraSubjects.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * setup the extra members (other than combobox), and maybe move the combobox down
   * @param loggedInSubject
   * @param request
   * @param considerRemoveSubjectSourceAndId if removing one
   * @param includeCombobox
   * @param allSubjects is a LinkedHashSet of subjects
   * @param errorOnNullCombobox is true if an error should appear if there is nothing in the combobox
   * @return true if ok, false if not
   */
  private boolean groupImportSetupExtraSubjects(Subject loggedInSubject, 
      HttpServletRequest request, GuiResponseJs guiResponseJs, boolean considerRemoveSubjectSourceAndId, 
      boolean includeCombobox, Set<Subject> allSubjects, boolean errorOnNullCombobox) {

    //extra source ids and subjects ids
    Set<MultiKey> extraSubjectSourceAndIds = new HashSet<MultiKey>();
    Set<GuiSubject> extraGuiSubjects = new LinkedHashSet<GuiSubject>();
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupImportContainer().setGroupImportExtraGuiSubjects(extraGuiSubjects);
    
    Set<MultiKey> allSubjectsSourceAndIds = new HashSet<MultiKey>();
  
    String removeSubjectSourceAndId = null;
  
    //if removing a group id
    if (considerRemoveSubjectSourceAndId) {
      removeSubjectSourceAndId = request.getParameter("removeSubjectSourceAndId");
      if (StringUtils.isBlank(removeSubjectSourceAndId)) {
        throw new RuntimeException("Why would removeSubjectSourceAndId be empty????");
      }
    }

    Subject theSubject = null;
    
    {
      //if moving combobox down to extra list or getting all groups
      String comboValue = request.getParameter("groupAddMemberComboName");
      
      if (StringUtils.isBlank(comboValue)) {
        //if didnt pick one from results
        comboValue = request.getParameter("groupAddMemberComboDisplay");
      }
      
      try {
        GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
        if (comboValue != null && comboValue.contains("||")) {
          String sourceId = GrouperUtil.prefixOrSuffix(comboValue, "||", true);
          String subjectId = GrouperUtil.prefixOrSuffix(comboValue, "||", false);
          theSubject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
        } else {
          theSubject = StringUtils.isBlank(comboValue) ? null : SubjectFinder.findByIdOrIdentifier(comboValue, false);
        }
      } finally {
        GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
      }
    }
    boolean success = true;
    if (theSubject == null) {
      if (includeCombobox && errorOnNullCombobox) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupAddMemberComboErrorId", 
            TextContainer.retrieveFromRequest().getText().get("groupImportSubjectNotFound")));
        success = false;
      }
      
    } else {
      MultiKey multiKey = new MultiKey(theSubject.getSourceId(), theSubject.getId());
      if (includeCombobox) {
        if (!extraSubjectSourceAndIds.contains(multiKey)) {
          extraGuiSubjects.add(new GuiSubject(theSubject));
          extraSubjectSourceAndIds.add(multiKey);
        }
      }
      //always add to all groups
      if (!allSubjectsSourceAndIds.contains(multiKey)) {
        allSubjects.add(theSubject);
        allSubjectsSourceAndIds.add(multiKey);
      }
    }
  
    //loop through all the hidden fields (max 100)
    for (int i=0;i<100;i++) {
      String extraSourceIdSubjectId = request.getParameter("extraSourceIdSubjectId_" + i);
      
      //we are at the end
      if (StringUtils.isBlank(extraSourceIdSubjectId)) {
        break;
      }
      
      //might be removing this one
      if (considerRemoveSubjectSourceAndId && StringUtils.equals(removeSubjectSourceAndId, extraSourceIdSubjectId)) {
        continue;
      }
      
      theSubject = null;

      try {
        GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
        if (extraSourceIdSubjectId != null && extraSourceIdSubjectId.contains("||")) {
          String sourceId = GrouperUtil.prefixOrSuffix(extraSourceIdSubjectId, "||", true);
          String subjectId = GrouperUtil.prefixOrSuffix(extraSourceIdSubjectId, "||", false);
          theSubject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
        }
      } finally {
        GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
      }
      
      if (theSubject != null) {
        MultiKey multiKey = new MultiKey(theSubject.getSourceId(), theSubject.getId());
        if (!extraSubjectSourceAndIds.contains(multiKey)) {
          extraGuiSubjects.add(new GuiSubject(theSubject));
          extraSubjectSourceAndIds.add(multiKey);
        }
  
        if (!allSubjectsSourceAndIds.contains(multiKey)) {
          allSubjects.add(theSubject);
          allSubjectsSourceAndIds.add(multiKey);
        }
      }      
    }
    return success;
  }

}
