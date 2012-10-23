/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.jivesoftware.smack.packet.Message;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import edu.internet2.middleware.grouperActivemq.config.GrouperActivemqConfig;
import edu.internet2.middleware.grouperClient.api.GcGetPermissionAssignments;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppMain;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppMessageHandler;

/**
 * keep track of all permissions
 */
public class GrouperActivemqPermissionsEngine implements Job, StatefulJob {

  /**
   * logger
   */
  private static Log log = GrouperClientUtils
      .retrieveLog(GrouperActivemqPermissionsEngine.class);

  /**
     * @param args
     */
  public static void main(String[] args) {

    log.debug("Starting cluster linux permissions daemon");

    //schedule full refresh job on startup
    performFullRefresh();

    scheduleQuartzJob();

    //do xmpp loop
    xmppLoop();

  }

  /**
   * schedule the full refresh job, e.g. nightly
   */
  private static void scheduleQuartzJob() {

    String jobName = "clusterLinuxFullRefreshJob";

    String quartzCronString = GrouperClientUtils.propertiesValue(
        "clusterLinux.fullRefreshQuartzCron", false);

    log.debug("Scheduling cluster linux permissions daemon for quartzCron string: "
        + quartzCronString);

    GrouperClientXmppMain.scheduleJob(jobName, quartzCronString, GrouperActivemqPermissionsEngine.class);

  }

  /** timer */
  private static Timer timer = null;

  /** timer scheduled for */
  private static long timerScheduledFor = -1;

