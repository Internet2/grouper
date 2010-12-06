package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO;
import edu.internet2.middleware.grouper.pit.PITMembership;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITMembershipDAO extends Hib3DAO implements PITMembershipDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITMembershipDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public void saveOrUpdate(PITMembership pitMembership) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitMembership);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#delete(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public void delete(PITMembership pitMembership) {
    HibernateSession.byObjectStatic().delete(pitMembership);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITMembership where id not in (select ms.immediateMembershipId from ImmediateMembershipEntry as ms)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findById(java.lang.String)
   */
  public PITMembership findById(String pitMembershipId) {
    PITMembership pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitMembershipId)
      .uniqueResult(PITMembership.class);
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#updateId(java.lang.String, java.lang.String)
   */
  public void updateId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITMembership set id = :newId where id = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITMembership m where m.endTimeDb is not null and m.endTimeDb < :time " +
      		"and not exists (select 1 from PITAttributeAssign a where a.ownerMembershipId = m.id)")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
}

