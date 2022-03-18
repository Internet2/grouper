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
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.url", "ldap://localhost:389");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.pass", "secret");
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

  public static void configureGroupFlatSubjectId() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.numberOfGroupAttributes").value("5").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("name").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.name").value("gidNumber").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("name").store();
    
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression").value("${grouperUtil.toSet('top', 'posixGroup')}").store();    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField").value("subjectId").store();
        
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.class").value(LdapSync.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.ldapExternalSystemConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.subjectSourcesToProvision").value("jdbc, personLdapSource").store();    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroups").value("false").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.hasTargetGroupLink").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.provisioningType").value("groupAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectMemberships").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupDnType").value("flat").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupSearchAllFilter").value("(objectClass=posixGroup)").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupSearchBaseDn").value("ou=Groups,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectGroups").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.logAllObjectsVerbose").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }

  public static void configureUserAttributeGroupExtension() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.class").value(LdapSync.class.getName()).store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.ldapExternalSystemConfigId").value("personLdap").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.numberOfEntityAttributes").value("3").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.provisioningType").value("entityAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.subjectSourcesToProvision").value("personLdapSource").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpression").value("${'uid=' + grouperProvisioningEntity.subjectId + ',ou=People,dc=example,dc=edu'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.1.name").value("uid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.name").value("eduPersonEntitlement").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.translateFromGroupSyncField").value("groupExtension").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.userSearchAllFilter").value("(uid=*)").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.userSearchBaseDn").value("ou=People,dc=example,dc=edu").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }

  public static void configureUserAttributesManyGroupNameFormatted() {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "1");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.name").value("entitlementValue").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpression").value("${'someprefix:' + grouperProvisioningGroup.name}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.valueType").value("string").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpression").value("${'uid=' + grouperProvisioningEntity.getSubjectId() + ',ou=People,dc=example,dc=edu'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.name").value("uid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.name").value("sn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.translateFromStaticValues").value("something").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.translateFromStaticValues").value("something").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.translateFromStaticValues").value("something").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.translateFromStaticValues").value("top, organizationalPerson, person, inetOrgPerson, eduPerson").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.valueType").value("string").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.6.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.6.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.6.name").value("eduPersonEntitlement").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.6.translateFromGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.6.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.class").value(LdapSync.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.ldapExternalSystemConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.subjectSourcesToProvision").value("jdbc").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.provisioningType").value("entityAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.userSearchBaseDn").value("ou=People,dc=example,dc=edu").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.hasTargetEntityLink").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperMemberships").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteEntitiesIfNotExistInGrouper").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.logAllObjectsVerbose").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();

  }
  
  public static void configureUserAttributesGroupExtensionOrMetadata() {
  
    configureUserAttributeGroupExtension();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.configureMetadata").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.metadata.0.formElementType").value("text").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.metadata.0.name").value("md_entitlementValue").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.metadata.0.showForGroup").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.metadata.0.valueType").value("string").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.numberOfGroupAttributes").value("1").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.numberOfMetadata").value("1").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.operateOnGrouperGroups").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetEntityAttribute.2.translateFromGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetGroupAttribute.0.name").value("entitlement").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateExpression")
      .value("${grouperUtil.defaultIfBlank(grouperProvisioningGroup.retrieveAttributeValueString('md_entitlementValue') , grouperProvisioningGroup.extension )}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.targetGroupAttribute.0.valueType").value("string").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  
  }

  public static void configureBushyEntityDnManyAttributes() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "6");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.name").value("businessCategory").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("extension").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression").value("${grouperUtil.toSet('top', 'groupOfNames')}").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.name").value("member").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.defaultValue").value("cn=admin,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField").value("memberToId2").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField").value("description").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpression").value("${'uid=' + grouperProvisioningEntity.getSubjectId() + ',ou=People,dc=example,dc=edu'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.name").value("uid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.name").value("sn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.translateFromStaticValues").value("something").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.translateFromStaticValues").value("something").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.translateFromStaticValues").value("something").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.translateExpressionType").value("staticValues").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.translateFromStaticValues").value("top, organizationalPerson, person, inetOrgPerson, eduPerson").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.update").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.class").value(LdapSync.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.ldapExternalSystemConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.subjectSourcesToProvision").value("jdbc").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.provisioningType").value("groupAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupSearchBaseDn").value("ou=Groups,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.userSearchBaseDn").value("ou=People,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupDnType").value("bushy").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.hasTargetEntityLink").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperMemberships").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.updateEntities").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.logAllObjectsVerbose").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();

  }
  
  /**
   * 
   * @param groupDnType flat or busy
   * @param groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or null
   * @param explicitFilters specify the filters and dont let Grouper pick them
   * @param translateFromGrouperProvisioningGroupField is name or extension
   * @param updateGroupsAndDn if update group and dn
   * @param businessCategoryTranslateFromGrouperProvisioningGroupField idIndex or id
   * @param membershipAttribute member or description
   * @param entityUidTranslateFromGrouperProvisioningEntityField subjectId or subjectIdentifier0
   * @param subjectSourcesToProvision personLdapSource or jdbc
   * @param insertEntityAttributes 
   * @param entityAttributeCount 2 or 6
   */
  public static void configureGroupAttributesWithEntityDn(String groupDnType, String groupDeleteType, 
      boolean explicitFilters, String translateFromGrouperProvisioningGroupField, boolean updateGroupsAndDn,
      String businessCategoryTranslateFromGrouperProvisioningGroupField, String membershipAttribute, 
      String entityUidTranslateFromGrouperProvisioningEntityField, String subjectSourcesToProvision,
      boolean insertEntityAttributes, int entityAttributeCount) {
  
    if (!StringUtils.equals("member", membershipAttribute) && !StringUtils.equals("description", membershipAttribute)) {
      throw new RuntimeException("Expecting member or description but was '" + membershipAttribute + "'");
    }
    if (2 != entityAttributeCount && 6 != entityAttributeCount) {
      throw new RuntimeException("Expecting 2 or 6 for entityAttributeCount but was '" + entityAttributeCount + "'");
    }
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "6");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.select").value("true").store();
    if (updateGroupsAndDn) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.update").value("true").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value(translateFromGrouperProvisioningGroupField).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.name").value("businessCategory").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.insert").value("true").store();
    if (StringUtils.equals(businessCategoryTranslateFromGrouperProvisioningGroupField, "idIndex")) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.valueType").value("long").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value(businessCategoryTranslateFromGrouperProvisioningGroupField).store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("extension").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression").value("${grouperUtil.toSet('top', 'groupOfNames')}").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.name").value("member").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.valueType").value("string").store();
    if (StringUtils.equals(membershipAttribute, "member")) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.defaultValue").value("cn=admin,dc=example,dc=edu").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField").value("memberToId2").store();
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.translateExpressionType").value("translationScript").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.translateExpression").value("'cn' + '=' + 'somethingbogussincethisisrequired'").store();
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.valueType").value("string").store();
    if (StringUtils.equals(membershipAttribute, "description")) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.multiValued").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.membershipAttribute").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.translateFromMemberSyncField").value("memberToId2").store();
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.insert").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.update").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.select").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpressionType").value("grouperProvisioningGroupField").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField").value("description").store();
    }    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttributeCount").value(entityAttributeCount + "").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.name").value("ldap_dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.select").value("true").store();
    if (insertEntityAttributes) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.insert").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionTypeCreateOnly").value("translationScript").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionCreateOnly").value("${'uid=' + grouperProvisioningEntity.retrieveAttributeValueString('" + entityUidTranslateFromGrouperProvisioningEntityField + "') + ',ou=People,dc=example,dc=edu'}").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.name").value("uid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.select").value("true").store();
    if (insertEntityAttributes) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.required").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.insert").value("true").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value(entityUidTranslateFromGrouperProvisioningEntityField).store();
    
    if (entityAttributeCount == 6) {
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.name").value("sn").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.valueType").value("string").store();
      if (insertEntityAttributes) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.insert").value("true").store();
      }        
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.2.translateExpressionCreateOnly").value("'something'").store();
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.name").value("cn").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.valueType").value("string").store();
      if (insertEntityAttributes) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.insert").value("true").store();
      }
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.3.translateExpressionCreateOnly").value("'something'").store();
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.name").value("givenName").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.valueType").value("string").store();
      if (insertEntityAttributes) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.insert").value("true").store();
      }
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.4.translateExpressionCreateOnly").value("'something'").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.name").value("objectClass").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.valueType").value("string").store();
      if (insertEntityAttributes) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.insert").value("true").store();
      }
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.multiValued").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetEntityAttribute.5.translateFromStaticValuesCreateOnly").value("top, organizationalPerson, person, inetOrgPerson, eduPerson").store();


    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.class").value(LdapSync.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.ldapExternalSystemConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.subjectSourcesToProvision").value(subjectSourcesToProvision).store();
  
    if (!StringUtils.isBlank(groupDeleteType)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroups").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest." + groupDeleteType).value("true").store();
    }
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.userSearchBaseDn").value("ou=People,dc=example,dc=edu").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.hasTargetGroupLink").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.provisioningType").value("groupAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectMemberships").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupDnType").value(groupDnType).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupSearchBaseDn").value("ou=Groups,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertGroups").value("true").store();
    if (insertEntityAttributes) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertEntities").value("true").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectGroups").value("true").store();
    if (updateGroupsAndDn) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.updateGroups").value("true").store();
    }
  
    if (explicitFilters) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupSearchAllFilter").value("(objectClass=groupOfNames)").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.groupSearchFilter").value("(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.userSearchAllFilter").value("(&(objectClass=person)(uid=*))").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.userSearchFilter").value("(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))").store();
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.selectEntities").value("true").store();
  
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.logAllObjectsVerbose").value("true").store();
  
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.ldapProvTestCLC.class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.ldapProvTestCLC.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.ldapProvTestCLC.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.ldapProvTestCLC.provisionerConfigId").value("ldapProvTest").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.ldapProvTestCLC.provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.ldapProvTestCLC.publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
