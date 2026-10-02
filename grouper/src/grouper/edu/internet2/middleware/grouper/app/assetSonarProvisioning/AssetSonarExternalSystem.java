package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;

/**
 * External system for AssetSonar (EZO IT asset inventory).
 *
 * <p>AssetSonar's two APIs use different credentials and different schemes, so (like the Azure
 * connector) one external system holds several named fields rather than one bearer token:</p>
 * <ul>
 *   <li>the native REST API takes a company token in a plain {@code token} request header --
 *       NOT a bearer token</li>
 *   <li>SCIM 2.0 takes a separate bearer connector key (optional; only login-capable members are
 *       visible over SCIM, so the provisioner is built on REST)</li>
 * </ul>
 *
 * <p>The access tier ids are configuration, never code: tiers are per-tenant role records, and
 * while Administrator and Staff User were small built-ins (1 and 2) in the tenant studied, Agent
 * was 1734.</p>
 *
 * Config (grouper-loader.properties), prefix grouper.assetSonarConnector.{configId}. :
 *   baseUrl, apiToken, scimConnectorKey, staffUserRoleId, agentRoleId, administratorRoleId, enabled
 */
public class AssetSonarExternalSystem extends GrouperExternalSystem {

  /** property prefix (before the config id) */
  public static final String PROPERTY_PREFIX = "grouper.assetSonarConnector.";

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return PROPERTY_PREFIX + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.assetSonarConnector)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myAssetSonar";
  }

  /**
   * Validate the configuration, then prove the API both answers AND understands the inactive
   * filter. A 200 from this API does not mean the request was understood: unknown query
   * parameters are silently ignored and return unfiltered page 1, so the test checks content.
   */
  @Override
  public List<String> test() throws UnsupportedOperationException {

    List<String> ret = new ArrayList<String>();
    String configId = this.getConfigId();

    for (String suffix : new String[] {"baseUrl", "apiToken", "staffUserRoleId"}) {
      if (StringUtils.isBlank(retrieveConfigValue(configId, suffix, false))) {
        ret.add("Undefined or blank property: " + PROPERTY_PREFIX + configId + "." + suffix);
      }
    }

    // role ids are integers addressing per-tenant role records
    for (String suffix : new String[] {"staffUserRoleId", "agentRoleId", "administratorRoleId"}) {
      String value = retrieveConfigValue(configId, suffix, false);
      if (!StringUtils.isBlank(value) && !value.trim().matches("^\\d+$")) {
        ret.add("Property " + PROPERTY_PREFIX + configId + "." + suffix + " must be a numeric role id: '" + value + "'");
      }
    }

    // baseUrl is not checked for a path: a proxy (or the test mock) can legitimately add one, and a
    // wrong url fails the members.api call below with a clearer error anyway

    if (ret.size() > 0) {
      return ret;
    }

    try {
      ret.addAll(AssetSonarApiCommands.testConnection(configId));
    } catch (Exception e) {
      ret.add(logAndDescribeTestException("Unable to connect to AssetSonar", e));
    }

    return ret;
  }

  /**
   * Read a config value for an AssetSonar external system. Sensitive values may be stored
   * encrypted or in a file, so they are run through Morph.
   * @param configId external system config id
   * @param suffix property suffix after grouper.assetSonarConnector.{configId}.
   * @param required true to throw if blank
   * @return the value, trimmed, or null
   */
  public static String retrieveConfigValue(String configId, String suffix, boolean required) {
    String propertyName = PROPERTY_PREFIX + configId + "." + suffix;
    String value = required
        ? GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(propertyName)
        : GrouperLoaderConfig.retrieveConfig().propertyValueString(propertyName);
    if (StringUtils.equals(suffix, "apiToken") || StringUtils.equals(suffix, "scimConnectorKey")) {
      value = Morph.decryptIfFile(value);
    }
    return StringUtils.trimToNull(value);
  }

  /**
   * @param configId external system config id
   * @return the tenant root url with no trailing slash
   */
  public static String retrieveBaseUrl(String configId) {
    return GrouperUtil.stripLastSlashIfExists(retrieveConfigValue(configId, "baseUrl", true));
  }

}
