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

  /** group saved */
  private WsGroup wsGroup;
  
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
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }
  
  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  
  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }
}
