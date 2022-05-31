package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * @author shilen
 */
public class LdapProvisionerTestUtils {
  
  public static void main(String args[]) throws Exception {
    GrouperSession.startRootSession();
    stopAndRemoveLdapContainer();
    startLdapContainer();
  }
  
  private static String dockerPath = null;

  public static void stopAndRemoveLdapContainer() {

    String dockerProcesses = new CommandLineExec().assignCommand(getDockerPath() + " ps -a")
        .assignErrorOnNonZero(true).execute().getStdout().getAllLines();
    
    if (dockerProcesses.contains("openldap-dinkel-grouper")) {

      new CommandLineExec().assignCommand(getDockerPath() + " stop openldap-dinkel-grouper")
          .assignErrorOnNonZero(true).execute();

      new CommandLineExec().assignCommand(getDockerPath() + " rm openldap-dinkel-grouper")
        .assignErrorOnNonZero(true).execute();
    }
  }
  
  public static void startLdapContainer() {
    
    String dockerImages = new CommandLineExec().assignCommand(getDockerPath() + " images")
        .assignErrorOnNonZero(true).execute().getStdout().getAllLines();

    String grouperMiscHome = GrouperUtil.getGrouperHome();
    
    if (grouperMiscHome.endsWith("grouper-pspng")) {
      grouperMiscHome = grouperMiscHome + File.separator + "..";
    } else {
      grouperMiscHome = grouperMiscHome + File.separator + ".." + File.separator + "grouper-misc";
    }

    // binds need a full path
    grouperMiscHome = new File(grouperMiscHome).getAbsolutePath();

    if (!dockerImages.contains("openldap-dinkel-grouper")) {
      
      new CommandLineExec().assignCommand(getDockerPath() + " build -t openldap-dinkel-grouper '"
          + grouperMiscHome + File.separator + "openldap-dinkel-grouper'")
        .assignErrorOnNonZero(true)
        .execute();
    }

    new CommandLineExec().assignCommand(getDockerPath() + " run -d -p 389:389 --name openldap-dinkel-grouper --mount type=bind,source='" 
        + grouperMiscHome + File.separator + "openldap-dinkel-grouper" + File.separator + "ldap-seed-data',target=/etc/ldap/prepopulate "
            + "-e SLAPD_PASSWORD=secret -e SLAPD_CONFIG_PASSWORD=secret -e SLAPD_DOMAIN=example.edu openldap-dinkel-grouper")
      .assignErrorOnNonZero(true)
      .execute();

    setupLdapExternalSystem();

    // abstract ldap class logs the errors, so just sleep 10 to wait until testing
    GrouperUtil.sleep(14000);
    
    RuntimeException lastException = null;
    for (int i = 0; i < 100; i++) {
      lastException = null;
      try {
        if (LdapSessionUtils.ldapSession().testConnection("personLdap")) {
          return;
        } else {
          GrouperUtil.sleep(1000);
        }
      } catch (RuntimeException e) {
        lastException = e;
        GrouperUtil.sleep(1000);
      }
    }
    if (lastException != null) {
      throw lastException;
    }
  }

  public static void setupLdapExternalSystem() {
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.url").value("ldap://localhost:389").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.user").value("cn=admin,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.pass").value("secret").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.uiTestAttributeName").value("dc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.uiTestExpectedValue").value("example").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.uiTestFilter").value("(dc=example)").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.uiTestSearchDn").value("dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.uiTestSearchScope").value("OBJECT_SCOPE").store();
    ConfigPropertiesCascadeBase.clearCache();
  }
  
  private static String getDockerPath() {
    if (dockerPath == null) {
      synchronized(LdapProvisionerTestUtils.class) {
        if (dockerPath == null) {
          String[] filesToCheck = { "/usr/bin/docker", "/usr/local/bin/docker", "/bin/docker" };
          for (String fileToCheck : filesToCheck) {
            if (new File(fileToCheck).exists()) {
              dockerPath = fileToCheck;
              break;
            } 
          }
          if (dockerPath == null) {
            dockerPath = "docker";
          }
        }
      }
    }
    
    
    return dockerPath;
  }

