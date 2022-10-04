package edu.internet2.middleware.grouper.app.midPointProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.midpointProvisioning.MidPointProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class MidPointProvisionerTestUtils {
  
  
  /**
   * 
   * @param midPointProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(MidPointProvisionerTestConfigInput midPointProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!midPointProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + midPointProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param provisioningTestConfigInput     
   */
  public static void configureMidpointProvisioner(MidPointProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "class", MidPointProvisioner.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "dbExternalSystemConfigId", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "midPointTablesPrefix", "gr");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "midPointLastModifiedColumnType", "long");
    configureProvisionerSuffix(provisioningTestConfigInput, "midPointLastModifiedColumnName", "last_modified");

    configureProvisionerSuffix(provisioningTestConfigInput, "midPointDeletedColumnName", "deleted");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "midPointListOfTargets", "a,b,c");

    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");

    
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");

    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");

    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_midPointProvTest.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_midPointProvTest.quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_midPointProvTest.provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_midPointProvTest.class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_midPointProvTest.quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_midPointProvTest.provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_midPointProvTest.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_midPointProvTest.publisher.debug").value("true").store();
    
    ConfigPropertiesCascadeBase.clearCache();
  
  }

}
