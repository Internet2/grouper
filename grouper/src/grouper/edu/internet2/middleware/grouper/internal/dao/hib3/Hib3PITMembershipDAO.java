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
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findAllByOwner(java.lang.String)
   */
  public Set<PITMembership> findAllByOwner(String ownerId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.ownerId = :ownerId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwner")
      .setString("ownerId", ownerId)
      .listSet(PITMembership.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findAllByMember(java.lang.String)
   */
  public Set<PITMembership> findAllByMember(String memberId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.memberId = :memberId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByMember")
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
          "not exists (select 1 from PITMembership pit where ms.immediateMembershipId = pit.id and pit.activeDb = 'T') " +
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
          "not exists (select 1 from ImmediateMembershipEntry ms where ms.immediateMembershipId = pit.id and ms.enabledDb='T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deletePrivilege' and type.changeLogCategory='privilege' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITMemberships")
      .listSet(PITMembership.class);
    
    return mships;
  }
}

