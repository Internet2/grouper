package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO;
import edu.internet2.middleware.grouper.pit.PITRoleSet;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITRoleSetDAO extends Hib3DAO implements PITRoleSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITRoleSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITRoleSet)
   */
  public void saveOrUpdate(PITRoleSet pitRoleSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitRoleSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#delete(edu.internet2.middleware.grouper.pit.PITRoleSet)
   */
  public void delete(PITRoleSet pitRoleSet) {
    HibernateSession.byObjectStatic().delete(pitRoleSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITRoleSet set parentRoleSetId = null where id not in (select roleSet.id from RoleSet as roleSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITRoleSet where id not in (select roleSet.id from RoleSet as roleSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findById(java.lang.String)
   */
  public PITRoleSet findById(String id) {
    PITRoleSet pitRoleSet = HibernateSession
      .byHqlStatic()
      .createQuery("select roleSet from PITRoleSet as roleSet where roleSet.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITRoleSet.class);
    
    return pitRoleSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys
    HibernateSession.byHqlStatic()
      .createQuery("update PITRoleSet set parentRoleSetId = null where endTimeDb is not null and endTimeDb < :time and parentRoleSetId is not null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITRoleSet where endTimeDb is not null and endTimeDb < :time and parentRoleSetId is null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
}