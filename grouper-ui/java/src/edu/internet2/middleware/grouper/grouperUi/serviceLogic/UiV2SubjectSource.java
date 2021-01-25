package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.subectSource.SubjectSourceConfiguration;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiSubjectSourceConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SubjectSourceContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;

public class UiV2SubjectSource {
  
  /**
   * @param request
   * @param response
   */
  public void viewSubjectSources(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectSourceContainer subjectSourceContainer = grouperRequestContainer.getSubjectSourceContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<Source> sources = SubjectFinder.getSources();
          subjectSourceContainer.setSources(sources);
          
          // sources.iterator().next().getClass() is assignable from 
          
          return null;
        }
      });
            
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSources.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * show form to add a new subject source
   * @param request
   * @param response
   */
  public void addSubjectSource(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      final SubjectSourceContainer subjectSourceContainer = grouperRequestContainer.getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceConfigId = request.getParameter("subjectSourceConfigId");
      
      String subjectSourceConfigType = request.getParameter("subjectSourceConfigType");
      
      if (StringUtils.isNotBlank(subjectSourceConfigType)) {
        
        if (!SubjectSourceConfiguration.sourceConfigClassNames.contains(subjectSourceConfigType)) {            
          throw new RuntimeException("Invalid subjectSourceConfigType "+subjectSourceConfigType);
        }

        Class<SubjectSourceConfiguration> klass = (Class<SubjectSourceConfiguration>) GrouperUtil.forName(subjectSourceConfigType);
        SubjectSourceConfiguration subjectSourceConfiguration = (SubjectSourceConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(subjectSourceConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#subjectSourceConfigId",
              TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigCreateErrorConfigIdRequired")));
          guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("subjectSourceConfigType", ""));
          return;
        }
        
        subjectSourceConfiguration.setConfigId(subjectSourceConfigId);
        
        if (subjectSourceConfiguration.retrieveConfigurationConfigIds().contains(subjectSourceConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#subjectSourceConfigId", TextContainer.retrieveFromRequest().getText().get("grouperConfigurationValidationConfigIdUsed")));
          return;
        }
        
        String previousSubjectSourceConfigId = request.getParameter("previousSubjectSourceConfigId");
        String previousSubjectSourceConfigType = request.getParameter("previousSubjectSourceConfigType");
        if (StringUtils.isBlank(previousSubjectSourceConfigId) 
            || !StringUtils.equals(subjectSourceConfigType, previousSubjectSourceConfigType)) {
          // first time loading the screen or
          // subject source config type changed
          // let's get values from config files/database
        } else {
          subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
          //populateSubjectSourceConfigurationFromUi(request, subjectSourceConfiguration);
        }
        
        GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = GuiSubjectSourceConfiguration.convertFromSubjectSourceConfiguration(subjectSourceConfiguration);
        subjectSourceContainer.setGuiSubjectSourceConfiguration(guiSubjectSourceConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSourceAdd.jsp"));
      
      String focusOnElementName = request.getParameter("focusOnElementName");
      if (!StringUtils.isBlank(focusOnElementName)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$(\"[name='" + focusOnElementName + "']\").focus()"));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new subject source in db
   * @param request
   * @param response
   */
  public void addSubjectSourceSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceConfigId = request.getParameter("subjectSourceConfigId");
      
      String subjectSourceConfigType = request.getParameter("subjectSourceConfigType");
      
      if (StringUtils.isBlank(subjectSourceConfigId)) {
        throw new RuntimeException("subjectSourceConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(subjectSourceConfigType)) {
        throw new RuntimeException("subjectSourceConfigType cannot be blank");
      }
      
      if (!SubjectSourceConfiguration.sourceConfigClassNames.contains(subjectSourceConfigType)) {            
        throw new RuntimeException("Invalid subjectSourceConfigType "+subjectSourceConfigType);
      }
      
      Class<SubjectSourceConfiguration> klass = (Class<SubjectSourceConfiguration>) GrouperUtil.forName(subjectSourceConfigType);
      SubjectSourceConfiguration subjectSourceConfiguration = (SubjectSourceConfiguration) GrouperUtil.newInstance(klass);
      
      subjectSourceConfiguration.setConfigId(subjectSourceConfigId);
      subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
      //populateSubjectSourceConfigurationFromUi(request, subjectSourceConfiguration);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      subjectSourceConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      SourceManager.getInstance().reloadSource(subjectSourceConfiguration.retrieveAttributes().get("id").getValueOrExpressionEvaluation());
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectSource.viewSubjectSources')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show screen to edit subject source config
   * @param request
   * @param response
   */
  public void editSubjectSource(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceId = request.getParameter("subjectSourceId");
      
      if (StringUtils.isBlank(subjectSourceId)) {
        throw new RuntimeException("subjectSourceId cannot be blank");
      }
      
      SubjectSourceConfiguration subjectSourceConfiguration = null;
      
      List<SubjectSourceConfiguration> subjectSourceConfigurations = SubjectSourceConfiguration.retrieveAllSubjectSourceConfigurations();
      
      for (SubjectSourceConfiguration subjectSourceConfig: subjectSourceConfigurations) {
        GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = subjectSourceConfig.retrieveAttributes().get("id");
        if (grouperConfigurationModuleAttribute != null) {
          String id = grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation();
          if (id != null && id.equals(subjectSourceId)) {
            subjectSourceConfiguration = subjectSourceConfig;
            break;
          }
        }
      }
      
      if (subjectSourceConfiguration == null) {
        throw new RuntimeException("Could not find subject source config for source id "+subjectSourceId);
      }
      
      subjectSourceContainer.setSubjectSourceId(subjectSourceId);
      
      String previousSubjectSourceConfigId = request.getParameter("previousSubjectSourceConfigId");
      
      if (StringUtils.isBlank(previousSubjectSourceConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = GuiSubjectSourceConfiguration.convertFromSubjectSourceConfiguration(subjectSourceConfiguration);
        subjectSourceContainer.setGuiSubjectSourceConfiguration(guiSubjectSourceConfiguration);
      } else {
        // change was made on the form
        subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
        GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = GuiSubjectSourceConfiguration.convertFromSubjectSourceConfiguration(subjectSourceConfiguration);
        subjectSourceContainer.setGuiSubjectSourceConfiguration(guiSubjectSourceConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectSource/subjectSourceEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * edit existing subject source in db
   * @param request
   * @param response
   */
  public void editSubjectSourceSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceConfigId = request.getParameter("subjectSourceConfigId");
      
      String subjectSourceConfigType = request.getParameter("subjectSourceConfigType");
      
      if (StringUtils.isBlank(subjectSourceConfigId)) {
        throw new RuntimeException("subjectSourceConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(subjectSourceConfigType)) {
        throw new RuntimeException("subjectSourceConfigType cannot be blank");
      }
      
      if (!SubjectSourceConfiguration.sourceConfigClassNames.contains(subjectSourceConfigType)) {            
        throw new RuntimeException("Invalid subjectSourceConfigType "+subjectSourceConfigType);
      }
      
      Class<SubjectSourceConfiguration> klass = (Class<SubjectSourceConfiguration>) GrouperUtil.forName(subjectSourceConfigType);
      SubjectSourceConfiguration subjectSourceConfiguration = (SubjectSourceConfiguration) GrouperUtil.newInstance(klass);
      
      subjectSourceConfiguration.setConfigId(subjectSourceConfigId);
      subjectSourceConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();

     subjectSourceConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      SourceManager.getInstance().reloadSource(subjectSourceConfiguration.retrieveAttributes().get("id").getValueOrExpressionEvaluation());
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectSource.viewSubjectSources')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete existing subject source in db
   * @param request
   * @param response
   */
  public void deleteSubjectSource(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SubjectSourceContainer subjectSourceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectSourceContainer();
      
      if (!subjectSourceContainer.isCanViewSubjectSources()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String subjectSourceId = request.getParameter("subjectSourceId");
      
      if (StringUtils.isBlank(subjectSourceId)) {
        throw new RuntimeException("subjectSourceId cannot be blank");
      }
      
      SubjectSourceConfiguration subjectSourceConfiguration = null;
      
      List<SubjectSourceConfiguration> subjectSourceConfigurations = SubjectSourceConfiguration.retrieveAllSubjectSourceConfigurations();
      
      for (SubjectSourceConfiguration subjectSourceConfig: subjectSourceConfigurations) {
        GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = subjectSourceConfig.retrieveAttributes().get("id");
        if (grouperConfigurationModuleAttribute != null) {
          String id = grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation();
          if (id != null && id.equals(subjectSourceId)) {
            subjectSourceConfiguration = subjectSourceConfig;
            break;
          }
        }
      }
      
      if (subjectSourceConfiguration == null) {
        throw new RuntimeException("Could not find subject source config for source id "+subjectSourceId);
      }
      
      subjectSourceConfiguration.deleteConfig(true);
      
      SourceManager.getInstance().reloadSource(subjectSourceId);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectSource.viewSubjectSources')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("subjectSourceConfigDeleteSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
}
