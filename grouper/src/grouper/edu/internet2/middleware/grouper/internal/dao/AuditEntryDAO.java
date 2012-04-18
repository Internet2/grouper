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
