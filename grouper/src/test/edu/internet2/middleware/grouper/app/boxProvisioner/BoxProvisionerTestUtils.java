package edu.internet2.middleware.grouper.app.boxProvisioner;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class BoxProvisionerTestUtils {
  
  public static void setupBoxExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.authenticationUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/box/token/").store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.baseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/box").store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.clientId").value("y5x93fo8m1uwxizeic9b88xvkkol1alm").store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.clientSecret").value("asS48XlwmeDAXLSxVxzpYOqZo5q1hIKWeQMo7nbgLnxblRnwlFD9oNC7H5BYDxq").store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.enterpriseId").value("964565703").store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.publicKeyId").value("z30urhgy").store();

    String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAymeK52tp3E5wzN4IIpfAOFKVSX/uC2VSP22cJp2S1VTUx+NiieJWadYYrjQNMPQzaNUw+HNbbHylxk1LTgSOR70UXXp+nCIto6L0PdJpmCSun9KuyIT2KnI43niWioQsPzKTsEkFPraEotyub4FQAwAst5JXgCS0X0V1Bu8YRsxKo/QLOGFWxA8KulqdEC7EJxoqNv1NdBVQmLe8D9uc7bMYPG9Js3BlM9jyTDTN5UsCutWprg7UdmY0ZUSWI4nFrmgranzPtZrrz2LuVHaRbHPlFzGZEH/F43hWlLRNNUa1a7DV1KTc5vE9c3l5AxCtG5lKaTmWwUP1cHIDnCQTUwIDAQAB";  
    String privateKey = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"+
"MIIFLTBXBgkqhkiG9w0BBQ0wSjApBgkqhkiG9w0BBQwwHAQIBsZ7tLaNTpACAggA\n"+
"MAwGCCqGSIb3DQIJBQAwHQYJYIZIAWUDBAEqBBBTxkTfBQQfUwIFZksTnIZyBIIE\n"+
"0GOKMSWSPLkdTMZ7A1RVN2d5HecHw/Xi+YwGSUBzmtazRjVQinp9rlcQ3iFerCpn\n"+
"TSo3VD3Jh3+CXHpsuIw1O3fpIhoK53uaq8K5cMtxZDP7lZudQQbmsj1mvO7ngkfh\n"+
"LIeDXAS3ejQJXcOCBc7VaPc5UJ4nNUGBT9BofdAeg6dvjGmlKeq3WzvBB1ezJ2DW\n"+
"hsWp8AkGzsdFzRnXfAylCplxWHgZfi6MU/u6+9N8eaS/KHAE06cCU4kW0Z9TREH5\n"+
"gpMcLN9e8nsHkwJV1yylWuPt3jLLq9u+8qrAg7/9UcA3IFom3jt+nQxAmxBzeafe\n"+
"6YiDf1zLpj+i2A/ijqQnZJb5UUOAYSH2eFelLgvyD4glf5uY/0T3gIrA8pJ1IDS9\n"+
"eh/Rf0CmqsqAioZPtOi5A4sPUfmZuDQ05CYV0OjMqFnWMBzUGd36si6GiFoGbR8w\n"+
"yFR9knVl5Nw0NMlJ9fpxSpE0EIz2J129YrwDST5uyeYWZyt8qlPcjwQq/+G4HTUu\n"+
"FSGbUZwyNIty12xZh5OMl0JzhZ9IP9IjkRX5J1xGubrFQPdVOqxm8TO+748cWzRq\n"+
"cmvOdWqnV9zXfbzbpUDZgtB3Aay72v4KPmxobLujjx6yX3SkTT0aBRBTrn0avEC1\n"+
"Semozx8PsbwXl78tQkCjZxFdNLcM8eIrFYXGB43Avp3puB7w82C+QRG9EzjYxDnY\n"+
"sLP1oZtgMTckr6a6vOWUFbxwHt3WsXQekIYRAyHACmAqYs/j3rZrhcQj66P5WmKY\n"+
"im2b66Vvbl55NHFG9SJ8sxyPQ+i04zNlFXaxSU3KupTUoJRCK2+J0n2S6iD7fs3+\n"+
"EaNSu5pBbEEEZY9jLtH8zj4Z5RYOJMWKa3iUoO87S0cqwroPvxBKFArc+oAsA7Un\n"+
"hmKL1xVrGTTdUBy6jMrYgPXrz7CgPAOoyp87OSpFA/k3yvLiTtivjxnP0ocEgcN3\n"+
"felsV0MNRYyhFXNjM8KWIyOm96LX6932lnUI9s8wquZLMJ7T2CbntzBZQzqOsDk+\n"+
"zYefc4zsgZ5X4H/tYIvUyNMhygLrOleRTb+MhG3KqesXOjlmFtc8fuvC6lxruC3/\n"+
"yzeozNZTROB6WNBP2ywRtVWXyPBQ4R95IuAdiVDw2hl0Fc1MSVGvEMoKLyQJVsv/\n"+
"CjVjk4dqnyl0bhFvgJt4bfoRfp5cG0EISoErJZSkafogG0nVnKh976cSs8M6h8f6\n"+
"OHzVmuRRX7QRm25G8X+q4NihPDgR4IRSZtEQVIPZRMEz8w6uAAtgU5c/r5iv9AZv\n"+
"7dGAcSeeHXpYJ4XNg/Lx7S8dJhkDIsHp8G9zjgnjn0DBqriPtL4zbgOZpAS7pgaW\n"+
"v0IEvNQxDq19YzJcOrhiozATMMw//WIvaJulNEZaQu7IwP7fdy/ivwTwS6Xb7w9I\n"+
"3rT+AKd5xzMHif6ZWbSpvUF8gIwPgocjJ+JbDE3pWwCBPDbTIKwBdxX8benUV8qF\n"+
"WcBE/irIFdYzyMLDFsVypUDDxeGpigZ35bFOizJNyeO6CZ2AcASkSIYoN7fuO7Nb\n"+
"Cm7DePyUn30I6732DA9hLHZOZWhov+ggwJzk7hRx/R3FMA6lcNVGY2fN4zjlSApq\n"+
"wloeBtiSXrqzPJBhWQBdfFWSPXcJBMn9LAlgw3DXSNNK\n"+
"-----END ENCRYPTED PRIVATE KEY-----"; 
    
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.privateKeyContents_0").value(privateKey).store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperClient.boxConnector.localBox.privateKeyPass").value("test123").store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperTest.box.mock.publicKey").value(publicKey).store();
    new GrouperDbConfig().configFileName("grouper.client.properties").propertyName("grouperTest.box.mock.configId").value("localBox").store();
    
  }
  
  /**
   * 
   * @param boxProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(BoxProvisionerTestConfigInput boxProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!boxProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + boxProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  public static void configureBoxProvisioner(BoxProvisionerTestConfigInput provisioningTestConfigInput) {

    
    configureProvisionerSuffix(provisioningTestConfigInput, "boxExternalSystemConfigId", "localBox");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "login");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.insert", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.update", "false");
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }

}
