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

  public static void v8_provisioningLdapDnAttributeChange() {
    // GRP-3931: change ldap DN from field name to attribute ldap_dn
    boolean didSomething = false;
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.class$"));
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String className = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configId + ".className");
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
              String nameKey = "provisioner." + configId + ".targetEntityAttribute." + i + ".name";
              new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(nameKey).value("ldap_dn").store();
              String action = "Provisioning upgrade: setting grouper-loader.properties " + nameKey + " = ldap_dn";
              LOG.warn(action);
              System.out.println(action);
            }
            
            {
              String isFieldElseAttributeKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".isFieldElseAttribute";
              boolean isFieldElseAttribute =GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(isFieldElseAttributeKey, false);
              if (isFieldElseAttribute) {
                new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(isFieldElseAttributeKey).value("false").store();
                String action = "Provisioning upgrade: setting grouper-loader.properties " + isFieldElseAttributeKey + " = false";
                LOG.warn(action);
                System.out.println(action);
              }
            }
            
            new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(fieldNameKey).delete();
            String action = "Provisioning upgrade: deleting grouper-loader.properties " + fieldNameKey;
            LOG.warn(action);
            System.out.println(action);
          }
        }
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for LDAP DN field name change to ldap_dn attribute";
      LOG.warn(action);
      System.out.println(action);
    }
  }

  public static void v8_provisioningSubjectSourcesInEntity() {
    // GRP-3911: subject sources to provision should not be in top config section
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^provisioner\\.([^.]+)\\.subjectSourcesToProvision$"));
    boolean didSomething = false;
    for (String configId : GrouperUtil.nonNull(configIds)) {
      String subjectSourcesToProvision = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configId + ".subjectSourcesToProvision");
      String operateOnGrouperEntitiesKey = "provisioner." + configId + ".operateOnGrouperEntities";
      String operateOnGrouperEntities = GrouperLoaderConfig.retrieveConfig().propertyValueString(operateOnGrouperEntitiesKey);
      boolean operateOnGrouperEntitiesBoolean = GrouperUtil.booleanValue(operateOnGrouperEntities, false);
      if (!StringUtils.isBlank(subjectSourcesToProvision) && !operateOnGrouperEntitiesBoolean) {
        String action = "Provisioning upgrade: setting grouper-loader.properties " + operateOnGrouperEntitiesKey + " = true";
        LOG.warn(action);
        System.out.println(action);
        didSomething = true;
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(operateOnGrouperEntitiesKey).value("true").store();
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for subjectSourcesToProvision requiring operateOnGrouperEntities";
      LOG.warn(action);
      System.out.println(action);
    }
  }

  public static void v8_provisioningFieldNameToAttributeChange() {
    // GRP-3931: change ldap DN from field name to attribute ldap_dn
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
              String nameKey = "provisioner." + configId + ".targetEntityAttribute." + i + ".name";
              new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(nameKey).value(fieldName).store();
              String action = "Provisioning upgrade: setting grouper-loader.properties " + nameKey + " = " + fieldName;
              LOG.warn(action);
              System.out.println(action);
            }
            
            {
              String isFieldElseAttributeKey = "provisioner." + configId + ".target" + objectType + "Attribute." + i + ".isFieldElseAttribute";
              String isFieldElseAttribute =GrouperLoaderConfig.retrieveConfig().propertyValueString(isFieldElseAttributeKey);
              if (!StringUtils.isBlank(isFieldElseAttribute)) {
                new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(isFieldElseAttributeKey).delete();
                String action = "Provisioning upgrade: deleting grouper-loader.properties " + isFieldElseAttributeKey;
                LOG.warn(action);
                System.out.println(action);
              }
            }
            
            new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(fieldNameKey).delete();
            String action = "Provisioning upgrade: deleting grouper-loader.properties " + fieldNameKey;
            LOG.warn(action);
            System.out.println(action);
          }
        }
      }
    }
    if (!didSomething) {
      String action = "Provisioning upgrade: no change for migrating provisioning fields to attributes";
      LOG.warn(action);
      System.out.println(action);
    }
  }
}
