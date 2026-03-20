package edu.internet2.middleware.grouper.app.datadog;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Settings for Datadog provisioner, passed to API command methods.
 */
public class DatadogSettings {

  /**
   * set of user emails to ignore (filter from retrieve, do not delete/insert/update)
   */
  private Set<String> ignoreUserEmails = new LinkedHashSet<String>();

  /**
   * set of role names to ignore (filter from retrieve, do not delete)
   */
  private Set<String> ignoreRoles = new LinkedHashSet<String>();

  /**
   * @return set of user emails to ignore
   */
  public Set<String> getIgnoreUserEmails() {
    return ignoreUserEmails;
  }

  /**
   * @param ignoreUserEmails
   */
  public void setIgnoreUserEmails(Set<String> ignoreUserEmails) {
    this.ignoreUserEmails = ignoreUserEmails;
  }

  /**
   * @return set of role names to ignore
   */
  public Set<String> getIgnoreRoles() {
    return ignoreRoles;
  }

  /**
   * @param ignoreRoles
   */
  public void setIgnoreRoles(Set<String> ignoreRoles) {
    this.ignoreRoles = ignoreRoles;
  }

  /**
   * Check if a user email should be ignored.
   * Matches against email or handle (which is typically the same as email in Datadog).
   * @param email the email to check
   * @return true if the email should be ignored
   */
  public boolean isIgnoredUserEmail(String email) {
    if (StringUtils.isBlank(email)) {
      return false;
    }
    return ignoreUserEmails.contains(email.toLowerCase());
  }

  /**
   * Check if a role name should be ignored.
   * @param roleName the role name to check
   * @return true if the role should be ignored
   */
  public boolean isIgnoredRole(String roleName) {
    if (StringUtils.isBlank(roleName)) {
      return false;
    }
    return ignoreRoles.contains(roleName.toLowerCase());
  }

  /**
   * Throw an exception if the email is in the ignore list.
   * Used for create/update/delete/retrieveByEmail operations where
   * operating on an ignored user indicates something is wrong.
   * @param email the email to check
   */
  public void assertNotIgnoredUserEmail(String email) {
    if (isIgnoredUserEmail(email)) {
      throw new RuntimeException("User email '" + email + "' is in the datadogIgnoreUserEmails list and should not be created/updated/deleted/looked up");
    }
  }

  /**
   * Load settings from the Datadog provisioner configuration.
   * @param datadogConfiguration the provisioner configuration
   */
  public void loadFromDatadogProvisionerConfiguration(DatadogProvisionerConfiguration datadogConfiguration) {
    this.ignoreUserEmails = parseCommaSeparatedOrNewlineSet(datadogConfiguration.getDatadogIgnoreUserEmails());
    this.ignoreRoles = parseCommaSeparatedOrNewlineSet(datadogConfiguration.getDatadogIgnoreRoles());
  }

  /**
   * Parse a comma separated or newline separated string into a set of lowercase trimmed values.
   * Ignores blank entries.
   * @param input the input string
   * @return set of lowercase trimmed values
   */
  private static Set<String> parseCommaSeparatedOrNewlineSet(String input) {
    Set<String> result = new LinkedHashSet<String>();
    if (StringUtils.isBlank(input)) {
      return result;
    }
    // split on comma or newline
    String[] parts = input.split("[,\\n\\r]+");
    for (String part : parts) {
      String trimmed = part.trim();
      if (StringUtils.isNotBlank(trimmed)) {
        result.add(trimmed.toLowerCase());
      }
    }
    return result;
  }

}
