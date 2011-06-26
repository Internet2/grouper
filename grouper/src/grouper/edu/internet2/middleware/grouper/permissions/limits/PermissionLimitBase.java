/**
 * 
 */
package edu.internet2.middleware.grouper.permissions.limits;


/**
 * @author mchyzer
 *
 */
public abstract class PermissionLimitBase implements PermissionLimitInterface {

  /**
   * @see PermissionLimitInterface#cacheLimitValueResultMinutes()
   */
  public int cacheLimitValueResultMinutes() {
    return 1;
  }

}
