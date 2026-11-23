package edu.internet2.middleware.grouper.app.config.check.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * SECURITY (GRP-7380, complements GRP-7580): grouper.messaging.system.&lt;name&gt;.trustAllCertificates
 * = true (grouper.client.properties, read by RabbitMQConnectionFactoryImpl) makes a TLS connection
 * to the RabbitMQ broker accept any certificate when no trust store is configured, with no host
 * name check.  The broker password and messages are then not protected from a man in the middle
 * (WARNING).  When the same system has pathToTrustStore and trustPassphrase set the flag is ignored,
 * so it is not flagged.
 */
public class MessagingTrustAllCertificatesConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * prefix of messaging system config (grouper.client.properties)
   */
  public static final String MESSAGING_SYSTEM_PREFIX = "grouper.messaging.system.";

  /**
   * trustAllCertificates keys of messaging systems, group 1 is the system name
   */
  private static final Pattern TRUST_ALL_CERTIFICATES_PATTERN = Pattern.compile(
      "^grouper\\.messaging\\.system\\.([^.]+)\\.trustAllCertificates$");

  @Override
  public String getName() {
    return "messagingTrustAllCertificates";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperClientConfig grouperClientConfig = GrouperClientConfig.retrieveConfig();

    Map<String, String> trustAllProperties = grouperClientConfig.propertiesMap(TRUST_ALL_CERTIFICATES_PATTERN);

    for (String propertyName : trustAllProperties.keySet()) {

      String value = trustAllProperties.get(propertyName);

      // same parse and default as RabbitMQConnectionFactoryImpl
      if (!GrouperUtil.booleanValue(value, false)) {
        continue;
      }

      Matcher matcher = TRUST_ALL_CERTIFICATES_PATTERN.matcher(propertyName);
      matcher.matches();
      String messagingSystemName = matcher.group(1);

      // a configured trust store wins over trustAllCertificates (see RabbitMQConnectionFactoryImpl.configureTls)
      String pathToTrustStore = grouperClientConfig.propertyValueString(
          MESSAGING_SYSTEM_PREFIX + messagingSystemName + ".pathToTrustStore");
      String trustPassphrase = grouperClientConfig.propertyValueString(
          MESSAGING_SYSTEM_PREFIX + messagingSystemName + ".trustPassphrase");
      if (StringUtils.isNotBlank(pathToTrustStore) && StringUtils.isNotBlank(trustPassphrase)) {
        continue;
      }

      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_CLIENT_PROPERTIES, propertyName, value,
          propertyName + " is true, so a TLS connection to messaging system '" + messagingSystemName
              + "' accepts any broker certificate without a host name check.  The broker password and "
              + "messages are not protected from a man in the middle.",
          "Set " + MESSAGING_SYSTEM_PREFIX + messagingSystemName + ".pathToTrustStore and trustPassphrase "
              + "(or put the broker CA in the JVM trust store) and set " + propertyName + "=false.  Only "
              + "use trustAllCertificates for a test broker."));
    }

    return results;
  }

}
