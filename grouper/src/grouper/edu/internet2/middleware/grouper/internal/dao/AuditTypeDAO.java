/*
 * @author mchyzer
 * $Id: AuditTypeDAO.java,v 1.2 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.audit.AuditType;


/**
 * audit type data access methods
 */
public interface AuditTypeDAO extends GrouperDAO {
  
  /**
   * find all audit types
   * @return all audit types
   */
  public Set<AuditType> findAll();
  
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
