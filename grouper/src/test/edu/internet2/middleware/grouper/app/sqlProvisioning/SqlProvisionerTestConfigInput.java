package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestConfigInput;
import edu.internet2.middleware.grouper.app.scim.ScimProvisionerTestConfigInput;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisionerTestConfigInput {

  private boolean operateOnGrouperMemberships = true;
  
  
  
  public boolean isOperateOnGrouperMemberships() {
    return operateOnGrouperMemberships;
  }


  public SqlProvisionerTestConfigInput assignOperateOnGrouperMemberships(boolean operateOnGrouperMemberships) {
    this.operateOnGrouperMemberships = operateOnGrouperMemberships;
    return this;
  }

  private boolean failsafeDefaults;
  
  
  
  public boolean isFailsafeDefaults() {
    return failsafeDefaults;
  }


  public SqlProvisionerTestConfigInput assignFailsafeDefaults(boolean failsafeDefaults) {
    this.failsafeDefaults = failsafeDefaults;
    return this;
  }

  /**
   * if has target entity link
   */
  private boolean hasTargetEntityLink;
  
  
  
  public boolean isHasTargetEntityLink() {
    return hasTargetEntityLink;
  }


  public SqlProvisionerTestConfigInput assignHasTargetEntityLink(boolean hasTargetEntityLink) {
    this.hasTargetEntityLink = hasTargetEntityLink;
    return this;
  }


  public boolean isHasTargetGroupLink() {
    return hasTargetGroupLink;
  }


  public SqlProvisionerTestConfigInput assignHasTargetGroupLink(boolean hasTargetGroupLink) {
    this.hasTargetGroupLink = hasTargetGroupLink;
    return this;
  }

  /**
   * if has target group link
   */
  private boolean hasTargetGroupLink;
  
  
  
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
  public SqlProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to sqlProvTest
   */
  private String configId = "sqlProvTest";
  /**
   * entityDeleteType e.g. deleteEntitiesIfNotExistInGrouper or deleteEntitiesIfGrouperDeleted or deleteEntitiesIfGrouperCreated or null (default)
   */
  private String entityDeleteType;
  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  private String groupDeleteType;
  /**
   * membershipDeleteType e.g. deleteMembershipsIfNotExistInGrouper or deleteMembershipsIfGrouperDeleted or deleteMembershipsIfGrouperCreated or null (default)
   */
  private String membershipDeleteType;

  /**
   * default to sqlProvTest
   * @param string
   * @return this for chaining
   */
  public SqlProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to sqlProvTest
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }


  /**
   * entityDeleteType e.g. deleteEntitiesIfNotExistInGrouper or deleteEntitiesIfGrouperDeleted or deleteEntitiesIfGrouperCreated or null (default)
   * @param entityDeleteType
   * @return this for chaining
   */
  public SqlProvisionerTestConfigInput assignEntityDeleteType(String entityDeleteType) {
    this.entityDeleteType = entityDeleteType;
    return this;
  }


  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   * @param groupDeleteType
   * @return this for chaining
   */
  public SqlProvisionerTestConfigInput assignGroupDeleteType(String groupDeleteType) {
    this.groupDeleteType = groupDeleteType;
    return this;
  }


  /**
   * membershipDeleteType e.g. deleteMembershipsIfNotExistInGrouper or deleteMembershipsIfGrouperDeleted or deleteMembershipsIfGrouperCreated or null (default)
   * @param membershipDeleteType
   * @return this for chaining
   */
  public SqlProvisionerTestConfigInput assignMembershipDeleteType(String membershipDeleteType) {
    this.membershipDeleteType = membershipDeleteType;
    return this;
  }


  /**
   * entityDeleteType e.g. deleteEntitiesIfNotExistInGrouper or deleteEntitiesIfGrouperDeleted or deleteEntitiesIfGrouperCreated or null (default)
   */
  public String getEntityDeleteType() {
    return entityDeleteType;
  }


  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  public String getGroupDeleteType() {
    return groupDeleteType;
  }


  /**
   * membershipDeleteType e.g. deleteMembershipsIfNotExistInGrouper or deleteMembershipsIfGrouperDeleted or deleteMembershipsIfGrouperCreated or null (default)
   */
  public String getMembershipDeleteType() {
    return membershipDeleteType;
  }
  
  private boolean entityResolverLocal;
  
  public boolean isEntityResolverLocal() {
    return entityResolverLocal;
  }


  public SqlProvisionerTestConfigInput assignEntityResolverLocal(boolean entityResolverLocal) {
    this.entityResolverLocal = entityResolverLocal;
    return this;
  }


  public boolean isEntityResolverGlobal() {
    return entityResolverGlobal;
  }


  public SqlProvisionerTestConfigInput assignEntityResolverGlobal(boolean entityResolverGlobal) {
    this.entityResolverGlobal = entityResolverGlobal;
    return this;
  }

  private boolean entityResolverGlobal;
  
  private boolean entityAttributesTable;

  public boolean isEntityAttributesTable() {
    return entityAttributesTable;
  }


  public SqlProvisionerTestConfigInput assignEntityAttributesTable(boolean entityAttributesTable) {
    this.entityAttributesTable = entityAttributesTable;
    return this;
  }
  
  private boolean groupAttributesTable;

  public boolean isGroupAttributesTable() {
    return groupAttributesTable;
  }


  public SqlProvisionerTestConfigInput assignGroupAttributesTable(boolean groupAttributesTable) {
    this.groupAttributesTable = groupAttributesTable;
    return this;
  }
  
  private String groupTableName;
  

  public String getGroupTableName() {
    return groupTableName;
  }


  public SqlProvisionerTestConfigInput assignGroupTableName(String groupTableName) {
    this.groupTableName = groupTableName;
    return this;
  }

  private String groupTableIdColumn;
  
  public String getGroupTableIdColumn() {
    return groupTableIdColumn;
  }


  public SqlProvisionerTestConfigInput assignGroupTableIdColumn(String groupTableIdColumn) {
    this.groupTableIdColumn = groupTableIdColumn;
    return this;
  }


  private String entityTableName;
  
  public String getEntityTableName() {
    return entityTableName;
  }


  public SqlProvisionerTestConfigInput assignEntityTableName(String entityTableName) {
    this.entityTableName = entityTableName;
    return this;
  }

  
  private String entityTableIdColumn;



  public String getEntityTableIdColumn() {
    return entityTableIdColumn;
  }


  public SqlProvisionerTestConfigInput assignEntityTableIdColumn(String entityTableIdColumn) {
    this.entityTableIdColumn = entityTableIdColumn;
    return this;
  }

  private String membershipTableName;
  

  public String getMembershipTableName() {
    return membershipTableName;
  }


  public SqlProvisionerTestConfigInput assignMembershipTableName(String membershipTableName) {
    this.membershipTableName = membershipTableName;
    return this;
  }


  private String membershipTableIdColumn;
  
  public String getMembershipTableIdColumn() {
    return membershipTableIdColumn;
  }


  public SqlProvisionerTestConfigInput assignMembershipTableIdColumn(String membershipTableIdColumn) {
    this.membershipTableIdColumn = membershipTableIdColumn;
    return this;
  }

  private String membershipGroupForeignKeyColumn;
  

  public String getMembershipGroupForeignKeyColumn() {
    return membershipGroupForeignKeyColumn;
  }


  public SqlProvisionerTestConfigInput assignMembershipGroupForeignKeyColumn(String membershipGroupForeignKeyColumn) {
    this.membershipGroupForeignKeyColumn = membershipGroupForeignKeyColumn;
    return this;
  }

  private String membershipEntityForeignKeyColumn;



  public String getMembershipEntityForeignKeyColumn() {
    return membershipEntityForeignKeyColumn;
  }


  public SqlProvisionerTestConfigInput assignMembershipEntityForeignKeyColumn(String membershipEntityForeignKeyColumn) {
    this.membershipEntityForeignKeyColumn = membershipEntityForeignKeyColumn;
    return this;
  }

  /**
   * true (default) is group attributes: uuid, posix_id, name, description
   * false is group attributes: uuid, subjectId, name, description
   */
  private boolean posixId = true;
  
  
  
  public boolean isPosixId() {
    return posixId;
  }


  public SqlProvisionerTestConfigInput assignPosixId(boolean posixId) {
    this.posixId = posixId;
    return this;
  }

  /**
   * 1, 3, 4 or 6
   * 1 is uuid as group name
   * 3 is uuid, groupName, subjectId
   * 4 is uuid, posix_id, name, description (posixId true, default true)
   * 4 is uuid, subjectId, name, description (posixId false)
   * 6 is cn, dn, gidNumber, member, objectClass, uuid
   */
  private int groupAttributeCount = 0;

  /**
   * 1, 3, 4 or 6
   * 1 is uuid as group name
   * 3 is uuid, groupName, subjectId
   * 4 is uuid, posix_id, name, description (posixId true, default true)
   * 4 is uuid, subjectId, name, description (posixId false)
   * 6 is cn, dn, gidNumber, member, objectClass, uuid
   */
  public SqlProvisionerTestConfigInput assignGroupAttributeCount(int groupAttributeCount) {
    this.groupAttributeCount = groupAttributeCount;
    return this;
  }

  /**
   * 1, 3, 4 or 6
   * 1 is uuid as group name
   * 3 is uuid, groupName, subjectId
   * 4 is uuid, posix_id, name, description (posixId true, default true)
   * 4 is uuid, subjectId, name, description (posixId false)
   * 6 is cn, dn, gidNumber, member, objectClass, uuid
   */
  public int getGroupAttributeCount() {
    return groupAttributeCount;
  }

  /**
   * 0 (default), 3, 5 or 6
   * 3 is dn, employeeId, and entity_uuid
   * 5 is uuid, name, subject_id_or_identifier, email, description
   * 6 is same as 5 with school attribute
   */
  private int entityAttributeCount = 0;

  /**
   * 0 (default), 3, 5 or 6
   * 3 is dn, employeeId, and entity_uuid
   * 5 is uuid, name, subject_id_or_identifier, email, description
   * 6 is same as 5 with school attribute
   */
  public SqlProvisionerTestConfigInput assignEntityAttributeCount(int entityAttributeCount) {
    this.entityAttributeCount = entityAttributeCount;
    return this;
  }

  /**
   * 0 (default), 3, 5 or 6
   * 3 is dn, employeeId, and entity_uuid
   * 5 is uuid, name, subject_id_or_identifier, email, description
   * 6 is same as 5 with school attribute
   */
  public int getEntityAttributeCount() {
    return entityAttributeCount;
  }

  /**
   * 0 (default), 2, 3
   * 2: group_name, subject_id
   * 3: uuid, group_uuid, entity_uuid
   */
  private int membershipAttributeCount = 0;
  
  /**
   * 0 (default), 2, 3
   * 2: group_name, subject_id
   * 3: uuid, group_uuid, entity_uuid
   * @return
   */
  public int getMembershipAttributeCount() {
    return membershipAttributeCount;
  }

  /**
   * 0 (default), 2, 3
   * 2: group_name, subject_id
   * 3: uuid, group_uuid, entity_uuid
   * @param membershipAttributeCount
   */
  public SqlProvisionerTestConfigInput assignMembershipAttributeCount(int membershipAttributeCount) {
    this.membershipAttributeCount = membershipAttributeCount;
    return this;
  }

  /**
   * groupAttributes, entityAttributes, membershipObjects (default)
   */
  private String provisioningType = "membershipObjects";


  /**
   * groupAttributes, entityAttributes, membershipObjects (default)
   * @return
   */
  public String getProvisioningType() {
    return provisioningType;
  }

  /**
   * groupAttributes, entityAttributes, membershipObjects (default)
   * @param provisioningType
   * @return
   */
  public SqlProvisionerTestConfigInput assignProvisioningType(String provisioningType) {
    this.provisioningType = provisioningType;
    return this;
  }
  
}
