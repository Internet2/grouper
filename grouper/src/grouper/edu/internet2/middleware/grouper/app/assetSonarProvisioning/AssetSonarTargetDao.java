package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * AssetSonar TargetDao -- members only (no groups, no memberships).
 *
 * <ul>
 *   <li><b>Select</b> reads the active list AND the inactive list. A deactivated member is hidden
 *       from the default list; without the inactive read Grouper would think it gone, drop its
 *       sync row after 7 days, and lose the member id.</li>
 *   <li><b>Insert</b> creates the member. If the email is already taken (a member the select could
 *       not see), the 403 carries no id, so the member is looked up by email and reactivated by
 *       id with status=1.</li>
 *   <li><b>Update</b> PUTs the changed attributes plus, always, the email.</li>
 *   <li><b>Delete</b> never DELETEs: it deactivates (status=0), because members carry asset
 *       checkout history.</li>
 * </ul>
 *
 * <p>Tiers whose Grouper group is not configured are unmanaged: an update never moves a member
 * out of one, and a delete never deactivates a member in one. See
 * {@link AssetSonarProvisioningTranslator}.</p>
 */
public class AssetSonarTargetDao extends GrouperProvisionerTargetDaoBase {

  /** attributes Grouper can write, in the order they are sent */
  public static final List<String> WRITABLE_ATTRIBUTES = Collections.unmodifiableList(Arrays.asList(
      AssetSonarMember.ATTR_EMAIL,
      AssetSonarMember.ATTR_FIRST_NAME,
      AssetSonarMember.ATTR_LAST_NAME,
      AssetSonarMember.ATTR_EMPLOYEE_ID,
      AssetSonarMember.ATTR_EMPLOYEE_IDENTIFICATION_NUMBER,
      AssetSonarMember.ATTR_ROLE_ID,
      AssetSonarMember.ATTR_STATUS));

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  private AssetSonarProvisionerConfiguration getAssetSonarConfiguration() {
    return (AssetSonarProvisionerConfiguration) this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration();
  }

  // ============================
  // Retrieve all entities: every page of the active list, then of the inactive list
  // ============================

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();
    try {
      AssetSonarProvisionerConfiguration config = getAssetSonarConfiguration();

      Map<String, AssetSonarMember> members = AssetSonarApiCommands.retrieveAllMembers(
          config.getAssetSonarExternalSystemConfigId(), config.isAssetSonarSelectInactiveMembers());

      List<ProvisioningEntity> targetEntities = new ArrayList<ProvisioningEntity>();
      for (AssetSonarMember member : members.values()) {
        targetEntities.add(member.toProvisioningEntity());
      }
      return new TargetDaoRetrieveAllEntitiesResponse(targetEntities);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  // ============================
  // Retrieve one entity: by cached member id, or by email (filter=email finds inactive members too)
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();
    try {
      String configId = getAssetSonarConfiguration().getAssetSonarExternalSystemConfigId();
      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      AssetSonarMember member;
      if (StringUtils.equals(AssetSonarMember.ATTR_ID, searchAttribute)) {
        member = AssetSonarApiCommands.retrieveMemberById(configId, searchValue);
      } else if (StringUtils.equals(AssetSonarMember.ATTR_EMAIL, searchAttribute)) {
        member = AssetSonarApiCommands.retrieveMemberByEmail(configId, searchValue);
      } else {
        // email is the only working filter; anything else would be silently ignored by the API
        throw new RuntimeException("AssetSonar members can only be searched by id or email, not '" + searchAttribute + "'");
      }
      return new TargetDaoRetrieveEntityResponse(member == null ? null : member.toProvisioningEntity());
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  // ============================
  // Insert entity: create, or on 403 "email taken" look up by email and reactivate by id
  // ============================

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      String configId = getAssetSonarConfiguration().getAssetSonarExternalSystemConfigId();

      AssetSonarMember member = AssetSonarMember.fromProvisioningEntity(targetEntity);
      if (StringUtils.isBlank(member.getEmail())) {
        throw new RuntimeException("email is required to create an AssetSonar member");
      }

      Map<String, String> userParams = userParams(member, WRITABLE_ATTRIBUTES);
      String memberId = AssetSonarApiCommands.createMember(configId, userParams);

      if (memberId == null) {
        // The email belongs to a member the select did not see (normally a deactivated one if the
        // inactive read is off or the filter changed). The 403 body carries no id, so find it by
        // email and reactivate it by id instead of failing on every sync.
        AssetSonarMember existing = AssetSonarApiCommands.retrieveMemberByEmail(configId, member.getEmail());
        if (existing == null) {
          throw new RuntimeException("AssetSonar says email '" + member.getEmail()
              + "' is taken but no member is found by that email");
        }
        memberId = existing.getId();
        userParams.put(AssetSonarMember.ATTR_STATUS, AssetSonarMember.STATUS_ACTIVE);
        AssetSonarApiCommands.updateMember(configId, memberId, userParams);
      }

      targetEntity.setId(memberId);
      // sync-back: create/reactivate returns no member body, so the drain re-reads it
      AssetSonarProvisioningTargetNativeSync.recordMemberWriteFromCurrentProvisioner(memberId);
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
  // Update entity: the changed writable attributes, plus the email every time
  // ============================

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      AssetSonarProvisionerConfiguration config = getAssetSonarConfiguration();
      String configId = config.getAssetSonarExternalSystemConfigId();

      String memberId = targetEntity.getId();
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("member id is required for updateEntity");
      }

      Set<String> changedAttributes = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String attributeName = change.getAttributeName();
        if (!WRITABLE_ATTRIBUTES.contains(attributeName)) {
          continue;
        }
        if (AssetSonarMember.ATTR_ROLE_ID.equals(attributeName)
            && isUnmanagedTier(config, GrouperUtil.stringValue(change.getOldValue()))) {
          // the member holds a tier Grouper does not manage (e.g. a hand-made Administrator while no
          // administrator group is configured): leave the tier alone
          continue;
        }
        changedAttributes.add(attributeName);
      }

