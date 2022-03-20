package edu.internet2.middleware.grouper.app.ldapProvisioning;

import static org.junit.Assume.assumeFalse;

import java.util.HashMap;
import java.util.Map;

public class LdapProvisioningTestConfigInput {

  /**
   * if allow dn override with config (default false)
   */
  private boolean dnOverrideConfig;
  
  
  /**
   * if allow dn override with config (default false)
   * @return
   */
  public boolean isDnOverrideConfig() {
    return dnOverrideConfig;
  }

  /**
   * 
   * @param dnOverrideConfig
   */
  public LdapProvisioningTestConfigInput assignDnOverrideConfig(boolean dnOverrideConfig) {
    this.dnOverrideConfig = dnOverrideConfig;
    return this;
  }

  /**
   * if allow dn override with translation (default false)
   */
  private boolean dnOverrideScript;
  
  
  /**
   * if allow dn override with translation (default false)
   */
  public boolean isDnOverrideScript() {
    return dnOverrideScript;
  }

  /**
   * if allow dn override with translation (default false)
   * @param dnOverrideScript
   */
  public LdapProvisioningTestConfigInput assignDnOverrideScript(boolean dnOverride) {
    this.dnOverrideScript = dnOverride;
    return this;
  }

  /**
   * extra config by suffix and value
   */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  /**
   * extra config by suffix and value
   * @param suffix
   * @param value
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  
  /**
   * extra config by suffix and value
   * @return map
   */
  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

  /**
   * if posix and gidnumber, default to false
   */
  private boolean posixGroup;
  
  /**
   * if posix and gidnumber, default to false
   * @return
   */
  public boolean isPosixGroup() {
    return posixGroup;
  }

  /**
   * if posix and gidnumber, default to false
   * @param posixGroup
   */
  public LdapProvisioningTestConfigInput assignPosixGroup(boolean posixGroup) {
    this.posixGroup = posixGroup;
    return this;
  }

  /**
   * if entity attributes instead of group attributes (default)
   */
  private boolean membershipStructureEntityAttributes;

  /**
   * if entity attributes instead of group attributes (default)
   * @return if entity attributes
   */
  public boolean isMembershipStructureEntityAttributes() {
    return this.membershipStructureEntityAttributes;
  }

  /**
   * if entity attributes instead of group attributes (default)
   * @param membershipStructureEntityAttributes1
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput assignMembershipStructureEntityAttributes(boolean membershipStructureEntityAttributes1) {
    this.membershipStructureEntityAttributes = membershipStructureEntityAttributes1;
    return this;
  }

  /**
   * groupDnType flat (default) or bushy
   */
  private boolean groupDnTypeBushy = false;

  /**
   * groupDnType flat (default) or bushy
   * @return
   */
  public boolean isGroupDnTypeBushy() {
    return groupDnTypeBushy;
  }

  /**
   * groupDnType flat (default) or bushy
   * @param groupDnType
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput assignGroupDnTypeBushy(boolean groupDnType) {
    this.groupDnTypeBushy = groupDnType;
    return this;
  }

  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  private String groupDeleteType; 
  
  /**
   * true to set an explicit searchall or search one filter, false to let grouper figure that out (default)
   */
  private boolean explicitFilters = false;
  
  /**
   * name (default) or extension
   */
  private String translateFromGrouperProvisioningGroupField = "name";

  /**
   * if crud for entity and dn update should be true (default false)
   */
  private boolean updateEntitiesAndDn = false;

  
  /**
   * if crud for entity and dn update should be true (default false)
   * @return if update entities and dn
   */
  public boolean isUpdateEntitiesAndDn() {
    return this.updateEntitiesAndDn;
  }

  /**
   * if crud for entity and dn update should be true (default false)
   * @param updateEntitiesAndDn1
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput assignUpdateEntitiesAndDn(boolean updateEntitiesAndDn1) {
    this.updateEntitiesAndDn = updateEntitiesAndDn1;
    return this;
  }

  /**
   * if crud for group and dn update should be true (default false)
   */
  private boolean updateGroupsAndDn = false;

  /**
   * idIndex (default) or id
   */
  private String businessCategoryTranslateFromGrouperProvisioningGroupField = "idIndex";
  
  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  public String getGroupDeleteType() {
    return groupDeleteType;
  }

  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   * @param groupDeleteType
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput assignGroupDeleteType(String groupDeleteType) {
    this.groupDeleteType = groupDeleteType;
    return this;
  }

  /**
   * true to set an explicit searchall or search one filter, false to let grouper figure that out (default)
   * @return
   */
  public boolean isExplicitFilters() {
    return explicitFilters;
  }

  /**
   * true to set an explicit searchall or search one filter, false to let grouper figure that out (default)
   * @param explicitFilters
   * @return
   */
  public LdapProvisioningTestConfigInput assignExplicitFilters(boolean explicitFilters) {
    this.explicitFilters = explicitFilters;
    return this;
  }

  /**
   * name (default) or extension
   */
  public String getTranslateFromGrouperProvisioningGroupField() {
    return translateFromGrouperProvisioningGroupField;
  }

  /**
   * name (default) or extension
   * @param translateFromGrouperProvisioningGroupField
   */
  public LdapProvisioningTestConfigInput assignTranslateFromGrouperProvisioningGroupField(String translateFromGrouperProvisioningGroupField) {
    this.translateFromGrouperProvisioningGroupField = translateFromGrouperProvisioningGroupField;
    return this;
  }

