/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

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
public class GrouperBoxFullRefresh implements Job {

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
  public static final String GROUPER_DUO_FULL_REFRESH = "CHANGE_LOG_grouperBoxFullRefresh";

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperBoxFullRefresh.class);

  /**
   * 
   */
  public GrouperBoxFullRefresh() {
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
    
    fullRefreshInProgress = true;
    
    GrouperBoxMessageConsumer.waitForIncrementalRefreshToEnd();
    
    //give a tiny bit of buffer
    lastFullRefreshStart = System.currentTimeMillis() - 500;

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "fullRefreshLogic");

    //lets enter a log entry so it shows up as error in the db    
    long startedMillis = System.currentTimeMillis();
    
    try {

      @SuppressWarnings("unchecked")
      List<WsGroup> grouperGroups = GrouperWsCommandsForBox.retrieveGrouperGroups();
      
      //take out include/exclude etc
      Iterator<WsGroup> iterator = grouperGroups.iterator();
      
      {
        int invalidGroupNameCount = 0;
        
        while (iterator.hasNext()) {
          WsGroup current = iterator.next();
          if (!GrouperBoxUtils.validBoxGroupName(current.getName())) {
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
      
      //get groups from box
      Map<String, GrouperBoxGroup> boxGroupNameToGroupMap = GrouperBoxCommands.retrieveBoxGroups();

      debugMap.put("boxGroupCount", boxGroupNameToGroupMap.size());

      debugMap.put("millisGetData", System.currentTimeMillis() - startedMillis);
      long startedUpdateData = System.currentTimeMillis();

      boolean needsGroupRefresh = false;
      
      int insertCount = 0;
      int deleteCount = 0;
      int unresolvableCount = 0;
      int totalCount = 0;
      
      //which groups are in box and not in grouper?
      Set<String> groupExtensionsInBoxNotInGrouper = new TreeSet<String>(boxGroupNameToGroupMap.keySet());
      groupExtensionsInBoxNotInGrouper.removeAll(grouperGroupExtensionToGroupMap.keySet());
      
      for (String groupExtensionToRemove : groupExtensionsInBoxNotInGrouper) {
        GrouperBoxGroup grouperBoxGroup = boxGroupNameToGroupMap.get(groupExtensionToRemove);
        if (GrouperBoxCommands.deleteBoxGroup(grouperBoxGroup, false)) {
        
          deleteCount++;
          debugMap.put("deleteBoxGroup_" + groupExtensionToRemove, true);
          
          needsGroupRefresh = true;
        }
      }

      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        GrouperBoxGroup groupInBox = boxGroupNameToGroupMap.get(groupExtensionInGrouper);
        
        if (groupInBox == null) {
          //create box group
          GrouperBoxCommands.createBoxGroup(groupExtensionInGrouper, false);
          needsGroupRefresh = true;
          debugMap.put("createBoxGroup_" + groupExtensionInGrouper, true);
          insertCount++;
        }
      }

      if (needsGroupRefresh) {
        //lets get them again if some were created
        boxGroupNameToGroupMap = GrouperBoxCommands.retrieveBoxGroups();
      }
      
      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        WsGroup grouperGroup = grouperGroupExtensionToGroupMap.get(groupExtensionInGrouper);
        
        GrouperBoxGroup grouperBoxGroup = boxGroupNameToGroupMap.get(groupExtensionInGrouper);
        
        Map<String, GrouperBoxUser> boxMemberUsernameToUser = grouperBoxGroup.getMemberUsers();
                    
        Set<String> grouperUsernamesInGroup = GrouperWsCommandsForBox.retrieveGrouperMembershipsForGroup(grouperGroup.getName());

        debugMap.put("grouperSubjectCount_" + grouperGroup.getExtension(), grouperUsernamesInGroup.size());
        totalCount += grouperUsernamesInGroup.size();
        
        //see which users are not in Box
        Set<String> grouperUsernamesNotInBox = new TreeSet<String>(grouperUsernamesInGroup);
        grouperUsernamesNotInBox.removeAll(boxMemberUsernameToUser.keySet());

        debugMap.put("additions_" + grouperGroup.getExtension(), grouperUsernamesNotInBox.size());

        int userCountNotInBox = 0;
        
        //add to box
        for (String grouperUsername : grouperUsernamesNotInBox) {
          
          GrouperBoxUser grouperBoxUser = GrouperBoxUser.retrieveUsers().get(grouperUsername);
          
          if (grouperBoxUser == null) {
            userCountNotInBox++;
          } else {
            insertCount++;
            grouperBoxGroup.assignUserToGroup(grouperBoxUser, false);
          }
        }

        debugMap.put("userCountDoesntExistInBox_" + grouperGroup.getExtension(), userCountNotInBox);

        //see which users are not in box
        Set<String> boxUsernamesNotInGrouper = new TreeSet<String>(boxMemberUsernameToUser.keySet());
        boxUsernamesNotInGrouper.removeAll(grouperUsernamesInGroup);

        debugMap.put("removes_" + grouperGroup.getExtension(), boxUsernamesNotInGrouper.size());

        //remove from box
        for (String boxUsername : boxUsernamesNotInGrouper) {
          GrouperBoxUser grouperBoxUser = boxMemberUsernameToUser.get(boxUsername);
          GrouperBoxCommands.removeUserFromBoxGroup(grouperBoxUser, grouperBoxGroup, false);
          deleteCount++;
        }
        
      }
      
      //lets reconcile which users are in box but not supposed to be
      Map<String, String[]> usersAllowedToBeInBox = GrouperWsCommandsForBox.retrieveGrouperUsers();
      if (usersAllowedToBeInBox != null) {
        
        Map<String, GrouperBoxUser> grouperBoxUsers = GrouperBoxUser.retrieveUsers();

        for (GrouperBoxUser grouperBoxUser : grouperBoxUsers.values()) {
          GrouperBoxCommands.deprovisionOrUndeprovision(grouperBoxUser, debugMap);
        }
        
      }
      
      debugMap.put("millisLoadData", System.currentTimeMillis() - startedUpdateData);
      debugMap.put("millis", System.currentTimeMillis() - startedMillis);
      
      debugMap.put("insertCount", insertCount);
      debugMap.put("deleteCount", deleteCount);
      debugMap.put("unresolvableCount", unresolvableCount);
      debugMap.put("totalCount", totalCount);
      
    } catch (Exception e) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(e));
      String errorMessage = "Problem running box full sync";
      LOG.error(errorMessage, e);
    
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
      fullRefreshInProgress = false;
    }
  }

}
