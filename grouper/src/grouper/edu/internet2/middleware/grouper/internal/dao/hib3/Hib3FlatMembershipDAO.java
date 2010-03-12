package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO;

/**
 * @author shilen
 * $Id$
 */
public class Hib3FlatMembershipDAO extends Hib3DAO implements FlatMembershipDAO {

  /**
   *
   */
  private static final String KLASS = Hib3FlatMembershipDAO.class.getName();


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#saveOrUpdate(edu.internet2.middleware.grouper.membership.FlatMembership)
   */
  public void saveOrUpdate(FlatMembership flatMembership) {
    HibernateSession.byObjectStatic().saveOrUpdate(flatMembership);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<FlatMembership> flatMemberships) {
    Iterator<FlatMembership> iter = flatMemberships.iterator();
    while (iter.hasNext()) {
      saveOrUpdate(iter.next());
    }
  }
  
  public void saveBatch(Set<FlatMembership> flatMemberships) {
    HibernateSession.byObjectStatic().saveBatch(flatMemberships);
  } 
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#delete(edu.internet2.middleware.grouper.membership.FlatMembership)
   */
  public void delete(FlatMembership flatMembership) {
    HibernateSession.byObjectStatic().delete(flatMembership);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#delete(java.util.Set)
   */
  public void delete(Set<FlatMembership> flatMemberships) {
    Iterator<FlatMembership> iter = flatMemberships.iterator();
    while (iter.hasNext()) {
      delete(iter.next());
    }
  }
  
  /**
   * reset flat membership
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from FlatMembership").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findById(java.lang.String)
   */
  public FlatMembership findById(String flatMembershipId) {
    FlatMembership flatMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship from FlatMembership as flatMship where flatMship.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", flatMembershipId)
      .uniqueResult(FlatMembership.class);
    
    return flatMembership;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findByOwnerAndMemberAndField(java.lang.String, java.lang.String, java.lang.String)
   */
  public FlatMembership findByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId) {
    FlatMembership flatMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship from FlatMembership as flatMship where flatMship.ownerId = :ownerId and " +
      		"flatMship.memberId = :memberId and flatMship.fieldId = :fieldId")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerAndMemberAndField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .uniqueResult(FlatMembership.class);
    
    return flatMembership;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findMembersToAddByGroupOwnerAndField(java.lang.String, java.lang.String)
   */
  public Set<Member> findMembersToAddByGroupOwnerAndField(String ownerId, String fieldId) {
    Set<Member> members = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct m from MembershipEntry as ms, Member as m " +
      		"where ms.memberUuid = m.uuid and ms.ownerGroupId = :ownerId and ms.fieldId = :fieldId " +
      		"and ms.memberUuid not in (select flatMship.memberId from FlatMembership as flatMship where flatMship.ownerId = :ownerId and flatMship.fieldId = :fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMembersToAddByGroupOwnerAndField")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .listSet(Member.class);
    
    return members;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findMembersToAddByStemOwnerAndField(java.lang.String, java.lang.String)
   */
  public Set<Member> findMembersToAddByStemOwnerAndField(String ownerId, String fieldId) {
    Set<Member> members = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct m from MembershipEntry as ms, Member as m " +
          "where ms.memberUuid = m.uuid and ms.ownerStemId = :ownerId and ms.fieldId = :fieldId " +
          "and ms.memberUuid not in (select flatMship.memberId from FlatMembership as flatMship where flatMship.ownerId = :ownerId and flatMship.fieldId = :fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMembersToAddByStemOwnerAndField")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .listSet(Member.class);
    
    return members;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findMembersToAddByAttrDefOwnerAndField(java.lang.String, java.lang.String)
   */
  public Set<Member> findMembersToAddByAttrDefOwnerAndField(String ownerId, String fieldId) {
    Set<Member> members = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct m from MembershipEntry as ms, Member as m " +
          "where ms.memberUuid = m.uuid and ms.ownerAttrDefId = :ownerId and ms.fieldId = :fieldId " +
          "and ms.memberUuid not in (select flatMship.memberId from FlatMembership as flatMship where flatMship.ownerId = :ownerId and flatMship.fieldId = :fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMembersToAddByAttrDefOwnerAndField")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .listSet(Member.class);
    
    return members;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findMembersToDeleteByGroupOwnerAndField(java.lang.String, java.lang.String)
   */
  public Set<FlatMembership> findMembersToDeleteByGroupOwnerAndField(String ownerId, String fieldId) {
    Set<Object[]> flatMemberships = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship, m from FlatMembership as flatMship, Member as m " +
          "where flatMship.memberId = m.uuid and flatMship.ownerId = :ownerId and flatMship.fieldId = :fieldId " +
          "and flatMship.memberId not in (select ms.memberUuid from MembershipEntry as ms where ms.ownerGroupId = :ownerId and ms.fieldId = :fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMembersToDeleteByGroupOwnerAndField")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .listSet(Object[].class);
    
    return _getFlatMembershipsFromFlatMembershipAndMemberQuery(flatMemberships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findMembersToDeleteByStemOwnerAndField(java.lang.String, java.lang.String)
   */
  public Set<FlatMembership> findMembersToDeleteByStemOwnerAndField(String ownerId, String fieldId) {
    Set<Object[]> flatMemberships = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship, m from FlatMembership as flatMship, Member as m " +
          "where flatMship.memberId = m.uuid and flatMship.ownerId = :ownerId and flatMship.fieldId = :fieldId " +
          "and flatMship.memberId not in (select ms.memberUuid from MembershipEntry as ms where ms.ownerStemId = :ownerId and ms.fieldId = :fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMembersToDeleteByStemOwnerAndField")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .listSet(Object[].class);
    
    return _getFlatMembershipsFromFlatMembershipAndMemberQuery(flatMemberships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findMembersToDeleteByAttrDefOwnerAndField(java.lang.String, java.lang.String)
   */
  public Set<FlatMembership> findMembersToDeleteByAttrDefOwnerAndField(String ownerId, String fieldId) {
    Set<Object[]> flatMemberships = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship, m from FlatMembership as flatMship, Member as m " +
          "where flatMship.memberId = m.uuid and flatMship.ownerId = :ownerId and flatMship.fieldId = :fieldId " +
          "and flatMship.memberId not in (select ms.memberUuid from MembershipEntry as ms where ms.ownerAttrDefId = :ownerId and ms.fieldId = :fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMembersToDeleteByAttrDefOwnerAndField")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .listSet(Object[].class);
    
    return _getFlatMembershipsFromFlatMembershipAndMemberQuery(flatMemberships);
  }
  

  private Set<FlatMembership> _getFlatMembershipsFromFlatMembershipAndMemberQuery(Collection<Object[]> flatMships) {
    Set<FlatMembership> flatMemberships = new LinkedHashSet<FlatMembership>();
    
    for (Object[] tuple:flatMships) {
      FlatMembership currFlatMembership = (FlatMembership)tuple[0];
      Member currMember = (Member)tuple[1];
      currFlatMembership.setMember(currMember);
      flatMemberships.add(currFlatMembership);
    }
    
    return flatMemberships;
  }

  public Set<FlatMembership> findByMemberId(String memberId) {
    Set<FlatMembership> flatMemberships = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship from FlatMembership as flatMship where flatMship.memberId = :memberId")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("memberId", memberId)
      .listSet(FlatMembership.class);
    
    return flatMemberships;
  }

  public Set<FlatMembership> findBadFlatMemberships() {
    Set<FlatMembership> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship from FlatMembership flatMship where " +
      		"not exists (select 1 from MembershipEntry ms " +
      		"    where (flatMship.ownerId=ms.ownerGroupId or flatMship.ownerId=ms.ownerStemId or flatMship.ownerId=ms.ownerAttrDefId) " +
      		"    and flatMship.memberId=ms.memberUuid and flatMship.fieldId=ms.fieldId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type where temp.string06 = flatMship.ownerId and temp.string09=flatMship.fieldId " +
          "    and type.actionName='deleteMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type where temp.string07 = flatMship.ownerId and temp.string10=flatMship.fieldId " +
          "    and type.actionName='deletePrivilege' and type.changeLogCategory='privilege' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type, Member m, FlatMembership flatMship2 " +
          "    where temp.string09=:fieldId and type.actionName='deleteMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId " +
          "    and m.subjectIdDb=temp.string06 and m.subjectSourceIdDb='g:gsa' " +
          "    and flatMship2.memberId = m.uuid and flatMship.ownerId=flatMship2.ownerId and flatMship.fieldId = flatMship2.fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBadFlatMemberships")
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(FlatMembership.class);
    
    return mships;
  }

  public Set<Membership> findMissingFlatMemberships() {
    Set<Membership> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select ms from MembershipEntry ms where " +
          "not exists (select 1 from FlatMembership flatMship " +
          "    where (flatMship.ownerId=ms.ownerGroupId or flatMship.ownerId=ms.ownerStemId or flatMship.ownerId=ms.ownerAttrDefId) " +
          "    and flatMship.memberId=ms.memberUuid and flatMship.fieldId=ms.fieldId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string06 = ms.ownerGroupId and temp.string09=ms.fieldId " +
          "    and type.actionName='addMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where (temp.string07 = ms.ownerGroupId or temp.string07 = ms.ownerStemId or temp.string07 = ms.ownerAttrDefId) and temp.string10=ms.fieldId " +
          "    and type.actionName='addPrivilege' and type.changeLogCategory='privilege' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type, Member m, MembershipEntry ms2 " +
          "    where temp.string09=:fieldId and type.actionName='addMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId " +
          "    and m.subjectIdDb=temp.string06 and m.subjectSourceIdDb='g:gsa' " +
          "    and ms2.memberUuid = m.uuid " +
          "    and (ms.ownerGroupId is not null and ms.ownerGroupId=ms2.ownerGroupId " +
          "         or ms.ownerStemId is not null and ms.ownerStemId=ms2.ownerStemId " +
          "         or ms.ownerAttrDefId is not null and ms.ownerAttrDefId=ms2.ownerAttrDefId) " +
          "    and ms.fieldId = ms2.fieldId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingFlatMemberships")
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Membership.class);
    
    return mships;
  }
}

