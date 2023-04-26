package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.boxProvisioner.BoxGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.google.GoogleGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.messaging.GrouperInternalMessagingExternalSystem;
import edu.internet2.middleware.grouper.app.oidc.OidcGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.smtp.SmtpGrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public abstract class GrouperExternalSystem extends GrouperConfigurationModuleBase implements OptionValueDriver {
  
  /**
   * return list of error messages
   * @return
   * @throws UnsupportedOperationException
   */
  public List<String> test() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * 
   * @param isInsert
   * @param fromUi
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void validatePreSave(boolean isInsert, boolean fromUi, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);

    if (!isInsert && !this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
      validationErrorsToDisplay.put("#externalSystemConfigId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdDoesntExist"));
    }
    
  }
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "externalSystem";
  }

  /**
   * 
   * @return
   */
  public List<GrouperExternalSystemConsumer> retrieveAllUsedBy() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * get value for one property
   * @param attributeName
   * @return
   */
  public String propertiesApiProperyValue(String attributeName) {
    return this.getConfigFileName().getConfig().propertyValueString(this.getConfigItemPrefix()+attributeName);
  }
  
  
  public final static Set<String> externalTypeClassNames = new LinkedHashSet<String>();
  static {
    
    List<String> externalTypeClassNamesList = new ArrayList<>();
    
    externalTypeClassNamesList.add("edu.internet2.middleware.grouperMessagingActiveMQ.ActiveMqGrouperExternalSystem");
    externalTypeClassNamesList.add(AzureGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add("edu.internet2.middleware.grouperBox.BoxGrouperExternalSystem");
    externalTypeClassNamesList.add(BoxGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add("edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem");
    externalTypeClassNamesList.add("edu.internet2.middleware.grouperDuo.DuoGrouperExternalSystem");
    externalTypeClassNamesList.add(GoogleGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(GrouperInternalMessagingExternalSystem.class.getName());
    externalTypeClassNamesList.add(LdapGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add("edu.internet2.middleware.grouper.o365.Office365GrouperExternalSystem");
    externalTypeClassNamesList.add(OidcGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add("edu.internet2.middleware.grouperMessagingRabbitmq.RabbitMqGrouperExternalSystem");
    externalTypeClassNamesList.add("edu.internet2.middleware.grouper.app.remedy.RemedyGrouperExternalSystem");
    externalTypeClassNamesList.add("edu.internet2.middleware.grouper.app.remedy.RemedyDigitalMarketplaceGrouperExternalSystem");
    externalTypeClassNamesList.add("edu.internet2.middleware.grouper.app.file.SftpGrouperExternalSystem");
    externalTypeClassNamesList.add(SmtpGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add("edu.internet2.middleware.grouperMessagingAWS.SqsGrouperExternalSystem");
    externalTypeClassNamesList.add(WsBearerTokenExternalSystem.class.getName());
    
    String extraExternalSystemRegex = "^grouperExtraExternalSystem\\.([^.]+)\\.class$";
    Pattern extraExternalSystemPattern = Pattern.compile(extraExternalSystemRegex);
    Map<String, String> extraExternalSystemClasses = GrouperConfig.retrieveConfig().propertiesMap(extraExternalSystemPattern);
    if (GrouperUtil.length(extraExternalSystemClasses) > 0) {
      for (String className : extraExternalSystemClasses.values()) {
        externalTypeClassNamesList.add(className);
      }
    }
    
    Collections.sort(externalTypeClassNamesList, new Comparator<String>() {

      @Override
      public int compare(String arg0, String arg1) {
        return GrouperUtil.suffixAfterChar(arg0, '.').compareTo(GrouperUtil.suffixAfterChar(arg1, '.'));
      }
    });
    
    externalTypeClassNames.addAll(externalTypeClassNamesList);
    
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<GrouperExternalSystem> retrieveAllModuleConfigurationTypes() {
    return (List<GrouperExternalSystem>) (Object) retrieveAllConfigurationTypesHelper(externalTypeClassNames);
  }
  
  /**
   * list of configured external systems
   * @return
   */
  public static List<GrouperExternalSystem> retrieveAllGrouperExternalSystems() {
   return (List<GrouperExternalSystem>) (Object) retrieveAllConfigurations(externalTypeClassNames);
  }

  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    List<GrouperExternalSystem> externalSystems = (List<GrouperExternalSystem>) (Object) this.listAllConfigurationsOfThisType();
    
    for (GrouperExternalSystem externalSystem: externalSystems) {
      
      if (externalSystem.isEnabled()) {
        String configId = externalSystem.getConfigId();
        keysAndLabels.add(new MultiKey(configId, configId));
      }
      
    }
    
    Collections.sort(keysAndLabels, new Comparator<MultiKey>() {

      @Override
      public int compare(MultiKey o1, MultiKey o2) {
        return ((String)o1.getKey(0)).compareTo((String)o2.getKey(0));
      }
    });
    
    return keysAndLabels;
  }
  
  /**
   * check if connections need to be refreshed due to config changes
   * @return
   * @throws UnsupportedOperationException
   */
  public void refreshConnectionsIfNeeded() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public boolean isCanAdd() {
    
    return true;
  }
  public boolean isCanDelete() {
    
    return true;
  }
}
