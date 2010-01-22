/**
 * @author mchyzer
 * $Id: AttributeAssignDelegatable.java,v 1.1 2009-10-10 18:02:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * if whoever has an assignment can grant to someone else
 */
public enum AttributeAssignDelegatable {

  /** true that whoever has this assignment can delegate to someone else */
  TRUE {

    @Override
    public boolean delegatable() {
      return true;
    }
  },
  
  /** false, whoever has this assignment cannot delegate to someone else */
  FALSE {

    @Override
    public boolean delegatable() {
      return false;
    }
  },
  
  /** true, whoever has this assignment can delegate to someone else, and can make that assignment delegatable or grant */
  GRANT {

    @Override
    public boolean delegatable() {
      return true;
    }
  };

  /**
   * if this enum is delegatable
   * @return if delegatable
   */
  public abstract boolean delegatable();
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignDelegatable valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignDelegatable.class, 
        string, exceptionOnNull);
  }

}
