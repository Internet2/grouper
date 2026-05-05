package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Convert LDAP entries into the generic native-target reporting beans, and compute the
 * additional LDAP attribute names that need to be requested from the directory based on
 * the provisioner's nativeAttributesJsonGroups / nativeAttributesJsonEntities config.
 *
 * <p>The DAO uses these helpers in two places per select pass:
 * <ol>
 *   <li>Before issuing the LDAP search, call
 *       {@link #widenLdapAttributeNamesForGroups(Set, List)} or
 *       {@link #widenLdapAttributeNamesForEntities(Set, List)} so the directory returns
 *       the report-only attributes alongside the configured ones.</li>
 *   <li>For each {@link LdapEntry} returned, call
 *       {@link #buildNativeGroup(LdapEntry)} or {@link #buildNativeUser(LdapEntry)} and
 *       append to {@code data.getTargetNativeGroups()} / {@code data.getTargetNativeUsers()}.</li>
 * </ol>
 */
public class LdapProvisioningTargetNativeBuilder {

  /**
   * Add the LDAP attribute names called for by the group native-attribute config to
   * the existing search attribute set. Set semantics dedup against already-configured names.
   */
  public static void widenLdapAttributeNamesForGroups(Set<String> ldapAttributeNames,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributesGroups) {
    widenLdapAttributeNames(ldapAttributeNames, nativeAttributesGroups);
  }

  /**
   * Add the LDAP attribute names called for by the entity native-attribute config to
   * the existing search attribute set. Set semantics dedup against already-configured names.
   */
  public static void widenLdapAttributeNamesForEntities(Set<String> ldapAttributeNames,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributesEntities) {
    widenLdapAttributeNames(ldapAttributeNames, nativeAttributesEntities);
  }

  private static void widenLdapAttributeNames(Set<String> ldapAttributeNames,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributes) {
    if (ldapAttributeNames == null) {
      return;
    }
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

  /**
   * Build a native group bean from an LDAP entry. {@code targetId} is the DN; attribute
   * map collects all attributes returned by the LDAP search (the DAO is expected to have
   * already widened the attribute list to include any report-only attrs).
   */
  public static GrouperProvisioningTargetNativeGroup buildNativeGroup(LdapEntry ldapEntry) {
    GrouperProvisioningTargetNativeGroup grouperProvisioningTargetNativeGroup = new GrouperProvisioningTargetNativeGroup();
    grouperProvisioningTargetNativeGroup.setTargetId(ldapEntry == null ? null : ldapEntry.getDn());
    populateAttributesFromLdapEntry(grouperProvisioningTargetNativeGroup.getAttributes(), ldapEntry);
    return grouperProvisioningTargetNativeGroup;
  }

  /**
   * Build a native user bean from an LDAP entry. {@code targetId} is the DN; attribute
   * map collects all attributes returned by the LDAP search.
   */
  public static GrouperProvisioningTargetNativeUser buildNativeUser(LdapEntry ldapEntry) {
    GrouperProvisioningTargetNativeUser grouperProvisioningTargetNativeUser = new GrouperProvisioningTargetNativeUser();
    grouperProvisioningTargetNativeUser.setTargetId(ldapEntry == null ? null : ldapEntry.getDn());
    populateAttributesFromLdapEntry(grouperProvisioningTargetNativeUser.getAttributes(), ldapEntry);
    return grouperProvisioningTargetNativeUser;
  }

  private static void populateAttributesFromLdapEntry(java.util.Map<String, Object> destination, LdapEntry ldapEntry) {
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

  /**
   * Extract native memberships from a group LdapEntry. For each value of
   * {@code groupMembershipAttributeName} on the group, append a {@link GrouperProvisioningTargetNativeMembership}
   * with {@code targetGroupId} = the group's DN and {@code targetUserId} = the membership value.
   *
   * <p>Whether a value matches a {@code grouper_prov_user.target_user_id} (typically a DN) depends on
   * the provisioner's membership representation: groupOfNames-style {@code member} attributes hold DNs
   * (matches), while posix-style {@code description} attributes hold subjectIds (won't match without a
   * resolver). Unmatched memberships are dropped at load time — failsafe.
   *
   * @param destination          where new membership entries are appended; must be non-null
   * @param groupLdapEntry       the LDAP group entry just retrieved
   * @param groupMembershipAttributeName the attribute on the group that holds member references
   *                             (e.g. "member", "uniqueMember", "description"); no-op if blank
   */
  public static void appendNativeMembershipsFromGroupEntry(
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
   * {@link #appendNativeMembershipsFromGroupEntry} for the entity-attribute style:
   * each value of {@code entityMembershipAttributeName} on the user becomes a membership
   * with {@code targetUserId} = the user's DN and {@code targetGroupId} = the value.
   */
  public static void appendNativeMembershipsFromEntityEntry(
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

  /** in-place de-dup helper exposed for tests / external consumers */
  static Set<String> caseInsensitiveSet() {
    return new LinkedHashSet<String>();
  }

}
