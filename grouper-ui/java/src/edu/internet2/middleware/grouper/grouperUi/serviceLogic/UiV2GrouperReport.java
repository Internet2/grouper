/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
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
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
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
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import net.redhogs.cronparser.CronExpressionDescriptor;

/**
 * 
 */
public class UiV2GrouperReport {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2GrouperReport.class);
    
  /**
   * view report configs on a folder
   * @param request
   * @param response
   */
  public void viewReportConfigsOnFolder(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      List<GuiReportConfig> guiReportConfigs = buildGuiReportConfigs(STEM);
      
      grouperReportContainer.setGuiReportConfigs(guiReportConfigs);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfig.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * view report configs on a group
   * @param request
   * @param response
   */
  public void viewReportConfigsOnGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      List<GuiReportConfig> guiReportConfigs = buildGuiReportConfigs(GROUP);
      
      grouperReportContainer.setGuiReportConfigs(guiReportConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/groupReportConfig.jsp"));
      
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
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEdit")));
        return;
      }
      
      
      @SuppressWarnings("unchecked")
      List<GrouperReportConfigurationBean> reportConfigBeans = (List<GrouperReportConfigurationBean>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
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
   * edit button was clicked on group report screen
   * @param request
   * @param response
   */
  public void reportOnGroupEdit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEdit")));
        return;
      }
      
      
      @SuppressWarnings("unchecked")
      List<GrouperReportConfigurationBean> reportConfigBeans = (List<GrouperReportConfigurationBean>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return GrouperReportConfigService.getGrouperReportConfigs(GROUP);
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
          "/WEB-INF/grouperUi2/grouperReport/groupReportConfigEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * screen to allow adding a new report config for folders
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
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAdd")));
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
   * screen to allow adding a new report config for groups
   * @param request
   * @param response
   */
  public void reportOnGroupAdd(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAdd")));
        return;
      }
      
      GrouperReportConfigurationBean newBean = new GrouperReportConfigurationBean();
      populateGrouperReportConfigBean(request, newBean);
      
      grouperReportContainer.setConfigBean(newBean);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/groupReportConfigAdd.jsp"));
      
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
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAdd")));
        return;
      }
      
      final GrouperReportConfigurationBean bean = new GrouperReportConfigurationBean();
      final String mode = request.getParameter("mode");
      final boolean isAdd = (mode != null) && mode.equalsIgnoreCase("add");
      
      populateGrouperReportConfigBean(request, bean);
      boolean isValid = validateGrouperReportConfigBean(bean, grouperSession, STEM, isAdd);
      if (!isValid) {
        return;
      }
      
      boolean saved = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          try {    
            GrouperReportConfigService.saveOrUpdateReportConfigAttributes(bean, STEM);
            GrouperReportConfigurationBean savedBean = GrouperReportConfigService.getGrouperReportConfigBean(STEM, bean.getReportConfigName());
            if (!isAdd) {              
              GrouperReportConfigService.unscheduleJob(savedBean, STEM); // unschedule and schedule again if we are editing
            }
            if (savedBean.isReportConfigEnabled()) {
              GrouperReportConfigService.scheduleJob(savedBean, STEM);
            }
            
            if (isAdd) {
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_REPORT_CONFIG_ADD, "stemId", 
                  STEM.getId(), "stemName", STEM.getName(), "reportConfigId", savedBean.getAttributeAssignmentMarkerId());
              auditEntry.setDescription("Addded report config on : " + STEM.getName() + " with id " + savedBean.getAttributeAssignmentMarkerId());
              reportSaveAudit(auditEntry);
            } else {
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_REPORT_CONFIG_UPDATE, "stemId", 
                  STEM.getId(), "stemName", STEM.getName(), "reportConfigId", savedBean.getAttributeAssignmentMarkerId());
              auditEntry.setDescription("Updated report config on : " + STEM.getName() + " with id " + savedBean.getAttributeAssignmentMarkerId());
              reportSaveAudit(auditEntry);
            }
            
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigAddEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * Save a new report config to the database 
   * @param request
   * @param response
   */
  public void reportOnGroupAddEditSubmit(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToAdd")));
        return;
      }
      
      final GrouperReportConfigurationBean bean = new GrouperReportConfigurationBean();
      final String mode = request.getParameter("mode");
      final boolean isAdd = (mode != null) && mode.equalsIgnoreCase("add");
      
      populateGrouperReportConfigBean(request, bean);
      boolean isValid = validateGrouperReportConfigBean(bean, grouperSession, GROUP, isAdd);
      if (!isValid) {
        return;
      }
      
      boolean saved = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          try {    
            GrouperReportConfigService.saveOrUpdateReportConfigAttributes(bean, GROUP);
            GrouperReportConfigurationBean savedBean = GrouperReportConfigService.getGrouperReportConfigBean(GROUP, bean.getReportConfigName());
            if (!isAdd) {              
              GrouperReportConfigService.unscheduleJob(savedBean, GROUP); // unschedule and schedule again if we are editing
            }
            if (savedBean.isReportConfigEnabled()) {
              GrouperReportConfigService.scheduleJob(savedBean, GROUP);
            }
            
            if (isAdd) {
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_REPORT_CONFIG_ADD, "groupId", 
                  GROUP.getId(), "groupName", GROUP.getName(), "reportConfigId", savedBean.getAttributeAssignmentMarkerId());
              auditEntry.setDescription("Addded report config on : " + GROUP.getName() + " with id " + savedBean.getAttributeAssignmentMarkerId());
              reportSaveAudit(auditEntry);
            } else {
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_REPORT_CONFIG_UPDATE, "groupId", 
                  GROUP.getId(), "groupName", GROUP.getName(), "reportConfigId", savedBean.getAttributeAssignmentMarkerId());
              auditEntry.setDescription("Updated report config on : " + GROUP.getName() + " with id " + savedBean.getAttributeAssignmentMarkerId());
              reportSaveAudit(auditEntry);
            }
            
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=" + group.getId() + "')"));
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
  public void viewAllReportInstancesForFolder(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      GrouperReportConfigInstance grouperReportConfigInstance = (GrouperReportConfigInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
          
          if (!configBean.isCanRead(loggedInSubject)) {
            return null;
          }
          
          List<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(STEM, attributeAssignmentMarkerId);
          
          List<GuiReportInstance> guiReportInstances = GuiReportInstance.buildGuiReportInstances(configBean, new ArrayList<GrouperReportInstance>(reportInstances));
          
          GrouperReportConfigInstance grouperReportConfigInstance = new GrouperReportConfigInstance();
          grouperReportConfigInstance.setGuiReportInstances(guiReportInstances);
          grouperReportConfigInstance.setReportConfigBean(configBean);
          
          return grouperReportConfigInstance;
        }
        
      });
      
      if (grouperReportConfigInstance == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNoEntitiesFound")));
        return;
      }
      
      grouperReportContainer.setGrouperReportConfigInstance(grouperReportConfigInstance);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportConfigWithInstances.jsp"));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * view report instances for a report config
   * @param request
   * @param response
   */
  public void viewAllReportInstancesForGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
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
      
      GrouperReportConfigInstance grouperReportConfigInstance = (GrouperReportConfigInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
          
          if (!configBean.isCanRead(loggedInSubject)) {
            return null;
          }
          
          List<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(GROUP, attributeAssignmentMarkerId);
          
          List<GuiReportInstance> guiReportInstances = GuiReportInstance.buildGuiReportInstances(configBean, new ArrayList<GrouperReportInstance>(reportInstances));
          
          GrouperReportConfigInstance grouperReportConfigInstance = new GrouperReportConfigInstance();
          grouperReportConfigInstance.setGuiReportInstances(guiReportInstances);
          grouperReportConfigInstance.setReportConfigBean(configBean);
          
          return grouperReportConfigInstance;
        }
        
      });
      
      if (grouperReportConfigInstance == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNoEntitiesFound")));
        return;
      }
      
      grouperReportContainer.setGrouperReportConfigInstance(grouperReportConfigInstance);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/groupReportConfigWithInstances.jsp"));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view report instance details for a folder
   * @param request
   * @param response
   */
  public void viewReportInstanceDetailsForFolder(final HttpServletRequest request, HttpServletResponse response) {
    
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
      
      GuiReportInstance guiReportInstance = (GuiReportInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportInstance reportInstance = GrouperReportInstanceService.getReportInstance(attributeAssignId);
          
          String configMarkerAssignmentId = reportInstance.getReportInstanceConfigMarkerAssignmentId();
          
          GrouperReportConfigurationBean grouperReportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(configMarkerAssignmentId);
          
          if (grouperReportConfigBean.isCanRead(loggedInSubject)) {
            GuiReportInstance guiReportInstance = new GuiReportInstance();
            guiReportInstance.setReportConfigBean(grouperReportConfigBean);
            guiReportInstance.setReportInstance(reportInstance);
            return guiReportInstance;
          }
          
          return null;
        }
        
      });
      
      if (guiReportInstance == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToViewDetails")));
        return;
      }
      
      grouperReportContainer.setGuiReportInstance(guiReportInstance);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/folderReportInstanceDetails.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view report instance details for a group
   * @param request
   * @param response
   */
  public void viewReportInstanceDetailsForGroup(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
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
      
      GuiReportInstance guiReportInstance = (GuiReportInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportInstance reportInstance = GrouperReportInstanceService.getReportInstance(attributeAssignId);
          
          String configMarkerAssignmentId = reportInstance.getReportInstanceConfigMarkerAssignmentId();
          
          GrouperReportConfigurationBean grouperReportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(configMarkerAssignmentId);
          
          if (grouperReportConfigBean.isCanRead(loggedInSubject)) {
            GuiReportInstance guiReportInstance = new GuiReportInstance();
            guiReportInstance.setReportConfigBean(grouperReportConfigBean);
            guiReportInstance.setReportInstance(reportInstance);
            return guiReportInstance;
          }
          
          return null;
        }
        
      });
      
      if (guiReportInstance == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToViewDetails")));
        return;
      }
      
      grouperReportContainer.setGuiReportInstance(guiReportInstance);
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperReport/groupReportInstanceDetails.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * enable/disable button was clicked
   * @param request
   * @param response
   */
  public void changeReportConfigStatusForFolder(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEdit")));
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
      
      boolean isSaved = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
          try {
            if (newStatus.equals("enable")) { 
              configBean.setReportConfigEnabled(true);
              GrouperReportConfigService.scheduleJob(configBean, STEM);
            } else {
              configBean.setReportConfigEnabled(false);
              GrouperReportConfigService.unscheduleJob(configBean, STEM);
            }
          } catch(SchedulerException e) {
            return false;
          }
          
          GrouperReportConfigService.saveOrUpdateReportConfigAttributes(configBean, STEM);
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_REPORT_CONFIG_UPDATE, "stemId", 
              STEM.getId(), "stemName", STEM.getName(), "reportConfigId", configBean.getAttributeAssignmentMarkerId());
          auditEntry.setDescription("Changed status for report config on : " + STEM.getName() + " with id " + configBean.getAttributeAssignmentMarkerId());
          reportSaveAudit(auditEntry);
          
          return true;
        }
        
      });
      
      if (!isSaved) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportJobScheduleError")));
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigStatusChangeSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable/disable button was clicked
   * @param request
   * @param response
   */
  public void changeReportConfigStatusForGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!checkReportConfigActive()) {
        return;
      }
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEdit")));
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
      
      boolean isSaved = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
          try {
            if (newStatus.equals("enable")) {            
              configBean.setReportConfigEnabled(true);
              GrouperReportConfigService.scheduleJob(configBean, GROUP);
            } else {
              configBean.setReportConfigEnabled(false);
              GrouperReportConfigService.unscheduleJob(configBean, GROUP);
            }
          } catch(SchedulerException e) {
            return false;
          }
          
          GrouperReportConfigService.saveOrUpdateReportConfigAttributes(configBean, GROUP);
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_REPORT_CONFIG_UPDATE, "groupId",
              GROUP.getId(), "groupName", GROUP.getName(), "reportConfigId", configBean.getAttributeAssignmentMarkerId());
          auditEntry.setDescription("Changed status for report config on : " + GROUP.getName() + " with id " + configBean.getAttributeAssignmentMarkerId());
          reportSaveAudit(auditEntry);
          
          return true;
        }
        
      });
      
      if (!isSaved) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportJobScheduleError")));
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigStatusChangeSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * download report link was clicked
   * @param request
   * @param response
   */
  public void downloadReportForFolder(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("why is attributeAssignId blank??");
      }
      
      GrouperReportInstance reportInstance = (GrouperReportInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          GrouperReportInstance recentReportInstance = GrouperReportInstanceService.getReportInstance(attributeAssignId);
          return recentReportInstance;
        }
        
      });
      
      if (reportInstance == null) {
        throw new RuntimeException("No report instance found");
      }
      
      
      if (!reportInstance.getGrouperReportConfigurationBean().isCanRead(loggedInSubject)) {

        final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToDownload")));
        return;
      }
      
      Stem stem = (Stem) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Stem stem = UiV2Stem.retrieveStemHelper(request, false).getStem();
          return stem;
        }
        
      });
      
      if (stem == null) {
        throw new RuntimeException();
      }
      
      final Stem STEM = stem;
      
      try {
        
        String reportContent = GrouperReportLogic.getReportContent(reportInstance);
        
        response.setContentType("text/csv");
        response.setHeader ("Content-Disposition", "inline;filename=\"" + reportInstance.getReportInstanceFileName() + "\"");
        
        PrintWriter out = response.getWriter();
        out.write(reportContent);
        out.close();
        
        reportInstance.setReportInstanceDownloadCount(reportInstance.getReportInstanceDownloadCount() + 1);
        GrouperReportInstanceService.saveReportInstanceAttributes(reportInstance, stem);
        
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_REPORT_DOWNLONAD, "stemId", STEM.getId(),
            "stemName", STEM.getName(), "reportInstanceId", reportInstance.getAttributeAssignId());
        auditEntry.setDescription("Downloaded report " + reportInstance.getReportInstanceFileName()+" for " + STEM.getName());
        reportSaveAudit(auditEntry);
        
      } catch (IOException e) {
        throw new RuntimeException("Error occured while downloading the report");
      }
      
      throw new ControllerDone();
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * download report link was clicked
   * @param request
   * @param response
   */
  public void downloadReportForGroup(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      if (!checkReportConfigActive()) {
        return;
      }
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("why is attributeAssignId blank??");
      }
      
      GrouperReportInstance reportInstance = (GrouperReportInstance) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          GrouperReportInstance recentReportInstance = GrouperReportInstanceService.getReportInstance(attributeAssignId);
          return recentReportInstance;
        }
        
      });
      
      if (!reportInstance.getGrouperReportConfigurationBean().isCanRead(loggedInSubject)) {

        final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToDownload")));
        return;
      }
      
      Group group = (Group) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
          
          return group;
        }
        
      });
      
      if (group == null) {
        throw new RuntimeException();
      }
      
      final Group GROUP = group;
      
      try {
        
        String reportContent = GrouperReportLogic.getReportContent(reportInstance);
        
        response.setContentType("text/csv");
        response.setHeader ("Content-Disposition", "inline;filename=\"" + reportInstance.getReportInstanceFileName() + "\"");
        
        PrintWriter out = response.getWriter();
        out.write(reportContent);
        out.close();
        
        reportInstance.setReportInstanceDownloadCount(reportInstance.getReportInstanceDownloadCount() + 1);
        GrouperReportInstanceService.saveReportInstanceAttributes(reportInstance, group);
        
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_REPORT_DOWNLONAD, "groupId", GROUP.getId(),
            "groupName", GROUP.getName(), "reportInstanceId", reportInstance.getAttributeAssignId());
        auditEntry.setDescription("Downloaded report " + reportInstance.getReportInstanceFileName()+" for " + GROUP.getName());
        reportSaveAudit(auditEntry);
        
      } catch (IOException e) {
        throw new RuntimeException("Error occured while downloading the report");
      }
      
      throw new ControllerDone();
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
 
  /**
   * delete report config
   * @param request
   * @param response
   */
  public void deleteReportConfigForFolder(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!checkReportConfigActive()) {
        return;
      }
            
      final String attributeAssignmentMarkerId = request.getParameter("attributeAssignmentMarkerId");
      if (StringUtils.isBlank(attributeAssignmentMarkerId)) {
        throw new RuntimeException("why is attributeAssignmentMarkerId blank??");
      }
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEdit")));
        return;
      }
      
      final Stem stem = (Stem) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Stem stem = UiV2Stem.retrieveStemHelper(request, false).getStem();
          return stem;
        }
        
      });
      
      if (stem == null) {
        throw new RuntimeException();
      }
      
      final Stem STEM = stem;
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
          if (configBean == null) {
            throw new RuntimeException("Invalid attributeAssignmentMarkerId");
          }
          
          try {            
            GrouperReportConfigService.deleteGrouperReportConfig(stem, configBean);
          } catch(SchedulerException e) {
            LOG.error("Error deleting quartz job for config: "+configBean.getReportConfigName());
          }
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_REPORT_CONFIG_DELETE, "stemId", 
              STEM.getId(), "stemName", STEM.getName(), "reportConfigId", configBean.getAttributeAssignmentMarkerId());
          auditEntry.setDescription("Deleted report config on : " + STEM.getName() + " with id " + configBean.getAttributeAssignmentMarkerId());
          reportSaveAudit(auditEntry);
          
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * delete report config
   * @param request
   * @param response
   */
  public void deleteReportConfigForGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!checkReportConfigActive()) {
        return;
      }
            
      final String attributeAssignmentMarkerId = request.getParameter("attributeAssignmentMarkerId");
      if (StringUtils.isBlank(attributeAssignmentMarkerId)) {
        throw new RuntimeException("why is attributeAssignmentMarkerId blank??");
      }
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final GrouperReportContainer grouperReportContainer = grouperRequestContainer.getGrouperReportContainer();
      
      if (!grouperReportContainer.isCanWriteGrouperReports()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperReportNotAllowedToEdit")));
        return;
      }
      
      final Group group = (Group) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
          return group;
        }
        
      });
      
      if (group == null) {
        throw new RuntimeException();
      }
      
      final Group GROUP = group;
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
          if (configBean == null) {
            throw new RuntimeException("Invalid attributeAssignmentMarkerId");
          }
          
          try {
            GrouperReportConfigService.deleteGrouperReportConfig(group, configBean);
          }
          catch(SchedulerException e) {
            LOG.error("Error deleting quartz job for config: "+configBean.getReportConfigName());
          }
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_REPORT_CONFIG_DELETE, "groupId", 
              GROUP.getId(), "groupName", GROUP.getName(), "reportConfigId", configBean.getAttributeAssignmentMarkerId());
          auditEntry.setDescription("Deleted report config on : " + GROUP.getName() + " with id " + configBean.getAttributeAssignmentMarkerId());
          reportSaveAudit(auditEntry);
          
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperReportConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * populate report config bean object from the form
   * @param request
   * @param bean
   */
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
    
    String viewersGroup = request.getParameter("grouperReportConfigViewersGroupComboName");
    
    if (StringUtils.isBlank(viewersGroup)) {
      //if didnt pick one from results
      viewersGroup = request.getParameter("grouperReportConfigViewersGroupComboNameDisplay");
    }
    
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
    
    String reportConfigSendEmailToGroupId = request.getParameter("grouperReportConfigSendEmailToGroupComboName");
    if (StringUtils.isBlank(reportConfigSendEmailToGroupId)) {
      //if didnt pick one from results
      reportConfigSendEmailToGroupId = request.getParameter("grouperReportConfigSendEmailToGroupComboNameDisplay");
    }
    bean.setReportConfigSendEmailToGroupId(reportConfigSendEmailToGroupId);
    
    String reportConfigQuery = request.getParameter("grouperReportConfigQuery");
    bean.setReportConfigQuery(reportConfigQuery);
    
  }
  
  /**
   * validate grouper report config bean
   * @param bean
   * @param session
   * @param grouperObject
   * @param isAddMode
   * @return
   */
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
      GrouperReportConfigurationBean existingConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(grouperObject, bean.getReportConfigName());
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
  
  /**
   * check if group exists - look up by id and name
   * @param idName
   * @param session
   * @return
   */
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
  
  /**
   * is cron expression valid
   * @param cronExpression
   * @return
   */
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
  
  /**
   * @param grouperObject
   * @return
   */
  private List<GuiReportConfig> buildGuiReportConfigs(final GrouperObject grouperObject) {
    
    @SuppressWarnings("unchecked")
    List<GuiReportConfig> guiReportConfigs = (List<GuiReportConfig>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        List<GuiReportConfig> guiReportConfigs = new ArrayList<GuiReportConfig>();
        List<GrouperReportConfigurationBean> grouperReportConfigs = GrouperReportConfigService.getGrouperReportConfigs(grouperObject);
        
        for (GrouperReportConfigurationBean configBean: grouperReportConfigs) {
          GrouperReportInstance mostRecentReportInstance = GrouperReportInstanceService.getMostRecentReportInstance(grouperObject, configBean.getAttributeAssignmentMarkerId());
          GuiReportConfig guiReportConfig = new GuiReportConfig(configBean, mostRecentReportInstance);
          if (guiReportConfig.isCanRead()) {              
            guiReportConfigs.add(guiReportConfig);
          }
        }
        
        return guiReportConfigs;
      }
      
    });
    return guiReportConfigs;
  }

  
  /**
   * 
   * @param auditEntry
   */
  private static void reportSaveAudit(final AuditEntry auditEntry) {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
          new HibernateHandler() {
              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                auditEntry.saveOrUpdate(true);
                return null;
              }
        });

  }
}
