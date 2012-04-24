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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;

/**
 * connection config information cached from grouper.properties
 * @author mchyzer
 *
 */
public class ClientConfig {

  /**
   * parse the config for writing to local or remote grouper, write by id or identifier or either
   *
   */
  public static enum GroupSyncWriteIdentifier {
    
    /** write with id */
    id {

      /**
       * @see GroupSyncWriteIdentifier#assignIdentifier(WsSubjectLookup, String)
       */
      @Override
      public void assignIdentifier(WsSubjectLookup wsSubjectLookup, String identifier) {
        wsSubjectLookup.setSubjectId(identifier);
      }

      /**
       * @see GroupSyncWriteIdentifier#findSubject(String, String)
       */
      @Override
      public Subject findSubject(String sourceId, String identifier) {
        
        if (StringUtils.isBlank(sourceId)) {
          return SubjectFinder.findById(identifier, false);
        }
        return SubjectFinder.findByIdAndSource(identifier, sourceId, false);
      }
    },
    
    /** write with identifier */
    identifier {

      /**
       * @see GroupSyncWriteIdentifier#assignIdentifier(WsSubjectLookup, String)
       */
      @Override
      public void assignIdentifier(WsSubjectLookup wsSubjectLookup, String identifier) {
        wsSubjectLookup.setSubjectIdentifier(identifier);
        
      }

      /**
       * @see GroupSyncWriteIdentifier#findSubject(String, String)
       */
      @Override
      public Subject findSubject(String sourceId, String identifier) {
        
        if (StringUtils.isBlank(sourceId)) {
          return SubjectFinder.findByIdentifier(identifier, false);
        }
        return SubjectFinder.findByIdentifierAndSource(identifier, sourceId, false);
      }
    },
    
    /** write with idOrIdentifier */
    idOrIdentifier {

      /**
       * @see GroupSyncWriteIdentifier#assignIdentifier(WsSubjectLookup, String)
       */
      @Override
      public void assignIdentifier(WsSubjectLookup wsSubjectLookup, String identifier) {
        wsSubjectLookup.setSubjectId(identifier);
        wsSubjectLookup.setSubjectIdentifier(identifier);
        
      }

      /**
       * @see GroupSyncWriteIdentifier#findSubject(String, String)
       */
      @Override
      public Subject findSubject(String sourceId, String identifier) {
        
        if (StringUtils.isBlank(sourceId)) {
          return SubjectFinder.findByIdOrIdentifier(identifier, false);
        }
        return SubjectFinder.findByIdOrIdentifierAndSource(identifier, sourceId, false);
      }
    };

    /**
     * assign the identifier
     * @param wsSubjectLookup
     * @param identifier
     */
    public abstract void assignIdentifier(WsSubjectLookup wsSubjectLookup, String identifier);
    
    /**
     * assign the identifier
     * @param sourceId
     * @param identifier
     * @return the subject
     */
    public abstract Subject findSubject(String sourceId, String identifier);
    
    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @param exceptionOnNull will not allow null or blank entries
     * @return the enum or null or exception if not found
     */
    public static GroupSyncWriteIdentifier valueOfIgnoreCase(String string, boolean exceptionOnNull) {
      return GrouperUtil.enumValueOfIgnoreCase(GroupSyncWriteIdentifier.class, 
          string, exceptionOnNull);
    
    }
  }
  
  /**
   * holds the state of one connection in the
   */
  public static class ClientConnectionSourceConfigBean {
    
    /**
     * id of this connection source config
     */
    private String configId;
    
    /**
     * id of this connection source config
     * @return the id
     */
    public String getConfigId() {
      return this.configId;
    }

    /**
     * id of this connection source config
     * @param id1
     */
    public void setConfigId(String id1) {
      this.configId = id1;
    }

    /**
     * sourceId can be blank if you dont want to specify
     */
    private String localSourceId;
    
    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     */
    private String localReadSubjectId;
    
    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
     */
    private GroupSyncWriteIdentifier localWriteSubjectId;
    
    
    /**
     * sourceId can be blank if you dont want to specify
     */
    private String remoteSourceId;
    
    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     */
    private String remoteReadSubjectId;
    
    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
     */
    private GroupSyncWriteIdentifier remoteWriteSubjectId;
    