  public static void setupSubjectSource() {
    
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.id").value("personLdapSource").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.name").value("personLdapSource").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.types").value("person").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.adapterClass").value("edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.ldapServerId.value").value("personLdap").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.SubjectID_AttributeType.value").value("uid").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.SubjectID_formatToLowerCase.value").value("false").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.Name_AttributeType.value").value("cn").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.Description_AttributeType.value").value("cn").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.subjectVirtualAttribute_0_searchAttribute0.value").value("${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('uid'), \"\")},${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('cn'), \"\")}").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.sortAttribute0.value").value("cn").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.searchAttribute0.value").value("searchAttribute0").store();

    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.subjectIdToFindOnCheckConfig.value").value("aanderson").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.subjectIdentifierToFindOnCheckConfig.value").value("aanderson").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.stringToFindOnCheckConfig.value").value("ders").store();

    
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.searchSubject.param.filter.value").value("(&(uid=%TERM%)(objectclass=person))").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.searchSubject.param.scope.value").value("SUBTREE_SCOPE").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.searchSubject.param.base.value").value("dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.searchSubjectByIdentifier.param.filter.value").value("(&(uid=%TERM%)(objectclass=person))").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.searchSubjectByIdentifier.param.scope.value").value("SUBTREE_SCOPE").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.searchSubjectByIdentifier.param.base.value").value("dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.search.param.filter.value").value("(&(|(uid=%TERM%)(cn=*%TERM%*))(objectclass=person))").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.search.param.scope.value").value("SUBTREE_SCOPE").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.search.search.param.base.value").value("dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.attributes").value("cn, uid, eduPersonAffiliation, givenName, sn").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.internalAttributes").value("searchAttribute0").store();
    new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.personLdapSource.param.subjectIdentifierAttribute0.value").value("uid").store();
    
    ConfigPropertiesCascadeBase.clearCache();

    SourceManager.getInstance().reloadSource("personLdapSource");
    SourceManager.getInstance().loadSource(SubjectConfig.retrieveConfig().retrieveSourceConfigs().get("personLdapSource"));
  }
  
  /**
   * 
   * @param ldapProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(LdapProvisionerTestConfigInput ldapProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!ldapProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + ldapProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param provisioningTestConfigInput     
   * LdapProvisionerTestUtils.configureLdapProvisioner(
   *       new LdapProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .assignMembershipStructureEntityAttributes(true)
   *    .assignGroupAttributeCount(0)
   *    .assignUpdateGroupsAndDn(true)
   *    .assignDnOverrideScript (true)
   *    .assignTranslateFromGrouperProvisioningGroupField("extension")
   *    .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
   *    .assignPosixGroup(true)
   *    .assignMembershipAttribute("description")
   *    .assignUpdateEntitiesAndDn(true)
   *    .assignInsertEntityAndAttributes(true)
   *    .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
   *    .assignEntityAttributeCount(6)
   *    .assignGroupDnTypeBushy(true)
   *    .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
   *    .assignDnOverrideConfig(true)
   *    .assignExplicitFilters(true)
   *    .assignSubjectSourcesToProvision("jdbc")
   *    .assignEntitlementMetadata(true)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *    .addExtraConfig("logCommandsAlways", "true")
   *
   */
  public static void configureLdapProvisioner(LdapProvisionerTestConfigInput provisioningTestConfigInput) {

    if (!StringUtils.equals("member", provisioningTestConfigInput.getMembershipAttribute()) 
        && !StringUtils.equals("description", provisioningTestConfigInput.getMembershipAttribute())) {
      throw new RuntimeException("Expecting member or description but was '" + provisioningTestConfigInput.getMembershipAttribute() + "'");
    }
    if (0 != provisioningTestConfigInput.getEntityAttributeCount() && 2 != provisioningTestConfigInput.getEntityAttributeCount() && 3 != provisioningTestConfigInput.getEntityAttributeCount() && 6 != provisioningTestConfigInput.getEntityAttributeCount()) {
      throw new RuntimeException("Expecting 0, 2 or 6 for entityAttributeCount but was '" + provisioningTestConfigInput.getEntityAttributeCount() + "'");
    }
    if (provisioningTestConfigInput.isPosixGroup() && !StringUtils.equals(provisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField(), "idIndex")) {
      throw new RuntimeException("Cant be posix and business category");
    }

    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount() + "");
      
      if (provisioningTestConfigInput.getGroupAttributeCount() == 1) {

        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "entitlement");

        if (provisioningTestConfigInput.isEntitlementMetadata()) {

          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", 
              "${grouperUtil.defaultIfBlank(grouperProvisioningGroup.retrieveAttributeValueString('md_entitlementValue') , grouperProvisioningGroup." + provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + " )}");

        } else {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
          
        }
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateGrouperToGroupSyncField", "groupAttributeValueCache0");
        
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", provisioningTestConfigInput.isDnOverrideScript() ? "translationScript" : "grouperProvisioningGroupField");
        if (provisioningTestConfigInput.isDnOverrideScript()) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression",
              "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), 'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup." 
                  + provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + ") + ',ou=Groups,dc=example,dc=edu')}");
        } else {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField",
              provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
        }
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "ldap_dn");

  
      
        String attributeName = provisioningTestConfigInput.isPosixGroup() ? "gidNumber" : "businessCategory";
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name",
            attributeName);
        
        if (StringUtils.equals(provisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField(), "idIndex")) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.showAttributeValueSettings", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.valueType", "long");
        }
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", 
            provisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField());
      
        configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", attributeName);

        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "cn");
    
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", provisioningTestConfigInput.isDnOverrideScript() ? "translationScript" : "grouperProvisioningGroupField");
        if (provisioningTestConfigInput.isDnOverrideScript()) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression",
              "${edu.internet2.middleware.grouper.util.GrouperUtil.ldapConvertDnToSpecificValue(grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), "
                  + "'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup." 
                  + provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + ") + ',ou=Groups,dc=example,dc=edu'))}");
        } else {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", 
              provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
        }
    
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "objectClass");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAdvancedAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeValueSettings", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", 
          "${grouperUtil.toSet('top', '" + (provisioningTestConfigInput.isPosixGroup() ? "posixGroup" : "groupOfNames") + "')}");
        
        if (StringUtils.equals(provisioningTestConfigInput.getMembershipAttribute(), "member")) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "member");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAttributeValueSettings", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
          
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", provisioningTestConfigInput.getEntityAttributeCount() > 0 ? "entityAttributeValueCache2" : "subjectId");
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "description");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");

        } else if (StringUtils.equals(provisioningTestConfigInput.getMembershipAttribute(), "description")) {

          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "description");

          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "description");
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", provisioningTestConfigInput.getEntityAttributeCount() > 0 ? "entityAttributeValueCache2" : "subjectId");
          
        } else {
          throw new RuntimeException("Not expecting membershipAttribute: '" + provisioningTestConfigInput.getMembershipAttribute() + "'");
        }
        
      }
    }
    
    if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", provisioningTestConfigInput.getEntityAttributeCount() + "");
    
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
      if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionTypeCreateOnly", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionCreateOnly", "${'uid=' + grouperProvisioningEntity.retrieveAttributeValueString('" 
            + provisioningTestConfigInput.getEntityUidTranslateFromGrouperProvisioningEntityField() + "') + ',ou=People,dc=example,dc=edu'}");
      }
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "ldap_dn");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
      if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.showAttributeValidation", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.required", "true");
      }
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", provisioningTestConfigInput.getEntityUidTranslateFromGrouperProvisioningEntityField());
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");

      if (provisioningTestConfigInput.getEntityAttributeCount() > 2) {
        
        if (!provisioningTestConfigInput.isMembershipStructureEntityAttributes() || provisioningTestConfigInput.getEntityAttributeCount() > 3) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "sn");
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionTypeCreateOnly", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionCreateOnly", "'something'");

        }
      }
      if (provisioningTestConfigInput.getEntityAttributeCount() == 6) {

        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "cn");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionTypeCreateOnly", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionCreateOnly", "'something'");
        
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "givenName");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionTypeCreateOnly", "staticValues");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromStaticValuesCreateOnly", "something");
  
        if (!provisioningTestConfigInput.isMembershipStructureEntityAttributes()) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.name", "objectClass");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.showAttributeValueSettings", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.multiValued", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateExpressionTypeCreateOnly", "staticValues");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateFromStaticValuesCreateOnly", "top, organizationalPerson, person, inetOrgPerson, eduPerson");
        }  
  
      }
      if (provisioningTestConfigInput.isMembershipStructureEntityAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + (provisioningTestConfigInput.getEntityAttributeCount()-1) + ".name", "eduPersonEntitlement");

        configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "eduPersonEntitlement");
        configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", provisioningTestConfigInput.getGroupAttributeCount() == 1 ? "groupAttributeValueCache0" : "extension");

      }

    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
  
    if (!StringUtils.isBlank(provisioningTestConfigInput.getGroupDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getGroupDeleteType(), "true");
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    }
  
    if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    }
  
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", provisioningTestConfigInput.isMembershipStructureEntityAttributes() ? "entityAttributes" : "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
  
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", provisioningTestConfigInput.isGroupDnTypeBushy() ? "bushy" : "flat");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
      configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    }
    if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
      if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
      }
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
      if (provisioningTestConfigInput.isUpdateGroupsAndDn()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
      }
    }
    
    if (provisioningTestConfigInput.isExplicitFilters()) {
      if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
        configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchAllFilter", "(objectClass=" + (provisioningTestConfigInput.isPosixGroup() ? "posixGroup" : "groupOfNames") + ")");
        if (provisioningTestConfigInput.isPosixGroup()) {
          configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchFilter", "(&(objectClass=posixGroup)(gidNumber=${targetGroup.retrieveAttributeValue('gidNumber')}))");
        } else {
          configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
        }
      }
      if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
        configureProvisionerSuffix(provisioningTestConfigInput, "userSearchAllFilter", "(&(objectClass=person)(uid=*))");
        configureProvisionerSuffix(provisioningTestConfigInput, "userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
      }
    }
    
    if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");

      if (provisioningTestConfigInput.isUpdateEntitiesAndDn()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
      }
    }
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      if (provisioningTestConfigInput.isDnOverrideConfig() || provisioningTestConfigInput.isDnOverrideScript()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "allowLdapGroupDnOverride", "true");
      }
    }
    
    if (provisioningTestConfigInput.isEntitlementMetadata()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "configureMetadata", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfMetadata", "1");
      configureProvisionerSuffix(provisioningTestConfigInput, "metadata.0.formElementType", "text");
      configureProvisionerSuffix(provisioningTestConfigInput, "metadata.0.name", "md_entitlementValue");
      configureProvisionerSuffix(provisioningTestConfigInput, "metadata.0.showForGroup", "true");
      
    }  

    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
  
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + provisioningTestConfigInput.getConfigId() + "CLC.class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + provisioningTestConfigInput.getConfigId() + "CLC.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + provisioningTestConfigInput.getConfigId() + "CLC.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + provisioningTestConfigInput.getConfigId() + "CLC.provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + provisioningTestConfigInput.getConfigId() + "CLC.provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + provisioningTestConfigInput.getConfigId() + "CLC.publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
