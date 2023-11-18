package edu.internet2.middleware.grouper.app.membershipRequire;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * codes in the database
 * @author mchyzer
 *
 */
public enum MembershipRequireEngineEnum {

  /**
   * originated from hook
   */
  hook("H"),
  
  /**
   * originated from change log consumer
   */
  changeLogConsumer("C"),
  
  /**
   * originated from full sync
   */
  fullSync("F");

  /**
   * code in the DB table
   */
  private String dbCode;

  /**
   * code in the DB table
   * @return db code
   */
  public String getDbCode() {
    return this.dbCode;
  }

  /**
   * constructs
   * @param theDbCode
   */
  private MembershipRequireEngineEnum(String theDbCode) {
    this.dbCode = theDbCode;
  }
  
  /**
   * 
   * @param engine
   * @param exceptionOnNull
   * @return field type
   */
  public static MembershipRequireEngineEnum valueOfIgnoreCase(String engine, boolean exceptionOnNull) {

    MembershipRequireEngineEnum engineEnum = null;
    
    if (StringUtils.isBlank(engine)) {
      if (!exceptionOnNull) {
        return null;
      }
      throw new RuntimeException("Cant find membershipRequireEngineEnum: " + engine);
    }
    
    try {
      engineEnum = GrouperUtil.enumValueOfIgnoreCase(MembershipRequireEngineEnum.class, 
          engine, false);
    } catch (Exception e) {
      //ignore this
    }
    
    if (engineEnum != null) {
      return engineEnum;
    }
    
    for (MembershipRequireEngineEnum membershipRequireEngineEnum : MembershipRequireEngineEnum.values()) {
      if (StringUtils.equalsIgnoreCase(engine, membershipRequireEngineEnum.getDbCode())) {
        return membershipRequireEngineEnum;
      }
    }
      
    throw new RuntimeException("Cant find membershipRequireEngineEnum: " + engine);
  }

}
