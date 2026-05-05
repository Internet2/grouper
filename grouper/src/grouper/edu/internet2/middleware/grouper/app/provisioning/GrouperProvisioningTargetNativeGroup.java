package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A native-target group, as returned by the target system's select pass.
 * Used purely to populate the generic provisioner reporting tables
 * (grouper_prov_group, grouper_prov_group_attr, grouper_prov_group_attr_value).
 *
 * <p>This is independent of {@link ProvisioningGroup} / {@link ProvisioningGroupWrapper},
 * which are bounded by Grouper's provisioning scope. Native-target reporting can include
 * groups in the target system that Grouper does not track.
 */
public class GrouperProvisioningTargetNativeGroup {

  /** the target system's unique identifier for this group (e.g. LDAP DN, SCIM id) */
  private String targetId;

  /** optional foreign key to grouper_groups.internal_id; null if Grouper does not track this group */
  private Long groupInternalId;

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

  public Long getGroupInternalId() {
    return groupInternalId;
  }

  public void setGroupInternalId(Long groupInternalId) {
    this.groupInternalId = groupInternalId;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

}
