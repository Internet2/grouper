package edu.internet2.middleware.grouper.app.subectSource;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.subj.GrouperLdapSourceAdapter2_5;
import edu.internet2.middleware.subject.provider.LdapSourceAdapter;

public class LdapSubjectSourceConfiguration extends SubjectSourceConfiguration {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.SUBJECT_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "subjectApi.source." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(subjectApi.source)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "adapterClass";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperLdapSourceAdapter2_5.class.getName();
  }

  private void assignCacheConfig() {
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    assignCacheConfig();
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    assignCacheConfig();
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }
  

}
