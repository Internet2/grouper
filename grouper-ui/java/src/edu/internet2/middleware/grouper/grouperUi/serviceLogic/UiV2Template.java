package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
      templateContainer.setServiceActions(serviceActions);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemTemplate.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemTemplate", 
          "/WEB-INF/grouperUi2/stem/stemNewTemplate.jsp"));
      
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
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GrouperTemplateLogicBase templateLogic = getTemplateLogic(request);
      
      if (templateLogic == null) {
        return;
      }
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemServiceCreateSuccess")));
      
      
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
