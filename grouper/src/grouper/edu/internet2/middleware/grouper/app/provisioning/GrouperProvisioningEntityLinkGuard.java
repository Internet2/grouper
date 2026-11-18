package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

/**
 * <p>Decides whether a Grouper entity that is already linked to a target entity may be matched to a
 * DIFFERENT target entity during matching (GrouperProvisioningMatchingIdIndex.indexMatchingIdEntities).</p>
 *
 * <p>Why: the matching index tries the CURRENT values of all matching attributes before the cached
 * (past) values.  Most provisioner wizards match on a mutable name first (SCIM userName, Duo loginId,
 * Okta/Box login, Google email, ...) and on the target id second.  When a member's desired name
 * changes to a value that another, pre-existing target account already holds (for example an account
 * an attacker created ahead of time), the current-name match wins and the member is silently moved to
 * that account: it then receives the member's provisioned memberships, and the member's real account
 * is orphaned (and may be deleted by deleteEntitiesIfNotExistInGrouper).</p>
 *
 * <p>The link: target-sourced entity attribute caches (entityAttributeValueCacheN with source=target,
 * type=entityAttribute, e.g. the target id) record which target object the member was linked to at the
 * last sync.  A candidate target entity "contradicts" the link when, for every cached attribute it can
 * be compared on, its current value differs from the cached value.  Agreeing on at least one cached
 * attribute (e.g. the same target id, even if the name changed out of band) is not a contradiction.</p>
 *
 * <p>Members with no target-sourced cache (never linked, or the link was cleared before a re-insert by
 * GrouperProvisioningLinkLogic.deleteEntityLink) are not affected: there is no link to protect.</p>
 *
 * <p>Security issue provisioningMatchingRelinksToPreclaimedAccount (sibling of
 * scimConflictAccountPreclaimRelink, which applies the same rule to the SCIM 409 recovery path).</p>
 */
public class GrouperProvisioningEntityLinkGuard {

  // ------------------------------------------------------------------ provisioner-aware helpers

  /**
   * the values of the target-sourced attribute caches of the member, as compare values (lower case
   * if the attribute compares case-insensitive).  This is the member's existing link.
   * @param grouperProvisioner
   * @param provisioningEntityWrapper the Grouper-side wrapper
   * @return attribute name to compare value, never null, empty if there is no link
   */
  public static Map<String, String> linkedTargetValues(GrouperProvisioner grouperProvisioner,
      ProvisioningEntityWrapper provisioningEntityWrapper) {

    Map<String, String> result = new LinkedHashMap<String, String>();

    if (grouperProvisioner == null || provisioningEntityWrapper == null) {
      return result;
    }

    GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
    if (gcGrouperSyncMember == null) {
      return result;
    }

    GrouperProvisioningConfigurationAttributeDbCache[] entityAttributeDbCaches =
        grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches();
    if (entityAttributeDbCaches == null) {
      return result;
    }

    for (GrouperProvisioningConfigurationAttributeDbCache cache : entityAttributeDbCaches) {

      // only caches of a target attribute record which target object we are linked to; grouper-sourced
      // caches are what Grouper computed (e.g. an old subject attribute), not proof of identity, and
      // object caches hold a whole json entity (not used here)
      if (cache == null || cache.getSource() != GrouperProvisioningConfigurationAttributeDbCacheSource.target
          || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
        continue;
      }

      String attributeName = cache.getAttributeName();
      if (StringUtils.isBlank(attributeName) || result.containsKey(attributeName)) {
        continue;
      }

      Object cachedValue = gcGrouperSyncMember.retrieveField("entityAttributeValueCache" + cache.getIndex());
      if (GrouperUtil.isBlank(cachedValue)) {
        continue;
      }

      String compareValue = grouperProvisioner.retrieveGrouperProvisioningCompare()
          .attributeValueForCompareEntity(attributeName, cachedValue);
      if (!StringUtils.isBlank(compareValue)) {
        result.put(attributeName, compareValue);
      }
    }
    return result;
  }

  /**
   * current values of a target entity for some attributes, as compare values
   * @param grouperProvisioner
   * @param targetProvisioningEntity
   * @param attributeNames
   * @return attribute name to compare value (blank values left out), never null
   */
  public static Map<String, String> targetValues(GrouperProvisioner grouperProvisioner,
      ProvisioningEntity targetProvisioningEntity, Collection<String> attributeNames) {

    Map<String, String> result = new LinkedHashMap<String, String>();
    if (grouperProvisioner == null || targetProvisioningEntity == null) {
      return result;
    }
    for (String attributeName : GrouperUtil.nonNull(attributeNames)) {
      Object value = targetProvisioningEntity.retrieveAttributeValue(attributeName);
      if (GrouperUtil.isBlank(value)) {
        continue;
      }
      String compareValue = grouperProvisioner.retrieveGrouperProvisioningCompare()
          .attributeValueForCompareEntity(attributeName, value);
      if (!StringUtils.isBlank(compareValue)) {
        result.put(attributeName, compareValue);
      }
    }
    return result;
  }

