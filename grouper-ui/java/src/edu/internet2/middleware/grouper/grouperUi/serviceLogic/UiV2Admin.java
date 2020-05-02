
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
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
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDaemonJob;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiInstrumentationDataInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AdminContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperLoaderContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataInstance;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataInstanceCounts;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataInstanceFinder;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * main logic for ui
 */
public class UiV2Admin extends UiServiceLogicBase {

  /** logger */
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
        List<InstrumentationDataInstance> instances = InstrumentationDataInstanceFinder.findAll(true);
        for (InstrumentationDataInstance instance : GrouperUtil.nonNull(instances)) {
          GuiInstrumentationDataInstance guiInstance = new GuiInstrumentationDataInstance(instance);
          guiInstances.add(guiInstance);
        }
        
        String filterDate = !StringUtils.isEmpty(request.getParameter("filterDate")) ? request.getParameter("filterDate") : null;
        long increment = StringUtils.isEmpty(filterDate) ? 86400000 : 3600000;
        
        adminContainer.setGuiInstrumentationDataInstances(guiInstances);
        adminContainer.setGuiInstrumentationFilterDate(filterDate);
        instrumentationGraphResultsHelper(instances, increment, filterDate);

        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/admin/adminInstrumentation.jsp"));
      } else {
        InstrumentationDataInstance instance = InstrumentationDataInstanceFinder.findById(request.getParameter("instanceId"), true, true);
        GuiInstrumentationDataInstance guiInstance = new GuiInstrumentationDataInstance(instance);
        guiInstances.add(guiInstance);

        String filterDate = !StringUtils.isEmpty(request.getParameter("filterDate")) ? request.getParameter("filterDate") : null;
        long increment = StringUtils.isEmpty(filterDate) ? 86400000 : 3600000;
        
        adminContainer.setGuiInstrumentationDataInstances(guiInstances);
        adminContainer.setGuiInstrumentationFilterDate(filterDate);
        instrumentationGraphResultsHelper(GrouperUtil.toList(instance), increment, filterDate);

        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/admin/adminInstrumentationInstance.jsp"));
      }            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * schedule jobs
   * @param request
   * @param response
   */
  public void daemonJobsSchedule(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //if the user is allowed
      if (!daemonJobsAllowed()) {
        return;
      }

      int changesMade = GrouperLoader.scheduleJobs();
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer().setScheduleChanges(changesMade);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("adminJobScheduleSuccess")));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * daemon jobs
   * @param request
   * @param response
   */
  public void daemonJobs(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      if (!daemonJobsHelper(request, response)) {
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/admin/adminDaemonJobs.jsp"));          
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#daemonJobsResultsId", "/WEB-INF/grouperUi2/admin/adminDaemonJobsContents.jsp"));

  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  

  /**
   * daemon jobs reset button
   * @param request
   * @param response
   */
  public void daemonJobsReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("daemonJobsFilter", ""));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("daemonJobsFilterShowExtendedResults", ""));
      
      //get the unfiltered jobs
      if (!daemonJobsHelper(request, response)) {
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#daemonJobsResultsId", "/WEB-INF/grouperUi2/admin/adminDaemonJobsContents.jsp"));

      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * daemon jobs
   * @param request
   * @param response
   */
  public void daemonJobsSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!daemonJobsHelper(request, response)) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#daemonJobsResultsId", "/WEB-INF/grouperUi2/admin/adminDaemonJobsContents.jsp"));

  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show daemon jobs screen
   * @param request
   * @param response
   * @return true if ok, false if not allowed
   */
  private boolean daemonJobsHelper(HttpServletRequest request, HttpServletResponse response) {
        
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {      
      //if the user is allowed
      if (!daemonJobsAllowed()) {
        return false;
      }
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      
      {
        String action = request.getParameter("action");
        String jobName = request.getParameter("jobName");
        if (!StringUtils.isEmpty(action) && !StringUtils.isEmpty(jobName)) {
          JobKey jobKey = new JobKey(jobName);
          if ("runNow".equals(action)) {
            scheduler.triggerJob(jobKey);
          } else if ("disable".equals(action)) {
            scheduler.pauseJob(jobKey);
          } else if ("enable".equals(action)) {
            scheduler.resumeJob(jobKey);
          } else {
            throw new RuntimeException("Unexpected action: " + action);
          }
        }
      }


      AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
      
      List<GuiDaemonJob> guiDaemonJobs = new ArrayList<GuiDaemonJob>();
                  
      String daemonJobsFilter = StringUtils.trimToEmpty(request.getParameter("daemonJobsFilter"));
      adminContainer.setDaemonJobsFilter(daemonJobsFilter);
      
      String daemonJobsCommonFilter = StringUtils.trimToEmpty(request.getParameter("daemonJobsCommonFilter"));
      adminContainer.setDaemonJobsCommonFilter(daemonJobsCommonFilter);
      
      String showExtendedResults = request.getParameter("daemonJobsFilterShowExtendedResults");
      adminContainer.setDaemonJobsShowExtendedResults(StringUtils.equals(showExtendedResults, "on"));

      String daemonJobsFilterShowOnlyErrors = request.getParameter("daemonJobsFilterShowOnlyErrors");
      adminContainer.setDaemonJobsShowOnlyErrors(StringUtils.equals(daemonJobsFilterShowOnlyErrors, "on"));
      
      Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());

      List<String> allJobNamesAfterFilter = new ArrayList<String>();
      for (JobKey jobKey : jobKeys) {
        String jobName = jobKey.getName();
        Boolean shouldAdd = null;
        if (!StringUtils.isBlank(daemonJobsFilter)) {
          shouldAdd = jobName.toLowerCase().contains(daemonJobsFilter.toLowerCase());
        }
        // not a false yet
        if (shouldAdd == null || shouldAdd) {
          if (!StringUtils.isBlank(daemonJobsCommonFilter)) {
            if (StringUtils.equals("INTERNAL_LOADER", daemonJobsCommonFilter)) {
              shouldAdd = jobName.toLowerCase().contains("sql_simple_")
                  || jobName.toLowerCase().contains("sql_group_list_")
                  || jobName.toLowerCase().contains("ldap_simple_")
                  || jobName.toLowerCase().contains("ldap_group_list_")
                  || jobName.toLowerCase().contains("ldap_groups_from_attributes_");
            } else {
              shouldAdd = jobName.toLowerCase().contains(daemonJobsCommonFilter.toLowerCase());
            }
          }
        }
      
        if (shouldAdd == null || shouldAdd) {
          allJobNamesAfterFilter.add(jobName);
        }
      }

      Collections.sort(allJobNamesAfterFilter);
      
      if (adminContainer.isDaemonJobsShowOnlyErrors()) {
        
        // i guess get all, and filter from there
        for (String jobName : allJobNamesAfterFilter) {
          
          GuiDaemonJob guiDaemonJob = new GuiDaemonJob(jobName);
          
          if ((guiDaemonJob.getOverallStatus() != null && guiDaemonJob.getOverallStatus().toLowerCase().contains("error"))
              || (guiDaemonJob.getLastRunStatus() != null && guiDaemonJob.getLastRunStatus().toLowerCase().contains("error"))
              ){
            guiDaemonJobs.add(guiDaemonJob);
          }
          
        }

        //ok lets do paging
        GuiPaging guiPaging = adminContainer.getDaemonJobsGuiPaging();
        GrouperPagingTag2.processRequest(request, guiPaging, null);
        guiPaging.setTotalRecordCount(guiDaemonJobs.size());
        guiDaemonJobs = GrouperUtil.batchList(guiDaemonJobs, guiPaging.getPageSize(), (guiPaging.getPageNumber() - 1));
        
        
      } else {
      
        GuiPaging guiPaging = adminContainer.getDaemonJobsGuiPaging();
        GrouperPagingTag2.processRequest(request, guiPaging, null);
        guiPaging.setTotalRecordCount(allJobNamesAfterFilter.size());
        
        List<String> currentJobNames = GrouperUtil.batchList(allJobNamesAfterFilter, guiPaging.getPageSize(), (guiPaging.getPageNumber() - 1));
        
        for (String jobName : currentJobNames) {
          
          GuiDaemonJob guiDaemonJob = new GuiDaemonJob(jobName);
          guiDaemonJobs.add(guiDaemonJob);
        }
      }
      
      if (GrouperUtil.length(guiDaemonJobs) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, TextContainer.retrieveFromRequest().getText().get("daemonJobsNoResultsFound")));
      }
      
      adminContainer.setGuiDaemonJobs(guiDaemonJobs);
      
      return true;
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  public void jobHistoryChart(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      //if the user is allowed
      GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();
      boolean canSeeLoader = grouperLoaderContainer.isCanSeeLoader();

      if (!canSeeLoader) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("adminJobHistoryErrorNotAllowed")));
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
        return;
      }
      
      // todo return message if can't view

      jobHistoryChartHelper(request, response);

    //todo move this to the helper
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  private void jobHistoryChartHelper(HttpServletRequest request, HttpServletResponse response) throws ParseException {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //int maxLogs = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.loader.logs.maxSize", 400);

    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();

    String dateFrom;
    if (!StringUtils.isEmpty(request.getParameter("dateFrom"))) {
      dateFrom = request.getParameter("dateFrom");
    } else {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      DateFormat dateFormat = new SimpleDateFormat(GrouperUtil.DATE_FORMAT2);
      dateFrom = dateFormat.format(cal.getTime());
    }
    adminContainer.setGuiJobHistoryDateFrom(dateFrom);

    String timeFrom;
    if (!StringUtils.isEmpty(request.getParameter("timeFrom"))) {
      timeFrom = request.getParameter("timeFrom");
    } else {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
      timeFrom = dateFormat.format(cal.getTime());
    }
    adminContainer.setGuiJobHistoryTimeFrom(timeFrom);

    // (throws ParseException)
    Date dateFromDate = new SimpleDateFormat(GrouperUtil.DATE_MINUTES_SECONDS_FORMAT).parse(dateFrom + " " + timeFrom);

    String dateTo;
    if (!StringUtils.isEmpty(request.getParameter("dateTo"))) {
      dateTo = request.getParameter("dateTo");
    } else {
      final Calendar cal = Calendar.getInstance();
      DateFormat dateFormat = new SimpleDateFormat(GrouperUtil.DATE_FORMAT2);
      dateTo = dateFormat.format(cal.getTime());
    }
    adminContainer.setGuiJobHistoryDateTo(dateTo);

    String timeTo;
    if (!StringUtils.isEmpty(request.getParameter("dateTo"))) {
      timeTo = request.getParameter("timeTo");
    } else {
      final Calendar cal = Calendar.getInstance();
      DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
      timeTo = dateFormat.format(cal.getTime());
    }
    adminContainer.setGuiJobHistoryTimeTo(timeTo);

    // (throws ParseException)
    Date dateToDate = new SimpleDateFormat(GrouperUtil.DATE_MINUTES_SECONDS_FORMAT).parse(dateTo + " " + timeTo);

    String minElapsedString = !StringUtils.isEmpty(request.getParameter("minElapsedSeconds"))
      ? request.getParameter("minElapsedSeconds")
      : "5";
    adminContainer.setGuiJobHistoryMinimumElapsedSeconds(minElapsedString);
    int minElapsed = Integer.parseInt(minElapsedString);

    String namesLikeFilterString = request.getParameter("namesLikeFilter");
    adminContainer.setGuiJobHistoryNamesLikeFilter(namesLikeFilterString);


    List<Criterion> criterionList = new ArrayList<>();
    //criterionList.add(Restrictions.not(Restrictions.in("jobType", new String[]{"CHANGE_LOG", "OTHER_JOB"})));
    criterionList.add(Restrictions.ge("millis", minElapsed * 1000));
    criterionList.add(Restrictions.isNull("parentJobId"));
    criterionList.add(Restrictions.between("startedTime", dateFromDate, dateToDate));
    if (!GrouperUtil.isEmpty(namesLikeFilterString)) {
      String[] filterStrings = GrouperUtil.splitTrim(namesLikeFilterString, ",");
      Criterion[] namesLikeCriteria = new Criterion[filterStrings.length];
      for (int i = 0; i < filterStrings.length; ++i) {
        namesLikeCriteria[i] = Restrictions.like("jobName", filterStrings[i]);
      }
      criterionList.add(Restrictions.and(Restrictions.or(namesLikeCriteria)));
    }
    Criterion allCriteria = HibUtils.listCrit(criterionList);

    QueryOptions queryOptions = new QueryOptions().sort(new QuerySort("startedTime", true));

    List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic().options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);

    JSONArray ganttJobs = new JSONArray();

    //add to the ordered array based on the first occurrence of the task, so it will be ordered by start time
    Set<String> ganttJobNameSet = new HashSet<>();
    List<String> ganttJobNameList = new ArrayList<>();

    final DateFormat dateFormat = new SimpleDateFormat(GrouperUtil.DATE_MINUTES_SECONDS_FORMAT);

    for (Hib3GrouperLoaderLog log : loaderLogs) {
      String jobShortName = log.getGroupNameFromJobName();
      Map<String, String> ganttJob = new HashMap<>();
      ganttJob.put("startDateString", dateFormat.format(log.getStartedTime()));
      ganttJob.put("endDateString", dateFormat.format(log.getEndedTime()));
      ganttJob.put("taskName", jobShortName);
      ganttJob.put("status", log.getStatus());

      if (!GrouperUtil.isBlank(log.getGroupUuid())) {
        ganttJob.put("url", "?operation=UiV2Group.viewGroup&groupId=" + log.getGroupUuid());
      }

      StringBuilder tooltipBuilder = new StringBuilder()
        .append(log.getJobName())
        .append("<br/>")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipStatus"))
        .append(": ").append(log.getStatus())
        .append("<br/>")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipStarted"))
        .append(": ").append(dateFormat.format(log.getStartedTime()));
      if (log.getEndedTime() != null) {
        tooltipBuilder.append("<br/>")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipFinished"))
        .append(": " + dateFormat.format(log.getEndedTime()));
        Duration duration = Duration.between(log.getStartedTime().toInstant(), log.getEndedTime().toInstant());
        tooltipBuilder.append("<br/>")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipElapsed"))
        .append(":" + duration.getSeconds() + " ")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipSecondsSuffix"))
        ;
      }
      tooltipBuilder.append("<br/>")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipInsertPrefix"))
        .append(":" + log.getInsertCount())
        .append(" ")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipUpdatePrefix"))
        .append(":" + log.getUpdateCount())
        .append(" ")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipDeletePrefix"))
        .append(":" + log.getDeleteCount())
        .append(" ")
        .append(TextContainer.retrieveFromRequest().getText().get("adminJobHistoryTooltipTotalPrefix"))
        .append(":" + log.getTotalCount());

      ganttJob.put("tooltip", tooltipBuilder.toString());

      ganttJobs.add(ganttJob);
      if (!ganttJobNameSet.contains(jobShortName)) {
        ganttJobNameList.add(jobShortName);
      }
    }

    Map<String, String> jobHistorySettings = new HashMap<>();
    jobHistorySettings.put("startDateString", dateFormat.format(dateFromDate));
    jobHistorySettings.put("endDateString", dateFormat.format(dateToDate));
    long hoursRange = TimeUnit.HOURS.convert(dateToDate.getTime() - dateFromDate.getTime(), TimeUnit.MILLISECONDS);
    if (hoursRange > 24 * 10) {
      jobHistorySettings.put("dateFormat", "%m-%d");
//    } else if (hoursRange > 36) {
//      jobHistorySettings.put("dateFormat", "%m-%d %H:%M");
    } else {
      jobHistorySettings.put("dateFormat", "%H:%M");
    }

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/admin/adminJobHistoryChart.jsp"));

    JSONArray jsonGanttTasks = JSONArray.fromObject(ganttJobs);
    guiResponseJs.addAction(GuiScreenAction.newAssign("jobHistoryTasks", jsonGanttTasks.toString()));

    JSONArray jsonGanttTaskNames = JSONArray.fromObject(ganttJobNameList);
    guiResponseJs.addAction(GuiScreenAction.newAssign("jobHistoryTaskNames", jsonGanttTaskNames.toString()));

    JSONObject jsonJobHistorySettings = JSONObject.fromObject(jobHistorySettings);
    guiResponseJs.addAction(GuiScreenAction.newAssign("jobHistoryChartSettings", jsonJobHistorySettings.toString()));

    guiResponseJs.addAction(GuiScreenAction.newScript("jobHistoryChartInit()"));

    //guiResponseJs.addAction(GuiScreenAction.newScript("gantt(tasks);"));
  }

  /**
   * view the logs filter for the daemon job
   * @param request
   * @param response
   */
  public void viewLogsFilter(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String jobName = request.getParameter("jobName");
      
      AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
      List<GuiDaemonJob> guiDaemonJobs = new ArrayList<GuiDaemonJob>();
      guiDaemonJobs.add(new GuiDaemonJob(jobName));
      adminContainer.setGuiDaemonJobs(guiDaemonJobs);

      viewLogsHelper(request, response);
      
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
   * view logs from filter or not
   * @param request 
   * @param response 
   */
  private void viewLogsHelper(HttpServletRequest request, HttpServletResponse response) {

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //if the user is allowed
    if (!daemonJobsAllowed()) {
      return;
    }

    String jobName = request.getParameter("jobName");

    List<Criterion> criterionList = new ArrayList<Criterion>();

    if (StringUtils.equals("true", request.getParameter("showSubjobsName"))) {

      criterionList.add(HibUtils.listCritOr(
          Restrictions.eq("jobName", jobName),
          Restrictions.eq("parentJobName", jobName)
          ));

    } else {

      criterionList.add(Restrictions.eq("jobName", jobName));
      
    }
    
    

    {
      String startTimeFrom = request.getParameter("startTimeFromName");
      
      if (!StringUtils.isBlank(startTimeFrom)) {
        Timestamp startTimeFromTimestamp = UiV2GrouperLoader.convertFormInputToTimestamp(startTimeFrom);
        if (startTimeFromTimestamp != null) {
          criterionList.add(Restrictions.ge("startedTime", startTimeFromTimestamp));
        }
      }
    }

    {
      String startTimeTo = request.getParameter("startTimeToName");
      
      if (!StringUtils.isBlank(startTimeTo)) {
        Timestamp startTimeToTimestamp = UiV2GrouperLoader.convertFormInputToTimestamp(startTimeTo);
        if (startTimeToTimestamp != null) {
          criterionList.add(Restrictions.le("startedTime", startTimeToTimestamp));
        }
      }
    }

    {
      String endTimeFrom = request.getParameter("endTimeFromName");
      
      if (!StringUtils.isBlank(endTimeFrom)) {
        Timestamp endTimeFromTimestamp = UiV2GrouperLoader.convertFormInputToTimestamp(endTimeFrom);
        if (endTimeFromTimestamp != null) {
          criterionList.add(Restrictions.ge("endedTime", endTimeFromTimestamp));
        }
      }
    }

    {
      String endTimeTo = request.getParameter("endTimeToName");
      
      if (!StringUtils.isBlank(endTimeTo)) {
        Timestamp endTimeToTimestamp = UiV2GrouperLoader.convertFormInputToTimestamp(endTimeTo);
        if (endTimeToTimestamp != null) {
          criterionList.add(Restrictions.le("endedTime", endTimeToTimestamp));
        }
      }
    }

    {
      String lastUpdateTimeFrom = request.getParameter("lastUpdateTimeFromName");
      
      if (!StringUtils.isBlank(lastUpdateTimeFrom)) {
        Timestamp lastUpdateTimeFromTimestamp = UiV2GrouperLoader.convertFormInputToTimestamp(lastUpdateTimeFrom);
        if (lastUpdateTimeFromTimestamp != null) {
          criterionList.add(Restrictions.ge("lastUpdated", lastUpdateTimeFromTimestamp));
        }
      }
    }

    {
      String lastUpdateTimeTo = request.getParameter("lastUpdateTimeToName");
      
      if (!StringUtils.isBlank(lastUpdateTimeTo)) {
        Timestamp lastUpdateTimeToTimestamp = UiV2GrouperLoader.convertFormInputToTimestamp(lastUpdateTimeTo);
        if (lastUpdateTimeToTimestamp != null) {
          criterionList.add(Restrictions.le("lastUpdated", lastUpdateTimeToTimestamp));
        }
      }
    }

    {
      List<String> statuses = new ArrayList<String>();
      if (StringUtils.equals("true", request.getParameter("statusSuccessName"))) {
        statuses.add("SUCCESS");
      }
      if (StringUtils.equals("true", request.getParameter("statusErrorName"))) {
        statuses.add("ERROR");
      }
      if (StringUtils.equals("true", request.getParameter("statusStartedName"))) {
        statuses.add("STARTED");
      }
      if (StringUtils.equals("true", request.getParameter("statusRunningName"))) {
        statuses.add("RUNNING");
      }
      if (StringUtils.equals("true", request.getParameter("statusConfigErrorName"))) {
        statuses.add("CONFIG_ERROR");
      }
      if (StringUtils.equals("true", request.getParameter("statusSubjectProblemsName"))) {
        statuses.add("SUBJECT_PROBLEMS");
      }
      if (StringUtils.equals("true", request.getParameter("statusWarningName"))) {
        statuses.add("WARNING");
      }
      if (statuses.size() > 0) {
        criterionList.add(Restrictions.in("status", statuses));
      }
    }
    QueryOptions queryOptions = null;
    
    {
      int maxLogs = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.loader.logs.maxSize", 400);
      
      String numberOfRows = request.getParameter("numberOfRowsName");
      numberOfRows = StringUtils.trimToNull(numberOfRows);
      
      if (!StringUtils.isBlank(numberOfRows)) {
        
        try {
          maxLogs = GrouperUtil.intValue(numberOfRows);
          
          int maxMaxLogs = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.loader.logs.maxMaxSize", 5000);
          if (maxLogs > maxMaxLogs) {
            maxLogs = maxMaxLogs;
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderLogsNumberOfRowsOverMax") + " " + maxLogs));
            
          }
          
        } catch (Exception e) {
          LOG.info("Not an integer: '" + numberOfRows + "'", e);
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("grouperLoaderLogsCannotParseNumberOfRows") + " " + GrouperUtil.xmlEscape(numberOfRows)));
        }
        
      }
      
      queryOptions = QueryOptions.create("lastUpdated", false, 1, maxLogs);
      
      
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
      .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);

    List<GuiHib3GrouperLoaderLog> guiLoaderLogs = GuiHib3GrouperLoaderLog.convertFromHib3GrouperLoaderLogs(loaderLogs);
    
    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
    adminContainer.setGuiHib3GrouperLoaderLogs(guiLoaderLogs);
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperLoaderLogsResultsId", 
        "/WEB-INF/grouperUi2/admin/adminDaemonJobsViewLogsResults.jsp"));

  }
  
  /**
   * view the logs for the daemon job
   * @param request
   * @param response
   */
  public void viewLogs(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String jobName = request.getParameter("jobName");
      
      AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
      List<GuiDaemonJob> guiDaemonJobs = new ArrayList<GuiDaemonJob>();
      guiDaemonJobs.add(new GuiDaemonJob(jobName));
      adminContainer.setGuiDaemonJobs(guiDaemonJobs);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/admin/adminDaemonJobsViewLogs.jsp"));
      
      viewLogsHelper(request, response);
      
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
   * 
   */
  private void instrumentationGraphResultsHelper(List<InstrumentationDataInstance> instances, long displayIncrement, String filterDate) {
    
    Map<String, Map<String, Long>> formattedData = new TreeMap<String, Map<String, Long>>();
    Set<String> daysWithData = new TreeSet<String>();
      
    long firstTime = 9999999999999L;
    long lastTime = 0L;
    
    SimpleDateFormat sdfDateTimeUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdfDateTimeUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    SimpleDateFormat sdfDateUTC = new SimpleDateFormat("yyyy-MM-dd");
    sdfDateUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    
    Map<String, Set<Long>> allStartTimesByType = new HashMap<String, Set<Long>>();

    for (InstrumentationDataInstance instance : instances) {
      List<InstrumentationDataInstanceCounts> instanceCountsList = instance.getCounts();
        
      for (InstrumentationDataInstanceCounts instanceCounts : instanceCountsList) {
        for (String type : instanceCounts.getCounts().keySet()) {
          Long count = instanceCounts.getCounts().get(type);
          
          long startTime = (instanceCounts.getStartTime().getTime() / displayIncrement) * displayIncrement;
          
          String formattedDateTimeUTC = sdfDateTimeUTC.format(new Date(startTime));
          String formattedDateUTC = sdfDateUTC.format(new Date(startTime));
          daysWithData.add(formattedDateUTC);
          
          if (StringUtils.isEmpty(filterDate) || filterDate.equals(formattedDateUTC)) {
            if (formattedData.get(type) == null) {
              formattedData.put(type, new TreeMap<String, Long>());
              allStartTimesByType.put(type, new HashSet<Long>());
            }
            
            if (formattedData.get(type).get(formattedDateTimeUTC) == null) {
              formattedData.get(type).put(formattedDateTimeUTC, 0L);
              
              firstTime = Math.min(startTime, firstTime);
              lastTime = Math.max(startTime, lastTime);
              allStartTimesByType.get(type).add(startTime);
            }
            
            formattedData.get(type).put(formattedDateTimeUTC, formattedData.get(type).get(formattedDateTimeUTC) + count);
          }
        }
      }
    }
    
    // fill in any gaps with 0s
    for (String type : formattedData.keySet()) {
      for (long i = firstTime; i <= lastTime; i = i + displayIncrement) {
        if (!allStartTimesByType.get(type).contains(i)) {
          String formattedDateUTC = sdfDateTimeUTC.format(new Date(i));
          formattedData.get(type).put(formattedDateUTC, 0L);
        }
      }
    }
    
    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
    adminContainer.setGuiInstrumentationGraphResults(formattedData);
    adminContainer.setGuiInstrumentationDaysWithData(daysWithData);
  }

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
   * 
   * @return true if ok, false if not allowed
   */
  private boolean daemonJobsAllowed() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    AdminContainer adminContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer();
    
    //if the user allowed
    if (!adminContainer.isDaemonJobsShow()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("adminDaemonJobsErrorNotAllowed")));
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
      
      String sourceId = request.getParameter("subjectApiSourceIdName");
      String subjectId = request.getParameter("subjectIdName");
      String subjectIdentifier = request.getParameter("subjectIdentifierName");
      String searchString = request.getParameter("searchStringName");
      
      StringBuilder subjectApiReport = new SubjectSourceDiagnostics().assignSourceId(sourceId)
          .assignSubjectId(subjectId).assignSubjectIdentifier(subjectIdentifier).assignSearchString(searchString)
          .subjectSourceDiagnostics();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#subjectApiDiagnosticsResultsId", subjectApiReport.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
}
