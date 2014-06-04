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

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefDAO extends Hib3DAO implements PITAttributeDefDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDef)
   */
  public void saveOrUpdate(PITAttributeDef pitAttributeDef) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDef);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeDef> pitAttributeDefs) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefs);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDef)
   */
  public void delete(PITAttributeDef pitAttributeDef) {
    HibernateSession.byObjectStatic().delete(pitAttributeDef);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeDef where sourceId not in (select a.id from AttributeDef as a)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeDef findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeDef pitAttributeDef = HibernateSession
      .byHqlStatic()
      .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeDef.class);
    
    if (pitAttributeDef == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeDef with sourceId=" + id + " not found");
    }
    
    return pitAttributeDef;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeDef findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeDef pitAttributeDef = HibernateSession
      .byHqlStatic()
      .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeDef.class);
    
    if (pitAttributeDef == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDef with sourceId=" + id + " not found");
    }
    
    return pitAttributeDef;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITAttributeDef> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeDef> pitAttributeDefs = HibernateSession
      .byHqlStatic()
      .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITAttributeDef.class);
    
    if (pitAttributeDefs.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDef with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefs;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeDef findById(String id, boolean exceptionIfNotFound) {
    PITAttributeDef pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDef as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeDef.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDef with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDef where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findByName(java.lang.String, boolean)
   */
  public Set<PITAttributeDef> findByName(String name, boolean orderByStartTime) {
    String sql = "select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITAttributeDef> pitAttributeDefs = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", name)
      .listSet(PITAttributeDef.class);
    
    return pitAttributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findByPITStemId(java.lang.String)
   */
  public Set<PITAttributeDef> findByPITStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.stemId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITStemId")
        .setString("id", id)
        .listSet(PITAttributeDef.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findMissingActivePITAttributeDefs()
   */
  public Set<AttributeDef> findMissingActivePITAttributeDefs() {

    Set<AttributeDef> attrs = HibernateSession
      .byHqlStatic()
      .createQuery("select def from AttributeDef def where " +
          "not exists (select 1 from PITAttributeDef pitAttributeDef, PITStem pitStem where pitAttributeDef.stemId = pitStem.id " +
          "            and def.id = pitAttributeDef.sourceId and def.nameDb = pitAttributeDef.nameDb and def.stemId = pitStem.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = def.id " +
          "    and type.actionName='addAttributeDef' and type.changeLogCategory='attributeDef' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = def.id " +
          "    and type.actionName='updateAttributeDef' and type.changeLogCategory='attributeDef' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeDefs")
      .listSet(AttributeDef.class);
    
    return attrs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findMissingInactivePITAttributeDefs()
   */
  public Set<PITAttributeDef> findMissingInactivePITAttributeDefs() {

    Set<PITAttributeDef> attrs = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDef pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeDef def where def.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeDef' and type.changeLogCategory='attributeDef' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeDefs")
      .listSet(PITAttributeDef.class);
    
    return attrs;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITAttributeDef where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDef where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

