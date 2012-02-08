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
    hibernateSession.byHql().createQuery("delete from PITMember where sourceId not in (select m.uuid from Member as m)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITMember findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITMember pitMember = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMember from PITMember as pitMember where pitMember.sourceId = :id and activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITMember.class);
    
    if (pitMember == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITMember with sourceId=" + id + " not found");
    }
    
    return pitMember;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITMember findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITMember pitMember = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMember from PITMember as pitMember where pitMember.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITMember.class);
    
    if (pitMember == null && exceptionIfNotFound) {
      throw new RuntimeException("PITMember with sourceId=" + id + " not found");
    }
    
    return pitMember;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITMember> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITMember> pitMembers = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMember from PITMember as pitMember where pitMember.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITMember.class);
    
    if (pitMembers.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITMember with sourceId=" + id + " not found");
    }
    
    return pitMembers;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findById(java.lang.String, boolean)
   */
  public PITMember findById(String id, boolean exceptionIfNotFound) {
    PITMember pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITMember as pit where pit.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITMember.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITMember with id=" + id + " not found");
    }
    
    return pit;
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findPITMembersBySubjectIdSourceAndType(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<PITMember> findPITMembersBySubjectIdSourceAndType(String id, String source, String type) {
    Set<PITMember> pitMembers = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMember from PITMember as pitMember where pitMember.subjectId = :id and pitMember.subjectSourceId = :source and pitMember.subjectTypeId = :type")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITMembersBySubjectIdSourceAndType")
      .setString("id", id)
      .setString("source", source)
      .setString("type", type)
      .listSet(PITMember.class);
    
    return pitMembers;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMemberDAO#findMissingActivePITMembers()
   */
  public Set<Member> findMissingActivePITMembers() {

    Set<Member> members = HibernateSession
      .byHqlStatic()
      .createQuery("select m from Member m where " +
          "not exists (select 1 from PITMember pit where m.uuid = pit.sourceId and m.subjectIdDb = pit.subjectId and m.subjectSourceIdDb = pit.subjectSourceId and m.subjectTypeId = pit.subjectTypeId) " +
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
          "not exists (select 1 from Member m where m.uuid = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteMember' and type.changeLogCategory='member' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITMember")
      .listSet(PITMember.class);
    
    return members;
  }
}

