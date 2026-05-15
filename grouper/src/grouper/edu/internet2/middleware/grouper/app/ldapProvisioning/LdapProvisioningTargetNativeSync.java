package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * LDAP-specific {@link GrouperProvisioningTargetNativeSync}: converts {@link LdapEntry}
 * objects into the generic native-target reporting beans, widens the LDAP search-attribute
 * set with the configured native-attribute names, and offers a per-entry capture convenience.
 *
 * <p>Typical DAO usage per retrieval method (read paths only — write capture is a future
 * opt-in via a {@code readAfterWrite} flag):
 * <ol>
 *   <li>Before issuing the LDAP search, call
 *       {@link #widenLdapAttributeNamesForGroups(Set)} or
 *       {@link #widenLdapAttributeNamesForEntities(Set)} so the directory returns
 *       the report-only attributes alongside the configured ones.</li>
 *   <li>For each {@link LdapEntry} returned, call
 *       {@link #captureGroupEntry(LdapEntry, String)} or
 *       {@link #captureEntityEntry(LdapEntry, String)} to build + record the native bean
 *       and (if applicable) the native memberships extracted from a membership attribute.</li>
 * </ol>
 *
 * <p>All methods no-op when the corresponding load flag is off, so callers do not need to
 * flag-check at the call site.
 */
public class LdapProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  // ----- widen --------------------------------------------------------------------------

  /**
   * Add the LDAP attribute names called for by {@code nativeAttributeConfigsGroups} to the
   * caller's existing search-attribute set. No-op when reporting is disabled.
   */
  public void widenLdapAttributeNamesForGroups(Set<String> ldapAttributeNames) {
    if (ldapAttributeNames == null) {
      return;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadGroupsToGenericGrouperTable()) {
      return;
    }
    widenLdapAttributeNames(ldapAttributeNames,
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getNativeAttributeConfigsGroups());
  }

  /**
   * Add the LDAP attribute names called for by {@code nativeAttributeConfigsEntities} to the
   * caller's existing search-attribute set. No-op when reporting is disabled.
   */
  public void widenLdapAttributeNamesForEntities(Set<String> ldapAttributeNames) {
    if (ldapAttributeNames == null) {
      return;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGenericGrouperTable()) {
      return;
    }
    widenLdapAttributeNames(ldapAttributeNames,
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getNativeAttributeConfigsEntities());
  }

  private static void widenLdapAttributeNames(Set<String> ldapAttributeNames,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributes) {
    for (GrouperProvisioningNativeAttributeConfig grouperProvisioningNativeAttributeConfig
        : GrouperUtil.nonNull(nativeAttributes)) {
      String ldapName = StringUtils.defaultIfBlank(
          grouperProvisioningNativeAttributeConfig.getPath(),
          grouperProvisioningNativeAttributeConfig.getName());
      if (!StringUtils.isBlank(ldapName)) {
        ldapAttributeNames.add(ldapName);
      }
    }
  }

  // ----- build --------------------------------------------------------------------------

  /**
   * Build a native group bean from an LDAP entry. {@code targetId} is the DN; attribute
   * map collects all attributes returned by the LDAP search.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroup(LdapEntry ldapEntry) {
    GrouperProvisioningTargetNativeGroup grouperProvisioningTargetNativeGroup = new GrouperProvisioningTargetNativeGroup();
    grouperProvisioningTargetNativeGroup.setTargetId(ldapEntry == null ? null : ldapEntry.getDn());
    populateAttributesFromLdapEntry(grouperProvisioningTargetNativeGroup.getAttributes(), ldapEntry);
    return grouperProvisioningTargetNativeGroup;
  }

  /**
   * Build a native user bean from an LDAP entry. {@code targetId} is the DN; attribute
   * map collects all attributes returned by the LDAP search.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUser(LdapEntry ldapEntry) {
    GrouperProvisioningTargetNativeUser grouperProvisioningTargetNativeUser = new GrouperProvisioningTargetNativeUser();
    grouperProvisioningTargetNativeUser.setTargetId(ldapEntry == null ? null : ldapEntry.getDn());
    populateAttributesFromLdapEntry(grouperProvisioningTargetNativeUser.getAttributes(), ldapEntry);
    return grouperProvisioningTargetNativeUser;
  }

  private static void populateAttributesFromLdapEntry(Map<String, Object> destination, LdapEntry ldapEntry) {
    if (destination == null || ldapEntry == null) {
      return;
    }
    for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
      java.util.Collection<Object> values = ldapAttribute.getValues();
      if (values == null) {
        destination.put(ldapAttribute.getName(), null);
        continue;
      }
      if (values.size() == 0) {
        // attribute returned with no values (rare in LDAP but possible) — preserve as empty
        destination.put(ldapAttribute.getName(), Collections.emptyList());
      } else if (values.size() == 1) {
        destination.put(ldapAttribute.getName(), values.iterator().next());
      } else {
        // multi-valued — keep as a list
        destination.put(ldapAttribute.getName(), new ArrayList<Object>(values));
      }
    }
  }

  // ----- memberships --------------------------------------------------------------------

  /**
   * Extract native memberships from a group LdapEntry. For each value of
   * {@code groupMembershipAttributeName} on the group, append a {@link GrouperProvisioningTargetNativeMembership}
   * with {@code targetGroupId} = the group's DN and {@code targetUserId} = the membership value.
   */
  public void appendNativeMembershipsFromGroupEntry(
      List<GrouperProvisioningTargetNativeMembership> destination,
      LdapEntry groupLdapEntry, String groupMembershipAttributeName) {

    if (destination == null || groupLdapEntry == null
        || StringUtils.isBlank(groupMembershipAttributeName)) {
      return;
    }
    String groupDn = groupLdapEntry.getDn();
    if (StringUtils.isBlank(groupDn)) {
      return;
    }
    LdapAttribute ldapAttribute = groupLdapEntry.getAttribute(groupMembershipAttributeName);
    if (ldapAttribute == null) {
      return;
    }
    for (Object memberValue : GrouperUtil.nonNull(ldapAttribute.getValues())) {
      String memberAsString = StringUtils.trimToNull(GrouperUtil.stringValue(memberValue));
      if (memberAsString == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership grouperProvisioningTargetNativeMembership = new GrouperProvisioningTargetNativeMembership();
      grouperProvisioningTargetNativeMembership.setTargetGroupId(groupDn);
      grouperProvisioningTargetNativeMembership.setTargetUserId(memberAsString);
      destination.add(grouperProvisioningTargetNativeMembership);
    }
  }

  /**
   * Extract native memberships from an entity LdapEntry. Mirror of
   * {@link #appendNativeMembershipsFromGroupEntry}: each value of
   * {@code entityMembershipAttributeName} on the user becomes a membership
   * with {@code targetUserId} = the user's DN and {@code targetGroupId} = the value.
   */
  public void appendNativeMembershipsFromEntityEntry(
      List<GrouperProvisioningTargetNativeMembership> destination,
      LdapEntry entityLdapEntry, String entityMembershipAttributeName) {

    if (destination == null || entityLdapEntry == null
        || StringUtils.isBlank(entityMembershipAttributeName)) {
      return;
    }
    String entityDn = entityLdapEntry.getDn();
    if (StringUtils.isBlank(entityDn)) {
      return;
    }
    LdapAttribute ldapAttribute = entityLdapEntry.getAttribute(entityMembershipAttributeName);
    if (ldapAttribute == null) {
      return;
    }
    for (Object groupValue : GrouperUtil.nonNull(ldapAttribute.getValues())) {
      String groupAsString = StringUtils.trimToNull(GrouperUtil.stringValue(groupValue));
      if (groupAsString == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership grouperProvisioningTargetNativeMembership = new GrouperProvisioningTargetNativeMembership();
      grouperProvisioningTargetNativeMembership.setTargetUserId(entityDn);
      grouperProvisioningTargetNativeMembership.setTargetGroupId(groupAsString);
      destination.add(grouperProvisioningTargetNativeMembership);
    }
  }

  // ----- capture convenience (build + record in one call) ------------------------------

  /**
   * Full per-entry capture for a group: build the native group bean, record it, then
   * extract and record any native memberships found on {@code groupMembershipAttributeName}.
   * No-ops cascade through the recorder methods, so this is safe to call unconditionally.
   */
  public void captureGroupEntry(LdapEntry ldapEntry, String groupMembershipAttributeName) {
    if (ldapEntry == null) {
      return;
    }
    this.recordTargetNativeGroup(this.buildNativeGroup(ldapEntry));
    if (!StringUtils.isBlank(groupMembershipAttributeName)) {
      List<GrouperProvisioningTargetNativeMembership> memberships =
          new ArrayList<GrouperProvisioningTargetNativeMembership>();
      this.appendNativeMembershipsFromGroupEntry(memberships, ldapEntry, groupMembershipAttributeName);
      this.recordTargetNativeMemberships(memberships);
    }
  }

  /**
   * Full per-entry capture for an entity (user): build the native user bean, record it,
   * then extract and record any native memberships found on {@code entityMembershipAttributeName}.
   */
  public void captureEntityEntry(LdapEntry ldapEntry, String entityMembershipAttributeName) {
    if (ldapEntry == null) {
      return;
    }
    this.recordTargetNativeUser(this.buildNativeUser(ldapEntry));
    if (!StringUtils.isBlank(entityMembershipAttributeName)) {
      List<GrouperProvisioningTargetNativeMembership> memberships =
          new ArrayList<GrouperProvisioningTargetNativeMembership>();
      this.appendNativeMembershipsFromEntityEntry(memberships, ldapEntry, entityMembershipAttributeName);
      this.recordTargetNativeMemberships(memberships);
    }
  }

}
