/*
 * @author mchyzer
 * $Id: AuditEntryDAO.java,v 1.3 2009-03-31 06:58:28 mchyzer Exp $
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
  
  /**
   * 
   * @param id
   * @return the entry or null if not there
   */
  public AuditEntry retrieveById(String id);
  
}
