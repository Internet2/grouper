package edu.internet2.middleware.grouper.azure;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.GraphApiClient;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import org.apache.log4j.Logger;

public class AzureGrouperExternalSystem extends GrouperExternalSystem {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.azureConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.azureConnector)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myAzure";
  }

  private static final Logger logger = Logger.getLogger(AzureGrouperExternalSystem.class);

  public static final String GROUP_ID_ATTRIBUTE_NAME = "etc:attribute:azure:azure";

  // Leverage the apiConnection for both the changelog consumer and the provisioning
  // system, even though they have different loader property prefixes
  public enum ProvisionerType {CHANGELOG_CONSUMER, EXTERNAL_SYSTEM}

  /**
   * cache connections
   */
  private static ExpirableCache<String, GraphApiClient> apiConnectionCache = new ExpirableCache<String, GraphApiClient>(5);

  public synchronized static GraphApiClient retrieveApiConnectionForChangelogConsumer(String configId) {
    return retrieveApiConnection(configId, ProvisionerType.CHANGELOG_CONSUMER);
  }

  public synchronized static GraphApiClient retrieveApiConnectionForProvisioning(String configId) {
    return retrieveApiConnection(configId, ProvisionerType.EXTERNAL_SYSTEM);
  }

  public synchronized static GraphApiClient retrieveApiConnection(String configId, ProvisionerType provisionerType) {
    GraphApiClient graphApiClient = apiConnectionCache.get(configId);
    if (graphApiClient == null) {
      GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
      String configPrefix;
      switch (provisionerType) {
        case CHANGELOG_CONSUMER:
          configPrefix = "changeLog.consumer." + configId + ".";
          break;
        case EXTERNAL_SYSTEM:
          configPrefix = "grouper.azureConnector." + configId + ".";
          break;
        default:
          configPrefix = "";
      }

      String clientId = config.propertyValueStringRequired(configPrefix + "clientId");
      String clientSecret = config.propertyValueStringRequired(configPrefix + "clientSecret");
      String tenantId = config.propertyValueStringRequired(configPrefix + "tenantId");
      String scope = config.propertyValueString(configPrefix + "scope", "https://graph.microsoft.com/.default");

      AzureGroupType groupType;
      String groupTypeString = config.propertyValueString(configPrefix + "groupType", AzureGroupType.Security.name());
      try {
        groupType = AzureGroupType.valueOf(groupTypeString);
      } catch (IllegalArgumentException e) {
        groupType = AzureGroupType.Security;
        logger.error("Provisioner " + configId + ": Invalid option for property " + configPrefix + "groupType: " + groupTypeString + " - reverting to type " + groupType.name());
      }

      AzureVisibility visibility = null;
      String visibilityString = config.propertyValueString(configPrefix + "visibility");
      if (visibilityString != null) {
        if (groupType == AzureGroupType.Unified) {
          try {
            // discrepancy in graph api -- documented as Hiddenmembership but object returns HiddenMembership.
            // The enum is fixed, but the older loader property was using Hiddenmembership. Map the old value
            // so existing configs don't break
            if ("Hiddenmembership".equals(visibilityString)) {
              visibilityString = "HiddenMembership";
              logger.warn("For " + configPrefix + "visibility, legacy value Hiddenmembership was remapped to HiddenMembership");
            }
            visibility = AzureVisibility.valueOf(visibilityString);
          } catch (IllegalArgumentException e) {
            visibility = AzureVisibility.Public;
            logger.error("Provisioner " + configId + ": Invalid option for property " + configPrefix + "visibility: " + visibilityString + " - reverting to type " + visibility.name());
          }
        } else {
          logger.error("Provisioner " + configId + ": Property " + configPrefix + "visibility is only valid for Unified group type -- ignoring");
        }
      }

      String proxyType = config.propertyValueString(configPrefix + "proxyType");
      String proxyHost;
      Integer proxyPort;
      if (proxyType != null) {
        proxyHost = config.propertyValueStringRequired(configPrefix + "proxyHost");
        proxyPort = config.propertyValueIntRequired(configPrefix + "proxyPort");
      } else {
        proxyHost = null;
        proxyPort = null;
      }

      graphApiClient = new GraphApiClient(clientId, clientSecret, tenantId, scope, groupType, visibility, proxyType, proxyHost, proxyPort);

    }
    return graphApiClient;

  }


}
