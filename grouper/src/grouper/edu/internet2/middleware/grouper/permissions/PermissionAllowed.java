/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;


/**
 * if allowed or disallowed
 */
public enum PermissionAllowed {
  
  /** normal assignment if allowed */
  ALLOWED {

    @Override
    public boolean isDisallowed() {
      return false;
    }
  },
  
  /** disallow underneath an inherited allow to limit the scope of the allow */
  DISALLOWED {

    @Override
    public boolean isDisallowed() {
      return true;
    }
  };
  
  /**
   * if disallowed
   * @return true or false
   */
  public abstract boolean isDisallowed();
  
  /**
   * convert from disallowed boolean to the enum
   * @param disallowed
   * @return the permission allowed
   */
  public static PermissionAllowed fromDisallowedBoolean(boolean disallowed) {
    return disallowed ? DISALLOWED : ALLOWED;
  }
  
}
