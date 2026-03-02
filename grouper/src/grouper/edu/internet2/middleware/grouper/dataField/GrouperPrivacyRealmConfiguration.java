package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.exception.GrouperReferentialIntegrityException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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

  @Override
  public void deleteConfig(boolean fromUi) {

    String configId = this.getConfigId();

    List<String> referencingConfigs = new ArrayList<>();

    // check if this privacy realm is referenced by any data fields or data rows
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(null);

    for (Map.Entry<String, GrouperDataFieldConfig> fieldEntry : grouperDataEngine.getFieldConfigByConfigId().entrySet()) {
      if (StringUtils.equals(configId, fieldEntry.getValue().getGrouperPrivacyRealmConfigId())) {
        referencingConfigs.add("data field '" + fieldEntry.getKey() + "'");
      }
    }

    for (Map.Entry<String, GrouperDataRowConfig> rowEntry : grouperDataEngine.getRowConfigByConfigId().entrySet()) {
      if (StringUtils.equals(configId, rowEntry.getValue().getPrivacyRealmName())) {
        referencingConfigs.add("data row '" + rowEntry.getKey() + "'");
      }
    }

    if (referencingConfigs.size() > 0) {
      String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
      throw new GrouperReferentialIntegrityException("Error: cannot delete this privacy realm because it is referenced by: " + referencingConfigsString);
    }

    super.deleteConfig(fromUi);
  }

}
