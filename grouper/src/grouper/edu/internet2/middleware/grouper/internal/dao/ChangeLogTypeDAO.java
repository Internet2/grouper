/**
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
 */
/*
 * @author mchyzer
 * $Id: ChangeLogTypeDAO.java,v 1.2 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogType;


/**
 * change log type data access methods
 */
public interface ChangeLogTypeDAO extends GrouperDAO {
  
  /**
   * find all change log types
   * @return all change log types
   */
  public Set<ChangeLogType> findAll();
  
  /** 
   * insert or update an change log entry object 
   * @param changeLogType 
   */
  public void saveOrUpdate(ChangeLogType changeLogType);
  
  /**
   * delete entries and types by category and action
   * @param category
   * @param action
   */
  public void deleteEntriesAndTypesByCategoryAndAction(String category, String action);
  
}