    /**
     * sourceId can be blank if you dont want to specify
     * @return sourceId
     */
    public String getLocalSourceId() {
      return this.localSourceId;
    }

    /**
     * sourceId can be blank if you dont want to specify
     * @param localSourceId1
     */
    public void setLocalSourceId(String localSourceId1) {
      this.localSourceId = localSourceId1;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @return local read subject id
     */
    public String getLocalReadSubjectId() {
      return this.localReadSubjectId;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @param localReadSubjectId1
     */
    public void setLocalReadSubjectId(String localReadSubjectId1) {
      this.localReadSubjectId = localReadSubjectId1;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
     * @return identifier
     */
    public GroupSyncWriteIdentifier getLocalWriteSubjectId() {
      return this.localWriteSubjectId;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
     * @param localWriteSubjectId1
     */
    public void setLocalWriteSubjectId(GroupSyncWriteIdentifier localWriteSubjectId1) {
      this.localWriteSubjectId = localWriteSubjectId1;
    }

    /**
     * sourceId can be blank if you dont want to specify
     * @return sourceId
     */
    public String getRemoteSourceId() {
      return this.remoteSourceId;
    }

    /**
     * sourceId can be blank if you dont want to specify
     * @param remoteSourceId1
     */
    public void setRemoteSourceId(String remoteSourceId1) {
      this.remoteSourceId = remoteSourceId1;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @return remoteReadSubjectId
     */
    public String getRemoteReadSubjectId() {
      return this.remoteReadSubjectId;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @param remoteReadSubjectId1
     */
    public void setRemoteReadSubjectId(String remoteReadSubjectId1) {
      this.remoteReadSubjectId = remoteReadSubjectId1;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
     * @return remote write subject id
     */
    public GroupSyncWriteIdentifier getRemoteWriteSubjectId() {
      return this.remoteWriteSubjectId;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
     * @param remoteWriteSubjectId1
     */
    public void setRemoteWriteSubjectId(GroupSyncWriteIdentifier remoteWriteSubjectId1) {
      this.remoteWriteSubjectId = remoteWriteSubjectId1;
    }

  }

  /**
   * bean represents the group connections to external groupers
   */
  public static class ClientGroupConfigBean {
    
    /**
     * <pre>
     * # connection name in grouper client connections above
     * #syncAnotherGrouper.testGroup0.connectionName = someOtherSchool
     * </pre>
     */
    private String connectionName;
    
    /**
     * in the config settings, this is the part that ties the configs together
     */
    private String configId;
    
    /**
     * in the config settings, this is the part that ties the configs together
     * @return the config id
     */
    public String getConfigId() {
      return this.configId;
    }

    /**
     * in the config settings, this is the part that ties the configs together
     * @param configId1
     */
    public void setConfigId(String configId1) {
      this.configId = configId1;
    }

    /**
     * <pre>
     * # incremental  or  push  or   pull  or  incremental_push
     * #syncAnotherGrouper.testGroup0.syncType = incremental_push
     * </pre>
     */
    private GroupSyncType groupSyncType;

    /**
     * <pre>
     * # quartz cron  to schedule the pull or push (incremental is automatic as events happen) (e.g. 5am daily)
     * #syncAnotherGrouper.testGroup0.cron =  0 0 5 * * ?
     * </pre>
     */
    private String cron;

    /**
     * <pre>
     * # local group which is being synced
     * #syncAnotherGrouper.testGroup0.local.groupName = test:testGroup
     * </pre>
     */
    private String localGroupName;
    
    /**
     * <pre>
     * # remote group at another grouper which is being synced
     * #syncAnotherGrouper.testGroup0.remote.groupName = test2:testGroup2
     * </pre>
     */
    private String remoteGroupName;

    /**
     * if subjects are external and should be created if not exist
     */
    private Boolean addExternalSubjectIfNotFound;

    /**
     * <pre>
     * # connection name in grouper client connections above
     * #syncAnotherGrouper.testGroup0.connectionName = someOtherSchool
     * </pre>
     * @return connection name
     */
    public String getConnectionName() {
      return this.connectionName;
    }

    /**
     * <pre>
     * # connection name in grouper client connections above
     * #syncAnotherGrouper.testGroup0.connectionName = someOtherSchool
     * </pre>
     * @param connectionName1
     */
    public void setConnectionName(String connectionName1) {
      this.connectionName = connectionName1;
    }

    /**
     * <pre>
     * # incremental  or  push  or   pull  or  incremental_push
     * #syncAnotherGrouper.testGroup0.syncType = incremental_push
     * </pre>
     * @return the sync type
     */
    public GroupSyncType getGroupSyncType() {
      return this.groupSyncType;
    }

    /**
     * <pre>
     * # incremental  or  push  or   pull  or  incremental_push
     * #syncAnotherGrouper.testGroup0.syncType = incremental_push
     * </pre>
     * @param groupSyncType1
     */
    public void setGroupSyncType(GroupSyncType groupSyncType1) {
      this.groupSyncType = groupSyncType1;
    }

    /**
     * <pre>
     * # quartz cron  to schedule the pull or push (incremental is automatic as events happen) (e.g. 5am daily)
     * #syncAnotherGrouper.testGroup0.cron =  0 0 5 * * ?
     * </pre>
     * @return the cron
     */
    public String getCron() {
      return this.cron;
    }

    /**
     * <pre>
     * # quartz cron  to schedule the pull or push (incremental is automatic as events happen) (e.g. 5am daily)
     * #syncAnotherGrouper.testGroup0.cron =  0 0 5 * * ?
     * </pre>
     * @param cron1
     */
    public void setCron(String cron1) {
      this.cron = cron1;
    }

    /**
     * <pre>
     * # local group which is being synced
     * #syncAnotherGrouper.testGroup0.local.groupName = test:testGroup
     * </pre>
     * @return local group name
     */
    public String getLocalGroupName() {
      return this.localGroupName;
    }

    /**
     * <pre>
     * # local group which is being synced
     * #syncAnotherGrouper.testGroup0.local.groupName = test:testGroup
     * </pre>
     * @param localGroupName1
     */
    public void setLocalGroupName(String localGroupName1) {
      this.localGroupName = localGroupName1;
    }

    /**
     * <pre>
     * # remote group at another grouper which is being synced
     * #syncAnotherGrouper.testGroup0.remote.groupName = test2:testGroup2
     * </pre>
     * @return remote group name
     */
    public String getRemoteGroupName() {
      return this.remoteGroupName;
    }

    /**
     * <pre>
     * # remote group at another grouper which is being synced
     * #syncAnotherGrouper.testGroup0.remote.groupName = test2:testGroup2
     * </pre>
     * @param remoteGroupName1
     */
    public void setRemoteGroupName(String remoteGroupName1) {
      this.remoteGroupName = remoteGroupName1;
    }

    /**
     * if subjects are external and should be created if not exist
     * @return if add
     */
    public Boolean getAddExternalSubjectIfNotFound() {
      return this.addExternalSubjectIfNotFound;
    }

    /**
     * if subjects are external and should be created if not exist
     * @param addExternalSubjectIfNotFound1
     */
    public void setAddExternalSubjectIfNotFound(Boolean addExternalSubjectIfNotFound1) {
      this.addExternalSubjectIfNotFound = addExternalSubjectIfNotFound1;
    }

  }
  
  /**
   * holds the state of one connection in the
   *
   */
  public static class ClientConnectionConfigBean {

    /**
     * connection id in config
     */
    private String connectionId;
    
    
    
    /**
     * connection id in config
     * @return connection id in config
     */
    public String getConnectionId() {
      return this.connectionId;
    }

    /**
     * connection id in config
     * @param connectionId1
     */
    public void setConnectionId(String connectionId1) {
      this.connectionId = connectionId1;
    }

    /**
     * describes sources in the grouper config, key is sourceId
     */
    private Map<String,ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans = null;

    /**
     * describes sources in the grouper config, key is sourceId
     * @return the sources
     */
    public Map<String,ClientConnectionSourceConfigBean> getClientConnectionSourceConfigBeans() {
      return this.clientConnectionSourceConfigBeans;
    }

    /**
     * describes sources in the grouper config, key is sourceId
     * @param clientConnectionSourceConfigBeans1
     */
    public void setClientConnectionSourceConfigBeans(
        Map<String,ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans1) {
      this.clientConnectionSourceConfigBeans = clientConnectionSourceConfigBeans1;
    }
    
    /**
     * this is the subject to act as local, if blank, act as GrouperSystem, specify with SubjectFinder packed string, e.g.
     * subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
     * sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier
     */
    private String localActAsSubject;
    /**
     * this is the subject to act as local, if blank, act as GrouperSystem, specify with SubjectFinder packed string, e.g.
     * subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
     * sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier
     * @return local act as subject
     */
    public String getLocalActAsSubject() {
      return this.localActAsSubject;
    }

    /**
     * this is the subject to act as local, if blank, act as GrouperSystem, specify with SubjectFinder packed string, e.g.
     * subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
     * sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier
     * @param localActAsSubject1
     */
    public void setLocalActAsSubject(String localActAsSubject1) {
      this.localActAsSubject = localActAsSubject1;
    }

    
    //# the part between "grouperClient.localhost.source." and ".id" links up the configs, 
    //# in this case, "jdbc", make sure it has no special chars.  sourceId can be blank if you dont want to specify
    //grouperClient.localhost.source.jdbc.local.sourceId = jdbc
    //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
    //grouperClient.localhost.source.jdbc.local.read.subjectId = identifier
    //# this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
    //grouperClient.localhost.source.jdbc.local.write.subjectId = identifier
    //# sourceId of the remote system, can be blank
    //grouperClient.localhost.source.jdbc.remote.sourceId = jdbc
    //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
    //grouperClient.localhost.source.jdbc.remote.read.subjectId = 
    //# this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
    //grouperClient.localhost.source.jdbc.remote.write.subjectId = 
    //# if subjects are external and should be created if not exist
    //grouperClient.localhost.source.jdbc.addExternalSubjectIfNotFound = true

  }
  
  /** cache this so if file changes it will pick it back up, key is connection id */
  private static GrouperCache<Boolean, Map<String, ClientConnectionConfigBean>> clientConnectionConfigBeanCache = new GrouperCache<Boolean, Map<String, ClientConnectionConfigBean>>(
      ClientConfig.class.getName() + ".clientConnectionConfigBeanCache", 50, false, 300, 300, false);
  
  /** cache this so if file changes it will pick it back up.  key is local group name */
  private static GrouperCache<Boolean, Map<String, ClientGroupConfigBean>> clientGroupConfigBeanCache = new GrouperCache<Boolean, Map<String, ClientGroupConfigBean>>(
      ClientConfig.class.getName() + ".clientGroupConfigBeanCache", 50, false, 300, 300, false);
  
  /**
   * clear the config cache (e.g. for testing)
   */
  public static void clearCache() {
    clientConnectionConfigBeanCache.clear();
    clientGroupConfigBeanCache.clear();
  }
  
  /**
   * get the bean map from cache or configure a new one
   * @return the config bean
   */
  public static Map<String, ClientConnectionConfigBean> clientConnectionConfigBeanCache() {
    Map<String, ClientConnectionConfigBean> theClientConnectionConfigBeanCache = clientConnectionConfigBeanCache.get(Boolean.TRUE);
    
    if (theClientConnectionConfigBeanCache == null) {
      
      synchronized (ExternalSubjectConfig.class) {

        //try again
        theClientConnectionConfigBeanCache = clientConnectionConfigBeanCache.get(Boolean.TRUE);
        if (theClientConnectionConfigBeanCache == null) {
          
          theClientConnectionConfigBeanCache = new HashMap<String, ClientConnectionConfigBean>();
          
          for (String propertyName : GrouperConfig.getPropertyNames()) {
            Matcher matcher = grouperClientConnectionIdPattern.matcher(propertyName);
            if (matcher.matches()) {

              //this is the ID
              String connectionId = matcher.group(1);
              
              ClientConnectionConfigBean clientConnectionConfigBean = new ClientConnectionConfigBean();
              
              //note, doesnt really matter what the id is... but it is a mandatory config
              
              //get the act as subject
              //grouperClient.localhost.localActAsSubject
              clientConnectionConfigBean.setLocalActAsSubject(GrouperConfig.getProperty(
                  "grouperClient." + connectionId + ".localActAsSubject"));
              
              clientConnectionConfigBean.setConnectionId(connectionId);
              
              clientConnectionConfigBean.setClientConnectionSourceConfigBeans(
                  ClientConfig.clientConnectionSourceConfigBeans(connectionId));
              
              theClientConnectionConfigBeanCache.put(connectionId, clientConnectionConfigBean);

            }
          }
          clientConnectionConfigBeanCache.put(Boolean.TRUE, theClientConnectionConfigBeanCache);
        }        
      }
    }
    return theClientConnectionConfigBeanCache;
  }

  /**
   * get the bean map from cache or configure a new one
   * @return the config bean
   */
  public static Map<String, ClientGroupConfigBean> clientGroupConfigBeanCache() {
    Map<String, ClientGroupConfigBean> theClientGroupConfigBeanCache = clientGroupConfigBeanCache.get(Boolean.TRUE);
    
    if (theClientGroupConfigBeanCache == null) {
      
      synchronized (ExternalSubjectConfig.class) {

        //try again
        theClientGroupConfigBeanCache = clientGroupConfigBeanCache.get(Boolean.TRUE);
        if (theClientGroupConfigBeanCache == null) {
          
          theClientGroupConfigBeanCache = new HashMap<String, ClientGroupConfigBean>();
          
          for (String propertyName : GrouperConfig.getPropertyNames()) {
            Matcher matcher = grouperClientGroupConnectionNamePattern.matcher(propertyName);
            if (matcher.matches()) {

              //this is the ID
              String groupConfigName = matcher.group(1);
              
              ClientGroupConfigBean clientGroupConfigBean = new ClientGroupConfigBean();
              
              //# we need to know where our
              //# connection name in grouper client connections above
              //#syncAnotherGrouper.testGroup0.connectionName = someOtherSchool
              clientGroupConfigBean.setConnectionName(GrouperConfig.getProperty(propertyName));
              
              clientGroupConfigBean.setConfigId(groupConfigName);
              
              //
              //# incremental  or  push  or   pull  or  incremental_push
              //#syncAnotherGrouper.testGroup0.syncType = incremental_push
              String syncType = GrouperConfig.getProperty("syncAnotherGrouper." + groupConfigName + ".syncType");
              if (StringUtils.isBlank(syncType)) {
                LOG.error("You need to pass in a sync type: syncAnotherGrouper." + groupConfigName + ".syncType");
              } else {
                clientGroupConfigBean.setGroupSyncType(GroupSyncType.valueOfIgnoreCase(syncType, true));
              }
              
              //
              //# quartz cron  to schedule the pull or push (incremental is automatic as events happen) (e.g. 5am daily)
              //#syncAnotherGrouper.testGroup0.cron =  0 0 5 * * ?
              clientGroupConfigBean.setCron(GrouperConfig.getProperty("syncAnotherGrouper." + groupConfigName + ".cron"));
              
              //
              //# local group which is being synced
              //#syncAnotherGrouper.testGroup0.local.groupName = test:testGroup
              String localGroupName = GrouperConfig.getProperty("syncAnotherGrouper." + groupConfigName + ".local.groupName");
              clientGroupConfigBean.setLocalGroupName(localGroupName);
              
              //
              //# remote group at another grouper which is being synced
              //#syncAnotherGrouper.testGroup0.remote.groupName = test2:testGroup2
              clientGroupConfigBean.setRemoteGroupName(GrouperConfig.getProperty("syncAnotherGrouper." + groupConfigName + ".remote.groupName"));
              
              
              //# if subjects are external and should be created if not exist
              //#syncAnotherGrouper.testGroup0.addExternalSubjectIfNotFound = true
              String theAddExternalSubjectIfNotFound = GrouperConfig.getProperty(
                  "syncAnotherGrouper." + groupConfigName + ".addExternalSubjectIfNotFound");
              if (!StringUtils.isBlank(theAddExternalSubjectIfNotFound)) {
                clientGroupConfigBean.setAddExternalSubjectIfNotFound(GrouperUtil.booleanValue(theAddExternalSubjectIfNotFound));
              }

              theClientGroupConfigBeanCache.put(localGroupName, clientGroupConfigBean);

            }
          }
          clientGroupConfigBeanCache.put(Boolean.TRUE, theClientGroupConfigBeanCache);
        }        
      }
    }
    return theClientGroupConfigBeanCache;
  }

  /**
   * get the client connection source config beans based on connection id
   * @param connectionId
   * @return the beans
   */
  private static Map<String,ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans(String connectionId) {

    //grouperClient.localhost.source.jdbc.id
    Pattern pattern = Pattern.compile("^grouperClient\\." + connectionId + "\\.source\\.([^.]+)\\.id$");
    
    Map<String,ClientConnectionSourceConfigBean> result = new HashMap<String, ClientConnectionSourceConfigBean>();
    
    //lets get the sources
    for (String sourcePropertyName : GrouperConfig.getPropertyNames()) {
      Matcher sourceMatcher = pattern.matcher(sourcePropertyName);
      if (sourceMatcher.matches()) {

        //id of the config
        String configId = GrouperConfig.getProperty(sourcePropertyName);
        
        
        //this is the ID
        String sourceConfigKey = sourceMatcher.group(1);
        
        ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = new ClientConnectionSourceConfigBean();
        
        clientConnectionSourceConfigBean.setConfigId(configId);
        
        //note, doesnt really matter what the id is... but it is a mandatory config so we can get started

        //# the part between "grouperClient.localhost.source." and ".id" links up the configs, 
        //# in this case, "jdbc", make sure it has no special chars.  sourceId can be blank if you dont want to specify
        //grouperClient.localhost.source.jdbc.local.sourceId = jdbc
        String localSourceId = GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceConfigKey + ".local.sourceId");
        
        clientConnectionSourceConfigBean.setLocalSourceId(localSourceId);
        
        //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
        //grouperClient.localhost.source.jdbc.local.read.subjectId = identifier
        clientConnectionSourceConfigBean.setLocalReadSubjectId(GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceConfigKey + ".local.read.subjectId"));

        
        //# this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
        //grouperClient.localhost.source.jdbc.local.write.subjectId = identifier
        String localWriteSubjectId = GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceConfigKey + ".local.write.subjectId");
        clientConnectionSourceConfigBean.setLocalWriteSubjectId(
            GroupSyncWriteIdentifier.valueOfIgnoreCase(localWriteSubjectId, false));
        
        //# sourceId of the remote system, can be blank
        //grouperClient.localhost.source.jdbc.remote.sourceId = jdbc
        clientConnectionSourceConfigBean.setRemoteSourceId(GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceConfigKey + ".remote.sourceId"));

        //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
        //grouperClient.localhost.source.jdbc.remote.read.subjectId = 
        clientConnectionSourceConfigBean.setRemoteReadSubjectId(GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceConfigKey + ".remote.read.subjectId"));

        //# this is the identifier to lookup to add a subject, should be "id" or "identifier" or "idOrIdentifier"
        //grouperClient.localhost.source.jdbc.remote.write.subjectId = 
        String remoteWriteSubjectId = GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceConfigKey + ".remote.write.subjectId");
        clientConnectionSourceConfigBean.setRemoteWriteSubjectId(
            GroupSyncWriteIdentifier.valueOfIgnoreCase(remoteWriteSubjectId, false));

        //we are going by source id (local)
        result.put(localSourceId, clientConnectionSourceConfigBean);
      }
    }
    return result;
  }
  
  /**
   * grouperClient.localhost.id
   * <pre>
   * ^grouperClient\.           matches start of string, externalSubjects, then a dot
   * ([^.]+)\.                  matches something not a dot, captures that, then a dot
   * id$                        matches id, and end of string
   * </pre>
   */
  private static final Pattern grouperClientConnectionIdPattern = Pattern.compile("^grouperClient\\.([^.]+)\\.id$");
  
  /**
   * #syncAnotherGrouper.testGroup0.connectionName = someOtherSchool
   * <pre>
   * ^syncAnotherGrouper\.      matches start of string, externalSubjects, then a dot
   * ([^.]+)\.                  matches something not a dot, captures that, then a dot
   * connectionName$            matches id, and end of string
   * </pre>
   */
  private static final Pattern grouperClientGroupConnectionNamePattern = Pattern.compile("^syncAnotherGrouper\\.([^.]+)\\.connectionName$");

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ClientConfig.class);
  
}
