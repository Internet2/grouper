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
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogConsumerDAO;

/**
 * Data Access Object for changeLog consumer
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogConsumerDAO.java,v 1.1 2009-06-09 17:24:13 mchyzer Exp $
 */
public class Hib3ChangeLogConsumerDAO extends Hib3DAO implements ChangeLogConsumerDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3ChangeLogConsumerDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogConsumerDAO#saveOrUpdate(edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer)
   */
  public void saveOrUpdate(ChangeLogConsumer changeLogConsumer) {
    HibernateSession.byObjectStatic().saveOrUpdate(changeLogConsumer);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogConsumerDAO#findByName(String, boolean)
   */
  public ChangeLogConsumer findByName(String name, boolean exceptionIfNotFound) {
    
    ChangeLogConsumer changeLogConsumer = HibernateSession.byHqlStatic().createQuery("from ChangeLogConsumer where name = :theName")
      .setString("theName", name).uniqueResult(ChangeLogConsumer.class);
    
    if (changeLogConsumer == null && exceptionIfNotFound) {
      throw new RuntimeException("Couldnt find a change log consumer by name: '" + name + "'");
    }
    
    return changeLogConsumer;
  }
  

  /**
   * reset the changeLog types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from ChangeLogConsumer").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogConsumerDAO#findAll()
   */
  public Set<ChangeLogConsumer> findAll() {
    return HibernateSession.byHqlStatic().createQuery("from ChangeLogConsumer").listSet(ChangeLogConsumer.class);
  }
  
} 

