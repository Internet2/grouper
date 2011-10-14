/**
 * 
 */
package edu.internet2.middleware.grouper.group;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 * @author mchyzer
 */
public enum TypeOfGroup {
  
  /** group (normal group of subjects) */
  group {

    /**
     * @see TypeOfGroup#supportsField(Field)
     */
    @Override
    public boolean supportsField(Field field) {
      return true;
    }
  },
  
  /** can be assigned groups or other subjects, and also privileges */
  role {

    /**
     * @see TypeOfGroup#supportsField(Field)
     */
    @Override
    public boolean supportsField(Field field) {
      return true;
    }
  },
   
  /** can be used as a subject which is not in a subject source, e.g. a service principal, schema, server, etc */
  entity {

    /**
     * @see TypeOfGroup#supportsField(Field)
     */
    @Override
    public boolean supportsField(Field field) {
      //only access privileges, admins or viewers
      return field.getType() == FieldType.ACCESS && (StringUtils.equals(Field.FIELD_NAME_ADMINS, field.getName()) || StringUtils.equals(Field.FIELD_NAME_VIEWERS, field.getName()));
    }
  };
  
  /**
   * if this type of group supports this field (i.e. entities dont have READ privilege or members list)
   * @param field
   * @return true if supports
   */
  public abstract boolean supportsField(Field field);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static TypeOfGroup valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(TypeOfGroup.class, 
        string, exceptionOnNull);

  }

}
