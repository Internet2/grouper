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
 * SECURITY (GRP-7580): grouper.messaging.system.&lt;name&gt;.hostnameVerification = false
 * (grouper.client.properties, read by RabbitMQConnectionFactoryImpl) turns off the check that the
 * RabbitMQ broker certificate names the host, for a TLS connection with a configured trust store.
 * A certificate for another host signed by a trusted CA is then accepted (WARNING).  Only flagged
 * when the flag applies: TLS on (tlsVersion set) and a trust store configured.
 */
public class MessagingHostnameVerificationOffConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * prefix of messaging system config (grouper.client.properties)
   */
  public static final String MESSAGING_SYSTEM_PREFIX = "grouper.messaging.system.";

  /**
   * hostnameVerification keys of messaging systems, group 1 is the system name
   */
  private static final Pattern HOSTNAME_VERIFICATION_PATTERN = Pattern.compile(
      "^grouper\\.messaging\\.system\\.([^.]+)\\.hostnameVerification$");

  @Override
  public String getName() {
    return "messagingHostnameVerificationOff";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperClientConfig grouperClientConfig = GrouperClientConfig.retrieveConfig();

    Map<String, String> properties = grouperClientConfig.propertiesMap(HOSTNAME_VERIFICATION_PATTERN);

    for (String propertyName : properties.keySet()) {

      String value = properties.get(propertyName);

      // same parse and default as RabbitMQConnectionFactoryImpl (blank means true)
      if (GrouperUtil.booleanValue(value, true)) {
        continue;
      }

      Matcher matcher = HOSTNAME_VERIFICATION_PATTERN.matcher(propertyName);
      matcher.matches();
      String messagingSystemName = matcher.group(1);

      // the flag only applies to TLS with a configured trust store (see RabbitMQConnectionFactoryImpl.configureTls)
      String tlsVersion = grouperClientConfig.propertyValueString(MESSAGING_SYSTEM_PREFIX + messagingSystemName + ".tlsVersion");
      String pathToTrustStore = grouperClientConfig.propertyValueString(
          MESSAGING_SYSTEM_PREFIX + messagingSystemName + ".pathToTrustStore");
      String trustPassphrase = grouperClientConfig.propertyValueString(
          MESSAGING_SYSTEM_PREFIX + messagingSystemName + ".trustPassphrase");
      if (StringUtils.isBlank(tlsVersion) || StringUtils.isBlank(pathToTrustStore) || StringUtils.isBlank(trustPassphrase)) {
        continue;
      }

      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_CLIENT_PROPERTIES, propertyName, value,
          propertyName + " is false, so the TLS connection to messaging system '" + messagingSystemName
              + "' does not check that the broker certificate names the broker host.  A certificate for "
              + "another host, signed by a CA in the trust store, is accepted.",
          "Use a broker certificate that names the host Grouper connects to, then set " + propertyName
              + "=true (the default)."));
    }

    return results;
  }

}
