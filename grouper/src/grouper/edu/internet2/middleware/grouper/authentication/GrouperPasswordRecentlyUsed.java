/**
 * 
 */
package edu.internet2.middleware.grouper.authentication;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * object to store recently used password
 * @author vsachdeva
 *
 */

@SuppressWarnings("serial")
public class GrouperPasswordRecentlyUsed extends GrouperAPI implements Hib3GrouperVersioned {
  
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    
    if (StringUtils.isBlank(this.id)) {
      this.setId(GrouperUuid.getUuid() );
    }
    
    super.onPreSave(hibernateSession);
  }

  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** db grouper password id for this row */
  public static final String COLUMN_GROUPER_PASSWORD_ID = "grouper_password_id";
  
  
  /** db id for this row */
  public static final String COLUMN_JWT_JTI = "jwt_jti";
  
  /** username */
  public static final String COLUMN_JWT_IAT = "jwt_iat";
  
  /** millis since 1970 this password was attempted */
  public static final String COLUMN_ATTEMPT_MILLIS = "attempt_millis";

  /** ip address from where the password was attempted */
  public static final String COLUMN_IP_ADDRESS = "ip_address";
  
  /** status of the attempt. S/F/E etc */
  public static final String COLUMN_STATUS = "status";
  
  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PASSWORD_RECENTLY_USED = "grouper_password_recently_used";
  
  
  private String id;
  
  private String grouperPasswordId;

  private String jwtJti;
  
  private Long jwtIat;

  private Long attemptMillis;
  
  private String ipAddress;
  
  private char status;
  
  
  public String getId() {
    return id;
  }



  
  public void setId(String id) {
    this.id = id;
  }



  
  public String getJwtJti() {
    return jwtJti;
  }



  
  public void setJwtJti(String jwtJti) {
    this.jwtJti = jwtJti;
  }



  
  public Long getJwtIat() {
    return jwtIat;
  }



  
  public void setJwtIat(Long jwtIat) {
    this.jwtIat = jwtIat;
  }



  
  public String getIpAddress() {
    return ipAddress;
  }



  
  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }



  
  
  public char getStatus() {
    return status;
  }




  
  public void setStatus(char status) {
    this.status = status;
  }




  public String getGrouperPasswordId() {
    return grouperPasswordId;
  }




  
  public void setGrouperPasswordId(String grouperPasswordId) {
    this.grouperPasswordId = grouperPasswordId;
  }







  
  public Long getAttemptMillis() {
    return attemptMillis;
  }




  
  public void setAttemptMillis(Long attemptMillis) {
    this.attemptMillis = attemptMillis;
  }




  @Override
  public GrouperAPI clone() {
    // TODO Auto-generated method stub
    return null;
  }

}
