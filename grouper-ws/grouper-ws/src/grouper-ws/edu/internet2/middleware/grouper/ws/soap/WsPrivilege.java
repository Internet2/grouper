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
public class WsPrivilege {

  /** subject lookup for the privilege */
  private WsSubjectLookup subjectLookup;

  /** group lookup for the privilege */
  private WsGroupLookup wsGroupLookup;

  /** T or F as to whether admin privilege is allowed */
  private String adminAllowed;

  /** T or F as to whether optin privilege is allowed */
  private String optinAllowed;

  /** T or F as to whether optout privilege is allowed */
  private String optoutAllowed;

  /** T or F as to whether read privilege is allowed */
  private String readAllowed;

  /** T or F as to whether system privilege is allowed */
  private String viewAllowed;

  /** T or F as to whether update privilege is allowed */
  private String updateAllowed;

  /**
   * T or F as to whether admin privilege is allowed
   * 
   * @return the adminAllowed
   */
  public String getAdminAllowed() {
    return this.adminAllowed;
  }

  /**
   * T or F as to whether admin privilege is allowed
   * 
   * @param adminAllowed1
   *            the adminAllowed to set
   */
  public void setAdminAllowed(String adminAllowed1) {
    this.adminAllowed = adminAllowed1;
  }

  /**
   * T or F as to whether optin privilege is allowed
   * 
   * @return the optinAllowed
   */
  public String getOptinAllowed() {
    return this.optinAllowed;
  }

  /**
   * T or F as to whether optin privilege is allowed
   * 
   * @param optinAllowed1
   *            the optinAllowed to set
   */
  public void setOptinAllowed(String optinAllowed1) {
    this.optinAllowed = optinAllowed1;
  }

  /**
   * T or F as to whether optout privilege is allowed
   * 
   * @return the optoutAllowed
   */
  public String getOptoutAllowed() {
    return this.optoutAllowed;
  }

  /**
   * T or F as to whether optout privilege is allowed
   * 
   * @param optoutAllowed1
   *            the optoutAllowed to set
   */
  public void setOptoutAllowed(String optoutAllowed1) {
    this.optoutAllowed = optoutAllowed1;
  }

  /**
   * T or F as to whether read privilege is allowed
   * 
   * @return the readAllowed
   */
  public String getReadAllowed() {
    return this.readAllowed;
  }

  /**
   * T or F as to whether read privilege is allowed
   * 
   * @param readAllowed1
   *            the readAllowed to set
   */
  public void setReadAllowed(String readAllowed1) {
    this.readAllowed = readAllowed1;
  }

  /**
   * T or F as to whether system privilege is allowed
   * 
   * @return the systemAllowed
   */
  public String getViewAllowed() {
    return this.viewAllowed;
  }

  /**
   * T or F as to whether system privilege is allowed
   * 
   * @param systemAllowed1
   *            the systemAllowed to set
   */
  public void setViewAllowed(String systemAllowed1) {
    this.viewAllowed = systemAllowed1;
  }

  /**
   * T or F as to whether update privilege is allowed
   * 
   * @return the updateAllowed
   */
  public String getUpdateAllowed() {
    return this.updateAllowed;
  }

  /**
   * T or F as to whether update privilege is allowed
   * 
   * @param updateAllowed1
   *            the updateAllowed to set
   */
  public void setUpdateAllowed(String updateAllowed1) {
    this.updateAllowed = updateAllowed1;
  }

  /**
   * subject lookup for the privilege
   * @return the subjectLookup
   */
  public WsSubjectLookup getSubjectLookup() {
    return this.subjectLookup;
  }

  /**
   * subject lookup for the privilege
   * @param subjectLookup1 the subjectLookup to set
   */
  public void setSubjectLookup(WsSubjectLookup subjectLookup1) {
    this.subjectLookup = subjectLookup1;
  }

  /**
   * group lookup for the privilege
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * subject lookup for the privilege
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }
}
