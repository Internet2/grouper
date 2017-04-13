
/*******************************************************************************
 * Copyright 2017 Internet2
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiInstrumentationDataInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AdminContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
//import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataInstance;
//import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataInstanceCounts;
//import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataInstanceFinder;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * main logic for ui
 */
public class UiV2Admin extends UiServiceLogicBase {

  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(UiV2Admin.class);
  
  /**
   * show screen or subject API diagnostics
   * @param request
   * @param response
   */
  public void subjectApiDiagnostics(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      //if the user allowed
      if (!subjectApiDiagnosticsAllowed()) {
        return;
      }
      
      //just show a jsp
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/admin/adminSubjectApiDiagnostics.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show instrumentation screen
   * @param request
   * @param response
   */
  public void instrumentation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      //if the user is allowed
      if (!instrumentationAllowed()) {
        return;
      }

      AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
      
      List<GuiInstrumentationDataInstance> guiInstances = new ArrayList<GuiInstrumentationDataInstance>();
      
      if (StringUtils.isEmpty(request.getParameter("instanceId"))) {
//        List<InstrumentationDataInstance> instances = InstrumentationDataInstanceFinder.findAll(true);
//        for (InstrumentationDataInstance instance : GrouperUtil.nonNull(instances)) {
//          GuiInstrumentationDataInstance guiInstance = new GuiInstrumentationDataInstance(instance);
//          guiInstances.add(guiInstance);
//        }
//        
//        String filterDate = !StringUtils.isEmpty(request.getParameter("filterDate")) ? request.getParameter("filterDate") : null;
//        long increment = StringUtils.isEmpty(filterDate) ? 86400000 : 3600000;
//        
//        adminContainer.setGuiInstrumentationDataInstances(guiInstances);
//        adminContainer.setGuiInstrumentationFilterDate(filterDate);
//        instrumentationGraphResultsHelper(instances, increment, filterDate);
//
//        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/admin/adminInstrumentation.jsp"));
      } else {
//        InstrumentationDataInstance instance = InstrumentationDataInstanceFinder.findById(request.getParameter("instanceId"), true, true);
//        GuiInstrumentationDataInstance guiInstance = new GuiInstrumentationDataInstance(instance);
//        guiInstances.add(guiInstance);
//
//        String filterDate = !StringUtils.isEmpty(request.getParameter("filterDate")) ? request.getParameter("filterDate") : null;
//        long increment = StringUtils.isEmpty(filterDate) ? 86400000 : 3600000;
//        
//        adminContainer.setGuiInstrumentationDataInstances(guiInstances);
//        adminContainer.setGuiInstrumentationFilterDate(filterDate);
//        instrumentationGraphResultsHelper(GrouperUtil.toList(instance), increment, filterDate);
//
//        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/admin/adminInstrumentationInstance.jsp"));
      }            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * 
   */
//  private void instrumentationGraphResultsHelper(List<InstrumentationDataInstance> instances, long displayIncrement, String filterDate) {
//    
//    Map<String, Map<String, Long>> formattedData = new TreeMap<String, Map<String, Long>>();
//    Set<String> daysWithData = new TreeSet<String>();
//      
//    long firstTime = 9999999999999L;
//    long lastTime = 0L;
//    
//    SimpleDateFormat sdfDateTimeUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    sdfDateTimeUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
//    SimpleDateFormat sdfDateUTC = new SimpleDateFormat("yyyy-MM-dd");
//    sdfDateUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
//    
//    Map<String, Set<Long>> allStartTimesByType = new HashMap<String, Set<Long>>();
//
//    for (InstrumentationDataInstance instance : instances) {
//      List<InstrumentationDataInstanceCounts> instanceCountsList = instance.getCounts();
//        
//      for (InstrumentationDataInstanceCounts instanceCounts : instanceCountsList) {
//        for (String type : instanceCounts.getCounts().keySet()) {
//          Long count = instanceCounts.getCounts().get(type);
//          
//          long startTime = (instanceCounts.getStartTime().getTime() / displayIncrement) * displayIncrement;
//          
//          String formattedDateTimeUTC = sdfDateTimeUTC.format(new Date(startTime));
//          String formattedDateUTC = sdfDateUTC.format(new Date(startTime));
//          daysWithData.add(formattedDateUTC);
//          
//          if (StringUtils.isEmpty(filterDate) || filterDate.equals(formattedDateUTC)) {
//            if (formattedData.get(type) == null) {
//              formattedData.put(type, new TreeMap<String, Long>());
//              allStartTimesByType.put(type, new HashSet<Long>());
//            }
//            
//            if (formattedData.get(type).get(formattedDateTimeUTC) == null) {
//              formattedData.get(type).put(formattedDateTimeUTC, 0L);
//              
//              firstTime = Math.min(startTime, firstTime);
//              lastTime = Math.max(startTime, lastTime);
//              allStartTimesByType.get(type).add(startTime);
//            }
//            
//            formattedData.get(type).put(formattedDateTimeUTC, formattedData.get(type).get(formattedDateTimeUTC) + count);
//          }
//        }
//      }
//    }
//    
//    // fill in any gaps with 0s
//    for (String type : formattedData.keySet()) {
//      for (long i = firstTime; i <= lastTime; i = i + displayIncrement) {
//        if (!allStartTimesByType.get(type).contains(i)) {
//          String formattedDateUTC = sdfDateTimeUTC.format(new Date(i));
//          formattedData.get(type).put(formattedDateUTC, 0L);
//        }
//      }
//    }
//    
//    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
//    adminContainer.setGuiInstrumentationGraphResults(formattedData);
//    adminContainer.setGuiInstrumentationDaysWithData(daysWithData);
//  }

  /**
   * when the source id changes fill in subject ids etc
   * @param request
   * @param response
   */
  public void subjectApiDiagnosticsSourceIdChanged(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      //if the user allowed
      if (!subjectApiDiagnosticsAllowed()) {
        return;
      }

      final String sourceId = request.getParameter("subjectApiSourceIdName");

      String subjectId = null;
      String subjectIdentifier = null;
      String searchString = null;
      
      if (!StringUtils.isBlank(sourceId)) {
        
        Source source = SourceManager.getInstance().getSource(sourceId);
        
        if (source == null) {
          throw new RuntimeException("Cant find source by id: '" + sourceId + "'");
        }
        
        subjectId = source.getInitParam("subjectIdToFindOnCheckConfig");
        subjectId = StringUtils.defaultIfBlank(subjectId, "someSubjectId");

        subjectIdentifier = source.getInitParam("subjectIdentifierToFindOnCheckConfig");
        subjectIdentifier = StringUtils.defaultIfBlank(subjectIdentifier, "someSubjectIdentifier");

        searchString = source.getInitParam("stringToFindOnCheckConfig");
        searchString = StringUtils.defaultIfBlank(searchString, "first last");
      }
      
      // change the textfields
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("subjectIdName", subjectId));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("subjectIdentifierName", subjectIdentifier));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("searchStringName", searchString));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * combo filter
   * @param request
   * @param response
   */
  public void subjectApiDiagnosticsActAsCombo(HttpServletRequest request, HttpServletResponse response) {

    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Subject>() {

      /**
       */
      @Override
      public Subject lookup(HttpServletRequest localRequest, GrouperSession grouperSessionPrevious, final String query) {

        //when we refer to subjects in the dropdown, we will use a sourceId / subject tuple
        
        return (Subject)GrouperSession.callbackGrouperSession(grouperSessionPrevious.internal_getRootSession(), new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            Subject subject = null;
            
            if (query != null && query.contains("||")) {
              String sourceId = GrouperUtil.prefixOrSuffix(query, "||", true);
              String subjectId = GrouperUtil.prefixOrSuffix(query, "||", false);
              subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
            } else {
              subject = SubjectFinder.findByIdOrIdentifierAndSource(query, SubjectHelper.nonGroupSources(), false);
            }
            
            return subject;
          }
        });
      

      }

      /**
       * 
       */
      @SuppressWarnings("unchecked")
      @Override
      public Collection<Subject> search(final HttpServletRequest localRequest, 
          final GrouperSession grouperSessionPrevious, final String query) {
        
        return (Collection<Subject>)GrouperSession.callbackGrouperSession(grouperSessionPrevious.internal_getRootSession(), new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            Collection<Subject> results = 
                SubjectFinder.findPage(query, SubjectHelper.nonGroupSources()).getResults();
            return results;
          }
        });
      
      }

      /**
       * 
       * @param t
       * @return source with id
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Subject t) {
        return t.getSourceId() + "||" + t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Subject t) {
        return new GuiSubject(t).getScreenLabelLong();
      }

      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Subject t) {
        String value = new GuiSubject(t).getScreenLabelLongWithIcon();
        return value;
      }

      /**
       * 
       */
      @Override
      public String initialValidationError(HttpServletRequest localRequest, GrouperSession grouperSession) {

        //MCH 20140316
        //Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
        //
        //if (group == null) {
        //  
        //  return "Not allowed to edit group";
        //}
        //
        return null;
      }
    });

              
  }
  
  /**
   * 
   * @return true if ok, false if not allowed
   */
  private boolean subjectApiDiagnosticsAllowed() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
    
    //if the user allowed
    if (!adminContainer.isSubjectApiDiagnosticsShow()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("adminSubjectApiDiagnosticsErrorNotAllowed")));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      return false;
    }
    return true;

  }
  
  /**
   * 
   * @return true if ok, false if not allowed
   */
  private boolean instrumentationAllowed() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
    
    //if the user allowed
    if (!adminContainer.isInstrumentationShow()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("adminInstrumentationErrorNotAllowed")));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      return false;
    }
    return true;

  }
  
  /**
   * run
   * @param request
   * @param response
   */
  public void subjectApiDiagnosticsRun(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GrouperSession grouperSession = null;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      //if the user allowed
      if (!subjectApiDiagnosticsAllowed()) {
        return;
      }
      
      Subject actAsSubject = null;
      { 
        String actAsComboName = request.getParameter("actAsComboName");
        if (!StringUtils.isBlank(actAsComboName)) {
          GrouperSession.stopQuietly(grouperSession);
          grouperSession = GrouperSession.startRootSession();
          try {
            if (actAsComboName != null && actAsComboName.contains("||")) {
              String theSourceId = GrouperUtil.prefixOrSuffix(actAsComboName, "||", true);
              String theSubjectId = GrouperUtil.prefixOrSuffix(actAsComboName, "||", false);
              actAsSubject =  SubjectFinder.findByIdOrIdentifierAndSource(theSubjectId, theSourceId, true);
            } else {
              actAsSubject = SubjectFinder.findByIdOrIdentifierAndSource(actAsComboName, SubjectHelper.nonGroupSources(), true);
            }
            
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
        }
      }
      
      //if there is an act as, use that, otherwise logged in
      grouperSession = GrouperSession.start(GrouperUtil.defaultIfNull(actAsSubject, loggedInSubject));
      
      StringBuilder subjectApiReport = new StringBuilder();
      
      subjectApiReport.append("<pre>\n");
      
      String sourceId = request.getParameter("subjectApiSourceIdName");
      String subjectId = request.getParameter("subjectIdName");
      String subjectIdentifier = request.getParameter("subjectIdentifierName");
      String searchString = request.getParameter("searchStringName");
      
      Subject theSubject = null;
      
      Set<Subject> subjectsSearch = null;
      Set<Subject> subjectsPage = null;
      
      if (StringUtils.isBlank(sourceId)) {
        
        subjectApiReport.append("<font color='red'>ERROR:</font> No source ID specified\n");
        
      } else {
        Source source = SourceManager.getInstance().getSource(sourceId);
        
        if (source == null) {
          throw new RuntimeException("Cant find source by id: '" + sourceId + "'");
        }

        {
          long now = System.nanoTime();
          Exception exception = null;
          Subject subject = null;
          try {
            subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
            theSubject = subject;
          } catch (Exception e) {
            exception = e;
          }
          long millis = (System.nanoTime() - now) / 1000000L;
          if (subject != null) {
            subjectApiReport.append("<font color='green'>SUCCESS:</font> Found subject by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdAndSource(\"" + GrouperUtil.xmlEscape(subjectId) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
            if (StringUtils.equals(subjectId, subject.getId())) {
              subjectApiReport.append("<font color='green'>SUCCESS:</font> Subject id in returned subject matches the subject id searched for: '" + GrouperUtil.xmlEscape(subjectId) + "'\n");
            } else {
              subjectApiReport.append("<font color='red'>ERROR:</font> Subject id in returned subject '" + GrouperUtil.xmlEscape(subject.getId()) + "'does not match the subject id searched for: '" + GrouperUtil.xmlEscape(subjectId) + "'\n");
            }
          } else if (exception == null) {
            subjectApiReport.append("<font color='orange'>WARNING:</font> No subject found by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdAndSource(\"" + GrouperUtil.xmlEscape(subjectId) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
          } else {
            subjectApiReport.append("<font color='red'>ERROR:</font> Exception thrown when finding subject by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdAndSource(\"" + GrouperUtil.xmlEscape(subjectId) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
            subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
          }
        }        
        
        {
          long now = System.nanoTime();
          Exception exception = null;
          Subject subject = null;
          try {
            subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, false);
            if (theSubject == null) {
              theSubject = subject;
            }
          } catch (Exception e) {
            exception = e;
          }
          long millis = (System.nanoTime() - now) / 1000000L;
          if (subject != null) {
            subjectApiReport.append("<font color='green'>SUCCESS:</font> Found subject by identifier in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectIdentifier) + "'\n         with SubjectFinder.findByIdentifierAndSource(\"" + GrouperUtil.xmlEscape(subjectIdentifier) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
          } else if (exception == null) {
            subjectApiReport.append("<font color='orange'>WARNING:</font> No subject found by identifier in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectIdentifier) + "'\n         with SubjectFinder.findByIdentifierAndSource(\"" + GrouperUtil.xmlEscape(subjectIdentifier) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
          } else {
            subjectApiReport.append("<font color='red'>ERROR:</font> Exception thrown when finding subject by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdentifierAndSource(\"" + GrouperUtil.xmlEscape(subjectIdentifier) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
            subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
          }
        }        
        
        {
          long now = System.nanoTime();
          Exception exception = null;
          Set<Subject> subjects = null;
          try {
            subjects = SubjectFinder.findAll(searchString, sourceId);
            subjectsSearch = subjects;
            if (theSubject == null && GrouperUtil.length(subjects) > 0) {
              theSubject = subjects.iterator().next();
            }
          } catch (Exception e) {
            exception = e;
          }
          long millis = (System.nanoTime() - now) / 1000000L;
          if (GrouperUtil.length(subjects) > 0) {
            subjectApiReport.append("<font color='green'>SUCCESS:</font> Found " + GrouperUtil.length(subjects) + " subjects by search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findAll(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
          } else if (exception == null) {
            subjectApiReport.append("<font color='orange'>WARNING:</font> Found no subjects by search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findAll(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
          } else {
            subjectApiReport.append("<font color='red'>ERROR:</font> Exception finding subjects by search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findAll(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
            subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
          }
        }        
        
        {
          long now = System.nanoTime();
          Exception exception = null;
          Set<Subject> subjects = null;
          SearchPageResult searchPageResult = null;
          try {
            searchPageResult = SubjectFinder.findPage(searchString, sourceId);
            if (searchPageResult != null) { 
              subjects = searchPageResult.getResults();
              subjectsPage = subjects;
              if (theSubject == null && GrouperUtil.length(subjects) > 0) {
                theSubject = subjects.iterator().next();
              }
            }
          } catch (Exception e) {
            exception = e;
          }
          long millis = (System.nanoTime() - now) / 1000000L;
          if (GrouperUtil.length(subjects) > 0) {
            subjectApiReport.append("<font color='green'>SUCCESS:</font> Found " + GrouperUtil.length(subjects) + " subjects by paged search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findPage(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
          } else if (exception == null) {
            subjectApiReport.append("<font color='orange'>WARNING:</font> Found no subjects by paged search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findPage(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
          } else {
            subjectApiReport.append("<font color='red'>ERROR:</font> Exception finding subjects by paged search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findPage(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
            subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
          }
        }       
        
        subjectApiReport.append("\n######## SUBJECT ATTRIBUTES ########\n\n");
        
        if (theSubject == null) {
          subjectApiReport.append("<font color='red'>ERROR:</font> Cannot list attributes of a subject if cannot find any subjects\n");
        } else {
          subjectApiReport.append("Subject id: '" + GrouperUtil.xmlEscape(theSubject.getId()) + "' with subject.getId()\n");
          subjectApiReport.append("  - the subject id should be an unchanging opaque identifier\n");
          subjectApiReport.append("  - the subject id is stored in the grouper_members table\n");
          subjectApiReport.append("Subject name: '" + GrouperUtil.xmlEscape(theSubject.getName()) + "' with subject.getName()\n");
          subjectApiReport.append("  - the subject name is generally first last\n");
          subjectApiReport.append("Subject description: '" + GrouperUtil.xmlEscape(theSubject.getDescription()) + "' with subject.getDescription()\n");
          subjectApiReport.append("  - the subject description can have more info such as the id, name, dept, etc\n");
          subjectApiReport.append("Subject type: '" + GrouperUtil.xmlEscape(theSubject.getTypeName()) + "' with subject.getTypeName()\n");
          subjectApiReport.append("  - the subject type is not really used\n");
          Map<String, Set<String>> attributes = theSubject.getAttributes(true);
          if (attributes != null) {
            for (String attributeName : attributes.keySet()) {
              Set<String> values = attributes.get(attributeName);
              if (GrouperUtil.length(values) == 1) {
                subjectApiReport.append("Subject attribute '" + GrouperUtil.xmlEscape(attributeName) + "' has 1 value: '" + GrouperUtil.xmlEscape(theSubject.getAttributeValue(attributeName)) + "'\n  - with subject.getAttributeValue(\"" + GrouperUtil.xmlEscape(attributeName) + "\")\n");
                
              } else if (GrouperUtil.length(values) > 1) {
                subjectApiReport.append("Subject attribute '" + GrouperUtil.xmlEscape(attributeName) + "' has " + GrouperUtil.length(values) + " value: '" + GrouperUtil.xmlEscape(theSubject.getAttributeValueOrCommaSeparated(attributeName)) + "'\n  - with subject.getAttributeValues(\"" + GrouperUtil.xmlEscape(attributeName) + "\")\n");
              } else {
                subjectApiReport.append("Subject attribute '" + GrouperUtil.xmlEscape(attributeName) + "' has no value\n  - with subject.getAttributeValue(\"" + GrouperUtil.xmlEscape(attributeName) + "\")\n");
              }
            }
          }
        }

        subjectApiReport.append("\n######## SUBJECT IN UI ########\n\n");
        {
          if (theSubject == null) {
            subjectApiReport.append("<font color='red'>ERROR:</font> Cannot show subject UI view if cannot find any subjects\n");
          } else {

            if (StringUtils.equals(GrouperSourceAdapter.groupSourceId(), theSubject.getSourceId())) {
              GuiGroup guiGroup = new GuiGroup(((GrouperSubject)theSubject).internal_getGroup());
              subjectApiReport.append("Short link with icon: " + guiGroup.getShortLinkWithIcon() + "\n");
              subjectApiReport.append("  - This is configured in grouper.text.en.us.base.properties with guiGroupShortLink\n");
              subjectApiReport.append("  - By default this is the display extension with a tooltip for path and description\n");
              
              subjectApiReport.append("Link with icon: " + guiGroup.getLinkWithIcon() + "\n");
              subjectApiReport.append("  - This is configured in grouper.text.en.us.base.properties with guiGroupLink\n");
              subjectApiReport.append("  - By default this is the display name with a tooltip for path and description\n");
              
            } else {
              GuiSubject guiSubject = new GuiSubject(theSubject);
              subjectApiReport.append("Short link with icon: " + guiSubject.getShortLinkWithIcon() + "\n");
              subjectApiReport.append("  - This is configured in grouper.text.en.us.base.properties with guiSubjectShortLink\n");
              subjectApiReport.append("  - Also configured in grouper-ui.properties with grouperUi.screenLabel2.sourceId.X\n");
              subjectApiReport.append("  - By default this is the name of the subject with a tooltip for description\n");
              
              subjectApiReport.append("Long label with icon: " + guiSubject.getScreenLabelLongWithIcon() + "\n");
              subjectApiReport.append("  - This is not used in the new UI\n");
              subjectApiReport.append("  - It is configured in grouper-ui.properties with grouperUi.subjectImg.screenEl.\n");
              subjectApiReport.append("  - By default this is the description of the subject\n");
              
            }
          }
          
        }

        
        subjectApiReport.append("\n######## SUBJECT IN WS ########\n\n");
        {
          if (theSubject == null) {
            subjectApiReport.append("<font color='red'>ERROR:</font> Cannot show subject WS view if cannot find any subjects\n");
          } else {
            subjectApiReport.append("Look in grouper-ws.properties to see how the WS uses subjects.  This is the default configuation:\n\n");
            subjectApiReport.append("# subject attribute names to send back when a WsSubjectResult is sent, comma separated\n");
            subjectApiReport.append("# e.g. name, netid\n");
            subjectApiReport.append("# default is none\n");
            subjectApiReport.append("ws.subject.result.attribute.names = \n\n");
            subjectApiReport.append("# subject result attribute names when extended data is requested (comma separated)\n");
            subjectApiReport.append("# default is name, description\n");
            subjectApiReport.append("# note, these will be in addition to ws.subject.result.attribute.names\n");
            subjectApiReport.append("ws.subject.result.detail.attribute.names = \n");

          }
          
        }

        subjectApiReport.append("\n######## SOURCE CONFIGURATION ########\n\n");
        
        {
          String sourceConfigId = null;
          Pattern sourceIdConfigPattern = Pattern.compile("^subjectApi\\.source\\.([^.]+)\\.id$");
          
          for (String configName : SubjectConfig.retrieveConfig().propertyNames()) {
            
            //  # generally the <configName> is the same as the source id.  Generally this should not have special characters
            //  # subjectApi.source.<configName>.id = sourceId

            Matcher matcher = sourceIdConfigPattern.matcher(configName);
            if (matcher.matches()) {
              sourceConfigId = matcher.group(1);
              if (StringUtils.equals(sourceId, SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".id"))) {
                break;
              }
            }
          }
          
          if (StringUtils.isBlank(sourceConfigId)) {

            subjectApiReport.append("<font color='red'>ERROR:</font> Cannot find source in subject.properties\n");
            
          } else {

            {
            
              String adapterClassName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".adapterClass");
              subjectApiReport.append("Adapter class: '" + GrouperUtil.xmlEscape(adapterClassName) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".adapterClass\n");
              
              Class<?> adapterClassClass = null;
              
              try {
                adapterClassClass = SubjectUtils.forName(adapterClassName);
                subjectApiReport.append("<font color='green'>SUCCESS:</font> Found adapter class\n");
                try {
                  SubjectUtils.newInstance(adapterClassClass);
                  subjectApiReport.append("<font color='green'>SUCCESS:</font> Instantiated adapter class\n");
                } catch (Exception e) {
                  subjectApiReport.append("<font color='red'>ERROR:</font> Cannot instantiate adapter class\n");
                }                
              } catch (Exception e) {
                subjectApiReport.append("<font color='red'>ERROR:</font> Cannot find adapter class\n");
              }
  
            }              
            
            //  # generally the <configName> is the same as the source id.  Generally this should not have special characters
            //  # subjectApi.source.<configName>.id = sourceId
            subjectApiReport.append("Source id: '" + GrouperUtil.xmlEscape(sourceId) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".id\n");
            
            //  # this is a friendly name for the source
            //  # subjectApi.source.<configName>.name = sourceName
            subjectApiReport.append("Source name: '" + GrouperUtil.xmlEscape(source.getName()) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".name\n");

            //  # type is not used all that much 
            //  # subjectApi.source.<configName>.types = person, application
            subjectApiReport.append("Source types: '" + GrouperUtil.xmlEscape(SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".types")) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".types\n");

            {
              Pattern paramValueConfigPattern = Pattern.compile("^subjectApi\\.source\\.[^.]+\\.param\\.([^.]+)\\.value$");
              
              //params (note, name is optional and generally not there)
              //  # subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value = true
              for (String paramValueKey : SubjectConfig.retrieveConfig().propertyNames()) {
                
                if (paramValueKey.startsWith("subjectApi.source." + sourceConfigId + ".param") 
                    && paramValueKey.endsWith(".value") ) {
                  String paramValue = SubjectConfig.retrieveConfig().propertyValueString(paramValueKey);
                  Matcher paramValueMatcher = paramValueConfigPattern.matcher(paramValueKey);
                  paramValueMatcher.matches();
                  String paramConfigId = paramValueMatcher.group(1);
                  String paramName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".param." + paramConfigId + ".name");
                  boolean hasParamName = true;
                  if (StringUtils.isBlank(paramName)) {
                    paramName = paramConfigId;
                    hasParamName = false;
                  }
                  subjectApiReport.append("Source param name: '" + GrouperUtil.xmlEscape(paramName) + "' has value: '");
                  if (paramName.toLowerCase().contains("pass") || paramName.toLowerCase().contains("cred") || paramName.toLowerCase().contains("pwd")) {
                    subjectApiReport.append("*******'\n");
                  } else {
                    subjectApiReport.append(GrouperUtil.xmlEscape(paramValue) + "'\n");
                  }
                  if (hasParamName) {
                    subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".param." + paramConfigId + ".name\n");
                  }
                  subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".param." + paramConfigId + ".value\n");
                }
              }
            }
            
            {

              //  # internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
              //  # subjectApi.source.<configName>.internalAttributes = someName, anotherName
              String internalAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceId + ".internalAttributes");
              if (StringUtils.isBlank(internalAttributes)) {
                subjectApiReport.append("No internal attributes configured\n");
              } else {
                subjectApiReport.append("Internal attributes: '" + GrouperUtil.xmlEscape(internalAttributes) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".internalAttributes\n");
              }
            }
  
            {
              //  # attributes from ldap object to become subject attributes.  comma separated
              //  # subjectApi.source.<configName>.attributes = cn, sn, uid, department, exampleEduRegId
              String attributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceId + ".attributes");
              if (StringUtils.isBlank(attributes)) {
                subjectApiReport.append("No attributes configured\n");
              } else {
                subjectApiReport.append("Attributes: '" + GrouperUtil.xmlEscape(attributes) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".attributes\n");
              }
            }
  
            //  digester.addObjectCreate("sources/source/search",
            //      "edu.internet2.middleware.subject.provider.Search");
            //  digester.addCallMethod("sources/source/search/searchType", "setSearchType", 0);
            //  digester.addCallMethod("sources/source/search/param", "addParam", 2);
            //  digester.addCallParam("sources/source/search/param/param-name", 0);
            //  digester.addCallParam("sources/source/search/param/param-value", 1);
            //  digester.addSetNext("sources/source/search", "loadSearch");
  
            //  # searchTypes are: 
            //  #   searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.  Each subject has one and only on ID.  Returns one result when searching for one ID.
            //  #   searchSubjectByIdentifier: find a subject by identifier.  Identifier is anything that uniquely identifies the user, e.g. jsmith or jsmith@institution.edu.  
            //  #        Subjects can have multiple identifiers.  Note: it is nice to have if identifiers are unique even across sources.  Returns one result when searching for one identifier.
            //  #   search: find subjects by free form search.  Returns multiple results.
            //  # subjectApi.source.<configName>.search.<searchType>.param.<paramName>.value = something
            {
              Pattern searchParamValueConfigPattern = Pattern.compile("^subjectApi\\.source\\.[^.]+\\.search\\.[^.]+\\.param\\.([^.]+)\\.value$");
              //params (note, name is optional and generally not there)
              //  # subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value = true
              for (String searchType : new String[] {"searchSubject", "searchSubjectByIdentifier", "search"}) {
                                
                for (String paramValueKey : SubjectConfig.retrieveConfig().propertyNames()) {
                  
                  //all search params has a value
                  if (paramValueKey.startsWith("subjectApi.source." + sourceId + ".search." + searchType + ".param.") 
                      && paramValueKey.endsWith(".value") ) {
                    String paramValue = SubjectConfig.retrieveConfig().propertyValueString(paramValueKey);
                    Matcher paramValueMatcher = searchParamValueConfigPattern.matcher(paramValueKey);
                    paramValueMatcher.matches();
                    String paramConfigId = paramValueMatcher.group(1);
                    String paramName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceId + ".search." + searchType + ".param." + paramConfigId + ".name");
                    boolean hasParamName = true;

                    //if name is not specified used the config id (most arent specified)
                    if (StringUtils.isBlank(paramName)) {
                      paramName = paramConfigId;
                      hasParamName = false;
                    }

                    subjectApiReport.append("Search '" + searchType + "' param name: '" + GrouperUtil.xmlEscape(paramName) + "' has value: '");
                    subjectApiReport.append(GrouperUtil.xmlEscape(paramValue) + "'\n");
                    
                    if (hasParamName) {
                      subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".search." + searchType + ".param." + paramConfigId + ".name\n");
                    }
                    subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".search." + searchType + ".param." + paramConfigId + ".value\n");

                  }
                }
                
              }
            }
          }
        }
      }
      
      subjectApiReport.append("\n######## SUBJECT SEARCH RESULTS ########\n\n");
      if (GrouperUtil.length(subjectsSearch) == 0) {
        
        subjectApiReport.append("No subjects found in search\n");
        
      } else {
        int subjectCount = 0;
        for (Subject subject : subjectsSearch) {
          if (subjectCount >= 99) {
            subjectApiReport.append("Only first 100 subjects displayed... of " + GrouperUtil.length(subjectsSearch) + "\n");
            break;
          }
          subjectApiReport.append("Subject " + subjectCount +  ": id: " + GrouperUtil.xmlEscape(subject.getId()) 
              + ", name: " + GrouperUtil.xmlEscape(subject.getName()) + "\n  - description: " + GrouperUtil.xmlEscape(subject.getDescription()) + "\n");
          subjectCount++;
        }
      }
      
      subjectApiReport.append("\n######## SUBJECT PAGE RESULTS ########\n\n");
      if (GrouperUtil.length(subjectsPage) == 0) {
        
        subjectApiReport.append("No subjects found in search page\n");
        
      } else {
        int subjectCount = 0;
        for (Subject subject : subjectsPage) {
          if (subjectCount >= 99) {
            subjectApiReport.append("Only first 100 subjects displayed... of " + GrouperUtil.length(subjectsPage) + "\n");
            break;
          }
          subjectApiReport.append("Subject " + subjectCount +  ": id: " + GrouperUtil.xmlEscape(subject.getId()) 
              + ", name: " + GrouperUtil.xmlEscape(subject.getName()) + "\n  - description: " + GrouperUtil.xmlEscape(subject.getDescription()) + "\n");
          subjectCount++;
        }
      }
      
      
      subjectApiReport.append("</pre>");
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#subjectApiDiagnosticsResultsId", subjectApiReport.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
}
