package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.List;

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
	    
	    String templateType = request.getParameter("templateType");
	    
	    final StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
	    
	    if (StringUtils.isNotBlank(templateType)) {
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
  
    Stem stem = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String templateType = request.getParameter("templateType");
      
      if (StringUtils.isBlank(templateType)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#templateTypeId",
            TextContainer.retrieveFromRequest().getText().get("stemTemplateTypeRequiredError")));
        return;
      }
      
      String templateKey = request.getParameter("templateKey");
      if (StringUtils.isBlank(templateKey)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#serviceKeyId",
            TextContainer.retrieveFromRequest().getText().get("stemServiceKeyRequiredError")));
        return;
      }
      
      final StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
      
      String className = GrouperUiConfig.retrieveConfig().properties().getProperty("grouperNewServiceLogicClass");
      Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(className);
      
      GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
      
      templateContainer.setTemplateType(templateType);
      templateContainer.setTemplateKey(templateKey);
      templateContainer.setTemplateDescription(request.getParameter("serviceDescription"));
      templateContainer.setTemplateFriendlyName(request.getParameter("serviceFriendlyName"));
      
      List<ServiceAction> serviceActions = templateLogic.displayOnScreen();
      templateContainer.setServiceActions(serviceActions);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemTemplate.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemTemplate", 
          "/WEB-INF/grouperUi2/stem/stemNewTemplate.jsp"));
      
    } finally {
        GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void editTemplateType(HttpServletRequest request, HttpServletResponse response) {}
    
}
