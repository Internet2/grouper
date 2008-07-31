/*
 * @author mchyzer
 * $Id: GrouperDdl.java,v 1.13 2008-07-31 05:18:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * ddl versions and stuff for grouper.  All ddl classes must have a currentVersion method that
 * returns the current version
 */
public enum GrouperDdl implements DdlVersionable {

  /**
   * delete backup cols if configured to and is exist
   */
  V7 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //if not configured to drop, then leave alone
      if (!GrouperConfig.getPropertyBoolean("ddlutils.dropBackupUuidCols", false)) {
        return;
      }

      //only drop cols if there are there, and all of them (which means the conversion probably happened, and they
      //havent been dropped yet)
      if (GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_TYPE_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_ID, false) != null) {
        
        GrouperDdlUtils.ddlutilsDropColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_TYPE_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_ID, ddlVersionBean);
      }
      
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_MEMBERSHIP_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_ID, false) != null) {
        
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_MEMBERSHIP_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_ID, ddlVersionBean);
      }
      
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_MEMBER_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_MEMBER_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_ID, ddlVersionBean);
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_ID, ddlVersionBean);
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_FIELD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_FIELD_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_ID, ddlVersionBean);
        
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_ID, ddlVersionBean);
        
      }

      if(GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_SESSION_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_SESSION_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_ID, ddlVersionBean);
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_ID, ddlVersionBean);
          
      }

      
    }
  },
  
  /**
   * delete uuid and backup cols if configured to and is exist
   */
  V6 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //only drop cols if there are there, and all of them (which means the conversion probably happened, and they
      //havent been dropped yet)
      if (GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_TYPE_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_TYPE_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_ID, false) != null) {
        
        GrouperDdlUtils.ddlutilsDropColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_TYPE_UUID, ddlVersionBean);
      }
      
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_MEMBERSHIP_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_MEMBERSHIP_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_ID, false) != null) {
        
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_MEMBERSHIP_UUID, ddlVersionBean);
      }
      
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_MEMBER_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_MEMBER_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_MEMBER_UUID, ddlVersionBean);
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_UUID, ddlVersionBean);
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_FIELD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_FIELD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_FIELD_UUID, ddlVersionBean);
        
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_UUID, ddlVersionBean);
        
      }

      if(GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_SESSION_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_SESSION_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_SESSION_UUID, ddlVersionBean);
      }

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_UUID, ddlVersionBean);
          
      }

      
    }
  },
  
  /**
   * convert uuid/id to just id
   */
  V5 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {
      
      boolean isDestinationVersion = ddlVersionBean.isDestinationVersion();
      
      StringBuilder additionalScripts = ddlVersionBean.getAdditionalScripts();
      
      //if there is no uuid col, then forget it, or if there is a old_uuid col forget it
      Table compositesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Composite.TABLE_GROUPER_COMPOSITES);
      
      Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS);
      
      Table fieldsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Field.TABLE_GROUPER_FIELDS);
      
      Table groupsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Group.TABLE_GROUPER_GROUPS);
      
      Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Member.TABLE_GROUPER_MEMBERS);
      
      Table grouperSessionsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          GrouperSession.TABLE_GROUPER_SESSIONS);
      
      Table stemsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Stem.TABLE_GROUPER_STEMS);

      Table typesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          GroupType.TABLE_GROUPER_TYPES);
      
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsCompositeConversion = needsCompositeIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsMembershipConversion = needsMembershipIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsFieldsConversion = needsFieldsIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsGroupsConversion = needsGroupsIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsMembersConversion = needsMembersIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsGrouperSessionsConversion = needsGrouperSessionsIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsStemsConversion = needsStemIdConversion(database);
      
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      boolean needsTypesConversion = needsTypesIdConversion(database);
      
      boolean needsConversion = needsCompositeConversion || needsMembershipConversion || needsFieldsConversion 
        || needsGroupsConversion || needsMembersConversion || needsGrouperSessionsConversion 
        || needsStemsConversion || needsTypesConversion;
      
      boolean isPostgres = ddlVersionBean.getPlatform().getName().toLowerCase().contains("postgres");
      
      //if we need any conversion, then drop all foreign keys
      if (needsConversion) {
        
        if (needsCompositeConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + compositesTable.getName() + " ADD COLUMN " + Composite.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + compositesTable.getName() + " ADD COLUMN " + Composite.COLUMN_OLD_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositesTable, Composite.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositesTable, Composite.COLUMN_OLD_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {

            //update records, move the uuid to the id
            additionalScripts.append("update grouper_composites set old_id = id, id = uuid, old_uuid = uuid where uuid is not null;\ncommit;\n");
          }          
        }
        
        if (needsMembershipConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + membershipsTable.getName() + " ADD COLUMN " + Membership.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + membershipsTable.getName() + " ADD COLUMN " + Membership.COLUMN_OLD_MEMBERSHIP_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OLD_MEMBERSHIP_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {

            //update records, move the uuid to the id
            additionalScripts.append("update grouper_memberships set old_id = id, id = membership_uuid, old_membership_uuid = membership_uuid where membership_uuid is not null;\ncommit;\n");
          }          
        }
        
        if (needsFieldsConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + fieldsTable.getName() + " ADD COLUMN " + Field.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + fieldsTable.getName() + " ADD COLUMN " + Field.COLUMN_OLD_FIELD_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, Field.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, Field.COLUMN_OLD_FIELD_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {
            //update records, move the uuid to the id
            additionalScripts.append("update grouper_fields set old_id = id, id = field_uuid, old_field_uuid = field_uuid where field_uuid is not null;\ncommit;\n");
          }          
        }
        
        if (needsGroupsConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + groupsTable.getName() + " ADD COLUMN " + Group.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + groupsTable.getName() + " ADD COLUMN " + Group.COLUMN_OLD_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_OLD_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {
            //update records, move the uuid to the id
            additionalScripts.append("update grouper_groups set old_id = id, id = uuid, old_uuid = uuid where uuid is not null;\ncommit;\n");
          }          
        }

        if (needsMembersConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + membersTable.getName() + " ADD COLUMN " + Member.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + membersTable.getName() + " ADD COLUMN " + Member.COLUMN_OLD_MEMBER_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, Member.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, Member.COLUMN_OLD_MEMBER_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {
            //update records, move the uuid to the id
            additionalScripts.append("update grouper_members set old_id = id, id = member_uuid, old_member_uuid = member_uuid where member_uuid is not null;\ncommit;\n");
          }          
        }

        if (needsGrouperSessionsConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + grouperSessionsTable.getName() + " ADD COLUMN " + GrouperSession.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + grouperSessionsTable.getName() + " ADD COLUMN " + GrouperSession.COLUMN_OLD_SESSION_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSessionsTable, GrouperSession.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSessionsTable, GrouperSession.COLUMN_OLD_SESSION_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {

            //update records, move the uuid to the id
            additionalScripts.append("update grouper_sessions set old_id = id, id = session_uuid, old_session_uuid = session_uuid where session_uuid is not null;\ncommit;\n");
          }          
        }

        if (needsStemsConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + stemsTable.getName() + " ADD COLUMN " + Stem.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + stemsTable.getName() + " ADD COLUMN " + Stem.COLUMN_OLD_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, Stem.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, Stem.COLUMN_OLD_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {
            //update records, move the uuid to the id
            additionalScripts.append("update grouper_stems set old_id = id, id = uuid, old_uuid = uuid where uuid is not null;\ncommit;\n");
          }          
        }

        if (needsTypesConversion) {
          
          if (isDestinationVersion && isPostgres) {
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + typesTable.getName() + " ADD COLUMN " + GroupType.COLUMN_OLD_ID + " varchar(128);\n");
            ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + typesTable.getName() + " ADD COLUMN " + GroupType.COLUMN_OLD_TYPE_UUID + " varchar(128);\n");
          } else {
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, GroupType.COLUMN_OLD_ID, "temp col for old id vals", Types.VARCHAR, "128", false, false);
            GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, GroupType.COLUMN_OLD_TYPE_UUID, "temp col for old uuid vals", Types.VARCHAR, "128", false, false);
          }
          
          if (isDestinationVersion) {
            //update records, move the uuid to the id
            additionalScripts.append("update grouper_types set old_id = id, id = type_uuid, old_type_uuid = type_uuid where type_uuid is not null;\ncommit;\n");
          }          
        }
        additionalScripts.append("commit;\n");
      }
      
      
    }

  },

  /** add in the hibernate_version_number cols */
  V4 {
    
    /**
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {
      
      versionNumberColumnAdd(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES);
      versionNumberColumnAdd(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES);
      versionNumberColumnAdd(ddlVersionBean, GroupTypeTuple.TABLE_GROUPER_GROUPS_TYPES);
      versionNumberColumnAdd(ddlVersionBean, Field.TABLE_GROUPER_FIELDS);
      versionNumberColumnAdd(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS);
      versionNumberColumnAdd(ddlVersionBean, Group.TABLE_GROUPER_GROUPS);
      versionNumberColumnAdd(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS);
      versionNumberColumnAdd(ddlVersionBean, GrouperSession.TABLE_GROUPER_SESSIONS);
      versionNumberColumnAdd(ddlVersionBean, Stem.TABLE_GROUPER_STEMS);
      versionNumberColumnAdd(ddlVersionBean, GroupType.TABLE_GROUPER_TYPES);
      
    }
  },
  
  /**
   * add grouper loader
   */
  V3 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //see if the grouper_ext_loader_log table is there
      Table grouploaderLogTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouper_loader_log", 
          "log table with a row for each grouper loader job run");
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "id", "uuid of this log record", 
          Types.VARCHAR, "128", true, true);
      
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_name", 
          "Could be group name (friendly) or just config name", Types.VARCHAR, "512", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "status", 
          "STARTED, SUCCESS, ERROR, WARNING, CONFIG_ERROR", Types.VARCHAR, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "started_time", 
          "When the job was started", Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "ended_time", 
          "When the job ended (might be blank if daemon died)", Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis", 
          "Milliseconds this process took", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis_get_data", 
          "Milliseconds this process took to get the data from the source", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis_load_data", 
          "Milliseconds this process took to load the data to grouper", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_type", 
          "GrouperLoaderJobType enum value", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_type", 
          "GrouperLoaderJobscheduleType enum value", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_description", 
          "More information about the job", Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_message", 
          "Could be a status or error message or stack", Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "host", 
          "Host that this job ran on", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "group_uuid", 
          "If this job involves one group, this is uuid", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_quartz_cron", 
          "Quartz cron string for this col", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_interval_seconds", 
          "How many seconds this is supposed to wait between runs", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "last_updated", 
          "When this record was last updated", Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "unresolvable_subject_count", 
          "The number of records which were not subject resolvable", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "insert_count", 
          "The number of records inserted", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "update_count", 
          "The number of records updated", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "delete_count", 
          "The number of records deleted", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "total_count", 
          "The total number of records (e.g. total number of members)", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "parent_job_name", 
          "If this job is a subjob of another job, then put the parent job name here", Types.VARCHAR, "512", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "parent_job_id", 
          "If this job is a subjob of another job, then put the parent job id here", Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "and_group_names", 
          "If this group query is anded with another group or groups, they are listed here comma separated", Types.VARCHAR, "512", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_priority", 
          "Priority of this job (5 is unprioritized, higher the better)", Types.INTEGER, null, false, false);

      //see if the grouper_ext_loader_log table is there
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_loader_log",
          "grouper_loader_job_name_idx", false, "job_name");

    }
    
    
  },
    
  /** all tables and indexes from grouper v1.3 */
  V2 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {
      
      int buildingToVersion = ddlVersionBean.getBuildingToVersion();
      
      boolean buildingToThisVersion = V4.getVersion() >= buildingToVersion;
      
      {
        Table attributeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Attribute.TABLE_GROUPER_ATTRIBUTES, "attributes for groups, including name, extension, etc");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "id", 
            "db id of this attribute record", Types.VARCHAR, "128", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "group_id", 
            "group_uuid foreign key", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "field_name", 
            "name of attribute", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "value", 
            "value this attribute record", Types.VARCHAR, "1024", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeTable.getName(), "attribute_uniq_idx", true, "group_id", "field_name");
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeTable.getName(), "attribute_value_idx", false, "value");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeTable.getName(), "attribute_field_value_idx", false, "field_name", "value");
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeTable.getName(), "attribute_group_idx", false, "group_id");
        
        versionNumberColumnFindOrCreate(attributeTable);
        
      }
      
      {
        Table compositeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Composite.TABLE_GROUPER_COMPOSITES, "records the composite group, and its factors");
  
        boolean needsConversion = needsCompositeIdConversion(database);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "id", 
            "db id of this composite record", Types.VARCHAR, "128", true, true);

        if (needsConversion || buildingToThisVersion) {

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "uuid", 
              "grouper uuid of this table", Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
              "composite_uuid_idx", true, "uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "owner", 
            "group uuid of the composite group", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "left_factor", 
            "left factor of the composite group", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "right_factor", 
            "right factor of the composite group", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "type", 
            "e.g. union, complement, intersection", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "creator_id", 
            "member uuid of who created this", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "create_time", 
            "number of millis since 1970 until when created", Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
            "composite_composite_idx", false, "owner");
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
            "composite_createtime_idx", false, "create_time");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
            "composite_creator_idx", false, "creator_id");
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
            "composite_factor_idx", false, "left_factor", "right_factor");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
            "composite_left_factor_idx", false, "left_factor");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
            "composite_right_factor_idx", false, "right_factor");

        versionNumberColumnFindOrCreate(compositeTable);

      }

      {

        //do this before table so we know if the table is already there
        GrouperDdlUtils.ddlutilsAddUniqueConstraint(Field.TABLE_GROUPER_FIELDS, "fields_name_unq", ddlVersionBean, "name");
        GrouperDdlUtils.ddlutilsAddUniqueConstraint(Field.TABLE_GROUPER_FIELDS, "fields_name_type_unq", ddlVersionBean, "name", "type");

        Table fieldsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Field.TABLE_GROUPER_FIELDS, "describes fields related to types");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "id", 
            "db id of this field record", Types.VARCHAR, "128", true, true);

        boolean needsConversion = needsFieldsIdConversion(database);
    
        if (needsConversion || buildingToThisVersion) {

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "field_uuid", 
              "grouper uuid of this table", Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, fieldsTable.getName(), 
              "field_uuid_idx", true, "field_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "grouptype_uuid", 
            "foreign key to group type", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "is_nullable", 
            "if this is nullable", Types.BIT, "1", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "name", 
            "name of the field", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "read_privilege", 
            "which privilege is required to read this field", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "type", 
            "type of field (e.g. attribute, list, access, naming)", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "write_privilege", 
            "which privilege is required to write this attribute", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, fieldsTable.getName(), 
            "name_and_type", true, "name", "type");
        
        versionNumberColumnFindOrCreate(fieldsTable);

      }
    
      {
        Table groupsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Group.TABLE_GROUPER_GROUPS, "holds the groups in the grouper system");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "id", 
            "db id of this group record", Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsGroupsIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "uuid", 
              "grouper uuid of this table", Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
              "group_uuid_idx", true, "uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "parent_stem", 
            "uuid of the stem that this group refers to", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "creator_id", 
            "member uuid of the creator of this group", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "create_time", 
            "number of millis since 1970 that this group was created", Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "modifier_id", 
            "member uuid of the last modifier of this group", Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "modify_time", 
            "number of millis since 1970 that this group was modified", Types.BIGINT, "20", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "create_source", 
            "subject source of who created this group", Types.VARCHAR, "255", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "modify_source", 
            "subject source of who modified this group", Types.VARCHAR, "255", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_parent_idx", false, "parent_stem");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_creator_idx", false, "creator_id");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_createtime_idx", false, "create_time");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_modifier_idx", false, "modifier_id");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_modifytime_idx", false, "modify_time");
        
        versionNumberColumnFindOrCreate(groupsTable);
        
      }
    
      {
        Table groupsTypesTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "grouper_groups_types", "holds the association between group and type");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, "id", 
            "id of this group/type record", Types.VARCHAR, "128", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, "group_uuid", 
            "group uuid foreign key", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, "type_uuid", 
            "type uuid foreign key", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTypesTable.getName(), 
            "grouptypetyple_grouptype_idx", true, "group_uuid", "type_uuid");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTypesTable.getName(), 
            "grouptypetuple_type_idx", false, "type_uuid");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTypesTable.getName(), 
            "grouptypetuple_group_idx", false, "group_uuid");
        
        versionNumberColumnFindOrCreate(groupsTypesTable);
      }
    
      {
        Table membersTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Member.TABLE_GROUPER_MEMBERS, "keeps track of subjects used in grouper.  Records are never deleted from this table");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "id", 
            "db id of this row", Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsMembersIdConversion(database);
        
        if (needsConversion || buildingToThisVersion ) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "member_uuid", 
              "member uuid used in foreign keys from other tables", Types.VARCHAR, "128", false, false);
    
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
              "member_uuid_idx", true, "member_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "subject_id", 
            "subject id is the id from the subject source", Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "subject_source", 
            "id of the source from sources.xml", Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "subject_type", 
            "type of subject, e.g. person", Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
            "member_subjectsourcetype_idx", true, "subject_id", "subject_source", 
            "subject_type");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
            "member_subjectsource_idx", false, "subject_source");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
            "member_subjectid_idx", false, "subject_id");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
            "member_subjecttype_idx", false, "subject_type");
        
        versionNumberColumnFindOrCreate(membersTable);
      }
    
      {
        Table membershipsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Membership.TABLE_GROUPER_MEMBERSHIPS, "keeps track of memberships and permissions");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "id", 
            "db id of this row", Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsMembersIdConversion(database);
        
        if (needsConversion || buildingToThisVersion ) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "membership_uuid", 
              "grouper id for membership (used is foreign keys to this row)", 
              Types.VARCHAR, "128", false, false);
    
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
              "membership_uuid_idx", true, "membership_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "owner_id", 
            "group of the membership", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "member_id", 
            "member of the memership", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "list_name", 
            "list, e.g. stemmers, admin, members, etc", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "list_type", 
            "type of list, e.g. naming, access, list", Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "mship_type", 
            "type of membership, one of the three: immediate, effective, composite", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "via_id", 
            "if effective, this is the group above the member in the chain, " +
            "for composite, this is the composite uuid", Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "depth", 
            "for effective membership, the number of hops (excluding composites) " +
            "along the membership graph", Types.INTEGER, "11", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "parent_membership", 
            "for effective membership, uuid of membership record from the group in " +
            "question to the parent group of the member", Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "creator_id", 
            "member uuid of the creator of this record", 
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "create_time", 
            "number of millis since 1970 that this record was created", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_createtime_idx", false, "create_time");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_creator_idx", false, "creator_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_depth_idx", false, "depth");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_member_idx", false, "member_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_member_list_idx", false, "member_id", "list_name", "list_type");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_member_via_idx", false, "member_id", "via_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_owner_idx", false, "owner_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_owner_list_type_idx", false, "owner_id", "list_name", "list_type", "mship_type");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_parent_idx", false, "parent_membership");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_type_idx", false, "mship_type");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_via_idx", false, "via_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_owner_member_idx", false, "owner_id", "member_id",
            "list_name", "list_type", "depth");

        versionNumberColumnFindOrCreate(membershipsTable);
       
      }
      {
        Table sessionsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            GrouperSession.TABLE_GROUPER_SESSIONS, "keeps track of grouper session when opened (deleted when closed)");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "id", 
            "db id of this row", Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsGrouperSessionsIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "session_uuid", 
              "hibernate uuid of the column", Types.VARCHAR, "128", false, false);
    
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, sessionsTable.getName(), 
              "session_uuid_idx", true, "session_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "member_id", 
            "member uuid foreign key of who started the session", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "starttime", 
            "number if millis since 1970 that the session was started",
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, sessionsTable.getName(), 
            "session_member_idx", false, "member_id");

        versionNumberColumnFindOrCreate(sessionsTable);
      }
      {
        Table stemsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Stem.TABLE_GROUPER_STEMS, "entries for stems and their attributes");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "id", 
            "db id of this row", Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsStemIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "uuid", 
              "grouper id of this row (used for foreign keys from other tables)", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
              "stem_uuid_idx", true, "uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "parent_stem", 
            "stem uuid of parent stem or empty if under root",
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "name", 
            "full name (id) path of stem", Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "display_name", 
            "full dislpay name path of stem", Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "creator_id", 
            "member_id of who created this stem", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "create_time", 
            "number of millis since 1970 since this was created", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "modifier_id", 
            "member_id of modifier who last edited", 
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "modify_time", 
            "number of millis since 1970 since this was edited", 
            Types.BIGINT, "20", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "display_extension", 
            "display extension (not full path) of stem", Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "extension", 
            "extension (id) (not full path) of this stem", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "description", 
            "description of stem", Types.VARCHAR, "1024", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "create_source", 
            "subject source of who created this stem", Types.VARCHAR, "255", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "modify_source", 
            "subject source of who modified this stem", Types.VARCHAR, "255", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_createtime_idx", false, "create_time");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_creator_idx", false, "creator_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_dislpayextn_idx", false, "display_extension");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_displayname_idx", false, "display_name");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_extn_idx", false, "extension");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_modifier_idx", false, "modifier_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_modifytime_idx", false, "modify_time");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_name_idx", false, "name");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_parent_idx", false, "parent_stem");

        versionNumberColumnFindOrCreate(stemsTable);
      }
      {
        Table typesTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "grouper_types", "the various types which can be assigned to groups");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "id", 
            "db id of this row", Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsTypesIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "type_uuid", 
              "grouper id of this row (used for foreign keys from other tables)", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, typesTable.getName(), 
              "type_uuid_idx", true, "type_uuid");

        }
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "name", 
            "name of this type",
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "creator_uuid", 
            "member_id of the creator", Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "create_time", 
            "number of millis since 1970 since this was created", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "is_assignable", 
            "if this type is assignable (not internal)", Types.BIT, "1", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "is_internal", 
            "if this type if internal (not assignable)", 
            Types.BIT, "1", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, typesTable.getName(), 
            "type_name_idx", true, "name");

        versionNumberColumnFindOrCreate(typesTable);
      }
    }
  }, 
  
  /** first version of grouper, make sure the ddl table is there */
  V1 {
    /**
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //see if the grouper_ext_loader_log table is there
      Table grouperDdlTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouper_ddl", 
          "holds a record for each database object name, and db version, and java version");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "id", "uuid of this ddl record", 
          Types.VARCHAR, "128", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "object_name", 
          "Corresponds to an enum in grouper.ddl package (with Ddl on end), represents one module, " +
          "so grouper itself is one object", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "db_version", 
          "Version of this object as far as DB knows about", Types.INTEGER, null, false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "last_updated", 
          "last update timestamp, string so it can easily be used from update statement", 
          Types.VARCHAR, "50", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "history", 
          "history of this object name, with most recent first (truncated after 4k)", 
          Types.VARCHAR, "4000", false, false);

      //object name is unique
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_ddl", "grouper_ddl_object_name_idx", 
          true, "object_name");
      
    }
  };

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getVersion()
   */
  public int getVersion() {
    return GrouperDdlUtils.versionIntFromEnum(this);
  }

  /**
   * cache this
   */
  private static int currentVersion = -1;
  
  /**
   * add version number col if not there
   * @param ddlVersionBean 
   * @param tableName
   */
  private static void versionNumberColumnAdd(DdlVersionBean ddlVersionBean, String tableName) {
    Database database = ddlVersionBean.getDatabase();

    //if there is no uuid col, then forget it, or if there is a old_uuid col forget it
    Table table = GrouperDdlUtils.ddlutilsFindTable(database, tableName);
    
    if (GrouperDdlUtils.ddlutilsFindColumn(database, tableName, COLUMN_HIBERNATE_VERSION_NUMBER, false) == null) {
      
      boolean destinationVersion = ddlVersionBean.isDestinationVersion();

      if (destinationVersion && ddlVersionBean.getPlatform().getName().toLowerCase().contains("postgres")) {
        ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE " + tableName + " ADD COLUMN hibernate_version_number bigint DEFAULT 0;\n");
      } else {
        versionNumberColumnFindOrCreate(table);
      }
      if (destinationVersion) {
        ddlVersionBean.getAdditionalScripts().append(
            "update " + tableName + " set hibernate_version_number = 0 where hibernate_version_number is null;\ncommit;\n");
      }
    }

  }

  /**
   * @param table
   */
  private static void versionNumberColumnFindOrCreate(Table table) {
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows", Types.BIGINT, "12", false, false);
  }
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (GrouperDdl grouperDdl : GrouperDdl.values()) {
        String number = grouperDdl.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getObjectName()
   */
  public String getObjectName() {
    return GrouperDdlUtils.objectName(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getDefaultTablePattern()
   */
  public String getDefaultTablePattern() {
    return "GROUPER%";
  }
  
  /** column for hibernate version number */
  private static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
   */
  public abstract void updateVersionFromPrevious(Database database, 
      DdlVersionBean ddlVersionBean);

  /**
   * @param database
   * @return true if needs
   */
  private static boolean needsTypesIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_TYPE_UUID, false) != null
    && GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_TYPE_UUID, false) == null
    && GrouperDdlUtils.ddlutilsFindColumn(database, GroupType.TABLE_GROUPER_TYPES, GroupType.COLUMN_OLD_ID, false) == null;
  }

  /**
   * @param database
   * @return true if needs
   */
  private static boolean needsMembersIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_MEMBER_UUID, false) != null
    && GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_MEMBER_UUID, false) == null
    && GrouperDdlUtils.ddlutilsFindColumn(database, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_OLD_ID, false) == null;
  }

  /**
   * @param database
   * @return true if needs
   */
  private static boolean needsGroupsIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_UUID, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_UUID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, Group.COLUMN_OLD_ID, false) == null;
  }

  /**
   * @param database
   * @return true if needs it
   */
  private static boolean needsFieldsIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_FIELD_UUID, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_FIELD_UUID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Field.TABLE_GROUPER_FIELDS, Field.COLUMN_OLD_ID, false) == null;
  }

  /**
   * @param database
   * @return true if needs composite conversion
   */
  private static boolean needsCompositeIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_UUID, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_UUID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Composite.TABLE_GROUPER_COMPOSITES, Composite.COLUMN_OLD_ID, false) == null;
  }  

  /**
   * @param database
   * @return true if needs composite conversion
   */
  private static boolean needsMembershipIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_MEMBERSHIP_UUID, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_MEMBERSHIP_UUID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_ID, false) == null;
  }  

  /**
   * @param database
   * @return true if needs
   */
  private static boolean needsGrouperSessionsIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_SESSION_UUID, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_SESSION_UUID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, GrouperSession.TABLE_GROUPER_SESSIONS, GrouperSession.COLUMN_OLD_ID, false) == null;
  }

  /**
   * @param database
   * @return true if needs
   */
  private static boolean needsStemIdConversion(Database database) {
    return GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_UUID, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_UUID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_ID, false) == null;
  }
  
  
  /**
   * add all foreign keys
   * @param database ddlutils database object
   * @param additionalScripts add additional scripts after the db ddl (e.g. sql).  scripts should be semicolon delimited
   * @param buildingToVersion version we are building towards (in case unit testing)
   */
  public void addAllForeignKeys(Database database, StringBuilder additionalScripts, int buildingToVersion) {

    //dont do anything if version is less than 2
    if (buildingToVersion < 2) {
      return;
    }
    
    String groupIdCol = "id";
    
    String stemIdCol = "id";
    
    String memberIdCol = "id";
    
    String typeIdCol = "id";
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Attribute.TABLE_GROUPER_ATTRIBUTES, 
        "fk_attributes_group_id", Group.TABLE_GROUPER_GROUPS, "group_id", groupIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Attribute.TABLE_GROUPER_ATTRIBUTES, 
        "fk_attributes_field_name", Field.TABLE_GROUPER_FIELDS, "field_name", "name");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Composite.TABLE_GROUPER_COMPOSITES, 
        "fk_composites_owner", Group.TABLE_GROUPER_GROUPS, "owner", groupIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Composite.TABLE_GROUPER_COMPOSITES, 
        "fk_composites_left_factor", Group.TABLE_GROUPER_GROUPS, "left_factor", groupIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Composite.TABLE_GROUPER_COMPOSITES, 
        "fk_composites_right_factor", Group.TABLE_GROUPER_GROUPS, "right_factor", groupIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Composite.TABLE_GROUPER_COMPOSITES, 
        "fk_composites_creator_id", Member.TABLE_GROUPER_MEMBERS, "creator_id", memberIdCol);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Field.TABLE_GROUPER_FIELDS, 
        "fk_fields_grouptype_uuid", "grouper_types", "grouptype_uuid", typeIdCol);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Group.TABLE_GROUPER_GROUPS, 
        "fk_groups_parent_stem", Stem.TABLE_GROUPER_STEMS, "parent_stem", stemIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Group.TABLE_GROUPER_GROUPS, 
        "fk_groups_creator_id", Member.TABLE_GROUPER_MEMBERS, "creator_id", memberIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Group.TABLE_GROUPER_GROUPS, 
        "fk_groups_modifier_id", Member.TABLE_GROUPER_MEMBERS, "modifier_id", memberIdCol);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_groups_types", 
        "fk_groups_types_group_uuid", Group.TABLE_GROUPER_GROUPS, "group_uuid", groupIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_groups_types", 
        "fk_groups_types_type_uuid", "grouper_types", "type_uuid", typeIdCol);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_member_id", Member.TABLE_GROUPER_MEMBERS, "member_id", memberIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_list_name_type", Field.TABLE_GROUPER_FIELDS, 
        GrouperUtil.toList("list_name", "list_type"),
        GrouperUtil.toList("name", "type"));
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_parent", Membership.TABLE_GROUPER_MEMBERSHIPS, "parent_membership", "id");
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_creator_id", Member.TABLE_GROUPER_MEMBERS, "creator_id", memberIdCol);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GrouperSession.TABLE_GROUPER_SESSIONS, 
        "fk_sessions_member_id", Member.TABLE_GROUPER_MEMBERS, "member_id", memberIdCol);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Stem.TABLE_GROUPER_STEMS, 
        "fk_stems_parent_stem", Stem.TABLE_GROUPER_STEMS, "parent_stem", stemIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Stem.TABLE_GROUPER_STEMS, 
        "fk_stems_creator_id", Member.TABLE_GROUPER_MEMBERS, "creator_id", memberIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Stem.TABLE_GROUPER_STEMS, 
        "fk_stems_modifier_id", Member.TABLE_GROUPER_MEMBERS, "modifier_id", memberIdCol);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_types", 
        "fk_types_creator_uuid", Member.TABLE_GROUPER_MEMBERS, "creator_uuid", memberIdCol);
  
  }

}
