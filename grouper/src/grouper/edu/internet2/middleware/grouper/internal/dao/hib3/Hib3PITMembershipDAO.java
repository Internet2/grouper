package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;

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
  

  
  public Set<Member> findAllMembersByGroupOwnerAndField(String ownerGroupId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions) {

    StringBuilder sql = new StringBuilder("select m "
        + "from Member m, PITMembershipView ms where "
        + "ms.ownerGroupId = :owner "
        + "and ms.fieldId = :fieldId "
        + "and ms.memberId = m.uuid");
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (ms.membershipEndTimeDb is null or ms.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (ms.groupSetEndTimeDb is null or ms.groupSetEndTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and ms.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and ms.groupSetStartTimeDb < '" + startDateBefore + "'");
    }

    if (sources != null && sources.size() > 0) {
      sql.append(" and m.subjectSourceIdDb in ").append(GrouperUtil.convertSourcesToSqlInString(sources));
    }
    
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByGroupOwnerAndField")
      .setString("owner", ownerGroupId) 
      .setString("fieldId", fieldId)
      .listSet(Member.class);
  }
}

