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
  
  /**
   * audit entry id
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
   * array of audit entry columns
   */
  private WsAuditEntryColumn[] auditEntryColumns;

  /**
   * @return audit action name
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
   * @return audit category
   */
  public String getAuditCategory() {
    return this.auditCategory;
  }

  /**
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

  /**
   * @param timestamp1
   */
  public void setTimestamp(String timestamp1) {
    this.timestamp = timestamp1;
  }

  /**
   * @return array of audit entry columns
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

}
