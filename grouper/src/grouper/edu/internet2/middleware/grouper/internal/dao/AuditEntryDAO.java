/*
 * @author mchyzer
 * $Id: AuditEntryDAO.java,v 1.4 2009-06-28 19:02:17 mchyzer Exp $
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
   * @param exceptionIfNotFound
   * @return the entry or null if not there
   */
  public AuditEntry findById(String id, boolean exceptionIfNotFound);
  
  /**
   * save the update properties which are auto saved when business method is called
   * @param auditEntry
   */
  public void saveUpdateProperties(AuditEntry auditEntry);

}
