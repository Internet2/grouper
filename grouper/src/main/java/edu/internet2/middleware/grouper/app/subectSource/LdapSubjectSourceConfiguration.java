package edu.internet2.middleware.grouper.app.subectSource;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.subj.GrouperLdapSourceAdapter2_5;

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

}
