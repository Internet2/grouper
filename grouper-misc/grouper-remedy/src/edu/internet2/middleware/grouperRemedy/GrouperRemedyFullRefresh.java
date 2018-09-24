/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;


/**
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperRemedyFullRefresh implements Job {

  /**
   * 
   */
  private int deleteCount;
  
  /**
   * 
   */
  private int insertCount;

  /**
   * 
   */
  private int totalCount;

  /**
   * 
   */
  private int millisGetData;
  
  /**
   * 
   */
  private int millisLoadData;

  
  
  
  /**
   * @return the deleteCount
   */
  public int getDeleteCount() {
    return this.deleteCount;
  }

  
  /**
   * @return the insertCount
   */
  public int getInsertCount() {
    return this.insertCount;
  }

  
  /**
   * @return the totalCount
   */
  public int getTotalCount() {
    return this.totalCount;
  }

  
  /**
   * @return the millisGetData
   */
  public int getMillisGetData() {
    return this.millisGetData;
  }

  
  /**
   * @return the millisLoadData
   */
  public int getMillisLoadData() {
    return this.millisLoadData;
  }

  /** when was last full refresh started */
  private static long lastFullRefreshStart = -1L;
  
  /**
   * when was last full refresh started
   * @return the lastFullRefreshStart
   */
  public static long getLastFullRefreshStart() {
    return lastFullRefreshStart;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    fullRefreshLogic();
  }
  
  /**
   * change log temp to change log
   */
  public static final String GROUPER_DUO_FULL_REFRESH = "CHANGE_LOG_grouperRemedyFullRefresh";

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperRemedyFullRefresh.class);

  /**
   * 
   */
  public GrouperRemedyFullRefresh() {
  }

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {

    fullRefreshLogic();

  }

  /**
   * if full refresh is in progress
   */
  private static boolean fullRefreshInProgress = false;
  
  
  /**
   * if full refresh is in progress
   * @return the fullRefreshInProgress
   */
  public static boolean isFullRefreshInProgress() {
    return fullRefreshInProgress;
  }

  /**
   * wait for full refresh to end
   */
  public static void waitForFullRefreshToEnd() {
    while (isFullRefreshInProgress()) {
      GrouperClientUtils.sleep(100);
    }
  }
  
  /**
   * full refresh logic
   */
  public static void fullRefreshLogic() {
    new GrouperRemedyFullRefresh().fullRefreshLogicHelper();
  }
    
  /**
   * full refresh logic
   */
  public void fullRefreshLogicHelper() {
    
    fullRefreshInProgress = true;
    
    GrouperRemedyMessageConsumer.waitForIncrementalRefreshToEnd();
    
    //give a tiny bit of buffer
    lastFullRefreshStart = System.currentTimeMillis() - 500;

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "fullRefreshLogic");

    //lets enter a log entry so it shows up as error in the db    
    long startedMillis = System.currentTimeMillis();
    
    try {

      @SuppressWarnings("unchecked")
      List<WsGroup> grouperGroups = GrouperWsCommandsForRemedy.retrieveGrouperGroups();
      
      //take out include/exclude etc
      Iterator<WsGroup> iterator = grouperGroups.iterator();
      
      {
        int invalidGroupNameCount = 0;
        
        while (iterator.hasNext()) {
          WsGroup current = iterator.next();
          if (!GrouperRemedyUtils.validRemedyGroupName(current.getName())) {
            iterator.remove();
            invalidGroupNameCount++;
          }
        }
  
        debugMap.put("grouperGroupNameCount", grouperGroups.size());
        if (invalidGroupNameCount > 0) {
          debugMap.put("invalidGrouperGroupNameCount", invalidGroupNameCount);
        }
      }
      
      //make a map from group extension
      Map<String, WsGroup> grouperGroupExtensionToGroupMap = new TreeMap<String, WsGroup>();
      
      for (WsGroup group : grouperGroups) {
        grouperGroupExtensionToGroupMap.put(group.getExtension(), group);
      }
      
      //get groups from remedy
      Map<Long, GrouperRemedyGroup> remedyGroupNumericNameToGroupMap = GrouperRemedyCommands.retrieveRemedyGroups();

      debugMap.put("remedyGroupCount", remedyGroupNumericNameToGroupMap.size());

      Map<String, GrouperRemedyGroup> remedyGroupStringNameToGroupMap = new HashMap<String, GrouperRemedyGroup>();
      for (GrouperRemedyGroup grouperRemedyGroup : remedyGroupNumericNameToGroupMap.values()) {
        remedyGroupStringNameToGroupMap.put(grouperRemedyGroup.getPermissionGroup(), grouperRemedyGroup);
      }

      
      this.millisGetData = (int)(System.currentTimeMillis() - startedMillis);

      debugMap.put("millisGetData", this.millisGetData);
      long startedUpdateData = System.currentTimeMillis();

      boolean needsGroupRefresh = false;
      
      this.insertCount = 0;
      this.deleteCount = 0;
      this.totalCount = 0;
      
      //which groups are in remedy and not in grouper?
      Set<String> groupStringExtensionsInRemedyNotInGrouper = new TreeSet<String>();
      
      for (GrouperRemedyGroup grouperRemedyGroup : remedyGroupNumericNameToGroupMap.values()) {
        if (grouperRemedyGroup.isEnabled()) {
          groupStringExtensionsInRemedyNotInGrouper.add(grouperRemedyGroup.getPermissionGroup());
        }
      }
      
      for (String grouperExtension : grouperGroupExtensionToGroupMap.keySet()) {
        groupStringExtensionsInRemedyNotInGrouper.remove(grouperExtension);
      }
            
//      for (Long groupExtensionToRemove : groupExtensionsInRemedyNotInGrouper) {
//        GrouperRemedyGroup grouperRemedyGroup = remedyGroupNameToGroupMap.get(groupExtensionToRemove.toString());
//        //ignore this, we are not deleting groups
//        
//        //create in grouper
//        String displayExtension = grouperRemedyGroup.getPermissionGroup();
//        try {
//          GrouperWsCommandsForRemedy.createGrouperGroup(groupExtensionToRemove.toString(), groupExtensionToRemove.toString() + "_" + displayExtension);
//        } catch (Exception e) {
//          LOG.error("Cant create group: '" + groupExtensionToRemove + "', '" + displayExtension + "'", e);
//        }
//        
//      }


      if (needsGroupRefresh) {
        //lets get them again if some were created
        remedyGroupNumericNameToGroupMap = GrouperRemedyCommands.retrieveRemedyGroups();
        
        remedyGroupStringNameToGroupMap = new HashMap<String, GrouperRemedyGroup>();
        for (GrouperRemedyGroup grouperRemedyGroup : remedyGroupNumericNameToGroupMap.values()) {
          remedyGroupStringNameToGroupMap.put(grouperRemedyGroup.getPermissionGroup(), grouperRemedyGroup);
        }
      }
      
      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        WsGroup grouperGroup = grouperGroupExtensionToGroupMap.get(groupExtensionInGrouper);

        GrouperRemedyGroup grouperRemedyGroup = remedyGroupStringNameToGroupMap.get(grouperGroup.getExtension());
        
        if (grouperRemedyGroup == null || !grouperRemedyGroup.isEnabled()) {
          debugMap.put("groupDoesntExistInRemedy_" + grouperGroup.getExtension(), "true");
          LOG.error("Group doesnt exist in remedy: " + groupExtensionInGrouper);
          continue;
        }
        Map<String, GrouperRemedyUser> remedyMemberUsernameToUser = grouperRemedyGroup.getMemberUsers();
                    
        Set<String> grouperUsernamesInGroup = GrouperWsCommandsForRemedy.retrieveGrouperMembershipsForGroup(grouperGroup.getName());

        debugMap.put("grouperSubjectCount_" + grouperGroup.getExtension(), grouperUsernamesInGroup.size());
        this.totalCount += grouperUsernamesInGroup.size();
        
        //see which users are not in Remedy
        Set<String> grouperUsernamesNotInRemedy = new TreeSet<String>(grouperUsernamesInGroup);
        grouperUsernamesNotInRemedy.removeAll(remedyMemberUsernameToUser.keySet());

        debugMap.put("additions_" + grouperGroup.getExtension(), grouperUsernamesNotInRemedy.size());

        int userCountNotInRemedy = 0;
        
        //add to Remedy
        for (String grouperUsername : grouperUsernamesNotInRemedy) {
          
          GrouperRemedyUser grouperRemedyUser = GrouperRemedyUser.retrieveUsers().get(grouperUsername);
          
          if (grouperRemedyUser == null) {
            userCountNotInRemedy++;
          } else {
            this.insertCount++;
            try {
              grouperRemedyGroup.assignUserToGroup(grouperRemedyUser, false);
            } catch (Exception e) {
              LOG.error("Cant add membership: '" + grouperRemedyGroup.getPermissionGroupId() + ", " 
                  + grouperRemedyGroup.getPermissionGroup() + "', '" + grouperRemedyUser.getRemedyLoginId() + "'", e);
            }
          }
        }

        debugMap.put("userCountDoesntExistInRemedy_" + grouperGroup.getExtension(), userCountNotInRemedy);

        //see which users are not in Remedy
        Set<String> remedyUsernamesNotInGrouper = new TreeSet<String>(remedyMemberUsernameToUser.keySet());
        remedyUsernamesNotInGrouper.removeAll(grouperUsernamesInGroup);

        debugMap.put("removes_" + grouperGroup.getExtension(), remedyUsernamesNotInGrouper.size());

        //remove from Remedy
        for (String remedyUsername : remedyUsernamesNotInGrouper) {
          GrouperRemedyUser grouperRemedyUser = remedyMemberUsernameToUser.get(remedyUsername);
          try {
            grouperRemedyGroup.removeUserFromGroup(grouperRemedyUser, false);
          } catch (Exception e) {
            LOG.error("Cant remove membership: '" + grouperRemedyGroup.getPermissionGroupId() + ", " 
                + grouperRemedyGroup.getPermissionGroup() + "', '" + grouperRemedyUser.getRemedyLoginId() + "'", e);
          }
          this.deleteCount++;
        }
        
      }
      
      //lets reconcile which users are in remedy but not supposed to be
//      Map<String, String[]> usersAllowedToBeInRemedy = GrouperWsCommandsForRemedy.retrieveGrouperUsers();
//      if (usersAllowedToBeInRemedy != null) {
//        
//        Map<String, GrouperRemedyUser> grouperRemedyUsers = GrouperRemedyUser.retrieveUsers();
//
//        for (GrouperRemedyUser grouperRemedyUser : grouperRemedyUsers.values()) {
//          GrouperRemedyCommands.deprovisionOrUndeprovision(grouperRemedyUser, debugMap);
//        }
//        
//      }
      
      this.millisLoadData = (int)(System.currentTimeMillis() - startedUpdateData);

      debugMap.put("millisLoadData", this.millisLoadData);
      debugMap.put("millis", System.currentTimeMillis() - startedMillis);
      
      debugMap.put("insertCount", this.insertCount);
      debugMap.put("deleteCount", this.deleteCount);
      debugMap.put("totalCount", this.totalCount);
      
    } catch (Exception e) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(e));
      String errorMessage = "Problem running remedy full sync";
      LOG.error(errorMessage, e);
    
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
      fullRefreshInProgress = false;
    }
  }

}
