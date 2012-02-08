package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Membership;
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITMembership> pitMemberships) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitMemberships);
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
    hibernateSession.byHql().createQuery("delete from PITMembership where sourceId not in (select ms.immediateMembershipId from ImmediateMembershipEntry as ms)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITMembership findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITMembership pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id and activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITMembership.class);
    
    if (pitMembership == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITMembership findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITMembership pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITMembership.class);
    
    if (pitMembership == null && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITMembership> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITMembership> pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITMembership.class);
    
    if (pitMembership.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdMostRecent(java.lang.String, boolean)
   */
  public PITMembership findBySourceIdMostRecent(String id, boolean exceptionIfNotFound) {
    Set<PITMembership> pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id order by startTimeDb desc")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdMostRecent")
      .setString("id", id)
      .listSet(PITMembership.class);
    
    if (pitMembership.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership.iterator().next();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findById(java.lang.String, boolean)
   */
  public PITMembership findById(String id, boolean exceptionIfNotFound) {
    PITMembership pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITMembership as pit where pit.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITMembership.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with id=" + id + " not found");
    }
    
    return pit;
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
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findAllByPITOwner(java.lang.String)
   */
  public Set<PITMembership> findAllByPITOwner(String ownerId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.ownerId = :ownerId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITOwner")
      .setString("ownerId", ownerId)
      .listSet(PITMembership.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findAllByPITMember(java.lang.String)
   */
  public Set<PITMembership> findAllByPITMember(String memberId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.memberId = :memberId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITMember")
      .setString("memberId", memberId)
      .listSet(PITMembership.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findMissingActivePITMemberships()
   */
  public Set<Membership> findMissingActivePITMemberships() {

    // note that doing actual checks for the addMembership and addPrivilege change log events seem to be very expensive...
    Set<Membership> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select ms from ImmediateMembershipEntry ms where ms.enabledDb='T' and " +
          "not exists (select 1 from PITMembership pit where ms.immediateMembershipId = pit.sourceId and pit.activeDb = 'T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp " +
          "    where temp.string01 = ms.immediateMembershipId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITMemberships")
      .listSet(Membership.class);
    
    return mships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findMissingInactivePITMemberships()
   */
  public Set<PITMembership> findMissingInactivePITMemberships() {

    Set<PITMembership> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITMembership pit where activeDb = 'T' and " +
          "not exists (select 1 from ImmediateMembershipEntry ms where ms.immediateMembershipId = pit.sourceId and ms.enabledDb='T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deletePrivilege' and type.changeLogCategory='privilege' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITMemberships")
      .listSet(PITMembership.class);
    
    return mships;
  }
}

