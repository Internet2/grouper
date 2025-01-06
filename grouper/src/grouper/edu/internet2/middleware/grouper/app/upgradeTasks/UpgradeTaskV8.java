package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.Set;

import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class UpgradeTaskV8 implements UpgradeTasksInterface {

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

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    boolean columnNullable = GrouperDdlUtils.isColumnNullable("grouper_members", "id_index", "subject_id", "GrouperSystem");
    return columnNullable;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.0.0");
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
