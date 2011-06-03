package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMemberDAO;
import edu.internet2.middleware.grouper.pit.PITMember;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITMemberDAO extends Hib3DAO implements PITMemberDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITMemberDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITMember)
   */
  public void saveOrUpdate(PITMember pitMember) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitMember);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITMember> pitMembers) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitMembers);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#delete(edu.internet2.middleware.grouper.pit.PITMember)
   */
  public void delete(PITMember pitMember) {
    HibernateSession.byObjectStatic().delete(pitMember);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITMember where id not in (select m.uuid from Member as m)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findById(java.lang.String)
   */
  public PITMember findById(String pitMemberId) {
    PITMember pitMember = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMember from PITMember as pitMember where pitMember.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitMemberId)
      .uniqueResult(PITMember.class);
    
    return pitMember;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITMember where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findMemberBySubjectIdSourceAndType(java.lang.String, java.lang.String, java.lang.String)
   */
  public PITMember findMemberBySubjectIdSourceAndType(String id, String source, String type) {
    PITMember pitMember = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMember from PITMember as pitMember where pitMember.subjectId = :id and pitMember.subjectSourceId = :source and pitMember.subjectTypeId = :type")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMemberBySubjectIdSourceAndType")
      .setString("id", id)
      .setString("source", source)
      .setString("type", type)
      .uniqueResult(PITMember.class);
    
    return pitMember;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findMissingActivePITMembers()
   */
  public Set<Member> findMissingActivePITMembers() {

    Set<Member> members = HibernateSession
      .byHqlStatic()
      .createQuery("select m from Member m where " +
          "not exists (select 1 from PITMember pit where m.uuid = pit.id and m.subjectIdDb = pit.subjectId and m.subjectSourceIdDb = pit.subjectSourceId and m.subjectTypeId = pit.subjectTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = m.uuid " +
          "    and type.actionName='addMember' and type.changeLogCategory='member' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = m.uuid " +
          "    and type.actionName='updateMember' and type.changeLogCategory='member' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITMembers")
      .listSet(Member.class);
    
    return members;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findMissingInactivePITMembers()
   */
  public Set<PITMember> findMissingInactivePITMembers() {

    Set<PITMember> members = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITMember pit where activeDb = 'T' and " +
          "not exists (select 1 from Member m where m.uuid = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteMember' and type.changeLogCategory='member' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITMember")
      .listSet(PITMember.class);
    
    return members;
  }
}

