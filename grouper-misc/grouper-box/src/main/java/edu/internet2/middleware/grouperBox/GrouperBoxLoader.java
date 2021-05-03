package edu.internet2.middleware.grouperBox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.box.sdk.BoxUser;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.OtherJobLogUpdater;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperBoxLoader extends OtherJobBase {

  public static Timestamp convertDateToTimestampRoundToSecond(Date date) {
    Timestamp createdAtTimestamp = null;
    if (date != null) {
      long millis = date.getTime();
      // round to nearset second
      millis = 1000L * ((millis + 500)/1000L);
      createdAtTimestamp = new Timestamp(millis);
    }
    return createdAtTimestamp;
  }
  
  public static void main(String args[]) {
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_boxLoader");
  }
  
  /**
   * 
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

  private Map<String, String> eppnToSubjectId = null;
  
  public void retrieveEppns() {
    
    this.debugMap.put("state", "retrieveEppns");
    
    String eppnQuery = "select ps.penn_id, ps.eppn from person_source ps where eppn is not null";
    GrouperUtil.assertion(!StringUtils.isBlank(this.jobName), "jobName cant be blank");
    GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + this.jobName + ".subjectIdToEppnQuery", eppnQuery);
    
    List<Object[]> subjectIdAndEppns = new GcDbAccess().sql(
        eppnQuery).selectList(Object[].class);
    
    eppnToSubjectId = new HashMap<String, String>();
    
    for (Object[] subjectIdAndEppn : GrouperUtil.nonNull(subjectIdAndEppns)) {
      eppnToSubjectId.put((String)subjectIdAndEppn[1], (String)subjectIdAndEppn[0]);
    }
    
    this.debugMap.put("totalEppns", GrouperUtil.length(this.eppnToSubjectId));

  }
  
  private Map<String, GrouperBoxDbUser> boxIdToGrouperBoxDbUserInBox;
  
  public void retrieveBoxData() {
    this.debugMap.put("state", "retrieveBoxData");

    // key is eppn
    Map<String, GrouperBoxUser> loginToUserInBox = GrouperBoxCommands.retrieveBoxUsers();
    this.boxIdToGrouperBoxDbUserInBox = new HashMap<String, GrouperBoxDbUser>();

    for (GrouperBoxUser grouperBoxUser : GrouperUtil.nonNull(loginToUserInBox).values()) {

      BoxUser boxUser = grouperBoxUser.getBoxUser();
      BoxUser.Info userInfo = grouperBoxUser.getBoxUserInfo();
      BoxUser.Status userStatus = userInfo.getStatus(); // 40 ACTIVE, CANNOT_DELETE_EDIT, CANNOT_DELETE_EDIT_UPLOAD, INACTIVE
      
      GrouperBoxDbUser grouperBoxDbUser = new GrouperBoxDbUser();
      grouperBoxDbUser.setBoxId(boxUser.getID());
      
      Timestamp createdAtTimestamp = convertDateToTimestampRoundToSecond(userInfo.getCreatedAt());
      
      grouperBoxDbUser.setCreatedAt(createdAtTimestamp);
      
      grouperBoxDbUser.setLogin(userInfo.getLogin());

      Timestamp modifiedAtTimestamp = convertDateToTimestampRoundToSecond(userInfo.getModifiedAt());
      grouperBoxDbUser.setModifiedAt(modifiedAtTimestamp);
      
      grouperBoxDbUser.setName(GrouperUtil.abbreviate(userInfo.getName(), 100));
      grouperBoxDbUser.setSpaceUsed(userInfo.getSpaceUsed());
      grouperBoxDbUser.setStatus(userStatus == null ? null : userStatus.name());
      if (!StringUtils.isBlank(userInfo.getLogin())) {
        grouperBoxDbUser.setSubjectId(eppnToSubjectId.get(userInfo.getLogin()));
      }
      this.boxIdToGrouperBoxDbUserInBox.put(boxUser.getID(), grouperBoxDbUser);
    }

    this.debugMap.put("totalInBox", GrouperUtil.length(this.boxIdToGrouperBoxDbUserInBox));

  }
  
  private Map<String, GrouperBoxDbUser> boxIdToGrouperBoxDbUserInGrouper;
  
  public void retrieveGrouperData() {
    this.debugMap.put("state", "retrieveGrouperData");
    // get existing data
    List<GrouperBoxDbUser> grouperBoxDbUsers = new GcDbAccess().sql("select * from penn_box_user").selectList(GrouperBoxDbUser.class);
    
    this.boxIdToGrouperBoxDbUserInGrouper = new HashMap<String, GrouperBoxDbUser>();
    
    for (GrouperBoxDbUser grouperBoxDbUser : GrouperUtil.nonNull(grouperBoxDbUsers)) {
      this.boxIdToGrouperBoxDbUserInGrouper.put(grouperBoxDbUser.getBoxId(), grouperBoxDbUser);
    }
    
    this.debugMap.put("totalInGrouper", GrouperUtil.length(this.boxIdToGrouperBoxDbUserInGrouper));

  }
  
  /**
   * 
   */
  public void fullSync() {

    final boolean[] done = new boolean[] {false};
    
//    Thread thread = new Thread(new Runnable() {
//      
//      @Override
//      public void run() {
//        
//        for (int i=0;i<10000;i++) {
//          GrouperUtil.sleep(10000);
//          if (done[0]) {
//            return;
//          }
//          System.out.println("Working: " + GrouperUtil.mapToString(debugMap));
//        }
//      }
//    });
//    
//    thread.start();
    
    try {
      this.retrieveEppns();
  
      this.retrieveBoxData();
      
      this.retrieveGrouperData();
  
      this.performDeletes();
      
      this.performInserts();
  
      this.performUpdates();

      this.debugMap.put("state", "done");

    } catch (RuntimeException re) {
      this.debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
//      System.out.println("Done: " + GrouperUtil.mapToString(this.debugMap));
      done[0]=true;
    }
  }
  
  /**
   * 
   */
  public void performInserts() {
    this.debugMap.put("state", "performInserts");

    Set<String> idsToInsert = new HashSet<String>(this.boxIdToGrouperBoxDbUserInBox.keySet());
    idsToInsert.removeAll(this.boxIdToGrouperBoxDbUserInGrouper.keySet());
    
    List<GrouperBoxDbUser> grouperBoxDbUsersToInsert = new ArrayList<GrouperBoxDbUser>();
    for (String idToInsert : idsToInsert) {
      GrouperBoxDbUser grouperBoxDbUserToInsert = this.boxIdToGrouperBoxDbUserInBox.get(idToInsert);
      grouperBoxDbUsersToInsert.add(grouperBoxDbUserToInsert);

      // tell the object its an insert
      grouperBoxDbUserToInsert.setBoxIdForInsert(grouperBoxDbUserToInsert.getBoxId());
      grouperBoxDbUserToInsert.setBoxId(null);
      
    }
    
    int inserts = GrouperBoxDbUser.storeBatch(grouperBoxDbUsersToInsert);
    
    this.debugMap.put("inserts", inserts);
  }

  /**
   * 
   */
  public void performUpdates() {
    this.debugMap.put("state", "performUpdates");

    Set<String> idsToUpdate = new HashSet<String>(this.boxIdToGrouperBoxDbUserInGrouper.keySet());
    idsToUpdate.retainAll(this.boxIdToGrouperBoxDbUserInBox.keySet());
    
    List<GrouperBoxDbUser> grouperBoxDbUsersToUpdate = new ArrayList<GrouperBoxDbUser>();
    for (String idToUpdate : idsToUpdate) {
      
      GrouperBoxDbUser grouperBoxDbUserInBox = this.boxIdToGrouperBoxDbUserInBox.get(idToUpdate);
      GrouperBoxDbUser grouperBoxDbUserInGrouper = this.boxIdToGrouperBoxDbUserInGrouper.get(idToUpdate);
      
      if (!grouperBoxDbUserInBox.equals(grouperBoxDbUserInGrouper)) {
        grouperBoxDbUsersToUpdate.add(grouperBoxDbUserInBox);
      }
      
    }
    
    int updates = GrouperBoxDbUser.storeBatch(grouperBoxDbUsersToUpdate);
    
    this.debugMap.put("updates", updates);
  }

  /**
   * 
   */
  public void performDeletes() {
    this.debugMap.put("state", "performDeletes");

    Set<String> idsToDelete = new HashSet<String>(this.boxIdToGrouperBoxDbUserInGrouper.keySet());
    idsToDelete.removeAll(this.boxIdToGrouperBoxDbUserInBox.keySet());
    
    List<GrouperBoxDbUser> grouperBoxDbUsersToDelete = new ArrayList<GrouperBoxDbUser>();
    for (String idToDelete : idsToDelete) {
      grouperBoxDbUsersToDelete.add(this.boxIdToGrouperBoxDbUserInGrouper.get(idToDelete));
    }
    
    int deletes = GrouperBoxDbUser.deleteBatch(grouperBoxDbUsersToDelete);
    
    this.debugMap.put("deletes", deletes);
  }

  private String jobName = null;
  
  /**
   * 
   */
  @Override
  public OtherJobOutput run(final OtherJobInput theOtherJobInput) {
    jobName = theOtherJobInput.getJobName();
    
    // jobName = OTHER_JOB_boxLoader
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

    OtherJobLogUpdater otherJobLogUpdater = new OtherJobLogUpdater() {
      
      @Override
      public void changeLoaderLogJavaObjectWithoutStoringToDb() {
        logDataAssign(theOtherJobInput.getHib3GrouperLoaderLog());
      }
    };

    try {
      otherJobLogUpdaterRegister(otherJobLogUpdater);
      
      this.fullSync();
      
      logDataAssign(theOtherJobInput.getHib3GrouperLoaderLog());
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      theOtherJobInput.getHib3GrouperLoaderLog().setStatus("ERROR");
    } finally {
      otherJobLogUpdaterDeregister(otherJobLogUpdater);
    }
    
    return null;
  }

  public void logDataAssign(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    String logMessage = GrouperUtil.mapToString(debugMap);
    hib3GrouperLoaderLog.setJobMessage(logMessage);
    hib3GrouperLoaderLog.setInsertCount((Integer)debugMap.get("inserts"));
    hib3GrouperLoaderLog.setUpdateCount((Integer)debugMap.get("updates"));
    hib3GrouperLoaderLog.setDeleteCount((Integer)debugMap.get("deletes"));
    hib3GrouperLoaderLog.setTotalCount((Integer)debugMap.get("totalInBox"));
  }
  
}