      if (!changedAttributes.isEmpty()) {
        AssetSonarMember member = AssetSonarMember.fromProvisioningEntity(targetEntity);
        if (StringUtils.isBlank(member.getEmail())) {
          throw new RuntimeException("email is required to update AssetSonar member " + memberId);
        }
        // email always goes first, changed or not, as the guard against the external_id clearing bug.
        // A changed attribute whose new value is blank is skipped (filterUserParams), so a source data
        // gap cannot blank out a good target value.
        changedAttributes.add(AssetSonarMember.ATTR_EMAIL);
        List<String> attributeOrder = new ArrayList<String>();
        for (String attributeName : WRITABLE_ATTRIBUTES) {
          if (changedAttributes.contains(attributeName)) {
            attributeOrder.add(attributeName);
          }
        }
        AssetSonarApiCommands.updateMember(configId, memberId, userParams(member, attributeOrder));
        AssetSonarProvisioningTargetNativeSync.recordMemberWriteFromCurrentProvisioner(memberId);
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
  // Delete entity: deactivate (status=0). Never DELETE -- it would orphan asset checkout history.
  // ============================

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      AssetSonarProvisionerConfiguration config = getAssetSonarConfiguration();
      String configId = config.getAssetSonarExternalSystemConfigId();

      String memberId = targetEntity.getId();
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("member id is required for deleteEntity");
      }

      AssetSonarMember member = AssetSonarMember.fromProvisioningEntity(targetEntity);

      // the select returns the inactive members too, so most "deletes" of out-of-scope members are
      // already deactivated: nothing to do
      if (member.isInactive()) {
        markProvisioned(targetEntity, true);
        return new TargetDaoDeleteEntityResponse();
      }

      // the entity may not carry the email or tier (e.g. built from the sync table); read it so the
      // email guard can be sent and the unmanaged-tier rule applied
      if (StringUtils.isBlank(member.getEmail()) || StringUtils.isBlank(member.getRoleId())) {
        AssetSonarMember current = AssetSonarApiCommands.retrieveMemberById(configId, memberId);
        if (current == null || current.isInactive()) {
          markProvisioned(targetEntity, true);
          return new TargetDaoDeleteEntityResponse();
        }
        member = current;
      }

      if (isUnmanagedTier(config, member.getRoleId())) {
        // never deactivate someone holding a tier Grouper does not manage
        markProvisioned(targetEntity, true);
        return new TargetDaoDeleteEntityResponse();
      }

      Map<String, String> userParams = new LinkedHashMap<String, String>();
      userParams.put(AssetSonarMember.ATTR_EMAIL, member.getEmail());
      userParams.put(AssetSonarMember.ATTR_STATUS, AssetSonarMember.STATUS_INACTIVE);
      AssetSonarApiCommands.updateMember(configId, memberId, userParams);
      // sync-back: a deactivation is a write, not a delete -- the member still exists with status 0
      AssetSonarProvisioningTargetNativeSync.recordMemberWriteFromCurrentProvisioner(memberId);

      markProvisioned(targetEntity, true);
      return new TargetDaoDeleteEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  /**
   * A tier is unmanaged when its role id is configured on the external system (so it can be
   * recognized) but its Grouper group is not configured on the provisioner.
   * @param config provisioner config
   * @param roleId a member's current role id
   * @return true if Grouper must not move the member out of this tier or deactivate it
   */
  boolean isUnmanagedTier(AssetSonarProvisionerConfiguration config, String roleId) {
    if (StringUtils.isBlank(roleId)) {
      return false;
    }
    String configId = config.getAssetSonarExternalSystemConfigId();
    if (StringUtils.isBlank(config.getAssetSonarAdministratorGroupName())
        && StringUtils.equals(roleId, AssetSonarExternalSystem.retrieveConfigValue(configId, "administratorRoleId", false))) {
      return true;
    }
    if (StringUtils.isBlank(config.getAssetSonarAgentGroupName())
        && StringUtils.equals(roleId, AssetSonarExternalSystem.retrieveConfigValue(configId, "agentRoleId", false))) {
      return true;
    }
    return false;
  }

  /**
   * @param member the member holding the values
   * @param attributeNames which attributes to include, in order
   * @return attribute name to value (blank values are dropped later by the api commands)
   */
  private static Map<String, String> userParams(AssetSonarMember member, List<String> attributeNames) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    for (String attributeName : attributeNames) {
      result.put(attributeName, member.attributeValue(attributeName));
    }
    return result;
  }

  /**
   * Mark a provisioning object and all its object-changes as provisioned or not.
   * @param provisioningObject the entity
   * @param provisioned true if the write succeeded
   */
  private static void markProvisioned(ProvisioningUpdatable provisioningObject, boolean provisioned) {
    if (provisioningObject == null) {
      return;
    }
    provisioningObject.setProvisioned(provisioned);
    for (ProvisioningObjectChange change : GrouperUtil.nonNull(provisioningObject.getInternal_objectChanges())) {
      change.setProvisioned(provisioned);
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    // "delete" is a deactivation, see deleteEntity
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);

    // sync-back: members are captured at the AssetSonarApiCommands read seams and writes are
    // recorded for re-read (AssetSonarProvisioningTargetNativeSync)
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);
    // retrieveAllEntities is skipped by the framework when users come from the sync-back cache,
    // and the default capture list holds every managed attribute, so the cache can stand in for
    // the member paging
    grouperProvisionerDaoCapabilities.setCanFullSyncEntitiesFromSyncBack(true);
  }

}
