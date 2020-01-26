/**
 * 
 */
package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;

/**
 * object to store recently used password
 * @author vsachdeva
 *
 */

@SuppressWarnings("serial")
public class GrouperPasswordRecentlyUsed extends GrouperAPI implements Hib3GrouperVersioned {

  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** db grouper password id for this row */
  public static final String COLUMN_GROUPER_PASSWORD_ID = "grouper_password_id";
  
  
  /** db id for this row */
  public static final String COLUMN_JWT_JTI = "jwt_jti";
  
  /** username */
  public static final String COLUMN_JWT_IAT = "jwt_iat";
  
  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PASSWORD_RECENTLY_USED = "grouper_password_recently_used";
  
  
  @Override
  public GrouperAPI clone() {
    // TODO Auto-generated method stub
    return null;
  }

}
