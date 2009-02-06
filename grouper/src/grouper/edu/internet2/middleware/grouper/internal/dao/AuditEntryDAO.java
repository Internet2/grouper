/*
 * @author mchyzer
 * $Id: AuditEntryDAO.java,v 1.2 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.audit.AuditEntry;


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
