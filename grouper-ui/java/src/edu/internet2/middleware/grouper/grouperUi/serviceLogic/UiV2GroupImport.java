package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupImportContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.j2ee.GrouperUiRestServlet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
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
          "dijit.byId('groupImportGroupComboId').set('displayedValue', ''); " +
          "dijit.byId('groupImportGroupComboId').set('value', '');"));

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
      
      groupContainer.setGuiGroup(new GuiGroup(group));
      
      if (!groupContainer.isCanRead()) {
        throw new RuntimeException("Cant read group: " + group.getName());
      }
      
      //ids
      String groupExportOptions = urlStrings.get(3);
      
      boolean exportAll = false;
      if (StringUtils.equals("all", groupExportOptions)) {
        groupContainer.setExportAll(true);
        exportAll = true;
      } else if (StringUtils.equals("ids", groupExportOptions)) {
        groupContainer.setExportAll(false);
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
  
        SimpleMembershipUpdateImportExport.exportGroupAllFieldsToBrowser(group, headersCommaSeparated, exportAllSortField);
      } else {
        
        SimpleMembershipUpdateImportExport.exportGroupSubjectIdsCsv(group);
        
      }
      
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
      
      GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
      
      if (StringUtils.equals("all", groupExportOptions)) {
        groupContainer.setExportAll(true);
      } else if (StringUtils.equals("ids", groupExportOptions)) {
        groupContainer.setExportAll(false);
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
   * @return true if ok, false if not
   */
  private boolean groupImportSetupExtraGroups(Subject loggedInSubject, 
      HttpServletRequest request, GuiResponseJs guiResponseJs, boolean considerRemoveGroupId, boolean includeCombobox,
      Set<Group> allGroups) {

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
      if (includeCombobox) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportGroupComboIdErrorId",
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
      boolean success = groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, groups);
      
      if (!success) {
        //error message already shown
        return;
      }

      // can be import, input, list
      String bulkAddOption = request.getParameter("bulk-add-options");
      Map<String, Integer> listInvalidSubjectIdsAndRow = new LinkedHashMap<String, Integer>();
      
      Set<Subject> subjects = new LinkedHashSet<Subject>();
      
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
        
        String fileName = StringUtils.defaultString(importCsvFile == null ? "" : importCsvFile.getName());

        SimpleMembershipUpdateImportExport.parseCsvImportFile(reader, fileName, new ArrayList<String>(), 
            listInvalidSubjectIdsAndRow);
        
      } else if (StringUtils.equals(bulkAddOption, "input")) {

        //combobox
        success = groupImportSetupExtraSubjects(loggedInSubject, request, guiResponseJs, false, true, subjects);
        
        if (!success) {
          //error message already shown
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
        subjects.addAll(GrouperUtil.nonNull(entityIdOrIdentifierMap).values());

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

      
      
      
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

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

      groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, true, false, new LinkedHashSet<Group>());
      
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

      groupImportSetupExtraSubjects(loggedInSubject, request, guiResponseJs, true, false, new LinkedHashSet<Subject>());
      
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

      groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true, new LinkedHashSet<Group>());

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
  
      groupImportSetupExtraSubjects(loggedInSubject, request, guiResponseJs, false, true, new LinkedHashSet<Subject>());
  
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
   * @return true if ok, false if not
   */
  private boolean groupImportSetupExtraSubjects(Subject loggedInSubject, 
      HttpServletRequest request, GuiResponseJs guiResponseJs, boolean considerRemoveSubjectSourceAndId, 
      boolean includeCombobox, Set<Subject> allSubjects) {

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
      if (includeCombobox) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#groupImportSubjectComboIdErrorId", 
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
