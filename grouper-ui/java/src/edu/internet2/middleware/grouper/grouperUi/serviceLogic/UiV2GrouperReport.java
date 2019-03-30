/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashMap;
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
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportSettings;
import edu.internet2.middleware.grouper.app.reports.ReportConfigFormat;
import edu.internet2.middleware.grouper.app.reports.ReportConfigType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperReportContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
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
      
      //TODO we need to merge report config with report instance to display attributes on the UI
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          GrouperReportService.getGrouperReportConfigs(STEM);
          return null;
        }
      });
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
          return GrouperReportService.getGrouperReportConfigs(STEM);
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
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!grouperReportContainer.isCanWriteGrouperReports()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAddFolder")));
            return false;
          }
          
          String mode = request.getParameter("mode");
          boolean isAdd = (mode != null) && mode.equalsIgnoreCase("add");
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
            GrouperReportService.saveOrUpdateReportConfigAttributes(bean, STEM);
            GrouperReportConfigurationBean savedBean = GrouperReportService.getGrouperReportConfig(STEM, bean.getReportConfigName());
            GrouperReportService.scheduleJob(savedBean, STEM);
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
      
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfigAdd.jsp"));
      
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
    
    String reportConfigViewersGroupIdOrName = request.getParameter("grouperReportConfigViewersGroupId");
    bean.setReportConfigViewersGroupId(reportConfigViewersGroupIdOrName);
    
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
      GrouperReportConfigurationBean existingConfigBean = GrouperReportService.getGrouperReportConfig(grouperObject, bean.getReportConfigName());
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
    if (!groupExists(reportConfigViewersGroupIdOrName, session)) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#grouperReportConfigViewersGroupIdId",
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
