/*
 * @author mchyzer
 * $Id: AuditTypeIdentifier.java,v 1.2 2009-02-08 21:30:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;


/**
 * interface which could be on the enum, or class, or whatever.
 * generally you will use the enum AuditTypeBuiltin
 */
public interface AuditTypeIdentifier {

  /**
   * get the id of the group type
   * @return the id
   */
  public String getId();
  
}
