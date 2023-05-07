package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningError;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningErrorSummary;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperSyncLogWithOwner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisionerConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisionerLog;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisioningAssignment;
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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;

public class UiV2ProvisionerConfiguration {
  
  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(UiV2ProvisionerConfiguration.class);
  
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

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);

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
          if (diagnosticsGroupsAllSelectName && provisioner.retrieveGrouperProvisioningBehavior().isSelectGroupsAll()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(diagnosticsGroupsAllSelectName);
          }
        }
        {
          boolean diagnosticsEntitiesAllSelectName = GrouperUtil.booleanValue(request.getParameter("diagnosticsEntitiesAllSelectName[]"), false);
          if (diagnosticsEntitiesAllSelectName && provisioner.retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntitiesAllSelect(diagnosticsEntitiesAllSelectName);
          }
        }
        {
          boolean diagnosticsMembershipsAllSelectName = GrouperUtil.booleanValue(request.getParameter("diagnosticsMembershipsAllSelectName[]"), false);
          if (diagnosticsMembershipsAllSelectName && provisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAll()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipsAllSelect(diagnosticsMembershipsAllSelectName);
          }
        }
        {
          String diagnosticsGroupNameName = request.getParameter("diagnosticsGroupNameName");
          grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName(diagnosticsGroupNameName);
        }
        {
          String diagnosticsSubjectIdOrIdentifierName = request.getParameter("diagnosticsSubjectIdOrIdentifierName");
          grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier(diagnosticsSubjectIdOrIdentifierName);
        }
        {
          boolean diagnosticsGroupsInsertName = GrouperUtil.booleanValue(request.getParameter("diagnosticsGroupsInsertName[]"), false);
          if (diagnosticsGroupsInsertName && provisioner.retrieveGrouperProvisioningBehavior().isInsertGroups()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(diagnosticsGroupsInsertName);
          }
        }
        {
          boolean diagnosticsGroupsDeleteName = GrouperUtil.booleanValue(request.getParameter("diagnosticsGroupsDeleteName[]"), false);
          if (diagnosticsGroupsDeleteName && provisioner.retrieveGrouperProvisioningBehavior().isDeleteGroups()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupDelete(diagnosticsGroupsDeleteName);
          }
        }
        {
          boolean diagnosticsEntitiesInsertName = GrouperUtil.booleanValue(request.getParameter("diagnosticsEntitiesInsertName[]"), false);
          if (diagnosticsEntitiesInsertName && provisioner.retrieveGrouperProvisioningBehavior().isInsertEntities()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(diagnosticsEntitiesInsertName);
          }
        }
        {
          boolean diagnosticsEntitiesDeleteName = GrouperUtil.booleanValue(request.getParameter("diagnosticsEntitiesDeleteName[]"), false);
          if (diagnosticsEntitiesDeleteName && provisioner.retrieveGrouperProvisioningBehavior().isDeleteEntities()) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityDelete(diagnosticsEntitiesDeleteName);
          }
        }
        {
          boolean diagnosticsMembershipInsertName = GrouperUtil.booleanValue(request.getParameter("diagnosticsMembershipInsertName[]"), false);
          if (diagnosticsMembershipInsertName) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipInsert(diagnosticsMembershipInsertName);
          }
        }
        {
          boolean diagnosticsMembershipDeleteName = GrouperUtil.booleanValue(request.getParameter("diagnosticsMembershipDeleteName[]"), false);
          if (diagnosticsMembershipDeleteName) {
            grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipDelete(diagnosticsMembershipDeleteName);
          }
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
      
      List<ProvisioningConfiguration> provisionerConfigurations = ProvisioningConfiguration.retrieveAllViewableProvisioningConfigurations(loggedInSubject);
      
      List<GuiProvisionerConfiguration> guiProvisionerConfigurations = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfigurations);
      
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
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
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
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);

      List<GcGrouperSyncJob> grouperSyncJobs = GrouperProvisioningService.retrieveGcGroupSyncJobs(provisionerConfigId);
      
      provisionerConfigurationContainer.setProvisionerJobs(grouperSyncJobs);
      
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
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerJobId = request.getParameter("provisionerJobId");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerJobId)) {
        throw new RuntimeException("provisionerJobId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerConfigId);
      
      if (gcGrouperSync == null) {
        throw new RuntimeException("Invalid provisionerConfigId: "+provisionerConfigId);
      }
      
      GcGrouperSyncJob grouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveById(provisionerJobId);
      if (grouperSyncJob == null) {
        throw new RuntimeException("Invalid provisionerJobId: "+provisionerJobId);
      }
      
      provisionerConfigurationContainer.setGrouperSyncJob(grouperSyncJob);
      
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
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigViewActivity.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * user clicked on view errors action. Show page that presents with filter
   * @param request
   * @param response
   */
  public void viewProvisionerErrors(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigViewErrors.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * user submitted view provisioner errors form. show errors for the selected filters
   * @param request
   * @param response
   */
  public void viewProvisionerErrorsSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigObjectType = request.getParameter("provisionerConfigObjectType");
      String provisionerConfigErrorType = request.getParameter("provisionerConfigErrorType");
      String provisionerConfigErrorDuration = request.getParameter("provisionerConfigErrorDuration");
      
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
      GrouperProvisioningErrorSummary provisioningErrorSummary = GrouperProvisioningService.retrieveProvisioningErrorSummary(provisionerConfigId, provisionerConfigObjectType,
          StringUtils.isBlank(provisionerConfigErrorType)? null: GcGrouperSyncErrorCode.valueOf(provisionerConfigErrorType), 
          provisionerConfigErrorDuration);
      
      provisionerConfigurationContainer.setGrouperProvisioningErrorSummary(provisioningErrorSummary);
      
      List<GrouperProvisioningError> provisioningErrors = GrouperProvisioningService.retrieveProvisioningErrors(provisionerConfigId, provisionerConfigObjectType, 
          StringUtils.isBlank(provisionerConfigErrorType)? null: GcGrouperSyncErrorCode.valueOf(provisionerConfigErrorType), 
          provisionerConfigErrorDuration);
      
      provisionerConfigurationContainer.setGrouperProvisioningErrors(provisioningErrors);
      
      provisionerConfigurationContainer.setProvisionerConfigObjectType(provisionerConfigObjectType);
      provisionerConfigurationContainer.setProvisionerConfigErrorDuration(provisionerConfigErrorDuration);
      provisionerConfigurationContainer.setSelectedErrorCode(provisionerConfigErrorType);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigViewErrors.jsp"));
      
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
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigObjectType = request.getParameter("provisionerConfigObjectType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigObjectType)) {
        throw new RuntimeException("provisionerConfigObjectType cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
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
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);

      GuiPaging guiPaging = provisionerConfigurationContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions, "uiV2.provisioning.logs.default.page.size");
      
      List<GrouperSyncLogWithOwner> grouperSyncLogsWithOwner = GrouperProvisioningService.retrieveGcGrouperSyncLogs(provisionerConfigId, queryOptions);
      
      List<GuiProvisionerLog> guiProvisionerLogs = GuiProvisionerLog.convertFromGcGrouperSyncWithOwner(grouperSyncLogsWithOwner);
      
      provisionerConfigurationContainer.setGuiProvisionerLogs(guiProvisionerLogs);
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigLogs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * view provisionable groups
   * @param request
   * @param response
   */
  public void groupsProvisionable(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      MultiKey totalCountAndListOfGroups = GrouperProvisioningService.retrieveGroupsProvisionable(provisionerConfigId);
      
      int totalGroups = (Integer)totalCountAndListOfGroups.getKey(0);
      List<GcGrouperSyncGroup> grouperSyncGroups = (List<GcGrouperSyncGroup>)totalCountAndListOfGroups.getKey(1);
      
      List<GuiGroup> guiGroups = new ArrayList<>();
      
      for (GcGrouperSyncGroup gcGrouperSyncGroup: grouperSyncGroups) {
        
        String groupId = gcGrouperSyncGroup.getGroupId();
        if (StringUtils.isNotBlank(groupId)) {
          Group group = GroupFinder.findByUuid(grouperSession, groupId, false);
          if (group != null) {
            GuiGroup guiGroup = new GuiGroup(group);
            guiGroups.add(guiGroup);
          }
        }
        
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);

      provisionerConfigurationContainer.setGuiGroupsProvisionable(guiGroups);
      provisionerConfigurationContainer.setTotalProvisionableGroups(totalGroups);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/groupsProvisionable.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  
  /**
   * view provisionable groups
   * @param request
   * @param response
   */
  public void viewAssignments(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (!provisionerConfigurationContainer.isCanViewProvisionerConfiguration(provisionerConfigId)) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      Map<String, List<String>> assignments = GrouperProvisioningService.retrieveAssignments(provisionerConfigId);
      
      List<String> groupNames = assignments.get("group");
      List<String> folderNames = assignments.get("stem");
      
      List<GuiProvisioningAssignment> guiProvisioningAssignments = new ArrayList<>();
      
      for (String groupName: groupNames) {
        Group group = GroupFinder.findByName(grouperSession, groupName, false);
        if (group != null) {
          GuiGroup guiGroup = new GuiGroup(group);
          guiProvisioningAssignments.add(new GuiProvisioningAssignment("group", guiGroup));
        }
      }

      for (String folderName: folderNames) {
        Stem stem = StemFinder.findByName(grouperSession, folderName, false);
        if (stem != null) {
          GuiStem guiStem = new GuiStem(stem);
          guiProvisioningAssignments.add(new GuiProvisioningAssignment("folder", guiStem));
        }
      }
      
      int totalAssignments = guiProvisioningAssignments.size();

      // show up to 1000
      if (totalAssignments > 1000) {
        guiProvisioningAssignments = guiProvisioningAssignments.subList(0, 1000);
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);

      provisionerConfigurationContainer.setGuiProvisioningAssignments(guiProvisioningAssignments);
      provisionerConfigurationContainer.setTotalProvisioningAssignments(totalAssignments);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/assignments.jsp"));
      
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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");

      String provisionerStartWithClass = request.getParameter("provisionerStartWithClass");
      
      if (StringUtils.isNotBlank(provisionerConfigType)) {
        
        if (!ProvisioningConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
          throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
        }

        Class<ProvisioningConfiguration> klass = (Class<ProvisioningConfiguration>) GrouperUtil.forName(provisionerConfigType);
        ProvisioningConfiguration provisionerConfiguration = (ProvisioningConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(provisionerConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#provisionerConfigId",
              TextContainer.retrieveFromRequest().getText().get("provisionerConfigCreateErrorConfigIdRequired")));
          guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("provisionerConfigType", ""));
          return;
        }
        
        provisionerConfiguration.setConfigId(provisionerConfigId);
        
        for (ProvisioningConfiguration provisioningConfiguration : ProvisioningConfiguration.retrieveAllProvisioningConfigurations()) {
          if (StringUtils.equals(provisioningConfiguration.getConfigId(), provisionerConfigId)) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
                "#provisionerConfigId", TextContainer.retrieveFromRequest().getText().get("grouperConfigurationValidationConfigIdUsed")));
            return;
          }
        }
        
        List<ProvisionerStartWithBase> startWithConfigClasses = provisionerConfiguration.getStartWithConfigClasses();
        
        if(startWithConfigClasses.size() > 0 && StringUtils.equals("empty", provisionerStartWithClass)) {
          GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
          provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
          
          provisionerConfigurationContainer.setShowStartWithSection(true);
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigAdd.jsp"));
          return;
        }
        
        String previousProvisionerStartWithClass = request.getParameter("previousProvisionerStartWithClass");
        
        boolean skipStartWith = false;
        if ( (StringUtils.isNotBlank(provisionerStartWithClass) && StringUtils.equals(provisionerStartWithClass, "blank")) || 
            (StringUtils.equals(previousProvisionerStartWithClass, "blank"))  ) {
          skipStartWith = true;
          provisionerConfigurationContainer.setBlankStartWithSelected(true);
        }
        
        if (!skipStartWith && StringUtils.isNotBlank(provisionerStartWithClass)) {
          Class<ProvisionerStartWithBase> startWithKlass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(provisionerStartWithClass);
          ProvisionerStartWithBase provisionerStartWith = (ProvisionerStartWithBase) GrouperUtil.newInstance(startWithKlass);
          provisionerStartWith.setConfigId(provisionerConfigId);
          
          provisionerStartWith.populateConfigurationValuesFromUi(request);
          
          provisionerConfigurationContainer.setProvisionerStartWith(provisionerStartWith);
          provisionerConfigurationContainer.setShowStartWithSection(true);
          
          
          String sessionId = request.getParameter("startWithSessionId");
          if (StringUtils.isBlank(sessionId)) {
            String newSessionId = GrouperUtil.uniqueId();
            provisionerConfigurationContainer.setStartWithSessionId(newSessionId);
            
            Map<String, GrouperConfigurationModuleAttribute> startWithAttributes = provisionerStartWith.retrieveAttributes();
            
            Map<String, String> configSuffixToValues = new HashMap<>();
            for (String key: startWithAttributes.keySet()) {
              String startWithValue = startWithAttributes.get(key).getValue();
              configSuffixToValues.put(key, startWithValue);
            }
            
            provisionerStartWith.populateCache(newSessionId, configSuffixToValues);
            
          } else {
            provisionerConfigurationContainer.setStartWithSessionId(sessionId);
            
            Map<String, GrouperConfigurationModuleAttribute> startWithAttributes = provisionerStartWith.retrieveAttributes();
            
            Map<String, String> configSuffixToValues = new HashMap<>();
            for (String key: startWithAttributes.keySet()) {
              String startWithValue = startWithAttributes.get(key).getValue();
              configSuffixToValues.put(key, startWithValue);
            }
            
            Set<String> suffixesUserJustChanged = new HashSet<>();
            Map<String, String> oldSuffixToValue = provisionerStartWith.getCachedConfigKeyToValue(sessionId);
            if (oldSuffixToValue != null) {
              
              // compare old values with new values and build suffixesUserJustChanged
              for (String key: startWithAttributes.keySet()) {
                String startWithNewValue = startWithAttributes.get(key).getValue();
                String startWithOldValue = oldSuffixToValue.get(key);
                
                if (!StringUtils.equals(startWithOldValue, startWithNewValue)) {
                  suffixesUserJustChanged.add(key);
                }
                
              }
              
            }
            
            provisionerStartWith.populateCache(sessionId, configSuffixToValues);
            
            Map<String, String> suffixToValueThatShouldChange = provisionerStartWith.screenRedraw(configSuffixToValues, suffixesUserJustChanged);
            if (suffixToValueThatShouldChange != null) {
              for (String key: suffixToValueThatShouldChange.keySet()) {
                String valueToSet = suffixToValueThatShouldChange.get(key);
                
                if (startWithAttributes.containsKey(key)) {
                  startWithAttributes.get(key).setValue(GrouperUtil.stringValue(valueToSet));
                  startWithAttributes.get(key).setShow(true);
                }
              }
            }
            
          }
          
          
        }
        
        // once the start with submit is done
        if (!skipStartWith && StringUtils.isNotBlank(previousProvisionerStartWithClass) && StringUtils.isBlank(provisionerStartWithClass)) {
          
          Class<ProvisionerStartWithBase> previousStartWithKlass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(previousProvisionerStartWithClass);
          ProvisionerStartWithBase previousProvisionerStartWith = (ProvisionerStartWithBase) GrouperUtil.newInstance(previousStartWithKlass);
          
          provisionerConfigurationContainer.setProvisionerStartWith(previousProvisionerStartWith);
          
          
        }
        
        if (!skipStartWith && ((GrouperUtil.nonNull(startWithConfigClasses).size() > 0
            && StringUtils.isBlank(provisionerStartWithClass)
            && StringUtils.isBlank(previousProvisionerStartWithClass))
            || (StringUtils.isNotBlank(provisionerStartWithClass)
                && StringUtils.isNotBlank(previousProvisionerStartWithClass)
                && !StringUtils.equals(provisionerStartWithClass, previousProvisionerStartWithClass)))) {
          GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
          provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
          provisionerConfigurationContainer.setShowStartWithSection(true);
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigAdd.jsp"));
          
          String focusOnElementName = request.getParameter("focusOnElementName");
          if (!StringUtils.isBlank(focusOnElementName)) {
            guiResponseJs.addAction(GuiScreenAction.newScript("$(\"[name='" + focusOnElementName + "']\").focus()"));
          }
          
          return;
        }
        
        String previousProvisionerConfigId = request.getParameter("previousProvisionerConfigId");
        String previousProvisionerConfigType = request.getParameter("previousProvisionerConfigType");
        
        if (StringUtils.isBlank(previousProvisionerConfigId)
            || !StringUtils.equals(provisionerConfigType, previousProvisionerConfigType)) {
          // first time loading the screen or
          // provisioner config type changed
          // let's get values from config files/database
          // let's also clear start with
          provisionerConfigurationContainer.setProvisionerStartWith(null);
        } else {
          
          
          provisionerConfiguration.populateConfigurationValuesFromUi(request);
        }
        
        GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
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
      
      if (!ProvisioningConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisioningConfiguration> klass = (Class<ProvisioningConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisioningConfiguration provisionerConfiguration = (ProvisioningConfiguration) GrouperUtil.newInstance(klass);
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      String provisionerStartWithClass = request.getParameter("provisionerStartWithClass");
      String previousProvisionerStartWithClass = request.getParameter("previousProvisionerStartWithClass");
      
      List<ProvisionerStartWithBase> startWithConfigClasses = provisionerConfiguration.getStartWithConfigClasses();
      
      if(startWithConfigClasses.size() > 0 && StringUtils.isBlank(previousProvisionerStartWithClass)) {
        
        GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#provisionerConfigStartWithId",
            TextContainer.retrieveFromRequest().getText().get("provisionerConfigStartWithIdRequired")));  
        
        provisionerConfigurationContainer.setShowStartWithSection(true);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigAdd.jsp"));
        
        
        return;
        
      }
      
      
      if (StringUtils.isNotBlank(provisionerStartWithClass) && !StringUtils.equals("empty", provisionerStartWithClass)) {
        Class<ProvisionerStartWithBase> startWithKlass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(provisionerStartWithClass);
        ProvisionerStartWithBase provisionerStartWith = (ProvisionerStartWithBase) GrouperUtil.newInstance(startWithKlass);
        provisionerStartWith.setConfigId(provisionerConfigId);
        
        provisionerStartWith.populateConfigurationValuesFromUi(request);
        provisionerConfigurationContainer.setProvisionerStartWith(provisionerStartWith);
        
        String sessionId = request.getParameter("startWithSessionId");
        if (StringUtils.isNotBlank(sessionId)) {
          provisionerConfigurationContainer.setStartWithSessionId(sessionId);
        }
        
        List<String> errorsToDisplay = new ArrayList<String>();
        Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
        
        provisionerStartWith.validatePreSave(false, errorsToDisplay, validationErrorsToDisplay);
        
        if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
          provisionerConfigurationContainer.setShowStartWithSection(true);
          for (String errorToDisplay: errorsToDisplay) {
            guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
          }
          for (String validationKey: validationErrorsToDisplay.keySet()) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
                validationErrorsToDisplay.get(validationKey)));
          }
        } else {
          
          provisionerStartWith.populateConfigurationValuesFromUi(request);
          Map<String, GrouperConfigurationModuleAttribute> startWithAttributes = provisionerStartWith.retrieveAttributes();
          
          Map<String, String> configSuffixToValues = new HashMap<>();
          Map<String, Object> provisionerSuffixToValue = new HashMap<>();
          Map<String, GrouperConfigurationModuleAttribute> attributes = provisionerConfiguration.retrieveAttributes();
          for (String key: startWithAttributes.keySet()) {
            
            GrouperConfigurationModuleAttribute startWithAttribute = startWithAttributes.get(key);
            
            String startWithValue = startWithAttribute.getValue();
            if (StringUtils.isBlank(startWithValue) && StringUtils.isNotBlank(startWithAttribute.getDefaultValue())) {
              startWithValue = startWithAttribute.getDefaultValue();
            }
            
            configSuffixToValues.put(key, startWithValue);

          }
          
          provisionerStartWith.populateProvisionerConfigurationValuesFromStartWith(configSuffixToValues, provisionerSuffixToValue);
          provisionerStartWith.manipulateProvisionerConfigurationValue(provisionerConfigId, configSuffixToValues, provisionerSuffixToValue);
          
          for (String key: provisionerSuffixToValue.keySet()) {
            Object valueToSet = provisionerSuffixToValue.get(key);
            
            if (attributes.containsKey(key)) {
              attributes.get(key).setValue(GrouperUtil.stringValue(valueToSet));
              
            }
          }
          
          for (String key: provisionerSuffixToValue.keySet()) {
            
            if (attributes.containsKey(key)) {
              
              if (attributes.get(key).getFormElement() == ConfigItemFormElement.DROPDOWN) {
                
                attributes.get(key).getGrouperConfigModule()
                  .populateValuesLabelsFromOptionValueClass(attributes, attributes.get(key));
                
              }
              
            }
          }
          
          provisionerConfigurationContainer.setShowStartWithSection(false);
        }
        
        GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigAdd.jsp"));
        
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        
        return;
        
      }
      
      provisionerConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      provisionerConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          //make error message clickable and navigate to the element when clicked
          String errorMessage = validationErrorsToDisplay.get(validationKey);
          String clickableErrorMessage = "<a href='"+validationKey+"'>"+errorMessage+"</a>";
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, clickableErrorMessage));
        }

        return;

      }
      
      GrouperProvisioningSettings.clearTargetsCache();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      StringBuilder messageBuilder = new StringBuilder();

      if (actionsPerformed.size() > 0) {
        for (String actionPerformed: actionsPerformed) {
          if (messageBuilder.length() > 0) {
            messageBuilder.append("<br />");
          }
          messageBuilder.append(actionPerformed);
        }
      }
      if (messageBuilder.length() > 0) {
        messageBuilder.append("<br />");
      }
      messageBuilder.append(TextContainer.retrieveFromRequest().getText().get("provisionerConfigAddEditSuccess"));
      guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, messageBuilder.toString()));
      
      
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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      String provisionerConfigType = request.getParameter("provisionerConfigType");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(provisionerConfigType)) {
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
        ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
        provisionerConfigType = provisionerConfiguration.getClass().getName();
      }
      
      if (!ProvisioningConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisioningConfiguration> klass = (Class<ProvisioningConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisioningConfiguration provisionerConfiguration = (ProvisioningConfiguration) GrouperUtil.newInstance(klass);
      
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      String previousProvisionerConfigId = request.getParameter("previousProvisionerConfigId");
      
      if (StringUtils.isBlank(previousProvisionerConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      } else {
        // change was made on the form
        provisionerConfiguration.populateConfigurationValuesFromUi(request);
        GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      }
      
      provisionerConfiguration.correctFormFieldsForExpressionLanguageValues();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigEdit.jsp"));
      
      String focusOnElementName = request.getParameter("focusOnElementName");
      if (!StringUtils.isBlank(focusOnElementName)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$(\"[name='" + focusOnElementName + "']\").focus()"));
      }

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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
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
      
      if (!ProvisioningConfiguration.provisionerConfigClassNames.contains(provisionerConfigType)) {            
        throw new RuntimeException("Invalid provisionerConfigType "+provisionerConfigType);
      }
      
      Class<ProvisioningConfiguration> klass = (Class<ProvisioningConfiguration>) GrouperUtil.forName(provisionerConfigType);
      ProvisioningConfiguration provisionerConfiguration = (ProvisioningConfiguration) GrouperUtil.newInstance(klass);
      provisionerConfiguration.setConfigId(provisionerConfigId);
      
      provisionerConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      List<String> actionsPerformed = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      provisionerConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);

      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          
          String errorMessage = validationErrorsToDisplay.get(validationKey);
          //make error message clickable and navigate to the element when clicked
          String clickableErrorMessage = "<a href='"+validationKey+"'>"+errorMessage+"</a>";
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, clickableErrorMessage));
        }

        return;

      }
      
      GrouperProvisioningSettings.clearTargetsCache();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      StringBuilder messageBuilder = new StringBuilder();
      
      if (actionsPerformed.size() > 0) {
        for (String actionPerformed: actionsPerformed) {
          if (messageBuilder.length() > 0) {
            messageBuilder.append("<br />");
          }
          messageBuilder.append(actionPerformed);
        }
      }
      if (messageBuilder.length() > 0) {
        messageBuilder.append("<br />");
      }
      messageBuilder.append(TextContainer.retrieveFromRequest().getText().get("provisionerConfigAddEditSuccess"));
      guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, messageBuilder.toString()));
      
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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
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
   * delete group member cache for a given provisioner
   * @param request
   * @param response
   */
  public void deleteGroupMemberCache(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      grouperProvisioner.setGcGrouperSync(GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerConfigId));
      
      List<GcGrouperSyncGroup> grouperSyncGroups = grouperProvisioner.getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll();
      
      for (GcGrouperSyncGroup gcGrouperSyncGroup: grouperSyncGroups) {
        gcGrouperSyncGroup.setGroupAttributeValueCache0(null);
        gcGrouperSyncGroup.setGroupAttributeValueCache1(null);
        gcGrouperSyncGroup.setGroupAttributeValueCache2(null);
        gcGrouperSyncGroup.setGroupAttributeValueCache3(null);
      }
      
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncGroupDao().internal_groupStoreAll();
      
      List<GcGrouperSyncMember> grouperSyncMembers = grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveAll();
      
      for (GcGrouperSyncMember grouperSyncMember: grouperSyncMembers) {
        grouperSyncMember.setEntityAttributeValueCache0(null);
        grouperSyncMember.setEntityAttributeValueCache1(null);
        grouperSyncMember.setEntityAttributeValueCache2(null);
        grouperSyncMember.setEntityAttributeValueCache3(null);
      }
      
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMemberDao().internal_memberStoreAll();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisionerGroupMemberCacheDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete all cache for a given provisioner
   * @param request
   * @param response
   */
  public void deleteAllCache(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ProvisionerConfigurationContainer provisionerConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer();
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      grouperProvisioner.setGcGrouperSync(GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerConfigId));
      
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMembershipDao().membershipDeleteAll(true);
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncGroupDao().groupDeleteAll(true, true);
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMemberDao().memberDeleteAll(true, true);
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncJobDao().jobDeleteAll(true);
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncLogDao().logDeleteAll();
      grouperProvisioner.getGcGrouperSync().getGcGrouperSyncDao().delete();
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisionerGroupMemberCacheDeleteSuccess")));
      
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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
            
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
      ProvisioningConfiguration provisionerConfiguration = grouperProvisioner.getControllerForProvisioningConfiguration();
      GuiProvisionerConfiguration guiProvisioningConfiguration = GuiProvisionerConfiguration.convertFromProvisioningConfiguration(provisionerConfiguration);
      provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisioningConfiguration);
      
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
      
      if (!provisionerConfigurationContainer.isCanEditProvisionerConfiguration()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String provisionerConfigId = request.getParameter("provisionerConfigId");
      String synchronous = request.getParameter("provisionerConfigSynchoronous");
      String readOnly = request.getParameter("provisionerConfigReadOnly");
            
      if (StringUtils.isBlank(provisionerConfigId)) {
        throw new RuntimeException("provisionerConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(synchronous)) {
        throw new RuntimeException("provisionerConfigSynchoronous cannot be blank");
      }
      
      if (StringUtils.isBlank(readOnly)) {
        throw new RuntimeException("provisionerConfigReadOnly cannot be blank");
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
