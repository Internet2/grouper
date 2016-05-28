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
/*
 * @author mchyzer
 * $Id: HibUtilsMapping.java,v 1.5 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.internal.SessionFactoryImpl;

import java.io.Serializable;


/**
 * metadata and other methods for hibernate
 */
public class HibUtilsMapping {

  /**
   * see if this object would be an insert for hibernate, or if it would be an update.
   * Note this only works if we dont have assigned keys...
   * @param object
   * @return true if insert
   */
  public static boolean isInsert(Object object) {
//    Session session = hibernateSession.getSession();
//    AbstractSessionImpl abstractSessionImpl = (AbstractSessionImpl)session;
//    EntityEntry entityEntry = abstractSessionImpl.getPersistenceContext().getEntry(object);
//    entityEntry.isExistsInDatabase()
    Serializable id = primaryKeyCurrentValue(object);
    
    if (id == null) {
      return true;
    }
    
    //see if object is hibernate versionable
    if (object instanceof Hib3GrouperVersioned) {
      return ((GrouperAPI)object).getHibernateVersionNumber() < 0;
    }
    
    //if null, then see if it is an assigned key
    IdentifierGenerator generator = ((SessionFactoryImpl)Hib3DAO.getSessionFactory()).getIdentifierGenerator(object.getClass().getName());

    if (generator instanceof UUIDHexGenerator) {
      return false;
    }
    
    //then how do we know???
    throw new RuntimeException("Cant tell if insert if assigned key! " + GrouperUtil.className(object) + ", " + generator);
  }
  
  /**
   * get the hibernate primary key property value (current value)
   * @param object
   * @return the value
   */
  public static Serializable primaryKeyCurrentValue(Object object) {
    Class clazz = object.getClass();
    clazz = GrouperUtil.unenhanceClass(clazz);
    String propertyName = Hib3DAO.getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName();
    Serializable value = (Serializable)GrouperUtil.propertyValue(object, propertyName);
    return value;
  }
  
  
}
