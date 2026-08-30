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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
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
 *   <li>Accounts (entities) support <b>full CRUD</b> (each op gated by the provisioner config):
 *       create (Group Access, random password), update (name/full_name/email), and delete. When
 *       {@code disableEntitiesInsteadOfDelete} is set, a delete disables the account (enabled=Disabled)
 *       instead of removing it, reads filter disabled accounts out, and an insert reactivates a
 *       disabled account rather than creating a duplicate. Accounts whose name/email/email_address is
 *       on the ignore list are never created, updated, disabled, deleted, or (un)assigned.</li>
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

  /**
   * Fetch full account detail (enabled/email/fullName) for an account known only by id+name from the
   * accounts list. Falls back to the list bean if the detail call comes back empty.
   * @param configId the external system config id
   * @param listAccount the id+name account from GET /accounts
   * @return the detailed account (or listAccount if detail was unavailable)
   */
  private JamfAccount withDetail(String configId, JamfAccount listAccount) {
    if (listAccount == null || StringUtils.isBlank(listAccount.getId())) {
      return listAccount;
    }
    JamfAccount detail = JamfApiCommands.retrieveAccountById(configId, listAccount.getId());
    return detail != null ? detail : listAccount;
  }

  /**
   * Look up an existing Jamf account for the reactivate path WITHOUT the disabled/ignore filter that
   * the normal retrieve applies. Prefers the native id (stable across a rename), falling back to the
   * name (EPPN). Returns null if no such account exists (so insertEntity creates a new one).
   * @param configId the external system config id
   * @param account the account being inserted (id may be set from a prior link; name is the EPPN)
   * @return the existing account, or null
   */
  private JamfAccount findExistingAccountUnfiltered(String configId, JamfAccount account) {
    JamfAccount existing = null;
    if (!StringUtils.isBlank(account.getId())) {
      existing = JamfApiCommands.retrieveAccountById(configId, account.getId());
    }
    if (existing == null && !StringUtils.isBlank(account.getName())) {
      existing = JamfApiCommands.retrieveAccountByName(configId, account.getName());
    }
    return existing;
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
      boolean disableInsteadOfDelete = config.isDisableEntitiesInsteadOfDelete();

      // The GET /accounts list carries only id+name. To filter out disabled ("soft-deleted")
      // accounts we must read each account's <enabled>, and to match the ignore list on
      // email/email_address we must read those -- so pull per-account detail when either applies.
      boolean needDetail = disableInsteadOfDelete || !ignoreAccounts.isEmpty();

      // all accounts (entities) + a name->nativeId index for membership resolution.
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      Map<String, String> nameToId = new LinkedHashMap<String, String>();
      for (JamfAccount listAccount : JamfApiCommands.retrieveAccounts(configId, ignoreAccounts)) {
        JamfAccount account = needDetail ? withDetail(configId, listAccount) : listAccount;

        // disable-instead-of-delete: a disabled account is treated as absent, so the framework
        // re-reads it (retrieveEntity, also filtered) and insertEntity reactivates it rather than
        // creating a duplicate. See SCIM's isDisableEntitiesInsteadOfDelete filter.
        if (disableInsteadOfDelete && account.isDisabled()) {
          continue;
        }
        // ignore list matches name/email/email_address -- never surface an ignored account
        if (JamfApiCommands.isAccountIgnored(account, ignoreAccounts)) {
          continue;
        }
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
  // Retrieve single entity: by id (the stable link, survives rename) or by name (EPPN backup)
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(
          targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      // match-by-id uses the native userid endpoint; the name backup uses the username endpoint
      JamfAccount account;
      if (StringUtils.equals("id", searchAttribute)) {
        account = JamfApiCommands.retrieveAccountById(configId, searchValue);
      } else {
        account = JamfApiCommands.retrieveAccountByName(configId, searchValue);
      }

      // treat ignored and (when disable-instead-of-delete) disabled accounts as not found, so the
      // framework reconciles them as absent -- consistent with the retrieveAllData filter above.
      // insertEntity re-reads WITHOUT this filter to reactivate a disabled account.
      if (account != null) {
        if (JamfApiCommands.isAccountIgnored(account, ignoreAccountNames(config))) {
          account = null;
        } else if (config.isDisableEntitiesInsteadOfDelete() && account.isDisabled()) {
          account = null;
        }
      }

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
      // an ignored account must never be provisionable. Reaching insertEntity means a subject on the
      // ignore list is a member of a provisioned group -- a real conflict the admin needs to resolve
      // (remove them from the group, or take them off the ignore list). Throw so the framework records
      // a per-member error (visible in the daemon log / sync status) instead of diverging silently.
      if (JamfApiCommands.isAccountIgnored(account, ignoreAccountNames(config))) {
        throw new RuntimeException("account '" + account.getName()
            + "' is on the Jamf ignore list and must not be provisioned; "
            + "remove it from the provisioned group or from the ignore list");
      }
      // Grouper-created accounts inherit privileges from role membership
      account.setAccessLevel(config.getJamfNewAccountAccessLevel());

      // disable-instead-of-delete: an earlier "delete" only disabled the account, and reads filter
      // disabled accounts out, so the account can look absent while still existing in Jamf. Re-read
      // WITHOUT the disabled filter; if it is there, reactivate it (enable + refresh name/full_name/
      // email) rather than POSTing a duplicate (which would 409 on the unique name).
      JamfAccount existing = null;
      if (config.isDisableEntitiesInsteadOfDelete()) {
        existing = findExistingAccountUnfiltered(configId, account);
      }

      if (existing != null) {
        account.setId(existing.getId());
        account.setEnabled(JamfAccount.ENABLED);
        // reactivate and refresh the managed fields in one PUT
        JamfApiCommands.updateAccount(configId, existing.getId(), account,
            GrouperUtil.toSet("name", "fullName", "email", "enabled"));
        targetEntity.setId(existing.getId());
      } else {
        JamfAccount createdAccount = JamfApiCommands.createAccount(configId, account);
        targetEntity.setId(createdAccount.getId());
      }
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
  // Update entity (partial PUT of the changed, updatable attributes: name, fullName, email).
  // Matched by id, so a name change here is a safe in-place rename.
  // ============================

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(
      TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      // which attributes changed
      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change
          : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }
      // only these entity attributes are updatable (id is immutable; accessLevel is not managed here)
      fieldNamesToUpdate.retainAll(GrouperUtil.toSet("name", "fullName", "email"));

      if (!fieldNamesToUpdate.isEmpty()) {
        JamfAccount account = JamfAccount.fromProvisioningEntity(targetEntity);
        String accountId = targetEntity.getId();
        if (StringUtils.isBlank(accountId)) {
          throw new RuntimeException("account id is required for updateEntity");
        }
        // never modify an ignore-listed account (name/email/email_address)
        if (!JamfApiCommands.isAccountIgnored(account, ignoreAccountNames(config))) {
          JamfApiCommands.updateAccount(configId, accountId, account, fieldNamesToUpdate);
        }
      }
      markProvisioned(targetEntity, true);

      return new TargetDaoUpdateEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  // ============================
  // Delete entity (hard-delete the account). Gated by provisioner config so Grouper only deletes
  // accounts it manages (set deleteEntitiesIfNotExistInGrouper=false to avoid touching pre-existing
  // accounts). Accounts on the ignore list are never deleted.
  // ============================

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(
      TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      JamfProvisionerConfiguration config = getJamfConfiguration();
      String configId = config.getJamfExternalSystemConfigId();

      JamfAccount account = JamfAccount.fromProvisioningEntity(targetEntity);

      // never delete/disable an account on the ignore list (protect break-glass / service admins)
      if (JamfApiCommands.isAccountIgnored(account, ignoreAccountNames(config))) {
        markProvisioned(targetEntity, true);
        return new TargetDaoDeleteEntityResponse();
      }

      String accountId = targetEntity.getId();
      if (StringUtils.isBlank(accountId)) {
        throw new RuntimeException("account id is required for deleteEntity");
      }
      if (config.isDisableEntitiesInsteadOfDelete()) {
        // soft delete: disable the account instead of removing it. Reads filter disabled accounts,
        // and a later insert reactivates it (see insertEntity).
        JamfApiCommands.setAccountEnabled(configId, accountId, JamfAccount.DISABLED);
        account.setEnabled(JamfAccount.DISABLED);
      } else {
        JamfApiCommands.deleteAccount(configId, accountId);
      }
      markProvisioned(targetEntity, true);

      return new TargetDaoDeleteEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
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

      Set<String> ignoreAccounts = ignoreAccountNames(config);
      Set<String> memberNames = new LinkedHashSet<String>();
      for (ProvisioningMembership membership : GrouperUtil.nonNull(targetMemberships)) {
        String name = membershipAccountName(membership);
        if (!StringUtils.isBlank(name) && !JamfApiCommands.isIgnored(name, ignoreAccounts)) {
          memberNames.add(name);
        }
      }

      // A full replace sends the complete member list, so it would drop members Grouper does not know
      // about -- including ignored accounts (which Grouper never sees). Re-add any ignored members
      // currently on the role so the replace never removes an ignore-listed account. The incremental
      // path preserves them naturally via retrieve-modify-write; this makes full sync match.
      if (!ignoreAccounts.isEmpty() && !StringUtils.isBlank(groupId)) {
        JamfAccountGroup currentRole = JamfApiCommands.retrieveAccountGroup(configId, groupId);
        if (currentRole != null) {
          for (String currentMember : GrouperUtil.nonNull(currentRole.getMembers())) {
            if (JamfApiCommands.isIgnored(currentMember, ignoreAccounts)) {
              memberNames.add(currentMember);
            }
          }
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

    // accounts (entities): create + update (name/fullName/email) + delete (delete gated by config)
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);

    // memberships: full-list replace + incremental add/remove (retrieve-modify-write)
    grouperProvisionerDaoCapabilities.setCanReplaceGroupMemberships(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
  }

}
