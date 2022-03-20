package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
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
    GrouperStartup.startup();
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
  private static void configureProvisionerSuffix(LdapProvisioningTestConfigInput ldapProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!ldapProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + ldapProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param ldapProvisioningTestConfigInput     
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
   *
   */
  public static void configureLdapProvisioner(LdapProvisioningTestConfigInput ldapProvisioningTestConfigInput) {

    if (!StringUtils.equals("member", ldapProvisioningTestConfigInput.getMembershipAttribute()) 
        && !StringUtils.equals("description", ldapProvisioningTestConfigInput.getMembershipAttribute())) {
      throw new RuntimeException("Expecting member or description but was '" + ldapProvisioningTestConfigInput.getMembershipAttribute() + "'");
    }
    if (0 != ldapProvisioningTestConfigInput.getEntityAttributeCount() && 2 != ldapProvisioningTestConfigInput.getEntityAttributeCount() && 3 != ldapProvisioningTestConfigInput.getEntityAttributeCount() && 6 != ldapProvisioningTestConfigInput.getEntityAttributeCount()) {
      throw new RuntimeException("Expecting 0, 2 or 6 for entityAttributeCount but was '" + ldapProvisioningTestConfigInput.getEntityAttributeCount() + "'");
    }
    if (ldapProvisioningTestConfigInput.isPosixGroup() && !StringUtils.equals(ldapProvisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField(), "idIndex")) {
      throw new RuntimeException("Cant be posix and business category");
    }

    if (ldapProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "numberOfGroupAttributes", "" + ldapProvisioningTestConfigInput.getGroupAttributeCount() + "");
      
      if (ldapProvisioningTestConfigInput.getGroupAttributeCount() == 1) {

        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.name", "entitlement");

        if (ldapProvisioningTestConfigInput.isEntitlementMetadata()) {

          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", 
              "${grouperUtil.defaultIfBlank(grouperProvisioningGroup.retrieveAttributeValueString('md_entitlementValue') , grouperProvisioningGroup." + ldapProvisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + " )}");

        } else {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", ldapProvisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
          
        }
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateGrouperToGroupSyncField", "groupFromId2");
        
      } else {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.insert", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
        if (ldapProvisioningTestConfigInput.isUpdateGroupsAndDn()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.update", "true");
        }
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", ldapProvisioningTestConfigInput.isDnOverrideScript() ? "translationScript" : "grouperProvisioningGroupField");
        if (ldapProvisioningTestConfigInput.isDnOverrideScript()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpression",
              "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), 'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup." 
                  + ldapProvisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + ") + ',ou=Groups,dc=example,dc=edu')}");
        } else {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField",
              ldapProvisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
        }
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
      
  
      
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.name",
            ldapProvisioningTestConfigInput.isPosixGroup() ? "gidNumber" : "businessCategory");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.valueType", "long");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
        if (StringUtils.equals(ldapProvisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField(), "idIndex")) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.valueType", "long");
        }
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", 
            ldapProvisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField());
      
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.2.name", "cn");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", ldapProvisioningTestConfigInput.isDnOverrideScript() ? "translationScript" : "grouperProvisioningGroupField");
        if (ldapProvisioningTestConfigInput.isDnOverrideScript()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpression",
              "${edu.internet2.middleware.grouper.util.GrouperUtil.ldapConvertDnToSpecificValue(grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), "
                  + "'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup." 
                  + ldapProvisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + ") + ',ou=Groups,dc=example,dc=edu'))}");
        } else {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", 
              ldapProvisioningTestConfigInput.isGroupDnTypeBushy() ? "extension" : ldapProvisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
        }
    
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.3.name", "objectClass");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.3.insert", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.3.select", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", 
          "${grouperUtil.toSet('top', '" + (ldapProvisioningTestConfigInput.isPosixGroup() ? "posixGroup" : "groupOfNames") + "')}");
        
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.name", "member");
        if (StringUtils.equals(ldapProvisioningTestConfigInput.getMembershipAttribute(), "member")) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.multiValued", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.membershipAttribute", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.translateFromMemberSyncField", ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0 ? "memberToId2" : "subjectId");
        } else {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "translationScript");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.4.translateExpression", "'cn' + '=' + 'somethingbogussincethisisrequired'");
        }
        
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.name", "description");
        if (StringUtils.equals(ldapProvisioningTestConfigInput.getMembershipAttribute(), "description")) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.multiValued", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.membershipAttribute", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.translateFromMemberSyncField", ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0 ? "memberToId2" : "subjectId");
        } else {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.insert", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.update", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.select", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
        }    
      }
    }
    
    if (ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttributeCount", ldapProvisioningTestConfigInput.getEntityAttributeCount() + "");
    
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
      if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.insert", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionTypeCreateOnly", "translationScript");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionCreateOnly", "${'uid=' + grouperProvisioningEntity.retrieveAttributeValueString('" 
            + ldapProvisioningTestConfigInput.getEntityUidTranslateFromGrouperProvisioningEntityField() + "') + ',ou=People,dc=example,dc=edu'}");
      }
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");
      if (ldapProvisioningTestConfigInput.isUpdateEntitiesAndDn()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.0.update", "true");
      }

      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
      if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.required", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.insert", "true");
      }
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.matchingId", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.searchAttribute", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", ldapProvisioningTestConfigInput.getEntityUidTranslateFromGrouperProvisioningEntityField());
      if (ldapProvisioningTestConfigInput.isUpdateEntitiesAndDn()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.1.update", "true");
      }
      
      if (ldapProvisioningTestConfigInput.getEntityAttributeCount() > 2) {
        
        if (!ldapProvisioningTestConfigInput.isMembershipStructureEntityAttributes() || ldapProvisioningTestConfigInput.getEntityAttributeCount() > 3) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.2.name", "sn");
          if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
            configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.2.insert", "true");
          }        
          
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionTypeCreateOnly", "translationScript");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionCreateOnly", "'something'");

        }
      }
      if (ldapProvisioningTestConfigInput.getEntityAttributeCount() == 6) {

        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.3.name", "cn");
        if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.3.insert", "true");
        }
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionTypeCreateOnly", "translationScript");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionCreateOnly", "'something'");
        
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.4.name", "givenName");
        if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.4.insert", "true");
        }
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionTypeCreateOnly", "staticValues");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.4.translateFromStaticValuesCreateOnly", "something");
  
        if (!ldapProvisioningTestConfigInput.isMembershipStructureEntityAttributes()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.5.name", "objectClass");
          if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
            configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.5.insert", "true");
          }
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.5.multiValued", "true");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.5.translateExpressionTypeCreateOnly", "staticValues");
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute.5.translateFromStaticValuesCreateOnly", "top, organizationalPerson, person, inetOrgPerson, eduPerson");
        }  
  
      }
      if (ldapProvisioningTestConfigInput.isMembershipStructureEntityAttributes()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute." + (ldapProvisioningTestConfigInput.getEntityAttributeCount()-1) + ".name", "eduPersonEntitlement");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute." + (ldapProvisioningTestConfigInput.getEntityAttributeCount()-1) + ".translateFromGroupSyncField", ldapProvisioningTestConfigInput.getGroupAttributeCount() == 1 ? "groupFromId2" : "groupExtension");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute." + (ldapProvisioningTestConfigInput.getEntityAttributeCount()-1) + ".membershipAttribute", "true");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "targetEntityAttribute." + (ldapProvisioningTestConfigInput.getEntityAttributeCount()-1) + ".multiValued", "true");
      }

    }
    
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "subjectSourcesToProvision", ldapProvisioningTestConfigInput.getSubjectSourcesToProvision());
  
    if (!StringUtils.isBlank(ldapProvisioningTestConfigInput.getGroupDeleteType())) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "deleteGroups", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, ldapProvisioningTestConfigInput.getGroupDeleteType(), "true");
    }
  
    if (ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    }
  
    if (ldapProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    }
    
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "provisioningType", ldapProvisioningTestConfigInput.isMembershipStructureEntityAttributes() ? "entityAttributes" : "groupAttributes");
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "selectMemberships", "true");
  
    if (ldapProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "groupDnType", ldapProvisioningTestConfigInput.isGroupDnTypeBushy() ? "bushy" : "flat");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "insertGroups", "true");
    }
    if (ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0) {
      if (ldapProvisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "insertEntities", "true");
      }
    }
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    if (ldapProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "selectGroups", "true");
      if (ldapProvisioningTestConfigInput.isUpdateGroupsAndDn()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "updateGroups", "true");
      }
    }
    
    if (ldapProvisioningTestConfigInput.isExplicitFilters()) {
      if (ldapProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "groupSearchAllFilter", "(objectClass=" + (ldapProvisioningTestConfigInput.isPosixGroup() ? "posixGroup" : "groupOfNames") + ")");
        if (ldapProvisioningTestConfigInput.isPosixGroup()) {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "groupSearchFilter", "(&(objectClass=posixGroup)(gidNumber=${targetGroup.retrieveAttributeValue('gidNumber')}))");
        } else {
          configureProvisionerSuffix(ldapProvisioningTestConfigInput, "groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
        }
      }
      if (ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "userSearchAllFilter", "(&(objectClass=person)(uid=*))");
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
      }
    }
    
    if (ldapProvisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "hasTargetEntityLink", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "selectEntities", "true");
      if (ldapProvisioningTestConfigInput.isUpdateEntitiesAndDn()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "updateEntities", "true");
      }
    }
    
    if (ldapProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      if (ldapProvisioningTestConfigInput.isDnOverrideConfig() || ldapProvisioningTestConfigInput.isDnOverrideScript()) {
        configureProvisionerSuffix(ldapProvisioningTestConfigInput, "allowLdapGroupDnOverride", "true");
      }
    }
    
    if (ldapProvisioningTestConfigInput.isEntitlementMetadata()) {
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "configureMetadata", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "numberOfMetadata", "1");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "metadata.0.formElementType", "text");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "metadata.0.name", "md_entitlementValue");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "metadata.0.showForGroup", "true");
      configureProvisionerSuffix(ldapProvisioningTestConfigInput, "metadata.0.valueType", "string");
      
    }  

    configureProvisionerSuffix(ldapProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
  
    for (String key: ldapProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = ldapProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + ldapProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + ldapProvisioningTestConfigInput.getConfigId() + "CLC.class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + ldapProvisioningTestConfigInput.getConfigId() + "CLC.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + ldapProvisioningTestConfigInput.getConfigId() + "CLC.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + ldapProvisioningTestConfigInput.getConfigId() + "CLC.provisionerConfigId").value(ldapProvisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + ldapProvisioningTestConfigInput.getConfigId() + "CLC.provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + ldapProvisioningTestConfigInput.getConfigId() + "CLC.publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
