package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.subectSource.SubjectSourceConfiguration;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiSubjectSourceConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SubjectSourceContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.GrouperDataFieldSourceAdapter;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectImpl;

public class UiV2SubjectSource {
  
  /**
   * @param request
   * @param response
   */
  public void viewSubjectSources(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectSourceContainer subjectSourceContainer = grouperRequestContainer.getSubjectSourceContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          List<Source> sortedSources = new ArrayList<Source>(SubjectFinder.getSources(false));
          
          Collections.sort(sortedSources, new Comparator<Source>() {

            @Override
            public int compare(Source o1, Source o2) {
              return GrouperUtil.compare(o1.getId().toLowerCase(), o2.getId().toLowerCase());
            }
          });
          
          subjectSourceContainer.setSources(sortedSources);
          
          return null;
        }
      });
            
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSources.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * when the source id changes fill in subject ids etc
   * @param request
   * @param response
   */
  public void compareSubjectSources(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectSourceContainer subjectSourceContainer = grouperRequestContainer.getSubjectSourceContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }

      final String subjectSourceId = request.getParameter("subjectSourceId");

      if (StringUtils.isBlank(subjectSourceId)) {
        throw new RuntimeException("subjectSourceId cannot be blank");
      }
      
      Source source = SourceManager.getInstance().getSource(subjectSourceId);
      
      if (source == null) {
        throw new RuntimeException("Cant find source by id: '" + subjectSourceId + "'");
      }
      
      subjectSourceContainer.setSource(source);
      
      List<Source> allSources = new ArrayList<Source>(SubjectFinder.getSources(false));
      List<Source> otherSources = new ArrayList<Source>();
      for (Source theSource : allSources) {
        if (GrouperAbac.internalSourceId(theSource.getId())
            || StringUtils.equals("grouperEntities", theSource.getId())
            || StringUtils.equals("grouperExternal", theSource.getId())) {
          continue;
        }
        if (source.isEnabled() != theSource.isEnabled()) {
          otherSources.add(theSource);
        }
      }
      
      Collections.sort(otherSources, new Comparator<Source>() {

        @Override
        public int compare(Source o1, Source o2) {
          return GrouperUtil.compare(o1.getId().toLowerCase(), o2.getId().toLowerCase());
        }
      });
      
      subjectSourceContainer.setSources(otherSources);
      
      String subjectId = source.getInitParam("subjectIdToFindOnCheckConfig");
      subjectId = StringUtils.defaultIfBlank(subjectId, "someSubjectId");

      String subjectIdentifier = source.getInitParam("subjectIdentifierToFindOnCheckConfig");
      subjectIdentifier = StringUtils.defaultIfBlank(subjectIdentifier, "someSubjectIdentifier");

      String searchString = source.getInitParam("stringToFindOnCheckConfig");
      searchString = StringUtils.defaultIfBlank(searchString, "first last");
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSourceCompare.jsp"));
      
      // change the textfields
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("subjectIdsName", subjectId));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("subjectIdentifiersName", subjectIdentifier));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("searchStringsName", searchString));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * run
   * @param request
   * @param response
   */
  public void compareSubjectSourcesSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectSourceContainer subjectSourceContainer = grouperRequestContainer.getSubjectSourceContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GrouperSession grouperSession = null;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      //if the user allowed
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      // want to make sure we're not using old cached data (e.g. virtual attribute config)
      ExpirableCache.clearAll();
      
      String actAsSubjectIdOrIdentifier = StringUtils.trim(request.getParameter("actAsName"));
      String sourceId1 = StringUtils.trim(request.getParameter("subjectApiSourceIdName"));
      String sourceId2 = StringUtils.trim(request.getParameter("otherSubjectApiSourceIdId"));
      String subjectIds = StringUtils.trim(request.getParameter("subjectIdsName"));
      String subjectIdentifiers = StringUtils.trim(request.getParameter("subjectIdentifiersName"));
      String subjectSearchStrings = StringUtils.trim(request.getParameter("searchStringsName"));
      List<String> subjectIdsList = new ArrayList<>();
      List<String> subjectIdentifiersList = new ArrayList<>();
      List<String> subjectSearchStringsList = new ArrayList<>();
      
      Source enabledSource = SubjectFinder.getSource(sourceId1).isEnabled() ? SubjectFinder.getSource(sourceId1) : SubjectFinder.getSource(sourceId2);
      Source disabledSource = SubjectFinder.getSource(sourceId1).isEnabled() ? SubjectFinder.getSource(sourceId2) : SubjectFinder.getSource(sourceId1);
      
      if (!enabledSource.isEnabled() || disabledSource.isEnabled()) {
        throw new RuntimeException("Unexpected");
      }

      if (GrouperAbac.internalSourceId(disabledSource.getId())
          || StringUtils.equals("grouperEntities", disabledSource.getId())
          || StringUtils.equals("grouperExternal", disabledSource.getId())) {
        throw new RuntimeException("Unexpected");
      }
      
      if (!GrouperUtil.isBlank(subjectIds)) {
        subjectIdsList = GrouperUtil.splitTrimToList(subjectIds.replace("\r\n", "\n").replace("\r", "\n"), "\n");
      }

      if (!GrouperUtil.isBlank(subjectIdentifiers)) {
        subjectIdentifiersList = GrouperUtil.splitTrimToList(subjectIdentifiers.replace("\r\n", "\n").replace("\r", "\n"), "\n");
      }
      
      if (!GrouperUtil.isBlank(subjectSearchStrings)) {
        subjectSearchStringsList = GrouperUtil.splitTrimToList(subjectSearchStrings.replace("\r\n", "\n").replace("\r", "\n"), "\n");
      }
      
      if (subjectIdsList.size() == 0 && subjectIdentifiersList.size() == 0 && subjectSearchStringsList.size() == 0) {
        // pick 100 random subject ids
        String sql = "select subject_id from grouper_members where subject_source = ? and subject_resolution_deleted='F' and subject_resolution_resolvable='T' ";
        if (GrouperDdlUtils.isOracle()) {
          sql += "and rownum <= 100";
        } else {
          sql += "limit 100";
        }
        subjectIdsList = new GcDbAccess().sql(sql).addBindVar(enabledSource.getId()).selectList(String.class);
      }
      
      if (!GrouperUtil.isBlank(actAsSubjectIdOrIdentifier)) {
        Subject actAsSubject = SubjectFinder.findByIdOrIdentifier(actAsSubjectIdOrIdentifier, true);
        GrouperSession.stopQuietly(grouperSession);
        grouperSession = GrouperSession.start(actAsSubject);
      }
      
      Map<String, Subject> enabledSourceFindByIds = subjectIdsList.size() == 0 ? new HashMap<>() : enabledSource.getSubjectsByIds(subjectIdsList);
      Map<String, Subject> enabledSourceFindByIdentifiers = subjectIdentifiersList.size() == 0 ? new HashMap<>() : enabledSource.getSubjectsByIdentifiers(subjectIdentifiersList);
      Map<String, Set<Subject>> enabledSourceFindAll = new HashMap<>();
      for (String searchString : subjectSearchStringsList) {
        Set<Subject> subjects = enabledSource.search(searchString);
        enabledSourceFindAll.put(searchString, subjects);
      }
      
      Map<String, Subject> enabledSourceFindByIdsUsingIdentifiers = subjectIdentifiersList.size() == 0 ? new HashMap<>() : enabledSource.getSubjectsByIds(subjectIdentifiersList);
      Map<String, Subject> enabledSourceFindByIdentifiersUsingIds = subjectIdsList.size() == 0 ? new HashMap<>() : enabledSource.getSubjectsByIdentifiers(subjectIdsList);

      Map<String, Subject> disabledSourceFindByIds;
      Map<String, Subject> disabledSourceFindByIdentifiers;
      Map<String, Set<Subject>> disabledSourceFindAll;
      Map<String, Subject> disabledSourceFindByIdsUsingIdentifiers;
      Map<String, Subject> disabledSourceFindByIdentifiersUsingIds;
      
      String disabledSourceIdToSet = null;
      
      try {
        if (disabledSource instanceof GrouperDataFieldSourceAdapter) {
          disabledSourceIdToSet = disabledSource.getId();
          disabledSource.setId(enabledSource.getId());
        }
        
        disabledSourceFindByIds = subjectIdsList.size() == 0 ? new HashMap<>() : disabledSource.getSubjectsByIds(subjectIdsList);
        disabledSourceFindByIdentifiers = subjectIdentifiersList.size() == 0 ? new HashMap<>() : disabledSource.getSubjectsByIdentifiers(subjectIdentifiersList);
        
        disabledSourceFindByIdsUsingIdentifiers = subjectIdentifiersList.size() == 0 ? new HashMap<>() : disabledSource.getSubjectsByIds(subjectIdentifiersList);
        disabledSourceFindByIdentifiersUsingIds = subjectIdsList.size() == 0 ? new HashMap<>() : disabledSource.getSubjectsByIdentifiers(subjectIdsList);

        disabledSourceFindAll = new HashMap<>();
        for (String searchString : subjectSearchStringsList) {
          Set<Subject> subjects = disabledSource.search(searchString);
          disabledSourceFindAll.put(searchString, subjects);
        }
      } finally {
        if (disabledSourceIdToSet != null) {
          disabledSource.setId(disabledSourceIdToSet);
        }
      }
      
      StringBuilder report = new StringBuilder();
      
      // SUJBECT IDS
      report.append("<pre>\n");
      report.append("<h4>Subject IDs Comparison</h4>\n");
      if (subjectIdsList.size() == 0) {
        report.append("<font color='blue'>No subject ids provided</font>\n");
      } else {
        for (String subjectId : subjectIdsList) {
          Subject enabledSubject = enabledSourceFindByIds.get(subjectId);
          Subject disabledSubject = disabledSourceFindByIds.get(subjectId);

          if (disabledSubject != null) {
            // correct source id so that virtual attributes resolve correctly
            ((SubjectImpl)disabledSubject).setSourceId(disabledSource.getId());
            ((SubjectImpl)disabledSubject).attributesInittedClear();
          }
          
          if (enabledSubject == null && disabledSubject == null) {
            report.append("<font color='green'>SUCCESS:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " wasn't found in either source\n");
          } else if (enabledSubject == null && disabledSubject != null) {
            report.append("<font color='red'>ERROR:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " only found in source " + disabledSource.getId() + "\n");
          } else if (enabledSubject != null && disabledSubject == null) {
            report.append("<font color='red'>ERROR:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " only found in source " + enabledSource.getId() + "\n");
          } else {
            report.append("<font color='green'>SUCCESS:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " found in both sources\n");
            addSubjectComparisonToReport(enabledSource, disabledSource, enabledSubject, disabledSubject, report);
          }
          
          report.append("<br />\n");
        }
        
        for (String subjectId : subjectIdsList) {
          Subject enabledSubject = enabledSourceFindByIdentifiersUsingIds.get(subjectId);
          Subject disabledSubject = disabledSourceFindByIdentifiersUsingIds.get(subjectId);

          if (enabledSubject == null && disabledSubject == null) {
            report.append("<font color='green'>SUCCESS:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " wasn't found in either source when querying by subject identifier\n");
          } else if (enabledSubject == null && disabledSubject != null) {
            report.append("<font color='red'>ERROR:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " only found in source " + disabledSource.getId() + " when querying by subject identifier\n");
          } else if (enabledSubject != null && disabledSubject == null) {
            report.append("<font color='red'>ERROR:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " only found in source " + enabledSource.getId() + " when querying by subject identifier\n");
          } else {
            report.append("<font color='green'>SUCCESS:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " found in both sources when querying by subject identifier\n");
          }          
        }
      }
      report.append("</pre>\n");
      
      // SUJBECT IDENTIFIERS
      report.append("<pre>\n");
      report.append("<h4>Subject Identifiers Comparison</h4>\n");
      if (subjectIdentifiersList.size() == 0) {
        report.append("<font color='blue'>No subject identifiers provided</font>\n");
      } else {
        for (String subjectIdentifier : subjectIdentifiersList) {
          Subject enabledSubject = enabledSourceFindByIdentifiers.get(subjectIdentifier);
          Subject disabledSubject = disabledSourceFindByIdentifiers.get(subjectIdentifier);
          
          if (disabledSubject != null) {
            // correct source id so that virtual attributes resolve correctly
            ((SubjectImpl)disabledSubject).setSourceId(disabledSource.getId());
            ((SubjectImpl)disabledSubject).attributesInittedClear();
          }
          
          if (enabledSubject == null && disabledSubject == null) {
            report.append("<font color='green'>SUCCESS:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " wasn't found in either source\n");
          } else if (enabledSubject == null && disabledSubject != null) {
            report.append("<font color='red'>ERROR:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " only found in source " + disabledSource.getId() + "\n");
          } else if (enabledSubject != null && disabledSubject == null) {
            report.append("<font color='red'>ERROR:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " only found in source " + enabledSource.getId() + "\n");
          } else {
            report.append("<font color='green'>SUCCESS:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " found in both sources\n");
            addSubjectComparisonToReport(enabledSource, disabledSource, enabledSubject, disabledSubject, report);
          }
          
          report.append("<br />\n");
        }
        
        for (String subjectIdentifier : subjectIdentifiersList) {
          Subject enabledSubject = enabledSourceFindByIdsUsingIdentifiers.get(subjectIdentifier);
          Subject disabledSubject = disabledSourceFindByIdsUsingIdentifiers.get(subjectIdentifier);
          
          if (enabledSubject == null && disabledSubject == null) {
            report.append("<font color='green'>SUCCESS:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " wasn't found in either source when querying by subject id\n");
          } else if (enabledSubject == null && disabledSubject != null) {
            report.append("<font color='red'>ERROR:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " only found in source " + disabledSource.getId() + " when querying by subject id\n");
          } else if (enabledSubject != null && disabledSubject == null) {
            report.append("<font color='red'>ERROR:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " only found in source " + enabledSource.getId() + " when querying by subject id\n");
          } else {
            report.append("<font color='green'>SUCCESS:</font> Subject Identifier " + GrouperUtil.xmlEscape(subjectIdentifier) + " found in both sources when querying by subject id\n");
          }          
        }
      }
      report.append("</pre>\n");
      
      // SUJBECT SEARCHES
      report.append("<pre>\n");
      report.append("<h4>Subject Searches Comparison</h4>\n");
      if (subjectSearchStringsList.size() == 0) {
        report.append("<font color='blue'>No subject search strings provided</font>\n");
      } else {
        for (String subjectSearchString : subjectSearchStringsList) {
          Set<Subject> enabledSubjects = GrouperUtil.nonNull(enabledSourceFindAll.get(subjectSearchString));
          Set<Subject> disabledSubjects = GrouperUtil.nonNull(disabledSourceFindAll.get(subjectSearchString));
          report.append("<h5>Results for search string: " + GrouperUtil.xmlEscape(subjectSearchString) + "</h5>\n");
          if (enabledSubjects.size() == 0 && disabledSubjects.size() == 0) {
            report.append("<font color='green'>SUCCESS:</font> No results found in either source\n");
          } else {
            Map<String, Subject> enabledSubjectsBySubjectId = new HashMap<>();
            Map<String, Subject> disabledSubjectsBySubjectId = new HashMap<>();
            
            for (Subject subject : enabledSubjects) {
              enabledSubjectsBySubjectId.put(subject.getId(), subject);
            }
            
            for (Subject subject : disabledSubjects) {
              disabledSubjectsBySubjectId.put(subject.getId(), subject);
            }
            
            Set<String> allSubjectIds = new HashSet<>(enabledSubjectsBySubjectId.keySet());
            allSubjectIds.addAll(disabledSubjectsBySubjectId.keySet());
            
            for (String subjectId : allSubjectIds) {
              Subject enabledSubject = enabledSubjectsBySubjectId.get(subjectId);
              Subject disabledSubject = disabledSubjectsBySubjectId.get(subjectId);
              
              if (disabledSubject != null) {
                // correct source id so that virtual attributes resolve correctly
                ((SubjectImpl)disabledSubject).setSourceId(disabledSource.getId());
                ((SubjectImpl)disabledSubject).attributesInittedClear();
              }
              
              if (enabledSubject == null && disabledSubject != null) {
                report.append("<font color='red'>ERROR:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " only found in source " + disabledSource.getId() + "\n");
              } else if (enabledSubject != null && disabledSubject == null) {
                report.append("<font color='red'>ERROR:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " only found in source " + enabledSource.getId() + "\n");
              } else {
                report.append("<font color='green'>SUCCESS:</font> Subject ID " + GrouperUtil.xmlEscape(subjectId) + " found in both sources\n");
                addSubjectComparisonToReport(enabledSource, disabledSource, enabledSubject, disabledSubject, report);
              }
              
              report.append("<br />\n");
            }
          }
          
          report.append("<br />\n");
        }
      }
      report.append("</pre>\n");
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#subjectApiCompareResultsId", report.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  private void addSubjectComparisonToReport(Source enabledSource, Source disabledSource, Subject enabledSubject, Subject disabledSubject, StringBuilder report) {
    if (GrouperUtil.equals(enabledSubject.getName(), disabledSubject.getName())) {
      report.append("<font color='green'>SUCCESS:</font> name matches (" + GrouperUtil.xmlEscape(enabledSubject.getName()) + ")\n"); 
    } else {
      report.append("<font color='red'>ERROR:</font> name mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSubject.getName()) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSubject.getName()) + "\n"); 
    }
    
    if (GrouperUtil.equals(enabledSubject.getDescription(), disabledSubject.getDescription())) {
      report.append("<font color='green'>SUCCESS:</font> description matches (" + GrouperUtil.xmlEscape(enabledSubject.getDescription()) + ")\n"); 
    } else {
      report.append("<font color='red'>ERROR:</font> description mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSubject.getDescription()) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSubject.getDescription()) + "\n"); 
    }
    
    // use getAttributes(true) to exclude internal attributes (name, description, etc.)
    Set<String> attributeNames = new HashSet<>(enabledSubject.getAttributes(true).keySet());
    attributeNames.addAll(new HashSet<>(disabledSubject.getAttributes(true).keySet()));
    for (String attributeName : attributeNames) {
      Set<String> enabledSubjectAttributeValues = new HashSet<>(GrouperUtil.nonNull(enabledSubject.getAttributes(true).get(attributeName)));
      Set<String> disabledSubjectAttributeValues = new HashSet<>(GrouperUtil.nonNull(disabledSubject.getAttributes(true).get(attributeName)));

      // remove blank values so that {} and {""} are treated as equivalent
      Iterator<String> enabledIterator = enabledSubjectAttributeValues.iterator();
      while (enabledIterator.hasNext()) {
        String value = enabledIterator.next();
        if (value == null || StringUtils.isBlank(value)) {
          enabledIterator.remove();
        }
      }
      Iterator<String> disabledIterator = disabledSubjectAttributeValues.iterator();
      while (disabledIterator.hasNext()) {
        String value = disabledIterator.next();
        if (value == null || StringUtils.isBlank(value)) {
          disabledIterator.remove();
        }
      }

      String enabledValuesString = GrouperUtil.join(enabledSubjectAttributeValues.iterator(), ",");
      String disabledValuesString = GrouperUtil.join(disabledSubjectAttributeValues.iterator(), ",");
      if (GrouperUtil.equals(enabledSubjectAttributeValues, disabledSubjectAttributeValues)) {
        report.append("<font color='green'>SUCCESS:</font> attribute " + GrouperUtil.xmlEscape(attributeName) + " matches (" + GrouperUtil.xmlEscape(enabledValuesString) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> attribute " + GrouperUtil.xmlEscape(attributeName) + " mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledValuesString) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledValuesString) + "\n");
      }
    }
    
    // email
    {
      String enabledSubjectEmailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(enabledSubject.getSourceId());
      String disabledSubjectEmailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(disabledSubject.getSourceId());
      
      if (GrouperUtil.equals(enabledSubject.getAttributeValue(enabledSubjectEmailAttributeName, false), disabledSubject.getAttributeValue(disabledSubjectEmailAttributeName, false))) {
        report.append("<font color='green'>SUCCESS:</font> email matches (" + GrouperUtil.xmlEscape(enabledSubject.getAttributeValue(enabledSubjectEmailAttributeName, false)) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> email mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSubject.getAttributeValue(enabledSubjectEmailAttributeName, false)) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSubject.getAttributeValue(disabledSubjectEmailAttributeName, false)) + "\n"); 
      }
    }
    
    // subject identifiers
    {
      String enabledSubjectIdentifier0 = null;
      String enabledSubjectIdentifier1 = null;
      String enabledSubjectIdentifier2 = null;
      String disabledSubjectIdentifier0 = null;
      String disabledSubjectIdentifier1 = null;
      String disabledSubjectIdentifier2 = null;
      
      Map<Integer, String> enabledSubjectIdentifierAttributes = enabledSubject.getSource().getSubjectIdentifierAttributesAll();
      if (enabledSubjectIdentifierAttributes.containsKey(0)) {
        enabledSubjectIdentifier0 = enabledSubject.getAttributeValue(enabledSubjectIdentifierAttributes.get(0), false);
      }
      if (enabledSubjectIdentifierAttributes.containsKey(1)) {
        enabledSubjectIdentifier1 = enabledSubject.getAttributeValue(enabledSubjectIdentifierAttributes.get(1), false);
      }
      if (enabledSubjectIdentifierAttributes.containsKey(2)) {
        enabledSubjectIdentifier2 = enabledSubject.getAttributeValue(enabledSubjectIdentifierAttributes.get(2), false);
      }
      
      Map<Integer, String> disabledSubjectIdentifierAttributes = disabledSubject.getSource().getSubjectIdentifierAttributesAll();
      if (disabledSubjectIdentifierAttributes.containsKey(0)) {
        disabledSubjectIdentifier0 = disabledSubject.getAttributeValue(disabledSubjectIdentifierAttributes.get(0), false);
      }
      if (disabledSubjectIdentifierAttributes.containsKey(1)) {
        disabledSubjectIdentifier1 = disabledSubject.getAttributeValue(disabledSubjectIdentifierAttributes.get(1), false);
      }
      if (disabledSubjectIdentifierAttributes.containsKey(2)) {
        disabledSubjectIdentifier2 = disabledSubject.getAttributeValue(disabledSubjectIdentifierAttributes.get(2), false);
      }
      
      if (GrouperUtil.equals(enabledSubjectIdentifier0, disabledSubjectIdentifier0)) {
        report.append("<font color='green'>SUCCESS:</font> subjectIdentifier0 matches (" + GrouperUtil.xmlEscape(enabledSubjectIdentifier0) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> subjectIdentifier0 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSubjectIdentifier0) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSubjectIdentifier0) + "\n"); 
      }
      
      if (GrouperUtil.equals(enabledSubjectIdentifier1, disabledSubjectIdentifier1)) {
        report.append("<font color='green'>SUCCESS:</font> subjectIdentifier1 matches (" + GrouperUtil.xmlEscape(enabledSubjectIdentifier1) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> subjectIdentifier1 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSubjectIdentifier1) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSubjectIdentifier1) + "\n"); 
      }
      
      if (GrouperUtil.equals(enabledSubjectIdentifier2, disabledSubjectIdentifier2)) {
        report.append("<font color='green'>SUCCESS:</font> subjectIdentifier2 matches (" + GrouperUtil.xmlEscape(enabledSubjectIdentifier2) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> subjectIdentifier2 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSubjectIdentifier2) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSubjectIdentifier2) + "\n"); 
      }
    }
    
    // search string
    {
      String enabledSearchString0 = null;
      String enabledSearchString1 = null;
      String enabledSearchString2 = null;
      String enabledSearchString3 = null;
      String enabledSearchString4 = null;
      String disabledSearchString0 = null;
      String disabledSearchString1 = null;
      String disabledSearchString2 = null;
      String disabledSearchString3 = null;
      String disabledSearchString4 = null;

      Map<Integer, String> enabledSearchAttributes = enabledSubject.getSource().getSearchAttributes();
      if (enabledSearchAttributes.containsKey(0)) {
        Set<String> attrs = enabledSubject.getAttributeValues(enabledSearchAttributes.get(0), false);
        if (attrs != null && attrs.size() > 0) {
          enabledSearchString0 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (enabledSearchAttributes.containsKey(1)) {
        Set<String> attrs = enabledSubject.getAttributeValues(enabledSearchAttributes.get(1), false);
        if (attrs != null && attrs.size() > 0) {
          enabledSearchString1 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (enabledSearchAttributes.containsKey(2)) {
        Set<String> attrs = enabledSubject.getAttributeValues(enabledSearchAttributes.get(2), false);
        if (attrs != null && attrs.size() > 0) {
          enabledSearchString2 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (enabledSearchAttributes.containsKey(3)) {
        Set<String> attrs = enabledSubject.getAttributeValues(enabledSearchAttributes.get(3), false);
        if (attrs != null && attrs.size() > 0) {
          enabledSearchString3 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (enabledSearchAttributes.containsKey(4)) {
        Set<String> attrs = enabledSubject.getAttributeValues(enabledSearchAttributes.get(4), false);
        if (attrs != null && attrs.size() > 0) {
          enabledSearchString4 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      Map<Integer, String> disabledSearchAttributes = disabledSubject.getSource().getSearchAttributes();
      if (disabledSearchAttributes.containsKey(0)) {
        Set<String> attrs = disabledSubject.getAttributeValues(disabledSearchAttributes.get(0), false);
        if (attrs != null && attrs.size() > 0) {
          disabledSearchString0 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (disabledSearchAttributes.containsKey(1)) {
        Set<String> attrs = disabledSubject.getAttributeValues(disabledSearchAttributes.get(1), false);
        if (attrs != null && attrs.size() > 0) {
          disabledSearchString1 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (disabledSearchAttributes.containsKey(2)) {
        Set<String> attrs = disabledSubject.getAttributeValues(disabledSearchAttributes.get(2), false);
        if (attrs != null && attrs.size() > 0) {
          disabledSearchString2 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (disabledSearchAttributes.containsKey(3)) {
        Set<String> attrs = disabledSubject.getAttributeValues(disabledSearchAttributes.get(3), false);
        if (attrs != null && attrs.size() > 0) {
          disabledSearchString3 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (disabledSearchAttributes.containsKey(4)) {
        Set<String> attrs = disabledSubject.getAttributeValues(disabledSearchAttributes.get(4), false);
        if (attrs != null && attrs.size() > 0) {
          disabledSearchString4 = GrouperUtil.join(attrs.iterator(), ",");
        }
      }
      
      if (GrouperUtil.equals(enabledSearchString0, disabledSearchString0)) {
        report.append("<font color='green'>SUCCESS:</font> searchString0 matches (" + GrouperUtil.xmlEscape(enabledSearchString0) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> searchString0 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSearchString0) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSearchString0) + "\n"); 
      }
      
      if (GrouperUtil.equals(enabledSearchString1, disabledSearchString1)) {
        report.append("<font color='green'>SUCCESS:</font> searchString1 matches (" + GrouperUtil.xmlEscape(enabledSearchString1) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> searchString1 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSearchString1) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSearchString1) + "\n");
      }
      
      if (GrouperUtil.equals(enabledSearchString2, disabledSearchString2)) {
        report.append("<font color='green'>SUCCESS:</font> searchString2 matches (" + GrouperUtil.xmlEscape(enabledSearchString2) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> searchString2 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSearchString2) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSearchString2) + "\n");
      }
      
      if (GrouperUtil.equals(enabledSearchString3, disabledSearchString3)) {
        report.append("<font color='green'>SUCCESS:</font> searchString3 matches (" + GrouperUtil.xmlEscape(enabledSearchString3) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> searchString3 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSearchString3) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSearchString3) + "\n");
      }
      
      if (GrouperUtil.equals(enabledSearchString4, disabledSearchString4)) {
        report.append("<font color='green'>SUCCESS:</font> searchString4 matches (" + GrouperUtil.xmlEscape(enabledSearchString4) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> searchString4 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSearchString4) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSearchString4) + "\n");
      }
    }
    
    // sort string
    {
      String enabledSortString0 = null;
      String enabledSortString1 = null;
      String enabledSortString2 = null;
      String enabledSortString3 = null;
      String enabledSortString4 = null;
      String disabledSortString0 = null;
      String disabledSortString1 = null;
      String disabledSortString2 = null;
      String disabledSortString3 = null;
      String disabledSortString4 = null;

      Map<Integer, String> enabledSortAttributes = enabledSubject.getSource().getSortAttributes();
      if (enabledSortAttributes.containsKey(0)) {
        enabledSortString0 = enabledSubject.getAttributeValue(enabledSortAttributes.get(0), false);
      }
      
      if (enabledSortAttributes.containsKey(1)) {
        enabledSortString1 = enabledSubject.getAttributeValue(enabledSortAttributes.get(1), false);
      }

      if (enabledSortAttributes.containsKey(2)) {
        enabledSortString2 = enabledSubject.getAttributeValue(enabledSortAttributes.get(2), false);
      }
      
      if (enabledSortAttributes.containsKey(3)) {
        enabledSortString3 = enabledSubject.getAttributeValue(enabledSortAttributes.get(3), false);
      }
      
      if (enabledSortAttributes.containsKey(4)) {
        enabledSortString4 = enabledSubject.getAttributeValue(enabledSortAttributes.get(4), false);
      }
      
      Map<Integer, String> disabledSortAttributes = disabledSubject.getSource().getSortAttributes();
      if (disabledSortAttributes.containsKey(0)) {
        disabledSortString0 = disabledSubject.getAttributeValue(disabledSortAttributes.get(0), false);
      }
      
      if (disabledSortAttributes.containsKey(1)) {
        disabledSortString1 = disabledSubject.getAttributeValue(disabledSortAttributes.get(1), false);
      }

      if (disabledSortAttributes.containsKey(2)) {
        disabledSortString2 = disabledSubject.getAttributeValue(disabledSortAttributes.get(2), false);
      }
  
      if (disabledSortAttributes.containsKey(3)) {
        disabledSortString3 = disabledSubject.getAttributeValue(disabledSortAttributes.get(3), false);
      }
      
      if (disabledSortAttributes.containsKey(4)) {
        disabledSortString4 = disabledSubject.getAttributeValue(disabledSortAttributes.get(4), false);
      }
      
      if (GrouperUtil.equals(enabledSortString0, disabledSortString0)) {
        report.append("<font color='green'>SUCCESS:</font> sortString0 matches (" + GrouperUtil.xmlEscape(enabledSortString0) + ")\n"); 
      } else {
        report.append("<font color='red'>ERROR:</font> sortString0 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSortString0) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSortString0) + "\n"); 
      }
      
      if (GrouperUtil.equals(enabledSortString1, disabledSortString1)) {
        report.append("<font color='green'>SUCCESS:</font> sortString1 matches (" + GrouperUtil.xmlEscape(enabledSortString1) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> sortString1 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSortString1) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSortString1) + "\n");
      }
      
      if (GrouperUtil.equals(enabledSortString2, disabledSortString2)) {
        report.append("<font color='green'>SUCCESS:</font> sortString2 matches (" + GrouperUtil.xmlEscape(enabledSortString2) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> sortString2 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSortString2) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSortString2) + "\n");
      }
      
      if (GrouperUtil.equals(enabledSortString3, disabledSortString3)) {
        report.append("<font color='green'>SUCCESS:</font> sortString3 matches (" + GrouperUtil.xmlEscape(enabledSortString3) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> sortString3 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSortString3) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSortString3) + "\n");
      }
      
      if (GrouperUtil.equals(enabledSortString4, disabledSortString4)) {
        report.append("<font color='green'>SUCCESS:</font> sortString4 matches (" + GrouperUtil.xmlEscape(enabledSortString4) + ")\n");
      } else {
        report.append("<font color='red'>ERROR:</font> sortString4 mismatch, source " + enabledSource.getId() + "=" + GrouperUtil.xmlEscape(enabledSortString4) + ", " + disabledSource.getId() + "=" + GrouperUtil.xmlEscape(disabledSortString4) + "\n");
      }
    }
  }
  
  
  /**
   * show form to add a new subject source
   * @param request
   * @param response
   */
  public void addSubjectSource(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final SubjectSourceContainer subjectSourceContainer = grouperRequestContainer.getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceConfigId = request.getParameter("subjectSourceConfigId");
      
      String subjectSourceConfigType = request.getParameter("subjectSourceConfigType");
      
      if (StringUtils.isNotBlank(subjectSourceConfigType)) {
        
        if (!SubjectSourceConfiguration.sourceConfigClassNames.contains(subjectSourceConfigType)) {            
          throw new RuntimeException("Invalid subjectSourceConfigType "+subjectSourceConfigType);
        }

        Class<SubjectSourceConfiguration> klass = (Class<SubjectSourceConfiguration>) GrouperUtil.forName(subjectSourceConfigType);
        SubjectSourceConfiguration subjectSourceConfiguration = (SubjectSourceConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(subjectSourceConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#subjectSourceConfigId",
              TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigCreateErrorConfigIdRequired")));
          guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("subjectSourceConfigType", ""));
          return;
        }
        
        subjectSourceConfiguration.setConfigId(subjectSourceConfigId);
        
        if (subjectSourceConfiguration.retrieveConfigurationConfigIds().contains(subjectSourceConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#subjectSourceConfigId", TextContainer.retrieveFromRequest().getText().get("grouperConfigurationValidationConfigIdUsed")));
          return;
        }
        
        String previousSubjectSourceConfigId = request.getParameter("previousSubjectSourceConfigId");
        String previousSubjectSourceConfigType = request.getParameter("previousSubjectSourceConfigType");
        if (StringUtils.isBlank(previousSubjectSourceConfigId) 
            || !StringUtils.equals(subjectSourceConfigType, previousSubjectSourceConfigType)) {
          // first time loading the screen or
          // subject source config type changed
          // let's get values from config files/database
        } else {
          subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
        }
        
        GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = GuiSubjectSourceConfiguration.convertFromSubjectSourceConfiguration(subjectSourceConfiguration);
        subjectSourceContainer.setGuiSubjectSourceConfiguration(guiSubjectSourceConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSourceAdd.jsp"));
      
      String focusOnElementName = request.getParameter("focusOnElementName");
      if (!StringUtils.isBlank(focusOnElementName)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$(\"[name='" + focusOnElementName + "']\").focus()"));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new subject source in db
   * @param request
   * @param response
   */
  public void addSubjectSourceSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceConfigId = request.getParameter("subjectSourceConfigId");
      
      String subjectSourceConfigType = request.getParameter("subjectSourceConfigType");
      
      if (StringUtils.isBlank(subjectSourceConfigId)) {
        throw new RuntimeException("subjectSourceConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(subjectSourceConfigType)) {
        throw new RuntimeException("subjectSourceConfigType cannot be blank");
      }
      
      if (!SubjectSourceConfiguration.sourceConfigClassNames.contains(subjectSourceConfigType)) {            
        throw new RuntimeException("Invalid subjectSourceConfigType "+subjectSourceConfigType);
      }
      
      Class<SubjectSourceConfiguration> klass = (Class<SubjectSourceConfiguration>) GrouperUtil.forName(subjectSourceConfigType);
      SubjectSourceConfiguration subjectSourceConfiguration = (SubjectSourceConfiguration) GrouperUtil.newInstance(klass);
      
      subjectSourceConfiguration.setConfigId(subjectSourceConfigId);
      subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      subjectSourceConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      SourceManager.getInstance().reloadSource(subjectSourceConfiguration.retrieveAttributes().get("id").getValueOrExpressionEvaluationValue());
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectSource.viewSubjectSources')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show screen to edit subject source config
   * @param request
   * @param response
   */
  public void editSubjectSource(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceId = request.getParameter("subjectSourceId");
      
      if (StringUtils.isBlank(subjectSourceId)) {
        throw new RuntimeException("subjectSourceId cannot be blank");
      }
      
      SubjectSourceConfiguration subjectSourceConfiguration = null;
      
      List<SubjectSourceConfiguration> subjectSourceConfigurations = SubjectSourceConfiguration.retrieveAllSubjectSourceConfigurations();
      
      for (SubjectSourceConfiguration subjectSourceConfig: subjectSourceConfigurations) {
        GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = subjectSourceConfig.retrieveAttributes().get("id");
        if (grouperConfigurationModuleAttribute != null) {
          String id = grouperConfigurationModuleAttribute.getValueOrExpressionEvaluationValue();
          if (id != null && id.equals(subjectSourceId)) {
            subjectSourceConfiguration = subjectSourceConfig;
            break;
          }
        }
      }
      
      if (subjectSourceConfiguration == null) {
        throw new RuntimeException("Could not find subject source config for source id "+subjectSourceId);
      }
      
      subjectSourceContainer.setSubjectSourceId(subjectSourceId);
      
      String previousSubjectSourceConfigId = request.getParameter("previousSubjectSourceConfigId");
      
      if (StringUtils.isBlank(previousSubjectSourceConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = GuiSubjectSourceConfiguration.convertFromSubjectSourceConfiguration(subjectSourceConfiguration);
        subjectSourceContainer.setGuiSubjectSourceConfiguration(guiSubjectSourceConfiguration);
      } else {
        // change was made on the form
        subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
        GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = GuiSubjectSourceConfiguration.convertFromSubjectSourceConfiguration(subjectSourceConfiguration);
        subjectSourceContainer.setGuiSubjectSourceConfiguration(guiSubjectSourceConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSourceEdit.jsp"));

      String focusOnElementName = request.getParameter("focusOnElementName");
      if (!StringUtils.isBlank(focusOnElementName)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$(\"[name='" + focusOnElementName + "']\").focus()"));
      }

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * edit existing subject source in db
   * @param request
   * @param response
   */
  public void editSubjectSourceSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceConfigId = request.getParameter("subjectSourceConfigId");
      
      String subjectSourceConfigType = request.getParameter("subjectSourceConfigType");
      
      if (StringUtils.isBlank(subjectSourceConfigId)) {
        throw new RuntimeException("subjectSourceConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(subjectSourceConfigType)) {
        throw new RuntimeException("subjectSourceConfigType cannot be blank");
      }
      
      if (!SubjectSourceConfiguration.sourceConfigClassNames.contains(subjectSourceConfigType)) {            
        throw new RuntimeException("Invalid subjectSourceConfigType "+subjectSourceConfigType);
      }
      
      Class<SubjectSourceConfiguration> klass = (Class<SubjectSourceConfiguration>) GrouperUtil.forName(subjectSourceConfigType);
      SubjectSourceConfiguration subjectSourceConfiguration = (SubjectSourceConfiguration) GrouperUtil.newInstance(klass);
      
      subjectSourceConfiguration.setConfigId(subjectSourceConfigId);
      subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

     subjectSourceConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      ExpirableCache.clearAll();
      
      SourceManager.getInstance().reloadSource(subjectSourceConfiguration.retrieveAttributes().get("id").getValueOrExpressionEvaluationValue());
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectSource.viewSubjectSources')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigAddEditSuccess")));
      
      if (actionsPerformed.size() > 0) {

        for (String actionPerformed: actionsPerformed) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, actionPerformed));
        }
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete existing subject source in db
   * @param request
   * @param response
   */
  public void deleteSubjectSource(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceId = request.getParameter("subjectSourceId");
      
      if (StringUtils.isBlank(subjectSourceId)) {
        throw new RuntimeException("subjectSourceId cannot be blank");
      }
      
      SubjectSourceConfiguration subjectSourceConfiguration = null;
      
      List<SubjectSourceConfiguration> subjectSourceConfigurations = SubjectSourceConfiguration.retrieveAllSubjectSourceConfigurations();
      
      for (SubjectSourceConfiguration subjectSourceConfig: subjectSourceConfigurations) {
        GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = subjectSourceConfig.retrieveAttributes().get("id");
        if (grouperConfigurationModuleAttribute != null) {
          String id = grouperConfigurationModuleAttribute.getValueOrExpressionEvaluationValue();
          if (id != null && id.equals(subjectSourceId)) {
            subjectSourceConfiguration = subjectSourceConfig;
            break;
          }
        }
      }
      
      if (subjectSourceConfiguration == null) {
        throw new RuntimeException("Could not find subject source config for source id "+subjectSourceId);
      }
      
      subjectSourceConfiguration.deleteConfig(true);
      
      SourceManager.getInstance().reloadSource(subjectSourceId);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectSource.viewSubjectSources')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigDeleteSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
}
