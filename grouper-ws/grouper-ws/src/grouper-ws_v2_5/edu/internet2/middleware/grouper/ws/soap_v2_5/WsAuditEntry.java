/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_5;

/**
 * @author vsachdeva
 *
 */
public class WsAuditEntry {
  
  private String actionName;
  
  private String auditCategory;
  
  private String timestamp;
  
  private WsAuditEntryColumn[] auditEntryColumns;

  public String getActionName() {
    return actionName;
  }

  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  public String getAuditCategory() {
    return auditCategory;
  }

  public void setAuditCategory(String auditCategory) {
    this.auditCategory = auditCategory;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public WsAuditEntryColumn[] getAuditEntryColumns() {
    return auditEntryColumns;
  }

  public void setAuditEntryColumns(WsAuditEntryColumn[] auditEntryColumns) {
    this.auditEntryColumns = auditEntryColumns;
  }
  

}
