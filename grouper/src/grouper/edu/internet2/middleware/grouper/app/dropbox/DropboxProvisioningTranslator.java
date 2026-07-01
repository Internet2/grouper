package edu.internet2.middleware.grouper.app.dropbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Translator for the Dropbox provisioner.  Implements the admin-role overlay.
 *
 * <p>When an admin-role folder is configured, each member's effective Dropbox admin
 * role is resolved as the highest tier (per {@link DropboxUser#ADMIN_ROLE_HIERARCHY})
 * among the admin-role <i>marker groups</i> they belong to, and stamped on the target
 * entity's {@code adminRole} attribute.  Members in no marker group are set to
 * {@code member_only}.</p>
 *
 * <p>The marker groups live under the configured folder and are named exactly after
 * the 8 Dropbox built-in admin roles (e.g. {@code <folder>:Team_Admin}).  They are
 * <b>not</b> provisioned as Dropbox groups -- they only drive admin roles.  Crucially,
 * they must NOT be marked provisionable for this provisioner; their membership is read
 * directly here via {@link GroupFinder}, independent of the provisioning selection, so
 * they never enter the group/membership pipeline.</p>
 *
 * <p>When no admin-role folder is configured, this translator is a pass-through.</p>
 */
public class DropboxProvisioningTranslator extends GrouperProvisioningTranslator {

  /** group attribute value indicating a normal (non-admin-role) group */
  public static final String ADMIN_ROLE_NONE = "none";

  /** entity attribute value indicating a regular member with no admin rights */
  public static final String MEMBER_ONLY = "member_only";

  @Override
  public List<ProvisioningEntity> translateGrouperToTargetEntities(List<ProvisioningEntity> grouperProvisioningEntities,
      boolean includeDelete, boolean forCreate) {

    List<ProvisioningEntity> grouperTargetEntities = super.translateGrouperToTargetEntities(grouperProvisioningEntities, includeDelete, forCreate);

    DropboxProvisionerConfiguration config = (DropboxProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    boolean manageAdminRoles = config.isManageAdminRoles();
    boolean manageLifecycle = config.isManageLifecycle();

    if (!manageAdminRoles && !manageLifecycle) {
      return grouperTargetEntities;
    }

    // memberId -> highest admin role tier (resolved from the admin-role marker groups, read directly
    // from Grouper so the marker groups do not need to be provisionable)
    Map<String, String> memberIdToAdminRole = manageAdminRoles
        ? resolveMemberIdToAdminRole(config.getDropboxAdminRoleFolderName())
        : java.util.Collections.<String, String>emptyMap();

    // members of <lifecycleFolder>:Suspended -> these get lifecycleState=suspended (others active).
    // (The Downgrade marker is resolved at delete time in the DAO, since a deprovisioned entity is no
    // longer translated here.)
    Set<String> suspendedMemberIds = manageLifecycle
        ? resolveMarkerGroupMemberIds(config.getDropboxLifecycleFolderName() + ":" + DropboxUser.LIFECYCLE_MARKER_SUSPENDED)
        : java.util.Collections.<String>emptySet();

    for (ProvisioningEntityWrapper entityWrapper : GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      ProvisioningEntity targetEntity = entityWrapper.getGrouperTargetEntity();
      String memberId = entityWrapper.getMemberId();
      if (memberId == null || targetEntity == null) {
        continue;
      }
      if (manageAdminRoles) {
        targetEntity.assignAttributeValue("adminRole",
            GrouperUtil.defaultIfBlank(memberIdToAdminRole.get(memberId), MEMBER_ONLY));
      }
      if (manageLifecycle) {
        targetEntity.assignAttributeValue("lifecycleState", suspendedMemberIds.contains(memberId)
            ? DropboxUser.LIFECYCLE_STATE_SUSPENDED : DropboxUser.LIFECYCLE_STATE_ACTIVE);
      }
    }

    return grouperTargetEntities;
  }

  /**
   * Read the member ids of a single Grouper marker group (e.g. {@code <lifecycleFolder>:Suspended})
   * directly, so the marker group does not need to be provisionable. Returns an empty set if the
   * group does not exist.
   * @param groupName the full marker group name
   * @return the set of Grouper memberIds in the group
   */
  private Set<String> resolveMarkerGroupMemberIds(String groupName) {

    Set<String> memberIds = new HashSet<String>();

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    if (grouperSession == null) {
      try {
        grouperSession = GrouperSession.startRootSession();
      } catch (edu.internet2.middleware.grouper.exception.SessionException se) {
        throw new RuntimeException("Could not start a Grouper session to resolve Dropbox lifecycle state", se);
      }
      startedSession = true;
    }

    try {
      Group markerGroup = GroupFinder.findByName(grouperSession, groupName, false);
      if (markerGroup != null) {
        for (Member member : GrouperUtil.nonNull(markerGroup.getMembers())) {
          memberIds.add(member.getUuid());
        }
      }
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

    return memberIds;
  }

  /**
   * Build a map of Grouper memberId -&gt; highest admin-role tier by looking up each of the
   * 8 possible marker groups ({@code <folder>:<RoleName>}) directly in Grouper and reading
   * their members.  Groups that do not exist are skipped.  Higher tiers (earlier in
   * {@link DropboxUser#ADMIN_ROLE_HIERARCHY}) win when a member is in more than one.
   * @param adminRoleFolderName the configured admin-role folder name
   * @return memberId -&gt; admin role name
   */
  private Map<String, String> resolveMemberIdToAdminRole(String adminRoleFolderName) {

    Map<String, String> memberIdToAdminRole = new HashMap<String, String>();

    // a provisioning daemon runs inside a Grouper session; fall back to a root session if absent
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    if (grouperSession == null) {
      try {
        grouperSession = GrouperSession.startRootSession();
      } catch (edu.internet2.middleware.grouper.exception.SessionException se) {
        throw new RuntimeException("Could not start a Grouper session to resolve Dropbox admin roles", se);
      }
      startedSession = true;
    }

    try {
      for (String roleName : DropboxUser.ADMIN_ROLE_HIERARCHY) {
        String groupName = adminRoleFolderName + ":" + roleName;
        Group markerGroup = GroupFinder.findByName(grouperSession, groupName, false);
        if (markerGroup == null) {
          continue;
        }
        for (Member member : GrouperUtil.nonNull(markerGroup.getMembers())) {
          String memberId = member.getUuid();
          memberIdToAdminRole.put(memberId, higherTier(memberIdToAdminRole.get(memberId), roleName));
        }
      }
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

    return memberIdToAdminRole;
  }

  /**
   * Return the higher (more privileged) of two admin role tiers per the hierarchy.
   * Either argument may be null.
   * @param tierA first tier name (may be null)
   * @param tierB second tier name (may be null)
   * @return the higher tier
   */
  private static String higherTier(String tierA, String tierB) {
    if (tierA == null) {
      return tierB;
    }
    if (tierB == null) {
      return tierA;
    }
    Set<String> both = new HashSet<String>();
    both.add(tierA);
    both.add(tierB);
    return DropboxUser.highestAdminRole(both);
  }

}
