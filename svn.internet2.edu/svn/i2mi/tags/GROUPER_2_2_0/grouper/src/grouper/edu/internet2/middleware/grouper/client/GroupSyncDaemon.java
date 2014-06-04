/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.client;

import java.util.HashMap;
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
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
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
   * @param localGroupName
   * @return the number of records changed
   */
  public static int syncGroup(final String localGroupName) {

    final Map<String, Object> infoMap = new LinkedHashMap<String, Object>();
    
    infoMap.put("configName", localGroupName);
    
    final ClientGroupConfigBean clientGroupConfigBean = ClientConfig.clientGroupConfigBeanCache().get(localGroupName);

    if (clientGroupConfigBean == null) {
      throw new RuntimeException("Cant find clientGroupConfigBean by config id: '" + localGroupName + "'");
    }
    
    //get the connection and the grouper session
    String connectionName = clientGroupConfigBean.getConnectionName();

    infoMap.put("connectionName", connectionName);

    final ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(connectionName);
    
    if (clientConnectionConfigBean == null) {
      throw new RuntimeException("Cant find clientConnectionBean by config id: '"       
          + localGroupName + "', connectionId: '" + connectionName + "'");      
    }

    String actAsSubjectString = clientConnectionConfigBean.getLocalActAsSubject();

    Subject actAsSubject = null;
    if (StringUtils.isBlank(actAsSubjectString)) {
      actAsSubject = SubjectFinder.findRootSubject();
    } else {
      actAsSubject = SubjectFinder.findByPackedSubjectString(actAsSubjectString, true);
    }    

    infoMap.put("actAsSubject", GrouperUtil.subjectToString(actAsSubject));

    GrouperSession grouperSession = GrouperSession.start(actAsSubject, false);
    
    //get the connection, and set it up
    ClientCustomizerContext clientCustomizerContext = new ClientCustomizerContext();
    clientCustomizerContext.setConnectionName(connectionName);
    ClientCustomizer clientCustomizer = new ClientCustomizer();
    clientCustomizer.init(clientCustomizerContext);
    clientCustomizer.setupConnection();
    
    try {
      int result = (Integer)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          //process the rest
          GroupSyncType groupSyncType = clientGroupConfigBean.getGroupSyncType();
          infoMap.put("groupSyncType", groupSyncType);
          if (groupSyncType == GroupSyncType.pull) {    
            return syncGroupPull(clientGroupConfigBean, clientConnectionConfigBean, theGrouperSession, infoMap);
          } else if (groupSyncType == GroupSyncType.incremental_push || groupSyncType == GroupSyncType.push) {    
            return syncGroupPush(clientGroupConfigBean, clientConnectionConfigBean, theGrouperSession, infoMap);
          } else {    
            throw new RuntimeException("Not expecting configName: " + localGroupName + ", groupSyncType: " + groupSyncType);
          }   
        }
      });

      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(infoMap));
      }
      return result;
    } catch (RuntimeException re) {
      
      LOG.error(GrouperUtil.mapToString(infoMap));
      GrouperUtil.injectInException(re, "Error in configName: " + localGroupName);
      throw re;

    } finally {     
      clientCustomizer.teardownConnection();
      GrouperSession.stopQuietly(grouperSession); 
    }     
    
  }

  /**
   * pull a full group from a remote site, and return the number of records changed
   * @param clientGroupConfigBean
   * @param clientConnectionConfigBean 
   * @param grouperSession 
   * @param infoMap
   * @return the number of records changed
   */
  private static int syncGroupPull(ClientGroupConfigBean clientGroupConfigBean,     
      ClientConnectionConfigBean clientConnectionConfigBean, GrouperSession grouperSession,
      Map<String, Object> infoMap) {

    GcGetMembers gcGetMembers = new GcGetMembers();

    String localGroupName = clientGroupConfigBean.getLocalGroupName();
    
    WsGetMembersResults wsGetMembersResults = null;
    Boolean success = false;
    Exception exception = null;
    try {
      
      Group localGroup = GroupFinder.findByName(grouperSession, localGroupName, true);
        
      //get remote members
      gcGetMembers.addGroupName(clientGroupConfigBean.getRemoteGroupName());

      Set<ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans = 
        new HashSet<ClientConnectionSourceConfigBean>(clientConnectionConfigBean.getClientConnectionSourceConfigBeans().values());
      
      if (GrouperUtil.length(clientConnectionSourceConfigBeans) == 0) {
        throw new RuntimeException("Why are no sources configured for this feed? " + clientConnectionConfigBean.getConnectionId());
      }

      //map by remote source id to the config
      Map<String, ClientConnectionSourceConfigBean> remoteSourceMap = new HashMap<String, ClientConnectionSourceConfigBean>();
      
      //add all the sources to search in
      for (ClientConnectionSourceConfigBean clientConnectionSourceConfigBean : clientConnectionSourceConfigBeans) {
        String remoteSourceId = clientConnectionSourceConfigBean.getRemoteSourceId();
        if (GrouperUtil.isBlank(remoteSourceId)) {
          throw new RuntimeException("Why is the remote source id blank for this feed? " 
              + clientConnectionSourceConfigBean.getConfigId() + ", " + clientConnectionConfigBean.getConnectionId());
        }
        infoMap.put("filterByRemoteSourceId_" + remoteSourceId, true);
        gcGetMembers.addSourceId(remoteSourceId);
        
        remoteSourceMap.put(remoteSourceId, clientConnectionSourceConfigBean);
        
        //lets also retrieve this attribute
        String remoteReadSubjectId = clientConnectionSourceConfigBean.getRemoteReadSubjectId();

        if (GrouperUtil.isBlank(remoteReadSubjectId)) {
          throw new RuntimeException("Why is the remote read subject id blank for this feed? " 
              + clientConnectionSourceConfigBean.getConfigId() + ", " + clientConnectionConfigBean.getConnectionId());
        }

        infoMap.put("subjectIdForSourceId_" + remoteSourceId, remoteReadSubjectId);

        if (!StringUtils.equals("id", remoteReadSubjectId)) {
          gcGetMembers.addSubjectAttributeName(remoteReadSubjectId);
          infoMap.put("requestingAttribute_" + remoteReadSubjectId, true);
        }
      }

      
      //see if we need to add external members
      
      wsGetMembersResults = gcGetMembers.execute();
      success = GrouperUtil.booleanValue(wsGetMembersResults.getResultMetadata().getSuccess(), false);
      
      if (success) {
        success = null;
        //lets get the subjects from remote
        WsSubject[] wsSubjects = wsGetMembersResults.getResults()[0].getWsSubjects();
        
        int subjectIndex = -1;
        
        Set<Subject> subjectsToReplace = new HashSet<Subject>();
        
        for (WsSubject wsSubject : GrouperUtil.nonNull(wsSubjects, WsSubject.class)) {
          
          subjectIndex++;
          
          Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
  
          //lets get the remote source
          String remoteSourceId = wsSubject.getSourceId();
  
          if (LOG.isDebugEnabled()) {
            debugMap.put("remoteSourceId", remoteSourceId);
            debugMap.put("localGroup", localGroup.getName());
            debugMap.put("connection", clientConnectionConfigBean.getConnectionId());
            debugMap.put("remoteGroup", clientGroupConfigBean.getRemoteGroupName());
            debugMap.put("subjectIndex", subjectIndex);
          }
          
          //get the config bean
          ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = remoteSourceMap.get(remoteSourceId);
          
          String remoteReadSubjectId = clientConnectionSourceConfigBean.getRemoteReadSubjectId();
  
          if (LOG.isDebugEnabled()) {
            debugMap.put("remoteReadSubjectId", remoteReadSubjectId);
          }
  
          String localSubjectIdOrIdentifier = null;
          if (StringUtils.equals("id", remoteReadSubjectId)) {
            localSubjectIdOrIdentifier = wsSubject.getId();
          } else {
            
            //lets get the subject attribute
            localSubjectIdOrIdentifier = GrouperClientUtils.subjectAttributeValue(wsSubject, wsGetMembersResults.getSubjectAttributeNames(), remoteReadSubjectId);
            
          }
          
          String localSourceId = clientConnectionSourceConfigBean.getLocalSourceId();
  
          //find the subject or create
          GroupSyncWriteIdentifier localLookupType = clientConnectionSourceConfigBean.getLocalWriteSubjectId();
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("localSourceId", localSourceId);
            debugMap.put("localSubjectIdOrIdentifier", localSubjectIdOrIdentifier);
            debugMap.put("localLookupType", localLookupType == null ? null : localLookupType.name());
          }        
          
          Subject subject = null;
          
          try {
            subject = localLookupType.findSubject(localSourceId, localSubjectIdOrIdentifier);
            
          } catch (RuntimeException re) {
            if (LOG.isDebugEnabled()) {

              debugMap.put("problem getting subject", re.getMessage());
              LOG.error(GrouperUtil.mapToString(debugMap), re);
            } else {
              LOG.error("error running record: " + wsSubject.getSourceId() + ", " + wsSubject.getId(), re);
            }
             
            continue;
            
          }
  
          //create if not there
          if (subject == null) {
            
            if (LOG.isDebugEnabled()) {
              debugMap.put("subjectIdNull", true);
            }
            
            if (clientGroupConfigBean.getAddExternalSubjectIfNotFound() == null || !clientGroupConfigBean.getAddExternalSubjectIfNotFound()) {
              
              if (LOG.isDebugEnabled()) {
                //if not creating, then we need to skip...
                debugMap.put("addExternalSubjectIfNotFound", false);
                
                LOG.debug(GrouperUtil.mapToString(debugMap));
              }
              
              continue;
            }
            if (LOG.isDebugEnabled()) {
              //if not creating, then we need to skip...
              debugMap.put("addExternalSubjectIfNotFound", true);
            }
            
            //lets check the sourceId
            if (!StringUtils.isBlank(localSourceId) && !StringUtils.equals(localSourceId, ExternalSubject.sourceId())) {
              if (LOG.isDebugEnabled()) {
                //local source is specified and not equal to the external subject source id
                debugMap.put("localSourceDoesntEqualExternalSourceSoSkipping", true);
                debugMap.put("localExternalSourceId", ExternalSubject.sourceId());
                LOG.debug(GrouperUtil.mapToString(debugMap));
              }            
              continue;
            }
            
            //create external subject
            //if it is still null, then it doesnt exist... lets validate it
            final ExternalSubject externalSubject = new ExternalSubject();
            externalSubject.setIdentifier(localSubjectIdOrIdentifier);
            try {
              externalSubject.validateIdentifier();
            } catch (Exception e) {
              
              if (LOG.isDebugEnabled()) {
                debugMap.put("invalidExternalIdentifier", localSubjectIdOrIdentifier);
                LOG.debug(GrouperUtil.mapToString(debugMap));
              }
              continue;
            }
            
            //lets store this, without validation... as root
            //send the invite as root
            GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
                externalSubject.store(null, null, false, true, false);
                return null;
              }
            });
            
            subject = SubjectFinder.findByIdAndSource(externalSubject.getUuid(), ExternalSubject.sourceId(), false);
            if (subject == null) {
              
              LOG.error("This should not be null, it was just created: " + externalSubject.getUuid());
              if (LOG.isDebugEnabled()) {
                LOG.debug(GrouperUtil.mapToString(debugMap));
              }
              continue;
            }
          }
          
          //add to set of subjects to add
          subjectsToReplace.add(subject);
          
          //log an entry for each subject
          if (LOG.isDebugEnabled()) {
            LOG.debug(GrouperUtil.mapToString(debugMap));
          }
          
        }
        
        infoMap.put("filteredMemberSize", GrouperUtil.length(subjectsToReplace));
        
        //only do this for info since causes a query
        if (LOG.isInfoEnabled()) {
          infoMap.put("originalGroupMemberSize", localGroup.getMembers().size());
        }
        
        int changedRecords = localGroup.replaceMembers(subjectsToReplace);
        infoMap.put("changedRecords", changedRecords);
        success = true;
        return changedRecords;
      }
      success = false;
      throw new RuntimeException("Error");
    } catch (RuntimeException re) {
      infoMap.put("error", re.getMessage());
      exception = re;
      throw re;
    } finally {
      if (wsGetMembersResults != null && wsGetMembersResults.getResultMetadata() != null) {
        infoMap.put("resultCode", wsGetMembersResults.getResultMetadata().getResultCode());
        infoMap.put("resultCode2", wsGetMembersResults.getResultMetadata().getResultCode2());
        infoMap.put("success", wsGetMembersResults.getResultMetadata().getSuccess());
        infoMap.put("resultMessage", wsGetMembersResults.getResultMetadata().getResultMessage());
      }
      if (wsGetMembersResults != null && wsGetMembersResults.getResponseMetadata() != null) {
        infoMap.put("millis", wsGetMembersResults.getResponseMetadata().getMillis());
        infoMap.put("resultWarnings", wsGetMembersResults.getResponseMetadata().getResultWarnings());
        infoMap.put("serverVersion", wsGetMembersResults.getResponseMetadata().getServerVersion());
      }
      if (success != null && success && LOG.isInfoEnabled()) {
        LOG.info(GrouperUtil.mapToString(infoMap));
      }
      if (success == null || !success) {
        LOG.error(GrouperUtil.mapToString(infoMap), exception);
      }
    }

  }

  /**
   * push a full group to a remote site, and return the number of records changed
   * @param clientGroupConfigBean
   * @param clientConnectionConfigBean 
   * @param grouperSession 
   * @param infoMap 
   * @return the number of records changed
   */
  private static int syncGroupPush(ClientGroupConfigBean clientGroupConfigBean,     
      ClientConnectionConfigBean clientConnectionConfigBean, GrouperSession grouperSession,
      Map<String, Object> infoMap) {

    GcAddMember gcAddMember = new GcAddMember();

    String localGroupName = clientGroupConfigBean.getLocalGroupName();

    WsAddMemberResults wsAddMemberResults = null;
    boolean success = false;
    try {
      
      Group localGroup = GroupFinder.findByName(grouperSession, localGroupName, true);
  
      Set<Member> members = localGroup.getMembers();
  
      Set<String> unmappedSourceIds = new HashSet<String>();
  
      infoMap.put("originalMemberSize", GrouperUtil.length(members));
      
      int filteredMemberSize = 0;
      
      int subjectIndex = -1;
      
      for (Member member : members) {
        
        subjectIndex++;
        
        Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
  
        String localSourceId = member.getSubjectSourceId();
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("localGroup", localGroup.getName());
          debugMap.put("connection", clientConnectionConfigBean.getConnectionId());
          debugMap.put("remoteGroup", clientGroupConfigBean.getRemoteGroupName());
          debugMap.put("subjectIndex", subjectIndex);
          debugMap.put("localSubjectId", member.getSubjectId());
          debugMap.put("localSourceId", localSourceId);
        }
        
        //see if we should skip
        if (unmappedSourceIds.contains(localSourceId)) {
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("unmappedSourceId", true);
            LOG.debug(GrouperUtil.mapToString(debugMap));
          }
          
          continue;
          
        }
        
        ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean
          .getClientConnectionSourceConfigBeans().get(localSourceId);
        
        if (clientConnectionSourceConfigBean == null) {
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("unmappedSourceId", true);
            LOG.debug(GrouperUtil.mapToString(debugMap));
          }
  
          //keep track of which ones logged
          unmappedSourceIds.add(localSourceId);
          infoMap.put("unmapped_sourceId_" + localSourceId, true);
  
          continue;
  
        }
  
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
  
        String remoteSourceId = clientConnectionSourceConfigBean.getRemoteSourceId();
        if (!StringUtils.isBlank(remoteSourceId)) {
          wsSubjectLookup.setSubjectSourceId(remoteSourceId);
        }
        
        String localReadSubjectId = clientConnectionSourceConfigBean.getLocalReadSubjectId();
        String subjectIdentifier = null;
        if (StringUtils.equals("id", localReadSubjectId)) {
          subjectIdentifier = member.getSubjectId();
        } else {
          subjectIdentifier = member.getSubject().getAttributeValue(localReadSubjectId);
        }
  
        GroupSyncWriteIdentifier groupSyncWriteIdentifier = (GroupSyncWriteIdentifier)ObjectUtils.defaultIfNull(
            clientConnectionSourceConfigBean.getRemoteWriteSubjectId(), GroupSyncWriteIdentifier.idOrIdentifier);
        
        groupSyncWriteIdentifier.assignIdentifier(wsSubjectLookup, subjectIdentifier);
        
        //log an entry for each subject
        if (LOG.isDebugEnabled()) {
          debugMap.put("remoteSourceId", remoteSourceId);
          debugMap.put("localReadSubjectId", localReadSubjectId);
          debugMap.put("subjectIdentifier", subjectIdentifier);
          LOG.debug(GrouperUtil.mapToString(debugMap));
        }
  
        gcAddMember.addSubjectLookup(wsSubjectLookup);
        filteredMemberSize++;
  
      }
  
      infoMap.put("filteredMemberSize", filteredMemberSize);
  
      //if not true, dont set it, it defaults to false
      if (clientGroupConfigBean.getAddExternalSubjectIfNotFound() != null && clientGroupConfigBean.getAddExternalSubjectIfNotFound()) {
        gcAddMember.assignAddExternalSubjectIfNotFound(clientGroupConfigBean.getAddExternalSubjectIfNotFound());
      }
      
      //replace what was there...
      gcAddMember.assignReplaceAllExisting(true);
      gcAddMember.assignGroupName(clientGroupConfigBean.getRemoteGroupName());

      wsAddMemberResults = gcAddMember.execute();
      success = GrouperUtil.booleanValue(wsAddMemberResults.getResultMetadata().getSuccess(), false);

      //not sure how many changed... hmm, return all I guess...
      return filteredMemberSize;
    } catch (RuntimeException re) {
      infoMap.put("error", re.getMessage());
      throw re;
    } finally {
      if (wsAddMemberResults != null && wsAddMemberResults.getResultMetadata() != null) {
        infoMap.put("resultCode", wsAddMemberResults.getResultMetadata().getResultCode());
        infoMap.put("resultCode2", wsAddMemberResults.getResultMetadata().getResultCode2());
        infoMap.put("success", wsAddMemberResults.getResultMetadata().getSuccess());
        infoMap.put("resultMessage", wsAddMemberResults.getResultMetadata().getResultMessage());
      }
      if (wsAddMemberResults != null && wsAddMemberResults.getResponseMetadata() != null) {
        infoMap.put("millis", wsAddMemberResults.getResponseMetadata().getMillis());
        infoMap.put("resultWarnings", wsAddMemberResults.getResponseMetadata().getResultWarnings());
        infoMap.put("serverVersion", wsAddMemberResults.getResponseMetadata().getServerVersion());
      }
      if (success && LOG.isInfoEnabled()) {
        LOG.info(GrouperUtil.mapToString(infoMap));
      }
      if (!success) {
        LOG.error(GrouperUtil.mapToString(infoMap));
      }
    }
    
  }
}
