package edu.internet2.middleware.grouper.app.github;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * TargetDao for the GitHub provisioner. This is a <b>membership-driven</b>
 * provisioner: it does not create accounts or teams. Teams must pre-exist
 * (organization teams; enterprise teams are read-only), and accounts are
 * resolved to their GitHub login via the enterprise SAML external-identities map
 * and/or org membership, then matched to Grouper subjects on the configured
 * attribute (typically samlNameId or login).
 *
 * <p>Org membership is a derived prerequisite: adding a resolved login to a team
 * ({@code PUT team membership}) also issues the pending org invitation as a side
 * effect, so a known GitHub user is invited to the org by being added to a team.
 * A subject whose login cannot be resolved yet (e.g. a brand-new account that has
 * not authenticated through SSO) has a blank membership target id, so the
 * framework defers that membership with a DNE error and retries it on a later
 * sync once the account resolves.</p>
 *
 * <p>Capabilities are intentionally narrow for v1 (see
 * {@link #registerGrouperProvisionerDaoCapabilities}): retrieve + membership
 * insert/delete + entity delete (full org deprovision). No group or entity
 * create/update.</p>
 */
public class GithubTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  /**
   * @return the typed provisioner configuration
   */
  private GithubProvisionerConfiguration getGithubConfiguration() {
    return (GithubProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  }

  /**
   * @return the runtime settings loaded from the provisioner configuration
   */
  private GithubSettings buildGithubSettings() {
    GithubSettings githubSettings = new GithubSettings();
    githubSettings.loadFromGithubProvisionerConfiguration(getGithubConfiguration());
    return githubSettings;
  }

  /**
   * @return the WsBearerToken external system config id (GitHub token + endpoint)
   */
  private String getConfigId() {
    return getGithubConfiguration().getGithubExternalSystemConfigId();
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      for (String org : GrouperUtil.nonNull(githubSettings.getManagedOrgs())) {
        List<GithubTeam> teams = GithubApiCommands.retrieveTeams(configId, githubSettings, org);
        for (GithubTeam team : GrouperUtil.nonNull(teams)) {
          // v1 manages organization teams only; enterprise teams are read-only
          if (team.isEnterpriseTeam()) {
            continue;
          }
          results.add(team.toProvisioningGroup());
          // sync-back: capture the native team while the typed bean is in scope
          GithubProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(team);
        }
      }

      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();
      if (grouperTargetGroup == null) {
        return new TargetDaoRetrieveGroupResponse(null);
      }

      String org = grouperTargetGroup.retrieveAttributeValueString("org");
      String slug = grouperTargetGroup.retrieveAttributeValueString("slug");
      if (StringUtils.isBlank(slug)) {
        slug = grouperTargetGroup.getId();
      }
      if (StringUtils.isBlank(org) || StringUtils.isBlank(slug)) {
        return new TargetDaoRetrieveGroupResponse(null);
      }

      GithubTeam team = GithubApiCommands.retrieveTeam(configId, githubSettings, org, slug);
      // sync-back: capture the native team while the typed bean is in scope
      GithubProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(team);
      ProvisioningGroup targetGroup = team == null ? null : team.toProvisioningGroup();
      return new TargetDaoRetrieveGroupResponse(targetGroup);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  /**
   * Build the union of resolvable accounts: the enterprise SAML external
   * identities (login + samlNameId, when an enterprise slug is configured) plus
   * the active members of each managed org, deduplicated by login. This is the
   * pool the framework matches Grouper subjects against.
   * @param configId the external system config id
   * @param githubSettings the settings
   * @return list of accounts (deduped by login)
   */
  private List<GithubUser> buildAllEntities(String configId, GithubSettings githubSettings) {
    Map<String, GithubUser> byLogin = new LinkedHashMap<String, GithubUser>();

    // SAML external identities give login + samlNameId for every enterprise-linked
    // account (enterprise-wide, so it includes people not yet in any managed org)
    for (GithubUser githubUser : GrouperUtil.nonNull(
        GithubApiCommands.retrieveExternalIdentities(configId, githubSettings))) {
      if (!StringUtils.isBlank(githubUser.getLogin())) {
        byLogin.put(githubUser.getLogin(), githubUser);
      }
    }

    // active org members (covers non-SAML deployments and members not in the map)
    for (String org : GrouperUtil.nonNull(githubSettings.getManagedOrgs())) {
      for (GithubUser member : GrouperUtil.nonNull(
          GithubApiCommands.retrieveOrgMembers(configId, githubSettings, org))) {
        if (!StringUtils.isBlank(member.getLogin()) && !byLogin.containsKey(member.getLogin())) {
          byLogin.put(member.getLogin(), member);
        }
      }
    }

    return new ArrayList<GithubUser>(byLogin.values());
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      for (GithubUser githubUser : buildAllEntities(configId, githubSettings)) {
        results.add(githubUser.toProvisioningEntity());
        // sync-back: capture the native account while the typed bean is in scope
        GithubProvisioningTargetNativeSync.captureAccountFromCurrentProvisioner(githubUser);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchAttributeValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      GithubUser found = null;
      for (GithubUser githubUser : buildAllEntities(configId, githubSettings)) {
        // the provisioning entity id IS the login (GithubUser.toProvisioningEntity does setId(login)),
        // so both "id" and "login" search attributes resolve against the login
        if ((StringUtils.equals("id", searchAttribute) || StringUtils.equals("login", searchAttribute))
            && StringUtils.equals(githubUser.getLogin(), searchAttributeValue)) {
          found = githubUser;
          break;
        } else if (StringUtils.equals("samlNameId", searchAttribute)
            && StringUtils.equals(githubUser.getSamlNameId(), searchAttributeValue)) {
          found = githubUser;
          break;
        } else if (StringUtils.equals("githubId", searchAttribute)
            && StringUtils.equals(githubUser.getId(), searchAttributeValue)) {
          found = githubUser;
          break;
        }
      }

      ProvisioningEntity targetEntity = found == null ? null : found.toProvisioningEntity();
      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      if (targetGroup == null) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      }

      String org = targetGroup.retrieveAttributeValueString("org");
      String slug = targetGroup.getId();
      if (StringUtils.isBlank(org) || StringUtils.isBlank(slug)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      }

      List<GithubMembership> memberships = GithubApiCommands.retrieveTeamMemberships(configId, githubSettings, org, slug);
      List<String> memberLogins = new ArrayList<String>();
      for (GithubMembership membership : GrouperUtil.nonNull(memberships)) {
        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
        targetMembership.setProvisioningGroupId(slug);
        targetMembership.setProvisioningEntityId(membership.getUserLogin());
        provisioningMemberships.add(targetMembership);
        memberLogins.add(membership.getUserLogin());
      }
      // sync-back: capture this team's full membership set (slug + logins)
      GithubProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(slug, memberLogins);

      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }

  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {

    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      String slug = targetMembership.getProvisioningGroupId();
      String login = targetMembership.getProvisioningEntityId();
      ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
      String org = provisioningGroup == null ? null : provisioningGroup.retrieveAttributeValueString("org");

      if (StringUtils.isBlank(org) || StringUtils.isBlank(slug) || StringUtils.isBlank(login)) {
        throw new RuntimeException("Cannot add team membership, missing org '" + org + "', slug '" + slug
            + "', or login '" + login + "'");
      }

      // PUT team membership by login. For a resolved login that is not yet an org
      // member, GitHub returns state=pending and issues the org invitation as a
      // side effect (org membership is the derived prerequisite).
      GithubApiCommands.addTeamMembership(configId, githubSettings, org, slug, login, "member");
      // sync-back: write-track the added membership into the native mirror
      GithubProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(slug, login);

      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw new RuntimeException("Failed to add GitHub team membership (group '"
          + targetMembership.getProvisioningGroupId() + "', entity '" + targetMembership.getProvisioningEntityId() + "')", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMembership", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {

    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      String slug = targetMembership.getProvisioningGroupId();
      String login = targetMembership.getProvisioningEntityId();
      ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
      String org = provisioningGroup == null ? null : provisioningGroup.retrieveAttributeValueString("org");

      if (StringUtils.isBlank(org) || StringUtils.isBlank(slug) || StringUtils.isBlank(login)) {
        throw new RuntimeException("Cannot remove team membership, missing org '" + org + "', slug '" + slug
            + "', or login '" + login + "'");
      }

      // Granular: removes only the team membership; the account stays an org member.
      GithubApiCommands.removeTeamMembership(configId, githubSettings, org, slug, login);
      // sync-back: write-track the removed membership out of the native mirror
      GithubProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(slug, login);

      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw new RuntimeException("Failed to remove GitHub team membership (group '"
          + targetMembership.getProvisioningGroupId() + "', entity '" + targetMembership.getProvisioningEntityId() + "')", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      String configId = getConfigId();
      GithubSettings githubSettings = buildGithubSettings();

      String login = targetEntity.getId();
      if (StringUtils.isBlank(login)) {
        login = targetEntity.retrieveAttributeValueString("login");
      }

      if (StringUtils.isBlank(login)) {
        throw new RuntimeException("Cannot deprovision entity, no login on entity: " + targetEntity);
      }

      // full deprovision: remove from every managed org (idempotent; 404 is fine).
      // GitHub org removal also drops that account's team memberships in the org.
      for (String org : GrouperUtil.nonNull(githubSettings.getManagedOrgs())) {
        GithubApiCommands.removeOrgMembership(configId, githubSettings, org, login);
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    // reads
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    // writes: memberships (team add/remove) and entity delete (full org deprovision)
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    // sync-back: teams/accounts captured from the typed beans at the retrieve seams;
    // membership edges captured in retrieveMembershipsByGroup and write-tracked in
    // insert/delete membership
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);
    // NOT set for v1: insert/update group (teams pre-exist), insert/update entity
    // (accounts are invited via team-add, not created).
  }

}
