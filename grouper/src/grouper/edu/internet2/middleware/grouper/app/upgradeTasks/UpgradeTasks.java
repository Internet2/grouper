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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
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
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
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
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new AddMissingGroupSets().addMissingSelfGroupSetsForGroups();
      //new SyncPITTables().processMissingActivePITGroupSets();
    }
  },
  
  /**
   * move subject resolution status attributes to member table
   */
  V2 {

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
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
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      GrouperRecentMemberships.upgradeFromV2_5_29_to_V2_5_30();
    }
    
  },
  V4{

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {

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
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {

      RuleUtils.changeInheritedPrivsToActAsGrouperSystem();
      
    }
    
  },
  V6 {

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {

      new AddMissingGroupSets().addMissingSelfGroupSetsForStems();

    }
    
  },
  V7 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {

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
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      // make sure id_index is populated in grouper_members and make column not null

      // check if grouper_members.id_index is already not null - better to use ddlutils here or check the resultset?
      /*
      Platform platform = GrouperDdlUtils.retrievePlatform(false);
      GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
      Connection connection = null;
      try {
        connection = grouperDb.connection();
        int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion("Grouper"); 
        DdlVersionable ddlVersionable = GrouperDdlUtils.retieveVersion("Grouper", javaVersion);
        DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionable);
        platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
        platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
        
        Database database = platform.readModelFromDatabase(connection, "grouper", null, null, null);
        Table membersTable = database.findTable(Member.TABLE_GROUPER_MEMBERS);
        Column idIndexColumn = membersTable.findColumn(Member.COLUMN_ID_INDEX);
        
        if (idIndexColumn.isRequired()) {
          return;
        }
      } finally {
        GrouperUtil.closeQuietly(connection);
      }
      */
      
      if (!GrouperDdlUtils.isColumnNullable("grouper_members", "id_index", "subject_id", "GrouperSystem")) {
        return;
      }
      
      // ok nulls are allowed so make the change
      GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
      
      String sql;
      
      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_members MODIFY (id_index NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_members MODIFY id_index BIGINT NOT NULL";
      } else {
        // assume postgres
        sql = "ALTER TABLE grouper_members ALTER COLUMN id_index SET NOT NULL";
      }
      
      HibernateSession.bySqlStatic().executeSql(sql);
    }
  }
  ,
  V21 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship")) {
            return null;
          }
          
          if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
            if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship_v", "mship_hst_internal_id")) {
              HibernateSession.bySqlStatic().executeSql("DROP VIEW grouper_sql_cache_mship_v");
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped view grouper_sql_cache_mship_v");
            }
          }
          
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "internal_id")) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN internal_id");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.internal_id");
          }
          
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "created_on")) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN created_on");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.created_on");
          }
          
          if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_group", "last_membership_sync")) {
            if (GrouperDdlUtils.isOracle()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD last_membership_sync DATE");
            } else if (GrouperDdlUtils.isMysql()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD COLUMN last_membership_sync DATETIME");
            } else {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD COLUMN last_membership_sync timestamp");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_sql_cache_group.last_membership_sync");
          }
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
            HibernateSession.bySqlStatic().executeSql("CREATE VIEW grouper_sql_cache_mship_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, flattened_add_timestamp, group_id, field_id, member_internal_id, group_internal_id, field_internal_id) AS SELECT gg.name AS group_name, gf.name AS list_name, gm.subject_id, gm.subject_identifier0,  gm.subject_identifier1, gm.subject_identifier2, gm.subject_source, gscm.flattened_add_timestamp,  gg.id AS group_id, gf.id AS field_id, gm.internal_id AS member_internal_id,  gg.internal_id AS group_internal_id, gf.internal_id AS field_internal_id  FROM grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_fields gf,  grouper_groups gg, grouper_members gm  WHERE gscg.group_internal_id = gg.internal_id AND gscg.field_internal_id = gf.internal_id  AND gscm.sql_cache_group_internal_id = gscg.internal_id AND gscm.member_internal_id = gm.internal_id");
            
            if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
              if (GrouperDdlUtils.isOracle()) {
                HibernateSession.bySqlStatic().executeSql("COMMENT ON TABLE grouper_sql_cache_mship_v IS 'SQL cache mship view'");
              } else {
                HibernateSession.bySqlStatic().executeSql("COMMENT ON VIEW grouper_sql_cache_mship_v IS 'SQL cache mship view'");
              }
              
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.group_name IS 'group_name: name of group'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.list_name IS 'list_name: name of list e.g. members or admins'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_id IS 'subject_id: subject id'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier0 IS 'subject_identifier0: subject identifier0 from subject source and members table'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier1 IS 'subject_identifier1: subject identifier1 from subject source and members table'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier2 IS 'subject_identifier2: subject identifier2 from subject source and members table'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_source IS 'subject_source: subject source id'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.flattened_add_timestamp IS 'flattened_add_timestamp: when this membership started'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.group_id IS 'group_id: uuid of group'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.field_id IS 'field_id: uuid of field'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.member_internal_id IS 'member_internal_id: member internal id'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.group_internal_id IS 'group_internal_id: group internal id'");
              HibernateSession.bySqlStatic().executeSql("COMMENT ON COLUMN grouper_sql_cache_mship_v.field_internal_id IS 'field_internal_id: field internal id'");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created view grouper_sql_cache_mship_v");
          }
          
          if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
            if (GrouperDdlUtils.isMysql()) {
              HibernateSession.bySqlStatic().executeSql("DROP INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship");
            } else {
              HibernateSession.bySqlStatic().executeSql("DROP INDEX grouper_sql_cache_mship2_idx");
            }
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index grouper_sql_cache_mship2_idx");
          }
          
          if (!GrouperDdlUtils.assertPrimaryKeyExists("grouper_sql_cache_mship")) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship ADD PRIMARY KEY (member_internal_id, sql_cache_group_internal_id)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added primary key to grouper_sql_cache_mship");
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group2_idx")) {
            HibernateSession.bySqlStatic().executeSql("CREATE INDEX grouper_sql_cache_group2_idx ON grouper_sql_cache_group (last_membership_sync)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_group2_idx");
          }
          
          return null;
        }
      });
    }
  }
  ,
  V22 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_pit_stems")) {
            return null;
          }
          
          if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_stems", "source_id_index")) {
            if (GrouperDdlUtils.isOracle()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE GROUPER_PIT_STEMS ADD source_id_index NUMBER(38)");
            } else {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_pit_stems ADD COLUMN source_id_index BIGINT");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_stems.source_id_index");
          }
         
          if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_attribute_def", "source_id_index")) {
            if (GrouperDdlUtils.isOracle()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE GROUPER_PIT_ATTRIBUTE_DEF ADD source_id_index NUMBER(38)");
            } else {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_pit_attribute_def ADD COLUMN source_id_index BIGINT");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_attribute_def.source_id_index");
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_pit_stems", "pit_stem_source_idindex_idx")) {
            HibernateSession.bySqlStatic().executeSql("CREATE INDEX pit_stem_source_idindex_idx ON grouper_pit_stems (source_id_index)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_stem_source_idindex_idx");
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_pit_attribute_def", "pit_attrdef_source_idindex_idx")) {
            HibernateSession.bySqlStatic().executeSql("CREATE INDEX pit_attrdef_source_idindex_idx ON grouper_pit_attribute_def (source_id_index)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_attrdef_source_idindex_idx");
          }
          
          new GcDbAccess().sql("update grouper_pit_stems  ps set source_id_index = (select s.id_index from grouper_stems  s where ps.source_id = s.id) where ps.source_id_index is null and ps.active='T'").executeSql();
          new GcDbAccess().sql("update grouper_pit_attribute_def  pad set source_id_index = (select ad.id_index from grouper_attribute_def ad where pad.source_id = ad.id) where pad.source_id_index is null and pad.active='T'").executeSql();
          
          return null;
        }
      });
    }
  }
  ,
  V23{
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          AttributeDef sqlCacheableGroupDef = AttributeDefFinder.findByName(SqlCacheGroup.attributeDefFolderName() + ":sqlCacheableGroupDef", false);
          if (sqlCacheableGroupDef != null) {
            sqlCacheableGroupDef.delete();
          }
          
          AttributeDef sqlCacheableGroupMarkerDef = AttributeDefFinder.findByName(SqlCacheGroup.attributeDefFolderName() + ":sqlCacheableGroupMarkerDef", false);
          if (sqlCacheableGroupMarkerDef != null) {
            sqlCacheableGroupMarkerDef.delete();
          }
          
          return null;
        }
      });
    }
  }
  ,
  V9{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
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
  V14{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
      
          try {
            if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_st_idx")) {
              new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_st_idx ON grouper_loader_log (job_name,started_time)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_st_idx");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_st_idx exists already");
              }
            }
  
            if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s2_idx")) {
              new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_s2_idx ON grouper_loader_log (job_name,status,last_updated)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_s2");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_s2 exists already");
              }
            }
            if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s3_idx")) {
              new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_s3_idx ON grouper_loader_log (status,last_updated)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_s3");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_s3 exists already");
              }
            }
            if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s4_idx")) {
              new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_s4_idx ON grouper_loader_log (parent_job_name)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_s4");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_s4 exists already");
              }
            }
          } catch (Throwable t) {
            String message = "Could not perform upgrade task V10 on grouper_loader_log adding indexes for GRP-5195!  Skipping this upgrade task, install the indexes manually";
            LOG.error(message, t);
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", " + message);
            }
          }
          return null;
        }
      });
    }
  }, 
  
  V16 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          try {
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_user")) {
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_user (id_index BIGINT NOT NULL, grouper_sync_id varchar(40) NOT NULL, group_id varchar(40) NOT NULL, field_id varchar(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_user ( id_index NUMBER(38) NOT NULL, grouper_sync_id VARCHAR2(40) NOT NULL, group_id VARCHAR2(40) NOT NULL, field_id VARCHAR2(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_user ( id_index BIGINT NOT NULL, grouper_sync_id VARCHAR(40) NOT NULL, group_id VARCHAR(40) NOT NULL, field_id VARCHAR(40) NOT NULL, PRIMARY KEY (id_index))").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON TABLE grouper_sync_dep_group_user IS 'Groups are listed that are used in user translations.  Users will need to be recalced if there are changes (not membership recalc)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.id_index IS 'primary key'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.grouper_sync_id IS 'provisioner'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.group_id IS 'group uuid'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.field_id IS 'field uuid'").executeSql();

              }
              new GcDbAccess().sql("alter table grouper_sync_dep_group_user add CONSTRAINT grouper_sync_dep_grp_user_fk_2 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_sync_dep_group_user");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_sync_dep_group_user exists already");
              }
            }

            if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx0")) {
              new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_user_idx0 ON grouper_sync_dep_group_user (grouper_sync_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_dep_grp_user_idx0");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_user_idx0 exists already");
              }
            }
            if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1")) {
              new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sync_dep_grp_user_idx1 ON grouper_sync_dep_group_user (grouper_sync_id,group_id,field_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_dep_grp_user_idx1");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_user_idx1 exists already");
              }
            }

            if (!GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_group")) {
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_group (id_index BIGINT NOT NULL, grouper_sync_id varchar(40) NOT NULL, group_id varchar(40) NOT NULL, field_id varchar(40) NOT NULL, provisionable_group_id varchar(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_group ( id_index NUMBER(38) NOT NULL,  grouper_sync_id VARCHAR2(40) NOT NULL, group_id VARCHAR2(40) NOT NULL, field_id VARCHAR2(40) NOT NULL, provisionable_group_id VARCHAR2(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_group ( id_index BIGINT NOT NULL, grouper_sync_id VARCHAR(40) NOT NULL, group_id VARCHAR(40) NOT NULL, field_id VARCHAR(40) NOT NULL, provisionable_group_id VARCHAR(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON TABLE grouper_sync_dep_group_group IS 'Groups are listed that are used in group translations.  Provisionable groups will need to be recalced if there are changes (not membership recalc)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.id_index IS 'primary key'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.grouper_sync_id IS 'provisioner'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.group_id IS 'group uuid'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.field_id IS 'field uuid'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.provisionable_group_id IS 'group uuid of the provisionable group that uses this other group as a role'").executeSql();
              }
                
              new GcDbAccess().sql("alter table grouper_sync_dep_group_group add CONSTRAINT grouper_sync_dep_grp_grp_fk_1 FOREIGN KEY (provisionable_group_id) REFERENCES grouper_groups(id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              new GcDbAccess().sql("alter table grouper_sync_dep_group_group add CONSTRAINT grouper_sync_dep_grp_grp_fk_3 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_sync_dep_group_group");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_sync_dep_group_group exists already");
              }
            }

            if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx0")) {
              new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_grp_idx0 ON grouper_sync_dep_group_group (grouper_sync_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx0 exists already");
              }
            }
            if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx1")) {
              new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sync_dep_grp_grp_idx1 ON grouper_sync_dep_group_group (grouper_sync_id,group_id,field_id,provisionable_group_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx1 exists already");
              }
            }
            if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2")) {
              new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_grp_idx2 ON grouper_sync_dep_group_group (grouper_sync_id,provisionable_group_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx2 exists already");
              }
            }
            if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx3")) {
              new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_grp_idx3 ON grouper_sync_dep_group_group (grouper_sync_id,group_id,field_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx3 exists already");
              }
            }
              
            
          } catch (Throwable t) {
            String message = "Could not perform upgrade task V11 on grouper_loader_log adding tables/indexes for GRP-5302 for provisioning attributes!  "
                + "Skipping this upgrade task, install the tables/foreign keys/indexes manually";
            LOG.error(message, t);
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", " + message);
            }
          }
          return null;
        }
      });
    }
  }, 
  
  V19 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          try {
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user")) {
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user ( config_id VARCHAR(50) NOT NULL, active VARCHAR(1), cost_center VARCHAR(256), department VARCHAR(256), display_name VARCHAR(256), division VARCHAR(256), email_type VARCHAR(256), email_value VARCHAR(256), email_type2 VARCHAR(256), email_value2 VARCHAR(256), employee_number VARCHAR(256), external_id VARCHAR(256), family_name VARCHAR(256), formatted_name VARCHAR(256), given_name VARCHAR(256), id VARCHAR(180) NOT NULL, middle_name VARCHAR(256), phone_number VARCHAR(256), phone_number_type VARCHAR(256), phone_number2 VARCHAR(256), phone_number_type2 VARCHAR(256), the_schemas VARCHAR(256), title VARCHAR(256), user_name VARCHAR(256), user_type VARCHAR(256), PRIMARY KEY (config_id, id) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user ( config_id VARCHAR2(50) NOT NULL, active VARCHAR2(1), cost_center VARCHAR2(256), department VARCHAR2(256), display_name VARCHAR2(256), division VARCHAR2(256), email_type VARCHAR2(256), email_value VARCHAR2(256), email_type2 VARCHAR2(256), email_value2 VARCHAR2(256), employee_number VARCHAR2(256), external_id VARCHAR2(256), family_name VARCHAR2(256), formatted_name VARCHAR2(256), given_name VARCHAR2(256), id VARCHAR2(180) NOT NULL, middle_name VARCHAR2(256), phone_number VARCHAR2(256), phone_number_type VARCHAR2(256), phone_number2 VARCHAR2(256), phone_number_type2 VARCHAR2(256), the_schemas VARCHAR2(256), title VARCHAR2(256), user_name VARCHAR2(256), user_type VARCHAR2(256), PRIMARY KEY (config_id, id) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user ( config_id VARCHAR(50) NOT NULL, active VARCHAR(1) NULL, cost_center VARCHAR(256) NULL, department VARCHAR(256) NULL, display_name VARCHAR(256) NULL, division VARCHAR(256) NULL, email_type VARCHAR(256) NULL, email_value VARCHAR(256) NULL, email_type2 VARCHAR(256) NULL, email_value2 VARCHAR(256) NULL, employee_number VARCHAR(256) NULL, external_id VARCHAR(256) NULL, family_name VARCHAR(256) NULL, formatted_name VARCHAR(256) NULL, given_name VARCHAR(256) NULL, id VARCHAR(180) NOT NULL, middle_name VARCHAR(256) NULL, phone_number VARCHAR(256) NULL, phone_number_type VARCHAR(256) NULL, phone_number2 VARCHAR(256) NULL, phone_number_type2 VARCHAR(256) NULL, the_schemas VARCHAR(256) NULL, title VARCHAR(256) NULL, user_name VARCHAR(256) NULL, user_type VARCHAR(256) NULL, PRIMARY KEY (config_id, id) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                
                new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_scim_user IS 'table to load scim users into a sql for reporting, provisioning, and deprovisioning'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.config_id IS 'scim config id identifies which scim external system is being loaded'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.id IS 'scim internal ID for this user (used in web services)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.active IS 'Is user active'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.cost_center IS 'cost center for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.department IS 'department for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.display_name IS 'display name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.division IS 'divsion for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_type IS 'email type for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_value IS 'email value for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_type2 IS 'email type2 for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_value2 IS 'email value2 for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.employee_number IS 'employee number for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.external_id IS 'external id for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.family_name IS 'family name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.formatted_name IS 'formatted name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.given_name IS 'given name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.middle_name IS 'middle name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number IS 'phone number for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number_type IS 'phone number type for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number2 IS 'phone number2 for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number_type2 IS 'phone number type2 for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.the_schemas IS 'schemas for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.title IS 'title for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.user_name IS 'user name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.user_type IS 'user type for the user'").executeSql();
          
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_scim_user");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_scim_user exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1")) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_user_idx1 ON grouper_prov_scim_user (email_value, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_user_idx1");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_user_idx1 exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2")) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_user_idx2 ON grouper_prov_scim_user (user_name, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_user_idx2");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_user_idx2 exists already");
              }
            }
            
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr")) {
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user_attr ( config_id VARCHAR(50) NOT NULL, id VARCHAR(256) NOT NULL, attribute_name VARCHAR(256) NULL, attribute_value VARCHAR(4000) NULL, PRIMARY KEY (config_id, id, attribute_name, attribute_value) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user_attr ( config_id VARCHAR2(50) NOT NULL, id VARCHAR2(256) NOT NULL, attribute_name VARCHAR2(256) NULL, attribute_value VARCHAR2(4000) NULL, PRIMARY KEY (config_id, id, attribute_name, attribute_value) )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user_attr ( config_id VARCHAR(50) NOT NULL, id VARCHAR(256) NOT NULL, attribute_name VARCHAR(256) NULL, attribute_value VARCHAR(4000) NULL )").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              
              new GcDbAccess().sql("ALTER TABLE grouper_prov_scim_user_attr ADD CONSTRAINT grouper_prov_scim_usat_fk FOREIGN KEY (config_id, id) REFERENCES grouper_prov_scim_user(config_id, id) on delete cascade").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                
                new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_scim_user_attr IS 'table to load scim user attributes into a sql for reporting, provisioning, and deprovisioning'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.config_id IS 'scim config id identifies which scim external system is being loaded'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.id IS 'scim internal ID for this user (used in web services)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_name IS 'scim user attribute name'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_value IS 'scim user attribute value'").executeSql();
          
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_scim_user_attr");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_scim_user_attr exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1")) {
              if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_name(100))").executeSql();
              } else {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id, config_id, attribute_name)").executeSql();
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_user_idx1");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_user_idx1 exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2")) {
              if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_value(100))").executeSql();
              } else {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id, config_id, attribute_value)").executeSql();
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_usat_idx2");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_usat_idx2 exists already");
              }
            }              
            
          } catch (Throwable t) {
            String message = "Could not perform upgrade task V12 adding tables/foreign keys/indexes for GRP-5514 scim loading!  "
                + "Skipping this upgrade task, install the tables/foreign keys/indexes manually";
            LOG.error(message, t);
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", " + message);
            }
          }
          return null;
        }
      });
    }
  }, 
  V20{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          try {
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user")) {
              
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_azure_user
              (
                  config_id VARCHAR(50) NOT NULL,
                  account_enabled VARCHAR(1) NULL,
                  display_name VARCHAR(256) NULL,
                  id VARCHAR(180) NOT NULL,
                  mail_nickname VARCHAR(256) NULL,
                  on_premises_immutable_id VARCHAR(256) NULL,
                  user_principal_name VARCHAR(256) NULL,
                  PRIMARY KEY (config_id, id)
              )                    
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_azure_user
              (
                  config_id VARCHAR2(50) NOT NULL,
                  account_enabled VARCHAR2(1) NULL,
                  display_name VARCHAR2(256) NULL,
                  id VARCHAR2(180) NOT NULL,
                  mail_nickname VARCHAR2(256) NULL,
                  on_premises_immutable_id VARCHAR2(256) NULL,
                  user_principal_name VARCHAR2(256) NULL,
                  PRIMARY KEY (config_id, id)
              )
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("""
                  CREATE TABLE grouper_prov_azure_user
                  (
                      config_id VARCHAR(50) NOT NULL,
                      account_enabled VARCHAR(1) NULL,
                      display_name VARCHAR(256) NULL,
                      id VARCHAR(180) NOT NULL,
                      mail_nickname VARCHAR(256) NULL,
                      on_premises_immutable_id VARCHAR(256) NULL,
                      user_principal_name VARCHAR(256) NULL,
                      PRIMARY KEY (config_id, id)
                  )
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                
                new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_azure_user IS 'table to load azure users into a sql for reporting, provisioning, and deprovisioning'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.config_id IS 'azure config id identifies which azure external system is being loaded'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.id IS 'azure internal ID for this user (used in web services)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.account_enabled IS 'Is account enabled'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.mail_nickname IS 'mail nickname for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.on_premises_immutable_id IS 'in premises immutable id for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.display_name IS 'display name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.user_principal_name IS 'user principal name for the user'").executeSql();
          
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_azure_user");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_azure_user exists already");
              }
            }
                        
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_azure_user", "grouper_prov_azure_user_idx1")) {
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_azure_user_idx1 ON grouper_prov_azure_user (user_principal_name, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql() ) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_azure_user_idx1 ON grouper_prov_azure_user (user_principal_name(180), config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_azure_user_idx1");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_azure_user_idx1 exists already");
              }
            }
            
             
            
            
          } catch (Throwable t) {
            String message = "Could not perform upgrade task V20 adding tables/foreign keys/indexes for GRP-5625 load azure from provisioner to table!  "
                + "Skipping this upgrade task, install the tables/foreign keys/indexes manually";
            LOG.error(message, t);
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", " + message);
            }
          }
          return null;
        }
      });
    }
  }
  ,
  
 V24{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          try {
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_user")) {
              
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_adobe_user
              (    
                  config_id VARCHAR(50) NOT NULL,
                  user_id VARCHAR(100) NOT NULL,
                  email VARCHAR(256) NOT NULL,
                  username VARCHAR(256) NOT NULL,
                  status VARCHAR(30) NULL,
                  "type" VARCHAR(30) NULL,
                  firstname VARCHAR(100) NULL,
                  lastname VARCHAR(100) NULL,
                  domain VARCHAR(100) NULL,
                  country VARCHAR(2) NULL,
                  PRIMARY KEY (config_id, user_id)
              );                    
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_adobe_user
              (
                  config_id VARCHAR2(50) NOT NULL,
                  user_id VARCHAR2(100) NOT NULL,
                  email VARCHAR2(256) NOT NULL,
                  username VARCHAR2(256) NOT NULL,
                  status VARCHAR2(30) NULL,
                  "type" VARCHAR2(30) NULL,
                  firstname VARCHAR2(100) NULL,
                  lastname VARCHAR2(100) NULL,
                  domain VARCHAR2(100) NULL,
                  country VARCHAR2(2) NULL,
                  PRIMARY KEY (config_id, user_id)
              );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("""
                  CREATE TABLE grouper_prov_adobe_user
                (
                    config_id VARCHAR(100) NOT NULL,
                    user_id VARCHAR(100) NOT NULL,
                    email VARCHAR(256) NOT NULL,
                    username VARCHAR(256) NOT NULL,
                    status VARCHAR(30) NULL,
                    "type" VARCHAR(30) NULL,
                    firstname VARCHAR(100) NULL,
                    lastname VARCHAR(100) NULL,
                    domain VARCHAR(100) NULL,
                    country VARCHAR(2) NULL,
                    PRIMARY KEY (config_id, user_id)
                );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                
                new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_adobe_user IS 'table to load adobe users into a sql for reporting, provisioning, and deprovisioning'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.config_id IS 'adobe config id identifies which adobe external system is being loaded'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.user_id IS 'adobe user id for this user (used in web services)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.email IS 'email address'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.username IS 'username the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.status IS 'adobe status for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.\"type\" IS 'type for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.firstname IS 'first name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.lastname IS 'last name for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.domain IS 'domain for the user'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.country IS 'country for the user'").executeSql();
          
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_adobe_user");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_adobe_user exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_group")) {
              
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_adobe_group
              (
                  config_id VARCHAR(100) NOT NULL,
                  group_id BIGINT NOT NULL,
                  name VARCHAR(2000) NOT NULL,
                  "type" VARCHAR(100) NULL,
                  product_name VARCHAR(2000) NULL,
                  member_count BIGINT NULL,
                  license_quota BIGINT NULL,
                  PRIMARY KEY (config_id, group_id)
              );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_adobe_group
            (
                config_id VARCHAR2(100) NOT NULL,
                group_id NUMBER(38) NOT NULL,
                name VARCHAR2(2000) NOT NULL,
                "type" VARCHAR2(100) NULL,
                product_name VARCHAR2(2000) NULL,
                member_count NUMBER(38) NULL,
                license_quota NUMBER(38) NULL,
                PRIMARY KEY (config_id, group_id)
            );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("""
                  CREATE TABLE grouper_prov_adobe_group
                (
                    config_id VARCHAR(100) NOT NULL,
                    group_id BIGINT NOT NULL,
                    name VARCHAR(2000) NOT NULL,
                    "type" VARCHAR(100) NULL,
                    product_name VARCHAR(2000) NULL,
                    member_count BIGINT NULL,
                    license_quota BIGINT NULL,
                    PRIMARY KEY (config_id, group_id)
                );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_adobe_group IS 'table to load adobe groups into a sql for reporting, provisioning, and deprovisioning'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.config_id IS 'adobe config id identifies which adobe external system is being loaded'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.group_id IS 'adobe group id for this group (used in web services)'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.\"name\" IS 'group name'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.\"type\" IS 'type for the group'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.product_name IS 'product name for the group'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.member_count IS 'member count for the group'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.license_quota IS 'license quota for the group'").executeSql();
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_adobe_group");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_adobe_group exists already");
              }
            }
                        
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_user", "grouper_prov_adobe_user_idx1")) {
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx1 ON grouper_prov_adobe_user (email, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql() ) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx1 ON grouper_prov_adobe_user (email, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_adobe_user_idx1");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_azure_user_idx1 exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_user", "grouper_prov_adobe_user_idx2")) {
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx2 ON grouper_prov_adobe_user (username, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql() ) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx2 ON grouper_prov_adobe_user (username, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_adobe_user_idx2");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_azure_user_idx2 exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_group", "grouper_prov_adobe_group_idx1")) {
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_group_idx1 ON grouper_prov_adobe_group (name, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql() ) {
                new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_group_idx1 ON grouper_prov_adobe_group (name, config_id)").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_adobe_group_idx1");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_azure_group_idx1 exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_membership")) {
              
              if (GrouperDdlUtils.isPostgres()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_adobe_membership
              (
                  config_id VARCHAR(100) NOT NULL,
                  group_id BIGINT NOT NULL,
                  user_id VARCHAR(100) NOT NULL,
                  PRIMARY KEY (config_id, group_id, user_id)
              );

                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              } else if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("""
              CREATE TABLE grouper_prov_adobe_membership
              (
                  config_id VARCHAR2(100) NOT NULL,
                  group_id NUMBER(38) NOT NULL,
                  user_id VARCHAR2(100) NOT NULL,
                  PRIMARY KEY (config_id, group_id, user_id)
              );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql()) {
                new GcDbAccess().sql("""
                  CREATE TABLE grouper_prov_adobe_membership
                  (
                      config_id VARCHAR(100) NOT NULL,
                      group_id BIGINT NOT NULL,
                      user_id VARCHAR(100) NOT NULL,
                      PRIMARY KEY (config_id, group_id, user_id)
                  );
                    """).executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_adobe_membership IS 'table to load adobe memberships into a sql for reporting, provisioning, and deprovisioning'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_membership.config_id IS 'adobe config id identifies which adobe external system is being loaded'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_membership.group_id IS 'adobe group id for this membership'").executeSql();
                new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_membership.user_id IS 'adobe user id for this membership'").executeSql();
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
              
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_adobe_membership");
              }
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_adobe_membership exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_adobe_membership", "grouper_prov_adobe_mship_fk1")) { 
              
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk1 FOREIGN KEY (config_id, group_id) REFERENCES grouper_prov_adobe_group(config_id, group_id) on delete cascade").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql() ) {
                new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk1 FOREIGN KEY (config_id, group_id) REFERENCES grouper_prov_adobe_group(config_id, group_id) on delete cascade").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_adobe_mship_fk1");
              }
              
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", foreign key grouper_prov_adobe_mship_fk1 exists already");
              }
            }
            
            if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_adobe_membership", "grouper_prov_adobe_mship_fk2")) { 
              
              if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk2 FOREIGN KEY (config_id, user_id) REFERENCES grouper_prov_adobe_user(config_id, user_id) on delete cascade").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }

              } else if (GrouperDdlUtils.isMysql() ) {
                new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk2 FOREIGN KEY (config_id, user_id) REFERENCES grouper_prov_adobe_user(config_id, user_id) on delete cascade").executeSql();
                if (otherJobInput != null) {
                  otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
                }
              }
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign grouper_prov_adobe_mship_fk2");
              }
              
            } else {
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", foreign key grouper_prov_adobe_mship_fk2 exists already");
              }
            }

          } catch (Throwable t) {
            String message = "Could not perform upgrade task V20 adding tables/foreign keys/indexes for GRP-5625 load azure from provisioner to table!  "
                + "Skipping this upgrade task, install the tables/foreign keys/indexes manually";
            LOG.error(message, t);
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", " + message);
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
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      // do a blank ten so v4 upgrades (which added ten) will get the new stuff
    }
  }      
  ,
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V11 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      boolean groupsNullable = GrouperDdlUtils.isColumnNullable("grouper_groups", "internal_id", "name", GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksMetadataGroup");
      boolean fieldsNullable = GrouperDdlUtils.isColumnNullable("grouper_fields", "internal_id", "name", "admins");
      
      if (groupsNullable || fieldsNullable) {
        // ok nulls are allowed so make the change
        GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
      }
      
      if (groupsNullable) {
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
        
        new GcDbAccess().sql(sql).executeSql();
      }
      
      if (fieldsNullable) {
        String sql = null;

        if (GrouperDdlUtils.isOracle()) {
          sql = "ALTER TABLE grouper_fields MODIFY (internal_id NOT NULL)";
        } else if (GrouperDdlUtils.isMysql()) {
          sql = "ALTER TABLE grouper_fields MODIFY internal_id BIGINT NOT NULL";
        } else if (GrouperDdlUtils.isPostgres()) {
          sql = "ALTER TABLE grouper_fields ALTER COLUMN internal_id SET NOT NULL";
        } else {
          throw new RuntimeException("Which database are we????");
        }
        
        new GcDbAccess().sql(sql).executeSql();
      }

      // cant add foreign key until this is there
      if (GrouperDdlUtils.isOracle()) {
        
        String sql = "ALTER TABLE grouper_fields ADD CONSTRAINT grouper_fie_internal_id_unq unique (internal_id)";
        
        if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_fie_internal_id_unq")) {
          try {
            new GcDbAccess().sql(sql).executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
              // throw if the exception is anything other than the constraint already exists
              throw e;
            }
          }
        }
        
        sql = "ALTER TABLE grouper_groups ADD CONSTRAINT grouper_grp_internal_id_unq unique (internal_id)";
        
        if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_grp_internal_id_unq")) {
          try {
            new GcDbAccess().sql(sql).executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
              // throw if the exception is anything other than the constraint already exists
              throw e;
            }
          }
        }

        sql = "ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id)";
        
        if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sql_cache_group1_fk")) {
          try {
            new GcDbAccess().sql(sql).executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              // throw if the exception is anything other than the constraint already exists
              throw e;
            }
          }
        }
      }

    }
  }
  , 
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V12 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
      if (!GrouperDdlUtils.isColumnNullable("grouper_members", "internal_id", "subject_id", "GrouperSystem")) {
        return;
      }
      
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
      
      new GcDbAccess().sql(sql).executeSql();
    }
  }
  ,
  /**
   * make sure source_internal_id is populated in pit tables (fields/members/groups)
   */
  V13 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      new GcDbAccess().sql("update grouper_pit_groups  pg set source_internal_id = (select g.internal_id from grouper_groups  g where pg.source_id = g.id) where pg.source_internal_id is null and pg.active='T'").executeSql();
      new GcDbAccess().sql("update grouper_pit_fields  pf set source_internal_id = (select f.internal_id from grouper_fields  f where pf.source_id = f.id) where pf.source_internal_id is null and pf.active='T'").executeSql();
      new GcDbAccess().sql("update grouper_pit_members pm set source_internal_id = (select m.internal_id from grouper_members m where pm.source_id = m.id) where pm.source_internal_id is null and pm.active='T'").executeSql();
    }
  }
  ,
  
  /**
   * remove old maintenance jobs
   */
  V15 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      try {
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
        List<TriggerKey> triggerKeys = new ArrayList<TriggerKey>();
        triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_cleanLogs"));
        triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_enabledDisabled"));
        triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_Messaging"));
        triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_externalSubjCalcFields"));
        triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_rules"));
        
        for (TriggerKey triggerKey : triggerKeys) {
          if (scheduler.checkExists(triggerKey)) {
            scheduler.unscheduleJob(triggerKey);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", removed quartz trigger " + triggerKey.getName());
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  ,
  /**
   * remove old maintenance jobs
   */
  V17 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      try {
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
        List<TriggerKey> triggerKeys = new ArrayList<TriggerKey>();
        triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_grouperReport"));
        
        for (TriggerKey triggerKey : triggerKeys) {
          if (scheduler.checkExists(triggerKey)) {
            scheduler.unscheduleJob(triggerKey);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", removed quartz trigger " + triggerKey.getName());
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  ,
  /**
   * remove old maintenance jobs
   */
  V18 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      try {
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
          String jobName = jobKey.getName();
          if (jobName.startsWith("MAINTENANCE__groupSync__")) {
            String triggerName = "trigger_" + jobName;
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", removed quartz trigger " + triggerName);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
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
