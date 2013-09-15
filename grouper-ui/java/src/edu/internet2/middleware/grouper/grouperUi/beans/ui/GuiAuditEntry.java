/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;


/**
 * @author mchyzer
 *
 */
public class GuiAuditEntry {

  /**
   * default constructor
   */
  public GuiAuditEntry() {
    
  }
  
  /**
   * 
   * @param theAuditEntry
   */
  public GuiAuditEntry(AuditEntry theAuditEntry) {
    this.auditEntry = theAuditEntry;
  }

  
  /**
   * 2/1/2013 8:03 AM
   * @return the date for screen
   */
  public String getGuiDate() {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    Locale locale = httpServletRequest.getLocale();
    DateFormat guiDateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm aa", locale);
    return guiDateFormat.format(this.auditEntry.getCreatedOn());
  }
  
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
