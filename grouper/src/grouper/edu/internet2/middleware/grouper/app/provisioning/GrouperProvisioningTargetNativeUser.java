package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A native-target user, as returned by the target system's select pass.
 * Used purely to populate the generic provisioner reporting tables
 * (grouper_prov_user, grouper_prov_user_attr, grouper_prov_user_attr_value).
 *
 * <p>This is independent of {@link ProvisioningEntity} / {@link ProvisioningEntityWrapper},
 * which are bounded by Grouper's provisioning scope. Native-target reporting can include
 * users in the target system that Grouper does not track.
 */
public class GrouperProvisioningTargetNativeUser {

  /** the target system's unique identifier for this user (e.g. LDAP DN, SCIM id) */
  private String targetId;

  /** optional foreign key to grouper_members.internal_id; null if Grouper does not track this user */
  private Long memberInternalId;

  /**
   * attribute name to value. value may be a single object (String / Boolean / Number / Date /
   * Timestamp) or a Collection for multi-valued attributes. Empty/blank values are preserved
   * so reporting can distinguish "attribute present, value empty" from "attribute not present".
   */
  private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }

  public Long getMemberInternalId() {
    return memberInternalId;
  }

  public void setMemberInternalId(Long memberInternalId) {
    this.memberInternalId = memberInternalId;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

}
