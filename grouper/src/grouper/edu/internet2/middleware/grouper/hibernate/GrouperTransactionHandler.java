/**
 * Copyright 2014 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Use this class to make your anonymous inner class for 
 * transactions with grouper (if transactions are supported by your DAO strategy
 * configured in grouper.properties)
 * 
 * 
 * @author mchyzer
 *
 */
public interface GrouperTransactionHandler {
  
  /**
   * This method will be called with the grouper transaction object to do 
   * what you wish.  Note, RuntimeExceptions can be
   * thrown by this method... others should be handled somehow.
   * @param grouperTransaction is the grouper transaction
   * @return the return value to be passed to return value of callback method
   * @throws GrouperDAOException if there is a problem, or runtime ones
   */
  public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException;

}
