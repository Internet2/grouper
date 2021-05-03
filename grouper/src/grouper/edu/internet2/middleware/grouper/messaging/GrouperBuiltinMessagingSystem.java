/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.messaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.type.LongType;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.exception.GrouperStaleStateException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageDefault;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.subject.Subject;


/**
 * built in messaging system from database
 */
public class GrouperBuiltinMessagingSystem implements GrouperMessagingSystem {

  /**
   * log
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperBuiltinMessagingSystem.class);

  /**
   * action for send to topic
   */
  public static final String actionSendToTopic = "send_to_topic";

  /**
   * action for send to queue
   */
  public static final String actionSendToQueue = "send_to_queue";

  /**
   * action for receive
   */
  public static final String actionReceive = "receive";
  
  /**
   * cache stuff in messaging, key is Boolean.TRUE
   */
  private static GrouperCache<Boolean, GrouperBuiltinMessagingCache> messageCache = new GrouperCache(
      "edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem.messageCache");


  /**
   * clear the cache
   */
  private static void clearCache() {

    synchronized (GrouperBuiltinMessagingCache.class) {

      messageCache.clear();

    }
  }


  /**
   * get the cache, if needed recalculate everything
   * @return the cache
   */
  private static GrouperBuiltinMessagingCache retrieveCache() {

    final GrouperBuiltinMessagingCache[] grouperBuiltinMessagingCache = new GrouperBuiltinMessagingCache[]{messageCache.get(Boolean.TRUE)};
    if (grouperBuiltinMessagingCache[0] == null) {
      synchronized (GrouperBuiltinMessagingCache.class) {

        grouperBuiltinMessagingCache[0] = messageCache.get(Boolean.TRUE);

        if (grouperBuiltinMessagingCache[0] == null) {

          GrouperSession grouperSession = GrouperSession.staticGrouperSession().internal_getRootSession();
          GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
            
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              GrouperBuiltinMessagingCache tempGrouperBuiltinMessagingCache = new GrouperBuiltinMessagingCache();
              
              Stem messageStem = StemFinder.findByName(grouperSession, messageRootStemName(), true, new QueryOptions().secondLevelCache(false));
              
              tempGrouperBuiltinMessagingCache.setRootStem(messageStem);

              //role
              Role messagingRole = new GroupFinder().assignGroupNames(GrouperUtil.toSet(grouperMessageNameOfRole())).findGroup();
              
              tempGrouperBuiltinMessagingCache.setMessagingRole(messagingRole);

              Stem queueStem = StemFinder.findByName(grouperSession, queueStemName(), true, new QueryOptions().secondLevelCache(false));

              {
                //queue
                AttributeDef queueDef = new AttributeDefFinder().assignFindByUuidOrName(false).assignScope(grouperMessageQueueNameOfDef()).findAttribute();
                
                tempGrouperBuiltinMessagingCache.setQueueAttributeDef(queueDef);
                
                //queueMap
                Map<String, AttributeDefName> queueMap = new HashMap<String, AttributeDefName>();
                
                
                Set<AttributeDefName> queueAttributeDefNames = new AttributeDefNameFinder().assignParentStemId(messageStem.getId()).assignStemScope(Scope.ONE)
                    .assignAttributeDefId(queueDef.getId()).assignParentStemId(queueStem.getId()).findAttributeNames();

                for (AttributeDefName queueAttributeDefName : queueAttributeDefNames) {
                  queueMap.put(queueAttributeDefName.getExtension(), queueAttributeDefName);
                }
                
                tempGrouperBuiltinMessagingCache.setExtensionOfQueueToAttributeDefName(queueMap);
              }

              Stem topicStem = StemFinder.findByName(grouperSession, topicStemName(), true, new QueryOptions().secondLevelCache(false));

              {
                AttributeDef topicDef = new AttributeDefFinder().assignFindByUuidOrName(false).assignScope(grouperMessageTopicNameOfDef()).findAttribute();
                
                tempGrouperBuiltinMessagingCache.setTopicAttributeDef(topicDef);
                
                Map<String, AttributeDefName> topicMap = new HashMap<String, AttributeDefName>();
                
                
                Set<AttributeDefName> topicAttributeDefNames = new AttributeDefNameFinder().assignParentStemId(messageStem.getId()).assignStemScope(Scope.ONE)
                    .assignAttributeDefId(topicDef.getId()).assignParentStemId(topicStem.getId()).findAttributeNames();
    
                for (AttributeDefName topicAttributeDefName : topicAttributeDefNames) {
                  topicMap.put(topicAttributeDefName.getExtension(), topicAttributeDefName);
                }
                
                tempGrouperBuiltinMessagingCache.setExtensionOfTopicToAttributeDefName(topicMap);
              }

              Set<PermissionEntry> permissionEntries = new PermissionFinder().addRole(messagingRole).findPermissions();

              for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {

                AttributeDefName attributeDefName = permissionEntry.getAttributeDefName();

                if (StringUtils.equals(attributeDefName.getStemId(), queueStem.getId())) {
                  if (!StringUtils.equals(permissionEntry.getAction(), actionReceive)
                      && !StringUtils.equals(permissionEntry.getAction(), actionSendToQueue)) {
                    //ignore this
                    continue;
                  }
                } else if (StringUtils.equals(attributeDefName.getStemId(), topicStem.getId())) {
                  if (!StringUtils.equals(permissionEntry.getAction(), actionSendToTopic)) {
                    //ignore this
                    continue;
                  }
                } else {
                  //ignore this
                  continue;
                }

                //hashmap of MultiKey[source id, subject id, topic/queue extension name, action], boolean if allow or not, lazy loaded
                MultiKey multiKey = new MultiKey(permissionEntry.getSubjectSourceId(), permissionEntry.getSubjectId(),
                    attributeDefName.getExtension(), permissionEntry.getAction());
                tempGrouperBuiltinMessagingCache.messagingPermissions.put(multiKey, true);

              }

              messageCache.put(Boolean.TRUE, tempGrouperBuiltinMessagingCache);

              grouperBuiltinMessagingCache[0] = tempGrouperBuiltinMessagingCache;
              return null;
            }
          });
          
        }
        
      }
    }
    return grouperBuiltinMessagingCache[0];
  }
  
  /**
   * cache stuff for messaging
   */
  private static class GrouperBuiltinMessagingCache {
    
    /**
     * root message stem
     */
    private Stem rootStem = null;

    /**
     * queue attribute def
     */
    private AttributeDef queueAttributeDef;

    /**
     * topic attribute def
     */
    private AttributeDef topicAttributeDef;

    /**
     * map of queue extension to attribute def name
     */
    private Map<String, AttributeDefName> extensionOfQueueToAttributeDefName = null;

    /**
     * map of topic extension to attribute def name
     */
    private Map<String, AttributeDefName> extensionOfTopicToAttributeDefName = null;

    /**
     * topic extension sends to queue extension, lazy loaded
     */
    private Map<String, Set<String>> topicExtSendsToQueueExtMap = Collections.synchronizedMap(new HashMap<String, Set<String>>());

    /**
     * hashmap of MultiKey[source id, subject id, topic/queue extension name, action], boolean if allow or not, lazy loaded
     */
    private Map<MultiKey, Boolean> messagingPermissions = Collections.synchronizedMap(new HashMap<MultiKey, Boolean>());

    /**
     * role that users have for permissions
     */
    private Role messagingRole = null;
    
    /**
     * role that users have for permissions
     * @return the messagingRole
     */
    public Role getMessagingRole() {
      return this.messagingRole;
    }
    
    /**
     * role that users have for permissions
     * @param messagingRole1 the messagingRole to set
     */
    public void setMessagingRole(Role messagingRole1) {
      this.messagingRole = messagingRole1;
    }

    /**
     * if queue exists
     * @param queue
     * @return true if queue exists
     */
    public boolean queueExists(String queue) {
      return this.extensionOfQueueToAttributeDefName.containsKey(queue);
    }
    
    /**
     * if topic exists
     * @param topic
     * @return true if queue exists
     */
    public boolean topicExists(String topic) {
      return this.extensionOfTopicToAttributeDefName.containsKey(topic);
    }
    
    /**
     * lazy load which topics send to which queues
     * @param topicExt
     * @return the set of queues that a topic sends to
     */
    public Set<String> topicExtSendsToQueueExts(final String topicExt) {
      
      Map<String, Set<String>> theTopicExtSendsToQueueExts = getTopicExtSendsToQueueExtMap();
      
      Set<String> queueExtensions = theTopicExtSendsToQueueExts.get(topicExt);
      
      if (queueExtensions == null) {

        synchronized (theTopicExtSendsToQueueExts) {
          
          //make sure there is no race condition
          queueExtensions = theTopicExtSendsToQueueExts.get(topicExt);
          
          if (queueExtensions == null) {

            GrouperSession grouperSession = GrouperSession.staticGrouperSession().internal_getRootSession();
            queueExtensions = (Set<String>)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {
                AttributeDefName topicAttributeDefName = getExtensionOfTopicToAttributeDefName().get(topicExt);
                
                if (topicAttributeDefName == null) {
                  throw new RuntimeException("Cant find topic: " + topicExt);
                }
                
                Set<AttributeDefName> queuesImpliedByThis = topicAttributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
                
                Set<String> tempQueueExtensions = new TreeSet<String>();
                
                String queueStemName = queueStemName();

                for (AttributeDefName queueImpliedByThis : queuesImpliedByThis) {
                  
                  //see if in right folder
                  if (GrouperUtil.nameInFolderDirect(queueImpliedByThis.getName(), queueStemName)) {
                    
                    //see if exists in this cache
                    if (GrouperBuiltinMessagingCache.this.getExtensionOfQueueToAttributeDefName().get(queueImpliedByThis.getExtension()) != null) {
                      tempQueueExtensions.add(queueImpliedByThis.getExtension());
                      
                    }
                  }
                }
                return tempQueueExtensions;
              }
              
            });
            
            //apply the cache
            theTopicExtSendsToQueueExts.put(topicExt, queueExtensions);
          }
        }
      }
      return queueExtensions;
    }
    
    /**
     * hashmap of source id, subject id, topic name, boolean if allow or not, lazy loaded
     * @return the allowedToSendToTopic
     */
    public Map<MultiKey, Boolean> getMessagingPermissions() {
      return this.messagingPermissions;
    }

    
    /**
     * topic extension sends to queue extension, lazy loaded
     * @return the topicExtSendsToQueueExtMap
     */
    public Map<String, Set<String>> getTopicExtSendsToQueueExtMap() {
      return this.topicExtSendsToQueueExtMap;
    }
    
    /**
     * root message stem
     * @return the rootStem
     */
    @SuppressWarnings("unused")
    public Stem getRootStem() {
      return this.rootStem;
    }

    
    /**
     * root message stem
     * @param rootStem1 the rootStem to set
     */
    public void setRootStem(Stem rootStem1) {
      this.rootStem = rootStem1;
    }

    
    /**
     * queue attribute def
     * @return the queueAttributeDef
     */
    @SuppressWarnings("unused")
    public AttributeDef getQueueAttributeDef() {
      return this.queueAttributeDef;
    }

    
    /**
     * queue attribute def
     * @param queueAttributeDef1 the queueAttributeDef to set
     */
    public void setQueueAttributeDef(AttributeDef queueAttributeDef1) {
      this.queueAttributeDef = queueAttributeDef1;
    }

    
    /**
     * topic attribute def
     * @return the topicAttributeDef
     */
    @SuppressWarnings("unused")
    public AttributeDef getTopicAttributeDef() {
      return this.topicAttributeDef;
    }

    
    /**
     * topic attribute def
     * @param topicAttributeDef1 the topicAttributeDef to set
     */
    public void setTopicAttributeDef(AttributeDef topicAttributeDef1) {
      this.topicAttributeDef = topicAttributeDef1;
    }

    
    /**
     * map of queue extension to attribute def name
     * @return the extensionOfQueueToAttributeDefName
     */
    public Map<String, AttributeDefName> getExtensionOfQueueToAttributeDefName() {
      return this.extensionOfQueueToAttributeDefName;
    }

    
    /**
     * map of queue extension to attribute def name
     * @param extensionOfQueueToAttributeDefName1 the extensionOfQueueToAttributeDefName to set
     */
    public void setExtensionOfQueueToAttributeDefName(
        Map<String, AttributeDefName> extensionOfQueueToAttributeDefName1) {
      this.extensionOfQueueToAttributeDefName = extensionOfQueueToAttributeDefName1;
    }

    
    /**
     * map of topic extension to attribute def name
     * @return the extensionOfTopicToAttributeDefName
     */
    public Map<String, AttributeDefName> getExtensionOfTopicToAttributeDefName() {
      return this.extensionOfTopicToAttributeDefName;
    }

    
    /**
     * map of topic extension to attribute def name
     * @param extensionOfTopicToAttributeDefName1 the extensionOfTopicToAttributeDefName to set
     */
    public void setExtensionOfTopicToAttributeDefName(
        Map<String, AttributeDefName> extensionOfTopicToAttributeDefName1) {
      this.extensionOfTopicToAttributeDefName = extensionOfTopicToAttributeDefName1;
    }
    
    
    
  }
  
  /**
   * message root stem
   * @return the message root stem
   */
  public static String messageRootStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":messages";
  }

  /**
   * grouper message topic name of attribute def
   * @return the name
   */
  public static String grouperMessageTopicNameOfDef() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageTopicDef";
  }

  /**
   * grouper message name of role
   * @return the name
   */
  public static String grouperMessageNameOfRole() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageRole";
  }

  /**
   * grouper message queue name of attribute def
   * @return the name
   */
  public static String grouperMessageQueueNameOfDef() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageQueueDef";
  }

  /**
   * topic stem name
   * @return topic stem name GrouperBuiltinMessagingSystem.
   */
  public static String topicStemName() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageTopics";
  }
  
  /**
   * queue stem name GrouperBuiltinMessagingSystem.
   * @return queue stem name
   */
  public static String queueStemName() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageQueues";
  }

  /**
   * name of built in messaging system
   */
  public final static String BUILTIN_NAME = "grouperBuiltinMessaging";
  
  /**
   * 
   * @param topicName
   * @return if created or already existed
   */
  public static boolean createTopic(String topicName) {

    if (StringUtils.isBlank(topicName)) {
      throw new NullPointerException("topicName cant be blank");
    }

    if (topicName.contains(":")) {
      throw new RuntimeException("topicName cant contain colon: " + topicName);
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    
    Stem topicStem = StemFinder.findByName(grouperSession, topicStemName(), true, new QueryOptions().secondLevelCache(false));

    AttributeDefName topicAttributeDefName = AttributeDefNameFinder.findByName(topicStem.getName() + ":" + topicName, false);

    if (topicAttributeDefName == null) {

      AttributeDef topicAttributeDef = new AttributeDefFinder().assignFindByUuidOrName(false).assignScope(grouperMessageTopicNameOfDef()).findAttribute();
      
      new AttributeDefNameSave(grouperSession, topicAttributeDef).assignName(topicStem.getName() + ":" + topicName).save();

      clearCache();
      
      return true;
    }
    
    return false;
    
  }

  /**
   * 
   * @param topicName
   * @return if deleted or didnt exist
   */
  public static boolean deleteTopic(String topicName) {

    if (StringUtils.isBlank(topicName)) {
      throw new NullPointerException("topicName cant be blank");
    }

    if (topicName.contains(":")) {
      throw new RuntimeException("topicName cant contain colon: " + topicName);
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    
    Stem topicStem = StemFinder.findByName(grouperSession, topicStemName(), true, new QueryOptions().secondLevelCache(false));

    AttributeDefName topicAttributeDefName = AttributeDefNameFinder.findByName(topicStem.getName() + ":" + topicName, false);

    if (topicAttributeDefName != null) {

      topicAttributeDefName.delete();
      
      clearCache();
      
      return true;
    }
    
    return false;
    
  }

  /**
   * 
   * @param queueName
   * @return if deleted or didnt exist
   */
  public static boolean deleteQueue(String queueName) {
    
    if (StringUtils.isBlank(queueName)) {
      throw new NullPointerException("queueName cant be blank");
    }

    if (queueName.contains(":")) {
      throw new RuntimeException("queueName cant contain colon: " + queueName);
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    
    Stem queueStem = StemFinder.findByName(grouperSession, queueStemName(), true, new QueryOptions().secondLevelCache(false));

    AttributeDefName queueAttributeDefName = AttributeDefNameFinder.findByName(queueStem.getName() + ":" + queueName, false);
    
    if (queueAttributeDefName != null) {
      queueAttributeDefName.delete();
      clearCache();
      return true;
    }
    
    return false;
    
  }

  /**
   * 
   * @param queueName
   * @return if created or already created
   */
  public static boolean createQueue(String queueName) {
    
    if (StringUtils.isBlank(queueName)) {
      throw new NullPointerException("queueName cant be blank");
    }

    if (queueName.contains(":")) {
      throw new RuntimeException("queueName cant contain colon: " + queueName);
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    
    Stem queueStem = StemFinder.findByName(grouperSession, queueStemName(), true, new QueryOptions().secondLevelCache(false));

    AttributeDefName queueAttributeDefName = AttributeDefNameFinder.findByName(queueStem.getName() + ":" + queueName, false);

    if (queueAttributeDefName == null) {
      AttributeDef queueAttributeDef = new AttributeDefFinder().assignFindByUuidOrName(false).assignScope(grouperMessageQueueNameOfDef()).findAttribute();
      
      new AttributeDefNameSave(grouperSession, queueAttributeDef).assignName(queueStem.getName() + ":" + queueName).save();
  
      clearCache();
      
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param topicName
   * @param queueName
   * @return true if a change occurred
   */
  public static boolean topicAddSendToQueue(String topicName, String queueName) {
    
    GrouperBuiltinMessagingCache grouperBuiltinMessagingCache = retrieveCache();
    
    AttributeDefName topicAttributeDefName = grouperBuiltinMessagingCache.getExtensionOfTopicToAttributeDefName().get(topicName);

    if (topicAttributeDefName == null) {
      throw new RuntimeException("Cant find topic: '" + topicName + "'");
    }
    
    AttributeDefName queueAttributeDefName = grouperBuiltinMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);

    if (queueAttributeDefName == null) {
      throw new RuntimeException("Cant find queue: '" + queueName + "'");
    }

    boolean changed = topicAttributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(queueAttributeDefName);
    
    if (changed) {
      clearCache();
    }
    
    return changed;
  }

  /**
   * 
   * @param topicName
   * @param queueName
   * @return true if a change occurred
   */
  public static boolean topicRemoveSendToQueue(String topicName, String queueName) {
    
    GrouperBuiltinMessagingCache grouperBuiltinMessagingCache = retrieveCache();
    
    AttributeDefName topicAttributeDefName = grouperBuiltinMessagingCache.getExtensionOfTopicToAttributeDefName().get(topicName);

    if (topicAttributeDefName == null) {
      throw new RuntimeException("Cant find topic: '" + topicName + "'");
    }
    
    AttributeDefName queueAttributeDefName = grouperBuiltinMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);

    if (queueAttributeDefName == null) {
      throw new RuntimeException("Cant find queue: '" + queueName + "'");
    }

    boolean changed = topicAttributeDefName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(queueAttributeDefName);
    
    if (changed) {
      clearCache();
    }
    
    return changed;
  }

  /**
   * 
   * @param topicName
   * @param subject
   * @return true if assigned or false if already existed
   */
  public static boolean allowSendToTopic(String topicName, Subject subject) {

    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    AttributeDefName topicAttributeDefName = grouperMessagingCache.getExtensionOfTopicToAttributeDefName().get(topicName);
    
    if (topicAttributeDefName == null) {
      throw new RuntimeException("topic doesnt exist '" + topicName + "'");
    }

    Role messagingRole = grouperMessagingCache.getMessagingRole();

    if (!messagingRole.hasMember(subject)) {
      messagingRole.addMember(subject, false);
    }
    
    AttributeAssignResult attributeAssignResult = messagingRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        actionSendToTopic, topicAttributeDefName, subject, PermissionAllowed.ALLOWED);

    if (attributeAssignResult.isChanged()) {
      clearCache();
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param topicName
   * @param subject
   * @return true if assigned or false if already existed
   */
  public static boolean disallowSendToTopic(String topicName, Subject subject) {

    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    AttributeDefName topicAttributeDefName = grouperMessagingCache.getExtensionOfTopicToAttributeDefName().get(topicName);
    
    if (topicAttributeDefName == null) {
      throw new RuntimeException("topic doesnt exist '" + topicName + "'");
    }

    Role messagingRole = grouperMessagingCache.getMessagingRole();

    AttributeAssignResult attributeAssignResult = messagingRole.getPermissionRoleDelegate().removeSubjectRolePermission(
        actionSendToTopic, topicAttributeDefName, subject);

    if (attributeAssignResult.isChanged()) {
      clearCache();
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if changed, false if not
   */
  public static boolean allowSendToQueue(String queueName, Subject subject) {
    
    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    AttributeDefName queueAttributeDefName = grouperMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);
    
    if (queueAttributeDefName == null) {
      throw new RuntimeException("queue doesnt exist '" + queueName + "'");
    }

    Role messagingRole = grouperMessagingCache.getMessagingRole();

    if (!messagingRole.hasMember(subject)) {
      messagingRole.addMember(subject, false);
    }
    
    AttributeAssignResult attributeAssignResult = messagingRole.getPermissionRoleDelegate().assignSubjectRolePermission(actionSendToQueue, queueAttributeDefName, subject, PermissionAllowed.ALLOWED);

    if (attributeAssignResult.isChanged()) {
      clearCache();
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if changed, false if not
   */
  public static boolean disallowSendToQueue(String queueName, Subject subject) {
    
    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    AttributeDefName queueAttributeDefName = grouperMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);
    
    if (queueAttributeDefName == null) {
      throw new RuntimeException("queue doesnt exist '" + queueName + "'");
    }

    Role messagingRole = grouperMessagingCache.getMessagingRole();

    if (!messagingRole.hasMember(subject)) {
      messagingRole.addMember(subject, false);
    }
    
    AttributeAssignResult attributeAssignResult = messagingRole.getPermissionRoleDelegate().removeSubjectRolePermission(actionSendToQueue, queueAttributeDefName, subject);

    if (attributeAssignResult.isChanged()) {
      clearCache();
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if changed or false if not changed
   */
  public static boolean allowReceiveFromQueue(String queueName, Subject subject) {

    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    AttributeDefName queueAttributeDefName = grouperMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);
    
    if (queueAttributeDefName == null) {
      throw new RuntimeException("queue doesnt exist '" + queueName + "'");
    }

    Role messagingRole = grouperMessagingCache.getMessagingRole();

    if (!messagingRole.hasMember(subject)) {
      messagingRole.addMember(subject, false);
    }

    AttributeAssignResult attributeAssignResult = messagingRole.getPermissionRoleDelegate().assignSubjectRolePermission(actionReceive, queueAttributeDefName, subject, PermissionAllowed.ALLOWED);

    if (attributeAssignResult.isChanged()) {
      clearCache();
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if changed or false if not changed
   */
  public static boolean disallowReceiveFromQueue(String queueName, Subject subject) {

    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    AttributeDefName queueAttributeDefName = grouperMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);
    
    if (queueAttributeDefName == null) {
      throw new RuntimeException("queue doesnt exist '" + queueName + "'");
    }

    Role messagingRole = grouperMessagingCache.getMessagingRole();

    if (!messagingRole.hasMember(subject)) {
      messagingRole.addMember(subject, false);
    }

    AttributeAssignResult attributeAssignResult = messagingRole.getPermissionRoleDelegate().removeSubjectRolePermission(actionReceive, queueAttributeDefName, subject);

    if (attributeAssignResult.isChanged()) {
      clearCache();
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if allowed to send to queue
   */
  public static boolean allowedToSendToQueue(String queueName, final Subject subject) {

    final GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    final AttributeDefName queueAttributeDefName = grouperMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);

    if (queueAttributeDefName == null) {
      throw new RuntimeException("queue doesnt exist '" + queueName + "'");
    }

    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }

    //hashmap of MultiKey[source id, subject id, topic/queue extension name, action], boolean if allow or not, lazy loaded
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId(),
        queueName, actionSendToQueue);
    Boolean allowed = grouperMessagingCache.getMessagingPermissions().get(multiKey);

    return allowed != null && allowed;
  }    
    
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if allowed to send to queue
   */
  public static boolean allowedToReceiveFromQueue(String queueName, final Subject subject) {
    final GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    final AttributeDefName queueAttributeDefName = grouperMessagingCache.getExtensionOfQueueToAttributeDefName().get(queueName);

    if (queueAttributeDefName == null) {
      throw new RuntimeException("queue doesnt exist '" + queueName + "'");
    }

    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    //hashmap of MultiKey[source id, subject id, topic/queue extension name, action], boolean if allow or not, lazy loaded
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId(),
        queueName, actionReceive);
    Boolean allowed = grouperMessagingCache.getMessagingPermissions().get(multiKey);

    return allowed != null && allowed;
  }

  /**
   * 
   * @param topicName
   * @param subject
   * @return true if allowed to send to topic
   */
  public static boolean allowedToSendToTopic(String topicName, final Subject subject) {
    final GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    final AttributeDefName topicAttributeDefName = grouperMessagingCache.getExtensionOfTopicToAttributeDefName().get(topicName);

    if (topicAttributeDefName == null) {
      throw new RuntimeException("topic doesnt exist '" + topicName + "'");
    }

    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }

    //hashmap of MultiKey[source id, subject id, topic/queue extension name, action], boolean if allow or not, lazy loaded
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId(),
        topicName, actionSendToTopic);
    Boolean allowed = grouperMessagingCache.getMessagingPermissions().get(multiKey);

    return allowed != null && allowed;
  }

  /**
   * 
   * @param topicName
   * @return the queue names that the topic sends to
   */
  public static Collection<String> queuesTopicSendsTo(String topicName) {
    
    GrouperBuiltinMessagingCache grouperBuiltinMessagingCache = retrieveCache();
    
    Set<String> queues = grouperBuiltinMessagingCache.topicExtSendsToQueueExts(topicName);
    
    return queues;
    
  }
  
  /** last millis number */
  private static long lastMillis = -1;
  
  /** nanos when the last millis was taken (since nanos are diffs) */
  private static long millisNanos = -1;
  
  /** last id generated */
  private static long lastResult = -1;
  /**
   * get a change log id
   * @return a change log id value
   */
  public static long messageSentTimeMicros() {
    long currentMillis = System.currentTimeMillis();
    long currentNanos = System.nanoTime();
    int currentThousandthsMicros = 0;
    long result = -1L;
    synchronized (GrouperBuiltinMessagingSystem.class) {
      //see if a milli has gone by since the last check
      if (currentMillis > lastMillis) {
        lastMillis = currentMillis;
        millisNanos = currentNanos;
      } else {
        
        //if less, then must have incremented
        currentMillis = lastMillis;
        
        //see if the micros are more.  if the number is 123456789, we want to get the 123456 number
        //note, this might add millis too, thats ok
        currentThousandthsMicros = (int)((currentNanos - millisNanos) / 1000);
      }
      
      //calculate and return
      result = (currentMillis * 1000) + currentThousandthsMicros;
      
      //make sure greater
      if (result <= lastResult) {
        result = lastResult + 1;
      }
      lastResult = result;
      
    }
    return result;
    
  }

  /**
   * 
   */
  public GrouperBuiltinMessagingSystem() {
  }

  /**
   * state of a message
   */
  public static enum GrouperBuiltinMessageState {
    
    /** if in queue waiting to be retrieved */
    IN_QUEUE, 
    
    /** if it is delivered but not confirmed */
    GET_ATTEMPTED, 
    
    /** if it is processed and ready to be deleted */
    PROCESSED;

    /**
     * convert a string to a message state
     * @param input
     * @param exceptionIfNotFound
     * @return the state or null
     */
    public static GrouperBuiltinMessageState valueOfIgnoreCase(String input, boolean exceptionIfNotFound) {
      return GrouperUtil.enumValueOfIgnoreCase(GrouperBuiltinMessageState.class, input, exceptionIfNotFound, true);
    }
    
  }
  
  /**
   * clean old unprocessed messages
   * @return the number of messages affected
   */
  public static int cleanOldUnprocessedMessages() {

    int deleteAllRecordsAfterHours = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.deleteAllMessagesMoreThanHoursOld", 72);

    if (deleteAllRecordsAfterHours == -1) {
      return -1;
    }
    
    //lets get a date
    Calendar calendar = GregorianCalendar.getInstance();

    //get however many days in the past
    calendar.add(Calendar.HOUR_OF_DAY, -1 * deleteAllRecordsAfterHours);
    
    long micros = calendar.getTimeInMillis() * 1000;

    int records = HibernateSession.bySqlStatic().executeSql("delete from grouper_message where sent_time_micros < ?", 
        GrouperUtil.toListObject(micros), HibUtils.listType(LongType.INSTANCE));

    return records;
  }
  
  /**
   * clean old unprocessed messages
   * @return the number of messages affected
   */
  public static int cleanOldProcessedMessages() {
    
    int deleteProcessedRecordsAfterMinutes = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.deleteProcessedMessagesMoreThanMinutesOld", 180);

    if (deleteProcessedRecordsAfterMinutes <= 0) {
      //try to delete again just in case
      int records = HibernateSession.bySqlStatic().executeSql("delete from grouper_message where state = 'PROCESSED'");
      return records;
    }
    
    //lets get a date
    Calendar calendar = GregorianCalendar.getInstance();

    //get however many minutes in the past
    calendar.add(Calendar.MINUTE, -1 * deleteProcessedRecordsAfterMinutes);
    
    long micros = calendar.getTimeInMillis();

    int records = HibernateSession.bySqlStatic().executeSql("delete from grouper_message where get_time_millis < ? and state = 'PROCESSED'", 
        GrouperUtil.toListObject(micros), HibUtils.listType(LongType.INSTANCE));

    return records;
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    Member fromMember = grouperSession.getMember();

    String queueOrTopicName = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();

    boolean isQueue = grouperMessagingCache.queueExists(queueOrTopicName);
    boolean isTopic = grouperMessagingCache.topicExists(queueOrTopicName);

    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageSendParam.getGrouperMessageSystemParam();

    if (!isQueue && !isTopic) {
      if (grouperMessageSystemParam != null && grouperMessageSystemParam.isAutocreateObjects()) {
        switch(grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType()) {
          case queue:
            createQueue(queueOrTopicName);
            break;
            
          case topic: 
            createTopic(queueOrTopicName);
            break;
            
          default:
            throw new RuntimeException("Not expecting queueType: " + grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType());  
        }
        grouperMessagingCache = retrieveCache();
        isQueue = grouperMessagingCache.queueExists(queueOrTopicName);
        isTopic = grouperMessagingCache.topicExists(queueOrTopicName);
      } else {
      
        throw new RuntimeException("This is not a queue or topic: '" + queueOrTopicName + "'");
      }
    }

    if (grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.queue && !isQueue) {
      throw new RuntimeException("expecting queue but is not queue: " + queueOrTopicName);
    }

    if (grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.topic && !isTopic) {
      throw new RuntimeException("expecting topic but is not topic: " + queueOrTopicName);
    }

    Set<String> queues = null;
    if (isQueue) {
      if (!allowedToSendToQueue(queueOrTopicName, grouperSession.getSubject())) {
        
        if (grouperMessageSystemParam != null && grouperMessageSystemParam.isAutocreateObjects()) {

          allowSendToQueue(queueOrTopicName, grouperSession.getSubject());
          
        
        } else {
        
          throw new RuntimeException(grouperSession.getSubject() + " is not allowed to send to queue: '" + queueOrTopicName + "'");
        }
      }
      queues = GrouperUtil.toSet(queueOrTopicName);
    } else {
      if (!allowedToSendToTopic(queueOrTopicName, grouperSession.getSubject())) {
        
        if (grouperMessageSystemParam != null && grouperMessageSystemParam.isAutocreateObjects()) {

          allowSendToTopic(queueOrTopicName, grouperSession.getSubject());
        } else {         
          throw new RuntimeException(grouperSession.getSubject() + " is not allowed to send to topic: '" + queueOrTopicName + "'");
        }
      }
      queues = grouperMessagingCache.topicExtSendsToQueueExts(queueOrTopicName);
    }

    for (GrouperMessage grouperMessage : GrouperUtil.nonNull(grouperMessageSendParam.getGrouperMessages())) {
      for (String queue : queues) {
        GrouperMessageHibernate grouperMessageHibernate = new GrouperMessageHibernate();
        if (!StringUtils.isBlank(grouperMessage.getFromMemberId())) {
          throw new RuntimeException("fromMemberId must be null in a message");
        }
        if (!StringUtils.isBlank(grouperMessage.getId())) {
          throw new RuntimeException("id must be null in a message");
        }
        grouperMessageHibernate.setFromMemberId(fromMember.getId());
        grouperMessageHibernate.setGetAttemptCount(0);
        grouperMessageHibernate.setGetAttemptTimeMillis(-1L);
        grouperMessageHibernate.setId(GrouperUuid.getUuid());
        grouperMessageHibernate.setMessageBody(grouperMessage.getMessageBody());
        grouperMessageHibernate.setQueueName(queue);
        grouperMessageHibernate.setSentTimeMicros(messageSentTimeMicros());
        grouperMessageHibernate.setState(GrouperBuiltinMessageState.IN_QUEUE.name());
        grouperMessageHibernate.saveOrUpdate();
      }
    }
    return new GrouperMessageSendResult();
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {

    Integer pageSize = grouperMessageReceiveParam.getMaxMessagesToReceiveAtOnce();

    int defaultPageSize = GrouperConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.defaultPageSize", 5);
    int maxPageSize = GrouperConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.maxPageSize", 50);
    
    if (pageSize == null) {
      pageSize = defaultPageSize;
    }
    
    if (pageSize > maxPageSize) {
      pageSize = maxPageSize;
    }
    
    Integer longPollMillis = grouperMessageReceiveParam.getLongPollMilis();
    
    if (longPollMillis == null) {
      longPollMillis = -1;
    }
    
    long startReceive = System.currentTimeMillis();
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);

    String queueOrTopicName = grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueOrTopicName();

    //must be queue
    GrouperBuiltinMessagingCache grouperMessagingCache = retrieveCache();
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    if (!grouperMessagingCache.queueExists(queueOrTopicName)) {

      if (grouperMessageSystemParam != null && grouperMessageSystemParam.isAutocreateObjects()) {
        createQueue(queueOrTopicName);
        grouperMessagingCache = retrieveCache();
      } else {
      
        throw new RuntimeException("Queue doesnt exist '" + queueOrTopicName + "'");
      }

    }
    
    if (!allowedToReceiveFromQueue(queueOrTopicName, grouperSession.getSubject())) {
      if (grouperMessageSystemParam != null && grouperMessageSystemParam.isAutocreateObjects()) {
        allowReceiveFromQueue(queueOrTopicName, grouperSession.getSubject());
      } else {
        throw new RuntimeException(grouperSession.getSubject() + " is not allowed to receive from queue: '" + queueOrTopicName + "'");
      }
    }

    int pollSleepSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.polling.sleep.seconds", 5);
    if (pollSleepSeconds < 1) {
      pollSleepSeconds = 1;
    }
    GrouperMessageReceiveResult grouperMessageReceiveResult = new GrouperMessageReceiveResult();

    List<GrouperMessage> messages = new ArrayList<GrouperMessage>();

    grouperMessageReceiveResult.setGrouperMessages(messages);

    Long attemptTimeExpired = System.currentTimeMillis() + (1000 * (long)GrouperConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.timeout.receive.seconds", 300));

    int timeToLive = 0;
    while (true) {
      
      if (timeToLive++ > 100) {
        break;
      }
      List<GrouperMessageHibernate> grouperMessageHibernates = GrouperDAOFactory.getFactory().getMessage().findByQueue(queueOrTopicName, pageSize);
      
      
      if (GrouperUtil.length(grouperMessageHibernates) > 0) {
        
        for (GrouperMessageHibernate grouperMessageHibernate : grouperMessageHibernates) {

          grouperMessageHibernate.setAttemptTimeExpiresMillis(attemptTimeExpired);
          if (grouperMessageHibernate.getGetAttemptCount() == null) {
            grouperMessageHibernate.setGetAttemptCount(1);
          } else {
            grouperMessageHibernate.setGetAttemptCount(grouperMessageHibernate.getGetAttemptCount() + 1);
          }
          grouperMessageHibernate.setGetAttemptTimeMillis(System.currentTimeMillis());
          grouperMessageHibernate.setState(GrouperBuiltinMessageState.GET_ATTEMPTED.name());
          messages.add(grouperMessageHibernate);
        }

        try {
          // its more efficient if this works
          HibernateSession.byObjectStatic().updateBatch(grouperMessageHibernates);
        } catch (Exception e) {
        
          // if there is a problem do each individually
          for (GrouperMessageHibernate grouperMessageHibernate : grouperMessageHibernates) {
  
            try {
              grouperMessageHibernate.saveOrUpdate();
            } catch (GrouperStaleStateException gsse) {
              //if this happens, it might be because it was received in another thread... just go to next one
              LOG.debug("Stale object state on message is not a problem: " + grouperMessageHibernate.getId() + ", " + grouperMessageHibernate.getQueueName(), gsse);
              continue;
            } catch (GrouperStaleObjectStateException gsose) {
              //if this happens, it might be because it was received in another thread... just go to next one
              LOG.debug("Stale object state on message is not a problem: " + grouperMessageHibernate.getId() + ", " + grouperMessageHibernate.getQueueName(), gsose);
              continue;
            }
          }
        }        
        break;
      }
      //dont long poll
      if (longPollMillis < 0) {
        break;
      }
      if (longPollMillis < System.currentTimeMillis() - startReceive ) {
        break;
      }
      GrouperUtil.sleep(Math.min(pollSleepSeconds*1000, System.currentTimeMillis() + 20 - startReceive));
    }
    return grouperMessageReceiveResult;
  }


  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(
      final GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    final GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);

    // TODO batch update or the messages...
    
    String queue = grouperMessageAcknowledgeParam.getGrouperMessageQueueParam().getQueueOrTopicName();

    //must be queue
    if (!retrieveCache().queueExists(queue)) {
      throw new RuntimeException("Queue doesnt exist '" + queue + "'");
    }
    
    boolean deleteOnProcessed = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.builtin.messaging.deleteProcessedMessagesMoreThanMinutesOld", 180) <= 0;
    
    if (!allowedToReceiveFromQueue(queue, grouperSession.getSubject())) {
      throw new RuntimeException(grouperSession.getSubject() + " is not allowed to receive (or acknowledge) from queue: '" + queue + "'");
    }
    
    for (GrouperMessage grouperMessage : GrouperUtil.nonNull(grouperMessageAcknowledgeParam.getGrouperMessages())) {

      if (StringUtils.isBlank(grouperMessage.getId())) {
        throw new RuntimeException("id cant be null in a message");
      }
      GrouperMessageHibernate grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findById(grouperMessage.getId(), false);

      //if not there, i guess thats ok
      if (grouperMessageHibernate != null) {
        
        if (!StringUtils.equals(queue, grouperMessageHibernate.getQueueName())) {
          throw new RuntimeException("Message to mark as processed: " + grouperMessageHibernate.getId() + ", expected queue: '" 
              + queue + "', doesnt equal actual queue: '" + grouperMessageHibernate.getQueueName() + "'");
        }
        switch(grouperMessageAcknowledgeParam.getAcknowledgeType()) {

          case mark_as_processed:
            
            if (deleteOnProcessed) {
              grouperMessageHibernate.delete();
            } else if (StringUtils.equals(GrouperBuiltinMessageState.PROCESSED.name(), grouperMessageHibernate.getState())) {
              LOG.warn("Grouper message already had state: " + grouperMessageHibernate.getState());
            } else {
              grouperMessageHibernate.setState(GrouperBuiltinMessageState.PROCESSED.name());
              grouperMessageHibernate.setGetTimeMillis(System.currentTimeMillis());
              grouperMessageHibernate.saveOrUpdate();
            }

            break;
            
          case return_to_queue:
            
            //if it is get attempted, set it back
            if (StringUtils.equals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState())) {
              grouperMessageHibernate.setState(GrouperBuiltinMessageState.IN_QUEUE.name());
              grouperMessageHibernate.saveOrUpdate();
            } else {
              LOG.warn("Grouper message already had state: " + grouperMessageHibernate.getState());
            }
            break;
            
          case return_to_end_of_queue:
            
            //if it is get attempted, set it back
            if (StringUtils.equals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState())) {
              grouperMessageHibernate.setState(GrouperBuiltinMessageState.IN_QUEUE.name());
              grouperMessageHibernate.setSentTimeMicros(messageSentTimeMicros());
              grouperMessageHibernate.saveOrUpdate();
            } else {
              LOG.warn("Grouper message already had state: " + grouperMessageHibernate.getState());
            }
            break;
            
          case send_to_another_queue:

            //if it is get attempted, set it back
            if (StringUtils.equals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState())) {

              LOG.warn("Sending message to another queue: " + grouperMessage.getId());

              final GrouperMessageHibernate GROUPER_MESSAGE_HIBERNATE = grouperMessageHibernate;
              
              //this could be going to a topic, so send, and delete this one
              HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                
                public Object callback(HibernateHandlerBean hibernateHandlerBean)
                    throws GrouperDAOException {
                  
                  GrouperMessageDefault grouperMessageDefault = new GrouperMessageDefault();
                  grouperMessageDefault.setMessageBody(GROUPER_MESSAGE_HIBERNATE.getMessageBody());
                  GrouperBuiltinMessagingSystem.this.send(new GrouperMessageSendParam().assignGrouperMessageQueueParam(
                      grouperMessageAcknowledgeParam.getGrouperMessageAnotherQueueParam())
                      .assignGrouperMessageSystemParam(grouperMessageAcknowledgeParam.getGrouperMessageSystemParam())
                      .addGrouperMessage(grouperMessageDefault));
                  
                  GROUPER_MESSAGE_HIBERNATE.delete();
                  
                  return null;
                }
              });
              
            } else {
              LOG.warn("Grouper message already had state: " + grouperMessageHibernate.getState());
            }
            break;
        }
      // if (grouperMessageHibernate == null) 
      } else { 
        LOG.warn("Grouper message doesnt exist, cant mark as processed: " + grouperMessage.getId());
      }
    }
    return new GrouperMessageAcknowledgeResult();
  }

}
