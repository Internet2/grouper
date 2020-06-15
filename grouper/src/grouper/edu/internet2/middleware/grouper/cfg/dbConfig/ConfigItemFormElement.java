package edu.internet2.middleware.grouper.cfg.dbConfig;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum ConfigItemFormElement {

  TEXT, TEXTAREA, PASSWORD, DROPDOWN, CHECKBOX;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigItemFormElement valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ConfigItemFormElement.class, 
        string, exceptionOnNull);

  }

}
