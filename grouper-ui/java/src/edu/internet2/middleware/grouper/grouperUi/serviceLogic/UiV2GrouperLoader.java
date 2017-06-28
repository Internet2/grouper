/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderScheduleType;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGrouperLoaderJob;
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
import edu.internet2.middleware.grouper.ldap.LdapHandler;
import edu.internet2.middleware.grouper.ldap.LdapHandlerBean;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import net.redhogs.cronparser.CronExpressionDescriptor;


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
      
      queryOptions = QueryOptions.create("lastUpdated", false, 1, maxLogs);
      
      
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
  public void editGrouperLoaderSave(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String nameOfLoaderAttributeDefName = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader";

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();
      
      boolean canEditLoader = grouperLoaderContainer.isCanEditLoader();

      if (!canEditLoader) {
        return;
      }

      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      editGrouperLoaderHelper(request, grouperLoaderContainer);

      boolean hasError = false;
      
      if (!grouperLoaderContainer.isEditLoaderIsLoader()) {
        if (grouperLoaderContainer.isGrouperLdapLoader()) {
          
          //first, get the attribute def name
          AttributeDefName grouperLoaderLdapName = GrouperDAOFactory.getFactory().getAttributeDefName()
              .findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
          group.getAttributeDelegate().removeAttribute(grouperLoaderLdapName);

          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditRemoved")));

        }
        if (grouperLoaderContainer.isGrouperSqlLoader()) {
          //first, get the attribute def name
          AttributeDefName grouperLoader = GrouperDAOFactory.getFactory().getAttributeDefName()
              .findByNameSecure(nameOfLoaderAttributeDefName, false);
          
          if (grouperLoader == null) {
            throw new RuntimeException("Cannot find attribute in registry: " + nameOfLoaderAttributeDefName);
          }

          group.getAttributeDelegate().removeAttribute(grouperLoader);

          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditRemoved")));

        }
      } else {
        if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderType())) {

          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditTypeRequired")));
          hasError = true;
        }

        if (StringUtils.equals("SQL", grouperLoaderContainer.getEditLoaderType())) {
          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlType())) {
  
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditSqlTypeRequired")));
            hasError = true;
          }
          if (!StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlType())) {
            try {
              GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderContainer.getEditLoaderSqlType(), true);
              if (grouperLoaderType != GrouperLoaderType.SQL_GROUP_LIST && grouperLoaderType != GrouperLoaderType.SQL_SIMPLE) {
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                    TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditSqlTypeWrong")));
                hasError = true;
                
              }
            } catch (Exception e) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditSqlTypeInvalid")));
              hasError = true;
            }
          }
  
          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlDatabaseName())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditServerIdRequired")));
            hasError = true;
          }
          
          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlQuery())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditSqlQueryRequired")));
            hasError = true;
          }
          
          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlQuery())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditSqlQueryRequired")));
            hasError = true;
          }
          
          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderScheduleType())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleTypeRequired")));
            hasError = true;
          }
          
          if (!hasError && StringUtils.equals(grouperLoaderContainer.getEditLoaderScheduleType(), GrouperLoaderScheduleType.START_TO_START_INTERVAL.name())) {
  
            if (!StringUtils.isBlank(grouperLoaderContainer.getEditLoaderScheduleInterval())) {
              try {
                GrouperUtil.intValue(grouperLoaderContainer.getEditLoaderScheduleInterval());
              } catch (Exception e) {
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                    TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleIntervalInvalid")));
                hasError = true;
              }
              
            } else {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleIntervalRequired")));
              hasError = true;
              
            }
  
          }
  
          if (!hasError && StringUtils.equals(grouperLoaderContainer.getEditLoaderScheduleType(), GrouperLoaderScheduleType.CRON.name())) {
  
            if (!StringUtils.isBlank(grouperLoaderContainer.getEditLoaderCron())) {
              if (StringUtils.equals(TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError"),
                  grouperLoaderContainer.getEditLoaderCronDescription())) {
                
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                    TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleCronInvalid")));
                hasError = true;
              }
              
            } else {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleCronRequired")));
              hasError = true;
              
            }
          }
  
          if (!hasError && !StringUtils.isBlank(grouperLoaderContainer.getEditLoaderPriority())) {
            try {
              GrouperUtil.intValue(grouperLoaderContainer.getEditLoaderPriority());
            } catch (Exception e) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditPriorityInvalid")));
              hasError = true;
            }
          }
        } else if (StringUtils.equals("LDAP", grouperLoaderContainer.getEditLoaderType())) {

          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapType())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapTypeRequired")));
            hasError = true;
          }
          GrouperLoaderType grouperLoaderType = null;
          if (!StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapType())) {
            try {
              grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderContainer.getEditLoaderLdapType(), true);
              if (grouperLoaderType != GrouperLoaderType.LDAP_GROUP_LIST && grouperLoaderType != GrouperLoaderType.LDAP_SIMPLE
                  && grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES) {
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                    TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapTypeWrong")));
                hasError = true;
                
              }
            } catch (Exception e) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapTypeInvalid")));
              hasError = true;
            }
          }

          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapServerId())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapServerIdRequired")));
            hasError = true;
          }
          
          if (!hasError && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapFilter())) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapFilterRequired")));
            hasError = true;
          }
          
          if (!StringUtils.isBlank(grouperLoaderContainer.getEditLoaderCron())) {
            if (StringUtils.equals(TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError"),
                grouperLoaderContainer.getEditLoaderCronDescription())) {
              
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleCronInvalid")));
              hasError = true;
            }
            
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditScheduleCronRequired")));
            hasError = true;
            
          }

          if (!hasError && !StringUtils.isBlank(grouperLoaderContainer.getEditLoaderPriority())) {
            try {
              GrouperUtil.intValue(grouperLoaderContainer.getEditLoaderPriority());
            } catch (Exception e) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditPriorityInvalid")));
              hasError = true;
            }
          }

          if (!hasError 
              && (grouperLoaderType == GrouperLoaderType.LDAP_GROUP_LIST || grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE)
              && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapSubjectAttributeName())) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapSubjectAttributeNameRequired")));
            hasError = true;
          }

          if (!hasError 
              && (grouperLoaderType == GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES)
              && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapGroupAttributeName())) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditLdapGroupAttributeNameRequired")));
            hasError = true;
          }

        }
        
      }
      if (!hasError) {

        if (grouperLoaderContainer.isEditLoaderIsLoader()) {
          
          //if sql is picked, and used to be LDAP, then remove LDAP
          if (StringUtils.equals("SQL", grouperLoaderContainer.getEditLoaderType()) && grouperLoaderContainer.isGrouperLdapLoader()) {
                        
            AttributeDefName grouperLoaderLdapName = GrouperDAOFactory.getFactory().getAttributeDefName()
                .findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
            if (grouperLoaderLdapName != null) {
              group.getAttributeDelegate().removeAttribute(grouperLoaderLdapName);
            }

          }

          //if ldap is picked, and used to be SQL, then remove LDAP
          if (StringUtils.equals("LDAP", grouperLoaderContainer.getEditLoaderType()) && grouperLoaderContainer.isGrouperSqlLoader()) {
            //first, get the attribute def name
            AttributeDefName grouperLoader = GrouperDAOFactory.getFactory().getAttributeDefName()
                .findByNameSecure(nameOfLoaderAttributeDefName, false);
            
            if (grouperLoader != null) {
              group.getAttributeDelegate().removeAttribute(grouperLoader);

              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                  TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditRemoved")));
            }
          }
          
          if (StringUtils.equals("SQL", grouperLoaderContainer.getEditLoaderType())) {
            AttributeDefName grouperLoaderAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
                .findByNameSecure(nameOfLoaderAttributeDefName, false);
            if (grouperLoaderAttributeDefName == null) {
              throw new RuntimeException("Cannot find attribute in registry: " + nameOfLoaderAttributeDefName);
            }
            AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, grouperLoaderAttributeDefName, false, false);
            if (attributeAssign == null) {
              attributeAssign = group.getAttributeDelegate().assignAttribute(grouperLoaderAttributeDefName).getAttributeAssign();
            }

            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_AND_GROUPS, grouperLoaderContainer.getEditLoaderAndGroups());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_DB_NAME, grouperLoaderContainer.getEditLoaderSqlDatabaseName() );
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_GROUP_QUERY, grouperLoaderContainer.getEditLoaderSqlGroupQuery());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_GROUP_TYPES, grouperLoaderContainer.getEditLoaderGroupTypes());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_GROUPS_LIKE, grouperLoaderContainer.getEditLoaderGroupsLike());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, grouperLoaderContainer.getEditLoaderScheduleInterval());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_PRIORITY, grouperLoaderContainer.getEditLoaderPriority());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, grouperLoaderContainer.getEditLoaderCron());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_QUERY, grouperLoaderContainer.getEditLoaderSqlQuery());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, grouperLoaderContainer.getEditLoaderScheduleType());
            assignGroupSqlAttribute(group, GrouperLoader.GROUPER_LOADER_TYPE, grouperLoaderContainer.getEditLoaderSqlType());
            
          }
          
          if (StringUtils.equals("LDAP", grouperLoaderContainer.getEditLoaderType())) {

            AttributeDefName grouperLoaderAttributeDefName = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);

            AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, grouperLoaderAttributeDefName, false, false);
            if (attributeAssign == null) {
              attributeAssign = group.getAttributeDelegate().assignAttribute(grouperLoaderAttributeDefName).getAttributeAssign();
            }
            
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapTypeAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapType());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapAndGroupsAttributeDefName(), grouperLoaderContainer.getEditLoaderAndGroups());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapAttributeFilterExpression());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupAttributeAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapGroupAttributeName());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapQuartzCronAttributeDefName(), grouperLoaderContainer.getEditLoaderCron());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapExtraAttributesAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapExtraAttributes());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapGroupDescriptionExpression());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapGroupDisplayNameExpression());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapGroupNameExpression());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupsLikeAttributeDefName(), grouperLoaderContainer.getEditLoaderGroupsLike());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupTypesAttributeDefName(), grouperLoaderContainer.getEditLoaderGroupTypes());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapAdminsAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapAdmins());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapAttrReaders());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapAttrUpdaters());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapOptinsAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapOptins());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapOptoutsAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapOptouts());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapReadersAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapReaders());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapUpdatersAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapUpdaters());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapViewersAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapViewers());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapServerIdAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapServerId());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapFilterAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapFilter());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapPriorityAttributeDefName(), grouperLoaderContainer.getEditLoaderPriority());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapSearchDnAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapSearchDn());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapSearchScopeAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapSearchScope());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapSourceIdAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapSourceId());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapSubjectAttributeAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapSubjectAttributeName());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapSubjectExpressionAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapSubjectExpression());
            assignGroupLdapAttribute(group, LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeAttributeDefName(), grouperLoaderContainer.getEditLoaderLdapSubjectLookupType());

  
          }

        }

      }
      
      if (!hasError) {
        
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperLoader.loaderDiagnostics&groupId=" + group.getId() + "')"));

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("grouperLoaderEditSaveSuccess")));

      } else {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/group/grouperLoaderEditGroupTab.jsp"));
      }
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
   * assign an attribute value
   * @param group
   * @param attributeName
   * @param value
   */
  @SuppressWarnings("deprecation")
  private static void assignGroupSqlAttribute(Group group, String attributeName, String value) {
    if (StringUtils.isBlank(value)) {
      if (!StringUtils.isBlank(group.getAttribute(attributeName))) {
        group.deleteAttribute(attributeName);
      }
    } else {
      group.setAttribute(attributeName, value, false);
    }
  }
  
  /**
   * assign an attribute value
   * @param group
   * @param attributeDefName
   * @param value
   */
  @SuppressWarnings("deprecation")
  private static void assignGroupLdapAttribute(Group group, AttributeDefName attributeDefName, String value) {

    AttributeDefName grouperLoaderAttributeDefName = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);

    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, grouperLoaderAttributeDefName, false, false);

    if (attributeAssign == null) {
      if (StringUtils.isBlank(value)) {
        return;
      }
      throw new RuntimeException("Cant find ldap loader attribute assign (" + attributeDefName.getName() + ") on group (" + group.getName() + ")");
    }

    if (StringUtils.isBlank(value)) {
      attributeAssign.getAttributeDelegate().removeAttribute(attributeDefName);
    } else {
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), value);
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

      GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();
      
      boolean canEditLoader = grouperLoaderContainer.isCanEditLoader();

      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();

      if (!canEditLoader || group == null) {
        return;
      }

      grouperLoaderContainer.setEditLoaderIsLoader(grouperLoaderContainer.isLoaderGroup());

      if (grouperLoaderContainer.isEditLoaderIsLoader()) {

        grouperLoaderContainer.setEditLoaderType(grouperLoaderContainer.isGrouperSqlLoader() ? "SQL" : (grouperLoaderContainer.isGrouperLdapLoader() ? "LDAP" : null));

        if (StringUtils.equals("SQL", grouperLoaderContainer.getEditLoaderType())) {
          grouperLoaderContainer.setEditLoaderSqlDatabaseName(grouperLoaderContainer.getSqlDatabaseName());
          grouperLoaderContainer.setEditLoaderPriority(grouperLoaderContainer.getSqlPriority());
          grouperLoaderContainer.setEditLoaderSqlQuery(grouperLoaderContainer.getSqlQuery());
          grouperLoaderContainer.setEditLoaderAndGroups(grouperLoaderContainer.getSqlAndGroups());
          grouperLoaderContainer.setEditLoaderScheduleType(grouperLoaderContainer.getSqlScheduleType());
          if (StringUtils.equals(grouperLoaderContainer.getEditLoaderScheduleType(), "CRON")) {
            grouperLoaderContainer.setEditLoaderCron(grouperLoaderContainer.getSqlCron());
          } else if (StringUtils.equals(grouperLoaderContainer.getEditLoaderScheduleType(), "START_TO_START_INTERVAL")) {
            grouperLoaderContainer.setEditLoaderScheduleInterval(grouperLoaderContainer.getSqlScheduleInterval());
          }
          grouperLoaderContainer.setEditLoaderSqlType(grouperLoaderContainer.getSqlLoaderType());
          if (StringUtils.equals("SQL_GROUP_LIST", grouperLoaderContainer.getEditLoaderSqlType())) {
            grouperLoaderContainer.setEditLoaderSqlGroupQuery(grouperLoaderContainer.getSqlGroupQuery());
            grouperLoaderContainer.setEditLoaderGroupsLike(grouperLoaderContainer.getSqlGroupsLike());
            grouperLoaderContainer.setEditLoaderGroupTypes(grouperLoaderContainer.getSqlGroupTypes());
          }
        } else if (StringUtils.equals("LDAP", grouperLoaderContainer.getEditLoaderType())) {
          grouperLoaderContainer.setEditLoaderLdapType(grouperLoaderContainer.getLdapLoaderType());
          grouperLoaderContainer.setEditLoaderAndGroups(grouperLoaderContainer.getLdapAndGroups());
          if (StringUtils.equals("LDAP_GROUPS_FROM_ATTRIBUTES", grouperLoaderContainer.getEditLoaderLdapType())) {
            grouperLoaderContainer.setEditLoaderLdapAttributeFilterExpression(grouperLoaderContainer.getLdapAttributeFilterExpression());
            grouperLoaderContainer.setEditLoaderLdapGroupAttributeName(grouperLoaderContainer.getLdapGroupAttributeName());
          }
          grouperLoaderContainer.setEditLoaderCron(grouperLoaderContainer.getLdapCron());
          if (StringUtils.equals("LDAP_GROUP_LIST", grouperLoaderContainer.getEditLoaderLdapType())) {
            grouperLoaderContainer.setEditLoaderLdapExtraAttributes(grouperLoaderContainer.getLdapExtraAttributes());
          }
          if (StringUtils.equals("LDAP_GROUP_LIST", grouperLoaderContainer.getEditLoaderLdapType()) 
              || StringUtils.equals("LDAP_GROUPS_FROM_ATTRIBUTES", grouperLoaderContainer.getEditLoaderLdapType())) {
            grouperLoaderContainer.setEditLoaderLdapGroupDescriptionExpression(grouperLoaderContainer.getLdapGroupDescriptionExpression());
            grouperLoaderContainer.setEditLoaderLdapGroupDisplayNameExpression(grouperLoaderContainer.getLdapGroupDisplayNameExpression());
            grouperLoaderContainer.setEditLoaderLdapGroupNameExpression(grouperLoaderContainer.getLdapGroupNameExpression());
            grouperLoaderContainer.setEditLoaderGroupsLike(grouperLoaderContainer.getLdapGroupsLike());
            grouperLoaderContainer.setEditLoaderGroupTypes(grouperLoaderContainer.getLdapGroupTypes());
            grouperLoaderContainer.setEditLoaderLdapAdmins(grouperLoaderContainer.getLdapAdmins());
            grouperLoaderContainer.setEditLoaderLdapAttrReaders(grouperLoaderContainer.getLdapAttrReaders());
            grouperLoaderContainer.setEditLoaderLdapAttrUpdaters(grouperLoaderContainer.getLdapAttrUpdaters());
            grouperLoaderContainer.setEditLoaderLdapOptins(grouperLoaderContainer.getLdapOptins());
            grouperLoaderContainer.setEditLoaderLdapOptouts(grouperLoaderContainer.getLdapOptouts());
            grouperLoaderContainer.setEditLoaderLdapReaders(grouperLoaderContainer.getLdapReaders());
            grouperLoaderContainer.setEditLoaderLdapUpdaters(grouperLoaderContainer.getLdapUpdaters());
            grouperLoaderContainer.setEditLoaderLdapViewers(grouperLoaderContainer.getLdapViewers());
          }
          grouperLoaderContainer.setEditLoaderLdapServerId(grouperLoaderContainer.getLdapServerId());
          grouperLoaderContainer.setEditLoaderCron(grouperLoaderContainer.getLdapCron());
          grouperLoaderContainer.setEditLoaderLdapFilter(grouperLoaderContainer.getLdapLoaderFilter());
          grouperLoaderContainer.setEditLoaderPriority(grouperLoaderContainer.getLdapPriority());
          grouperLoaderContainer.setEditLoaderLdapSearchDn(grouperLoaderContainer.getLdapSearchDn());
          grouperLoaderContainer.setEditLoaderLdapSearchScope(grouperLoaderContainer.getLdapSearchScope());
          grouperLoaderContainer.setEditLoaderLdapSourceId(grouperLoaderContainer.getLdapSourceId());
          grouperLoaderContainer.setEditLoaderLdapSubjectAttributeName(grouperLoaderContainer.getLdapSubjectAttributeName());

          grouperLoaderContainer.setEditLoaderLdapSubjectExpression(grouperLoaderContainer.getLdapSubjectExpression());
          grouperLoaderContainer.setEditLoaderLdapSubjectLookupType(grouperLoaderContainer.getLdapSubjectLookupType());
          
        }
      }      
      
      editGrouperLoaderHelper(request, grouperLoaderContainer);
      
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

  /**
   * @param request
   * @param grouperLoaderContainer
   */
  private void editGrouperLoaderHelper(HttpServletRequest request,
      GrouperLoaderContainer grouperLoaderContainer) {
    {
      Boolean isLoaderFromFormBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperLoaderHasLoaderName"));

      if (isLoaderFromFormBoolean != null) {
        grouperLoaderContainer.setEditLoaderIsLoader(isLoaderFromFormBoolean);
      }
    }
    
    //dont show anything at first
    //by default it doesnt

    boolean error = false;
    
    if (grouperLoaderContainer.isEditLoaderIsLoader()) {
      grouperLoaderContainer.setEditLoaderShowLoaderType(true);
    }

    {
      String grouperLoaderTypeString = request.getParameter("grouperLoaderTypeName");
      if (!error && !StringUtils.isBlank(grouperLoaderTypeString)) {
        grouperLoaderContainer.setEditLoaderType(grouperLoaderTypeString);
      }
      if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderType())) {
        error = true;
      }
      if (StringUtils.equals("SQL", grouperLoaderContainer.getEditLoaderType())) {
        grouperLoaderContainer.setEditLoaderShowSqlLoaderType(true);
        
        {
          String grouperLoaderSqlTypeString = request.getParameter("grouperLoaderSqlTypeName");
          if (!error && !StringUtils.isBlank(grouperLoaderSqlTypeString)) {
            grouperLoaderContainer.setEditLoaderSqlType(grouperLoaderSqlTypeString);
          }
          if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlType())) {
            error = true;
          } else {
            grouperLoaderContainer.setEditLoaderShowSqlDatabaseName(true);
            try {
              GrouperLoaderType.valueOfIgnoreCase(grouperLoaderContainer.getEditLoaderSqlType(), true);
            } catch (Exception e) {
              error = true;
            }
          }
        }
        {
          String grouperLoaderSqlDatabaseNameString = request.getParameter("grouperLoaderSqlDatabaseNameName");
          if (!error && !StringUtils.isBlank(grouperLoaderSqlDatabaseNameString)) {
            grouperLoaderContainer.setEditLoaderSqlDatabaseName(grouperLoaderSqlDatabaseNameString);
          }
          if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlDatabaseName())) {
            error = true;
          } else {
            grouperLoaderContainer.setEditLoaderShowSqlQuery(true);
            grouperLoaderContainer.setEditLoaderShowFields(true);
          }
        }
        {
          String grouperLoaderSqlQuery = request.getParameter("grouperLoaderSqlQueryName");
          if (!error && !StringUtils.isBlank(grouperLoaderSqlQuery)) {
            grouperLoaderContainer.setEditLoaderSqlQuery(grouperLoaderSqlQuery);
          }
          if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderSqlQuery())) {
            error = true;
          }
        }
        String grouperLoaderScheduleType = null;
        {
          grouperLoaderScheduleType = request.getParameter("editLoaderScheduleTypeName");
          if (!error && !StringUtils.isBlank(grouperLoaderScheduleType)) {
            grouperLoaderContainer.setEditLoaderScheduleType(grouperLoaderScheduleType);
          }
          if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderScheduleType())) {
            error = true;
          }
        }
        if (StringUtils.equals(grouperLoaderContainer.getEditLoaderScheduleType(), GrouperLoaderScheduleType.START_TO_START_INTERVAL.name())) {
          String grouperLoaderScheduleInterval = StringUtils.trimToNull(request.getParameter("editLoaderScheduleIntervalName"));
          if (!error && !StringUtils.isBlank(grouperLoaderScheduleInterval)) {
            
            grouperLoaderContainer.setEditLoaderScheduleInterval(grouperLoaderScheduleInterval);
            
          }
          if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderScheduleInterval())) {
            error = true;
          }
        }

        if (StringUtils.equals(grouperLoaderContainer.getEditLoaderScheduleType(), GrouperLoaderScheduleType.CRON.name())) {
          String grouperLoaderCron = StringUtils.trimToNull(request.getParameter("editLoaderCronName"));
          if (!error && !StringUtils.isBlank(grouperLoaderCron)) {
            
            grouperLoaderContainer.setEditLoaderCron(grouperLoaderCron);
          }
          if (!error && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderCron())) {
            
            error = true;
          }
        }

        {
          String grouperLoaderPriority = StringUtils.trimToNull(request.getParameter("editLoaderPriorityName"));
          if (!error && !StringUtils.isBlank(grouperLoaderPriority)) {
            
            grouperLoaderContainer.setEditLoaderPriority(grouperLoaderPriority);
            
          }
        }
        
        {
          String grouperLoaderSqlGroupQuery = StringUtils.trimToNull(request.getParameter("grouperLoaderSqlGroupQueryName"));
          if (!error && !StringUtils.isBlank(grouperLoaderSqlGroupQuery)) {
            
            grouperLoaderContainer.setEditLoaderSqlGroupQuery(grouperLoaderSqlGroupQuery);
            
          }
        }
        
        {
          String grouperLoaderGroupsLike = StringUtils.trimToNull(request.getParameter("grouperLoaderSqlGroupsLikeName"));
          if (!error && !StringUtils.isBlank(grouperLoaderGroupsLike)) {
            
            grouperLoaderContainer.setEditLoaderGroupsLike(grouperLoaderGroupsLike);
            
          }
        }
        
        {
          String grouperLoaderGroupTypes = StringUtils.trimToNull(request.getParameter("grouperLoaderSqlGroupTypesName"));
          if (!error && !StringUtils.isBlank(grouperLoaderGroupTypes)) {
            
            grouperLoaderContainer.setEditLoaderGroupTypes(grouperLoaderGroupTypes);
            
          }
        }
        {
          String editLoaderAndGroupsName = StringUtils.trimToNull(request.getParameter("editLoaderAndGroupsName"));
          if (!error && !StringUtils.isBlank(editLoaderAndGroupsName)) {
            
            grouperLoaderContainer.setEditLoaderAndGroups(editLoaderAndGroupsName);
            
          }
        }
        
        
      }
    }
    
    
    if (StringUtils.equals("LDAP", grouperLoaderContainer.getEditLoaderType())) {
      grouperLoaderContainer.setEditLoaderShowLdapLoaderType(true);
      
      {
        String grouperLoaderLdapTypeString = request.getParameter("grouperLoaderLdapTypeName");
        if (!error && !StringUtils.isBlank(grouperLoaderLdapTypeString)) {
          grouperLoaderContainer.setEditLoaderLdapType(grouperLoaderLdapTypeString);
        }
        if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapType())) {
          error = true;
        } else {
          grouperLoaderContainer.setEditLoaderShowLdapServerId(true);
          try {
            GrouperLoaderType.valueOfIgnoreCase(grouperLoaderContainer.getEditLoaderLdapType(), true);
          } catch (Exception e) {
            error = true;
          }
        }
      }
      {
        String grouperLoaderLdapServerIdNameString = request.getParameter("grouperLoaderLdapServerIdName");
        if (!error && !StringUtils.isBlank(grouperLoaderLdapServerIdNameString)) {
          grouperLoaderContainer.setEditLoaderLdapServerId(grouperLoaderLdapServerIdNameString);
        }
        if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapServerId())) {
          error = true;
        } else {
          grouperLoaderContainer.setEditLoaderShowLdapFilter(true);
          grouperLoaderContainer.setEditLoaderShowFields(true);
        }
      }
      {
        String grouperLoaderLdapFilterName = request.getParameter("grouperLoaderLdapFilterName");
        if (!error && !StringUtils.isBlank(grouperLoaderLdapFilterName)) {
          grouperLoaderContainer.setEditLoaderLdapFilter(grouperLoaderLdapFilterName);
        }
        if (StringUtils.isBlank(grouperLoaderContainer.getEditLoaderLdapFilter())) {
          error = true;
        }
      }
      {
        String grouperLoaderCron = StringUtils.trimToNull(request.getParameter("editLoaderCronName"));
        if (!error && !StringUtils.isBlank(grouperLoaderCron)) {
          
          grouperLoaderContainer.setEditLoaderCron(grouperLoaderCron);
        }
        if (!error && StringUtils.isBlank(grouperLoaderContainer.getEditLoaderCron())) {
          
          error = true;
        }
      }

      {
        String grouperLoaderPriority = StringUtils.trimToNull(request.getParameter("editLoaderPriorityName"));
        if (!error && !StringUtils.isBlank(grouperLoaderPriority)) {
          
          grouperLoaderContainer.setEditLoaderPriority(grouperLoaderPriority);
          
        }
      }

      {
        String editLoaderAndGroupsName = StringUtils.trimToNull(request.getParameter("editLoaderAndGroupsName"));
        if (!error && !StringUtils.isBlank(editLoaderAndGroupsName)) {
          
          grouperLoaderContainer.setEditLoaderAndGroups(editLoaderAndGroupsName);
          
        }
      }

      {
        String editLoaderLdapSubjectAttributeName = StringUtils.trimToNull(request.getParameter("editLoaderLdapSubjectAttributeName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapSubjectAttributeName)) {
          
          grouperLoaderContainer.setEditLoaderLdapSubjectAttributeName(editLoaderLdapSubjectAttributeName);
          
        }
      }

      {
        String editLoaderLdapSearchDnName = StringUtils.trimToNull(request.getParameter("editLoaderLdapSearchDnName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapSearchDnName)) {
          
          grouperLoaderContainer.setEditLoaderLdapSearchDn(editLoaderLdapSearchDnName);
          
        }
      }

      {
        String editLoaderLdapSourceName = StringUtils.trimToNull(request.getParameter("editLoaderLdapSourceName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapSourceName)) {
          
          grouperLoaderContainer.setEditLoaderLdapSourceId(editLoaderLdapSourceName);
          
        }
      }

      {
        String editLoaderLdapSubjectLookupTypeName = StringUtils.trimToNull(request.getParameter("editLoaderLdapSubjectLookupTypeName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapSubjectLookupTypeName)) {
          
          grouperLoaderContainer.setEditLoaderLdapSubjectLookupType(editLoaderLdapSubjectLookupTypeName);
          
        }
      }


      {
        String editLoaderLdapSearchScopeName = StringUtils.trimToNull(request.getParameter("editLoaderLdapSearchScopeName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapSearchScopeName)) {
          
          grouperLoaderContainer.setEditLoaderLdapSearchScope(editLoaderLdapSearchScopeName);
          
        }
      }
      
      {
        String editLoaderLdapAttributeFilterExpressionName = StringUtils.trimToNull(request.getParameter("editLoaderLdapAttributeFilterExpressionName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapAttributeFilterExpressionName)) {
          
          grouperLoaderContainer.setEditLoaderLdapAttributeFilterExpression(editLoaderLdapAttributeFilterExpressionName);
          
        }
      }

      {
        String editLoaderLdapSubjectExpressionName = StringUtils.trimToNull(request.getParameter("editLoaderLdapSubjectExpressionName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapSubjectExpressionName)) {
          
          grouperLoaderContainer.setEditLoaderLdapSubjectExpression(editLoaderLdapSubjectExpressionName);
          
        }
      }

      {
        String editLoaderLdapExtraAttributesName = StringUtils.trimToNull(request.getParameter("editLoaderLdapExtraAttributesName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapExtraAttributesName)) {
          
          grouperLoaderContainer.setEditLoaderLdapExtraAttributes(editLoaderLdapExtraAttributesName);
          
        }
      }

      {
        String editLoaderLdapGroupAttributeName = StringUtils.trimToNull(request.getParameter("editLoaderLdapGroupAttributeName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapGroupAttributeName)) {
          
          grouperLoaderContainer.setEditLoaderLdapGroupAttributeName(editLoaderLdapGroupAttributeName);
          
        }
      }

      {
        String grouperLoaderGroupsLike = StringUtils.trimToNull(request.getParameter("grouperLoaderSqlGroupsLikeName"));
        if (!error && !StringUtils.isBlank(grouperLoaderGroupsLike)) {
          
          grouperLoaderContainer.setEditLoaderGroupsLike(grouperLoaderGroupsLike);
          
        }
      }
      
      {
        String editLoaderLdapGroupNameExpression = StringUtils.trimToNull(request.getParameter("grouperLoaderGroupNameExpressionName"));
        if (!error && !StringUtils.isBlank(editLoaderLdapGroupNameExpression)) {
          
          grouperLoaderContainer.setEditLoaderLdapGroupNameExpression(editLoaderLdapGroupNameExpression);
          
        }
      }
      {
        String grouperLoaderLdapGroupDisplayNameName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapGroupDisplayNameName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapGroupDisplayNameName)) {
          
          grouperLoaderContainer.setEditLoaderLdapGroupDisplayNameExpression(grouperLoaderLdapGroupDisplayNameName);
          
        }
      }
      {
        String grouperLoaderLdapGroupDescriptionName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapGroupDescriptionName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapGroupDescriptionName)) {
          
          grouperLoaderContainer.setEditLoaderLdapGroupDescriptionExpression(grouperLoaderLdapGroupDescriptionName);
          
        }
      }
      
      {
        String grouperLoaderSqlGroupTypesName = StringUtils.trimToNull(request.getParameter("grouperLoaderSqlGroupTypesName"));
        if (!error && !StringUtils.isBlank(grouperLoaderSqlGroupTypesName)) {
          
          grouperLoaderContainer.setEditLoaderGroupTypes(grouperLoaderSqlGroupTypesName);
          
        }
      }
      {
        String grouperLoaderLdapReadersName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapReadersName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapReadersName)) {
          
          grouperLoaderContainer.setEditLoaderLdapReaders(grouperLoaderLdapReadersName);
          
        }
      }
      {
        String grouperLoaderLdapViewersName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapViewersName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapViewersName)) {
          
          grouperLoaderContainer.setEditLoaderLdapViewers(grouperLoaderLdapViewersName);
          
        }
      }
      {
        String grouperLoaderLdapAdminsName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapAdminsName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapAdminsName)) {
          
          grouperLoaderContainer.setEditLoaderLdapAdmins(grouperLoaderLdapAdminsName);
          
        }
      }
      {
        String grouperLoaderLdapUpdatersName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapUpdatersName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapUpdatersName)) {
          
          grouperLoaderContainer.setEditLoaderLdapUpdaters(grouperLoaderLdapUpdatersName);
          
        }
      }
      {
        String grouperLoaderLdapOptinsName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapOptinsName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapOptinsName)) {
          
          grouperLoaderContainer.setEditLoaderLdapOptins(grouperLoaderLdapOptinsName);
          
        }
      }
      {
        String grouperLoaderLdapOptoutsName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapOptoutsName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapOptoutsName)) {
          
          grouperLoaderContainer.setEditLoaderLdapOptouts(grouperLoaderLdapOptoutsName);
          
        }
      }
      {
        String grouperLoaderLdapAttrReadersName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapAttrReadersName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapAttrReadersName)) {
          
          grouperLoaderContainer.setEditLoaderLdapAttrReaders(grouperLoaderLdapAttrReadersName);
          
        }
      }
      {
        String grouperLoaderLdapUpdatersName = StringUtils.trimToNull(request.getParameter("grouperLoaderLdapUpdatersName"));
        if (!error && !StringUtils.isBlank(grouperLoaderLdapUpdatersName)) {
          
          grouperLoaderContainer.setEditLoaderLdapAttrUpdaters(grouperLoaderLdapUpdatersName);
          
        }
      }
      
    }

    
  }


  /**
   * run diagnostics
   * @param request
   * @param response
   */
  public void loaderDiagnostics(HttpServletRequest request, HttpServletResponse response) {
    
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

      //not sure who can see attributes etc, just go root
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.startRootSession();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/grouperLoaderDiagnostics.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /** */
  private static final String INVALID_PROPERTIES_REGEX = "[^a-zA-Z0-9._-]";

  /**
   * run diagnostics
   * @param request
   * @param response
   */
  @SuppressWarnings("deprecation")
  public void loaderDiagnosticsRun(HttpServletRequest request, HttpServletResponse response) {
    
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

      //not sure who can see attributes etc, just go root
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.startRootSession();

      GrouperLoaderContainer grouperLoaderContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer();
      
      StringBuilder loaderReport = new StringBuilder();
      
      boolean fatal = false;
      
      loaderReport.append("<pre>\n");
      
      loaderReport.append("\n######## CONFIGURATION ########\n\n");
      
      boolean isLdap = grouperLoaderContainer.isGrouperLdapLoader();
      boolean isSql = grouperLoaderContainer.isGrouperSqlLoader();
      
      long groupsLikeCount = -1;
      if (!isLdap && !isSql) {
        loaderReport.append("<font color='red'>ERROR:</font> Not LDAP or SQL!\n");
        fatal = true;
      }
      if (isLdap && isSql) {
        loaderReport.append("<font color='red'>ERROR:</font> Is LDAP *and* SQL!\n");
        fatal = true;
      }

      GrouperLoaderType grouperLoaderType = null;
      if (!fatal) {
      
        try {
          grouperLoaderType = grouperLoaderContainer.getGrouperLoaderType();
          loaderReport.append("<font color='green'>SUCCESS:</font> grouperLoaderType is: " + grouperLoaderType.name() + "\n");
        } catch (Exception e) {
          loaderReport.append("<font color='red'>ERROR:</font> grouperLoaderType is invalid: " + ExceptionUtils.getFullStackTrace(e) + "\n");
          fatal = true;
        }
        if (grouperLoaderType == null) {
          loaderReport.append("<font color='red'>ERROR:</font> grouperLoaderType is null\n");
          fatal = true;
        }
      }
      
      if (!fatal && isLdap) {
        loaderReport.append("<font color='green'>SUCCESS:</font> This is an LDAP job\n");

        if (grouperLoaderType == GrouperLoaderType.LDAP_GROUP_LIST 
            || grouperLoaderType == GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES
            || grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE) {
          loaderReport.append("<font color='green'>SUCCESS:</font> grouperLoaderType " + grouperLoaderType + " is an LDAP type\n");
        } else {
          loaderReport.append("<font color='red'>ERROR:</font> grouperLoaderType is not valid for LDAP: '" + grouperLoaderType + "'\n");
          fatal = true;
        }

        if (!fatal) {
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapServerId())) {
            loaderReport.append("<font color='red'>ERROR:</font> LDAP server id is not set!\n");
            fatal = true;
          } else {
            if (!StringUtils.isBlank(grouperLoaderContainer.getLdapServerIdUrl())) {
              loaderReport.append("<font color='green'>SUCCESS:</font> LDAP server id: " 
                  + grouperLoaderContainer.getLdapServerId() + " was found in grouper-loader.properties\n");
              loaderReport.append("<font color='green'>SUCCESS:</font> LDAP server id points to url: " + 
                  grouperLoaderContainer.getLdapServerIdUrl() + "\n");
            } else {
              loaderReport.append("<font color='red'>ERROR:</font> LDAP server id: '" + 
                  grouperLoaderContainer.getLdapServerId() + "' is not found in grouper-loader.properties\n");
              fatal = true;
            }
          }
        }
        
        if (!fatal && StringUtils.isBlank(grouperLoaderContainer.getLdapLoaderFilter())) {
          loaderReport.append("<font color='red'>ERROR:</font> LDAP filter is not set!\n");
          fatal = true;
        }
        
        if (!fatal) {
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectAttributeName())) {
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES) {
              loaderReport.append("<font color='red'>ERROR:</font> LDAP subjectAttribute is not set and grouperLoaderType is " + grouperLoaderType + "!\n");
              fatal = true;
            }
          }

          if (!StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectAttributeName())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> LDAP subjectAttribute is set and grouperLoaderType is " + grouperLoaderType + "!\n");
          }
          
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupAttributeName())) {
            if (grouperLoaderType == GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES) {
              loaderReport.append("<font color='red'>ERROR:</font> LDAP groupAttribute is not set and grouperLoaderType is " + grouperLoaderType + "!\n");
              fatal = true;
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> LDAP groupAttribute is set\n");
            }
          }
          
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapCron())) {
            loaderReport.append("<font color='red'>ERROR:</font> Cron is not set!\n");
          } else {
            
            String grouperLoaderQuartzCron = grouperLoaderContainer.getLdapCron();
            
            try {
              String descripton = CronExpressionDescriptor.getDescription(grouperLoaderQuartzCron);
              loaderReport.append("<font color='green'>SUCCESS:</font> Cron '" + grouperLoaderQuartzCron 
                  + "' is set to: '" + descripton + "'\n");

            } catch (Exception e) {
              
              loaderReport.append("<font color='red'>ERROR:</font> cron is invalid!\n");
              loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
            }
          }
          
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapSourceId())) {
            loaderReport.append("<font color='orange'>WARNING:</font> sourceId is null, would be better performance if set\n");
          } else {
            try {
              Source source = SourceManager.getInstance().getSource(grouperLoaderContainer.getLdapSourceId());
              if (source != null) {
                
                loaderReport.append("<font color='green'>SUCCESS:</font> sourceId '" 
                    + grouperLoaderContainer.getLdapSourceId() + "' was found in the subject API\n");

              } else {

                loaderReport.append("<font color='red'>ERROR:</font> sourceId '" 
                    + grouperLoaderContainer.getLdapSourceId() + "' was not found in the subject API!\n");
                fatal = true;

              }
            } catch (Exception e) {
              LOG.info("sourceId '" 
                  + grouperLoaderContainer.getLdapSourceId() + "' was not found in the subject API", e);
              loaderReport.append("<font color='red'>ERROR:</font> sourceId '" 
                  + grouperLoaderContainer.getLdapSourceId() + "' was not found in the subject API!\n");
              fatal = true;

            }
          }
          
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectLookupType())) {

            loaderReport.append("<font color='green'>SUCCESS:</font> Subject type is not set so defaults to subjectId\n");

          } else {
            
            if (StringUtils.equals("subjectId", grouperLoaderContainer.getLdapSubjectLookupType())) {
              loaderReport.append("<font color='green'>SUCCESS:</font> Subject type is set to subjectId\n");
            } else if (StringUtils.equals("subjectIdentifier", grouperLoaderContainer.getLdapSubjectLookupType())) {
              loaderReport.append("<font color='orange'>WARNING:</font> Subject type is subjectIdentifier which is not as efficient as subjectId but maybe its not possible to use subjectId\n");
            } else if (StringUtils.equals("subjectIdOrIdentifier", grouperLoaderContainer.getLdapSubjectLookupType())) {
              loaderReport.append("<font color='orange'>WARNING:</font> Subject type is subjectIdOrIdentifier which is not as efficient as subjectId but maybe its not possible to use subjectId\n");
            } else {
              loaderReport.append("<font color='red'>ERROR:</font> Subject type is '" + grouperLoaderContainer.getLdapSubjectLookupType() + "', which is not found, should be subjectId, subjectIdentifier, or subjectIdOrIdentifier\n");
              fatal = true;
            }
          }
          
          if (StringUtils.isBlank(grouperLoaderContainer.getLdapSearchScope())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> search scope is not set and defaults to SUBTREE_SCOPE\n");
          } else if (StringUtils.equals("OBJECT_SCOPE", grouperLoaderContainer.getLdapSearchScope())
              || StringUtils.equals("ONELEVEL_SCOPE", grouperLoaderContainer.getLdapSearchScope())
              || StringUtils.equals("SUBTREE_SCOPE", grouperLoaderContainer.getLdapSearchScope())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Search scope is set to: " + grouperLoaderContainer.getLdapSearchScope() + "\n");
          } else {
            loaderReport.append("<font color='red'>ERROR:</font> Search scope is '" + grouperLoaderContainer.getLdapSubjectLookupType() + "', which is not found, should be OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE\n");
            fatal = true;
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapAndGroups())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> 'and groups' is not set\n");
          } else {
            if (grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE) {
              loaderReport.append("<font color='red'>ERROR:</font> 'and groups' is not valid for " + grouperLoaderType + "\n");
            }
            int count = 1;
            for (GuiGroup guiGroup : grouperLoaderContainer.getLdapAndGuiGroups()) {
              if (guiGroup.getGroup() == null) {
                loaderReport.append("<font color='red'>ERROR:</font> 'and group' number " 
                    + count + " was not found: '" + grouperLoaderContainer.getLdapAndGroups() + "'\n");

              } else {
                loaderReport.append("<font color='green'>SUCCESS:</font> 'and group' " 
                    + guiGroup.getGroup().getName() + " found\n");
              }
              count++;
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapPriority())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Scheduling priority is not set and defaults to medium: 5\n");
          } else {
            int priority = grouperLoaderContainer.getLdapPriorityInt();
            
            if (priority >=0) {
              loaderReport.append("<font color='green'>SUCCESS:</font> Scheduling priority is a valid integer: " + priority + "\n");
            } else {
              loaderReport.append("<font color='red'>ERROR:</font> Scheduling priority is not a valid integer: '" 
                  + grouperLoaderContainer.getLdapPriority() + "'\n");

            }
            
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupsLike())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> 'groups like' SQL config is not set\n");
          } else {
            if (grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE) {
              loaderReport.append("<font color='red'>ERROR:</font> 'groups like' SQL config is set but shouldnt be for " + grouperLoaderType + "\n");
              fatal = true;

            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> 'groups like' SQL config is set to '" 
                  + grouperLoaderContainer.getLdapGroupsLike() + "' for " + grouperLoaderType + "\n");

              groupsLikeCount = HibernateSession.byHqlStatic()
                  .createQuery("select count(*) from Group g where g.nameDb like :thePattern")
                  .setString("thePattern", grouperLoaderContainer.getLdapGroupsLike())
                  .uniqueResult(Long.class);
              if (groupsLikeCount == 0L) {
                loaderReport.append("<font color='red'>ERROR:</font> 'groups like' returned no records '" 
                    + grouperLoaderContainer.getLdapGroupsLike() + "'.  Either this job has never run or maybe its misconfigured?  Is that where groups are for this job????\n");
              } else {
                loaderReport.append("<font color='green'>SUCCESS:</font> 'groups like' returned " + groupsLikeCount + " groups for '" 
                    + grouperLoaderContainer.getLdapGroupsLike() + "'\n");
              }
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapExtraAttributes())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Extra attributes are not set\n");
          } else {
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUP_LIST) {
              loaderReport.append("<font color='red'>ERROR:</font> Extra attributes are set but shouldnt be for " + grouperLoaderType + "\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Extra attributes are set for " + grouperLoaderType + "\n");
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapAttributeFilterExpression())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Extra attributes filter expression is not set\n");
          } else {
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES) {
              loaderReport.append("<font color='red'>ERROR:</font> Extra attributes filter expression is set but shouldnt be for " + grouperLoaderType + "\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Extra attributes filter expression is set for " + grouperLoaderType + "\n");
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupNameExpression())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Group name expression is not set\n");
          } else {
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES 
                && grouperLoaderType != GrouperLoaderType.LDAP_GROUP_LIST) {
              loaderReport.append("<font color='red'>ERROR:</font> Group name expression is set but shouldnt be for " + grouperLoaderType + "\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Group name expression is set for " + grouperLoaderType + "\n");
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDisplayNameExpression())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Group display name expression is not set\n");
          } else {
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES 
                && grouperLoaderType != GrouperLoaderType.LDAP_GROUP_LIST) {
              loaderReport.append("<font color='red'>ERROR:</font> Group display name expression is set but shouldnt be for " + grouperLoaderType + "\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Group display name expression is set for " + grouperLoaderType + "\n");
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDescriptionExpression())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Group description expression is not set\n");
          } else {
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES 
                && grouperLoaderType != GrouperLoaderType.LDAP_GROUP_LIST) {
              loaderReport.append("<font color='red'>ERROR:</font> Group description expression is set but shouldnt be for " + grouperLoaderType + "\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Group description expression is set for " + grouperLoaderType + "\n");
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectExpression())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Subject expression is not set\n");
          } else {
            loaderReport.append("<font color='green'>SUCCESS:</font> Subject expression is set\n");
          }


          if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupTypes())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Group types are not set\n");
          } else {
            
            if (grouperLoaderType != GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES 
                && grouperLoaderType != GrouperLoaderType.LDAP_GROUP_LIST) {
              loaderReport.append("<font color='red'>ERROR:</font> Group types are set but shouldnt be for " + grouperLoaderType + "\n");
            }
            
            String groupTypesString = grouperLoaderContainer.getLdapGroupTypes();
            List<String> groupTypesList = GrouperUtil.splitTrimToList(groupTypesString, ",");
            
            for (String groupTypeString : groupTypesList) {
              try {
                GroupTypeFinder.find(groupTypeString, true);
                
                loaderReport.append("<font color='green'>SUCCESS:</font> Group type found: " + groupTypeString + "\n");

              } catch (Exception e) {

                loaderReport.append("<font color='red'>ERROR:</font> Group type not found: " + groupTypeString + "\n");

              }
            }
          }

          Map<String, String> privilegeMap = new LinkedHashMap<String, String>();
          privilegeMap.put("admins", grouperLoaderContainer.getLdapAdmins());
          privilegeMap.put("attrReaders", grouperLoaderContainer.getLdapAttrReaders());
          privilegeMap.put("attrUpdaters", grouperLoaderContainer.getLdapAttrUpdaters());
          privilegeMap.put("optins", grouperLoaderContainer.getLdapOptins());
          privilegeMap.put("optouts", grouperLoaderContainer.getLdapOptouts());
          privilegeMap.put("readers", grouperLoaderContainer.getLdapReaders());
          privilegeMap.put("updaters", grouperLoaderContainer.getLdapUpdaters());
          privilegeMap.put("viewers", grouperLoaderContainer.getLdapViewers());
          
          for (String privilegeName : privilegeMap.keySet()) {
            String subjectIdOrIdentifierStrings = privilegeMap.get(privilegeName);
            if (StringUtils.isBlank(subjectIdOrIdentifierStrings)) {
              
              loaderReport.append("<font color='green'>SUCCESS:</font> Group privilege " + privilegeName + " not set\n");

            } else {
              
              for (String subjectIdOrIdentifier : GrouperUtil.splitTrim(subjectIdOrIdentifierStrings, ",")) {
                try {
                  Subject subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, true);

                  loaderReport.append("<font color='green'>SUCCESS:</font> Subject found for privilege: " + privilegeName + ", " + GrouperUtil.subjectToString(subject) + "\n");

                } catch (Exception e) {
                  if (StringUtils.contains(subjectIdOrIdentifier, ':')) {

                    loaderReport.append("<font color='green'>SUCCESS:</font> Subject not found for privilege: " + privilegeName + ", but has a colon so its a new group\n");

                  } else {

                    //ignore I guess
                    loaderReport.append("<font color='green'>Error:</font> Subject not found for privilege: " + privilegeName + ", '" + subjectIdOrIdentifier + "'!\n");
                    
                  }
                }
              }
            }
          }
        }
        
        //check filter
        if (!fatal) {
        
          loaderReport.append("\n######## CHECKING FILTER ########\n\n");

          LdapSearchScope ldapSearchScopeEnum = null;          
          {
            String ldapSearchScope = GrouperUtil.defaultString(grouperLoaderContainer.getLdapSearchScope(), "SUBTREE_SCOPE");
            ldapSearchScopeEnum = LdapSearchScope.valueOfIgnoreCase(ldapSearchScope, false);
          }

          switch (grouperLoaderType) {
            case LDAP_GROUP_LIST:
              
              diagnosticsTryLdapGroupList(grouperLoaderContainer, group, loaderReport, ldapSearchScopeEnum, groupsLikeCount);
              
              break;
            case LDAP_GROUPS_FROM_ATTRIBUTES:
              
              diagnosticsTryLdapGroupsFromAttributes(grouperLoaderContainer, group, loaderReport, ldapSearchScopeEnum, groupsLikeCount);
              
              break;
            case LDAP_SIMPLE:
              List<String> results = null;
              long startNanos = System.nanoTime();
              try {
                results = LdapSession.list(String.class, grouperLoaderContainer.getLdapServerId(), grouperLoaderContainer.getLdapSearchDn(),
                    ldapSearchScopeEnum, grouperLoaderContainer.getLdapLoaderFilter(), grouperLoaderContainer.getLdapSubjectAttributeName());
                if (GrouperUtil.length(results) > 0) {
                  loaderReport.append("<font color='green'>SUCCESS:</font> Ran filter, got " + GrouperUtil.length(results) 
                      + " results in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms \n");
                } else {
                  loaderReport.append("<font color='red'>ERROR:</font> Ran filter, got 0 results.  Generally this should not happen\n");
                  fatal = true;
                }
              } catch (Exception e) {
                loaderReport.append("<font color='red'>ERROR:</font> Could not run filter, searchDn, scope, subject attribute!\n");
                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                fatal = true;
              }

              if (!fatal && GrouperUtil.length(results) > 0) {
                String firstResult = results.get(0);
                
                grouperLoaderFindSubject(loaderReport, firstResult, grouperLoaderContainer.getLdapSubjectExpression(), 
                    grouperLoaderContainer.getLdapSourceId(), grouperLoaderContainer.getLdapSubjectLookupType());

              }
              
              break;
            default: 
              throw new RuntimeException("Cant find grouperLoaderType: " + grouperLoaderType);
          }
          
        }
        
      } else if (!fatal && isSql) {
        loaderReport.append("<font color='green'>SUCCESS:</font> This is a SQL job\n");
        
        if (grouperLoaderType == GrouperLoaderType.SQL_SIMPLE 
            || grouperLoaderType == GrouperLoaderType.SQL_GROUP_LIST) {
          loaderReport.append("<font color='green'>SUCCESS:</font> grouperLoaderType " + grouperLoaderType + " is a SQL type\n");
        } else {
          loaderReport.append("<font color='red'>ERROR:</font> grouperLoaderType is not valid for SQL: '" + grouperLoaderType + "'\n");
          fatal = true;
        }

        if (!fatal) {
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlDatabaseName())) {
            loaderReport.append("<font color='red'>ERROR:</font> SQL database name is not set!\n");
            fatal = true;
          } else {
            if (StringUtils.equals("grouper", grouperLoaderContainer.getSqlDatabaseName())) {
              loaderReport.append("<font color='green'>SUCCESS:</font> SQL database name is 'grouper' which uses the Grouper database connection: "
                  + grouperLoaderContainer.getSqlDatabaseNameUrl() + "\n");
            } else if (!StringUtils.isBlank(grouperLoaderContainer.getSqlDatabaseNameUrl())) {
              loaderReport.append("<font color='green'>SUCCESS:</font> SQL database name: " 
                  + grouperLoaderContainer.getSqlDatabaseName() + " was found in grouper-loader.properties\n");
              loaderReport.append("<font color='green'>SUCCESS:</font> SQL database name points to connect string: " + 
                  grouperLoaderContainer.getSqlDatabaseNameUrl() + "\n");
            } else {
              loaderReport.append("<font color='red'>ERROR:</font> SQL database name: '" + 
                  grouperLoaderContainer.getSqlDatabaseName() + "' is not found in grouper-loader.properties\n");
              fatal = true;
            }
          }
        }
        
        if (!fatal) {
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlQuery())) {
            loaderReport.append("<font color='red'>ERROR:</font> SQL query is not set!\n");
            fatal = true;
          } else {
            loaderReport.append("<font color='blue'>NOTE:</font> SQL query is set to '" + grouperLoaderContainer.getSqlQuery() + "'\n");
          }
        } 
        
        if (!fatal) {
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlAndGroups())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> 'and groups' is not set\n");
          } else {
            if (grouperLoaderType == GrouperLoaderType.SQL_SIMPLE) {
              loaderReport.append("<font color='red'>ERROR:</font> 'and groups' is not valid for " + grouperLoaderType + "\n");
            }
            int count = 1;
            for (GuiGroup guiGroup : grouperLoaderContainer.getSqlAndGuiGroups()) {
              if (guiGroup.getGroup() == null) {
                loaderReport.append("<font color='red'>ERROR:</font> 'and group' number " 
                    + count + " was not found: '" + grouperLoaderContainer.getSqlAndGroups() + "'\n");
  
              } else {
                loaderReport.append("<font color='green'>SUCCESS:</font> 'and group' " 
                    + guiGroup.getGroup().getName() + " found\n");
              }
              count++;
            }
          }
        }
        
        if (!fatal) {
          GrouperLoaderScheduleType grouperLoaderScheduleType = null;
          
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlScheduleType())) {
            loaderReport.append("<font color='red'>ERROR:</font> Schedule type is not set!\n");
          } else {
            
            String scheduleType = grouperLoaderContainer.getSqlScheduleType();
            try {
              grouperLoaderScheduleType = GrouperLoaderScheduleType.valueOfIgnoreCase(scheduleType, true);
  
              loaderReport.append("<font color='green'>SUCCESS:</font> Schedule type correctly set to: " + grouperLoaderScheduleType.name() + "\n");
              
            } catch (Exception e) {
  
              loaderReport.append("<font color='red'>ERROR:</font> Invalid schedule type: " + scheduleType + ", should be CRON or START_TO_START_INTERVAL\n");
              
            }
          }
          
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlCron())) {
            if (grouperLoaderScheduleType == GrouperLoaderScheduleType.CRON) {
              loaderReport.append("<font color='red'>ERROR:</font> Cron schedule is not set and schedule type is CRON!\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Cron schedule is not set and schedule type is " + grouperLoaderScheduleType + "\n");
            }
          } else {
            
            if (grouperLoaderScheduleType != GrouperLoaderScheduleType.CRON) {
              loaderReport.append("<font color='red'>ERROR:</font> Cron schedule is set and schedule type is not CRON! " + grouperLoaderScheduleType + "\n");
            }
  
            String grouperLoaderQuartzCron = grouperLoaderContainer.getSqlCron();
            
            try {
              String descripton = CronExpressionDescriptor.getDescription(grouperLoaderQuartzCron);
              loaderReport.append("<font color='green'>SUCCESS:</font> Cron '" + grouperLoaderQuartzCron 
                  + "' is set to: '" + descripton + "'\n");
  
            } catch (Exception e) {
              
              loaderReport.append("<font color='red'>ERROR:</font> cron is invalid!\n");
              loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
            }
          }
  
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlScheduleInterval())) {
            if (grouperLoaderScheduleType == GrouperLoaderScheduleType.START_TO_START_INTERVAL) {
              loaderReport.append("<font color='red'>ERROR:</font> Schedule interval is not set and schedule type is START_TO_START_INTERVAL!\n");
            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> Schedule interval is not set and schedule type is " + grouperLoaderScheduleType + "\n");
            }
          } else {
            
            if (grouperLoaderScheduleType != GrouperLoaderScheduleType.START_TO_START_INTERVAL) {
              loaderReport.append("<font color='red'>ERROR:</font> Cron schedule is set and schedule type is not START_TO_START_INTERVAL! " + grouperLoaderScheduleType + "\n");
            }
            
            if (grouperLoaderContainer.getSqlScheduleIntervalSecondsTotal() > 0) {
              loaderReport.append("<font color='green'>SUCCESS:</font> Schedule interval is set to a valid integer '" 
                  + grouperLoaderContainer.getSqlScheduleIntervalSecondsTotal() + "', " + grouperLoaderContainer.getSqlScheduleIntervalHumanReadable() + "\n");
            } else {
              loaderReport.append("<font color='red'>ERROR:</font> Schedule interval is not set to a valid integer '" + grouperLoaderContainer.getSqlScheduleInterval() + "'\n");
            }
            
          }
          if (StringUtils.isBlank(grouperLoaderContainer.getSqlGroupQuery())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> SQL group query is not set!\n");
          } else {
            if (grouperLoaderType == GrouperLoaderType.SQL_SIMPLE) {
              loaderReport.append("<font color='red'>ERROR:</font> SQL group query should not be set for " + grouperLoaderType + "\n");
            } else {
              loaderReport.append("<font color='blue'>NOTE:</font> SQL group query is set to '" + grouperLoaderContainer.getSqlGroupQuery() + "'\n");
            }
          }

          if (StringUtils.isBlank(grouperLoaderContainer.getSqlGroupsLike())) {
            loaderReport.append("<font color='green'>SUCCESS:</font> 'groups like' SQL config is not set\n");
          } else {
            if (grouperLoaderType == GrouperLoaderType.SQL_SIMPLE) {
              loaderReport.append("<font color='red'>ERROR:</font> 'groups like' SQL config is set but shouldnt be for " + grouperLoaderType + "\n");

            } else {
              loaderReport.append("<font color='green'>SUCCESS:</font> 'groups like' SQL config is set to '" 
                  + grouperLoaderContainer.getSqlGroupsLike() + "' for " + grouperLoaderType + "\n");
              
              groupsLikeCount = HibernateSession.byHqlStatic()
                  .createQuery("select count(*) from Group g where g.nameDb like :thePattern")
                  .setString("thePattern", grouperLoaderContainer.getSqlGroupsLike())
                  .uniqueResult(Long.class);
              if (groupsLikeCount == 0L) {
                loaderReport.append("<font color='red'>ERROR:</font> 'groups like' returned no records '" 
                    + grouperLoaderContainer.getSqlGroupsLike() + "'.  Either this job has never run or maybe its misconfigured?  Is that where groups are for this job????\n");
              } else {
                loaderReport.append("<font color='green'>SUCCESS:</font> 'groups like' returned " + groupsLikeCount + " groups for '" 
                    + grouperLoaderContainer.getSqlGroupsLike() + "'\n");
              }
            }
          }
        }

        if (StringUtils.isBlank(grouperLoaderContainer.getSqlGroupTypes())) {
          loaderReport.append("<font color='green'>SUCCESS:</font> Group types are not set\n");
        } else {
          
          if (grouperLoaderType != GrouperLoaderType.SQL_GROUP_LIST ) {
            loaderReport.append("<font color='red'>ERROR:</font> Group types are set but shouldnt be for " + grouperLoaderType + "\n");
          }
          
          String groupTypesString = grouperLoaderContainer.getSqlGroupTypes();
          List<String> groupTypesList = GrouperUtil.splitTrimToList(groupTypesString, ",");
          
          for (String groupTypeString : groupTypesList) {
            try {
              GroupTypeFinder.find(groupTypeString, true);
              
              loaderReport.append("<font color='green'>SUCCESS:</font> Group type found: " + groupTypeString + "\n");

            } catch (Exception e) {

              loaderReport.append("<font color='red'>ERROR:</font> Group type not found: " + groupTypeString + "\n");

            }
          }
        }
        
        if (StringUtils.isBlank(grouperLoaderContainer.getSqlPriority())) {
          loaderReport.append("<font color='green'>SUCCESS:</font> Scheduling priority is not set and defaults to medium: 5\n");
        } else {
          int priority = grouperLoaderContainer.getSqlPriorityInt();
          
          if (priority >=0) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Scheduling priority is a valid integer: " + priority + "\n");
          } else {
            loaderReport.append("<font color='red'>ERROR:</font> Scheduling priority is not a valid integer: '" 
                + grouperLoaderContainer.getSqlPriority() + "'\n");
          }
        }
        
        //check filter
        GrouperLoaderDb grouperLoaderDb = null;
        
        if (!fatal) {
        
          loaderReport.append("\n######## CHECKING QUERIES ########\n\n");

          try {
            grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(grouperLoaderContainer.getSqlDatabaseName());
            if (grouperLoaderDb == null) {
              throw new NullPointerException();
            }
            loaderReport.append("<font color='green'>SUCCESS:</font> Found DB profile for: '" 
                + grouperLoaderContainer.getSqlDatabaseName() + "'\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Cannot retrieve DB profile for: '" 
                + grouperLoaderContainer.getSqlDatabaseName() + "'\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e));
            fatal = true;
          }
        }

        GrouperLoaderResultset grouperLoaderResultset = null;
        if (!fatal) {
          long startNanos = System.nanoTime();
  
          try {
            grouperLoaderResultset = new GrouperLoaderResultset(
                grouperLoaderDb, grouperLoaderContainer.getSqlQuery() + (grouperLoaderType == GrouperLoaderType.SQL_GROUP_LIST ? " order by group_name" : ""), 
                grouperLoaderContainer.getJobName(), 
                new Hib3GrouperLoaderLog());
            loaderReport.append("<font color='green'>SUCCESS:</font> Ran query, got " + grouperLoaderResultset.numberOfRows()
                + " results in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Error running query in " 
                + ((System.nanoTime() - startNanos) / 1000000L) + "ms\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e));
            fatal=true;
          }
  
        }

        Set<String> columnNames = new LinkedHashSet<String>();
        String subjectCol = null;
        if (!fatal) {
          if (grouperLoaderResultset.numberOfRows() == 0) {
            loaderReport.append("<font color='red'>ERROR:</font> Query returned 0 records, which might be ok, but generally there should be results\n");
          }
          
          
          boolean foundSubjectIdCol = false;
          for (String columnName : GrouperUtil.nonNull(grouperLoaderResultset.getColumnNames())) {
            columnName = columnName.toUpperCase();
            columnNames.add(columnName);
            
            if (StringUtils.equalsIgnoreCase(columnName, "SUBJECT_ID")) {
              if (!foundSubjectIdCol) {

                loaderReport.append("<font color='green'>SUCCESS:</font> Found SUBJECT_ID col\n");
                subjectCol = "SUBJECT_ID";
              } else {
                loaderReport.append("<font color='red'>ERROR:</font> Found SUBJECT_ID col, but already found a subject col!\n");
                fatal = true;
                
              }
              foundSubjectIdCol = true;
            } else if (StringUtils.equalsIgnoreCase(columnName, "SUBJECT_IDENTIFIER")) {
              if (!foundSubjectIdCol) {

                loaderReport.append("<font color='orange'>WARNING:</font> Found SUBJECT_IDENTIFIER col, which is fine, but SUBJECT_ID col is has better performance if possible to use\n");
                subjectCol = "SUBJECT_IDENTIFIER";

              } else {
                loaderReport.append("<font color='red'>ERROR:</font> Found SUBJECT_IDENTIFIER col, but already found a subject col!\n");
                fatal = true;
                
              }
              foundSubjectIdCol = true;
            } else if (StringUtils.equalsIgnoreCase(columnName, "SUBJECT_ID_OR_IDENTIFIER")) {
              if (!foundSubjectIdCol) {

                loaderReport.append("<font color='orange'>WARNING:</font> Found SUBJECT_ID_OR_IDENTIFIER col, which is fine, but SUBJECT_ID col is has better performance if possible to use\n");
                subjectCol = "SUBJECT_IDENTIFIER";

              } else {
                loaderReport.append("<font color='red'>ERROR:</font> Found SUBJECT_ID_OR_IDENTIFIER col, but already found a subject col!\n");
                fatal = true;
                
              }
              foundSubjectIdCol = true;              
            } else if (!StringUtils.equals("GROUP_NAME", columnName) && !StringUtils.equals("SUBJECT_SOURCE_ID", columnName)) {
              loaderReport.append("<font color='orange'>WANING:</font> Found " + columnName + " col, which is not used by grouper\n");
              
            }
          }

          if (!foundSubjectIdCol) {
            loaderReport.append("<font color='red'>ERROR:</font> Did not find subject column!  Should have a column SUBJECT_ID, SUBJECT_IDENTIFIER, or SUBJECT_ID_OR_IDENTIFIER\n");
            fatal = true;
          }

          if (!columnNames.contains("SUBJECT_SOURCE_ID")) {
            loaderReport.append("<font color='orange'>WARNING:</font> Did not find col: SUBJECT_SOURCE_ID, this column improves performance\n");
            
          }
          
        }
        
        if (!fatal && grouperLoaderResultset.numberOfRows() > 0) {
          String subjectId = (String)grouperLoaderResultset.getCell(0, subjectCol, true);
          String sourceId = null;
          if (columnNames.contains("SUBJECT_SOURCE_ID")) {
            sourceId = (String)grouperLoaderResultset.getCell(0, "SUBJECT_SOURCE_ID", true);
          }
          grouperLoaderFindSubject(loaderReport, subjectId, null, 
              sourceId, subjectCol);
        }
        
        
        if (!fatal) {
          switch (grouperLoaderType) {
            case SQL_SIMPLE:
              
              if (columnNames.contains("GROUP_NAME")) {
                loaderReport.append("<font color='red'>ERROR:</font> A SQL_SIMPLE job should not have a GROUP_NAME column\n");
                
              } else {
                loaderReport.append("<font color='green'>SUCCESS:</font> This SQL_SIMPLE job does not have a GROUP_NAME column\n");
                
              }
              break;
            case SQL_GROUP_LIST:
              if (!columnNames.contains("GROUP_NAME")) {
                loaderReport.append("<font color='red'>ERROR:</font> A SQL_GROUP_LIST job must have a GROUP_NAME column\n");
                fatal = true;
                
              } else {
                loaderReport.append("<font color='green'>SUCCESS:</font> This SQL_GROUP_LIST job does have a GROUP_NAME column\n");
                
              }
              
              GrouperLoaderResultset grouperLoaderResultsetForGroups = null;
              if (!fatal) {
                if (StringUtils.isBlank(grouperLoaderContainer.getSqlGroupQuery())) {

                  loaderReport.append("<font color='blue'>NOTE:</font> Not running group query since its not configured\n");

                } else {
                  long startNanos = System.nanoTime();

                  try {
                    grouperLoaderResultsetForGroups = new GrouperLoaderResultset(
                        grouperLoaderDb, grouperLoaderContainer.getSqlGroupQuery() + " order by group_name", 
                        grouperLoaderContainer.getJobName(), 
                        new Hib3GrouperLoaderLog());
                    loaderReport.append("<font color='green'>SUCCESS:</font> Ran group query, got " + grouperLoaderResultset.numberOfRows()
                        + " results in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms\n");
                  } catch (Exception e) {
                    loaderReport.append("<font color='red'>ERROR:</font> Error running group query in " 
                        + ((System.nanoTime() - startNanos) / 1000000L) + "ms\n");
                    loaderReport.append(ExceptionUtils.getFullStackTrace(e));
                    fatal=true;
                  }
                  
                  Set<String> columnNamesForGroups = new LinkedHashSet<String>();
                  
                  if (!fatal) {
                    if (grouperLoaderResultsetForGroups.numberOfRows() == 0) {
                      loaderReport.append("<font color='red'>ERROR:</font> Group query returned 0 records, which might be ok, but generally there should be results\n");
                    }
                  }
                  
                  if (!fatal) {
                    
                    for (String columnNameForGroup : GrouperUtil.nonNull(grouperLoaderResultsetForGroups.getColumnNames())) {
                      columnNameForGroup = columnNameForGroup.toUpperCase();
                      columnNamesForGroups.add(columnNameForGroup);

                      if (!StringUtils.equals("GROUP_NAME", columnNameForGroup) && !StringUtils.equals("GROUP_DISPLAY_NAME", columnNameForGroup)
                          && !StringUtils.equals("GROUP_DESCRIPTION", columnNameForGroup) && !StringUtils.equals("VIEWERS", columnNameForGroup)
                          && !StringUtils.equals("ADMINS", columnNameForGroup) && !StringUtils.equals("UPDATERS", columnNameForGroup)
                          && !StringUtils.equals("READERS", columnNameForGroup)
                          && !StringUtils.equals("OPTINS", columnNameForGroup) && !StringUtils.equals("OPTOUTS", columnNameForGroup)
                          && !StringUtils.equals("GROUP_ATTR_READERS", columnNameForGroup) && !StringUtils.equals("GROUP_ATTR_UPDATERS", columnNameForGroup)
                          
                          ) {
                        loaderReport.append("<font color='orange'>WARNING:</font> Found " + columnNameForGroup + " group query col, which is not used by grouper\n");
                      }                      
                    }
                    String groupName = null;
                    if (columnNamesForGroups.contains("GROUP_NAME")) {

                      loaderReport.append("<font color='green'>SUCCESS:</font> Found GROUP_NAME col in group query\n");
                      
                      if (grouperLoaderResultsetForGroups.numberOfRows() > 0) {
                       
                        groupName = (String)grouperLoaderResultsetForGroups.getCell(0, "GROUP_NAME", true);
                        
                        if (StringUtils.isBlank(groupName)) {
                          loaderReport.append("<font color='red'>ERROR:</font> GROUP_NAME is blank in group query!\n");
                        } else {
                          if (groupName.contains(":")) {
                            loaderReport.append("<font color='green'>SUCCESS:</font> GROUP_NAME exists and contains a colon: '" + groupName + "'\n");
                          } else {
                            loaderReport.append("<font color='red'>ERROR:</font> GROUP_NAME should contain at least one colon in group query! (for folders)\n");
                          }
                        }
                      }
                      
                    } else {
                      loaderReport.append("<font color='red'>ERROR:</font> Didn't find GROUP_NAME col in group query!\n");
                      fatal = true;
                      
                    }
                    if (!fatal) {
                      if (columnNamesForGroups.contains("GROUP_DISPLAY_NAME")) {

                        loaderReport.append("<font color='green'>SUCCESS:</font> Found GROUP_DISPLAY_NAME col in group query\n");
                        
                        if (grouperLoaderResultsetForGroups.numberOfRows() > 0) {

                          String groupDisplayName = (String)grouperLoaderResultsetForGroups.getCell(0, "GROUP_DISPLAY_NAME", true);

                          if (StringUtils.isBlank(groupDisplayName)) {
                            loaderReport.append("<font color='red'>ERROR:</font> GROUP_DISPLAY_NAME is blank in group query!\n");
                          } else {
                            if (groupDisplayName.contains(":")) {
                              loaderReport.append("<font color='green'>SUCCESS:</font> GROUP_DISPLAY_NAME exists and contains a colon: '" + groupDisplayName + "'\n");
                              int groupNameNumberOfColons = StringUtils.countMatches(groupName, ":");
                              int groupDisplayNameNumberOfColons = StringUtils.countMatches(groupDisplayName, ":");
                              if (groupNameNumberOfColons != groupDisplayNameNumberOfColons) {
                                loaderReport.append("<font color='red'>ERROR:</font> GROUP_DISPLAY_NAME has " + groupDisplayNameNumberOfColons 
                                    + ", and GROUP_NAME has " + groupNameNumberOfColons + " colons\n");
                              } else {
                                loaderReport.append("<font color='green'>SUCCESS:</font> GROUP_DISPLAY_NAME has " + groupDisplayNameNumberOfColons 
                                    + ", and GROUP_NAME also has " + groupNameNumberOfColons + " colons\n");
                              }
                            } else {
                              loaderReport.append("<font color='red'>ERROR:</font> GROUP_DISPLAY_NAME should contain at least one colon in group query! (for folders)\n");
                            }
                          }
                        }
                      } else {
                        loaderReport.append("<font color='blue'>NOTE:</font> Didn't find GROUP_DISPLAY_NAME col in group query, will set the display name to be the same as the group name: '" + groupName + "'\n");
                      }

                      if (columnNamesForGroups.contains("GROUP_DESCRIPTION")) {

                        loaderReport.append("<font color='green'>SUCCESS:</font> Found GROUP_DESCRIPTION col in group query\n");
                        
                        if (grouperLoaderResultsetForGroups.numberOfRows() > 0) {

                          String groupDescription = (String)grouperLoaderResultsetForGroups.getCell(0, "GROUP_DESCRIPTION", true);

                          loaderReport.append("<font color='green'>SUCCESS:</font> GROUP_DESCRIPTION exists '" + groupDescription + "'\n");
                        }
                      } else {
                        loaderReport.append("<font color='blue'>NOTE:</font> Didn't find GROUP_DESCRIPTION col in group query");
                      }

                      for (String privilegeColumn : new String[]{"VIEWERS", "READERS", "ADMINS", "UPDATERS", "OPTINS", "OPTOUTS", "GROUP_ATTR_READERS", "GROUP_ATTR_UPDATERS"}) {
                        if (columnNamesForGroups.contains(privilegeColumn)) {

                          loaderReport.append("<font color='green'>SUCCESS:</font> Found " + privilegeColumn + " col in group query\n");
                          
                          if (grouperLoaderResultsetForGroups.numberOfRows() > 0) {

                            String privilegeData = (String)grouperLoaderResultsetForGroups.getCell(0, privilegeColumn, true);

                            if (!StringUtils.isBlank(privilegeData)) {
                              
                              loaderReport.append("<font color='green'>SUCCESS:</font> " + privilegeColumn + " data exists '" + privilegeData + "'\n");

                              
                              for (String subjectIdOrIdentifier : GrouperUtil.splitTrim(privilegeData, ",")) {
                                try {
                                  Subject subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, true);

                                  loaderReport.append("<font color='green'>SUCCESS:</font> Subject found for privilege: " + privilegeColumn + ", " + GrouperUtil.subjectToString(subject) + "\n");

                                } catch (Exception e) {
                                  if (StringUtils.contains(subjectIdOrIdentifier, ':')) {

                                    loaderReport.append("<font color='green'>SUCCESS:</font> Subject not found for privilege: " + privilegeColumn + ", but has a colon so its a new group\n");

                                  } else {

                                    //ignore stack I guess
                                    loaderReport.append("<font color='red'>Error:</font> Subject not found for privilege: " + privilegeColumn + ", '" + subjectIdOrIdentifier + "'!\n");
                                    
                                  }
                                }
                              }
                              
                            } else {

                              loaderReport.append("<font color='gren'>SUCCESS:</font> " + privilegeColumn + " data doesnt exist in first row\n");

                            }
                          }
                        } else {
                          loaderReport.append("<font color='blue'>NOTE:</font> Didn't find " + privilegeColumn + " col in group query\n");
                        }
                        
                      }
                      
                    }
                  }
                  if (groupsLikeCount == 0) {
                    
                    if (grouperLoaderResultsetForGroups.numberOfRows() > 0) {
                      // see if the groups like count is similar to the number of groups returned
                      loaderReport.append("<font color='red'>ERROR:</font> 0 groups in 'groups like' and " + grouperLoaderResultsetForGroups.numberOfRows() 
                          + " groups in SQL group query, maybe job hasnt been run yet?  Or groupsLike '" 
                          + grouperLoaderContainer.getSqlGroupsLike() + "' is misconfigured?\n");
                    }
                    
                  } else if (groupsLikeCount == grouperLoaderResultsetForGroups.numberOfRows()) {
                    loaderReport.append("<font color='green'>SUCCESS:</font> " + groupsLikeCount + " groups in 'groups like' and " 
                        + grouperLoaderResultsetForGroups.numberOfRows() + " groups in SQL group query are the same number!\n");
                    
                  } else if (groupsLikeCount > 0) {

                    double percentOff = Math.abs(groupsLikeCount - grouperLoaderResultsetForGroups.numberOfRows()) / groupsLikeCount;
                    if (percentOff > 0.1) {
                      loaderReport.append("<font color='red'>ERROR:</font> " + groupsLikeCount + " groups in 'groups like' and " 
                          + grouperLoaderResultsetForGroups.numberOfRows() + " groups in SQL group query more than 10% away from each other.  Maybe job needs to be run?  Or groupsLike '" 
                          + grouperLoaderContainer.getSqlGroupsLike() + "' is misconfigured?\n");
                    } else {
                      loaderReport.append("<font color='green'>SUCCESS:</font> " + groupsLikeCount + " groups in 'groups like' and " 
                          + grouperLoaderResultsetForGroups.numberOfRows() + " groups in SQL group query are within 10%\n");
                    }
                  }
                }
              }
                
              break;
            default: 
              throw new RuntimeException("Cant find grouperLoaderType: " + grouperLoaderType);
          }
          
        }

      }
      
      if (!fatal) {
        loaderReport.append("\n######## CHECKING LOGS ########\n\n");
  
        {
          List<Criterion> criterionList = new ArrayList<Criterion>();
          
          String jobName = grouperLoaderContainer.getJobName();
          
          criterionList.add(Restrictions.eq("jobName", jobName));
          criterionList.add(Restrictions.eq("status", "SUCCESS"));
    
          int maxRows = 1000;
          QueryOptions queryOptions = QueryOptions.create("lastUpdated", false, 1, maxRows);
          
          Criterion allCriteria = HibUtils.listCrit(criterionList);
          
          List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
            .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);
    
          if (GrouperUtil.length(loaderLogs) == 0) {
            loaderReport.append("<font color='red'>ERROR:</font> Cannot find a recent success in grouper_loader_log for job name: " + jobName + "\n");
          } else if (GrouperUtil.length(loaderLogs) >= maxRows ) {
            loaderReport.append("<font color='green'>SUCCESS:</font> Found more than " + maxRows + " successes in grouper_loader_log for job name: " + jobName + "\n");
          } else {
            loaderReport.append("<font color='green'>SUCCESS:</font> Found " + GrouperUtil.length(loaderLogs) + " successes in grouper_loader_log for job name: " + jobName + "\n");
          }
          
          if (GrouperUtil.length(loaderLogs) > 0) {
            Hib3GrouperLoaderLog hib3GrouperLoaderLog = loaderLogs.get(0);
            
                      
            loaderSuccessFromLogs(loaderReport, jobName, hib3GrouperLoaderLog, true);
          }
        }
        
      }
      
      if (!fatal) {
        List<Criterion> criterionList = new ArrayList<Criterion>();
        
        String jobName = grouperLoaderContainer.getJobName();
        
        criterionList.add(Restrictions.eq("jobName", jobName));
        criterionList.add(Restrictions.in("status", new String[]{"ERROR", "CONFIG_ERROR", "SUBJECT_PROBLEMS", "WARNING"}));

        int maxRows = 1000;
        QueryOptions queryOptions = QueryOptions.create("lastUpdated", false, 1, maxRows);
        
        Criterion allCriteria = HibUtils.listCrit(criterionList);
        
        List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
          .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);

        if (GrouperUtil.length(loaderLogs) == 0) {
          loaderReport.append("<font color='green'>SUCCESS:</font> Found no errors in grouper_loader_log for job name: " + jobName + "\n");
        } else {
          loaderReport.append("<font color='orange'>WARNING:</font> Found " + GrouperUtil.length(loaderLogs) 
              + " errors in grouper_loader_log for job name: " + jobName + "\n");
          Hib3GrouperLoaderLog hib3GrouperLoaderLog = loaderLogs.get(0);
          if (hib3GrouperLoaderLog.getLastUpdated() == null || hib3GrouperLoaderLog.getLastUpdated().getTime() > System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3)) {
            loaderReport.append("<font color='red'>ERROR:</font> Found an error in grouper_loader_log for job name: " + jobName + " within the last 3 days\n");
            if (!StringUtils.isBlank(hib3GrouperLoaderLog.getJobMessage())) {
              loaderReport.append(hib3GrouperLoaderLog.getJobMessage() + "\n");
            }
          } else {
            loaderReport.append("<font color='orange'>WARNING:</font> Most recent error in grouper_loader_log for job name: " + jobName + " was longer ago than 3 days\n");
          }
        }
      }      

      if (!fatal && grouperLoaderContainer.isHasSubjobs()) {
  
        List<Criterion> criterionList = new ArrayList<Criterion>();
        
        String jobName = grouperLoaderContainer.getJobName();
        
        criterionList.add(Restrictions.eq("parentJobName", jobName));
        criterionList.add(Restrictions.in("status", new String[]{"ERROR", "CONFIG_ERROR", "SUBJECT_PROBLEMS", "WARNING"}));

        int maxRows = 1000;
        QueryOptions queryOptions = QueryOptions.create("lastUpdated", false, 1, maxRows);
        
        Criterion allCriteria = HibUtils.listCrit(criterionList);
        
        List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
          .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);
    
        if (GrouperUtil.length(loaderLogs) == 0) {
          loaderReport.append("<font color='green'>SUCCESS:</font> Found no errors in grouper_loader_log for subjobs of job name: " + jobName + "\n");
        } else {
          loaderReport.append("<font color='orange'>WARNING:</font> Found " + GrouperUtil.length(loaderLogs) 
              + " errors in grouper_loader_log for subjobs of job name: " + jobName + "\n");
          Hib3GrouperLoaderLog hib3GrouperLoaderLog = loaderLogs.get(0);
          if (hib3GrouperLoaderLog.getLastUpdated() == null || hib3GrouperLoaderLog.getLastUpdated().getTime() > System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3)) {
            loaderReport.append("<font color='red'>ERROR:</font> Found an error in grouper_loader_log for subjob of job name: " + jobName + " within the last 3 days\n");
            if (!StringUtils.isBlank(hib3GrouperLoaderLog.getJobMessage())) {
              loaderReport.append(hib3GrouperLoaderLog.getJobMessage() + "\n");
            }
          } else {
            loaderReport.append("<font color='orange'>WARNING:</font> Most recent error in grouper_loader_log for subjobs of job name: " + jobName + " was longer ago than 3 days\n");
          }
        }
      }
      
      loaderReport.append("</pre>");
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#grouperLoaderDiagnosticsResults", loaderReport.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * 
   * @param loaderReport
   * @param jobName
   * @param hib3GrouperLoaderLog
   * @param runningFromDiagnostics
   * @return if success
   */
  private static boolean loaderSuccessFromLogs(StringBuilder loaderReport, String jobName,
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, boolean runningFromDiagnostics) {
    
    if (hib3GrouperLoaderLog == null) {
      loaderReport.append((runningFromDiagnostics ? "<font color='red'>ERROR:</font> " : "") + "Can't find a success in grouper_loader_log for job name: " + jobName);
      return false;
    }
    
    //default of last success is usually 25 hours, but can be less for change log jobs
    int minutesSinceLastSuccess = -1;
 
    //for these, also accept with no uuid
     
    int underscoreIndex = jobName.lastIndexOf("__");
    
    if (underscoreIndex != -1) {
      
      String jobNameWithoutUuid = jobName.substring(0, underscoreIndex);
      jobNameWithoutUuid = jobNameWithoutUuid.replaceAll(INVALID_PROPERTIES_REGEX, "_");
      minutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.minutesSinceLastSuccess." + jobNameWithoutUuid, -1);
      
    }
    
    //try with full job name
    if (minutesSinceLastSuccess == -1) {
      String configName = jobName.replaceAll(INVALID_PROPERTIES_REGEX, "_");
 
      //we will give it 52 hours... 48 (two days), plus 4 hours to run...
      int defaultMinutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.defaultMinutesSinceLastSuccess", 60*52);

      minutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.minutesSinceLastSuccess." + configName, defaultMinutesSinceLastSuccess);
    }

    Timestamp timestamp = hib3GrouperLoaderLog.getEndedTime();

    Long lastSuccess = timestamp == null ? null : timestamp.getTime();

    boolean isSuccess = lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess;
    
    if (isSuccess) {
      loaderReport.append((runningFromDiagnostics ? "<font color='green'>SUCCESS:</font> " : "") + "Found a success on " + timestamp + " in grouper_loader_log for job name: " + jobName 
          + " which is within the threshold of " + minutesSinceLastSuccess + " minutes \n");
    } else {
      loaderReport.append((runningFromDiagnostics ? "<font color='red'>ERROR:</font> " : "") + "Found most recent success on " + timestamp + " in grouper_loader_log for job name: " + jobName 
          + " which is NOT within the threshold of " + minutesSinceLastSuccess + " minutes \n");
    }
    
    return isSuccess;
  }

  /**
   * @param group
   * @param grouperLoaderContainer
   * @param loaderReport
   * @param ldapSearchScopeEnum 
   * @param groupsLikeCount
   */
  public static void diagnosticsTryLdapGroupList(final GrouperLoaderContainer grouperLoaderContainer, Group group, 
      final StringBuilder loaderReport, final LdapSearchScope ldapSearchScopeEnum, final long groupsLikeCount) {
    final String groupName = group.getName();
  
    boolean requireTopStemAsStemFromConfigGroup = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
        "loader.ldap.requireTopStemAsStemFromConfigGroup", true);
    
    String groupParentFolderNameTemp = requireTopStemAsStemFromConfigGroup ? (GrouperUtil.parentStemNameFromName(groupName) + ":") : "";
    if (!StringUtils.isBlank(groupParentFolderNameTemp) && !groupParentFolderNameTemp.endsWith(":")) {
      groupParentFolderNameTemp += ":";
    }
    final String groupParentFolderName = groupParentFolderNameTemp;
  
    loaderReport.append("<font color='blue'>NOTE:</font> groupParentFolderName: " 
        + ("".equals(groupParentFolderName) ? "Root" : groupParentFolderName) 
        + "\n");
  
    final int[] subObjectOverallCount = new int[]{0};
  
    final String[] firstValueObject = new String[]{null};
  
    @SuppressWarnings("unused")
    Map<String, List<String>> resultMap = null;
    try {
  
      LdapSession.callbackLdapSession(
          grouperLoaderContainer.getLdapServerId(), new LdapHandler() {
  
            public Object callback(LdapHandlerBean ldapHandlerBean)
                throws NamingException {
  
              Ldap ldap = ldapHandlerBean.getLdap();
  
              Iterator<SearchResult> searchResultIterator = null;
  
              List<String> attributesList = new ArrayList<String>();
              loaderReport.append("<font color='blue'>NOTE:</font> Adding attribute to return from LDAP: '" + grouperLoaderContainer.getLdapSubjectAttributeName() + "'\n");
              attributesList.add(grouperLoaderContainer.getLdapSubjectAttributeName());
              String[] extraAttributeArray = null;
  
              if (!StringUtils.isBlank(grouperLoaderContainer.getLdapExtraAttributes())) {
                extraAttributeArray = GrouperUtil.splitTrim(grouperLoaderContainer.getLdapExtraAttributes(), ",");
                for (String attribute : extraAttributeArray) {
                  loaderReport.append("<font color='blue'>NOTE:</font> Adding attribute to return from LDAP: '" + attribute + "'\n");
                  attributesList.add(attribute);
                }
              }
  
              loaderReport.append("<font color='blue'>NOTE:</font> Using filter: '" + grouperLoaderContainer.getLdapLoaderFilter() + "'\n");
              SearchFilter searchFilterObject = new SearchFilter(grouperLoaderContainer.getLdapLoaderFilter());
              String[] attributeArray = GrouperUtil.toArray(attributesList, String.class);
  
              SearchControls searchControls = ldap.getLdapConfig().getSearchControls(
                  attributeArray);
              
              if (ldapSearchScopeEnum != null) {
                loaderReport.append("<font color='blue'>NOTE:</font> Using scope: '" + ldapSearchScopeEnum.name() + "'\n");
                searchControls.setSearchScope(ldapSearchScopeEnum.getSeachControlsConstant());
              }
              try {
                long startNanos = System.nanoTime();

                if (StringUtils.isBlank(grouperLoaderContainer.getLdapSearchDn())) {
                  searchResultIterator = ldap.search(
                      searchFilterObject, searchControls);
                } else {
                  loaderReport.append("<font color='blue'>NOTE:</font> Using search DN: '" + grouperLoaderContainer.getLdapSearchDn() + "'\n");
                  searchResultIterator = ldap.search(grouperLoaderContainer.getLdapSearchDn(),
                      searchFilterObject, searchControls);
                }
                loaderReport.append("<font color='green'>SUCCESS:</font> Filter ran and did not throw an error in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms\n");
              } catch (Exception e) {
                loaderReport.append("<font color='red'>ERROR:</font> Filter threw an error\n");
                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                return null;
              }
  
              Map<String, List<String>> result = new HashMap<String, List<String>>();
              int subObjectCount = 0;
              
              boolean firstObject = true;
              
              while (searchResultIterator.hasNext()) {
  
                SearchResult searchResult = searchResultIterator.next();
  
                List<String> valueResults = new ArrayList<String>();
                String nameInNamespace = searchResult.getName();
  
                if (firstObject) {
                  loaderReport.append("<font color='blue'>NOTE:</font> Original nameInNamespace: '" + nameInNamespace + "'\n");
                }
  
                //for some reason this returns: cn=test:testGroup,dc=upenn,dc=edu
                // instead of cn=test:testGroup,ou=groups,dc=upenn,dc=edu
                GrouperLoaderLdapServer grouperLoaderLdapServer = GrouperLoaderConfig
                  .retrieveLdapProfile(grouperLoaderContainer.getLdapServerId());
                if (nameInNamespace != null && !StringUtils.isBlank(grouperLoaderContainer.getLdapSearchDn())) {
                  String baseDn = grouperLoaderLdapServer.getBaseDn();
                  if (!StringUtils.isBlank(baseDn)
                      && nameInNamespace.endsWith("," + baseDn)) {
  
                    //sub one to get the comma out of there
                    nameInNamespace = nameInNamespace.substring(0, nameInNamespace
                        .length()
                        - (baseDn.length() + 1));
                    nameInNamespace += "," + grouperLoaderContainer.getLdapSearchDn() + "," + baseDn;
                    if (firstObject) {
                      loaderReport.append("<font color='blue'>NOTE:</font> Massaged nameInNamespace: '" + nameInNamespace + "'\n");
                    }
                  }
                }
                
                String defaultFolder = defaultLdapFolder();
                
                String loaderGroupName = defaultFolder + LoaderLdapElUtils.convertDnToSubPath(nameInNamespace, 
                    grouperLoaderLdapServer.getBaseDn(), grouperLoaderContainer.getLdapSearchDn());
  
                if (firstObject) {
                  loaderReport.append("<font color='blue'>NOTE:</font> Original group name: '" + loaderGroupName + "'\n");
                }
                
                if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupNameExpression())
                    || !StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDisplayNameExpression())
                    || !StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDescriptionExpression())) {
                  
                  Map<String, Object> envVars = new HashMap<String, Object>();
  
                  Map<String, Object> groupAttributes = new HashMap<String, Object>();
                  groupAttributes.put("dn", nameInNamespace);
                  if (!StringUtils.isBlank(grouperLoaderContainer.getLdapExtraAttributes())) {
                    for (String groupAttributeName : extraAttributeArray) {
                      Attribute groupAttribute = searchResult.getAttributes().get(
                          groupAttributeName);
  
                      if (groupAttribute != null) {
  
                        if (groupAttribute.size() > 1) {
                          throw new RuntimeException(
                              "Grouper LDAP loader only supports single valued group attributes at this point: "
                                  + groupAttributeName);
                        }
                        Object attributeValue = groupAttribute.get(0);
                        attributeValue = GrouperUtil.typeCast(attributeValue,
                            String.class);
                        groupAttributes.put(groupAttributeName, attributeValue);
                        if (firstObject) {
                          loaderReport.append("<font color='blue'>NOTE:</font> Found attribute: '" 
                              + groupAttributeName + "' with value '" + attributeValue + "'\n");
                        }
                      }
                    }
                  }
                  envVars.put("groupAttributes", groupAttributes);
                  if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupNameExpression())) {
                    String elGroupName = null;
                    try {
                      elGroupName = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapGroupNameExpression(),
                          envVars);
                      if (firstObject) {
                        loaderReport.append("<font color='green'>SUCCESS:</font> Evaluated group name expression: '" 
                            + grouperLoaderContainer.getLdapGroupNameExpression() + "' to value '" + elGroupName + "'\n");
                      }
                    } catch (Exception e) {
                      loaderReport.append("<font color='red'>ERROR:</font> Error evaluating group name expression: '" 
                          + grouperLoaderContainer.getLdapGroupNameExpression() + "'\n");
                      loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                      return null;
                    }
                    loaderGroupName = groupParentFolderName + elGroupName;
                    if (firstObject) {
                      loaderReport.append("<font color='blue'>NOTE:</font> Final group name: '" + loaderGroupName + "'\n");
                    }
                  }
                  if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDisplayNameExpression())) {
                    String elGroupDisplayName = null;
                    try {
                      elGroupDisplayName = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapGroupDisplayNameExpression(),
                          envVars);
                      if (firstObject) {
                        loaderReport.append("<font color='green'>SUCCESS:</font> Evaluated group display name expression: '" 
                            + grouperLoaderContainer.getLdapGroupDisplayNameExpression() + "' to value '" + elGroupDisplayName + "'\n");
                      }
                    } catch (Exception e) {
                      loaderReport.append("<font color='red'>ERROR:</font> Error evaluating group display name expression: '" 
                          + grouperLoaderContainer.getLdapGroupDisplayNameExpression() + "'\n");
                      loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                      return null;
                    }
                  }
                  if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDescriptionExpression())) {
                    String elGroupDescription = null;
                    try {
                      elGroupDescription = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapGroupDescriptionExpression(),
                          envVars);
                      if (firstObject) {
                        loaderReport.append("<font color='green'>SUCCESS:</font> Evaluated group description expression: '" 
                            + grouperLoaderContainer.getLdapGroupDescriptionExpression() + "' to value '" + elGroupDescription + "'\n");
                      }
                    } catch (Exception e) {
                      loaderReport.append("<font color='red'>ERROR:</font> Error evaluating group description expression: '" 
                          + grouperLoaderContainer.getLdapGroupDescriptionExpression() + "'\n");
                      loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                      return null;
                    }
                  }
                }
                
                result.put(loaderGroupName, valueResults);
  
                Attribute subjectAttribute = searchResult.getAttributes().get(
                    grouperLoaderContainer.getLdapSubjectAttributeName());
  
                if (subjectAttribute != null) {
                  for (int i = 0; i < subjectAttribute.size(); i++) {
  
                    Object attributeValue = subjectAttribute.get(i);
                    attributeValue = GrouperUtil.typeCast(attributeValue, String.class);
                    if (attributeValue != null) {
                      subObjectCount++;
                      subObjectOverallCount[0]++;
                      valueResults.add((String) attributeValue);
                      if (firstValueObject[0] == null) {
                        firstValueObject[0] = (String)attributeValue;
                      }
                    }
                  }
                }
                firstObject = false;
              }
  
              loaderReport.append("<font color='green'>SUCCESS:</font> Found " + result.size() + " groups, and " + subObjectCount
                  + " subjects\n");
  
              if (groupsLikeCount == 0) {
                
                if (result.size() > 0) {
                  // see if the groups like count is similar to the number of groups returned
                  loaderReport.append("<font color='red'>ERROR:</font> 0 groups in 'groups like' and " + result.size() + " groups in ldap, maybe job hasnt been run yet?  Or groupsLike '" 
                      + grouperLoaderContainer.getLdapGroupsLike() + "' is misconfigured?\n");
                }
                
              } else if (groupsLikeCount == result.size()) {
                loaderReport.append("<font color='green'>SUCCESS:</font> " + groupsLikeCount + " groups in 'groups like' and " + result.size() + " groups in ldap are the same number!\n");
                
              } else if (groupsLikeCount > 0) {
                double percentOff = Math.abs(groupsLikeCount - result.size()) / groupsLikeCount;
                if (percentOff > 0.1) {
                  loaderReport.append("<font color='red'>ERROR:</font> " + groupsLikeCount + " groups in 'groups like' and " 
                      + result.size() + " groups in ldap more than 10% away from each other.  Maybe job needs to be run?  Or groupsLike '" 
                      + grouperLoaderContainer.getLdapGroupsLike() + "' is misconfigured?\n");
                } else {
                  loaderReport.append("<font color='green'>SUCCESS:</font> " + groupsLikeCount + " groups in 'groups like' and " + result.size() + " groups in ldap are within 10%\n");
                  
                }
              }
              
              return result;
            }
          });
    } catch (RuntimeException re) {
      loaderReport.append("<font color='red'>ERROR:</font> " + re.getMessage() + "\n");
      loaderReport.append(ExceptionUtils.getFullStackTrace(re) + "\n");
    }
  
    if (subObjectOverallCount[0] > 0) {
      
      grouperLoaderFindSubject(loaderReport, firstValueObject[0], grouperLoaderContainer.getLdapSubjectExpression(), 
          grouperLoaderContainer.getLdapSourceId(), grouperLoaderContainer.getLdapSubjectLookupType());
      
    } else {
      loaderReport.append("<font color='red'>ERROR:</font> Did not find any subjects.  Is the attribute configured correctly?\n");
    }
    
  }

  /**
   * @param group
   * @param grouperLoaderContainer
   * @param loaderReport
   * @param ldapSearchScopeEnum 
   * @param groupsLikeCount
   */
  public static void diagnosticsTryLdapGroupsFromAttributes(final GrouperLoaderContainer grouperLoaderContainer, Group group, 
      final StringBuilder loaderReport, final LdapSearchScope ldapSearchScopeEnum, final long groupsLikeCount) {

    final String overallGroupName = group.getName();

    boolean requireTopStemAsStemFromConfigGroup = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
        "loader.ldap.requireTopStemAsStemFromConfigGroup", true);

    String groupParentFolderNameTemp = requireTopStemAsStemFromConfigGroup ? (GrouperUtil.parentStemNameFromName(overallGroupName) + ":") : "";
    if (!StringUtils.isBlank(groupParentFolderNameTemp) && !groupParentFolderNameTemp.endsWith(":")) {
      groupParentFolderNameTemp += ":";
    }
    final String groupParentFolderName = groupParentFolderNameTemp;
    
    loaderReport.append("<font color='blue'>NOTE:</font> groupParentFolderName: " 
        + ("".equals(groupParentFolderName) ? "Root" : groupParentFolderName) 
        + "\n");

    @SuppressWarnings("unused")
    final int[] subObjectOverallCount = new int[]{0};
    
    @SuppressWarnings("unused")
    final String[] firstValueObject = new String[]{null};

    @SuppressWarnings("unused")
    Map<String, List<String>> resultMap = null;
    try {

      resultMap = (Map<String, List<String>>) LdapSession.callbackLdapSession(
          grouperLoaderContainer.getLdapServerId(), new LdapHandler() {

            public Object callback(LdapHandlerBean ldapHandlerBean)
                throws NamingException {

              Ldap ldap = ldapHandlerBean.getLdap();

              Iterator<SearchResult> searchResultIterator = null;

              List<String> attributesList = new ArrayList<String>();

              //there can be subject attribute
              if (!StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectAttributeName())) {
                attributesList.add(grouperLoaderContainer.getLdapSubjectAttributeName());
                loaderReport.append("<font color='blue'>NOTE:</font> Adding subject attribute to return from LDAP: '" + grouperLoaderContainer.getLdapSubjectAttributeName() + "'\n");
              }
              //there must be a group attribute points to group
              // and multiple attributes may be present and separated
              // by a comma
              String[] groupAttributeNameArray = null;
              groupAttributeNameArray = GrouperUtil.splitTrim(grouperLoaderContainer.getLdapGroupAttributeName(), ",");
              for (String attribute: groupAttributeNameArray) {
                loaderReport.append("<font color='blue'>NOTE:</font> Adding group attribute to return from LDAP: '" + attribute + "'\n");
                attributesList.add(attribute);
              }
              
              String[] extraAttributeArray = null;

              if (!StringUtils.isBlank(grouperLoaderContainer.getLdapExtraAttributes())) {
                extraAttributeArray = GrouperUtil.splitTrim(grouperLoaderContainer.getLdapExtraAttributes(), ",");
                for (String attribute : extraAttributeArray) {
                  loaderReport.append("<font color='blue'>NOTE:</font> Adding extra attribute to return from LDAP: '" + attribute + "'\n");
                  attributesList.add(attribute);
                }

              }

              loaderReport.append("<font color='blue'>NOTE:</font> Using filter: '" + grouperLoaderContainer.getLdapLoaderFilter() + "'\n");
              SearchFilter searchFilterObject = new SearchFilter(grouperLoaderContainer.getLdapLoaderFilter());
              String[] attributeArray = GrouperUtil.toArray(attributesList, String.class);

              SearchControls searchControls = ldap.getLdapConfig().getSearchControls(
                  attributeArray);

              if (ldapSearchScopeEnum != null) {
                loaderReport.append("<font color='blue'>NOTE:</font> Using scope: '" + ldapSearchScopeEnum.name() + "'\n");
                searchControls.setSearchScope(ldapSearchScopeEnum
                    .getSeachControlsConstant());
              }
              try {
                long startNanos = System.nanoTime();

                if (StringUtils.isBlank(grouperLoaderContainer.getLdapSearchDn())) {
                  searchResultIterator = ldap.search(
                      searchFilterObject, searchControls);
                } else {
                  loaderReport.append("<font color='blue'>NOTE:</font> Using search DN: '" + grouperLoaderContainer.getLdapSearchDn() + "'\n");
                  searchResultIterator = ldap.search(grouperLoaderContainer.getLdapSearchDn(),
                      searchFilterObject, searchControls);
                }
                loaderReport.append("<font color='green'>SUCCESS:</font> Filter ran and did not throw an error in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms\n");
              } catch (Exception e) {
                loaderReport.append("<font color='red'>ERROR:</font> Filter threw an error\n");
                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                return null;
              }
              
              Map<String, String> attributeNameToGroupNameMap = new HashMap<String, String>();
              
              Map<String, List<String>> result = new HashMap<String, List<String>>();
              int subObjectCount = 0;
              @SuppressWarnings("unused")
              int subObjectValidCount = 0;
              
              //if filtering attributes by a jexl, then this is the cached result true or false, for if it is a valid attribute
              Map<String, Boolean> validAttributes = new HashMap<String, Boolean>();

              boolean firstObject = true;

              while (searchResultIterator.hasNext()) {

                SearchResult searchResult = searchResultIterator.next();

                String subjectNameInNamespace = searchResult.getName();

                if (firstObject) {
                  loaderReport.append("<font color='blue'>NOTE:</font> Original subjectNameInNamespace: '" + subjectNameInNamespace + "'\n");
                }

                //for some reason this returns: cn=test:testGroup,dc=upenn,dc=edu
                // instead of cn=test:testGroup,ou=groups,dc=upenn,dc=edu
                if (subjectNameInNamespace != null && !StringUtils.isBlank(grouperLoaderContainer.getLdapSearchDn())) {
                  GrouperLoaderLdapServer grouperLoaderLdapServer = GrouperLoaderConfig
                      .retrieveLdapProfile(grouperLoaderContainer.getLdapServerId());
                  String baseDn = grouperLoaderLdapServer.getBaseDn();
                  if (!StringUtils.isBlank(baseDn)
                      && subjectNameInNamespace.endsWith("," + baseDn)) {

                    //sub one to get the comma out of there
                    subjectNameInNamespace = subjectNameInNamespace.substring(0,
                        subjectNameInNamespace.length() - (baseDn.length() + 1));
                    subjectNameInNamespace += "," + grouperLoaderContainer.getLdapSearchDn() + "," + baseDn;
                    if (firstObject) {
                      loaderReport.append("<font color='blue'>NOTE:</font> Massaged subjectNameInNamespace: '" + subjectNameInNamespace + "'\n");
                    }
                  }
                }
                String subjectId = null;

                if (!StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectAttributeName())) {
                  Attribute subjectAttributeObject = searchResult.getAttributes().get(
                      grouperLoaderContainer.getLdapSubjectAttributeName());
                  if (subjectAttributeObject == null) {
                    loaderReport.append("<font color='red'>ERROR:</font> Cant find attribute " + grouperLoaderContainer.getLdapSubjectAttributeName() + " in LDAP record.  Maybe you have "
                        + "bad data in your LDAP or need to add to your filter a restriction that this attribute exists: '" 
                        + subjectNameInNamespace + "'\n");
                    return null;
                  }
                  subjectId = (String) subjectAttributeObject.get(0);
                  if (firstObject) {
                    loaderReport.append("<font color='blue'>NOTE:</font> Original subjectId: '" + subjectId + "'\n");
                  }
                }

                if (!StringUtils.isBlank(grouperLoaderContainer.getLdapSubjectExpression())) {
                  Map<String, Object> envVars = new HashMap<String, Object>();

                  Map<String, Object> subjectAttributes = new HashMap<String, Object>();
                  subjectAttributes.put("dn", subjectNameInNamespace);
                  
                  if (!StringUtils.isBlank(subjectId)) {
                    subjectAttributes.put("subjectId", subjectId);
                  }
                  
                  if (!StringUtils.isBlank(grouperLoaderContainer.getLdapExtraAttributes())) {
                    for (String subjectAttributeString : extraAttributeArray) {
                      Attribute subjectAttribute = searchResult.getAttributes().get(
                          subjectAttributeString);

                      if (subjectAttribute != null) {

                        if (subjectAttribute.size() > 1) {
                          throw new RuntimeException(
                              "Grouper LDAP loader only supports single valued subject attributes at this point: "
                                  + subjectAttribute);
                        }
                        Object attributeValue = subjectAttribute.get(0);
                        attributeValue = GrouperUtil.typeCast(attributeValue,
                            String.class);
                        subjectAttributes.put(subjectAttributeString, attributeValue);

                      }
                    }
                  }
                  envVars.put("subjectAttributes", subjectAttributes);
                  try {
                    subjectId = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapSubjectExpression(),
                        envVars);
                    if (firstObject) {
                      loaderReport.append("<font color='blue'>SUCCESS:</font> subjectId is: '" + subjectId 
                          + "' after subjectExpression '" + grouperLoaderContainer.getLdapSubjectExpression() + "'\n");
                    }
                  } catch (Exception e) {
                    loaderReport.append("<font color='red'>ERROR:</font> Could not run expression language: '" + grouperLoaderContainer.getLdapSubjectExpression() + "'\n");
                    loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                    return null;
                  }
                }

                if (StringUtils.isBlank(grouperLoaderContainer.getLdapGroupAttributeName())) {
                  loaderReport.append("<font color='red'>ERROR:</font> LDAP_GROUPS_FROM_ATTRIBUTES loader type requires group attribute name\n");
                  return null;
                }

                // loop over attribute names that indicate group membership
                for (String attribute: groupAttributeNameArray) {
                
                  Attribute groupAttribute = searchResult.getAttributes().get(attribute);

                  if (groupAttribute != null) {
                    for (int i = 0; i < groupAttribute.size(); i++) {
  
                      Object attributeValue = groupAttribute.get(i);
                      attributeValue = GrouperUtil.typeCast(attributeValue, String.class);
                      
                      if (attributeValue != null) {
                        subObjectCount++;

                        if (subObjectCount == 1) {
                          loaderReport.append("<font color='green'>SUCCESS:</font> First group attribute value: '" 
                              + attributeValue + "'\n");
                        }

                        //lets see if we know the groupName
                        String groupName = attributeNameToGroupNameMap.get(attributeValue);
                        if (StringUtils.isBlank(groupName)) {
  
                          //lets see if valid attribute, see if a filter expression is set
                          if (!StringUtils.isBlank(grouperLoaderContainer.getLdapAttributeFilterExpression())) {
                            //see if we have already calculated it
                            if (!validAttributes.containsKey(attributeValue)) {
                              
                              Map<String, Object> variableMap = new HashMap<String, Object>();
                              variableMap.put("attributeValue", attributeValue);
                              
                              //lets run the filter on the attribute name
                              String attributeResultBooleanString = null;
                              
                              try {
                                attributeResultBooleanString = GrouperUtil.substituteExpressionLanguage(
                                    grouperLoaderContainer.getLdapAttributeFilterExpression(), variableMap, true, false, false);
                              } catch (Exception e) {
                                loaderReport.append("<font color='red'>ERROR:</font> Error running expression: '" 
                                    + grouperLoaderContainer.getLdapAttributeFilterExpression() + "'\n");
                                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                                return null;
                                
                              }
                              boolean attributeResultBoolean = false;
                              try {
                                attributeResultBoolean = GrouperUtil.booleanValue(attributeResultBooleanString);
                              } catch (RuntimeException re) {
                                throw new RuntimeException("Error parsing boolean: '" + attributeResultBooleanString 
                                    + "', expecting true or false, from expression: " + grouperLoaderContainer.getLdapAttributeFilterExpression() );
                              }
                              
                              if (LOG.isDebugEnabled()) {
                                LOG.debug("Attribute '" + attributeValue + "' is allowed to be used based on expression? " 
                                    + attributeResultBoolean + ", '" + grouperLoaderContainer.getLdapAttributeFilterExpression() + "', note the attributeValue is" +
                                        " in a variable called attributeValue");
                              }
                              
                              validAttributes.put((String)attributeValue, attributeResultBoolean);
                              
                            }
                            
                            //lets see if filtering
                            if (!validAttributes.get(attributeValue)) {
                              continue;
                            }
                          }
                          
                          subObjectValidCount++;
                          
                          String defaultFolder = defaultLdapFolder();
                          
                          groupName = defaultFolder + attributeValue;
                          
                          
                          String loaderGroupDisplayName = null;
                          String loaderGroupDescription = null;
                          
                          if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupNameExpression())
                              || !StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDisplayNameExpression())
                              || !StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDescriptionExpression())) {
                            
                            //calculate it
  
                            Map<String, Object> envVars = new HashMap<String, Object>();
  
                            envVars.put("groupAttribute", attributeValue);
                            
                            if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupNameExpression())) {
                              try {

                                groupName = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapGroupNameExpression(),
                                    envVars);
                                if (subObjectCount == 1) {
                                  loaderReport.append("<font color='green'>SUCCESS:</font> Group name: '" 
                                      + groupName + "' evaluated from '" + grouperLoaderContainer.getLdapGroupNameExpression() + "'\n");
                                  loaderReport.append("<font color='green'>SUCCESS:</font> Final group name: '" 
                                      + groupParentFolderName + groupName + "'\n");
                                } 
                              } catch (Exception e) {
                                loaderReport.append("<font color='red'>ERROR:</font> Error evaluating group name expression: '" 
                                    + grouperLoaderContainer.getLdapGroupNameExpression() + "'\n");
                                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                                return null;
                              }
                                
                            }
                            if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDisplayNameExpression())) {
                              try {
                                String elGroupDisplayName = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapGroupDisplayNameExpression(),
                                    envVars);
                                loaderGroupDisplayName = groupParentFolderName + elGroupDisplayName;
                                if (subObjectCount == 1) {
                                  loaderReport.append("<font color='green'>SUCCESS:</font> Group display name: '" 
                                      + elGroupDisplayName + "' evaluated from '" + grouperLoaderContainer.getLdapGroupDisplayNameExpression() + "'\n");
                                  loaderReport.append("<font color='green'>SUCCESS:</font> Final group display name: '" 
                                      + loaderGroupDisplayName + "'\n");
                                }
                              } catch (Exception e) {
                                loaderReport.append("<font color='red'>ERROR:</font> Error evaluating group display name expression: '" 
                                    + grouperLoaderContainer.getLdapGroupDisplayNameExpression() + "'\n");
                                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                                return null;
                              }
                            }
                            if (!StringUtils.isBlank(grouperLoaderContainer.getLdapGroupDescriptionExpression())) {
                              try {
                                String elGroupDescription = LoaderLdapUtils.substituteEl(grouperLoaderContainer.getLdapGroupDescriptionExpression(),
                                    envVars);
                                loaderGroupDescription = elGroupDescription;
                                if (subObjectCount == 1) {
                                  loaderReport.append("<font color='green'>SUCCESS:</font> Group description: '" 
                                      + loaderGroupDescription + "' evaluated from '" + grouperLoaderContainer.getLdapGroupDescriptionExpression() + "'\n");
                                }
                              } catch (Exception e) {
                                loaderReport.append("<font color='red'>ERROR:</font> Error evaluating group description expression: '" 
                                    + grouperLoaderContainer.getLdapGroupDescriptionExpression() + "'\n");
                                loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
                                return null;
                              }
                            }
                          }
                          
                          groupName = groupParentFolderName + groupName;
                          
                          //cache this
                          attributeNameToGroupNameMap.put((String)attributeValue, groupName);
                          
                          //init the subject list
                        if (!result.containsKey(groupName)) {
                          result.put(groupName, new ArrayList<String>());
                        }
                      }
                        //get the "row" for the group
                        List<String> valueResults = result.get(groupName);
                        //add the subject
                        valueResults.add(subjectId);
                        
                        if (firstObject) {
                          grouperLoaderFindSubject(loaderReport, subjectId, grouperLoaderContainer.getLdapSubjectExpression(), 
                              grouperLoaderContainer.getLdapSourceId(), grouperLoaderContainer.getLdapSubjectLookupType());

                        }
                        
                      }
                    }
                  }
                } // end of looping over attributes indicating group membership
                firstObject = false;

              } // end of looping over search results

              loaderReport.append("<font color='green'>SUCCESS:</font> Found " + result.size() + " subjects, and " + subObjectCount
                  + " memberships\n");

              return result;
            }
          });
    } catch (RuntimeException re) {
      loaderReport.append("<font color='red'>ERROR:</font> " + re.getMessage() + "\n");
      loaderReport.append(ExceptionUtils.getFullStackTrace(re) + "\n");
    }

  }
  
  /**
   * @return default ldap folder for groups including trailing colon if not blank
   */
  private static String defaultLdapFolder() {
    String defaultFolder = "groups:";
    
    if (GrouperLoaderConfig.retrieveConfig().properties().containsKey("loader.ldap.defaultGroupFolder")) {
      defaultFolder = StringUtils.defaultString(GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.ldap.defaultGroupFolder"));
      if (!StringUtils.isBlank(defaultFolder) && !defaultFolder.endsWith(":")) {
        defaultFolder += ":";
      }
    }
    return defaultFolder;
  }

  /**
   * 
   * @param loaderReport
   * @param subjectIdOrIdentifier
   * @param ldapSubjectExpression
   * @param sourceId
   * @param subjectIdType
   */
  private static void grouperLoaderFindSubject(StringBuilder loaderReport, String subjectIdOrIdentifier, String ldapSubjectExpression, 
          String sourceId, String subjectIdType) {
    
    String defaultSubjectSourceId = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        GrouperLoaderConfig.DEFAULT_SUBJECT_SOURCE_ID);
    
    subjectIdType = GrouperUtil.defaultIfBlank(subjectIdType, defaultSubjectSourceId);
    subjectIdType = GrouperUtil.defaultIfBlank(subjectIdType, "subjectId");
    
    if (!StringUtils.isBlank(ldapSubjectExpression)) {

      Map<String, Object> envVars = new HashMap<String, Object>();
      envVars.clear();
      envVars.put("subjectId", subjectIdOrIdentifier);

      try {
        String newSubjectId = LoaderLdapUtils.substituteEl(ldapSubjectExpression, envVars);
        loaderReport.append("<font color='green'>SUCCESS:</font> Massaged subjectId with ldap subject expression: '" + 
            ldapSubjectExpression + "' from '" + subjectIdOrIdentifier + "', to '" + newSubjectId + "'\n");

        subjectIdOrIdentifier = newSubjectId;
        
      } catch(Exception e) {
        loaderReport.append("<font color='red'>ERROR:</font> Could do EL on subject '" + subjectIdOrIdentifier + "', '" + ldapSubjectExpression + "'!\n");
        loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
        return;
      }
      
    }
    
    if (!StringUtils.isBlank(subjectIdType)) {

      if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_ID")
          || StringUtils.equalsIgnoreCase(subjectIdType, "subjectId")) {

        if (!StringUtils.isBlank(sourceId)) {
          
          try {
            Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, sourceId, true);
            loaderReport.append("<font color='green'>SUCCESS:</font> Found subject '" + subjectIdOrIdentifier + "' in source: '" 
                + sourceId + "' by id: " + GrouperUtil.subjectToString(subject) + "\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Could not find subject by id '" + subjectIdOrIdentifier + "' in source: '" + sourceId + "'!\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
          }
          
        } else {

          try {
            Subject subject = SubjectFinder.findById(subjectIdOrIdentifier, true);
            loaderReport.append("<font color='green'>SUCCESS:</font> Found subject '" + subjectIdOrIdentifier 
                + "' by id: " + GrouperUtil.subjectToString(subject) + "\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Could not find subject by id '" + subjectIdOrIdentifier + "'!\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
          }

        }
        
      } else if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_IDENTIFIER")
          || StringUtils.equalsIgnoreCase(subjectIdType, "subjectIdentifier")) {

        if (!StringUtils.isBlank(sourceId)) {
          
          try {
            Subject subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, sourceId, true);
            loaderReport.append("<font color='green'>SUCCESS:</font> Found subject '" + subjectIdOrIdentifier + "' in source: '" 
                + sourceId + "' by identifier: " + GrouperUtil.subjectToString(subject) + "\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Could not find subject by identifier '" + subjectIdOrIdentifier + "' in source: '" + sourceId + "'!\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
          }
          
        } else {

          try {
            Subject subject = SubjectFinder.findByIdentifier(subjectIdOrIdentifier, true);
            loaderReport.append("<font color='green'>SUCCESS:</font> Found subject '" + subjectIdOrIdentifier 
                + "' by identifier: " + GrouperUtil.subjectToString(subject) + "\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Could not find subject by identifier '" + subjectIdOrIdentifier + "'!\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
          }

        }
        

      } else if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_ID_OR_IDENTIFIER")
          || StringUtils.equalsIgnoreCase(subjectIdType, "subjectIdOrIdentifier")) {

        if (!StringUtils.isBlank(sourceId)) {
          
          try {
            Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectIdOrIdentifier, sourceId, true);
            loaderReport.append("<font color='green'>SUCCESS:</font> Found subject '" + subjectIdOrIdentifier + "' in source: '" 
                + sourceId + "' by idOrIdentifier: " + GrouperUtil.subjectToString(subject) + "\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Could not find subject by idOrIdentifier '" + subjectIdOrIdentifier + "' in source: '" + sourceId + "'!\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
          }
          
        } else {

          try {
            Subject subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, true);
            loaderReport.append("<font color='green'>SUCCESS:</font> Found subject '" + subjectIdOrIdentifier 
                + "' by idOrIdentifier: " + GrouperUtil.subjectToString(subject) + "\n");
          } catch (Exception e) {
            loaderReport.append("<font color='red'>ERROR:</font> Could not find subject by idOrIdentifier '" + subjectIdOrIdentifier + "'!\n");
            loaderReport.append(ExceptionUtils.getFullStackTrace(e) + "\n");
          }

        }
        
      } else {
        throw new RuntimeException("Not expecting subjectIdType: '" + subjectIdType
            + "', should be subjectId, subjectIdentifier, or subjectIdOrIdentifier");
      }
    }

    
  }
      
  /**
   * the loader overall button was pressed from misc page
   * @param request
   * @param response
   */
  @SuppressWarnings("deprecation")
  public void loaderOverall(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      boolean canSeeLoaderOverall = GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer().isCanSeeLoaderOverall();
      
      if (!canSeeLoaderOverall) {
        return;
      }
      
      //not sure who can see attributes etc, just go root
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.startRootSession();
      
      List<GuiGrouperLoaderJob> guiGrouperLoaderJobs = new ArrayList<GuiGrouperLoaderJob>();
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperLoaderContainer().setGuiGrouperLoaderJobs(guiGrouperLoaderJobs);
      
      {
        Set<Group> groups = GrouperLoaderType.retrieveGroups(grouperSession);
        
        for (Group group : GrouperUtil.nonNull(groups)) {
          
          GuiGrouperLoaderJob guiGrouperLoaderJob = new GuiGrouperLoaderJob();
          guiGrouperLoaderJobs.add(guiGrouperLoaderJob);
          
          guiGrouperLoaderJob.setGuiGroup(new GuiGroup(group));
          
          String grouperLoaderType = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_TYPE, false, false);

          if (!StringUtils.isBlank(grouperLoaderType)) {

            guiGrouperLoaderJob.setType(grouperLoaderType);

            GrouperLoaderType grouperLoaderTypeEnum = null;
            
            try {
              grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
              String jobName = grouperLoaderTypeEnum.name() + "__" + group.getName() + "__" + group.getUuid();
              guiGrouperLoaderJob.setJobName(jobName);
            } catch (Exception e) {
              //ignore
            }

          }
          
          {
            String query = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_QUERY, false, false);

            guiGrouperLoaderJob.setQuery(query);
          }
          
          {
            String source = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_DB_NAME, false, false);

            guiGrouperLoaderJob.setSource(source);
            
            String url = GrouperLoaderContainer.convertDatabaseNameToUrl(source);
            String description = GrouperLoaderContainer.convertDatabaseUrlToText(url);
            
            guiGrouperLoaderJob.setSourceDescription(description);
            
          }
          
        }
        
      }
      Set<AttributeAssign> ldapAttributeAssigns = GrouperLoaderType.retrieveLdapAttributeAssigns();
      
      for (AttributeAssign ldapAttributeAssign : GrouperUtil.nonNull(ldapAttributeAssigns)) {
        
        GuiGrouperLoaderJob guiGrouperLoaderJob = new GuiGrouperLoaderJob();
        guiGrouperLoaderJobs.add(guiGrouperLoaderJob);
        
        Group group = ldapAttributeAssign.getOwnerGroup();
        if (group == null) {
          continue;
        }
        guiGrouperLoaderJob.setGuiGroup(new GuiGroup(group));

        String grouperLoaderType = ldapAttributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapTypeName());

        if (!StringUtils.isBlank(grouperLoaderType)) {

          guiGrouperLoaderJob.setType(grouperLoaderType);

          GrouperLoaderType grouperLoaderTypeEnum = null;
          
          try {
            grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
            String jobName = grouperLoaderTypeEnum.name() + "__" + group.getName() + "__" + group.getUuid();
            guiGrouperLoaderJob.setJobName(jobName);
          } catch (Exception e) {
            //ignore
          }

        }

        {
          String query = ldapAttributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapFilterName());
              
          guiGrouperLoaderJob.setQuery(query);
        }
        
        {
          String source = ldapAttributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapServerIdName());
              
          guiGrouperLoaderJob.setSource(source);
          
          String url = GrouperLoaderContainer.convertLdapServerIdToUrl(source);
          String description = GrouperLoaderContainer.convertLdapUrlToDescription(url);
          
          guiGrouperLoaderJob.setSourceDescription(description);
          
        }

        
      }
      
      for (GuiGrouperLoaderJob guiGrouperLoaderJob : guiGrouperLoaderJobs) {
        List<Criterion> criterionList = new ArrayList<Criterion>();
        
        String jobName = guiGrouperLoaderJob.getJobName();
        
        criterionList.add(Restrictions.eq("jobName", jobName));
        criterionList.add(Restrictions.eq("status", "SUCCESS"));
  
        int maxRows = 1;
        QueryOptions queryOptions = QueryOptions.create("lastUpdated", false, 1, maxRows);
        
        Criterion allCriteria = HibUtils.listCrit(criterionList);
        
        List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
          .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);

        StringBuilder message = new StringBuilder();
        
        boolean success = loaderSuccessFromLogs(message, jobName, GrouperUtil.length(loaderLogs) > 0 ? loaderLogs.get(0) : null, false);
        
        if (success) {
          guiGrouperLoaderJob.setStatus("SUCCESS");
        } else {
          guiGrouperLoaderJob.setStatus("ERROR");
        }
        
        if (GrouperUtil.length(loaderLogs) > 0) {
          Hib3GrouperLoaderLog hib3GrouperLoaderLog = loaderLogs.get(0);
          guiGrouperLoaderJob.setChanges(GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), 0) 
              + GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), 0) 
              + GrouperUtil.intValue(hib3GrouperLoaderLog.getUpdateCount(), 0));
          guiGrouperLoaderJob.setCount(GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), 0));
        }
        
        guiGrouperLoaderJob.setStatusDescription(message.toString());
        
      }
      
      Collections.sort(guiGrouperLoaderJobs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/grouperLoaderOverall.jsp"));

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
