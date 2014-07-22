/**
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
 */
/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message.Type;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.util.JsonUtils;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob.XmppJobEventAction;

/**
* run an xmpp job to manage memberships in groups
*/
public class GrouperClientXmppMain {

  /**
  * logger
  */
  private static Log log = GrouperClientUtils.retrieveLog(GrouperClientXmppMain.class);

  /** supported event types */
  private static final Set<String> SUPPORTED_EVENT_TYPES = Collections.unmodifiableSet(GrouperClientUtils.toSet(
      "MEMBERSHIP_ADD",
      "MEMBERSHIP_DELETE",
      "GROUP_ADD",
      "GROUP_DELETE",
      "GROUP_UPDATE"
      ));

  /**
  * see if the esb event matches an EL filter. Note the available objects are
  * event for the EsbEvent, and grouperUtil for the GrouperUtil class which has
  * a lot of utility methods
  * @param filterString
  * @param esbEvent
  * @return true if matches, false if doesnt
  */
  public static boolean matchesFilter(EsbEvent esbEvent, String filterString) {
    try {
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("event", esbEvent);
      variableMap.put("grouperClientUtils", new GrouperClientUtils());
      
      String elResultString = GrouperClientUtils.substituteExpressionLanguage(filterString, variableMap, true, false, false, true);
      return GrouperClientUtils.booleanValue(elResultString);
      
    } catch (Exception e) {
      throw new RuntimeException("Problem seeing if matches filter for sequence: "
          + (esbEvent == null ? null : esbEvent.getSequenceNumber()) + ", '" + filterString + "'", e);
    }
  }

  /** keep a reference here so we dont have to keep logging in */
  private static XMPPConnection xmppConnection = null;

  /**
  * get an xmpp connection
  * @return xmpp connection
  */
  private static synchronized XMPPConnection xmppConnection() {
    if (xmppConnection == null || !xmppConnection.isAuthenticated() || !xmppConnection.isConnected()) {
      String user = null;
      String pass = null;
      String resource = null;
      String server = null;
      int port = -1;
      try {
        if (xmppConnection != null) {
          try {
            xmppConnection.disconnect();
          } catch (Exception e) {
            //this is ok
          }
        }
        server = xmppServer();
        port = xmppPort();
        ConnectionConfiguration config = new ConnectionConfiguration(server, port);

        boolean debuggerEnabled = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.xmpp.debuggerEnabled", false);

        config.setDebuggerEnabled(debuggerEnabled);
        config.setReconnectionAllowed(true);

        config.setSASLAuthenticationEnabled(true);
        SASLAuthentication.supportSASLMechanism("PLAIN");
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        xmppConnection = new XMPPConnection(config);
        xmppConnection.connect();

        user = xmppUser();
        pass = xmppPass();
        resource = xmppResource();
        xmppConnection.login(user, pass, resource);
      } catch (XMPPException xe) {
        throw new RuntimeException("Problem connecting: server: " + server + ", port: "
            + port + ", user: " + user + ", pass not included, "
            //+ GrouperClientUtils.repeat("*", GrouperClientUtils.defaultString(pass).length())
            + ", resource: " + resource, xe);
      }
    }
    return xmppConnection;
  }

  /**
  * xmpp pass (decrypted if file)
  * @return the pass
  */
  public static String xmppPass() {

    boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");

    //lets lookup if file
    String pass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.xmpp.pass");
    String passFromFile = GrouperClientUtils.readFromFileIfFile(pass, disableExternalFileLookup);
    
    if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("encrypt.encryptLikeServer", false)) {
      if (!GrouperClientUtils.equals(pass, passFromFile)) {
  
        String encryptKey = GrouperClientUtils.encryptKey();
        pass = new Crypto(encryptKey).decrypt(passFromFile);
        
      }

      return pass;
    }
    if (!GrouperClientUtils.equals(pass, passFromFile)) {

      String encryptKey = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("encrypt.key");
      encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
      passFromFile = new Crypto(encryptKey).decrypt(passFromFile);

    }

