/*
 * @author mchyzer
 * $Id: AuditTypeIdentifier.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;


/**
 * interface which could be on the enum, or class, or whatever
 */
public interface AuditTypeIdentifier {

  /**
   * get the id of the group type
   * @return the id
   */
  public String getId();
  
}
