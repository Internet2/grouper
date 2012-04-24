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
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Data Access Object for attribute assign action
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignActionDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeAssignActionDAO extends Hib3DAO implements AttributeAssignActionDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignActionDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssignAction").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findById(java.lang.String, boolean)
   */
  public AttributeAssignAction findById(String id, boolean exceptionIfNotFound) {
    AttributeAssignAction attributeAssignAction = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignAction where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignAction.class);
    if (attributeAssignAction == null && exceptionIfNotFound) {
      throw new AttributeAssignActionNotFoundException("Cant find attribute assign action by id: " + id);
   }

    return attributeAssignAction;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void saveOrUpdate(AttributeAssignAction attributeAssignAction) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignAction);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#delete(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void delete(AttributeAssignAction attributeAssignAction) {
    HibernateSession.byObjectStatic().delete(attributeAssignAction);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findByAttributeDefId(java.lang.String)
   */
  public Set<AttributeAssignAction> findByAttributeDefId(String attributeDefId) {
    Set<AttributeAssignAction> attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssignAction where attributeDefId = :theAttributeDefId order by nameDb")
      .setString("theAttributeDefId", attributeDefId)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByAttributeDefId")
      .listSet(AttributeAssignAction.class);
    
    return attributeAssignActions;
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findByUuidOrKey(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public AttributeAssignAction findByUuidOrKey(String id, String attributeDefId,
      String name, boolean exceptionIfNull) {
    try {
      AttributeAssignAction attributeAssignAction = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssignAction as theAttributeAssignAction " +
        		"where theAttributeAssignAction.id = :theId " +
        		"or (theAttributeAssignAction.attributeDefId = :theAttributeDefId " +
        		"and theAttributeAssignAction.nameDb = :theNameDb)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theAttributeDefId", attributeDefId)
        .setString("theNameDb", name)
        .uniqueResult(AttributeAssignAction.class);
      if (attributeAssignAction == null && exceptionIfNull) {
        throw new RuntimeException("Can't find attributeAssignAction by id: '" 
            + id + "' or attributeDefId: " + attributeDefId + ", name: '" + name + "'");
      }
      return attributeAssignAction;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeAssignAction by id: '" 
            + id + "' or attributeDefId: " + attributeDefId 
            + ", name: '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void saveUpdateProperties(AttributeAssignAction attributeAssignAction) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeAssignAction " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeAssignAction.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeAssignAction.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeAssignAction.getLastUpdatedDb())
        .setString("theContextId", attributeAssignAction.getContextId())
        .setString("theId", attributeAssignAction.getId()).executeUpdate();
    
  }

} 