    return passFromFile;
  }

  /**
  * port to connect to, or 1522 as default
  * @return port
  */
  public static int xmppPort() {
    return GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.xmpp.server.port", 1522);
  }

  /**
  * xmpp resource
  * @return the resource
  */
  public static String xmppResource() {
    return GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.xmpp.resource");
  }

  /**
  * xpp server to connect to
  * @return xmpp server
  */
  public static String xmppServer() {
    return GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.xmpp.server.host");
  }

  /**
  * xmpp user
  * @return the user
  */
  public static String xmppUser() {
    return GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.xmpp.user");
  }

  /**
  * connect to xmpp
   * @param grouperClientXmppMessageHandler the handler for the message
   */
   public static void xmppConnect(final GrouperClientXmppMessageHandler grouperClientXmppMessageHandler) {
     
     XMPPConnection theXmppConnection = xmppConnection();
     theXmppConnection.addPacketListener(new PacketListener() {

       // @Override
       public void processPacket(Packet packet) {
         Message message = null;
         try {
           message = (Message) packet;
           if (log.isDebugEnabled()) {
             log.debug(message == null ? null : message.toXML());
           }
           grouperClientXmppMessageHandler.handleMessage(message);

         } catch (Throwable re) {
           String messageXml = message == null ? null : message.toXML();
           log.error("Problem with message: " + messageXml, re);
           throw new RuntimeException(re);
         }

       }
     }, new PacketFilter() {

       // @Override
       public boolean accept(Packet packet) {
         if (packet instanceof Message) {
           Message message = (Message) packet;
           Type type = message.getType();
           if (type == Type.chat && !GrouperClientUtils.isBlank(message.getBody())) {
             if (allowFromJabberIds().contains(message.getFrom())) {
               return true;
             }
             if (log.isDebugEnabled()) {
               log.debug("Not expecting message from: " + message.getFrom());
             }
           }
         }
         return false;
       }
     });
   }

  /**
  * connect to xmpp
  */
  private static void xmppLoopForGroups() {
    xmppLoop(new GrouperClientXmppMessageHandler() {
      
      @Override
      public void handleMessage(Message message) {
        
        String body = message.getBody();
        EsbEvents esbEvents = (EsbEvents)JsonUtils.jsonConvertFrom(body, EsbEvents.class);
        for (EsbEvent esbEvent : GrouperClientUtils.nonNull(esbEvents.getEsbEvent(), EsbEvent.class)) {
          //loop through jobs and see what matches
          for (GrouperClientXmppJob grouperClientXmppJob : GrouperClientXmppJob.retrieveXmppJobs()) {
            String elfilter = grouperClientXmppJob.getElfilter();
            Boolean matches = null;

            String groupName = esbEvent.getGroupName();
            if (GrouperClientUtils.isBlank(groupName) && (
                "GROUP_ADD".equals(esbEvent.getEventType()) || 
                "GROUP_DELETE".equals(esbEvent.getEventType()) ||
                "GROUP_UPDATE".equals(esbEvent.getEventType()))) {
              groupName = esbEvent.getName();
            }

            if (!GrouperClientUtils.isBlank(elfilter)) {
              if (!matchesFilter(esbEvent, elfilter)) {

                matches = false;

                if (log.isDebugEnabled()) {
                  log.debug("skipping event to not match filter, sequence: " + (esbEvent == null ? null : esbEvent.getSequenceNumber()) + ", '" + elfilter + "', " + grouperClientXmppJob.getJobName());
                }
              } else {
                matches = true;
              }

            }
            if (XmppJobEventAction.incremental == grouperClientXmppJob.getEventAction()
                && grouperClientXmppJob.isAllowIncrementalNotInGroupNamesList()) {
              if (log.isDebugEnabled()) {
                log.debug("including since incremental and allowIncrementalNotInGroupNamesList is true: " + grouperClientXmppJob.getJobName());
              }
              matches = true;
            } else {
              if (GrouperClientUtils.nonNull(grouperClientXmppJob.getGroupNames()).size() > 0) {
                if (grouperClientXmppJob.getGroupNames().contains(groupName)) {
                  if (matches == null) {
                    matches = true;
                  }
                } else {
                  if (log.isDebugEnabled()) {
                    log.debug("skipping event to not match group name list, sequence: " + (esbEvent == null ? null : esbEvent.getSequenceNumber()) + ", " + grouperClientXmppJob.getJobName());
                  }
                  matches = false;
                }
              }
            }
            if (matches != null && !matches) {
              continue;
            }

            //see what type
            if (XmppJobEventAction.reload_group == grouperClientXmppJob.getEventAction()) {

              if (log.isDebugEnabled()) {
                log.debug("performing a full reload on group: " + groupName
                    + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + esbEvent.getSubjectId());
              }
              fullRefreshGroup(grouperClientXmppJob, groupName);

            } else if (XmppJobEventAction.incremental == grouperClientXmppJob.getEventAction()) {

              if (log.isDebugEnabled()) {
                log.debug("performing an incremental reload on group: " + groupName
                    + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + esbEvent.getSubjectId());
              }

              GrouperClientXmppSubject grouperClientXmppSubject = new GrouperClientXmppSubject(esbEvent);

              incrementalRefreshGroup(grouperClientXmppJob, groupName,
                  grouperClientXmppSubject, esbEvent.getEventType());

            } else {
              throw new RuntimeException("Not expecting event action: " + grouperClientXmppJob.getEventAction());
            }

          }
        }
      }
    });
    }

  // /**
  // *
  // * @return xstream
  // */
  // private static XStream xstream() {
  // if (xStream == null) {
  // xStream = new XStream(new XppDriver());
  //
  // //do javabean properties, not fields
  // xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {
  //
  // /**
  // * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
  // */
  // @SuppressWarnings("unchecked")
  // @Override
  // public boolean canConvert(Class type) {
  // //see if one of our beans
  // return type.getName().startsWith("edu.internet2");
  // }
  //
  // });
  //
  // xStream.alias("XmppMembershipChange", XmppMembershipChange.class);
  // xStream.alias("XmppSubject", XmppSubject.class);
  //
  // }
  // return xStream;
  // }

  /** allowed from jabber ids */
  private static Set<String> allowFromJabberIds = null;

  
  /**
   * @return the allowFromJabberIds
   */
  public static Set<String> allowFromJabberIds() {
    if (allowFromJabberIds == null) {
      Set<String> temp = new HashSet<String>();
      
      String allowFroms = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.xmpp.trustedMessagesFromJabberIds");
      temp.addAll(GrouperClientUtils.splitTrimToList(allowFroms, ","));
      
      allowFromJabberIds = temp;
    }
    return allowFromJabberIds;
  }

  /**
  * @param args
  */
  public static void main(String[] args) {

    fullRefreshAll();

    scheduleFullRefreshJobs();
    //note this doesnt return
    xmppLoopForGroups();

  }

  /** mape of group name to list of subjects in group */
  private static Map<String, List<GrouperClientXmppSubject>> groupMemberships = new HashMap<String, List<GrouperClientXmppSubject>>();

  /**
  *
  * @param grouperClientXmppJob
  * @param groupName
  */
  @SuppressWarnings("unchecked")
  public static void fullRefreshGroup(GrouperClientXmppJob grouperClientXmppJob, String groupName) {
    GcGetMembers gcGetMembers = new GcGetMembers();

    List<String> subjectAttributeNames = grouperClientXmppJob.getSubjectAttributeNames();

    if (GrouperClientUtils.length(subjectAttributeNames) > 0) {
      for (String subjectAttributeName : subjectAttributeNames) {
        gcGetMembers.addSubjectAttributeName(subjectAttributeName);
      }
    }

    WsGetMembersResults wsGetMembersResults = gcGetMembers.addGroupName(groupName).execute();
    WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];

    List<GrouperClientXmppSubject> xmppSubjects = new ArrayList<GrouperClientXmppSubject>();

    for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
      GrouperClientXmppSubject xmppSubject = new GrouperClientXmppSubject(wsSubject, wsGetMembersResults.getSubjectAttributeNames());

      xmppSubjects.add(xmppSubject);
    }

    if (log.isDebugEnabled()) {
      log.debug("Refreshing all for " + groupName + " found " + GrouperClientUtils.length(xmppSubjects) + " subjects");
    }

    Class<GrouperClientXmppHandler> handlerClass = GrouperClientUtils.forName(grouperClientXmppJob.getHandlerClass());
    GrouperClientXmppHandler grouperClientXmppHandler = GrouperClientUtils.newInstance(handlerClass);
    groupMemberships.put(groupName, xmppSubjects);
    grouperClientXmppHandler.handleAll(grouperClientXmppJob, groupName, GrouperClientUtils.extensionFromName(groupName), xmppSubjects);

  }

  /**
  * full refresh all groups
  */
  private static void fullRefreshAll() {
    //loop through jobs
    for (GrouperClientXmppJob grouperClientXmppJob : GrouperClientXmppJob.retrieveXmppJobs()) {

      //get the groups
      for (String groupName : GrouperClientUtils.nonNull(grouperClientXmppJob.getGroupNames())) {
        fullRefreshGroup(grouperClientXmppJob, groupName);
      }
    }
  }

  /**
  *
  */
  @SuppressWarnings("unused")
  private static void pocEl() {
    String outputTemplate = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.xmpp.job.myJobName.fileHandler.iteratorEl");
    outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
    GrouperClientXmppSubject xmppSubject = new GrouperClientXmppSubject();
    xmppSubject.getAttribute().put("loginid", "mchyzer");
    substituteMap.put("subject", xmppSubject);
    String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
    System.out.println(output);

  }

  /**
   * note, this doesnt return, and you should only call this once...
   * @param grouperClientXmppMessageHandler handle the message
   */
   public static void xmppLoop(GrouperClientXmppMessageHandler grouperClientXmppMessageHandler) {
     //are we already in this loop?
     if (xmppConnection != null) {
       return;
     }
     while (true) {
       try {
         if (xmppConnection == null || !xmppConnection.isConnected() || !xmppConnection.isAuthenticated()) {

           //if not starting, this is a problem
           if (xmppConnection != null) {
             log.error("xmpp connection is not connected");
             try {
               xmppConnection.disconnect();
             } catch (Exception e) {
               log.error("error", e);
             }
             xmppConnection = null;
           }
           xmppConnect(grouperClientXmppMessageHandler);
         }
       } catch (Exception e) {
         log.error("Problem with xmpp", e);
       }
       GrouperClientUtils.sleep(60000);
     }
   }

   /**
    * schedule a cron job
    * @param jobName something unique and descriptive
    * @param quartzCronString
    * @param jobClass
    */
   public static void scheduleJob(String jobName, String quartzCronString, Class<? extends Job> jobClass) {
     //no cron string, dont run the cron
     if (GrouperClientUtils.isBlank(quartzCronString)) {
       return;
     }
     
     String jobGroup = Scheduler.DEFAULT_GROUP;
     JobDetail jobDetail = new JobDetail(jobName,
         jobGroup, jobClass);

     Scheduler scheduler = GrouperClientXmppMain.scheduler();

     //atlassian requires durable jobs...
     jobDetail.setDurability(true);

     boolean uniqueTriggerNames = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.xmpp.uniqueQuartzTriggerNames", false);

     //in old versions of quartz, the trigger group cannot be null
     String triggerName = "trigger_" + jobName;

     if (uniqueTriggerNames) {
       triggerName += GrouperClientUtils.uniqueId();
     }

     CronTrigger cronTrigger = null;
     if (!uniqueTriggerNames) {
       try {
         cronTrigger = (CronTrigger) scheduler.getTrigger(triggerName, jobGroup);
       } catch (SchedulerException se) {
         throw new RuntimeException("Problem with trigger: " + jobName, se);
       }
     }
     if (cronTrigger == null) {
       cronTrigger = new CronTrigger(triggerName, jobGroup);
     }

     try {
       cronTrigger.setCronExpression(quartzCronString);
     } catch (ParseException pe) {
       throw new RuntimeException("Problems parsing: '" + quartzCronString + "'", pe);
     }

     try {
       scheduler.scheduleJob(jobDetail, cronTrigger);
     } catch (SchedulerException se) {
       throw new RuntimeException("Problem with job: " + jobName, se);
     }


   }


  /**
  *
  */
  private static void scheduleFullRefreshJobs() {

    //loop through jobs
    for (GrouperClientXmppJob grouperClientXmppJob : GrouperClientXmppJob.retrieveXmppJobs()) {

      String jobName = "fullRefresh_" + grouperClientXmppJob.getJobName();

      scheduleJob(jobName, grouperClientXmppJob.getFullRefreshQuartzCronString(), MembershipFullRefreshJob.class);
      
    }
  }

  /**
  * scheduler
  * @return scheduler
  */
  public static Scheduler scheduler() {
    try {
      return schedulerFactory().getScheduler();
    } catch (SchedulerException se) {
      throw new RuntimeException(se);
    }
  }

  /**
  * scheduler factory singleton
  */
  private static SchedulerFactory schedulerFactory = null;

  /**
  * lazy load (and start the scheduler) the scheduler factory
  * @return the scheduler factory
  */
  public static SchedulerFactory schedulerFactory() {
    if (schedulerFactory == null) {
      schedulerFactory = new StdSchedulerFactory();
      try {
        schedulerFactory.getScheduler().start();
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }

  /**
  * Mutate the membership list depending on the action.
  * Cache the current membership in a field of this class.
  * Pass the previous and current membership list off to the handler class for the {@link GrouperClientXmppJob}
  * @param grouperClientXmppJob
  * @param groupName
  * @param grouperClientXmppSubject
  * @param eventType
  */
  @SuppressWarnings("unchecked")
  private static void incrementalRefreshGroup(GrouperClientXmppJob grouperClientXmppJob,
      String groupName, GrouperClientXmppSubject grouperClientXmppSubject, String eventType) {

    if (!SUPPORTED_EVENT_TYPES.contains(eventType)) {
      throw new RuntimeException("Not expecting action: '" + eventType + "'");
    }

    List<GrouperClientXmppSubject> oldList = java.util.Collections.EMPTY_LIST;

    if (eventType.equals("MEMBERSHIP_ADD") || eventType.equals("MEMBERSHIP_DELETE")) {
      oldList = groupMemberships.get(groupName);
      if (oldList == null) {
        // Do a full refresh and then ask for the group membership again
        fullRefreshGroup(grouperClientXmppJob, groupName);
        oldList = groupMemberships.get(groupName);
      }
      if (oldList == null) {
        throw new NullPointerException("Why is old list null????");
      }
    }

    List<GrouperClientXmppSubject> newList = new ArrayList<GrouperClientXmppSubject>(oldList);

    if (GrouperClientUtils.equals(eventType, "MEMBERSHIP_ADD")) {

      boolean changed = newList.add(grouperClientXmppSubject);
      if (changed && log.isDebugEnabled()) {
        log.debug("Group " + groupName + " already contains subject: " + grouperClientXmppSubject.getSubjectId());
      }

    } else if (GrouperClientUtils.equals(eventType, "MEMBERSHIP_DELETE")) {

      Collection<GrouperClientXmppSubject> toRemove = GrouperClientUtils.toList(grouperClientXmppSubject);
      boolean changed = newList.removeAll(toRemove);
      if (!changed && log.isDebugEnabled()) {
        log.debug("Group " + groupName + " already doesnt contain subject: " + grouperClientXmppSubject.getSubjectId());
      }
    }

    groupMemberships.put(groupName, newList);

    if (log.isDebugEnabled()) {
      log.debug("Event: " + eventType + " for " + groupName + ", memberships list was "
          + GrouperClientUtils.length(oldList)
          + " and is now " + GrouperClientUtils.length(newList) + " subjects");
    }

    // Instantiate the handler for this job and call the handleIncremental method
    Class<GrouperClientXmppHandler> handlerClass = GrouperClientUtils
        .forName(grouperClientXmppJob.getHandlerClass());
    GrouperClientXmppHandler grouperClientXmppHandler = GrouperClientUtils
        .newInstance(handlerClass);
    grouperClientXmppHandler.handleIncremental(grouperClientXmppJob,
        groupName, GrouperClientUtils.extensionFromName(groupName),
         newList, oldList, grouperClientXmppSubject, eventType);
  }

}
