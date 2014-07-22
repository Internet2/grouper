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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITFieldDAO;
import edu.internet2.middleware.grouper.pit.PITField;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITFieldDAO extends Hib3DAO implements PITFieldDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITFieldDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITField)
   */
  public void saveOrUpdate(PITField pitField) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitField);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITField> pitFields) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitFields);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#delete(edu.internet2.middleware.grouper.pit.PITField)
   */
  public void delete(PITField pitField) {
    HibernateSession.byObjectStatic().delete(pitField);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITField where sourceId not in (select f.uuid from Field as f)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITField findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITField pitField = HibernateSession
      .byHqlStatic()
      .createQuery("select pitField from PITField as pitField where pitField.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITField.class);
    
    if (pitField == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITField with sourceId=" + id + " not found");
    }
    
    return pitField;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITField findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITField pitField = HibernateSession
      .byHqlStatic()
      .createQuery("select pitField from PITField as pitField where pitField.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITField.class);
    
    if (pitField == null && exceptionIfNotFound) {
      throw new RuntimeException("PITField with sourceId=" + id + " not found");
    }
    
    return pitField;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITField> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITField> pitFields = HibernateSession
      .byHqlStatic()
      .createQuery("select pitField from PITField as pitField where pitField.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITField.class);
    
    if (pitFields.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITField with sourceId=" + id + " not found");
    }
    
    return pitFields;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findById(java.lang.String, boolean)
   */
  public PITField findById(String id, boolean exceptionIfNotFound) {
    PITField pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITField as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITField.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITField with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITField where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findMissingActivePITFields()
   */
  public Set<Field> findMissingActivePITFields() {

    Set<Field> fields = HibernateSession
      .byHqlStatic()
      .createQuery("select f from Field f where " +
          "not exists (select 1 from PITField pit where f.uuid = pit.sourceId and f.name = pit.nameDb and f.typeString = pit.typeDb) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = f.uuid " +
          "    and type.actionName='addGroupField' and type.changeLogCategory='groupField' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = f.uuid " +
          "    and type.actionName='updateGroupField' and type.changeLogCategory='groupField' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITFields")
      .listSet(Field.class);
    
    return fields;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findMissingInactivePITFields()
   */
  public Set<PITField> findMissingInactivePITFields() {

    Set<PITField> fields = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITField pit where activeDb = 'T' and " +
          "not exists (select 1 from Field f where f.uuid = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteGroupField' and type.changeLogCategory='groupField' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITFields")
      .listSet(PITField.class);
    
    return fields;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITField where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITField where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

