/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderScheduleType;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;

/**
 *
 */
@DisallowConcurrentExecution
public class GrouperDuoFullRefresh extends OtherJobBase implements Job {

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
  public static final String GROUPER_DUO_FULL_REFRESH = "CHANGE_LOG_grouperDuoFullRefresh";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDuoFullRefresh.class);

  /**
   * 
   */
  public GrouperDuoFullRefresh() {
  }

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {

    fullRefreshLogic();

    
  }

  /**
   * full refresh logic
   */
  public static void fullRefreshLogic() {
    GrouperSession grouperSession = null;
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "fullRefreshLogic");

    //lets enter a log entry so it shows up as error in the db
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName(GrouperDuoFullRefresh.GROUPER_DUO_FULL_REFRESH);
    hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
    hib3GrouploaderLog.setJobType(GrouperLoaderType.MAINTENANCE.name());

    hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
    
    long startedMillis = System.currentTimeMillis();
    
    try {
      
      grouperSession = GrouperSession.startRootSession();
      
      //# put groups in here which go to duo, the name in duo will be the extension here
      //grouperDuo.folder.name.withDuoGroups = duo
      String grouperDuoFolderName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.folder.name.withDuoGroups");
      Stem grouperDuoFolder = StemFinder.findByName(grouperSession, grouperDuoFolderName, true);
      
      Set<Group> grouperGroups = grouperDuoFolder.getChildGroups(Scope.ONE);
      
      //take out include/exclude etc
      Iterator<Group> iterator = grouperGroups.iterator();
      
      {
        int invalidGroupNameCount = 0;
        
        while (iterator.hasNext()) {
          Group current = iterator.next();
          if (!GrouperDuoUtils.validDuoGroupName(current.getName())) {
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
      Map<String, Group> grouperGroupExtensionToGroupMap = new HashMap<String, Group>();
      
      for (Group group : grouperGroups) {
        grouperGroupExtensionToGroupMap.put(group.getExtension(), group);
      }
      
      //get groups from duo
      Map<String, GrouperDuoGroup> duoGroupNameToGroupMap = GrouperDuoCommands.retrieveGroups();

      debugMap.put("duoGroupCount", duoGroupNameToGroupMap.size());

      debugMap.put("millisGetData", System.currentTimeMillis() - startedMillis);
      hib3GrouploaderLog.setMillisGetData((int)(System.currentTimeMillis() - startedMillis));
      long startedUpdateData = System.currentTimeMillis();

      boolean needsGroupRefresh = false;
      
      int insertCount = 0;
      int deleteCount = 0;
      int unresolvableCount = 0;
      int totalCount = 0;
      
      //# is grouper the true system of record, delete duo groups which dont exist in grouper
      if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.deleteGroupsInDuoWhichArentInGrouper", true)) {
        
        //which groups are in duo and not in grouper?
        Set<String> groupExtensionsInDuoNotInGrouper = new TreeSet<String>(duoGroupNameToGroupMap.keySet());
        groupExtensionsInDuoNotInGrouper.removeAll(grouperGroupExtensionToGroupMap.keySet());
        
        for (String groupExtensionToRemove : groupExtensionsInDuoNotInGrouper) {
          GrouperDuoGroup grouperDuoGroup = duoGroupNameToGroupMap.get(groupExtensionToRemove);
          GrouperDuoCommands.deleteDuoGroup(grouperDuoGroup.getId(), false);
          
          deleteCount++;
          debugMap.put("deleteDuoGroup_" + groupExtensionToRemove, true);
          
          needsGroupRefresh = true;
        }
        
      }

      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        Group groupInGrouper = grouperGroupExtensionToGroupMap.get(groupExtensionInGrouper);
        
        GrouperDuoGroup groupInDuo = duoGroupNameToGroupMap.get(groupExtensionInGrouper);
        
        if (groupInDuo == null) {
          //create duo group
          GrouperDuoCommands.createDuoGroup(groupExtensionInGrouper, groupInGrouper.getDescription(), false);
          needsGroupRefresh = true;
          debugMap.put("createDuoGroup_" + groupExtensionInGrouper, true);
          insertCount++;
        }
      }

      if (needsGroupRefresh) {
        //lets get them again if some were created
        duoGroupNameToGroupMap = GrouperDuoCommands.retrieveGroups();
      }
      
      //# put the comma separated list of sources to send to duo
      //grouperDuo.sourcesForSubjects = pennperson
      Set<String> sourcesForSubjects = GrouperDuoUtils.configSourcesForSubjects();
      
      //# either have id for subject id or an attribute for the duo username (e.g. netId)
      //grouperDuo.subjectAttributeForDuoUsername = pennname
      String subjectAttributeForDuoUsername = GrouperDuoUtils.configSubjectAttributeForDuoUsername();
      
      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        Group grouperGroup = grouperGroupExtensionToGroupMap.get(groupExtensionInGrouper);
        
        GrouperDuoGroup duoGroup = duoGroupNameToGroupMap.get(groupExtensionInGrouper);

        //get group info to see if description ok
        JSONObject groupInfoResponse = GrouperDuoCommands.retrieveGroupInfo(duoGroup.getId(), false);

        //see if update description
        {
          String duoDescription = groupInfoResponse.getString("desc");
          
          if (!StringUtils.equals(grouperGroup.getDescription(), duoDescription)) {
            GrouperDuoCommands.updateDuoGroup(duoGroup.getId(), grouperGroup.getDescription(), false);
          }
        }
        
        Map<String, GrouperDuoUser> duoUsernameToUser = GrouperDuoCommands.retrieveUsersForGroup(groupInfoResponse);
        
        Set<String> grouperUsernamesInGroup = new HashSet<String>();
        
        //get usernames from grouper
        for (Member member : grouperGroup.getMembers()) {
          
          if (sourcesForSubjects.contains(member.getSubjectSourceId())) {
            if (StringUtils.equals("id", subjectAttributeForDuoUsername)) {
              grouperUsernamesInGroup.add(member.getSubjectId());
            } else {
              try {
                Subject subject = member.getSubject();
                String attributeValue = subject.getAttributeValue(subjectAttributeForDuoUsername);
                if (StringUtils.isBlank(attributeValue)) {
                  //i guess this is ok
                  LOG.info("Subject has a blank: " + subjectAttributeForDuoUsername + ", " + member.getSubjectSourceId() + ", " + member.getSubjectId());
                  unresolvableCount++;
                } else {
                  grouperUsernamesInGroup.add(attributeValue);
                }
              } catch (SubjectNotFoundException snfe) {
                unresolvableCount++;
                LOG.error("Cant find subject: " + member.getSubjectSourceId() + ": " +  member.getSubjectId());
                //i guess continue
              }
            }
          }
        }

        debugMap.put("grouperSubjectCount_" + grouperGroup.getExtension(), grouperUsernamesInGroup.size());
        totalCount += grouperUsernamesInGroup.size();
        
        //see which users are not in Duo
        Set<String> grouperUsernamesNotInDuo = new TreeSet<String>(grouperUsernamesInGroup);
        grouperUsernamesNotInDuo.removeAll(duoUsernameToUser.keySet());

        debugMap.put("additions_" + grouperGroup.getExtension(), grouperUsernamesNotInDuo.size());

        //add to duo
        for (String grouperUsername : grouperUsernamesNotInDuo) {
          String duoUserId = GrouperDuoCommands.retrieveUserIdFromUsername(grouperUsername);
          if (StringUtils.isBlank(duoUserId)) {
            LOG.warn("User is not in duo: " + grouperUsername);
          } else {
            insertCount++;
            GrouperDuoCommands.assignUserToGroup(duoUserId, duoGroup.getId(), false);
          }
        }

        //see which users are not in duo
        Set<String> duoUsernamesNotInGrouper = new TreeSet<String>(duoUsernameToUser.keySet());
        duoUsernamesNotInGrouper.removeAll(grouperUsernamesInGroup);

        debugMap.put("removes_" + grouperGroup.getExtension(), duoUsernamesNotInGrouper.size());

        //remove from duo
        for (String duoUsername : duoUsernamesNotInGrouper) {
          String duoUserId = duoUsernameToUser.get(duoUsername).getUserId();
          GrouperDuoCommands.removeUserFromGroup(duoUserId, duoGroup.getId(), false);
          deleteCount++;
        }
        
      }
      debugMap.put("millisLoadData", System.currentTimeMillis() - startedUpdateData);
      hib3GrouploaderLog.setMillisLoadData((int)(System.currentTimeMillis() - startedUpdateData));
      debugMap.put("millis", System.currentTimeMillis() - startedMillis);
      hib3GrouploaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
      hib3GrouploaderLog.setMillis((int)(System.currentTimeMillis() - startedMillis));
      
      //lets enter a log entry so it shows up as error in the db
      hib3GrouploaderLog.setJobMessage(GrouperUtil.mapToString(debugMap));
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      hib3GrouploaderLog.setUnresolvableSubjectCount(unresolvableCount);
      hib3GrouploaderLog.setInsertCount(insertCount);
      hib3GrouploaderLog.setDeleteCount(deleteCount);
      hib3GrouploaderLog.setTotalCount(totalCount);
      hib3GrouploaderLog.store();
      
    } catch (Exception e) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(e));
      String errorMessage = "Problem running job: '" + GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        hib3GrouploaderLog.setMillis((int)(System.currentTimeMillis() - startedMillis));
        hib3GrouploaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTimeNanos);
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
