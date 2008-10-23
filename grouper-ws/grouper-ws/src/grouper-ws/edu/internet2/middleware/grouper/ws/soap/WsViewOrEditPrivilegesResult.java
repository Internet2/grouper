/*
 * @author mchyzer $Id: WsViewOrEditPrivilegesResult.java,v 1.2 2008-10-23 04:49:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap;

/**
 * 
 */
public class WsViewOrEditPrivilegesResult {

  /** group for reault */
  private WsGroup wsGroup;

  /** subject for result */
  private WsSubject wsSubject;

  /**
   * result code of a request
   */
  public enum WsViewOrEditPrivilegesResultCode {

    /** invalid request */
    INVALID_QUERY,

    /** successful addition */
    SUCCESS,

    /** the subject was not found */
    SUBJECT_NOT_FOUND,

    /** problem with addigion */
    EXCEPTION,

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES,

    /** subject duplicate found */
    SUBJECT_DUPLICATE;

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
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /** privilege this result refers to */
  private WsGrouperPrivilegeResult wsPrivilege = null;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * privilege this result refers to
   * @return the wsPrivilege
   */
  public WsGrouperPrivilegeResult getWsPrivilege() {
    return this.wsPrivilege;
  }

  /**
   * privilege this result refers to
   * @param wsPrivilege1 the wsPrivilege to set
   */
  public void setWsPrivilege(WsGrouperPrivilegeResult wsPrivilege1) {
    this.wsPrivilege = wsPrivilege1;
  }

  /**
   * assign the code from the enum
   * 
   * @param viewOrEditPrivilegesResultCode
   */
  public void assignResultCode(
      WsViewOrEditPrivilegesResultCode viewOrEditPrivilegesResultCode) {
    this.getResultMetadata().assignResultCode(
        viewOrEditPrivilegesResultCode == null ? null : viewOrEditPrivilegesResultCode
            .name());
    this.getResultMetadata().assignSuccess(
        viewOrEditPrivilegesResultCode.isSuccess() ? "T" : "F");
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
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsSubject1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

}
