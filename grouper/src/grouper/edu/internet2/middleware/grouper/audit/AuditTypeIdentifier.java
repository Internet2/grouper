/*
 * @author mchyzer
 * $Id: AuditTypeIdentifier.java,v 1.3 2009-02-09 21:36:43 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;


/**
 * interface which could be on the enum, or class, or whatever.
 * generally you will use the enum AuditTypeBuiltin
 */
public interface AuditTypeIdentifier {

  /**
   * get the audit category
   * @return the id
   */
  public String getAuditCategory();

  /**
   * get the action name of the audit type
   * @return the name
   */
  public String getActionName();
  
}
