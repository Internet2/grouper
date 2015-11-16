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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.MessageDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.messaging.GrouperMessageHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Data Access Object for messaging index
 * @author  mchyzer
 * @version $Id$
 */
public class Hib3MessageDAO extends Hib3DAO implements MessageDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3MessageDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    hibernateSession.byHql().createQuery("delete from GrouperMessageHibernate").executeUpdate();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MessageDAO#findById(java.lang.String, boolean)
   */
  public GrouperMessageHibernate findById(String id, boolean exceptionIfNotFound) {
    GrouperMessageHibernate message = HibernateSession.byHqlStatic()
      .createQuery("from GrouperMessageHibernate where id = :theId")
      .setString("theId", id).uniqueResult(GrouperMessageHibernate.class);
    
    if (message == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find message by id: " + id);
    }
    
    return message;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MessageDAO#findByFromMemberId(java.lang.String)
   */
  public Set<GrouperMessageHibernate> findByFromMemberId(String fromMemberId) {
    return HibernateSession.byHqlStatic()
      .createQuery("from GrouperMessageHibernate where fromMemberId = :theFromMemberId")
      .setString("theFromMemberId", fromMemberId).listSet(GrouperMessageHibernate.class);
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MessageDAO#findByQueue(String, int)
   */
  public List<GrouperMessageHibernate> findByQueue(String queue, int pageSize) {
    List<GrouperMessageHibernate> messages = HibernateSession.byHqlStatic()
      .createQuery("from GrouperMessageHibernate gmh "
          + " where gmh.queueName = :theQueueName and gmh.state = 'IN_QUEUE' "
          + " order by gmh.sentTimeMicros, gmh.id ")
      .setString("theQueueName", queue).options(QueryOptions.create(null, null, 1, pageSize))
      .list(GrouperMessageHibernate.class);
    return messages;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MessageDAO#saveOrUpdate(GrouperMessageHibernate)
   */
  public void saveOrUpdate(GrouperMessageHibernate grouperMessageHibernate) {
    HibernateSession.byObjectStatic().saveOrUpdate(grouperMessageHibernate);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MessageDAO#delete(GrouperMessageHibernate)
   */
  public void delete(final GrouperMessageHibernate grouperMessageHibernate) {
    HibernateSession.byObjectStatic().delete(grouperMessageHibernate);
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3MessageDAO.class);


}
