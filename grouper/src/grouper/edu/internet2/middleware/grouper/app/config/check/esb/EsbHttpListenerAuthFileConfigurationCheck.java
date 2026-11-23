package edu.internet2.middleware.grouper.app.config.check.esb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * SECURITY (GRP-7380, complements GRP-7577): the experimental ESB HTTP listener fails closed.  When
 * esb.listeners.http.enable=true, EsbHttpServer only starts if esb.listeners.http.authConfigFile is
 * set and loads as a Jetty HashUserRealm file; otherwise it logs an error and does not start.  This
 * check flags an enabled listener whose auth file is blank or is not a readable file (ERROR).
 */
public class EsbHttpListenerAuthFileConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * switch that starts the ESB HTTP listener (grouper-loader.properties)
   */
  public static final String ENABLE_PROPERTY_NAME = "esb.listeners.http.enable";

  /**
   * Jetty HashUserRealm file the listener authenticates callers against (grouper-loader.properties)
   */
  public static final String AUTH_CONFIG_FILE_PROPERTY_NAME = "esb.listeners.http.authConfigFile";

  @Override
  public String getName() {
    return "esbHttpListenerAuthFile";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    // listener off (the default): nothing to check
    if (!grouperLoaderConfig.propertyValueBoolean(ENABLE_PROPERTY_NAME, false)) {
      return results;
    }

    String authConfigFile = grouperLoaderConfig.propertyValueString(AUTH_CONFIG_FILE_PROPERTY_NAME, "");

    // same rule as EsbHttpServer.buildHandler: blank means the listener does not start
    if (StringUtils.isBlank(authConfigFile)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_LOADER_PROPERTIES, AUTH_CONFIG_FILE_PROPERTY_NAME, authConfigFile,
          "esb.listeners.http.enable is true but esb.listeners.http.authConfigFile is blank.  The ESB "
              + "HTTP listener no longer runs without authentication, so it does not start.",
          "Create a Jetty HashUserRealm file (lines like 'username: password,grouper') listing only trusted "
              + "systems, set esb.listeners.http.authConfigFile to its path, and have callers send Basic auth.  "
              + "Or set esb.listeners.http.enable=false."));
      return results;
    }

    // Jetty also accepts a URL here; only check things that are plain paths (or file: URLs), since
    // a jar: or http: resource cannot be checked from the file system
    String path = authConfigFile.trim();
    if (path.startsWith("file:")) {
      path = path.substring("file:".length());
    } else if (path.matches("^[a-zA-Z][a-zA-Z0-9+.-]+:.*$")) {
      // other URL scheme (note a one letter prefix like C: is a windows drive, which is a path)
      return results;
    }

    File file = new File(path);
    if (!file.isFile() || !file.canRead()) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_LOADER_PROPERTIES, AUTH_CONFIG_FILE_PROPERTY_NAME, authConfigFile,
          "esb.listeners.http.authConfigFile is '" + authConfigFile + "', which is not a readable file on "
              + "the server that ran this review.  The ESB HTTP listener does not start when its auth file "
              + "does not load.",
          "Make sure esb.listeners.http.authConfigFile points at a readable Jetty HashUserRealm file on the "
              + "daemon server (if the daemon runs in another container, check the path there), or set "
              + "esb.listeners.http.enable=false."));
    }

    return results;
  }

}
