/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * config item metadata
 */
public enum ConfigItemMetadataType {
  
  /** true/false */
  BOOLEAN {

    @Override
    public String getStringForUi() {
      return "boolean (true or false)";
    }
  }, 
  
  /** any string */
  STRING {

    @Override
    public String getStringForUi() {
      return "text";
    }
  }, 

  /** group name in system */
  GROUP {

    @Override
    public String getStringForUi() {
      return "group";
    }
  }, 

  /** folder in system */
  STEM {

    @Override
    public String getStringForUi() {
      return "folder";
    }
  }, 
  
  /** name of attribute def in system */
  ATTRIBUTEDEF {

    @Override
    public String getStringForUi() {
      return "attribute definition";
    }
  }, 

  /** name of attribute def name in system */
  ATTRIBUTEDEFNAME {

    @Override
    public String getStringForUi() {
      return "attribute definition name";
    }
  }, 

  /** subject id or identifier in system */
  SUBJECT {

    @Override
    public String getStringForUi() {
      return "entity (e.g. subject)";
    }
  }, 
  
  /** any integer or long datatype */
  INTEGER {

    @Override
    public String getStringForUi() {
      return "integer";
    }
  }, 
  
  /** floating point number in system */
  FLOATING {

    @Override
    public String getStringForUi() {
      return "decimal";
    }
  }, 
  
  /** password, or encrypted, or file name, or script */
  PASSWORD {

    @Override
    public String getStringForUi() {
      return "password";
    }
  }, 
  
  /** fully qualified class in system */
  CLASS {

    @Override
    public String getStringForUi() {
      return "Java class";
    }
  };
  
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigItemMetadataType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ConfigItemMetadataType.class, 
        string, exceptionOnNull);

  }
  
  /**
   * 
   */
  public abstract String getStringForUi();
  
}
