package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.boxProvisioner.BoxGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.file.SftpGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.google.GoogleGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.interfolio.InterfolioExternalSystem;
import edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.messaging.GrouperInternalMessagingExternalSystem;
import edu.internet2.middleware.grouper.app.oidc.OidcGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.remedy.RemedyDigitalMarketplaceGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.remedy.RemedyGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.smtp.SmtpGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixExternalSystem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperDuo.DuoGrouperExternalSystem;
import edu.internet2.middleware.grouperMessagingAWS.SqsGrouperExternalSystem;
import edu.internet2.middleware.grouperMessagingActiveMQ.ActiveMqGrouperExternalSystem;
import edu.internet2.middleware.grouperMessagingRabbitmq.RabbitMqGrouperExternalSystem;

public abstract class GrouperExternalSystem extends GrouperConfigurationModuleBase implements OptionValueDriver {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperExternalSystem.class);

  /**
   * return list of error messages
   * @return
   * @throws UnsupportedOperationException
   */
  public List<String> test() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * a test() which catches an exception and turns it into a message loses the stack and the
   * cause chain, so the operator sees only the message of the outermost wrapper exception and
   * nothing at all is written to the logs.  log the throwable as an error and return a message
   * which includes the message of each throwable in the cause chain, where the real problem
   * generally lives.
   * @param messagePrefix describes what failed, without trailing punctuation, e.g.
   * "Unable to retrieve Azure authentication token"
   * @param throwable the exception caught in test()
   * @return the html escaped message to add to the errors returned from test()
   */
  protected String logAndDescribeTestException(String messagePrefix, Throwable throwable) {
    
    LOG.error("Error testing external system '" + this.getConfigId() + "' (" 
        + this.getClass().getSimpleName() + "): " + messagePrefix, throwable);
    
    return GrouperUtil.escapeHtml(messagePrefix + ": " + causeChainMessage(throwable), true);
  }
  
  /**
   * build a message from the message of each throwable in the cause chain, since the message of
   * a wrapper exception is often generic (e.g. "Error building client_assertion JWT") and the
   * actionable detail (e.g. "Unrecognized PEM header in private key") is only in the cause
   * @param throwable
   * @return the messages of the cause chain, outermost first, or empty string if null
   */
  public static String causeChainMessage(Throwable throwable) {
    
    StringBuilder result = new StringBuilder();
    
    // guard against a cause chain which loops back on itself
    Set<Throwable> throwablesSeen = new LinkedHashSet<Throwable>();
    
    for (Throwable current = throwable; current != null && throwablesSeen.add(current); current = current.getCause()) {
      
      String message = StringUtils.trimToNull(current.getMessage());
      
      // some exceptions (e.g. NullPointerException) have no message, so at least say which one it was
      if (message == null) {
        message = current.getClass().getSimpleName();
      }
      
      // dont repeat text, e.g. when a wrapper's message already contains the message of its cause
      if (result.indexOf(message) >= 0) {
        continue;
      }
      
      if (result.length() > 0) {
        result.append(": ");
      }
      result.append(message);
    }
    
    return result.toString();
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
    
    externalTypeClassNamesList.add(ActiveMqGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(AzureGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(BoxGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(DatabaseGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(DuoGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(GoogleGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(GrouperInternalMessagingExternalSystem.class.getName());
    externalTypeClassNamesList.add(InterfolioExternalSystem.class.getName());
    externalTypeClassNamesList.add(LdapGrouperExternalSystem.class.getName());
    //TODO remove in v5
    externalTypeClassNamesList.add("edu.internet2.middleware.grouper.o365.Office365GrouperExternalSystem");
    externalTypeClassNamesList.add(OidcGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(RabbitMqGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(RemedyGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(RemedyDigitalMarketplaceGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(SftpGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(SmtpGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(SqsGrouperExternalSystem.class.getName());
    externalTypeClassNamesList.add(TeamDynamixExternalSystem.class.getName());
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
