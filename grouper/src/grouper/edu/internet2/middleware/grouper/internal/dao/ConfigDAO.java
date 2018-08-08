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

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;

/** 
 * Basic <code>Config</code> DAO interface.
 * @author  chris hyzer.
 * @since   2.4
 */
public interface ConfigDAO extends GrouperDAO {
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the config
   */
  public GrouperConfigHibernate findById(String id, boolean exceptionIfNotFound);

  /**
   * save the object to the database
   * @param config
   */
  public void saveOrUpdate(GrouperConfigHibernate config);

  /**
   * delete the object from the database
   * @param config
   */
  public void delete(GrouperConfigHibernate config);

  /**
   * find all config
   * @param configFileName optional, if filtering by config file name
   * @param changedAfterDate optional, if only want configs changed after a certain date
   * @param configKey optional, if only want a certain config key
   * @return the configs
   */
  public Set<GrouperConfigHibernate> findAll(ConfigFileName configFileName, Timestamp changedAfterDate, String configKey);

} 

