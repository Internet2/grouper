package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl;
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
    setupLdapExternalSystem();
    setupSubjectSource();
    System.exit(0);
  }
  
  private static String dockerPath = null;

  public static void stopAndRemoveLdapContainer() {
    LdaptiveSessionImpl.internal_closeAllPools();
    
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
    int ldapSleepMillisOnTestStartup = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.ldapSleepMillisOnTestStartup", 14000);
    GrouperUtil.sleep(ldapSleepMillisOnTestStartup);
        
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
  
  public static void configureLdapProvisioner_1(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
   
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "eduPersonEntitlement");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "entityAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "eduPersonEntitlement");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    
  configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
  configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
  configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
  configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
  configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
  configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
  configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", provisioningTestConfigInput.getGroupAttributeCount() + "");
  configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "groupId");
  configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
  configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
  
  configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
  configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
  configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
  configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
  configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "groupId");
  configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    
//    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchAllFilter", "(&(objectClass=person)(uid=*))");
//    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    
//    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpression", "${gcGrouperSyncMember.getEntityAttributeValueCache2()}");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "translationScript");
//    
//    
//    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
//    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
//    
//    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchAllFilter", "(&(objectClass=person)(uid=*))");
//    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    configureDaemons(provisioningTestConfigInput);

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
    
    if (StringUtils.equals("configureEntityLinkLdap", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureEntityLinkLdap(provisioningTestConfigInput); 
      
    } else if (StringUtils.equals("configureEntityLinkLdap2", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureEntityLinkLdap2(provisioningTestConfigInput);  

      
      
    } else if (StringUtils.equals("gatechEntitlement", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureGatechEntitlement(provisioningTestConfigInput);  

    } else if (StringUtils.equals("gettesPosix", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureGettesPosix(provisioningTestConfigInput);  

    } else if (StringUtils.equals("benEntityAttributes", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureBenEntityAttributes(provisioningTestConfigInput);  

    } else if (StringUtils.equals("harvardGroupOfNames", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureHarvardGroupOfNames(provisioningTestConfigInput);  

    } else if (StringUtils.equals("umassActiveDirectory", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureUmassActiveDirectory(provisioningTestConfigInput);  

    } else if (StringUtils.equals("coloradoSingleEntityAttribute", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureColoradoSingleEntityAttribute(provisioningTestConfigInput);  
      
    } else if (StringUtils.equals("internet2groupOfNames", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureInternet2groupOfNames(provisioningTestConfigInput);
      
    } else if (StringUtils.equals("internet2memberOf", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureInternet2memberOf(provisioningTestConfigInput);
      
    } else if (StringUtils.equals("korandaGroupOfNames", provisioningTestConfigInput.getProvisioningStrategy())) {
      
      configureKorandaGroupOfNames(provisioningTestConfigInput);
      
    } else {
      
    
      configureSettings(provisioningTestConfigInput);  
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
  
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    configureDaemons(provisioningTestConfigInput);
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }

  private static void configureEntityLinkLdap(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledFullSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledIncrementalSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "allowLdapGroupDnOverride", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.attributes", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.baseDN", "ou=people,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.filterPart", "objectClass=eduPerson");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMappingEntityAttribute", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMappingType", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMatchingSearchAttribute", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.resolveAttributesWithLDAP", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.searchScope", "SUBTREE_SCOPE");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "flat");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupRdnAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "personLdapSource");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpression", "${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverLdap__ldap_dn')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", provisioningTestConfigInput.isDnOverrideScript() ? "translationScript" : "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromStaticValues", "top,groupOfNames");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.defaultValue", "<emptyString>");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeValueSettings", "true");

  }

  private static void configureEntityLinkLdap2(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledFullSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledIncrementalSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "allowLdapGroupDnOverride", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.attributes", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.baseDN", "ou=people,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.filterPart", "objectClass=eduPerson");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMappingEntityAttribute", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMappingType", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.ldapMatchingSearchAttribute", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.resolveAttributesWithLDAP", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.searchScope", "SUBTREE_SCOPE");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "flat");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupRdnAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "personLdapSource");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpression", "${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverLdap__ldap_dn')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression",
        "${'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup.name) + ',ou=Groups,dc=example,dc=edu'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromStaticValues", "top,groupOfNames");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.defaultValue", "<emptyString>");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeValueSettings", "true");

  }
  
  private static void configureGatechEntitlement(LdapProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledFullSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledIncrementalSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteValueIfManagedByGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1entityAttribute", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2translationScript", "${subject.getAttributeValue('uid')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "subjectTranslationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "entityAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "personLdapSource");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpression", "${grouperProvisioningEntity.subjectId  == 'aanderson' ? grouperProvisioningEntity.subjectId : gcGrouperSyncMember.entityAttributeValueCache2}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromStaticValues", "person");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().getProperty('gatech.provisioner.group_name_prefix', '') + grouperProvisioningGroup.name}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=people,dc=example,dc=edu");
  }
  
  private static void configureGettesPosix(
      LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledFullSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledIncrementalSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "createEntityDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "createGroupDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1entityAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "bushy");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "gidNumber");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupRdnAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "5");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromStaticValues", "top,person");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "sn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", "${edu.internet2.middleware.grouper.util.GrouperUtil.ldapBushyDn(edu.internet2.middleware.grouper.util.GrouperUtil.stripPrefix(grouperProvisioningGroup.name, 'app:'), 'cn', 'ou', true, false) + ',ou=Groups,dc=example,dc=edu'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "gidNumber");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "idIndex");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateFromStaticValues", "top,posixGroup");
    configureProvisionerSuffix(provisioningTestConfigInput, "testGroupName", "some:test");
    configureProvisionerSuffix(provisioningTestConfigInput, "testSubjectIdOrIdentifier", "aanderson");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "userRdnAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    
  }

  private static void configureSettings(
      LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    if (!StringUtils.equals("member", provisioningTestConfigInput.getMembershipAttribute()) 
        && !StringUtils.equals("description", provisioningTestConfigInput.getMembershipAttribute())) {
      throw new RuntimeException("Expecting member or description but was '" + provisioningTestConfigInput.getMembershipAttribute() + "'");
    }
    if (0 != provisioningTestConfigInput.getEntityAttributeCount() && 1 != provisioningTestConfigInput.getEntityAttributeCount() && 2 != provisioningTestConfigInput.getEntityAttributeCount() 
        && 3 != provisioningTestConfigInput.getEntityAttributeCount() && 6 != provisioningTestConfigInput.getEntityAttributeCount()
        && 7 != provisioningTestConfigInput.getEntityAttributeCount()) {
      throw new RuntimeException("Expecting 0, 2, 3, 6, or 7 for entityAttributeCount but was '" + provisioningTestConfigInput.getEntityAttributeCount() + "'");
    }
    if (provisioningTestConfigInput.isPosixGroup() && !StringUtils.equals(provisioningTestConfigInput.getBusinessCategoryTranslateFromGrouperProvisioningGroupField(), "idIndex")) {
      throw new RuntimeException("Cant be posix and business category");
    }

    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      if (provisioningTestConfigInput.getGroupAttributeCount() == 6 && StringUtils.equals(provisioningTestConfigInput.getMembershipAttribute(), "description")
          && provisioningTestConfigInput.isPosixGroup()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "5");
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount() + "");
      }
      
      if (provisioningTestConfigInput.getGroupAttributeCount() == 1) {

        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "entitlement");

        if (provisioningTestConfigInput.isEntitlementMetadata()) {

          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", 
              "${grouperUtil.defaultIfBlank(grouperProvisioningGroup.retrieveAttributeValueString('md_entitlementValue') , grouperProvisioningGroup." + provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField() + " )}");

        } else {
          if (!GrouperUtil.nonNull(provisioningTestConfigInput.getExtraConfig()).containsKey("targetGroupAttribute.0.translateExpression")) {
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField());
          } else {
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
          }
        }
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "entitlement");
        
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
        if (provisioningTestConfigInput.isGroupDnTranslate()) {
          if (StringUtils.equals("true", provisioningTestConfigInput.getExtraConfig().get("onlyLdapGroupDnOverride"))) {
            
            configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
            configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
            configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
            
          } else {
            
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", provisioningTestConfigInput.isDnOverrideScript() ? "translationScript" : "grouperProvisioningGroupField");
            String dnAttribute = provisioningTestConfigInput.isGroupDnTypeBushy() ? "name" : provisioningTestConfigInput.getTranslateFromGrouperProvisioningGroupField();
            if (provisioningTestConfigInput.isDnOverrideScript()) {
              configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression",
                  "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), 'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup." 
                      + dnAttribute + ") + ',ou=Groups,dc=example,dc=edu')}");
            } else {
              configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField",
                  dnAttribute);

            }
          }
        }
        if (provisioningTestConfigInput.isGroupAttributeValueCache2dn()) {
          configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
          configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
          configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "ldap_dn");
        }
      
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

        if (!StringUtils.equals("true", provisioningTestConfigInput.getExtraConfig().get("onlyLdapGroupDnOverride"))) {
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", attributeName);
        } else {
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "ldap_dn");
        }

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
          //TODO fix it. subjectId is not even a valid value
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", provisioningTestConfigInput.getEntityAttributeCount() > 0 ? "entityAttributeValueCache2" : "subjectId");
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "description");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");

        } else if (StringUtils.equals(provisioningTestConfigInput.getMembershipAttribute(), "description")) {

          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "description");

          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "description");
          //TODO fix it. subjectId is not even a valid value
          configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", provisioningTestConfigInput.getEntityAttributeCount() > 0 ? "entityAttributeValueCache2" : "subjectId");
          
          if (!provisioningTestConfigInput.isPosixGroup()) {
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "member");
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "translationScript");
            configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpression", "'cn' + '=' + 'somethingbogussincethisisrequired'");
            
          }

        } else {
          throw new RuntimeException("Not expecting membershipAttribute: '" + provisioningTestConfigInput.getMembershipAttribute() + "'");
        }
        
      }
    }
    
    if (provisioningTestConfigInput.getEntityAttributeCount() == 1) {
      
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", provisioningTestConfigInput.getEntityAttributeCount() + "");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "subjectId");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    }
    
    if (provisioningTestConfigInput.getGroupAttributeCount() == 1) {
      
//      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
//      configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
//      configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
//      configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
//      configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
//      configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
//      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", provisioningTestConfigInput.getGroupAttributeCount() + "");
//      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "groupId");
//      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
//      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "id");
//      
//      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
//      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
//      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
//      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
//      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "groupId");
//      configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    }
    
    if (provisioningTestConfigInput.getEntityAttributeCount() >= 2) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", provisioningTestConfigInput.getEntityAttributeCount() + "");
    
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
      if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {

        configureProvisionerSuffix(provisioningTestConfigInput, "userRdnAttribute", "uid");

        if (provisioningTestConfigInput.isEntityDnTranslate()) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionTypeCreateOnly", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionCreateOnly", "${'uid=' + grouperProvisioningEntity.retrieveAttributeValueString('" 
              + provisioningTestConfigInput.getEntityUidTranslateFromGrouperProvisioningEntityField() + "') + ',ou=People,dc=example,dc=edu'}");
        }
      }
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "ldap_dn");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
      if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.showAdvancedAttribute", "true");
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
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpression", "'something'");
          
          if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
            
            configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAdvancedAttribute", "true");
            configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAttributeValidation", "true");
            configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.required", "true");
          }


        }
      }
      if (provisioningTestConfigInput.getEntityAttributeCount() >= 6) {

        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "cn");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAdvancedAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpression", "'something'");
        
        if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAttributeValidation", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.required", "true");
        }

        
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "givenName");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.showAdvancedAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "staticValues");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromStaticValues", "something");

        if (provisioningTestConfigInput.isInsertEntityAndAttributes()) {
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.showAttributeValidation", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.required", "true");
        }

      }
      if (provisioningTestConfigInput.getEntityAttributeCount() >= 6) {
        int objectClassIndex = -1;
        if (!provisioningTestConfigInput.isMembershipStructureEntityAttributes() && provisioningTestConfigInput.getEntityAttributeCount() == 6) {
          objectClassIndex = 5;
        }
        if (provisioningTestConfigInput.getEntityAttributeCount() >= 7) {
          objectClassIndex = 6;
        }
        if (objectClassIndex != -1) {
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".name", "objectClass");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".showAdvancedAttribute", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".showAttributeValueSettings", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".multiValued", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".showAttributeValidation", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".required", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".translateExpressionType", "staticValues");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + objectClassIndex + ".translateFromStaticValues", "top, organizationalPerson, person, inetOrgPerson, eduPerson");
          
        }  
      
      }
      if (provisioningTestConfigInput.isMembershipStructureEntityAttributes()) {
        int membershipIndex = (provisioningTestConfigInput.getEntityAttributeCount()-1);
        if (provisioningTestConfigInput.getEntityAttributeCount() == 7) {
          membershipIndex = 5;
        }
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute." + membershipIndex + ".name", "eduPersonEntitlement");

        configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "eduPersonEntitlement");
        configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", provisioningTestConfigInput.getGroupAttributeCount() == 1 ? "groupAttributeValueCache0" : "extension");

      }

    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
  
    if (!StringUtils.isBlank(provisioningTestConfigInput.getGroupDeleteType())) {
      // this is the default
      if (!StringUtils.equals(provisioningTestConfigInput.getGroupDeleteType(), "deleteGroupsIfGrouperCreated")) {
        configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getGroupDeleteType(), "true");
      }
    } else {
      if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {

        configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
      }
    }
  
    if (provisioningTestConfigInput.getEntityAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    }
  
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
      if (provisioningTestConfigInput.getGroupAttributeCount() > 1) {
        configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
      }
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", provisioningTestConfigInput.isMembershipStructureEntityAttributes() ? "entityAttributes" : "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
  
    if (provisioningTestConfigInput.getGroupAttributeCount() == 1) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
      
    }
    if (provisioningTestConfigInput.getGroupAttributeCount() > 1) {
      configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", provisioningTestConfigInput.isGroupDnTypeBushy() ? "bushy" : "flat");
      if (!StringUtils.equals("true", provisioningTestConfigInput.getExtraConfig().get("onlyLdapGroupDnOverride"))) {
        configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
      }
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getMembershipDeleteType(), "true");
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

      if (provisioningTestConfigInput.isUpdateEntitiesAndDn() || provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
        if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
          // this is the default
          if (!StringUtils.equals(provisioningTestConfigInput.getEntityDeleteType(), "deleteEntitiesIfGrouperCreated")) {
            configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
          }
        } else {
          configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "false");
        }

        configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
      }
      if (!provisioningTestConfigInput.isUpdateEntitiesAndDn() && provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "false");
      }
      if (provisioningTestConfigInput.isUpdateEntitiesAndDn() && !provisioningTestConfigInput.isInsertEntityAndAttributes()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "false");
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
  }

  public static void configureDaemons(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();
  }

  private static void configureBenEntityAttributes(
      LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "createEntityDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "createGroupDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "flat");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "entityAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntitiesDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroupsDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectIdentifier0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpression", "${grouperProvisioningGroup.getExtension().toLowerCase()}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "testGroupName", "IAM:affiliationGroups:AFL_CIVIL_SERVICE");
    configureProvisionerSuffix(provisioningTestConfigInput, "testSubjectIdOrIdentifier", "jmneff");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    
    
    
  }

  private static void configureHarvardGroupOfNames(
      LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "flat");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "6");
    configureProvisionerSuffix(provisioningTestConfigInput, "onlyAddMembershipsIfUserExistsInTarget", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "recalculateAllOperations", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpression", "${grouperProvisioningGroup.displayExtension ? ( edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup.displayExtension) + ':Member') : null}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "businessCategory");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "${grouperProvisioningGroup.displayExtension ? ('ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup.displayExtension) + ':Member,ou=Groups,dc=example,dc=edu)') : null}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.defaultValue", "ou=People-qa,dc=law,dc=harvard,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateFromStaticValues", "top, groupOfNames");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
    
    
  }

  private static void configureInternet2memberOf(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "entityAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
  }
  
  private static void configureInternet2groupOfNames(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "flat");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "businessCategory");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupRdnAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "5");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.defaultValue", "cn=admin-seed-data,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "businessCategory");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "idIndex");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateFromStaticValues", "top, groupOfNames");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
  }
  
  
  private static void configureKorandaGroupOfNames(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    
    //new --------
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1entityAttribute", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "entityAttribute");
    // ------------
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    
    //new ---------
    configureProvisionerSuffix(provisioningTestConfigInput, "entityResolver.entityAttributesNotInSubjectSource", "true");
    // ------------
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    
    
    //new -----------------------
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1groupAttribute", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1type", "groupAttribute");
    // ---------------------------
    
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "bushy"); // used to be flat in internet2 config
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute1name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "2");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupRdnAttribute", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu"); // ou=grouper,dc=ligo,dc=org
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "5");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromStaticValues", "person");
    
        
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "\u0024{'grouper_idIndex_' + grouperProvisioningGroup.idIndex}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "translationScript");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeValueSettings", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromStaticValues", "top,groupOfNames");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.defaultValue", "<emptyString>");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
  }
  
  
  private static void configureColoradoSingleEntityAttribute(LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfGrouperDeleted", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "extAttr");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerboseToLogFile", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "entityAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.multiValued", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAttributeValidation", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "extAttr");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromStaticValues", "SomeStaticValue");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");
  }
  
  private static void configureUmassActiveDirectory(
      LdapProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", LdapSync.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupDnType", "bushy");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "businessCategory");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "ldapExternalSystemConfigId", "personLdap");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsOnError", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "5");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", provisioningTestConfigInput.getSubjectSourcesToProvision());
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "uid");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectIdentifier1");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ldap_dn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "cn");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "member");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache0");
    // <emptyString>, cn=admin,dc=example,dc=edu
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.defaultValue", "<emptyString>");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.multiValued", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "objectClass");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeValueSettings", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromStaticValues", "top,groupOfNames");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "businessCategory");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateFromGrouperProvisioningGroupField", "idIndexString");
    configureProvisionerSuffix(provisioningTestConfigInput, "userSearchBaseDn", "ou=People,dc=example,dc=edu");

    
    
    //configureProvisionerSuffix(provisioningTestConfigInput, "onlyAddMembershipsIfUserExistsInTarget", "true");
    
    
  }
}
