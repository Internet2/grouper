/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: AuditTypeDAO.java,v 1.3 2009-05-13 12:15:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;


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
  
  
  
  
  /**
   * @param id 
   * @param auditCategory 
   * @param actionName 
   * @param exceptionIfNull 
   * @return the stem or null
   * @throws GrouperDAOException 
   * @throws GroupNotFoundException 
   * @since   1.6.0
   */
  AuditType findByUuidOrName(String id, String auditCategory, String actionName, boolean exceptionIfNull);

  /**
   * save the update properties which are auto saved when business method is called
   * @param auditType
   */
  public void saveUpdateProperties(AuditType auditType);

}