  /**
   * do a full refresh in one minute (batch subsequent requests)
   */
  public static synchronized void scheduleFullRefresh() {

    //if it is already scheduled, then we are all good
    if (timer != null) {

      log.debug("Job is already scheduled at " + new Date(timerScheduledFor).toString()
          + ", exiting");

      return;
    }
    timer = new Timer(true);

    int timeInFuture = 1000 * 60;

    timerScheduledFor = System.currentTimeMillis() + timeInFuture;

    //schedule in 60 seconds
    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        performFullRefresh();
      }
    }, timeInFuture);
  }

  /**
   * base folder for grouper
   * @return the folder, e.g. school:apps:clusterLinux
   */
  public static String grouperFolderBase() {
    return GrouperClientUtils.propertiesValue("clusterLinux.grouperFolderBase", true);
  }

  /**
   * do a full refresh in one minute (batch subsequent requests)
   */
  public static synchronized void performFullRefresh() {

    try {
      //let another timer be scheduled, whether from schedule or xmpp
      timer = null;

      GcGetPermissionAssignments gcGetPermissionAssignments = new GcGetPermissionAssignments();
      
      {
        String nameOfAttributeDefsString = GrouperActivemqConfig.retrieveConfig().propertyValueStringRequired("grouperActivemq.roleName");
        
        List<String> namesOfAttributeDefs = GrouperClientUtils.splitTrimToList(nameOfAttributeDefsString, ","); 

        for (String nameOfAttributeDef : namesOfAttributeDefs) {
          gcGetPermissionAssignments.addAttributeDefName(nameOfAttributeDef);
        }
        
      }
      
      {
        
        String theRoleNamesString = GrouperActivemqConfig.retrieveConfig().propertyValueStringRequired("grouperActivemq.roleNames");
  
        List<String> roleNames = GrouperClientUtils.splitTrimToList(theRoleNamesString, ","); 
        
        for (String roleName : roleNames) {
          gcGetPermissionAssignments.addRoleName(roleName);
        }
        
      }
      Map<String, String> sourceToLoginIdAttributeName = new HashMap<String, String>();

      for (String sourceId : sourceToLoginIdAttributeName.keySet()) {
        String loginAttributeName = sourceToLoginIdAttributeName.get(sourceId);
        gcGetPermissionAssignments.addSubjectAttributeName(loginAttributeName);
        //hmm, cant add source
      }
      
      WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = gcGetPermissionAssignments.execute();

      WsSubject[] wsSubjects = wsGetPermissionAssignmentsResults.getWsSubjects();

      //lets make the permissions file
      StringBuilder fileContents = new StringBuilder(
          "# File automatically generated from PennGroups...\n\n");

      //lets sort these so they are easier to manage
      Set<String> lines = new TreeSet<String>();

      if (log.isDebugEnabled()) {
        log.debug("Received "
              + GrouperClientUtils.length(wsGetPermissionAssignmentsResults
                  .getWsPermissionAssigns())
              + " permission entries from Grouper");
      }

      for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(
            wsGetPermissionAssignmentsResults.getWsPermissionAssigns(),
          WsPermissionAssign.class)) {

        //only worried about personal access at this time
        if (!GrouperClientUtils.equals(wsPermissionAssign.getSourceId(), "pennperson")) {
          continue;
        }

        //get the subject
        WsSubject wsSubject = retrieveSubject(
              wsSubjects, wsPermissionAssign.getSourceId(),
              wsPermissionAssign.getSubjectId());

        if (wsSubject == null) {
          throw new RuntimeException("Why is wsSubject null??? "
              + wsPermissionAssign.getSourceId()
              + wsPermissionAssign.getSubjectId());
        }

        String pennname = GrouperClientUtils.subjectAttributeValue(wsSubject,
              wsGetPermissionAssignmentsResults.getSubjectAttributeNames(), "PENNNAME");

        //not sure why there wouldnt be a pennname, but if not, then skip

        if (GrouperClientUtils.isBlank(pennname)) {
          continue;
        }

        //ok, we have pennname, extension, action, lets write the line
        String extension = GrouperClientUtils.extensionFromName(wsPermissionAssign
            .getAttributeDefNameName());
        String action = wsPermissionAssign.getAction();

        lines.add("clusterLinux__" + pennname + "__" + extension + "__" + action
            + "=true");

      }

      for (String line : lines) {
        fileContents.append(line).append("\n");
      }

      if (log.isDebugEnabled()) {
        log.debug("Generated file has "
              + GrouperClientUtils.length(lines)
              + " lines");
      }

      File fileToSave = new File(GrouperClientUtils.propertiesValue(
          "clusterLinux.fileToSave", true));

      //save this to file (try 3 times)
      for (int i = 0; i < 3; i++) {

        try {

          boolean updated = GrouperClientUtils.saveStringIntoFile(fileToSave,
              fileContents.toString(), true, true);
          if (updated) {
            log.debug("File: " + fileToSave.getAbsolutePath()
                + " was saved since there were updates from Grouper");
          } else {
            log.debug("File: " + fileToSave.getAbsolutePath()
                + " was not saved since there were no changes from Grouper");
          }

          //we are done
          break;
        } catch (Exception e) {
          log.error("Error saving file", e);
        }
        GrouperClientUtils.sleep(2000);

      }

    } catch (Exception e) {
      log.error("Error in full refresh", e);
    }

  }

  /**
   * this will be run when the quertz nightly job fires
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  // @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    String jobName = null;
    try {
      jobName = context.getJobDetail().getName();
      if (log.isDebugEnabled()) {
        log.debug("Scheduling full refresh on job: " + jobName);
      }
      scheduleFullRefresh();
    } catch (RuntimeException re) {
      log.error("Error in job: " + jobName, re);
      throw re;
    }

  }

  /**
   * connect to xmpp, listen for messages from a certain sender (the grouper server)
   */
  private static void xmppLoop() {

    GrouperClientXmppMain.xmppLoop(new GrouperClientXmppMessageHandler() {

      @Override
      public void handleMessage(Message message) {

        log.debug("Received message: " + message.getBody());

        //whatever message we get, we know it is from the right sender based on
        //config, so just schedule a full refresh a minute from now if its not already scheduled
        scheduleFullRefresh();

      }
    });

  }

  /**
   * lookup a subject by subject id and source id
   * @param subjects
   * @param sourceId
   * @param subjectId
   * @return probably shouldnt be null, but if cant be found, then will be null
   */
  public static WsSubject retrieveSubject(WsSubject[] subjects, String sourceId,
      String subjectId) {

    for (WsSubject wsSubject : GrouperClientUtils.nonNull(subjects, WsSubject.class)) {
      if (GrouperClientUtils.equals(sourceId, wsSubject.getSourceId())
           && GrouperClientUtils.equals(subjectId, wsSubject.getId())) {
        return wsSubject;
      }
    }
    return null;
  }

}