  /**
   * if crud for group and dn update should be true (default false)
   * @return
   */
  public boolean isUpdateGroupsAndDn() {
    return updateGroupsAndDn;
  }

  /**
   * if crud for group and dn update should be true (default false)
   * @param updateGroupsAndDn
   * @return
   */
  public LdapProvisioningTestConfigInput assignUpdateGroupsAndDn(boolean updateGroupsAndDn) {
    this.updateGroupsAndDn = updateGroupsAndDn;
    return this;
  }

  /**
   * idIndex or id
   * @return
   */
  public String getBusinessCategoryTranslateFromGrouperProvisioningGroupField() {
    return businessCategoryTranslateFromGrouperProvisioningGroupField;
  }

  /**
   * idIndex (default) or id
   * @param businessCategoryTranslateFromGrouperProvisioningGroupField
   * @return
   */
  public LdapProvisioningTestConfigInput assignBusinessCategoryTranslateFromGrouperProvisioningGroupField(
      String businessCategoryTranslateFromGrouperProvisioningGroupField) {
    this.businessCategoryTranslateFromGrouperProvisioningGroupField = businessCategoryTranslateFromGrouperProvisioningGroupField;
    return this;
  }

  /**
   * member (default) or description
   */
  public String getMembershipAttribute() {
    return membershipAttribute;
  }
  
  /**
   * member (default) or description
   */
  public LdapProvisioningTestConfigInput assignMembershipAttribute(String membershipAttribute) {
    this.membershipAttribute = membershipAttribute;
    return this;
  }

  /**
   * subjectId (default) or subjectIdentifier0
   */
  public String getEntityUidTranslateFromGrouperProvisioningEntityField() {
    return entityUidTranslateFromGrouperProvisioningEntityField;
  }

  /**
   * subjectId (default) or subjectIdentifier0
   */
  public LdapProvisioningTestConfigInput assignEntityUidTranslateFromGrouperProvisioningEntityField(
      String entityUidTranslateFromGrouperProvisioningEntityField) {
    this.entityUidTranslateFromGrouperProvisioningEntityField = entityUidTranslateFromGrouperProvisioningEntityField;
    return this;
  }

  /**
   * personLdapSource (default) or jdbc
   */
  public String getSubjectSourcesToProvision() {
    return subjectSourcesToProvision;
  }

  /**
   * personLdapSource (default) or jdbc
   */
  public LdapProvisioningTestConfigInput assignSubjectSourcesToProvision(String subjectSourcesToProvision) {
    this.subjectSourcesToProvision = subjectSourcesToProvision;
    return this;
  }

  /**
   * if insert entity and attributes (default false)
   * @return
   */
  public boolean isInsertEntityAndAttributes() {
    return insertEntityAndAttributes;
  }

  /**
   * if insert entity and attributes (default false)
   * @param insertEntityAndAttributes
   * @return
   */
  public LdapProvisioningTestConfigInput assignInsertEntityAndAttributes(boolean insertEntityAttributes) {
    this.insertEntityAndAttributes = insertEntityAttributes;
    return this;
  }

  /**
   * 0, 2 (default), 3 or 6 (if has extended entity attributes
   */
  public int getEntityAttributeCount() {
    return entityAttributeCount;
  }

  /**
   * 0, 2 (default), 3 or 6 (if has extended entity attributes
   */
  public LdapProvisioningTestConfigInput assignEntityAttributeCount(int entityAttributeCount) {
    this.entityAttributeCount = entityAttributeCount;
    return this;
  }

  /**
   * 0, 1, or 6 (default)
   */
  public int getGroupAttributeCount() {
    return groupAttributeCount;
  }

  /**
   * 0, 1, or 6 (default)
   */
  public LdapProvisioningTestConfigInput assignGroupAttributeCount(int groupAttributeCount) {
    this.groupAttributeCount = groupAttributeCount;
    return this;
  }

  /**
   * member (default) or description
   */
  private String membershipAttribute = "member";
  
  /**
   * subjectId (default) or subjectIdentifier0
   */
  private String entityUidTranslateFromGrouperProvisioningEntityField = "subjectId";
  
  /**
   * personLdapSource (default) or jdbc
   */
  private String subjectSourcesToProvision = "personLdapSource";

  /**
   * if insert entity and attributes (default false)
   */
  private boolean insertEntityAndAttributes = false;

  /**
   * if there should be an entitlement metadata
   */
  private boolean entitlementMetadata = false;
  
  
  /**
   * if there should be an entitlement metadata
   * @return true if so
   */
  public boolean isEntitlementMetadata() {
    return this.entitlementMetadata;
  }

  /**
   * if there should be an entitlement metadata
   * @param entitlementMetadata1
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput assignEntitlementMetadata(boolean entitlementMetadata1) {
    this.entitlementMetadata = entitlementMetadata1;
    return this;
  }

  /**
   * 0, 2 (default), 3 or 6 (if has extended entity attributes
   */
  private int entityAttributeCount = 2;

  /**
   * default to ldapProvTest
   */
  private String configId = "ldapProvTest";

  /**
   * 0, 1, or 6 (default)
   */
  private int groupAttributeCount = 6;
  
  /**
   * default to ldapProvTest
   * @param string
   * @return
   */
  public LdapProvisioningTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to ldapProvTest
   * @return
   */
  public String getConfigId() {
    return configId;
  }
  
}
