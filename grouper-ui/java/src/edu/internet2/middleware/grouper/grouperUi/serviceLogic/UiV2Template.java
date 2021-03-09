package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.gsh.template.GshOutputLine;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshValidationLine;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGshTemplateConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGshTemplateInputConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ServiceAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.StemTemplateContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class UiV2Template {
	
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Template.class);
  
  
  private static Pattern grouperTemplateServiceClassPattern = Pattern.compile(
      "^grouper\\.template\\.(\\w+)\\.logicClass$");
  
  
  /**
   * Show fields for new template
   * @param request
   * @param response
   */
  public void newTemplate(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
	  Stem stem = null;
	  
	  try {
	    grouperSession = GrouperSession.start(loggedInSubject);
	    stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
	    
	    if (stem == null) {
	      return;
	    }
	    
	    setTemplateOptions();
	    
	    StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
	    
	    String templateType = request.getParameter("templateType");
	    
	    if (StringUtils.isNotBlank(templateType)) {
	      
	      templateContainer.setTemplateType(templateType);
	      
	      GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);
	      
	      if (templateLogic == null) {
	        // must be gsh custom template
	        Map<String, GuiGshTemplateInputConfig> customTemplateInputs = populateCustomTemplateInputs(request, templateType);
	        if (customTemplateInputs == null) {
	          return;
	        }
	        
	      } else {
	        templateContainer.setTemplateLogic(templateLogic);
	        templateLogic.initScreen();
	      }
	      
	    }
	    
	    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemTemplate.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemTemplate", 
          "/WEB-INF/grouperUi2/stem/stemNewTemplate.jsp"));
	    
	  } finally {
        GrouperSession.stopQuietly(grouperSession);
	  }
	  
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
      
      final StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
      
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
  
  private Map<String, GuiGshTemplateInputConfig> populateCustomTemplateInputs(HttpServletRequest request, String templateConfigId) {
   
    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateConfigId);
    gshTemplateConfig.populateConfiguration();
    
    GuiGshTemplateConfig guiGshTemplateConfig = new GuiGshTemplateConfig();
    guiGshTemplateConfig.setGshTemplateConfig(gshTemplateConfig);
    
    Map<String, GuiGshTemplateInputConfig> guiTemplateInputConfigsMap = new LinkedHashMap<String, GuiGshTemplateInputConfig>();
    Map<String, Object> variableMap = new HashMap<String, Object>();
    
    variableMap.put("grouperUtil", new GrouperUtil());
    
    Collections.sort(gshTemplateConfig.getGshTemplateInputConfigs(), new Comparator<GshTemplateInputConfig>() {

      @Override
      public int compare(GshTemplateInputConfig o1, GshTemplateInputConfig o2) {
        return new Integer(o1.getIndex()).compareTo(new Integer(o2.getIndex()));
      }
    });
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    for (GshTemplateInputConfig gshTemplateInputConfig: gshTemplateConfig.getGshTemplateInputConfigs()) {
      GuiGshTemplateInputConfig guiGshTemplateInputConfig = new GuiGshTemplateInputConfig();
      guiGshTemplateInputConfig.setGshTemplateInputConfig(gshTemplateInputConfig);
      
      String value = request.getParameter("config_"+gshTemplateInputConfig.getName());
      
      if (!gshTemplateInputConfig.getGshTemplateInputType().canConvertToCorrectType(value)) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.conversion.message");
        errorMessage = errorMessage.replace("$$valueFromUser$$", GrouperUtil.escapeHtml(value, true));
        errorMessage = errorMessage.replace("$$type$$", GrouperUtil.escapeHtml(gshTemplateInputConfig.getGshTemplateInputType().name().toLowerCase(), true));
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#config_"+gshTemplateInputConfig.getName()+"_id", 
            errorMessage));
        return null;
      }
      
      guiGshTemplateInputConfig.setValue(value);
      
      variableMap.put(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getGshTemplateInputType().converToType(value));
      guiTemplateInputConfigsMap.put(gshTemplateInputConfig.getName(), guiGshTemplateInputConfig);
      
    }
    
    //for showEL, loop again and remove the ones where showEL is evaluated to false
    for (GshTemplateInputConfig gshTemplateInputConfig: gshTemplateConfig.getGshTemplateInputConfigs()) {
      
      String showElScript = gshTemplateInputConfig.getShowEl();
      if (StringUtils.isNotBlank(showElScript)) {
        try {
          Object substituteExpressionLanguageScript = GrouperUtil.substituteExpressionLanguageScript(showElScript, variableMap, true, false, false);
          Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(substituteExpressionLanguageScript);
          if (booleanObjectValue == null || !booleanObjectValue) {
            guiTemplateInputConfigsMap.remove(gshTemplateInputConfig.getName());
          }
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, ", script: '" + showElScript + "', ");
          GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(variableMap));
          throw re;
        }
        
      }
      
    }
    
    guiGshTemplateConfig.setGuiGshTemplateInputConfigs(guiTemplateInputConfigsMap);
    StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
    templateContainer.setGuiGshTemplateConfig(guiGshTemplateConfig);
    
    return guiTemplateInputConfigsMap;
    
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
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      stem = UiV2Stem.retrieveStemHelper(request, false, true, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
      
      String templateType = request.getParameter("templateType");
      
      if (StringUtils.isBlank(templateType)) {
        throw new RuntimeException("templateType cannot be blank.");
      }
      
      templateContainer.setTemplateType(templateType);
      
      GshTemplateExec exec = new GshTemplateExec();
      exec.assignConfigId(templateType);
      exec.assignCurrentUser(loggedInSubject);
      
      exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
      exec.assignOwnerStemName(stem.getName());
      
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(templateType);
      gshTemplateConfig.populateConfiguration();
      
      Map<String, GuiGshTemplateInputConfig> gshTemplateInputs = populateCustomTemplateInputs(request, templateType);
      
      if (gshTemplateInputs == null) {
        return;
      }
      
      for (String inputName: gshTemplateInputs.keySet()) {
        
        String value = request.getParameter("config_"+inputName);
        
        GshTemplateInput input = new GshTemplateInput();
        input.assignName(inputName);
        input.assignValueString(value);
        exec.addGshTemplateInput(input);
        
      }
      
      
      GshTemplateExecOutput gshTemplateExecOutput = exec.execute();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
        
        for (GshValidationLine gshValidationLIne: gshTemplateExecOutput.getGshTemplateOutput().getValidationLines()) {
          if (StringUtils.isNotBlank(gshValidationLIne.getInputName())) {
            
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#config_"+gshValidationLIne.getInputName()+"_id", 
                gshValidationLIne.getText()));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, gshValidationLIne.getText()));
          }
          
        }
        
      }
      
      for (GshOutputLine outputLine: gshTemplateExecOutput.getGshTemplateOutput().getOutputLines()) {
        
        GuiMessageType guiMessageType = GuiMessageType.valueOf(GrouperUtil.defaultIfBlank(GrouperUtil.defaultString(outputLine.getMessageType()).toLowerCase(), "success"));
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(guiMessageType, outputLine.getText()));
      }
      
      if (gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
        return;
      }
      
      if (!gshTemplateExecOutput.isSuccess()) {
        
        if (gshTemplateConfig.isDisplayErrorOutput()) {
          
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, "<pre>"+GrouperUtil.escapeHtml(gshTemplateExecOutput.getGshScriptOutput(), true)+"</pre>"));
          
          String exceptionMessage = null; 
          if (gshTemplateExecOutput.getException() != null) {        
            exceptionMessage = ExceptionUtils.getStackTrace(gshTemplateExecOutput.getException());
          }
          
          if (StringUtils.isNotBlank(exceptionMessage)) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, "<pre>"+exceptionMessage+"</pre>"));
          }
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemTemplateCustomGshTemplateExecuteError")));
        }
        
      }  else {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));
        //lets show a success message on the new screen
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("stemTemplateCustomGshTemplateExecuteSuccess")));
        
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
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

      final StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();

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
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("stemTemplateHierarchySelectError")));
        return;
      }
      
      for (ServiceAction serviceAction: selectedServiceActions) {
        serviceAction.getServiceActionType().createTemplateItem(serviceAction);
      }

      String errorKey = templateLogic.postCreateSelectedActions(selectedServiceActions);

      if (!StringUtils.isBlank(errorKey)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get(errorKey)));
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
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
      
      final StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
      
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
    
    StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
    
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
      
      String regex = "^[a-zA-Z0-9_]*$";
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
    
    try {
      
      GrouperTemplateLogicBase templateLogic = getTemplateLogic(templateType, 
          GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer());

      return templateLogic;
    } catch(Exception e) {
      return null;
    }
    
  }

  /**
   * @param templateType 
   * @param stemTemplateContainer 
   * @return the instance
   */
  public static GrouperTemplateLogicBase getTemplateLogic(String templateType, StemTemplateContainer stemTemplateContainer) {
    
    String implementationClass = GrouperUiConfig.retrieveConfig().propertyValueStringRequired("grouper.template."+templateType+".logicClass");
    
    Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(implementationClass);
    
    GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
    
    templateLogic.setStemTemplateContainer(stemTemplateContainer);
    return templateLogic;
  }
  
  /**
   * Find all template types and set them in container
   */
  private void setTemplateOptions() {
    
    StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
    
    Map<String, String> serviceClassPatterns = GrouperUiConfig.retrieveConfig().propertiesMap(grouperTemplateServiceClassPattern);
    
    for (Entry<String, String> entry: serviceClassPatterns.entrySet()) {
      
      String property = entry.getKey();
      String implementationClass = entry.getValue();
      
      Matcher matcher = grouperTemplateServiceClassPattern.matcher(property);
      
      if (matcher.matches()) {          
      
        String templateKey = matcher.group(1);
        
        Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(implementationClass);
        
        GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
        
        String label = TextContainer.retrieveFromRequest().getText().get(templateLogic.getSelectLabelKey());
        templateContainer.getTemplateOptions().put(templateKey, label);
        
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
  
  
    
}
