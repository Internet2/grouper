package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO;

/**
 * @author shilen
 * $Id$
 */
public class Hib3FlatGroupDAO extends Hib3DAO implements FlatGroupDAO {

  /**
   *
   */
  private static final String KLASS = Hib3FlatGroupDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#save(edu.internet2.middleware.grouper.flat.FlatGroup)
   */
  public void save(FlatGroup flatGroup) {
    HibernateSession.byObjectStatic().save(flatGroup);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#delete(edu.internet2.middleware.grouper.flat.FlatGroup)
   */
  public void delete(FlatGroup flatGroup) {
    HibernateSession.byObjectStatic().delete(flatGroup);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<FlatGroup> flatGroups) {
    HibernateSession.byObjectStatic().saveBatch(flatGroups);
  }
  
  /**
   * reset flat group
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from FlatGroup").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#findById(java.lang.String)
   */
  public FlatGroup findById(String flatGroupId) {
    FlatGroup flatGroup = HibernateSession
      .byHqlStatic()
      .createQuery("select flatGroup from FlatGroup as flatGroup where flatGroup.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", flatGroupId)
      .uniqueResult(FlatGroup.class);
    
    return flatGroup;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#removeGroupForeignKey(java.lang.String)
   */
  public void removeGroupForeignKey(String flatGroupId) {
    HibernateSession.byHqlStatic()
      .createQuery("update FlatGroup set groupId = null where id = :id")
      .setString("id", flatGroupId)
      .executeUpdate();
  }

  public Set<Group> findMissingFlatGroups() {
    Set<Group> groups = HibernateSession
      .byHqlStatic()
      .createQuery("select g from Group g where not exists (select 1 from FlatGroup flatGroup where flatGroup.id=g.uuid) " +
      		"and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type where temp.string01 = g.uuid " +
      		"and type.actionName='addGroup' and type.changeLogCategory='group' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingFlatGroups")
      .listSet(Group.class);
    
    return groups;
  }

  public Set<FlatGroup> findBadFlatGroups() {
    Set<FlatGroup> groups = HibernateSession
      .byHqlStatic()
      .createQuery("select flatGroup from FlatGroup flatGroup where not exists (select 1 from Group g where flatGroup.id=g.uuid) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type where temp.string01 = flatGroup.id " +
          "and type.actionName='deleteGroup' and type.changeLogCategory='group' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBadFlatGroups")
      .listSet(FlatGroup.class);
    
    return groups;
  }
}

