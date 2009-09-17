package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.exception.RoleSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.RoleSetDAO;
import edu.internet2.middleware.grouper.permissions.RoleSet;

/**
 * Data Access Object for role set
 * @author  mchyzer
 * @version $Id: Hib3RoleSetDAO.java,v 1.1 2009-09-17 04:19:15 mchyzer Exp $
 */
public class Hib3RoleSetDAO extends Hib3DAO implements RoleSetDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3RoleSetDAO.class.getName();

  /**
   * reset the role sets
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("update RoleSet set parentRoleSetId = null").executeUpdate();
    hibernateSession.byHql().createQuery("delete from RoleSet").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findById(java.lang.String, boolean)
   */
  public RoleSet findById(String id, boolean exceptionIfNotFound) throws AttributeDefNameSetNotFoundException {
    RoleSet roleSet = HibernateSession.byHqlStatic().createQuery(
        "from RoleSet where id = :theId")
      .setString("theId", id).uniqueResult(RoleSet.class);
    if (roleSet == null && exceptionIfNotFound) {
      throw new RoleSetNotFoundException("Cant find role name set by id: " + id);
    }
    return roleSet;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.permissions.RoleSet)
   */
  public void saveOrUpdate(RoleSet roleSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(roleSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#delete(edu.internet2.middleware.grouper.permissions.RoleSet)
   */
  public void delete(RoleSet roleSet) {
    HibernateSession.byObjectStatic().delete(roleSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByIfHasRoleId(java.lang.String)
   */
  public Set<RoleSet> findByIfHasRoleId(String id) {
    Set<RoleSet> roleSets = HibernateSession.byHqlStatic().createQuery(
      "from RoleSet where ifHasRoleId = :theId")
      .setString("theId", id).listSet(RoleSet.class);
    return roleSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByIfThenHasRoleId(java.lang.String, java.lang.String)
   */
  public Set<RoleSet> findByIfThenHasRoleId(String roleSetForThens, String roleSetForIfs) {
    Set<RoleSet> roleSets = HibernateSession.byHqlStatic().createQuery(
        "select distinct theRoleSet from RoleSet as theRoleSet, RoleSet as theRoleSetThens, "
        + "RoleSet as theRoleSetIfs "
        + "where theRoleSetThens.thenHasRoleId = :roleSetForThens "
        + "and theRoleSetIfs.ifHasRoleId = :roleSetForIfs "
        + "and theRoleSet.ifHasRoleId = theRoleSetThens.ifHasRoleId "
        + "and theRoleSet.thenHasRoleId = theRoleSetIfs.thenHasRoleId "
      )
      .setString("roleSetForThens", roleSetForThens)
      .setString("roleSetForIfs", roleSetForIfs)
      .listSet(RoleSet.class);
    return roleSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
   */
  public RoleSet findByIfThenImmediate(String roleIdIf, String roleIdThen,
      boolean exceptionIfNotFound) {
    RoleSet roleSet = HibernateSession.byHqlStatic().createQuery(
        "from RoleSet where ifHasRoleId = :ifId " +
        "and thenHasRoleId = :thenId")
        .setString("ifId", roleIdIf).setString("thenId", roleIdThen)
        .uniqueResult(RoleSet.class);
      if (roleSet == null && exceptionIfNotFound) {
        throw new RoleSetNotFoundException("RoleSet immediate if "
            + roleIdIf + ", then: " + roleIdThen);
      }
      return roleSet;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByThenHasRoleId(java.lang.String)
   */
  public Set<RoleSet> findByThenHasRoleId(String id) {
    Set<RoleSet> roleSets = HibernateSession.byHqlStatic().createQuery(
      "from RoleSet where thenHasRoleId = :theId")
      .setString("theId", id).listSet(RoleSet.class);
    return roleSets;
  }

} 

