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
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.XppDriver;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob.XmppJobEventAction;


/**
 *
 */
public class GrouperClientXmppMain {

  /**
   * logger
   */
  private static Log log = GrouperClientUtils.retrieveLog(GrouperClientXmppMain.class);

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
          XmppMembershipChange xmppMembershipChange = (XmppMembershipChange)xstream().fromXML(body);
          
          //lets set the attribute map for the subject
          XmppSubject xmppSubject = xmppMembershipChange.getXmppSubject();
          xmppSubject.assignAttributeMap(xmppMembershipChange.getSubjectAttributeNames());
          //loop through jobs and see what matches
          boolean matches = false;
          for (GrouperClientXmppJob grouperClientXmppJob : GrouperClientXmppJob.retrieveXmppJobs()) {
            if (grouperClientXmppJob.getGroupNames().contains(xmppMembershipChange.getGroupName())) {
              matches = true;
              if (!grouperClientXmppJob.matches(xmppSubject)) {
                if (log.isDebugEnabled()) {
                  log.debug("skipping due to attribute or source: " + xmppMembershipChange.getGroupName() 
                      + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + xmppSubject.getId());
                }
                continue;
              }
              
              //see what type
              if (XmppJobEventAction.reload_group == grouperClientXmppJob.getEventAction()) {
                
                if (log.isDebugEnabled()) {
                  log.debug("performing a full reload on group: " + xmppMembershipChange.getGroupName() 
                      + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + xmppSubject.getId());
                }
                fullRefreshGroup(grouperClientXmppJob, xmppMembershipChange.getGroupName());
              
              } else if (XmppJobEventAction.incremental == grouperClientXmppJob.getEventAction()) {
                
                if (log.isDebugEnabled()) {
                  log.debug("performing an incremental reload on group: " + xmppMembershipChange.getGroupName() 
                      + " for job: " + grouperClientXmppJob.getJobName() + ", subject: " + xmppSubject.getId());
                }
                incrementalRefreshGroup(grouperClientXmppJob, xmppMembershipChange.getGroupName(), 
                    xmppSubject, xmppMembershipChange.getAction());
                
              } else {
                throw new RuntimeException("Not expecting event action: " + grouperClientXmppJob.getEventAction());
              }
              
              
            }
          }
          
