package edu.internet2.middleware.grouper.app.provisioning;


public class ProvisioningConfigSyncStats {
  
  /**
   * last full sync timestamp
   */
  private String lastFullSyncTimestamp;
  
  /**
   * last full sync start
   */
  private String lastFullSyncStartTimestamp;
  
  
  /**
   * last incremental sync timestamp
   */
  private String lastIncrementalSyncTimestamp;
  
  /**
   * last full metadata sync start timestamp
   */
  private String lastFullMetadataSyncStartTimestamp;
  
  /**
   * last full metadata sync timestamp
   */
  private String lastFullMetadataSyncTimestamp;
  
  /**
   * group count
   */
  private int groupCount;
  
  /**
   * user count
   */
  private int userCount;
  
  /**
   * records count
   */
  private int membershipCount;

  
  public String getLastFullSyncTimestamp() {
    return lastFullSyncTimestamp;
  }

  
  public void setLastFullSyncTimestamp(String lastFullSyncTimestamp) {
    this.lastFullSyncTimestamp = lastFullSyncTimestamp;
  }

  
  public String getLastFullSyncStartTimestamp() {
    return lastFullSyncStartTimestamp;
  }

  
  public void setLastFullSyncStartTimestamp(String lastFullSyncStartTimestamp) {
    this.lastFullSyncStartTimestamp = lastFullSyncStartTimestamp;
  }

  
  public String getLastIncrementalSyncTimestamp() {
    return lastIncrementalSyncTimestamp;
  }

  
  public void setLastIncrementalSyncTimestamp(String lastIncrementalSyncTimestamp) {
    this.lastIncrementalSyncTimestamp = lastIncrementalSyncTimestamp;
  }

  
  public String getLastFullMetadataSyncStartTimestamp() {
    return lastFullMetadataSyncStartTimestamp;
  }

  
  public void setLastFullMetadataSyncStartTimestamp(
      String lastFullMetadataSyncStartTimestamp) {
    this.lastFullMetadataSyncStartTimestamp = lastFullMetadataSyncStartTimestamp;
  }

  
  public String getLastFullMetadataSyncTimestamp() {
    return lastFullMetadataSyncTimestamp;
  }

  
  public void setLastFullMetadataSyncTimestamp(String lastFullMetadataSyncTimestamp) {
    this.lastFullMetadataSyncTimestamp = lastFullMetadataSyncTimestamp;
  }

  
  public int getGroupCount() {
    return groupCount;
  }

  
  public void setGroupCount(int groupCount) {
    this.groupCount = groupCount;
  }

  
  public int getUserCount() {
    return userCount;
  }

  
  public void setUserCount(int userCount) {
    this.userCount = userCount;
  }

  
  public int getMembershipCount() {
    return membershipCount;
  }

  
  public void setMembershipCount(int membershipCount) {
    this.membershipCount = membershipCount;
  }
  
  

}
