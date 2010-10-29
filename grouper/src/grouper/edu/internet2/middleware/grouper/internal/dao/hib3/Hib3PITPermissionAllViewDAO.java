package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
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
}