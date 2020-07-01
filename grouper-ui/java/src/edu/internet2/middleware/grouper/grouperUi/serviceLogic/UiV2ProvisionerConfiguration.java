package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisionerConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ProvisionerConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2ProvisionerConfiguration {
  
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
          return;
        }
        
        provisionerConfiguration.setConfigId(provisionerConfigId);
        
        String previousProvisionerConfigId = request.getParameter("previousProvisionerConfigId");
        String previousProvisionerConfigType = request.getParameter("previousProvisionerConfigType");
        if (StringUtils.isBlank(previousProvisionerConfigId) 
            || !StringUtils.equals(provisionerConfigType, previousProvisionerConfigType)) {
          // first time loading the screen or
          // provisioner config type changed
          // let's get values from config files/database
        } else {
          populateProvisionerConfigurationFromUi(request, provisionerConfiguration);
        }
        
        GuiProvisionerConfiguration guiProvisionerConfiguration = GuiProvisionerConfiguration.convertFromProvisionerConfiguration(provisionerConfiguration);
        provisionerConfigurationContainer.setGuiProvisionerConfiguration(guiProvisionerConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigAdd.jsp"));
      
      
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
      
      populateProvisionerConfigurationFromUi(request, provisionerConfiguration);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      provisionerConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);

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
        populateProvisionerConfigurationFromUi(request, provisionerConfiguration);
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
      
      populateProvisionerConfigurationFromUi(request, provisionerConfiguration);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("provisionerConfigDeleteSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  private void populateProvisionerConfigurationFromUi(final HttpServletRequest request, ProvisionerConfiguration provisionerConfiguration) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = provisionerConfiguration.retrieveAttributes();
    
    for (GrouperConfigurationModuleAttribute attribute: attributes.values()) {
      String name = "config_"+attribute.getConfigSuffix();
      String elCheckboxName = "config_el_"+attribute.getConfigSuffix();
      
      String elValue = request.getParameter(elCheckboxName);
      
      String value = null;
      if (attribute.getConfigItemMetadata().getFormElement() == ConfigItemFormElement.CHECKBOX) {
        String[] values = request.getParameterValues(name+"[]");
        if (values != null && values.length > 0) {
          value = String.join(",", Arrays.asList(values));
        }
      } else {
        value = request.getParameter(name);
      }
      
      if (StringUtils.isNotBlank(elValue) && elValue.equalsIgnoreCase("on")) {
        attribute.setExpressionLanguage(true);
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setExpressionLanguageScript(value);
      } else {
        attribute.setExpressionLanguage(false);
        attribute.setValue(value);
      }
        
    }
    
  }

  
  
}