          if (!matches) {
            if (log.isDebugEnabled()) {
              log.debug("couldnt find match for event: " + xmppMembershipChange.getGroupName() 
                  + ", subject: " + xmppSubject.getId() + ", action: " + xmppMembershipChange.getAction());
            }
          }
        } catch (RuntimeException re) {
          String messageXml = message == null ? null : message.toXML();
          log.error("Problem with message: " + messageXml, re);
          throw re;
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
  private static Map<String, List<XmppSubject>> groupMemberships = new HashMap<String, List<XmppSubject>>();
  
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

    List<XmppSubject> xmppSubjects = new ArrayList<XmppSubject>();

    for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
      XmppSubject xmppSubject = new XmppSubject(wsSubject, wsGetMembersResults.getSubjectAttributeNames());
      
      //filter if require sources
      if (!grouperClientXmppJob.matches(xmppSubject)) {
        continue;
      }
      
      xmppSubjects.add(xmppSubject);
    }

    if (log.isDebugEnabled()) {
      log.debug("Refreshing all for " + groupName + " found " + GrouperClientUtils.length(xmppSubjects) + " subjects");
    }

    Class<GrouperClientXmppHandler> handlerClass = GrouperClientUtils.forName(grouperClientXmppJob.getHandlerClass());
    GrouperClientXmppHandler grouperClientXmppHandler = GrouperClientUtils.newInstance(handlerClass);
    groupMemberships.put(groupName, xmppSubjects);
    grouperClientXmppHandler.handleAll(grouperClientXmppJob, groupName, GrouperClientUtils.extensionFromName(groupName), subjectAttributeNames, xmppSubjects);
    
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
    XmppSubject xmppSubject = new XmppSubject();
    xmppSubject.getAttribute().put("pennname", "mchyzer");
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
      JobDetail jobDetail = new JobDetail("fullRefresh_" + jobName, 
          null, MembershipFullRefreshJob.class);

      CronTrigger cronTrigger = new CronTrigger();
      String quartzCronString = grouperClientXmppJob.getFullRefreshQuartzCronString();
      try {
        cronTrigger.setCronExpression(quartzCronString);
      } catch (ParseException pe) {
        throw new RuntimeException("Problems parsing: '" + quartzCronString + "'", pe);
      }

      cronTrigger.setName("triggerFullRefresh_" + jobName);

      try {
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

  /** keep a reference here so we dont have to keep logging in */
  private static XMPPConnection xmppConnection = null;
  /** xstream object */
  private static XStream xStream = null;

  /**
   * get an xmpp connection
   * @return xmpp connection
   */
  private static synchronized XMPPConnection xmppConnection() {
    if (xmppConnection == null || !xmppConnection.isAuthenticated() || !xmppConnection.isConnected()) {
      try {
        if (xmppConnection != null) {
          try {
            xmppConnection.disconnect();
          } catch (Exception e) {
            //this is ok
          }
        }
        ConnectionConfiguration config = new ConnectionConfiguration(xmppServer(), xmppPort());
  
        config.setDebuggerEnabled(false);
        config.setReconnectionAllowed(true);
        
        config.setSASLAuthenticationEnabled(true);
        SASLAuthentication.supportSASLMechanism("PLAIN");
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        xmppConnection = new XMPPConnection(config);
        xmppConnection.connect();
  
        xmppConnection.login(xmppUser(), xmppPass(), xmppResource());
      } catch (XMPPException xe) {
        throw new RuntimeException(xe);
      }
    }
    return xmppConnection;
  }

  /**
   * xmpp pass (decrypted if file)
   * @return the pass
   */
  private static String xmppPass() {
    String pass = GrouperClientUtils.propertiesValue("grouperClient.xmpp.pass", true);
    return Morph.decryptIfFile(pass);
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
   * 
   * @return xstream
   */
  private static XStream xstream() {
    if (xStream == null) {
      xStream = new XStream(new XppDriver());
      
      //do javabean properties, not fields
      xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {
  
        /**
         * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean canConvert(Class type) {
          //see if one of our beans
          return type.getName().startsWith("edu.internet2");
        }
        
      }); 
      
      xStream.alias("XmppMembershipChange", XmppMembershipChange.class);
      xStream.alias("XmppSubject", XmppSubject.class);
  
    }
    return xStream;
  }

  /**
   * 
   * @param grouperClientXmppJob 
   * @param groupName
   * @param xmppSubject 
   * @param action ADD_MEMBER or REMOVE_MEMBER
   */
  @SuppressWarnings("unchecked")
  private static void incrementalRefreshGroup(GrouperClientXmppJob grouperClientXmppJob, 
      String groupName, XmppSubject xmppSubject, String action) {
    
    List<XmppSubject> oldList = groupMemberships.get(groupName);
    if (oldList == null) {
      throw new NullPointerException("Why is old list null????");
    }
    
    List<XmppSubject> newList = new ArrayList<XmppSubject>(oldList);
    
    if (GrouperClientUtils.equals(action, "ADD_MEMBER")) {
      if (!newList.contains(xmppSubject)) {
        newList.add(xmppSubject);
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Group " + groupName + " already contains subject: " + xmppSubject.getId());
        }
      }
    } else if (GrouperClientUtils.equals(action, "REMOVE_MEMBER")) {
      if (newList.contains(xmppSubject)) {
        int i=0;
        while (true) {
          if (!newList.remove(xmppSubject)) {
            break;
          }
          if (i++ > 100) {
            throw new RuntimeException("Time to live exceeded for group " + groupName + ", subject " + xmppSubject.getId());
          }
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Group " + groupName + " already doesnt contain subject: " + xmppSubject.getId());
        }
        
      }
    } else {
      throw new RuntimeException("Not expecting action: '" + action + "'");
    }
  
    if (log.isDebugEnabled()) {
      log.debug("Refreshing all for " + groupName + " was " + GrouperClientUtils.length(oldList) 
          + " and is now " + GrouperClientUtils.length(newList) + " subjects");
    }
    Class<GrouperClientXmppHandler> handlerClass = GrouperClientUtils.forName(grouperClientXmppJob.getHandlerClass());
    GrouperClientXmppHandler grouperClientXmppHandler = GrouperClientUtils.newInstance(handlerClass);
    groupMemberships.put(groupName, newList);
    grouperClientXmppHandler.handleIncremental(grouperClientXmppJob, 
        groupName, GrouperClientUtils.extensionFromName(groupName), 
        grouperClientXmppJob.getSubjectAttributeNames(), newList, oldList, xmppSubject, action);
    
  }

  
}
