package edu.internet2.middleware.grouper.app.ldapProvisioning;

public class LdapProvisioningTestConfigInput {

  /**
   * groupDnType flat (default) or bushy
   */
  private String groupDnType = "flat";

  /**
   * groupDnType flat (default) or bushy
   * @return
   */
  public String getGroupDnType() {
    return groupDnType;
  }

  /**
   * groupDnType flat (default) or bushy
   * @param groupDnType
   * @return this for chaining
   */
  public LdapProvisioningTestConfigInput assignGroupDnType(String groupDnType) {
    this.groupDnType = groupDnType;
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
  private String translateFromGrouperProvisioningGroupField;

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
   * 2 (default) or 6 (if has extended entity attributes
   */
  public int getEntityAttributeCount() {
    return entityAttributeCount;
  }

  /**
   * 2 (default) or 6 (if has extended entity attributes
   */
  public LdapProvisioningTestConfigInput assignEntityAttributeCount(int entityAttributeCount) {
    this.entityAttributeCount = entityAttributeCount;
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
   * 2 (default) or 6 (if has extended entity attributes
   */
  private int entityAttributeCount = 2;

  /**
   * default to ldapProvTest
   */
  private String configId = "ldapProvTest";
  
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
