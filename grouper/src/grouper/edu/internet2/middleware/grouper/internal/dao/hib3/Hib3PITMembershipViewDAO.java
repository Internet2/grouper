package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;

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
          "and not exists (select ms from PITMembershipView ms where ms.ownerId=gs.ownerId and ms.memberId = :msMemberId and ms.fieldId=gs.fieldId and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T')")
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
          "and not exists (select ms from PITMembershipView ms where ms.ownerId=gs.ownerId and ms.memberId = :msMemberId and ms.fieldId=gs.fieldId and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T' and ms.membershipId <> :msMembershipId)")
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
          "and not exists (select ms2 from PITMembershipView ms2 where ms2.ownerId = :ms2OwnerId and ms2.memberId = ms.memberId and ms2.fieldId = :ms2FieldId and ms2.groupSetActiveDb = 'T' and ms2.membershipActiveDb = 'T')" +
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
          "and not exists (select ms2 from PITMembershipView ms2 where ms2.ownerId = :ms2OwnerId and ms2.memberId = ms.memberId and ms2.fieldId = :ms2FieldId and ms2.groupSetActiveDb = 'T' and ms2.membershipActiveDb = 'T' and ms2.groupSetId <> :ms2GroupSetId)" +
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
      currPITMembership.setMember(currPITMember);
      pitMemberships.add(currPITMembership);
    }
    return pitMemberships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findByOwnerAndMemberAndField(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Set<PITMembershipView> findByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId, boolean activeOnly) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ms, m from PITMembershipView as ms, PITMember as m where ms.ownerId = :ownerId and ms.memberId = :memberId and ms.fieldId = :fieldId");
    
    if (activeOnly) {
      sql.append(" and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T'");
    }
    
    sql.append(" and ms.memberId = m.id");
    
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerAndMemberAndField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .listSet(Object[].class);
    
    return _getPITMembershipViewsFromPITMembershipAndPITMemberQuery(mships);
  }
}

