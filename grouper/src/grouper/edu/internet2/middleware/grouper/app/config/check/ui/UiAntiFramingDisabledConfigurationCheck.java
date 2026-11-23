package edu.internet2.middleware.grouper.app.config.check.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;

/**
 * SECURITY (GRP-7380, complements GRP-7577): grouperUi.antiFraming.enabled=false stops
 * GrouperUiAntiFramingFilter from sending Content-Security-Policy frame-ancestors and
 * X-Frame-Options, so another web site can load the UI in a frame and trick a user into clicking
 * (clickjacking).  This is only safe when something in front of Grouper sends those headers (WARNING).
 */
public class UiAntiFramingDisabledConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about (grouper-ui.properties)
   */
  public static final String PROPERTY_NAME = "grouperUi.antiFraming.enabled";

  @Override
  public String getName() {
    return "uiAntiFramingDisabled";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    // same read and default as GrouperUiAntiFramingFilter
    if (!GrouperUiConfigInApi.retrieveConfig().propertyValueBoolean(PROPERTY_NAME, true)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_UI_PROPERTIES, PROPERTY_NAME, "false",
          "grouperUi.antiFraming.enabled is false, so the UI does not send anti-framing headers "
              + "(Content-Security-Policy frame-ancestors, X-Frame-Options).  Another web site could load the "
              + "UI in a frame and trick a user into clicking (clickjacking).",
          "Set grouperUi.antiFraming.enabled=true (the default), unless a proxy or load balancer in front of "
              + "Grouper already sends these headers.  To embed the UI in a portal, list it in "
              + "grouperUi.antiFraming.frameAncestors instead."));
    }

    return results;
  }

}
