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

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITStemDAO;
import edu.internet2.middleware.grouper.pit.PITStem;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITStemDAO extends Hib3DAO implements PITStemDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITStemDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void saveOrUpdate(PITStem pitStem) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitStem);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITStem> pitStems) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitStems);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#delete(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void delete(PITStem pitStem) {
    HibernateSession.byObjectStatic().delete(pitStem);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys    
    hibernateSession.byHql().createQuery("update PITStem set parentStemId = null where sourceId not in (select s.uuid from Stem as s)").executeUpdate();

    hibernateSession.byHql().createQuery("delete from PITStem where sourceId not in (select s.uuid from Stem as s)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITStem findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITStem pitStem = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITStem.class);
    
    if (pitStem == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITStem with sourceId=" + id + " not found");
    }
    
    return pitStem;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITStem findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITStem pitStem = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITStem.class);
    
    if (pitStem == null && exceptionIfNotFound) {
      throw new RuntimeException("PITStem with sourceId=" + id + " not found");
    }
    
    return pitStem;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITStem> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITStem> pitStems = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITStem.class);
    
    if (pitStems.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITStem with sourceId=" + id + " not found");
    }
    
    return pitStems;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findById(java.lang.String, boolean)
   */
  public PITStem findById(String id, boolean exceptionIfNotFound) {
    PITStem pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITStem as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITStem.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITStem with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys    
    HibernateSession.byHqlStatic()
      .createQuery("update PITStem set parentStemId = null where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
      
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITStem where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findByParentPITStemId(java.lang.String)
   */
  public Set<PITStem> findByParentPITStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitStem from PITStem as pitStem where pitStem.parentStemId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByParentPITStemId")
        .setString("id", id)
        .listSet(PITStem.class);
  }

  public Set<PITStem> findByName(String stemName, boolean orderByStartTime) {
    String sql = "select pitStem from PITStem as pitStem where pitStem.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITStem> pitStems = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", stemName)
      .listSet(PITStem.class);
    
    return pitStems;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findMissingActivePITStems()
   */
  public Set<Stem> findMissingActivePITStems() {

    Set<Stem> stems = HibernateSession
      .byHqlStatic()
      .createQuery("select s from Stem s where " +
          "not exists (select 1 from PITStem pitStem, PITStem pitParentStem where pitStem.parentStemId = pitParentStem.id" +
          "            and s.uuid = pitStem.sourceId and s.nameDb = pitStem.nameDb and s.parentUuid = pitParentStem.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = s.uuid " +
          "    and type.actionName='addStem' and type.changeLogCategory='stem' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = s.uuid " +
          "    and type.actionName='updateStem' and type.changeLogCategory='stem' and type.id=temp.changeLogTypeId) " +
          "order by s.nameDb")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITStems")
      .listSet(Stem.class);
    
    return stems;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findMissingInactivePITStems()
   */
  public Set<PITStem> findMissingInactivePITStems() {

    Set<PITStem> stems = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITStem pit where activeDb = 'T' and " +
          "not exists (select 1 from Stem s where s.uuid = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteStem' and type.changeLogCategory='stem' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITStems")
      .listSet(PITStem.class);
    
    return stems;
  }
}

