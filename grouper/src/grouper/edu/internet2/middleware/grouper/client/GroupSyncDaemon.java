package edu.internet2.middleware.grouper.client;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientConnectionConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientConnectionSourceConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientGroupConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.GroupSyncWriteIdentifier;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;

/**
 * process logic for the cron part of a group sync
 * @author mchyzer
 *
 */
public class GroupSyncDaemon {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GroupSyncDaemon.class);

  /**
   * sync a group by config name from the cron daemon
   * @param configName
   * @return the number of records changed
   */
  public static int syncGroup(final String configName) {

    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("configName", configName);
    
    final ClientGroupConfigBean clientGroupConfigBean = ClientConfig.clientGroupConfigBeanCache().get(configName);

    if (clientGroupConfigBean == null) {
      throw new RuntimeException("Cant find clientGroupConfigBean by config id: '" + configName + "'");
    }
    
    //get the connection and the grouper session
    String connectionName = clientGroupConfigBean.getConnectionName();

    debugMap.put("connectionName", connectionName);

    final ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(connectionName);
    
    if (clientConnectionConfigBean == null) {
      throw new RuntimeException("Cant find clientConnectionBean by config id: '"       
          + configName + "', connectionId: '" + connectionName + "'");      
    }
    
    String actAsSubjectString = clientConnectionConfigBean.getLocalActAsSubject();

    Subject actAsSubject = null;
    if (StringUtils.isBlank(actAsSubjectString)) {
      actAsSubject = SubjectFinder.findRootSubject();
    } else {
      actAsSubject = SubjectFinder.findByPackedSubjectString(actAsSubjectString, true);
    }    

    debugMap.put("actAsSubject", GrouperUtil.subjectToString(actAsSubject));

    GrouperSession grouperSession = GrouperSession.start(actAsSubject, false);
    
    try {
      int result = (Integer)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          //process the rest
          GroupSyncType groupSyncType = clientGroupConfigBean.getGroupSyncType();
          
          if (groupSyncType == GroupSyncType.pull) {    
            return syncGroupPull(clientGroupConfigBean, clientConnectionConfigBean, theGrouperSession, debugMap);
          } else if (groupSyncType == GroupSyncType.incremental_push || groupSyncType == GroupSyncType.push) {    
            return syncGroupPush(clientGroupConfigBean, clientConnectionConfigBean, theGrouperSession, debugMap);
          } else {    
            throw new RuntimeException("Not expecting configName: " + configName + ", groupSyncType: " + groupSyncType);
          }   
        }
      });

      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
      return result;
    } catch (RuntimeException re) {
      
      LOG.error(GrouperUtil.mapToString(debugMap));
      GrouperUtil.injectInException(re, "Error in configName: " + configName);
      throw re;

    } finally {     
      GrouperSession.stopQuietly(grouperSession); 
    }     
    
  }

  /**
   * pull a full group from a remote site, and return the number of records changed
   * @param clientGroupConfigBean
   * @param clientConnectionConfigBean 
   * @param grouperSession 
   * @return the number of records changed
   */
  private static int syncGroupPull(ClientGroupConfigBean clientGroupConfigBean,     
      ClientConnectionConfigBean clientConnectionConfigBean, GrouperSession grouperSession,
      Map<String, Object> debugMap) {
    return -1;
//    GcAddMember gcAddMember = new GcAddMember();
//    Subject subject = groupSyncConsumerBean.getSubject();
//    String localGroupName = clientGroupConfigBean.getLocalGroupName();
//    
//    ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(clientGroupConfigBean.getConnectionName());
//    
//    ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean.getClientConnectionSourceConfigBeans().get(subject.getSourceId());
//
//    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
//    
//    if (!StringUtils.isBlank(clientConnectionSourceConfigBean.getRemoteSourceId())) {
//      wsSubjectLookup.setSubjectSourceId(clientConnectionSourceConfigBean.getRemoteSourceId());
//    }
//    
//    String localReadSubjectId = clientConnectionSourceConfigBean.getLocalReadSubjectId();
//    String subjectIdentifier = null;
//    if (StringUtils.equals("id", localReadSubjectId)) {
//      subjectIdentifier = subject.getId();
//    } else {
//      subjectIdentifier = subject.getAttributeValue(localReadSubjectId);
//    }
//    
//    GroupSyncWriteIdentifier groupSyncWriteIdentifier = (GroupSyncWriteIdentifier)ObjectUtils.defaultIfNull(
//        clientConnectionSourceConfigBean.getRemoteWriteSubjectId(), GroupSyncWriteIdentifier.idOrIdentifier);
//    
//    groupSyncWriteIdentifier.assignIdentifier(wsSubjectLookup, subjectIdentifier);
//    
//    gcAddMember.addSubjectLookup(wsSubjectLookup);
//    
//    gcAddMember.assignAddExternalSubjectIfNotFound(clientConnectionSourceConfigBean.getAddExternalSubjectIfNotFound());
//    
//    gcAddMember.assignGroupName(clientGroupConfigBean.getRemoteGroupName());
//
//    StringBuilder logMessage = new StringBuilder();
//    if (LOG.isDebugEnabled()) {
//      logMessage.append("Sending add member " + GrouperUtil.subjectToString(subject) + ", " 
//          + subjectIdentifier + ", to remote grouper: " + clientConnectionConfigBean.getConnectionId()
//          + ", from local group: " + localGroupName + ", to remote group: " + clientGroupConfigBean.getRemoteGroupName() );
//    }
//    WsAddMemberResults wsAddMemberResults = null;
//    boolean success = false;
//    try {
//      
//      wsAddMemberResults = gcAddMember.execute();
//      if (LOG.isDebugEnabled()) {
//        logMessage.append(", resultCode: " + wsAddMemberResults.getResultMetadata().getResultCode() 
//            + ", success: " + wsAddMemberResults.getResultMetadata().getSuccess());
//      }
//      success = GrouperUtil.booleanValue(wsAddMemberResults.getResultMetadata().getSuccess(), false);
//      
//    } finally {
//      if (success && LOG.isDebugEnabled()) {
//        LOG.debug(logMessage);
//      }
//      if (!success) {
//        LOG.error("Error sending add member to remote group: " + logMessage);
//      }
//    }

    
  }

  /**
   * push a full group to a remote site, and return the number of records changed
   * @param clientGroupConfigBean
   * @param clientConnectionConfigBean 
   * @param grouperSession 
   * @param debugMap 
   * @return the number of records changed
   */
  private static int syncGroupPush(ClientGroupConfigBean clientGroupConfigBean,     
      ClientConnectionConfigBean clientConnectionConfigBean, GrouperSession grouperSession,
      Map<String, Object> debugMap) {

    GcAddMember gcAddMember = new GcAddMember();

    String localGroupName = clientGroupConfigBean.getLocalGroupName();

    Group localGroup = GroupFinder.findByName(grouperSession, localGroupName, true);
    
    Set<Member> members = localGroup.getMembers();
    return -1;
////    Set<String> unmappedSourceIds = new HashSet()<String>();
//    
//    for (Member member : members) {
//      
//      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
//      String localSourceId = member.getSubjectSourceId();
//      ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean
//        .getClientConnectionSourceConfigBeans().get(localSourceId);
//      
//      if (clientConnectionSourceConfigBean == null) {
//        
//        if (!unmappedSourceIds.contains(localSourceId)) {
//          //keep track of which ones logged
//          unmappedSourceIds.add(localSourceId);
//          debugMap.put("unmapped_sourceId_" + localSourceId, true);
//        }
//        
//        continue;
//        
//      }
//      
//      if (!StringUtils.isBlank(clientConnectionSourceConfigBean.getLocalSourceId())) {
//        wsSubjectLookup.setSubjectSourceId(clientConnectionSourceConfigBean.getRemoteSourceId());
//      }
//      
//      String localReadSubjectId = clientConnectionSourceConfigBean.getLocalReadSubjectId();
//      String subjectIdentifier = null;
//      if (StringUtils.equals("id", localReadSubjectId)) {
//        subjectIdentifier = subject.getId();
//      } else {
//        subjectIdentifier = subject.getAttributeValue(localReadSubjectId);
//      }
//      
//      
//    }
//    
//    ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(clientGroupConfigBean.getConnectionName());
//    
//    ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean.getClientConnectionSourceConfigBeans().get(subject.getSourceId());
//
//    
//    GroupSyncWriteIdentifier groupSyncWriteIdentifier = (GroupSyncWriteIdentifier)ObjectUtils.defaultIfNull(
//        clientConnectionSourceConfigBean.getRemoteWriteSubjectId(), GroupSyncWriteIdentifier.idOrIdentifier);
//    
//    groupSyncWriteIdentifier.assignIdentifier(wsSubjectLookup, subjectIdentifier);
//    
//    gcAddMember.addSubjectLookup(wsSubjectLookup);
//    
//    gcAddMember.assignAddExternalSubjectIfNotFound(clientConnectionSourceConfigBean.getAddExternalSubjectIfNotFound());
//    
//    gcAddMember.assignGroupName(clientGroupConfigBean.getRemoteGroupName());
//
//    StringBuilder logMessage = new StringBuilder();
//    if (LOG.isDebugEnabled()) {
//      logMessage.append("Sending add member " + GrouperUtil.subjectToString(subject) + ", " 
//          + subjectIdentifier + ", to remote grouper: " + clientConnectionConfigBean.getConnectionId()
//          + ", from local group: " + localGroupName + ", to remote group: " + clientGroupConfigBean.getRemoteGroupName() );
//    }
//    WsAddMemberResults wsAddMemberResults = null;
//    boolean success = false;
//    try {
//      
//      wsAddMemberResults = gcAddMember.execute();
//      if (LOG.isDebugEnabled()) {
//        logMessage.append(", resultCode: " + wsAddMemberResults.getResultMetadata().getResultCode() 
//            + ", success: " + wsAddMemberResults.getResultMetadata().getSuccess());
//      }
//      success = GrouperUtil.booleanValue(wsAddMemberResults.getResultMetadata().getSuccess(), false);
//      
//    } finally {
//      if (success && LOG.isDebugEnabled()) {
//        LOG.debug(logMessage);
//      }
//      if (!success) {
//        LOG.error("Error sending add member to remote group: " + logMessage);
//      }
//    }
    
  }
}
