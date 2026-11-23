package edu.internet2.middleware.grouper.app.config.check.ws;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;

/**
 * SECURITY (GRP-7380, complements GRP-7579): ws.allowJsonWrapper=true turns on the JSONP wrapper
 * (grouperJsonResponseWrapper) for WS REST responses.  Another web site can then read WS responses
 * using credentials a browser has cached.  Off by default (WARNING when on).
 */
public class WsAllowJsonWrapperConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about (grouper-ws.properties)
   */
  public static final String PROPERTY_NAME = "ws.allowJsonWrapper";

  @Override
  public String getName() {
    return "wsAllowJsonWrapper";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    // same read and default as GrouperRestServlet
    if (GrouperWsConfigInApi.retrieveConfig().propertyValueBoolean(PROPERTY_NAME, false)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_WS_PROPERTIES, PROPERTY_NAME, "true",
          "ws.allowJsonWrapper is true, so WS REST responses can be wrapped as JSONP.  Another web site "
              + "could read WS responses using credentials cached in a user's browser.",
          "Set ws.allowJsonWrapper=false (the default) unless a client needs the JSONP wrapper."));
    }

    return results;
  }

}
