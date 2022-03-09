/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 *
 */
public class GrouperDbConfig {

  /**
   * 
   */
  public GrouperDbConfig() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
  }
  
  /**
   * which config file
   */
  private String configFileName;
  
  /**
   * 
   * @param theConfigFile
   * @return this for chaining
   */
  public GrouperDbConfig configFileName(String theConfigFile) {
    this.configFileName = theConfigFile;
    return this;
  }
  
  /**
   * property name of the config
   */
  private String propertyName;

  /**
   * property name of the config
   * @param thePropertyName
   * @return this for chaining
   */
  public GrouperDbConfig propertyName(String thePropertyName) {
    this.propertyName = thePropertyName;
    return this;
  }
  
  /**
   * comment about the setting
   */
  private String comment;
  
  /**
   * comment about the setting
   * @param theComment comment about setting
   * @return this for chaining
   */
  public GrouperDbConfig comment(String theComment) {
    this.comment = theComment;
    return this;
  }
  
  /**
   * value of the config.  Can be encrypted
   */
  private String value;
  
  /**
   * value of the config.  Can be encrypted
   * @param theValue
   * @return this for chaining
   */
  public GrouperDbConfig value(String theValue) {
    this.value = theValue;
    return this;
  }
  
  /**
   * 
   */
  public static void seeIfAllowed() {
    
    //    this is ok
    //
    //    final boolean uiConfigurationEnabled = GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true);
    //    
    //    if (!uiConfigurationEnabled) {
    //      throw new RuntimeException("grouperUi.configuration.enabled needs to be true in grouper-ui.properties");
    //    }

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);

    if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
      throw new RuntimeException("User is not allowed to edit database configuration! " 
          + SubjectUtils.subjectToString(grouperSession.getSubject()));
    }

  }
  
  /**
   * @return message
   */
  public String store() {
    seeIfAllowed();
    StringBuilder message = null;
    
    if (StringUtils.isBlank(this.configFileName)) {
      throw new RuntimeException("Config file is required!");
    }
    if (StringUtils.isBlank(this.propertyName)) {
      throw new RuntimeException("Property name is required!");
    }
    Boolean[] error = new Boolean[]{null};
    Boolean[] added = new Boolean[]{null};
    message = new StringBuilder();
    ConfigFileName configFileNameObject = ConfigFileName.valueOfIgnoreCase(this.configFileName, true);
    ConfigFileMetadata configFileMetadata = configFileNameObject.configFileMetadata();
    List<String> actionsPerformed = new ArrayList<String>();

    Set<MultiKey> configFileNameAndKeys = GrouperUtil.toSet(new MultiKey(this.configFileName, this.propertyName));

    Map<String, Set<GrouperConfigHibernate>> keyToConfigHibernate = GrouperDAOFactory.getFactory().getConfig().findByFileAndKey(configFileNameAndKeys);

    String thePropertyNameString = GrouperUtil.stripSuffix(this.propertyName, ".elConfig");
    Set<GrouperConfigHibernate> grouperConfigHibernates = keyToConfigHibernate.get(thePropertyNameString);
    
    String propertyNameStringEl = thePropertyNameString + ".elConfig";
    Set<GrouperConfigHibernate> grouperConfigHibernatesEl = keyToConfigHibernate.get(propertyNameStringEl);

    DbConfigEngine.configurationFileAddEditHelper2(configFileNameObject, this.configFileName, configFileMetadata, this.propertyName,
        Boolean.toString(this.propertyName.endsWith(".elConfig")), this.value, null, message, added, error, false, 
        this.comment, new ArrayList<String>(), new HashMap<String, String>(), true, actionsPerformed, grouperConfigHibernates, grouperConfigHibernatesEl);
    if (error[0] != null && error[0]) {
      throw new RuntimeException("Has error.  Message: " + message);
    }

    return message == null ? null : message.toString();

  }
  /**
   * @return message
   */
  public String delete() {
    seeIfAllowed();
    StringBuilder message = null;
    
    if (StringUtils.isBlank(this.configFileName)) {
      throw new RuntimeException("Config file is required!");
    }
    if (StringUtils.isBlank(this.propertyName)) {
      throw new RuntimeException("Property name is required!");
    }
    if (!StringUtils.isBlank(this.value)) {
      throw new RuntimeException("Cant set value on delete");
    }
    if (!StringUtils.isBlank(this.comment)) {
      throw new RuntimeException("Cant set comment on delete");
    }
    
    Set<MultiKey> configFileNameAndKeys = GrouperUtil.toSet(new MultiKey(this.configFileName, this.propertyName));

    Map<String, Set<GrouperConfigHibernate>> keyToConfigHibernate = GrouperDAOFactory.getFactory().getConfig().findByFileAndKey(configFileNameAndKeys);
    
    String propertyNameString = GrouperUtil.stripSuffix(this.propertyName, ".elConfig");
    Set<GrouperConfigHibernate> grouperConfigHibernates = keyToConfigHibernate.get(propertyNameString);
    
    String propertyNameStringEl = propertyNameString + ".elConfig";
    Set<GrouperConfigHibernate> grouperConfigHibernatesEl = keyToConfigHibernate.get(propertyNameStringEl);

    // this clears the cache if necessary
    DbConfigEngine.configurationFileItemDeleteHelper(this.configFileName, this.propertyName, false, true, new ArrayList<String>(), grouperConfigHibernates, grouperConfigHibernatesEl);

    return message == null ? null : message.toString();

  }
  
}
