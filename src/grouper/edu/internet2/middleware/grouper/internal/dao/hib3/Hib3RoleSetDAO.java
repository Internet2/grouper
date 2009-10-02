package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.exception.RoleSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.RoleSetDAO;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;

/**
 * Data Access Object for role set
 * @author  mchyzer
 * @version $Id: Hib3RoleSetDAO.java,v 1.4 2009-10-02 05:57:58 mchyzer Exp $
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
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.permissions.role.RoleSet)
   */
  public void saveOrUpdate(RoleSet roleSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(roleSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#delete(edu.internet2.middleware.grouper.permissions.role.RoleSet)
   */
  public void delete(final RoleSet roleSet) {
    //HibernateSession.byObjectStatic().delete(roleSet);
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            //set parent to null so mysql doest get mad
            //http://bugs.mysql.com/bug.php?id=15746
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                "update RoleSet set parentRoleSetId = null where id = :id")
                .setString("id", roleSet.getId()).executeUpdate();
            hibernateHandlerBean.getHibernateSession().byObject().delete(roleSet);
            return null;
          }
      
    });
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#rolesInheritPermissionsFromThis(java.lang.String)
   */
  public Set<Role> rolesInheritPermissionsFromThis(String id) {
    Set<Group> roles = HibernateSession.byHqlStatic().createQuery(
      "select distinct r from RoleSet as rs, Group as r " +
      "where rs.ifHasRoleId = :theId and r.id = rs.thenHasRoleId " +
      "and r.id != :theId order by r.nameDb")
      .setString("theId", id).listSet(Group.class);
    return (Set<Role>)(Object)roles;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#deleteByIfHasRole(edu.internet2.middleware.grouper.permissions.role.Role)
   */
  public void deleteByIfHasRole(final Role role) {

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler()  {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            //do this since mysql cant handle self-referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update RoleSet set parentRoleSetId = null where ifHasRoleId = :id")
              .setString("id", role.getId())
              .executeUpdate();    
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "delete from RoleSet where ifHasRoleId = :id")
              .setString("id", role.getId())
              .executeUpdate();    
            return null;
          }
        });
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#rolesInheritPermissionsFromThisImmediate(java.lang.String)
   */
  public Set<Role> rolesInheritPermissionsFromThisImmediate(String id) {
    Set<Group> roles = HibernateSession.byHqlStatic().createQuery(
        "select distinct r from RoleSet as rs, Group as r " +
        "where rs.ifHasRoleId = :theId and r.id = rs.thenHasRoleId " +
        "and r.id != :theId and rs.typeDb = 'immediate' order by r.nameDb")
        .setString("theId", id).listSet(Group.class);
      return (Set<Role>)(Object)roles;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#rolesInheritPermissionsToThis(java.lang.String)
   */
  public Set<Role> rolesInheritPermissionsToThis(String id) {
    Set<Group> roles = HibernateSession.byHqlStatic().createQuery(
        "select distinct r from RoleSet as rs, Group as r " +
        "where rs.thenHasRoleId = :theId and r.id = rs.ifHasRoleId " +
        "and r.id != :theId order by r.nameDb")
        .setString("theId", id).listSet(Group.class);
      return (Set<Role>)(Object)roles;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#rolesInheritPermissionsToThisImmediate(java.lang.String)
   */
  public Set<Role> rolesInheritPermissionsToThisImmediate(String id) {
    Set<Group> roles = HibernateSession.byHqlStatic().createQuery(
        "select distinct r from RoleSet as rs, Group as r " +
        "where rs.thenHasRoleId = :theId and r.id = rs.ifHasRoleId " +
        "and r.id != :theId and rs.typeDb = 'immediate' order by r.nameDb")
        .setString("theId", id).listSet(Group.class);
      return (Set<Role>)(Object)roles;
  }

} 

