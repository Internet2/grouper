/*
 * @author mchyzer
 * $Id: AuditEntryDAO.java,v 1.1 2009-02-01 22:38:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.AuditEntry;


/**
 * audit entry data access methods
 */
public interface AuditEntryDAO extends GrouperDAO {
  
  /** 
   * insert or update an audit entry object 
   * @param auditEntry 
   */
  public void saveOrUpdate(AuditEntry auditEntry);
  
}
