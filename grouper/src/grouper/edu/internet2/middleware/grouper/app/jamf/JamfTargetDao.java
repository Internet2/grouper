package edu.internet2.middleware.grouper.app.jamf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Jamf TargetDao -- manages admin accounts and account-group (role) memberships via the Jamf Pro
 * Classic API.
 *
 * <p>Capabilities (see {@link #registerGrouperProvisionerDaoCapabilities}):</p>
 * <ul>
 *   <li>Roles (groups) are <b>read-only</b>: retrieve only, never insert/update/delete. Jamf admins
 *       own role privilege sets.</li>
 *   <li>Accounts (entities) are <b>create-only</b>: retrieve + insert, never update/delete. An
 *       account is created (Group Access, random password) only so it can be added to a role.</li>
 *   <li>Memberships are <b>full CRUD</b> via full-list replace. Jamf has no atomic add/remove for
 *       account groups, so membership changes retrieve the current member list, modify it, and PUT
 *       the whole list back. Only {@code <name>}/{@code <members>} are sent, so a membership change
 *       never rewrites a role's privileges.</li>
 * </ul>
 */
public class JamfTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  private JamfProvisionerConfiguration getJamfConfiguration() {
    return (JamfProvisionerConfiguration) this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration();
  }

  private Set<String> ignoreAccountNames(JamfProvisionerConfiguration config) {
    return JamfApiCommands.parseIgnoreSet(config.getJamfIgnoreAccountNames());
  }

  private Set<String> ignoreRoleNames(JamfProvisionerConfiguration config) {
    return JamfApiCommands.parseIgnoreSet(config.getJamfIgnoreRoleNames());
  }

  // ============================
  // Retrieve all data: accounts (entities) + roles (groups) + role memberships.
  // GET /accounts gives accounts and roles (id/name only); each role's member list needs a
  // per-role GET /accounts/groupid/{id}.
  // ============================

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    long startNanos = System.nanoTime();

    TargetDaoRetrieveAllDataResponse response = new TargetDaoRetrieveAllDataResponse();
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    response.setTargetData(targetData);

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();
      Set<String> ignoreAccounts = ignoreAccountNames(config);
      Set<String> ignoreRoles = ignoreRoleNames(config);

      // all accounts (entities) + a name->nativeId index for membership resolution
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      Map<String, String> nameToId = new LinkedHashMap<String, String>();
      for (JamfAccount account : JamfApiCommands.retrieveAccounts(configId, ignoreAccounts)) {
        provisioningEntities.add(account.toProvisioningEntity());
        if (!StringUtils.isBlank(account.getName()) && !StringUtils.isBlank(account.getId())) {
          nameToId.put(account.getName().toLowerCase(), account.getId());
        }
      }
      targetData.setProvisioningEntities(provisioningEntities);

      // all roles (groups); then one detail call per role to get its members
      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      for (JamfAccountGroup role : JamfApiCommands.retrieveAccountGroups(configId, ignoreRoles)) {
        provisioningGroups.add(role.toProvisioningGroup());

        JamfAccountGroup roleWithMembers = JamfApiCommands.retrieveAccountGroup(configId, role.getId());
        if (roleWithMembers == null) {
          continue;
        }
        for (String memberName : GrouperUtil.nonNull(roleWithMembers.getMembers())) {
          if (JamfApiCommands.isIgnored(memberName, ignoreAccounts)) {
            continue;
          }
          String nativeId = nameToId.get(memberName.toLowerCase());
          if (StringUtils.isBlank(nativeId)) {
            // member account not in the accounts list (or ignored) -- skip
            continue;
          }
          ProvisioningMembership membership = new ProvisioningMembership(false);
          membership.setProvisioningGroupId(role.getId());
          membership.setProvisioningEntityId(nativeId);
          provisioningMemberships.add(membership);
        }
      }
      targetData.setProvisioningGroups(provisioningGroups);
      targetData.setProvisioningMemberships(provisioningMemberships);

      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
  }

  // ============================
  // Retrieve single entity (account by name = EPPN)
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      String searchValue = GrouperUtil.stringValue(
          targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      // Jamf can find an account only by name (username endpoint); id-search would be a separate
      // userid endpoint but name is the matching attribute, so name is all we need
      JamfAccount account = JamfApiCommands.retrieveAccountByName(configId, searchValue);

      ProvisioningEntity targetEntity = account == null ? null : account.toProvisioningEntity();
      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  // ============================
  // Retrieve single group (role by name or id), with members
  // ============================

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(
          targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      JamfAccountGroup role = null;
      if (StringUtils.equals("id", searchAttribute)) {
        role = JamfApiCommands.retrieveAccountGroup(configId, searchValue);
      } else {
        // resolve the role id by name from the list, then fetch its detail
        for (JamfAccountGroup candidate
            : JamfApiCommands.retrieveAccountGroups(configId, ignoreRoleNames(config))) {
          if (StringUtils.equals(candidate.getName(), searchValue)) {
            role = JamfApiCommands.retrieveAccountGroup(configId, candidate.getId());
            break;
          }
        }
      }

      ProvisioningGroup targetGroup = role == null ? null : role.toProvisioningGroup();
      return new TargetDaoRetrieveGroupResponse(targetGroup);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  // ============================
  // Insert entity (create an account so it can be added to a role)
  // ============================

  @Override
  public TargetDaoInsertEntityResponse insertEntity(
      TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      JamfAccount account = JamfAccount.fromProvisioningEntity(targetEntity);
      if (StringUtils.isBlank(account.getName())) {
        throw new RuntimeException("account name (EPPN) is required for insertEntity");
      }
      // never create an account on the ignore list
      if (JamfApiCommands.isIgnored(account.getName(), ignoreAccountNames(config))) {
        throw new RuntimeException("account '" + account.getName() + "' is on the ignore list");
      }
      // Grouper-created accounts inherit privileges from role membership
      account.setAccessLevel(config.getJamfNewAccountAccessLevel());

      JamfAccount createdAccount = JamfApiCommands.createAccount(configId, account);
      targetEntity.setId(createdAccount.getId());
      markProvisioned(targetEntity, true);

      return new TargetDaoInsertEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }

  // ============================
  // Replace group memberships (full desired list -> single PUT). This is the natural Jamf op.
  // ============================

  @Override
  public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(
      TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoReplaceGroupMembershipsRequest.getTargetGroup();
    List<ProvisioningMembership> targetMemberships =
        targetDaoReplaceGroupMembershipsRequest.getTargetMemberships();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      String groupId = targetGroup == null ? null : targetGroup.getId();
      String groupName = targetGroup == null ? null : targetGroup.retrieveAttributeValueString("name");

      Set<String> memberNames = new LinkedHashSet<String>();
      for (ProvisioningMembership membership : GrouperUtil.nonNull(targetMemberships)) {
        String name = membershipAccountName(membership);
        if (!StringUtils.isBlank(name) && !JamfApiCommands.isIgnored(name, ignoreAccountNames(config))) {
          memberNames.add(name);
        }
      }

      try {
        JamfApiCommands.replaceAccountGroupMembers(configId, groupId, groupName,
            new ArrayList<String>(memberNames));
        for (ProvisioningMembership membership : GrouperUtil.nonNull(targetMemberships)) {
          markProvisioned(membership, true);
        }
      } catch (RuntimeException e) {
        for (ProvisioningMembership membership : GrouperUtil.nonNull(targetMemberships)) {
          markProvisioned(membership, false);
        }
        throw e;
      }

      return new TargetDaoReplaceGroupMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("replaceGroupMemberships", startNanos));
    }
  }

  // ============================
  // Insert memberships (incremental): retrieve-modify-write, add the members and PUT the full list
  // ============================

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {

    long startNanos = System.nanoTime();
    try {
      adjustMemberships(targetDaoInsertMembershipsRequest.getTargetMemberships(), true);
      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

  // ============================
  // Delete memberships (incremental): retrieve-modify-write, remove the members and PUT the list
  // ============================

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {

    long startNanos = System.nanoTime();
    try {
      adjustMemberships(targetDaoDeleteMembershipsRequest.getTargetMemberships(), false);
      return new TargetDaoDeleteMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }

  /**
   * Apply incremental membership changes by retrieve-modify-write. Memberships are grouped by
   * role; for each role the current member list is fetched, the batch is added or removed, and the
   * complete list is PUT back.
   * @param targetMemberships the memberships to add or remove
   * @param add true to add, false to remove
   */
  private void adjustMemberships(List<ProvisioningMembership> targetMemberships, boolean add) {
    JamfProvisionerConfiguration config = getJamfConfiguration();
    String configId = config.getJamfExternalSystemConfigId();
    Set<String> ignoreAccounts = ignoreAccountNames(config);

    // group by role id, remembering the role name and the account names in this batch
    Map<String, String> groupIdToName = new LinkedHashMap<String, String>();
    Map<String, Set<String>> groupIdToBatchNames = new LinkedHashMap<String, Set<String>>();
    Map<String, List<ProvisioningMembership>> groupIdToMemberships =
        new LinkedHashMap<String, List<ProvisioningMembership>>();

    for (ProvisioningMembership membership : GrouperUtil.nonNull(targetMemberships)) {
      String groupId = membership.getProvisioningGroupId();
      if (StringUtils.isBlank(groupId)) {
        continue;
      }
      ProvisioningGroup provisioningGroup = membership.getProvisioningGroup();
      if (provisioningGroup != null && !groupIdToName.containsKey(groupId)) {
        groupIdToName.put(groupId, provisioningGroup.retrieveAttributeValueString("name"));
      }
      String name = membershipAccountName(membership);
      if (!StringUtils.isBlank(name) && !JamfApiCommands.isIgnored(name, ignoreAccounts)) {
        groupIdToBatchNames.computeIfAbsent(groupId, k -> new LinkedHashSet<String>()).add(name);
      }
      groupIdToMemberships.computeIfAbsent(groupId, k -> new ArrayList<ProvisioningMembership>())
          .add(membership);
    }

    for (String groupId : groupIdToMemberships.keySet()) {
      List<ProvisioningMembership> memberships = groupIdToMemberships.get(groupId);
      try {
        // retrieve current members (case-insensitive set keyed by lowercased name)
        JamfAccountGroup currentRole = JamfApiCommands.retrieveAccountGroup(configId, groupId);
        String groupName = groupIdToName.get(groupId);
        if (currentRole != null && StringUtils.isBlank(groupName)) {
          groupName = currentRole.getName();
        }
        // preserve original-case names in a map keyed by lowercase so we can add/remove reliably
        Map<String, String> lowerToName = new LinkedHashMap<String, String>();
        if (currentRole != null) {
          for (String memberName : GrouperUtil.nonNull(currentRole.getMembers())) {
            lowerToName.put(memberName.toLowerCase(), memberName);
          }
        }
        for (String batchName : GrouperUtil.nonNull(groupIdToBatchNames.get(groupId))) {
          if (add) {
            lowerToName.put(batchName.toLowerCase(), batchName);
          } else {
            lowerToName.remove(batchName.toLowerCase());
          }
        }
        JamfApiCommands.replaceAccountGroupMembers(configId, groupId, groupName,
            new ArrayList<String>(lowerToName.values()));
        for (ProvisioningMembership membership : memberships) {
          markProvisioned(membership, true);
        }
      } catch (RuntimeException e) {
        for (ProvisioningMembership membership : memberships) {
          markProvisioned(membership, false);
        }
        throw e;
      }
    }
  }

  /**
   * The member account name (EPPN) for a membership, from the entity's "name" attribute.
   * @param membership the provisioning membership
   * @return the account name, or null
   */
  private static String membershipAccountName(ProvisioningMembership membership) {
    ProvisioningEntity entity = membership == null ? null : membership.getProvisioningEntity();
    return entity == null ? null : entity.retrieveAttributeValueString("name");
  }

  /**
   * Mark a provisioning object and all its object-changes as provisioned or not.
   * @param provisioningObject the entity or membership
   * @param provisioned true if the write succeeded
   */
  private static void markProvisioned(
      edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable provisioningObject,
      boolean provisioned) {
    if (provisioningObject == null) {
      return;
    }
    provisioningObject.setProvisioned(provisioned);
    for (ProvisioningObjectChange change
        : GrouperUtil.nonNull(provisioningObject.getInternal_objectChanges())) {
      change.setProvisioned(provisioned);
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    // roles (groups) are read-only -- resolve only, never write
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);

    // accounts (entities) are create-only
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);

    // memberships: full-list replace + incremental add/remove (retrieve-modify-write)
    grouperProvisionerDaoCapabilities.setCanReplaceGroupMemberships(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);

    // sync-back: accounts/roles are captured from the typed beans at the JamfApiCommands read seams;
    // role memberships are captured here in the DAO (where account ids are resolved) and write-tracked
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);
  }

}
