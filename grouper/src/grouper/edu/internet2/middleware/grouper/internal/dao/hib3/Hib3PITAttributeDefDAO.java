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
    hibernateSession.byHql().createQuery("delete from PITAttributeDef where id not in (select a.id from AttributeDef as a)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findById(java.lang.String)
   */
  public PITAttributeDef findById(String pitAttributeDefId) {
    PITAttributeDef pitAttributeDef = HibernateSession
      .byHqlStatic()
      .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitAttributeDefId)
      .uniqueResult(PITAttributeDef.class);
    
    return pitAttributeDef;
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findByStemId(java.lang.String)
   */
  public Set<PITAttributeDef> findByStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.stemId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByStemId")
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
          "not exists (select 1 from PITAttributeDef pit where def.id = pit.id and def.nameDb = pit.nameDb and def.stemId = pit.stemId) " +
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
          "not exists (select 1 from AttributeDef def where def.id = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteAttributeDef' and type.changeLogCategory='attributeDef' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeDefs")
      .listSet(PITAttributeDef.class);
    
    return attrs;
  }
}

