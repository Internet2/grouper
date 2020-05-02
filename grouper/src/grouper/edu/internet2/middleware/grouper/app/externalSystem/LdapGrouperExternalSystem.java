package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class LdapGrouperExternalSystem extends GrouperExternalSystem {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    // ldap://ldap.andrew.cmu.edu:389/dc=cmu,dc=edu
    LdapGrouperExternalSystem ldapGrouperExternalSystem = new LdapGrouperExternalSystem();
    ldapGrouperExternalSystem.setConfigId("sdfasdf");
    
    System.out.println(ldapGrouperExternalSystem.test());
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

  @Override
  public List<String> test() throws UnsupportedOperationException {
    if (LdapSessionUtils.ldapSession().testConnection(this.getConfigId())) {
      return null;
    }
    return GrouperUtil.toList("Invalid config");
  }

  
  
}
