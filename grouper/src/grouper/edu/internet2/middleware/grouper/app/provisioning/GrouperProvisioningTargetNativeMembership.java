package edu.internet2.middleware.grouper.app.provisioning;

/**
 * A native-target membership (user-in-group), as returned by the target system's
 * select pass. Used purely to populate the generic provisioner reporting tables
 * (grouper_prov_mship, grouper_prov_mship_role).
 *
 * <p>This is independent of {@link ProvisioningMembership} / {@link ProvisioningMembershipWrapper},
 * which are bounded by Grouper's provisioning scope. Native-target reporting can include
 * memberships in the target system where neither side is tracked by Grouper.
 */
public class GrouperProvisioningTargetNativeMembership {

  /** the target user's identifier (matches targetId on a native user, e.g. an LDAP DN) */
  private String targetUserId;

  /** the target group's identifier (matches targetId on a native group) */
  private String targetGroupId;

  /** optional role name (e.g. "owner", "admin"); null implies the default role */
  private String roleName;

  public String getTargetUserId() {
    return targetUserId;
  }

  public void setTargetUserId(String targetUserId) {
    this.targetUserId = targetUserId;
  }

  public String getTargetGroupId() {
    return targetGroupId;
  }

  public void setTargetGroupId(String targetGroupId) {
    this.targetGroupId = targetGroupId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

}
