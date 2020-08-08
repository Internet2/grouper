/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperZoomFullSync extends OtherJobBase {

  /**
   * 
   */
  public GrouperZoomFullSync() {
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperZoomFullSync.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (GrouperUtil.length(args) != 1) {
      throw new RuntimeException("Pass in the configId to full sync");
    }
    GrouperStartup.startup();
    fullSync(args[0]);
  }

  /**
   * 
   * @param configId
   * @return map with groupCount, groupAddCount, membershipAddCount, membershipDeleteCount, membershipTotalCount
   */
  public static Map<String, Object> fullSync(final String configId) {
    return (Map<String, Object>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        long startedNanos = System.nanoTime();
        Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
        
        debugMap.put("method", "fullSync");
        try {

          // group extensions to provision
          Set<String> groupsToProvision = GrouperZoomLocalCommands.groupExtensionsToProvision(configId);

          // group extension to list of emails
          Map<String,Set<String>> groupsEmailsToProvision =  GrouperZoomLocalCommands.groupsEmailsToProvision(configId);
          
          // groups that exist in zoom
          Map<String, Map<String, Object>> groupsProvisioned = GrouperZoomCommands.retrieveGroups(configId);
          
          debugMap.put("groupCount", GrouperUtil.length(groupsToProvision));
          debugMap.put("groupAddCount", 0);
          debugMap.put("userDeleteCount", 0);
          debugMap.put("userDeactivateCount", 0);
          debugMap.put("membershipAddCount", 0);
          debugMap.put("membershipDeleteCount", 0);
          debugMap.put("membershipTotalCount", 0);
          if (GrouperUtil.length(groupsToProvision) == 0) {
            return debugMap;
          }

          int groupAddCount = 0;
          
          // add groups
          for (String groupName : GrouperUtil.nonNull(groupsToProvision)) {
            if (!groupsProvisioned.containsKey(groupName)) {
              Map<String, Object> group = GrouperZoomCommands.createGroup(configId, groupName);
              groupsProvisioned.put(groupName, group);
              groupAddCount++;
            }
          }

          debugMap.put("groupAddCount", groupAddCount);

          int membershipTotalCount = 0;
          int membershipAddCount = 0;
          int membershipDeleteCount = 0;

          Map<String, String> emailToUserId = new HashMap<String, String>();
          
          // manage memberships
          for (String groupName : GrouperUtil.nonNull(groupsToProvision)) {
            Set<String> emailsToProvision = groupsEmailsToProvision.get(groupName);
            membershipTotalCount += GrouperUtil.length(emailsToProvision);

            Map<String, Object> groupProvisioned = groupsProvisioned.get(groupName);
            String groupId = (String)groupProvisioned.get("id");
            
            List<Map<String, Object>> memberships = GrouperZoomCommands.retrieveGroupMemberships(configId, groupId);
            Set<String> emailsProvisioned = new HashSet<String>();
            for (Map<String, Object> membership : GrouperUtil.nonNull(memberships)) {
              String email = (String)membership.get("email");
              String userId = (String)membership.get("id");
              emailsProvisioned.add(email);
              emailToUserId.put(email, userId);
            }
            
            {
              Set<String> emailsToAdd = new TreeSet<String>(GrouperUtil.nonNull(emailsToProvision));
              emailsToAdd.removeAll(GrouperUtil.nonNull(emailsProvisioned));
              
              membershipAddCount+= emailsToAdd.size();
              for (String emailToAdd : emailsToAdd) {
                Map<String, Object> user = GrouperZoomCommands.retrieveUser(configId, emailToAdd);
                // TODO add user?
                if (user != null) {
                  String userId = (String)user.get("id");
                  GrouperZoomCommands.addGroupMembership(configId, groupId, userId);
                }
              }
            }
            
            {
              Set<String> emailsToRemove = new TreeSet<String>(GrouperUtil.nonNull(emailsProvisioned));
              emailsToRemove.removeAll(GrouperUtil.nonNull(emailsToProvision));
              
              membershipDeleteCount+= emailsToRemove.size();
              for (String emailToRemove : emailsToRemove) {
                String userId = emailToUserId.get(emailToRemove);
                GrouperZoomCommands.removeGroupMembership(configId, groupId, userId);
              }
            }
            
          }
          
          debugMap.put("membershipAddCount", membershipAddCount);
          debugMap.put("membershipDeleteCount", membershipDeleteCount);
          debugMap.put("membershipTotalCount", membershipTotalCount);
          
          debugMap.put("userDeleteCount", 0);

          String groupNameToDeleteUsers = GrouperZoomLocalCommands.groupNameToDeleteUsers(configId);
          if (!StringUtils.isBlank(groupNameToDeleteUsers)) {
            int userDeleteCount = 0;

            Set<String> emails = GrouperZoomLocalCommands.groupEmailsFromGroup(configId, groupNameToDeleteUsers);
            
            for (String email : GrouperUtil.nonNull(emails)) {

              Map<String, Object> user = GrouperZoomCommands.retrieveUser(configId, email);
              
              if (user == null) {
                continue;
              }
        
              GrouperZoomCommands.deleteUser(configId, email);
              userDeleteCount++;
              
            }
            debugMap.put("userDeleteCount", userDeleteCount);
          }
          
          debugMap.put("userDeactivateCount", 0);
          String groupNameToDeactivateUsers = GrouperZoomLocalCommands.groupNameToDeactivateUsers(configId);
          if (!StringUtils.isBlank(groupNameToDeactivateUsers)) {
            int userDeactivateCount = 0;

            Set<String> emails = GrouperZoomLocalCommands.groupEmailsFromGroup(configId, groupNameToDeactivateUsers);
            
            for (String email : GrouperUtil.nonNull(emails)) {

              Map<String, Object> user = GrouperZoomCommands.retrieveUser(configId, email);
              
              if (user == null || StringUtils.equals("inactive", (String)user.get("status")) || StringUtils.equals("pending", (String)user.get("status"))) {
                continue;
              }

              GrouperZoomCommands.userChangeStatus(configId, email, false);
              userDeactivateCount++;
              
            }
            debugMap.put("userDeactivateCount", userDeactivateCount);
          }
          
        } catch (RuntimeException e) {
          debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
          throw e;
        } finally {
          debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
          if (LOG.isDebugEnabled()) {
            LOG.debug(GrouperUtil.mapToString(debugMap));
          }
        }
        return debugMap;
      }
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_csvSync
    jobName = GrouperClientUtils.stripPrefix(jobName, "OTHER_JOB_");

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    if (hib3GrouperLoaderLog == null) {
      hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    }

    String configId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomConfigId");

    Map<String, Object> resultMap = fullSync(configId);
    
    int groupAddCount = resultMap.containsKey("groupAddCount") ? (Integer)resultMap.get("groupAddCount") : 0;
    int groupCount = resultMap.containsKey("groupCount") ? (Integer)resultMap.get("groupCount") : 0;
    int membershipAddCount = resultMap.containsKey("membershipAddCount") ? (Integer)resultMap.get("membershipAddCount") : 0;
    int membershipDeleteCount = resultMap.containsKey("membershipDeleteCount") ? (Integer)resultMap.get("membershipDeleteCount") : 0;
    int membershipTotalCount = resultMap.containsKey("membershipTotalCount") ? (Integer)resultMap.get("membershipTotalCount") : 0;

    hib3GrouperLoaderLog.addInsertCount(groupAddCount);
    hib3GrouperLoaderLog.addInsertCount(membershipAddCount);

    hib3GrouperLoaderLog.addDeleteCount(membershipDeleteCount);

    hib3GrouperLoaderLog.addTotalCount(groupCount);
    hib3GrouperLoaderLog.addTotalCount(membershipTotalCount);
    
    hib3GrouperLoaderLog.setJobMessage(GrouperUtil.toStringForLog(resultMap));

    return null;
  }
  
}
