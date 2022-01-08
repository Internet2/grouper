package edu.internet2.middleware.grouper.misc;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperFailsafe {

  public static void main(String[] args) {

  }

  /**
   * see if this failsafe is approved
   * @param name
   * @return true if this failsafe is approved
   */
  public static boolean isApproved(String name) {
    
    int count = new GcDbAccess().sql("select count(1) from grouper_failsafe where name = ? and (approved_once = 'T' or approved_until >= ?)")
        .addBindVar(name).addBindVar(System.currentTimeMillis()).select(int.class);
    
    return count > 0;
  }
  
  /**
   * see if there is a failsafe issue last run (might be approved)
   * @param name
   * @return true if this failsafe is approved
   */
  public static boolean isFailsafeIssue(String name) {
    
    int count = new GcDbAccess().sql("select count(1) from grouper_failsafe where name = ? "
        + "and ((last_failsafe_issue is not null and last_success is null) "
        + "or (last_failsafe_issue is not null and last_success is not null and last_failsafe_issue >= last_success ))")
        .addBindVar(name).select(int.class);
    
    return count > 0;
  }
  
  /**
   * make sure a row exists for this job name
   * @param name
   */
  public static void insertRow(String name) {
    
    // try multiple times in case another process adds the row for the name
    GrouperUtil.tryMultipleTimes(5, new Runnable() {

      @Override
      public void run() {
        
        int count = new GcDbAccess().sql("select count(1) from grouper_failsafe where name = ?")
            .addBindVar(name).select(int.class);
        if (count == 0) {
          new GcDbAccess().sql("insert into grouper_failsafe (id, name, approved_once, last_updated) values (?, ?, ?, ?)")
            .addBindVar(GrouperUuid.getUuid()).addBindVar(name).addBindVar("F").addBindVar(System.currentTimeMillis()).executeSql();
        }
      }
      
    });
  }
  
  /**
   * 
   * @param name
   */
  public static void assignApproveNextRun(String name) {
    insertRow(name);
    long now = System.currentTimeMillis();
    new GcDbAccess().sql("update grouper_failsafe set approved_once = ?, last_updated = ? where name = ?")
      .addBindVar("T").addBindVar(now).addBindVar(name).executeSql();
  }

  /**
   * assign a success to this job
   * @param jobName
   */
  public static void assignSuccess(String jobName) {
    insertRow(jobName);
    long now = System.currentTimeMillis();
    new GcDbAccess().sql("update grouper_failsafe set approved_once = 'F', last_run = ?, last_failsafe_issue_started = null, last_failsafe_issue = null, "
        + "last_success = ?, last_updated = ? where name = ?").addBindVar(now).addBindVar(now).addBindVar(now).addBindVar(jobName).executeSql();
    
  }

  /**
   * assign an failure to this job
   * @param jobName
   */
  public static void assignFailed(String jobName) {
    insertRow(jobName);
    long now = System.currentTimeMillis();
    new GcDbAccess().sql("update grouper_failsafe set last_failsafe_issue_started = ?, last_updated = ? where name = ? and last_failsafe_issue_started is null")
      .addBindVar(now).addBindVar(now).addBindVar(jobName).executeSql();
    new GcDbAccess().sql("update grouper_failsafe set last_run = ?, last_failsafe_issue = ?, "
        + "last_updated = ? where name = ?").addBindVar(now).addBindVar(now).addBindVar(now).addBindVar(jobName).executeSql();
    
  }

  /**
   * get all job names where not approved and needs approval
   * @return job names
   */
  public static Set<String> retrieveJobNamesNeedApprovalNotApproved() {
    Set<String> jobNames = new HashSet<String>(new GcDbAccess().sql("select name from grouper_failsafe "
        + " where (approved_once is null or approved_once != 'T') and (approved_until is null or approved_until < ?) "
        + " and ((last_failsafe_issue is not null and last_success is null) "
        + "or (last_failsafe_issue is not null and last_success is not null and last_failsafe_issue >= last_success )) ")
        .addBindVar(System.currentTimeMillis()).selectList(String.class));
    
    return jobNames;
  }
  
}
