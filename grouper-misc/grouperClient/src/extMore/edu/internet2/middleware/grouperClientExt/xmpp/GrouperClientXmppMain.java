/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.text.ParseException;
import java.util.ArrayList;
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
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.Expression;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.ExpressionFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlContext;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlHelper;
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

  /**
   * see if the esb event matches an EL filter.  Note the available objects are
   * event for the EsbEvent, and grouperUtil for the GrouperUtil class which has
   * a lot of utility methods
   * @param filterString
   * @param esbEvent
   * @return true if matches, false if doesnt
   */
  @SuppressWarnings("unchecked")
  public static boolean matchesFilter(EsbEvent esbEvent, String filterString) {
    try {
      Expression e = ExpressionFactory.createExpression(filterString);
      JexlContext jc = JexlHelper.createContext();
  
      jc.getVars().put("event", esbEvent);
      jc.getVars().put("grouperClientUtils", new GrouperClientUtils());
      return (Boolean) e.evaluate(jc);
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
  
        boolean debuggerEnabled = GrouperClientUtils.propertiesValueBoolean("grouperClient.xmpp.debuggerEnabled", false, false);
        
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
            + port + ",  user: " + user + ", pass not included, "
            //+ GrouperClientUtils.repeat("*", GrouperClientUtils.defaultString(pass).length()) 
            + ", resource: " + resource,  xe);
      }
    }
    return xmppConnection;
  }

  /**
   * xmpp pass (decrypted if file)
   * @return the pass
   */
  private static String xmppPass() {
    
    boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
        "encrypt.disableExternalFileLookup", false, true);
    
    //lets lookup if file
    String pass = GrouperClientUtils.propertiesValue("grouperClient.xmpp.pass", true);
    String passFromFile = GrouperClientUtils.readFromFileIfFile(pass, disableExternalFileLookup);
    
    if (!GrouperClientUtils.equals(pass, passFromFile)) {

      String encryptKey = GrouperClientUtils.propertiesValue("encrypt.key", true);
      encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
      passFromFile = new Crypto(encryptKey).decrypt(passFromFile);
      
    }
    
    return passFromFile;
  }

  /**
   * port to connect to, or 1522 as default 
   * @return port
   */
  private static int xmppPort() {
    return GrouperClientUtils.propertiesValueInt("grouperClient.xmpp.server.port", 1522, false);
  }

  /**
   * xmpp resource
   * @return the resource
   */
  private static String xmppResource() {
    return GrouperClientUtils.propertiesValue("grouperClient.xmpp.resource", false);
  }

  /**
   * xpp server to connect to
   * @return xmpp server
   */
  private static String xmppServer() {
    return GrouperClientUtils.propertiesValue("grouperClient.xmpp.server.host", true);
  }

  /**
   * xmpp user
   * @return the user
   */
  private static String xmppUser() {
    return GrouperClientUtils.propertiesValue("grouperClient.xmpp.user", true);
  }



  /**
   * connect to xmpp
   */
  private static void xmppConnect() {
    XMPPConnection theXmppConnection = xmppConnection();
    theXmppConnection.addPacketListener(new PacketListener() {
      
      @Override
      public void processPacket(Packet packet) {
        Message message = null;
        try {
          message = (Message)packet;
          if (log.isDebugEnabled()) {
            log.debug(message == null ? null : message.toXML());
          }
          String body = message.getBody();
          EsbEvents esbEvents = (EsbEvents)JsonUtils.jsonConvertFrom(body, EsbEvents.class);
          for (EsbEvent esbEvent : GrouperClientUtils.nonNull(esbEvents.getEsbEvent(), EsbEvent.class)) {
            //loop through jobs and see what matches
            for (GrouperClientXmppJob grouperClientXmppJob : GrouperClientXmppJob.retrieveXmppJobs()) {
              String elfilter = grouperClientXmppJob.getElfilter();
              Boolean matches = null;
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
              
              if (GrouperClientUtils.nonNull(grouperClientXmppJob.getGroupNames()).size() > 0) {
                if (grouperClientXmppJob.getGroupNames().contains(esbEvent.getGroupName())) {
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
              if (matches != null && !matches) {
                continue;
              }
              
              //see what type
              if (XmppJobEventAction.reload_group == grouperClientXmppJob.getEventAction()) {
                
                if (log.isDebugEnabled()) {
                  log.debug("performing a full reload on group: " + esbEvent.getGroupName()
                      + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + esbEvent.getSubjectId());
                }
                fullRefreshGroup(grouperClientXmppJob, esbEvent.getGroupName());
              
              } else if (XmppJobEventAction.incremental == grouperClientXmppJob.getEventAction()) {
                
                if (log.isDebugEnabled()) {
                  log.debug("performing an incremental reload on group: " + esbEvent.getGroupName()
                      + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + esbEvent.getSubjectId());
                }
                
                GrouperClientXmppSubject grouperClientXmppSubject = new GrouperClientXmppSubject(esbEvent);
                
                incrementalRefreshGroup(grouperClientXmppJob, esbEvent.getGroupName(), 
                    grouperClientXmppSubject, esbEvent.getEventType());
                
              } else {
                throw new RuntimeException("Not expecting event action: " + grouperClientXmppJob.getEventAction());
              }
              
            }
            
          }           
            
        } catch (Throwable re) {
          String messageXml = message == null ? null : message.toXML();
          log.error("Problem with message: " + messageXml, re);
          throw new RuntimeException(re);
        }
        
      }
    }, new PacketFilter() {
      
      @Override
      public boolean accept(Packet packet) {
        if (packet instanceof Message) {
          Message message = (Message)packet;
          Type type = message.getType();
          if (type == Type.chat && !GrouperClientUtils.isBlank(message.getBody())) {
            if (allowFromJabberIds.contains(message.getFrom())) {
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
  
//  /**
//   * 
//   * @return xstream
//   */
//  private static XStream xstream() {
//    if (xStream == null) {
//      xStream = new XStream(new XppDriver());
//      
//      //do javabean properties, not fields
//      xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {
//  
//        /**
//         * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
//         */
//        @SuppressWarnings("unchecked")
//        @Override
//        public boolean canConvert(Class type) {
//          //see if one of our beans
//          return type.getName().startsWith("edu.internet2");
//        }
//        
//      }); 
//      
//      xStream.alias("XmppMembershipChange", XmppMembershipChange.class);
//      xStream.alias("XmppSubject", XmppSubject.class);
//  
//    }
//    return xStream;
//  }

  /** allowed from jabber ids */
  private static Set<String> allowFromJabberIds = new HashSet<String>();
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    fullRefreshAll();
    
    String allowFroms = GrouperClientUtils.propertiesValue("grouperClient.xmpp.trustedMessagesFromJabberIds", true);
    allowFromJabberIds.addAll(GrouperClientUtils.splitTrimToList(allowFroms, ","));
    
    scheduleFullRefreshJobs();
    //note this doesnt return
    xmppLoop();
    
    
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
    String outputTemplate = GrouperClientUtils.propertiesValue("grouperClient.xmpp.job.myJobName.fileHandler.iteratorEl", true);
    outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
    GrouperClientXmppSubject xmppSubject = new GrouperClientXmppSubject();
    xmppSubject.getAttribute().put("loginid", "mchyzer");
    substituteMap.put("subject", xmppSubject);
    String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
    System.out.println(output);

  }
  
  /**
   * note, this doesnt return
   */
  private static void xmppLoop() {
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
          xmppConnect();
        }
      } catch (Exception e) {
        log.error("Problem with xmpp", e);
      }
      GrouperClientUtils.sleep(60000);
    }
  }

  /**
   * 
   */
  private static void scheduleFullRefreshJobs() {
    
    //loop through jobs
    for (GrouperClientXmppJob grouperClientXmppJob : GrouperClientXmppJob.retrieveXmppJobs()) {
      
      Scheduler scheduler = scheduler();
      String jobName = grouperClientXmppJob.getJobName();
      
      //note, in old versions of quartz, blank group cannot be used
      String quartzJobName = "fullRefresh_" + jobName;
      String jobGroup = Scheduler.DEFAULT_GROUP;
      
      JobDetail jobDetail = null;
      
      //try {
      //  jobDetail = scheduler.getJobDetail(jobName, jobGroup);
      //} catch (SchedulerException se) {
      //  throw new RuntimeException("Problem finding job: " + quartzJobName, se);
      //}
      
      if (jobDetail == null) {
        jobDetail = new JobDetail(quartzJobName, 
            jobGroup, MembershipFullRefreshJob.class);
        
      }
      
      //atlassian requires durable jobs...
      jobDetail.setDurability(true);
      
      boolean uniqueTriggerNames = GrouperClientUtils.propertiesValueBoolean("grouperClient.xmpp.uniqueQuartzTriggerNames", false, false);
      
      //in old versions of quartz, the trigger group cannot be null
      String triggerName = "triggerFullRefresh_" + jobName;
      
      if (uniqueTriggerNames) {
        triggerName += GrouperClientUtils.uniqueId();
      }
      
      CronTrigger cronTrigger = null;
      if (!uniqueTriggerNames) {
        try {
          cronTrigger = (CronTrigger)scheduler.getTrigger(triggerName, jobGroup);
        } catch (SchedulerException se) {
          throw new RuntimeException("Problem with trigger: " + jobName, se);
        }
      }
      if (cronTrigger == null) {
        cronTrigger = new CronTrigger(triggerName, jobGroup);
      }
      
      String quartzCronString = grouperClientXmppJob.getFullRefreshQuartzCronString();
      try {
        cronTrigger.setCronExpression(quartzCronString);
      } catch (ParseException pe) {
        throw new RuntimeException("Problems parsing: '" + quartzCronString + "'", pe);
      }

      try {
        //for persistent jobs, if already scheduled, will not be able to reschedule unless delete job
        try {
          if (!uniqueTriggerNames) {
            scheduler.unscheduleJob(triggerName, quartzJobName);
          }
        } catch (Exception e) {
          log.warn("Non fatal error unscheduling job", e);
        }
        try {
          scheduler.deleteJob(quartzJobName, jobGroup);
        } catch (Exception e) {
          log.warn("Non fatal error deleting job", e);
        }
        scheduler.scheduleJob(jobDetail, cronTrigger);
      } catch (SchedulerException se) {
        throw new RuntimeException("Problem with job: " + jobName, se);
      }

    }
    
  }
  
  /**
   * scheduler
   * @return scheduler
   */
  private static Scheduler scheduler() {
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
   * 
   * @param grouperClientXmppJob 
   * @param groupName
   * @param grouperClientXmppSubject 
   * @param eventType 
   */
  @SuppressWarnings("unchecked")
  private static void incrementalRefreshGroup(GrouperClientXmppJob grouperClientXmppJob, 
      String groupName, GrouperClientXmppSubject grouperClientXmppSubject, String eventType) {
    
    List<GrouperClientXmppSubject> oldList = groupMemberships.get(groupName);

    if (oldList == null) {
      //we need to get all
      //throw new NullPointerException("Why is old list null????");
      fullRefreshGroup(grouperClientXmppJob, groupName);
      
      oldList = groupMemberships.get(groupName);
    }

    if (oldList == null) {
      throw new NullPointerException("Why is old list null????");
    }

    List<GrouperClientXmppSubject> newList = new ArrayList<GrouperClientXmppSubject>(oldList);
    
    if (GrouperClientUtils.equals(eventType, "MEMBERSHIP_ADD")) {
      if (!newList.contains(grouperClientXmppSubject)) {
        newList.add(grouperClientXmppSubject);
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Group " + groupName + " already contains subject: " + grouperClientXmppSubject.getSubjectId());
        }
      }
      
    } else if (GrouperClientUtils.equals(eventType, "MEMBERSHIP_DELETE")) {
      if (newList.contains(grouperClientXmppSubject)) {
        int i=0;
        while (true) {
          if (!newList.remove(grouperClientXmppSubject)) {
            break;
          }
          if (i++ > 100) {
            throw new RuntimeException("Time to live exceeded for group " + groupName + ", subject " + grouperClientXmppSubject.getSubjectId());
          }
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Group " + groupName + " already doesnt contain subject: " + grouperClientXmppSubject.getSubjectId());
        }
        
      }
    } else {
      throw new RuntimeException("Not expecting action: '" + eventType + "'");
    }
  
    if (log.isDebugEnabled()) {
      log.debug("Refreshing incremental for " + groupName + " was " + GrouperClientUtils.length(oldList) 
          + " and is now " + GrouperClientUtils.length(newList) + " subjects");
    }
    Class<GrouperClientXmppHandler> handlerClass = GrouperClientUtils.forName(grouperClientXmppJob.getHandlerClass());
    GrouperClientXmppHandler grouperClientXmppHandler = GrouperClientUtils.newInstance(handlerClass);
    groupMemberships.put(groupName, newList);
    grouperClientXmppHandler.handleIncremental(grouperClientXmppJob, 
        groupName, GrouperClientUtils.extensionFromName(groupName), 
         newList, oldList, grouperClientXmppSubject, eventType);
    
  }

}
