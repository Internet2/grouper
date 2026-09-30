package edu.internet2.middleware.grouper.app.config.check;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;

/**
 * A single finding produced by a {@link GrouperConfigurationCheck}.  This is a plain value object so
 * it can be consumed by the UI, web services, or GSH.  The message and recommendation are already
 * resolved to English by the check that produced them.
 */
public class ConfigurationCheckResult {

  /**
   * value shown instead of the real value when the property is marked sensitive in its metadata
   */
  public static final String REDACTED_VALUE = "********";

  /**
   * how serious this finding is
   */
  private ConfigurationCheckSeverity severity;

  /**
   * stable name of the check that produced this finding (see {@link GrouperConfigurationCheck#getName()})
   */
  private String checkName;

  /**
   * config file the offending property lives in, or null if this finding is not tied to one property
   */
  private ConfigFileName configFileName;

  /**
   * property key this finding is about, or null if this finding is not tied to one property
   */
  private String propertyName;

  /**
   * current (effective) value of the property, redacted if the property is sensitive, or null
   */
  private String currentValue;

  /**
   * human readable description of the problem (English)
   */
  private String message;

  /**
   * human readable recommended action to fix the problem (English)
   */
  private String recommendation;

  /**
   *
   */
  public ConfigurationCheckResult() {
  }

  /**
   * @param severity1 how serious this finding is
   * @param checkName1 stable name of the check that produced this finding
   * @param configFileName1 config file the offending property lives in, or null
   * @param propertyName1 property key this finding is about, or null
   * @param currentValue1 current value of the property (redacted here if sensitive), or null
   * @param message1 human readable description of the problem (English)
   * @param recommendation1 human readable recommended action (English)
   */
  public ConfigurationCheckResult(ConfigurationCheckSeverity severity1, String checkName1,
      ConfigFileName configFileName1, String propertyName1, String currentValue1, String message1,
      String recommendation1) {
    this.severity = severity1;
    this.checkName = checkName1;
    this.configFileName = configFileName1;
    this.propertyName = propertyName1;
    this.currentValue = redactValueIfSensitive(configFileName1, propertyName1, currentValue1);
    this.message = message1;
    this.recommendation = recommendation1;
  }

  /**
   * if the property is marked sensitive in its metadata, do not expose its value
   * @param configFileName1 the config file the property lives in, or null to search all files
   * @param propertyName1 the property key
   * @param value the raw value
   * @return the value, or {@link #REDACTED_VALUE} if the property is sensitive
   */
  public static String redactValueIfSensitive(ConfigFileName configFileName1, String propertyName1, String value) {
    if (StringUtils.isBlank(value) || StringUtils.isBlank(propertyName1)) {
      return value;
    }
    ConfigItemMetadata configItemMetadata = configFileName1 == null
        ? ConfigFileName.findConfigItemMetdata(propertyName1)
        : configFileName1.findConfigItemMetdataFromConfig(propertyName1);
    if (configItemMetadata != null && configItemMetadata.isSensitive()) {
      return REDACTED_VALUE;
    }
    return value;
  }

  /**
   * @return how serious this finding is
   */
  public ConfigurationCheckSeverity getSeverity() {
    return this.severity;
  }

  /**
   * @param severity1 how serious this finding is
   */
  public void setSeverity(ConfigurationCheckSeverity severity1) {
    this.severity = severity1;
  }

  /**
   * @return stable name of the check that produced this finding
   */
  public String getCheckName() {
    return this.checkName;
  }

  /**
   * @param checkName1 stable name of the check that produced this finding
   */
  public void setCheckName(String checkName1) {
    this.checkName = checkName1;
  }

  /**
   * @return config file the offending property lives in, or null
   */
  public ConfigFileName getConfigFileName() {
    return this.configFileName;
  }

  /**
   * @param configFileName1 config file the offending property lives in, or null
   */
  public void setConfigFileName(ConfigFileName configFileName1) {
    this.configFileName = configFileName1;
  }

  /**
   * @return property key this finding is about, or null
   */
  public String getPropertyName() {
    return this.propertyName;
  }

  /**
   * @param propertyName1 property key this finding is about, or null
   */
  public void setPropertyName(String propertyName1) {
    this.propertyName = propertyName1;
  }

  /**
   * @return current value of the property, redacted if sensitive, or null
   */
  public String getCurrentValue() {
    return this.currentValue;
  }

  /**
   * @param currentValue1 current value of the property, or null
   */
  public void setCurrentValue(String currentValue1) {
    this.currentValue = currentValue1;
  }

  /**
   * @return human readable description of the problem (English)
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * @param message1 human readable description of the problem (English)
   */
  public void setMessage(String message1) {
    this.message = message1;
  }

  /**
   * @return human readable recommended action (English)
   */
  public String getRecommendation() {
    return this.recommendation;
  }

  /**
   * @param recommendation1 human readable recommended action (English)
   */
  public void setRecommendation(String recommendation1) {
    this.recommendation = recommendation1;
  }

  @Override
  public String toString() {
    return this.severity + ": [" + this.checkName + "] " + this.message
        + (StringUtils.isBlank(this.recommendation) ? "" : "  Recommendation: " + this.recommendation);
  }

}
