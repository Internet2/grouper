/*
 * @author mchyzer
 * $Id: GrouperDdl.java,v 1.77 2009-10-03 13:47:12 shilen Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignValue;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * ddl versions and stuff for grouper.  All ddl classes must have a currentVersion method that
 * returns the current version.  
 */
public enum GrouperDdl implements DdlVersionable {

  /**
   * change stem name index if applicable
   */
  V13 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      Table stemsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Stem.TABLE_GROUPER_STEMS);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
          "stem_name_idx", true, "name");

    }
  },

  /**
   * delete create source and modify source cols if they exist
   */
  V12 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //only drop cols if there are there, and all of them (which means the conversion probably happened, and they
      //havent been dropped yet)
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, "CREATE_SOURCE", false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Group.TABLE_GROUPER_GROUPS, "CREATE_SOURCE", ddlVersionBean);
      }
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, "MODIFY_SOURCE", false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Group.TABLE_GROUPER_GROUPS, "MODIFY_SOURCE", ddlVersionBean);
      }
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, "CREATE_SOURCE", false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, "CREATE_SOURCE", ddlVersionBean);
      }
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, "MODIFY_SOURCE", false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, "MODIFY_SOURCE", ddlVersionBean);
      }
    }
  },

  /**
   * <pre>
   * if needs upgrade:
   * backup attribute table, create group cols if not exist, move data to groups,
   * delete old attribute data, 
   * 
   * if configured to drop backup attribute table, and it exists, then drop it
   * </pre>
   */
  V14 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //we need an upgrade if there is a name attribute field, and if there is no name attribute of the groups table
      boolean needsUpgrade = true;
      
      int count = 0;
      
      if (GrouperDdlUtils.assertTablesThere(false, false, "grouper_fields")) {
        
        count = HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from grouper_fields where type='attribute' and name='name'");
      
        //if there is not name attribute field, then we dont need an upgrade
        if (count == 0) {
          
          //see how many attributes are there
          count = HibernateSession.bySqlStatic().select(int.class, 
            "select count(*) from grouper_fields where type='attribute'");
          
          //are there any attributes?
          if (count > 0) {
            needsUpgrade = false;
          }
        }
        
        //if there is a name groups col, then no upgrade
        if (GrouperDdlUtils.ddlutilsFindColumn(database, Group.TABLE_GROUPER_GROUPS, "NAME", false) != null) {
          needsUpgrade = false;
        }
      } else {
        needsUpgrade = false;
      }
      boolean dropAttributeBackupTableFromGroupUpgrade = GrouperConfig.getPropertyBoolean(
          "ddlutils.dropAttributeBackupTableFromGroupUpgrade", false);
      
      if (needsUpgrade) {
        
        //first order of business, backup
        if (!dropAttributeBackupTableFromGroupUpgrade) {
          
          //make a backup
          GrouperDdlUtils.ddlutilsBackupTable(ddlVersionBean, "GROUPER_ATTRIBUTES", BAK_GROUPER_ATTRIBUTES);
          
        }
        
        //create the group cols if not exist
        addGroupNameColumns(ddlVersionBean, database);
        
        //move data to the group cols
        ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_groups set name = \n" +
        	 "  (select ga.value from grouper_attributes ga, grouper_fields gf \n" +
           "    where ga.FIELD_ID = gf.ID and gf.TYPE = 'attribute' \n" +
           "    and gf.NAME = 'name' and ga.GROUP_ID = grouper_groups.id);\ncommit;\n");
        
        ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_groups set display_name = \n" +
            "  (select ga.value from grouper_attributes ga, grouper_fields gf \n" +
            "    where ga.FIELD_ID = gf.ID and gf.TYPE = 'attribute' \n" +
            "    and gf.NAME = 'displayName' and ga.GROUP_ID = grouper_groups.id);\ncommit;\n");

        ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_groups set extension = \n" +
            "  (select ga.value from grouper_attributes ga, grouper_fields gf \n" +
            "    where ga.FIELD_ID = gf.ID and gf.TYPE = 'attribute' \n" +
            "    and gf.NAME = 'extension' and ga.GROUP_ID = grouper_groups.id);\ncommit;\n");
           
        ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_groups set display_extension = \n" +
            "  (select ga.value from grouper_attributes ga, grouper_fields gf \n" +
            "    where ga.FIELD_ID = gf.ID and gf.TYPE = 'attribute' \n" +
            "    and gf.NAME = 'displayExtension' and ga.GROUP_ID = grouper_groups.id);\ncommit;\n");
           
        ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_groups set description = \n" +
            "  (select ga.value from grouper_attributes ga, grouper_fields gf \n" +
            "    where ga.FIELD_ID = gf.ID and gf.TYPE = 'attribute' \n" +
            "    and gf.NAME = 'description' and ga.GROUP_ID = grouper_groups.id);\ncommit;\n");
        
        //delete old cols
        ddlVersionBean.appendAdditionalScriptUnique("\ndelete from grouper_attributes where  field_id in \n" +
            "  (select gf.ID from grouper_fields gf where gf.type = 'attribute' \n" +
            "    and gf.name in ('name', 'displayName', 'extension', 'displayExtension', 'description' ));\ncommit;\n");
        
        //delete old fields
        ddlVersionBean.appendAdditionalScriptUnique("\ndelete from grouper_fields where  type = 'attribute' \n" +
            " and name in ('name', 'description', 'displayExtension', 'displayName', 'extension');\ncommit;\n");

      }
      
      //whether or not needs an upgrade, see if we should delete the bak table
      if (dropAttributeBackupTableFromGroupUpgrade) {
        GrouperDdlUtils.ddlutilsDropTable(ddlVersionBean, BAK_GROUPER_ATTRIBUTES);
      }
      
    }
  },

  /** add indexes on group attribute name cols */
  V15 {
    
    public void updateVersionFromPrevious(Database database, DdlVersionBean ddlVersionBean) {

      Table groupsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Group.TABLE_GROUPER_GROUPS);

      {
        String scriptOverrideName = ddlVersionBean.isMysql() ? "\nCREATE unique INDEX group_name_idx " +
            "ON grouper_groups (name(333));\n" : null;
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, groupsTable.getName(), 
            "group_name_idx", scriptOverrideName, true, "name");
        
        String scriptOverrideDisplayName = ddlVersionBean.isMysql() ? "\nCREATE INDEX group_display_name_idx " +
            "ON grouper_groups (display_name(333));\n" : null;
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, groupsTable.getName(), 
            "group_display_name_idx", scriptOverrideDisplayName, false, "display_name");
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
          "group_parent_idx", true, "parent_stem", "extension");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
          "group_parent_display_idx", false, "parent_stem", "display_extension");
      
    }
    
  },
  
  /**
     * <pre>
     * if needs upgrade:
     * backup memberships table, create new tables, views, columns if not exist, move data around,
     * delete old data, 
     * 
     * if configured to drop backup membership cols, and they exists, then drop them
     * </pre>
     */
  V16 {
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
       */
      @Override
      public void updateVersionFromPrevious(Database database, 
          DdlVersionBean ddlVersionBean) {
        
        // there shouldn't be any effective memberships
        ddlVersionBean.appendAdditionalScriptUnique("\ndelete from grouper_memberships where mship_type = 'effective';\ncommit;\n");
  
        // check if we need to upgrade to use group set
        boolean needsUpgrade = needsMembershipAndGroupSetConversion(database);
        
        boolean dropMembershipBackupColFromMshipUpgrade = GrouperConfig.getPropertyBoolean(
            "ddlutils.dropMembershipBackupColsFromOwnerViaUpgrade", false);
        
          
        // find or add columns and indexes whether or not upgrade is needed.
          runMembershipAndGroupSetConversion(database, ddlVersionBean, false);
          
        
        if (needsUpgrade) {
          
          Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
              Membership.TABLE_GROUPER_MEMBERSHIPS);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_ID_BAK, 
              Types.VARCHAR, "128", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_VIA_ID_BAK, 
              Types.VARCHAR, "128", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_DEPTH_BAK, 
              Types.INTEGER, "11", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_PARENT_MEMBERSHIP_BAK, 
              Types.VARCHAR, "128", false, false);
  
          
          //move data to the group cols
          ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_memberships \n"
              + "set owner_group_id = (select gg.id from grouper_groups gg where gg.id = owner_id), \n"
              + "owner_stem_id = (select gs.id from grouper_stems gs where gs.id = owner_id), \n"
              + "owner_id_bak = owner_id, \n"
              + "owner_id = ' ', \n"
              + "via_composite_id = (select gc.id from grouper_composites gc where gc.id = via_id), \n"
              + "via_id_bak = via_id, \n"
              + "via_id = null, \n"
              + "depth_bak = depth, \n"
              + "depth = 0, \n"
              + "parent_membership_bak = parent_membership, \n"
              + "parent_membership = null \n"
              + "where owner_group_id is null and owner_stem_id is null \n"  
              + "and via_composite_id is null and owner_id_bak is null and via_id_bak is null;\ncommit;\n");
          
          
          // copy data from owner group id and owner stem id to the null columns
          ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_memberships \n"
              + "set owner_group_id_null = owner_group_id, \n"
              + "owner_stem_id_null = owner_stem_id, \n"
              + "owner_attr_def_id_null = owner_attr_def_id;\n");
          
          ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_memberships \n"
              + "set owner_group_id_null = '" + GroupSet.nullColumnValue + "' \n"
              + "where owner_group_id_null is null;\n");

          ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_memberships \n"
              + "set owner_attr_def_id_null = '" + GroupSet.nullColumnValue + "' \n"
              + "where owner_attr_def_id_null is null;\n");
          
          ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_memberships \n"
              + "set owner_stem_id_null = '" + GroupSet.nullColumnValue + "' \n"
              + "where owner_stem_id_null is null;\ncommit;\n");
          
          ddlVersionBean.appendAdditionalScriptUnique("\nupdate grouper_memberships \n"
              + "set enabled = 'T' \n"
              + "where enabled is null;\ncommit;\n");
          
        }
        
        // see if we should delete the bak columns
        if (!needsUpgrade && dropMembershipBackupColFromMshipUpgrade) {
          GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_VIA_ID_BAK, ddlVersionBean);
          GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_ID_BAK, ddlVersionBean);
          GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_DEPTH_BAK, ddlVersionBean);
          GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_PARENT_MEMBERSHIP_BAK, ddlVersionBean);
        }
      }
    }, 
    
  /**
   * <pre>
   * drop original columns in membership table not needed for 1.5.  also add unique index and field indexes.
   * </pre>
   */
  V17 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS);
      
      GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
          Membership.COLUMN_VIA_ID, ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
          Membership.COLUMN_OWNER_ID, ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
          Membership.COLUMN_DEPTH, ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
          Membership.COLUMN_PARENT_MEMBERSHIP, ddlVersionBean);
      
      addMembershipUniqueIndex(database, membershipsTable);
      
      addMembershipFieldIndexes(database, membershipsTable);
    }
  },

  /**
   * <pre>
   * add last membership change
   * </pre>
   */
  V18 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      Table groupsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Group.TABLE_GROUPER_GROUPS);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_LAST_MEMBERSHIP_CHANGE, Types.BIGINT, "20", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Group.TABLE_GROUPER_GROUPS,
          "group_last_membership_idx", false, Group.COLUMN_LAST_MEMBERSHIP_CHANGE);

      Table stemsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Stem.TABLE_GROUPER_STEMS);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, Stem.COLUMN_LAST_MEMBERSHIP_CHANGE, Types.BIGINT, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Stem.TABLE_GROUPER_STEMS,
          "stem_last_membership_idx", false, Stem.COLUMN_LAST_MEMBERSHIP_CHANGE);
    }
  },

  /**
   * <pre>
   * add user auditing and context ids
   * </pre>
   */
  V19 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      
      addContextIdColsLoader(database);
      
      addContextIdCols(database);
      
      addAuditTables(ddlVersionBean, database);
      
    }
  },
  
  /**
   * <pre>
   * add alternate name
   * </pre>
   */
  V20 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      Table groupsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Group.TABLE_GROUPER_GROUPS);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_ALTERNATE_NAME, Types.VARCHAR, "1024", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Group.TABLE_GROUPER_GROUPS,
          "group_alternate_name_idx", false, Group.COLUMN_ALTERNATE_NAME);
    }
  },

  /**
   * <pre>
   * add change log
   * </pre>
   */
  V21 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      addChangeLogTables(ddlVersionBean, database);
      
    }
  },
  
  /**
   * <pre>
   * add privilege management
   * </pre>
   */
  V22 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      addPrivilegeManagement(ddlVersionBean, database, false);
      
    }
  },
  
  /**
   * delete backup cols if configured to and if exist
   */
  V11 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      //if not configured to drop, then leave alone
      if (!GrouperConfig.getPropertyBoolean("ddlutils.dropBackupFieldNameTypeCols", false)) {
        return;
      }

      //only drop cols if there are there, and all of them (which means the conversion probably happened, and they
      //havent been dropped yet)
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_OLD_FIELD_NAME, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_OLD_FIELD_NAME, ddlVersionBean);
      }
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_NAME, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_TYPE, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_NAME, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_TYPE, ddlVersionBean);
      }
    }
  },

  /**
   * delete field name/type if in the right situation
   */
  V10 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      Table attributesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Attribute.TABLE_GROUPER_ATTRIBUTES);

      addAttributeFieldIndexes(database, ddlVersionBean, attributesTable);

      //only drop cols if there are there, and all of them (which means the conversion probably happened, and they
      //havent been dropped yet)
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_FIELD_NAME, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_OLD_FIELD_NAME, false) != null) {
        
        GrouperDdlUtils.ddlutilsDropColumn(database, Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_FIELD_NAME, ddlVersionBean);
      }
      if (GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_LIST_NAME, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_LIST_TYPE, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_NAME, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_TYPE, false) != null) {
        
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_LIST_NAME, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_LIST_TYPE, ddlVersionBean);
      }
    }
  },
  
  /**
   * update the fields id as a foreign key, keep backups of old data
   */
  V9 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      boolean isDestinationVersion = ddlVersionBean.isDestinationVersion();

      final StringBuilder additionalScripts = ddlVersionBean.getAdditionalScripts();

      boolean needsAttributeFieldIdConversion = needsAttributeFieldIdConversion(database);
      boolean needsMembershipFieldIdConversion = needsMembershipFieldIdConversion(database);
      
      if (needsAttributeFieldIdConversion) {
        
        Table attributesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Attribute.TABLE_GROUPER_ATTRIBUTES);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributesTable, Attribute.COLUMN_OLD_FIELD_NAME,  
            Types.VARCHAR, "32", false, false);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributesTable, Attribute.COLUMN_FIELD_ID, 
            Types.VARCHAR, "128", false, false);
      }

      if (needsMembershipFieldIdConversion) {
        
        Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Membership.TABLE_GROUPER_MEMBERSHIPS);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OLD_LIST_NAME,  
            Types.VARCHAR, "32", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OLD_LIST_TYPE,  
            Types.VARCHAR, "32", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_FIELD_ID, 
            Types.VARCHAR, "128", false, false);
        
      }
      
      //dont put scripts here if this isnt the right time (at this stage, not building toward a different one)
      //also we need to need the conversion in attributes or memberships
      if (isDestinationVersion && (needsAttributeFieldIdConversion || needsMembershipFieldIdConversion)) {
        
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          @SuppressWarnings("deprecation")
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            Connection connection = hibernateSession.getSession().connection();
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            
            //we dont know where the user is in the upgrade steps, so see if the uuid is still there...
            boolean uuidStillExists = true;
            try {
              statement = connection.prepareStatement("select field_uuid from grouper_fields");
              resultSet = statement.executeQuery();
              while (resultSet.next()) {
                //just testing
              }
             
            } catch (Exception e) {
              uuidStillExists = false;
            } finally {
              GrouperUtil.closeQuietly(resultSet);
              GrouperUtil.closeQuietly(statement);
            }
            String idCol = uuidStillExists ? "field_uuid" : "id";
            String query = "select " + idCol + ", name, type, id from grouper_fields";
            try {
              statement = connection.prepareStatement(query);
              resultSet = statement.executeQuery();
              while (resultSet.next()) {
                
                String uuid = resultSet.getString(1);
                String name = resultSet.getString(2);
                String type = resultSet.getString(3);
                String id = resultSet.getString(4);
                
                //use id if uuid is blank
                uuid = GrouperUtil.defaultIfBlank(uuid, id);
                
                if (StringUtils.isBlank(uuid)) {
                  throw new RuntimeException("Something is wrong, why is uuid blank??? '" + name + "', '" + type + "', '" + query + "'");
                }
                
                //attributes work on the attributes table, and non-attributes work on the memberships table
                if (FieldType.ATTRIBUTE.getType().equals(type)) {
                  
                  //update records, move the name to the id, commit inline so that the db undo required is not too huge
                  additionalScripts.append("update grouper_attributes set old_field_name = field_name, " +
                      "field_id = '" + uuid + "' where field_name = '" + name + "';\ncommit;\n");

                } else {
                  
                  //update records, move the name to the id, commit inline so that the db undo required is not too huge
                  additionalScripts.append("update grouper_memberships set old_list_name = list_name, old_list_type = list_type, " +
                      "field_id = '" + uuid + "' " +
                          "where list_name = '" + name + "' and list_type = '" + type + "';\ncommit;\n");
                  
                }
                
              }
            } catch (Exception e) {
              throw new RuntimeException("Problem with running query: " + query, e);
            } finally {
              GrouperUtil.closeQuietly(resultSet);
              GrouperUtil.closeQuietly(statement);
            }
            
            return null;
          }
          
        });
        
        //CH 20080823 THIS DIDNT WORK SINCE THE MAPPING DOESNT EXIST ANYMORE!!!!
        //loop through all fields:
        //List<Field> fields = HibernateSession.byCriteriaStatic().list(Field.class, null);
        //
        //for (Field field : fields) {
        //  
        //  
        //}
      }

      
    }
  },
  
  /**
   * drop grouper_sessions table if exists
   */
  V8 {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, 
        DdlVersionBean ddlVersionBean) {

      Table grouperSessionsTable = database.findTable("grouper_sessions");
      if (grouperSessionsTable != null) {
        database.removeTable(grouperSessionsTable);
      }
      
    }
  },
  
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

      if (GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_UUID, false) != null
          && GrouperDdlUtils.ddlutilsFindColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_ID, false) != null) {
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_UUID, ddlVersionBean);
        GrouperDdlUtils.ddlutilsDropColumn(database, Stem.TABLE_GROUPER_STEMS, Stem.COLUMN_OLD_ID, ddlVersionBean);
          
      }

      
    }
  },
  
  /**
   * delete uuid if in right situation
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
      
      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          Composite.TABLE_GROUPER_COMPOSITES), Composite.COLUMN_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsCompositeIdConversion(database)) {

        Table compositesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Composite.TABLE_GROUPER_COMPOSITES);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositesTable, Composite.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositesTable, Composite.COLUMN_OLD_UUID, Types.VARCHAR, "128", false, false);
        if (isDestinationVersion) {

          //update records, move the uuid to the id
          additionalScripts.append("update grouper_composites set old_id = id, id = uuid, old_uuid = uuid, uuid = ' ' where uuid != ' ' and uuid is not null;\ncommit;\n");
        }          
      }
      
      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS), Membership.COLUMN_MEMBERSHIP_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsMembershipIdConversion(database)) {
        
        Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Membership.TABLE_GROUPER_MEMBERSHIPS);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OLD_MEMBERSHIP_UUID, Types.VARCHAR, "128", false, false);

        if (isDestinationVersion) {

          //update records, move the uuid to the id
          additionalScripts.append("update grouper_memberships set old_id = id, id = membership_uuid, old_membership_uuid = membership_uuid, membership_uuid = ' ' where membership_uuid != ' ' and membership_uuid is not null;\ncommit;\n");
        }          
      }
      
      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          Field.TABLE_GROUPER_FIELDS), Field.COLUMN_FIELD_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsFieldsIdConversion(database)) {
        
        Table fieldsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Field.TABLE_GROUPER_FIELDS);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, Field.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, Field.COLUMN_OLD_FIELD_UUID, Types.VARCHAR, "128", false, false);
        
        if (isDestinationVersion) {
          //update records, move the uuid to the id
          additionalScripts.append("update grouper_fields set old_id = id, id = field_uuid, old_field_uuid = field_uuid, field_uuid = ' ' where field_uuid != ' ' and field_uuid is not null;\ncommit;\n");
        }          
      }
      
      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          Group.TABLE_GROUPER_GROUPS), Group.COLUMN_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsGroupsIdConversion(database)) {
        
        Table groupsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Group.TABLE_GROUPER_GROUPS);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_OLD_UUID, Types.VARCHAR, "128", false, false);
        
        if (isDestinationVersion) {
          //update records, move the uuid to the id
          additionalScripts.append("update grouper_groups set old_id = id, id = uuid, old_uuid = uuid, uuid = ' ' where uuid != ' ' and uuid is not null;\ncommit;\n");
        }          
      }

      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          Member.TABLE_GROUPER_MEMBERS), Member.COLUMN_MEMBER_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsMembersIdConversion(database)) {
        
        Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Member.TABLE_GROUPER_MEMBERS);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, Member.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, Member.COLUMN_OLD_MEMBER_UUID, Types.VARCHAR, "128", false, false);
        
        if (isDestinationVersion) {
          //update records, move the uuid to the id
          additionalScripts.append("update grouper_members set old_id = id, id = member_uuid, old_member_uuid = member_uuid, member_uuid = ' ' where member_uuid != ' ' and member_uuid is not null;\ncommit;\n");
        }          
      }

      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          Stem.TABLE_GROUPER_STEMS), Stem.COLUMN_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsStemIdConversion(database)) {
        
        Table stemsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            Stem.TABLE_GROUPER_STEMS);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, Stem.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, Stem.COLUMN_OLD_UUID, Types.VARCHAR, "128", false, false);
        
        if (isDestinationVersion) {
          //update records, move the uuid to the id
          additionalScripts.append("update grouper_stems set old_id = id, id = uuid, old_uuid = uuid, uuid = ' ' where uuid != ' ' and uuid is not null;\ncommit;\n");
        }          
      }

      GrouperDdlUtils.ddlutilsDropIndexes(GrouperDdlUtils.ddlutilsFindTable(database, 
          GroupType.TABLE_GROUPER_TYPES), GroupType.COLUMN_TYPE_UUID);
      //we need conversion if there is a uuid col, and not an old_uuid col or old_id col
      if (needsTypesIdConversion(database)) {
        
        Table typesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
            GroupType.TABLE_GROUPER_TYPES);
              
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, GroupType.COLUMN_OLD_ID, Types.VARCHAR, "128", false, false);
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, GroupType.COLUMN_OLD_TYPE_UUID, Types.VARCHAR, "128", false, false);
        
        if (isDestinationVersion) {
          //update records, move the uuid to the id
          additionalScripts.append("update grouper_types set old_id = id, id = type_uuid, old_type_uuid = type_uuid, type_uuid = ' ' where type_uuid != ' ' and type_uuid is not null;\ncommit;\n");
        }          
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
      Table grouploaderLogTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouper_loader_log");
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "id", 
          Types.VARCHAR, "128", true, true);
      
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_name", 
          Types.VARCHAR, "512", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "status", 
          Types.VARCHAR, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "started_time", 
          Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "ended_time", 
          Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis_get_data", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis_load_data", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_type", 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_type", 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_description", 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_message", 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "host", 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "group_uuid", 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_quartz_cron", 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_interval_seconds", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "last_updated", 
          Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "unresolvable_subject_count", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "insert_count", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "update_count", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "delete_count", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "total_count", 
          Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "parent_job_name", 
          Types.VARCHAR, "512", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "parent_job_id", 
          Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "and_group_names", 
          Types.VARCHAR, "512", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_priority", 
          Types.INTEGER, null, false, false);

      //see if the grouper_ext_loader_log table is there
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_loader_log",
          "grouper_loader_job_name_idx", false, "job_name");

      addContextIdColsLoader(database);
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

      boolean groupsTableNew = database.findTable(Group.TABLE_GROUPER_GROUPS) == null;

      {
        
        boolean attributesTableNew = database.findTable(Attribute.TABLE_GROUPER_ATTRIBUTES) == null;
        
        Table attributeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Attribute.TABLE_GROUPER_ATTRIBUTES);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "group_id", 
            Types.VARCHAR, "128", false, true);
  
        if (buildingToThisVersion) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "field_name", 
              Types.VARCHAR, "32", false, false);
        
        } 
        
        //this is needed for hibernate, so always add it if the table is being created
        if (attributesTableNew || attributeTable.findColumn(Attribute.COLUMN_FIELD_ID) != null) {
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, Attribute.COLUMN_FIELD_ID, 
              Types.VARCHAR, "128", false, true);
        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, "value", 
            Types.VARCHAR, "1024", false, true);
  
        //dont add foreign keys if col not there
        if (attributeTable.findColumn(Attribute.COLUMN_FIELD_ID) != null) {
          addAttributeFieldIndexes(database, ddlVersionBean, attributeTable);
        }
        
        //see if we have a custom script here, do this since some versions of mysql cant handle indexes on columns that large
        String scriptOverride = ddlVersionBean.isMysql() ? "\nCREATE INDEX attribute_value_idx " +
            "ON grouper_attributes (value(333));\n" : null;
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, attributeTable.getName(), 
            "attribute_value_idx", scriptOverride, false, "value");
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeTable.getName(), "attribute_group_idx", false, "group_id");
        
        versionNumberColumnFindOrCreate(attributeTable);
        
      }
      
      {
        Table compositeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Composite.TABLE_GROUPER_COMPOSITES);
  
        boolean needsConversion = needsCompositeIdConversion(database);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "id", 
            Types.VARCHAR, "128", true, true);

        if (needsConversion || buildingToThisVersion) {

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "uuid", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
              "composite_uuid_idx", true, "uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "owner", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "left_factor", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "right_factor", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "type", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "creator_id", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, "create_time", 
            Types.BIGINT, "20", false, true);
  
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

        Table fieldsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Field.TABLE_GROUPER_FIELDS);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "id", 
            Types.VARCHAR, "128", true, true);

        boolean needsConversion = needsFieldsIdConversion(database);
    
        if (needsConversion || buildingToThisVersion) {

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "field_uuid", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, fieldsTable.getName(), 
              "field_uuid_idx", true, "field_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "grouptype_uuid", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "is_nullable", 
            Types.BIT, "1", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "name", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "read_privilege", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "type", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, "write_privilege", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, fieldsTable.getName(), 
            "name_and_type", true, "name", "type");
        
        versionNumberColumnFindOrCreate(fieldsTable);

      }
    
      {
        Table groupsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Group.TABLE_GROUPER_GROUPS);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsGroupsIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "uuid", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
              "group_uuid_idx", true, "uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "parent_stem", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "creator_id", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "create_time", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "modifier_id", 
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "modify_time", 
            Types.BIGINT, "20", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_LAST_MEMBERSHIP_CHANGE, 
            Types.BIGINT, "20", false, false); 
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, Group.COLUMN_ALTERNATE_NAME, 
            Types.VARCHAR, "1024", false, false); 
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Group.TABLE_GROUPER_GROUPS,
            "group_alternate_name_idx", false, Group.COLUMN_ALTERNATE_NAME);
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Group.TABLE_GROUPER_GROUPS,
            "group_last_membership_idx", false, Group.COLUMN_LAST_MEMBERSHIP_CHANGE);

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_creator_idx", false, "creator_id");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_createtime_idx", false, "create_time");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_modifier_idx", false, "modifier_id");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
            "group_modifytime_idx", false, "modify_time");
        
        versionNumberColumnFindOrCreate(groupsTable);
        
        addGroupNameColumns(ddlVersionBean, database);

        //only do this if there is a uuid col
        if (groupsTable.findColumn("UUID") != null) {
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupsTable.getName(), 
              "group_uuid_idx", true, "uuid");
        }
        
      }
    
      {
        Table groupsTypesTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "grouper_groups_types");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, "group_uuid", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, "type_uuid", 
            Types.VARCHAR, "128", false, true);
  
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
            Member.TABLE_GROUPER_MEMBERS);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsMembersIdConversion(database);
        
        if (needsConversion || buildingToThisVersion ) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "member_uuid", 
              Types.VARCHAR, "128", false, false);
    
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
              "member_uuid_idx", true, "member_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "subject_id", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "subject_source", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, "subject_type", 
            Types.VARCHAR, "255", false, true);
  
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
        boolean membershipsTableExists = database.findTable(Membership.TABLE_GROUPER_MEMBERSHIPS) != null;
        Table membershipsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Membership.TABLE_GROUPER_MEMBERSHIPS);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsMembersIdConversion(database);
        
        if (needsConversion || buildingToThisVersion ) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "membership_uuid", 
              Types.VARCHAR, "128", false, false);
    
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
              "membership_uuid_idx", true, "membership_uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "member_id", 
            Types.VARCHAR, "128", false, true);
        
        //only add the col if a new table, else it is added in a subsequent version
        if (!membershipsTableExists || membershipsTable.findColumn(Membership.COLUMN_FIELD_ID) != null) {
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_FIELD_ID, 
              Types.VARCHAR, "128", false, true);
        }
  
        //if it doesnt exist, then add these cols/indexes...
        if (!membershipsTableExists) {
          runMembershipAndGroupSetConversion(database, ddlVersionBean, !membershipsTableExists);
          addMembershipUniqueIndex(database, membershipsTable);
        }
        
        //if not testing, dont worry about these columns
        if (buildingToThisVersion) {
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "list_name", 
              Types.VARCHAR, "32", false, false);
    
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "list_type", 
              Types.VARCHAR, "32", false, false);
        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "mship_type", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "creator_id", 
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, "create_time", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_createtime_idx", false, "create_time");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_creator_idx", false, "creator_id");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_member_idx", false, "member_id");

        //dont add foreign keys if col not there
        if (membershipsTable.findColumn(Membership.COLUMN_FIELD_ID) != null) {

          addMembershipFieldIndexes(database, membershipsTable);

        }

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
            "membership_type_idx", false, "mship_type");

        versionNumberColumnFindOrCreate(membershipsTable);
       
      }
      {
        if (buildingToThisVersion) {
          Table sessionsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
              "grouper_sessions");
    
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "id", 
              Types.VARCHAR, "128", true, true);
          
          //note, this code is only here for unit testing... no need to do id conversion
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "member_id", 
              Types.VARCHAR, "128", false, true);
    
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(sessionsTable, "starttime", 
              Types.BIGINT, "20", false, true);
    
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, sessionsTable.getName(), 
              "session_member_idx", false, "member_id");
  
          versionNumberColumnFindOrCreate(sessionsTable);
        }
      }
      {
        Table stemsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            Stem.TABLE_GROUPER_STEMS);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsStemIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "uuid", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
              "stem_uuid_idx", true, "uuid");

        }
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "parent_stem", 
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "name", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "display_name", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "creator_id", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "create_time", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "modifier_id", 
            Types.VARCHAR, "128", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "modify_time", 
            Types.BIGINT, "20", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "display_extension", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "extension", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "description", 
            Types.VARCHAR, "1024", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, Stem.COLUMN_LAST_MEMBERSHIP_CHANGE, 
            Types.BIGINT, "20", false, false); 

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Stem.TABLE_GROUPER_STEMS,
            "stem_last_membership_idx", false, Stem.COLUMN_LAST_MEMBERSHIP_CHANGE);

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
            "stem_name_idx", true, "name");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, stemsTable.getName(), 
            "stem_parent_idx", false, "parent_stem");

        versionNumberColumnFindOrCreate(stemsTable);
      }
      {
        Table typesTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "grouper_types");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "id", 
            Types.VARCHAR, "128", true, true);
  
        boolean needsConversion = needsTypesIdConversion(database);
        
        if (needsConversion || buildingToThisVersion) {
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "type_uuid", 
              Types.VARCHAR, "128", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, typesTable.getName(), 
              "type_uuid_idx", true, "type_uuid");

        }
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "name", 
            Types.VARCHAR, "255", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "creator_uuid", 
            Types.VARCHAR, "128", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "create_time", 
            Types.BIGINT, "20", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "is_assignable", 
            Types.BIT, "1", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, "is_internal", 
            Types.BIT, "1", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, typesTable.getName(), 
            "type_name_idx", true, "name");

        versionNumberColumnFindOrCreate(typesTable);
      }
      
      addContextIdCols(database);
      
      addAuditTables(ddlVersionBean, database);

      addChangeLogTables(ddlVersionBean, database);

      addPrivilegeManagement(ddlVersionBean, database, groupsTableNew);
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
      Table grouperDdlTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouper_ddl");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "id",
          Types.VARCHAR, "128", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "object_name", 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "db_version", 
          Types.INTEGER, null, false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "last_updated", 
          Types.VARCHAR, "50", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "history", 
          Types.VARCHAR, "4000", false, false);

      //object name is unique
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_ddl", "grouper_ddl_object_name_idx", 
          true, "object_name");
      
    }
  };

  /**
   * context id column name
   */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /**
   * 
   */
  public static final String BAK_GROUPER_ATTRIBUTES = "BAK_GROUPER_ATTRIBUTES";

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
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_HIBERNATE_VERSION_NUMBER, 
        Types.BIGINT, "12", false, false);
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
   * @return true if needs composite conversion
   */
  private static boolean needsMembershipFieldIdConversion(Database database) {
    //no field_id
    return GrouperDdlUtils.ddlutilsFindColumn(database, 
        Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_FIELD_ID, false) == null
        //has list name and type
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_LIST_NAME, false) != null
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_LIST_TYPE, false) != null
        //no old list name or type
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_NAME, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OLD_LIST_TYPE, false) == null;
  }  

  /**
   * @param database
   * @return true if we're upgrading to use group set
   */
  private static boolean needsMembershipAndGroupSetConversion(Database database) {
    //has via_id
    return GrouperDdlUtils.ddlutilsFindColumn(database, 
        Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_VIA_ID, false) != null
        //not has via_composite_id
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_VIA_COMPOSITE_ID, false) == null
      //not has via bak
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_VIA_ID_BAK, false) == null
      
      //has owner id
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_ID, false) != null
      //not has owner_group_id, or owner_stem_id
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_GROUP_ID, false) == null
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_STEM_ID, false) == null
      //not has owner bak
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, "OWNER_ID_BAK", false) == null
      
      // has depth
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_DEPTH, false) != null
      
      // not has depth bak
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_DEPTH_BAK, false) == null    
      
      // has parent membership
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_PARENT_MEMBERSHIP, false) != null  
      
      // not has parent membership bak
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_PARENT_MEMBERSHIP_BAK, false) == null  
          
      // not has owner group id null
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_GROUP_ID_NULL, false) == null  
      
      // not has owner stem id null
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_STEM_ID_NULL, false) == null;
  }  

  
  
  /**
   * @param database
   * @return true if needs composite conversion
   */
  private static boolean needsAttributeFieldIdConversion(Database database) {
    //no field_id
    return GrouperDdlUtils.ddlutilsFindColumn(database, 
        Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_FIELD_ID, false) == null
      //has field name
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_FIELD_NAME, false) != null
      //no old field name
      && GrouperDdlUtils.ddlutilsFindColumn(database, 
          Attribute.TABLE_GROUPER_ATTRIBUTES, Attribute.COLUMN_OLD_FIELD_NAME, false) == null;
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
   * drop all views
   * @param ddlVersionBean 
   */
  public void dropAllViews(DdlVersionBean ddlVersionBean) {
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attributes_v");

    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_group_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_efmship_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_stem_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_member_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_mship_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_attrdef_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_assn_v");

    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_asn_group_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_asn_stem_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_asn_member_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_asn_mship_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_asn_efmship_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_asn_asn_attrdef_v");
    

    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_def_name_set_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_attr_def_priv_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_audit_entry_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_composites_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_groups_types_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_groups_v");
    
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_perms_role_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_perms_role_subject_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_perms_all_v");
    
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_roles_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_memberships_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_memberships_lw_v");
    
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_memberships_all_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_memberships_group_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_memberships_stem_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_memberships_attr_def_v");
    
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_role_set_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_attributes_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_composites_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_group_field_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_groups_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_members_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_roles_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_stems_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_rpt_types_v");
    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_stems_v");
    
  }
  
  /**
   * add all foreign keys
   * @param ddlVersionBean 
   */
  public void addAllForeignKeysViewsEtc(DdlVersionBean ddlVersionBean) {

    Database database = ddlVersionBean.getDatabase();
    
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    //dont do anything if version is less than 2, or if not putting all cols in there
    if (buildingToVersion < V2.getVersion() || !addGroupNameColumns) {
      return;
    }
    
    //add comments on tables, comments
    //see if the grouper_ext_loader_log table is there
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,"grouper_ddl", 
        "holds a record for each database object name, and db version, and java version");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_ddl", "id", "uuid of this ddl record");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_ddl", "object_name", 
        "Corresponds to an enum in grouper.ddl package (with Ddl on end), represents one module, " +
        "so grouper itself is one object");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_ddl", "db_version", 
        "Version of this object as far as DB knows about");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_ddl", "last_updated", 
        "last update timestamp, string so it can easily be used from update statement");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_ddl", "history", 
        "history of this object name, with most recent first (truncated after 4k)");

    
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        Attribute.TABLE_GROUPER_ATTRIBUTES, "attributes for groups, including name, extension, etc");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES,  "id", 
        "db id of this attribute record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES,  "group_id", 
        "group_uuid foreign key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES,  Attribute.COLUMN_FIELD_ID, 
          "foreign key to field by id");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES,   "value", 
        "value this attribute record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES, 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Attribute.TABLE_GROUPER_ATTRIBUTES, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");
    
    
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,AuditEntry.TABLE_GROUPER_AUDIT_ENTRY, 
      "holds one record for each audit entry record which is a high level action that ties together lower level actions");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "act_as_member_id", "Member id (foreign key) of the user who is being acted as");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "audit_type_id", "");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "context_id", "Context id links together multiple operations into one high level action");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "created_on", "When this audit entry record was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "description", "Description is a sentence form expression of what is being audited");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "env_name", "environment label of the system running, from grouper.properties");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "grouper_engine", "Grouper engine is e.g. UI, WS, GSH, loader, etc");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "grouper_version", "Grouper version of the API executing");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "hibernate_version_number", "hibernate version number keeps track of if multiple sessions step on toes");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "id", "db id of this audit entry record");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "int01", "The int 01 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "int02", "The int 02 value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "int03", "The int 03 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "int04", "The int 04 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "int05", "The int 05 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "last_updated", "When this audit entry was last updated");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "logged_in_member_id", "Member id (foreign key) of the user logged in");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "server_host", "Host of the system running the grouper API");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string01", "The string 01 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string02", "The string 02 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string03", "The string 03 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string04", "The string 04 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string05", "The string 05 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string06", "The string 06 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string07", "The string 07 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "string08", "The string 08 value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "user_ip_address", "IP address of the user connecting to the system (e.g. from UI or WS)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "duration_microseconds", "Duration of the context, in microseconds (millionths of a second)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "query_count", "Number of database queries required for this context");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY,  
        "server_user_name", "Username of the OS user running the API.  This might identify who ran a GSH call");

    
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,AuditType.TABLE_GROUPER_AUDIT_TYPE, 
      "audit type is a category and an action that organizes audits.  Also holds labels for all the misc string and int fields");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "action_name", "The action in this audit category to differentiate from others");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "audit_category", "The category of this audit in logical grouping");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "context_id", "Context id links together multiple operations into one high level action");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "created_on", "When this audit type was created");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "hibernate_version_number", "Hibernate version number makes sure multiple sessions do not step on toes");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "id", "Unique id of this audit entry");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_int01", "The int 01 value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_int02", "The int 02 value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_int03", "The int 03 value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_int04", "The int 04 value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_int05", "The int 05 value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string01", "The label of the string field 01 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string02", "The label of the string field 02 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string03", "The label of the string field 03 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string04", "The label of the string field 04 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string05", "The label of the string field 05 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string06", "The label of the string field 06 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string07", "The label of the string field 07 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "label_string08", "The label of the string field 08 from grouper_audit_type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, AuditType.TABLE_GROUPER_AUDIT_TYPE,  
        "last_updated", "When this audit type was last updated");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
          Composite.TABLE_GROUPER_COMPOSITES, "records the composite group, and its factors");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES, "id", 
          "db id of this composite record");

    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES,  "owner", 
          "group uuid of the composite group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES,  "left_factor", 
          "left factor of the composite group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES,  "right_factor", 
          "right factor of the composite group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES,  "type", 
          "e.g. union, complement, intersection");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES,  "creator_id", 
          "member uuid of who created this");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES,  "create_time", 
          "number of millis since 1970 until when created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES, 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Composite.TABLE_GROUPER_COMPOSITES, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS, "describes fields related to types");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS,  "id", 
          "db id of this field record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS,  "grouptype_uuid", 
          "foreign key to group type");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS, "is_nullable", 
          "if this is nullable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS, "name", 
          "name of the field");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS,  "read_privilege", 
          "which privilege is required to read this field");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS,  "type", 
          "type of field (e.g. attribute, list, access, naming)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS, "write_privilege", 
          "which privilege is required to write this attribute");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Field.TABLE_GROUPER_FIELDS, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
          Group.TABLE_GROUPER_GROUPS, "holds the groups in the grouper system");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,  "id", 
          "db id of this group record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "parent_stem", 
          "uuid of the stem that this group refers to");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "creator_id", 
          "member uuid of the creator of this group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "create_time", 
          "number of millis since 1970 that this group was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "modifier_id", 
          "member uuid of the last modifier of this group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "modify_time", 
          "number of millis since 1970 that this group was modified");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS, 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "name", 
      "group name is the fully qualified extension of group and all parent stems.  It shouldnt change much, and can be used to reference group from external systems");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "display_name", 
      "group display name is the fully qualified display extension of group and all parent stems.  It can change as needed, and can not be used to reference group from external systems");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "extension", 
      "group extension is the label for this group inside a stem.  It shouldnt change much, and can be used to reference group from external systems (in conjunction with parent stem id)");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "display_extension", 
      "group display extension is the display label for this group inside a stem.  It cant change as needed, and can not be used to reference group from external systems");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS,   "description", 
      "group description is an optional text blurb that can be used to describe the group");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS, 
        Group.COLUMN_LAST_MEMBERSHIP_CHANGE, "If configured to keep track, this is the last membership change for this group");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS, 
        Group.COLUMN_ALTERNATE_NAME, "An alternate name for this group");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Group.TABLE_GROUPER_GROUPS, 
        Group.COLUMN_TYPE_OF_GROUP, "if this is a group or role");
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        "grouper_groups_types", "holds the association between group and type");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_groups_types", "id", 
          "id of this group/type record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_groups_types",  "group_uuid", 
          "group uuid foreign key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_groups_types",  "type_uuid", 
          "type uuid foreign key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_groups_types", 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_groups_types", 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
          Member.TABLE_GROUPER_MEMBERS, "keeps track of subjects used in grouper.  Records are never deleted from this table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS,  "id", 
          "db id of this row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS, "subject_id", 
          "subject id is the id from the subject source");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS,  "subject_source", 
          "id of the source from sources.xml");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS,  "subject_type", 
          "type of subject, e.g. person");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS, 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Member.TABLE_GROUPER_MEMBERS, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
          Membership.TABLE_GROUPER_MEMBERSHIPS, "keeps track of memberships and permissions");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS,  "id", 
          "db id of this row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_GROUP_ID, 
          "group of the membership if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_OWNER_STEM_ID, 
    "stem of the membership if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, "member_id", 
          "member of the memership");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_FIELD_ID, 
            "foreign key to field by id");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, "mship_type", 
          "type of membership, immediate or composite");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, Membership.COLUMN_VIA_COMPOSITE_ID, 
        "for composite, this is the composite uuid");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, "creator_id", 
          "member uuid of the creator of this record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, "create_time", 
          "number of millis since 1970 that this record was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        Membership.COLUMN_OWNER_GROUP_ID_NULL, "Same as " + Membership.COLUMN_OWNER_GROUP_ID + " except nulls are replaced with the string " + Membership.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        Membership.COLUMN_OWNER_STEM_ID_NULL, "Same as " + Membership.COLUMN_OWNER_STEM_ID + " except nulls are replaced with the string " + Membership.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        Membership.COLUMN_ENABLED, "T or F to indicate if the membership is enabled");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        Membership.COLUMN_ENABLED_TIMESTAMP, "When the membership will be enabled if the time is in the future.");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        Membership.COLUMN_DISABLED_TIMESTAMP, "When the membership will be disabled if the time is in the future.");
    
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        GroupSet.TABLE_GROUPER_GROUP_SET, "keeps track of the set of immediate and effective group members for all groups and stems");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_ID, "db id of this row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_FIELD_ID, "field represented by this group set");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MSHIP_TYPE, "type of membership represented by this group set, immediate or composite or effective");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_VIA_GROUP_ID, "same as member_group_id if depth is greater than 0, otherwise null.");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_DEPTH, "number of hops in directed graph");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_PARENT_ID, "parent group set");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_CREATOR_ID, "member uuid of the creator of this record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_CREATE_TIME, "number of millis since 1970 that this record was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_OWNER_ATTR_DEF_ID, "owner attr def if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_OWNER_ATTR_DEF_ID_NULL, "same as " + GroupSet.COLUMN_OWNER_ATTR_DEF_ID + " except nulls are replaced with the string " + GroupSet.nullColumnValue);

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_OWNER_GROUP_ID, "owner group if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_OWNER_GROUP_ID_NULL, "same as " + GroupSet.COLUMN_OWNER_GROUP_ID + " except nulls are replaced with the string " + GroupSet.nullColumnValue);

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_OWNER_STEM_ID, "owner stem if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_OWNER_STEM_ID_NULL, "same as " + GroupSet.COLUMN_OWNER_STEM_ID + " except nulls are replaced with the string " + GroupSet.nullColumnValue);

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MEMBER_ATTR_DEF_ID, "member attr def if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MEMBER_ATTR_DEF_ID_NULL, "same as " + GroupSet.COLUMN_MEMBER_ATTR_DEF_ID + " except nulls are replaced with the string " + GroupSet.nullColumnValue);

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MEMBER_GROUP_ID, "member group if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MEMBER_GROUP_ID_NULL, "same as " + GroupSet.COLUMN_MEMBER_GROUP_ID + " except nulls are replaced with the string " + GroupSet.nullColumnValue);

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MEMBER_STEM_ID, "member stem if applicable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_MEMBER_STEM_ID_NULL, "same as " + GroupSet.COLUMN_MEMBER_STEM_ID + " except nulls are replaced with the string " + GroupSet.nullColumnValue);

    
    
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        Stem.TABLE_GROUPER_STEMS, "entries for stems and their attributes");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS, "id", 
          "db id of this row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "parent_stem", 
          "stem uuid of parent stem or empty if under root");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "name", 
          "full name (id) path of stem");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "display_name", 
          "full dislpay name path of stem");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS, "creator_id", 
          "member_id of who created this stem");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "create_time", 
          "number of millis since 1970 since this was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "modifier_id", 
          "member_id of modifier who last edited");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "modify_time", 
          "number of millis since 1970 since this was edited");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "display_extension", 
          "display extension (not full path) of stem");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS, "extension", 
          "extension (id) (not full path) of this stem");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS,  "description", 
          "description of stem");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS, 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS, 
        Stem.COLUMN_LAST_MEMBERSHIP_CHANGE, "If configured to keep track, this is the last membership change for this stem");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, Stem.TABLE_GROUPER_STEMS, 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
          "grouper_types", "the various types which can be assigned to groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types",  "id", 
          "db id of this row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types",  "name", 
          "name of this type");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types",  "creator_uuid", 
          "member_id of the creator");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types",  "create_time", 
          "number of millis since 1970 since this was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types",  "is_assignable", 
          "if this type is assignable (not internal)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types",  "is_internal", 
          "if this type if internal (not assignable)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types", 
        COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate uses this to version rows");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_types", 
        COLUMN_CONTEXT_ID, "Context id links together multiple operations into one high level action");

    
    //see if the grouper_ext_loader_log table is there
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, "grouper_loader_log", 
        "log table with a row for each grouper loader job run");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "id", "uuid of this log record");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_name", 
        "Could be group name (friendly) or just config name");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "status", 
        "STARTED, RUNNING, SUCCESS, ERROR, WARNING, CONFIG_ERROR");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "started_time", 
        "When the job was started");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "ended_time", 
        "When the job ended (might be blank if daemon died)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "millis", 
        "Milliseconds this process took");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "millis_get_data", 
        "Milliseconds this process took to get the data from the source");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "millis_load_data", 
        "Milliseconds this process took to load the data to grouper");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_type", 
        "GrouperLoaderJobType enum value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_schedule_type", 
        "GrouperLoaderJobscheduleType enum value");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_description", 
        "More information about the job");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_message", 
        "Could be a status or error message or stack");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "host", 
        "Host that this job ran on");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "group_uuid", 
        "If this job involves one group, this is uuid");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_schedule_quartz_cron", 
        "Quartz cron string for this col");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_schedule_interval_seconds", 
        "How many seconds this is supposed to wait between runs");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "last_updated", 
        "When this record was last updated");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "unresolvable_subject_count", 
        "The number of records which were not subject resolvable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "insert_count", 
        "The number of records inserted");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "update_count", 
        "The number of records updated");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "delete_count", 
        "The number of records deleted");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "total_count", 
        "The total number of records (e.g. total number of members)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "parent_job_name", 
        "If this job is a subjob of another job, then put the parent job name here");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "parent_job_id", 
        "If this job is a subjob of another job, then put the parent job id here");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "and_group_names", 
        "If this group query is anded with another group or groups, they are listed here comma separated");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_loader_log",  "job_schedule_priority", 
        "Priority of this job (5 is unprioritized, higher the better)");


    
    String groupIdCol = "id";
    
    String stemIdCol = "id";
    
    String memberIdCol = "id";
    
    String typeIdCol = "id";
    
    //dont add if just testing
    boolean buildingAudits = buildingToVersion >= V19.getVersion();
    if (buildingAudits) {
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, AuditEntry.TABLE_GROUPER_AUDIT_ENTRY, 
          "fk_audit_entry_type_id", AuditType.TABLE_GROUPER_AUDIT_TYPE, "audit_type_id", "id");

    }
    
    //dont add if just testing
    boolean buildingChangeLogs = buildingToVersion >= V21.getVersion();
    if (buildingChangeLogs) {
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, ChangeLogEntry.TABLE_GROUPER_CHANGE_LOG_ENTRY, 
          "fk_change_log_entry_type_id", ChangeLogType.TABLE_GROUPER_CHANGE_LOG_TYPE, "change_log_type_id", "id");

    }
    
    //dont add if just testing
    boolean buildingFieldIds = buildingToVersion > V4.getVersion();
    if (buildingFieldIds) {
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Attribute.TABLE_GROUPER_ATTRIBUTES, 
          "fk_attributes_field_id", Field.TABLE_GROUPER_FIELDS, "field_id", "id");
    }

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, AttributeDefName.TABLE_GROUPER_ATTRIBUTE_DEF_NAME, 
        "fk_attr_def_name_stem", Stem.TABLE_GROUPER_STEMS, 
        AttributeDefName.COLUMN_STEM_ID, Stem.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF, 
        "fk_attr_def_stem", Stem.TABLE_GROUPER_STEMS, 
        AttributeDef.COLUMN_STEM_ID, Stem.COLUMN_ID);

    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, AttributeDefNameSet.TABLE_GROUPER_ATTRIBUTE_DEF_NAME_SET, 
        "fk_attr_def_name_set_parent", AttributeDefNameSet.TABLE_GROUPER_ATTRIBUTE_DEF_NAME_SET, 
        AttributeDefNameSet.COLUMN_PARENT_ATTR_DEF_NAME_SET_ID, AttributeDefNameSet.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, AttributeDefNameSet.TABLE_GROUPER_ATTRIBUTE_DEF_NAME_SET, 
        "fk_attr_def_name_if", AttributeDefName.TABLE_GROUPER_ATTRIBUTE_DEF_NAME, 
        AttributeDefNameSet.COLUMN_IF_HAS_ATTRIBUTE_DEF_NAME_ID, AttributeDefName.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, AttributeDefNameSet.TABLE_GROUPER_ATTRIBUTE_DEF_NAME_SET, 
        "fk_attr_def_name_then", AttributeDefName.TABLE_GROUPER_ATTRIBUTE_DEF_NAME, 
        AttributeDefNameSet.COLUMN_THEN_HAS_ATTRIBUTE_DEF_NAME_ID, AttributeDefName.COLUMN_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, RoleSet.TABLE_GROUPER_ROLE_SET, 
        "fk_role_set_parent", RoleSet.TABLE_GROUPER_ROLE_SET, 
        RoleSet.COLUMN_PARENT_ROLE_SET_ID, RoleSet.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, RoleSet.TABLE_GROUPER_ROLE_SET, 
        "fk_role_if", Group.TABLE_GROUPER_GROUPS, 
        RoleSet.COLUMN_IF_HAS_ROLE_ID, Group.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, RoleSet.TABLE_GROUPER_ROLE_SET, 
        "fk_role_then", Group.TABLE_GROUPER_GROUPS, 
        RoleSet.COLUMN_THEN_HAS_ROLE_ID, Group.COLUMN_ID);
    
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
    if (buildingFieldIds) {

      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
          "fk_membership_field_id", Field.TABLE_GROUPER_FIELDS, "field_id", "id");
    }
 
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_creator_id", Member.TABLE_GROUPER_MEMBERS, "creator_id", memberIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_group_owner_id", Group.TABLE_GROUPER_GROUPS, Membership.COLUMN_OWNER_GROUP_ID, Group.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_stem_owner_id", Stem.TABLE_GROUPER_STEMS, Membership.COLUMN_OWNER_STEM_ID, Stem.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Membership.TABLE_GROUPER_MEMBERSHIPS, 
        "fk_memberships_comp_via_id", Composite.TABLE_GROUPER_COMPOSITES, Membership.COLUMN_VIA_COMPOSITE_ID, Composite.COLUMN_ID);

    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_creator_id", Member.TABLE_GROUPER_MEMBERS, GroupSet.COLUMN_CREATOR_ID, memberIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_field_id", Field.TABLE_GROUPER_FIELDS, GroupSet.COLUMN_FIELD_ID, "id");
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_via_group_id", Group.TABLE_GROUPER_GROUPS, GroupSet.COLUMN_VIA_GROUP_ID, Group.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_parent_id", GroupSet.TABLE_GROUPER_GROUP_SET, GroupSet.COLUMN_PARENT_ID, GroupSet.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_owner_attr_def_id", AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF, GroupSet.COLUMN_OWNER_ATTR_DEF_ID, AttributeDef.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_mbr_attr_def_id", AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF, GroupSet.COLUMN_MEMBER_GROUP_ID, AttributeDef.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_owner_group_id", Group.TABLE_GROUPER_GROUPS, GroupSet.COLUMN_OWNER_GROUP_ID, Group.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_member_group_id", Group.TABLE_GROUPER_GROUPS, GroupSet.COLUMN_MEMBER_GROUP_ID, Group.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_owner_stem_id", Stem.TABLE_GROUPER_STEMS, GroupSet.COLUMN_OWNER_STEM_ID, Stem.COLUMN_ID);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GroupSet.TABLE_GROUPER_GROUP_SET, 
        "fk_group_set_member_stem_id", Stem.TABLE_GROUPER_STEMS, GroupSet.COLUMN_MEMBER_STEM_ID, Stem.COLUMN_ID);
          
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Stem.TABLE_GROUPER_STEMS, 
        "fk_stems_parent_stem", Stem.TABLE_GROUPER_STEMS, "parent_stem", stemIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Stem.TABLE_GROUPER_STEMS, 
        "fk_stems_creator_id", Member.TABLE_GROUPER_MEMBERS, "creator_id", memberIdCol);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, Stem.TABLE_GROUPER_STEMS, 
        "fk_stems_modifier_id", Member.TABLE_GROUPER_MEMBERS, "modifier_id", memberIdCol);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_types", 
        "fk_types_creator_uuid", Member.TABLE_GROUPER_MEMBERS, "creator_uuid", memberIdCol);
  
    //now lets add views
    if (buildingAudits) {
      GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_audit_entry_v",
          "Join of audit entry and audit type, and converts member ids to subject ids",
          GrouperUtil.toSet("created_on", "audit_category", "action_name", 
              "logged_in_subject_id",
              "act_as_subject_id",
              "label_string01", "string01",
              "label_string02", "string02",
              "label_string03", "string03",
              "label_string04", "string04",
              "label_string05", "string05",
              "label_string06", "string06",
              "label_string07", "string07",
              "label_string08", "string08",
              "label_int01", "int01",
              "label_int02", "int02",
              "label_int03", "int03",
              "label_int04", "int04",
              "label_int05", "int05",
              "context_id", "grouper_engine",
              "description", "logged_in_source_id", "act_as_source_id", 
              "logged_in_member_id", "act_as_member_id",
              "audit_type_id",
              "user_ip_address", "server_host",
              "audit_entry_last_updated", "audit_entry_id", "grouper_version", "env_name"),
           GrouperUtil.toSet("When this audit entry record was created",
               "The category of this audit from grouper_audit_type",
               "The action in this audit category from grouper_audit_type",
               "The subject id of the logged in subject, e.g. from WS or UI",
               "The subject id of the user using the system if they are acting as another user, e.g. from WS",
               "The label of the string field 01 from grouper_audit_type", "The string 01 value",
               "The label of the string field 02 from grouper_audit_type", "The string 02 value",
               "The label of the string field 03 from grouper_audit_type", "The string 03 value",
               "The label of the string field 04 from grouper_audit_type", "The string 04 value",
               "The label of the string field 05 from grouper_audit_type", "The string 05 value",
               "The label of the string field 06 from grouper_audit_type", "The string 06 value",
               "The label of the string field 07 from grouper_audit_type", "The string 07 value",
               "The label of the string field 08 from grouper_audit_type", "The string 08 value",
               "The label of the int field 01 from grouper_audit_type", "The int 01 value",
               "The label of the int field 02 from grouper_audit_type", "The int 02 value",
               "The label of the int field 03 from grouper_audit_type", "The int 03 value",
               "The label of the int field 04 from grouper_audit_type", "The int 04 value",
               "The label of the int field 05 from grouper_audit_type", "The int 05 value",
               "Context id links together multiple operations into one high level action",
               "Grouper engine is e.g. UI, WS, GSH, loader, etc",
               "Description is a sentence form expression of what is being audited",
               "Source id of the user who is logged in",
               "Source id of the user who is being acted as (e.g. in WS)",
               "Member id (foreign key) of the user logged in",
               "Member id (foreign key) of the user who is being acted as",
               "ID of the audit type row",
               "IP address of the user connecting to the system (e.g. from UI or WS)",
               "Host of the system running the grouper API",
               "When this audit entry was last updated", "ID of this audit entry", "Grouper version of the API executing", 
               "environment label of the system running, from grouper.properties"),
               "select gae.created_on, gat.audit_category, gat.action_name, "
               + "(select gm.subject_id from grouper_members gm where gm.id = gae.logged_in_member_id) as logged_in_subject_id, "
               + "(select gm.subject_id from grouper_members gm where gm.id = gae.act_as_member_id) as act_as_subject_id, "
               + "gat.label_string01, gae.string01, "
               + "gat.label_string02, gae.string02, "
               + "gat.label_string03, gae.string03, "
               + "gat.label_string04, gae.string04, "
               + "gat.label_string05, gae.string05, "
               + "gat.label_string06, gae.string06, "
               + "gat.label_string07, gae.string07, "
               + "gat.label_string08, gae.string08, "
               + "gat.label_int01, gae.int01, "
               + "gat.label_int02, gae.int02, "
               + "gat.label_int03, gae.int03, "
               + "gat.label_int04, gae.int04, "
               + "gat.label_int05, gae.int05, "
               + "gae.context_id, "
               + "gae.grouper_engine, "
               + "gae.description, "
               + "(select gm.subject_source from grouper_members gm where gm.id = gae.logged_in_member_id) as logged_in_source_id, "
               + "(select gm.subject_source from grouper_members gm where gm.id = gae.act_as_member_id) as act_as_source_id, "
               + "gae.logged_in_member_id, gae.act_as_member_id, "
               + "gat.id as audit_type_id, "
               + "gae.user_ip_address, gae.server_host, "
               + "gae.last_updated, gae.id as audit_entry_id, gae.grouper_version, gae.env_name "
               + "from grouper_audit_type gat, grouper_audit_entry gae "
               + "where gat.id = gae.audit_type_id ");
    }
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attributes_v",
        "Join of groups and attributes with friendly names.  Attributes are name/value pairs for groups.  " +
        "Each group type is related to a set of 0 to many attributes, each attribute is related to one group type." +
        "grouper_fields holds each attribute name under the field type of ^attribute^",
        GrouperUtil.toSet("GROUP_NAME", 
            "GROUP_DISPLAY_NAME", 
            "ATTRIBUTE_NAME", 
            "ATTRIBUTE_VALUE", 
            "GROUP_TYPE_NAME", 
            "FIELD_ID", 
            "ATTRIBUTE_ID", 
            "GROUP_ID", 
            "GROUPTYPE_UUID",
            "CONTEXT_ID"),
         GrouperUtil.toSet("Group name is full id path, e.g. school:stem1:groupId",
             "Group display name is the full friendly name, e.g. My School:Stem 1:The Group",
             "Attribute name is the name of the name/value pair",
             "Attribute value is the value of the name/value pair",
             "Group_type_name is the name of the group type this attribute is related to",
             "Field_id is the uuid that uniquely identifies a the field",
             "Attribute_id is the uuid that uniquely identifies the pairing of group and attribute",
             "Group_id is the uuid that uniquely identifies a group",
             "GroupType_uuid is the uuid that uniquely identifies a group type",
             "Context id links together multiple operations into one high level action"),
            "select  "
            + "gg.name as group_name, "
            + "gg.display_name as group_display_name, "
            + "gf.NAME as attribute_name, "
            + "ga.VALUE as attribute_value, "
            + "gt.NAME as group_type_name, "
            + "ga.FIELD_ID, "
            + "ga.ID as attribute_id, "
            + "gg.ID as group_id, "
            + "gf.grouptype_uuid, ga.context_id "
            + "from grouper_attributes ga, grouper_groups gg, grouper_fields gf, grouper_types gt "
            + "where ga.FIELD_ID = gf.ID "
            + "and ga.GROUP_ID = gg.ID and gf.GROUPTYPE_UUID = gt.ID ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_composites_v", 
        "Grouper_composites_v is a view of composite relationships with friendly names.  A composite" +
        " is a joining of two groups with a group math operator of: union, intersection, or complement.",
        GrouperUtil.toSet("OWNER_GROUP_NAME", 
            "COMPOSITE_TYPE", 
            "LEFT_FACTOR_GROUP_NAME", 
            "RIGHT_FACTOR_GROUP_NAME", 
            "OWNER_GROUP_DISPLAYNAME",
            "LEFT_FACTOR_GROUP_DISPLAYNAME", 
            "RIGHT_FACTOR_GROUP_DISPLAYNAME", 
            Membership.COLUMN_OWNER_GROUP_ID, 
            "LEFT_FACTOR_GROUP_ID", 
            "RIGHT_FACTOR_GROUP_ID",
            "COMPOSITE_ID", 
            "CREATE_TIME", 
            "CREATOR_ID", 
            "HIBERNATE_VERSION_NUMBER",
            "CONTEXT_ID"),
        GrouperUtil.toSet("OWNER_GROUP_NAME: Name of the group which is the result of the composite operation, e.g. school:stem1:allPeople",
            "COMPOSITE_TYPE: union (all members), intersection (only members in both), or complement (in first, not in second)", 
            "LEFT_FACTOR_GROUP_NAME: Name of group which is the first of two groups in the composite operation, e.g. school:stem1:part1", 
            "RIGHT_FACTOR_GROUP_NAME: Name of group which is the second of two groups in the composite operation, e.g. school:stem1:part2", 
            "OWNER_GROUP_DISPLAYNAME: Display name of result group of composite operation, e.g. My school:The stem1:All people",
            "LEFT_FACTOR_GROUP_DISPLAYNAME: Display name of group which is the first of two groups in the composite operation, e.g. My school:The stem1:Part 1", 
            "RIGHT_FACTOR_GROUP_DISPLAYNAME: Display name of group which is the second of two groups in the composite operation, e.g. My school:The stem1:Part 1", 
            "OWNER_GROUP_ID: UUID of the result group", 
            "LEFT_FACTOR_GROUP_ID: UUID of the first group of the composite operation", 
            "RIGHT_FACTOR_GROUP_ID: UUID of the second group of the composite operation",
            "COMPOSITE_ID: UUID of the composite relationship among the three groups", 
            "CREATE_TIME: number of millis since 1970 that the composite was created", 
            "CREATOR_ID: member id of the subject that created the composite relationship", 
            "HIBERNATE_VERSION_NUMBER: increments with each update, starts at 0",
            "CONTEXT_ID: Context id links together multiple operations into one high level action"
        ),
        "select  "
        + "(select gg.name from grouper_groups gg  "
        + "where gg.id = gc.owner) as owner_group_name,  "
        + "gc.TYPE as composite_type,  "
        + "(select gg.name from grouper_groups gg  "
        + "where gg.id =  gc.left_factor) as left_factor_group_name,  "
        + "(select gg.name from grouper_groups gg  "
        + "where gg.id = gc.right_factor) as right_factor_group_name,  "
        + "(select gg.display_name from grouper_groups gg  "
        + "where gg.id = gc.owner) as owner_group_displayname,  "
        + "(select gg.display_name from grouper_groups gg  "
        + "where gg.id = gc.left_factor) as left_factor_group_displayname,  "
        + "(select gg.display_name from grouper_groups gg  "
        + "where gg.id = gc.right_factor) as right_factor_group_displayname,  "
        + "gc.OWNER as owner_group_id,  "
        + "gc.LEFT_FACTOR as left_factor_group_id,  "
        + "gc.RIGHT_FACTOR as right_factor_group_id,  "
        + "gc.ID as composite_id,  "
        + "gc.CREATE_TIME,  "
        + "gc.CREATOR_ID,  "
        + "gc.HIBERNATE_VERSION_NUMBER, gc.context_id "
        + "from grouper_composites gc  "
      );
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_groups_types_v", 
        "A group can have one or many types associated.  This is a view of those relationships with friendly names",
        GrouperUtil.toSet("GROUP_NAME", 
            "GROUP_DISPLAYNAME", 
            "GROUP_TYPE_NAME", 
            "GROUP_ID", 
            "GROUP_TYPE_UUID", 
            "GROUPER_GROUPS_TYPES_ID", 
            "HIBERNATE_VERSION_NUMBER",
            "CONTEXT_ID"),
        GrouperUtil.toSet("GROUP_NAME: name of group which has the type, e.g. school:stem1:theGroup", 
            "GROUP_DISPLAYNAME: display name of the group which has the type, e.g. My school, the stem 1, The group", 
            "GROUP_TYPE_NAME: friendly name of the type, e.g. grouperLoader", 
            "GROUP_ID: uuid unique id of the group which has the type", 
            "GROUP_TYPE_UUID: uuid unique id of the type related to the group", 
            "GROUPER_GROUPS_TYPES_ID: uuid unique id of the relationship between the group and type", 
            "HIBERNATE_VERSION_NUMBER: increments by one with each update, starts at 0",
            "Context id links together multiple operations into one high level action"),
            "select   "
            + "(select gg.name from grouper_groups gg  "
            + "where gg.id = ggt.GROUP_UUID) as group_name,  "
            + "(select gg.display_name from grouper_groups gg  "
            + "where gg.id = ggt.GROUP_UUID) as group_displayname,  "
            + "gt.NAME as group_type_name,  "
            + "ggt.GROUP_UUID as group_id,  "
            + "ggt.TYPE_UUID as group_type_uuid,  "
            + "ggt.ID as grouper_groups_types_id,  "
            + "ggt.HIBERNATE_VERSION_NUMBER, ggt.context_id  "
            + "from grouper_groups_types ggt, grouper_types gt  "
            + "where ggt.TYPE_UUID = gt.ID  ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_groups_v", 
        "Contains one record for each group, with friendly names for some attributes and some more information",
        GrouperUtil.toSet("EXTENSION", 
            "NAME", 
            "DISPLAY_EXTENSION", 
            "DISPLAY_NAME", 
            "DESCRIPTION", 
            "PARENT_STEM_NAME", 
            "TYPE_OF_GROUP", 
            "GROUP_ID", 
            "PARENT_STEM_ID", 
            "MODIFIER_SOURCE", 
            "MODIFIER_SUBJECT_ID", 
            "CREATOR_SOURCE", 
            "CREATOR_SUBJECT_ID", 
            "IS_COMPOSITE_OWNER", 
            "IS_COMPOSITE_FACTOR", 
            "CREATOR_ID", 
            "CREATE_TIME", 
            "MODIFIER_ID", 
            "MODIFY_TIME", 
            "HIBERNATE_VERSION_NUMBER", "CONTEXT_ID"),
        GrouperUtil.toSet("EXTENSION: part of group name not including path information, e.g. theGroup", 
            "NAME: name of the group, e.g. school:stem1:theGroup", 
            "DISPLAY_EXTENSION: name for display of the group, e.g. My school:The stem 1:The group", 
            "DISPLAY_NAME: name for display of the group without any path information, e.g. The group", 
            "DESCRIPTION: contains user entered information about the group e.g. why it exists", 
            "PARENT_STEM_NAME: name of the stem this group is in, e.g. school:stem1", 
            "TYPE_OF_GROUP: group if it is a group, role if it is a role", 
            "GROUP_ID: uuid unique id of the group", 
            "PARENT_STEM_ID: uuid unique id of the stem this group is in", 
            "MODIFIER_SOURCE: source name of the subject who last modified this group, e.g. schoolPersonSource", 
            "MODIFIER_SUBJECT_ID: subject id of the subject who last modified this group, e.g. 12345", 
            "CREATOR_SOURCE: source name of the subject who created this group, e.g. schoolPersonSource", 
            "CREATOR_SUBJECT_ID: subject id of the subject who created this group, e.g. 12345", 
            "IS_COMPOSITE_OWNER: T if this is a result of a composite operation (union, intersection, complement), or blank if not", 
            "IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemeneted", 
            "CREATOR_ID: member id of the subject who created this group, foreign key to grouper_members", 
            "CREATE_TIME: number of millis since 1970 since this group was created", 
            "MODIFIER_ID: member id of the subject who last modified this group, foreign key to grouper_members", 
            "MODIFY_TIME: number of millis since 1970 since this group was last changed", 
            "HIBERNATE_VERSION_NUMBER: increments by 1 for each update",
            "Context id links together multiple operations into one high level action"),
            "select  "
            + "gg.extension as extension, "
            + "gg.name as name, "
            + "gg.display_extension as display_extension, "
            + "gg.display_name as display_name, "
            + "gg.description as description, "
            + "gs.NAME as parent_stem_name, "
            + "gg.type_of_group, "
            + "gg.id as group_id, "
            + "gs.ID as parent_stem_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, "
            + "(select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, "
            + "(select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, "
            + "gg.CREATOR_ID, "
            + "gg.CREATE_TIME, "
            + "gg.MODIFIER_ID, "
            + "gg.MODIFY_TIME, "
            + "gg.HIBERNATE_VERSION_NUMBER, gg.context_id  "
            + " from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_roles_v", 
        "Contains one record for each role, with friendly names for some attributes and some more information",
        GrouperUtil.toSet("EXTENSION", 
            "NAME", 
            "DISPLAY_EXTENSION", 
            "DISPLAY_NAME", 
            "DESCRIPTION", 
            "PARENT_STEM_NAME", 
            "ROLE_ID", 
            "PARENT_STEM_ID", 
            "MODIFIER_SOURCE", 
            "MODIFIER_SUBJECT_ID", 
            "CREATOR_SOURCE", 
            "CREATOR_SUBJECT_ID", 
            "IS_COMPOSITE_OWNER", 
            "IS_COMPOSITE_FACTOR", 
            "CREATOR_ID", 
            "CREATE_TIME", 
            "MODIFIER_ID", 
            "MODIFY_TIME", 
            "HIBERNATE_VERSION_NUMBER", "CONTEXT_ID"),
        GrouperUtil.toSet("EXTENSION: part of role name not including path information, e.g. theRole", 
            "NAME: name of the role, e.g. school:stem1:theRole", 
            "DISPLAY_EXTENSION: name for display of the role, e.g. My school:The stem 1:The role", 
            "DISPLAY_NAME: name for display of the role without any path information, e.g. The role", 
            "DESCRIPTION: contains user entered information about the group e.g. why it exists", 
            "PARENT_STEM_NAME: name of the stem this role is in, e.g. school:stem1", 
            "ROLE_ID: uuid unique id of the role", 
            "PARENT_STEM_ID: uuid unique id of the stem this role is in", 
            "MODIFIER_SOURCE: source name of the subject who last modified this role, e.g. schoolPersonSource", 
            "MODIFIER_SUBJECT_ID: subject id of the subject who last modified this role, e.g. 12345", 
            "CREATOR_SOURCE: source name of the subject who created this role, e.g. schoolPersonSource", 
            "CREATOR_SUBJECT_ID: subject id of the subject who created this role, e.g. 12345", 
            "IS_COMPOSITE_OWNER: T if this is a result of a composite operation (union, intersection, complement), or blank if not", 
            "IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemeneted", 
            "CREATOR_ID: member id of the subject who created this role, foreign key to grouper_members", 
            "CREATE_TIME: number of millis since 1970 since this role was created", 
            "MODIFIER_ID: member id of the subject who last modified this role, foreign key to grouper_members", 
            "MODIFY_TIME: number of millis since 1970 since this role was last changed", 
            "HIBERNATE_VERSION_NUMBER: increments by 1 for each update",
            "Context id links together multiple operations into one high level action"),
            "select  "
            + "gg.extension as extension, "
            + "gg.name as name, "
            + "gg.display_extension as display_extension, "
            + "gg.display_name as display_name, "
            + "gg.description as description, "
            + "gs.NAME as parent_stem_name, "
            + "gg.id as role_id, "
            + "gs.ID as parent_stem_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, "
            + "(select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, "
            + "(select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, "
            + "gg.CREATOR_ID, "
            + "gg.CREATE_TIME, "
            + "gg.MODIFIER_ID, "
            + "gg.MODIFY_TIME, "
            + "gg.HIBERNATE_VERSION_NUMBER, gg.context_id  "
            + " from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and" +
                " type_of_group = 'role' ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_memberships_all_v", 
        "Grouper_memberships_all_v holds one record for each immediate, composite and effective membership or privilege in the system for members to groups or stems (for privileges).",
        GrouperUtil.toSet("MEMBERSHIP_ID", 
        		"IMMEDIATE_MEMBERSHIP_ID", 
            "GROUP_SET_ID", 
            "MEMBER_ID", 
            "FIELD_ID", 
            "IMMEDIATE_FIELD_ID", 
            "OWNER_ATTR_DEF_ID", 
            "OWNER_GROUP_ID", 
            "OWNER_STEM_ID", 
            "VIA_GROUP_ID",
            "VIA_COMPOSITE_ID",
            "DEPTH", 
            "MSHIP_TYPE", 
            "IMMEDIATE_MSHIP_ENABLED",
            "IMMEDIATE_MSHIP_ENABLED_TIME",
            "IMMEDIATE_MSHIP_DISABLED_TIME",
            "GROUP_SET_PARENT_ID", 
            "MEMBERSHIP_CREATOR_ID", 
            "MEMBERSHIP_CREATE_TIME",
            "GROUP_SET_CREATOR_ID", 
            "GROUP_SET_CREATE_TIME", 
            "HIBERNATE_VERSION_NUMBER", 
            "CONTEXT_ID"),
        GrouperUtil.toSet("MEMBERSHIP_ID: uuid unique id of this membership", 
        		"IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate (or composite) membership that causes this membership", 
            "GROUP_SET_ID: uuid of the group set that causes this membership", 
            "MEMBER_ID: id in the grouper_members table", 
            "FIELD_ID: id in the grouper_fields table", 
            "IMMEDIATE_FIELD_ID: id in the grouper_fields table for the immediate (or composite) membership that causes this membership", 
            "OWNER_ATTR_DEF_ID: owner attribute def id if applicable", 
            "OWNER_GROUP_ID: owner group if applicable", 
            "OWNER_STEM_ID: owner stem if applicable", 
            "VIA_GROUP_ID: membership is due to this group if effective",
            "VIA_COMPOSITE_ID: membership is due to this composite if applicable",
            "DEPTH: number of hops in a directed graph", 
            "MSHIP_TYPE: type of membership, immediate or effective or composite", 
            "IMMEDIATE_MSHIP_ENABLED: T or F to indicate if this membership is enabled",
            "IMMEDIATE_MSHIP_ENABLED_TIME: when the membership will be enabled if the time is in the future",
            "IMMEDIATE_MSHIP_DISABLED_TIME: when the membership will be disabled if the time is in the future.",
            "GROUP_SET_PARENT_ID: parent group set", 
            "MEMBERSHIP_CREATOR_ID: member uuid of the creator of the immediate or composite membership", 
            "MEMBERSHIP_CREATOR_TIME: number of millis since 1970 the immedate or composite membership was created",
            "GROUP_SET_CREATOR_ID: member uuid of the creator of the group set", 
            "GROUP_SET_CREATE_TIME: number of millis since 1970 the group set was created", 
            "HIBERNATE_VERSION_NUMBER: hibernate uses this to version rows", 
            "CONTEXT_ID: Context id links together multiple operations into one high level action"),
            "select "
            + GrouperDdlUtils.sqlConcatenation("ms.id", "gs.id", Membership.membershipIdSeparator) + " as membership_id, "
            + "ms.id as immediate_membership_id, "
            + "gs.id as group_set_id, "
            + "ms.member_id, "
            + "gs.field_id, "
            + "ms.field_id, "
            + "gs.owner_attr_def_id, "
            + "gs.owner_group_id, "
            + "gs.owner_stem_id, " 
            + "gs.via_group_id, " 
            + "ms.via_composite_id, " 
            + "gs.depth, " 
            + "gs.mship_type, " 
            + "ms.enabled, " 
            + "ms.enabled_timestamp, " 
            + "ms.disabled_timestamp, "
            + "gs.parent_id as group_set_parent_id, "
            + "ms.creator_id as membership_creator_id, "
            + "ms.create_time as membership_create_time, "
            + "gs.creator_id as group_set_creator_id, "
            + "gs.create_time as group_set_create_time, "
            + "ms.hibernate_version_number, "
            + "ms.context_id "
            + "from grouper_memberships ms, grouper_group_set gs, grouper_fields f1 "
            + "where (ms.owner_stem_id = gs.member_stem_id or ms.owner_group_id = gs.member_group_id" +
            		" or ms.owner_attr_def_id = gs.member_attr_def_id) and " 
            + "      f1.id = ms.field_id and "
            + "      ((ms.field_id = gs.field_id and gs.depth = '0') or (f1.name='members' and f1.type='list' and gs.mship_type = 'effective'))");


    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_memberships_group_v", 
        "grouper_memberships_group_v is a join between grouper_group_set and grouper_memberships for group based memberships.  It is not a complete join in that it does not join fields together.",
        GrouperUtil.toSet("MEMBERSHIP_ID", 
            "IMMEDIATE_MEMBERSHIP_ID", 
            "GROUP_SET_ID", 
            "MEMBER_ID", 
            "FIELD_ID", 
            "IMMEDIATE_FIELD_ID", 
            "OWNER_ATTR_DEF_ID", 
            "OWNER_GROUP_ID", 
            "OWNER_STEM_ID", 
            "VIA_GROUP_ID",
            "VIA_COMPOSITE_ID",
            "DEPTH", 
            "MSHIP_TYPE", 
            "IMMEDIATE_MSHIP_ENABLED",
            "IMMEDIATE_MSHIP_ENABLED_TIME",
            "IMMEDIATE_MSHIP_DISABLED_TIME",
            "GROUP_SET_PARENT_ID", 
            "MEMBERSHIP_CREATOR_ID", 
            "MEMBERSHIP_CREATE_TIME",
            "GROUP_SET_CREATOR_ID", 
            "GROUP_SET_CREATE_TIME", 
            "HIBERNATE_VERSION_NUMBER", 
            "CONTEXT_ID"),
        GrouperUtil.toSet("MEMBERSHIP_ID: uuid unique id of this membership", 
            "IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate (or composite) membership that causes this membership", 
            "GROUP_SET_ID: uuid of the group set that causes this membership", 
            "MEMBER_ID: id in the grouper_members table", 
            "FIELD_ID: id in the grouper_fields table", 
            "IMMEDIATE_FIELD_ID: id in the grouper_fields table for the immediate (or composite) membership that causes this membership", 
            "OWNER_ATTR_DEF_ID: owner attribute def id if applicable", 
            "OWNER_GROUP_ID: owner group if applicable", 
            "OWNER_STEM_ID: owner stem if applicable", 
            "VIA_GROUP_ID: membership is due to this group if effective",
            "VIA_COMPOSITE_ID: membership is due to this composite if applicable",
            "DEPTH: number of hops in a directed graph", 
            "MSHIP_TYPE: type of membership, immediate or effective or composite", 
            "IMMEDIATE_MSHIP_ENABLED: T or F to indicate if this membership is enabled",
            "IMMEDIATE_MSHIP_ENABLED_TIME: when the membership will be enabled if the time is in the future",
            "IMMEDIATE_MSHIP_DISABLED_TIME: when the membership will be disabled if the time is in the future.",
            "GROUP_SET_PARENT_ID: parent group set", 
            "MEMBERSHIP_CREATOR_ID: member uuid of the creator of the immediate or composite membership", 
            "MEMBERSHIP_CREATOR_TIME: number of millis since 1970 the immedate or composite membership was created",
            "GROUP_SET_CREATOR_ID: member uuid of the creator of the group set", 
            "GROUP_SET_CREATE_TIME: number of millis since 1970 the group set was created", 
            "HIBERNATE_VERSION_NUMBER: hibernate uses this to version rows", 
            "CONTEXT_ID: Context id links together multiple operations into one high level action"),
            "select "
            + GrouperDdlUtils.sqlConcatenation("ms.id", "gs.id", Membership.membershipIdSeparator) + " as membership_id, "
            + "ms.id as immediate_membership_id, "
            + "gs.id as group_set_id, "
            + "ms.member_id, "
            + "gs.field_id, "
            + "ms.field_id, "
            + "gs.owner_attr_def_id, "
            + "gs.owner_group_id, "
            + "gs.owner_stem_id, " 
            + "gs.via_group_id, " 
            + "ms.via_composite_id, " 
            + "gs.depth, " 
            + "gs.mship_type, " 
            + "ms.enabled, " 
            + "ms.enabled_timestamp, " 
            + "ms.disabled_timestamp, "
            + "gs.parent_id as group_set_parent_id, "
            + "ms.creator_id as membership_creator_id, "
            + "ms.create_time as membership_create_time, "
            + "gs.creator_id as group_set_creator_id, "
            + "gs.create_time as group_set_create_time, "
            + "ms.hibernate_version_number, "
            + "ms.context_id "
            + "from grouper_memberships ms, grouper_group_set gs "
            + "where ms.owner_group_id = gs.member_group_id");
    

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_memberships_stem_v", 
        "grouper_memberships_stem_v is a join between grouper_group_set and grouper_memberships for stem based memberships.  It is not a complete join in that it does not join fields together.",
        GrouperUtil.toSet("MEMBERSHIP_ID", 
            "IMMEDIATE_MEMBERSHIP_ID", 
            "GROUP_SET_ID", 
            "MEMBER_ID", 
            "FIELD_ID", 
            "IMMEDIATE_FIELD_ID", 
            "OWNER_ATTR_DEF_ID", 
            "OWNER_GROUP_ID", 
            "OWNER_STEM_ID", 
            "VIA_GROUP_ID",
            "VIA_COMPOSITE_ID",
            "DEPTH", 
            "MSHIP_TYPE", 
            "IMMEDIATE_MSHIP_ENABLED",
            "IMMEDIATE_MSHIP_ENABLED_TIME",
            "IMMEDIATE_MSHIP_DISABLED_TIME",
            "GROUP_SET_PARENT_ID", 
            "MEMBERSHIP_CREATOR_ID", 
            "MEMBERSHIP_CREATE_TIME",
            "GROUP_SET_CREATOR_ID", 
            "GROUP_SET_CREATE_TIME", 
            "HIBERNATE_VERSION_NUMBER", 
            "CONTEXT_ID"),
        GrouperUtil.toSet("MEMBERSHIP_ID: uuid unique id of this membership", 
            "IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate (or composite) membership that causes this membership", 
            "GROUP_SET_ID: uuid of the group set that causes this membership", 
            "MEMBER_ID: id in the grouper_members table", 
            "FIELD_ID: id in the grouper_fields table", 
            "IMMEDIATE_FIELD_ID: id in the grouper_fields table for the immediate (or composite) membership that causes this membership", 
            "OWNER_ATTR_DEF_ID: owner attribute def id if applicable", 
            "OWNER_GROUP_ID: owner group if applicable", 
            "OWNER_STEM_ID: owner stem if applicable", 
            "VIA_GROUP_ID: membership is due to this group if effective",
            "VIA_COMPOSITE_ID: membership is due to this composite if applicable",
            "DEPTH: number of hops in a directed graph", 
            "MSHIP_TYPE: type of membership, immediate or effective or composite", 
            "IMMEDIATE_MSHIP_ENABLED: T or F to indicate if this membership is enabled",
            "IMMEDIATE_MSHIP_ENABLED_TIME: when the membership will be enabled if the time is in the future",
            "IMMEDIATE_MSHIP_DISABLED_TIME: when the membership will be disabled if the time is in the future.",
            "GROUP_SET_PARENT_ID: parent group set", 
            "MEMBERSHIP_CREATOR_ID: member uuid of the creator of the immediate or composite membership", 
            "MEMBERSHIP_CREATOR_TIME: number of millis since 1970 the immedate or composite membership was created",
            "GROUP_SET_CREATOR_ID: member uuid of the creator of the group set", 
            "GROUP_SET_CREATE_TIME: number of millis since 1970 the group set was created", 
            "HIBERNATE_VERSION_NUMBER: hibernate uses this to version rows", 
            "CONTEXT_ID: Context id links together multiple operations into one high level action"),
            "select "
            + GrouperDdlUtils.sqlConcatenation("ms.id", "gs.id", Membership.membershipIdSeparator) + " as membership_id, "
            + "ms.id as immediate_membership_id, "
            + "gs.id as group_set_id, "
            + "ms.member_id, "
            + "gs.field_id, "
            + "ms.field_id, "
            + "gs.owner_attr_def_id, "
            + "gs.owner_group_id, "
            + "gs.owner_stem_id, " 
            + "gs.via_group_id, " 
            + "ms.via_composite_id, " 
            + "gs.depth, " 
            + "gs.mship_type, " 
            + "ms.enabled, " 
            + "ms.enabled_timestamp, " 
            + "ms.disabled_timestamp, "
            + "gs.parent_id as group_set_parent_id, "
            + "ms.creator_id as membership_creator_id, "
            + "ms.create_time as membership_create_time, "
            + "gs.creator_id as group_set_creator_id, "
            + "gs.create_time as group_set_create_time, "
            + "ms.hibernate_version_number, "
            + "ms.context_id "
            + "from grouper_memberships ms, grouper_group_set gs "
            + "where ms.owner_stem_id = gs.member_stem_id");
    

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_memberships_attr_def_v", 
        "grouper_memberships_attr_def_v is a join between grouper_group_set and grouper_memberships for attribute based memberships.  It is not a complete join in that it does not join fields together.",
        GrouperUtil.toSet("MEMBERSHIP_ID", 
            "IMMEDIATE_MEMBERSHIP_ID", 
            "GROUP_SET_ID", 
            "MEMBER_ID", 
            "FIELD_ID", 
            "IMMEDIATE_FIELD_ID", 
            "OWNER_ATTR_DEF_ID", 
            "OWNER_GROUP_ID", 
            "OWNER_STEM_ID", 
            "VIA_GROUP_ID",
            "VIA_COMPOSITE_ID",
            "DEPTH", 
            "MSHIP_TYPE", 
            "IMMEDIATE_MSHIP_ENABLED",
            "IMMEDIATE_MSHIP_ENABLED_TIME",
            "IMMEDIATE_MSHIP_DISABLED_TIME",
            "GROUP_SET_PARENT_ID", 
            "MEMBERSHIP_CREATOR_ID", 
            "MEMBERSHIP_CREATE_TIME",
            "GROUP_SET_CREATOR_ID", 
            "GROUP_SET_CREATE_TIME", 
            "HIBERNATE_VERSION_NUMBER", 
            "CONTEXT_ID"),
        GrouperUtil.toSet("MEMBERSHIP_ID: uuid unique id of this membership", 
            "IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate (or composite) membership that causes this membership", 
            "GROUP_SET_ID: uuid of the group set that causes this membership", 
            "MEMBER_ID: id in the grouper_members table", 
            "FIELD_ID: id in the grouper_fields table", 
            "IMMEDIATE_FIELD_ID: id in the grouper_fields table for the immediate (or composite) membership that causes this membership", 
            "OWNER_ATTR_DEF_ID: owner attribute def id if applicable", 
            "OWNER_GROUP_ID: owner group if applicable", 
            "OWNER_STEM_ID: owner stem if applicable", 
            "VIA_GROUP_ID: membership is due to this group if effective",
            "VIA_COMPOSITE_ID: membership is due to this composite if applicable",
            "DEPTH: number of hops in a directed graph", 
            "MSHIP_TYPE: type of membership, immediate or effective or composite", 
            "IMMEDIATE_MSHIP_ENABLED: T or F to indicate if this membership is enabled",
            "IMMEDIATE_MSHIP_ENABLED_TIME: when the membership will be enabled if the time is in the future",
            "IMMEDIATE_MSHIP_DISABLED_TIME: when the membership will be disabled if the time is in the future.",
            "GROUP_SET_PARENT_ID: parent group set", 
            "MEMBERSHIP_CREATOR_ID: member uuid of the creator of the immediate or composite membership", 
            "MEMBERSHIP_CREATOR_TIME: number of millis since 1970 the immedate or composite membership was created",
            "GROUP_SET_CREATOR_ID: member uuid of the creator of the group set", 
            "GROUP_SET_CREATE_TIME: number of millis since 1970 the group set was created", 
            "HIBERNATE_VERSION_NUMBER: hibernate uses this to version rows", 
            "CONTEXT_ID: Context id links together multiple operations into one high level action"),
            "select "
            + GrouperDdlUtils.sqlConcatenation("ms.id", "gs.id", Membership.membershipIdSeparator) + " as membership_id, "
            + "ms.id as immediate_membership_id, "
            + "gs.id as group_set_id, "
            + "ms.member_id, "
            + "gs.field_id, "
            + "ms.field_id, "
            + "gs.owner_attr_def_id, "
            + "gs.owner_group_id, "
            + "gs.owner_stem_id, " 
            + "gs.via_group_id, " 
            + "ms.via_composite_id, " 
            + "gs.depth, " 
            + "gs.mship_type, " 
            + "ms.enabled, " 
            + "ms.enabled_timestamp, " 
            + "ms.disabled_timestamp, "
            + "gs.parent_id as group_set_parent_id, "
            + "ms.creator_id as membership_creator_id, "
            + "ms.create_time as membership_create_time, "
            + "gs.creator_id as group_set_creator_id, "
            + "gs.create_time as group_set_create_time, "
            + "ms.hibernate_version_number, "
            + "ms.context_id "
            + "from grouper_memberships ms, grouper_group_set gs "
            + "where ms.owner_attr_def_id = gs.member_attr_def_id");

    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_memberships_lw_v", 
        "Grouper_memberships_lw_v unique membership records that can be read from a SQL interface outside of grouper.  Immediate and effective memberships are represented here (distinct)",
        GrouperUtil.toSet("SUBJECT_ID", "SUBJECT_SOURCE", "GROUP_NAME", "LIST_NAME", "LIST_TYPE", "GROUP_ID"),
        GrouperUtil.toSet("SUBJECT_ID: of the member of the group", 
            "SUBJECT_SOURCE: of the member of the group", 
            "GROUP_NAME: system name of the group", 
            "LIST_NAME: name of the list, e.g. members", 
            "LIST_TYPE: type of list e.g. access or list", 
            "GROUP_ID: uuid of the group"),
            "select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gg.name as group_name, "
            + "gfl.NAME as list_name, gfl.TYPE as list_type, gg.ID as group_id "
            + "from grouper_memberships_all_v gms, grouper_members gm, " 
            + "grouper_groups gg, grouper_fields gfl "
            + "where gms.OWNER_GROUP_ID = gg.id " 
            + "and gms.FIELD_ID = gfl.ID "
            + "and gms.MEMBER_ID = gm.ID");

        
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_memberships_v", 
        "Grouper_memberships_v holds one record for each membership or privilege in the system for members to groups or stems (for privileges).  This is denormalized so there are records for the actual immediate relationships, and the cascaded effective relationships.  This has friendly names.",
        GrouperUtil.toSet("GROUP_NAME", 
            "GROUP_DISPLAYNAME", 
            "STEM_NAME", 
            "STEM_DISPLAYNAME", 
            "SUBJECT_ID", 
            "SUBJECT_SOURCE", 
            "MEMBER_ID",
            "LIST_TYPE", 
            "LIST_NAME", 
            "MEMBERSHIP_TYPE", 
            "COMPOSITE_PARENT_GROUP_NAME", 
            "DEPTH", 
            "CREATOR_SOURCE", 
            "CREATOR_SUBJECT_ID", 
            "MEMBERSHIP_ID", 
            "IMMEDIATE_MEMBERSHIP_ID", 
            "GROUP_SET_ID", 
            "STEM_ID", 
            "GROUP_ID", 
            "CREATE_TIME", 
            "CREATOR_ID", 
            "FIELD_ID", "CONTEXT_ID"),
        GrouperUtil.toSet("GROUP_NAME: name of the group if this is a group membership, e.g. school:stem1:theGroup", 
            "GROUP_DISPLAYNAME: display name of the group if this is a group membership, e.g. My school:The stem1:The group", 
            "STEM_NAME: name of the stem if this is a stem privilege, e.g. school:stem1", 
            "STEM_DISPLAYNAME: display name of the stems if this is a stem privilege, e.g. My school:The stem1", 
            "SUBJECT_ID: e.g. a school id of a person in the membership e.g. 12345", 
            "SUBJECT_SOURCE: source where the subject in the membership is from e.g. mySchoolPeople", 
            "MEMBER_ID: id in the grouper_members table", 
            "LIST_TYPE: list: members of a group, access: privilege of a group, naming: privilege of a stem", 
            "LIST_NAME: subset of list type.  which list if a list membership.  which privilege if a privilege.  e.g. members", 
            "MEMBERSHIP_TYPE: either immediate (direct membership or privilege), of effective (membership due to a composite or a group being a member of another group)",
            "COMPOSITE_PARENT_GROUP_NAME: name of group if this membership relates to a composite relationship, e.g. school:stem:allStudents", 
            "DEPTH: 0 for composite, if not then it is the 0 indexed count of number of group hops between member and group", 
            "CREATOR_SOURCE: subject source where the creator of the group is from", 
            "CREATOR_SUBJECT_ID: subject id of the creator of the group, e.g. 12345", 
            "MEMBERSHIP_ID: uuid unique id of this membership", 
            "IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate membership that causes this membership", 
            "GROUP_SET_ID: uuid of the group set that causes this membership", 
            "STEM_ID: if this is a stem privilege, this is the stem uuid unique id", 
            "GROUP_ID: if this is a group list or privilege, this is the group uuid unique id", 
            "CREATE_TIME: number of millis since 1970 since this membership was created", 
            "CREATOR_ID: member_id of the creator, foreign key into grouper_members", 
            "FIELD_ID: uuid unique id of the field.  foreign key to grouper_fields.  This represents the list_type and list_name",
            "CONTEXT_ID: Context id links together multiple operations into one high level action"),
            "select  "
            + "(select gg.name from grouper_groups gg  "
            + "where gg.id = gms.owner_group_id) as group_name,  "
            + "(select gg.display_name from grouper_groups gg  "
            + "where gg.id = gms.owner_group_id) as group_displayname,  "
            + "(select gs.NAME from grouper_stems gs  "
            + "where gs.ID = gms.owner_stem_id) as stem_name,  "
            + "(select gs.display_NAME from grouper_stems gs  "
            + "where gs.ID = gms.owner_stem_id) as stem_displayname,  "
            + "gm.subject_id, gm.subject_source, gms.member_id, "
            + "gf.TYPE as list_type,  "
            + "gf.NAME as list_name,  "
            + "gms.MSHIP_TYPE as membership_type,  "
            + "(select gg.name from grouper_groups gg, grouper_composites gc  "
            + "where gg.id = gms.VIA_composite_ID and gg.id = gc.OWNER) as composite_parent_group_name,  "
            + "depth,   "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gms.membership_creator_ID) as creator_source,  "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gms.membership_creator_ID) as creator_subject_id,  "
            + "gms.membership_id as membership_id,   "
            + "gms.immediate_membership_id as immediate_membership_id,   "
            + "gms.GROUP_SET_ID as group_set_id,  "
            + "(select gs.id from grouper_stems gs where gs.ID = gms.owner_stem_id) as stem_id,  "
            + "(select gg.id from grouper_groups gg where gg.id = gms.owner_group_id) as group_id,  "
            + "gms.membership_create_time,  "
            + "gms.membership_creator_id,  "
            + "gms.field_id, gms.context_id  "
            + " from grouper_memberships_all_v gms, grouper_members gm, grouper_fields gf  "
            + " where gms.MEMBER_ID = gm.ID and gms.field_id = gf.id  ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_stems_v",
        "GROUPER_STEMS_V: holds one record for each stem (folder) in grouper, with friendly names",
        GrouperUtil.toSet("EXTENSION", 
            "NAME", 
            "DISPLAY_EXTENSION", 
            "DISPLAY_NAME", 
            "DESCRIPTION",
            "PARENT_STEM_NAME", 
            "PARENT_STEM_DISPLAYNAME", 
            "CREATOR_SOURCE", 
            "CREATOR_SUBJECT_ID", 
            "MODIFIER_SOURCE",
            "MODIFIER_SUBJECT_ID", 
            "CREATE_TIME", 
            "CREATOR_ID", 
            "STEM_ID", 
            "MODIFIER_ID",
            "MODIFY_TIME", 
            "PARENT_STEM", 
            "HIBERNATE_VERSION_NUMBER", "CONTEXT_ID"),
        GrouperUtil.toSet("EXTENSION: name of the stem without the parent stem names, e.g. stem1", 
            "NAME: name of the stem including parent stem names, e.g. school:stem1", 
            "DISPLAY_EXTENSION: display name of the stem without parent stem names, e.g. The stem 1", 
            "DISPLAY_NAME: display name of the stem including parent stem names, e.g. My school: The stem 1", 
            "DESCRIPTION: description entered in about the stem, for example including why the stem exists and who has access",
            "PARENT_STEM_NAME: name of the stem (folder) that this stem is in.  e.g. school", 
            "PARENT_STEM_DISPLAYNAME: display name of the stem (folder) that this stem is in.  e.g. My school", 
            "CREATOR_SOURCE: subject source where the subject that created this stem is from, e.g. mySchoolPeople", 
            "CREATOR_SUBJECT_ID: e.g. the school id of the subject that created this stem, e.g. 12345", 
            "MODIFIER_SOURCE: subject source where the subject that last modified this stem is from, e.g. mySchoolPeople",
            "MODIFIER_SUBJECT_ID: e.g. the school id of the subject who last modified this stem, e.g. 12345", 
            "CREATE_TIME: number of millis since 1970 that this stem was created", 
            "CREATOR_ID: member id of the subject who created this stem, foreign key to grouper_members", 
            "STEM_ID: uuid unique id of this stem", 
            "MODIFIER_ID: member id of the subject who last modified this stem, foreign key to grouper_members",
            "MODIFY_TIME: number of millis since 1970 since this stem was last modified", 
            "PARENT_STEM: stem_id uuid unique id of the stem (folder) that this stem is in", 
            "HIBERNATE_VERSION_NUMBER: increments by one for each update from hibernate",
            "CONTEXT_ID: Context id links together multiple operations into one high level action"),
         "select gs.extension, gs.NAME, "
            + "gs.DISPLAY_EXTENSION, gs.DISPLAY_NAME, gs.DESCRIPTION, "
            + "(select gs_parent.NAME from grouper_stems gs_parent where gs_parent.id = gs.PARENT_STEM) as parent_stem_name, "
            + "(select gs_parent.DISPLAY_NAME from grouper_stems gs_parent where gs_parent.id = gs.PARENT_STEM) as parent_stem_displayname, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gs.creator_ID) as creator_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gs.creator_ID) as creator_subject_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gs.MODIFIER_ID) as modifier_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gs.MODIFIER_ID) as modifier_subject_id, "
            + "gs.CREATE_TIME, gs.CREATOR_ID,  "
            + "gs.ID as stem_id, gs.MODIFIER_ID, gs.MODIFY_TIME, gs.PARENT_STEM, gs.HIBERNATE_VERSION_NUMBER, gs.context_id "
            + "from grouper_stems gs ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_attributes_v", 
        "GROUPER_RPT_ATTRIBUTES_V: report on attributes, how many groups use each attribute",
        GrouperUtil.toSet("ATTRIBUTE_NAME", 
            "GROUP_COUNT", 
            "GROUP_TYPE_NAME", 
            "FIELD_ID", 
            "GROUP_TYPE_ID"),
        GrouperUtil.toSet("ATTRIBUTE_NAME: friendly name of the attribute which is actually from grouper_fields", 
            "GROUP_COUNT: number of groups which define this attribute", 
            "GROUP_TYPE_NAME: group type which owns this attribute", 
            "FIELD_ID: uuid unique id of this field (attribute), foreign key from grouper_attributes to grouper_fields", 
            "GROUP_TYPE_ID: uuid unique id of the group type.  foreign key from grouper_fields to grouper_types"),
        "select gf.NAME as attribute_name,  "
        + "(select count(*) from grouper_attributes ga where ga.FIELD_ID = gf.id) as group_count,   "
        + "gt.NAME as group_type_name, "
        + "gf.ID as field_id, "
        + "gt.ID as group_type_id "
        + "from grouper_fields gf, grouper_types gt "
        + "where gf.TYPE = 'attribute' and gf.GROUPTYPE_UUID = gt.ID ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_composites_v", 
        "GROUPER_RPT_COMPOSITES_V: report on the three composite types: union, intersection, complement and how many of each exist",
        GrouperUtil.toSet("COMPOSITE_TYPE", 
            "THE_COUNT"),
        GrouperUtil.toSet("COMPOSITE_TYPE: either union: all members from both factors, intersection: only members in both factors, complement: members in first but not second factor", 
            "THE_COUNT: nubmer of composites of this type in the system"),
        "select gc.TYPE as composite_type, count(*) as the_count " 
        + "from grouper_composites gc group by gc.type ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_group_field_v", 
        "GROUPER_RPT_GROUP_FIELD_V: report on how many unique members are in each group based on field (or list) name and type",
        GrouperUtil.toSet("GROUP_NAME", 
            "GROUP_DISPLAYNAME", 
            "FIELD_TYPE", 
            "FIELD_NAME", 
            "MEMBER_COUNT"),
        GrouperUtil.toSet("GROUP_NAME: name of the group where the list and members are, e.g. school:stem1:myGroup", 
            "GROUP_DISPLAYNAME: display name of the group where the list and members are, e.g. My school:The stem1:My group", 
            "FIELD_TYPE: membership field type, e.g. list or access", 
            "FIELD_NAME: membership field name, e.g. members, admins, readers", 
            "MEMBER_COUNT: number of unique members in the group/field"),
        "select gg.name as group_name, gg.display_name as group_displayName, "
        + "gf.type as field_type, gf.name as field_name, count(distinct gms.member_id) as member_count "
        + "from grouper_memberships gms, grouper_groups gg, grouper_fields gf "
        + "where gms.FIELD_ID = gf.ID "
        + "and gg.id = gms.OWNER_group_ID "
        + "group by gg.name, gg.display_name, gf.type, gf.name ");
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_groups_v", 
        "GROUPER_RPT_GROUPS_V: report with a line for each group and some counts of immediate and effective members etc",
        GrouperUtil.toSet("GROUP_NAME", 
            "GROUP_DISPLAYNAME", 
            "TYPE_OF_GROUP", 
            "IMMEDIATE_MEMBERSHIP_COUNT", 
            "MEMBERSHIP_COUNT", 
            "ATTRIBUTE_COUNT", 
            "GROUPS_TYPES_COUNT", 
            "ISA_COMPOSITE_FACTOR_COUNT", 
            "ISA_MEMBER_COUNT", 
            "GROUP_ID"),  
        GrouperUtil.toSet("GROUP_NAME: name of group which has the stats, e.g. school:stem1:theGroup", 
            "GROUP_DISPLAYNAME: display name of the group which has the stats, e.g. My school:The stem1:The group", 
            "TYPE_OF_GROUP: group if it is a group, role if it is a role", 
            "IMMEDIATE_MEMBERSHIP_COUNT: number of unique immediate members, directly assigned to this group", 
            "MEMBERSHIP_COUNT: total number of unique members, immediate or effective", 
            "ATTRIBUTE_COUNT: number of attributes defined for this group", 
            "GROUPS_TYPES_COUNT: number of group types associated with this group", 
            "ISA_COMPOSITE_FACTOR_COUNT: number of composites this group is a factor of", 
            "ISA_MEMBER_COUNT: number of groups this group is an immediate or effective member of", 
            "GROUP_ID: uuid unique id of this group"),  
        "select  "
        + "gg.name as group_name, "
        + "gg.display_name as group_displayname, "
        + "gg.type_of_group, "
        + "(select count(distinct gms.MEMBER_ID) from grouper_memberships gms where gms.OWNER_group_ID = gg.id and gms.MSHIP_TYPE = 'immediate') as immediate_membership_count, "
        + "(select count(distinct gms.MEMBER_ID) from grouper_memberships gms where gms.OWNER_group_ID = gg.id) as membership_count, "
        + "(select count(*) from grouper_attributes ga where ga.GROUP_ID = gg.id) as attribute_count, "
        + "(select count(*) from grouper_groups_types ggt where ggt.GROUP_UUID = gg.id) as groups_types_count, "
        + "(select count(*) from grouper_composites gc where gc.LEFT_FACTOR = gg.id or gc.RIGHT_FACTOR = gg.id) as isa_composite_factor_count, "
        + "(select count(distinct gms.OWNER_group_ID) from grouper_memberships gms, grouper_members gm where gm.SUBJECT_ID = gg.ID and gms.MEMBER_ID = gm.ID ) as isa_member_count, "
        + "gg.ID as group_id "
        + "from grouper_groups gg ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_roles_v", 
        "GROUPER_RPT_ROLES_V: report with a line for each role and some counts of immediate and effective members etc",
        GrouperUtil.toSet("ROLE_NAME", 
            "ROLE_DISPLAYNAME", 
            "IMMEDIATE_MEMBERSHIP_COUNT", 
            "MEMBERSHIP_COUNT", 
            "ATTRIBUTE_COUNT", 
            "ROLES_TYPES_COUNT", 
            "ISA_COMPOSITE_FACTOR_COUNT", 
            "ISA_MEMBER_COUNT", 
            "ROLE_ID"),  
        GrouperUtil.toSet("ROLE_NAME: name of group which has the stats, e.g. school:stem1:theGroup", 
            "ROLE_DISPLAYNAME: display name of the group which has the stats, e.g. My school:The stem1:The group", 
            "IMMEDIATE_MEMBERSHIP_COUNT: number of unique immediate members, directly assigned to this group", 
            "MEMBERSHIP_COUNT: total number of unique members, immediate or effective", 
            "ATTRIBUTE_COUNT: number of attributes defined for this group", 
            "ROLES_TYPES_COUNT: number of group types associated with this group", 
            "ISA_COMPOSITE_FACTOR_COUNT: number of composites this group is a factor of", 
            "ISA_MEMBER_COUNT: number of groups this group is an immediate or effective member of", 
            "ROLE_ID: uuid unique id of this group"),  
        "select  "
        + "gg.name as role_name, "
        + "gg.display_name as role_displayname, "
        + "(select count(distinct gms.MEMBER_ID) from grouper_memberships gms where gms.OWNER_group_ID = gg.id and gms.MSHIP_TYPE = 'immediate') as immediate_membership_count, "
        + "(select count(distinct gms.MEMBER_ID) from grouper_memberships gms where gms.OWNER_group_ID = gg.id) as membership_count, "
        + "(select count(*) from grouper_attributes ga where ga.GROUP_ID = gg.id) as attribute_count, "
        + "(select count(*) from grouper_groups_types ggt where ggt.GROUP_UUID = gg.id) as roles_types_count, "
        + "(select count(*) from grouper_composites gc where gc.LEFT_FACTOR = gg.id or gc.RIGHT_FACTOR = gg.id) as isa_composite_factor_count, "
        + "(select count(distinct gms.OWNER_group_ID) from grouper_memberships gms, grouper_members gm where gm.SUBJECT_ID = gg.ID and gms.MEMBER_ID = gm.ID ) as isa_member_count, "
        + "gg.ID as role_id "
        + "from grouper_groups gg  where gg.type_of_group = 'role' ");

    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_members_v", 
        "GROUPER_RPT_MEMBERS_V: report for each member in grouper_members and some stats like how many groups they are in",
        GrouperUtil.toSet("SUBJECT_ID", 
            "SUBJECT_SOURCE", 
            "MEMBERSHIP_COUNT", 
            "MEMBER_ID"), 
        GrouperUtil.toSet("SUBJECT_ID: e.g. the school person id of the person e.g. 12345", 
            "SUBJECT_SOURCE: subject source where the subject is from, e.g. schoolAllPeople", 
            "MEMBERSHIP_COUNT: number of distinct groups or stems this member has a membership with", 
            "MEMBER_ID: uuid unique id of the member in grouper_members"), 
            "select gm.SUBJECT_ID, gm.SUBJECT_SOURCE, "
            + "(select count(distinct gms.owner_group_id) from grouper_memberships gms where gms.MEMBER_ID = gm.ID) as membership_count, "
            + "gm.ID as member_id "
            + "from grouper_members gm ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_stems_v", 
        "GROUPER_RPT_STEMS_V: report with a row for each stem and stats on many groups or members are inside",
        GrouperUtil.toSet("STEM_NAME", 
            "STEM_DISPLAYNAME", 
            "GROUP_IMMEDIATE_COUNT", 
            "STEM_IMMEDIATE_COUNT", 
            "GROUP_COUNT",
            "STEM_COUNT", 
            "THIS_STEM_MEMBERSHIP_COUNT", 
            "CHILD_GROUP_MEMBERSHIP_COUNT", 
            "GROUP_MEMBERSHIP_COUNT", 
            "STEM_ID"), 
        GrouperUtil.toSet("STEM_NAME: name of the stem in report, e.g. school:stem1", 
            "STEM_DISPLAYNAME: display name of the stem in report, e.g. My school:The stem 1", 
            "GROUP_IMMEDIATE_COUNT: number of groups directly inside this stem", 
            "STEM_IMMEDIATE_COUNT: number of stems directly inside this stem", 
            "GROUP_COUNT: number of groups inside this stem, or in a stem inside this stem etc",
            "STEM_COUNT: number of stems inside this stem or in a stem inside this stem etc", 
            "THIS_STEM_MEMBERSHIP_COUNT: number of access memberships related to this stem (e.g. how many people can create groups/stems inside)", 
            "CHILD_GROUP_MEMBERSHIP_COUNT: number of memberships in groups immediately in this stem", 
            "GROUP_MEMBERSHIP_COUNT: number of memberships in groups in this stem or in stems in this stem etc", 
            "STEM_ID: uuid unique id of this stem"), 
            "select gs.NAME as stem_name, gs.DISPLAY_NAME as stem_displayname, "
            + "(select count(*) from grouper_groups gg where gg.PARENT_STEM = gs.ID) as group_immediate_count, "
            + "(select count(*) from grouper_stems gs2 where gs.id = gs2.PARENT_STEM ) as stem_immediate_count, "
            + "(select count(*) from grouper_attributes ga, grouper_fields gf where ga.FIELD_ID = gf.ID and gf.NAME = 'name' and ga.value like gs.NAME || '%') as group_count, "
            + "(select count(*) from grouper_stems gs2 where gs2.name like gs.NAME || '%') as stem_count, "
            + "(select count(distinct gm.member_id) from grouper_memberships gm where gm.OWNER_stem_ID = gs.id) as this_stem_membership_count,  "
            + "(select count(distinct gm.member_id) from grouper_memberships gm, grouper_groups gg where gg.parent_stem = gs.id and gm.OWNER_stem_ID = gg.id) as child_group_membership_count,  "
            + "(select count(distinct gm.member_id) from grouper_memberships gm, grouper_attributes ga, grouper_fields gf where gm.owner_group_id = ga.group_id and ga.FIELD_ID = gf.ID and gf.NAME = 'name' and ga.value like gs.NAME || '%') as group_membership_count, "
            + "gs.ID as stem_id "
            + "from grouper_stems gs ");
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_rpt_types_v", 
        "GROUPER_RPT_TYPES_V: report on group types and how many groups have that type",
        GrouperUtil.toSet("GROUP_TYPE_NAME", 
            "GROUP_COUNT", 
            "GROUP_TYPE_ID"),
        GrouperUtil.toSet("GROUP_TYPE_NAME: friendly name of this group type", 
            "GROUP_COUNT: number of groups that have this group type", 
            "GROUP_TYPE_ID: uuid unique id of this group type"),
        "select gt.NAME as group_type_name, "
        + "(select count(*) from grouper_groups_types ggt where ggt.TYPE_UUID = gt.ID) as group_count, "
        + "gt.id as group_type_id "
        + "from grouper_types gt ");

    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_role_set_v", 
        "grouper_role_set_v: shows all role set relationships",
        GrouperUtil.toSet("if_has_role_name", 
            "then_has_role_name", 
            "depth", "type", 
            "parent_if_has_name", "parent_then_has_name",
            "id", "if_has_role_id", "then_has_role_id",
            "parent_role_set_id"),
        GrouperUtil.toSet("if_has_role_name: name of the set role", 
            "then_has_role_name: name of the member role", 
            "depth: number of hops in the directed graph",
            "type: self, immediate, effective",
            "parent_if_has_name: name of the role set record which is the parent ifHas on effective path (everything but last hop)",
            "parent_then_has_name: name of the role set record which is the parent thenHas on effective path (everything but last hop)",
            "id: id of the set record", "if_has_role_id: id of the set role",
            "then_has_role_id: id of the member role", 
            "parent_role_set_id: id of the role set record which is the parent on effective path (everything but last hop)"
        ),
        "select ifHas.name as if_has_role_name, thenHas.name as then_has_role_name,  grs.depth,   "
        + "grs.type, grParentIfHas.name as parent_if_has_name, grParentThenHas.name as parent_then_has_name,   "
        + "grs.id, ifHas.id as if_has_role_id, thenHas.id as then_has_role_id,   "
        + "grs.parent_role_set_id  "
        + "from grouper_role_set grs,   "
        + "grouper_role_set grsParent,   "
        + "grouper_groups grParentIfHas,   "
        + "grouper_groups grParentThenHas,   "
        + "grouper_groups ifHas, grouper_groups thenHas   "
        + "where  thenHas.id = grs.then_has_role_id   "
        + "and ifHas.id = grs.if_has_role_id   "
        + "and grs.parent_role_set_id = grsParent.id   "
        + "and grParentIfHas.id = grsParent.if_has_role_id   "
        + "and grParentThenHas.id = grsParent.then_has_role_id   "
        + "order by ifHas.name, thenHas.name, grs.depth, grParentIfHas.name, grParentThenHas.name ");

    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_def_name_set_v", 
        "grouper_attr_def_name_set_v: shows all attribute def name set relationships",
        GrouperUtil.toSet("if_has_attr_def_name_name", 
            "then_has_attr_def_name_name", 
            "depth", "type", 
            "parent_if_has_name", "parent_then_has_name",
            "id", "if_has_attr_def_name_id", "then_has_attr_def_name_id",
            "parent_attr_def_name_set_id"),
        GrouperUtil.toSet("if_has_attr_def_name_name: name of the set attribute def name", 
            "then_has_attr_def_name_name: name of the member attribute def name", 
            "depth: number of hops in the directed graph",
            "type: self, immediate, effective",
            "parent_if_has_name: name of the attribute def name set record which is the parent ifHas on effective path (everything but last hop)",
            "parent_then_has_name: name of the attribute def name set record which is the parent thenHas on effective path (everything but last hop)",
            "id: id of the set record", "if_has_attr_def_name_id: id of the set attribute def name",
            "then_has_attr_def_name_id: id of the member attribute def name", 
            "parent_attr_def_name_set_id: id of the attribute def name set record which is the parent on effective path (everything but last hop)"
        ),
        "select ifHas.name as if_has_attr_def_name_name, thenHas.name as then_has_attr_def_name_name,  "
        + "gadns.depth,  "
        + "gadns.type, gadnParentIfHas.name as parent_if_has_name, gadnParentThenHas.name as parent_then_has_name,  "
        + "gadns.id,  "
        + "ifHas.id as if_has_attr_def_name_id, thenHas.id as then_has_attr_def_name_id,  "
        + "gadns.parent_attr_def_name_set_id "
        + "from grouper_attribute_def_name_set gadns,  "
        + "grouper_attribute_def_name_set gadnsParent,  "
        + "grouper_attribute_def_name gadnParentIfHas,  "
        + "grouper_attribute_def_name gadnParentThenHas,  "
        + "grouper_attribute_def_name ifHas, grouper_attribute_def_name thenHas  "
        + "where  thenHas.id = gadns.then_has_attribute_def_name_id  "
        + "and ifHas.id = gadns.if_has_attribute_def_name_id  "
        + "and gadns.parent_attr_def_name_set_id = gadnsParent.id  "
        + "and gadnParentIfHas.id = gadnsParent.if_has_attribute_def_name_id  "
        + "and gadnParentThenHas.id = gadnsParent.then_has_attribute_def_name_id  "
        + "order by ifHas.name, thenHas.name, gadns.depth, gadnParentIfHas.name, gadnParentThenHas.name ");


    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_group_v", 
        "grouper_attr_asn_group_v: attribute assigned to a group, with related columns",
        GrouperUtil.toSet("group_name",
          "action",
          "attribute_def_name_name",
          "group_display_name",
          "attribute_def_name_disp_name",
          "name_of_attribute_def",
          "attribute_assign_notes",
          "group_id",
          "attribute_assign_id",
          "attribute_def_name_id",
          "attribute_def_id"
        ),
        GrouperUtil.toSet("group_name: name of group assigned the attribute",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "group_display_name: display name of the group assigned an attribute",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
            "attribute_assign_notes: notes related to the attribute assignment",
            "group_id: group id of the group assigned the attribute",
            "attribute_assign_id: id of the attribute assignment",
            "attribute_def_name_id: id of the attribute definition name",
            "attribute_def_id: id of the attribute definition"
        ),
        "select gg.name as group_name, " +
        "gaa.action, " +
        "gadn.name as attribute_def_name_name, "
        + "gg.display_name as group_display_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gad.name as name_of_attribute_def, "
        + "gaa.notes as attribute_assign_notes, "
        + "gg.id as group_id, "
        + "gaa.id as attribute_assign_id, "
        + "gadn.id as attribute_def_name_id, "
        + "gad.id as attribute_def_id "
        + "from grouper_attribute_assign gaa, grouper_groups gg, "
        + "grouper_attribute_def_name gadn, grouper_attribute_def gad "
        + "where gaa.owner_group_id = gg.id "
        + "and gaa.attribute_def_name_id = gadn.id "
        + "and gadn.attribute_def_id = gad.id "
        + "and gaa.enabled = 'T' ");


    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_efmship_v", 
        "grouper_attr_asn_efmship_v: attribute assigned to an effective membership",
        GrouperUtil.toSet("group_name",
          "subject_source_id",
          "subject_id",
          "action",
          "attribute_def_name_name",
          "group_display_name",
          "attribute_def_name_disp_name",
          "name_of_attribute_def",
          "attribute_assign_notes",
          "list_name",
          "group_id",
          "attribute_assign_id",
          "attribute_def_name_id",
          "attribute_def_id",
          "member_id"
        ),
        GrouperUtil.toSet("group_name: name of group assigned the attribute",
            "subject_source_id: source id of the subject being assigned",
            "subject_id: subject id of the subject being assigned",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "group_display_name: display name of the group assigned an attribute",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
            "attribute_assign_notes: notes related to the attribute assignment",
            "list_name: name of the membership list for this effective membership",
            "group_id: group id of the group assigned the attribute",
            "attribute_assign_id: id of the attribute assignment",
            "attribute_def_name_id: id of the attribute definition name",
            "attribute_def_id: id of the attribute definition",
            "member_id: id of the member assigned the attribute"
        ),
        "select distinct gg.name as group_name, "
        + "gm.subject_source as subject_source_id, "
        + "gm.subject_id, "
        + "gaa.action, "
        + "gadn.name as attribute_def_name_name, "
        + "gg.display_name as group_display_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gad.name as name_of_attribute_def, "
        + "gaa.notes as attribute_assign_notes, "
        + "gf.name as list_name, "
        + "gg.id as group_id, "
        + "gaa.id as attribute_assign_id, "
        + "gadn.id as attribute_def_name_id, "
        + "gad.id as attribute_def_id, "
        + "gm.id as member_id "
        + "from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, "
        + "grouper_attribute_def_name gadn, grouper_attribute_def gad, "
        + "grouper_groups gg, grouper_fields gf, grouper_members gm "
        + "where gaa.owner_group_id = gmav.owner_group_id "
        + "and gaa.owner_member_id = gmav.member_id "
        + "and gaa.attribute_def_name_id = gadn.id "
        + "and gadn.attribute_def_id = gad.id "
        + "and gaa.enabled = 'T' "
        + "and gmav.immediate_mship_enabled = 'T' "
        + "and gmav.owner_group_id = gg.id "
        + "and gmav.field_id = gf.id "
        + "and gf.type = 'list' "
        + "and gmav.member_id = gm.id ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_stem_v", 
        "grouper_attr_asn_stem_v: attribute assigned to a stem and related cols",
        GrouperUtil.toSet("stem_name",
            "action",
            "attribute_def_name_name",
            "stem_display_name",
            "attribute_def_name_disp_name",
            "name_of_attribute_def",
            "attribute_assign_notes",
            "stem_id",
            "attribute_assign_id",
            "attribute_def_name_id",
            "attribute_def_id"
        ),
        GrouperUtil.toSet("stem_name: name of stem assigned the attribute",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "stem_display_name: display name of the stem assigned an attribute",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
            "attribute_assign_notes: notes related to the attribute assignment",
            "stem_id: stem id of the stem assigned the attribute",
            "attribute_assign_id: id of the attribute assignment",
            "attribute_def_name_id: id of the attribute definition name",
            "attribute_def_id: id of the attribute definition"
        ),
        "select gs.name as stem_name, " +
        "gaa.action, " +
        "gadn.name as attribute_def_name_name, "
        + "gs.display_name as stem_display_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gad.name as name_of_attribute_def, "
        + "gaa.notes as attribute_assign_notes, "
        + "gs.id as stem_id, "
        + "gaa.id as attribute_assign_id, "
        + "gadn.id as attribute_def_name_id, "
        + "gad.id as attribute_def_id "
        + "from grouper_attribute_assign as gaa, grouper_stems as gs, "
        + "grouper_attribute_def_name as gadn, grouper_attribute_def as gad "
        + "where gaa.owner_stem_id = gs.id "
        + "and gaa.attribute_def_name_id = gadn.id "
        + "and gadn.attribute_def_id = gad.id "
        + "and gaa.enabled = 'T' ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_member_v", 
        "grouper_attr_asn_member_v: attribute assigned to a member and related cols",
        GrouperUtil.toSet("source_id", "subject_id",
            "action",
            "attribute_def_name_name",
            "attribute_def_name_disp_name",
            "name_of_attribute_def",
            "attribute_assign_notes",
            "member_id",
            "attribute_assign_id",
            "attribute_def_name_id",
            "attribute_def_id"
        ),
        GrouperUtil.toSet("source_id: source of the subject that belongs to the member",
            "subject_id: subject_id of the subject that belongs to the member",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
            "attribute_assign_notes: notes related to the attribute assignment",
            "member_id: member id of the member assigned the attribute (this is an internal grouper uuid)",
            "attribute_assign_id: id of the attribute assignment",
            "attribute_def_name_id: id of the attribute definition name",
            "attribute_def_id: id of the attribute definition"
        ),
        "select gm.subject_source as source_id, gm.subject_id, " +
        "gaa.action, " +
        "gadn.name as attribute_def_name_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gad.name as name_of_attribute_def, "
        + "gaa.notes as attribute_assign_notes, "
        + "gm.id as member_id, "
        + "gaa.id as attribute_assign_id, "
        + "gadn.id as attribute_def_name_id, "
        + "gad.id as attribute_def_id "
        + "from grouper_attribute_assign as gaa, grouper_members as gm, "
        + "grouper_attribute_def_name as gadn, grouper_attribute_def as gad "
        + "where gaa.owner_member_id = gm.id "
        + "and gaa.attribute_def_name_id = gadn.id "
        + "and gadn.attribute_def_id = gad.id "
        + "and gaa.enabled = 'T' ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_mship_v", 
        "grouper_attr_asn_mship_v: attribute assigned to an immediate memberships, and related cols",
        GrouperUtil.toSet("group_name",
            "source_id",
            "subject_id",
            "action",
            "attribute_def_name_name",
            "attribute_def_name_disp_name",
            "list_name",
            "name_of_attribute_def",
            "attribute_assign_notes",
            "group_id",
            "membership_id",
            "member_id",
            "attribute_assign_id",
            "attribute_def_name_id",
            "attribute_def_id"
          ),
          GrouperUtil.toSet("group_name: name of group in membership assigned the attribute",
              "source_id: source of the subject that belongs to the member",
              "subject_id: subject_id of the subject that belongs to the member",
              "action: the action associated with the attribute assignment (default is assign)",
              "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
              "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
              "list_name: name of list in membership assigned the attribute",
              "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
              "attribute_assign_notes: notes related to the attribute assignment",
              "group_id: group id of the membership assigned the attribute",
              "membership_id: membership id assigned the attribute",
              "member_id: internal grouper member uuid of the membership assigned the attribute",
              "attribute_assign_id: id of the attribute assignment",
              "attribute_def_name_id: id of the attribute definition name",
              "attribute_def_id: id of the attribute definition"
          ),
          "select gg.name as group_name, " +
          "gm.subject_source as source_id, " +
          "gm.subject_id, " +
          "gaa.action, " +
          "gadn.name as attribute_def_name_name, "
          + "gadn.display_name as attribute_def_name_disp_name, "
          + "gf.name as list_name, "
          + "gad.name as name_of_attribute_def, "
          + "gaa.notes as attribute_assign_notes, "
          + "gg.id as group_id, "
          + "gms.id as membership_id, "
          + "gm.id as member_id, "
          + "gaa.id as attribute_assign_id, "
          + "gadn.id as attribute_def_name_id, "
          + "gad.id as attribute_def_id "
          + "from grouper_attribute_assign gaa, grouper_groups gg, grouper_memberships gms, "
          + "grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_members gm, grouper_fields gf "
          + "where gaa.owner_membership_id = gms.id "
          + "and gaa.attribute_def_name_id = gadn.id "
          + "and gadn.attribute_def_id = gad.id "
          + "and gaa.enabled = 'T'" +
          		" and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id" +
          		" and gf.type = 'list'");

    

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_attrdef_v", 
        "grouper_attr_asn_attrdef_v: attribute assigned to an attribute definition, and related columns",
        GrouperUtil.toSet("name_of_attr_def_assigned_to",
            "action",
            "attribute_def_name_name",
            "attribute_def_name_disp_name",
            "name_of_attribute_def_assigned",
            "attribute_assign_notes",
            "id_of_attr_def_assigned_to",
            "attribute_assign_id",
            "attribute_def_name_id",
            "attribute_def_id"
          ),
          GrouperUtil.toSet("name_of_attr_def_assigned_to: name of attribute def assigned the attribute",
              "action: the action associated with the attribute assignment (default is assign)",
              "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
              "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
              "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
              "attribute_assign_notes: notes related to the attribute assignment",
              "id_of_attr_def_assigned_to: attrDef id of the attributeDef assigned the attribute",
              "attribute_assign_id: id of the attribute assignment",
              "attribute_def_name_id: id of the attribute definition name",
              "attribute_def_id: id of the attribute definition"
          ),
          "select gad_assigned_to.name as name_of_attr_def_assigned_to, " +
          "gaa.action, " +
          "gadn.name as attribute_def_name_name, "
          + "gadn.display_name as attribute_def_name_disp_name, "
          + "gad.name as name_of_attribute_def, "
          + "gaa.notes as attribute_assign_notes, "
          + "gad_assigned_to.id as id_of_attr_def_assigned_to, "
          + "gaa.id as attribute_assign_id, "
          + "gadn.id as attribute_def_name_id, "
          + "gad.id as attribute_def_id "
          + "from grouper_attribute_assign gaa, grouper_attribute_def gad_assigned_to, "
          + "grouper_attribute_def_name gadn, grouper_attribute_def gad "
          + "where gaa.owner_attribute_def_id = gad_assigned_to.id "
          + "and gaa.attribute_def_name_id = gadn.id "
          + "and gadn.attribute_def_id = gad.id "
          + "and gaa.enabled = 'T' ");


    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_asn_group_v", 
        "grouper_attr_asn_asn_group_v: attribute assigned to an assignment of attribute to a group, and related cols",
        GrouperUtil.toSet("group_name",
            "action1",
            "action2",
            "attribute_def_name_name1",
            "attribute_def_name_name2",
            "group_display_name",
            "attribute_def_name_disp_name1",
            "attribute_def_name_disp_name2",
            "name_of_attribute_def1",
            "name_of_attribute_def2",
            "attribute_assign_notes1",
            "attribute_assign_notes2",
            "group_id",
            "attribute_assign_id1",
            "attribute_assign_id2",
            "attribute_def_name_id1",
            "attribute_def_name_id2",
            "attribute_def_id1",
            "attribute_def_id2"
          ),
          GrouperUtil.toSet("group_name: name of group assigned the attribute",
              "action1: the action associated with the original attribute assignment (default is assign)",
              "action2: the action associated with this attribute assignment (default is assign)",
              "attribute_def_name_name1: name of the original attribute definition name which is assigned to the group",
              "attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment",
              "group_display_name: display name of the group assigned an attribute",
              "attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute",
              "attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute",
              "name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group",
              "name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment",
              "attribute_assign_notes1: notes related to the original attribute assignment to the group",
              "attribute_assign_notes2: notes related to the new attribute assignment to the assignment",
              "group_id: group id of the group assigned the attribute",
              "attribute_assign_id1: id of the original attribute assignment to the group",
              "attribute_assign_id2: id of the new attribute assignment to the assignment",
              "attribute_def_name_id1: id of the original attribute definition name assigned to the group",
              "attribute_def_name_id2: id of the new attribute definition name assigned to the assignment",
              "attribute_def_id1: id of the original attribute definition assigned to the group",
              "attribute_def_id2: id of the new attribute definition assigned to the attribute"
          ),
          "select gg.name as group_name, " +
          "gaa1.action as action1, gaa2.action as action2, " +
          "gadn1.name as attribute_def_name_name1, " +
          "gadn2.name as attribute_def_name_name2, "
          + "gg.display_name as group_display_name, "
          + "gadn1.display_name as attribute_def_name_disp_name1, "
          + "gadn2.display_name as attribute_def_name_disp_name2, "
          + "gad1.name as name_of_attribute_def1, "
          + "gad2.name as name_of_attribute_def2, "
          + "gaa1.notes as attribute_assign_notes1, "
          + "gaa2.notes as attribute_assign_notes2, "
          + "gg.id as group_id, "
          + "gaa1.id as attribute_assign_id1, "
          + "gaa2.id as attribute_assign_id2, "
          + "gadn1.id as attribute_def_name_id1, "
          + "gadn2.id as attribute_def_name_id2, "
          + "gad1.id as attribute_def_id1, "
          + "gad2.id as attribute_def_id2 "
          + "from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, "
          + "grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, "
          + "grouper_attribute_def gad2 "
          + "where gaa1.id = gaa2.owner_attribute_assign_id "
          + "and gaa1.attribute_def_name_id = gadn1.id "
          + "and gaa2.attribute_def_name_id = gadn2.id "
          + "and gadn1.attribute_def_id = gad1.id "
          + "and gadn2.attribute_def_id = gad2.id "
          + "and gaa1.enabled = 'T' and gaa2.enabled = 'T' " +
          		"and gg.id = gaa1.owner_group_id");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_asn_efmship_v", 
        "grouper_attr_asn_asn_efmship_v: attribute assigned to an assignment of an attribute to an effective membership, and related cols",
        GrouperUtil.toSet("group_name",
            "source_id",
            "subject_id",
            "action1",
            "action2",
            "attribute_def_name_name1",
            "attribute_def_name_name2",
            "attribute_def_name_disp_name1",
            "attribute_def_name_disp_name2",
            "list_name",
            "name_of_attribute_def1",
            "name_of_attribute_def2",
            "attribute_assign_notes1",
            "attribute_assign_notes2",
            "group_id",
            "member_id",
            "attribute_assign_id1",
            "attribute_assign_id2",
            "attribute_def_name_id1",
            "attribute_def_name_id2",
            "attribute_def_id1",
            "attribute_def_id2"
          ),
          GrouperUtil.toSet("group_name: name of group in membership assigned the attribute",
              "source_id: source of the subject that belongs to the member",
              "subject_id: subject_id of the subject that belongs to the member",
              "action1: the action associated with the original attribute assignment (default is assign)",
              "action2: the action associated with the new attribute assignment (default is assign)",
              "attribute_def_name_name1: name of the original attribute definition name which is assigned to the group",
              "attribute_def_name_name2: name of the new attribute definition name which is assigned to the group",
              "attribute_def_name_disp_name1: display name of the original attribute definition name assigned to the attribute",
              "attribute_def_name_disp_name2: display name of the new attribute definition name assigned to the attribute",
              "list_name: name of list in membership assigned the attribute",
              "name_of_attribute_def1: name of the original attribute definition associated with the attribute definition name assigned to the group",
              "name_of_attribute_def2: name of the new attribute definition associated with the attribute definition name assigned to the group",
              "attribute_assign_notes1: notes related to the original attribute assignment",
              "attribute_assign_notes2: notes related to the new attribute assignment",
              "group_id: group id of the membership assigned the attribute",
              "member_id: internal grouper member uuid of the membership assigned the attribute",
              "attribute_assign_id1: id of the original attribute assignment",
              "attribute_assign_id2: id of the new attribute assignment",
              "attribute_def_name_id1: id of the original attribute definition name",
              "attribute_def_name_id2: id of the new attribute definition name",
              "attribute_def_id1: id of the original attribute definition",
              "attribute_def_id2: id of the new attribute definition"
          ),
          "select distinct gg.name as group_name, " +
          "gm.subject_source as source_id, " +
          "gm.subject_id, " +
          "gaa1.action as action1, gaa2.action as action2, " +
          "gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, "
          + "gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, "
          + "gf.name as list_name, "
          + "gad1.name as name_of_attribute_def1, "
          + "gad2.name as name_of_attribute_def2, "
          + "gaa1.notes as attribute_assign_notes1, "
          + "gaa2.notes as attribute_assign_notes2, "
          + "gg.id as group_id, "
          + "gm.id as member_id, "
          + "gaa1.id as attribute_assign_id1, "
          + "gaa2.id as attribute_assign_id2, "
          + "gadn1.id as attribute_def_name_id1, "
          + "gadn2.id as attribute_def_name_id2, "
          + "gad1.id as attribute_def_id1, "
          + "gad2.id as attribute_def_id2 "
          + "from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, " +
              "grouper_groups gg, grouper_memberships_all_v gmav, "
          + "grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, " +
              "grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf "
          + "where gaa1.owner_member_id = gmav.member_id and gaa1.owner_group_id = gmav.owner_group_id" +
          		" and gaa2.owner_attribute_assign_id = gaa1.id  "
          + "and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id "
          + "and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id "
          + "and gaa1.enabled = 'T' and gaa2.enabled = 'T' and gmav.immediate_mship_enabled = 'T'" +
              " and gmav.field_id = gf.id and gmav.member_id = gm.id and gmav.owner_group_id = gg.id" +
              " and gf.type = 'list'");

    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_asn_stem_v", 
        "grouper_attr_asn_asn_stem_v: attribute assigned to an assignment of attribute to a stem, and related cols",
        GrouperUtil.toSet("stem_name",
            "action1",
            "action2",
            "attribute_def_name_name1",
            "attribute_def_name_name2",
            "stem_display_name",
            "attribute_def_name_disp_name1",
            "attribute_def_name_disp_name2",
            "name_of_attribute_def1",
            "name_of_attribute_def2",
            "attribute_assign_notes1",
            "attribute_assign_notes2",
            "stem_id",
            "attribute_assign_id1",
            "attribute_assign_id2",
            "attribute_def_name_id1",
            "attribute_def_name_id2",
            "attribute_def_id1",
            "attribute_def_id2"
          ),
          GrouperUtil.toSet("stem_name: name of stem assigned the attribute",
              "action1: the action associated with the original attribute assignment (default is assign)",
              "action2: the action associated with this attribute assignment (default is assign)",
              "attribute_def_name_name1: name of the original attribute definition name which is assigned to the group",
              "attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment",
              "stem_display_name: display name of the stem assigned an attribute",
              "attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute",
              "attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute",
              "name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group",
              "name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment",
              "attribute_assign_notes1: notes related to the original attribute assignment to the group",
              "attribute_assign_notes2: notes related to the new attribute assignment to the assignment",
              "stem_id: stem id of the stem assigned the attribute",
              "attribute_assign_id1: id of the original attribute assignment to the group",
              "attribute_assign_id2: id of the new attribute assignment to the assignment",
              "attribute_def_name_id1: id of the original attribute definition name assigned to the group",
              "attribute_def_name_id2: id of the new attribute definition name assigned to the assignment",
              "attribute_def_id1: id of the original attribute definition assigned to the group",
              "attribute_def_id2: id of the new attribute definition assigned to the attribute"
          ),
          "select gs.name as stem_name, " +
          "gaa1.action as action1, gaa2.action as action2, " +
          "gadn1.name as attribute_def_name_name1, " +
          "gadn2.name as attribute_def_name_name2, "
          + "gs.display_name as stem_display_name, "
          + "gadn1.display_name as attribute_def_name_disp_name1, "
          + "gadn2.display_name as attribute_def_name_disp_name2, "
          + "gad1.name as name_of_attribute_def1, "
          + "gad2.name as name_of_attribute_def2, "
          + "gaa1.notes as attribute_assign_notes1, "
          + "gaa2.notes as attribute_assign_notes2, "
          + "gs.id as stem_id, "
          + "gaa1.id as attribute_assign_id1, "
          + "gaa2.id as attribute_assign_id2, "
          + "gadn1.id as attribute_def_name_id1, "
          + "gadn2.id as attribute_def_name_id2, "
          + "gad1.id as attribute_def_id1, "
          + "gad2.id as attribute_def_id2 "
          + "from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_stems gs, "
          + "grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, "
          + "grouper_attribute_def gad2 "
          + "where gaa1.id = gaa2.owner_attribute_assign_id "
          + "and gaa1.attribute_def_name_id = gadn1.id "
          + "and gaa2.attribute_def_name_id = gadn2.id "
          + "and gadn1.attribute_def_id = gad1.id "
          + "and gadn2.attribute_def_id = gad2.id "
          + "and gaa1.enabled = 'T' and gaa2.enabled = 'T' " +
              "and gs.id = gaa1.owner_stem_id");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_asn_member_v", 
        "grouper_attr_asn_asn_member_v: attribute assigned to an assignment of an attribute to a member, and related cols",
        GrouperUtil.toSet("source_id", "subject_id", 
            "action1",
            "action2",
            "attribute_def_name_name1",
            "attribute_def_name_name2",
            "attribute_def_name_disp_name1",
            "attribute_def_name_disp_name2",
            "name_of_attribute_def1",
            "name_of_attribute_def2",
            "attribute_assign_notes1",
            "attribute_assign_notes2",
            "member_id",
            "attribute_assign_id1",
            "attribute_assign_id2",
            "attribute_def_name_id1",
            "attribute_def_name_id2",
            "attribute_def_id1",
            "attribute_def_id2"
          ),
          GrouperUtil.toSet("source_id: source id of the member assigned the original attribute",
              "subject_id: subject id of the member assigned the original attribute",
              "action1: the action associated with the original attribute assignment (default is assign)",
              "action2: the action associated with this attribute assignment (default is assign)",
              "attribute_def_name_name1: name of the original attribute definition name which is assigned to the group",
              "attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment",
              "attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute",
              "attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute",
              "name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group",
              "name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment",
              "attribute_assign_notes1: notes related to the original attribute assignment to the group",
              "attribute_assign_notes2: notes related to the new attribute assignment to the assignment",
              "member_id: member id of the member assigned the original attribute",
              "attribute_assign_id1: id of the original attribute assignment to the group",
              "attribute_assign_id2: id of the new attribute assignment to the assignment",
              "attribute_def_name_id1: id of the original attribute definition name assigned to the group",
              "attribute_def_name_id2: id of the new attribute definition name assigned to the assignment",
              "attribute_def_id1: id of the original attribute definition assigned to the group",
              "attribute_def_id2: id of the new attribute definition assigned to the attribute"
          ),
          "select gm.subject_source as source_id, gm.subject_id, " +
          "gaa1.action as action1, gaa2.action as action2, " +
          "gadn1.name as attribute_def_name_name1, " +
          "gadn2.name as attribute_def_name_name2, "
          + "gadn1.display_name as attribute_def_name_disp_name1, "
          + "gadn2.display_name as attribute_def_name_disp_name2, "
          + "gad1.name as name_of_attribute_def1, "
          + "gad2.name as name_of_attribute_def2, "
          + "gaa1.notes as attribute_assign_notes1, "
          + "gaa2.notes as attribute_assign_notes2, "
          + "gm.id as member_id, "
          + "gaa1.id as attribute_assign_id1, "
          + "gaa2.id as attribute_assign_id2, "
          + "gadn1.id as attribute_def_name_id1, "
          + "gadn2.id as attribute_def_name_id2, "
          + "gad1.id as attribute_def_id1, "
          + "gad2.id as attribute_def_id2 "
          + "from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_members gm, "
          + "grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, "
          + "grouper_attribute_def gad2 "
          + "where gaa1.id = gaa2.owner_attribute_assign_id "
          + "and gaa1.attribute_def_name_id = gadn1.id "
          + "and gaa2.attribute_def_name_id = gadn2.id "
          + "and gadn1.attribute_def_id = gad1.id "
          + "and gadn2.attribute_def_id = gad2.id "
          + "and gaa1.enabled = 'T' and gaa2.enabled = 'T' " +
              "and gm.id = gaa1.owner_member_id");

    
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_asn_mship_v", 
        "grouper_attr_asn_asn_mship_v: attribute assigned to an assignment of an attribute to a membership, and related cols",
        GrouperUtil.toSet("group_name",
            "source_id",
            "subject_id",
            "action1",
            "action2",
            "attribute_def_name_name1",
            "attribute_def_name_name2",
            "attribute_def_name_disp_name1",
            "attribute_def_name_disp_name2",
            "list_name",
            "name_of_attribute_def1",
            "name_of_attribute_def2",
            "attribute_assign_notes1",
            "attribute_assign_notes2",
            "group_id",
            "membership_id",
            "member_id",
            "attribute_assign_id1",
            "attribute_assign_id2",
            "attribute_def_name_id1",
            "attribute_def_name_id2",
            "attribute_def_id1",
            "attribute_def_id2"
          ),
          GrouperUtil.toSet("group_name: name of group in membership assigned the attribute",
              "source_id: source of the subject that belongs to the member",
              "subject_id: subject_id of the subject that belongs to the member",
              "action1: the action associated with the original attribute assignment (default is assign)",
              "action2: the action associated with the new attribute assignment (default is assign)",
              "attribute_def_name_name1: name of the original attribute definition name which is assigned to the group",
              "attribute_def_name_name2: name of the new attribute definition name which is assigned to the group",
              "attribute_def_name_disp_name1: display name of the original attribute definition name assigned to the attribute",
              "attribute_def_name_disp_name2: display name of the new attribute definition name assigned to the attribute",
              "list_name: name of list in membership assigned the attribute",
              "name_of_attribute_def1: name of the original attribute definition associated with the attribute definition name assigned to the group",
              "name_of_attribute_def2: name of the new attribute definition associated with the attribute definition name assigned to the group",
              "attribute_assign_notes1: notes related to the original attribute assignment",
              "attribute_assign_notes2: notes related to the new attribute assignment",
              "group_id: group id of the membership assigned the attribute",
              "membership_id: membership id assigned the attribute",
              "member_id: internal grouper member uuid of the membership assigned the attribute",
              "attribute_assign_id1: id of the original attribute assignment",
              "attribute_assign_id2: id of the new attribute assignment",
              "attribute_def_name_id1: id of the original attribute definition name",
              "attribute_def_name_id2: id of the new attribute definition name",
              "attribute_def_id1: id of the original attribute definition",
              "attribute_def_id2: id of the new attribute definition"
          ),
          "select gg.name as group_name, " +
          "gm.subject_source as source_id, " +
          "gm.subject_id, " +
          "gaa1.action as action1, gaa2.action as action2, " +
          "gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, "
          + "gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, "
          + "gf.name as list_name, "
          + "gad1.name as name_of_attribute_def1, "
          + "gad2.name as name_of_attribute_def2, "
          + "gaa1.notes as attribute_assign_notes1, "
          + "gaa2.notes as attribute_assign_notes2, "
          + "gg.id as group_id, "
          + "gms.id as membership_id, "
          + "gm.id as member_id, "
          + "gaa1.id as attribute_assign_id1, "
          + "gaa2.id as attribute_assign_id2, "
          + "gadn1.id as attribute_def_name_id1, "
          + "gadn2.id as attribute_def_name_id2, "
          + "gad1.id as attribute_def_id1, "
          + "gad2.id as attribute_def_id2 "
          + "from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, " +
          		"grouper_groups gg, grouper_memberships gms, "
          + "grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, " +
          		"grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf "
          + "where gaa1.owner_membership_id = gms.id and gaa2.owner_attribute_assign_id = gaa1.id  "
          + "and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id "
          + "and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id "
          + "and gaa1.enabled = 'T' and gaa2.enabled = 'T'" +
              " and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id" +
              " and gf.type = 'list'");


    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_asn_asn_attrdef_v", 
        "grouper_attr_asn_asn_attrdef_v: attribute assigned to an assignment of an attribute to an attribute definition, and related cols",
        GrouperUtil.toSet("name_of_attr_def_assigned_to",
            "action1",
            "action2",
            "attribute_def_name_name1",
            "attribute_def_name_name2",
            "attribute_def_name_disp_name1",
            "attribute_def_name_disp_name2",
            "name_of_attribute_def1",
            "name_of_attribute_def2",
            "attribute_assign_notes1",
            "attribute_assign_notes2",
            "id_of_attr_def_assigned_to",
            "attribute_assign_id1",
            "attribute_assign_id2",
            "attribute_def_name_id1",
            "attribute_def_name_id2",
            "attribute_def_id1",
            "attribute_def_id2"
          ),
          GrouperUtil.toSet("name_of_attr_def_assigned_to: name of attribute_def originally assigned the attribute",
              "action1: the action associated with the original attribute assignment (default is assign)",
              "action2: the action associated with this attribute assignment (default is assign)",
              "attribute_def_name_name1: name of the original attribute definition name which is assigned to the group",
              "attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment",
              "attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute",
              "attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute",
              "name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group",
              "name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment",
              "attribute_assign_notes1: notes related to the original attribute assignment to the group",
              "attribute_assign_notes2: notes related to the new attribute assignment to the assignment",
              "id_of_attr_def_assigned_to: id of the attribute def assigned the attribute",
              "attribute_assign_id1: id of the original attribute assignment to the group",
              "attribute_assign_id2: id of the new attribute assignment to the assignment",
              "attribute_def_name_id1: id of the original attribute definition name assigned to the group",
              "attribute_def_name_id2: id of the new attribute definition name assigned to the assignment",
              "attribute_def_id1: id of the original attribute definition assigned to the group",
              "attribute_def_id2: id of the new attribute definition assigned to the attribute"
          ),
          "select gad.name as name_of_attr_def_assigned_to, " +
          "gaa1.action as action1, gaa2.action as action2, " +
          "gadn1.name as attribute_def_name_name1, " +
          "gadn2.name as attribute_def_name_name2, "
          + "gadn1.display_name as attribute_def_name_disp_name1, "
          + "gadn2.display_name as attribute_def_name_disp_name2, "
          + "gad1.name as name_of_attribute_def1, "
          + "gad2.name as name_of_attribute_def2, "
          + "gaa1.notes as attribute_assign_notes1, "
          + "gaa2.notes as attribute_assign_notes2, "
          + "gad.id as id_of_attr_def_assigned_to, "
          + "gaa1.id as attribute_assign_id1, "
          + "gaa2.id as attribute_assign_id2, "
          + "gadn1.id as attribute_def_name_id1, "
          + "gadn2.id as attribute_def_name_id2, "
          + "gad1.id as attribute_def_id1, "
          + "gad2.id as attribute_def_id2 "
          + "from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_attribute_def gad, "
          + "grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, "
          + "grouper_attribute_def gad2 "
          + "where gaa1.id = gaa2.owner_attribute_assign_id "
          + "and gaa1.attribute_def_name_id = gadn1.id "
          + "and gaa2.attribute_def_name_id = gadn2.id "
          + "and gadn1.attribute_def_id = gad1.id "
          + "and gadn2.attribute_def_id = gad2.id "
          + "and gaa1.enabled = 'T' and gaa2.enabled = 'T' " +
              "and gad.id = gaa1.owner_attribute_def_id");

    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_attr_def_priv_v", 
        "grouper_attr_def_priv_v: shows all privileges internal to grouper of attribute defs",
        GrouperUtil.toSet("subject_id", 
            "subject_source_id", 
            "field_name", 
            "attribute_def_name", 
            "attribute_def_description",
            "attribute_def_type", 
            "attribute_def_stem_id", 
            "attribute_def_id",
            "member_id", "field_id", 
            "immediate_membership_id", 
            "membership_id"),
        GrouperUtil.toSet("subject_id: of who has the priv", 
            "subject_source_id: source id of the subject with the priv", 
            "field_name: field name of priv, e.g. attrView, attrRead, attrAdmin, attrUpdate, attrOptin, attrOptout",
            "attribute_def_name: name of attribute definition",
            "attribute_def_description: description of the attribute def",
            "attribute_def_type: type of attribute, e.g. attribute, privilege, domain", 
            "attribute_def_stem_id: id of stem the attribute def is in",
            "attribute_def_id: id of the attribute definition", 
            "member_id: id of the subject in the members table",
            "field_id: id of the field of membership",
            "immediate_membership_id: id of the membership in the memberships table",
            "membership_id: id of the membership in the membership all view"
        ),
        "select distinct gm.subject_id, " +
        "gm.subject_source as subject_source_id,  "
        + "gf.name as field_name, " +
        		"gad.name as attribute_def_name, "
        + "gad.description as attribute_def_description,  "
        + "gad.attribute_def_type, "
        + "gad.stem_id as attribute_def_stem_id, "
        + "gad.id as attribute_def_id,  "
        + "gm.id as member_id, "
        + "gmav.field_id, " +
        		"gmav.immediate_membership_id, " +
        		"gmav.membership_id  "
        + "from grouper_memberships_all_v gmav, grouper_attribute_def gad, grouper_fields gf, grouper_members gm "
        + "where gmav.owner_attr_def_id = gad.id and gmav.field_id = gf.id "
        + "and gmav.immediate_mship_enabled = 'T' and gmav.member_id = gm.id ");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_perms_role_v", 
        "grouper_perms_role_v: shows all permissions assigned to users due to the users being in a role, and the role being assigned the permission",
        GrouperUtil.toSet("role_name", 
            "subject_source_id", 
            "subject_id",
            "action", 
            "attribute_def_name_name",
            "attribute_def_name_disp_name",
            "role_display_name",
            "role_id",
            "attribute_def_id",
            "member_id", 
            "attribute_def_name_id"
            ),
        GrouperUtil.toSet("role_name: name of the role that the user is in and that has the permission",
            "subject_source_id: source id of the subject which is in the role and thus has the permission",
            "subject_id: subject id of the subject which is in the role and thus has the permission",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "role_display_name: display name of role the subject is in, and that the permissions are assigned to",
            "role_id: id of role the subject is in, and that the permissions are assigned to",
            "attribute_def_id: id of the attribute definition",
            "member_id: id of the subject in the members table",
            "attribute_def_name_id: id of the attribute definition name"
        ),
        "select distinct gr.name as role_name, "
        + "gm.subject_source as subject_source_id, "
        + "gm.subject_id, "
        + "gaa.action as action, "
        + "gadn.name as attribute_def_name_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gr.display_name as role_display_name, "
        + "gr.id as role_id, "
        + "gadn.attribute_def_id, "
        + "gm.id as member_id, "
        + "gadn.id as attribute_def_name_id "
        + "from grouper_groups gr, "
        + "grouper_memberships_all_v gmav, "
        + "grouper_members gm, "
        + "grouper_fields gf, "
        + "grouper_role_set grs, " 
        + "grouper_attribute_assign gaa, "
        + "grouper_attribute_def_name gadn "
        + "where gr.type_of_group = 'role' "
        + "and gmav.owner_group_id = gr.id "
        + "and gmav.field_id = gf.id "
        + "and gf.type = 'list' "
        + "and gf.name = 'members' "
        + "and gmav.immediate_mship_enabled = 'T' "
        + "and gmav.member_id = gm.id "
        + "and grs.if_has_role_id = gr.id "
        + "and gaa.owner_group_id = grs.then_has_role_id "
        + "and gaa.enabled = 'T' "
        + "and gaa.attribute_def_name_id = gadn.id");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_perms_role_subject_v", 
        "grouper_perms_role_subject_v: shows all permissions assigned to users directly while in a role",
        GrouperUtil.toSet("role_name", 
            "subject_source_id", 
            "subject_id",
            "action", 
            "attribute_def_name_name",
            "attribute_def_name_disp_name",
            "role_display_name",
            "role_id",
            "attribute_def_id",
            "member_id", 
            "attribute_def_name_id"
            ),
        GrouperUtil.toSet("role_name: name of the role that the user is in and that has the permission",
            "subject_source_id: source id of the subject which is in the role and thus has the permission",
            "subject_id: subject id of the subject which is in the role and thus has the permission",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "role_display_name: display name of role the subject is in, and that the permissions are assigned to",
            "role_id: id of role the subject is in, and that the permissions are assigned to",
            "attribute_def_id: id of the attribute definition",
            "member_id: id of the subject in the members table",
            "attribute_def_name_id: id of the attribute definition name"
        ),
        "select distinct gr.name as role_name, "
        + "gm.subject_source as subject_source_id, "
        + "gm.subject_id, "
        + "gaa.action as action, "
        + "gadn.name as attribute_def_name_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gr.display_name as role_display_name, "
        + "gr.id as role_id, "
        + "gadn.attribute_def_id, "
        + "gm.id as member_id, "
        + "gadn.id as attribute_def_name_id "
        + "from grouper_groups gr, "
        + "grouper_memberships_all_v gmav, "
        + "grouper_members gm, "
        + "grouper_fields gf, "
        + "grouper_attribute_assign gaa, " 
        + "grouper_attribute_def_name gadn "
        + "where gr.type_of_group = 'role' "
        + "and gmav.owner_group_id = gr.id "
        + "and gmav.field_id = gf.id "
        + "and gmav.owner_group_id = gaa.owner_group_id "
        + "and gmav.member_id = gaa.owner_member_id "
        + "and gf.type = 'list' "
        + "and gf.name = 'members' "
        + "and gmav.immediate_mship_enabled = 'T' "
        + "and gmav.member_id = gm.id "
        + "and gaa.enabled = 'T' "
        + "and gaa.attribute_def_name_id = gadn.id");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_perms_all_v", 
        "grouper_perms_all_v: shows all permissions assigned to users directly while in a role, or assigned to roles (and users in the role)",
        GrouperUtil.toSet("role_name", 
            "subject_source_id", 
            "subject_id",
            "action", 
            "attribute_def_name_name",
            "attribute_def_name_disp_name",
            "role_display_name",
            "role_id",
            "attribute_def_id",
            "member_id", 
            "attribute_def_name_id"
            ),
        GrouperUtil.toSet("role_name: name of the role that the user is in and that has the permission",
            "subject_source_id: source id of the subject which is in the role and thus has the permission",
            "subject_id: subject id of the subject which is in the role and thus has the permission",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "role_display_name: display name of role the subject is in, and that the permissions are assigned to",
            "role_id: id of role the subject is in, and that the permissions are assigned to",
            "attribute_def_id: id of the attribute definition",
            "member_id: id of the subject in the members table",
            "attribute_def_name_id: id of the attribute definition name"
        ),
        "select role_name, " + 
        "subject_source_id, " + 
        "subject_id, " +
        "action, " +
        "attribute_def_name_name, " + 
        "attribute_def_name_disp_name, " + 
        "role_display_name, " +
        "role_id, " +
        "attribute_def_id, " + 
        "member_id, " +
        "attribute_def_name_id " +
        "from grouper_perms_role_v " +
        "union " +
        "select role_name, " +
        "subject_source_id, " +
        "subject_id, " +
        "action, " +
        "attribute_def_name_name, " + 
        "attribute_def_name_disp_name, " + 
        "role_display_name, " +
        "role_id, " +
        "attribute_def_id, " + 
        "member_id, " +
        "attribute_def_name_id " +
        "from grouper_perms_role_subject_v ");

  }

  /**
   * an example table name so we can hone in on the exact metadata
   * @return the table name
   */
  public String[] getSampleTablenames() {
    return new String[]{"grouper_groups", "grouper_ddl", "grouper_attributes", "grouper_composites",
        "grouper_fields", "grouper_groups_types", "grouper_loader_log", "grouper_members", "grouper_memberships", 
        "grouper_stems", "grouper_types"};
  }
  
  /**
   * @param database
   */
  private static void addContextIdColsLoader(Database database) {
    Table loaderLogTable = GrouperDdlUtils.ddlutilsFindTable(database, 
        Hib3GrouperLoaderLog.TABLE_GROUPER_LOADER_LOG);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderLogTable, COLUMN_CONTEXT_ID, 
        Types.VARCHAR, "128", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Hib3GrouperLoaderLog.TABLE_GROUPER_LOADER_LOG,
        "loader_context_idx", false, COLUMN_CONTEXT_ID);
  }

  /**
   * @param database
   */
  private static void addContextIdCols(Database database) {
    {
      Table attributeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          Attribute.TABLE_GROUPER_ATTRIBUTES);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Attribute.TABLE_GROUPER_ATTRIBUTES,
          "attribute_context_idx", false, COLUMN_CONTEXT_ID);
    }
    
    {
      Table compositeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          Composite.TABLE_GROUPER_COMPOSITES);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(compositeTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);
 
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Composite.TABLE_GROUPER_COMPOSITES,
          "composite_context_idx", false, COLUMN_CONTEXT_ID);
    }
    
    {
      Table fieldsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          Field.TABLE_GROUPER_FIELDS);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(fieldsTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);
 
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Field.TABLE_GROUPER_FIELDS,
          "fields_context_idx", false, COLUMN_CONTEXT_ID);
    }
    
    {
      Table groupsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Group.TABLE_GROUPER_GROUPS);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Group.TABLE_GROUPER_GROUPS,
          "group_context_idx", false, COLUMN_CONTEXT_ID);
    }
    
    {
      Table groupsTypesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          GroupTypeTuple.TABLE_GROUPER_GROUPS_TYPES);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTypesTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, GroupTypeTuple.TABLE_GROUPER_GROUPS_TYPES,
          "grouptypetuple_context_idx", false, COLUMN_CONTEXT_ID);
    }

    {
      Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Member.TABLE_GROUPER_MEMBERS);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Member.TABLE_GROUPER_MEMBERS,
          "member_context_idx", false, COLUMN_CONTEXT_ID);
    }
    
    {
      Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Membership.TABLE_GROUPER_MEMBERSHIPS);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Membership.TABLE_GROUPER_MEMBERSHIPS,
          "membership_context_idx", false, COLUMN_CONTEXT_ID);
    }
    
    {
      Table stemsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          Stem.TABLE_GROUPER_STEMS);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false); 
 
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Stem.TABLE_GROUPER_STEMS,
          "stem_context_idx", false, COLUMN_CONTEXT_ID);
    }

    {
      Table typesTable = GrouperDdlUtils.ddlutilsFindTable(database, 
          GroupType.TABLE_GROUPER_TYPES);
 
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(typesTable, COLUMN_CONTEXT_ID, 
          Types.VARCHAR, "128", false, false); 
 
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, GroupType.TABLE_GROUPER_TYPES,
          "type_context_idx", false, COLUMN_CONTEXT_ID);
    }
  }

  /**
   * @param database
   * @param ddlVersionBean 
   * @param requireNewMembershipColumns 
   */
  private static void runMembershipAndGroupSetConversion(Database database, 
      @SuppressWarnings("unused") DdlVersionBean ddlVersionBean,
      boolean requireNewMembershipColumns) {

    Table membershipsTable = GrouperDdlUtils.ddlutilsFindTable(database, 
        Membership.TABLE_GROUPER_MEMBERSHIPS);
    GrouperDdlUtils.ddlutilsDropIndexes(membershipsTable, Membership.COLUMN_VIA_ID);
    GrouperDdlUtils.ddlutilsDropIndexes(membershipsTable, Membership.COLUMN_OWNER_ID);
    GrouperDdlUtils.ddlutilsDropIndexes(membershipsTable, Membership.COLUMN_DEPTH);
    GrouperDdlUtils.ddlutilsDropIndexes(membershipsTable, Membership.COLUMN_PARENT_MEMBERSHIP);
        
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_GROUP_ID, 
        Types.VARCHAR, "128", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_owner_group_idx", false, Membership.COLUMN_OWNER_GROUP_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_STEM_ID, 
        Types.VARCHAR, "128", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_owner_stem_idx", false, Membership.COLUMN_OWNER_STEM_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_ATTR_DEF_ID, 
        Types.VARCHAR, "128", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_owner_attr_idx", false, Membership.COLUMN_OWNER_ATTR_DEF_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_VIA_COMPOSITE_ID, 
        Types.VARCHAR, "128", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_via_composite_idx", false, Membership.COLUMN_VIA_COMPOSITE_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_member_cvia_idx", false, "member_id", Membership.COLUMN_VIA_COMPOSITE_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_GROUP_ID_NULL, 
        Types.VARCHAR, "128", false, requireNewMembershipColumns, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_STEM_ID_NULL, 
        Types.VARCHAR, "128", false, requireNewMembershipColumns, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable, Membership.COLUMN_OWNER_ATTR_DEF_ID_NULL, 
        Types.VARCHAR, "128", false, requireNewMembershipColumns, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_gowner_member_idx", false, Membership.COLUMN_OWNER_GROUP_ID, "member_id",
        "field_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_sowner_member_idx", false, Membership.COLUMN_OWNER_STEM_ID, "member_id",
        "field_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_aowner_member_idx", false, Membership.COLUMN_OWNER_ATTR_DEF_ID, "member_id",
        "field_id");
    
    // add columns for membership expiration
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable,
        Membership.COLUMN_ENABLED, Types.VARCHAR, "1", false, requireNewMembershipColumns, "T");
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable,
        Membership.COLUMN_ENABLED_TIMESTAMP, Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membershipsTable,
        Membership.COLUMN_DISABLED_TIMESTAMP, Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_enabled_idx", false, Membership.COLUMN_ENABLED);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_enabled_time_idx", false, Membership.COLUMN_ENABLED_TIMESTAMP);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_disabled_time_idx", false, Membership.COLUMN_DISABLED_TIMESTAMP);
    
    addGroupSetTable(database);
  }

  /**
   * 
   * @param database
   */
  private static void addGroupSetTable(Database database) {
    Table grouperGroupSet = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GroupSet.TABLE_GROUPER_GROUP_SET);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "id", 
        Types.VARCHAR, "40", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "owner_attr_def_id",
        Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "owner_attr_def_id_null",
        Types.VARCHAR, "40", false, true, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "owner_group_id",
        Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "owner_group_id_null",
        Types.VARCHAR, "40", false, true, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "owner_stem_id",
        Types.VARCHAR, "40", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "owner_stem_id_null",
        Types.VARCHAR, "40", false, true, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "member_attr_def_id",
        Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "member_attr_def_id_null",
        Types.VARCHAR, "40", false, true, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "member_group_id",
        Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "member_group_id_null",
        Types.VARCHAR, "40", false, true, GroupSet.nullColumnValue);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "member_stem_id",
        Types.VARCHAR, "40", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "member_stem_id_null",
        Types.VARCHAR, "40", false, true, GroupSet.nullColumnValue);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "field_id",
        Types.VARCHAR, "40", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "mship_type",
        Types.VARCHAR, "16", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "depth",
        Types.INTEGER, "11", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "via_group_id",
        Types.VARCHAR, "40", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "parent_id",
        Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "creator_id",
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "create_time",
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "context_id", 
        Types.VARCHAR, "40", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperGroupSet, "hibernate_version_number", 
        Types.INTEGER, null, false, false);
    
    // field_id doesn't need to be in the unique index, but i'm adding it so that we can
    // set parent_id to null before removing self groupSets without getting a constraint
    // violiations.
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperGroupSet.getName(), 
        "group_set_uniq_idx", true, "owner_attr_def_id_null", "owner_group_id_null", 
        "owner_stem_id_null", "member_attr_def_id_null", "member_group_id_null",
        "member_stem_id_null", "mship_type", "field_id", "parent_id");
  }

  /**
   * @param database
   * @param ddlVersionBean 
   * @param attributeTable
   */
  private static void addAttributeFieldIndexes(Database database, DdlVersionBean ddlVersionBean,  Table attributeTable) {
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeTable.getName(), "attribute_uniq_idx", true, "group_id", Attribute.COLUMN_FIELD_ID);
    
    //see if we have a custom script here, do this since some versions of mysql cant handle indexes on columns that large
    String scriptOverride = ddlVersionBean.isMysql() ? "\nCREATE INDEX attribute_field_value_idx " +
        "ON grouper_attributes (field_id, value(333));\n" : null;
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, attributeTable.getName(), "attribute_field_value_idx", 
        scriptOverride, false, Attribute.COLUMN_FIELD_ID, "value");
  }

  /**
   * @param database
   * @param membershipsTable
   */
  private static void addMembershipFieldIndexes(Database database, Table membershipsTable) {
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_member_list_idx", false, "member_id", "field_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_gowner_field_type_idx", false, Membership.COLUMN_OWNER_GROUP_ID, "field_id", "mship_type");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_sowner_field_type_idx", false, Membership.COLUMN_OWNER_STEM_ID, "field_id", "mship_type");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_gowner_member_idx", false, Membership.COLUMN_OWNER_GROUP_ID, "member_id",
        "field_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_sowner_member_idx", false, Membership.COLUMN_OWNER_STEM_ID, "member_id",
        "field_id");

  }
  
  /**
   * @param database
   * @param membershipsTable
   */
  private static void addMembershipUniqueIndex(Database database, Table membershipsTable) {
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membershipsTable.getName(), 
        "membership_uniq_idx", true, Membership.COLUMN_OWNER_GROUP_ID_NULL, Membership.COLUMN_OWNER_STEM_ID_NULL,
        Membership.COLUMN_OWNER_ATTR_DEF_ID_NULL, Membership.COLUMN_MEMBER_ID, Membership.COLUMN_FIELD_ID);
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl.class);

  /** set to false when testing if shouldnt add the group columns e.g. name */
  static boolean addGroupNameColumns = true;
  
  /**
   * add group name columns if supposed to
   * @param ddlVersionBean
   * @param database
   */
  private static void addGroupNameColumns(@SuppressWarnings("unused") DdlVersionBean ddlVersionBean, Database database) {
    
    if (!addGroupNameColumns) {
      return;
    }

    Table groupsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        Group.TABLE_GROUPER_GROUPS);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "name", 
        Types.VARCHAR, "1024", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "display_name", 
        Types.VARCHAR, "1024", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "extension", 
        Types.VARCHAR, "255", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "display_extension", 
        Types.VARCHAR, "255", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupsTable, "description", 
        Types.VARCHAR, "1024", false, false);

  }
  
  /**
   * add audit tables in v2 or when needed
   * @param ddlVersionBean
   * @param database
   */
  private static void addAuditTables(DdlVersionBean ddlVersionBean, Database database) {
    {
      Table grouperAuditTypeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          AuditType.TABLE_GROUPER_AUDIT_TYPE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "action_name", 
          Types.VARCHAR, "50", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "audit_category", 
          Types.VARCHAR, "50", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "context_id", 
          Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "created_on", 
          Types.BIGINT, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "hibernate_version_number", 
          Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "id", 
          Types.VARCHAR, "128", true, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_int01", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_int02", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_int03", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_int04", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_int05", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string01", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string02", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string03", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string04", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string05", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string06", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string07", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "label_string08", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditTypeTable, "last_updated", 
          Types.BIGINT, "20", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperAuditTypeTable.getName(), 
          "audit_type_category_type_idx", true, "audit_category", "action_name");

    }
    
    {
      Table grouperAuditEntryTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          AuditEntry.TABLE_GROUPER_AUDIT_ENTRY);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "act_as_member_id", Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "audit_type_id", Types.VARCHAR, "128", false, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "context_id", Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "created_on", Types.BIGINT, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "description", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "env_name", Types.VARCHAR, "50", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "grouper_engine", Types.VARCHAR, "50", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "grouper_version", Types.VARCHAR, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "hibernate_version_number", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "id", Types.VARCHAR, "128", true, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "int01", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "int02", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "int03", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "int04", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "int05", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "last_updated", Types.BIGINT, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "logged_in_member_id", Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "server_host", Types.VARCHAR, "50", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string01", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string02", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string03", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string04", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string05", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string06", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string07", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "string08", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "user_ip_address", Types.VARCHAR, "50", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "duration_microseconds", Types.INTEGER, null, false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "query_count", Types.INTEGER, null, false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperAuditEntryTable, 
          "server_user_name", Types.VARCHAR, "50", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperAuditEntryTable.getName(), 
          "audit_entry_act_as_idx", false, "act_as_member_id");

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperAuditEntryTable.getName(), 
          "audit_entry_type_idx", false, "audit_type_id");

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperAuditEntryTable.getName(), 
          "audit_entry_context_idx", false, "context_id");

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperAuditEntryTable.getName(), 
          "audit_entry_logged_in_idx", false, "logged_in_member_id");

      //do 5 string indexes, probably dont need them on the other string cols
      for (int i=1;i<=5;i++) {
        //see if we have a custom script here, do this since some versions of mysql cant handle indexes on columns that large
        String scriptOverride = ddlVersionBean.isMysql() ? "\nCREATE INDEX audit_entry_string0" + i + "_idx " +
            "ON grouper_audit_entry (string0" + i + "(333));\n" : null;
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, grouperAuditEntryTable.getName(), 
            "audit_entry_string0" + i + "_idx", scriptOverride, false, "string0" + i);
        
      }
      
    }

  }
  
  /**
   * add change log tables in v2 or when needed
   * @param ddlVersionBean
   * @param database
   */
  private static void addChangeLogTables(DdlVersionBean ddlVersionBean, Database database) {
    {
      Table grouperChangeLogTypeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          ChangeLogType.TABLE_GROUPER_CHANGE_LOG_TYPE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "action_name", 
          Types.VARCHAR, "50", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "change_log_category", 
          Types.VARCHAR, "50", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "context_id", 
          Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "created_on", 
          Types.BIGINT, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "hibernate_version_number", 
          Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "id", 
          Types.VARCHAR, "128", true, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string01", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string02", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string03", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string04", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string05", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string06", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string07", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string08", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string09", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string10", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string11", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "label_string12", 
          Types.VARCHAR, "50", false, false); 
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTypeTable, "last_updated", 
          Types.BIGINT, "20", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperChangeLogTypeTable.getName(), 
          "change_log_type_cat_type_idx", true, "change_log_category", "action_name");

    }
    {
      Table grouperChangeLogConsumerTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          ChangeLogConsumer.TABLE_GROUPER_CHANGE_LOG_CONSUMER);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogConsumerTable, "name", 
          Types.VARCHAR, "100", false, true); 
  
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogConsumerTable, "last_sequence_processed", 
          Types.BIGINT, "20", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogConsumerTable, "last_updated", 
          Types.BIGINT, "20", false, false); 
  
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogConsumerTable, "created_on", 
          Types.BIGINT, "20", false, false); 
  
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogConsumerTable, "id", 
          Types.VARCHAR, "128", true, true); 
  
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogConsumerTable, 
          "hibernate_version_number", Types.INTEGER, null, false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperChangeLogConsumerTable.getName(), 
          "change_log_consumer_name_idx", true, "name");
  
    }

    {
      Table grouperChangeLogTempEntryTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          ChangeLogEntry.TABLE_GROUPER_CHANGE_LOG_ENTRY_TEMP);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "change_log_type_id", Types.VARCHAR, "128", false, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "context_id", Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "created_on", Types.BIGINT, "20", false, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string01", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string02", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string03", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string04", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string05", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string06", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string07", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string08", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string09", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string10", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string11", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogTempEntryTable, 
          "string12", Types.VARCHAR, "4000", false, false); 

    }

    {
      Table grouperChangeLogEntryTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          ChangeLogEntry.TABLE_GROUPER_CHANGE_LOG_ENTRY);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "change_log_type_id", Types.VARCHAR, "128", false, true); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "context_id", Types.VARCHAR, "128", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "created_on", Types.BIGINT, "20", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "sequence_number", Types.BIGINT, "20", true, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string01", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string02", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string03", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string04", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string05", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string06", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string07", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string08", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string09", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string10", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string11", Types.VARCHAR, "4000", false, false); 

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperChangeLogEntryTable, 
          "string12", Types.VARCHAR, "4000", false, false); 

      //do 12 string indexes
      for (int i=1;i<=12;i++) {
        //see if we have a custom script here, do this since some versions of mysql cant handle indexes on columns that large
        String scriptOverride = ddlVersionBean.isMysql() ? "\nCREATE INDEX change_log_entry_string" + StringUtils.leftPad(i + "", 2, '0') + "_idx " +
            "ON grouper_change_log_entry (string" + StringUtils.leftPad(i + "", 2, '0') + "(333));\n" : null;
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, grouperChangeLogEntryTable.getName(), 
            "change_log_entry_string" + StringUtils.leftPad(i + "", 2, '0') 
            + "_idx", scriptOverride, false, "string" + StringUtils.leftPad(i + "", 2, '0'));
        
      }

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, grouperChangeLogEntryTable.getName(), 
          "change_log_sequence_number_idx", null, false, "sequence_number", "created_on");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, grouperChangeLogEntryTable.getName(), 
          "change_log_context_id_idx", null, false, "context_id");
      
    }

  }

  /**
   * add privilege management in v2 or when needed
   * @param ddlVersionBean
   * @param database
   * @param groupsTableNew 
   */
  private static void addPrivilegeManagement(DdlVersionBean ddlVersionBean, 
      Database database, boolean groupsTableNew) {
    {
      Table groupTable = GrouperDdlUtils.ddlutilsFindTable(database, Group.TABLE_GROUPER_GROUPS);
      
      boolean columnNew = !groupsTableNew && groupTable.findColumn("type_of_group") == null;
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "type_of_group", 
          Types.VARCHAR, "10", false, groupsTableNew, "group"); 
      
      //see if we need to add a script on top to massage data
      if (columnNew) {
        ddlVersionBean.appendAdditionalScriptUnique(
          "\nupdate grouper_groups set type_of_group = 'group' where " +
      		"type_of_group is null;\ncommit;\n");
      }
    }
    
    {
      Table attributeDefTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_ACTIONS, Types.VARCHAR, "512", false, true, "assign");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_ASSIGNABLE_TO, Types.VARCHAR, "32", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_ATTRIBUTE_DEF_PUBLIC, Types.VARCHAR, "1", false, true, "F");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_ATTRIBUTE_DEF_TYPE, Types.VARCHAR, "32", false, true, "attr");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_CREATOR_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable, AttributeDef.COLUMN_HIBERNATE_VERSION_NUMBER, 
          Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_DESCRIPTION, Types.VARCHAR, "1024", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_EXTENSION, Types.VARCHAR, "255", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_NAME, Types.VARCHAR, "1024", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_MULTI_ASSIGNABLE, Types.VARCHAR, "1", false, true, "F");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_MULTI_VALUED, Types.VARCHAR, "1", false, true, "F");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_STEM_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefTable,
          AttributeDef.COLUMN_VALUE_TYPE, Types.VARCHAR, "32", false, true, "marker");
          
      String scriptOverrideName = ddlVersionBean.isMysql() ? "\nCREATE unique INDEX attribute_def_name_idx " +
          "ON grouper_attribute_def (name(333));\n" : null;
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, attributeDefTable.getName(), 
          "attribute_def_name_idx", scriptOverrideName, true, "name");
      
    }

    {
      Table attributeDefNameTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, AttributeDefName.TABLE_GROUPER_ATTRIBUTE_DEF_NAME);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable, 
          AttributeDefName.COLUMN_HIBERNATE_VERSION_NUMBER, 
          Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_DESCRIPTION, Types.VARCHAR, "1024", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_EXTENSION, Types.VARCHAR, "255", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_NAME, Types.VARCHAR, "1024", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_STEM_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_ATTRIBUTE_DEF_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_DISPLAY_EXTENSION, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
          AttributeDefName.COLUMN_DISPLAY_NAME, Types.VARCHAR, "1024", false, true);

      String scriptOverrideName = ddlVersionBean.isMysql() ? "\nCREATE unique INDEX attribute_def_name_name_idx " +
          "ON grouper_attribute_def_name (name(333));\n" : null;
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, attributeDefNameTable.getName(), 
          "attribute_def_name_name_idx", scriptOverrideName, true, "name");
      
    }

    {
      Table attributeAssignTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, AttributeAssign.TABLE_GROUPER_ATTRIBUTE_ASSIGN);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_ACTION, Types.VARCHAR, "32", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_ATTRIBUTE_DEF_NAME_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_DISABLED_TIME, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_ENABLED, Types.VARCHAR, "1", false, true, "T");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_ENABLED_TIME, Types.VARCHAR, "1", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable, 
          AttributeAssign.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_NOTES, Types.VARCHAR, "1024", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_OWNER_ATTRIBUTE_ASSIGN_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_OWNER_ATTRIBUTE_DEF_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_OWNER_GROUP_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_OWNER_MEMBER_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_OWNER_MEMBERSHIP_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignTable,
          AttributeAssign.COLUMN_OWNER_STEM_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attribute_asgn_attr_name_idx", false, 
          AttributeAssign.COLUMN_ATTRIBUTE_DEF_NAME_ID, AttributeAssign.COLUMN_ACTION);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attr_asgn_own_asgn_idx", false, 
          AttributeAssign.COLUMN_OWNER_ATTRIBUTE_ASSIGN_ID, AttributeAssign.COLUMN_ACTION);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attr_asgn_own_def_idx", false, 
          AttributeAssign.COLUMN_OWNER_ATTRIBUTE_DEF_ID, AttributeAssign.COLUMN_ACTION);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attr_asgn_own_group_idx", false, 
          AttributeAssign.COLUMN_OWNER_GROUP_ID, AttributeAssign.COLUMN_ACTION);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attr_asgn_own_mem_idx", false, 
          AttributeAssign.COLUMN_OWNER_MEMBER_ID, AttributeAssign.COLUMN_ACTION);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attr_asgn_own_mship_idx", false, 
          AttributeAssign.COLUMN_OWNER_MEMBERSHIP_ID, AttributeAssign.COLUMN_ACTION);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
          "attr_asgn_own_stem_idx", false, 
          AttributeAssign.COLUMN_OWNER_STEM_ID, AttributeAssign.COLUMN_ACTION);

    }

    {
      Table attributeAssignValueTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, AttributeAssignValue.TABLE_GROUPER_ATTRIBUTE_ASSIGN_VALUE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_ATTRIBUTE_ASSIGN_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable, 
          AttributeAssignValue.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_VALUE_INTEGER, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_VALUE_STRING, Types.VARCHAR, "4000", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeAssignValueTable,
          AttributeAssignValue.COLUMN_VALUE_MEMBER_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignValueTable.getName(), 
          "attribute_val_assign_idx", false, 
          AttributeAssignValue.COLUMN_ATTRIBUTE_ASSIGN_ID);

      {
        String scriptOverrideName = ddlVersionBean.isMysql() ? "\nCREATE INDEX attribute_val_string_idx " +
            "ON grouper_attribute_assign_value (value_string(333));\n" : null;
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, attributeAssignValueTable.getName(), 
            "attribute_val_string_idx", scriptOverrideName, false, AttributeAssignValue.COLUMN_VALUE_STRING);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignValueTable.getName(), 
          "attribute_val_integer_idx", false, 
          AttributeAssignValue.COLUMN_VALUE_INTEGER);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignValueTable.getName(), 
          "attribute_val_member_id_idx", false, 
          AttributeAssignValue.COLUMN_VALUE_MEMBER_ID);

    }

    {
      Table attributeDefScopeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, AttributeDefScope.TABLE_GROUPER_ATTRIBUTE_DEF_SCOPE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_ATTRIBUTE_DEF_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable, 
          AttributeDefScope.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_ATTRIBUTE_DEF_SCOPE_TYPE, Types.VARCHAR, "32", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_SCOPE_STRING, Types.VARCHAR, "1024", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefScopeTable,
          AttributeDefScope.COLUMN_SCOPE_STRING2, Types.VARCHAR, "1024", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeDefScopeTable.getName(), 
          "attribute_def_scope_atdef_idx", false, 
          AttributeDefScope.COLUMN_ATTRIBUTE_DEF_ID);

    }

    {
      Table attributeDefNameSetTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, AttributeDefNameSet.TABLE_GROUPER_ATTRIBUTE_DEF_NAME_SET);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable, 
          AttributeDefNameSet.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_DEPTH, Types.BIGINT, "10", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_IF_HAS_ATTRIBUTE_DEF_NAME_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_THEN_HAS_ATTRIBUTE_DEF_NAME_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_PARENT_ATTR_DEF_NAME_SET_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameSetTable,
          AttributeDefNameSet.COLUMN_TYPE, Types.VARCHAR, "32", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeDefNameSetTable.getName(), 
          "attr_def_name_set_ifhas_idx", false, 
          AttributeDefNameSet.COLUMN_IF_HAS_ATTRIBUTE_DEF_NAME_ID);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeDefNameSetTable.getName(), 
          "attr_def_name_set_then_idx", false, 
          AttributeDefNameSet.COLUMN_THEN_HAS_ATTRIBUTE_DEF_NAME_ID);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeDefNameSetTable.getName(), 
          "attr_def_name_set_unq_idx", true, 
          AttributeDefNameSet.COLUMN_PARENT_ATTR_DEF_NAME_SET_ID, 
          AttributeDefNameSet.COLUMN_IF_HAS_ATTRIBUTE_DEF_NAME_ID, AttributeDefNameSet.COLUMN_THEN_HAS_ATTRIBUTE_DEF_NAME_ID);

    }

    {
      Table roleSetTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(
          database, RoleSet.TABLE_GROUPER_ROLE_SET);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_CONTEXT_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_CREATED_ON, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable, 
          RoleSet.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, "12", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_ID, Types.VARCHAR, "128", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_LAST_UPDATED, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_DEPTH, Types.BIGINT, "10", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_IF_HAS_ROLE_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_THEN_HAS_ROLE_ID, Types.VARCHAR, "128", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_PARENT_ROLE_SET_ID, Types.VARCHAR, "128", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleSetTable,
          RoleSet.COLUMN_TYPE, Types.VARCHAR, "32", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, roleSetTable.getName(), 
          "role_set_ifhas_idx", false, 
          RoleSet.COLUMN_IF_HAS_ROLE_ID);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, roleSetTable.getName(), 
          "role_set_then_idx", false, 
          RoleSet.COLUMN_THEN_HAS_ROLE_ID);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, roleSetTable.getName(), 
          "role_set_unq_idx", true, 
          RoleSet.COLUMN_PARENT_ROLE_SET_ID, 
          RoleSet.COLUMN_IF_HAS_ROLE_ID, RoleSet.COLUMN_THEN_HAS_ROLE_ID);

    }

  }
}
