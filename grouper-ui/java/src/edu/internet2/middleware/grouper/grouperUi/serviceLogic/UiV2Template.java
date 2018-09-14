package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ServiceAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.StemTemplateContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class UiV2Template {
	
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Template.class);
  
  
  public static Pattern grouperTemplateServiceClassPattern = Pattern.compile(
      "^grouper\\.template\\.(\\w+)\\.logicClass$");
  
  
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
	      
	      GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);
	      
	      templateContainer.setTemplateLogic(templateLogic);
	      templateContainer.setTemplateType(templateType);
	      
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
      
      setTemplateOptions();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
      
      templateLogic.setStemId(stem.getUuid());
      
      templateContainer.setTemplateLogic(templateLogic);
      
      String templateType = request.getParameter("templateType");
      templateContainer.setTemplateType(templateType);
      
      if(templateLogic.isPromptForKeyAndLabelAndDescription()) {
        String templateKey = request.getParameter("templateKey");
        if (StringUtils.isBlank(templateKey)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#serviceKeyId",
              TextContainer.retrieveFromRequest().getText().get("stemServiceKeyRequiredError")));
          return;
        }
        templateContainer.setTemplateKey(templateKey);
        templateContainer.setTemplateDescription(request.getParameter("serviceDescription"));
        templateContainer.setTemplateFriendlyName(request.getParameter("serviceFriendlyName"));
      } 

      List<ServiceAction> serviceActions = templateLogic.getServiceActions();
      templateContainer.setServiceActions(serviceActions);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemTemplate.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemTemplate", 
          "/WEB-INF/grouperUi2/stem/stemNewTemplate.jsp"));
      
    } finally {
        GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void newTemplateSubmit(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      templateLogic.setStemId(stem.getUuid());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemServiceCreateSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
  }
  
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
      String implementationClass = GrouperUiConfig.retrieveConfig().propertyValueStringRequired("grouper.template."+templateType+".logicClass");
      
      Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(implementationClass);
      
      GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
      
      return templateLogic;
    } catch(Exception e) {
      LOG.error("Could not load the logic implementation class.");
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
          "#templateTypeId",
          TextContainer.retrieveFromRequest().getText().get("stemTemplateImplementationClassConfigError")));
      return null;
    }
    
  }
  
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
  }
    
}
