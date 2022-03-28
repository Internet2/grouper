/**
 * Copyright 2019 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.app.usdu.UsduSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.hooks.examples.AttributeAutoCreateHook;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.AddMissingGroupSets;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SyncPITTables;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author shilen
 */
public enum UpgradeTasks implements UpgradeTasksInterface {
  

  /**
   * add groupAttrRead/groupAttrUpdate group sets for entities
   */
  V1 {

    @Override
    public void updateVersionFromPrevious() {
      new AddMissingGroupSets().addMissingSelfGroupSetsForGroups();
      new SyncPITTables().processMissingActivePITGroupSets();
    }
  },
  
  /**
   * move subject resolution status attributes to member table
   */
  V2 {

    @Override
    public void updateVersionFromPrevious() {
      AttributeDefName deletedMembersAttr = AttributeDefNameFinder.findByName(UsduSettings.usduStemName() + ":subjectResolutionDeleted", false);

      if (deletedMembersAttr != null) {
        Set<Member> deletedMembers = new MemberFinder()
            .assignAttributeCheckReadOnAttributeDef(false)
            .assignNameOfAttributeDefName(UsduSettings.usduStemName() + ":subjectResolutionDeleted")
            .addAttributeValuesOnAssignment("true")
            .findMembers();
        
        for (Member deletedMember : deletedMembers) {
          deletedMember.setSubjectResolutionDeleted(true);
          deletedMember.setSubjectResolutionResolvable(false);
          deletedMember.store();
        }
        
        deletedMembersAttr.delete();
      }
      
      AttributeDefName resolvableMembersAttr = AttributeDefNameFinder.findByName(UsduSettings.usduStemName() + ":subjectResolutionResolvable", false);

      if (resolvableMembersAttr != null) {
        Set<Member> unresolvableMembers = new MemberFinder()
            .assignAttributeCheckReadOnAttributeDef(false)
            .assignNameOfAttributeDefName(UsduSettings.usduStemName() + ":subjectResolutionResolvable")
            .addAttributeValuesOnAssignment("false")
            .findMembers();
        
        for (Member unresolvableMember : unresolvableMembers) {
          unresolvableMember.setSubjectResolutionResolvable(false);
          unresolvableMember.store();
        }
        
        resolvableMembersAttr.delete();
      }
    }
  },
  V3{

    @Override
    public void updateVersionFromPrevious() {
      GrouperRecentMemberships.upgradeFromV2_5_29_to_V2_5_30();
    }
    
  },
  V4{

    @Override
    public void updateVersionFromPrevious() {

      String recentMembershipsRootStemName = GrouperRecentMemberships.recentMembershipsStemName();
      String recentMembershipsMarkerDefName = recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER_DEF;
      AttributeDef recentMembershipsMarkerDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
          recentMembershipsMarkerDefName, true, new QueryOptions().secondLevelCache(false));

      // these attribute tell a grouper rule to auto assign the three name value pair attributes to the assignment when the marker is assigned
      AttributeDefName autoCreateMarker = AttributeDefNameFinder.findByName(AttributeAutoCreateHook.attributeAutoCreateStemName() 
          + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER, true);
      AttributeDefName thenNames = AttributeDefNameFinder.findByName(AttributeAutoCreateHook.attributeAutoCreateStemName() 
          + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_THEN_NAMES_ON_ASSIGN, true);

      AttributeAssign attributeAssign = recentMembershipsMarkerDef.getAttributeDelegate().retrieveAssignment("assign", autoCreateMarker, false, false);

      if (attributeAssign != null) {
        
        String thenNamesValue = attributeAssign.getAttributeValueDelegate().retrieveValueString(thenNames.getName());
        String shouldHaveValue = recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS
            + ", " + recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM 
                + ", " + recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT;
        if (!StringUtils.equals(thenNamesValue, shouldHaveValue)) {
          attributeAssign.getAttributeValueDelegate().assignValue(thenNames.getName(), shouldHaveValue);
        }
      }

      
    }
    
  },
  V5 {

    @Override
    public void updateVersionFromPrevious() {

      RuleUtils.changeInheritedPrivsToActAsGrouperSystem();
      
    }
    
  },
  V6 {

    @Override
    public void updateVersionFromPrevious() {

      new AddMissingGroupSets().addMissingSelfGroupSetsForStems();

    }
    
  },
  V7 {
    
    @Override
    public void updateVersionFromPrevious() {

      Pattern gshTemplateFolderUuidsToShowPattern = Pattern.compile("^grouperGshTemplate\\.([^.]+)\\.folderUuidsToShow$");
      
      Map<String, String> properties = GrouperConfig.retrieveConfig().propertiesMap(gshTemplateFolderUuidsToShowPattern);
      
      if (GrouperUtil.length(properties) > 0) {
        
        for (String key : properties.keySet()) {
          
          Matcher matcher = gshTemplateFolderUuidsToShowPattern.matcher(key);
          if (matcher.matches()) {
            String configId = matcher.group(1);
            String folderUuidsToShow = properties.get("grouperGshTemplate." + configId + ".folderUuidsToShow");
            folderUuidsToShow = StringUtils.trim(folderUuidsToShow);
            
            String singularFolderUuidToShow = GrouperConfig.retrieveConfig().propertyValueString("grouperGshTemplate." + configId + ".folderUuidToShow");
            singularFolderUuidToShow = StringUtils.trim(singularFolderUuidToShow);
            
            if (!StringUtils.equals(folderUuidsToShow, singularFolderUuidToShow)) {
              new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate." + configId + ".folderUuidToShow")
              .value(folderUuidsToShow).store();
            }
            
          }
        }
        
      }

    }
  }, 
  V8 { 
    
    @Override
    public void updateVersionFromPrevious() {
  
      v8_provisioningSubjectSourcesInEntity();
      
      v8_provisioningLdapDnAttributeChange();
     
      v8_provisioningFieldNameToAttributeChange();
      
      v8_provisioningSelectAllEntitiesDefault();
      
      v8_provisioningEntityResolverRefactor();
      
      v8_provisioningCustomizeMembershipCrud();

      v8_provisioningCustomizeGroupCrud();
    }
  };
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTasks.class);

  private static int currentVersion = -1;
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (UpgradeTasks task : UpgradeTasks.values()) {
        String number = task.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
  }

  /**
   * @return if did something
   */
  public static boolean v8_provisioningLdapDnAttributeChange() {
    // GRP-3931: change ldap DN from field name to attribute ldap_dn
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String className = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configId + ".class");
      if (!StringUtils.equals(className, LdapSync.class.getName())) {
        continue;
      }
      for (String objectType : new String[] {"Entity", "Group" }) {
        // lets look at group and entity attributes
        for (int i=0;i<20;i++) {
          String fieldNameKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".fieldName";
          String fieldName = GrouperLoaderConfig.retrieveConfig().propertyValueString(fieldNameKey);
          if (!StringUtils.isBlank(fieldName) && StringUtils.equals(fieldName, "name")) {
            didSomething = true;

            {
              String nameKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".name";
              grouperLoaderConfigUpdate(nameKey, "ldap_dn");
            }
            
            {
              String isFieldElseAttributeKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".isFieldElseAttribute";
              boolean isFieldElseAttribute =GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(isFieldElseAttributeKey, false);
              if (isFieldElseAttribute) {
                grouperLoaderConfigUpdate(isFieldElseAttributeKey, "false");
              }
            }
            grouperLoaderConfigDelete(fieldNameKey);
          }
        }
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for LDAP DN field name change to ldap_dn attribute";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }

  /**
   * @return if did something
   */
  public static boolean v8_provisioningSubjectSourcesInEntity() {
    // GRP-3911: subject sources to provision should not be in top config section
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.subjectSourcesToProvision$"));
    boolean didSomething = false;
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String subjectSourcesToProvision = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configId + ".subjectSourcesToProvision");
      String operateOnGrouperEntitiesKey = "provisioner." + configId + ".operateOnGrouperEntities";
      String operateOnGrouperEntities = GrouperLoaderConfig.retrieveConfig().propertyValueString(operateOnGrouperEntitiesKey);
      boolean operateOnGrouperEntitiesBoolean = GrouperUtil.booleanValue(operateOnGrouperEntities, false);
      if (!StringUtils.isBlank(subjectSourcesToProvision) && !operateOnGrouperEntitiesBoolean) {
        didSomething = true;
        grouperLoaderConfigUpdate(operateOnGrouperEntitiesKey, "true");
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for subjectSourcesToProvision requiring operateOnGrouperEntities";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }

  /**
   * 
   * @return if did something
   */
  public static boolean v8_provisioningFieldNameToAttributeChange() {
    // GRP-3927: There is no provisioning concept of field anymore, only attribute
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {
      for (String objectType : new String[] {"Entity", "Group", "Membership" }) {

        // lets look at group and entity and membership attributes
        Set<String> existingAttributeNames = new HashSet<String>();
        for (int i=0;i<20;i++) {
          String attributeNameKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".name";
          String attributeName = GrouperLoaderConfig.retrieveConfig().propertyValueString(attributeNameKey);
          if (!StringUtils.isBlank(attributeName)) {
            existingAttributeNames.add(attributeName);
          }
        }
        
        for (int i=0;i<20;i++) {
          String fieldNameKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".fieldName";
          String fieldName = GrouperLoaderConfig.retrieveConfig().propertyValueString(fieldNameKey);
          if (!StringUtils.isBlank(fieldName)) {
            didSomething = true;
            
            if (existingAttributeNames.contains(fieldName)) {
              throw new RuntimeException("Cannot continue since provisioning configId '" + configId + "' has a field with name '" + fieldName + "' and an attribute with the same name.  Either rename the attribute or contact the grouper team.");
            }
            
            
            {
              String nameKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".name";
              grouperLoaderConfigUpdate(nameKey, fieldName);
            }
            grouperLoaderConfigDelete(fieldNameKey);

          }
          {
            String isFieldElseAttributeKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".isFieldElseAttribute";
            String isFieldElseAttribute =GrouperLoaderConfig.retrieveConfig().propertyValueString(isFieldElseAttributeKey);
            if (!StringUtils.isBlank(isFieldElseAttribute)) {
              didSomething = true;
              grouperLoaderConfigDelete(isFieldElseAttributeKey);
            }
          }
            
        }
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for migrating provisioning fields to attributes";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }

  public static final Set<String> v8_entityResolverSuffixesToRefactor = GrouperUtil.toSet("entityAttributesNotInSubjectSource",
      "resolveAttributesWithSQL",
      "useGlobalSQLResolver",
      "globalSQLResolver",
      "sqlConfigId",
      "tableOrViewName",
      "columnNames",
      "subjectSourceIdColumn",
      "subjectSearchMatchingColumn",
      "sqlMappingType",
      "sqlMappingEntityAttribute",
      "sqlMappingExpression",
      "lastUpdatedColumn",
      "lastUpdatedType",
      "selectAllSQLOnFull",
      "resolveAttributesWithLDAP",
      "useGlobalLDAPResolver",
      "globalLDAPResolver",
      "ldapConfigId",
      "baseDN",
      "subjectSourceId",
      "searchScope",
      "filterPart",
      "attributes",
      "multiValuedLdapAttributes",
      "ldapMatchingSearchAttribute",
      "ldapMappingType",
      "ldapMappingEntityAttribute",
      "ldapMatchingExpression",
      "filterAllLDAPOnFull",
      "lastUpdatedAttribute",
      "lastUpdatedFormat" );
  
  /**
   * 
   * @return if did something
   */
  public static boolean v8_provisioningEntityResolverRefactor() {
    
    // FROM provisioner.genericProvisioner.entityAttributesNotInSubjectSource
    // TO provisioner.genericProvisioner.entityResolver.entityAttributesNotInSubjectSource
    
    // GRP-3939: Refactor entity attribute resolver config
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {

      for (String suffixToRefactor : v8_entityResolverSuffixesToRefactor) {
        
        String resolverKeyOld = "provisioner." + configId + "." + suffixToRefactor;
        String resolverKeyNew = "provisioner." + configId + ".entityResolver." + suffixToRefactor;
        String resolverValue = GrouperLoaderConfig.retrieveConfig().propertyValueString(resolverKeyOld);
        if (StringUtils.isBlank(resolverValue)) {
          continue;
        }
        didSomething = true;

        grouperLoaderConfigUpdate(resolverKeyNew, resolverValue);
        grouperLoaderConfigDelete(resolverKeyOld);
        
      }

    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for entity resolver refactor";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }
  
  /**
   * 
   * @return if did something
   */
  public static boolean v8_provisioningSelectAllEntitiesDefault() {
    // GRP-3938: provisioning selectAllEntities should not have a default
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String selectAllEntitiesKey = "provisioner." + configId + ".selectAllEntities";
      String selectAllEntities = GrouperLoaderConfig.retrieveConfig().propertyValueString(selectAllEntitiesKey);
      if (StringUtils.isBlank(selectAllEntities)) {
        continue;
      }
      didSomething = true;
      grouperLoaderConfigUpdate(selectAllEntitiesKey, "false");
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for 'select all entities' default";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }

  /**
   * 
   * @return if did something
   */
  public static boolean v8_provisioningCustomizeMembershipCrud() {
    // GRP-3953: add provisioning customizeMembershipCrud
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String operateOnGrouperMembershipsKey = "provisioner." + configId + ".operateOnGrouperMemberships";
      String customizeMembershipCrudKey = "provisioner." + configId + ".customizeMembershipCrud";
      String insertMembershipsKey = "provisioner." + configId + ".insertMemberships";
      String selectMembershipsKey = "provisioner." + configId + ".selectMemberships";
      String deleteMembershipsKey = "provisioner." + configId + ".deleteMemberships";
      String deleteMembershipsIfNotExistInGrouperKey = "provisioner." + configId + ".deleteMembershipsIfNotExistInGrouper";
      String deleteMembershipsIfGrouperDeletedKey = "provisioner." + configId + ".deleteMembershipsIfGrouperDeleted";
      String deleteMembershipsIfGrouperCreatedKey = "provisioner." + configId + ".deleteMembershipsIfGrouperCreated";
      
      Boolean customizeMembershipCrud = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(customizeMembershipCrudKey));
      
      // if we are already up to date
      if (customizeMembershipCrud != null) {
        continue;
      }

      Boolean operateOnGrouperMemberships = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(operateOnGrouperMembershipsKey));
      Boolean insertMemberships = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(insertMembershipsKey));
      Boolean selectMemberships = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(selectMembershipsKey));
      Boolean deleteMemberships = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteMembershipsKey));
      Boolean deleteMembershipsIfNotExistInGrouper = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteMembershipsIfNotExistInGrouperKey));
      Boolean deleteMembershipsIfGrouperDeleted = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteMembershipsIfGrouperDeletedKey));
      Boolean deleteMembershipsIfGrouperCreated = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteMembershipsIfGrouperCreatedKey));

      // if nothing set thats ok
      if ((operateOnGrouperMemberships == null || !operateOnGrouperMemberships) 
          && customizeMembershipCrud == null
          && insertMemberships == null
          && deleteMemberships == null
          && deleteMembershipsIfNotExistInGrouper == null
          && deleteMembershipsIfGrouperDeleted == null
          && deleteMembershipsIfGrouperCreated == null ) {
        continue;
      }

      didSomething = true;


      // if we are somehow at the default
      if (insertMemberships != null && insertMemberships
          && selectMemberships != null && selectMemberships
          && deleteMemberships != null && deleteMemberships
          && (deleteMembershipsIfNotExistInGrouper == null || !deleteMembershipsIfNotExistInGrouper)
          && (deleteMembershipsIfGrouperDeleted == null || !deleteMembershipsIfGrouperDeleted)
          && deleteMembershipsIfGrouperCreated != null && deleteMembershipsIfGrouperCreated) {
        grouperLoaderConfigDelete(insertMembershipsKey);
        grouperLoaderConfigDelete(selectMembershipsKey);
        grouperLoaderConfigDelete(deleteMembershipsKey);
        if (deleteMembershipsIfNotExistInGrouper != null) {
          grouperLoaderConfigDelete(deleteMembershipsIfNotExistInGrouperKey);
        }
        if (deleteMembershipsIfGrouperDeleted != null) {
          grouperLoaderConfigDelete(deleteMembershipsIfGrouperDeletedKey);
        }
        grouperLoaderConfigDelete(deleteMembershipsIfGrouperCreatedKey);
      } else {
        grouperLoaderConfigUpdate(customizeMembershipCrudKey, "true");
        
        if (insertMemberships == null) {
          grouperLoaderConfigUpdate(insertMembershipsKey, "false");
        } else if (insertMemberships) {
          grouperLoaderConfigDelete(insertMembershipsKey);
        }
        if (selectMemberships == null) {
          grouperLoaderConfigUpdate(selectMembershipsKey, "false");
        } else if (selectMemberships) {
          grouperLoaderConfigDelete(selectMembershipsKey);
        }
        if (deleteMemberships == null) {
          grouperLoaderConfigUpdate(deleteMembershipsKey, "false");
        } else if (deleteMemberships && deleteMembershipsIfGrouperCreated != null && deleteMembershipsIfGrouperCreated) {
          grouperLoaderConfigDelete(deleteMembershipsKey);
          grouperLoaderConfigDelete(deleteMembershipsIfGrouperCreatedKey);
        }
        
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for 'membership CRUD defaults'";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }

  /**
   * 
   * @return if did something
   */
  public static boolean v8_provisioningCustomizeGroupCrud() {
    // GRP-3953: add provisioning customizeGroupCrud
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String operateOnGrouperGroupsKey = "provisioner." + configId + ".operateOnGrouperGroups";
      String customizeGroupCrudKey = "provisioner." + configId + ".customizeGroupCrud";
      String insertGroupsKey = "provisioner." + configId + ".insertGroups";
      String updateGroupsKey = "provisioner." + configId + ".updateGroups";
      String selectGroupsKey = "provisioner." + configId + ".selectGroups";
      String deleteGroupsKey = "provisioner." + configId + ".deleteGroups";
      String deleteGroupsIfNotExistInGrouperKey = "provisioner." + configId + ".deleteGroupsIfNotExistInGrouper";
      String deleteGroupsIfGrouperDeletedKey = "provisioner." + configId + ".deleteGroupsIfGrouperDeleted";
      String deleteGroupsIfGrouperCreatedKey = "provisioner." + configId + ".deleteGroupsIfGrouperCreated";
      
      Boolean customizeGroupCrud = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(customizeGroupCrudKey));
      Boolean operateOnGrouperGroups = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(operateOnGrouperGroupsKey));
      
      // if we are already up to date
      if (operateOnGrouperGroups != null && operateOnGrouperGroups && customizeGroupCrud != null) {
        continue;
      }

      Boolean insertGroups = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(insertGroupsKey));
      Boolean updateGroups = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(updateGroupsKey));
      Boolean selectGroups = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(selectGroupsKey));
      Boolean deleteGroups = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteGroupsKey));
      Boolean deleteGroupsIfNotExistInGrouper = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteGroupsIfNotExistInGrouperKey));
      Boolean deleteGroupsIfGrouperDeleted = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteGroupsIfGrouperDeletedKey));
      Boolean deleteGroupsIfGrouperCreated = GrouperUtil.booleanObjectValue(GrouperLoaderConfig.retrieveConfig().propertyValueString(deleteGroupsIfGrouperCreatedKey));

      // if nothing set thats ok
      if ((operateOnGrouperGroups == null || !operateOnGrouperGroups) 
          && customizeGroupCrud == null
          && insertGroups == null
          && updateGroups == null
          && deleteGroups == null
          && deleteGroupsIfNotExistInGrouper == null
          && deleteGroupsIfGrouperDeleted == null
          && deleteGroupsIfGrouperCreated == null ) {
        continue;
      }
          
      didSomething = true;

      // if we are somehow at the default
      if (insertGroups != null && insertGroups
          && updateGroups != null && updateGroups
          && selectGroups != null && selectGroups
          && deleteGroups != null && deleteGroups
          && (deleteGroupsIfNotExistInGrouper == null || !deleteGroupsIfNotExistInGrouper)
          && (deleteGroupsIfGrouperDeleted == null || !deleteGroupsIfGrouperDeleted)
          && deleteGroupsIfGrouperCreated != null && deleteGroupsIfGrouperCreated) {
        grouperLoaderConfigDelete(insertGroupsKey);
        grouperLoaderConfigDelete(updateGroupsKey);
        grouperLoaderConfigDelete(selectGroupsKey);
        grouperLoaderConfigDelete(deleteGroupsKey);
        if (deleteGroupsIfNotExistInGrouper != null) {
          grouperLoaderConfigDelete(deleteGroupsIfNotExistInGrouperKey);
        }
        if (deleteGroupsIfGrouperDeleted != null) {
          grouperLoaderConfigDelete(deleteGroupsIfGrouperDeletedKey);
        }
        grouperLoaderConfigDelete(deleteGroupsIfGrouperCreatedKey);
      } else {
        grouperLoaderConfigUpdate(customizeGroupCrudKey, "true");
        
        if (insertGroups == null) {
          grouperLoaderConfigUpdate(insertGroupsKey, "false");
        } else if (insertGroups) {
          grouperLoaderConfigDelete(insertGroupsKey);
        }
        if (updateGroups == null) {
          grouperLoaderConfigUpdate(updateGroupsKey, "false");
        } else if (updateGroups) {
          grouperLoaderConfigDelete(updateGroupsKey);
        }
        if (selectGroups == null) {
          grouperLoaderConfigUpdate(selectGroupsKey, "false");
        } else if (selectGroups) {
          grouperLoaderConfigDelete(selectGroupsKey);
        }
        if (deleteGroups == null) {
          grouperLoaderConfigUpdate(deleteGroupsKey, "false");
        } else if (deleteGroups && deleteGroupsIfGrouperCreated != null && deleteGroupsIfGrouperCreated) {
          grouperLoaderConfigDelete(deleteGroupsKey);
          grouperLoaderConfigDelete(deleteGroupsIfGrouperCreatedKey);
        }
        
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for 'group CRUD defaults'";
      LOG.warn(action);
      System.out.println(action);
    } else {
      ConfigPropertiesCascadeBase.clearCache();
    }
    return didSomething;
  }

  private static void grouperLoaderConfigDelete(String key) {
    if (!key.startsWith("provisioner.")) {
      throw new RuntimeException("Invalid key, should start with provisioner.");
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key).delete();
    String action = "Provisioning upgrade: deleting grouper-loader.properties " + key;
    LOG.warn(action);
    System.out.println(action);
    
  }
  
  private static void grouperLoaderConfigUpdate(String key, String value) {
    if (!key.startsWith("provisioner.")) {
      throw new RuntimeException("Invalid key, should start with provisioner.");
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key).value(value).store();
    String action = "Provisioning upgrade: setting grouper-loader.properties " + key + " = " + value;
    LOG.warn(action);
    System.out.println(action);

  }
}
