/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperLoaderContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class UiV2GrouperLoader {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(UiV2GrouperLoader.class);

  /**
   * 
   */
  public UiV2GrouperLoader() {
  }
  
  /**
   * view the logs filter for the loader job
   * @param request
   * @param response
   */
  public void viewLogsFilter(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();
      
      boolean canSeeLoader = grouperLoaderContainer.isCanSeeLoader();
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();

      if (group == null || !canSeeLoader) {
        return;
      }

      //not sure who can see attributes etc, just go root
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.startRootSession();

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

    GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();

    String jobName = grouperLoaderContainer.getJobName();

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
        Timestamp startTimeFromTimestamp = convertFormInputToTimestamp(startTimeFrom);
        if (startTimeFromTimestamp != null) {
          criterionList.add(Restrictions.ge("startedTime", startTimeFromTimestamp));
        }
      }
    }

    {
      String startTimeTo = request.getParameter("startTimeToName");
      
      if (!StringUtils.isBlank(startTimeTo)) {
        Timestamp startTimeToTimestamp = convertFormInputToTimestamp(startTimeTo);
        if (startTimeToTimestamp != null) {
          criterionList.add(Restrictions.le("startedTime", startTimeToTimestamp));
        }
      }
    }

    {
      String endTimeFrom = request.getParameter("endTimeFromName");
      
      if (!StringUtils.isBlank(endTimeFrom)) {
        Timestamp endTimeFromTimestamp = convertFormInputToTimestamp(endTimeFrom);
        if (endTimeFromTimestamp != null) {
          criterionList.add(Restrictions.ge("endedTime", endTimeFromTimestamp));
        }
      }
    }

    {
      String endTimeTo = request.getParameter("endTimeToName");
      
      if (!StringUtils.isBlank(endTimeTo)) {
        Timestamp endTimeToTimestamp = convertFormInputToTimestamp(endTimeTo);
        if (endTimeToTimestamp != null) {
          criterionList.add(Restrictions.le("endedTime", endTimeToTimestamp));
        }
      }
    }

    {
      String lastUpdateTimeFrom = request.getParameter("lastUpdateTimeFromName");
      
      if (!StringUtils.isBlank(lastUpdateTimeFrom)) {
        Timestamp lastUpdateTimeFromTimestamp = convertFormInputToTimestamp(lastUpdateTimeFrom);
        if (lastUpdateTimeFromTimestamp != null) {
          criterionList.add(Restrictions.ge("lastUpdated", lastUpdateTimeFromTimestamp));
        }
      }
    }

    {
      String lastUpdateTimeTo = request.getParameter("lastUpdateTimeToName");
      
      if (!StringUtils.isBlank(lastUpdateTimeTo)) {
        Timestamp lastUpdateTimeToTimestamp = convertFormInputToTimestamp(lastUpdateTimeTo);
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
      
      queryOptions = QueryOptions.create("startedTime", false, 1, maxLogs);
      
      
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
      .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);

    List<GuiHib3GrouperLoaderLog> guiLoaderLogs = GuiHib3GrouperLoaderLog.convertFromHib3GrouperLoaderLogs(loaderLogs);
    
    grouperLoaderContainer.setGuiHib3GrouperLoaderLogs(guiLoaderLogs);
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperLoaderLogsResultsId", 
        "/WEB-INF/grouperUi2/group/grouperLoaderViewLogsResults.jsp"));

  }
  
  /**
   * <pre>
   * yy or yyyy pattern
   * 
   * ^           start
   * \\s*        maybe whitespace
   * (\\d{2,4})  capture a 2 o 4 digits year
   * \\s*        maybe whitespace
   * $           end
   * </pre>
   */
  private static Pattern timestampYearPattern = Pattern.compile("^\\s*(\\d{2,4})\\s*$");
  
  /**
   * <pre>
   * yy-mm or yyyy-m pattern etc
   * 
   * ^           start
   * \\s*        maybe whitespace
   * (\\d{2,4})  capture a 2 or 4 digits year
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits month
   * \\s*        maybe whitespace
   * $           end
   * </pre>
   */
  private static Pattern timestampMonthPattern = Pattern.compile("^\\s*(\\d{2,4})\\s*-\\s*(\\d{1,2})\\s*$");
  
  /**
   * <pre>
   * yy-mm-dd or yyyy-m-d pattern etc
   * 
   * ^           start
   * \\s*        maybe whitespace
   * (\\d{2,4})  capture a 2 or 4 digits year
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits month
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits day
   * \\s*        maybe whitespace
   * $           end
   * </pre>
   */
  private static Pattern timestampDayPattern = Pattern.compile("^\\s*(\\d{2,4})\\s*-\\s*(\\d{1,2})\\s*-\\s*(\\d{1,2})\\s*$");
  
  /**
   * <pre>
   * yy-mm-dd or yyyy-m-d pattern etc
   * 
   * ^           start
   * \\s*        maybe whitespace
   * (\\d{2,4})  capture a 2 or 4 digits year
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits month
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits day
   * \\s+        at least one space
   * (\\d{1,2})  capture a 1 or 2 digit hour
   * \\s*        maybe whitespace
   * :           colon
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digit minute
   * \\s*        maybe whitespace
   * $           end
   * </pre>
   */
  private static Pattern timestampMinutePattern = Pattern.compile("^\\s*(\\d{2,4})\\s*-\\s*(\\d{1,2})\\s*-\\s*(\\d{1,2})\\s+(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*$");
  
  /**
   * <pre>
   * yy-mm-dd or yyyy-m-d pattern etc
   * 
   * ^           start
   * \\s*        maybe whitespace
   * (\\d{2,4})  capture a 2 or 4 digits year
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits month
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits day
   * \\s+        at least one space
   * (\\d{1,2})  capture a 1 or 2 digit hour
   * \\s*        maybe whitespace
   * :           colon
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digit minute
   * \\s*        maybe whitespace
   * :           colon
   * \\s*        maybe whitespace
   * (\d{1,2})   capture a 1 or 2 digit second
   * \\s*        maybe whitespace
   * $           end
   * </pre>
   */
  private static Pattern timestampSecondPattern = Pattern.compile("^\\s*(\\d{2,4})\\s*-\\s*(\\d{1,2})\\s*-\\s*(\\d{1,2})\\s+(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*$");
  
  /**
   * <pre>
   * yy-mm-dd or yyyy-m-d pattern etc
   * 
   * ^           start
   * \\s*        maybe whitespace
   * (\\d{2,4})  capture a 2 or 4 digits year
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits month
   * \\s*        maybe whitespace
   * -           dash
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digits day
   * \\s+        at least one space
   * (\\d{1,2})  capture a 1 or 2 digit hour
   * \\s*        maybe whitespace
   * :           colon
   * \\s*        maybe whitespace
   * (\\d{1,2})  capture a 1 or 2 digit minute
   * \\s*        maybe whitespace
   * :           colon
   * \\s*        maybe whitespace
   * (\d{1,2})   capture a 1 or 2 digit second
   * \\s*        maybe whitespace
   * \.          dot
   * \\s*        maybe whitespace
   * (\d{1,3})   millis
   * $           end
   * </pre>
   */
  private static Pattern timestampMilliPattern = Pattern.compile("^\\s*(\\d{2,4})\\s*-\\s*(\\d{1,2})\\s*-\\s*(\\d{1,2})\\s+(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*\\.\\s*(\\d{1,2})\\s*$");

  /**
   * 
   * @param formInput
   * @return the timestamp
   */
  public static Timestamp convertFormInputToTimestamp(String formInput) {
    
    int year = 0;
    int month = -1;
    int day = -1;
    int hour = -1;
    int minute = -1;
    int second = -1;
    int millis = -1;
    
    try {
    
      Matcher matcher = timestampMilliPattern.matcher(formInput);
      boolean foundMatch = false;
      
      if (!foundMatch && matcher.matches()) {
        year = GrouperUtil.intValue(matcher.group(1));
        month = GrouperUtil.intValue(matcher.group(2));
        day = GrouperUtil.intValue(matcher.group(3));
        hour = GrouperUtil.intValue(matcher.group(4));
        minute = GrouperUtil.intValue(matcher.group(5));
        second = GrouperUtil.intValue(matcher.group(6));
        millis = GrouperUtil.intValue(matcher.group(7));
        foundMatch = true;
      }
      
      if (!foundMatch) {
        matcher = timestampSecondPattern.matcher(formInput);
        
        if (matcher.matches()) {
          year = GrouperUtil.intValue(matcher.group(1));
          month = GrouperUtil.intValue(matcher.group(2));
          day = GrouperUtil.intValue(matcher.group(3));
          hour = GrouperUtil.intValue(matcher.group(4));
          minute = GrouperUtil.intValue(matcher.group(5));
          second = GrouperUtil.intValue(matcher.group(6));
          foundMatch = true;
        }
      }
      if (!foundMatch) {
        matcher = timestampMinutePattern.matcher(formInput);
        
        if (matcher.matches()) {
          year = GrouperUtil.intValue(matcher.group(1));
          month = GrouperUtil.intValue(matcher.group(2));
          day = GrouperUtil.intValue(matcher.group(3));
          hour = GrouperUtil.intValue(matcher.group(4));
          minute = GrouperUtil.intValue(matcher.group(5));
          foundMatch = true;
        }
      }
      if (!foundMatch) {
        matcher = timestampDayPattern.matcher(formInput);
        
        if (matcher.matches()) {
          year = GrouperUtil.intValue(matcher.group(1));
          month = GrouperUtil.intValue(matcher.group(2));
          day = GrouperUtil.intValue(matcher.group(3));
          foundMatch = true;
        }
      }
      if (!foundMatch) {
        matcher = timestampMonthPattern.matcher(formInput);
        
        if (matcher.matches()) {
          year = GrouperUtil.intValue(matcher.group(1));
          month = GrouperUtil.intValue(matcher.group(2));
          foundMatch = true;
        }
      }
      if (!foundMatch) {
        matcher = timestampYearPattern.matcher(formInput);
        
        if (matcher.matches()) {
          year = GrouperUtil.intValue(matcher.group(1));
          foundMatch = true;
        }
      }
      if (!foundMatch) {
        throw new RuntimeException("Cant find match");
      }

      Calendar calendar = new GregorianCalendar();

      if (year < 100) {
        year += 2000;
      }

      calendar.set(Calendar.YEAR, year);

      if (month == -1) {
        month = 1;
      }
      {
        if (month < 1 || month > 12) {
          throw new RuntimeException("Invalid month");
        }
        // months are 0 based
        calendar.set(Calendar.MONTH, month-1);
      }

      if (day == -1) {
        day = 1;
      }
      {
        if (day < 1 || day > 31) {
          throw new RuntimeException("Invalid day");
        }
        calendar.set(Calendar.DAY_OF_MONTH, day);
      }

      if (hour == -1) {
        hour = 0;
      }
      {
        if (hour > 23) {
          throw new RuntimeException("Invalid hour");
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
      }

      if (minute == -1) {
        minute = 0;
      }
      {
        if (minute > 59) {
          throw new RuntimeException("Invalid minute");
        }
        calendar.set(Calendar.MINUTE, minute);
      }
      
      if (second == -1) {
        second = 0;
      }
      {
        if (second > 59) {
          throw new RuntimeException("Invalid second");
        }
        calendar.set(Calendar.SECOND, second);
      }
      
      if (millis == -1) {
        millis = 0;
      }
      {
        if (millis > 999) {
          throw new RuntimeException("Invalid millis");
        }
        calendar.set(Calendar.MILLISECOND, millis);
      }
      
      return new Timestamp(calendar.getTimeInMillis());
      
    } catch (Exception e) {
      LOG.info("Cant parse: '" + formInput + "'", e);
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("grouperLoaderLogsCannotParseDate") + " " + GrouperUtil.xmlEscape(formInput)));
    }
    return null;
  }
  
  /**
   * view the logs for the loader job
   * @param request
   * @param response
   */
  public void viewLogs(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();
      
      boolean canSeeLoader = grouperLoaderContainer.isCanSeeLoader();
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();

      if (group == null || !canSeeLoader) {
        return;
      }

      //not sure who can see attributes etc, just go root
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.startRootSession();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/grouperLoaderViewLogsTab.jsp"));
      
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
   * the loader button was pressed
   * @param request
   * @param response
   */
  public void loader(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      boolean canSeeLoader = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer().isCanSeeLoader();
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();

      if (group == null || !canSeeLoader) {
        return;
      }
      
      //not sure who can see attributes etc, just go root
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.startRootSession();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/grouperLoaderGroupTab.jsp"));

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
   * edit the grouper loader
   * @param request
   * @param response
   */
  public void editGrouperLoader(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      boolean canSeeLoader = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer().isCanSeeLoader();

      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();

      if (group == null || !canSeeLoader) {
        return;
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/grouperLoaderEditGroupTab.jsp"));

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
}
