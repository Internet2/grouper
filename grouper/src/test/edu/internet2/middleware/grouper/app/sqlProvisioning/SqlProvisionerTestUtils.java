package edu.internet2.middleware.grouper.app.sqlProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class SqlProvisionerTestUtils {
  
  /**
   * 
   * @param sqlProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(SqlProvisionerTestConfigInput sqlProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!sqlProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + sqlProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param provisioningTestConfigInput     
   * SqlProvisionerTestUtils.configureSqlProvisioner(
   *       new SqlProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureSqlProvisioner(SqlProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "class", SqlProvisioner.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "dbExternalSystemConfigId", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getMembershipDeleteType(), "true");
    }
    if (provisioningTestConfigInput.isEntityResolverGlobal()) {

      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.baseDn").value("ou=People,dc=example,dc=edu").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.grouperAttributeThatMatchesRecord").value("subjectId").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.ldapAttributes").value("givenName,mail,objectClass").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.ldapConfigId").value("personLdap").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.resolverType").value("ldap").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.searchScope").value("SUBTREE_SCOPE").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.subjectSearchMatchingAttribute").value("mail").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.columnNames").value("school,subject_id_or_identifier").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.grouperAttributeThatMatchesRow").value("subjectId").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.resolverType").value("sql").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.sqlConfigId").value("grouper").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.subjectSearchMatchingColumn").value("subject_id_or_identifier").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.subjectSourceIdColumn").value("jdbc").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.tableOrViewName").value("testgrouper_prov_entity1").store();

      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.globalLDAPResolver", "globalLdapConfig");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.globalSQLResolver", "globalSqlEntityResolver");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.resolveAttributesWithLDAP", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.resolveAttributesWithSQL", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.useGlobalLDAPResolver", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.useGlobalSQLResolver", "true");


    } else if (provisioningTestConfigInput.isEntityResolverLocal()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.attributes", "givenName,mail,objectClass");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.baseDN", "ou=People,dc=example,dc=edu");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.filterAllLDAPOnFull", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapConfigId", "personLdap");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMappingEntityAttribute", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMappingType", "entityAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMatchingSearchAttribute", "mail");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.resolveAttributesWithLDAP", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.resolveAttributesWithSQL", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.searchScope", "SUBTREE_SCOPE");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.selectAllSQLOnFull", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.useGlobalLDAPResolver", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.useGlobalSQLResolver", "false");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.sqlConfigId", "grouper");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.sqlMappingEntityAttribute", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.sqlMappingType", "entityAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.subjectSearchMatchingColumn", "subject_id_or_identifier");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.tableOrViewName", "testgrouper_prov_entity1");

    }
    
    if (provisioningTestConfigInput.isEntityAttributesTable()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "useSeparateTableForEntityAttributes", "true");

      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributesAttributeNameColumn", "entity_attribute_name");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributesAttributeValueColumn", "entity_attribute_value");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributesEntityForeignKeyColumn", "entity_uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributesLastModifiedColumn", "last_modified");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributesLastModifiedColumnType", "timestamp");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributesTableName", "testgrouper_pro_dap_entity_attr");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "showFailsafe", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeMaxOverallPercentGroupsRemove", "-1");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeMaxOverallPercentMembershipsRemove", "-1");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeMaxPercentRemove", "-1");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeMinGroupSize", "-1");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeMinManagedGroups", "-1");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeMinOverallNumberOfMembers", "-1");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeSendEmail", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "failsafeUse", "true");
    if (provisioningTestConfigInput.isGroupAttributesTable()) {
      
      configureProvisionerSuffix(provisioningTestConfigInput, "useSeparateTableForGroupAttributes", "true");

      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributesAttributeNameColumn", "attribute_name");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributesAttributeValueColumn", "attribute_value");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributesGroupForeignKeyColumn", "group_uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributesLastModifiedColumn", "last_modified");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributesLastModifiedColumnType", "timestamp");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributesTableName", "testgrouper_pro_ldap_group_attr");
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getGroupTableIdColumn())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "groupTableIdColumn", provisioningTestConfigInput.getGroupTableIdColumn());
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getGroupTableName())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "groupTableName", provisioningTestConfigInput.getGroupTableName());
    }
    if (provisioningTestConfigInput.isHasTargetEntityLink()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    }
    if (provisioningTestConfigInput.isHasTargetGroupLink()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipEntityForeignKeyColumn())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "membershipEntityForeignKeyColumn", provisioningTestConfigInput.getMembershipEntityForeignKeyColumn());
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipGroupForeignKeyColumn())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "membershipGroupForeignKeyColumn", provisioningTestConfigInput.getMembershipGroupForeignKeyColumn());
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipTableIdColumn())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "membershipPrimaryKey", provisioningTestConfigInput.getMembershipTableIdColumn());
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipTableName())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "membershipTableName", provisioningTestConfigInput.getMembershipTableName());
    }
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

    if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "" + provisioningTestConfigInput.getEntityAttributeCount());
      configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
      
      for (int i=0;i<provisioningTestConfigInput.getEntityAttributeCount();i++) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + i + ".select", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + i + ".insert", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + i + ".update", "true");
        
      }
      
    }
    
    if (provisioningTestConfigInput.getEntityAttributeCount() == 3) {
      
      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "dn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.matchingId", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "employeeID");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "entity_uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "id");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.storageType", "entityTableColumn");


      
    } else if (provisioningTestConfigInput.getEntityAttributeCount() >= 5) {

      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.storageType", "entityTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateGrouperToMemberSyncField", "memberFromId2");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.matchingId", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.searchAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.storageType", "entityTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "subject_id_or_identifier");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.storageType", "entityTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "email");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.storageType", "entityTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "email");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "description");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "description");

      if (provisioningTestConfigInput.getEntityAttributeCount() == 6) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.name", "school");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.storageType", "separateAttributesTable");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateExpressionType", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateExpression", "${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverLdap__givenname')}");
        
      }
    }

    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount());
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
      
      for (int i=0;i<provisioningTestConfigInput.getGroupAttributeCount();i++) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute." + i + ".select", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute." + i + ".insert", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute." + i + ".update", "true");
        
      }
      
    }
    if (provisioningTestConfigInput.getGroupAttributeCount() == 1) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.storageType", "groupTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.matchingId", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    }

    
    if (provisioningTestConfigInput.getGroupAttributeCount() == 3) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.matchingId", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.multiValued", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.storageType", "groupTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.membershipAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.multiValued", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromMemberSyncField", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "groupName");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    }
    
    
    if (provisioningTestConfigInput.getGroupAttributeCount() == 4) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.storageType", "groupTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateGrouperToGroupSyncField", "groupFromId2");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "description");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "description");

      if (provisioningTestConfigInput.isPosixId()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "name");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.searchAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.storageType", "groupTableColumn");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.matchingId", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "posix_id");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.storageType", "groupTableColumn");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "idIndex");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.valueType", "long");
        
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "name");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.matchingId", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.searchAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.storageType", "separateAttributesTable");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.membershipAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "subjectId");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.storageType", "separateAttributesTable");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromMemberSyncField", "subjectId");

      }
    }
    
    if (provisioningTestConfigInput.getGroupAttributeCount() == 6) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "cn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "dn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpression", "${'cn=' + grouperProvisioningGroup.getName() + ',OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu'}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateGrouperToGroupSyncField", "groupToId2");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.matchingId", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.multiValued", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "gidNumber");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.searchAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "idIndex");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "objectClass");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('group')}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.membershipAttribute", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.multiValued", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "member");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.storageType", "separateAttributesTable");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.storageType", "groupTableColumn");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "name");

    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    
    if (provisioningTestConfigInput.getMembershipAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfMembershipAttributes", "" + provisioningTestConfigInput.getMembershipAttributeCount());
      configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
      
      for (int i=0;i<provisioningTestConfigInput.getMembershipAttributeCount();i++) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute." + i + ".select", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute." + i + ".insert", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute." + i + ".update", "true");
        
      }
      
    }
    if (provisioningTestConfigInput.getMembershipAttributeCount() == 2) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.name", "group_name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.name", "subject_id");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    }
    if (provisioningTestConfigInput.getMembershipAttributeCount() == 3) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.name", "uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.name", "group_uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "groupSyncField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGroupSyncField", "groupFromId2");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.name", "entity_uuid");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.translateExpressionType", "memberSyncField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.translateFromMemberSyncField", "memberFromId2");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", provisioningTestConfigInput.getProvisioningType());
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");

    
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");

    
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityTableIdColumn())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "userPrimaryKey", provisioningTestConfigInput.getEntityTableIdColumn());
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityTableName())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "userTableName", provisioningTestConfigInput.getEntityTableName());
    }

    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.class").value("edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.quartzCron").value("0 0 4 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.provisionerConfigId").value("sqlProvTest").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.class").value("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.provisionerConfigId").value("sqlProvTest").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.publisher.class").value("edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.publisher.debug").value("true").store();
    
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
