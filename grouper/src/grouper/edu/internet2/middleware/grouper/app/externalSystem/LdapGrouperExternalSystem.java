package edu.internet2.middleware.grouper.app.externalSystem;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class LdapGrouperExternalSystem extends GrouperExternalSystem {
  
  
  @Override
  public String getType() {
    return "Ldap";
  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "ldap." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(ldap)\\.([^.]+)\\.(.*)$";
  }



}
