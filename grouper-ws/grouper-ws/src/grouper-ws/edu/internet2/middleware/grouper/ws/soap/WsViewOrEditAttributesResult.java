/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

/**
 * Result of one subject having priveleges updated. The number of subjects will
 * equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsViewOrEditAttributesResult {

  /** groupUuid for this group */
  private String groupUuid;

  /** group name for this group */
  private String groupName;

  /** array of attributes */
  private WsAttribute[] attributes;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public enum WsViewOrEditAttributesResultCode {

    /** invalid request */
    INVALID_QUERY,

    /** successful addition */
    SUCCESS,

    /** cant find attribute */
    ATTRIBUTE_NOT_FOUND,

    /** problem with addigion */
    EXCEPTION,

    /** problem with addigion */
    GROUP_NOT_FOUND,

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES;

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /**
   * assign the code from the enum
   * 
   * @param viewOrEditAttributesResultCode code to assign
   */
  public void assignResultCode(
      WsViewOrEditAttributesResultCode viewOrEditAttributesResultCode) {
    this.getResultMetadata().assignResultCode(
        viewOrEditAttributesResultCode == null ? null : viewOrEditAttributesResultCode
            .name());
    this.getResultMetadata().assignSuccess(
        viewOrEditAttributesResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * groupUuid for this group
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }

  /**
   * groupUuid for this group
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }

  /**
   * group name for this group
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * groupUuid for this group
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * array of attributes
   * @return the attributes
   */
  public WsAttribute[] getAttributes() {
    return this.attributes;
  }

  /**
   * @param attributes1 the attributes to set
   */
  public void setAttributes(WsAttribute[] attributes1) {
    this.attributes = attributes1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }
}
