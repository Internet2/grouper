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
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;

/**
 * @author mchyzer
 * $Id$
 */
public interface TableIndexDAO extends GrouperDAO {
  
  /** 
   * insert or update a tableIndex
   * @param tableIndex 
   */
  public void saveOrUpdate(TableIndex tableIndex);
  
  /** 
   * delete a table index
   * @param tableIndex 
   */
  public void delete(TableIndex tableIndex);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the table index
   */
  public TableIndex findById(String id, boolean exceptionIfNotFound);

  /**
   * find by type
   * @param type
   * @return the table index
   */
  public TableIndex findByType(TableIndexType tableIndexType);

  /**
   * reserve a certain amount of indices
   * @param tableIndexType
   * @param numberOfIndicesToReserve
   * @return the tabe index that was saved (subtract the number of indices to reserve to see the start index
   */
  public TableIndex reserveIds(TableIndexType tableIndexType, int numberOfIndicesToReserve);
}
