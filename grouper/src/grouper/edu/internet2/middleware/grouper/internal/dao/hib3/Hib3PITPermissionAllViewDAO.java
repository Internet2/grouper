package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
/**
 * @author shilen
 * $Id$
 */
public class Hib3PITPermissionAllViewDAO extends Hib3DAO implements PITPermissionAllViewDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITPermissionAllViewDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findNewOrDeletedFlatPermissionsAfterActionSetAddOrDelete(java.lang.String)
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterActionSetAddOrDelete(String actionSetId) {
    Set<PITPermissionAllView> perms = HibernateSession
      .byHqlStatic()
      .createQuery("select perm from PITPermissionAllView as perm where actionSetId = :actionSetId " +
      		"and groupSetActiveDb = 'T' and membershipActiveDb = 'T' and roleSetActiveDb = 'T' and actionSetActiveDb = 'T' and attributeDefNameSetActiveDb = 'T' and attributeAssignActiveDb = 'T' " +
          "and not exists (select 1 from PITPermissionAllView perm2 where perm2.attributeDefNameId=perm.attributeDefNameId and perm2.actionId=perm.actionId and perm2.memberId=perm.memberId and perm2.actionSetId <> :actionSetId " +
          "and perm2.groupSetActiveDb = 'T' and perm2.membershipActiveDb = 'T' and perm2.roleSetActiveDb = 'T' and perm2.actionSetActiveDb = 'T' and perm2.attributeDefNameSetActiveDb = 'T' and perm2.attributeAssignActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindNewOrDeletedFlatPermissionsAfterActionSetAddOrDelete")
      .setString("actionSetId", actionSetId)
      .listSet(PITPermissionAllView.class);
    
    return perms;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findNewOrDeletedFlatPermissionsAfterAttributeDefNameSetAddOrDelete(java.lang.String)
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterAttributeDefNameSetAddOrDelete(String attributeDefNameSetId) {
    Set<PITPermissionAllView> perms = HibernateSession
      .byHqlStatic()
      .createQuery("select perm from PITPermissionAllView as perm where attributeDefNameSetId = :attributeDefNameSetId " +
          "and groupSetActiveDb = 'T' and membershipActiveDb = 'T' and roleSetActiveDb = 'T' and actionSetActiveDb = 'T' and attributeDefNameSetActiveDb = 'T' and attributeAssignActiveDb = 'T' " +
          "and not exists (select 1 from PITPermissionAllView perm2 where perm2.attributeDefNameId=perm.attributeDefNameId and perm2.actionId=perm.actionId and perm2.memberId=perm.memberId and perm2.attributeDefNameSetId <> :attributeDefNameSetId " +
          "and perm2.groupSetActiveDb = 'T' and perm2.membershipActiveDb = 'T' and perm2.roleSetActiveDb = 'T' and perm2.actionSetActiveDb = 'T' and perm2.attributeDefNameSetActiveDb = 'T' and perm2.attributeAssignActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindNewOrDeletedFlatPermissionsAfterAttributeDefNameSetAddOrDelete")
      .setString("attributeDefNameSetId", attributeDefNameSetId)
      .listSet(PITPermissionAllView.class);
    
    return perms;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findNewOrDeletedFlatPermissionsAfterRoleSetAddOrDelete(java.lang.String)
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterRoleSetAddOrDelete(String roleSetId) {
    Set<PITPermissionAllView> perms = HibernateSession
      .byHqlStatic()
      .createQuery("select perm from PITPermissionAllView as perm where roleSetId = :roleSetId " +
          "and groupSetActiveDb = 'T' and membershipActiveDb = 'T' and roleSetActiveDb = 'T' and actionSetActiveDb = 'T' and attributeDefNameSetActiveDb = 'T' and attributeAssignActiveDb = 'T' " +
          "and not exists (select 1 from PITPermissionAllView perm2 where perm2.attributeDefNameId=perm.attributeDefNameId and perm2.actionId=perm.actionId and perm2.memberId=perm.memberId and perm2.roleSetId <> :roleSetId " +
          "and perm2.groupSetActiveDb = 'T' and perm2.membershipActiveDb = 'T' and perm2.roleSetActiveDb = 'T' and perm2.actionSetActiveDb = 'T' and perm2.attributeDefNameSetActiveDb = 'T' and perm2.attributeAssignActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindNewOrDeletedFlatPermissionsAfterRoleSetAddOrDelete")
      .setString("roleSetId", roleSetId)
      .listSet(PITPermissionAllView.class);
    
    return perms;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findNewOrDeletedFlatPermissionsAfterAttributeAssignAddOrDelete(java.lang.String)
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterAttributeAssignAddOrDelete(String attributeAssignId) {
    Set<PITPermissionAllView> perms = HibernateSession
      .byHqlStatic()
      .createQuery("select perm from PITPermissionAllView as perm where attributeAssignId = :attributeAssignId " +
          "and groupSetActiveDb = 'T' and membershipActiveDb = 'T' and roleSetActiveDb = 'T' and actionSetActiveDb = 'T' and attributeDefNameSetActiveDb = 'T' and attributeAssignActiveDb = 'T' " +
          "and not exists (select 1 from PITPermissionAllView perm2 where perm2.attributeDefNameId=perm.attributeDefNameId and perm2.actionId=perm.actionId and perm2.memberId=perm.memberId and perm2.attributeAssignId <> :attributeAssignId " +
          "and perm2.groupSetActiveDb = 'T' and perm2.membershipActiveDb = 'T' and perm2.roleSetActiveDb = 'T' and perm2.actionSetActiveDb = 'T' and perm2.attributeDefNameSetActiveDb = 'T' and perm2.attributeAssignActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindNewOrDeletedFlatPermissionsAfterAttributeAssignAddOrDelete")
      .setString("attributeAssignId", attributeAssignId)
      .listSet(PITPermissionAllView.class);
    
    return perms;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findNewOrDeletedFlatPermissionsAfterGroupSetAddOrDelete(java.lang.String)
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterGroupSetAddOrDelete(String groupSetId) {
    Set<PITPermissionAllView> perms = HibernateSession
      .byHqlStatic()
      .createQuery("select perm from PITPermissionAllView as perm where groupSetId = :groupSetId " +
          "and groupSetActiveDb = 'T' and membershipActiveDb = 'T' and roleSetActiveDb = 'T' and actionSetActiveDb = 'T' and attributeDefNameSetActiveDb = 'T' and attributeAssignActiveDb = 'T' " +
          "and not exists (select 1 from PITPermissionAllView perm2 where perm2.attributeDefNameId=perm.attributeDefNameId and perm2.actionId=perm.actionId and perm2.memberId=perm.memberId and perm2.groupSetId <> :groupSetId " +
          "and perm2.groupSetActiveDb = 'T' and perm2.membershipActiveDb = 'T' and perm2.roleSetActiveDb = 'T' and perm2.actionSetActiveDb = 'T' and perm2.attributeDefNameSetActiveDb = 'T' and perm2.attributeAssignActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindOrDeletedNewFlatPermissionsAfterGroupSetAddOrDelete")
      .setString("groupSetId", groupSetId)
      .listSet(PITPermissionAllView.class);
    
    return perms;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findNewOrDeletedFlatPermissionsAfterMembershipAddOrDelete(java.lang.String)
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterMembershipAddOrDelete(String membershipId) {
    Set<PITPermissionAllView> perms = HibernateSession
      .byHqlStatic()
      .createQuery("select perm from PITPermissionAllView as perm where membershipId = :membershipId " +
          "and groupSetActiveDb = 'T' and membershipActiveDb = 'T' and roleSetActiveDb = 'T' and actionSetActiveDb = 'T' and attributeDefNameSetActiveDb = 'T' and attributeAssignActiveDb = 'T' " +
          "and not exists (select 1 from PITPermissionAllView perm2 where perm2.attributeDefNameId=perm.attributeDefNameId and perm2.actionId=perm.actionId and perm2.memberId=perm.memberId and perm2.membershipId <> :membershipId " +
          "and perm2.groupSetActiveDb = 'T' and perm2.membershipActiveDb = 'T' and perm2.roleSetActiveDb = 'T' and perm2.actionSetActiveDb = 'T' and perm2.attributeDefNameSetActiveDb = 'T' and perm2.attributeAssignActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindNewOrDeletedFlatPermissionsAfterMembershipAddOrDelete")
      .setString("membershipId", membershipId)
      .listSet(PITPermissionAllView.class);
    
    return perms;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findPermissions(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.sql.Timestamp, java.sql.Timestamp)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds, Collection<String> attributeDefNameIds, 
      Collection<String> roleIds, Collection<String> actions, Collection<String> memberIds, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo) {
    
    int memberIdsSize = GrouperUtil.length(memberIds);
    int roleIdsSize = GrouperUtil.length(roleIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (memberIdsSize == 0 && roleIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in members and/or attributeDefId(s) and/or roleId(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (memberIdsSize + roleIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many memberIdsSize " + memberIdsSize 
          + " roleIdsSize " + roleIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct pea ";
    
    StringBuilder sqlTables = new StringBuilder(" from PITPermissionAllView pea ");
    
    StringBuilder sqlWhereClause = new StringBuilder("");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "pea.attributeDefId", AttributeDefPrivilege.READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "pea.roleId", AccessPrivilege.READ_PRIVILEGES);

    StringBuilder sql;
    if (sqlWhereClause.length() > 0) {
      sql = sqlTables.append(" where ").append(sqlWhereClause);
    } else {
      sql = sqlTables;
    }
    
    if (actionsSize > 0) {
      sql.append(" and pea.action in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (roleIdsSize > 0) {
      sql.append(" and pea.roleId in (");
      sql.append(HibUtils.convertToInClause(roleIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and pea.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and pea.attributeDefNameId in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and pea.memberId in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
    }
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (pea.membershipEndTimeDb is null or pea.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.groupSetEndTimeDb is null or pea.groupSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.actionSetEndTimeDb is null or pea.actionSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.attributeDefNameSetEndTimeDb is null or pea.attributeDefNameSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.roleSetEndTimeDb is null or pea.roleSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.attributeAssignEndTimeDb is null or pea.attributeAssignEndTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and pea.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.groupSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.actionSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.attributeDefNameSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.roleSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.attributeAssignStartTimeDb < '" + startDateBefore + "'");
    }
    
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findPermissions");

    int maxAssignments = GrouperConfig.getPropertyInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    
    Set<PermissionEntry> results = byHqlStatic.createQuery(selectPrefix + sqlString).listSet(PermissionEntry.class);

    int size = GrouperUtil.length(results);
    if (maxAssignments >= 0) {
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
    }

    //nothing to filter
    if (size == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
    
    //we should be down to the secure list
    return results;
  }
}