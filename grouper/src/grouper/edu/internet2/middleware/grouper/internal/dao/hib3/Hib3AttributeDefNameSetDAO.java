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
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSetDelegate;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;

/**
 * Data Access Object for attribute def name set
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameSetDAO.java,v 1.11 2009-11-17 02:52:29 mchyzer Exp $
 */
public class Hib3AttributeDefNameSetDAO extends Hib3DAO implements AttributeDefNameSetDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3AttributeDefNameSetDAO.class.getName();

  /**
   * reset the attribute def scopes
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
      //do this since mysql cant handle self-referential foreign keys
      // restrict this only to mysql since in oracle this might cause unique constraint violations
      hibernateSession.byHql().createQuery("update AttributeDefNameSet set parentAttrDefNameSetId = null").executeUpdate();
    }
    
    hibernateSession.byHql().createQuery("delete from AttributeDefNameSet").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findById(java.lang.String, boolean)
   */
  public AttributeDefNameSet findById(String id, boolean exceptionIfNotFound) throws AttributeDefNameSetNotFoundException {
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefNameSet where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefNameSet.class);
    if (attributeDefNameSet == null && exceptionIfNotFound) {
      throw new AttributeDefNameSetNotFoundException("Cant find attribute def name set by id: " + id);
    }
    return attributeDefNameSet;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDefNameSet)
   */
  public void saveOrUpdate(AttributeDefNameSet attributeDefNameSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameSet);
  }

  /**
   * @see AttributeDefNameSetDAO#findByIfHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSet> findByIfHasAttributeDefNameId(String id) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where ifHasAttributeDefNameId = :theId")
      .setString("theId", id).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;

  }

  /**
   * @see AttributeDefNameSetDAO#findByThenHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSet> findByThenHasAttributeDefNameId(String id) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where thenHasAttributeDefNameId = :theId")
      .setString("theId", id).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findByIfThenHasAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefNameSet> findByIfThenHasAttributeDefNameId(
      String attributeDefNameSetForThens, String attributeDefNameSetForIfs) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefNameSet from AttributeDefNameSet as theAttributeDefNameSet, AttributeDefNameSet as theAttributeDefNameSetThens, "
        + "AttributeDefNameSet as theAttributeDefNameSetIfs "
        + "where theAttributeDefNameSetThens.thenHasAttributeDefNameId = :attributeDefNameSetForThens "
        + "and theAttributeDefNameSetIfs.ifHasAttributeDefNameId = :attributeDefNameSetForIfs "
        + "and theAttributeDefNameSet.ifHasAttributeDefNameId = theAttributeDefNameSetThens.ifHasAttributeDefNameId "
        + "and theAttributeDefNameSet.thenHasAttributeDefNameId = theAttributeDefNameSetIfs.thenHasAttributeDefNameId "
    )
    .setString("attributeDefNameSetForThens", attributeDefNameSetForThens)
    .setString("attributeDefNameSetForIfs", attributeDefNameSetForIfs)
    .listSet(AttributeDefNameSet.class);
  return attributeDefNameSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#delete(edu.internet2.middleware.grouper.attr.AttributeDefNameSet)
   */
  public void delete(final AttributeDefNameSet attributeDefNameSet) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
              //set parent to null so mysql doest get mad
              //http://bugs.mysql.com/bug.php?id=15746
              hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                  "update AttributeDefNameSet set parentAttrDefNameSetId = null where id = :id")
                  .setString("id", attributeDefNameSet.getId()).executeUpdate();
            }
            
            hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDefNameSet);
            return null;
          }
      
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDefNameSet findByIfThenImmediate(String attributeDefNameIdIf,
      String attributeDefNameIdThen, boolean exceptionIfNotFound) throws AttributeDefNameSetNotFoundException {
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where ifHasAttributeDefNameId = :ifId " +
      "and thenHasAttributeDefNameId = :thenId " +
      "and depth = 1")
      .setString("ifId", attributeDefNameIdIf).setString("thenId", attributeDefNameIdThen)
      .uniqueResult(AttributeDefNameSet.class);
    if (attributeDefNameSet == null && exceptionIfNotFound) {
      throw new AttributeDefNameSetNotFoundException("AttributeDefNameSet immediate if "
          + attributeDefNameIdIf + ", then: " + attributeDefNameIdThen);
    }
    return attributeDefNameSet;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#deleteByIfHasAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  public void deleteByIfHasAttributeDefName(final AttributeDefName attributeDefName) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler()  {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            
            Set<AttributeDefNameSet> attributeDefNameSets = findByIfHasAttributeDefNameId(attributeDefName.getId());
            for (AttributeDefNameSet attributeDefNameSet : attributeDefNameSets) {
              if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
                //do this since mysql cant handle self-referential foreign keys
                attributeDefNameSet.setParentAttrDefNameSetId(null);
                attributeDefNameSet.saveOrUpdate();
              }
              
              hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDefNameSet);
            }
              
            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesImpliedByThis(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesImpliedByThis(String attributeDefNameId) {
    return attributeDefNamesImpliedHelper(attributeDefNameId, 
        "where adns.ifHasAttributeDefNameId = :theId and adn.id = adns.thenHasAttributeDefNameId " +
        "and adn.id != :theId");
  }

  /**
   * @param attributeDefNameId 
   * @param whereFragment 
   * @return the attribute def names
   */
  private static Set<AttributeDefName> attributeDefNamesImpliedHelper(String attributeDefNameId, String whereFragment) {
    
    //make sure ok to view
    AttributeDefNameFinder.findById(attributeDefNameId, true);

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    StringBuilder sql = new StringBuilder("select distinct adn from AttributeDefNameSet as adns, AttributeDefName as adn ");
    StringBuilder whereClause = new StringBuilder(whereFragment);
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    //see if we are adding more to the query
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(grouperSession.getSubject(), 
        byHqlStatic,
        sql, whereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES);
  
    sql.append(" ").append(whereClause).append(" order by adn.nameDb");
    
    Set<AttributeDefName> attributeDefNames = byHqlStatic.createQuery(sql.toString())
        .setString("theId", attributeDefNameId).listSet(AttributeDefName.class);
    
    return attributeDefNames;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesImpliedByThisImmediate(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate(String attributeDefNameId) {
    
    return attributeDefNamesImpliedHelper(attributeDefNameId, 
        "where adns.ifHasAttributeDefNameId = :theId and adn.id = adns.thenHasAttributeDefNameId " +
        "and adn.id != :theId and adns.typeDb = 'immediate'");

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesThatImplyThis(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesThatImplyThis(String attributeDefNameId) {
    
    return attributeDefNamesImpliedHelper(attributeDefNameId, 
        "where adns.thenHasAttributeDefNameId = :theId and adn.id = adns.ifHasAttributeDefNameId " +
        "and adn.id != :theId");

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesThatImplyThisImmediate(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate(String attributeDefNameId) {
    return attributeDefNamesImpliedHelper(attributeDefNameId, 
        "where adns.thenHasAttributeDefNameId = :theId and adn.id = adns.ifHasAttributeDefNameId " +
        "and adn.id != :theId and adns.typeDb = 'immediate'");

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findByUuidOrKey(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, boolean)
   */
  public AttributeDefNameSet findByUuidOrKey(String id, String ifHasAttributeDefNameId,
      String thenHasAttributeDefNameId, String parentAttributeDefNameSetId, int depth,
      boolean exceptionIfNull) {
    try {
      AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDefNameSet as theAttributeDefNameSet where theAttributeDefNameSet.id = :theId " +
        		"or (theAttributeDefNameSet.ifHasAttributeDefNameId = :theIfHasAttributeDefNameId " +
            " and theAttributeDefNameSet.thenHasAttributeDefNameId = :theThenHasAttributeDefNameId " +
            "and theAttributeDefNameSet.parentAttrDefNameSetId = :theParentAttributeDefNameSetId " +
            " and theAttributeDefNameSet.depth = :theDepth)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrKey")
        .setString("theId", id)
        .setString("theIfHasAttributeDefNameId", ifHasAttributeDefNameId)
        .setString("theThenHasAttributeDefNameId", thenHasAttributeDefNameId)
        .setString("theParentAttributeDefNameSetId", parentAttributeDefNameSetId)
        .setInteger("theDepth", depth)
        .uniqueResult(AttributeDefNameSet.class);
      if (attributeDefNameSet == null && exceptionIfNull) {
        throw new RuntimeException("Can't find attributeDefNameSet by id: '" + id + "' or ifHasAttributeDefNameId '" + ifHasAttributeDefNameId 
            + "', thenHasAttributeDefNameId: " + thenHasAttributeDefNameId + ", parentAttributeDefNameSetId: " + parentAttributeDefNameSetId + ", depth: " + depth);
      }
      return attributeDefNameSet;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeDefNameSet by id: '" + id + "' or ifHasAttributeDefNameId '" + ifHasAttributeDefNameId 
            + "', thenHasAttributeDefNameId: " + thenHasAttributeDefNameId + ", parentAttributeDefNameSetId: " + parentAttributeDefNameSetId 
            + ", depth: " + depth + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.AttributeDefNameSet)
   */
  public void saveUpdateProperties(AttributeDefNameSet attributeDefNameSet) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeDefNameSet " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdated " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeDefNameSet.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeDefNameSet.getCreatedOnDb())
        .setLong("theLastUpdated", attributeDefNameSet.getLastUpdatedDb())
        .setString("theContextId", attributeDefNameSet.getContextId())
        .setString("theId", attributeDefNameSet.getId()).executeUpdate();
  }

  /**
   * @see AttributeDefNameSetDAO#findByDepthOneForAttributeDef(String)
   */
  public Set<AttributeDefNameSet> findByDepthOneForAttributeDef(String attributeDefId) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
        "select distinct adns from AttributeDefNameSet as adns, AttributeDefName as adn " +
        "where adn.id = adns.ifHasAttributeDefNameId " +
        "and adn.attributeDefId = :theId and adns.depth = 1 ")
        .setString("theId", attributeDefId).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;
  }

} 

