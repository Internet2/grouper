package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public enum GrouperProvisioningConfigurationAttributeDbCacheType {

  attribute, subjectTranslationScript, translationScript;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnBlank will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningConfigurationAttributeDbCacheType valueOfIgnoreCase(String string, boolean exceptionOnBlank) {
    if (StringUtils.equalsIgnoreCase(string, "groupAttribute") || StringUtils.equalsIgnoreCase(string, "entityAttribute")) {
      return GrouperProvisioningConfigurationAttributeDbCacheType.attribute;
    }
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningConfigurationAttributeDbCacheType.class, 
        string, exceptionOnBlank);
  }
}
