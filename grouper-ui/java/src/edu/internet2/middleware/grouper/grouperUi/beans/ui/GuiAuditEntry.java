/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.audit.AuditEntry;


/**
 * @author mchyzer
 *
 */
public class GuiAuditEntry {

  /**
   * underlying audit entry
   */
  private AuditEntry auditEntry;

  /**
   * underlying audit entry
   * @return audit
   */
  public AuditEntry getAuditEntry() {
    return this.auditEntry;
  }

  /**
   * underlying audit entry
   * @param auditEntry1
   */
  public void setAuditEntry(AuditEntry auditEntry1) {
    this.auditEntry = auditEntry1;
  }
  
  
  
}
