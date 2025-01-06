package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV27 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.14.0");
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public boolean runOnNewInstall() {
    return true;
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp")) {
      return true;
    }
    
    if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp_utc")) {
      return true;
    }
    
    return false;
    
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (GrouperDdlUtils.isPostgres()) {
          // REGISTER THIS FUNCTION IN GrouperDdlCompare.analyzeFunctions()
          if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp")) {
            new GcDbAccess().sql("""
                CREATE FUNCTION grouper_to_timestamp(bigint) RETURNS timestamp AS $$
                    DECLARE
                        timestamp_value bigint;
                    BEGIN
                        timestamp_value := CASE
                            WHEN $1 > 100000000000000 THEN $1 / 1000000
                            ELSE $1 / 1000
                        END;
                        RETURN to_timestamp(timestamp_value);
                    END;
                $$ LANGUAGE plpgsql IMMUTABLE RETURNS NULL ON NULL INPUT;               
                """).executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added function grouper_to_timestamp");
            }
          }
          // REGISTER THIS FUNCTION IN GrouperDdlCompare.analyzeFunctions()
          if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp_utc")) {
            new GcDbAccess().sql("""
                CREATE FUNCTION grouper_to_timestamp_utc(bigint) RETURNS timestamp AS $$
                    DECLARE
                        timestamp_value bigint;
                    BEGIN
                        timestamp_value := CASE
                            WHEN $1 > 100000000000000 THEN $1 / 1000000
                            ELSE $1 / 1000
                        END;
                        RETURN to_timestamp(timestamp_value) AT TIME ZONE 'UTC';
                    END;
                $$ LANGUAGE plpgsql IMMUTABLE RETURNS NULL ON NULL INPUT;           
                """).executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added function grouper_to_timestamp_utc");
            }
          }
        } else if (GrouperDdlUtils.isMysql()) {
          // REGISTER THIS FUNCTION IN GrouperDdlCompare.analyzeFunctions()
          if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp")) {
            new GcDbAccess().sql("""
                CREATE FUNCTION grouper_to_timestamp(input BIGINT)
                RETURNS DATETIME
                DETERMINISTIC 
                NO SQL
                BEGIN
                    DECLARE timestamp_value BIGINT;

                    IF input > 100000000000000 THEN
                        SET timestamp_value = input / 1000000;
                    ELSE
                        SET timestamp_value = input / 1000;
                    END IF;

                    RETURN FROM_UNIXTIME(timestamp_value);
                END; -- function grouper_to_timestamp
                """).executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created function grouper_to_timestamp if it didn't exist");
            }
          }
          // REGISTER THIS FUNCTION IN GrouperDdlCompare.analyzeFunctions()
          if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp_utc")) {
            new GcDbAccess().sql("""
                CREATE FUNCTION IF NOT EXISTS grouper_to_timestamp_utc(input BIGINT)
                RETURNS DATETIME
                DETERMINISTIC
                NO SQL
                BEGIN
                    DECLARE timestamp_value BIGINT;
        
                    IF input > 100000000000000 THEN
                        SET timestamp_value = input / 1000000;
                    ELSE
                        SET timestamp_value = input / 1000;
                    END IF;

                    RETURN CONVERT_TZ(FROM_UNIXTIME(timestamp_value), @@session.time_zone,'+00:00');
                END; -- function grouper_to_timestamp_utc
                """).executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created function grouper_to_timestamp_utc if it didn't exist");
            }
          }
          
        } else if (GrouperDdlUtils.isOracle()) {
          // REGISTER THIS FUNCTION IN GrouperDdlCompare.analyzeFunctions()
          if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp")) {
            new GcDbAccess().sql("""
                CREATE FUNCTION grouper_to_timestamp(input IN NUMBER)
                RETURN TIMESTAMP IS
                    timestamp_value NUMBER;
                BEGIN
                    IF input > 100000000000000 THEN
                        timestamp_value := input / 1000000;
                    ELSE
                        timestamp_value := input / 1000;
                    END IF;
                    RETURN (timestamp '1970-01-01 00:00:00.000 UTC' + numtodsinterval(timestamp_value,'SECOND')) AT TIME ZONE SESSIONTIMEZONE;
                END FUNCTION grouper_to_timestamp;     
                """).executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", replaced function grouper_to_timestamp");
            }
          }
          // REGISTER THIS FUNCTION IN GrouperDdlCompare.analyzeFunctions()
          if (!GrouperDdlUtils.doesFunctionExist("grouper_to_timestamp_utc")) {
            new GcDbAccess().sql("""
                CREATE OR REPLACE FUNCTION grouper_to_timestamp_utc(input IN NUMBER)
                RETURN TIMESTAMP IS
                    timestamp_value NUMBER;
                BEGIN
                    IF input > 100000000000000 THEN
                        timestamp_value := input / 1000000;
                    ELSE
                        timestamp_value := input / 1000;
                    END IF;
                    RETURN (timestamp '1970-01-01 00:00:00.000 UTC' + numtodsinterval(timestamp_value,'SECOND')) AT TIME ZONE 'UTC';
                END FUNCTION grouper_to_timestamp_utc;
                """).executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", replaced function grouper_to_timestamp_utc");
            }
          }
        }
        
        return null;
      }
    });
  }

}
