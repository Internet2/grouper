/*
 * @author mchyzer
 * $Id: AuditTypeDAO.java,v 1.1 2009-02-01 22:38:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.AuditType;


/**
 * audit type data access methods
 */
public interface AuditTypeDAO extends GrouperDAO {
  
  /** 
   * insert or update an audit entry object 
   * @param auditType 
   */
  public void saveOrUpdate(AuditType auditType);
  
  /**
   * delete entries and types by category and action
   * @param category
   * @param action
   */
  public void deleteEntriesAndTypesByCategoryAndAction(String category, String action);
  
}
