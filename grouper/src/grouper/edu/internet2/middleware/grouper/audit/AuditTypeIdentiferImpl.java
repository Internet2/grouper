/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.audit;


/**
 *
 */
public class AuditTypeIdentiferImpl implements AuditTypeIdentifier {

  /** action name */
  private String actionName;

  /** audit category */
  private String auditCategory;
  
  /**
   * @param actionName1
   * @param auditCategory1
   */
  public AuditTypeIdentiferImpl(String actionName1, String auditCategory1) {
    super();
    this.actionName = actionName1;
    this.auditCategory = auditCategory1;
  }

  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getActionName()
   */
  public String getActionName() {
    return this.actionName;
  }

  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getAuditCategory()
   */
  public String getAuditCategory() {
    return this.auditCategory;
  }

}
