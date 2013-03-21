package edu.internet2.middleware.grouper.userData;

/**
 * generic object in the user data
 * @author mchyzer
 */
public class UserDataObject {

  /**
   * construct with fields
   * @param uuid
   * @param theTimestamp
   */
  public UserDataObject(String uuid, long theTimestamp) {
    super();
    this.uuid = uuid;
    this.theTimestamp = theTimestamp;
  }

  /**
   * 
   * @param uuid
   */
  public UserDataObject(String uuid) {
    super();
    this.uuid = uuid;
  }

  /**
   * 
   */
  public UserDataObject() {
    super();
  }

  /**
   * uuid of the object
   */
  private String uuid;
  
  /**
   * timestamp that the thing was last favorited or whatever
   */
  private long theTimestamp;


  /**
   * uuid of the object
   * @return
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * uuid of the object
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * timestamp that the thing was last favorited or whatever
   * @return timestamp in millis since 1970
   */
  public long getTheTimestamp() {
    return theTimestamp;
  }

  /**
   * timestamp that the thing was last favorited or whatever
   * @param theTimestamp1 in millis since 1970
   */
  public void setTheTimestamp(long theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }
  
}
