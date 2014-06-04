/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
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
 * @version $Id: Hib3RoleSetDAO.java,v 1.6 2009-11-08 13:16:53 mchyzer Exp $
 */
public class Hib3RoleSetDAO extends Hib3DAO implements RoleSetDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3RoleSetDAO.class.getName();

  /**
   * reset the role sets
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
      //do this since mysql cant handle self-referential foreign keys
      // restrict this only to mysql since in oracle this might cause unique constraint violations
      hibernateSession.byHql().createQuery("update RoleSet set parentRoleSetId = null").executeUpdate();
    }
    
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
            
            if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
              //set parent to null so mysql doest get mad
              //http://bugs.mysql.com/bug.php?id=15746
              hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                  "update RoleSet set parentRoleSetId = null where id = :id")
                  .setString("id", roleSet.getId()).executeUpdate();
            }
            
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
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByIfHasRoleIdImmediate(java.lang.String)
   */
  public Set<RoleSet> findByIfHasRoleIdImmediate(String id) {
    Set<RoleSet> roleSets = HibernateSession.byHqlStatic().createQuery(
      "from RoleSet where ifHasRoleId = :theId and depth = 1")
      .setString("theId", id).listSet(RoleSet.class);
    return roleSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByThenHasRoleIdImmediate(java.lang.String)
   */
  public Set<RoleSet> findByThenHasRoleIdImmediate(String id) {
    Set<RoleSet> roleSets = HibernateSession.byHqlStatic().createQuery(
      "from RoleSet where thenHasRoleId = :theId and depth = 1")
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
        "and thenHasRoleId = :thenId " + 
        "and depth = 1")
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
                        
            Set<RoleSet> roleSets = HibernateSession.byHqlStatic().createQuery(
              "from RoleSet where ifHasRoleId = :theId order by depth desc").setString("theId", role.getId()).listSet(RoleSet.class);
            for (RoleSet roleSet : roleSets) {
              if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
                //do this since mysql cant handle self-referential foreign keys
                roleSet.setParentRoleSetId(null);
                roleSet.saveOrUpdate();
              }
              
              hibernateHandlerBean.getHibernateSession().byObject().delete(roleSet);
            }
 
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#findByUuidOrKey(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, boolean)
   */
  public RoleSet findByUuidOrKey(String id, String ifHasRoleId, String thenHasRoleId,
      String parentRoleSetId, int depth, boolean exceptionIfNull) {
    try {
      RoleSet roleSet = HibernateSession.byHqlStatic()
        .createQuery("from RoleSet as theRoleSet where theRoleSet.id = :theId or (theRoleSet.ifHasRoleId = :theIfHasRoleId " +
        		" and theRoleSet.thenHasRoleId = :theThenHasRoleId and theRoleSet.parentRoleSetId = :theParentRoleSetId " +
        		" and theRoleSet.depth = :theDepth)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrKey")
        .setString("theId", id)
        .setString("theIfHasRoleId", ifHasRoleId)
        .setString("theThenHasRoleId", thenHasRoleId)
        .setString("theParentRoleSetId", parentRoleSetId)
        .setInteger("theDepth", depth)
        .uniqueResult(RoleSet.class);
      if (roleSet == null && exceptionIfNull) {
        throw new RuntimeException("Can't find roleSet by id: '" + id + "' or ifHasRoleId '" + ifHasRoleId 
            + "', thenHasRoleId: " + thenHasRoleId + ", parentRoleSetId: " + parentRoleSetId + ", depth: " + depth);
      }
      return roleSet;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find roleSet by id: '" + id + "' or ifHasRoleId '" + ifHasRoleId 
            + "', thenHasRoleId: " + thenHasRoleId + ", parentRoleSetId: " + parentRoleSetId 
            + ", depth: " + depth + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetDAO#saveUpdateProperties(edu.internet2.middleware.grouper.permissions.role.RoleSet)
   */
  public void saveUpdateProperties(RoleSet roleSet) {

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update RoleSet " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdated " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", roleSet.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", roleSet.getCreatedOnDb())
        .setLong("theLastUpdated", roleSet.getLastUpdatedDb())
        .setString("theContextId", roleSet.getContextId())
        .setString("theId", roleSet.getId()).executeUpdate();
  }

  public RoleSet findSelfRoleSet(String groupId, boolean exceptionIfNotFound) {
    RoleSet roleSet = HibernateSession.byHqlStatic().createQuery(
        "from RoleSet where ifHasRoleId = :groupId and thenHasRoleId = :groupId and depth = 0")
        .setString("groupId", groupId)
        .uniqueResult(RoleSet.class);
    
    if (roleSet == null && exceptionIfNotFound) {
      throw new RoleSetNotFoundException("Self roleSet for not found for " + groupId);
    }
    
    return roleSet;
  }

} 

