/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;

/** 
 * Basic <code>GrouperPassword</code> DAO interface.
 * @author  vsachdeva
 * @since   2.4
 */
public interface GrouperPasswordDAO extends GrouperDAO {
  
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the config
   */
  public GrouperPassword findById(String id, boolean exceptionIfNotFound);
  
  /**
   * 
   * @param username
   * @param application
   * @return
   */
  public GrouperPassword findByUsernameApplication(String username, String application);

  /**
   * save the object to the database
   * @param config
   */
  public void saveOrUpdate(GrouperPassword grouperPassword);

  /**
   * delete the object from the database
   * @param config
   */
  public void delete(GrouperPassword grouperPassword);

} 

