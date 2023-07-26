package edu.internet2.middleware.grouper.sqlCache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public enum SqlCacheDependencyTypeType {

  group("G"),
  
  dataField("D");
  
  private static Map<String, SqlCacheDependencyTypeType> lookupMap = new HashMap<String, SqlCacheDependencyTypeType>();
  
  private SqlCacheDependencyTypeType(String theChar1) {
    this.theChar = theChar1;    
  }
  
  private String theChar;

  
  public String getTheChar() {
    return theChar;
  }
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static SqlCacheDependencyTypeType valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    if (lookupMap.size() == 0) {
      synchronized (SqlCacheDependencyTypeType.class) {
        
        if (lookupMap.size() == 0) {
          
          for (SqlCacheDependencyTypeType sqlCacheDependencyTypeType : SqlCacheDependencyTypeType.values()) {
            
            lookupMap.put(sqlCacheDependencyTypeType.name().toLowerCase(), sqlCacheDependencyTypeType);
            lookupMap.put(sqlCacheDependencyTypeType.getTheChar().toLowerCase(), sqlCacheDependencyTypeType);
            
          }
        }
      }
    }
    SqlCacheDependencyTypeType sqlCacheDependencyTypeType = lookupMap.get(type.toLowerCase());
    if (sqlCacheDependencyTypeType == null && exceptionOnNull) {
      throw new RuntimeException("Cannot find SqlCacheDependencyTypeType '" + type + "'");
    }
    return sqlCacheDependencyTypeType;
        
  }

}