  /**
   * if matching this Grouper entity to this target entity would move an existing link to a different
   * target object, return why (for the object error), else null
   * @param grouperProvisioner
   * @param provisioningEntityWrapper the Grouper-side wrapper being matched
   * @param candidateTargetEntity the target entity the matching value found
   * @return null if the match is allowed, else a short reason
   */
  public static String relinkRefusal(GrouperProvisioner grouperProvisioner,
      ProvisioningEntityWrapper provisioningEntityWrapper, ProvisioningEntity candidateTargetEntity) {

    if (grouperProvisioner == null || provisioningEntityWrapper == null || candidateTargetEntity == null) {
      return null;
    }

    // Grouper itself inserted this entity in this run (and cleared the old link before the insert);
    // matching it to the object it just created is never a relink
    ProvisioningStateEntity provisioningStateEntity = provisioningEntityWrapper.getProvisioningStateEntity();
    if (provisioningStateEntity != null && provisioningStateEntity.isInsertResultProcessed()) {
      return null;
    }

    Map<String, String> linkedValues = linkedTargetValues(grouperProvisioner, provisioningEntityWrapper);
    if (linkedValues.isEmpty()) {
      return null;
    }

    return linkContradiction(linkedValues,
        targetValues(grouperProvisioner, candidateTargetEntity, linkedValues.keySet()));
  }

  /**
   * if this target entity is the one the Grouper entity is linked to (agrees on at least one cached
   * target attribute).  Used to keep the existing link after a refused relink.
   * @param grouperProvisioner
   * @param provisioningEntityWrapper the Grouper-side wrapper
   * @param targetProvisioningEntity
   * @return true if it is the linked target entity
   */
  public static boolean isLinkedTargetEntity(GrouperProvisioner grouperProvisioner,
      ProvisioningEntityWrapper provisioningEntityWrapper, ProvisioningEntity targetProvisioningEntity) {

    Map<String, String> linkedValues = linkedTargetValues(grouperProvisioner, provisioningEntityWrapper);
    if (linkedValues.isEmpty()) {
      return false;
    }
    return linkAgrees(linkedValues,
        targetValues(grouperProvisioner, targetProvisioningEntity, linkedValues.keySet()));
  }

  // ------------------------------------------------------------------ pure decision logic (unit tested)

  /**
   * the candidate contradicts the link when at least one cached attribute can be compared and none of
   * the compared attributes agree
   * @param linkedValues attribute name to cached compare value (the existing link)
   * @param candidateValues attribute name to the candidate's current compare value
   * @return null if no contradiction (no link, nothing comparable, or some attribute agrees), else a
   * description like "linked id=abc, candidate id=def"
   */
  public static String linkContradiction(Map<String, String> linkedValues, Map<String, String> candidateValues) {

    if (linkedValues == null || linkedValues.isEmpty()) {
      return null;
    }

    boolean compared = false;
    StringBuilder description = new StringBuilder();

    for (String attributeName : linkedValues.keySet()) {
      String linkedValue = linkedValues.get(attributeName);
      String candidateValue = candidateValues == null ? null : candidateValues.get(attributeName);

      // the candidate has no value for this attribute: cannot tell, do not count it either way
      if (StringUtils.isBlank(linkedValue) || StringUtils.isBlank(candidateValue)) {
        continue;
      }
      compared = true;

      // same target object (e.g. same target id even though the name changed): allowed
      if (StringUtils.equals(linkedValue, candidateValue)) {
        return null;
      }

      if (description.length() > 0) {
        description.append(", ");
      }
      description.append("linked ").append(attributeName).append("=").append(linkedValue)
        .append(", candidate ").append(attributeName).append("=").append(candidateValue);
    }

    return compared ? description.toString() : null;
  }

  /**
   * @param linkedValues attribute name to cached compare value (the existing link)
   * @param candidateValues attribute name to the candidate's current compare value
   * @return true if at least one attribute is present on both sides and equal
   */
  public static boolean linkAgrees(Map<String, String> linkedValues, Map<String, String> candidateValues) {
    if (linkedValues == null || candidateValues == null) {
      return false;
    }
    for (String attributeName : linkedValues.keySet()) {
      String linkedValue = linkedValues.get(attributeName);
      if (!StringUtils.isBlank(linkedValue) && StringUtils.equals(linkedValue, candidateValues.get(attributeName))) {
        return true;
      }
    }
    return false;
  }

}
