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
import org.quartz.Scheduler;
import org.quartz.TriggerKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
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
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
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
  }
  ,
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
