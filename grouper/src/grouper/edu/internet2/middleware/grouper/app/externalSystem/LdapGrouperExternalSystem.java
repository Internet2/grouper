package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.pool.CompareValidator;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.pool.Validator;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class LdapGrouperExternalSystem extends GrouperExternalSystem {
  
  
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
    
//    // save with insert or edit
//    
//    LdapSessionUtils.ldapSession().authenticate(this.getConfigId(), 
//        GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() + "user"),
//        GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() + "pass"));
//     
    
//    Validator<Connection> validator = null;
//
//    String ldapValidator = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".validator", "SearchValidator");
//
//    if (StringUtils.equalsIgnoreCase(ldapValidator, CompareValidator.class.getSimpleName())
//        || StringUtils.equalsIgnoreCase(ldapValidator, "CompareLdapValidator")) {
//      String validationDn = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareDn");
//      String validationAttribute = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareAttribute");
//      String validationValue = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareValue");
//      validator = new CompareValidator(new CompareRequest(validationDn, new LdapAttribute(validationAttribute, validationValue)));
//    } else if (StringUtils.equalsIgnoreCase(ldapValidator, SearchValidator.class.getSimpleName())) {
//      validator = new SearchValidator();
//    }

   throw new UnsupportedOperationException(); 
  }

  
  
}
