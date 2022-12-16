package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperPrivacyRealmConfiguration extends GrouperConfigurationModuleBase {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperPrivacyRealm." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperPrivacyRealm)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperPrivacyRealm";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "privacyRealmConfigId";
  }
  
  /**
   * list of configured privacy realm configs
   * @return
   */
  public static List<GrouperPrivacyRealmConfiguration> retrieveAllPrivacyRealmConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperPrivacyRealmConfiguration.class.getName());
   return (List<GrouperPrivacyRealmConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

}
