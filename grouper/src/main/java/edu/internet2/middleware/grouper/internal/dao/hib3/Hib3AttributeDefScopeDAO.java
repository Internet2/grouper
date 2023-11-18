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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.exception.AttributeDefScopeNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Data Access Object for attribute def scope
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefScopeDAO.java,v 1.1 2009-06-29 15:58:24 mchyzer Exp $
 */
public class Hib3AttributeDefScopeDAO extends Hib3DAO implements AttributeDefScopeDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefScopeDAO.class.getName();

  /**
   * reset the attribute def scopes
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDefScope").executeUpdate();
  }

  /**
   * retrieve by id
   * @param id 
   * @param exceptionIfNotFound 
   * @return  attribute def scope
   */
  public AttributeDefScope findById(String id, boolean exceptionIfNotFound) {
    AttributeDefScope attributeDefScope = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefScope where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefScope.class);
    if (attributeDefScope == null && exceptionIfNotFound) {
      throw new AttributeDefScopeNotFoundException("Cant find attribute def scope by id: " + id);
   }

    return attributeDefScope;
  }

  /**
   * save or update
   * @param attributeDefScope 
   */
  public void saveOrUpdate(AttributeDefScope attributeDefScope) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefScope);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO#findByUuidOrKey(java.util.Collection, java.lang.String, java.lang.String, java.lang.String, boolean, String)
   */
  public AttributeDefScope findByUuidOrKey(Collection<String> idsToIgnore, String id, String attributeDefId,
      String attributeDefScopeType, boolean exceptionIfNull, String scopeString) throws GrouperDAOException {
    try {
      Set<AttributeDefScope> attributeDefScopes = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDefScope as theAttributeDefScope where " +
        		"theAttributeDefScope.id = :theId or (theAttributeDefScope.attributeDefId = :theAttributeDefId and " +
        		"theAttributeDefScope.attributeDefScopeTypeDb = :theAttributeDefScopeTypeDb)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theAttributeDefId", attributeDefId)
        .setString("theAttributeDefScopeTypeDb", attributeDefScopeType)
        .listSet(AttributeDefScope.class);
      if (GrouperUtil.length(attributeDefScopes) == 0) {
        if (exceptionIfNull) {
          throw new RuntimeException("Can't find attributeDefScope by id: '" + id + "' or attributeDefId '" + attributeDefId 
              + "', attributeDefScopeType: " + attributeDefScopeType);
        }
        return null;
      }
      
      idsToIgnore = GrouperUtil.nonNull(idsToIgnore);
      
      //lets remove ones we have already processed or will process
      Iterator<AttributeDefScope> iterator = attributeDefScopes.iterator();
      while (iterator.hasNext()) {
        
        AttributeDefScope attributeDefScope = iterator.next();
        if (idsToIgnore.contains(attributeDefScope.getId())) {
          iterator.remove();
        }
      }
      
      //first case, the ID matches
      iterator = attributeDefScopes.iterator();
      while (iterator.hasNext()) {
        
        AttributeDefScope attributeDefScope = iterator.next();
        if (StringUtils.equals(id, attributeDefScope.getId())) {
          return attributeDefScope;
        }
      }

      //second case, the value matches
      iterator = attributeDefScopes.iterator();
      while (iterator.hasNext()) {
        
        AttributeDefScope attributeDefScope = iterator.next();
        if (StringUtils.equals(scopeString, attributeDefScope.getScopeString())) {
          return attributeDefScope;
        }
      }
      
      //ok, if there is one left, return it
      if (attributeDefScopes.size() > 0) {
        return attributeDefScopes.iterator().next();
      }
      
      //cant find one
      return null;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeDefScope by id: '" + id + "' or attributeDefId '" + attributeDefId 
            + "', attributeDefScopeType: " + attributeDefScopeType 
            + "', scopeString: " + scopeString + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.AttributeDefScope)
   */
  public void saveUpdateProperties(AttributeDefScope attributeDefScope) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeDefScope " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeDefScope.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeDefScope.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeDefScope.getLastUpdatedDb())
        .setString("theContextId", attributeDefScope.getContextId())
        .setString("theId", attributeDefScope.getId()).executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO#findByAttributeDefId(java.lang.String, QueryOptions)
   */
  public Set<AttributeDefScope> findByAttributeDefId(String attributeDefId, QueryOptions queryOptions) {
    Set<AttributeDefScope> attributeDefScopes = HibernateSession.byHqlStatic()
      .createQuery("from AttributeDefScope as theAttributeDefScope where " +
          "theAttributeDefScope.attributeDefId = :theAttributeDefId")
      .options(queryOptions)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByUuidOrName")
      .setString("theAttributeDefId", attributeDefId)
      .listSet(AttributeDefScope.class);
    return attributeDefScopes;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO#delete(edu.internet2.middleware.grouper.attr.AttributeDefScope)
   */
  public void delete(AttributeDefScope attributeDefScope) {
    HibernateSession.byObjectStatic().delete(attributeDefScope);
  }

} 


