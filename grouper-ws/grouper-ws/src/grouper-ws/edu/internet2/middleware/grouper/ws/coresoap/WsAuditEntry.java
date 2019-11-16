/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author vsachdeva
 *
 */
public class WsAuditEntry {
  
  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
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
