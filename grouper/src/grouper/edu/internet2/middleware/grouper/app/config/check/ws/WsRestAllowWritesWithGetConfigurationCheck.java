package edu.internet2.middleware.grouper.app.config.check.ws;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;

/**
 * SECURITY (GRP-7380, complements GRP-7577): ws.rest.allowWritesWithGet=true lets WS REST operations
 * that change data run when sent with HTTP GET or HEAD.  Links, images on other web sites,
 * prefetchers and crawlers can send a GET with credentials a browser cached.  Off by default
 * (WARNING when on).
 */
public class WsRestAllowWritesWithGetConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about (grouper-ws.properties)
   */
  public static final String PROPERTY_NAME = "ws.rest.allowWritesWithGet";

  @Override
  public String getName() {
    return "wsRestAllowWritesWithGet";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    // same read and default as WsRestHttpMethodValidator
    if (GrouperWsConfigInApi.retrieveConfig().propertyValueBoolean(PROPERTY_NAME, false)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_WS_PROPERTIES, PROPERTY_NAME, "true",
          "ws.rest.allowWritesWithGet is true, so WS REST operations that change data are allowed with "
              + "HTTP GET or HEAD.  A link or image on another web site could make a change with credentials "
              + "cached in a browser.",
          "Change the clients that write with GET to use POST or PUT (the WS log has a WARN naming each one), "
              + "then set ws.rest.allowWritesWithGet=false (the default)."));
    }

    return results;
  }

}
