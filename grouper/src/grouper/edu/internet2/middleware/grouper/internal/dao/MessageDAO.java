/**
 * Copyright 2015 Internet2
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

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.messaging.GrouperMessageHibernate;

/** 
 * Basic <code>Message</code> DAO interface.
 * @author  chris hyzer.
 * @version $Id: MemberDAO.java,v 1.11 2009-12-28 06:08:37 mchyzer Exp $
 * @since   2.3
 */
public interface MessageDAO extends GrouperDAO {
  
  /**
   * find a list by the from member id
   * @param fromMemberId
   * @return the set
   */
  public Set<GrouperMessageHibernate> findByFromMemberId(String fromMemberId);

  /**
   * find messages by queue
   * @param queue
   * @param pageSize
   * @return collection of grouper messages
   */
  public List<GrouperMessageHibernate> findByQueue(String queue, int pageSize);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the message
   */
  public GrouperMessageHibernate findById(String id, boolean exceptionIfNotFound);

  /**
   * save the object to the database
   * @param message
   */
  public void saveOrUpdate(GrouperMessageHibernate message);

  /**
   * delete the object from the database
   * @param message
   */
  public void delete(GrouperMessageHibernate message);


} 

