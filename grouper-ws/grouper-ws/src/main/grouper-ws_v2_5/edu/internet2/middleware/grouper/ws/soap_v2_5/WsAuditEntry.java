/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_5;

/**
 * @author vsachdeva
 *
 */
public class WsAuditEntry {
  
  /**
   * id of the audit entry
   */
  private String id;
  
  /**
   * audit action name
   */
  private String actionName;
  
  /**
   * audit category
   */
  private String auditCategory;
  
  /**
   * timestamp when the audit happened
   */
  private String timestamp;
  
  /**
   * array of audit columns
   */
  private WsAuditEntryColumn[] auditEntryColumns;

  /**
   * @return action name
   */
  public String getActionName() {
    return this.actionName;
  }

  /**
   * @param actionName1
   */
  public void setActionName(String actionName1) {
    this.actionName = actionName1;
  }

  /**
   * 
   * @return audit category
   */
  public String getAuditCategory() {
    return this.auditCategory;
  }

  /**
   * 
   * @param auditCategory1
   */
  public void setAuditCategory(String auditCategory1) {
    this.auditCategory = auditCategory1;
  }

  /**
   * @return timestamp when the audit happened
   */
  public String getTimestamp() {
    return this.timestamp;
  }

  /**
   * 
   * @param timestamp1
   */
  public void setTimestamp(String timestamp1) {
    this.timestamp = timestamp1;
  }

  /**
   * @return array of audit columns
   */
  public WsAuditEntryColumn[] getAuditEntryColumns() {
    return this.auditEntryColumns;
  }

  /**
   * 
   * @param auditEntryColumns1
   */
  public void setAuditEntryColumns(WsAuditEntryColumn[] auditEntryColumns1) {
    this.auditEntryColumns = auditEntryColumns1;
  }

  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }
  

}
