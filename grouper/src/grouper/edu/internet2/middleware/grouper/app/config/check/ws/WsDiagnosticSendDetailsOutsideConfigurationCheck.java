package edu.internet2.middleware.grouper.app.config.check.ws;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * SECURITY (GRP-7380, complements GRP-7579): the status / diagnostic servlet sends details (hostname,
 * version, uptime, diagnostic task names, failure text and stack traces) only to callers on
 * ws.diagnostic.sourceIpAddresses.  ws.diagnostic.sendDetailsOutsideSourceIpAddresses=true (in
 * grouper.properties, read by GrouperStatusServletDetails) also sends them to every other caller,
 * or to everyone when the address list is blank (WARNING).
 */
public class WsDiagnosticSendDetailsOutsideConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about (grouper.properties)
   */
  public static final String PROPERTY_NAME = "ws.diagnostic.sendDetailsOutsideSourceIpAddresses";

  /**
   * the addresses that get details by default (grouper.properties)
   */
  public static final String SOURCE_IP_ADDRESSES_PROPERTY_NAME = "ws.diagnostic.sourceIpAddresses";

  @Override
  public String getName() {
    return "wsDiagnosticSendDetailsOutsideSourceIpAddresses";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    // same read and default as GrouperStatusServletDetails
    if (!grouperConfig.propertyValueBoolean(PROPERTY_NAME, false)) {
      return results;
    }

    boolean addressListBlank = StringUtils.isBlank(grouperConfig.propertyValueString(SOURCE_IP_ADDRESSES_PROPERTY_NAME));

    results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
        ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, "true",
        "ws.diagnostic.sendDetailsOutsideSourceIpAddresses is true, so the status endpoint sends details "
            + "(hostname, version, uptime, diagnostic task names, failure text and stack traces) to "
            + (addressListBlank ? "every caller (ws.diagnostic.sourceIpAddresses is blank)."
                : "callers that are not on ws.diagnostic.sourceIpAddresses."),
        "Set ws.diagnostic.sendDetailsOutsideSourceIpAddresses=false (the default) and list your monitoring "
            + "hosts in ws.diagnostic.sourceIpAddresses."));

    return results;
  }

}
