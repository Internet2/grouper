package edu.internet2.middleware.grouper.app.ldapProvisioning;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Rules for LDAP group dns that come from group metadata (md_grouper_ldapGroupDnOverride), and for
 * the bushy-folder parent walk.
 *
 * The override is set per group by anyone who can assign the provisioner to a group (that can be a
 * delegated, non-wheel group via groupAllowedToAssign), but it is used with Grouper's LDAP bind: the
 * provisioner reads the entry at that dn, writes its membership attribute, and deletes it when the
 * group is unprovisioned.  So the save is the gate:
 * <ul>
 *   <li>a sysadmin (wheel member or GrouperSystem) may set any dn,</li>
 *   <li>anyone else must give a dn that parses and is strictly below groupSearchBaseDn (a blank
 *   base means nothing can be checked, so a non-sysadmin cannot set an override at all).</li>
 * </ul>
 * At sync time the stored value is trusted: the provisioning metadata attribute can only be written
 * directly (attribute API, WS, UI attribute screens) by a sysadmin, and the provisioning screens
 * and ProvisionableGroupSave go through the save check.
 *
 * The bushy-folder walk uses the same dn rule: parent entries are only created or deleted strictly
 * below the base; for a dn outside the base (set by a sysadmin) the parents must already exist.
 *
 * Dn comparison is by javax.naming.ldap.LdapName (per RDN, attribute types and values
 * case-insensitive).  Plain JDK plus Grouper core (no commons-lang, no LDAP library), so the same
 * file works on every release.
 */
public class LdapGroupDnOverrideGuard {

  /** metadata item name of the group dn override */
  public static final String MD_GROUP_DN_OVERRIDE = "md_grouper_ldapGroupDnOverride";

  /**
   * no instances
   */
  private LdapGroupDnOverrideGuard() {
  }

  // ------------------------------------------------------------------------------------------------
  // dn checks

  /**
   * @param value string or null
   * @return true if null or only whitespace
   */
  private static boolean isBlank(String value) {
    return value == null || value.trim().length() == 0;
  }

  /**
   * @param dn a dn, e.g. cn=a,ou=groups,dc=example,dc=edu
   * @param baseDn the base, e.g. ou=groups,dc=example,dc=edu
   * @return true if both parse and dn is a strict descendant of baseDn (the base itself is not)
   */
  public static boolean isStrictlyUnderBase(String dn, String baseDn) {
    if (isBlank(dn) || isBlank(baseDn)) {
      return false;
    }
    try {
      LdapName dnName = new LdapName(dn.trim());
      LdapName baseName = new LdapName(baseDn.trim());
      if (baseName.size() == 0) {
        // an empty base would make every dn a descendant: treat as not configured
        return false;
      }
      // LdapName.startsWith compares the rightmost RDNs, i.e. the ancestors
      return dnName.size() > baseName.size() && dnName.startsWith(baseName);
    } catch (InvalidNameException ine) {
      return false;
    } catch (RuntimeException re) {
      // anything else unparseable is not under the base either
      return false;
    }
  }

  /**
   * The dn rule alone (for a saver who is not a sysadmin).
   * @param overrideDn value of md_grouper_ldapGroupDnOverride (blank is fine, it means no override)
   * @param baseDn the provisioner's groupSearchBaseDn
   * @return null if the override is blank or under the base, else an error message for the user
   */
  public static String overrideError(String overrideDn, String baseDn) {
    if (isBlank(overrideDn)) {
      return null;
    }
    if (isBlank(baseDn)) {
      return "The LDAP group DN override can only be set by a system administrator because the provisioner has no group search base DN";
    }
    if (!isStrictlyUnderBase(overrideDn, baseDn)) {
      return "The LDAP group DN override must be a valid DN under the group search base DN: " + baseDn.trim();
    }
    return null;
  }

  // ------------------------------------------------------------------------------------------------
  // save time

  /**
   * @param subject subject saving, may be null
   * @return true if the subject is GrouperSystem or a wheel member (the release's normal check)
   */
  public static boolean isSysadmin(Subject subject) {
    if (subject == null) {
      return false;
    }
    // PrivilegeHelper.isWheelOrRoot reads the static session; without one nobody is a sysadmin
    if (GrouperSession.staticGrouperSession(false) == null) {
      return false;
    }
    return PrivilegeHelper.isWheelOrRoot(subject);
  }

  /**
   * @return the subject of the current grouper session (the subject saving), or null if none
   */
  public static Subject currentSubjectOrNull() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    return grouperSession == null ? null : grouperSession.getSubject();
  }

  /**
   * Save-time check: a sysadmin may set any dn, anyone else only a dn under the base.
   * @param overrideDn value of md_grouper_ldapGroupDnOverride
   * @param baseDn the provisioner's groupSearchBaseDn
   * @param saver subject saving the metadata (null is treated as not a sysadmin)
   * @return null if allowed, else an error message for the user
   */
  public static String overrideErrorForSaver(String overrideDn, String baseDn, Subject saver) {
    if (isBlank(overrideDn)) {
      return null;
    }
    if (isSysadmin(saver)) {
      return null;
    }
    return overrideError(overrideDn, baseDn);
  }
}
