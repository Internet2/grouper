package edu.internet2.middleware.grouper.app.github;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;

/**
 * GitHub-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting beans
 * for sync-back into the generic grouper_prov_group / grouper_prov_user / grouper_prov_mship tables.
 *
 * <p>The GitHub target speaks JSON that is parsed into the typed {@link GithubTeam} /
 * {@link GithubUser} / {@link GithubMembership} beans, so capture reads those beans. The native
 * target ids match the provisioning ids: a team's target id is its <b>slug</b> and an account's
 * target id is its <b>login</b> (not the numeric GitHub ids). Object capture is hooked at the
 * {@link GithubTargetDao} read seams (retrieveAllGroups / retrieveAllEntities /
 * retrieveMembershipsByGroup) and membership write-tracking at the insert/delete membership seams.</p>
 */
public class GithubProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /** default captured attributes for a GitHub account (entity); target id is the login */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("samlNameId"),
          attrConfig("email"),
          attrConfig("githubId")));

  /** default captured attributes for a GitHub team (group); target id is the slug */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          attrConfig("name"),
          attrConfig("org"),
          attrConfig("teamType"),
          attrConfig("privacy"),
          attrConfig("description")));

  private static GrouperProvisioningNativeAttributeConfig attrConfig(String name) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    return cfg;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsEntities() {
    return DEFAULT_ENTITY_ATTRS;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsGroups() {
    return DEFAULT_GROUP_ATTRS;
  }

  // ----- build (typed bean -> native reporting bean) -----------------------------------

  /**
   * Build a native group bean from a GithubTeam. Returns null if the team has no slug.
   * @param team the team
   * @return the native group bean, or null
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroup(GithubTeam team) {
    if (team == null || StringUtils.isBlank(team.getSlug())) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(team.getSlug());
    Map<String, Object> attributes = bean.getAttributes();
    for (GrouperProvisioningNativeAttributeConfig cfg : effectiveNativeAttributeConfigsGroups()) {
      Object value = groupAttributeValue(team, cfg.getName());
      if (value != null) {
        attributes.put(cfg.getName(), value);
      }
    }
    return bean;
  }

  /**
   * Build a native user bean from a GithubUser. Returns null if the account has no login.
   * @param account the account
   * @return the native user bean, or null
   */
  public GrouperProvisioningTargetNativeUser buildNativeUser(GithubUser account) {
    if (account == null || StringUtils.isBlank(account.getLogin())) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(account.getLogin());
    Map<String, Object> attributes = bean.getAttributes();
    for (GrouperProvisioningNativeAttributeConfig cfg : effectiveNativeAttributeConfigsEntities()) {
      Object value = entityAttributeValue(account, cfg.getName());
      if (value != null) {
        attributes.put(cfg.getName(), value);
      }
    }
    return bean;
  }

  private static Object groupAttributeValue(GithubTeam team, String name) {
    if ("name".equals(name)) {
      return team.getName();
    }
    if ("org".equals(name)) {
      return team.getOrg();
    }
    if ("teamType".equals(name)) {
      return team.getTeamType();
    }
    if ("privacy".equals(name)) {
      return team.getPrivacy();
    }
    if ("description".equals(name)) {
      return team.getDescription();
    }
    return null;
  }

  private static Object entityAttributeValue(GithubUser account, String name) {
    if ("samlNameId".equals(name)) {
      return account.getSamlNameId();
    }
    if ("email".equals(name)) {
      return account.getEmail();
    }
    if ("githubId".equals(name)) {
      return account.getId();
    }
    return null;
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a GitHub team. No-op when sync-back is off or the team has no slug. */
  public void captureGroup(GithubTeam team) {
    this.recordTargetNativeGroup(this.buildNativeGroup(team));
  }

  /** Build + record a GitHub account. No-op when sync-back is off or the account has no login. */
  public void captureAccount(GithubUser account) {
    this.recordTargetNativeUser(this.buildNativeUser(account));
  }

  /**
   * Record (slug, login) memberships for a single team.
   * @param targetGroupId the team slug
   * @param targetUserIds the member logins
   */
  public void captureMembershipsForGroup(String targetGroupId, Collection<String> targetUserIds) {
    if (targetGroupId == null || targetUserIds == null || targetUserIds.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (String userId : targetUserIds) {
      if (userId == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(targetGroupId);
      membership.setTargetUserId(userId);
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GithubTargetDao) ------------------------------

  /** Capture a team read against the current provisioner's sync. No-op outside a GitHub cycle. */
  public static void captureGroupFromCurrentProvisioner(GithubTeam team) {
    GithubProvisioningTargetNativeSync githubSync = githubSyncForCurrentProvisioner();
    if (githubSync != null) {
      githubSync.captureGroup(team);
    }
  }

  /** Capture an account read against the current provisioner's sync. No-op outside a GitHub cycle. */
  public static void captureAccountFromCurrentProvisioner(GithubUser account) {
    GithubProvisioningTargetNativeSync githubSync = githubSyncForCurrentProvisioner();
    if (githubSync != null) {
      githubSync.captureAccount(account);
    }
  }

  /** Record a team's full membership set (already-resolved logins) against the current sync. */
  public static void captureMembershipsForGroupFromCurrentProvisioner(
      String targetGroupId, Collection<String> targetUserIds) {
    GithubProvisioningTargetNativeSync githubSync = githubSyncForCurrentProvisioner();
    if (githubSync != null) {
      githubSync.captureMembershipsForGroup(targetGroupId, targetUserIds);
    }
  }

  /** Write-track a single successful membership add. */
  public static void captureMembershipInsertFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    GithubProvisioningTargetNativeSync githubSync = githubSyncForCurrentProvisioner();
    if (githubSync != null) {
      githubSync.recordTargetNativeMembershipInsert(targetGroupId, targetUserId);
    }
  }

  /** Write-track a single successful membership remove. */
  public static void captureMembershipDeleteFromCurrentProvisioner(String targetGroupId, String targetUserId) {
    GithubProvisioningTargetNativeSync githubSync = githubSyncForCurrentProvisioner();
    if (githubSync != null) {
      githubSync.recordTargetNativeMembershipDelete(targetGroupId, targetUserId);
    }
  }

  private static GithubProvisioningTargetNativeSync githubSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GithubProvisioningTargetNativeSync) {
      return (GithubProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}
