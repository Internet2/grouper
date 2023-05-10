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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.app.usdu.UsduSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.examples.AttributeAutoCreateHook;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.AddMissingGroupSets;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

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
      //new SyncPITTables().processMissingActivePITGroupSets();
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
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V8 {
    
    @Override
    public void updateVersionFromPrevious() {
      
      // ok nulls are allowed so make the change
      GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
      String sql = null;
      
      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_members MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_members MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_members ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      // TODO check to see if non null
      try {
        new GcDbAccess().sql(sql).executeSql();
      } catch (Exception e) {
        LOG.error("Cannot run upgrade task V8!", e);
      }

    }
  }
  , 
  V9{
    
    @Override
    public void updateVersionFromPrevious() {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
      
          String[] attributeDefNames = new String[] {
              // etc:attribute:entities:entitySubjectIdentifierDef
              EntityUtils.attributeEntityStemName() + ":entitySubjectIdentifierDef",
              // etc:attribute:permissionLimits:limitsDef
              PermissionLimitUtils.attributeLimitStemName() + ":" + PermissionLimitUtils.LIMIT_DEF,
              // etc:attribute:permissionLimits:limitsDefInt
              PermissionLimitUtils.attributeLimitStemName() + ":" + PermissionLimitUtils.LIMIT_DEF_INT,
              // etc:attribute:permissionLimits:limitsDefMarker
              PermissionLimitUtils.attributeLimitStemName() + ":" + PermissionLimitUtils.LIMIT_DEF_MARKER
              
              
          };

          for (String attributeDefName : attributeDefNames) {
            AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefName, false);
            
            if (attributeDef != null) {
              attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
              attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
            }
          }          
          
          
          return null;
        }
      });
    }
  },
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V10 {
    
    @Override
    public void updateVersionFromPrevious() {
      
      // ok nulls are allowed so make the change
      GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
      String sql = null;
      
      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_groups MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_groups MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_groups ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      // TODO check to see if non null
      try {
        new GcDbAccess().sql(sql).executeSql();
      } catch (Exception e) {
        LOG.error("Cannot run upgrade task V10!", e);
      }
      
      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_fields MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_fields MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_fields ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      // TODO check to see if non null
      try {
        new GcDbAccess().sql(sql).executeSql();
      } catch (Exception e) {
        LOG.error("Cannot run upgrade task V10!", e);
      }

      // cant add foreign key until this is there
      if (GrouperDdlUtils.isOracle()) {
        
        // TODO check to see if constraint is there
        sql = "ALTER TABLE grouper_fields ADD CONSTRAINT grouper_fie_internal_id_unq unique (internal_id)";
        
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          LOG.error("Cannot run upgrade task V10!", e);
        }
        
        sql = "ALTER TABLE grouper_groups ADD CONSTRAINT grouper_grp_internal_id_unq unique (internal_id)";
        
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          LOG.error("Cannot run upgrade task V10!", e);
        }

        sql = "ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id)";
        
        // TODO check to see if constraint is there
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          LOG.error("Cannot run upgrade task V10!", e);
        }
      }

    }
  }
  , 

  ;
  
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

}
