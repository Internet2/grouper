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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVRecord;
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
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupImportError;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupImportGroupSummary;
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
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
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
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectUtils;

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
    //TODO cant this loop and the above logic be collapsed?
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
   * keep an expirable cache of import progress for 5 hours (longest an import is expected).  This has multikey of session id and some random uuid
   * uniquely identifies this import as opposed to other imports in other tabs.  This cannot have any request objects or j2ee objects
   */
  private static ExpirableCache<MultiKey, GroupImportContainer> importThreadProgress = new ExpirableCache<MultiKey, GroupImportContainer>(300);

  /**
   * submit a group import
   * @param request
   * @param response
   */
  public void groupImportSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startNanos = System.nanoTime();
    
    debugMap.put("method", "groupImportSubmit");
    
    try {
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
      final GroupImportContainer groupImportContainer = grouperRequestContainer.getGroupImportContainer();
  
      String sessionId = request.getSession().getId();
      
      debugMap.put("sessionId", GrouperUtil.abbreviate(sessionId, 8));
  
      
      // uniquely identifies this import as opposed to other imports in other tabs
      String uniqueImportId = GrouperUuid.getUuid();
  
      debugMap.put("uniqueImportId", GrouperUtil.abbreviate(uniqueImportId, 8));
  
      groupImportContainer.setUniqueImportId(uniqueImportId);
      
      MultiKey reportMultiKey = new MultiKey(sessionId, uniqueImportId);
      
      importThreadProgress.put(reportMultiKey, groupImportContainer);
      
      GrouperSession grouperSession = null;
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      final String bulkAddOption = request.getParameter("bulkAddOptions");
  
      
      //TODO should this be called "groupsTheUserCanUpdate" ?
      final Set<Group> groups = new LinkedHashSet<Group>();
      final Set<Subject> subjectSet = new LinkedHashSet<Subject>();
      final Map<String, Integer> listInvalidSubjectIdsAndRow = new LinkedHashMap<String, Integer>();
      
      final boolean importReplaceMembers = GrouperUtil.booleanValue(request.getParameter("replaceExistingMembers"), false);
      final boolean removeMembers = GrouperUtil.booleanValue(request.getParameter("removeMembers"), false);
  
      final Object[] csvEntriesObject = new Object[1];
  
      final String[] fileName = new String[1];
  
      try {
        grouperSession = GrouperSession.start(loggedInSubject);
  
        boolean success = groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, groups, false);
        
        if (!success) {
          //error message already shown
          return;
        }
  
        
        debugMap.put("groups", groups.size());

        if (groups.size() == 0) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#groupImportGroupComboErrorId",
              TextContainer.retrieveFromRequest().getText().get("groupImportGroupNotFound")));
          return;
        }
  
        // can be import, input, list
        debugMap.put("bulkAddOption", bulkAddOption);
  
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
          
          fileName[0] = StringUtils.defaultString(importCsvFile == null ? "" : importCsvFile.getName());
  
          try {
            
            List<CSVRecord> csvEntries = SimpleMembershipUpdateImportExport.parseCsvImportFileToCsv(reader, fileName[0]);
            debugMap.put("csvEntries", GrouperUtil.length(csvEntries));
            csvEntriesObject[0] = csvEntries;
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

          debugMap.put("entityIdOrIdentifiers", GrouperUtil.length(entityIdOrIdentifiers));
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
            groupImportContainer.setGroupId(group.getId());
          }
        }
        {
          Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
          if (subject != null) {
            groupImportContainer.setImportFromSubject(true);
            groupImportContainer.setSubjectId(subject.getId());
            groupImportContainer.setSourceId(subject.getSourceId());
          }
        }
  
        if (importReplaceMembers && removeMembers) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#replaceExistingMembersId",
              TextContainer.retrieveFromRequest().getText().get("groupImportCantReplaceAndRemove")));
          return;
        }
        
        Iterator<Group> groupIterator = groups.iterator();
  
        //TODO first off, why checking VIEW?  should it be READ?  or just UPDATE?
        //TODO second, are groups not checked for UPDATE above in groupImportSetupExtraGroups()?  or is it just groups added from gruop screen?
        
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
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,  
                  TextContainer.retrieveFromRequest().getText().get("groupImportGroupCantUpdate")));
              continue;
            }
          }
        }
        
      } catch (Exception e) {
        throw new RuntimeException("error", e);
  
  
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      
      GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("groupImportMembers") {

        @Override
        public Void callLogic() {
          try {
            groupImportContainer.getProgressBean().setStartedMillis(System.currentTimeMillis());

            UiV2GroupImport.this.groupImportSubmitHelper(loggedInSubject, groupImportContainer, groups, subjectSet, 
                listInvalidSubjectIdsAndRow, removeMembers, importReplaceMembers, bulkAddOption, fileName[0], (List<CSVRecord>)csvEntriesObject[0]);
          } catch (RuntimeException re) {
            groupImportContainer.getProgressBean().setHasException(true);
            // log this since the thread will just end and will never get logged
            LOG.error("error", re);
          } finally {
            // we done
            groupImportContainer.getProgressBean().setComplete(true);
          }
          return null;
        }
      };      
      
      // see if running in thread
      boolean useThreads = GrouperUiConfig.retrieveConfig().propertyValueBooleanRequired("grouperUi.import.useThread");
      debugMap.put("useThreads", useThreads);

      if (useThreads) {
        
        GrouperFuture<Void> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
        
        Integer waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.import.progressStartsInSeconds");
        debugMap.put("waitForCompleteForSeconds", waitForCompleteForSeconds);

        GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
        
        debugMap.put("threadAlive", !grouperFuture.isDone());

      } else {
        grouperCallable.callLogic();
      }
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportReportWrapper.jsp"));
      
      groupImportReportStatusHelper(sessionId, uniqueImportId);
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime()-startNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * get the status of a report
   * @param request
   * @param response
   */
  public void groupImportReportStatus(HttpServletRequest request, HttpServletResponse response) {
    String sessionId = request.getSession().getId();
    String uniqueImportId = request.getParameter("uniqueImportId");
    groupImportReportStatusHelper(sessionId, uniqueImportId);
  }
  
  /**
   * get the status of a report
   * @param request
   * @param response
   */
  private void groupImportReportStatusHelper(String sessionId, String uniqueImportId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "groupImportReportStatus");
    debugMap.put("sessionId", GrouperUtil.abbreviate(sessionId, 8));
    debugMap.put("uniqueImportId", GrouperUtil.abbreviate(uniqueImportId, 8));

    long startNanos = System.nanoTime();
    try {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      MultiKey reportMultiKey = new MultiKey(sessionId, uniqueImportId);
      
      GroupImportContainer groupImportContainer = importThreadProgress.get(reportMultiKey);
      
      GrouperRequestContainer.retrieveFromRequestOrCreate().setGroupImportContainer(groupImportContainer);
  
      //show the report screen
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#id_"+uniqueImportId, 
          "/WEB-INF/grouperUi2/groupImport/groupImportReport.jsp"));
      // guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));

      debugMap.put("percentComplete", groupImportContainer.getProgressBean().getPercentComplete());
      debugMap.put("progressCompleteRecords", groupImportContainer.getProgressBean().getProgressCompleteRecords());
      debugMap.put("progressTotalRecords", groupImportContainer.getProgressBean().getProgressTotalRecords());
      

      if (groupImportContainer != null) {
        
        // endless loop?
        if (groupImportContainer.getProgressBean().isThisLastStatus()) {
          return;
        }
        
        if (groupImportContainer.getProgressBean().isHasException()) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("groupImportException")));
          // it has an exception, leave it be
          importThreadProgress.put(reportMultiKey, null);
          return;
        }
        // kick it off again?
        debugMap.put("complete", groupImportContainer.getProgressBean().isComplete());
        if (!groupImportContainer.getProgressBean().isComplete()) {
          int progressRefreshSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.import.progressRefreshSeconds");
          progressRefreshSeconds = Math.max(progressRefreshSeconds, 1);
          progressRefreshSeconds *= 1000;
          guiResponseJs.addAction(GuiScreenAction.newScript("setTimeout(function() {ajax('../app/UiV2GroupImport.groupImportReportStatus?uniqueImportId=" + uniqueImportId + "')}, " + progressRefreshSeconds + ")"));
        } else {
          // it is complete, leave it be
          importThreadProgress.put(reportMultiKey, null);
        }
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime()-startNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }


  }

  /**
   * method to do logic for import submit (note, dont use anything related to session here)
   * @param loggedInSubject 
   * @param groupImportContainer 
   * @param groups 
   * @param subjectSet 
   * @param listInvalidSubjectIdsAndRow 
   * @param removeMembers 
   * @param importReplaceMembers 
   * @param bulkAddOption 
   * @param fileName 
   */
  private void groupImportSubmitHelper(final Subject loggedInSubject, final GroupImportContainer groupImportContainer, 
      final Set<Group> groups, final Set<Subject> subjectSet, Map<String, Integer> listInvalidSubjectIdsAndRow, 
      boolean removeMembers, boolean importReplaceMembers, String bulkAddOption, String fileName, List<CSVRecord> csvEntries) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "groupImportSubmit");

    GrouperSession grouperSession = null;

    int pauseBetweenRecordsMillis = GrouperUiConfig.retrieveConfig().propertyValueIntRequired("grouperUi.import.pauseInBetweenRecordsMillis");
        
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      ProgressBean progressBean = groupImportContainer.getProgressBean();
      
      if (GrouperUtil.length(subjectSet) == 0 && csvEntries != null) {
        subjectSet.addAll(SimpleMembershipUpdateImportExport.parseCsvImportFile(csvEntries, new ArrayList<String>(), 
            listInvalidSubjectIdsAndRow, true));
      }
      
      Iterator<Group> groupIterator = groups.iterator();

      Set<GuiGroup> guiGroups = new LinkedHashSet<GuiGroup>();
      groupImportContainer.setGuiGroups(guiGroups);
      
      progressBean.setProgressTotalRecords(GrouperUtil.length(groups) * GrouperUtil.length(subjectSet));
      
      //lets go through the groups that were submitted
      while (groupIterator.hasNext()) {

        final Group group = groupIterator.next();

        guiGroups.add(new GuiGroup(group));

        GroupImportGroupSummary groupImportGroupSummary = new GroupImportGroupSummary();
        groupImportContainer.getGroupImportGroupSummaryForGroupMap().put(group, groupImportGroupSummary);
        
        List<Member> existingMembers = new ArrayList<Member>(GrouperUtil.nonNull(group.getImmediateMembers()));
        List<Subject> subjectList = new ArrayList<Subject>(GrouperUtil.nonNull(subjectSet));
        groupImportGroupSummary.setGroupCountOriginal(GrouperUtil.length(existingMembers));

        List<Member> overlappingMembers = new ArrayList<Member>(GrouperUtil.nonNull(GrouperUiUtils.removeOverlappingSubjects(existingMembers, subjectList)));

        // figure out subject not founds
        if (listInvalidSubjectIdsAndRow.size() > 0) {
          for (String subjectLabel : listInvalidSubjectIdsAndRow.keySet()) {
            int rowNumber = listInvalidSubjectIdsAndRow.get(subjectLabel);
            
            GroupImportError groupImportError = new GroupImportError(subjectLabel, TextContainer.retrieveFromRequest().getText().get(
                "groupImportProblemFindingSubjectError"), rowNumber);
            
            groupImportGroupSummary.getGroupImportErrors().add(groupImportError);
            
            groupImportGroupSummary.groupCountErrorsIncrement();
          }
        }

        if (!removeMembers) {
          progressBean.addProgressCompleteRecords(GrouperUtil.length(subjectSet) - GrouperUtil.length(subjectList));
          //first lets add some members
          for (int i=0;i<subjectList.size();i++) {
            
            Subject subject = subjectList.get(i);

            boolean hasError = false;
            if (subject instanceof ImportSubjectWrapper) {
              try {
                subject = ((ImportSubjectWrapper)subject).wrappedSubject();
              } catch (Exception e) {
                int rowNumber = ((ImportSubjectWrapper)subject).getRow();
                String label = ImportSubjectWrapper.errorLabelForRowStatic(rowNumber, ((ImportSubjectWrapper)subject).getRowData());
                GroupImportError groupImportError = new GroupImportError(label, TextContainer.retrieveFromRequest().getText().get(
                    "groupImportProblemFindingSubjectError"), rowNumber);
                
                groupImportGroupSummary.getGroupImportErrors().add(groupImportError);
                
                groupImportGroupSummary.groupCountErrorsIncrement();
                hasError = true;
              }
            }
            
            try {
              // try this even if we have an error
              group.addMember(subject, false);
              GrouperUtil.sleep(pauseBetweenRecordsMillis);
              groupImportGroupSummary.groupCountAddedIncrement();
            } catch (Exception e) {
              if (!hasError) {
                // if not already logged
                String subjectString = SubjectUtils.subjectToString(subject);

                GroupImportError groupImportError = new GroupImportError(subjectString, GrouperUtil.xmlEscape(e.getMessage()));
                groupImportGroupSummary.getGroupImportErrors().add(groupImportError);

                groupImportGroupSummary.groupCountErrorsIncrement();
                LOG.warn("error with " + subjectString, e);
              }
            }
            progressBean.addProgressCompleteRecords(1);
      
          }
        } else {
          progressBean.addProgressCompleteRecords(GrouperUtil.length(subjectSet) - GrouperUtil.length(overlappingMembers));
          //first lets remove some members
          for (int i=0;i<overlappingMembers.size();i++) {
            
            Member member = overlappingMembers.get(i);
            
            try {
                
              group.deleteMember(member, false);
              GrouperUtil.sleep(pauseBetweenRecordsMillis);
              
              groupImportGroupSummary.groupCountDeletedIncrement();
            } catch (Exception e) {
              String subjectString = SubjectUtils.subjectToString(member.getSubject());
              GroupImportError groupImportError = new GroupImportError(subjectString, GrouperUtil.xmlEscape(e.getMessage()));
              groupImportGroupSummary.getGroupImportErrors().add(groupImportError);
              groupImportGroupSummary.groupCountErrorsIncrement();
              LOG.warn("error with " + subjectString, e);
            }

            progressBean.addProgressCompleteRecords(1);

          }
          
        }
    
        boolean didntImportDueToSubjects = groupImportGroupSummary.getGroupCountErrors() > 0;
    
        //remove the ones which are already there
        if (importReplaceMembers && !didntImportDueToSubjects && !removeMembers) {
          
          progressBean.addProgressCompleteRecords(GrouperUtil.length(subjectSet) - GrouperUtil.length(existingMembers));
          for (Member existingMember : existingMembers) {
            
            try {
              group.deleteMember(existingMember, false);
              GrouperUtil.sleep(pauseBetweenRecordsMillis);
              groupImportGroupSummary.groupCountDeletedIncrement();
            } catch (Exception e) {

              
              String subjectString = SubjectUtils.subjectToString(existingMember.getSubject());
              GroupImportError groupImportError = new GroupImportError(subjectString, GrouperUtil.xmlEscape(e.getMessage()));
              groupImportGroupSummary.getGroupImportErrors().add(groupImportError);
              groupImportGroupSummary.groupCountErrorsIncrement();
              LOG.warn("error with " + subjectString, e);

            }
            progressBean.addProgressCompleteRecords(1);
          }
        }

        //this might be a little wasteful, but I think it is a good sanity check
        int newSize = group.getImmediateMembers().size();

        groupImportGroupSummary.setGroupCountNew(newSize);
        
        try {
          GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
              loggedInSubject, group);
        } catch (Exception e) {
          LOG.warn("Cant add recently used group: " + group.getName() + ", for subject: " + SubjectUtils.subjectToString(loggedInSubject) + ", maybe a priv was lost after import started???", e);
        }
        
        if (StringUtils.equals(bulkAddOption, "import")) {
          auditImport(group.getUuid(), group.getName(), fileName, groupImportGroupSummary.getGroupCountAdded(), groupImportGroupSummary.getGroupCountDeleted());
        }

        groupImportGroupSummary.setComplete(true);
      }
      // done
      progressBean.setProgressCompleteRecords(progressBean.getProgressTotalRecords());
      

    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
    private void auditImport(final String groupId, final String groupName, final String fileName,
        final int countAdded, final int countDeleted) {
      HibernateSession.callbackHibernateSession(
		    GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
			    new HibernateHandler() {
				    public Object callback(HibernateHandlerBean hibernateHandlerBean)
					    throws GrouperDAOException {
						  
				      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBERSHIP_GROUP_IMPORT, "file", fileName, "totalAdded", 
				          String.valueOf(countAdded), "groupId", groupId, "groupName", groupName, "totalDeleted", String.valueOf(countDeleted));
						  
				      String description = "Added : " + countAdded + " subjects "
				          + "  and deleted "+countDeleted + " subjects in group ."+groupName;
						auditEntry.setDescription(description);
						auditEntry.saveOrUpdate(true);
						  
						return null;
					  }
      });
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
          try {
            theSubject = StringUtils.isBlank(comboValue) ? null : SubjectFinder.findByIdOrIdentifier(comboValue, false);
          } catch (SubjectNotUniqueException snue) {
            //ignore
          }
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
