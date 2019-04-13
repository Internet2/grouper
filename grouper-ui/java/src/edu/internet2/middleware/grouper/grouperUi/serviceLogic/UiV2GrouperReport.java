/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstance;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportLogic;
import edu.internet2.middleware.grouper.app.reports.GrouperReportSettings;
import edu.internet2.middleware.grouper.app.reports.ReportConfigFormat;
import edu.internet2.middleware.grouper.app.reports.ReportConfigType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperReportConfigInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperReportContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiReportConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiReportInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.subject.Subject;
import net.redhogs.cronparser.CronExpressionDescriptor;

/**
 * 
 */
public class UiV2GrouperReport {
    
  /**
   * view report configs on a folder
   * @param request
   * @param response
   */
  public void viewReportOnFolder(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      @SuppressWarnings("unchecked")
      Set<GuiReportConfig> guiReportConfigs = (Set<GuiReportConfig>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<GuiReportConfig> guiReportConfigs = new HashSet<GuiReportConfig>();
          Set<GrouperReportConfigurationBean> grouperReportConfigs = GrouperReportConfigService.getGrouperReportConfigs(STEM);
          
          for (GrouperReportConfigurationBean configBean: grouperReportConfigs) {
            GrouperReportInstance mostRecentReportInstance = GrouperReportInstanceService.getMostRecentReportInstance(STEM, configBean.getAttributeAssignmentMarkerId());
            GuiReportConfig guiReportConfig = new GuiReportConfig(configBean, mostRecentReportInstance);
            if (guiReportConfig.isCanRead()) {              
              guiReportConfigs.add(guiReportConfig);
            }
          }
          
          return guiReportConfigs;
        }
        
      });
      
      grouperReportContainer.setGuiReportConfigs(guiReportConfigs);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfig.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * edit button was clicked on folder report screen
   * @param request
   * @param response
   */
  public void reportOnFolderEdit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!grouperReportContainer.isCanWriteGrouperReports()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEditFolder")));
            return false;
          }

          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      @SuppressWarnings("unchecked")
      Set<GrouperReportConfigurationBean> reportConfigBeans = (Set<GrouperReportConfigurationBean>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return GrouperReportConfigService.getGrouperReportConfigs(STEM);
        }
      });
      
      grouperReportContainer.setReportConfigBeans(reportConfigBeans);
      
      Map<String, GrouperReportConfigurationBean> reportConfigNameToBean = new HashMap<String, GrouperReportConfigurationBean>();
      
      for (GrouperReportConfigurationBean bean: reportConfigBeans) {
        reportConfigNameToBean.put(bean.getReportConfigName(), bean);
      }
      
      String previousReportConfigName = request.getParameter("previousReportConfigName");
      String reportConfigName = request.getParameter("grouperReportConfigName");
      
      // first time loading the bean by config name, load from database
      if (StringUtils.isBlank(previousReportConfigName) && StringUtils.isNotBlank(reportConfigName)) {
        GrouperReportConfigurationBean grouperReportConfigurationBean = reportConfigNameToBean.get(reportConfigName);
        grouperReportContainer.setConfigBean(grouperReportConfigurationBean);
      }
      
      // change was made on the form
      if (StringUtils.isNotBlank(previousReportConfigName) && previousReportConfigName.equals(reportConfigName)) {
        GrouperReportConfigurationBean bean = new GrouperReportConfigurationBean();
        populateGrouperReportConfigBean(request, bean);
        grouperReportContainer.setConfigBean(bean);
      }
      
      // config name was changed, load from database
      if (StringUtils.isNotBlank(previousReportConfigName) && !previousReportConfigName.equals(reportConfigName)) {
        GrouperReportConfigurationBean grouperReportConfigurationBean = reportConfigNameToBean.get(reportConfigName);
        grouperReportContainer.setConfigBean(grouperReportConfigurationBean);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfigEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * screen to allow adding a new report config
   * @param request
   * @param response
   */
  public void reportOnFolderAdd(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!grouperReportContainer.isCanWriteGrouperReports()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAddFolder")));
            return false;
          }

          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      GrouperReportConfigurationBean newBean = new GrouperReportConfigurationBean();
      populateGrouperReportConfigBean(request, newBean);
      
      grouperReportContainer.setConfigBean(newBean);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * Save a new report config to the database 
   * @param request
   * @param response
   */
  public void reportOnFolderAddEditSubmit(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final GrouperReportConfigurationBean bean = new GrouperReportConfigurationBean();
      final String mode = request.getParameter("mode");
      final boolean isAdd = (mode != null) && mode.equalsIgnoreCase("add");
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!grouperReportContainer.isCanWriteGrouperReports()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAddFolder")));
            return false;
          }
          
          populateGrouperReportConfigBean(request, bean);
          boolean isValid = validateGrouperReportConfigBean(bean, theGrouperSession, STEM, isAdd);
          return isValid;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      boolean saved = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          try {    
            GrouperReportConfigService.saveOrUpdateReportConfigAttributes(bean, STEM);
            GrouperReportConfigurationBean savedBean = GrouperReportConfigService.getGrouperReportConfig(STEM, bean.getReportConfigName());
            if (!isAdd) {              
              GrouperReportConfigService.unscheduleJob(savedBean, STEM); // unschedule and schedule again if we are editing
            }
            GrouperReportConfigService.scheduleJob(savedBean, STEM);
          } catch (SchedulerException e) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!saved) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportJobScheduleError")));
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigAddEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view report instances for a report config
   * @param request
   * @param response
   */
  public void viewAllReportInstances(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String attributeAssignmentMarkerId = request.getParameter("attributeAssignmentMarkerId");
      if (StringUtils.isBlank(attributeAssignmentMarkerId)) {
        throw new RuntimeException("why is attributeAssignmentMarkerId blank??");
      }
      
      //TODO check that logged in user can view the settings and view/download the report
      
      GrouperReportConfigInstance grouperReportConfigInstance = (GrouperReportConfigInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(STEM, attributeAssignmentMarkerId);
          Set<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(STEM, attributeAssignmentMarkerId);
          
          List<GuiReportInstance> guiReportInstances = GuiReportInstance.buildGuiReportInstances(configBean, new ArrayList<GrouperReportInstance>(reportInstances));
          
          GrouperReportConfigInstance grouperReportConfigInstance = new GrouperReportConfigInstance();
          grouperReportConfigInstance.setGuiReportInstances(guiReportInstances);
          grouperReportConfigInstance.setReportConfigBean(configBean);
          
          return grouperReportConfigInstance;
        }
        
      });
      
      grouperReportContainer.setGrouperReportConfigInstance(grouperReportConfigInstance);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfigWithInstances.jsp"));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void viewReportInstanceDetails(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("why is attributeAssignId blank??");
      }
      
      //TODO check that logged in user can view the settings and view/download the report
      GuiReportInstance guiReportInstance = (GuiReportInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportInstance reportInstance = GrouperReportInstanceService.getReportInstance(attributeAssignId);
          
          String configMarkerAssignmentId = reportInstance.getReportInstanceConfigMarkerAssignmentId();
          
          GrouperReportConfigurationBean grouperReportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(STEM, configMarkerAssignmentId);
          
          GuiReportInstance guiReportInstance = new GuiReportInstance();
          guiReportInstance.setReportConfigBean(grouperReportConfigBean);
          guiReportInstance.setReportInstance(reportInstance);
          
          return guiReportInstance;
        }
        
      });
      
      grouperReportContainer.setGuiReportInstance(guiReportInstance);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportInstanceDetails.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void changeReportConfigStatus(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String newStatus = request.getParameter("newStatus");
      if (StringUtils.isBlank(newStatus)) {
        throw new RuntimeException("why is newStatus blank??");
      }
      
      final String attributeAssignmentMarkerId = request.getParameter("attributeAssignmentMarkerId");
      if (StringUtils.isBlank(attributeAssignmentMarkerId)) {
        throw new RuntimeException("why is attributeAssignmentMarkerId blank??");
      }
      
      //TODO check that logged in user can enable/disable the report
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(STEM, attributeAssignmentMarkerId);
          if (newStatus.equals("enable")) {            
            configBean.setReportConfigEnabled(true);
          } else {
            configBean.setReportConfigEnabled(false);
          }
          GrouperReportConfigService.saveOrUpdateReportConfigAttributes(configBean, STEM);
          
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigStatusChangeSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void downloadReport(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String attributeAssignmentMarkerId = request.getParameter("attributeAssignmentMarkerId");
      if (StringUtils.isBlank(attributeAssignmentMarkerId)) {
        throw new RuntimeException("why is attributeAssignmentMarkerId blank??");
      }
      
      GrouperReportInstance reportInstance = (GrouperReportInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportInstance recentReportInstance = GrouperReportInstanceService.getMostRecentReportInstance(STEM, attributeAssignmentMarkerId);
          return recentReportInstance;
        }
        
      });
      
      try {
        
        //TODO remove the following two lines. they are here so that we 
        // don't have to run daemon
        reportInstance.setReportInstanceEncryptionKey("fcPInvBpYbEO0XLP");
        reportInstance.setReportInstanceFilePointer("https://grouper-reports.s3.us-west-2.amazonaws.com/one_rel_history.csv");
        
        String reportContent = GrouperReportLogic.getReportContent(reportInstance);
        
        response.setContentType("text/csv");
        response.setHeader ("Content-Disposition", "inline;filename=\"" + reportInstance.getReportInstanceFileName() + "\"");
        
        PrintWriter out = response.getWriter();
        out.write(reportContent);
        out.close();
      } catch (IOException e) {
        // TODO: handle exception
        e.printStackTrace();
      }
      
      throw new ControllerDone();
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
 
  public void deleteReportConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String attributeAssignmentMarkerId = request.getParameter("attributeAssignmentMarkerId");
      if (StringUtils.isBlank(attributeAssignmentMarkerId)) {
        throw new RuntimeException("why is attributeAssignmentMarkerId blank??");
      }
      
      //TODO check that logged in user can delete the report
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(STEM, attributeAssignmentMarkerId);
          if (configBean == null) {
            throw new RuntimeException("Invalid attributeAssignmentMarkerId");
          }
          
          GrouperReportConfigService.deleteGrouperReportConfig(STEM, configBean);
          
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  private void populateGrouperReportConfigBean(HttpServletRequest request, GrouperReportConfigurationBean bean) {
    
    String reportConfigType = request.getParameter("grouperReportConfigType");
    if (StringUtils.isNotBlank(reportConfigType)) {
      ReportConfigType configType = ReportConfigType.valueOf(reportConfigType);
      bean.setReportConfigType(configType);
    }
    
    String reportConfigFormat = request.getParameter("grouperReportConfigFormat");
    ReportConfigFormat configFormat = null;
    if (StringUtils.isNotBlank(reportConfigFormat)) {
      configFormat = ReportConfigFormat.valueOf(reportConfigFormat);
      bean.setReportConfigFormat(configFormat);
    }
    
    String reportConfigEnabled = request.getParameter("grouperReportConfigEnabled");
    if (StringUtils.isNotBlank(reportConfigEnabled)) {
      bean.setReportConfigEnabled(BooleanUtils.toBoolean(reportConfigEnabled));
    }
    
    String reportConfigName = request.getParameter("grouperReportConfigName");
    bean.setReportConfigName(reportConfigName);
    
    String reportConfigFileName = request.getParameter("grouperReportConfigFileName");
    bean.setReportConfigFilename(reportConfigFileName);
    
    String reportConfigDescription = request.getParameter("grouperReportConfigDescription");
    bean.setReportConfigDescription(reportConfigDescription);
    
    
    //if moving combobox down to extra list or getting all groups
    String viewersGroup = request.getParameter("grouperReportConfigViewersGroupComboName");
    
    if (StringUtils.isBlank(viewersGroup)) {
      //if didnt pick one from results
      viewersGroup = request.getParameter("grouperReportConfigViewersGroupComboNameDisplay");
    }
    
    //String reportConfigViewersGroupIdOrName = request.getParameter("grouperReportConfigViewersGroupId");
    bean.setReportConfigViewersGroupId(viewersGroup);
    
    String reportConfigQuartzCron = request.getParameter("grouperReportConfigQuartzCron");
    bean.setReportConfigQuartzCron(reportConfigQuartzCron);
    
    String reportConfigSendEmail = request.getParameter("grouperReportConfigSendEmail");
    boolean reportConfigSendEmailBoolean = true;
    if (StringUtils.isNotBlank(reportConfigSendEmail)) {
      reportConfigSendEmailBoolean = BooleanUtils.toBoolean(reportConfigSendEmail);
    }
    bean.setReportConfigSendEmail(reportConfigSendEmailBoolean);
    
    String reportConfigEmailSubject = request.getParameter("grouperReportConfigEmailSubject");
    bean.setReportConfigEmailSubject(reportConfigEmailSubject);
    
    String reportConfigEmailBody = request.getParameter("grouperReportConfigEmailBody");
    bean.setReportConfigEmailBody(reportConfigEmailBody);
    
    String reportConfigSendEmailToViewers = request.getParameter("grouperReportConfigSendEmailToViewers");
    boolean configSendEmailToViewers = true;
    if (StringUtils.isNotBlank(reportConfigSendEmailToViewers)) {
      configSendEmailToViewers = BooleanUtils.toBoolean(reportConfigSendEmailToViewers);
    }
    bean.setReportConfigSendEmailToViewers(configSendEmailToViewers);
    
    String reportConfigSendEmailToGroupId = request.getParameter("grouperReportConfigSendEmailToGroupId");
    bean.setReportConfigSendEmailToGroupId(reportConfigSendEmailToGroupId);
    
    String reportConfigQuery = request.getParameter("grouperReportConfigQuery");
    bean.setReportConfigQuery(reportConfigQuery);
    
  }
  
  private boolean validateGrouperReportConfigBean(GrouperReportConfigurationBean bean, GrouperSession session,
      GrouperObject grouperObject, boolean isAddMode) {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    // in edit mode, if reportConfigEnabled is false, no need to validate anything except the presence of config name
    if (!isAddMode && !bean.isReportConfigEnabled()) {
      if (StringUtils.isBlank(bean.getReportConfigName())) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#grouperReportConfigNameId",
            TextContainer.retrieveFromRequest().getText().get("grouperReportConfigNameBlankError")));
        return false;
      }
      return true;
    }
    
    if (bean.getReportConfigType() == null) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#grouperReportConfigHasTypeId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigTypeBlankError")));
      return false;
    }
    
    if (bean.getReportConfigFormat() == null) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#grouperReportConfigHasFormatId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigFormatBlankError")));
      return false;
    }
    
    if (StringUtils.isBlank(bean.getReportConfigName())) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigNameId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigNameBlankError")));
      return false;
    }
    
    if (isAddMode) {
      GrouperReportConfigurationBean existingConfigBean = GrouperReportConfigService.getGrouperReportConfig(grouperObject, bean.getReportConfigName());
      if (existingConfigBean != null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#grouperReportConfigNameId",
            TextContainer.retrieveFromRequest().getText().get("grouperReportConfigNameAlreadyExistsError")));
        return false;
      }
    }
    
    if (bean.getReportConfigFormat() == ReportConfigFormat.CSV && StringUtils.isBlank(bean.getReportConfigFilename())) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigFileNameId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigFilenameBlankError")));
      return false;
    }
    
    if (StringUtils.isBlank(bean.getReportConfigDescription())) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigDescriptionId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigDescriptionBlankError")));
      return false;
    }
    
    if (bean.getReportConfigDescription().length() > 4000) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigDescriptionId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigDescriptionTooLongError")));
      return false;
    }
    
    String reportConfigViewersGroupIdOrName = bean.getReportConfigViewersGroupId();
    if (StringUtils.isNotBlank(reportConfigViewersGroupIdOrName) && !groupExists(reportConfigViewersGroupIdOrName, session)) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigViewersGroupComboErrorId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigInvalidViewersGroupNameIdError")));
      return false;
    }
    
    String reportConfigQuartzCron = bean.getReportConfigQuartzCron();
    if (StringUtils.isBlank(reportConfigQuartzCron)) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigQuartzCronId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigQuartzCronBlankError")));
      return false;
    }
    
    if (!isCronExpressionValid(reportConfigQuartzCron)) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigQuartzCronId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigQuartzCronInvalidError")));
      return false;
    }
    
    if (bean.isReportConfigSendEmail()) {
      
      if (!bean.isReportConfigSendEmailToViewers() && StringUtils.isBlank(bean.getReportConfigSendEmailToGroupId())) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#grouperReportConfigSendEmailToGroupIdId",
            TextContainer.retrieveFromRequest().getText().get("grouperReportConfigSendEmailToGroupIdBlankError")));
        return false;
      }
      
      if (StringUtils.isNotBlank(bean.getReportConfigSendEmailToGroupId()) && !groupExists(bean.getReportConfigSendEmailToGroupId(), session)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperReportConfigSendEmailToGroupIdId",
            TextContainer.retrieveFromRequest().getText().get("grouperReportConfigInvalidSendEmailToGroupNameIdError")));
        return false;
      }
    }
    
    if (StringUtils.isBlank(bean.getReportConfigQuery())) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigQueryId",
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigQueryBlankError")));
      return false;
    }
    
    return true;
  }
  
  private boolean groupExists(String idName, GrouperSession session) {
    
    Group group = GroupFinder.findByUuid(session, idName, false);
    if (group == null) {
      group = GroupFinder.findByName(session, idName, false);
    }
    
    if (group == null) {
      try {        
        Long mayBeGroupId = Long.valueOf(idName);
        group = GroupFinder.findByIdIndexSecure(mayBeGroupId, false, new QueryOptions());
      } catch(Exception e) {
        return false;
      }
    }
    return group != null;
  }
  
  private boolean isCronExpressionValid(String cronExpression) {
    try {
      CronExpressionDescriptor.getDescription(cronExpression);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  
  
  /**
   * make sure reports are enabled
   * @return true if k
   */
  private boolean checkReportConfigActive() {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    if (!GrouperReportSettings.grouperReportsEnabled()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportNotEnabledError")));
      return false;
    }

    
    return true;
  }

}
