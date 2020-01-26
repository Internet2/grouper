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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperPasswordDAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Data Access Object for grouper password
 * @author  vsachdeva
 * @version $Id$
 */
public class Hib3GrouperPasswordDAO extends Hib3DAO implements GrouperPasswordDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3GrouperPasswordDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    hibernateSession.byHql().createQuery("delete from GrouperPassword").executeUpdate();
    
  }

  /**
   * 
   * @param username
   * @param application
   * @return
   */
  public GrouperPassword findByUsernameApplication(String username, String application) {
    GrouperPassword grouperPassword = HibernateSession.byHqlStatic()
      .createQuery("from GrouperPassword where username = :username and application = :application")
      .setString("username", username)
      .setString("application", application)
      .uniqueResult(GrouperPassword.class);
    
    return grouperPassword;
  }


 /**
  * 
  * @param grouperPassword
  */
  public void saveOrUpdate(GrouperPassword grouperPassword) {
    HibernateSession.byObjectStatic().saveOrUpdate(grouperPassword);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ConfigDAO#delete(GrouperConfigHibernate)
   */
  public void delete(final GrouperPassword grouperPassword) {
    HibernateSession.byObjectStatic().delete(grouperPassword);
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3GrouperPasswordDAO.class);

}
