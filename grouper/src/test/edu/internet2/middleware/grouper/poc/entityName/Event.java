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
package edu.internet2.middleware.grouper.poc.entityName;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;


/*
 * @author mchyzer
 * $Id: Event.java,v 1.1 2009-06-01 03:23:25 mchyzer Exp $
 */

/**
 * <pre>
 * make sure to build the tables, e.g. for mysql:
 * 
 * CREATE TABLE `event` (`id` int(11) NOT NULL auto_increment,                  
 * `title` varchar(200) default NULL, PRIMARY KEY  (`id`) )
 * 
 * CREATE TABLE `event2` (`id` int(11) NOT NULL auto_increment,                  
 * `title` varchar(200) default NULL, PRIMARY KEY  (`id`) )
 * 
 * </pre>
 */
public class Event {
  private Long id;

  private String title;

  public Event() {}

  public Event(Long id, String title) {
    this.id = id;
    this.title = title;
  }

  public Event(String title) {
    this.title = title;
  }

  public Long getId() {
      return id;
  }

  private void setId(Long id) {
      this.id = id;
  }

  public String getTitle() {
      return title;
  }

  public void setTitle(String title) {
      this.title = title;
  }
  public static void main(String[] args) {
    
    System.out.println("Should print out: title1 title2");
    
    Properties  p   = GrouperHibernateConfig.retrieveConfig().properties();
    
    //unencrypt pass
    if (p.containsKey("hibernate.connection.password")) {
      String newPass = Morph.decryptIfFile(p.getProperty("hibernate.connection.password"));
      p.setProperty("hibernate.connection.password", newPass);
    }
    
    // And now load all configuration information
    Configuration configuration = new Configuration().addProperties(p);
    
    configuration.addXML(GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/poc/entityName/Event.hbm.xml", false));
    configuration.addXML(GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/poc/entityName/Event2.hbm.xml", false));
    
    SessionFactory sessionFactory = configuration.buildSessionFactory();
    Session session = sessionFactory.openSession();
    Transaction transaction = session.beginTransaction();
    session.createQuery("delete from Event1").executeUpdate();
    session.createQuery("delete from Event2").executeUpdate();
    
    transaction.commit();
    
    transaction.begin();
    Event event = new Event("title1");
    session.save("Event1", event);
    transaction.commit();
    
    transaction.begin();
    Event event2 = new Event("title2");
    session.save("Event2", event2);
    transaction.commit();
    
    event = (Event)session.createQuery("from Event1 where title = 'title1'").uniqueResult();
    System.out.println(event.getTitle());
    
    event2 = (Event)session.createQuery("from Event2 where title = 'title2'").uniqueResult();
    System.out.println(event2.getTitle());
    
    session.close();
  }
}
