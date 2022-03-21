package edu.internet2.middleware.grouper.app.sqlProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class SqlProvisionerTestUtils {
  
  /**
   * 
   * @param sqlProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(SqlProvisionerTestConfigInput sqlProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!sqlProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + sqlProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param sqlProvisioningTestConfigInput     
   * SqlProvisionerTestUtils.configureSqlProvisioner(
   *       new SqlProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureSqlProvisioner(SqlProvisionerTestConfigInput sqlProvisioningTestConfigInput) {

    
    for (String key: sqlProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = sqlProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + sqlProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
