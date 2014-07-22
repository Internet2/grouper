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

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefNameSetDAO extends Hib3DAO implements PITAttributeDefNameSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefNameSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public void saveOrUpdate(PITAttributeDefNameSet pitAttributeDefNameSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefNameSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeDefNameSet> pitAttributeDefNameSets) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefNameSets);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet) {
    HibernateSession.byObjectStatic().delete(pitAttributeDefNameSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeDefNameSet set parentAttrDefNameSetId = null where sourceId not in (select attrDefNameSet.id from AttributeDefNameSet as attrDefNameSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeDefNameSet where sourceId not in (select attrDefNameSet.id from AttributeDefNameSet as attrDefNameSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeDefNameSet findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeDefNameSet pitAttributeDefNameSet = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefNameSet from PITAttributeDefNameSet as attrDefNameSet where attrDefNameSet.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeDefNameSet.class);
    
    if (pitAttributeDefNameSet == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeDefNameSet with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefNameSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeDefNameSet findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeDefNameSet pitAttributeDefNameSet = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefNameSet from PITAttributeDefNameSet as attrDefNameSet where attrDefNameSet.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeDefNameSet.class);
    
    if (pitAttributeDefNameSet == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDefNameSet with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefNameSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeDefNameSet findById(String id, boolean exceptionIfNotFound) {
    PITAttributeDefNameSet pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDefNameSet as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeDefNameSet.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDefNameSet with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys
    HibernateSession.byHqlStatic()
      .createQuery("update PITAttributeDefNameSet set parentAttrDefNameSetId = null where endTimeDb is not null and endTimeDb < :time and parentAttrDefNameSetId is not null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDefNameSet where endTimeDb is not null and endTimeDb < :time and parentAttrDefNameSetId is null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findImmediateChildren(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public Set<PITAttributeDefNameSet> findImmediateChildren(PITAttributeDefNameSet pitAttributeDefNameSet) {

    Set<PITAttributeDefNameSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select adns from PITAttributeDefNameSet as adns where adns.parentAttrDefNameSetId = :parent and adns.depth <> '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildren")
        .setString("parent", pitAttributeDefNameSet.getId())
        .listSet(PITAttributeDefNameSet.class);
    
    return children;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#deleteSelfByPITAttributeDefNameId(java.lang.String)
   */
  public void deleteSelfByPITAttributeDefNameId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //update before delete since mysql cant handle self referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update PITAttributeDefNameSet set parentAttrDefNameSetId = null where ifHasAttributeDefNameId = :id and thenHasAttributeDefNameId = :id and depth = '0'")
              .setString("id", id)
              .executeUpdate();

            Set<PITAttributeDefNameSet> pitAttributeDefNameSetsToDelete = findAllSelfPITAttributeDefNameSetsByPITAttributeDefNameId(id);
            for (PITAttributeDefNameSet rs : pitAttributeDefNameSetsToDelete) {
              delete(rs);
            }

            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findAllSelfPITAttributeDefNameSetsByPITAttributeDefNameId(java.lang.String)
   */
  public Set<PITAttributeDefNameSet> findAllSelfPITAttributeDefNameSetsByPITAttributeDefNameId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select adns from PITAttributeDefNameSet as adns where adns.ifHasAttributeDefNameId = :id and adns.thenHasAttributeDefNameId = :id and adns.depth = '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfPITAttributeDefNameSetsByPITAttributeDefNameId")
        .setString("id", id)
        .listSet(PITAttributeDefNameSet.class);    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findByThenHasPITAttributeDefNameId(java.lang.String)
   */
  public Set<PITAttributeDefNameSet> findByThenHasPITAttributeDefNameId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select adns from PITAttributeDefNameSet as adns where thenHasAttributeDefNameId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByThenHasPITAttributeDefNameId")
        .setString("id", id)
        .listSet(PITAttributeDefNameSet.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findMissingActivePITAttributeDefNameSets()
   */
  public Set<AttributeDefNameSet> findMissingActivePITAttributeDefNameSets() {

    Set<AttributeDefNameSet> attrSets = HibernateSession
      .byHqlStatic()
      .createQuery("select attrSet from AttributeDefNameSet attrSet where " +
          "not exists (select 1 from PITAttributeDefNameSet pit where attrSet.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = attrSet.id " +
          "    and type.actionName='addAttributeDefNameSet' and type.changeLogCategory='attributeDefNameSet' and type.id=temp.changeLogTypeId) " +
          "order by attrSet.depth")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeDefNameSets")
      .listSet(AttributeDefNameSet.class);
    
    return attrSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findMissingInactivePITAttributeDefNameSets()
   */
  public Set<PITAttributeDefNameSet> findMissingInactivePITAttributeDefNameSets() {

    Set<PITAttributeDefNameSet> attrSets = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDefNameSet pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeDefNameSet attrSet where attrSet.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeDefNameSet' and type.changeLogCategory='attributeDefNameSet' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeDefNameSets")
      .listSet(PITAttributeDefNameSet.class);
    
    return attrSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITAttributeDefNameSet where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITAttributeDefNameSet> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeDefNameSet> pitAttributeDefNameSets = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefNameSet from PITAttributeDefNameSet as attrDefNameSet where attrDefNameSet.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITAttributeDefNameSet.class);
    
    if (pitAttributeDefNameSets.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDefNameSet with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefNameSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDefNameSet where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}
