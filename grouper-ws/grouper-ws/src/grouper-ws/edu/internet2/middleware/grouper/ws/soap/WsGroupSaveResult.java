/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one group being saved.  The number of
 * these result objects will equal the number of groups sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsGroupSaveResult {

  /** group that was saved */
  private String groupName;

  /** group uuid that was saved */
  private String groupUuid;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public enum WsGroupSaveResultCode {

    /** successful addition (success: T) */
    SUCCESS,

    /** invalid query, can only happen if Lite query (success: F) */
    INVALID_QUERY,

    /** the group was not found (success: F) */
    GROUP_NOT_FOUND,

    /** the stem was not found (success: F) */
    STEM_NOT_FOUND,

    /** problem with saving (success: F) */
    EXCEPTION,

    /** user not allowed (success: F) */
    INSUFFICIENT_PRIVILEGES;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /**
   * assign the code from the enum
   * @param groupSaveResultCode
   */
  public void assignResultCode(WsGroupSaveResultCode groupSaveResultCode) {
    this.getResultMetadata().assignResultCode(
        groupSaveResultCode == null ? null : groupSaveResultCode.name());
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(groupSaveResultCode.isSuccess()));
  }

  /**
   * group that was saved
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * group that was saved
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * group uuid that was saved
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }

  /**
   * group uuid that was saved
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }
}
