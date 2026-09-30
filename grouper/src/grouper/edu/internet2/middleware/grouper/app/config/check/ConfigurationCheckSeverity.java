package edu.internet2.middleware.grouper.app.config.check;

/**
 * Severity of a configuration review finding.  Errors are configuration that is broken or actively
 * unsafe (e.g. a gate pointing at a group that does not exist); warnings are configuration that is
 * valid but left at an insecure or not-recommended posture (e.g. a recommended gate left blank).
 */
public enum ConfigurationCheckSeverity {

  /**
   * configuration that is broken or actively unsafe and should be corrected
   */
  ERROR,

  /**
   * configuration that is valid but not at the recommended/safer posture
   */
  WARNING;

}
