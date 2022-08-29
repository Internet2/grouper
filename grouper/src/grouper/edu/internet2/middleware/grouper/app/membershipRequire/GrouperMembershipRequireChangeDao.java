package edu.internet2.middleware.grouper.app.membershipRequire;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * dao for membership require logs
 * @author mchyzer
 *
 */
public class GrouperMembershipRequireChangeDao {

  /**
   * 
   */
  public GrouperMembershipRequireChangeDao() {
  }

  /**
   * 
   * @param grouperMembershipRequireChange 
   * @return true if changed
   */
  public static boolean store(GrouperMembershipRequireChange grouperMembershipRequireChange) {
    grouperMembershipRequireChange.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(grouperMembershipRequireChange);
    return changed;
  }

  /**
   * select grouper sync by id
   * @param id
   * @return the sync
   */
  public static GrouperMembershipRequireChange retrieveById(String id) {
    GrouperMembershipRequireChange grouperMembershipRequireChange = new GcDbAccess()
        .sql("select * from grouper_mship_req_change where id = ?").addBindVar(id).select(GrouperMembershipRequireChange.class);
    return grouperMembershipRequireChange;
  }

  /**
   * delete old records
   * @return the count of how many deleted
   */
  public static int deleteOldRecords() {
    int daysToKeepLogs = GrouperConfig.retrieveConfig().propertyValueInt("grouper.membershipRequirement.keepLogsForDays", 90);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - (1000*60*60*24*daysToKeepLogs));
    int rowsDeleted = new GcDbAccess()
        .sql("delete from grouper_mship_req_change where the_timestamp < ?").addBindVar(timestamp).executeSql();
    return rowsDeleted;
  }
  
  /**
   * delete all records
   * @return the count of how many deleted
   */
  public static int deleteAllRecords() {
    int rowsDeleted = new GcDbAccess()
        .sql("delete from grouper_mship_req_change").executeSql();
    return rowsDeleted;
  }
  
  /**
   * @param grouperMembershipRequireChange
   */
  public void delete(GrouperMembershipRequireChange grouperMembershipRequireChange) {
    grouperMembershipRequireChange.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperMembershipRequireChange);
  }

}
