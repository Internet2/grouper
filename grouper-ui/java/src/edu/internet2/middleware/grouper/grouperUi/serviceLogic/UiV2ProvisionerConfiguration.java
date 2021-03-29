package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperSyncLogWithOwner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisionerConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisionerLog;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ProvisionerConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;

public class UiV2ProvisionerConfiguration {
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2ProvisionerConfiguration.class);
  
  /**
   * keep an expirble cache of diagnostic progress for 5 hours (longest a diagnostics is expected).  This has multikey of session id and some random uuid
   * uniquely identifies this diagnostics request as opposed to other diagnostics in other tabs
   */
  private static ExpirableCache<MultiKey, GrouperProvisioner> diagnosticsThreadProgress = new ExpirableCache<MultiKey, GrouperProvisioner>(300);

  /**
   * start diagnostics
   * @param request
   * @param response
   */
  public void diagnostics(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startNanos = System.nanoTime();
    
    debugMap.put("method", "diagnostics");
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }

      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      if (provisioner == null) {
        throw new RuntimeException("No provisioner found for "+provisionerConfigId);
      }

      provisioner.initialize(GrouperProvisioningType.diagnostics);
      
      final GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      
      grouperRequestContainer.setGrouperProvisioningDiagnosticsContainer(grouperProvisioningDiagnosticsContainer);

      String initted = request.getParameter("provisionerInitted");
      if (!GrouperUtil.booleanValue(initted, false)) {
        
        grouperProvisioningDiagnosticsContainer.initFromConfiguration();
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/provisionerConfigs/provisionerDiagnosticsInit.jsp"));
        return;
      }      
      String sessionId = request.getSession().getId();
      
      debugMap.put("sessionId", GrouperUtil.abbreviate(sessionId, 8));
      
      // uniquely identifies this diagnostics as opposed to other diagnostics in other tabs
      String uniqueDiagnosticsId = GrouperUuid.getUuid();
  
      debugMap.put("uniqueDiagnosticsId", GrouperUtil.abbreviate(uniqueDiagnosticsId, 8));
  
      grouperProvisioningDiagnosticsContainer.setUniqueDiagnosticsId(uniqueDiagnosticsId);
      
      {
        //deal with inputs and configuration
        {
          boolean diagnosticsGroupsAllSelectName = GrouperUtil.booleanValue(request.getParameter("diagnosticsGroupsAllSelectName[]"), false);
          grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(diagnosticsGroupsAllSelectName);
        }
        {
          boolean diagnosticsEntitiesAllSelectName = GrouperUtil.booleanValue(request.getParameter("diagnosticsEntitiesAllSelectName[]"), false);
          grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntitiesAllSelect(diagnosticsEntitiesAllSelectName);
        }
        {
          boolean diagnosticsMembershipsAllSelectName = GrouperUtil.booleanValue(request.getParameter("diagnosticsMembershipsAllSelectName[]"), false);
          grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipsAllSelect(diagnosticsMembershipsAllSelectName);
        }
        {
          String diagnosticsGroupNameName = request.getParameter("diagnosticsGroupNameName");
          grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName(diagnosticsGroupNameName);
        }
      }
      
      
      MultiKey diagnosticsMultiKey = new MultiKey(sessionId, uniqueDiagnosticsId);
      
      diagnosticsThreadProgress.put(diagnosticsMultiKey, provisioner);
      
      GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("grouperProvisioningDiagnostics") {

        @Override
        public Void callLogic() {
          try {
            provisioner.provision(GrouperProvisioningType.diagnostics);
          } catch (RuntimeException re) {
            provisioner.retrieveGrouperProvisioningDiagnosticsContainer().getProgressBean().setHasException(true);
            // log this since the thread will just end and will never get logged
            LOG.error("error", re);
          } finally {
            // we done
            provisioner.retrieveGrouperProvisioningDiagnosticsContainer().getProgressBean().setComplete(true);
          }
          return null;
        }
      };      
      
      // see if running in thread
      boolean useThreads = GrouperUiConfig.retrieveConfig().propertyValueBooleanRequired("uiV2.provisioning.diagnostics.useThread");
      debugMap.put("useThreads", useThreads);

      if (useThreads) {
        
        GrouperFuture<Void> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
        
        Integer waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.provisioning.diagnostics.progressStartsInSeconds");
        debugMap.put("waitForCompleteForSeconds", waitForCompleteForSeconds);

        GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
        
        debugMap.put("threadAlive", !grouperFuture.isDone());

      } else {
        grouperCallable.callLogic();
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerDiagnosticsWrapper.jsp"));
      
      diagnosticsStatusHelper(sessionId, uniqueDiagnosticsId, provisionerConfigId);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * get diagnostics status
   * @param request
   * @param response
   */
  public void diagnosticsStatus(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }

      String sessionId = request.getSession().getId();
      String uniqueDiagnosticsId = request.getParameter("uniqueDiagnosticsId");
      diagnosticsStatusHelper(sessionId, uniqueDiagnosticsId, provisionerConfigId);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * get diagnostics status
   * @param request
   * @param response
   */
  private void diagnosticsStatusHelper(String sessionId, String uniqueDiagnosticsId, String provisionerConfigId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "diagnosticsStatusHelper");
    debugMap.put("provisionerConfigId", provisionerConfigId);
    debugMap.put("sessionId", GrouperUtil.abbreviate(sessionId, 8));
    
    long startNanos = System.nanoTime();
    try {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      MultiKey diagnosticsMultiKey = new MultiKey(sessionId, uniqueDiagnosticsId);
      
      GrouperProvisioner provisioner = diagnosticsThreadProgress.get(diagnosticsMultiKey);
      if (provisioner == null) return;
      
      GrouperProvisioningDiagnosticsContainer diagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      
      GrouperRequestContainer.retrieveFromRequestOrCreate().setGrouperProvisioningDiagnosticsContainer(diagnosticsContainer);
  
      //show the diagnostics screen
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#id_"+uniqueDiagnosticsId, 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerDiagnostics.jsp"));

      debugMap.put("percentComplete", diagnosticsContainer.getProgressBean().getPercentComplete());
      debugMap.put("progressCompleteRecords", diagnosticsContainer.getProgressBean().getProgressCompleteRecords());
      debugMap.put("progressTotalRecords", diagnosticsContainer.getProgressBean().getProgressTotalRecords());

      if (diagnosticsContainer != null) {
        
        // endless loop?
        if (diagnosticsContainer.getProgressBean().isThisLastStatus()) {
          return;
        }
        
        if (diagnosticsContainer.getProgressBean().isHasException()) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("grouperProvisioningDiagnosticsException")));
          // it has an exception, leave it be
          diagnosticsThreadProgress.put(diagnosticsMultiKey, null);
          return;
        }
        // kick it off again?
        debugMap.put("complete", diagnosticsContainer.getProgressBean().isComplete());
        if (!diagnosticsContainer.getProgressBean().isComplete()) {
          int progressRefreshSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.provisioning.diagnostics.progressRefreshSeconds");
          progressRefreshSeconds = Math.max(progressRefreshSeconds, 1);
          progressRefreshSeconds *= 1000;
          guiResponseJs.addAction(GuiScreenAction.newScript("setTimeout(function() {ajax('../app/UiV2ProvisionerConfiguration.diagnosticsStatus?uniqueDiagnosticsId=" + uniqueDiagnosticsId + "&provisionerConfigId="+provisionerConfigId+"')}, " + progressRefreshSeconds + ")"));
        } else {
          // it is complete, leave it be
          diagnosticsThreadProgress.put(diagnosticsMultiKey, null);
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
   * view configured provisioner configurations
   * @param request
   * @param response
   */
  public void viewProvisionerConfigurations(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String sessionId = request.getSession().getId();
      String uniqueDiagnosticsId = request.getParameter("uniqueDiagnosticsId");
      
      if (StringUtils.isNotBlank(uniqueDiagnosticsId)) {
        MultiKey diagnosticsMultiKey = new MultiKey(sessionId, uniqueDiagnosticsId);
        diagnosticsThreadProgress.put(diagnosticsMultiKey, null);
      }
      
      List<ProvisionerConfiguration> provisionerConfigurations = ProvisionerConfiguration.retrieveAllProvisionerConfigurations();
      
      List<GuiProvisionerConfiguration> guiProvisionerConfigurations = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfigurations);
      
      provisionerConfigurationContainer.setGuiProvisionerConfigurations(guiProvisionerConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigs.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view details of one provisioner configuration
   * @param request
   * @param response
   */
  public void viewProvisionerConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigDetails.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * view provisioner jobs
   * @param request
   * @param response
   */
  public void viewProvisionerJobs(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      List<GcGrouperSyncJob> grouperSyncJobs = GrouperProvisioningService.retrieveGcGroupSyncJobs(provisionerConfigId);
      
      provisionerConfigurationContainer.setProvisionerJobs(grouperSyncJobs);
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigJobs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view provisioner job details
   * @param request
   * @param response
   */
  public void viewProvisionerJobDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");

      String provisionerJobId = request.getParameter("provisionerJobId");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerJobId)) {
        throw new RuntimeException("provisionerJobId cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerConfigId);
      
      if (gcGrouperSync == null) {
        throw new RuntimeException("Invalid provisionerConfigId: "+provisionerConfigId);
      }
      
      GcGrouperSyncJob grouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveById(provisionerJobId);
      if (grouperSyncJob == null) {
        throw new RuntimeException("Invalid provisionerJobId: "+provisionerJobId);
      }
      
      provisionerConfigurationContainer.setGrouperSyncJob(grouperSyncJob);
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigJobDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * user clicked on view provisioner activity action. show screen so user can pick Group, Entity or Membership
   * @param request
   * @param response
   */
  public void viewProvisionerActivity(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigViewActivity.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * user submitted view provisioner activity form. show activity for the selected object type
   * @param request
   * @param response
   */
  public void viewProvisionerActivitySubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
      
      String provisionerConfigObjectType = request.getParameter("provisionerConfigObjectType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigObjectType)) {
        throw new RuntimeException("provisionerConfigObjectType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      
      if (provisionerConfigObjectType.equals("group")) {
        List<GcGrouperSyncGroup> activityForGroup = GrouperProvisioningService.retrieveRecentActivityForGroup(provisionerConfigId);
        provisionerConfigurationContainer.setActivityForGroup(activityForGroup);
      } else if (provisionerConfigObjectType.equals("entity")) {
        List<GcGrouperSyncMember> activityForMember = GrouperProvisioningService.retrieveRecentActivityForMember(provisionerConfigId);
        provisionerConfigurationContainer.setActivityForMember(activityForMember);
      } else if (provisionerConfigObjectType.equals("membership")) {
        List<GcGrouperSyncMembership> activityForMembership = GrouperProvisioningService.retrieveRecentActivityForMembership(provisionerConfigId);
        provisionerConfigurationContainer.setActivityForMembership(activityForMembership);
      } else {
        throw new RuntimeException("invalid provisionerConfigObjectType: "+provisionerConfigObjectType);
      }
      
      provisionerConfigurationContainer.setProvisionerConfigObjectType(provisionerConfigObjectType);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigViewActivity.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view provisioner logs
   * @param request
   * @param response
   */
  public void viewProvisionerLogs(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      GuiPaging guiPaging = provisionerConfigurationContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions, "uiV2.provisioning.logs.default.page.size");
      
      List<GrouperSyncLogWithOwner> grouperSyncLogsWithOwner = GrouperProvisioningService.retrieveGcGrouperSyncLogs(provisionerConfigId, queryOptions);
      
      List<GuiProvisionerLog> guiProvisionerLogs = GuiProvisionerLog.convertFromGcGrouperSyncWithOwner(grouperSyncLogsWithOwner);
      
      provisionerConfigurationContainer.setGuiProvisionerLogs(guiProvisionerLogs);
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigLogs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show screen to add a new provisioner configuration
   * @param request
   * @param response
   */
  public void addProvisionerConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
      
      if (StringUtils.isNotBlank(provisionerConfigType)) {
        
        if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
          throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
        }

        Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
        ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(provisionerConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#provisionerConfigId",
              TextContainer.retrieveFromRequest().getText().get("provisionerConfigCreateErrorConfigIdRequired")));
          guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("provisionerConfigType", ""));
          return;
        }
        
        provisionerConfiguration.setConfigId(provisionerConfigId);
        
        if (provisionerConfiguration.retrieveConfigurationConfigIds().contains(provisionerConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#provisionerConfigId", TextContainer.retrieveFromRequest().getText().get("grouperConfigurationValidationConfigIdUsed")));
          return;
        }
        
        String previousProvisionerConfigId = request.getParameter("previousProvisionerConfigId");
        String previousProvisionerConfigType = request.getParameter("previousProvisionerConfigType");
        if (StringUtils.isBlank(previousProvisionerConfigId) 
            || !StringUtils.equals(provisionerConfigType, previousProvisionerConfigType)) {
          // first time loading the screen or
          // provisioner config type changed
          // let's get values from config files/database
        } else {
          provisionerConfiguration.populateConfigurationValuesFromUi(request);
        }
        
        GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigAdd.jsp"));
      
      String focusOnElementName = request.getParameter("focusOnElementName");
      if (!StringUtils.isBlank(focusOnElementName)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$(\"[name='" + focusOnElementName + "']\").focus()"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * insert a new provisioner config to db
   * @param request
   * @param response
   */
  public void addProvisionerConfigurationSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      provisionerConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      {
        //lets validate via provisioner 
        Map<String, String> fieldSuffixToValue = new HashMap<String, String>();
        Map<String, GrouperConfigurationModuleAttribute> attributesBySuffix = provisionerConfiguration.retrieveAttributes();
        for (String suffix : attributesBySuffix.keySet()) {
          
          GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = attributesBySuffix.get(suffix);
          
          if (grouperConfigurationModuleAttribute.isHasValue()) {
            fieldSuffixToValue.put(suffix, grouperConfigurationModuleAttribute.getValue());
          }
        }

        GrouperConfigurationModuleAttribute provisionerClassModuleAttribute = attributesBySuffix.get("class");        
        if (provisionerClassModuleAttribute != null && provisionerClassModuleAttribute.isHasValue()) {
          String provisionerClassName = provisionerClassModuleAttribute.getValue();
          Class<GrouperProvisioner> grouperProvisionerClass = GrouperUtil.forName(provisionerClassName);
          GrouperProvisioner grouperProvisioner = GrouperUtil.newInstance(grouperProvisionerClass);
          List<MultiKey> errorAndSuffixList = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromSuffixValueMap(fieldSuffixToValue);
          for (MultiKey errorAndSuffix : GrouperUtil.nonNull(errorAndSuffixList)) {
            String error = (String)errorAndSuffix.getKey(0);
            String suffix = errorAndSuffix.size() >= 2 ? (String)errorAndSuffix.getKey(1) : null;
            if (StringUtils.isBlank(error)) {
              LOG.error("error is blank!!!! '" + suffix + "'");
              continue;
            }
            if (StringUtils.isBlank(suffix)) {
              errorsToDisplay.add(error);
            } else {
              if (suffix.startsWith("#")) {
                validationErrorsToDisplay.put(suffix, error);
              } else {
                validationErrorsToDisplay.put("#config_" + suffix + "_id", error);
              }
            }
          }
        }
      }
      
      // dont have double errors here
      if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
        // this will not continue if there are validation problems
        provisionerConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      }
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      GrouperProvisioningSettings.clearTargetsCache();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("provisionerConfigAddEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show screen to edit provisioner configuration
   * @param request
   * @param response
   */
  public void editProvisionerConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      String previousProvisionerConfigId = request.getParameter("previousProvisionerConfigId");
      
      if (StringUtils.isBlank(previousProvisionerConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      } else {
        // change was made on the form
        provisionerConfiguration.populateConfigurationValuesFromUi(request);
        GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigEdit.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * update an existing provisioner config in db
   * @param request
   * @param response
   */
  public void editProvisionerConfigurationSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      provisionerConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      provisionerConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);

      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      GrouperProvisioningSettings.clearTargetsCache();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("provisionerConfigAddEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete a provisioner configuration
   * @param request
   * @param response
   */
  public void deleteProvisionerConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      provisionerConfiguration.deleteConfig(true);
      
      Thread thread = new Thread(new Runnable() {
        public void run() {
          GrouperSession.startRootSession();
          GrouperProvisioningService.deleteInvalidConfigs();
        }
      });
      thread.start();
      
      GrouperProvisioningSettings.clearTargetsCache();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisionerConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * show form to run full sync job
   * @param request
   * @param response
   */
  public void runFullSync(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisionerConfiguration> klass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisionerConfiguration provisionerConfiguration = (ProvisionerConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigRunFullSync.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * run full sync job
   * @param request
   * @param response
   */
  public void runFullSyncSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      String provisionerConfigType = request.getParameter("provisionerConfigType");
      String synchronous = request.getParameter("provisionerConfigSynchoronous");
      String readOnly = request.getParameter("provisionerConfigReadOnly");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        throw new RuntimeException("provisionerConfigType cannot be blank");
      }
      
      if (StringUtils.isBlank(synchronous)) {
        throw new RuntimeException("provisionerConfigSynchoronous cannot be blank");
      }
      
      if (StringUtils.isBlank(readOnly)) {
        throw new RuntimeException("provisionerConfigReadOnly cannot be blank");
      }
      
      if (!ProvisionerConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      ProvisioningMessage provisioningMessage = new ProvisioningMessage();
      provisioningMessage.setFullSync(true);
      provisioningMessage.setBlocking(BooleanUtils.toBoolean(synchronous));
      provisioningMessage.setReadOnly(BooleanUtils.toBoolean(readOnly));
      provisioningMessage.send(provisionerConfigId);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PROVISIONER_SYNC_RUN, "provisionerName", provisionerConfigId);
      auditEntry.setDescription("Ran provisioner sync for "+provisionerConfigId);
      provisionerSaveAudit(auditEntry);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisionerConfigRunFullSyncSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * 
   * @param auditEntry
   */
  private static void provisionerSaveAudit(final AuditEntry auditEntry) {
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
