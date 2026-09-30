package edu.internet2.middleware.grouper.app.config.check;

import java.util.List;

/**
 * Extension point for the configuration review framework.  A check inspects the effective Grouper
 * configuration (properties files merged with database overrides) and/or the database itself and
 * returns zero or more findings.  One check represents one logical concern (one recommendation
 * topic), which may span several properties or files; a check may return more than one result.
 *
 * Register a new check by adding it to the built-in list in {@link GrouperConfigurationCheckEngine}.
 * Checks live in the core grouper module so they can be unit tested and consumed from the UI, web
 * services, and GSH.
 */
public abstract class GrouperConfigurationCheck {

  /**
   * stable, unique name of this check.  Used for logging, deduplication, and as a machine readable
   * identifier for non-UI consumers.  Not shown to end users (findings carry their own messages).
   * @return the check name
   */
  public abstract String getName();

  /**
   * run this check against the current effective configuration and/or database.
   * @return the findings, or an empty list if nothing is wrong
   */
  public abstract List<ConfigurationCheckResult> checkConfiguration();

}
