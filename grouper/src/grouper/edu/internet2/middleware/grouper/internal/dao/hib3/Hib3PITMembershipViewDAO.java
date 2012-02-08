package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;
import edu.internet2.middleware.subject.Source;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITMembershipViewDAO extends Hib3DAO implements PITMembershipViewDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITMembershipViewDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITGroupSetsJoinedWithNewPITMembership(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithNewPITMembership(PITMembership pitMembership) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct gs from PITGroupSet as gs where memberId = :gsMemberId and memberFieldId = :gsMemberFieldId and activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms where ms.ownerId=gs.ownerId and ms.memberId = :msMemberId and ms.fieldId=gs.fieldId and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITGroupSetsJoinedWithNewPITMembership")
      .setString("gsMemberId", pitMembership.getOwnerId())
      .setString("gsMemberFieldId", pitMembership.getFieldId())
      .setString("msMemberId", pitMembership.getMemberId())
      .listSet(PITGroupSet.class);
    
    return pitGroupSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITGroupSetsJoinedWithOldPITMembership(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithOldPITMembership(PITMembership pitMembership) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct gs from PITGroupSet as gs where memberId = :gsMemberId and memberFieldId = :gsMemberFieldId and activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms where ms.ownerId=gs.ownerId and ms.memberId = :msMemberId and ms.fieldId=gs.fieldId and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T' and ms.membershipId <> :msMembershipId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITGroupSetsJoinedWithOldPITMembership")
      .setString("gsMemberId", pitMembership.getOwnerId())
      .setString("gsMemberFieldId", pitMembership.getFieldId())
      .setString("msMemberId", pitMembership.getMemberId())
      .setString("msMembershipId", pitMembership.getId())
      .listSet(PITGroupSet.class);
    
    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITMembershipsJoinedWithNewPITGroupSet(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public Set<PITMembership> findPITMembershipsJoinedWithNewPITGroupSet(PITGroupSet pitGroupSet) {
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct ms, m from PITMembership as ms, PITMember as m where ms.ownerId = :msOwnerId and ms.fieldId = :msFieldId and ms.activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms2 where ms2.ownerId = :ms2OwnerId and ms2.memberId = ms.memberId and ms2.fieldId = :ms2FieldId and ms2.groupSetActiveDb = 'T' and ms2.membershipActiveDb = 'T')" +
          "and ms.memberId = m.id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITMembershipsJoinedWithNewPITGroupSet")
      .setString("msOwnerId", pitGroupSet.getMemberId())
      .setString("msFieldId", pitGroupSet.getMemberFieldId())
      .setString("ms2OwnerId", pitGroupSet.getOwnerId())
      .setString("ms2FieldId", pitGroupSet.getFieldId())
      .listSet(Object[].class);
    
    return _getPITMembershipsFromPITMembershipAndPITMemberQuery(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITMembershipsJoinedWithOldPITGroupSet(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public Set<PITMembership> findPITMembershipsJoinedWithOldPITGroupSet(PITGroupSet pitGroupSet) {
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct ms, m from PITMembership as ms, PITMember as m where ms.ownerId = :msOwnerId and ms.fieldId = :msFieldId and ms.activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms2 where ms2.ownerId = :ms2OwnerId and ms2.memberId = ms.memberId and ms2.fieldId = :ms2FieldId and ms2.groupSetActiveDb = 'T' and ms2.membershipActiveDb = 'T' and ms2.groupSetId <> :ms2GroupSetId)" +
          "and ms.memberId = m.id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITMembershipsJoinedWithOldPITGroupSet")
      .setString("msOwnerId", pitGroupSet.getMemberId())
      .setString("msFieldId", pitGroupSet.getMemberFieldId())
      .setString("ms2OwnerId", pitGroupSet.getOwnerId())
      .setString("ms2FieldId", pitGroupSet.getFieldId())
      .setString("ms2GroupSetId", pitGroupSet.getId())
      .listSet(Object[].class);
    
    return _getPITMembershipsFromPITMembershipAndPITMemberQuery(mships);
  }
  
  /**
   * @param mships
   * @return set
   */
  private Set<PITMembership> _getPITMembershipsFromPITMembershipAndPITMemberQuery(Collection<Object[]> mships) {
    Set<PITMembership> pitMemberships = new LinkedHashSet<PITMembership>();
  
    for(Object[] tuple:mships) {
      PITMembership currPITMembership = (PITMembership)tuple[0];
      PITMember currPITMember = (PITMember)tuple[1];
      currPITMembership.setMember(currPITMember);
      pitMemberships.add(currPITMembership);
    }
    return pitMemberships;
  }
  
  /**
   * @param mships
   * @return set
   */
  private Set<PITMembershipView> _getPITMembershipViewsFromPITMembershipAndPITMemberQuery(Collection<Object[]> mships) {
    Set<PITMembershipView> pitMemberships = new LinkedHashSet<PITMembershipView>();
  
    for(Object[] tuple:mships) {
      PITMembershipView currPITMembership = (PITMembershipView)tuple[0];
      PITMember currPITMember = (PITMember)tuple[1];
      currPITMembership.setPITMember(currPITMember);
      pitMemberships.add(currPITMembership);
    }
    return pitMemberships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findByPITOwnerAndPITMemberAndPITField(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Set<PITMembershipView> findByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId, boolean activeOnly) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ms, m from PITMembershipView as ms, PITMember as m where ms.ownerId = :ownerId and ms.memberId = :memberId and ms.fieldId = :fieldId");
    
    if (activeOnly) {
      sql.append(" and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T'");
    }
    
    sql.append(" and ms.memberId = m.id");
    
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false).setCacheRegion(KLASS + ".FindByPITOwnerAndPITMemberAndPITField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .listSet(Object[].class);
    
    return _getPITMembershipViewsFromPITMembershipAndPITMemberQuery(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findAllMembersByPITOwnerAndPITField(java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Member> findAllMembersByPITOwnerAndPITField(String ownerId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions) {

    StringBuilder sql = new StringBuilder("select m "
        + "from Member m, PITMember pitMember, PITMembershipView ms where "
        + "ms.ownerId = :ownerId "
        + "and ms.fieldId = :fieldId "
        + "and ms.memberId = pitMember.id "
        + "and pitMember.sourceId = m.uuid");
    
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
      sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
    }
    
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByPITOwnerAndPITField")
      .setString("ownerId", ownerId) 
      .setString("fieldId", fieldId)
      .listSet(Member.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findAllByPITOwnerAndPITMemberAndPITField(java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<PITMembershipView> findAllByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembershipView ms where "
        + "ms.ownerId = :ownerId "
        + "and ms.memberId = :memberId "
        + "and ms.fieldId = :fieldId");
    
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
    
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITOwnerAndPITMemberAndPITField")
      .setString("ownerId", ownerId) 
      .setString("memberId", memberId) 
      .setString("fieldId", fieldId)
      .listSet(PITMembershipView.class);
  }
}

