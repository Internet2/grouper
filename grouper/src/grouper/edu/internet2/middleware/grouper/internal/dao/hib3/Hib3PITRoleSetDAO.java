package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITRoleSet> pitRoleSets) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitRoleSets);
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
    hibernateSession.byHql().createQuery("update PITRoleSet set parentRoleSetId = null where sourceId not in (select roleSet.id from RoleSet as roleSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITRoleSet where sourceId not in (select roleSet.id from RoleSet as roleSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITRoleSet findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITRoleSet pitRoleSet = HibernateSession
      .byHqlStatic()
      .createQuery("select roleSet from PITRoleSet as roleSet where roleSet.sourceId = :id and activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITRoleSet.class);
    
    if (pitRoleSet == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITRoleSet with sourceId=" + id + " not found");
    }
    
    return pitRoleSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITRoleSet findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITRoleSet pitRoleSet = HibernateSession
      .byHqlStatic()
      .createQuery("select roleSet from PITRoleSet as roleSet where roleSet.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITRoleSet.class);
    
    if (pitRoleSet == null && exceptionIfNotFound) {
      throw new RuntimeException("PITRoleSet with sourceId=" + id + " not found");
    }
    
    return pitRoleSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findById(java.lang.String, boolean)
   */
  public PITRoleSet findById(String id, boolean exceptionIfNotFound) {
    PITRoleSet pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITRoleSet as pit where pit.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITRoleSet.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITRoleSet with id=" + id + " not found");
    }
    
    return pit;
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findImmediateChildren(edu.internet2.middleware.grouper.pit.PITRoleSet)
   */
  public Set<PITRoleSet> findImmediateChildren(PITRoleSet pitRoleSet) {
    Set<PITRoleSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select rs from PITRoleSet as rs where rs.parentRoleSetId = :parent and rs.depth <> '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildren")
        .setString("parent", pitRoleSet.getId())
        .listSet(PITRoleSet.class);
    
    return children;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findAllByPITOwnerAndPITMemberAndPITField(java.lang.String)
   */
  public void deleteSelfByPITRoleId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //update before delete since mysql cant handle self referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update PITRoleSet set parentRoleSetId = null where ifHasRoleId = :id and thenHasRoleId = :id and depth = '0'")
              .setString("id", id)
              .executeUpdate();

            Set<PITRoleSet> pitRoleSetsToDelete = findAllSelfPITRoleSetsByPITRoleId(id);
            for (PITRoleSet rs : pitRoleSetsToDelete) {
              delete(rs);
            }

            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findAllSelfPITRoleSetsByPITRoleId(java.lang.String)
   */
  public Set<PITRoleSet> findAllSelfPITRoleSetsByPITRoleId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select rs from PITRoleSet as rs where rs.ifHasRoleId = :id and rs.thenHasRoleId = :id and rs.depth = '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfPITRoleSetsByPITRoleId")
        .setString("id", id)
        .listSet(PITRoleSet.class);    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findByThenHasPITRoleId(java.lang.String)
   */
  public Set<PITRoleSet> findByThenHasPITRoleId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select rs from PITRoleSet as rs where thenHasRoleId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByThenHasPITRoleId")
        .setString("id", id)
        .listSet(PITRoleSet.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findMissingActivePITRoleSets()
   */
  public Set<RoleSet> findMissingActivePITRoleSets() {

    Set<RoleSet> roleSets = HibernateSession
      .byHqlStatic()
      .createQuery("select r from RoleSet r where " +
          "not exists (select 1 from PITRoleSet pit where r.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = r.id " +
          "    and type.actionName='addRoleSet' and type.changeLogCategory='roleSet' and type.id=temp.changeLogTypeId) " +
          "order by r.depth")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITRoleSets")
      .listSet(RoleSet.class);
    
    return roleSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO#findMissingInactivePITRoleSets()
   */
  public Set<PITRoleSet> findMissingInactivePITRoleSets() {

    Set<PITRoleSet> roleSets = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITRoleSet pit where activeDb = 'T' and " +
          "not exists (select 1 from RoleSet r where r.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteRoleSet' and type.changeLogCategory='roleSet' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITRoleSes")
      .listSet(PITRoleSet.class);
    
    return roleSets;
  }
}