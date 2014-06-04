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
 * $Id: ChangeLogConsumerDAO.java,v 1.1 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;

/**
 * change log consumer data access methods
 */
public interface ChangeLogConsumerDAO extends GrouperDAO {
  
  /**
   * find all change log consumers
   * @return all consumers
   */
  public Set<ChangeLogConsumer> findAll();
  
  /**
   * find a change log consumer by name
   * @param name is the consumer name
   * @param exceptionIfNotFound true if exception should be thrown if not found
   * @return the consumer or null or exception
   */
  public ChangeLogConsumer findByName(String name, boolean exceptionIfNotFound);
  
  /** 
   * insert or update an change log entry object 
   * @param changeLogConsumer 
   */
  public void saveOrUpdate(ChangeLogConsumer changeLogConsumer);
  
}
