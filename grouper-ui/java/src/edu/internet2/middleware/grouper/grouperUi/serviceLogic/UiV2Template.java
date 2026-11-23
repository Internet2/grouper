package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.file.GrouperFileDao;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.template.GshOutputLine;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputMask;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecTestOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateTestExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateValidationService;
import edu.internet2.middleware.grouper.app.gsh.template.GshValidationLine;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupStemTemplateContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GshTemplateContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGshTemplateConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ServiceAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class UiV2Template {
	
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Template.class);
  
  
  private static Pattern grouperTemplateServiceClassPattern = Pattern.compile(
      "^grouper\\.template\\.([\\w-]+)\\.logicClass$");
  
  /**
   * keep an expirable cache of import progress for 5 hours (longest an import is expected).  This has multikey of session id and some random uuid
   * uniquely identifies this import as opposed to other imports in other tabs.  This cannot have any request objects or j2ee objects
   */
  private static ExpirableCache<MultiKey, Object> gshExecThreadProgress = new ExpirableCache<MultiKey, Object>(300);

  /**
   * Show fields for new template
   * @param request
   * @param response
   */
  public void newTemplate(final HttpServletRequest request, final HttpServletResponse response) {

    newTemplateHelper(request, response, false, false, true);
	  
  }
  
  /**
   * Show checkboxes/service actions for new template
   * @param request
   * @param response
   */
  public void loadBeansForServiceTemplateType(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Stem stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);
      
      if (templateLogic == null) {
        return;
      }
      
      templateLogic.initScreen();

      setTemplateOptions();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
      
      templateLogic.setStemId(stem.getUuid());
      
      templateContainer.setTemplateLogic(templateLogic);
      
      String templateType = request.getParameter("templateType");
      templateContainer.setTemplateType(templateType);
      
      boolean isSuccess = populateServiceInfo(request, templateLogic);
      if (!isSuccess) {
        return;
      }

      List<ServiceAction> serviceActions = templateLogic.getServiceActions();
      
      // make sure unique id
      Set<String> idsUsed = new HashSet<String>();
      for (ServiceAction serviceAction : GrouperUtil.nonNull(serviceActions)) {
        if (idsUsed.contains(serviceAction.getId())) {
          throw new RuntimeException("id must be unique for service actions in a template! " + serviceAction.getId());
        }
        idsUsed.add(serviceAction.getId());
      }
      
      templateContainer.setServiceActions(serviceActions);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemTemplate.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemTemplate", 
          "/WEB-INF/grouperUi2/stem/stemNewTemplate.jsp"));
      
    } finally {
        GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public static Map<String, GshTemplateInputConfigAndValue> populateCustomTemplateInputs(HttpServletRequest request, String templateConfigId) {
   
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateConfigId);
    gshTemplateConfig.setCurrentUser(loggedInSubject);

    gshTemplateConfig.populateConfiguration();
    
    GuiGshTemplateConfig guiGshTemplateConfig = new GuiGshTemplateConfig();
    guiGshTemplateConfig.setGshTemplateConfig(gshTemplateConfig);
    
    Map<String, GshTemplateInputConfigAndValue> guiTemplateInputConfigsMap = new LinkedHashMap<String, GshTemplateInputConfigAndValue>();
    Map<String, Object> variableMap = new HashMap<String, Object>();
    
    variableMap.put("grouperUtil", new GrouperUtil());
    
    Collections.sort(gshTemplateConfig.getGshTemplateInputConfigs(), new Comparator<GshTemplateInputConfig>() {

      @Override
      public int compare(GshTemplateInputConfig o1, GshTemplateInputConfig o2) {
        return new Integer(o1.getIndex()).compareTo(new Integer(o2.getIndex()));
      }
    });
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    // a validation error added below stops the caller before any run (see the end of this method)
    int actionCountBeforeInputs = GrouperUtil.length(guiResponseJs.getActions());
    
    for (GshTemplateInputConfig gshTemplateInputConfig: gshTemplateConfig.getGshTemplateInputConfigs()) {
      GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue = new GshTemplateInputConfigAndValue();
      gshTemplateInputConfigAndValue.setGshTemplateInputConfig(gshTemplateInputConfig);
      
      Object value = null;
      if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.FILE) {
        
        GrouperRequestWrapper grouperRequestWrapper = GrouperRequestWrapper.retrieveGrouperRequestWrapper(request);
        if (grouperRequestWrapper == null) {
          continue;
        }
        FileItem gshTemplateFile = grouperRequestWrapper.isMultipart() ? grouperRequestWrapper.getParameterFileItem("config_"+gshTemplateInputConfig.getName()): null;
        if (gshTemplateFile != null) {
          String fileName = StringUtils.defaultString(gshTemplateFile == null ? "" : gshTemplateFile.getName());
          try {
            // check the user may run this template on this owner, then write under a new temp dir using only a
            // sanitized basename of the client file name (it could contain ../)
            File file = GshTemplateFileUpload.writeUploadAfterAuthorization(gshTemplateConfig, loggedInSubject, gshTemplateFile);
            if (gshTemplateInputConfig.getGshTemplateInputType() == GshTemplateInputType.FILE) {
              value = file;
            } else if (gshTemplateInputConfig.getGshTemplateInputType() == GshTemplateInputType.STRING) {
              try {
                value = FileUtils.readFileToString(file, Charset.defaultCharset());
              } finally {
                // the contents are in memory now, so the copy on disk is not needed
                GshTemplateFileUpload.deleteUpload(file);
              }
            } else {
              GshTemplateFileUpload.deleteUpload(file);
              throw new RuntimeException("With form element type 'file', input type must be 'string' or 'file'.");
            }
          } catch (Exception e) {
            throw new RuntimeException("Cant process file upload '" + fileName + "'", e);
          } 
        } else {
          String fileName = request.getParameter("config_"+gshTemplateInputConfig.getName());
          if (StringUtils.isNotBlank(fileName)) {
            String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.fileInput.selectAgain.message");
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#config_"+gshTemplateInputConfig.getName()+"_id", 
                errorMessage));
          }
        }
      } else {
        // for a boolean checkbox (BOOLEANCHECKBOX), the value is carried by a plain hidden field
        // named "config_X" that the checkbox's onchange keeps in sync ("true"/"false"), so it is
        // read here the same way as any other simple input. Null (first render) leaves the default.
        value = request.getParameter("config_"+gshTemplateInputConfig.getName());
      }
      
      if (!gshTemplateInputConfig.getGshTemplateInputType().canConvertToCorrectType(value)) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.conversion.message");
        errorMessage = errorMessage.replace("##valueFromUser##", GrouperUtil.escapeHtml(value.toString(), true));
        errorMessage = errorMessage.replace("##type##", GrouperUtil.escapeHtml(gshTemplateInputConfig.getGshTemplateInputType().name().toLowerCase(), true));
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#config_"+gshTemplateInputConfig.getName()+"_id", 
            errorMessage));
      }
      
      gshTemplateInputConfigAndValue.setValueObject(value);
      
      variableMap.put(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getGshTemplateInputType().convertToType(value));
      guiTemplateInputConfigsMap.put(gshTemplateInputConfig.getName(), gshTemplateInputConfigAndValue);
      
    }
    
    // every input with its value before showEl removes some, to delete the uploads that will not be used
    Map<String, GshTemplateInputConfigAndValue> allInputConfigsMap = new LinkedHashMap<String, GshTemplateInputConfigAndValue>(guiTemplateInputConfigsMap);

    //for showEL, loop again and remove the ones where showEL is evaluated to false
    for (GshTemplateInputConfig gshTemplateInputConfig: gshTemplateConfig.getGshTemplateInputConfigs()) {
      
      String showElScript = gshTemplateInputConfig.getShowEl();
      if (StringUtils.isNotBlank(showElScript)) {
        try {
          Object substituteExpressionLanguageScript = ScriptType.GSH_TEMPLATE_SHOW_EL.runJexl(variableMap, showElScript);
          Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(substituteExpressionLanguageScript);
          if (booleanObjectValue == null || !booleanObjectValue) {
            guiTemplateInputConfigsMap.remove(gshTemplateInputConfig.getName());
          }
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, ", script: '" + showElScript + "', ");
          GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(GshTemplateInputMask.variableMapForLog(gshTemplateConfig, variableMap)));
          GshTemplateFileUpload.deleteUploads(allInputConfigsMap);
          throw re;
        }
        
      }
      
    }
    
    // a validation error stops the caller before the run, and an input hidden by showEl is not passed to the run,
    // so delete those uploads now; the others are deleted when the run (customTemplateExecute) or the form
    // decoration (newTemplateHelper) is done
    GshTemplateFileUpload.deleteUnusedUploads(allInputConfigsMap, guiTemplateInputConfigsMap,
        actionCountBeforeInputs != GrouperUtil.length(guiResponseJs.getActions()));

    guiGshTemplateConfig.setGshTemplateInputConfigAndValues(guiTemplateInputConfigsMap);
    GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
    templateContainer.setGuiGshTemplateConfig(guiGshTemplateConfig);
    
    return guiTemplateInputConfigsMap;
    
  }
  
  public static void customTemplateExecuteHelper(HttpServletRequest request, HttpServletResponse response, boolean useThreads) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    Stem stem = null;
    Group group = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      stem = UiV2Stem.retrieveStemHelper(request, false, false, true).getStem();
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, false).getGroup();
      
      if (stem == null && group == null) {
        return;
      }
      
      GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      String templateType = request.getParameter("templateType");
      
      if (StringUtils.isBlank(templateType)) {
        throw new RuntimeException("templateType cannot be blank.");
      }
      
      templateContainer.setTemplateType(templateType);
      
      GshTemplateExec exec = new GshTemplateExec();
      
      exec.assignConfigId(templateType);
      exec.assignCurrentUser(loggedInSubject);
      // GRP-7598: a UI run with no owner needs allowUiFromNoOwner
      exec.assignRunFromUi(true);
      
      HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
      String remoteAddr = httpServletRequest == null ? null : httpServletRequest.getRemoteAddr();
      exec.assignRemoteAddr(remoteAddr);
      
      if (stem != null) {
        exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
        if (stem.isRootStem()) {
          exec.assignOwnerStemName(":");
        } else {
          exec.assignOwnerStemName(stem.getName());
        }
      } else {
        exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
        exec.assignOwnerGroupName(group.getName());
      }
      
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateType);
      gshTemplateConfig.populateConfiguration();
      
      // SECURITY: check enabled / owner / who can run it BEFORE populating inputs: an input's showEl
      // runs in populateCustomTemplateInputs with the unrestricted expression engine
      if (!customTemplatePreflightOrAddErrors(templateType, loggedInSubject, group, stem)) {
        return;
      }
      int actionCount = GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions());
      Map<String, GshTemplateInputConfigAndValue> gshTemplateInputs = populateCustomTemplateInputs(request, templateType);
      if (gshTemplateInputs == null || actionCount != GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions())) {
        return;
      }
      
      for (String inputName: gshTemplateInputs.keySet()) {
        
        String value = request.getParameter("config_"+inputName);
        
        GshTemplateInput input = new GshTemplateInput();
        input.assignName(inputName);
        GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue = gshTemplateInputs.get(inputName);
        input.assignValue(gshTemplateInputConfigAndValue.getValueObject());
        exec.addGshTemplateInput(input);
        
      }
      String sessionId = request.getSession().getId();
      
      // uniquely identifies this task as opposed to other tasks in other tabs
      String uniqueId = GrouperUuid.getUuid();

      gshTemplateContainer.setUniqueId(uniqueId);

      MultiKey reportMultiKey = new MultiKey(sessionId, uniqueId);
      
      gshExecThreadProgress.put(reportMultiKey, exec);

      GrouperCallable<GshTemplateExecOutput> grouperCallable = new GrouperCallable<GshTemplateExecOutput>("gshTemplateExec") {

        @Override
        public GshTemplateExecOutput callLogic() {
          try {

            exec.getProgressBean().setStartedMillis(System.currentTimeMillis());

            GshTemplateExecOutput gshTemplateExecOutput = exec.execute();

            if (gshTemplateExecOutput.getException() != null) {
              LOG.error("error running template: " + exec.getConfigId(), gshTemplateExecOutput.getException());
            }
            
            return gshTemplateExecOutput;
            
          } catch (RuntimeException re) {
            exec.getProgressBean().setHasException(true);
            // log this since the thread will just end and will never get logged
            LOG.error("error", re);
          } finally {
            // we done
            // the run is done (maybe on a background thread after the request returned): delete its uploads
            GshTemplateFileUpload.deleteUploads(gshTemplateInputs);
            exec.getProgressBean().setComplete(true);
          }
          return null;
        }
      };      
      
      if (useThreads) {
        
        GrouperFuture<GshTemplateExecOutput> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
        
        int waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.gshExec.progressStartsInSeconds", 5);

        GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
        
      } else {
        GshTemplateExecOutput gshTemplateExecOutput = grouperCallable.callLogic();
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", GrouperTextContainer.textOrNull("stemTemplateCustomGshTemplateSubheading")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
  }
  
  /**
   * Execute custom gsh template
   * @param request
   * @param response
   */
  public void customTemplateExecute(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    Stem stem = null;
    Group group = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      int actionCountBeforeOwner = GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions());
      // GRP-7598: for a template with allowUiFromNoOwner, a folder or group it cannot run on is ignored (old urls
      // that pass one keep working); see ignoreStemParams / ignoreGroupParams
      GshTemplateConfig noOwnerConfig = customTemplateNoOwnerConfig(request.getParameter("templateType"));
      boolean ignoreStemParams = ignoreStemParams(noOwnerConfig);
      boolean ignoreGroupParams = ignoreGroupParams(noOwnerConfig);
      stem = ignoreStemParams ? null : UiV2Stem.retrieveStemHelper(request, false, false, false).getStem();
      group = ignoreGroupParams ? null : UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, false).getGroup();
      
      // GRP-7598: no owner is ok for a gsh template with allowUiFromNoOwner (preflight below checks the user)
      // an owner that was asked for, that the template could run on, but not found (or not visible) stops here
      if (stem == null && group == null && (noOwnerConfig == null || requestHasOwnerParam(request, ignoreStemParams, ignoreGroupParams))) {
        templateNotAvailableMessageIfNone(actionCountBeforeOwner);
        return;
      }
      
      GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      String templateType = request.getParameter("templateType");
      
      if (StringUtils.isBlank(templateType)) {
        throw new RuntimeException("templateType cannot be blank.");
      }
      
      templateContainer.setTemplateType(templateType);
      
      GshTemplateExec exec = new GshTemplateExec();
      
      exec.assignConfigId(templateType);
      exec.assignCurrentUser(loggedInSubject);
      // GRP-7598: a UI run with no owner needs allowUiFromNoOwner
      exec.assignRunFromUi(true);
      
      HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
      String remoteAddr = httpServletRequest == null ? null : httpServletRequest.getRemoteAddr();

      exec.assignRemoteAddr(remoteAddr);
      
      if (stem != null) {
        exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
        if (stem.isRootStem()) {
          exec.assignOwnerStemName(":");
        } else {
          exec.assignOwnerStemName(stem.getName());
        }
      } else if (group != null) {
        exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
        exec.assignOwnerGroupName(group.getName());
      }
      
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateType);
      gshTemplateConfig.populateConfiguration();
      
      // SECURITY: check enabled / owner / who can run it BEFORE populating inputs: an input's showEl
      // runs in populateCustomTemplateInputs with the unrestricted expression engine
      if (!customTemplatePreflightOrAddErrors(templateType, loggedInSubject, group, stem)) {
        return;
      }
      int actionCount = GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions());
      Map<String, GshTemplateInputConfigAndValue> gshTemplateInputs = populateCustomTemplateInputs(request, templateType);
      
      if (gshTemplateInputs == null || actionCount != GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions())) {
        return;
      }
      
      for (String inputName: gshTemplateInputs.keySet()) {
        
        String value = request.getParameter("config_"+inputName);
        
        GshTemplateInput input = new GshTemplateInput();
        input.assignName(inputName);
        GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue = gshTemplateInputs.get(inputName);
        input.assignValue(gshTemplateInputConfigAndValue.getValueObject());
        exec.addGshTemplateInput(input);
        
      }
      String sessionId = request.getSession().getId();
      
      // uniquely identifies this task as opposed to other tasks in other tabs
      String uniqueId = GrouperUuid.getUuid();

      gshTemplateContainer.setUniqueId(uniqueId);

      MultiKey reportMultiKey = new MultiKey(sessionId, uniqueId);
      
      gshExecThreadProgress.put(reportMultiKey, exec);

      GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("gshTemplateExec") {

        @Override
        public Void callLogic() {
          try {

            exec.getProgressBean().setStartedMillis(System.currentTimeMillis());

            GshTemplateExecOutput gshTemplateExecOutput = exec.execute();

            if (gshTemplateExecOutput.getException() != null) {
              LOG.error("error running template: " + exec.getConfigId(), gshTemplateExecOutput.getException());
            }
            
          } catch (RuntimeException re) {
            exec.getProgressBean().setHasException(true);
            exec.getProgressBean().setException(re);
            // log this since the thread will just end and will never get logged
            LOG.error("error", re);
          } finally {
            // we done
            // the run is done (maybe on a background thread after the request returned): delete its uploads
            GshTemplateFileUpload.deleteUploads(gshTemplateInputs);
            exec.getProgressBean().setComplete(true);
          }
          return null;
        }
      };      
      
      // see if running in thread
      boolean useThreads = GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.gshExec.useThread", true);

      boolean done = false;
      if (useThreads) {
        
        GrouperFuture<Void> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
        
        int waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.gshExec.progressStartsInSeconds", 10);

        done = GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
        
      } else {
        grouperCallable.callLogic();
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      if (done) {
        GshTemplateExecOutput gshTemplateExecOutput = exec.getGshTemplateExecOutput();
        String redirectToGrouperOperation = (gshTemplateExecOutput == null || gshTemplateExecOutput.getGshTemplateOutput() == null) ? null 
            : gshTemplateExecOutput.getGshTemplateOutput().getRedirectToGrouperOperation();
        
        // if we are done and not moving pages, then redraw the form
        if (StringUtils.equalsIgnoreCase("NONE", redirectToGrouperOperation)) {
          // a no-owner run is always on the simplified screen (there is no folder or group screen to draw it in)
          boolean simplifiedUi = (stem == null && group == null)
              || GrouperConfig.retrieveConfig().propertyValueBoolean("grouperGshTemplate." + exec.getConfigId() + ".simplifiedUi", false);

          newTemplateHelper(request, response, simplifiedUi, true, false);
        }
        // TODO do we need this here?
        //return;
      }
      // running...
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", GrouperTextContainer.textOrNull("stemTemplateCustomGshTemplateSubheading")));
      
      customTemplateExecuteHelper(request, response, sessionId, uniqueId); 
                  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
  }
  
  /**
   * get the status of a report
   * @param request
   * @param response
   */
  public void customTemplateExecuteStatus(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      String sessionId = request.getSession().getId();
      String uniqueId = request.getParameter("uniqueId");
      customTemplateExecuteHelper(request, response, sessionId, uniqueId);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * get the status of a report
   * @param request
   * @param response
   */
  private void customTemplateExecuteHelper(final HttpServletRequest request, final HttpServletResponse response, String sessionId, String uniqueImportId) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    MultiKey reportMultiKey = new MultiKey(sessionId, uniqueImportId);
    
    GshTemplateExec gshTemplateExec = (GshTemplateExec)gshExecThreadProgress.get(reportMultiKey);
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer().setGshTemplateExec(gshTemplateExec);

    if (gshTemplateExec != null) {

      // GRP-7598: a run with no owner (allowUiFromNoOwner) is on the simplified screen
      boolean noOwner = StringUtils.isBlank(gshTemplateExec.getOwnerStemName()) && StringUtils.isBlank(gshTemplateExec.getOwnerGroupName());
      boolean simplifiedUi = noOwner
          || GrouperConfig.retrieveConfig().propertyValueBoolean("grouperGshTemplate." + gshTemplateExec.getConfigId() + ".simplifiedUi", false);
      
      // endless loop?
      if (gshTemplateExec.getProgressBean().isThisLastStatus()) {
        return;
      }
      
      if (gshTemplateExec.getProgressBean().isHasException()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("gshTemplateError")));
        // it has an exception, leave it be
        gshExecThreadProgress.put(reportMultiKey, null);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", ""));
        return;
      }

      Stem stem = null;
      Group group = null;
      if (StringUtils.isNotBlank(gshTemplateExec.getOwnerStemName())) {
        stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), gshTemplateExec.getOwnerStemName(), false);
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setGuiStem(new GuiStem(stem));      
      } else if (StringUtils.isNotBlank(gshTemplateExec.getOwnerGroupName())) {
        group = GroupFinder.findByName(gshTemplateExec.getOwnerGroupName(), false);
        GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setGuiGroup(new GuiGroup(group));
      }
      
      // the owner is gone (e.g. the template deleted it): go to the root folder after.  With no owner at all
      // there is nowhere to go, so the messages show on the simplified screen
      if (stem == null && group == null && !noOwner) {
        stem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
      }


      GshTemplateExecOutput gshTemplateExecOutput = gshTemplateExec.getGshTemplateExecOutput();
      String templateType = gshTemplateExec.getConfigId();
      
      List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();
      
      if (gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
        
        for (GshValidationLine gshValidationLIne: gshTemplateExecOutput.getGshTemplateOutput().getValidationLines()) {
          if (StringUtils.isNotBlank(gshValidationLIne.getInputName())) {
            
            guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#config_"+gshValidationLIne.getInputName()+"_id", 
                gshValidationLIne.getText()));
          } else {
            guiScreenActions.add(GuiScreenAction.newMessageAppend(GuiMessageType.error, gshValidationLIne.getText()));
          }
          
        }
        
      }
      
      for (GshOutputLine outputLine: gshTemplateExecOutput.getGshTemplateOutput().getOutputLines()) {
        
        GuiMessageType guiMessageType = GuiMessageType.valueOf(GrouperUtil.defaultIfBlank(GrouperUtil.defaultString(outputLine.getMessageType()).toLowerCase(), "success"));
        
        guiScreenActions.add(GuiScreenAction.newMessageAppend(guiMessageType, outputLine.getText()));
      }
            
      // consolidate messages
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperGshTemplate." + templateType + ".consolidateOutput", true)) {
        for (GuiMessageType guiMessageType : GuiMessageType.values() ) {
          
          StringBuilder newMessageForType = new StringBuilder();
          
          Iterator<GuiScreenAction> iterator = guiScreenActions.iterator();
          
          // see if there is at least one message of this type
          while (iterator.hasNext()) {
            GuiScreenAction guiScreenAction = iterator.next();
            
            if (!StringUtils.isBlank(guiScreenAction.getValidationMessage())) {
              continue;
            }
            
            if (StringUtils.equalsIgnoreCase(guiMessageType.name(), guiScreenAction.getMessageType())) {
              
              String message = GrouperUtil.trimToEmpty(StringUtils.defaultString(guiScreenAction.getMessage()));
              if (!StringUtils.isBlank(message)) {
                if (newMessageForType.length() > 0) {
                  newMessageForType.append("<br />");
                }
                
                newMessageForType.append(message);
              }
              iterator.remove();
            }
          }
          if (newMessageForType.length() > 0) {
            guiScreenActions.add(GuiScreenAction.newMessageAppend(guiMessageType, newMessageForType.toString()));
          }
        }
      }

      if (!gshTemplateExec.getProgressBean().isComplete()) {
        
        //percent complete
        gshTemplateExec.getProgressBean().setProgressTotalRecords(100);
        
        GrouperGroovyRuntime grouperGroovyRuntime = gshTemplateExec.getGrouperGroovyRuntime();
        if (grouperGroovyRuntime != null) {
          gshTemplateExec.getProgressBean().setProgressCompleteRecords(grouperGroovyRuntime.percentDone());
        }
        
        //show the report screen, running...
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", GrouperTextContainer.textOrNull("stemTemplateCustomGshTemplateSubheading")));

        for (GuiScreenAction guiScreenAction : guiScreenActions) {
          guiResponseJs.addAction(guiScreenAction);
        }

        int progressRefreshSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.gshTemplate.progressRefreshSeconds", 5);
        progressRefreshSeconds = Math.max(progressRefreshSeconds, 1);
        progressRefreshSeconds *= 1000;
        guiResponseJs.addAction(GuiScreenAction.newScript("setTimeout(function() {ajax('../app/UiV2Template.customTemplateExecuteStatus?uniqueId=" + uniqueImportId + "')}, " + progressRefreshSeconds + ")"));
      } else {
        // it is complete, leave it be
        gshExecThreadProgress.put(reportMultiKey, null);
        String runTemplateHeaderText = simplifiedUi ? "" : GrouperTextContainer.textOrNull("gshTemplateScreenDecription");
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", runTemplateHeaderText));

        if (gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {

          for (GuiScreenAction guiScreenAction : guiScreenActions) {
            guiResponseJs.addAction(guiScreenAction);
          }

          return;
        }

        if (!gshTemplateExecOutput.isSuccess()) {
          
          for (GuiScreenAction guiScreenAction : guiScreenActions) {
            guiResponseJs.addAction(guiScreenAction);
          }
          
          boolean displayErrorOutput = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperGshTemplate." + templateType + ".displayErrorOutput", false);
          if (displayErrorOutput) {
            
            if (!StringUtils.isBlank(gshTemplateExecOutput.getGshScriptOutput())) {
              guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, "<pre>"+GrouperUtil.escapeHtml(gshTemplateExecOutput.getGshScriptOutput(), true)+"</pre>"));
            }
            
            String exceptionMessage = null; 
            if (gshTemplateExecOutput.getException() != null) {        
              exceptionMessage = ExceptionUtils.getStackTrace(gshTemplateExecOutput.getException());
            }
            
            if (StringUtils.isNotBlank(exceptionMessage)) {
              // escape like the script output above: exception messages can carry user/remote data
              guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, "<pre>"+GrouperUtil.escapeHtml(exceptionMessage, true)+"</pre>"));
            }
          } else {
            LOG.error("Error in gshTemplate: " + templateType + "\n" + gshTemplateExecOutput.getGshScriptOutput());
            guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("stemTemplateCustomGshTemplateExecuteError")));
          }
          guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", runTemplateHeaderText));
          
        }  else {
          
          String redirectToGrouperOperation = gshTemplateExecOutput.getGshTemplateOutput() == null ? null : gshTemplateExecOutput.getGshTemplateOutput().getRedirectToGrouperOperation();
          
          if (StringUtils.equalsIgnoreCase("NONE", redirectToGrouperOperation)) {
            // redraw screen doesnt work since state is not there
            //newTemplateHelper(request, response, simplifiedUi);
          } else if (!StringUtils.isBlank(gshTemplateExecOutput.getGshTemplateOutput().getRedirectToGrouperOperation())) {
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('" + redirectToGrouperOperation + "')"));
          } else {
            
            if (stem != null) {
              String currentName = stem.getName();
              //lets see if stem exists, maybe template deleted it
              for (int i=0;i<20;i++) {
                Stem currentStem = currentName == null ? StemFinder.findRootStem(GrouperSession.staticGrouperSession()) 
                    : StemFinder.findByName(GrouperSession.staticGrouperSession(), currentName, false);
                if (currentStem !=  null) {
                  guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + currentStem.getId() + "')"));
                  break;
                } 
                currentName = GrouperUtil.parentStemNameFromName(currentName);
              }
            } else if (group != null) {
              String currentName = group.getName();
              //lets see if group exists, maybe template deleted it
              Group currentGroup = GroupFinder.findByName(currentName, false);
              if (currentGroup !=  null) {
                guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + currentGroup.getId() + "')"));
              } else {
                Stem currentStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
                guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + currentStem.getId() + "')"));
              }
              
            }
            
          }
          for (GuiScreenAction guiScreenAction : guiScreenActions) {
            guiResponseJs.addAction(guiScreenAction);
          }
          if (GrouperUtil.length(guiScreenActions) == 0) {
            //lets show a success message on the new screen
            guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("stemTemplateCustomGshTemplateExecuteSuccess")));
          }        
          
          // GRP-7438: the template returned a file, start the download and show a link in case the browser blocks it
          String downloadGrouperFileId = gshTemplateExecOutput.getGshTemplateOutput().getDownloadGrouperFileId();
          if (!StringUtils.isBlank(downloadGrouperFileId)) {
            // only select the file name so the contents are not loaded
            String downloadFileName = GrouperFileDao.findFileNameById(downloadGrouperFileId);
            if (downloadFileName == null) {
              LOG.error("gsh template: " + templateType + " returned download file id that does not exist: " + downloadGrouperFileId);
            } else {
              String downloadUrl = UiV2DownloadFile.downloadUrl(downloadGrouperFileId);
              String downloadMessage = TextContainer.retrieveFromRequest().getText().get("stemTemplateCustomGshTemplateDownloadFile");
              downloadMessage = StringUtils.replace(downloadMessage, "$$downloadUrl$$", GrouperUtil.escapeHtml(downloadUrl, true));
              downloadMessage = StringUtils.replace(downloadMessage, "$$fileName$$", GrouperUtil.escapeHtml(downloadFileName, true));
              guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.info, downloadMessage));
              guiResponseJs.addAction(GuiScreenAction.newScript("location.href = '" + GrouperUiUtils.escapeJavascript(downloadUrl, true) + "'"));
            }
          }
        }
        

        
        
      }
    }


  }

  /**
   * Perform actions (Create stem, Create group, etc)
   * @param request
   * @param response
   */
  public void newTemplateSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      stem = UiV2Stem.retrieveStemHelper(request, false, true, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);
      
      if (templateLogic == null) {
        return;
      }

      final GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();

      templateContainer.setTemplateLogic(templateLogic);

      templateLogic.initScreen();

      templateLogic.setStemId(stem.getUuid());
      
      boolean isSuccess = populateServiceInfo(request, templateLogic);
      if (!isSuccess) {
        return;
      }
      
      setTemplateOptions();

      String[] selectedServiceActionIds = request.getParameterValues("serviceActionId[]");
      
      List<ServiceAction> selectedServiceActions = new ArrayList<ServiceAction>();
      
      List<ServiceAction> allServiceActions = templateLogic.getServiceActions();
      
      for (ServiceAction serviceAction: allServiceActions) {
        
        for (String selectedServiceActionId: selectedServiceActionIds) {          
          if (serviceAction.getId().equals(selectedServiceActionId)) {
            selectedServiceActions.add(serviceAction);
          }
        }
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      boolean allGood = templateLogic.validate(selectedServiceActions);
      
      if (!allGood) {
        guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("stemTemplateHierarchySelectError")));
        return;
      }
      
      for (ServiceAction serviceAction: selectedServiceActions) {
        serviceAction.getServiceActionType().createTemplateItem(serviceAction);
      }

      String errorKey = templateLogic.postCreateSelectedActions(selectedServiceActions);

      if (!StringUtils.isBlank(errorKey)) {
        guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get(errorKey)));
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemTemplateCreateSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
  }
  
  /**
   * Show/Hide children service actions when user checks/unchecks checkboxes
   * @param request
   * @param response
   */
  public void reloadServiceActions(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);
      
      if (templateLogic == null) {
        return;
      }
      
      setTemplateOptions();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
      
      templateLogic.setStemId(stem.getUuid());
      
      templateContainer.setTemplateLogic(templateLogic);
      
      String templateType = request.getParameter("templateType");
      templateContainer.setTemplateType(templateType);
      
      boolean isSuccess = populateServiceInfo(request, templateLogic);
      if (!isSuccess) {
        return;
      }
      
      String selectedServiceActionIdChecked = request.getParameter("checked");
      Boolean isChecked = GrouperUtil.booleanObjectValue(selectedServiceActionIdChecked);
      
      String selectedServiceActionId = request.getParameter("serviceActionId");
      
      List<ServiceAction> allServiceActions = templateLogic.getServiceActions();
      
      ServiceAction selectedServiceAction = null;
      
      for (ServiceAction serviceAction: allServiceActions) {
        if (serviceAction.getId().equals(selectedServiceActionId)) {
          selectedServiceAction = serviceAction;
          break;
        }
      }
      
      List<ServiceAction> childrenServiceActions = getChildrenUpToLeaf(selectedServiceAction);
      
      if (isChecked) {
        for(ServiceAction serviceAction: childrenServiceActions) {
          guiResponseJs.addAction(GuiScreenAction.newScript("$('."+serviceAction.getId()+"').show('slow')"));
          guiResponseJs.addAction(GuiScreenAction.newScript("$('input[type=checkbox][value="+serviceAction.getId()+"]').prop('checked', true)"));
        }
      } else {
        for(ServiceAction serviceAction: childrenServiceActions) {
          guiResponseJs.addAction(GuiScreenAction.newScript("$('."+serviceAction.getId()+"').hide('slow')"));
          guiResponseJs.addAction(GuiScreenAction.newScript("$('input[type=checkbox][value="+serviceAction.getId()+"]').prop('checked', false)"));
        }
      }
      
    } finally {
        GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * Populate template type, key and friendly name
   * @param request
   * @param templateLogic
   * @return isSuccess
   */
  private boolean populateServiceInfo(HttpServletRequest request, GrouperTemplateLogicBase templateLogic) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
    
    String createSubfolder = request.getParameter("createSubfolder");
    
    if (StringUtils.isNotBlank(createSubfolder)) {
      templateContainer.setCreateNoSubfolder(true);
    }
    
    if(StringUtils.isBlank(createSubfolder)) {
      String templateKey = request.getParameter("templateKey");
      if (StringUtils.isBlank(templateKey)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#serviceKeyId",
            TextContainer.retrieveFromRequest().getText().get("stemServiceKeyRequiredError")));
        return false;
      }
      
      String regex = "^[a-zA-Z0-9_-]*$";
      if (!templateKey.matches(regex)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#serviceKeyId",
            TextContainer.retrieveFromRequest().getText().get("stemServiceKeyInvaldError")));
        return false;
      }
      templateContainer.setTemplateKey(templateKey);
      templateContainer.setTemplateDescription(request.getParameter("serviceDescription"));
      templateContainer.setTemplateFriendlyName(request.getParameter("serviceFriendlyName"));
    }
    
    return true;
    
  }
  
  /**
   * Get implementation class from the template key
   * @param request
   * @return
   */
  private GrouperTemplateLogicBase getTemplateLogic(HttpServletRequest request) {
    
    String templateType = request.getParameter("templateType");
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    if (StringUtils.isBlank(templateType)) {
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#templateTypeId",
          TextContainer.retrieveFromRequest().getText().get("stemTemplateTypeRequiredError")));
      return null;
    }

    // a built-in template that is disabled cannot be run, even if its key is submitted directly
    if (!builtInTemplateEnabledOrAddError(templateType)) {
      return null;
    }

    try {

      GrouperTemplateLogicBase templateLogic = getTemplateLogic(templateType,
          GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer());

      return templateLogic;
    } catch(Exception e) {
      return null;
    }
    
  }

  /**
   * see if a built-in template (configured with grouper.template.&lt;key&gt;.logicClass) is turned off.
   * a built-in template is disabled if grouper.template.&lt;key&gt;.enabled is false, or if its
   * logicClass is configured blank (older way to hide it, GRP-7376).  Custom GSH templates do not
   * have a logicClass, so they are never a disabled built-in template
   * @param templateType template key, e.g. service, policyGroup, tierStructure
   * @return true if this is a built-in template that is disabled
   */
  public static boolean builtInTemplateDisabled(String templateType) {

    String logicClassKey = "grouper.template." + templateType + ".logicClass";

    // not a built-in template (e.g. a custom GSH template)
    if (!GrouperUiConfig.retrieveConfig().containsKey(logicClassKey)) {
      return false;
    }

    if (StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString(logicClassKey))) {
      return true;
    }

    return !GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouper.template." + templateType + ".enabled", true);
  }

  /**
   * if the template is a disabled built-in template, add an error to the screen
   * @param templateType
   * @return true if ok to proceed, false if disabled (and error was added)
   */
  private static boolean builtInTemplateEnabledOrAddError(String templateType) {

    if (!builtInTemplateDisabled(templateType)) {
      return true;
    }

    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
        "#templateTypeId",
        TextContainer.retrieveFromRequest().getText().get("stemTemplateTypeNotEnabledError")));
    return false;
  }

  /**
   * @param templateType
   * @param stemTemplateContainer
   * @return the instance
   */
  public static GrouperTemplateLogicBase getTemplateLogic(String templateType, GroupStemTemplateContainer stemTemplateContainer) {

    // never instantiate a disabled built-in template
    if (builtInTemplateDisabled(templateType)) {
      throw new RuntimeException("Template is not enabled: '" + templateType + "'");
    }

    String implementationClass = GrouperUiConfig.retrieveConfig().propertyValueStringRequired("grouper.template."+templateType+".logicClass");
    
    Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(implementationClass);
    
    GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
    
    templateLogic.setStemTemplateContainer(stemTemplateContainer);
    return templateLogic;
  }
  
  /**
   * test gsh template
   * @param request
   * @param response
   */
  public void test(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      String templateType = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(templateType)) {
        throw new RuntimeException("gshTemplateConfigId cannot be blank.");
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("User is not an admin! " + SubjectHelper.getPretty(loggedInSubject));
      }
      
      GshTemplateTestExec exec = new GshTemplateTestExec();
      
      exec.assignConfigId(templateType);
            
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateType);
      gshTemplateConfig.populateConfiguration();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      if (gshTemplateConfig.getGshTemplateType() == GshTemplateType.provisioner) {
        guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText()
                .get("gshTemplate.error.cannotTestProvisioner.message")));
        return;
      }
      
      String sessionId = request.getSession().getId();
      
      // uniquely identifies this task as opposed to other tasks in other tabs
      String uniqueId = GrouperUuid.getUuid();

      gshTemplateContainer.setUniqueId(uniqueId);

      MultiKey reportMultiKey = new MultiKey(sessionId, uniqueId);
      
      gshExecThreadProgress.put(reportMultiKey, exec);

      GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("gshTemplateExec") {

        @Override
        public Void callLogic() {
          try {

            exec.getProgressBean().setStartedMillis(System.currentTimeMillis());

            exec.executeTests();

          } catch (RuntimeException re) {
            exec.getProgressBean().setHasException(true);
            exec.getProgressBean().setException(re);
            // log this since the thread will just end and will never get logged
            LOG.error("error", re);
          } finally {
            // we done
            exec.getProgressBean().setComplete(true);
          }
          return null;
        }
      };      
      
      // see if running in thread
      boolean useThreads = GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.gshExec.useThread", true);

      if (useThreads) {
        
        GrouperFuture<Void> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
        
        int waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.gshExec.progressStartsInSeconds", 5);

        GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
        
      } else {
        grouperCallable.callLogic();
      }
  
      // running...
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", GrouperTextContainer.textOrNull("stemTemplateCustomGshTemplateTestSubheading")));
      
      customTemplateTestExecuteHelper(sessionId, uniqueId); 
                  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * Find all template types and set them in container
   */
  private void setTemplateOptions() {
    
    GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
    
    // add the standard ones if can create
    // final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    
    
//    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
//    Group group = null;
//    if (guiGroup != null && guiGroup.getGroup() != null) {
//      group = guiGroup.getGroup();
//    }
    
//    if (stem.canHavePrivilege(loggedInSubject, "creators", false)) {
//      
//    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    if (guiStem != null && guiStem.getStem() != null) {
      // only for stems; predefined templates are valid
      
      Map<String, String> serviceClassPatterns = GrouperUiConfig.retrieveConfig().propertiesMap(grouperTemplateServiceClassPattern);
      
      for (Entry<String, String> entry: serviceClassPatterns.entrySet()) {
        
        String property = entry.getKey();
        String implementationClass = entry.getValue();
        
        Matcher matcher = grouperTemplateServiceClassPattern.matcher(property);
        
        if (matcher.matches()) {          
        
          String templateKey = matcher.group(1);

          // org can hide a built-in template with grouper.template.<key>.enabled = false (GRP-7376)
          if (StringUtils.isBlank(implementationClass) || builtInTemplateDisabled(templateKey)) {
            continue;
          }

          Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(implementationClass);
          
          GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
          
          String label = TextContainer.retrieveFromRequest().getText().get(templateLogic.getSelectLabelKey());
          templateContainer.getTemplateOptions().put(templateKey, label);
          
        }
      }
      
    }
    
    // add custom gsh templates
    Map<String, String> customGshTemplates = templateContainer.getCustomGshTemplates();
    templateContainer.getTemplateOptions().putAll(customGshTemplates);
  }
  
  
  /**
   * Given a service action, find all the children up to leaf
   * @param serviceAction
   * @return
   */
  private List<ServiceAction> getChildrenUpToLeaf(ServiceAction serviceAction) {
    
   List<ServiceAction> allChildren = new ArrayList<ServiceAction>();
   
   List<ServiceAction> queue = new LinkedList<ServiceAction>();
   queue.add(serviceAction);
   
   while (queue.size() > 0) {
     List<ServiceAction> children = queue.remove(0).getChidrenServiceActions();
     queue.addAll(children);
     allChildren.addAll(children);
   }
   
   return allChildren;
    
  }

  /**
   * GRP-7598: a template screen could not go on with no folder or group (the template needs one, or the one
   * asked for was not found).  Show a short message instead of a blank screen, unless the folder or group lookup
   * already showed one.  The message is the same for every reason, so it does not tell the user whether the
   * template, folder or group exists
   * @param actionCountBeforeOwner number of screen actions before the folder and group were looked up
   */
  private static void templateNotAvailableMessageIfNone(int actionCountBeforeOwner) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    if (GrouperUtil.length(guiResponseJs.getActions()) == actionCountBeforeOwner) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          TextContainer.retrieveFromRequest().getText().get("gshTemplateNotAvailable")));
    }
  }

  /**
   * GRP-7598: true if the request names an owner folder or group (any of the ids, names or indexes the owner
   * lookups read), not counting ones that are being ignored.  When one was sent but did not resolve (missing,
   * or the user cannot VIEW it), the screen stops instead of running a no-owner template with the owner
   * silently dropped
   * @param request
   * @param ignoreStemParams true to not count stemId / stemName / stemIndex
   * @param ignoreGroupParams true to not count groupId / groupName / groupIndex
   * @return true if an owner was asked for
   */
  private static boolean requestHasOwnerParam(HttpServletRequest request, boolean ignoreStemParams, boolean ignoreGroupParams) {
    if (!ignoreStemParams) {
      for (String paramName : new String[] {"stemId", "stemName", "stemIndex"}) {
        if (!StringUtils.isBlank(request.getParameter(paramName))) {
          return true;
        }
      }
    }
    if (!ignoreGroupParams) {
      for (String paramName : new String[] {"groupId", "groupName", "groupIndex"}) {
        if (!StringUtils.isBlank(request.getParameter(paramName))) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * GRP-7598: for a template that the UI can run with no owner but that does not show on folders, a folder in
   * the request could never be used (the run would be refused on it), so it is ignored and the template runs with
   * no owner.  That keeps old urls that pass a stemId working.  It does not widen anything: the user could leave
   * the folder off, and who may run the template is still checked
   * @param noOwnerConfig from customTemplateNoOwnerConfig, null if the template cannot run with no owner
   * @return true if stemId / stemName / stemIndex should be ignored
   */
  private static boolean ignoreStemParams(GshTemplateConfig noOwnerConfig) {
    return noOwnerConfig != null && !noOwnerConfig.isShowOnFolders();
  }

  /**
   * GRP-7598: same as ignoreStemParams, for groups: a template that the UI can run with no owner but that does
   * not show on groups ignores groupId / groupName / groupIndex
   * @param noOwnerConfig from customTemplateNoOwnerConfig, null if the template cannot run with no owner
   * @return true if groupId / groupName / groupIndex should be ignored
   */
  private static boolean ignoreGroupParams(GshTemplateConfig noOwnerConfig) {
    return noOwnerConfig != null && !noOwnerConfig.isShowOnGroups();
  }

  /**
   * GRP-7598: the config of a custom gsh template that may be run with no owner folder or group
   * from the UI (allowUiFromNoOwner), so a template screen with no stemId / groupId goes on instead of stopping.  This only
   * decides whether the screen proceeds: whether the user may run the template is still checked by
   * customTemplatePreflightOrAddErrors (and again by the validation service when it runs).  Built-in templates
   * always need an owner
   * @param templateType template config id from the request
   * @return the populated config if the template can be run with no owner from the UI, else null
   */
  private static GshTemplateConfig customTemplateNoOwnerConfig(String templateType) {
    if (StringUtils.isBlank(templateType)) {
      return null;
    }
    try {
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateType);
      gshTemplateConfig.populateConfiguration();
      if (gshTemplateConfig.isAllowUiFromNoOwner() && gshTemplateConfig.getGshTemplateType() == GshTemplateType.gsh) {
        return gshTemplateConfig;
      }
      return null;
    } catch (RuntimeException re) {
      // not a gsh template config (e.g. a built-in template), or bad config: needs an owner
      LOG.debug("Template is not a no-owner gsh template: " + templateType, re);
      return null;
    }
  }

  /**
   * Before a custom gsh template form is rendered (which loads the V2 template class and runs its
   * decorateTemplateForUiDisplay), check that the logged in user may use this template on this group or folder:
   * the template is enabled, is allowed on the owner, and the user satisfies its securityRunType.  The template
   * dropdown (GroupStemTemplateContainer) only hides templates, so without this a direct request with
   * templateType=&lt;config id&gt; would run the code of a disabled or restricted template.
   * On failure the validation messages are shown as errors.
   * @param templateType gsh template config id from the request
   * @param loggedInSubject
   * @param group owner group or null
   * @param stem owner folder or null
   * @return true if ok to proceed, false if not allowed (and errors were added to the screen)
   */
  private static boolean customTemplatePreflightOrAddErrors(final String templateType, final Subject loggedInSubject, 
      final Group group, final Stem stem) {
    
    GshTemplateOutput gshTemplateOutput = new GshTemplateOutput();
    boolean allowed = false;
    try {
      
      // build the exec the same way customTemplateExecute does, so the checks are the ones execution runs
      GshTemplateExec gshTemplateExec = new GshTemplateExec();
      gshTemplateExec.assignConfigId(templateType);
      gshTemplateExec.assignCurrentUser(loggedInSubject);
      // GRP-7598: a UI run with no owner needs allowUiFromNoOwner
      gshTemplateExec.assignRunFromUi(true);
      HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
      gshTemplateExec.assignRemoteAddr(httpServletRequest == null ? null : httpServletRequest.getRemoteAddr());
      if (stem != null) {
        gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
        gshTemplateExec.assignOwnerStemName(stem.isRootStem() ? ":" : stem.getName());
      } else if (group != null) {
        gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
        gshTemplateExec.assignOwnerGroupName(group.getName());
      }
      
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateType);
      gshTemplateConfig.populateConfiguration();
      gshTemplateConfig.setCurrentUser(loggedInSubject);
      
      allowed = new GshTemplateValidationService().validatePreflight(gshTemplateConfig, gshTemplateExec, gshTemplateOutput);
      
    } catch (RuntimeException re) {
      // e.g. unknown template id or bad config: fail closed
      LOG.warn("Error checking gsh template: " + templateType, re);
      allowed = false;
    }
    
    if (allowed) {
      return true;
    }
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    boolean addedMessage = false;
    for (GshValidationLine gshValidationLine : GrouperUtil.nonNull(gshTemplateOutput.getValidationLines())) {
      // validation line text is externalized text with escaped values, same as shown after a run
      if (StringUtils.isNotBlank(gshValidationLine.getText())) {
        guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, gshValidationLine.getText()));
        addedMessage = true;
      }
    }
    if (!addedMessage) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          "Template is not available: " + GrouperUtil.escapeHtml(templateType, true)));
    }
    return false;
  }
  
  /**
   * Show fields for new template
   * @param request
   * @param response
   * @param simplifiedRequest true if from simple request, false if not
   */
  private void newTemplateHelper(final HttpServletRequest request, final HttpServletResponse response, 
      boolean simplifiedRequest, boolean submit, boolean newTemplate) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
  
    Stem stem = null;
    Group group = null;
    boolean startedSession = grouperSession == null;
    try {
      grouperSession = startedSession ? GrouperSession.start(loggedInSubject) : grouperSession;
      
      // GRP-7598: the simplified screen's message area is in its top container, which used to be drawn only
      // with the form, so a refusal (template not available, not allowed to run it) left a blank page.  Draw it
      // first so any message below has somewhere to show; the form path draws it again, which is harmless
      if (simplifiedRequest) {
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#theTopContainer", 
            "/WEB-INF/grouperUi2/gshTemplate/indexGshSimplifiedUiTopContainer.jsp"));
      }

      int actionCountBeforeOwner = GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions());
      // GRP-7598: for a template with allowUiFromNoOwner, a folder or group it cannot run on is ignored (old urls
      // that pass one keep working); see ignoreStemParams / ignoreGroupParams
      GshTemplateConfig noOwnerConfig = customTemplateNoOwnerConfig(request.getParameter("templateType"));
      boolean ignoreStemParams = ignoreStemParams(noOwnerConfig);
      boolean ignoreGroupParams = ignoreGroupParams(noOwnerConfig);
      stem = ignoreStemParams ? null : UiV2Stem.retrieveStemHelper(request, false, false, false).getStem();
      group = ignoreGroupParams ? null : UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, false).getGroup();
      
      // GRP-7598: no owner is ok for a gsh template with allowUiFromNoOwner (preflight below checks the user),
      // and it is always drawn on the simplified screen since there is no folder or group screen to draw it in
      boolean noOwner = stem == null && group == null;
      // an owner that was asked for, that the template could run on, but not found (or not visible) stops here
      if (noOwner && (noOwnerConfig == null || requestHasOwnerParam(request, ignoreStemParams, ignoreGroupParams))) {
        templateNotAvailableMessageIfNone(actionCountBeforeOwner);
        return;
      }
      
      setTemplateOptions();
      
      GroupStemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupStemTemplateContainer();
      templateContainer.setSimplifiedRequest(simplifiedRequest);
      if (stem != null) {
        templateContainer.setShowOnFolder(true);
      }
      if (group != null) {
        templateContainer.setShowOnGroup(true);
      }
      
      String templateType = request.getParameter("templateType");
      
      if (StringUtils.isNotBlank(templateType)) {
        
        templateContainer.setTemplateType(templateType);

        // a disabled built-in template returns null below, which would otherwise be treated as a custom gsh template
        if (!builtInTemplateEnabledOrAddError(templateType)) {
          return;
        }

        GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);

        if (templateLogic == null) {
          // must be gsh custom template
          // check enabled / owner / who can run it BEFORE populating inputs and loading or decorating the template
          if (!customTemplatePreflightOrAddErrors(templateType, loggedInSubject, group, stem)) {
            return;
          }
          int actionCount = GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions());
          Map<String, GshTemplateInputConfigAndValue> customTemplateInputs = populateCustomTemplateInputs(request, templateType);
          if (customTemplateInputs == null || actionCount !=  GrouperUtil.length(GuiResponseJs.retrieveGuiResponseJs().getActions())) {
            return;
          }
          if (StringUtils.equals("V2", templateContainer.getGuiGshTemplateConfig().getGshTemplateConfig().getTemplateVersion())) {
            GshTemplateExec gshTemplateExec = new GshTemplateExec();
            gshTemplateExec.assignConfigId(templateType);
            GshTemplateV2 executeForTemplateV2instance = gshTemplateExec.executeForTemplateV2instance();
            
            GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput = new GshTemplateDecorateForUiInput();
            
            gshTemplateDecorateForUiInput.setGshTemplateInputConfigAndValues(customTemplateInputs);
            gshTemplateDecorateForUiInput.setCurrentSubject(loggedInSubject);
            gshTemplateDecorateForUiInput.setTemplateConfigId(templateType);
            gshTemplateDecorateForUiInput.setEventConfigId(request.getParameter("eventConfigId"));
            gshTemplateDecorateForUiInput.setSubmit(submit);
            gshTemplateDecorateForUiInput.setNewForm(newTemplate);
            gshTemplateDecorateForUiInput.setOwnerGroupName(group == null ? null : group.getName());
            gshTemplateDecorateForUiInput.setOwnerStemName(stem == null ? null : stem.getName());
            try {
              executeForTemplateV2instance.decorateTemplateForUiDisplay(gshTemplateDecorateForUiInput);
            } catch (Throwable t) {

              t = GrouperGroovysh.handleGshException(executeForTemplateV2instance.isLightWeight(), executeForTemplateV2instance.getScriptPrependHeaders(), executeForTemplateV2instance.getSource(), t);
              throw GrouperUtil.exceptionConvertToRuntime(t, null);
            }

            
          }
          if (customTemplateInputs == null) {
            return;
          }

        } else {
          templateContainer.setTemplateLogic(templateLogic);
          templateLogic.initScreen();
        }
        
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (simplifiedRequest) {
        // the top container was drawn at the start (GRP-7598), drawing it again would wipe any message
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/gshTemplate/simplifiedNewTemplate.jsp"));
      } else {
      
        if (!StringUtils.isBlank(templateType)) {
          if (noOwner || GrouperConfig.retrieveConfig().propertyValueBoolean(
              "grouperGshTemplate." + templateType + ".simplifiedUi", false)) {
            String redirectUrl = "UiV2Main.indexGshSimplifiedUi?operation=UiV2Template.newTemplateSimplifiedUi&templateType=" 
                + GrouperUtil.escapeUrlEncode(templateType);
  
            if (stem != null) {
              redirectUrl += "&stemId=" + GrouperUtil.escapeUrlEncode(stem.getId());
            } else if (group != null) {
              redirectUrl += "&groupId=" + GrouperUtil.escapeUrlEncode(group.getId());
            }
            // GrouperUiFilter.retrieveServletContext() + "/grouperUi/app/" + 
            guiResponseJs.addAction(GuiScreenAction.newScript(
                "window.location = '" + redirectUrl + "'"));
            return;
          }
        }

        if (stem != null) {
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/stem/stemTemplate.jsp"));
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemTemplate", 
              "/WEB-INF/grouperUi2/stem/stemNewTemplate.jsp"));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/group/groupTemplate.jsp"));
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupTemplate", 
              "/WEB-INF/grouperUi2/group/groupNewTemplate.jsp"));
        }
      }      
    } catch (Exception e) {
      LOG.warn("Error in newTemplateHelper", e);
      return;
    } finally {
      // nothing runs with the inputs this form was built from, so delete their uploads once it is drawn
      GshTemplateFileUpload.deleteUploads(GrouperRequestContainer.retrieveFromRequestOrCreate()
          .getGroupStemTemplateContainer().getGuiGshTemplateConfig());
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

  }
  
  /**
   * Show fields for new template
   * @param request
   * @param response
   */
  public void newTemplateSimplifiedUi(final HttpServletRequest request, final HttpServletResponse response) {
    newTemplateHelper(request, response, true, false, true);
    
  }

  /**
   * get the status of a report
   * @param request
   * @param response
   */
  private void customTemplateTestExecuteHelper(String sessionId, String uniqueImportId) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    MultiKey reportMultiKey = new MultiKey(sessionId, uniqueImportId);
    
    GshTemplateTestExec gshTemplateTestExec = (GshTemplateTestExec)gshExecThreadProgress.get(reportMultiKey);
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer().setGshTemplateTestExec(gshTemplateTestExec);
  
    if (gshTemplateTestExec != null) {
      
      // endless loop?
      if (gshTemplateTestExec.getProgressBean().isThisLastStatus()) {
        return;
      }
      
      if (gshTemplateTestExec.getProgressBean().isHasException()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("gshTemplateError") + ", " 
              + (gshTemplateTestExec.getProgressBean() != null ?
                  GrouperUtil.getFullStackTraceHtml(gshTemplateTestExec.getProgressBean().getException()) : "")));
        // it has an exception, leave it be
        gshExecThreadProgress.put(reportMultiKey, null);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", ""));
        return;
      }
  
      GshTemplateExecTestOutput gshTemplateExecTestOutput = gshTemplateTestExec.getGshTemplateExecTestOutput();
      String templateType = gshTemplateTestExec.getConfigId();
      
      List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();
      
      if (gshTemplateExecTestOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
        
        for (GshValidationLine gshValidationLIne: gshTemplateExecTestOutput.getGshTemplateOutput().getValidationLines()) {
          if (StringUtils.isNotBlank(gshValidationLIne.getInputName())) {
            
            guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#config_"+gshValidationLIne.getInputName()+"_id", 
                gshValidationLIne.getText()));
          } else {
            guiScreenActions.add(GuiScreenAction.newMessageAppend(GuiMessageType.error, gshValidationLIne.getText()));
          }
        }
      }
      
      for (GshOutputLine outputLine: gshTemplateExecTestOutput.getGshTemplateOutput().getOutputLines()) {
        
        GuiMessageType guiMessageType = GuiMessageType.valueOf(GrouperUtil.defaultIfBlank(GrouperUtil.defaultString(outputLine.getMessageType()).toLowerCase(), "success"));
        
        guiScreenActions.add(GuiScreenAction.newMessageAppend(guiMessageType, outputLine.getText()));
      }
            
      // consolidate messages
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperGshTemplate." + templateType + ".consolidateOutput", true)) {
        for (GuiMessageType guiMessageType : GuiMessageType.values() ) {
          
          StringBuilder newMessageForType = new StringBuilder();
          
          Iterator<GuiScreenAction> iterator = guiScreenActions.iterator();
          
          // see if there is at least one message of this type
          while (iterator.hasNext()) {
            GuiScreenAction guiScreenAction = iterator.next();
            
            if (!StringUtils.isBlank(guiScreenAction.getValidationMessage())) {
              continue;
            }
            
            if (StringUtils.equalsIgnoreCase(guiMessageType.name(), guiScreenAction.getMessageType())) {
              
              String message = GrouperUtil.trimToEmpty(StringUtils.defaultString(guiScreenAction.getMessage()));
              if (!StringUtils.isBlank(message)) {
                if (newMessageForType.length() > 0) {
                  newMessageForType.append("<br />");
                }
                
                newMessageForType.append(message);
              }
              iterator.remove();
            }
          }
          if (newMessageForType.length() > 0) {
            guiScreenActions.add(GuiScreenAction.newMessageAppend(guiMessageType, newMessageForType.toString()));
          }
        }
      }
  
      if (!gshTemplateTestExec.getProgressBean().isComplete()) {
                
        //show the report screen, running...
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", GrouperTextContainer.textOrNull("stemTemplateCustomGshTemplateTestSubheading")));
  
        for (GuiScreenAction guiScreenAction : guiScreenActions) {
          guiResponseJs.addAction(guiScreenAction);
        }
  
        int progressRefreshSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.gshTemplate.progressRefreshSeconds", 5);
        progressRefreshSeconds = Math.max(progressRefreshSeconds, 1);
        progressRefreshSeconds *= 1000;
        guiResponseJs.addAction(GuiScreenAction.newScript("setTimeout(function() {ajax('../app/UiV2Template.customTemplateTestExecuteStatus?uniqueId=" + uniqueImportId + "')}, " + progressRefreshSeconds + ")"));
      } else {
        // it is complete, leave it be
        gshExecThreadProgress.put(reportMultiKey, null);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#templateHeader", ""));
  
        if (gshTemplateExecTestOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
  
          for (GuiScreenAction guiScreenAction : guiScreenActions) {
            guiResponseJs.addAction(guiScreenAction);
          }
  
          return;
        }
  
        
        for (GuiScreenAction guiScreenAction : guiScreenActions) {
          guiResponseJs.addAction(guiScreenAction);
        }
        if (GrouperUtil.length(guiScreenActions) == 0) {
          throw new RuntimeException("Why no test output???");
        }        
        
      }
    }
  
  
  }

  /**
   * get the status of a report
   * @param request
   * @param response
   */
  public void customTemplateTestExecuteStatus(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      String sessionId = request.getSession().getId();
      String uniqueId = request.getParameter("uniqueId");
      customTemplateTestExecuteHelper(sessionId, uniqueId);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
    
}
