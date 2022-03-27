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
   * @param sqlProvisioningTestConfigInput     
   * SqlProvisionerTestUtils.configureSqlProvisioner(
   *       new SqlProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureSqlProvisioner(SqlProvisionerTestConfigInput sqlProvisioningTestConfigInput) {

    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "class", SqlProvisioner.class.getName());
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "dbExternalSystemConfigId", "grouper");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "debugLog", "true");
    
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, sqlProvisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, sqlProvisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getMembershipDeleteType())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "deleteMemberships", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, sqlProvisioningTestConfigInput.getMembershipDeleteType(), "true");
    }
    if (sqlProvisioningTestConfigInput.isEntityResolverGlobal()) {

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

      
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.globalLDAPResolver", "globalLdapConfig");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.globalSQLResolver", "globalSqlEntityResolver");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.resolveAttributesWithLDAP", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.resolveAttributesWithSQL", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.useGlobalLDAPResolver", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.useGlobalSQLResolver", "true");


    } else if (sqlProvisioningTestConfigInput.isEntityResolverLocal()) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.attributes", "givenName,mail,objectClass");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.baseDN", "ou=People,dc=example,dc=edu");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.filterAllLDAPOnFull", "false");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.ldapConfigId", "personLdap");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.ldapMappingEntityAttribute", "subjectId");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.ldapMappingType", "entityAttribute");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.ldapMatchingSearchAttribute", "mail");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.resolveAttributesWithLDAP", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.resolveAttributesWithSQL", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.searchScope", "SUBTREE_SCOPE");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.selectAllSQLOnFull", "false");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.useGlobalLDAPResolver", "false");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.useGlobalSQLResolver", "false");
      
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.sqlConfigId", "grouper");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.sqlMappingEntityAttribute", "subjectId");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.sqlMappingType", "entityAttribute");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.subjectSearchMatchingColumn", "subject_id_or_identifier");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityResolver.tableOrViewName", "testgrouper_prov_entity1");

    }
    
    if (sqlProvisioningTestConfigInput.isEntityAttributesTable()) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "useSeparateTableForEntityAttributes", "true");

      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityAttributesAttributeNameColumn", "entity_attribute_name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityAttributesAttributeValueColumn", "entity_attribute_value");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityAttributesEntityForeignKeyColumn", "entity_uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityAttributesLastModifiedColumn", "last_modified");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityAttributesLastModifiedColumnType", "timestamp");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "entityAttributesTableName", "testgrouper_pro_dap_entity_attr");
    }
    
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "showFailsafe", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeMaxOverallPercentGroupsRemove", "-1");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeMaxOverallPercentMembershipsRemove", "-1");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeMaxPercentRemove", "-1");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeMinGroupSize", "-1");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeMinManagedGroups", "-1");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeMinOverallNumberOfMembers", "-1");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeSendEmail", "false");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "failsafeUse", "true");
    if (sqlProvisioningTestConfigInput.isGroupAttributesTable()) {
      
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "useSeparateTableForGroupAttributes", "true");

      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupAttributesAttributeNameColumn", "attribute_name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupAttributesAttributeValueColumn", "attribute_value");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupAttributesGroupForeignKeyColumn", "group_uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupAttributesLastModifiedColumn", "last_modified");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupAttributesLastModifiedColumnType", "timestamp");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupAttributesTableName", "testgrouper_pro_ldap_group_attr");
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getGroupTableIdColumn())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupTableIdColumn", sqlProvisioningTestConfigInput.getGroupTableIdColumn());
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getGroupTableName())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "groupTableName", sqlProvisioningTestConfigInput.getGroupTableName());
    }
    if (sqlProvisioningTestConfigInput.isHasTargetEntityLink()) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    }
    if (sqlProvisioningTestConfigInput.isHasTargetGroupLink()) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    }
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getMembershipEntityForeignKeyColumn())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "membershipEntityForeignKeyColumn", sqlProvisioningTestConfigInput.getMembershipEntityForeignKeyColumn());
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getMembershipGroupForeignKeyColumn())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "membershipGroupForeignKeyColumn", sqlProvisioningTestConfigInput.getMembershipGroupForeignKeyColumn());
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getMembershipTableIdColumn())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "membershipPrimaryKey", sqlProvisioningTestConfigInput.getMembershipTableIdColumn());
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getMembershipTableName())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "membershipTableName", sqlProvisioningTestConfigInput.getMembershipTableName());
    }
    
    
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

    if (sqlProvisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "numberOfEntityAttributes", "" + sqlProvisioningTestConfigInput.getEntityAttributeCount());
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertEntities", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "selectEntities", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "updateEntities", "true");
      
      for (int i=0;i<sqlProvisioningTestConfigInput.getEntityAttributeCount();i++) {
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute." + i + ".select", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute." + i + ".insert", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute." + i + ".update", "true");
        
      }
      
    }
    
    if (sqlProvisioningTestConfigInput.getEntityAttributeCount() == 3) {
      
      
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.name", "dn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.matchingId", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.name", "employeeID");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.name", "entity_uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "id");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.storageType", "entityTableColumn");


      
    } else if (sqlProvisioningTestConfigInput.getEntityAttributeCount() >= 5) {

      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "selectAllEntities", "true");

      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.name", "uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.storageType", "entityTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "translationScript");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.0.translateGrouperToMemberSyncField", "memberFromId2");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.matchingId", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.name", "name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.searchAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.storageType", "entityTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.name", "subject_id_or_identifier");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.storageType", "entityTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.3.name", "email");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.3.storageType", "entityTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "email");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.4.name", "description");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.4.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "description");

      if (sqlProvisioningTestConfigInput.getEntityAttributeCount() == 6) {
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.5.name", "school");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.5.storageType", "separateAttributesTable");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.5.translateExpressionType", "translationScript");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetEntityAttribute.5.translateExpression", "${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverLdap__givenname')}");
        
      }
    }

    if (sqlProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "numberOfGroupAttributes", "" + sqlProvisioningTestConfigInput.getGroupAttributeCount());
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertGroups", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "selectGroups", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "updateGroups", "true");
      
      for (int i=0;i<sqlProvisioningTestConfigInput.getGroupAttributeCount();i++) {
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute." + i + ".select", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute." + i + ".insert", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute." + i + ".update", "true");
        
      }
      
    }
    if (sqlProvisioningTestConfigInput.getGroupAttributeCount() == 1) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.name", "uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.storageType", "groupTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.matchingId", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    }

    
    if (sqlProvisioningTestConfigInput.getGroupAttributeCount() == 3) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.matchingId", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.multiValued", "false");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.name", "uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.storageType", "groupTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "false");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.membershipAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.multiValued", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.name", "subjectId");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromMemberSyncField", "subjectId");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.name", "groupName");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    }
    
    
    if (sqlProvisioningTestConfigInput.getGroupAttributeCount() == 4) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.name", "uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.storageType", "groupTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateGrouperToGroupSyncField", "groupFromId2");

      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.name", "description");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "description");

      if (sqlProvisioningTestConfigInput.isPosixId()) {
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.name", "name");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.searchAttribute", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.storageType", "groupTableColumn");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.matchingId", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.name", "posix_id");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.storageType", "groupTableColumn");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "idIndex");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.valueType", "long");
        
      } else {
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.name", "name");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.matchingId", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.searchAttribute", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.storageType", "separateAttributesTable");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.membershipAttribute", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.name", "subjectId");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.storageType", "separateAttributesTable");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.translateFromMemberSyncField", "subjectId");

      }
    }
    
    if (sqlProvisioningTestConfigInput.getGroupAttributeCount() == 6) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.name", "cn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.name", "dn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpression", "${'cn=' + grouperProvisioningGroup.getName() + ',OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu'}");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "translationScript");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.1.translateGrouperToGroupSyncField", "groupToId2");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.matchingId", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.multiValued", "false");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.name", "gidNumber");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.searchAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "idIndex");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.name", "objectClass");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('group')}");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.4.membershipAttribute", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.4.multiValued", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.4.name", "member");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.4.storageType", "separateAttributesTable");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.5.name", "uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.5.storageType", "groupTableColumn");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "name");

    }
    
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    
    if (sqlProvisioningTestConfigInput.getMembershipAttributeCount() > 0) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "numberOfMembershipAttributes", "" + sqlProvisioningTestConfigInput.getMembershipAttributeCount());
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertMemberships", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "selectMemberships", "true");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "updateMemberships", "true");
      
      for (int i=0;i<sqlProvisioningTestConfigInput.getMembershipAttributeCount();i++) {
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute." + i + ".select", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute." + i + ".insert", "true");
        configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute." + i + ".update", "true");
        
      }
      
    }
    if (sqlProvisioningTestConfigInput.getMembershipAttributeCount() == 2) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.0.name", "group_name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.1.name", "subject_id");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    }
    if (sqlProvisioningTestConfigInput.getMembershipAttributeCount() == 3) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.0.name", "uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "translationScript");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.1.name", "group_uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "groupSyncField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGroupSyncField", "groupFromId2");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.2.name", "entity_uuid");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.2.translateExpressionType", "memberSyncField");
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "targetMembershipAttribute.2.translateFromMemberSyncField", "memberFromId2");
    }
    
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "provisioningType", sqlProvisioningTestConfigInput.getProvisioningType());
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "showAdvanced", "true");

    
    configureProvisionerSuffix(sqlProvisioningTestConfigInput, "showProvisioningDiagnostics", "true");

    
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getEntityTableIdColumn())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "userPrimaryKey", sqlProvisioningTestConfigInput.getEntityTableIdColumn());
    }
    if (!StringUtils.isBlank(sqlProvisioningTestConfigInput.getEntityTableName())) {
      configureProvisionerSuffix(sqlProvisioningTestConfigInput, "userTableName", sqlProvisioningTestConfigInput.getEntityTableName());
    }

    
    for (String key: sqlProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = sqlProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + sqlProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
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
