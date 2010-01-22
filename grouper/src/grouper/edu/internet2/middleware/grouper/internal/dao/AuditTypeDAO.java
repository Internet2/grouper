/*
 * @author mchyzer
 * $Id: AuditTypeDAO.java,v 1.3 2009-05-13 12:15:01 mchyzer Exp $
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
   * find all audit types by category
   * @param categoryName 
   * @return all audit types
   */
  public Set<AuditType> findByCategory(String categoryName);
  
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
