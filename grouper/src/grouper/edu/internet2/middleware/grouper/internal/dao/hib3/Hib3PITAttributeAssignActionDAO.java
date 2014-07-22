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

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignActionDAO extends Hib3DAO implements PITAttributeAssignActionDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignActionDAO.class.getName();

  /**
   *  @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignAction)
   */
  public void saveOrUpdate(PITAttributeAssignAction pitAttributeAssignAction) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignAction);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeAssignAction> pitAttributeAssignActions) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignActions);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignAction)
   */
  public void delete(PITAttributeAssignAction pitAttributeAssignAction) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignAction);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignAction where sourceId not in (select action.id from AttributeAssignAction as action)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeAssignAction findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignAction pitAttributeAssignAction = HibernateSession
      .byHqlStatic()
      .createQuery("select action from PITAttributeAssignAction as action where action.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignAction.class);
    
    if (pitAttributeAssignAction == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeAssignAction with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignAction;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeAssignAction findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignAction pitAttributeAssignAction = HibernateSession
      .byHqlStatic()
      .createQuery("select action from PITAttributeAssignAction as action where action.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignAction.class);
    
    if (pitAttributeAssignAction == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignAction with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignAction;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITAttributeAssignAction> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeAssignAction> pitAttributeAssignAction = HibernateSession
      .byHqlStatic()
      .createQuery("select action from PITAttributeAssignAction as action where action.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITAttributeAssignAction.class);
    
    if (pitAttributeAssignAction.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignAction with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignAction;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeAssignAction findById(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignAction pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignAction as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignAction.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignAction with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssignAction where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findByPITAttributeDefId(java.lang.String)
   */
  public Set<PITAttributeAssignAction> findByPITAttributeDefId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select action from PITAttributeAssignAction as action where action.attributeDefId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITAttributeDefId")
        .setString("id", id)
        .listSet(PITAttributeAssignAction.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findMissingActivePITAttributeAssignActions()
   */
  public Set<AttributeAssignAction> findMissingActivePITAttributeAssignActions() {

    Set<AttributeAssignAction> actions = HibernateSession
      .byHqlStatic()
      .createQuery("select a from AttributeAssignAction a where " +
          "not exists (select 1 from PITAttributeAssignAction pit where a.id = pit.sourceId and (a.nameDb = pit.nameDb or (a.nameDb is null and pit.nameDb is null))) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = a.id " +
          "    and type.actionName='addAttributeAssignAction' and type.changeLogCategory='attributeAssignAction' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = a.id " +
          "    and type.actionName='updateAttributeAssignAction' and type.changeLogCategory='attributeAssignAction' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeAssignActions")
      .listSet(AttributeAssignAction.class);
    
    return actions;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findMissingInactivePITAttributeAssignActions()
   */
  public Set<PITAttributeAssignAction> findMissingInactivePITAttributeAssignActions() {

    Set<PITAttributeAssignAction> actions = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignAction pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeAssignAction a where a.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeAssignAction' and type.changeLogCategory='attributeAssignAction' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssignActions")
      .listSet(PITAttributeAssignAction.class);
    
    return actions;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITAttributeAssignAction where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssignAction where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}
